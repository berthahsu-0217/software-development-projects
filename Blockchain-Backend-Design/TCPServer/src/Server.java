/*
 * @author Bertha Hsu
 * This script is used to launch a TCP server.
 * It contains code to create a server socket for listening and sending messages.
 * When the server receives a request from the client, it performs the operation
 * specified by the client (using the value it sent).
 */

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.time.Clock;
import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {

    /**
     * Method connect contains the socket communication code.
     * It also performs whichever operation requested by the client.
     */
    public static void connect(BlockChain blockchain){

        Socket clientSocket = null;
        try {
            //Allocate a port to the server
            int serverPort = 7777;
            //Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);

            /*
             * Block waiting for a new connection request from a client.
             * When the request is received, "accept" it, and the rest
             * the tcp protocol handshake will then take place, making
             * the socket ready for reading and writing.
             */
            while(true) {

                //Accept a connection on the server socket
                clientSocket = listenSocket.accept();

                //Set up "in" to read from the client socket
                Scanner in;
                in = new Scanner(clientSocket.getInputStream());

                //Set up "out" to write to the client socket
                PrintWriter out;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                //Read request data from the client
                String request = in.nextLine();
                JSONObject map = readJSON(request);

                //Check user id and signature
                boolean valid = check(map);

                //If valid, perform option chosen by user
                if (valid) {

                    int type = ((Long) map.get("type")).intValue();

                    //If choosing option 0
                    if(type == 0){

                        //Make required information into a JSON format message
                        JSONObject obj = new JSONObject();
                        obj.put("chain_size", blockchain.getChainSize());
                        obj.put("hashesPerSecond", blockchain.hashesPerSecond());
                        Block latestBlock = blockchain.getLatestBlock();
                        obj.put("latestDifficulty", latestBlock.getDifficulty());
                        obj.put("latestNonce", latestBlock.getNonce());
                        obj.put("chainHash", blockchain.chainHash);
                        String response = obj.toString();
                        //send the message back to client
                        out.println(response);
                        out.flush();

                    //If choosing option 1
                    }else if(type == 1){

                        //Retrieve difficulty and transaction from the request
                        int difficulty = ((Long) map.get("param1")).intValue();
                        String transaction = (String) map.get("param2");

                        //Calculate executed time to generate and add a new block
                        Clock clock = Clock.systemDefaultZone();
                        long t0 = clock.millis();
                        //Generate and add a new block to blockchain
                        Block newBlock = new Block(blockchain.getChainSize(), blockchain.getTime(), transaction, difficulty);
                        blockchain.addBlock(newBlock);
                        long t1 = clock.millis();

                        //Make required information into a JSON format message
                        JSONObject obj = new JSONObject();
                        obj.put("elapsed_time", t1-t0);
                        String response = obj.toString();
                        //Send the message back to client
                        out.println(response);
                        out.flush();

                    //If choose option 2
                    }else if(type == 2){

                        //Calculate executed time to check if the chain is valid
                        Clock clock = Clock.systemDefaultZone();
                        long t0 = clock.millis();
                        boolean check = blockchain.isChainValid();
                        long t1 = clock.millis();

                        //Make required information into a JSON format message
                        JSONObject obj = new JSONObject();
                        obj.put("elapsed_time", t1-t0);
                        obj.put("valid", check);
                        String response = obj.toString();
                        //Send the message back to client
                        out.println(response);
                        out.flush();

                    //If choosing option 3
                    }else if(type == 3){

                        //Retrieve JSON representation of blockchain
                        String output = blockchain.toString();

                        //Make required information into a JSON format message
                        JSONObject obj = new JSONObject();
                        obj.put("output", output);
                        String response = obj.toString();
                        //Send the message back to client
                        out.println(response);
                        out.flush();

                    //If choosing option 4
                    }else if(type == 4){

                        //Retrieve index and transaction from the request
                        int index = ((Long) map.get("param1")).intValue();
                        String transaction = (String) map.get("param2");

                        //Modify data of the specified block
                        blockchain.blocks.get(index).setData(transaction);

                        //Make required information into a JSON format message
                        JSONObject obj = new JSONObject();
                        obj.put("index", index);
                        obj.put("Tx", blockchain.blocks.get(index).getData());
                        String response = obj.toString();
                        //Send the message back to client
                        out.println(response);
                        out.flush();

                    //If recieve option 5
                    }else if(type == 5){

                        //Calculate executed time to repair the blockchain
                        Clock clock = Clock.systemDefaultZone();
                        long t0 = clock.millis();
                        blockchain.repairChain();
                        long t1 = clock.millis();

                        //Make required information into a JSON format message
                        JSONObject obj = new JSONObject();
                        obj.put("elapsed_time", t1-t0);
                        String response = obj.toString();
                        //Send the message back to client
                        out.println(response);
                        out.flush();

                    }
                }
            }
        //Handle exceptions
        } catch (IOException error) {
            System.out.println("IO Exception:" + error.getMessage());
        //If quitting (typically by you sending quit signal) clean up sockets
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException error) {
                //Ignore exception on close
            }
        }

    }

    /**
     * This method reads request in to JSONObject from the client
     * @param request a String in JSON format
     * @return JSONObject
     */
    public static JSONObject readJSON(String request){

        Map<String, Object> map = new HashMap<>();
        JSONParser parser = new JSONParser();
        JSONObject obj = new JSONObject();
        try {
            obj = (JSONObject) parser.parse(request);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return obj;
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
        }
        catch (NoSuchAlgorithmException nsa) {
            System.out.println("No such algorithm exception thrown " + nsa);
        }
        catch (UnsupportedEncodingException uee ) {
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
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Method generateID uses public key to generate an user ID
     * @param e public key
     * @param n public key
     * @return user ID
     */
    public static String generateID(BigInteger e, BigInteger n){

        String str = e.toString() + n.toString();
        byte[] hashedStr = SHA256Hash(str);
        String id = byteArrayToString(Arrays.copyOfRange(hashedStr,hashedStr.length-20,hashedStr.length));
        return id;
    }

    /**
     * Method check checks if the user ID is hashed properly, and if the request is properly signed
     * @return whether operation is valid to resume
     */
    public static boolean check(Map map){

        String id = (String) map.get("id");
        BigInteger e = new BigInteger((String) map.get("e"));
        BigInteger n = new BigInteger((String) map.get("n"));
        int type = ((Long) map.get("type")).intValue();
        int param1 = ((Long) map.get("param1")).intValue();
        String param2 = (String) map.get("param2");
        String signature = (String) map.get("signature");

        //Recreate the user id with public keys
        String IDtoCheck = generateID(e,n);

        //Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(signature);
        //Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        //Retrieve the origin hashed message
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("e", e.toString());
        obj.put("n", n.toString());
        obj.put("type",type);
        obj.put("param1", param1);
        obj.put("param2", param2);
        String messageToCheck = obj.toString();

        byte[] bytes = SHA256Hash(messageToCheck);
        //Retrieve the BigInteger created by the hash
        BigInteger m = new BigInteger(bytes);
        BigInteger bigIntegerToCheck = new BigInteger(1,bytes);

        //Check if the id and signature are valid, return the result
        if((bigIntegerToCheck.compareTo(decryptedHash) == 0) && (IDtoCheck.equals(id))){
            System.out.println("Valid id and signature.");
            return true;
        }
        else {
            System.out.println("Invalid id and signature.");
            return false;
        }
    }

    /**
     * This routine acts as a test driver for your Blockchain.
     * It takes about 100~400 milliseconds on average to generate and add a new block with difficulty 4 to the blockchain.
     * It takes about 1000~4000 milliseconds on average to generate and add a new block with difficulty 5 to the blockchain.
     * It both takes about 0~3 milliseconds on average to verify blocks with difficulty 4 and 5.
     * @param args
     */
    public static void main(String args[]) {

        System.out.println("BlockChain initalizing...");

        //Generate an empty blockchain
        BlockChain blockchain = new BlockChain();
        //Generate the genesis block with difficulty equal to 2, add it to blockchain
        Block genesis = new Block(0, blockchain.getTime(), "", 2);
        blockchain.addBlock(genesis);

        //Launch a server socket
        connect(blockchain);

    }
}


