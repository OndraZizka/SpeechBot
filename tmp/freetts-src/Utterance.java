/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import com.sun.speech.freetts.util.SegmentRelationUtils;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.Serializable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class Utterance
/*     */   implements FeatureSet, Serializable
/*     */ {
/*     */   private Voice voice;
/*     */   private FeatureSetImpl features;
/*     */   private FeatureSetImpl relations;
/*     */   private boolean first;
/*     */   private boolean last;
/*     */   private FreeTTSSpeakable speakable;
/*     */ 
/*     */   public Utterance(Voice voice)
/*     */   {
/*  46 */     this.voice = voice;
/*  47 */     this.features = new FeatureSetImpl();
/*  48 */     this.relations = new FeatureSetImpl();
/*     */   }
/*     */ 
/*     */   public Utterance(Voice voice, List tokenList)
/*     */   {
/*  58 */     this(voice);
/*  59 */     setTokenList(tokenList);
/*     */   }
/*     */ 
/*     */   public void setSpeakable(FreeTTSSpeakable speakable)
/*     */   {
/*  68 */     this.speakable = speakable;
/*     */   }
/*     */ 
/*     */   public FreeTTSSpeakable getSpeakable()
/*     */   {
/*  77 */     return this.speakable;
/*     */   }
/*     */ 
/*     */   public Relation createRelation(String name)
/*     */   {
/*  89 */     Relation relation = new Relation(name, this);
/*  90 */     this.relations.setObject(name, relation);
/*  91 */     return relation;
/*     */   }
/*     */ 
/*     */   public Relation getRelation(String name)
/*     */   {
/* 103 */     return (Relation)this.relations.getObject(name);
/*     */   }
/*     */ 
/*     */   public boolean hasRelation(String name)
/*     */   {
/* 113 */     return this.relations.isPresent(name);
/*     */   }
/*     */ 
/*     */   public Voice getVoice()
/*     */   {
/* 122 */     return this.voice;
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter output, int pad, String title, boolean justRelations)
/*     */   {
/* 135 */     output.println(" ============ " + title + " ========== ");
/* 136 */     if (!justRelations) {
/* 137 */       this.voice.dump(output, pad + 4, "Voice");
/* 138 */       this.features.dump(output, pad + 4, "Features");
/*     */     }
/* 140 */     this.relations.dump(output, pad + 4, "Relations");
/* 141 */     output.flush();
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter output, int pad, String title)
/*     */   {
/* 152 */     dump(output, pad, title, false);
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter output, String title)
/*     */   {
/* 162 */     dump(output, 0, title, false);
/*     */   }
/*     */ 
/*     */   public void dump(String title)
/*     */   {
/* 171 */     dump(new PrintWriter(System.out), 0, title, false);
/*     */   }
/*     */ 
/*     */   public void dumpRelations(String title)
/*     */   {
/* 179 */     dump(new PrintWriter(System.out), 0, title, true);
/*     */   }
/*     */ 
/*     */   public boolean isPresent(String name)
/*     */   {
/* 191 */     if (!this.features.isPresent(name)) {
/* 192 */       return getVoice().getFeatures().isPresent(name);
/*     */     }
/* 194 */     return true;
/*     */   }
/*     */ 
/*     */   public void remove(String name)
/*     */   {
/* 204 */     this.features.remove(name);
/*     */   }
/*     */ 
/*     */   public String getString(String name)
/*     */   {
/* 221 */     if (!this.features.isPresent(name)) {
/* 222 */       return getVoice().getFeatures().getString(name);
/*     */     }
/* 224 */     return this.features.getString(name);
/*     */   }
/*     */ 
/*     */   public int getInt(String name)
/*     */   {
/* 242 */     if (!this.features.isPresent(name)) {
/* 243 */       return getVoice().getFeatures().getInt(name);
/*     */     }
/* 245 */     return this.features.getInt(name);
/*     */   }
/*     */ 
/*     */   public float getFloat(String name)
/*     */   {
/* 263 */     if (!this.features.isPresent(name)) {
/* 264 */       return getVoice().getFeatures().getFloat(name);
/*     */     }
/* 266 */     return this.features.getFloat(name);
/*     */   }
/*     */ 
/*     */   public Object getObject(String name)
/*     */   {
/* 281 */     if (!this.features.isPresent(name)) {
/* 282 */       return getVoice().getFeatures().getObject(name);
/*     */     }
/* 284 */     return this.features.getObject(name);
/*     */   }
/*     */ 
/*     */   public void setInt(String name, int value)
/*     */   {
/* 295 */     this.features.setInt(name, value);
/*     */   }
/*     */ 
/*     */   public void setFloat(String name, float value)
/*     */   {
/* 305 */     this.features.setFloat(name, value);
/*     */   }
/*     */ 
/*     */   public void setString(String name, String value)
/*     */   {
/* 315 */     this.features.setString(name, value);
/*     */   }
/*     */ 
/*     */   public void setObject(String name, Object value)
/*     */   {
/* 325 */     this.features.setObject(name, value);
/*     */   }
/*     */ 
/*     */   public Item getItem(String relation, float time)
/*     */   {
/* 340 */     Relation segmentRelation = null;
/*     */ 
/* 342 */     if ((segmentRelation = getRelation("Segment")) == null) {
/* 343 */       throw new IllegalStateException("Utterance has no Segment relation");
/*     */     }
/*     */ 
/* 347 */     String pathName = null;
/*     */ 
/* 349 */     if (!relation.equals("Segment"))
/*     */     {
/* 351 */       if (relation.equals("Syllable"))
/* 352 */         pathName = "R:SylStructure.parent.R:Syllable";
/* 353 */       else if (relation.equals("SylStructure"))
/* 354 */         pathName = "R:SylStructure.parent.parent";
/* 355 */       else if (relation.equals("Word"))
/* 356 */         pathName = "R:SylStructure.parent.parent.R:Word";
/* 357 */       else if (relation.equals("Token"))
/* 358 */         pathName = "R:SylStructure.parent.parent.R:Token.parent";
/* 359 */       else if (relation.equals("Phrase"))
/* 360 */         pathName = "R:SylStructure.parent.parent.R:Phrase.parent";
/*     */       else {
/* 362 */         throw new IllegalArgumentException("Utterance.getItem(): relation cannot be " + relation);
/*     */       }
/*     */     }
/*     */ 
/* 366 */     PathExtractor path = new PathExtractorImpl(pathName, false);
/*     */ 
/* 369 */     Item segmentItem = SegmentRelationUtils.getItem(segmentRelation, time);
/*     */ 
/* 372 */     if (relation.equals("Segment"))
/* 373 */       return segmentItem;
/* 374 */     if (segmentItem != null) {
/* 375 */       return path.findItem(segmentItem);
/*     */     }
/* 377 */     return null;
/*     */   }
/*     */ 
/*     */   public float getDuration()
/*     */   {
/* 390 */     float duration = -1.0F;
/* 391 */     if (((duration = getLastFloat("Segment", "end")) == -1.0F) && 
/* 392 */       ((duration = getLastFloat("Target", "pos")) == -1.0F)) {
/* 393 */       throw new IllegalStateException("Utterance: Error finding duration");
/*     */     }
/*     */ 
/* 397 */     return duration;
/*     */   }
/*     */ 
/*     */   private float getLastFloat(String relationName, String feature)
/*     */   {
/* 408 */     float duration = -1.0F;
/*     */     Relation relation;
/* 410 */     if ((relation = getRelation(relationName)) != null) {
/* 411 */       Item lastItem = relation.getTail();
/* 412 */       if (lastItem != null) {
/* 413 */         duration = lastItem.getFeatures().getFloat(feature);
/*     */       }
/*     */     }
/* 416 */     return duration;
/*     */   }
/*     */ 
/*     */   private void setInputText(List tokenList)
/*     */   {
/* 427 */     StringBuffer sb = new StringBuffer();
/* 428 */     for (Iterator i = tokenList.iterator(); i.hasNext(); ) {
/* 429 */       sb.append(i.next().toString());
/*     */     }
/* 431 */     setString("input_text", sb.toString());
/*     */   }
/*     */ 
/*     */   private void setTokenList(List tokenList)
/*     */   {
/* 444 */     setInputText(tokenList);
/*     */ 
/* 446 */     Relation relation = createRelation("Token");
/* 447 */     for (Iterator i = tokenList.iterator(); i.hasNext(); ) {
/* 448 */       Token token = (Token)i.next();
/* 449 */       String tokenWord = token.getWord();
/*     */ 
/* 451 */       if ((tokenWord != null) && (tokenWord.length() > 0)) {
/* 452 */         Item item = relation.appendItem();
/*     */ 
/* 454 */         FeatureSet featureSet = item.getFeatures();
/* 455 */         featureSet.setString("name", tokenWord);
/* 456 */         featureSet.setString("whitespace", token.getWhitespace());
/* 457 */         featureSet.setString("prepunctuation", token.getPrepunctuation());
/*     */ 
/* 459 */         featureSet.setString("punc", token.getPostpunctuation());
/* 460 */         featureSet.setString("file_pos", String.valueOf(token.getPosition()));
/*     */ 
/* 462 */         featureSet.setString("line_number", String.valueOf(token.getLineNumber()));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isFirst()
/*     */   {
/* 477 */     return this.first;
/*     */   }
/*     */ 
/*     */   public void setFirst(boolean first)
/*     */   {
/* 486 */     this.first = first;
/*     */   }
/*     */ 
/*     */   public boolean isLast()
/*     */   {
/* 497 */     return this.last;
/*     */   }
/*     */ 
/*     */   public void setLast(boolean last)
/*     */   {
/* 506 */     this.last = last;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Utterance
 * JD-Core Version:    0.5.4
 */