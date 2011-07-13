package org.infinispansupport.hotrodextenssion.tx;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.config.Configuration;
import org.infinispansupport.hotrodextenssion.tx.ops.ClearOperation;
import org.infinispansupport.hotrodextenssion.tx.ops.Operation;
import org.infinispansupport.hotrodextenssion.tx.ops.PutOperation;
import org.infinispansupport.hotrodextenssion.tx.ops.RemoveOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Sample usage:
 * <pre>
 * <code>
 *    RemoteCacheManager rcm = getRcm();//somehow...
 *    RemoteCache remoteCache = rcm.getCache("aCache");
 *    BatchEnabledRemoteCache berm = new BatchEnabledRemoteCache(remoteCache);
 *    berm.startBatch();
 *    berm.put("k", "v");
 *    berm.remove("k2");
 *    berm.put("k3", "v3");
 *    berm.endBatch(true);//when this method returns the transaction is committed
 *    assert remoteCache.get("k").equals("v");
 * </code>
 * </pre>
 * Important: this class is NOT thread safe and should not be used from more than a thread at a time. If multiple
 * threads want to run transactions concurrently on top of the same {@link RemoteCache}, each such thread should build its own
 * instance of BatchEnabledRemoteCache.
 *
 * @author Mircea Markus
 */
public class BatchEnabledRemoteCache<K, V> extends AbstractBatchEnabledRemoteCache<K, V> {

   private List<Operation> transaction = new ArrayList<Operation>();

   public BatchEnabledRemoteCache(RemoteCache<K, V> original) {
      super(original);
   }

   public Configuration getConfiguration() {
      return original.getConfiguration();
   }

   public boolean startBatch() {
      if (batchStarted) return false;
      batchStarted = true;
      return true;
   }

   public void endBatch(boolean commit) {
      if (!batchStarted) throw new IllegalStateException("Cannot end a batch that hasn't been started!");
      if (commit) {
         final Operation[] writeCommands = transaction.toArray(new Operation[transaction.size()]);
         final Object response = original.put("__hr_transaction_key__", writeCommands);
         if (!response.equals("OK")) {
            throw new RuntimeException("The transaction was not committed! : " + response);
         }
      }
      transaction.clear();
      batchStarted = false;
   }

   public Object put(Object key, Object value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
      if (batchStarted) {
         transaction.add(new PutOperation(key, value, lifespanUnit.toMillis(lifespan), maxIdleTimeUnit.toMillis(maxIdleTime)));
         return null;
      } else {
         return original.put(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
      }
   }

   public void putAll(Map map, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
      final Set<Entry> set = map.entrySet();
      for (Map.Entry e : set) {
         put(e.getKey(), e.getValue(), lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
      }
   }

   public V remove(Object key) {
      if (batchStarted) {
         transaction.add(new RemoveOperation(key));
         return null;
      } else {
         return (V) original.remove(key);
      }
   }

   public void clear() {
      if (batchStarted) {
         transaction.add(new ClearOperation());
      } else {
         original.clear();
      }
   }
}
