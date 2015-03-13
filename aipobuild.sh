# set in aipo-opensocial dir
# aipo is set in same dir
echo 'full build start!'
cd ../aipo
mvn clean
cd ../aipo-opensocial
mvn clean
mvn install
cd ../aipo
mvn install
cd ../aipo-opensocial
echo 'full build fin.'