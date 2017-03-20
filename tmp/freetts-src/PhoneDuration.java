/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ public class PhoneDuration
/*    */ {
/*    */   private float mean;
/*    */   private float standardDeviation;
/*    */ 
/*    */   public PhoneDuration(float mean, float standardDeviation)
/*    */   {
/* 40 */     this.mean = mean;
/* 41 */     this.standardDeviation = standardDeviation;
/*    */   }
/*    */ 
/*    */   public float getMean()
/*    */   {
/* 50 */     return this.mean;
/*    */   }
/*    */ 
/*    */   public float getStandardDeviation()
/*    */   {
/* 59 */     return this.standardDeviation;
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.PhoneDuration
 * JD-Core Version:    0.5.4
 */