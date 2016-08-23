rem java -cp . -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=y -Dfile.encoding=UTF-8 -jar clouddesk-jar-with-dependencies.jar down
java -cp . -Dfile.encoding=UTF-8 -jar clouddesk-jar-with-dependencies.jar down
pause
