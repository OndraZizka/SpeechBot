/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ public class Gender
/*    */   implements Comparable
/*    */ {
/*    */   private final String name;
/* 23 */   private static int nextOrdinal = 0;
/*    */ 
/* 26 */   private final int ordinal = nextOrdinal++;
/*    */ 
/* 51 */   public static final Gender MALE = new Gender("MALE");
/*    */ 
/* 56 */   public static final Gender FEMALE = new Gender("FEMALE");
/*    */ 
/* 61 */   public static final Gender NEUTRAL = new Gender("NEUTRAL");
/*    */ 
/* 66 */   public static final Gender DONT_CARE = new Gender("DONT_CARE");
/*    */ 
/*    */   private Gender(String name)
/*    */   {
/* 28 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 35 */     return this.name;
/*    */   }
/*    */ 
/*    */   public int compareTo(Object o)
/*    */   {
/* 41 */     if ((o == DONT_CARE) || (this == DONT_CARE)) {
/* 42 */       return 0;
/*    */     }
/* 44 */     return this.ordinal - ((Gender)o).ordinal;
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Gender
 * JD-Core Version:    0.5.4
 */