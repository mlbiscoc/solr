/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.request.SolrQueryRequest;

/** */
public class QueryUtils {

  /** return true if this query has no positive components */
  public static boolean isNegative(Query q) {
    if (!(q instanceof BooleanQuery bq)) return false;
    Collection<BooleanClause> clauses = bq.clauses();
    if (clauses.size() == 0) return false;
    for (BooleanClause clause : clauses) {
      if (!clause.isProhibited()) return false;
    }
    return true;
  }

  /**
   * Recursively unwraps the specified query to determine whether it is capable of producing a score
   * that varies across different documents. Returns true if this query is not capable of producing
   * a varying score (i.e., it is a constant score query).
   */
  public static boolean isConstantScoreQuery(Query q) {
    while (true) {
      if (q instanceof BoostQuery) {
        q = ((BoostQuery) q).getQuery();
      } else if (q instanceof WrappedQuery) {
        q = ((WrappedQuery) q).getWrappedQuery();
      } else if (q instanceof ConstantScoreQuery) {
        return true;
      } else if (q instanceof MatchAllDocsQuery) {
        return true;
      } else if (q instanceof MatchNoDocsQuery) {
        return true;
      } else if (q instanceof DocSetQuery) {
        return true;
      } else if (q instanceof BooleanQuery) {
        // NOTE: this check can be very simple because:
        //  1. there's no need to check `q == clause.getQuery()` because BooleanQuery is final, with
        //     a builder that prevents direct loops.
        //  2. we don't bother recursing to second-guess a nominally "scoring" clause that actually
        //     wraps a constant-score query.
        return ((BooleanQuery) q).clauses().stream().noneMatch(BooleanClause::isScoring);
      } else {
        return false;
      }
    }
  }

  public static final int NO_PREFIX_QUERY_LENGTH_LIMIT = -1;

