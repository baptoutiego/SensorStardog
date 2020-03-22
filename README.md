# build Project

In Eclipse, import the two projects independantly : 

- For demoIotConnector :

import.. -> General -> Existing project into workspace ...browse directory and finish

- For hygronometric :

import.. -> Gradle -> Existing gradle project ...browse directory and finish

The demoIotConnector project is only used to add dependancies to the main project which is hygronometric. We only run the mains of this project.
To reset the database, run the main of StardogClient.java
To start storing the datas of the captors to the database, start the stardog client then run IoTClient.java
