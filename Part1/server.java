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
	
	public String cmd = "Q means QUIT\n"
			+ "u sets your username \n"
			+ "b broadcasts a message to all users connected \n"
			+ "s USERNAME sends a message to a specified user connected \n" 
			+ "c prints the list of users connected \n" 
			+ "h lists this set of commands again \n";
			

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
				
				// receive message from user
				String message = inFromClient.readLine();
				
				// process the message the user sent
				processCommand(message);
				System.out.println(message);
			}
			
		} catch(Exception e) {
			
			System.out.println("got an exception");
		}

	}
		
	public String processCommand(String command) {
		
		String username;
		String message;

		String[] com = command.split(" ");
		
		switch(com[1]) {
			case "u":
				// set username for client
				username = com[2];
				addUser(username);
			case "h":
				// print out help to user
				singleMessage(user, cmd);
			case "Q":
				// close this socket 
				kickUser(user);
			case "b":
				// broadcast the message
				message = command.substring(3, command.length()-1);
				broadcast(message);
			case "s":
				// send single message to user
				username = com[2];
				message = command.substring(3 + username.length(), command.length()-1);
				singleMessage(username, message);
			case "c":
				// send client list to user
				String listOfClients = getClientList();
				singleMessage(user, listOfClients);
			default:
				// inform user that their command doesn't work
				String incorrectCmd = "Please enter a correct command. \n";
				singleMessage(user, incorrectCmd);
		}
	}

	public void broadcast(String message) {
		
	}

	public void singleMessage(String username, String message) {
		
		// find the socket of the requested user
		Socket userSocket;
		userSocket = clientList.get(username);
		DataOutputStream outToClient = new DataOutputStream(userSocket.getOutputStream());
		
		// send message out on this socket
		outToClient.write(message);
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
		
		// close the socket
		userSocket.close();
		
		// remove the user from the client list
		clientList.remove(username);
		System.out.println(username + " signed out. \n");
	}
}
