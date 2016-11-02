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
		

	public void run() {
		
		try {
			
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			while(clientSocket.isConnected()){
				
				// receive message from user
				String message = inFromClient.readLine();
				
				// process the message the user sent
				processCommand(message);
				System.out.println(message);
				outToClient.writeBytes("Processed.\n");
			}
			
		} catch(Exception e) {
			
			System.out.println("Something went wrong in run. \n");
		}

	}
		
	public void processCommand(String command) {
		
		String username;
		String message;

		String[] com = command.split(" ");
		
		try {
			
			switch(com[0]) {
				case "u":
					// set username for client
					username = com[1];
					System.out.println("Adding user: " + username + "\n");
					addUser(username);
					break;
				case "h":
					// print out help to user
					String help = "help";
					singleMessage(user, help);
					break;
				case "Q":
					// close this socket 
					kickUser(user);
					break;
				case "b":
					// broadcast the message
					message = user + ": " + command.substring(2, command.length());
					broadcast(message);
					break;
				case "s":
					// send single message to user
					username = com[1];
					message = user + ": " + command.substring(3 + username.length(), command.length());
					singleMessage(username, message);
					break;
				case "c":
					// send client list to user
					String listOfClients = getClientList();
					System.out.println(listOfClients);
					singleMessage(user, listOfClients);
					break;
				default:
					// inform user that their command doesn't work
					String incorrectCmd = "Please enter a correct command. \n";
					singleMessage(user, incorrectCmd);
					break;
			}
			
		} catch (Exception e) {
			
			System.out.println("Something went wrong in processCommand. \n");
			
		}
	}

	public void broadcast(String message) {
        try{
            
        for(Map.Entry<String, Socket> entry : clientList.entrySet()){
            String tempUser = entry.getKey();
            if(tempUser != user){
                singleMessage(tempUser, message);
            }
            else{
                continue;
            }
        }
        }catch(Exception e){
            System.out.println("Something went wrong with broadcast. \n");
        }
		
	}

	public void singleMessage(String username, String message) {
		
		// find the socket of the requested user
		Socket userSocket;
		userSocket = clientList.get(username);
		
		try {
			
			DataOutputStream outToClient = new DataOutputStream(userSocket.getOutputStream());
			
			// send message out on this socket
			outToClient.writeBytes(message + "\n");
			
		} catch (Exception e) {
			
			System.out.println("Something went wrong in singleMessage. \n");
			
		}
	}

	public String getClientList() {
		
		String cList = "Currently connected users: \n";
		
		// create a set of the keys
		Set temp = clientList.keySet();
		String[] cTemp = new String[clientList.size()];
		
		// add keys to an array
		for(int j = 0; j < temp.size(); j++) {
			cTemp[j] = temp.toArray()[j].toString();
		}
		
		// add each element of the array to a String
		// to send out to the user
		for(int i = 0; i < clientList.size(); i++) {
			cList += cTemp[i] + "\n";
		}
		return cList;
	}
		
	public void addUser(String username) {
		
		// add the username and socket to the HashMap
		clientList.put(username, clientSocket);
		
		// set this thread's user
		user = username;
	}

	public void kickUser(String username) {
		
		// find the socket of the requested user
		Socket userSocket;
		userSocket = clientList.get(username);
		
		try {
			// inform user that they're being logged out
			String logoutMsg = "You have been logged out. \n";
			singleMessage(username, logoutMsg);
			// close the socket
			userSocket.close();
			
		} catch (Exception e) {
			
			System.out.println("Something went wrong in kickUser. \n");
			
		}
		
		// remove the user from the client list
		clientList.remove(username);
		System.out.println(username + " signed out. \n");
	}
}
