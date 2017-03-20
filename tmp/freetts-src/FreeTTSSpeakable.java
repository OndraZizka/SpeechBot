package com.sun.speech.freetts;

import java.io.InputStream;
import org.w3c.dom.Document;

public abstract interface FreeTTSSpeakable
{
  public abstract void started();

  public abstract void completed();

  public abstract void cancelled();

  public abstract boolean isCompleted();

  public abstract boolean waitCompleted();

  public abstract boolean isPlainText();

  public abstract boolean isStream();

  public abstract boolean isDocument();

  public abstract String getText();

  public abstract Document getDocument();

  public abstract InputStream getInputStream();
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FreeTTSSpeakable
 * JD-Core Version:    0.5.4
 */