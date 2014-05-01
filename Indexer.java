package com.mp3local;

import java.util.*;
import java.io.*;

public class Indexer {
    private static char KEYWORD_DELIM = ' ';
    private static char MAPPING_DELIM = '\n';

    /*
     * Indexes a single message into a mapping of keywords to sets of messages,
     * given a string containing separators and a list of stopwords. Adds the
     * message ID to the set of any keywords found in the message.
     *
     * Parameters:
     * map: Map to add index data to.
     * messagePath: Message filepath.
     * messageID: Message ID.
     * separators: String of separators.
     * stopwords: List of stopwords.
     *
     * Returns:
     * Map with added index data.
     */
    public static Map<String, Set<String>> indexMessage(Map<String, Set<String>> map, String messagePath, String messageID, String separators, List<String> stopwords) throws IOException {
        Set<String> keywords;
        FileReader fileRead;
        String currString;
        int nextChar;
        Set<String> messages;

        // PARSE EACH WORD OF THE MESSAGE
        // IF THE WORD IS NOT A STOPWORD, ADD IT TO THE SET OF KEYWORDS
        fileRead = new FileReader(messagePath);

        keywords = new HashSet<String>();

        currString = "";
        nextChar = fileRead.read();
        while (nextChar != -1) {
            if (separators.indexOf((char) nextChar) == -1) {
                currString += (char) nextChar;
            } else if (currString != "") {
                if (!stopwords.contains(currString)) {
                    keywords.add(currString);
                }
                currString = "";
            }
            nextChar = fileRead.read();
        }

        fileRead.close();

        // FOR EACH KEYWORD, ADD THIS MESSAGE TO ITS SET OF MESSAGES
        if (map == null) {
            map = new HashMap<String, Set<String>>();
        }
        for (String keyword : keywords) {
            messages = map.get(keyword);
            if (messages == null) {
                messages = new HashSet<String>();
            }
            messages.add(messageID);
            map.put(keyword, messages);
        }

        return map;
    }

    /*
     * Indexes a list of messages into a mapping of keywords to sets of messages,
     * given a file containing separators and stopwords.
     *
     * Parameters:
     * map: Map to add index data to (null to create a new map).
     * messagePaths: List of message filepaths (must be same length as messageIDs).
     * messageIDs: List of message IDs (must be same length as messagePaths).
     * separatorsPath: Filepath for separators.
     * stopwordsPath: Filepath for stopwords.
     *
     * Returns:
     * Map with added index data.
     */
    public static Map<String, Set<String>> indexMessages(Map<String, Set<String>> map, List<String> messagePaths, List<String> messageIDs, String separatorsPath, String stopwordsPath) throws IOException {
        FileReader fileRead;
        String currString;
        int nextChar;
        String separators;
        List<String> stopwords;

        if (map == null) {
            map = new HashMap<String, Set<String>>();
        }
        if (messagePaths.size() != messageIDs.size() || messagePaths.size() == 0) {
            return map;
        }

        // PARSE IN SEPARATORS
/*        fileRead = new FileReader(separatorsPath);
        separators = "";

        currString = "";
        nextChar = fileRead.read();
        while (nextChar != -1) {
            separators += (char) nextChar;
            nextChar = fileRead.read();
        }

        fileRead.close();*/

        separators = "\t\n\r ";

        // PARSE IN STOPWORDS
        fileRead = new FileReader(stopwordsPath);
        stopwords = new ArrayList<String>();

        currString = "";
        nextChar = fileRead.read();
        while (nextChar != -1) {
            if ((char) nextChar != '\n') {
                currString += (char) nextChar;
            } else {
                stopwords.add(currString);
                currString = "";
            }
            nextChar = fileRead.read();
        }

        fileRead.close();

        // INDEX MESSAGES
        for (int i = 0; i < messagePaths.size(); i++) {
            map = indexMessage(map, messagePaths.get(i), messageIDs.get(i), separators, stopwords);
        }
        return map;
    }

    /*
     * Writes a mapping of keywords to message ID's to a file of the following
     * format:
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...<MAPPING_DELIM>
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...<MAPPING_DELIM>
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...<MAPPING_DELIM>
     *
     * Parameters:
     * map: Map to write to the file.
     * indexPath: The filepath for the index file.
     *
     * Returns:
     * Whether or not the operation was successful.
     */
    public static Boolean mapToIndexFile(Map<String, Set<String>> map, String indexPath) throws IOException {
        FileWriter fileWrite;
        String toWrite;

        if (map == null) {
            return false;
        }

        fileWrite = new FileWriter(indexPath);

        for (String keyword : map.keySet()) {
            toWrite = keyword + KEYWORD_DELIM;
            for (String messageID : map.get(keyword)) {
                toWrite += messageID + KEYWORD_DELIM;
            }
            toWrite = toWrite.substring(0, toWrite.length() - 1) + MAPPING_DELIM;

            for (char nextChar : toWrite.toCharArray()) {
                fileWrite.write(nextChar);
            }
        }

        fileWrite.close();

        return true;
    }

    /*
     * Parses a mapping of keywords to message ID's from a file.
     *
     * Parameters:
     * indexPath: The filepath for the index file.
     *
     * Returns:
     * The parsed mapping. If there is a failure in execution, null is returned.
     */
    public static Map<String, Set<String>> indexFileToMap(String indexPath) throws IOException {
        Map<String, Set<String>> map;
        FileReader fileRead;
        String currString;
        int nextChar;
        String currKeyword;
        Set<String> currMessages;

        map = new HashMap<String, Set<String>>();

        fileRead = new FileReader(indexPath);

        currString = "";
        currKeyword = "";
        currMessages = new HashSet<String>();

        nextChar = fileRead.read();
        while (nextChar != -1) {
            if (((char) nextChar == KEYWORD_DELIM) || ((char) nextChar == MAPPING_DELIM)) {
                if (currKeyword == "") {
                    currKeyword = currString;
                    currString = "";
                } else {
                    currMessages.add(currString);
                    currString = "";
                }

                if ((char) nextChar == MAPPING_DELIM) {
                    if (currKeyword != "") {
                        map.put(currKeyword, currMessages);
                    }
                    currKeyword = "";
                    currMessages = new HashSet<String>();
                }
            } else {
                currString += (char) nextChar;
            }
            nextChar = fileRead.read();
        }

        fileRead.close();

        return map;
    }
}
