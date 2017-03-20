
#java -Dmbrola.base=/home/ondra/work/JawaBot/SpeechBot/mbrola301 -classpath .:./pircbot-1.5.0/pircbot.jar:freetts-1.2/lib/freetts.jar SpeechBot porky.stuttgart.redhat.com '#mychannel'

FREETTS=freetts-1.2/lib/
FREETSS_LIBS=$FREETSS/cmu_time_awb.jar:$FREETSS/cmu_us_kal.jar:$FREETSS/cmulex.jar:$FREETSS/cmutimelex.jar:$FREETSS/en_us.jar:$FREETSS/mbrola.jar

MBROLA_HOME=/home/ondra/work/JawaBot/SpeechBot/mbrola301

java -Dmbrola.base=$MBROLA_HOME -classpath .:./pircbot-1.5.0/pircbot.jar:freetts-1.2/lib/freetts.jar:$FREETSS_LIBS SpeechBot porky.stuttgart.redhat.com '#jbossas'