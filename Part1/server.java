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

class server {

	public static void main(String args[]) throws Exception {

		ServerSocket listenSocket = new ServerSocket(9876);
		while(true) {

			// list on socket and run thread for multiple connections
			Socket clientSocket = listenSocket.accept();	
			Runnable r = new ClientHandler(clientSocket);
			Thread t = new Thread(r);
			t.start();
		}

	}
}

class ClientHandler implements Runnable {

		public ArrayList<Socket> clients = new ArrayList<Socket>();
		public HashMap <String, Socket> clientList = new HashMap<String, Socket>();

		Socket clientSocket;
		ClientHandler(Socket connection) {
			clientSocket = connection;
			clients.add(clientSocket);
		}
		
        //getClientList();

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
					System.out.println("The client said " + message);
                }
                

			} catch(Exception e) {

				System.out.println("got an exception");

			}

		}
		
		public void processCommand(String command) {
			String[] com = command.split(" ");
			String message = command.substring(3, command.length()-1);
			switch(com[0]) {
				case "u":
					// set username for client
					String username = com[1];
					// addUser(username, client)
				case "h":
					// print out help to user
				case "Q":
					// close this socket 
					// kickUser(username)
				case "b":
					// broadcast the message
					// broadcast(message)
				case "s":
					// send single message to user
					String username = com[1];
					// singleMessage(username, message)
				case "c":
					// send client list to user
					// getClientList()
				default:
					// inform user that their command doesn't work
			}
		}

		public void broadcast(String message) {

		}

		public void singleMessage(String username, String message) {

		}

		public String getClientList() {
			String clientList = "";
			for(int i = 0; i < clients.size(); i++) {
				System.out.println("Address of Client " + i + ": " + clients.get(i).getInetAddress().getAddress());
				clientList += "Client " + i + "\n";
			}
			return clientList;
		}
		
		public void addUser(String username) {
			
		}

		public void kickUser() {

		}
}
