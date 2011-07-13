package org.infinispansupport.hotrodextenssion.tx;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.CacheSupport;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.concurrent.NotifyingFuture;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Mircea Markus
 */
public abstract class AbstractBatchEnabledRemoteCache<K, V> extends CacheSupport<K, V> implements Cache<K, V> {

   protected final RemoteCache original;

   protected boolean batchStarted;

   public AbstractBatchEnabledRemoteCache(RemoteCache original) {
      this.original = original;
   }

   public Object replace(Object key, Object value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
      forceNotWithinTransaction();
      return original.replace(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
   }

   private void forceNotWithinTransaction() {
      if (batchStarted) throw new IllegalStateException("Not transactional!");
   }

   public boolean replace(Object key, Object oldValue, Object value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
      forceNotWithinTransaction();
      return original.replace(key, oldValue, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
   }

   public NotifyingFuture putAsync(Object key, Object value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
      forceNotWithinTransaction();
      return original.putAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
   }

   public NotifyingFuture<Void> putAllAsync(Map data, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
      forceNotWithinTransaction();
      return original.putAllAsync(data, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
   }

   public NotifyingFuture<Void> clearAsync() {
      forceNotWithinTransaction();
      return original.clearAsync();
   }

   public NotifyingFuture putIfAbsentAsync(Object key, Object value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
      forceNotWithinTransaction();
      return original.putIfAbsentAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
   }

   public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
      forceNotWithinTransaction();
      return (V) original.putIfAbsent(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
   }

   public NotifyingFuture removeAsync(Object key) {
      forceNotWithinTransaction();
      return original.removeAsync(key);
   }

   public NotifyingFuture<Boolean> removeAsync(Object key, Object value) {
      forceNotWithinTransaction();
      return original.removeAsync(key, value);
   }

   public NotifyingFuture replaceAsync(Object key, Object value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
      forceNotWithinTransaction();
      return original.replaceAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
   }

   public NotifyingFuture<Boolean> replaceAsync(Object key, Object oldValue, Object newValue, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
      forceNotWithinTransaction();
      return original.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
   }

   public boolean remove(Object key, Object value) {
      forceNotWithinTransaction();
      throw new IllegalStateException("Not transactional!");
   }

   public NotifyingFuture getAsync(Object key) {
      forceNotWithinTransaction();
      return original.getAsync(key);
   }

   public AdvancedCache getAdvancedCache() {
      forceNotWithinTransaction();
      return original.getAdvancedCache();
   }

   public void compact() {
      original.compact();
   }

   public ComponentStatus getStatus() {
      return original.getStatus();
   }

   public Set keySet() {
      return original.keySet();
   }

   public Collection values() {
      return original.values();
   }

   public Set entrySet() {
      return original.entrySet();
   }

   public String getName() {
      return original.getName();
   }

   public String getVersion() {
      return original.getVersion();
   }

   public EmbeddedCacheManager getCacheManager() {
      return original.getCacheManager();
   }

   public void putForExternalRead(Object key, Object value) {
      original.putForExternalRead(key, value);
   }

   public void evict(Object key) {
      original.evict(key);
   }

   public void start() {
      original.start();
   }

   public void stop() {
      original.stop();
   }

   public void addListener(Object listener) {
      original.addListener(listener);
   }

   public void removeListener(Object listener) {
      original.removeListener(listener);
   }

   public Set<Object> getListeners() {
      return original.getListeners();
   }

   public int size() {
      return original.size();
   }

   public boolean isEmpty() {
      return original.isEmpty();
   }

   public boolean containsKey(Object key) {
      return original.containsKey(key);
   }

   public boolean containsValue(Object value) {
      return original.containsValue(value);
   }

   public V get(Object key) {
      return (V) original.get(key);
   }

}
