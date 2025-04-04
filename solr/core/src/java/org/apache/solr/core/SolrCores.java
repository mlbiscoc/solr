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
package org.apache.solr.core;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.ExecutorUtil;
import org.apache.solr.common.util.SolrNamedThreadFactory;
import org.apache.solr.logging.MDCLoggingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** AKA CoreManager: Holds/manages {@link SolrCore}s within {@link CoreContainer}. */
public class SolrCores {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // for locking around manipulating any of the core maps.
  protected final Object modifyLock = new Object();

  private final Map<String, SolrCore> cores = new LinkedHashMap<>(); // For "permanent" cores

  // These descriptors, once loaded, will _not_ be unloaded, i.e. they are not "transient".
  private final Map<String, CoreDescriptor> residentDescriptors = new LinkedHashMap<>();

  private final CoreContainer container;

  private final Set<String> currentlyLoadingCores =
      Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

  // This map will hold objects that are being currently operated on. The core (value) may be null
  // in the case of initial load. The rule is, never to any operation on a core that is currently
  // being operated upon.
  private final Set<String> pendingCoreOps = new HashSet<>();

  // Due to the fact that closes happen potentially whenever anything is _added_ to the transient
  // core list, we need to essentially queue them up to be handled via pendingCoreOps.
  private final List<SolrCore> pendingCloses = new ArrayList<>();

  public static SolrCores newSolrCores(CoreContainer coreContainer) {
    final int transientCacheSize = coreContainer.getConfig().getTransientCacheSize();
    if (transientCacheSize > 0) {
      return new TransientSolrCores(coreContainer, transientCacheSize);
    } else {
      return new SolrCores(coreContainer);
    }
  }

  SolrCores(CoreContainer container) {
    this.container = container;
  }

  public void addCoreDescriptor(CoreDescriptor p) {
    synchronized (modifyLock) {
      residentDescriptors.put(p.getName(), p);
    }
  }

  public void removeCoreDescriptor(CoreDescriptor p) {
    synchronized (modifyLock) {
      residentDescriptors.remove(p.getName());
    }
  }

  // We are shutting down. You can't hold the lock on the various lists of cores while they shut
  // down, so we need to make a temporary copy of the names and shut them down outside the lock.
  protected void close() {
    waitForLoadingCoresToFinish(30 * 1000);

    // It might be possible for one of the cores to move from one list to another while we're
    // closing them. So loop through the lists until they're all empty. In particular, the core
    // could have moved from the transient list to the pendingCloses list.
    while (true) {
      Collection<SolrCore> coreList = new ArrayList<>();

      synchronized (modifyLock) {
        // remove all loaded cores; add to our working list.
        for (String name : getLoadedCoreNames()) {
          final var core = remove(name);
          if (core != null) { // maybe in pendingCloses due to transient core eviction
            coreList.add(core);
          }
        }

        coreList.addAll(pendingCloses);
        pendingCloses.clear();
      }

      if (coreList.isEmpty()) {
        break;
      }

      ExecutorService coreCloseExecutor =
          ExecutorUtil.newMDCAwareFixedThreadPool(
              Integer.MAX_VALUE, new SolrNamedThreadFactory("coreCloseExecutor"));
      try {
        for (SolrCore core : coreList) {
          coreCloseExecutor.execute(
              () -> {
                MDCLoggingContext.setCore(core);
                try {
                  core.close();
                } catch (Throwable e) {
                  log.error("Error shutting down core", e);
                  if (e instanceof Error) {
                    throw (Error) e;
                  }
                } finally {
                  MDCLoggingContext.clear();
                }
              });
        }
      } finally {
        ExecutorUtil.shutdownAndAwaitTermination(coreCloseExecutor);
      }
    }
  }

  // Returns the old core if there was a core of the same name.
  // WARNING! This should be the _only_ place you put anything into the list of transient cores!
  public SolrCore putCore(CoreDescriptor cd, SolrCore core) {
    synchronized (modifyLock) {
      addCoreDescriptor(cd); // cd must always be registered if we register a core
      return cores.put(cd.getName(), core);
    }
  }

  /**
   * @return A list of "permanent" cores, i.e. cores that may not be swapped out and are currently
   *     loaded.
   *     <p>A core may be non-transient but still lazily loaded. If it is "permanent" and lazy-load
   *     _and_ not yet loaded it will _not_ be returned by this call.
   *     <p>This list is a new copy, it can be modified by the caller (e.g. it can be sorted).
   *     <p>Note: This is one of the places where SolrCloud is incompatible with Transient Cores.
   *     This call is used in cancelRecoveries, transient cores don't participate.
   */
  @Deprecated
  public List<SolrCore> getCores() {
    synchronized (modifyLock) {
      return new ArrayList<>(cores.values());
    }
  }

