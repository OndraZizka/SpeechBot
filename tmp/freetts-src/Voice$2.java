/*      */ package com.sun.speech.freetts;
/*      */ 
/*      */ import java.util.ArrayList;
/*      */ import java.util.Iterator;
/*      */ 
/*      */ class Voice$2
/*      */   implements Iterator
/*      */ {
/* 1446 */   boolean first = true;
/* 1447 */   Token savedToken = null;
/*      */   private final Voice.FreeTTSSpeakableTokenizer this$1;
/*      */ 
/*      */   public boolean hasNext()
/*      */   {
/* 1455 */     return (this.savedToken != null) || (this.this$1.tok.hasMoreTokens());
/*      */   }
/*      */ 
/*      */   public Object next()
/*      */   {
/* 1465 */     ArrayList tokenList = new ArrayList();
/* 1466 */     Utterance utterance = null;
/*      */ 
/* 1468 */     if (this.savedToken != null) {
/* 1469 */       tokenList.add(this.savedToken);
/* 1470 */       this.savedToken = null;
/*      */     }
/*      */ 
/* 1473 */     while (this.this$1.tok.hasMoreTokens()) {
/* 1474 */       Token token = this.this$1.tok.getNextToken();
/* 1475 */       if ((token.getWord().length() == 0) || (tokenList.size() > 500) || (this.this$1.tok.isBreak()))
/*      */       {
/* 1478 */         this.savedToken = token;
/* 1479 */         break;
/*      */       }
/* 1481 */       tokenList.add(token);
/*      */     }
/* 1483 */     utterance = new Utterance(Voice.FreeTTSSpeakableTokenizer.access$400(this.this$1), tokenList);
/* 1484 */     utterance.setSpeakable(this.this$1.speakable);
/* 1485 */     utterance.setFirst(this.first);
/* 1486 */     this.first = false;
/* 1487 */     boolean isLast = (!this.this$1.tok.hasMoreTokens()) && (((this.savedToken == null) || (this.savedToken.getWord().length() == 0)));
/*      */ 
/* 1491 */     utterance.setLast(isLast);
/* 1492 */     return utterance;
/*      */   }
/*      */ 
/*      */   public void remove() {
/* 1496 */     throw new UnsupportedOperationException("remove");
/*      */   }
/*      */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Voice.2
 * JD-Core Version:    0.5.4
 */