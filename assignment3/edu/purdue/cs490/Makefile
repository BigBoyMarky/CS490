all: ChatClient.class

#ChatClientMessage.class: ChatClientMessage.java
#	javac ChatClientMessage.java
#Process.class: Process.java
#	javac Process.java
#BroadcastReceiver.class: BroadcastReceiver.java
#	javac Message.java ChannelInterface.java ClientObject.java Process.java ChatClientMessage.java BroadcastReceiver.java
#ClientObject.class: ClientObject.java
#	javac Process.java ClientObject.java
#Receiver.class: Receiver.java
#	javac BroadcastReceiver.java Receiver.java
#BroadcastReceiver.class: BroadcastReceiver.java
#	javac Message.java BroadcastReceiver.java
ChatClient.class: ChatClient.java
	javac \
		Message.java \
		BroadcastReceiver.java \
		Process.java \
		ClientObject.java \
		ChannelInterface.java \
		ChatClient.java \
		ChatClientMessage.java \
		ReliableBroadcast.java \
		MultiThreadedChatServer.java \
		ReliableBroadcaster.java \
		BEBroadcaster.java \
		FIFOReliableBroadcast.java \
		FIFOReliableBroadcaster.java \
		VectorClock.java

#FIFOReliableBroadCast.java
#Caster.class: Caster.java
#	javac Message.java ChannelInterface.java ClientObject.java Process.java ChatClientMessage.java Caster.java
#ChannelInterface.class: ChannelInterface.java
#	javac ChannelInterface.java
#Message.class: Message.java
#	javac Message.java
clean:
	rm -f *.class