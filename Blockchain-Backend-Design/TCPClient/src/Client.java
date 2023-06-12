/*
 * @author Bertha Hsu
 * This script is used to launch a TCP client.
 * It contains code to create a client socket for sending and receiving messages from the server.
 * It lets users choose an option and type parameters required by the server to execute blockchain operations.
 */

import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Client {

    /**
     * Method RSA generates a pair of private and public keys for encryption and decryption.
     * @return a map containing keys
     */
    public static Map RSA() {

        //Initalize a map to store the keys
        Map<String, BigInteger> key = new HashMap<>();
        //Each public and private key consists of an exponent and a modulus
        BigInteger n; //n is the modulus for both the private and public keys
        BigInteger e; //e is the exponent of the public key
        BigInteger d; //d is the exponent of the private key
        Random rnd = new Random();
        //Step 1: Generate two large random primes
        //Note that I used 400 bits here, but best practice for security is 2048 bits
        BigInteger p = new BigInteger(400, 100, rnd);
        BigInteger q = new BigInteger(400, 100, rnd);
        //Step 2: Compute n by the equation n = p * q
        n = p.multiply(q);
        //Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        //Step 4: Select a small odd integer e that is relatively prime to phi(n)
        //By convention the prime 65537 is used as the public exponent
        e = new BigInteger("65537");
        //Step 5: Compute d as the multiplicative inverse of e modulo phi(n)
        d = e.modInverse(phi);

        //System.out.println(" e = " + e);  // Step 6: (e,n) is the RSA public key
        //System.out.println(" d = " + d);  // Step 7: (d,n) is the RSA private key
        //System.out.println(" n = " + n);  // Modulus for both keys

        //Store keys in the map
        key.put("e", e);
        key.put("d", d);
        key.put("n", n);
        return key;
    }

    /**
     * Method SHA256Hash hashes a given string using "SHA-256"
     * @param text a String
     * @return byte array of the hash
     */
    public static byte[] SHA256Hash(String text) {

        try {
            //Create a SHA256 digest
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            //Allocate room for the result of the hash
            byte[] hashBytes;
            //Perform the hash
            digest.update(text.getBytes("UTF-8"), 0, text.length());
            //Collect result
            hashBytes = digest.digest();
            return hashBytes;
        } catch (NoSuchAlgorithmException nsa) {
            System.out.println("No such algorithm exception thrown " + nsa);
        } catch (UnsupportedEncodingException uee) {
            System.out.println("Unsupported encoding exception thrown " + uee);
        }
        return null;
    }

    /**
     * Method byteArrayToString converts a byte array to a hex string
     * @param data byte array
     * @return a hex String
     */
    public static String byteArrayToString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Method generateID uses public key to generate an user ID
     * @param e public key
     * @param n public key
     * @return user ID
     */
    public static String generateID(BigInteger e, BigInteger n) {

        String str = e.toString() + n.toString();
        byte[] hashedStr = SHA256Hash(str);
        String id = byteArrayToString(Arrays.copyOfRange(hashedStr, hashedStr.length - 20, hashedStr.length));
        return id;
    }

    /**
     * Method sign uses privates keys to encrypt a message
     * @param bytes byte array of the hashed message
     * @param d private key
     * @param n private key
     * @return a encrypted String
     */
    public static String sign(byte[] bytes, BigInteger d, BigInteger n) {
        //Make sure the hash generates a positive BigInteger
        BigInteger m = new BigInteger(1, bytes);
        //System.out.println("bigint:"+m);
        BigInteger c = m.modPow(d, n);
        return c.toString();
    }

    /**
     * This method connects to and sends the JSON formatted message to the server
     * @param message JSON representation of request
     * @return JSON representation of response
     */
    public static String sendInput(String message) {

        Socket clientSocket = null;
        try {
            //Allocate a server port to the client
            int serverPort = 7777;
            //Create a TCP client Socket
            clientSocket = new Socket("localhost", serverPort);

            //Set up "in" to read from the client socket
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Set up "out" to write to the client socket
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            //Write data to the socket
            out.println(message);
            out.flush();
            //Read data from the socket
            String data = in.readLine();
            //System.out.println("Received: " + data);
            return data;

            //Handle exceptions
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
            //If quitting (typically by user sending quit signal), clean up sockets
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                //Ignore exception on close
            }
        }
        return "";
    }

    /**
     * This method creates a message in JSON format
     * @param id user id generated by the client
     * @param e public keys
     * @param n public keys
     * @param type option chosen by user
     * @param param1 first parameter entered by user, set to 0 if no such parameter was entered
     * @param param2 second parameter entered by user, set to "" if no such parameter was entered
     * @return JSON representation of message
     */
    public static String createMessage(String id, String e, String n, int type, int param1, String param2){

        JSONObject obj = new JSONObject();
        obj.put("id",id);
        obj.put("e",e);
        obj.put("n",n);
        obj.put("type",type);
        obj.put("param1", param1);
        obj.put("param2", param2);
        return obj.toString();
    }

    /**
     * This method adds signature to JSON formatted message
     * @param message previous version of message
     * @param signature signature encrpyted by client
     * @return JSON representation of final message to be sent
     */
    public static String addSignature(String message, String signature){

        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(message);
            obj.put("signature", signature);
            return obj.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This method reads the response into a JSONObject
     * @param response response sent from the server
     * @return JSONObject
     */
    public static JSONObject readJSON(String response){

        JSONParser parser = new JSONParser();
        JSONObject obj = new JSONObject();
        try {
            obj = (JSONObject) parser.parse(response);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return obj;

    }

    public static void main(String args[]) {

        System.out.println("Welcome to Project3 Task 1.");
        System.out.println();

        //Create private and public keys
        Map key = RSA();
        String id = generateID((BigInteger) key.get("e"), (BigInteger) key.get("n"));
        String e = key.get("e").toString();
        String n = key.get("n").toString();

        Scanner input = new Scanner(System.in);

        //Create a client side menu
        while (true) {

            System.out.println("BlockChain Menu");
            System.out.println("0. View basic blockchain status.");
            System.out.println("1. Add a transaction to the blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Corrupt the chain.");
            System.out.println("5. Hide the Corruption by repairing the chain.");
            System.out.println("6. Exit");

            String choice = input.nextLine();
            try{
                int num = Integer.parseInt(choice);

                //If choosing 0
                if(num == 0){

                    //Create message in JSON format
                    String message = createMessage(id, e, n, 0,0,"");
                    //Hash message
                    byte[] bytes = SHA256Hash(message);
                    //Encrypt hashed message
                    String signature = sign(bytes, (BigInteger) key.get("d"), (BigInteger) key.get("n"));
                    message = addSignature(message, signature);
                    //System.out.println(message);
                    //Sends final message to socket
                    String response = sendInput(message);
                    JSONObject map = readJSON(response);

                    System.out.println("Current size of chain: "+ map.get("chain_size"));
                    System.out.println("Current hashes per second by this machine: "+ map.get("hashesPerSecond"));
                    System.out.println("Difficulty of most recent block: "+ map.get("latestDifficulty"));
                    System.out.println("Nonce for most recent block: "+  map.get("latestNonce"));
                    System.out.println("Chain hash: "+ map.get("chainHash"));

                //If choosing option 1
                }else if(num == 1){

                    //Prompt the user to enter difficulty
                    System.out.println("Enter difficulty > 0.");
                    int difficulty;
                    while(true){
                        String reply = input.nextLine();
                        try{
                            difficulty = Integer.parseInt(reply);
                            if (difficulty <= 0){
                                System.out.println("Difficulty must be a positive integer. Try again.");
                                continue;
                            }
                            break;
                        }catch(NumberFormatException ex){
                            System.out.println("Difficulty must be a positive integer. Try again.");
                        }
                    }
                    //Prompt the user to enter transaction
                    System.out.println("Enter transaction.");
                    String transaction = input.nextLine();

                    //Create message in JSON format
                    String message = createMessage(id, e, n, 1,difficulty,transaction);
                    //Hash message
                    byte[] bytes = SHA256Hash(message);
                    //Encrypt hashed message
                    String signature = sign(bytes, (BigInteger) key.get("d"), (BigInteger) key.get("n"));
                    message = addSignature(message, signature);
                    //System.out.println(message);
                    //Sends final message to socket
                    String response = sendInput(message);

                    JSONObject map = readJSON(response);
                    System.out.println("Total execution time required to verify the chain was "+map.get("elapsed_time")+" milliseconds");

                //If choosing option 2
                }else if(num == 2){

                    System.out.println("Verifying entire chain.");

                    //Create message in JSON format
                    String message = createMessage(id, e, n, 2, 0, "");
                    //Hash message
                    byte[] bytes = SHA256Hash(message);
                    //Encrypt hashed message
                    String signature = sign(bytes, (BigInteger) key.get("d"), (BigInteger) key.get("n"));
                    message = addSignature(message, signature);
                    //System.out.println(message);
                    //Sends final message to socket
                    String response = sendInput(message);
                    JSONObject map = readJSON(response);

                    System.out.println("Chain verification: "+ map.get("valid"));
                    System.out.println("Total execution time required to verify the chain was "+map.get("elapsed_time")+" milliseconds");

                //If Choosing option 3
                }else if(num == 3){

                    System.out.println("View the Blockchain.");

                    //Create message in JSON format
                    String message = createMessage(id, e, n, 3,0,"");
                    //Hash message
                    byte[] bytes = SHA256Hash(message);
                    //Encrypt hashed message
                    String signature = sign(bytes, (BigInteger) key.get("d"), (BigInteger) key.get("n"));
                    message = addSignature(message, signature);
                    //System.out.println(message);
                    //Send final message to socket
                    String response = sendInput(message);
                    JSONObject map = readJSON(response);

                    System.out.println(map.get("output"));

                //If choosing option 4
                }else if(num == 4){

                    System.out.println("Corrupt the Blockchain.");
                    //Prompt the user to enter a block id
                    System.out.println("Enter block ID to Corrupt");
                    int index;
                    while(true) {
                        String reply = input.nextLine();
                        try {
                            index = Integer.parseInt(reply);
                            if ((index < 0)) {
                                System.out.println("Invalid index. Try again.");
                                continue;
                            }
                            break;
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid index. Try again.");
                        }
                    }
                    //Prompt the user to enter a modified transaction
                    System.out.println("Enter new data for block "+index);
                    String newData = input.nextLine();

                    //Create message in JSON format
                    String message = createMessage(id, e, n, 4,index,newData);
                    //Hash message
                    byte[] bytes = SHA256Hash(message);
                    //Encrypt hashed message
                    String signature = sign(bytes, (BigInteger) key.get("d"), (BigInteger) key.get("n"));
                    message = addSignature(message, signature);
                    //System.out.println(message);
                    //Sends final message to socket
                    String response = sendInput(message);
                    JSONObject map = readJSON(response);

                    System.out.println("Block "+map.get("index")+" now holds "+map.get("Tx"));

                //If choosing option 5
                }else if(num == 5){

                    System.out.println("Repairing the entire chain");

                    //Create message in JSON format
                    String message = createMessage(id, e, n, 5,0,"");
                    //Hash message
                    byte[] bytes = SHA256Hash(message);
                    //Encrypt hashed message
                    String signature = sign(bytes, (BigInteger) key.get("d"), (BigInteger) key.get("n"));
                    message = addSignature(message, signature);
                    //System.out.println(message);
                    //Sends final message to socket
                    String response = sendInput(message);
                    JSONObject map = readJSON(response);

                    System.out.println("Total execution time required to repair the chain was "+map.get("elapsed_time")+" milliseconds");

                //If choosing option 6
                }else if(num == 6) {

                    System.out.println("Exit.");
                    System.exit(0);

                }else{
                    System.out.println("Input is not a number from 0 to 6. Try again.");
                }


            }catch (NumberFormatException ex){
                System.out.println("Invalid choice. Try again.");
            }

            System.out.println();
        }
    }
}

