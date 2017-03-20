/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of SpeechBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: SpeechBot.java,v 1.1 2004/05/15 13:07:51 pjm2 Exp $

*/

//package org.jibble.speechbot;

import org.jibble.pircbot.*;
import com.sun.speech.freetts.*;
import com.sun.speech.freetts.audio.*;
import javax.sound.sampled.*;
import java.io.File;

public class SpeechBot extends PircBot {

    private boolean noisy = true;
    private Voice voice;
    private AudioPlayer voicePlayer;
    
    public SpeechBot(String name) {
        setName(name);

        //String voiceName = "kevin16";
        String voiceName = "mbrola_us1";
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(voiceName);

        if (voice == null) {
            System.out.println("Voice not found.");
            System.exit(1);
        }

        voice.allocate();

        //voicePlayer = new JavaClipAudioPlayer();
        //voicePlayer.setAudioFormat(new AudioFormat(8000, 16, 1, false, true));
    }
    
    // This method is not called from anywhere yet.
    public void exit() {
        voice.deallocate();
    }

    public void onMessage(String channel, String sender, String login, String hostname, String message) {

				String nickNorm = IrcUtils.normalizeUserNick( fromUser );

        message = message.trim();
        if (message.toLowerCase().startsWith("!send ")) {
            String input = message.substring(6);
            String filename = "./archive/SpeechBot-" + System.currentTimeMillis();
            AudioPlayer filePlayer = filePlayer = new SingleFileAudioPlayer(filename,  AudioFileFormat.Type.WAVE);
            filePlayer.setAudioFormat(new AudioFormat(8000, 16, 1, false, true));
            speak(input, filePlayer);
            filePlayer.close();
            dccSendFile(new File(filename + ".wav"), sender, 120000);
        }
        if (message.toLowerCase().startsWith("!say ")) {
            String input = sender + " on " + channel + " says: " + message.substring(5);
            System.out.println( input );
            speak(input, voicePlayer);
        }
        else if (noisy) {
            //String input = sender + " on " + channel + " says: " + message;
            String input = sender + " says: " + message;
            System.out.println( input );
            speak(input, voicePlayer);
        }
    }

    private void speak(String input, AudioPlayer player) {
        //voice.setAudioPlayer(player);
        voice.speak(input);
    }


    /** Main */
    public static void main(String[] args) throws Exception {
        SpeechBot bot = new SpeechBot("mjfox");
        if( args.length < 2 ){
          System.err.println("  Usage: java -classpath ... SpeechBot irc.freenode.net #mychannel");
          System.exit(1);
		  }
        bot.connect(args[0]);
        bot.joinChannel(args[1]);
    }
}