  /**
   * Validates that a provided prefix query obeys any limits (if configured) on the minimum
   * allowable prefix size
   *
   * <p>The limit is retrieved from the provided QParser (see {@link
   * QParser#getPrefixQueryMinPrefixLength()} for the default implementation).
   *
   * @param parser the QParser used to parse the query being validated. No limit will be enforced if
   *     'null'
   * @param query the query to validate. Limits will only be enforced if this is a {@link
   *     PrefixQuery}
   * @param prefix a String term included in the provided query. Its size is compared against the
   *     configured limit
   */
  public static void ensurePrefixQueryObeysMinimumPrefixLength(
      QParser parser, Query query, String prefix) {
    if (!(query instanceof PrefixQuery)) {
      return;
    }

    final var minPrefixLength =
        parser != null ? parser.getPrefixQueryMinPrefixLength() : NO_PREFIX_QUERY_LENGTH_LIMIT;
    if (minPrefixLength == NO_PREFIX_QUERY_LENGTH_LIMIT) {
      return;
    }

    if (prefix.length() < minPrefixLength) {
      final var message =
          String.format(
              Locale.ROOT,
              "Query [%s] does not meet the minimum prefix length [%d] (actual=[%d]).  Please try with a larger prefix, or adjust %s in your solrconfig.xml",
              query,
              minPrefixLength,
              prefix.length(),
              SolrConfig.MIN_PREFIX_QUERY_TERM_LENGTH);
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, message);
    }
  }

  /**
   * Returns the original query if it was already a positive query, otherwise return the negative of
   * the query (i.e., a positive query).
   *
   * <p>Example: both id:10 and id:-10 will return id:10
   *
   * <p>The caller can tell the sign of the original by a reference comparison between the original
   * and returned query.
   *
   * @param q Query to create the absolute version of
   * @return Absolute version of the Query
   */
  public static Query getAbs(Query q) {
    if (q instanceof BoostQuery bq) {
      Query subQ = bq.getQuery();
      Query absSubQ = getAbs(subQ);
      if (absSubQ.equals(subQ)) return q;
      return new BoostQuery(absSubQ, bq.getBoost());
    }

    if (q instanceof WrappedQuery) {
      Query subQ = ((WrappedQuery) q).getWrappedQuery();
      Query absSubQ = getAbs(subQ);
      if (absSubQ.equals(subQ)) return q;
      return new WrappedQuery(absSubQ);
    }

    if (!(q instanceof BooleanQuery bq)) return q;

    Collection<BooleanClause> clauses = bq.clauses();
    if (clauses.size() == 0) return q;

    for (BooleanClause clause : clauses) {
      if (!clause.isProhibited()) return q;
    }

    if (clauses.size() == 1) {
      // if only one clause, dispense with the wrapping BooleanQuery
      Query negClause = clauses.iterator().next().getQuery();
      // we shouldn't need to worry about adjusting the boosts since the negative
      // clause would have never been selected in a positive query, and hence would
      // not contribute to a score.
      return negClause;
    } else {
      BooleanQuery.Builder newBqB = new BooleanQuery.Builder();
      // ignore minNrShouldMatch... it doesn't make sense for a negative query

      // the inverse of -a -b is a OR b
      for (BooleanClause clause : clauses) {
        newBqB.add(clause.getQuery(), BooleanClause.Occur.SHOULD);
      }
      return newBqB.build();
    }
  }

  /** Makes negative queries suitable for querying by lucene. */
  public static Query makeQueryable(Query q) {
    if (q instanceof WrappedQuery) {
      return makeQueryable(((WrappedQuery) q).getWrappedQuery());
    }
    return isNegative(q) ? fixNegativeQuery(q) : q;
  }

  /**
   * Fixes a negative query by adding a MatchAllDocs query clause. The query passed in *must* be a
   * negative query.
   */
  public static Query fixNegativeQuery(Query q) {
    float boost = 1f;
    if (q instanceof BoostQuery bq) {
      boost = bq.getBoost();
      q = bq.getQuery();
    }
    BooleanQuery bq = (BooleanQuery) q;
    BooleanQuery.Builder newBqB = new BooleanQuery.Builder();
    newBqB.setMinimumNumberShouldMatch(bq.getMinimumNumberShouldMatch());
    for (BooleanClause clause : bq) {
      newBqB.add(clause);
    }
    newBqB.add(new MatchAllDocsQuery(), Occur.MUST);
    BooleanQuery newBq = newBqB.build();
    return new BoostQuery(newBq, boost);
  }

  /**
   * @lucene.experimental throw exception if max boolean clauses are exceeded
   */
  public static BooleanQuery build(BooleanQuery.Builder builder, QParser parser) {

    int configuredMax =
        parser != null
            ? parser.getReq().getCore().getSolrConfig().booleanQueryMaxClauseCount
            : IndexSearcher.getMaxClauseCount();
    BooleanQuery bq = builder.build();
    if (bq.clauses().size() > configuredMax) {
      throw new SolrException(
          SolrException.ErrorCode.BAD_REQUEST,
          "Too many clauses in boolean query: encountered="
              + bq.clauses().size()
              + " configured in solrconfig.xml via maxBooleanClauses="
              + configuredMax);
    }
    return bq;
  }

  /**
   * Combines a scoring query with a non-scoring (filter) query. If both parameters are null then
   * return a {@link MatchAllDocsQuery}. If only {@code scoreQuery} is present then return it. If
   * only {@code filterQuery} is present then return it wrapped with constant scoring. If neither
   * are null then we combine with a BooleanQuery.
   */
  public static Query combineQueryAndFilter(Query scoreQuery, Query filterQuery) {
    // check for *:* is simple and avoids needless BooleanQuery wrapper even though BQ.rewrite
    // optimizes this away
    if (scoreQuery == null || scoreQuery instanceof MatchAllDocsQuery) {
      if (filterQuery == null) {
        return new MatchAllDocsQuery(); // default if nothing -- match everything
      } else {
        /*
        NOTE: we _must_ wrap filter in a ConstantScoreQuery (default score `1f`) in order to
        guarantee score parity with the actual user-specified scoreQuery (i.e., MatchAllDocsQuery).
        This should only matter if score is _explicitly_ requested to be returned, but we don't know
        that here, and it's probably not worth jumping through the necessary hoops simply to avoid
        wrapping in the case where `true==isConstantScoreQuery(filterQuery)`
         */
        return new ConstantScoreQuery(filterQuery);
      }
    } else {
      if (filterQuery == null || filterQuery instanceof MatchAllDocsQuery) {
        return scoreQuery;
      } else {
        return new BooleanQuery.Builder()
            .add(scoreQuery, Occur.MUST)
            .add(filterQuery, Occur.FILTER)
            .build();
      }
    }
  }

  /**
   * Parse the filter queries in Solr request
   *
   * @param req Solr request
   * @return and array of Query. If the request does not contain filter queries, returns an empty
   *     list.
   * @throws SyntaxError if an error occurs during parsing
   */
  public static List<Query> parseFilterQueries(SolrQueryRequest req) throws SyntaxError {

    String[] filterQueriesStr = req.getParams().getParams(CommonParams.FQ);

    if (filterQueriesStr != null) {
      List<Query> filters = new ArrayList<>(filterQueriesStr.length);
      for (String fq : filterQueriesStr) {
        if (fq != null && fq.trim().length() != 0) {
          QParser fqp = QParser.getParser(fq, req);
          fqp.setIsFilter(true);
          Query query = fqp.getQuery();
          filters.add(query);
        }
      }
      return filters;
    }

    return Collections.emptyList();
  }

  /**
   * Returns a Set containing all of the Queries in the designated SolrQueryRequest possessing a tag
   * in the provided list of desired tags. The Set uses reference equality so, for example, it will
   * have 2 elements if the caller requests the tag "t1" for a request where
   * "fq={!tag=t1}a:b&amp;fq={!tag=t1}a:b". The Set will be empty (not null) if there are no
   * matches.
   *
   * <p>This method assumes that the provided SolrQueryRequest's context has been populated with a
   * "tags" entry, which should be a Map from a tag name to a Collection of QParsers. In general,
   * the "tags" entry will not be present until the QParsers have been instantiated, for example via
   * QueryComponent.prepare()
   *
   * @param req Solr request
   * @param desiredTags the tags to look for
   * @return Set of Queries in the given SolrQueryRequest possessing any of the desiredTags
   */
  public static Set<Query> getTaggedQueries(SolrQueryRequest req, Collection<String> desiredTags) {
    Map<?, ?> tagMap = (Map<?, ?>) req.getContext().get("tags");

    if (tagMap == null || tagMap.isEmpty() || desiredTags == null || desiredTags.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Query> taggedQueries = Collections.newSetFromMap(new IdentityHashMap<>());

    for (String tagName : desiredTags) {
      Object tagVal = tagMap.get(tagName);
      if (!(tagVal instanceof Collection)) continue;
      for (Object obj : (Collection<?>) tagVal) {
        if (!(obj instanceof QParser qParser)) continue;
        Query query;
        try {
          query = qParser.getQuery();
        } catch (SyntaxError syntaxError) {
          // should not happen since we should only be retrieving a previously parsed query
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, syntaxError);
        }
        taggedQueries.add(query);
      }
    }

    return taggedQueries;
  }
}
