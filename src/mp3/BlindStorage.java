package mp3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BlindStorage {

    private Integer blockSize;
    private MP3Encryption enc;
    private String key = "illinois2";
    private int kappa = 25; //temp
    private int alpha = 8;
    private int totalSize = 500000;
    private final String DIR = "./blind/";
    private final int BLOCK_SIZE = 2048;
    private byte[] nullBlock;
    private RandomAccessFile store;
    public static final String SERVER_IP = "172.16.184.240";// "172.22.152.61";
	public static final int SERVER_PORT = 8888;
	public static final String LOOKUP_CMD = "LOOKUP ";
	public static final String BYE_CMD = "BYE";
	public static final String DOWNLOAD_CMD = "DOWNLD ";
	public static final String REPLY_DATA = "REPLY ";

    public BlindStorage(Integer blockSize, boolean server) {
//        this.BLOCK_SIZE = BLOCK_SIZE;
        enc = new MP3Encryption("illinois");
        try {
            store = new RandomAccessFile(DIR + "store.txt", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (server)
            initBlocks();
    }

    private void initBlocks() {
        nullBlock = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            nullBlock[0] = '\0';
        }
        for (int i = 0; i < totalSize; i++) {
            try {

                store.seek(i * BLOCK_SIZE);
                store.write(nullBlock);
                //FileOutputStream blockOutputStream = new FileOutputStream(DIR + i);
                //blockOutputStream.write(nullBlock);
                //blockOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        Integer sizef = ((Double) Math.ceil((double) inputBytes.size() / (double) BLOCK_SIZE)).intValue();
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
                stop = BLOCK_SIZE - headerBytes.size() + start - 1;
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
        if (block.size() < BLOCK_SIZE - 1) {
            for (int i = block.size(); i < BLOCK_SIZE - 1; i++) {
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
    private List<Integer> getLocations(String fileName, Integer max) {
        Set<Integer> locations = new LinkedHashSet<Integer>();
        try {
            final Charset asciiCs = Charset.forName("US-ASCII");
            final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(asciiCs.encode("key").array(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            final byte[] mac_data = sha256_HMAC.doFinal(asciiCs.encode(fileName).array());
            int seed = new BigInteger(mac_data).intValue();
            Random pseudoGenerator = new Random(seed);
            for (int i = 0; i < max; i++) {
                locations.add(pseudoGenerator.nextInt(totalSize));
            }

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ArrayList<Integer>(locations);
    }

    /*
     * Returns the blocks Identified by blockIds.
     * This reads from the filesystem.
     */
    private List<byte[]> getBlocks(Integer[] blockIds) {
        List<byte[]> encryptedBlocksFromFiles = new ArrayList<byte[]>();
        for (int i = 0; i < blockIds.length; i++) {
            try {
                if (isOccupied(blockIds[i])) {
                    //FileInputStream inStream = new FileInputStream(DIR + blockIds[i]);
                    // Every block is only 256 bytes
                    byte[] block = new byte[BLOCK_SIZE];
//                    if (inStream.read(block) != -1) {
//                    }
                    store.seek(blockIds[i] * BLOCK_SIZE);
                    store.readFully(block);
                    encryptedBlocksFromFiles.add(block);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return encryptedBlocksFromFiles;
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
        // Perform our checks
        if (!(sizefOneBlockSuccess(locations) && allGood(locations, blocks.size()))) {
            return false;
        }
        // Interate through the blocks
        for (int i = 0; i < blocks.size(); i++, prgIndex++) {
            Integer location = locations[prgIndex];
            // Ensure the location is not occupied
            while (isOccupied(location)) {
                prgIndex++;
                location = locations[prgIndex];
            }
            try {
                //FileOutputStream blockOutputStream = new FileOutputStream(DIR + location);
                //blockOutputStream.write(blocks.get(i));
                //blockOutputStream.close();
                store.seek(location * BLOCK_SIZE);
                store.write(blocks.get(i));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        // return that we had success
        return true;
    }

    /*
     * Returns true if there is at least 1 block free
     * in the first kappa blocks.
     */
    private boolean sizefOneBlockSuccess(Integer[] kappaLocation) {
        boolean allGood = false;
        for (Integer location : kappaLocation) {
            if (!isOccupied(location)) {
                allGood = true;
                break;
            }
        }
        return allGood;
    }

    /*
     * returns true if sizef locations are free
     * The chance of this returning false is suppose to be negligible.
     */
    private boolean allGood(Integer[] locations, Integer sizef) {
        boolean allGood = true;
        Integer numGood = locations.length;
        for (Integer index : locations) {
            if (isOccupied(index)) {
                numGood--;
                if (numGood < sizef) {
                    allGood = false;
                    break;
                }
            }
        }
        return allGood;
    }

    private boolean isOccupied(Integer location) {
        File file = new File(DIR + location);
        try {
//            FileInputStream inStream = new FileInputStream(DIR + location);
            // Every block is only 256 bytes
            byte[] block = new byte[BLOCK_SIZE];
//            if (inStream.read(block) != -1) {
//                // yup, just an empty if...
//            }
            store.seek(location * BLOCK_SIZE);
            store.readFully(block);
            if (Arrays.equals(block, nullBlock)) {
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /*
     * Decrypts a given set of blocks and returns them with headers
     */
    private List<String> decryptBlocks(List<byte[]> blocks) {
        List<String> output = new ArrayList<String>();
        for (byte[] block : blocks) {
            try {
                output.add(new String(enc.decrypt(block), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return output;
    }


    /*
     * Takes a list of blocks and a message id and puts back together the original message
     */
    private String buildMessage(List<String> blocks, String messageId) {
        //String decoded;
        String message = "";
        int index = 0;
        int sizef = 0;
        for (String s : blocks) {
            if (s.contains(messageId)) {
                if (Character.isDigit(s.charAt(0))) {
                    if (s.indexOf(":") == 1) {
                        sizef = Character.getNumericValue(s.charAt(0)); //NEED THIS FOR LATER
                    }
                }
                index = s.indexOf(";");
                message = message + s.replace(s.substring(0, index + 1), "");
            }
        }
        return message;
    }

    /*
     * Takes a group of blocks and message id and gets sizef
     */
    private int getSizef(List<String> blocks, String messageId) {
        //String decoded;
        String message = "";
        int index = 0;
        int sizef = 0;
        for (String s : blocks) {
            if (s.contains(messageId)) {
                if (Character.isDigit(s.charAt(0))) {
                    if (s.indexOf(":") == 1) {
                        sizef = Character.getNumericValue(s.charAt(0)); //NEED THIS FOR LATER
                        return sizef;
                    }
                }
                index = s.indexOf(";");
                message = message + s.replace(s.substring(0, index + 1), "");
            }
        }
        return sizef;
    }

    public boolean addFile(File file) {
        List<byte[]> chunks = chunk(file);
        List<Integer> locations = getLocations(file.getName(), maxSize(chunks.size()));
        Integer[] intLocations = Arrays.copyOf(locations.toArray(), locations.size(), Integer[].class);
        boolean success = writeBlocks(chunks, intLocations);
        return success;
    }

    public String getFile(String messageId) {
        // Get first "kappa" locations
        List<Integer> locations = getLocations(messageId, kappa);
        Integer[] intLocations = Arrays.copyOf(locations.toArray(), locations.size(), Integer[].class);
        // Get the first "kappa" blocks
        // TODO: On client, this has to be network call
        List<byte[]> blocks = getBlocks(intLocations);
        List<String> decryptedBlocks = decryptBlocks(blocks);
        for (byte[] b : blocks) {
            b = toByteArray(removePadding(toByteList(b)));
        }
        int sizef = getSizef(decryptedBlocks, messageId);
        // See if we got all the blocks in the first go, and if not, get 'em
        if (maxSize(sizef) != kappa) {
            locations = getLocations(messageId, maxSize(sizef));
            intLocations = Arrays.copyOf(locations.toArray(), locations.size(), Integer[].class);
            blocks = getBlocks(intLocations);
            for (byte[] b : blocks) {
                b = toByteArray(removePadding(toByteList(b)));
            }
            decryptedBlocks = decryptBlocks(blocks);
        }
        String message = buildMessage(decryptedBlocks, messageId);
        return message;
    }

    public boolean testFile(File file) {
        System.out.println("Adding file...");
        List<byte[]> chunks = chunk(file);
        System.out.println("Encrypted Length " + chunks.get(0).length);
        String decryptedMessage;
        // Write blocks to file
        // for (int i = 0; i < chunks.size(); i++) {
        // byte[] block = chunks.get(i);
        // try {
        // FileOutputStream blockOutputStream = new FileOutputStream("./" + i);
        // blockOutputStream.write(block);
        // blockOutputStream.close();
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        Integer[] locations = {0, 1, 2, 3, 4, 5};
        System.out.println(writeBlocks(chunks, locations));

        // Read Bytes from file
        // List<byte[]> encryptedBlocksFromFile = new ArrayList<byte[]>();
        // for (int i = 0; i < chunks.size(); i++) {
        // try {
        // FileInputStream inStream = new FileInputStream("./" + i);
        // byte[] block = new byte[256];
        // if (inStream.read(block) != -1) {
        // encryptedBlocksFromFile.add(block);
        // }
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        List<byte[]> encryptedBlocksFromFile = getBlocks(locations);
        for (String block : decryptBlocks(encryptedBlocksFromFile)) {
            System.out.print(block);
        }
        decryptedMessage = buildMessage(decryptBlocks(encryptedBlocksFromFile), file.getName());
        System.out.println("***********************MESSAGE***********************");
        System.out.println(decryptedMessage);
        System.out.println("***********************MESSAGE***********************");
        return true;
    }
    
    public List<byte[]> getRemoteBlockIds (Integer[] blockIds, Socket socket) {
    	ArrayList<byte[]> blockIdsList = new ArrayList<byte[]>();
    	for (Integer id : blockIds) {
    		try {
				socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
				output.println(DOWNLOAD_CMD + id);
				InputStreamReader inStreamRead = new InputStreamReader(socket.getInputStream());
				BufferedReader buffRead = new BufferedReader(inStreamRead);
				String serverOutput = buffRead.readLine();
				
				byte[] serverOutputBytes = serverOutput.getBytes();
				serverOutput = new String(serverOutputBytes, "UTF-8");
				serverOutput.substring(REPLY_DATA.length());
				serverOutputBytes = serverOutput.getBytes();
				
				blockIdsList.add(serverOutputBytes);
				socket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		return blockIdsList;
    }

    public static void main(String[] args) {
        System.out.println("Starting...");
        BlindStorage blind = new BlindStorage(2048, true);
        File file = new File("./main.xml");
        blind.addFile(file);
        System.out.println("Getting file...");
        System.out.println(blind.getFile(file.getName()));
    }

}