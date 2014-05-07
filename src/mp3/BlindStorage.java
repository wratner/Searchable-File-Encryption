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
            byte[] fileBytes = builder.toString().getBytes();
            return createBlocks(fileBytes, file.getName());
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /*
     * Creates blocks from file chunks by adding
     * headers and DOES NOT encrypt them.
     *
     * HEADER FORMAT: ??????
     *
     */
    private List<String> createBlocks(byte[] fileBytes, String message_id) {
        String header;
        Integer sizef = ((Double) Math.ceil((double) fileBytes.length / (double) blockSize)).intValue();
        int start = 0;
        int stop = fileBytes.length;
        List<String> output = new ArrayList<String>();
        try {
            for (start = 0; start < fileBytes.length; start = stop) {
                if (start == 0) {
                    header = fileBytes.length + ":" + message_id + ";";
                } else {
                    header = message_id;
                }
                byte[] headerBytes = header.getBytes();
                List<Byte> fileByteList = new ArrayList(Arrays.asList(fileBytes));
                List<Byte> byteList = new ArrayList(Arrays.asList(headerBytes));
                stop = blockSize - headerBytes.length;
                // stop index is exclusive
                byteList.addAll(fileByteList.subList(start, stop));
                Byte[] block = (Byte[]) byteList.toArray();
                byte[] blockBytes = new byte[block.length];
                int index = 0;
                for (byte b : block) {
                    blockBytes[index++] = b;
                }
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
        List<String> chunks = chunk(file);
        System.out.println("Unencrypted Length " + chunks.get(0).getBytes().length);
        System.out.println("Encrypted Length " + enc.encrypt(chunks.get(0)).getBytes().length);
        return true;
    }

    public static void main(String[] args) {
        BlindStorage blind = new BlindStorage(256);
        File file = new File("./main.xml");
        blind.addFile(file);
    }

}