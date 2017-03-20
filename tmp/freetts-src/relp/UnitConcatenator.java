/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import com.sun.speech.freetts.FeatureSet;
/*     */ import com.sun.speech.freetts.Item;
/*     */ import com.sun.speech.freetts.ProcessException;
/*     */ import com.sun.speech.freetts.Relation;
/*     */ import com.sun.speech.freetts.Unit;
/*     */ import com.sun.speech.freetts.Utterance;
/*     */ import com.sun.speech.freetts.UtteranceProcessor;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ 
/*     */ public class UnitConcatenator
/*     */   implements UtteranceProcessor
/*     */ {
/*     */   private static final int ADD_RESIDUAL_PULSE = 1;
/*     */   private static final int ADD_RESIDUAL_WINDOWED = 2;
/*     */   private static final int ADD_RESIDUAL = 3;
/*     */   public static final String PROP_OUTPUT_LPC = "com.sun.speech.freetts.outputLPC";
/*     */   private boolean outputLPC;
/*     */ 
/*     */   public UnitConcatenator()
/*     */   {
/*  37 */     this.outputLPC = Utilities.getBoolean("com.sun.speech.freetts.outputLPC");
/*     */   }
/*     */ 
/*     */   public void processUtterance(Utterance utterance)
/*     */     throws ProcessException
/*     */   {
/*  52 */     float uIndex = 0.0F;
/*  53 */     int pmI = 0; int targetResidualPosition = 0;
/*  54 */     int targetStart = 0;
/*  55 */     Relation unitRelation = utterance.getRelation("Unit");
/*     */ 
/*  60 */     int addResidualMethod = 3;
/*     */ 
/*  62 */     String residualType = utterance.getString("residual_type");
/*  63 */     if (residualType != null) {
/*  64 */       if (residualType.equals("pulse"))
/*  65 */         addResidualMethod = 1;
/*  66 */       else if (residualType.equals("windowed")) {
/*  67 */         addResidualMethod = 2;
/*     */       }
/*     */     }
/*     */ 
/*  71 */     SampleInfo sampleInfo = (SampleInfo)utterance.getObject("SampleInfo");
/*  72 */     if (sampleInfo == null) {
/*  73 */       throw new IllegalStateException("UnitConcatenator: SampleInfo does not exist");
/*     */     }
/*     */ 
/*  77 */     LPCResult lpcResult = (LPCResult)utterance.getObject("target_lpcres");
/*  78 */     lpcResult.setValues(sampleInfo.getNumberOfChannels(), sampleInfo.getSampleRate(), sampleInfo.getResidualFold(), sampleInfo.getCoeffMin(), sampleInfo.getCoeffRange());
/*     */ 
/*  85 */     int[] targetTimes = lpcResult.getTimes();
/*  86 */     int[] residualSizes = lpcResult.getResidualSizes();
/*     */ 
/*  88 */     int samplesSize = 0;
/*  89 */     if (lpcResult.getNumberOfFrames() > 0) {
/*  90 */       samplesSize = targetTimes[(lpcResult.getNumberOfFrames() - 1)];
/*     */     }
/*  92 */     lpcResult.resizeResiduals(samplesSize);
/*     */ 
/*  94 */     for (Item unitItem = unitRelation.getHead(); unitItem != null; )
/*     */     {
/*  96 */       FeatureSet featureSet = unitItem.getFeatures();
/*     */ 
/*  98 */       String unitName = featureSet.getString("name");
/*  99 */       int targetEnd = featureSet.getInt("target_end");
/* 100 */       Unit unit = (Unit)featureSet.getObject("unit");
/* 101 */       int unitSize = unit.getSize();
/*     */ 
/* 103 */       uIndex = 0.0F;
/* 104 */       float m = unitSize / (targetEnd - targetStart);
/* 105 */       int numberFrames = lpcResult.getNumberOfFrames();
/*     */ 
/* 108 */       while ((pmI < numberFrames) && (targetTimes[pmI] <= targetEnd))
/*     */       {
/* 111 */         Sample sample = unit.getNearestSample(uIndex);
/*     */ 
/* 114 */         lpcResult.setFrame(pmI, sample.getFrameData());
/*     */ 
/* 117 */         int residualSize = lpcResult.getFrameShift(pmI);
/*     */ 
/* 119 */         residualSizes[pmI] = residualSize;
/* 120 */         byte[] residualData = sample.getResidualData();
/*     */ 
/* 122 */         if (addResidualMethod == 1) {
/* 123 */           lpcResult.copyResidualsPulse(residualData, targetResidualPosition, residualSize);
/*     */         }
/*     */         else {
/* 126 */           lpcResult.copyResiduals(residualData, targetResidualPosition, residualSize);
/*     */         }
/*     */ 
/* 130 */         targetResidualPosition += residualSize;
/* 131 */         uIndex += residualSize * m;
/*     */ 
/* 109 */         ++pmI;
/*     */       }
/*     */ 
/* 133 */       targetStart = targetEnd;
/*     */ 
/*  95 */       unitItem = unitItem.getNext();
/*     */     }
/*     */ 
/* 135 */     lpcResult.setNumberOfFrames(pmI);
/*     */ 
/* 137 */     if (this.outputLPC)
/* 138 */       lpcResult.dump();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 147 */     return "UnitConcatenator";
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.UnitConcatenator
 * JD-Core Version:    0.5.4
 */