/*     */ package com.sun.speech.freetts.cart;
/*     */ 
/*     */ import com.sun.speech.freetts.FeatureSet;
/*     */ import com.sun.speech.freetts.Item;
/*     */ import com.sun.speech.freetts.PathExtractor;
/*     */ import com.sun.speech.freetts.PathExtractorImpl;
/*     */ import com.sun.speech.freetts.PhoneDuration;
/*     */ import com.sun.speech.freetts.PhoneDurations;
/*     */ import com.sun.speech.freetts.ProcessException;
/*     */ import com.sun.speech.freetts.Relation;
/*     */ import com.sun.speech.freetts.Utterance;
/*     */ import com.sun.speech.freetts.UtteranceProcessor;
/*     */ import com.sun.speech.freetts.Voice;
/*     */ 
/*     */ public class Durator
/*     */   implements UtteranceProcessor
/*     */ {
/*     */   private final float meanRate;
/*     */   protected final CART cart;
/*     */   protected final PhoneDurations durations;
/*  54 */   private static final PathExtractor DURATION_STRETCH_PATH = new PathExtractorImpl("R:SylStructure.parent.parent.R:Token.parent.local_duration_stretch", true);
/*     */ 
/*     */   public Durator(CART cart, float meanRate, PhoneDurations durations)
/*     */   {
/*  68 */     this.cart = cart;
/*  69 */     this.meanRate = meanRate;
/*  70 */     this.durations = durations;
/*     */   }
/*     */ 
/*     */   public void processUtterance(Utterance utterance)
/*     */     throws ProcessException
/*     */   {
/*  90 */     float durationStretch = utterance.getVoice().getDurationStretch();
/*     */ 
/*  93 */     float end = 0.0F;
/*     */ 
/* 100 */     float durStretch = this.meanRate / utterance.getVoice().getRate();
/*     */ 
/* 106 */     Item segment = utterance.getRelation("Segment").getHead();
/*     */ 
/* 108 */     for (; segment != null; segment = segment.getNext()) {
/* 109 */       float zdur = ((Float)this.cart.interpret(segment)).floatValue();
/* 110 */       PhoneDuration durStat = this.durations.getPhoneDuration(segment.getFeatures().getString("name"));
/*     */ 
/* 113 */       Object tval = DURATION_STRETCH_PATH.findFeature(segment);
/* 114 */       float localDurationStretch = Float.parseFloat(tval.toString());
/*     */ 
/* 116 */       if (localDurationStretch == 0.0D)
/* 117 */         localDurationStretch = durationStretch;
/*     */       else {
/* 119 */         localDurationStretch *= durationStretch;
/*     */       }
/*     */ 
/* 122 */       float dur = localDurationStretch * (zdur * durStat.getStandardDeviation() + durStat.getMean());
/*     */ 
/* 124 */       end += dur;
/* 125 */       segment.getFeatures().setFloat("end", end);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 131 */     return "CARTDurator";
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.cart.Durator
 * JD-Core Version:    0.5.4
 */