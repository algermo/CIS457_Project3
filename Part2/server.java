/*
 * Molly Alger & Chris Fracssi
 * CIS457 F16
 * Project 3 Part 2 - TCP Chat Server
 * November 14, 2016
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import javax.xml.bind.DatatypeConverter;

class server {
    private static PrivateKey privKey;
    private static PublicKey pubKey;

	
    public static ConcurrentHashMap <String, Socket> clientList = new ConcurrentHashMap<String, Socket>();
	public static ConcurrentHashMap <String, SecretKey> keyList = new ConcurrentHashMap<String, SecretKey>();

	public static void main(String args[]) throws Exception {

        //set keys
        server s = new server();
        s.setPrivateKey("RSApriv.der");
        s.setPublicKey("RSApub.der");
        
		ServerSocket listenSocket = new ServerSocket(9876);

		
		while(true) {

			// list on socket and run thread for multiple connections
			Socket clientSocket = listenSocket.accept();	
			Runnable r = new ClientHandler(clientSocket, clientList, pubKey, privKey, keyList);
			Thread t = new Thread(r);
			t.start();
		}

	}
    //constructor method
    public server(){
        privKey = null;
        pubKey = null;
        
    }
    //Set public key
    public void setPublicKey(String filename){
        try{
            File f = new File(filename);
            FileInputStream fs = new FileInputStream(f);
            byte[] keybytes = new byte[(int)f.length()];
            fs.read(keybytes);
            fs.close();
            X509EncodedKeySpec keyspec = new X509EncodedKeySpec(keybytes);
            KeyFactory rsafactory = KeyFactory.getInstance("RSA");
            pubKey = rsafactory.generatePublic(keyspec);
        }catch(Exception e){
            System.out.println("Public Key Exception");
            System.exit(1);
        }
    }
    
    //Set private key
    public void setPrivateKey(String filename){
        try{
            File f = new File(filename);
            FileInputStream fs = new FileInputStream(f);
            byte[] keybytes = new byte[(int)f.length()];
            fs.read(keybytes);
            fs.close();
            PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(keybytes);
            KeyFactory rsafactory = KeyFactory.getInstance("RSA");
            privKey = rsafactory.generatePrivate(keyspec);
        }catch(Exception e){
            System.out.println("Private Key Exception");
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

class ClientHandler implements Runnable {
	
	/** Hashmap containing the connected clients (keys = client usernames) **/
    public ConcurrentHashMap <String, Socket> clientList = new ConcurrentHashMap<String, Socket>();

	public static ConcurrentHashMap <String, SecretKey> keyList = new ConcurrentHashMap<String, SecretKey>();
	
	/** The username of the client connected on this thread **/
    public String user;
    
    /** Public key to send to client **/
    private PublicKey pubKey;
    
    /**Private key to use for decryption**/
    private PrivateKey privKey;
    
    SecretKey sKey;

    boolean firstReceive = true;
    boolean secondReceive = false;

	IvParameterSpec iv;

	

	Socket clientSocket;
	ClientHandler(Socket connection, ConcurrentHashMap <String, Socket> clients, PublicKey pbKey, PrivateKey pvKey, ConcurrentHashMap <String, SecretKey> keys) {
		clientSocket = connection;
        clientList = clients;
        pubKey = pbKey;
        privKey = pvKey;
		keyList = keys;
	}
		

	/*******************************************************************
	 * Runs the client thread
	 ******************************************************************/
	public void run() {
		
		try {

			
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			InputStream is = clientSocket.getInputStream();

			//outToClient.writeBytes("Welcome");
			
			//Get IV
			if(firstReceive == true){
				System.out.println("Reading iv");
				byte ivbytes[] = new byte[16];
				int ivCount = is.read(ivbytes);
				System.out.println("Bytes read for iv: " + ivCount);
				iv = new IvParameterSpec(ivbytes);
				firstReceive = false;
				secondReceive = true;
			}
		
				//Get key
                if(secondReceive == true){
					System.out.println("Reading key");
                  //  InputStream is = clientSocket.getInputStream();
                    //receive encrypted key from client
                    //TODO: See if the byte array needs a specific size
                    byte sKeyEncrypted[] = new byte[256];
                    int keyCount = is.read(sKeyEncrypted);
					System.out.println("Bytes read for key: " + keyCount);
                    
                    //decrypt key using private key
                    byte sKeyDecrypted[] = RSADecrypt(sKeyEncrypted);
                    sKey = new SecretKeySpec(sKeyDecrypted, "AES");
					secondReceive = false;
                }

            
			while(clientSocket.isConnected()){
            

                
				// TODO: decrpyt the message received
				
				// receive message from user

				//String message = inFromClient.readLine();

				//TODO: seperate receiving username and message
				byte userEncrypted[] = new byte[16];
				int userCount = is.read(userEncrypted);
				if(userCount < 0){
					continue;
				}
				System.out.printf("Encrypted message received: %s%n",DatatypeConverter.printHexBinary(userEncrypted));
				System.out.println("bytes read for user: " + userCount);
				System.out.println("sKey: " + sKey);
				System.out.println("iv: " + iv);
				byte userDecrypted[] = decrypt(userEncrypted, sKey, iv);
				
				String message = new String(userDecrypted);
				System.out.println("Decrypted message: " + message);
				
				if(message.equals("") || message.equals(" ")) {
					// inform user of empty message
					String emptyCmd = "Please enter a command. \n";
					singleMessage(user, emptyCmd);
					continue;
				} else {
					// process the message the user sent
					System.out.println(message);
					processCommand(message);
					continue;
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

		SecretKey userKey;
		userKey = keyList.get(username);
		
		try {
			
			DataOutputStream outToClient = new DataOutputStream(userSocket.getOutputStream());
			// TODO: encrypt message
			// send message out on this socket
			System.out.println(message);
			byte[] singleMessageEncrypted = encrypt(message.getBytes(),userKey,iv);
			System.out.println("sKey: " + userKey);
			System.out.println("iv: " + iv);
			System.out.println("Sendng singleMessage");
			System.out.printf("Encrpyted message sent: %s%n",DatatypeConverter.printHexBinary(singleMessageEncrypted));
			outToClient.write(singleMessageEncrypted, 0 , singleMessageEncrypted.length);
			outToClient.flush();
			
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

		keyList.put(username, sKey);
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
	//TODO: Encrypt and Decrypt methods
    public byte[] encrypt(byte[] plaintext, SecretKey secKey, IvParameterSpec iv){
        try{
			System.out.println("Encrypting message");
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
    
    public byte[] decrypt(byte[] ciphertext, SecretKey secKey, IvParameterSpec iv){
        try{
			System.out.println("Decrypting message");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE,secKey,iv);
            byte[] plaintext = c.doFinal(ciphertext);
            return plaintext;
        }catch(Exception e){
            System.out.println("AES Decrypt Exception" + e.getMessage());
            System.exit(1);
            return null;
        }
    }

	//TODO: RSA Decrypt key
    public byte[] RSADecrypt(byte[] ciphertext){
        try{
            Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            c.init(Cipher.DECRYPT_MODE,privKey);
            byte[] plaintext=c.doFinal(ciphertext);
            return plaintext;
        }catch(Exception e){
            System.out.println("RSA Decrypt Exception");
            System.exit(1);
            return null;
        }
    }
}
