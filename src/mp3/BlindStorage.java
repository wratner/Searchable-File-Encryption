package mp3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private int kappa = 60; //temp
    private int alpha = 8;

    public BlindStorage(Integer blockSize) {
        this.blockSize = blockSize;
        enc = new MP3Encryption("illinois");
    }

    /*
     * Creates chunks from the file of BLOCK_SIZE
     */
    private List<byte[]> chunk(File file) {
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
            //String encFile = enc.encrypt(fileContent);
            //byte[] fileBytes = encFile.getBytes();
            //List<Byte> daBytes = toByteList(fileBytes);
            List<Byte> daBytes = toByteList(fileContent.getBytes());
            return createBlocks(daBytes, file.getName());
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return new ArrayList<byte[]>();
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
    private List<byte[]> createBlocks(List<Byte> inputBytes, String message_id) {
        String header;
        Integer sizef = ((Double) Math.ceil((double) inputBytes.size() / (double) blockSize)).intValue();
        int start = 0;
        int stop = inputBytes.size();
        List<byte[]> output = new ArrayList<byte[]>();
        try {
            for (start = 0; start < inputBytes.size(); start = stop) {
                if (start == 0) {
                    header = sizef + ":" + message_id + ";";
                } else {
                    header = message_id + ";";
                }
                List<Byte> headerBytes = toByteList(header.getBytes());
                List<Byte> byteList = new ArrayList<Byte>();
                byteList.addAll(headerBytes);
                // Take into account the header when getting the parts of the file that we need
                stop = blockSize - headerBytes.size() + start - 1;
                // Make sure that the size isn't past the max
                if (stop > inputBytes.size()) {
                    stop = inputBytes.size();
                }
                // stop index is exclusive
                byteList.addAll(inputBytes.subList(start, stop));

                if (stop == inputBytes.size()) {
                    byteList = addPadding(byteList);
                }

                byte[] blockBytes = toByteArray(byteList);
                String blockString = new String(blockBytes, "UTF-8");
                //output.add(blockString);
                output.add(enc.encrypt(blockString));

            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
        }
        return output;
    }

    private List<Byte> addPadding(List<Byte> block) {
        if (block.size() < blockSize - 1) {
            for (int i = block.size(); i < blockSize - 1; i++) {
                block.add("\0".getBytes()[0]);
            }
            return block;
        } else {
            return block;
        }
    }

    private List<Byte> removePadding(List<Byte> block) {
        while (block.get(block.size() - 1) == "\0".getBytes()[0]) {
            block.remove(block.size() - 1);
        }
        return block;
    }
    
    private int maxSize(int sizef) {
    	sizef = sizef * alpha;
    	if (sizef > kappa)
    		return sizef;
    	else
    		return kappa;
    	
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
            sha256_HMAC.init(secret_key);
            final byte[] mac_data = sha256_HMAC.doFinal(asciiCs.encode(fileName).array());
            int seed = new BigInteger(mac_data).intValue();
			Random pseudoGenerator = new Random(seed);
			for(int i = 0; i < kappa; i++) {
				locations.add(pseudoGenerator.nextInt(400000));
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
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
    private boolean writeBlocks(List<byte[]> blocks, Integer[] locations) {
        int prgIndex = 0;
        for (int i = 0; i < blocks.size(); i++) {
            Integer location = locations[prgIndex];
            while (isOccupied(location)) {
                prgIndex++;
                location = locations[prgIndex];
            }
            try {
                FileOutputStream blockOutputStream = new FileOutputStream("./" + location);
                blockOutputStream.write(blocks.get(i));
                blockOutputStream.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
     * Returns true if there is at least 1 block free
     * in the first kappa blocks.
     */
    private boolean sizef0Success(Integer[] locations) {
        return true;
    }

    /*
     * returns true if all the locations are free
     */
    private boolean sizefSuccess(Integer[] locations) {
        return true;
    }

    private boolean isOccupied(Integer location) {
        return false;
    }

    /*
     * Decrypts a given set of blocks and returns them with headers
     */
    private List<String> decryptBlocks(List<byte[]> blocks) throws UnsupportedEncodingException {
        List<String> output = new ArrayList<String>();
        for (byte[] block : blocks) {
            output.add(new String(enc.decrypt(block), "UTF-8"));
        }
        return output;
    }


    /*
     * Takes a list of blocks and a message id and puts back together the original message
     */
    private String buildMessage(String[] blocks, String messageId) {
        return "";
    }

    public boolean addFile(File file) {
        System.out.println("Adding file...");
        List<byte[]> chunks = chunk(file);
        System.out.println("Encrypted Length " + chunks.get(0).length);
        // Write blocks to file
        for (int i = 0; i < chunks.size(); i++) {
            byte[] block = chunks.get(i);
            try {
                FileOutputStream blockOutputStream = new FileOutputStream("./" + i);
                blockOutputStream.write(block);
                blockOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<byte[]> encryptedBlocksFromFile = new ArrayList<byte[]>();
        for (int i = 0; i < chunks.size(); i++) {
            try {
                FileInputStream inStream = new FileInputStream("./" + i);
                byte[] block = new byte[256];
                if (inStream.read(block) != -1) {
                    encryptedBlocksFromFile.add(block);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            for (String block : decryptBlocks(encryptedBlocksFromFile)) {
                System.out.println(block);
            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Starting...");
        BlindStorage blind = new BlindStorage(256);
        File file = new File("./main.xml");
        blind.addFile(file);
    }

}