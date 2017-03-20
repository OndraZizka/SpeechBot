package com.sun.speech.freetts;

import java.io.Reader;

public abstract interface Tokenizer
{
  public abstract void setInputText(String paramString);

  public abstract void setInputReader(Reader paramReader);

  public abstract Token getNextToken();

  public abstract boolean hasMoreTokens();

  public abstract boolean hasErrors();

  public abstract String getErrorDescription();

  public abstract void setWhitespaceSymbols(String paramString);

  public abstract void setSingleCharSymbols(String paramString);

  public abstract void setPrepunctuationSymbols(String paramString);

  public abstract void setPostpunctuationSymbols(String paramString);

  public abstract boolean isBreak();
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Tokenizer
 * JD-Core Version:    0.5.4
 */