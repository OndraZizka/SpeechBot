/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.PrintWriter;
/*     */ 
/*     */ public class Relation
/*     */   implements Dumpable
/*     */ {
/*     */   private String name;
/*     */   private Utterance owner;
/*     */   private Item head;
/*     */   private Item tail;
/*     */   public static final String TOKEN = "Token";
/*     */   public static final String WORD = "Word";
/*     */   public static final String PHRASE = "Phrase";
/*     */   public static final String SEGMENT = "Segment";
/*     */   public static final String SYLLABLE = "Syllable";
/*     */   public static final String SYLLABLE_STRUCTURE = "SylStructure";
/*     */   public static final String TARGET = "Target";
/*     */   public static final String UNIT = "Unit";
/*     */ 
/*     */   Relation(String name, Utterance owner)
/*     */   {
/* 107 */     this.name = name;
/* 108 */     this.owner = owner;
/* 109 */     this.head = null;
/* 110 */     this.tail = null;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 119 */     return this.name;
/*     */   }
/*     */ 
/*     */   public Item getHead()
/*     */   {
/* 128 */     return this.head;
/*     */   }
/*     */ 
/*     */   void setHead(Item item)
/*     */   {
/* 137 */     this.head = item;
/*     */   }
/*     */ 
/*     */   public Item getTail()
/*     */   {
/* 146 */     return this.tail;
/*     */   }
/*     */ 
/*     */   void setTail(Item item)
/*     */   {
/* 155 */     this.tail = item;
/*     */   }
/*     */ 
/*     */   public Item appendItem()
/*     */   {
/* 165 */     return appendItem(null);
/*     */   }
/*     */ 
/*     */   public Item appendItem(Item originalItem)
/*     */   {
/*     */     ItemContents contents;
/*     */     ItemContents contents;
/* 181 */     if (originalItem == null)
/* 182 */       contents = null;
/*     */     else {
/* 184 */       contents = originalItem.getSharedContents();
/*     */     }
/* 186 */     Item newItem = new Item(this, contents);
/* 187 */     if (this.head == null) {
/* 188 */       this.head = newItem;
/*     */     }
/*     */ 
/* 191 */     if (this.tail != null) {
/* 192 */       this.tail.attach(newItem);
/*     */     }
/* 194 */     this.tail = newItem;
/* 195 */     return newItem;
/*     */   }
/*     */ 
/*     */   public Utterance getUtterance()
/*     */   {
/* 205 */     return this.owner;
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter pw, int pad, String title)
/*     */   {
/* 219 */     Utilities.dump(pw, pad, "========= Relation: " + title + " =========");
/*     */ 
/* 221 */     Item item = this.head;
/* 222 */     while (item != null) {
/* 223 */       item.dump(pw, pad + 4, title);
/* 224 */       item = item.getNext();
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Relation
 * JD-Core Version:    0.5.4
 */