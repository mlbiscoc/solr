package org.apache.solr.update;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LeafReader;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomIndexReaderWarmer implements IndexWriter.IndexReaderWarmer {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final SolrCore core;

  public CustomIndexReaderWarmer(SolrCore core) {
    this.core = core;
  }

  @Override
  public void warm(LeafReader reader) throws IOException {
    log.info(
        "Lets warm the indexFingerprint on this newly merged segment before we start searching");
//    core.getIndexFingerprintForASegment(core.getSearcher().get(), reader, Long.MAX_VALUE);
  }
}
