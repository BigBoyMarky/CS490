JCC = javac
default: ChatClient.class MultiThreadedChatServer.class

ChatClient.class: ChatClient.java ChannelInterface.java ClientObject.java
	$(JCC) ClientObject.java $(JCC) ChannelInterface.java $(JCC) ChatClient.java
#add Broadcasts here
MultiThreadedChatServer.class: MultiThreadedChatServer.java ClientObject.java
	$(JCC) MultiThreadedChatServer.java
#add DummyClients2 here