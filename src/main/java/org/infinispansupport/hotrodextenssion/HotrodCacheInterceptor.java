package org.infinispansupport.hotrodextenssion;


import org.apache.commons.logging.Log;
import org.infinispan.CacheException;
import org.infinispan.commands.write.PutKeyValueCommand;
import org.infinispan.context.InvocationContext;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.marshall.StreamingMarshaller;
import org.infinispan.marshall.jboss.JBossMarshaller;
import org.infinispan.util.ByteArrayKey;
import org.infinispan.util.ReflectionUtil;
import org.apache.commons.logging.LogFactory;
import org.infinispan.server.core.CacheValue;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Mircea Markus
 */
public abstract class HotrodCacheInterceptor extends CommandInterceptor {

   private static Log log = LogFactory.getLog(HotrodCacheInterceptor.class);

   private JBossMarshaller jbm;
   private byte[] keyToWatch;

   public void init(StreamingMarshaller sm) {
      jbm = (JBossMarshaller) ReflectionUtil.getValue(sm, "defaultMarshaller");
      try {
         keyToWatch = jbm.objectToByteBuffer(getKeyToWatch());
      } catch (Exception e) {
         throw new CacheException(e);
      }
   }

   @Override
   public Object visitPutKeyValueCommand(InvocationContext ctx, PutKeyValueCommand command) throws Throwable {
      Object key = command.getKey();

      if (key instanceof ByteArrayKey) {
         byte[] keyData = ((ByteArrayKey) key).getData();
         if (Arrays.equals(keyData, keyToWatch)) {
            if (log.isTraceEnabled()) {
               log.trace("Found key matching: " + getKeyToWatch());
            }
            byte[] valueData = ((CacheValue) command.getValue()).data();
            Object result = processPayload(jbm.objectFromByteBuffer(valueData));
            return hrValue(result);
         }
      }
      return super.visitPutKeyValueCommand(ctx, command);
   }

   protected final Object hrKey(Object o) {
      try {
         return new ByteArrayKey(jbm.objectToByteBuffer(o));
      } catch (Exception e) {
         throw newIllegalStateEx(e);
      }
   }

   protected final Object hrValue(Object o) {
      try {
         return new CacheValue(jbm.objectToByteBuffer(o), 0);
      } catch (Exception e) {
         throw newIllegalStateEx(e);
      }
   }

   /**
    * In hotrod 0 means forever: http://community.jboss.org/wiki/HotRodProtocol#lifespan_vint
    */
   protected final long hrTime(long millis) {
      return millis == 0 ? -1 : millis;
   }


   private IllegalStateException newIllegalStateEx(Exception e) {
      return new IllegalStateException("This cannot happen!", e);
   }

   public abstract String getKeyToWatch();

   public abstract Object processPayload(Object value);
}
