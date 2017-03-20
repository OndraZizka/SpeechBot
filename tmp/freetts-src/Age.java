/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ public class Age
/*    */   implements Comparable
/*    */ {
/*    */   private final String name;
/* 22 */   private static int nextOrdinal = 0;
/*    */ 
/* 25 */   private final int ordinal = nextOrdinal++;
/*    */ 
/* 51 */   public static final Age CHILD = new Age("CHILD");
/*    */ 
/* 56 */   public static final Age TEENAGER = new Age("TEENAGER");
/*    */ 
/* 61 */   public static final Age YOUNGER_ADULT = new Age("YOUNGER_ADULT");
/*    */ 
/* 66 */   public static final Age MIDDLE_ADULT = new Age("MIDDLE_ADULT");
/*    */ 
/* 71 */   public static final Age OLDER_ADULT = new Age("OLDER_ADULT");
/*    */ 
/* 76 */   public static final Age NEUTRAL = new Age("NEUTRAL");
/*    */ 
/* 81 */   public static final Age DONT_CARE = new Age("DONT_CARE");
/*    */ 
/*    */   private Age(String name)
/*    */   {
/* 27 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 34 */     return this.name;
/*    */   }
/*    */ 
/*    */   public int compareTo(Object o)
/*    */   {
/* 41 */     if ((o == DONT_CARE) || (this == DONT_CARE)) {
/* 42 */       return 0;
/*    */     }
/* 44 */     return this.ordinal - ((Age)o).ordinal;
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Age
 * JD-Core Version:    0.5.4
 */