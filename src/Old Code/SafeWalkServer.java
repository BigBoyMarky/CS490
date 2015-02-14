import java.io.*; import java.util.*; import java.net.*;
.
/**
PROJECT 5
@author Charlie Su, su99, 818
@author Saurav Khanna, khanna16, 818
*/

public class SafeWalkServer implements Runnable
{
    static SafeWalkServer server;
    ServerSocket serverSocket;
    static Thread myThread;
    
    public static void main(String[] args) throws IOException
    {
        if(args.length > 0)
        {
            try
            {
                int port = Integer.parseInt(args[0]);
                if(port >=1025 && port <= 65535)
                {
                    server = new SafeWalkServer(port);
                    myThread = new Thread(server);
                }
                else
                {
                    System.out.println("Port number is out of range. Try a port between 1025 and 65535. This program will end.");
                    return;
                }
            }
            catch(Exception e)
            {
                System.out.println("Invalid input. Please specify the port number with integers. Also make sure the port is between 1025 and 65535");
            }
        }
        else
        {
            server = new SafeWalkServer();
            System.out.println("Port not specified. Using free port " + server.getLocalPort());
            myThread = new Thread(server);
        }
        myThread.start();
    }

    public SafeWalkServer(int port) throws IOException 
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
    }
 
    public SafeWalkServer() throws IOException 
    {
        serverSocket = new ServerSocket(0);
        serverSocket.setReuseAddress(true);
    }
    
    public void run()
    {
        try
        {
            ArrayList<Socket> socketList = new ArrayList<Socket>();
			ArrayList<String> nameList = new ArrayList<String>();
			ArrayList<String> fromList = new ArrayList<String>(); 
			ArrayList<String> toList = new ArrayList<String>();
			ArrayList<Integer> priorityList = new ArrayList<Integer>();						
			int counter = 1;
            while(true)
            {
                Socket socket = serverSocket.accept();
				//System.out.println("Connected with client #" + counter);
				++counter;			
				PrintWriter print = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
                String s1 = reader.readLine();
				//System.out.println("MESSAGE RECEIVED:" + s1);

                if (s1.equals(":LIST_PENDING_REQUESTS"))
                {
					print.print("[");
					int nameListSize = nameList.size();
					for(int i = 0; i < nameListSize-1; i++)
					{
						print.print("[" + nameList.get(i) + ", " + fromList.get(i) + ", " + toList.get(i) + ", " + priorityList.get(i) + "], ");
					}
					print.print("[" + nameList.get(nameListSize-1) + ", " + fromList.get(nameListSize-1) + ", " + toList.get(nameListSize-1) + ", " + priorityList.get(nameListSize-1) + "]");
					
					print.println("]");
					continue;
					//oos.flush();
                }
                else if (s1.equals(":RESET"))
                {
					print.println("RESPONSE: success");
					int nameListSize = nameList.size();
					for(int i = 0; i < nameListSize; i++)
					{
						//oos = new ObjectOutputStream(socketList.get(i).getOutputStream());
						print = new PrintWriter(socketList.get(i).getOutputStream(),true);
						print.println("ERROR: connection reset");
						//oos.flush(); // ensure data is sent to the client
						//oos.close();
					}
					print.close();
					reader.close();
					continue;
                }
                else if (s1.equals(":SHUTDOWN"))
                {
					print.println("RESPONSE: success");
					int nameListSize = nameList.size();
					for(int i = 0; i < nameListSize; i++)
					{
						//oos = new ObjectOutputStream(socketList.get(i).getOutputStream());
						print = new PrintWriter(socketList.get(i).getOutputStream(),true);
						print.println("ERROR: connection reset");
						//oos.flush(); // ensure data is sent to the client
						//oos.close();
					}					
					print.close();
					reader.close();
					return;
                }								
				int length = s1.length();				
				String token1, token2, token3, token4;				
				int index1 = s1.indexOf(",");
				if(index1 < 0)
				{
					print.println("ERROR: invalid request");
					print.close();
					reader.close();					
					continue;
				}
				token1 = s1.substring(0, index1);
				int index2 = s1.substring(index1+1,length).indexOf(",");
				if(index2 < 0)
				{
					print.println("ERROR: invalid request");	
					print.close();
					reader.close();
					continue;
				}
				token2 = s1.substring(index1+1,index1+1+index2);
				int index3 = s1.substring(index1+index2+2, length).indexOf(",");
				if(index3 < 0)
				{
					print.println("ERROR: invalid request");	
					print.close();
					reader.close();					
					continue;				
				}
				token3 = s1.substring(index1+index2+2,length-2);
				token4 = s1.substring(length-1, length);
				//System.out.println(token1 + " " + token2+ " " + token3+ " "+token4);				
				//remember: substring(startIndex (inclusive), endIndex (exclusive))
				if((token2.equals("CL50") || token2.equals("PMU") || token2.equals("EE") || token2.equals("LWSN") || token2.equals("PUSH")))
				{
					if((token3.equals("CL50") || token3.equals("PMU") || token3.equals("EE") || token3.equals("LWSN") || token3.equals("PUSH") || token3.equals("*")) && !token3.equals(token2))
					{
						if(token4.equals("0"))
						{
							socketList.add(socket);
							nameList.add(token1);
							fromList.add(token2);
							toList.add(token3);
							priorityList.add(Integer.parseInt(token4));
							//System.out.println(token1 + " " + token2 + " " + token3 + " " + token4 + " added to ArrayList!" + socketList.size());
							//all are true, therefore we must add them to our arraylists
						}
						else
						{
						print.println("ERROR: invalid request");	
						print.close();
						reader.close();
						continue;
						}
					}
					else
					{
						print.println("ERROR: invalid request");	
						print.close();
						reader.close();
						continue;		
					}					
				}
				else
				{
					print.println("ERROR: invalid request");	
					print.close();
					reader.close();					
					continue;		
				}
				
				//After parsing the request, we say ON HOLD
				//We add the request to a list
				//We go through list and look for matches
				int max = nameList.size();
				for(int listIndex = 0; listIndex < max; listIndex++)
				{//we will be going through everyone
					//FROM and TO
					for(int j = listIndex+1; j < max; j++)
					{
						if(fromList.get(listIndex).equals(fromList.get(listIndex+j)))
						{
							if(toList.get(listIndex).equals(toList.get(listIndex+j)) && !(toList.get(listIndex).equals("*") && toList.get(listIndex + j).equals("*")) || toList.get(listIndex).equals("*") && !(toList.get(listIndex+j).equals("*")) || toList.get(listIndex+j).equals("*") && !(toList.get(listIndex).equals("*") ))
							{
								print = new PrintWriter(socketList.get(listIndex).getOutputStream(),true);				
								print.println("RESPONSE: " + nameList.get(listIndex+j) + "," + fromList.get(listIndex+j) + ","+toList.get(listIndex+j) + ","+priorityList.get(listIndex+j));
						
								print = new PrintWriter(socketList.get(listIndex+j).getOutputStream(),true);
								print.println("RESPONSE: " + nameList.get(listIndex) + "," + fromList.get(listIndex) + ","+toList.get(listIndex) + ","+priorityList.get(listIndex));
								socketList.remove(listIndex + j);
								nameList.remove(listIndex + j);
								fromList.remove(listIndex + j);
								toList.remove(listIndex + j);
								priorityList.remove(listIndex + j);								
								print.println(nameList.get(listIndex));	
								//oos.flush();
								socketList.remove(listIndex);
								nameList.remove(listIndex);
								fromList.remove(listIndex);
								toList.remove(listIndex);
								priorityList.remove(listIndex);
								max = nameList.size();	//becayse arraylist resized
								//match found, print & remove from lists
							}
						}
					}
				}
            }
        }
        catch (Exception e)
        {
			System.err.println(e);
            System.out.println("Something went terribly wrong.");
        }
    }
    public int getLocalPort()
    {
        return this.serverSocket.getLocalPort();
    }
}