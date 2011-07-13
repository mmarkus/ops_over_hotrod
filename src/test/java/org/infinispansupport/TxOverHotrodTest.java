package org.infinispansupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.TestHelper;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.StreamingMarshaller;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispansupport.hotrodextenssion.HotrodTransactionsInterceptor;
import org.infinispansupport.hotrodextenssion.tx.BatchEnabledRemoteCache;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Properties;

/**
 * @author Mircea Markus
 */
@Test
public class TxOverHotrodTest extends SingleCacheManagerTest {


   private static Log log = LogFactory.getLog(TxOverHotrodTest.class);

   HotRodServer hotrodServer;
   RemoteCacheManager rcm;
   RemoteCache c;
   private DefaultCacheManager dcm;

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      dcm = new DefaultCacheManager("ispn-config.xml");
      hotrodServer = TestHelper.startHotRodServer(dcm);

      Properties props = new Properties();
      props.put("infinispan.client.hotrod.server_list", "127.0.0.1:" + hotrodServer.getPort());
      props.put("infinispan.client.hotrod.force_return_values", "true");
      props.put("testOnBorrow", "false");
      rcm = new RemoteCacheManager(props);
      c = rcm.getCache(true);

      StreamingMarshaller c = dcm.getCache().getAdvancedCache().getComponentRegistry().getGlobalComponentRegistry().getComponent(StreamingMarshaller.class);
      HotrodTransactionsInterceptor tdi = (HotrodTransactionsInterceptor) dcm.getCache().getAdvancedCache().getInterceptorChain().get(0);
      tdi.init(c);
      tdi.init(dcm.getCache());
      return dcm;
   }


   @AfterClass
   protected void destroyAfterClass() {
      try {
         rcm.stop();
         hotrodServer.stop();
         dcm.stop();
      } catch (Exception e) {
         //ignore
      }
   }

   public void testTransactionalChange() {
      c.put("k", "v0");
      BatchEnabledRemoteCache berc = new BatchEnabledRemoteCache(c);
      berc.startBatch();
      berc.put("k", "v1");
      berc.put("k2", "v2");
      berc.put("k3", "v3");
      assert c.get("k").equals("v0");
      assert c.get("k2") == null;
      assert c.get("k3") == null;
      berc.endBatch(true);

      log.trace("Before calling get");

      assert c.get("k").equals("v1");
      assert c.get("k2").equals("v2");
      assert c.get("k3").equals("v3");

      berc.startBatch();
      berc.remove("k2");
      assert c.get("k2").equals("v2");
      berc.endBatch(true);
      assert c.get("k2") == null;

      try {
         berc.endBatch(true);
         assert false;
      } catch (IllegalStateException e) {
         //expected
      }

      berc.startBatch();
      berc.clear();
      assert c.get("k3").equals("v3");
      berc.endBatch(false);
      assert c.get("k3").equals("v3");

      berc.startBatch();
      berc.clear();
      assert c.get("k3").equals("v3");
      berc.endBatch(true);
      assert c.isEmpty();
   }

   private void sampleUsage() {
      RemoteCacheManager rcm = getRcm();//somehow...
      RemoteCache remoteCache = rcm.getCache("aCache");
      BatchEnabledRemoteCache berm = new BatchEnabledRemoteCache(remoteCache);
      berm.startBatch();
      berm.put("k", "v");
      berm.remove("k2");
      berm.put("k3", "v3");
      berm.endBatch(true);//when this method returns the transaction has been committed
      assert remoteCache.get("k").equals("v");

   }

   private RemoteCacheManager getRcm() {
      return rcm;
   }
}
