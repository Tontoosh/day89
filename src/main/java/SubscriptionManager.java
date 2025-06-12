// Source code is decompiled from a .class file using FernFlower decompiler.
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class SubscriptionManager {
   private List<Subscription> subscriptions = new ArrayList();

   public SubscriptionManager(List<Subscription> var1) {
      this.subscriptions.addAll(var1);
   }

   public List<Subscription> getSubscriptions() {
      return this.subscriptions;
   }

   public void addSubscription(String var1, String var2, String var3, String var4, String var5, String var6, String var7) {
      String var8 = this.generateNewId();
      Object var9 = var7.equalsIgnoreCase("Product") ? new ProductSubscription(var8, var1, var2, var3, var4, var5, var6) : new ServiceSubscription(var8, var1, var2, var3, var4, var5, var6);
      this.subscriptions.add((Subscription) var9);
   }

   public String generateNewId() {
      Object[] var10001 = new Object[]{this.subscriptions.size() + 1};
      return "S" + String.format("%04d", var10001);
   }

   public Subscription findById(String var1) {
      return (Subscription)this.subscriptions.stream().filter((var1x) -> {
         return var1x.id.equals(var1);
      }).findFirst().orElse((Subscription)null);
   }

   public void editSubscription(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) {
      Subscription var9 = this.findById(var1);
      if (var9 != null) {
         this.subscriptions.remove(var9);
         Object var10 = var8.equalsIgnoreCase("Product") ? new ProductSubscription(var1, var2, var3, var4, var5, var6, var7) : new ServiceSubscription(var1, var2, var3, var4, var5, var6, var7);
         this.subscriptions.add((Subscription) var10);
      }

   }

   public void removeSubscription(String var1) {
      this.subscriptions.removeIf((var1x) -> {
         return var1x.id.equals(var1);
      });
   }

   public List<Subscription> filter(String var1) {
      var1 = var1.toLowerCase();
      ArrayList var2 = new ArrayList();
      Iterator var3 = this.subscriptions.iterator();

      while(true) {
         Subscription var4;
         do {
            if (!var3.hasNext()) {
               return var2;
            }

            var4 = (Subscription)var3.next();
         } while(!var4.id.toLowerCase().contains(var1) && !var4.customer.toLowerCase().contains(var1) && !var4.phone.contains(var1) && !var4.plan.toLowerCase().contains(var1) && !var4.status.toLowerCase().contains(var1));

         var2.add(var4);
      }
   }
}