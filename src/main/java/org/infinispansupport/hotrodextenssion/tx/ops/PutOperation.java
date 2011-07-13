package org.infinispansupport.hotrodextenssion.tx.ops;

/**
 * @author Mircea Markus
 */
public class PutOperation implements Operation {

   public static final int ID = 1;

   private final Object key;

   private final Object value;

   private final long lifespanMillis;

   private final long maxIdleTimeMillis;

   public PutOperation(Object key, Object value, long lifespanMillis, long maxIdleTimeMillis) {
      this.key = key;
      this.value = value;
      this.lifespanMillis = lifespanMillis;
      this.maxIdleTimeMillis = maxIdleTimeMillis;
   }

   public int getId() {
      return ID;
   }

   public Object getKey() {
      return key;
   }

   public Object getValue() {
      return value;
   }

   public long getLifespanMillis() {
      return lifespanMillis;
   }

   public long getMaxIdleTimeMillis() {
      return maxIdleTimeMillis;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PutOperation that = (PutOperation) o;

      if (lifespanMillis != that.lifespanMillis) return false;
      if (maxIdleTimeMillis != that.maxIdleTimeMillis) return false;
      if (key != null ? !key.equals(that.key) : that.key != null) return false;
      if (value != null ? !value.equals(that.value) : that.value != null) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      result = 31 * result + (int) (lifespanMillis ^ (lifespanMillis >>> 32));
      result = 31 * result + (int) (maxIdleTimeMillis ^ (maxIdleTimeMillis >>> 32));
      return result;
   }

   @Override
   public String toString() {
      return "PutOperation{" +
            "key=" + key +
            ", value=" + value +
            ", lifespanMillis=" + lifespanMillis +
            ", maxIdleTimeMillis=" + maxIdleTimeMillis +
            '}';
   }
}
