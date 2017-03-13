# README #

Test task for
https://drive.google.com/drive/folders/0ByBbp_SbF9I8Z05WZEZSUTJxM2c

#To build
cd {base directory of project}
mvn clean install

#To run server
cd {base directory of project}/server/target
java -jar {server jar}

#To run client
cd {base directory of project}/client/target
java -jar {client jar}

#To configure server and client please edit properties file
{base directory of project}/client/target/app.properties  for client
{base directory of project}/server/target/app.properties  for server

Please read comments in properties file to configure client and server