/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ class OpEnum
/*     */ {
/* 251 */   private static Map map = new HashMap();
/*     */ 
/* 253 */   public static final OpEnum NEXT = new OpEnum("n");
/* 254 */   public static final OpEnum PREV = new OpEnum("p");
/* 255 */   public static final OpEnum NEXT_NEXT = new OpEnum("nn");
/* 256 */   public static final OpEnum PREV_PREV = new OpEnum("pp");
/* 257 */   public static final OpEnum PARENT = new OpEnum("parent");
/* 258 */   public static final OpEnum DAUGHTER = new OpEnum("daughter");
/* 259 */   public static final OpEnum LAST_DAUGHTER = new OpEnum("daughtern");
/* 260 */   public static final OpEnum RELATION = new OpEnum("R");
/*     */   private String name;
/*     */ 
/*     */   private OpEnum(String name)
/*     */   {
/* 270 */     this.name = name;
/* 271 */     map.put(name, this);
/*     */   }
/*     */ 
/*     */   public static OpEnum getInstance(String name)
/*     */   {
/* 280 */     return (OpEnum)map.get(name);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 285 */     return this.name;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.OpEnum
 * JD-Core Version:    0.5.4
 */