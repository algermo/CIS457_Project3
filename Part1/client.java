/*
 * Molly Alger & Chris Fracssi
 * CIS457 F16
 * Project 3 Part 1 - TCP Chat Client
 * November 8, 2016
 *
 */


import java.io.*;
import java.net.*;

class client {

	public static void main(String args[]) throws Exception {
		
		// does all network setup for us
		// creates socket, specifies address and port number, and goes out
		// and tries to make connection
		Socket clientSocket = new Socket("127.0.0.1", 9876);
		
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		// TODO: keep client open after a message is sent
		System.out.println("Enter a message: ");
		String message = inFromUser.readLine();
		outToServer.writeBytes(message + '\n');
	}

	public void broadcast(String message) {

	}

	public void singleMessage(String message) {

	}

	public void getClientList() {

	}

}
