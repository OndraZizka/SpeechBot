/*    */ package com.sun.speech.freetts.cart;
/*    */ 
/*    */ import com.sun.speech.freetts.FeatureSet;
/*    */ import com.sun.speech.freetts.Item;
/*    */ import com.sun.speech.freetts.ProcessException;
/*    */ import com.sun.speech.freetts.Relation;
/*    */ import com.sun.speech.freetts.Utterance;
/*    */ import com.sun.speech.freetts.UtteranceProcessor;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ 
/*    */ public class Phraser
/*    */   implements UtteranceProcessor
/*    */ {
/* 31 */   private static final Logger LOGGER = Logger.getLogger(UtteranceProcessor.class.getName());
/*    */   protected final CART cart;
/*    */ 
/*    */   public Phraser(CART cart)
/*    */   {
/* 48 */     this.cart = cart;
/*    */   }
/*    */ 
/*    */   public void processUtterance(Utterance utterance)
/*    */     throws ProcessException
/*    */   {
/* 63 */     Relation relation = utterance.createRelation("Phrase");
/* 64 */     Item p = null;
/* 65 */     Item w = utterance.getRelation("Word").getHead();
/* 66 */     for (; w != null; w = w.getNext()) {
/* 67 */       if (p == null) {
/* 68 */         p = relation.appendItem();
/* 69 */         p.getFeatures().setString("name", "BB");
/*    */       }
/* 71 */       p.addDaughter(w);
/* 72 */       String results = (String)this.cart.interpret(w);
/*    */ 
/* 74 */       if (LOGGER.isLoggable(Level.FINER)) {
/* 75 */         LOGGER.finer("word: " + w + ", results: " + results);
/*    */       }
/* 77 */       if (results.equals("BB"))
/* 78 */         p = null;
/*    */     }
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 85 */     return "CARTPhraser";
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.cart.Phraser
 * JD-Core Version:    0.5.4
 */