/*     */ package com.sun.speech.freetts.util;
/*     */ 
/*     */ import com.sun.speech.freetts.FeatureSet;
/*     */ import com.sun.speech.freetts.Item;
/*     */ import com.sun.speech.freetts.Relation;
/*     */ 
/*     */ public class SegmentRelationUtils
/*     */ {
/*     */   public static Item getItem(Relation segmentRelation, float time)
/*     */   {
/*  34 */     Item lastSegment = segmentRelation.getTail();
/*     */ 
/*  40 */     float lastSegmentEndTime = getSegmentEnd(lastSegment);
/*     */ 
/*  43 */     if ((time < 0.0F) || (lastSegmentEndTime < time))
/*  44 */       return null;
/*  45 */     if (lastSegmentEndTime - time > time) {
/*  46 */       return findFromFront(segmentRelation, time);
/*     */     }
/*  48 */     return findFromEnd(segmentRelation, time);
/*     */   }
/*     */ 
/*     */   public static float getSegmentEnd(Item segment)
/*     */   {
/*  61 */     FeatureSet segmentFeatureSet = segment.getFeatures();
/*  62 */     return segmentFeatureSet.getFloat("end");
/*     */   }
/*     */ 
/*     */   public static Item findFromFront(Relation segmentRelation, float time)
/*     */   {
/*  75 */     Item item = segmentRelation.getHead();
/*     */ 
/*  78 */     while ((item != null) && (time > getSegmentEnd(item))) {
/*  79 */       item = item.getNext();
/*     */     }
/*     */ 
/*  82 */     return item;
/*     */   }
/*     */ 
/*     */   public static Item findFromEnd(Relation segmentRelation, float time)
/*     */   {
/*  95 */     Item item = segmentRelation.getTail();
/*     */ 
/*  98 */     while ((item != null) && (getSegmentEnd(item) > time)) {
/*  99 */       item = item.getPrevious();
/*     */     }
/*     */ 
/* 102 */     if (item != segmentRelation.getTail()) {
/* 103 */       item = item.getNext();
/*     */     }
/*     */ 
/* 106 */     return item;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.util.SegmentRelationUtils
 * JD-Core Version:    0.5.4
 */