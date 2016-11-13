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
		byte[] encryptedUser = c.encrypt(username.getBytes(),sKey,iv);
		System.out.printf("Encrpyted message: %s%n",DatatypeConverter.printHexBinary(encryptedUser));
 
		outToServer.write(encryptedUser, 0, encryptedUser.length);

		// run threads for output to server and input from server
		Runnable out = new OutputHandler(clientSocket, pubKey, sKeyEncrypted, iv, sKey);
		Runnable in = new InputHandler(clientSocket, iv, sKey);
		Thread outputThread = new Thread(out);
		Thread inputThread = new Thread(in);
		outputThread.start();
		inputThread.start();
		
	}
    
    //constructor
    public client(){
        pubKey = null;
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
    
    //set Public key
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
	
    //Secret key to send to server
    byte[] OutSecretKey;
    
    //public key
    private PublicKey pubKey;
    
    boolean doOnce = false;
	
	IvParameterSpec iv;

	SecretKey sKey;
    
    
	Socket clientSocket;
	OutputHandler(Socket connection, PublicKey pKey, byte[] s, IvParameterSpec ivIn, SecretKey sk) {
		clientSocket = connection;
        pubKey = pKey;
        OutSecretKey = s;
		iv = ivIn;
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
            
           /* if(doOnce == false){
                outToServer.write(OutSecretKey, 0, OutSecretKey.length);
				outToServer.flush();
                doOnce = true;
            }*/
            
			while(true){
					
				// TODO: encrypt message
				String message = inFromUser.readLine();
				byte[] encryptedMessage = encrypt(message.getBytes(),sKey,iv);
				outToServer.write(encryptedMessage, 0, encryptedMessage.length);
	
			}
		} catch (Exception e) {
			
			System.out.println("Something when wrong in output run. \n");
			System.out.println("Error: " + e + "\n");
			
		}
	}
    
	//encrypt method
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

	IvParameterSpec iv;
	SecretKey sKey;
	
	/** List of commands available to user **/
	public static String cmd = "Q logs you out\n"
			+ "b broadcasts a message to all users connected \n"
			+ "s USERNAME sends a message to a specified user connected \n" 
			+ "c prints the list of users connected \n" 
			+ "k USERNAME kicks out the requested user \n"
			+ "h lists this set of commands again \n";
	
   	Socket clientSocket;
	InputHandler(Socket connection, IvParameterSpec ivIn, SecretKey sk) {
		clientSocket = connection;
		iv = ivIn;
		sKey = sk;
	}
	
	/*******************************************************************
	 * Runs the thread to get input from the server
	 ******************************************************************/
	public void run() {
	
		try {
	
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
			while(true){
                
				
				// TODO: decrypt message
				String message = inFromServer.readLine();
				byte[] recvMessageDecrypted = decrypt(message.getBytes(), sKey, iv);
				String recvMessage = new String(recvMessageDecrypted);
				System.out.println(recvMessage);
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
	//Decrypt method
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
