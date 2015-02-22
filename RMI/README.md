# Instruction

* Compile all the classes
* Type ``` rmic ChatHandler ```
* Type ``` rmiregistry <port>``` 
* To start the server: ``` java -Djava.rmi.server.codebase=<your classes folder> -Djava.security.policy=<the location of security.policy file> ChatServer ``` 
* To start the client: ``` java -Djava.rmi.server.codebase=<your classes folder> -Djava.security.policy=<the location of security.policy file> ChatClient ```