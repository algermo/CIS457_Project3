/*
 * Molly Alger & Chris Fracssi
 * CIS457 F16
 * Project 3 Part 1 - TCP Chat Client
 * November 8, 2016
 *
 */


import java.io.*;
import java.net.*;
import java.awt.*;

class client {
			
	public static String username;
	
	public static void main(String args[]) throws Exception {
		
		Socket clientSocket = new Socket("127.0.0.1", 9876);
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
        System.out.println("Enter a username: ");
		username = inFromUser.readLine();
		outToServer.writeBytes("u " + username + "\n");

		Runnable out = new OutputHandler(clientSocket);
		Runnable in = new InputHandler(clientSocket);
		Thread outputThread = new Thread(out);
		Thread inputThread = new Thread(in);
		outputThread.start();
		inputThread.start();
		
	}
}

class OutputHandler implements Runnable {
	
	Socket clientSocket;
	OutputHandler(Socket connection) {
		clientSocket = connection;
	}
	
	public void run() {
		try {
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
			while(true){
					
				System.out.println("Enter a message: ");
				String message = inFromUser.readLine();
				outToServer.writeBytes(message + '\n');
					
			}
		} catch (Exception e) {
			System.out.println("Something when wrong in output run. \n");
		}
	}
	
	
}

class InputHandler implements Runnable {
	
	public static String cmd = "Q means QUIT\n"
			+ "b broadcasts a message to all users connected \n"
			+ "s USERNAME sends a message to a specified user connected \n" 
			+ "c prints the list of users connected \n" 
			+ "h lists this set of commands again \n";
	
	Socket clientSocket;
	InputHandler(Socket connection) {
		clientSocket = connection;
	}
	
	public void run() {
	
		try {
	
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
			while(true){
					
				String recvMessage = inFromServer.readLine();
				if(recvMessage.equals("help")) {
					System.out.println(cmd);
				} else {
					System.out.println(recvMessage);
				}
			}
		} catch (Exception e) {
			System.out.println("Something when wrong in input run. \n");
		}
	}
	
}


/*
public class ClientGUI extends JFrame {
	
	private JButton sendBroadcast;
	private JButton sendMessage;
	private JButton clientList;
	
	private class ButtonListener implements ActionListener {
 
 
 
 
 
 
 
	}
 
}
*/