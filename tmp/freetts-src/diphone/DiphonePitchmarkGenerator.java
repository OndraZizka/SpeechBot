/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ import com.sun.speech.freetts.FeatureSet;
/*     */ import com.sun.speech.freetts.Item;
/*     */ import com.sun.speech.freetts.ProcessException;
/*     */ import com.sun.speech.freetts.Relation;
/*     */ import com.sun.speech.freetts.Utterance;
/*     */ import com.sun.speech.freetts.UtteranceProcessor;
/*     */ import com.sun.speech.freetts.Voice;
/*     */ import com.sun.speech.freetts.relp.LPCResult;
/*     */ import com.sun.speech.freetts.relp.SampleInfo;
/*     */ 
/*     */ public class DiphonePitchmarkGenerator
/*     */   implements UtteranceProcessor
/*     */ {
/*     */   public void processUtterance(Utterance utterance)
/*     */     throws ProcessException
/*     */   {
/*  48 */     Relation targetRelation = utterance.getRelation("Target");
/*  49 */     if (targetRelation == null) {
/*  50 */       throw new IllegalStateException("DiphonePitchmarkGenerator: Target relation does not exist");
/*     */     }
/*     */ 
/*  55 */     SampleInfo sampleInfo = (SampleInfo)utterance.getObject("SampleInfo");
/*  56 */     if (sampleInfo == null) {
/*  57 */       throw new IllegalStateException("DiphonePitchmarkGenerator: SampleInfo does not exist");
/*     */     }
/*     */ 
/*  61 */     float m = 0.0F;
/*  62 */     float lf0 = utterance.getVoice().getPitch();
/*     */ 
/*  64 */     double time = 0.0D;
/*  65 */     int pitchMarks = 0;
/*     */ 
/*  68 */     IntLinkedList timesList = new IntLinkedList();
/*     */ 
/*  71 */     Item targetItem = targetRelation.getHead();
/*  72 */     for (; targetItem != null; targetItem = targetItem.getNext()) {
/*  73 */       FeatureSet featureSet = targetItem.getFeatures();
/*  74 */       float pos = featureSet.getFloat("pos");
/*  75 */       float f0 = featureSet.getFloat("f0");
/*     */ 
/*  77 */       if (time == pos) {
/*  78 */         lf0 = f0;
/*     */       }
/*     */       else {
/*  81 */         m = (f0 - lf0) / pos;
/*     */ 
/*  83 */         for (; time < pos; ++pitchMarks) {
/*  84 */           time += 1.0D / (lf0 + time * m);
/*     */ 
/*  87 */           timesList.add((int)(time * sampleInfo.getSampleRate()));
/*     */         }
/*  89 */         lf0 = f0;
/*     */       }
/*     */     }
/*  91 */     LPCResult lpcResult = new LPCResult();
/*     */ 
/*  93 */     lpcResult.resizeFrames(pitchMarks);
/*     */ 
/*  95 */     pitchMarks = 0;
/*     */ 
/*  97 */     int[] targetTimes = lpcResult.getTimes();
/*     */ 
/* 100 */     timesList.resetIterator();
/* 101 */     for (; pitchMarks < targetTimes.length; ++pitchMarks) {
/* 102 */       targetTimes[pitchMarks] = timesList.nextInt();
/*     */     }
/* 104 */     utterance.setObject("target_lpcres", lpcResult);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 114 */     return "DiphonePitchmarkGenerator";
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.DiphonePitchmarkGenerator
 * JD-Core Version:    0.5.4
 */