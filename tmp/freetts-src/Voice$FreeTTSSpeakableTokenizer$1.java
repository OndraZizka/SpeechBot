/*      */ package com.sun.speech.freetts;
/*      */ 
/*      */ import java.util.ArrayList;
/*      */ import java.util.Iterator;
/*      */ 
/*      */ class Voice$FreeTTSSpeakableTokenizer$1
/*      */   implements Iterator
/*      */ {
/* 1450 */   boolean first = true;
/* 1451 */   Token savedToken = null;
/*      */   private final Voice.FreeTTSSpeakableTokenizer this$1;
/*      */ 
/*      */   public boolean hasNext()
/*      */   {
/* 1459 */     return (this.savedToken != null) || (this.this$1.tok.hasMoreTokens());
/*      */   }
/*      */ 
/*      */   public Object next()
/*      */   {
/* 1469 */     ArrayList tokenList = new ArrayList();
/* 1470 */     Utterance utterance = null;
/*      */ 
/* 1472 */     if (this.savedToken != null) {
/* 1473 */       tokenList.add(this.savedToken);
/* 1474 */       this.savedToken = null;
/*      */     }
/*      */ 
/* 1477 */     while (this.this$1.tok.hasMoreTokens()) {
/* 1478 */       Token token = this.this$1.tok.getNextToken();
/* 1479 */       if ((token.getWord().length() == 0) || (tokenList.size() > 500) || (this.this$1.tok.isBreak()))
/*      */       {
/* 1482 */         this.savedToken = token;
/* 1483 */         break;
/*      */       }
/* 1485 */       tokenList.add(token);
/*      */     }
/* 1487 */     utterance = new Utterance(Voice.FreeTTSSpeakableTokenizer.access$400(this.this$1), tokenList);
/* 1488 */     utterance.setSpeakable(this.this$1.speakable);
/* 1489 */     utterance.setFirst(this.first);
/* 1490 */     this.first = false;
/* 1491 */     boolean isLast = (!this.this$1.tok.hasMoreTokens()) && (((this.savedToken == null) || (this.savedToken.getWord().length() == 0)));
/*      */ 
/* 1495 */     utterance.setLast(isLast);
/* 1496 */     return utterance;
/*      */   }
/*      */ 
/*      */   public void remove() {
/* 1500 */     throw new UnsupportedOperationException("remove");
/*      */   }
/*      */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Voice.FreeTTSSpeakableTokenizer.1
 * JD-Core Version:    0.5.4
 */