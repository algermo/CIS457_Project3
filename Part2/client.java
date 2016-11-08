/*
 * Molly Alger & Chris Fracssi
 * CIS457 F16
 * Project 3 Part 2 - TCP Chat Client
 * November 14, 2016
 *
 */


import java.io.*;
import java.net.*;
import java.awt.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import javax.xml.bind.DatatypeConverter;

class client {
			
	int serverPublicKey;
	int symmetricKey;
			
	/** Username for the client **/
	public static String username;
	
	public static void main(String args[]) throws Exception {
		
		Socket clientSocket = new Socket("127.0.0.1", 9876);
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		//TODO: encrypt user info
		// request a username from the client and send it to the server
        System.out.println("Enter a username: ");
		username = inFromUser.readLine();
		outToServer.writeBytes("u " + username + "\n");

		// run threads for output to server and input from server
		Runnable out = new OutputHandler(clientSocket);
		Runnable in = new InputHandler(clientSocket);
		Thread outputThread = new Thread(out);
		Thread inputThread = new Thread(in);
		outputThread.start();
		inputThread.start();
		
	}
	//TODO: encrypt method for user message
    public byte[] encrypt(byte[] plaintext, SecretKey secKey, IvParameterSpec iv){
        try{
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE,secKey,iv);
            byte[] ciphertext = c.doFinal(plaintext);
            return ciphertext;
        }catch(Exception e){
            System.out.println("AES Encrypt Exception");
            System.exit(1);
            return null;
        }
    }

	//TODO: RSA Encrypt key
    public byte[] RSAEncrypt(byte[] plaintext){
        try{
            Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            c.init(Cipher.ENCRYPT_MODE,pubKey);
            byte[] ciphertext=c.doFinal(plaintext);
            return ciphertext;
        }catch(Exception e){
            System.out.println("RSA Encrypt Exception");
            System.exit(1);
            return null;
        }
    }

	//TODO: Generate key method
    public SecretKey generateAESKey(){
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secKey = keyGen.generateKey();
            return secKey;
        }catch(Exception e){
            System.out.println("Key Generation Exception");
            System.exit(1);
            return null;
        }
    }

}

class OutputHandler implements Runnable {
	
	Socket clientSocket;
	OutputHandler(Socket connection) {
		clientSocket = connection;
	}
	
	/*******************************************************************
	 * Runs the thread to get user input and output messages 
	 * to the server
	 ******************************************************************/
	public void run() {
		try {
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
			while(true){
					
				// TODO: encrypt message
				String message = inFromUser.readLine();
				outToServer.writeBytes(message + '\n');
	
			}
		} catch (Exception e) {
			
			System.out.println("Something when wrong in output run. \n");
			System.out.println("Error: " + e + "\n");
			
		}
	}

	//TODO: encrypt method
    public byte[] encrypt(byte[] plaintext, SecretKey secKey, IvParameterSpec iv){
        try{
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE,secKey,iv);
            byte[] ciphertext = c.doFinal(plaintext);
            return ciphertext;
        }catch(Exception e){
            System.out.println("AES Encrypt Exception");
            System.exit(1);
            return null;
        }
    }
	
}

class InputHandler implements Runnable {
	
	/** List of commands available to user **/
	public static String cmd = "Q logs you out\n"
			+ "b broadcasts a message to all users connected \n"
			+ "s USERNAME sends a message to a specified user connected \n" 
			+ "c prints the list of users connected \n" 
			+ "k USERNAME kicks out the requested user \n"
			+ "h lists this set of commands again \n";
	
	Socket clientSocket;
	InputHandler(Socket connection) {
		clientSocket = connection;
	}
	
	/*******************************************************************
	 * Runs the thread to get input from the server
	 ******************************************************************/
	public void run() {
	
		try {
	
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
			while(true){
				
				// TODO: decrypt message
				String recvMessage = inFromServer.readLine();
				if(recvMessage.equals("help")) {
					System.out.println(cmd);
				} else {
					if (recvMessage.equals("Q")) {
						System.out.println("You have been signed out. \n");
						System.exit(0);
					}
					System.out.println(recvMessage);
				}
			}
		} catch (Exception e) {
			
			System.out.println("Something when wrong in input run. \n");
			System.out.println("Error: " + e + "\n");
			
		}
	}
	//TODO: Decrypt method
    public byte[] decrypt(byte[] ciphertext, SecretKey secKey, IvParameterSpec iv){
        try{
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE,secKey,iv);
            byte[] plaintext = c.doFinal(ciphertext);
            return plaintext;
        }catch(Exception e){
            System.out.println("AES Decrypt Exception");
            System.exit(1);
            return null;
        }
    }

	
}
