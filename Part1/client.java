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
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
        System.out.println("Enter a username: ");
		username = inFromUser.readLine();
		outToServer.writeBytes("u " + username + "\n");
		
        while(true){
			
			System.out.println("Enter a message: ");
			String message = inFromUser.readLine();
			outToServer.writeBytes(username + " " + message + '\n');
			
			String recvMessage = inFromServer.readLine();
			System.out.println(recvMessage);
			
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