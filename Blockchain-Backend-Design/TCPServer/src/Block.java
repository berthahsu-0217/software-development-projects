/*
 * @author Bertha Hsu
 * This script is used to construct a block.
 * It contains code to create a block with many attributes and a previousHash variable
 * that is used to be chained to the previously created block.
 */

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.simple.JSONObject;

public class Block {

    private int index;
    private java.sql.Timestamp timestamp;
    private String data;
    private int difficulty;
    private String previousHash;
    private BigInteger nonce = BigInteger.valueOf(0);

    /**
     * This the Block constructor.
     * @param index This is the position within the chain. Genesis is at 0.
     * @param timestamp This is the time this block was added.
     * @param data This is the transaction to be included on the blockchain.
     * @param difficulty This is the number of leftmost nibbles that need to be 0.
     */
    public Block(int index, java.sql.Timestamp timestamp, java.lang.String data, int difficulty){
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
    }

    /**
     * This method computes a hash of the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty.
     * @return a String holding Hexadecimal characters
     */
    public java.lang.String calculateHash() {

        try {
            //Create a String concatenating all attributes
            String string = Integer.toString(this.index);
            string = string.concat(this.timestamp.toString());
            string = string.concat(this.data);
            string = string.concat(this.previousHash);
            string = string.concat(this.nonce.toString());
            string = string.concat(Integer.toString(this.difficulty));

            //Hash the string
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(string.getBytes("UTF-8"), 0, string.length());
            byte[] hash = digest.digest();

            //Convert the hash to hexdecimals
            StringBuffer hexStringBuffer = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                int halfbyte = (hash[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        hexStringBuffer.append((char) ('0' + halfbyte));
                    else
                        hexStringBuffer.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = hash[i] & 0x0F;
                } while (two_halfs++ < 1);
            }
            String hexString = hexStringBuffer.toString();
            return hexString;

        }catch(NoSuchAlgorithmException | UnsupportedEncodingException ex){
            System.out.println("Error");
            System.exit(1);
        }
        return "";
    }

    /**
     * This method returns the nonce for this block. The nonce is a number that has been found to cause the hash of this block to have the correct number of leading hexadecimal zeroes.
     * @return a BigInteger representing the nonce for this block.
     */
    public java.lang.String proofOfWork() {

        String hexString;
        int leading_zeros = 0;
        while(true){
            //Generate a hash with current attribute values
            hexString = calculateHash();
            leading_zeros = 0;
            //Counting leading zeros
            for(int i = 0; i < hexString.length(); i++){
                if (hexString.charAt(i) == '0'){
                    leading_zeros += 1;
                }else break;
            }
            //Compare leading zeros with difficulty, if requirement met, return this hexstring.
            if (leading_zeros >= this.difficulty){
                return hexString;
            }
            //Else, add 1 to nonce, and generate a new hash again.
            this.nonce = this.nonce.add(BigInteger.valueOf(1));
        }
    }

    /**
     * Override Java's toString method and print out JSON representation of the block
     * @return A JSON representation of all of this block's data is returned.
     */
    @Override
    public java.lang.String toString(){

        JSONObject obj = new JSONObject();
        obj.put("index", this.index);
        obj.put("timestamp", this.timestamp.toString());
        obj.put("Tx", this.data);
        obj.put("difficulty", this.difficulty);
        obj.put("nonce", this.nonce.toString());
        obj.put("previousHash", this.previousHash);

        return obj.toString();
    }

    /**
     * This method returns the nonce for this block. The nonce is a number that has been found to cause the hash of this block to have the correct number of leading hexadecimal zeroes.
     * @return a BigInteger representing the nonce for this block.
     */
    public java.math.BigInteger getNonce(){
        return this.nonce;
    }

    /**
     * Getter for previousHash
     * @return previousHash
     */
    public java.lang.String getPreviousHash(){
        return previousHash;
    }

    /**
     * Setter for previousHash
     * @param previousHash
     */
    public void setPreviousHash(java.lang.String previousHash){
        this.previousHash = previousHash;
    }

    /**
     * Getter for difficulty
     * @return difficulty
     */
    public int getDifficulty(){
        return this.difficulty;
    }

    /**
     * Setter for difficulty
     * @param difficulty
     */
    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    /**
     * Getter for index
     * @return index
     */
    public int getIndex(){
        return this.index;
    }

    /**
     * Setter for index
     * @param index
     */
    public void setIndex(int index){
        this.index = index;
    }

    /**
     * Getter for timestamp
     * @return timestamp
     */
    public java.sql.Timestamp getTimestamp(){
        return this.timestamp;
    }

    /**
     * Setter for timestamp
     * @param timestamp
     */
    public void setTimestamp(java.sql.Timestamp timestamp){
        this.timestamp = timestamp;
    }

    /**
     * Getter for data
     * @return data
     */
    public java.lang.String getData(){
        return this.data;
    }

    /**
     * Setter for data
     * @param data
     */
    public void setData(java.lang.String data){
        this.data = data;
    }

    public static void main(java.lang.String[] args){


    }

}

