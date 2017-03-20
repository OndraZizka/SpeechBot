/*      */ package com.sun.speech.freetts.clunits;
/*      */ 
/*      */ import com.sun.speech.freetts.util.Utilities;
/*      */ import java.io.DataInputStream;
/*      */ import java.io.DataOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.nio.ByteBuffer;
/*      */ 
/*      */ class UnitType
/*      */ {
/*      */   private String name;
/*      */   private int start;
/*      */   private int count;
/*      */ 
/*      */   UnitType(String name, int start, int count)
/*      */   {
/* 1018 */     this.name = name;
/* 1019 */     this.start = start;
/* 1020 */     this.count = count;
/*      */   }
/*      */ 
/*      */   UnitType(DataInputStream is)
/*      */     throws IOException
/*      */   {
/* 1031 */     this.name = Utilities.getString(is);
/* 1032 */     this.start = is.readInt();
/* 1033 */     this.count = is.readInt();
/*      */   }
/*      */ 
/*      */   UnitType(ByteBuffer bb)
/*      */     throws IOException
/*      */   {
/* 1044 */     this.name = Utilities.getString(bb);
/* 1045 */     this.start = bb.getInt();
/* 1046 */     this.count = bb.getInt();
/*      */   }
/*      */ 
/*      */   String getName()
/*      */   {
/* 1055 */     return this.name;
/*      */   }
/*      */ 
/*      */   int getStart()
/*      */   {
/* 1064 */     return this.start;
/*      */   }
/*      */ 
/*      */   int getCount()
/*      */   {
/* 1073 */     return this.count;
/*      */   }
/*      */ 
/*      */   void dumpBinary(DataOutputStream os)
/*      */     throws IOException
/*      */   {
/* 1084 */     Utilities.outString(os, this.name);
/* 1085 */     os.writeInt(this.start);
/* 1086 */     os.writeInt(this.count);
/*      */   }
/*      */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.clunits.UnitType
 * JD-Core Version:    0.5.4
 */