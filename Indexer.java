package com.mp3;

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
     * Map with added index data. If there is a failure in execution, the original
     * is returned.
     */
    public static Map<String, Set<String>> indexMessage(Map<String, Set<String>> map, String messagePath, String messageID, String separators, List<String> stopwords) {
        Set<String> keywords;
        FileReader fileRead;
        String currString;
        int nextChar;
        Set<String> messages;

        // PARSE EACH WORD OF THE MESSAGE
        // IF THE WORD IS NOT A STOPWORD, ADD IT TO THE SET OF KEYWORDS
        fileRead = null;
        keywords = new HashSet<String>();
        try {
            fileRead = new FileReader(messagePath);

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
        } catch (IOException e) {
            keywords.clear();
        }
        if (fileRead != null) {
            try {
                fileRead.close();
            } catch (IOException e) {
            }
        }

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
     * Map with added index data. If there is a failure in execution, the original
     * is returned.
     */
    public static Map<String, Set<String>> indexMessages(Map<String, Set<String>> map, List<String> messagePaths, List<String> messageIDs, String separatorsPath, String stopwordsPath) {
        FileReader fileRead;
        String currString;
        int nextChar;
        Boolean exception;
        String separators;
        List<String> stopwords;

        if (map == null) {
            map = new HashMap<String, Set<String>>();
        }
        if (messagePaths.size() != messageIDs.size() || messagePaths.size() == 0) {
            return map;
        }

        exception = false;

        // PARSE IN SEPARATORS
/*        fileRead = null;
        separators = "";
        try {
            fileRead = new FileReader(separatorsPath);

            currString = "";
            nextChar = fileRead.read();
            while (nextChar != -1) {
                separators += (char) nextChar;
                nextChar = fileRead.read();
            }
        } catch (IOException e) {
            exception = true;
        }
        if (fileRead != null) {
            try {
                fileRead.close();
            } catch (IOException e) {
            }
        }

        if (exception) {
            return map;
        }*/
        separators = "\t\n\r ";

        // PARSE IN STOPWORDS
        fileRead = null;
        stopwords = new ArrayList<String>();
        try {
            fileRead = new FileReader(stopwordsPath);

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
        } catch (IOException e) {
            exception = true;
        }
        if (fileRead != null) {
            try {
                fileRead.close();
            } catch (IOException e) {
            }
        }

        if (exception) {
            return map;
        }

        // INDEX MESSAGES
        for (int i = 0; i < messagePaths.size(); i++) {
            map = indexMessage(map, messagePaths.get(i), messageIDs.get(i), separators, stopwords);
        }
        return map;
    }

    /*
     * Writes a mapping of keywords to message ID's to a file of the following
     * format:
     * <keyword>:<message id>,<message id>,...
     * <keyword>:<message id>,<message id>,...
     * <keyword>:<message id>,<message id>,...
     *
     * Parameters:
     * map: Map to write to the file.
     * indexPath: The filepath for the index file.
     *
     * Returns:
     * Whether or not the operation was successful.
     */
    public static Boolean mapToIndexFile(Map<String, Set<String>> map, String indexPath) {
        Boolean result;
        FileWriter fileWrite;
        String toWrite;

        if (map == null) {
            return false;
        }

        fileWrite = null;
        result = true;
        try {
            fileWrite = new FileWriter(indexPath);

            for (String keyword : map.keySet()) {
                toWrite = keyword + KEYWORD_DELIM;
                for (String messageID : map.get(keyword)) {
                    toWrite += messageID + KEYWORD_DELIM;
                }
                toWrite = toWrite.substring(0, toWrite.length() - 1) + '\n';

                for (char nextChar : toWrite.toCharArray()) {
                    fileWrite.write(nextChar);
                }
            }
        } catch (IOException e) {
            result = false;
        }
        if (fileWrite != null) {
            try {
                fileWrite.close();
            } catch (IOException e) {
            }
        }

        return result;
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
    public static Map<String, Set<String>> indexFileToMap(String indexPath) {
        Map<String, Set<String>> map;
        FileReader fileRead;
        String currString;
        int nextChar;
        String currKeyword;
        Set<String> currMessages;

        map = new HashMap<String, Set<String>>();

        fileRead = null;
        try {
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
        } catch (IOException e) {
            map = null;
        }
        if (fileRead != null) {
            try {
                fileRead.close();
            } catch (IOException e) {
            }
        }

        return map;
    }
}
