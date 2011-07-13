package org.infinispansupport.hotrodextenssion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.factories.annotations.Inject;
import org.infinispansupport.hotrodextenssion.tx.ops.ClearOperation;
import org.infinispansupport.hotrodextenssion.tx.ops.Operation;
import org.infinispansupport.hotrodextenssion.tx.ops.PutOperation;
import org.infinispansupport.hotrodextenssion.tx.ops.RemoveOperation;

import javax.transaction.TransactionManager;
import java.util.concurrent.TimeUnit;

/**
 * @author Mircea Markus
 */
public class HotrodTransactionsInterceptor extends HotrodCacheInterceptor {

   private static Log log = LogFactory.getLog(HotrodTransactionsInterceptor.class);

   private AdvancedCache cache;

   @Inject
   public void init(Cache cache) {
      this.cache = cache.getAdvancedCache();
   }

   @Override
   public String getKeyToWatch() {
      return "__hr_transaction_key__";
   }

   @Override
   public Object processPayload(Object value) {
      Operation[] ops = (Operation[]) value;
      final TransactionManager tm = cache.getTransactionManager();
      if (tm == null) throw exception();
      try {
         tm.begin();
         for (Operation operation : ops) {
            if (log.isTraceEnabled()) log.trace("Processing " + operation);
            switch (operation.getId()) {
               case PutOperation.ID: {
                  PutOperation po = (PutOperation) operation;
                  cache.put(hrKey(po.getKey()), hrValue(po.getValue()), hrTime(po.getLifespanMillis()), TimeUnit.MILLISECONDS,
                            hrTime(po.getMaxIdleTimeMillis()), TimeUnit.MILLISECONDS);
                  break;
               }
               case RemoveOperation.ID: {
                  RemoveOperation ro = (RemoveOperation) operation;
                  cache.remove(hrKey(ro.getKey()));
                  break;
               }
               case ClearOperation.ID: {
                  cache.clear();
                  break;
               }
               default: {
                  throw new IllegalStateException("Unknown operation: " + operation);
               }
            }
         }
         tm.commit();
      } catch (Throwable throwable) {
         log.error("Unexpected!", throwable);
         return "Could not succeed: " + throwable.getMessage();
      }
      return "OK";
   }

   private IllegalStateException exception() {
      return new IllegalStateException("There's no transaction manager configured for this cache!. " +
                                             "You can configure one e.g. through XML as follows: <transaction transactionManagerLookupClass=\"org.infinispan.transaction.lookup.GenericTransactionManagerLookup\"/>");
   }
}
