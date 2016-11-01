/*
 * Molly Alger & Chris Fracssi
 * CIS457 F16
 * Project 3 Part 1 - TCP Chat Server
 * November 8, 2016
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class server {
    
    public static ConcurrentHashMap <String, Socket> clientList = new ConcurrentHashMap<String, Socket>();

	public static void main(String args[]) throws Exception {

		ServerSocket listenSocket = new ServerSocket(9876);
		while(true) {

			// list on socket and run thread for multiple connections
			Socket clientSocket = listenSocket.accept();	
			Runnable r = new ClientHandler(clientSocket, clientList);
			Thread t = new Thread(r);
			t.start();
		}

	}
}

class ClientHandler implements Runnable {
    public ConcurrentHashMap <String, Socket> clientList = new ConcurrentHashMap<String, Socket>();
    public String user;

		Socket clientSocket;
		ClientHandler(Socket connection, ConcurrentHashMap <String, Socket> clients) {
			clientSocket = connection;
            clientList = clients;
		}
		

		public void run(){
            getClientList();
			try {
				DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while(true){
					String message = inFromClient.readLine();
					// check what the message was
					// we can change the keywords based on what we want
					// formatting for keywords will be made in client
					processCommand(message);
                    System.out.println(getClientList());
                    
					System.out.println(message);
                }
                

			} catch(Exception e) {

				System.out.println("got an exception");

			}

		}
		
		public void processCommand(String command) {
            String username;
			String[] com = command.split(" ");
			String message = command.substring(3, command.length()-1);
			switch(com[0]) {
				case "u":
					// set username for client
					username = com[1];
                    addUser(username);
				case "h":
					// print out help to user
				case "Q":
					// close this socket 
					// kickUser(username)
				case "b":
					// broadcast the message
					 broadcast(message)
				case "s":
					// send single message to user
					username = com[1];
					// singleMessage(username, message)
				case "c":
					// send client list to user
					// getClientList()
				default:
					// inform user that their command doesn't work
			}
		}

		public void broadcast(String message) {
            for(int i = 0; i < clientList.size(); i++){
            
            }
		}

		public void singleMessage(String username, String message) {

		}

		public String getClientList() {
			String cList = "";
			Set temp = clientList.keySet();
			String[] cTemp = new String[clientList.size()];
			for(int j = 0; j < temp.size(); j++) {
				cTemp[j] = temp.toArray()[j].toString();
			}
			//cTemp = temp.toArray();
			for(int i = 0; i < clientList.size(); i++) {
				cList += cTemp[i] + "\n";
			}
			return cList;
		}
		
		public void addUser(String username) {
			clientList.put(username, clientSocket);
            user = username;
		}

		public void kickUser() {

		}
}
