// Source code is decompiled from a .class file using FernFlower decompiler.
abstract class Subscription {
   String id;
   String customer;
   String phone;
   String nextDate;
   String recurring;
   String plan;
   String status;

   public Subscription(String var1, String var2, String var3, String var4, String var5, String var6, String var7) {
      this.id = var1;
      this.customer = var2;
      this.phone = var3;
      this.nextDate = var4;
      this.recurring = var5;
      this.plan = var6;
      this.status = var7;
   }

   public abstract String getType();

   public Object[] toRow() {
      return new Object[]{this.id, this.customer, this.phone, this.nextDate, this.recurring, this.plan, this.status, this.getType()};
   }
}
