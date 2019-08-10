# Java Coding Challenge - Weather API

This is a maven project which uses an embedded derby database to store weather data for different cities.

The table structure is provided below:

**Table name** - *cities_weather*

**Table columns:**
- *id* int not null generated always as identity,
- *city* varchar(256) not null,
- *date* date not null,
- *temperature* decimal(5,2),
- *wind* int,
- *rain* int,
- *primary key (id, city, date)*

Your challenge is to build a production grade API suite that uses HTTP to allow our customers to manage this weather data. 

## Requirements

You will need to provide APIs for the following actions:  
 
1. Fetch data for all the cities.
1. Fetch data for a record.
1. Ability to get the coldest or warmest city.
1. Add a new record.
1. Modify a record.
1. Delete a record.


## Pre-requisites
1. Java needs to be installed on the system and environment variable JAVA_HOME should be set correctly to the JDK path.  
   Check by running below command in command prompt  
   `java -version`  
2. Maven needs to be installed on the system.  
   Check by running below command in command prompt  
   `mvn -v`  

## Run the application
This project uses Jetty as an embedded container to host the web application.  
Goto the base folder of the application and execute the following command to launch the application.  
`mvn jetty:run`  

The application will be available at [http://localhost:8080](http://localhost:8080)  
 
You can replace the jetty plugin with something you feel comfortable using as well but make sure we have clear instructions to run your application. 
