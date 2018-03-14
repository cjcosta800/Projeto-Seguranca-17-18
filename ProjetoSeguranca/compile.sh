#!/bin/bash



echo Compiling Server
javac -cp bin/ -d bin/ ./PhotoShareServer/src/ServerLogic.java
javac -cp bin/ -d bin/ ./PhotoShareServer/src/PhotoShareServer.java
echo Compiling Client
javac -cp bin/ -d bin/ ./PhotoShareClient/src/ClientLogic.java
javac -cp bin/ -d bin/ ./PhotoShareClient/src/PhotoShare.java

