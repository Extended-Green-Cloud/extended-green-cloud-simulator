#!/bin/bash
mvn install:install-file -Dfile='.\lib\jade.jar' -DgroupId='com.tilab.jade' -DartifactId=jade -Dversion='4.6' -Dpackaging=jar &&
mvn build
