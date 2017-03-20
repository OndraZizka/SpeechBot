

package cz.dynawest.speechbot;


import org.jibble.pircbot.*;
import cz.dynawest.speechbot.synt.*;
import java.io.File;



public class SpeechBot extends PircBot {

    private boolean noisy = true;
    private ISpeaker speaker;
    
    
	/**
	 *  Const
	 *  @param name 
	 */
	public SpeechBot(String name) {
		this.setName(name);
		//this.speaker = new JavaSpeechSpeaker();
		this.speaker = new MbrolaSpeaker();
    }
		
    
		
	/**
	 *  Message callback.
	 */
	@Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {

		String nickNorm = IrcUtils.normalizeUserNick( sender );

        message = message.trim();
        if (message.toLowerCase().startsWith("!send ")) {
            String input = message.substring(6);
            String filename = "./archive/SpeechBot-" + System.currentTimeMillis() + ".wav";
			File file = new File(filename);
            //this.speaker.speakToFile( input, file );
            dccSendFile( file, sender, 120000 );
        }
        if (message.toLowerCase().startsWith("!say ")) {
            String input = sender + " on " + channel + " says: " + message.substring(5);
            System.out.println( input );
            this.speaker.speak( input );
        }
        else if( this.noisy ) {
            //String input = sender + " on " + channel + " says: " + message;
            String input = sender + " says: " + message;
            System.out.println( input );
            this.speaker.speak( input );
        }
    }



    /**
	 * Main
	 */
    public static void main(String[] args) throws Exception {
        SpeechBot bot = new SpeechBot("mjfox");
        if( args.length < 2 ){
			System.err.println("  Usage: java -classpath ... SpeechBot irc.freenode.net #mychannel");
			System.exit(1);
		}
		System.out.println("  Connecting "+ args[0] + " ...");
        bot.connect(args[0]);
		System.out.println("  Joining    "+ args[1] + " ...");
        bot.joinChannel(args[1]);
    }
		
}// class
