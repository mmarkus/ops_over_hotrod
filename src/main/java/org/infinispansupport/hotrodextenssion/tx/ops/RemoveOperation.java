package org.infinispansupport.hotrodextenssion.tx.ops;

/**
 * @author Mircea Markus
 */
public class RemoveOperation implements Operation {

   public static final int ID = 2;

   private final Object key;

   public RemoveOperation(Object key) {
      this.key = key;
   }

   public int getId() {
      return ID;
   }

   public Object getKey() {
      return key;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      RemoveOperation that = (RemoveOperation) o;

      if (key != null ? !key.equals(that.key) : that.key != null) return false;

      return true;
   }

   @Override
   public int hashCode() {
      return key != null ? key.hashCode() : 0;
   }

   @Override
   public String toString() {
      return "RemoveOperation{" +
            "key=" + key +
            '}';
   }
}
