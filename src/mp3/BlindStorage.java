package mp3;

import java.io.*;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlindStorage {

    private Integer blockSize;
    private MP3Encryption enc;

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
                stop = blockSize - headerBytes.size() + start;
                if (stop > encryptedBytes.size()) {
                    stop = encryptedBytes.size();
                }
                // stop index is exclusive
                byteList.addAll(encryptedBytes.subList(start, stop));
                byte[] blockBytes = toByteArray(byteList);
                String blockString = new String(blockBytes, "UTF-8");
                output.add(blockString);
            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
        }
        return output;
    }

    /*
     * gets the placement of blocks using the filename as
     * a hashed seed to a random number generator
     *
     * QUESTION: How to pick the array size? (How many numbers do I generate?)
     */
    private Integer[] getLocations(String fileName) {
        return new Integer[0];
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