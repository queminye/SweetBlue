#!/bin/sh

source config.sh
ver=$(sh echo_version.sh)
JAR_NAME=sweetblue_$ver
cp $STAGE/sweetblue.zip $STAGE/$JAR_NAME.zip
echo "\n${GLITZ}UPLOADING ZIPS TO SERVER${GLITZ}"
cd $STAGE
echo $JAR_NAME.zip
SERVER_ADDRESS="162.209.102.219"	     
sshpass -p $SWEETBLUE_COM_FTP_PASSWORD scp -p $JAR_NAME.zip "${SWEETBLUE_COM_FTP_USERNAME}@${SERVER_ADDRESS}:/var/www/html/sweetblue/downloads"
echo "next one..."
sshpass -p $SWEETBLUE_COM_FTP_PASSWORD scp -pv sweetblue.zip "${SWEETBLUE_COM_FTP_USERNAME}@${SERVER_ADDRESS}:/var/www/html/sweetblue/downloads"

if [ "$?" != 0 ];
then	
	ssh -o StrictHostKeyChecking=no sweetblue@162.209.102.219 uptime
	echo "Problem with scp. Attempted to setup host key, please try this script again."
	exit 1
fi
cd -