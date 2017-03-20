/*    */ package com.sun.speech.freetts.clunits;
/*    */ 
/*    */ import com.sun.speech.freetts.FeatureSet;
/*    */ import com.sun.speech.freetts.Item;
/*    */ import com.sun.speech.freetts.ProcessException;
/*    */ import com.sun.speech.freetts.Relation;
/*    */ import com.sun.speech.freetts.Utterance;
/*    */ import com.sun.speech.freetts.UtteranceProcessor;
/*    */ import com.sun.speech.freetts.relp.LPCResult;
/*    */ import com.sun.speech.freetts.relp.Sample;
/*    */ import com.sun.speech.freetts.relp.SampleSet;
/*    */ 
/*    */ public class ClusterUnitPitchmarkGenerator
/*    */   implements UtteranceProcessor
/*    */ {
/*    */   public void processUtterance(Utterance utterance)
/*    */     throws ProcessException
/*    */   {
/* 47 */     int pitchmarks = 0;
/* 48 */     int uttSize = 0;
/*    */ 
/* 53 */     SampleSet sts = (SampleSet)utterance.getObject("sts_list");
/* 54 */     LPCResult lpcResult = new LPCResult();
/*    */ 
/* 56 */     Item unit = utterance.getRelation("Unit").getHead();
/* 57 */     for (; unit != null; unit = unit.getNext()) {
/* 58 */       int unitEntry = unit.getFeatures().getInt("unit_entry");
/* 59 */       int unitStart = unit.getFeatures().getInt("unit_start");
/* 60 */       int unitEnd = unit.getFeatures().getInt("unit_end");
/* 61 */       uttSize += sts.getUnitSize(unitStart, unitEnd);
/* 62 */       pitchmarks += unitEnd - unitStart;
/* 63 */       unit.getFeatures().setInt("target_end", uttSize);
/*    */     }
/*    */ 
/* 66 */     lpcResult.resizeFrames(pitchmarks);
/*    */ 
/* 68 */     pitchmarks = 0;
/* 69 */     uttSize = 0;
/*    */ 
/* 71 */     int[] targetTimes = lpcResult.getTimes();
/*    */ 
/* 73 */     Item unit = utterance.getRelation("Unit").getHead();
/* 74 */     for (; unit != null; unit = unit.getNext()) {
/* 75 */       int unitEntry = unit.getFeatures().getInt("unit_entry");
/* 76 */       int unitStart = unit.getFeatures().getInt("unit_start");
/* 77 */       int unitEnd = unit.getFeatures().getInt("unit_end");
/* 78 */       for (int i = unitStart; i < unitEnd; ++pitchmarks) {
/* 79 */         uttSize += sts.getSample(i).getResidualSize();
/* 80 */         targetTimes[pitchmarks] = uttSize;
/*    */ 
/* 78 */         ++i;
/*    */       }
/*    */ 
/*    */     }
/*    */ 
/* 83 */     utterance.setObject("target_lpcres", lpcResult);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 92 */     return "ClusterUnitPitchmarkGenerator";
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.clunits.ClusterUnitPitchmarkGenerator
 * JD-Core Version:    0.5.4
 */