package com.sun.speech.freetts.cart;

import com.sun.speech.freetts.Item;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract interface CART
{
  public abstract Object interpret(Item paramItem);

  public abstract void dumpBinary(DataOutputStream paramDataOutputStream)
    throws IOException;
}

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.cart.CART
 * JD-Core Version:    0.5.4
 */