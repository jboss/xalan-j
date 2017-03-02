How to build
------------
Switch to jdk 1.8

First add servlet.jar and ejb.jar to the classpath

./build.sh clean dist

Deploy to local Maven repository to test with other projects (ensure that maven/pom*.xml are updated as per below)
mvn install:install-file \
                       -DpomFile=maven/pom-xalan.xml \
                       -Dfile=build/xalan.jar \
                       -Dsources=build/xalan-sources.jar 

# Deploy serializer.jar
mvn install:install-file \
                       -DpomFile=maven/pom-serializer.xml \
                       -Dfile=build/serializer.jar \
                       -Dsources=build/serializer-sources.jar 

# Deploy the full source zip
mvn install:install-file \
                       -DpomFile=maven/pom-xalan.xml \
                       -Dfile=build/xalan-j_2_7_1-src.zip \
                       -Dpackaging=zip \
                       -Dclassifier=source-release

Deploy to the Maven repository
------------------------------
First update the pom files maven/pom-serializer.xml and maven/pom-xalan.xml
maven/deploy.sh

