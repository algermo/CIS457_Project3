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
	
	/** Hashmap containing the connected clients (keys = client usernames) **/
    public ConcurrentHashMap <String, Socket> clientList = new ConcurrentHashMap<String, Socket>();
	
	/** The username of the client connected on this thread **/
    public String user;
			

	Socket clientSocket;
	ClientHandler(Socket connection, ConcurrentHashMap <String, Socket> clients) {
		clientSocket = connection;
        clientList = clients;
	}
		

	/*******************************************************************
	 * Runs the client thread
	 ******************************************************************/
	public void run() {
		
		try {
			
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			while(clientSocket.isConnected()){
				
				// receive message from user
				String message = inFromClient.readLine();
				
				if(message.equals("") || message.equals(" ")) {
					// inform user of empty message
					String emptyCmd = "Please enter a command. \n";
					singleMessage(user, emptyCmd);
				} else {
					// process the message the user sent
					processCommand(message);
				}

			}
			
		} catch(Exception e) {
			
			// System.out.println("Something went wrong in run.\n");
			// System.out.println("Error: " + e + "\n");
			
		}

	}
	
	/*******************************************************************
	 * Processes the command given by the client
	 * 
	 * @param command the client's command and any associated messages
	 ******************************************************************/
	public void processCommand(String command) {
		
		String username;
		String message;

		String[] com = command.split(" ");
		
		try {
			
			switch(com[0]) {
				case "u":
					// set username for client
					username = com[1];
					addUser(username);
					System.out.println("Added user " + username + "\n");
					break;
				case "h":
					// print out help to user
					String help = "help";
					singleMessage(user, help);
					break;
				case "Q":
					// close this socket 
					singleMessage(user, "Q");
					kickUser(user);
					break;
				case "k":
					// kick out the requested user
					username = com[1];
					singleMessage(username, "Q");
					kickUser(username);
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
			System.out.println("Error: " + e + "\n");
			
		}
	}

	/*******************************************************************
	 * Broadcasts the client's message to all connected clients
	 * 
	 * @param message the message being broadcasted
	 ******************************************************************/
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
			System.out.println("Error: " + e + "\n");
			
        }
		
	}

	/*******************************************************************
	 * Sends a single message from the client to another specified and
	 * connected client
	 * 
	 * @param username the user to receive the message
	 * @param message the single message being sent
	 ******************************************************************/
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
			System.out.println("Error: " + e + "\n");
			
		}
	}

	/*******************************************************************
	 * Gets the list of connected clients
	 * 
	 * @return String the list of connected clients
	 ******************************************************************/
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
	
	/*******************************************************************
	 * Adds a user to the HashMap of connected clients
	 * 
	 * @param username the username of the client to be added
	 ******************************************************************/
	public void addUser(String username) {
		
		// add the username and socket to the HashMap
		clientList.put(username, clientSocket);
		
		// set this thread's user
		user = username;
	}

	/*******************************************************************
	 * Disconnects the client socket with the associated username
	 * 
	 * @param username the username of the client to be disconnected
	 ******************************************************************/
	public void kickUser(String username) {
		
		// find the socket of the requested user
		Socket userSocket;
		userSocket = clientList.get(username);
		
		try {
			
			// close the socket
			userSocket.close();
			
		} catch (Exception e) {
			
			System.out.println("Something went wrong in kickUser. \n");
			System.out.println("Error: " + e + "\n");
			
		}
		
		// remove the user from the client list
		clientList.remove(username);
		System.out.println(username + " signed out. \n");
	}
}
