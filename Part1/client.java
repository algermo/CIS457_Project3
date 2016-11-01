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

	/** The Initial Set of Commands **/
	public String cmd = "Q means QUIT\n"
			+ "u sets your username \n"
			+ "b broadcasts a message to all users connected \n"
			+ "s USERNAME sends a message to a specified user connected \n" 
			+ "c prints the list of users connected \n" 
			+ "h lists this set of commands again \n";
			
	public String username;

	public static void main(String args[]) throws Exception {
		
		// does all network setup for us
		// creates socket, specifies address and port number, and goes out
		// and tries to make connection
		Socket clientSocket = new Socket("127.0.0.1", 9876);
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		System.out.println("Enter a username: ")
		username = inFromUser.readLine();
		outToServer.writeBytes("u " + username "\n");
		
        while(true){
			
			System.out.println("Enter a message: ");
			String message = inFromUser.readLine();
			outToServer.writeBytes(message + '\n');
			
        }
	}

	public void broadcast(String message) {

	}

	public void singleMessage(String message) {

	}

	public void getClientList() {
		
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