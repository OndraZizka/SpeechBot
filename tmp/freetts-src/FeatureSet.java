package com.sun.speech.freetts;

import java.io.PrintWriter;

public abstract interface FeatureSet extends Dumpable
{
  public abstract boolean isPresent(String paramString);

  public abstract void remove(String paramString);

  public abstract String getString(String paramString);

  public abstract int getInt(String paramString);

  public abstract float getFloat(String paramString);

  public abstract Object getObject(String paramString);

  public abstract void setInt(String paramString, int paramInt);

  public abstract void setFloat(String paramString, float paramFloat);

  public abstract void setString(String paramString1, String paramString2);

  public abstract void setObject(String paramString, Object paramObject);

  public abstract void dump(PrintWriter paramPrintWriter, int paramInt, String paramString);
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FeatureSet
 * JD-Core Version:    0.5.4
 */