##!/usr/bin/bash


##

if [ 0 == 1 ] ; then
sudo strace -e trace=all -o log.txt \
java -Xrunjdwp:transport=dt_socket,server=y,address=4000 \
     -Djpda.listen=true -Djpda.address=4000 \
     -Dmbrola.base="`pwd`/../mbrola301"\
     -Dfreetts.voicespath="`pwd`/../mbrola301/voices"\
     -jar /home/ondra/work/BOTS/SpeechBot/SpeechBot-mavenized/target/SpeechBot-1.0-SNAPSHOT.jar \
     irc.eng.brq.redhat.com '#some'
else
#sudo strace -e verbose=all  \
java \
     -Dmbrola.base="`pwd`/../mbrola301"\
     -Dfreetts.voicespath="`pwd`/../mbrola301/voices"\
     -jar /home/ondra/work/BOTS/SpeechBot/SpeechBot-mavenized/target/SpeechBot-1.0-SNAPSHOT.jar \
     irc.eng.brq.redhat.com '#some'
     #2>&1 | grep jar
fi

##  Specifying voices.txt location:
# -Dfreetts.voicesfile=...voices.txt

##  Otherwise, it uses .getResourceAsStream("internal_voices.txt");

##  This leads to maven repo:
# voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromFile(getBaseDirectory() + "voices.txt"));


##  Other options:

# String voiceClasses = System.getProperty("freetts.voices");
# boolean noexpansion = Boolean.getBoolean("freetts.nocpexpansion");
# String voicesFile = System.getProperty("freetts.voicesfile");
# String voicesPath = System.getProperty("freetts.voicespath", "");  <jar>:<jar>:...

