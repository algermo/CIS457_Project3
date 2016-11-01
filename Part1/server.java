/*
 * Molly Alger & Chris Fracssi
 * CIS457 F16
 * Project 3 Part 1 - TCP Chat Server
 * November 8, 2016
 *
 */

import java.io.*;
import java.net.*;

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

		ArrayList<Socket> clients = new ArrayList<Socket>();

		Socket clientSocket;
		ClientHandler(Socket connection) {
			clientSocket = connection;
			clients.add(clientSocket);
		}
		
		getClientList();

		public void run(){
			try {
				DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while(true){
					String message = inFromClient.readLine();
					if(message.equals("client list")) {
						String reply = getClientList();
						// send reply to the client
					}
					System.out.println("The client said " + message);
                }
                

			} catch(Exception e) {

				System.out.println("got an exception");

			}

		}

		public void broadcast(String message) {

		}

		public void singleMessage(String message) {

		}

		public String getClientList() {
			String clientList;
			for(int i = 0; i < clients.length; i++) {
				System.out.println("Address of Client " + i + ": " + clients.get(i).getInetAddress().getAddress());
				clientList += "Client " + i + "\n";
			}
			return clientList;
		}

		public void kickUser() {

		}
}
