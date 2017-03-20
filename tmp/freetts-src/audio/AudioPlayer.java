package com.sun.speech.freetts.audio;

import javax.sound.sampled.AudioFormat;

public abstract interface AudioPlayer
{
  public abstract void setAudioFormat(AudioFormat paramAudioFormat);

  public abstract AudioFormat getAudioFormat();

  public abstract void pause();

  public abstract void resume();

  public abstract void reset();

  public abstract boolean drain();

  public abstract void begin(int paramInt);

  public abstract boolean end();

  public abstract void cancel();

  public abstract void close();

  public abstract float getVolume();

  public abstract void setVolume(float paramFloat);

  public abstract long getTime();

  public abstract void resetTime();

  public abstract void startFirstSampleTimer();

  public abstract boolean write(byte[] paramArrayOfByte);

  public abstract boolean write(byte[] paramArrayOfByte, int paramInt1, int paramInt2);

  public abstract void showMetrics();
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.AudioPlayer
 * JD-Core Version:    0.5.4
 */