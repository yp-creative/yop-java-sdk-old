#!/bin/bash
#set -x
mvn clean install deploy -Dmaven.test.skip=true -Ddbinit.skip -Pjdk18
mv -f target/*.zip .

mvn clean install deploy -Dmaven.test.skip=true -Ddbinit.skip -Pjdk17
mv -f target/*.zip .

mvn clean install deploy -Dmaven.test.skip=true -Ddbinit.skip -Pjdk16
mv -f target/*.zip .

md5 *.zip
du -h *.zip

# package(){
# 	mvn clean install deploy -Dmaven.test.skip=true -Ddbinit.skip -Pjdk18
# 	$filename = `ls target/*.zip`


# 	mv -f target/*.zip .
	
# 	echo "Package for ${app_name}"
# }

exit 0