  /**
   * Gets the cores that are currently loaded, i.e. cores that have 1: loadOnStartup=true and are
   * either not-transient or, if transient, have been loaded and have not been aged out 2:
   * loadOnStartup=false and have been loaded but either non-transient or have not been aged out.
   *
   * <p>Put another way, this will not return any names of cores that are lazily loaded but have not
   * been called for yet or are transient and either not loaded or have been swapped out.
   *
   * @return An unsorted list. This list is a new copy, it can be modified by the caller (e.g. it
   *     can be sorted).
   */
  public List<String> getLoadedCoreNames() {
    synchronized (modifyLock) {
      return new ArrayList<>(cores.keySet());
    }
  }

  /**
   * Gets a collection of all cores names, loaded and unloaded. For efficiency, prefer to check
   * {@link #getCoreDescriptor(String)} != null instead of {@link
   * #getAllCoreNames()}.contains(String)
   *
   * @return An unsorted list. This list is a new copy, it can be modified by the caller (e.g. it
   *     can be sorted).
   */
  public List<String> getAllCoreNames() {
    synchronized (modifyLock) {
      return new ArrayList<>(residentDescriptors.keySet());
    }
  }

  /**
   * Gets the number of currently loaded permanent (non transient) cores. Faster equivalent for
   * {@link #getCores()}.size().
   */
  public int getNumLoadedPermanentCores() {
    synchronized (modifyLock) {
      return cores.size();
    }
  }

  /** Gets the number of currently loaded transient cores. */
  public int getNumLoadedTransientCores() {
    // TODO; this metric ought to simply not exist here
    return 0;
  }

  /** Gets the number of unloaded cores, including permanent and transient cores. */
  public int getNumUnloadedCores() {
    synchronized (modifyLock) {
      return residentDescriptors.size() - cores.size();
    }
  }

  /**
   * Gets the total number of cores, including permanent and transient cores, loaded and unloaded
   * cores. Faster equivalent for {@link #getAllCoreNames()}.size().
   */
  public int getNumAllCores() {
    synchronized (modifyLock) {
      return residentDescriptors.size();
    }
  }

