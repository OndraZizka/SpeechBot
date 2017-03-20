package com.sun.speech.freetts;

public abstract interface PhoneSet
{
  public static final String VC = "vc";
  public static final String VLNG = "vlng";
  public static final String VHEIGHT = "vheight";
  public static final String VFRONT = "vfront";
  public static final String VRND = "vrnd";
  public static final String CTYPE = "ctype";
  public static final String CPLACE = "cplace";
  public static final String CVOX = "cvox";

  public abstract String getPhoneFeature(String paramString1, String paramString2);
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.PhoneSet
 * JD-Core Version:    0.5.4
 */