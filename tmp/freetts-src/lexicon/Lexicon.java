package com.sun.speech.freetts.lexicon;

import java.io.IOException;
import java.util.List;

public abstract interface Lexicon
{
  public abstract String[] getPhones(String paramString1, String paramString2);

  public abstract String[] getPhones(String paramString1, String paramString2, boolean paramBoolean);

  public abstract void addAddendum(String paramString1, String paramString2, String[] paramArrayOfString);

  public abstract void removeAddendum(String paramString1, String paramString2);

  public abstract boolean isSyllableBoundary(List paramList, String[] paramArrayOfString, int paramInt);

  public abstract void load()
    throws IOException;

  public abstract boolean isLoaded();
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.lexicon.Lexicon
 * JD-Core Version:    0.5.4
 */