  public void swap(String n0, String n1) {
    synchronized (modifyLock) {
      SolrCore c0 = cores.get(n0);
      SolrCore c1 = cores.get(n1);
      // TODO DWS: honestly this doesn't appear to work properly unless the core is loaded
      if (c0 == null) { // Might be an unloaded transient core
        c0 = container.getCore(n0);
        if (c0 == null) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n0);
        }
      }
      if (c1 == null) { // Might be an unloaded transient core
        c1 = container.getCore(n1);
        if (c1 == null) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n1);
        }
      }
      // When we swap the cores, we also need to swap the associated core descriptors. Note, this
      // changes the name of the coreDescriptor by virtue of the c-tor
      CoreDescriptor cd1 = c1.getCoreDescriptor();
      addCoreDescriptor(new CoreDescriptor(n1, c0.getCoreDescriptor()));
      addCoreDescriptor(new CoreDescriptor(n0, cd1));
      cores.put(n0, c1);
      cores.put(n1, c0);
      c0.setName(n1);
      c1.setName(n0);

      container
          .getMetricManager()
          .swapRegistries(
              c0.getCoreMetricManager().getRegistryName(),
              c1.getCoreMetricManager().getRegistryName());
    }
  }

  public SolrCore remove(String name) {
    synchronized (modifyLock) {
      return cores.remove(name);
    }
  }

  public SolrCore getCoreFromAnyList(String name, boolean incRefCount) {
    return getCoreFromAnyList(name, incRefCount, null);
  }

  /* If you don't increment the reference count, someone could close the core before you use it. */
  public SolrCore getCoreFromAnyList(String name, boolean incRefCount, UUID coreId) {
    synchronized (modifyLock) {
      SolrCore core = getLoadedCoreWithoutIncrement(name);

      if (core != null && coreId != null && !coreId.equals(core.uniqueId)) return null;

      if (core != null && incRefCount) {
        core.open();
      }

      return core;
    }
  }

  /** (internal) Return a core that is already loaded, if it is. NOT incremented! */
  protected SolrCore getLoadedCoreWithoutIncrement(String name) {
    synchronized (modifyLock) {
      return cores.get(name);
    }
  }

  // See SOLR-5366 for why the UNLOAD command needs to know whether a core is actually loaded or
  // not, it might have to close the core. However, there's a race condition. If the core happens to
  // be in the pending "to close" queue, we should NOT close it in unload core.
  public boolean isLoadedNotPendingClose(String name) {
    synchronized (modifyLock) {
      if (!isLoaded(name)) {
        return false;
      }
      // Check pending
      for (SolrCore core : pendingCloses) {
        if (core.getName().equals(name)) {
          return false;
        }
      }

      return true;
    }
  }

  public boolean isLoaded(String name) {
    synchronized (modifyLock) {
      return cores.containsKey(name);
    }
  }

  /** The core is currently loading, unloading, or reloading. */
  protected boolean hasPendingCoreOps(String name) {
    synchronized (modifyLock) {
      return pendingCoreOps.contains(name);
    }
  }

  // Wait here until any pending operations (load, unload or reload) are completed on this core.
  public SolrCore waitAddPendingCoreOps(String name) {

    // Keep multiple threads from operating on a core at one time.
    synchronized (modifyLock) {
      boolean pending;
      do { // Are we currently doing anything to this core? Loading, unloading, reloading?
        pending = pendingCoreOps.contains(name); // wait for the core to be done being operated upon
        if (!pending) { // Linear list, but shouldn't be too long
          for (SolrCore core : pendingCloses) {
            if (core.getName().equals(name)) {
              pending = true;
              break;
            }
          }
        }
        if (container.isShutDown()) {
          // Just stop already.
          // Seems best to throw a SolrException if shutting down, because returning any value,
          // including null, would mean the waiting is complete.
          throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Server is shutting down");
        }

        if (pending) {
          try {
            modifyLock.wait();
          } catch (InterruptedException e) {
            // Seems best to throw a SolrException if interrupted, because returning any value,
            // including null, would mean the waiting is complete.
            Thread.currentThread().interrupt();
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
          }
        }
      } while (pending);
      // We _really_ need to do this within the synchronized block!
      if (!pendingCoreOps.add(name)) {
        log.warn("Replaced an entry in pendingCoreOps {}, we should not be doing this", name);
      }
      // we might have been _unloading_ the core, so return the core if it was loaded.
      return getCoreFromAnyList(name, false);
    }
  }

  // We should always be removing the first thing in the list with our name! The idea here is to NOT
  // do anything on any core while some other operation is working on that core.
  public void removeFromPendingOps(String name) {
    synchronized (modifyLock) {
      if (!pendingCoreOps.remove(name)) {
        log.warn("Tried to remove core {} from pendingCoreOps and it wasn't there. ", name);
      }
      modifyLock.notifyAll();
    }
  }

  public Object getModifyLock() {
    return modifyLock;
  }

  // Be a little careful. We don't want to either open or close a core unless it's _not_ being
  // opened or closed by another thread. So within this lock we'll walk along the list of pending
  // closes until we find something NOT in the list of threads currently being loaded or reloaded.
  // The "usual" case will probably return the very first one anyway.
  public SolrCore getCoreToClose() {
    synchronized (modifyLock) {
      for (SolrCore core : pendingCloses) {
        if (!pendingCoreOps.contains(core.getName())) {
          pendingCoreOps.add(core.getName());
          pendingCloses.remove(core);
          return core;
        }
      }
    }
    return null;
  }

  /**
   * Return the CoreDescriptor corresponding to a given core name. Blocks if the SolrCore is still
   * loading until it is ready.
   *
   * @param coreName the name of the core
   * @return the CoreDescriptor
   */
  public CoreDescriptor getCoreDescriptor(String coreName) {
    synchronized (modifyLock) {
      return residentDescriptors.get(coreName);
    }
  }

  /**
   * Get the CoreDescriptors for every {@link SolrCore} managed here (permanent and transient,
   * loaded and unloaded).
   *
   * @return An unordered list copy. This list can be modified by the caller (e.g. sorted).
   */
  public List<CoreDescriptor> getCoreDescriptors() {
    synchronized (modifyLock) {
      return new ArrayList<>(residentDescriptors.values());
    }
  }

  // cores marked as loading will block on getCore
  public void markCoreAsLoading(CoreDescriptor cd) {
    synchronized (modifyLock) {
      currentlyLoadingCores.add(cd.getName());
    }
  }

  // cores marked as loading will block on getCore
  public void markCoreAsNotLoading(CoreDescriptor cd) {
    synchronized (modifyLock) {
      currentlyLoadingCores.remove(cd.getName());
    }
  }

  // returns when no cores are marked as loading
  public void waitForLoadingCoresToFinish(long timeoutMs) {
    long time = System.nanoTime();
    long timeout = time + TimeUnit.NANOSECONDS.convert(timeoutMs, TimeUnit.MILLISECONDS);
    synchronized (modifyLock) {
      while (!currentlyLoadingCores.isEmpty()) {
        try {
          modifyLock.wait(500);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        if (System.nanoTime() >= timeout) {
          log.warn("Timed out waiting for SolrCores to finish loading.");
          break;
        }
      }
    }
  }

  // returns when core is finished loading, throws exception if no such core loading or loaded
  public void waitForLoadingCoreToFinish(String core, long timeoutMs) {
    long time = System.nanoTime();
    long timeout = time + TimeUnit.NANOSECONDS.convert(timeoutMs, TimeUnit.MILLISECONDS);
    synchronized (modifyLock) {
      while (isCoreLoading(core)) {
        try {
          modifyLock.wait(500);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        if (System.nanoTime() >= timeout) {
          log.warn("Timed out waiting for SolrCore, {},  to finish loading.", core);
          break;
        }
      }
    }
  }

  public boolean isCoreLoading(String name) {
    return currentlyLoadingCores.contains(name);
  }

  public void queueCoreToClose(SolrCore coreToClose) {
    synchronized (modifyLock) {
      pendingCloses.add(coreToClose); // Essentially just queue this core up for closing.
      modifyLock.notifyAll(); // Wakes up closer thread too
    }
  }
}
