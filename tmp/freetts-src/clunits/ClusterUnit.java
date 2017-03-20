/*      */ package com.sun.speech.freetts.clunits;
/*      */ 
/*      */ import com.sun.speech.freetts.Unit;
/*      */ import com.sun.speech.freetts.relp.Sample;
/*      */ import com.sun.speech.freetts.relp.SampleSet;
/*      */ 
/*      */ class ClusterUnit
/*      */   implements Unit
/*      */ {
/*      */   private ClusterUnitDatabase db;
/*      */   private String name;
/*      */   private int start;
/*      */   private int end;
/*      */ 
/*      */   public ClusterUnit(ClusterUnitDatabase db, String name, int start, int end)
/*      */   {
/* 1102 */     this.db = db;
/* 1103 */     this.start = start;
/* 1104 */     this.end = end;
/* 1105 */     this.name = name;
/*      */   }
/*      */ 
/*      */   public int getStart()
/*      */   {
/* 1115 */     return this.start;
/*      */   }
/*      */ 
/*      */   public int getEnd()
/*      */   {
/* 1124 */     return this.end;
/*      */   }
/*      */ 
/*      */   public String getName()
/*      */   {
/* 1133 */     return this.name;
/*      */   }
/*      */ 
/*      */   public int getSize()
/*      */   {
/* 1142 */     return this.db.getSts().getUnitSize(this.start, this.end);
/*      */   }
/*      */ 
/*      */   public Sample getNearestSample(float index)
/*      */   {
/* 1153 */     int iSize = 0;
/* 1154 */     SampleSet sts = this.db.getSts();
/*      */ 
/* 1157 */     for (int i = this.start; i < this.end; ++i) {
/* 1158 */       Sample sample = sts.getSample(i);
/* 1159 */       int nSize = iSize + sample.getResidualSize();
/*      */ 
/* 1161 */       if (Math.abs(index - iSize) < Math.abs(index - nSize))
/*      */       {
/* 1163 */         return sample;
/*      */       }
/* 1165 */       iSize = nSize;
/*      */     }
/* 1167 */     return sts.getSample(this.end - 1);
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1176 */     return getName();
/*      */   }
/*      */ 
/*      */   public void dump()
/*      */   {
/*      */   }
/*      */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.clunits.ClusterUnit
 * JD-Core Version:    0.5.4
 */