

rm -rf m2repo

#  FreeTTS.
for FILE_PATH in `pwd`/freetts-1.2/lib/*.jar ; do
	BASENAME=`basename $FILE_PATH |  cut -d'.' -f1`
	mvn deploy:deploy-file \
                       -Durl=file://`pwd`/m2repo \
                       -DrepositoryId=some.id \
                       -Dfile=$FILE_PATH \
                       -DgroupId=cz.dynawest.ext.freetts \
                       -DartifactId=$BASENAME \
                       -Dversion=1.2 \
                       -Dpackaging=jar
done

#  MBrola - not needed, actually.
for FILE_PATH in `pwd`/freetts-1.2/mbrola/*.jar ; do
	BASENAME=`basename $FILE_PATH |  cut -d'.' -f1`
	mvn deploy:deploy-file \
                       -Durl=file://`pwd`/m2repo \
                       -DrepositoryId=some.id \
                       -Dfile=$FILE_PATH \
                       -DgroupId=de.dfki.lt.freetts \
                       -DartifactId=$BASENAME \
                       -Dversion=1.0 \
                       -Dpackaging=jar
done

#  PircBot.
for FILE_PATH in `pwd`/pircbot-1.5.0/*.jar ; do
	BASENAME=`basename $FILE_PATH |  cut -d'.' -f1`
	mvn deploy:deploy-file \
                       -Durl=file://`pwd`/m2repo \
                       -DrepositoryId=some.id \
                       -Dfile=$FILE_PATH \
                       -DgroupId=org.jibble.pircbot \
                       -DartifactId=$BASENAME \
                       -Dversion=1.5.0 \
                       -Dpackaging=jar
done


for FILE_PATH in `pwd`/freetts-1.2/lib/*.jar ; do
	BASENAME=`basename $FILE_PATH |  cut -d'.' -f1`
	echo  "<dependency> <groupId>cz.dynawest.ext.freetts</groupId> <artifactId>$BASENAME</artifactId> <version>1.2</version> </dependency>"
done
