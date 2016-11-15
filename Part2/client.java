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
    
	/** Username for the client **/
	public static String username;
    
    /** Public Key **/
    private static PublicKey pubKey;
	
	public static void main(String args[]) throws Exception {
        client c = new client();
        
        //generate public key
        c.setPublicKey("RSApub.der");
        
        //generate secret key
        SecretKey sKey = c.generateAESKey();
        
        //Encrypt secret key
        byte sKeyEncrypted[] = c.RSAEncrypt(sKey.getEncoded());

		SecureRandom r = new SecureRandom();
		byte ivbytes[] = new byte[16];
		r.nextBytes(ivbytes);
		IvParameterSpec iv = new IvParameterSpec(ivbytes);
        
		Socket clientSocket = new Socket("127.0.0.1", 9876);
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

		//Send iv
		outToServer.write(ivbytes, 0, ivbytes.length);		

		//Send key
		outToServer.write(sKeyEncrypted, 0, sKeyEncrypted.length);


		//TODO: encrypt user info
		//request a username from the client and send it to the server
        System.out.println("Enter a username: ");
		username = inFromUser.readLine();
		username = "u " + username;
		System.out.println("sKey: " + sKey);
		System.out.println("iv: " + iv);
		byte[] encryptedUser = c.encrypt(username.getBytes(),sKey,iv);
		System.out.printf("Encrpyted message username: %s%n",DatatypeConverter.printHexBinary(encryptedUser));
 
		outToServer.write(encryptedUser, 0, encryptedUser.length);

		// run threads for output to server and input from server
		Runnable out = new OutputHandler(clientSocket, pubKey, sKeyEncrypted, ivbytes, sKey);
		Runnable in = new InputHandler(clientSocket, ivbytes, sKey);
		Thread outputThread = new Thread(out);
		Thread inputThread = new Thread(in);
		outputThread.start();
		inputThread.start();
		
	}
    
    /*******************************************************************
	 * Constructor for the client
	 ******************************************************************/
    public client(){
        pubKey = null;
    }
    
	/*******************************************************************
	 * Encrypts messages for the server
	 * @param plaintext message to encrypt
	 * @param secKey the symmetric key
	 * @param iv initialization vector
	 ******************************************************************/
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

	/*******************************************************************
	 * Encrypts the symmetric key
	 * @param plaintext the key to encrypt
	 ******************************************************************/
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

	/*******************************************************************
	 * Generates the AES key (symmetric key)
	 ******************************************************************/
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
    
    /*******************************************************************
	 * Sets the public key for the server
	 * @param filename String for where to receive the key from
	 ******************************************************************/
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


}

class OutputHandler implements Runnable {
	
    /** secret key to send to server **/
    byte[] OutSecretKey;
	SecretKey sKey;
    
    /** Public key to send to server **/
    private PublicKey pubKey;
    
    boolean doOnce = false;
	
	/** initialization vector **/
	IvParameterSpec iv;

	/** the socket the client is connected on **/
	Socket clientSocket;
	
	/*******************************************************************
	 * Constructor for the OutputHandler
	 ******************************************************************/
	OutputHandler(Socket connection, PublicKey pKey, byte[] s, byte[] ivIn, SecretKey sk) {
		clientSocket = connection;
        pubKey = pKey;
        OutSecretKey = s;
		iv = new IvParameterSpec(ivIn);
		sKey = sk;
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

				String message = inFromUser.readLine();
				if(message != null){
					System.out.println("sKey: " + sKey);
					System.out.println("Iv encrypt: " + iv);
					byte[] encryptedMessage = encrypt(message.getBytes(),sKey,iv);
					System.out.println("writing to server");
					outToServer.write(encryptedMessage, 0, encryptedMessage.length);
					outToServer.flush();
				}
			}
		} catch (Exception e) {
			
			System.out.println("Something when wrong in output run. \n");
			System.out.println("Error: " + e + "\n");
			
		}
	}
    
	/*******************************************************************
	 * Encrypts messages for the server
	 * @param plaintext message to encrypt
	 * @param secKey the symmetric key
	 * @param iv initialization vector
	 ******************************************************************/
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

	/** initialization vector **/
	IvParameterSpec iv;
	
	/** secret key for crytography **/
	SecretKey sKey;
	
	/** List of commands available to user **/
	public static String cmd = "Q logs you out\n"
			+ "b broadcasts a message to all users connected \n"
			+ "s USERNAME sends a message to a specified user connected \n" 
			+ "c prints the list of users connected \n" 
			+ "k USERNAME kicks out the requested user \n"
			+ "h lists this set of commands again \n";
	
	/** the socket the client is connected on **/
   	Socket clientSocket;
	
	/*******************************************************************
	 * Constructor for the InputHandler
	 ******************************************************************/
	InputHandler(Socket connection, byte[] ivIn, SecretKey sk) {
		clientSocket = connection;
		iv = new IvParameterSpec(ivIn);
		sKey = sk;
	}
	
	/*******************************************************************
	 * Runs the thread to get input from the server
	 ******************************************************************/
	public void run() {
	
		try {
	
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			InputStream is = clientSocket.getInputStream();
		
			while(true){
                
				byte serverEncrypted[] = new byte[16];

				int serverCount = is.read(serverEncrypted);
				if( serverCount < 0){
					continue;
				}

				System.out.println("Bytes read from server: " + serverCount);
				System.out.printf("Encrpyted message received: %s%n",DatatypeConverter.printHexBinary(serverEncrypted));

				System.out.println("sKey: " + sKey);
				System.out.println("iv decrypt: " + iv);
				byte serverDecrypted[] = decrypt(serverEncrypted, sKey, iv);

				String message = new String(serverDecrypted);
				System.out.println("Decrypted message: " + message);
				if(message.equals("help")) {
					System.out.println(cmd);
				} else {
					if (message.equals("Q")) {
						System.out.println("You have been signed out. \n");
						System.exit(0);
					}
					System.out.println(message);
				}
			}
		} catch (Exception e) {
			
			System.out.println("Something when wrong in input run. \n");
			System.out.println("Error: " + e + "\n");
			
		}
	}
	
	/*******************************************************************
	 * Decrypts messages from the server
	 * @param ciphertext message to decrypt
	 * @param secKey the symmetric key
	 * @param iv initialization vector
	 ******************************************************************/
    public byte[] decrypt(byte[] ciphertext, SecretKey secKey, IvParameterSpec iv){
        try{
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE,secKey,iv);
            byte[] plaintext = c.doFinal(ciphertext);
            return plaintext;
        }catch(Exception e){
            System.out.println("AES Decrypt Exception" + e);
            System.exit(1);
            return null;
        }
    }

	
}
