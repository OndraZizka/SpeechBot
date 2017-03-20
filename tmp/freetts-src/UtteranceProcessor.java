package com.sun.speech.freetts;

public abstract interface UtteranceProcessor
{
  public abstract void processUtterance(Utterance paramUtterance)
    throws ProcessException;
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.UtteranceProcessor
 * JD-Core Version:    0.5.4
 */