/*
 * @author Bertha Hsu
 * This script is used to construct a blockchain.
 * It contains a mechanism to store, chain, verify, modify and repair the blockchain.
 */

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Scanner;

public class BlockChain {

    ArrayList<Block> blocks;
    String chainHash;

    /**
     *This constructor initalizes an array to store the blocks in and sets the chain hash to the empty string.
     */
    public BlockChain(){
        this.blocks = new ArrayList<>();
        this.chainHash = "";
    }

    /**
     *This method returns the current time in timestamp
     * @return the current system time
     */
    public java.sql.Timestamp getTime(){
        return (new Timestamp(System.currentTimeMillis()));
    }

    /**
     * This method returns the latest block created in the blockchain
     * @return
     */
    public Block getLatestBlock(){
        return this.blocks.get(getChainSize()-1);
    }

    /**
     * This method returns the size of the blockchain
     * @return
     */
    public int getChainSize(){
        return this.blocks.size();
    }

    /**
     * This method computes hashes for one second using a simple string - "00000000" to hash.
     * @return hashes per second of the computer holding this chain.
     */
    public int hashesPerSecond(){
        long t0 = System.currentTimeMillis();
        String string = "00000000";
        int count = 0;
        //In one second, count number of hashes computed
        while(!(System.currentTimeMillis()-t0 >= 1000)){
            try {
                //Hash the string "00000000"
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(string.getBytes("UTF-8"), 0, string.length());
                byte[] hash = digest.digest();
                count += 1;
            }catch(NoSuchAlgorithmException | UnsupportedEncodingException ex){
                System.out.println("Error");
                System.exit(1);
            }
        }
        return count;
    }

    /**
     * This method adds the newly created block to the blockchain
     * @param newBlock
     */
    public void addBlock(Block newBlock){
        //Chain the blocks
        newBlock.setPreviousHash(this.chainHash);
        //Generate hash for the new block and assign it to the most recent chainHash
        this.chainHash = newBlock.proofOfWork();
        //Add it to the blockchain
        this.blocks.add(newBlock);
    }

