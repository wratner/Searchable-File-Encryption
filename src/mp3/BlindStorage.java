package mp3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BlindStorage {

    private Integer blockSize;
    private MP3Encryption enc;
    private String key = "illinois2";

    public BlindStorage(Integer blockSize) {
        this.blockSize = blockSize;
        enc = new MP3Encryption("illinois");
    }

    /*
     * Creates chunks from the file of BLOCK_SIZE
     */
    private List<String> chunk(File file) {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                builder.append(line);
                builder.append("\n");
                line = bufferedReader.readLine();
            }
            String fileContent = builder.toString();
            String encFile = enc.encrypt(fileContent);
            byte[] fileBytes = encFile.getBytes();
            List<Byte> daBytes = toByteList(fileBytes);
            return createBlocks(daBytes, file.getName());
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return new ArrayList<String>();
    }

    public List<Byte> toByteList(byte[] inputBytes) {
        List<Byte> daBytes = new ArrayList<Byte>();
        for (byte b : inputBytes) {
            daBytes.add(b);
        }
        return daBytes;
    }

    public byte[] toByteArray(List<Byte> inputList) {
        byte[] blockBytes = new byte[inputList.size()];
        int index = 0;
        for (byte b : inputList) {
            blockBytes[index++] = b;
        }
        return blockBytes;
    }

    /*
     * Creates blocks from file chunks by adding
     * headers and DOES NOT encrypt them.
     *
     * HEADER FORMAT: ??????
     *
     */
    private List<String> createBlocks(List<Byte> encryptedBytes, String message_id) {
        String header;
        Integer sizef = ((Double) Math.ceil((double) encryptedBytes.size() / (double) blockSize)).intValue();
        int start = 0;
        int stop = encryptedBytes.size();
        List<String> output = new ArrayList<String>();
        try {
            for (start = 0; start < encryptedBytes.size(); start = stop) {
                if (start == 0) {
                    header = sizef + ":" + message_id + ";";
                } else {
                    header = message_id + ";";
                }
                List<Byte> headerBytes = toByteList(header.getBytes());
                List<Byte> byteList = new ArrayList<Byte>();
                byteList.addAll(headerBytes);
                // Take into account the header when getting the parts of the file that we need
                stop = blockSize - headerBytes.size() + start;
                // Make sure that the size isn't past the max
                if (stop > encryptedBytes.size()) {
                    stop = encryptedBytes.size();
                }
                // stop index is exclusive
                byteList.addAll(encryptedBytes.subList(start, stop));

                if (stop == encryptedBytes.size()) {
                    byteList = addPadding(byteList);
                }

                byte[] blockBytes = toByteArray(byteList);
                String blockString = new String(blockBytes, "UTF-8");
                output.add(blockString);

            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
        }
        return output;
    }

    private List<Byte> addPadding(List<Byte> block) {
        if (block.size() < blockSize -1 ) {
            for (int i = block.size(); i < blockSize; i++) {
                block.add("\0".getBytes()[0]);
            }
            return block;
        }
        else {
            return block;
        }
    }

    private List<Byte> removePadding(List<Byte> block) {
        while (block.get(block.size() - 1) == "\0".getBytes()[0]) {
            block.remove(block.size() - 1);
        }
        return block;
    }

    /*
     * gets the placement of blocks using the filename as
     * a hashed seed to a random number generator
     *
     * QUESTION: How to pick the array size? (How many numbers do I generate?)
     */
    /* NEED TO CHANGE HARDCODED VALUES*/
    private List<Integer> getLocations(String fileName) {
    	List<Integer> locations = new ArrayList<Integer>();
    	try {
    		final Charset asciiCs = Charset.forName("US-ASCII");
            final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(asciiCs.encode("key").array(), "HmacSHA256");
            int Kappa = 10;
            sha256_HMAC.init(secret_key);
            final byte[] mac_data = sha256_HMAC.doFinal(asciiCs.encode(fileName).array());
            int seed = new BigInteger(mac_data).intValue();
			Random pseudoGenerator = new Random(seed);
			/*for(int i = 0; i < (1 + (int)(pseudoGenerator.nextInt() * ((Kappa - 1) +1))); i++) {
				locations.add( (1 + (int)(pseudoGenerator.nextInt() * ((400000 - 1) +1))));
			}*/
			for(int i = 0; i < Kappa; i++) {
				locations.add(pseudoGenerator.nextInt(400000));
			}
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return locations;
    }

    
    /*
     * Returns the blocks Identified by blockIds.
     * This reads from the filesystem.
     */
    private String[] getBlocks(Integer[] blockIds) {
        return new String[0];
    }

    /*
     * Writes the blocks out to disk by first checking to see
     * if a block is occupied before tyring to write it, if it
     * is occupied, it tries to put the block in the next place
     * in the list of locations
     *
     * TODO: ADD ENCRYPTION EITHER HERE OR SOMEWHERE ELSE
     *
     * QUESTION: Paper says to check for something and abort, need to look at this
     */
    private boolean writeBlocks(String[] blocks, Integer[] locations) {
        return false;
    }

    /*
     * Decrypts a given set of blocks and returns them with headers
     */
    private String[] decryptBlocks(String[] blocks) {
        return new String[0];
    }

    /*
     * Takes a list of blocks and a message id and puts back together the original message
     */
    private String buildMessage(String[] blocks, String messageId) {
        return "";
    }

    public boolean addFile(File file) {
        System.out.println("Adding file...");
        List<String> chunks = chunk(file);
        System.out.println("Unencrypted Length " + chunks.get(0).getBytes().length);
        System.out.println("Encrypted Length " + enc.encrypt(chunks.get(0)).getBytes().length);
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Starting...");
        BlindStorage blind = new BlindStorage(256);
        File file = new File("./main.xml");
        blind.addFile(file);
    }

}