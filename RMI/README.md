# Instruction

* Compile all the classes
* Type ``` rmic ChatHandler ```
* Type ``` rmiregistry <port>``` 
* To start the server: ``` java -Djava.rmi.server.codebase=<your classes folder URL> -Djava.security.policy=<the location of security.policy file URL> ChatServer ``` 
* To start the client: ``` java -Djava.rmi.server.codebase=<your classes folder URL> -Djava.security.policy=<the location of security.policy file URL> ChatClient ```


With this you will be able to test client and server in the same machine.