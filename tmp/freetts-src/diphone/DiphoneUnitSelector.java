/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ import com.sun.speech.freetts.FeatureSet;
/*     */ import com.sun.speech.freetts.Item;
/*     */ import com.sun.speech.freetts.ProcessException;
/*     */ import com.sun.speech.freetts.Relation;
/*     */ import com.sun.speech.freetts.Utterance;
/*     */ import com.sun.speech.freetts.UtteranceProcessor;
/*     */ import com.sun.speech.freetts.relp.SampleInfo;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ 
/*     */ public class DiphoneUnitSelector
/*     */   implements UtteranceProcessor
/*     */ {
/*     */   private DiphoneUnitDatabase diphoneDatabase;
/*     */ 
/*     */   public DiphoneUnitSelector(URL url)
/*     */     throws IOException
/*     */   {
/*  51 */     if (url == null) {
/*  52 */       throw new IOException("Can't load unit database");
/*     */     }
/*  54 */     boolean binary = url.getPath().endsWith(".bin");
/*  55 */     this.diphoneDatabase = new DiphoneUnitDatabase(url, binary);
/*     */   }
/*     */ 
/*     */   public SampleInfo getSampleInfo()
/*     */   {
/*  63 */     return this.diphoneDatabase.getSampleInfo();
/*     */   }
/*     */ 
/*     */   public void processUtterance(Utterance utterance)
/*     */     throws ProcessException
/*     */   {
/*  76 */     if (utterance.getRelation("Segment") == null) {
/*  77 */       throw new IllegalStateException("DiphoneUnitSelector: Segment relation does not exist");
/*     */     }
/*     */ 
/*  81 */     utterance.setObject("SampleInfo", this.diphoneDatabase.getSampleInfo());
/*     */ 
/*  83 */     createUnitRelation(utterance);
/*     */   }
/*     */ 
/*     */   private void createUnitRelation(Utterance utterance)
/*     */   {
/* 103 */     Relation unitRelation = utterance.createRelation("Unit");
/* 104 */     Relation segmentRelation = utterance.getRelation("Segment");
/*     */ 
/* 106 */     Item segmentItem0 = segmentRelation.getHead();
/* 107 */     while ((segmentItem0 != null) && (segmentItem0.getNext() != null))
/*     */     {
/* 109 */       Item segmentItem1 = segmentItem0.getNext();
/* 110 */       String diphoneName = segmentItem0.getFeatures().getString("name") + "-" + segmentItem1.getFeatures().getString("name");
/*     */ 
/* 115 */       float end0 = segmentItem0.getFeatures().getFloat("end");
/* 116 */       int targetEnd = (int)(end0 * this.diphoneDatabase.getSampleInfo().getSampleRate());
/*     */ 
/* 118 */       Item unitItem0 = createUnitItem(unitRelation, diphoneName, targetEnd, 1);
/* 119 */       segmentItem0.addDaughter(unitItem0);
/*     */ 
/* 122 */       float end1 = segmentItem1.getFeatures().getFloat("end");
/* 123 */       targetEnd = (int)((end0 + end1) / 2.0D * this.diphoneDatabase.getSampleInfo().getSampleRate());
/*     */ 
/* 125 */       Item unitItem1 = createUnitItem(unitRelation, diphoneName, targetEnd, 2);
/* 126 */       segmentItem1.addDaughter(unitItem1);
/*     */ 
/* 108 */       segmentItem0 = segmentItem1;
/*     */     }
/*     */   }
/*     */ 
/*     */   private Item createUnitItem(Relation unitRelation, String diphoneName, int targetEnd, int unitPart)
/*     */   {
/* 144 */     Diphone diphone = this.diphoneDatabase.getUnit(diphoneName);
/* 145 */     if (diphone == null) {
/* 146 */       System.err.println("FreeTTS: unit database failed to find entry for: " + diphoneName);
/*     */     }
/*     */ 
/* 150 */     Item unit = unitRelation.appendItem();
/* 151 */     FeatureSet unitFeatureSet = unit.getFeatures();
/*     */ 
/* 153 */     unitFeatureSet.setString("name", diphoneName);
/* 154 */     unitFeatureSet.setInt("target_end", targetEnd);
/* 155 */     unitFeatureSet.setObject("unit", new DiphoneUnit(diphone, unitPart));
/*     */ 
/* 157 */     return unit;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 166 */     return "DiphoneUnitSelector";
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.DiphoneUnitSelector
 * JD-Core Version:    0.5.4
 */