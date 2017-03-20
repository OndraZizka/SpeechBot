package com.sun.speech.freetts;

import com.sun.speech.freetts.relp.Sample;

public abstract interface Unit
{
  public abstract String getName();

  public abstract int getSize();

  public abstract Sample getNearestSample(float paramFloat);

  public abstract void dump();
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Unit
 * JD-Core Version:    0.5.4
 */