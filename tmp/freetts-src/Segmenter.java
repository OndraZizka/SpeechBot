/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import com.sun.speech.freetts.lexicon.Lexicon;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class Segmenter
/*     */   implements UtteranceProcessor
/*     */ {
/*     */   private static final String STRESS = "1";
/*     */   private static final String NO_STRESS = "0";
/*     */ 
/*     */   public void processUtterance(Utterance utterance)
/*     */     throws ProcessException
/*     */   {
/*  52 */     if (utterance.getRelation("Word") == null) {
/*  53 */       throw new IllegalStateException("Word relation has not been set");
/*     */     }
/*  55 */     if (utterance.getRelation("Syllable") != null) {
/*  56 */       throw new IllegalStateException("Syllable relation has already been set");
/*     */     }
/*  58 */     if (utterance.getRelation("SylStructure") != null)
/*     */     {
/*  60 */       throw new IllegalStateException("SylStructure relation has already been set");
/*     */     }
/*  62 */     if (utterance.getRelation("Segment") != null) {
/*  63 */       throw new IllegalStateException("Segment relation has already been set");
/*     */     }
/*     */ 
/*  67 */     String stress = "0";
/*  68 */     Relation syl = utterance.createRelation("Syllable");
/*  69 */     Relation sylstructure = utterance.createRelation("SylStructure");
/*     */ 
/*  71 */     Relation seg = utterance.createRelation("Segment");
/*  72 */     Lexicon lex = utterance.getVoice().getLexicon();
/*  73 */     List syllableList = null;
/*     */ 
/*  75 */     Item word = utterance.getRelation("Word").getHead();
/*  76 */     for (; word != null; word = word.getNext()) {
/*  77 */       Item ssword = sylstructure.appendItem(word);
/*  78 */       Item sylItem = null;
/*  79 */       Item segItem = null;
/*  80 */       Item sssyl = null;
/*     */ 
/*  82 */       String[] phones = null;
/*     */ 
/*  84 */       Item token = word.getItemAs("Token");
/*  85 */       FeatureSet featureSet = null;
/*     */ 
/*  87 */       if (token != null) {
/*  88 */         Item parent = token.getParent();
/*  89 */         featureSet = parent.getFeatures();
/*     */       }
/*     */ 
/*  92 */       if ((featureSet != null) && (featureSet.isPresent("phones")))
/*  93 */         phones = (String[])featureSet.getObject("phones");
/*     */       else {
/*  95 */         phones = lex.getPhones(word.toString(), null);
/*     */       }
/*     */ 
/*  98 */       for (int j = 0; j < phones.length; ++j) {
/*  99 */         if (sylItem == null) {
/* 100 */           sylItem = syl.appendItem();
/* 101 */           sssyl = ssword.addDaughter(sylItem);
/* 102 */           stress = "0";
/* 103 */           syllableList = new ArrayList();
/*     */         }
/* 105 */         segItem = seg.appendItem();
/* 106 */         if (isStressed(phones[j])) {
/* 107 */           stress = "1";
/* 108 */           phones[j] = deStress(phones[j]);
/*     */         }
/* 110 */         segItem.getFeatures().setString("name", phones[j]);
/* 111 */         sssyl.addDaughter(segItem);
/* 112 */         syllableList.add(phones[j]);
/* 113 */         if (lex.isSyllableBoundary(syllableList, phones, j + 1)) {
/* 114 */           sylItem = null;
/* 115 */           if (sssyl != null)
/* 116 */             sssyl.getFeatures().setString("stress", stress);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean isStressed(String phone)
/*     */   {
/* 134 */     return phone.endsWith("1");
/*     */   }
/*     */ 
/*     */   protected String deStress(String phone)
/*     */   {
/* 147 */     String retPhone = phone;
/* 148 */     if (isStressed(phone)) {
/* 149 */       retPhone = phone.substring(0, phone.length() - 1);
/*     */     }
/* 151 */     return retPhone;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 160 */     return "Segmenter";
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Segmenter
 * JD-Core Version:    0.5.4
 */