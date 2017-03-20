/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ import java.io.PrintWriter;
/*    */ 
/*    */ public class ItemContents
/*    */ {
/*    */   private FeatureSetImpl features;
/*    */   private FeatureSetImpl relations;
/*    */ 
/*    */   public ItemContents()
/*    */   {
/* 25 */     this.features = new FeatureSetImpl();
/* 26 */     this.relations = new FeatureSetImpl();
/*    */   }
/*    */ 
/*    */   public void addItemRelation(String relationName, Item item)
/*    */   {
/* 41 */     this.relations.setObject(relationName, item);
/*    */   }
/*    */ 
/*    */   public void removeItemRelation(String relationName)
/*    */   {
/* 50 */     this.relations.remove(relationName);
/*    */   }
/*    */ 
/*    */   public void showRelations()
/*    */   {
/* 55 */     PrintWriter pw = new PrintWriter(System.out);
/* 56 */     this.relations.dump(pw, 0, "Contents relations", true);
/* 57 */     pw.flush();
/*    */   }
/*    */ 
/*    */   public Item getItemRelation(String relationName)
/*    */   {
/* 70 */     return (Item)this.relations.getObject(relationName);
/*    */   }
/*    */ 
/*    */   public FeatureSet getFeatures()
/*    */   {
/* 79 */     return this.features;
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.ItemContents
 * JD-Core Version:    0.5.4
 */