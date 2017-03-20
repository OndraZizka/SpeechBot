/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ public class InputMode
/*    */ {
/*    */   private final String name;
/* 31 */   public static final InputMode NONE = new InputMode("none");
/*    */ 
/* 36 */   public static final InputMode FILE = new InputMode("file");
/*    */ 
/* 41 */   public static final InputMode TEXT = new InputMode("text");
/*    */ 
/* 46 */   public static final InputMode URL = new InputMode("url");
/*    */ 
/* 51 */   public static final InputMode LINES = new InputMode("lines");
/*    */ 
/* 57 */   public static final InputMode INTERACTIVE = new InputMode("interactive");
/*    */ 
/*    */   private InputMode(String name)
/*    */   {
/* 21 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public String toString() {
/* 25 */     return this.name;
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.InputMode
 * JD-Core Version:    0.5.4
 */