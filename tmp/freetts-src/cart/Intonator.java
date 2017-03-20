/*    */ package com.sun.speech.freetts.cart;
/*    */ 
/*    */ import com.sun.speech.freetts.FeatureSet;
/*    */ import com.sun.speech.freetts.Item;
/*    */ import com.sun.speech.freetts.ProcessException;
/*    */ import com.sun.speech.freetts.Relation;
/*    */ import com.sun.speech.freetts.Utterance;
/*    */ import com.sun.speech.freetts.UtteranceProcessor;
/*    */ 
/*    */ public class Intonator
/*    */   implements UtteranceProcessor
/*    */ {
/*    */   protected CART accentCart;
/*    */   protected CART toneCart;
/*    */ 
/*    */   public Intonator(CART accentCart, CART toneCart)
/*    */   {
/* 53 */     this.accentCart = accentCart;
/* 54 */     this.toneCart = toneCart;
/*    */   }
/*    */ 
/*    */   public void processUtterance(Utterance utterance)
/*    */     throws ProcessException
/*    */   {
/* 71 */     Item syllable = utterance.getRelation("Syllable").getHead();
/*    */ 
/* 73 */     while (syllable != null)
/*    */     {
/* 75 */       String results = (String)this.accentCart.interpret(syllable);
/* 76 */       if (!results.equals("NONE")) {
/* 77 */         syllable.getFeatures().setString("accent", results);
/*    */       }
/* 79 */       results = (String)this.toneCart.interpret(syllable);
/* 80 */       if (!results.equals("NONE"))
/* 81 */         syllable.getFeatures().setString("endtone", results);
/* 74 */       syllable = syllable.getNext();
/*    */     }
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 88 */     return "CARTIntonator";
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.cart.Intonator
 * JD-Core Version:    0.5.4
 */