    /**
     * This method uses the toString method defined on each individual block.
     * @return JSON representation of the blockchain
     */
    @Override
    public java.lang.String toString(){

        JSONObject obj = new JSONObject();

        //For every block in the blockchain
        ArrayList<JSONObject> items = new ArrayList<>();
        for(int i = 0; i < getChainSize(); i++){
            JSONParser parser = new JSONParser();
            try {
                //Concatenate the JSON representation
                JSONObject item = (JSONObject) parser.parse(this.blocks.get(i).toString());
                items.add(item);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        obj.put("ds_chain", items);
        obj.put("chainHash", this.chainHash);
        String raw = obj.toString();
        String pretty = raw.replace("},","},\n");
        pretty = pretty.replace("],","],\n");
        return pretty;
    }

    /**
     * This method checks for any illegal block and return true if the blockchain is valid, else false
     * @return boolean value signifying whether the chain is valid
     */
    public boolean isChainValid(){

        int chain_size = getChainSize();

        //For every block in the blockchain
        for(int i = 0; i < chain_size; i++){

            Block currentBlock = this.blocks.get(i);
            //Compute hash of the current block
            String hash = currentBlock.calculateHash();
            //Counting leading zeros
            int leading_zeros = 0;
            int len = hash.length();
            for(int k = 0; k < len; k++){
                if (hash.charAt(k) == '0'){
                    leading_zeros += 1;
                }else break;
            }

            String zeros = "0";
            zeros = zeros.repeat(currentBlock.getDifficulty());
            //If leading zeros requirements not met
            if(currentBlock.getDifficulty() > leading_zeros){
                //Return false
                System.out.println("..Improper hash on node "+i+" Does not begin with "+zeros);
                return false;
            }

            //If blocks are not properly chained, return false
            if(i != chain_size-1){
                if(!hash.equals(this.blocks.get(i+1).getPreviousHash())) return false;
            }else{
                if(!hash.equals(this.chainHash)) return false;
            }
        }

        return true;
    }

    /**
     *This routine repairs the chain. It checks the hashes of each block and ensures that any illegal hashes are recomputed. After this routine is run, the chain will be valid. The routine does not modify any difficulty values. It computes new proof of work based on the difficulty specified in the Block.
     */
    public void repairChain(){

        boolean valid = true;
        int invalid_index = 0;

        //Check if any block is invalid
        for(int i = 0; i < getChainSize(); i++){

            String hash = this.blocks.get(i).calculateHash();

            //Counting leading zeros
            int leading_zeros = 0;
            for(int k = 0; k < hash.length(); k++){
                if (hash.charAt(k) == '0'){
                    leading_zeros += 1;
                }else break;
            }
            //If leading zeros requirements not met
            if(this.blocks.get(i).getDifficulty() > leading_zeros){
                //Signify that the chain is invalid, and record the first illegal block
                valid = false;
                invalid_index = i;
                break;
            }

            //Check if the blocks are properly chained
            if(i != getChainSize()-1){
                if(!hash.equals(this.blocks.get(i+1).getPreviousHash())){
                    valid = false;
                    invalid_index = i;
                    break;
                }
            }else{
                if(!hash.equals(this.chainHash)){
                    valid = false;
                    invalid_index = i;
                    break;
                }
            }
        }

        //If there is an illegal block
        if(!valid){
            //For every block starting from the invalid block, regenerate hashes and chain them
            for(int i = invalid_index; i < getChainSize(); i++){
                String correctHash = this.blocks.get(i).proofOfWork();
                if(i != getChainSize()-1) this.blocks.get(i+1).setPreviousHash(correctHash);
                else this.chainHash = correctHash;
            }
        }

    }

    /**
     * This routine acts as a test driver for your Blockchain.
     * It takes about 100~400 milliseconds to generate and add a new block with difficulty 4 to the blockchain.
     * It takes about 1000~4000 milliseconds to generate and add a new block with difficulty 4 to the blockchain.
     * It both takes about 0~3 milliseconds to verify blocks with difficulty 4 and 5.
     * @param args
     */
    public static void main(java.lang.String[] args){

        System.out.println("BlockChain initalizing...");

        //Generate an empty blockchain
        BlockChain blockchain = new BlockChain();
        //Generate the genesis block with difficulty equal to 2, add it to blockchain
        Block genesis = new Block(0, blockchain.getTime(), "", 2);
        blockchain.addBlock(genesis);

        Scanner input = new Scanner(System.in);
        Clock clock = Clock.systemDefaultZone();

        //Construct blockchain menu
        while(true){

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

                //If choosing option 0
                if(num == 0){

                    //Print required information
                    System.out.println("Current size of chain: "+blockchain.getChainSize());
                    System.out.println("Current hashes per second by this machine: "+blockchain.hashesPerSecond());
                    Block latestBlock = blockchain.getLatestBlock();
                    System.out.println("Difficulty of most recent block: "+latestBlock.getDifficulty());
                    System.out.println("Nonce for most recent block: "+latestBlock.getNonce());
                    System.out.println("Chain hash: "+blockchain.chainHash);
                    System.out.println();

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

                    //Calculate executed time to add a new block
                    long t0 = clock.millis();
                    //Add the new block to blockchain
                    Block newBlock = new Block(blockchain.getChainSize(), blockchain.getTime(), transaction, difficulty);
                    blockchain.addBlock(newBlock);
                    long t1 = clock.millis();
                    System.out.println("Total execution time to add this block was "+(t1-t0)+" milliseconds");
                    System.out.println();

                //If choosing option 2
                }else if(num == 2){

                    //Verify the blockchain and print out the result
                    System.out.println("Verifying entire chain.");
                    long t0 = clock.millis();
                    System.out.println("Chain verification: "+blockchain.isChainValid());
                    long t1 = clock.millis();
                    System.out.println("Total execution time required to verify the chain was "+(t1-t0)+" milliseconds");
                    System.out.println();

                //If choosing option 3
                }else if(num == 3){

                    //Print out JSON representation of the blockchain
                    System.out.println("View the Blockchain.");
                    System.out.println(blockchain.toString());
                    System.out.println();

                //If choosing option 4
                }else if(num == 4){

                    //Prompt the user to enter the block id to corrupt
                    System.out.println("Corrupt the Blockchain.");
                    System.out.println("Enter block ID to Corrupt");
                    int index;
                    while(true) {
                        String reply = input.nextLine();
                        try {
                            index = Integer.parseInt(reply);
                            if ((index < 0) || (index >= blockchain.getChainSize())) {
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
                    blockchain.blocks.get(index).setData(newData);
                    System.out.println("Block "+index+" now holds "+newData);
                    System.out.println();

                //If choosing option 5
                }else if(num == 5){

                    //Repair the blockchain and print out executed time
                    System.out.println("Repairing the entire chain");
                    long t0 = clock.millis();
                    blockchain.repairChain();
                    long t1 = clock.millis();
                    System.out.println("Total execution time required to repair the chain was "+(t1-t0)+" milliseconds");
                    System.out.println();

                //If choosing option 6
                }else if(num == 6) {

                    //Exit the program
                    System.out.println("Exit.");
                    System.exit(0);

                }else{
                    System.out.println("Input is not a number from 0 to 6. Try again.");
                }


            }catch (NumberFormatException ex){
                System.out.println("Invalid choice. Try again.");
            }
        }

    }
}

