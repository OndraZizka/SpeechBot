package com.sun.speech.freetts;

public abstract interface FeatureProcessor
{
  public abstract String process(Item paramItem)
    throws ProcessException;
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FeatureProcessor
 * JD-Core Version:    0.5.4
 */