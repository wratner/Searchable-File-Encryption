import java.util.*;
import java.io.*;

public char KEYWORD_DELIM = ':';
public char MESSAGE_ID_DELIM = ',';

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
public Map<String, Set<String>> indexMessage(Map<String, Set<String>> map, String messagePath, String messageID, String separators, List<String> stopwords) {
	Set<String> keywords;
	FileReader fileRead;
	String currString;
	int nextChar;
	Set<String> messages;

	// PARSE EACH WORD OF THE MESSAGE
	// IF THE WORD IS NOT A STOPWORD, ADD IT TO THE SET OF KEYWORDS
	keywords = new HashSet<String>();
	try {
		fileRead = new FileReader(stopwordsPath);

		currString = "";
		nextChar = fileRead.read();
		if (nextChar != -1) {
			if (separators.indexOf((char) nextChar) == -1) {
				currString += nextChar;
			} else {
				if (!stopwords.contains(currString)) {
					keywords.add(currString);
				}
				currString = "";
			}
			nextChar = fileRead.read();
		}
	} catch (IOException e) {
		keywords.clear();
	} finally {
		if (fileRead != null) {
			fileRead.close();
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
public Map<String, Set<String>> indexMessages(Map<String, Set<String>> map, List<String> messagePaths, List<String> messageIDs, String separatorsPath, String stopwordsPath) {
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
	} finally {
		if (fileRead != null) {
			fileRead.close();
		}
	}

	if (exception) {
		return map;
	}

	// PARSE IN STOPWORDS
	stopwords = new ArrayList<String>();
	try {
		fileRead = new FileReader(stopwordsPath);

		currString = "";
		nextChar = fileRead.read();
		while (nextChar != -1) {
			if ((char) nextChar != '\n') {
				currString += nextChar;
			} else {
				stopwords.add(currString);
				currString = "";
			}
			nextChar = fileRead.read();
		}
	} catch (IOException e) {
		exception = true;
	} finally {
		if (fileRead != null) {
			fileRead.close();
		}
	}

	if (exception) {
		return map;
	}

	// INDEX MESSAGES
	for (int i = 0; i <= messagePaths.size(); i++) {
		map = indexMessage(map, messagePath, messageID, separators, stopwords);
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
public Boolean mapToIndexFile(Map<String, Set<String>> map, String indexPath) {
	Boolean result;
	FileWriter fileWrite;
	String toWrite;

	result = true
	try {
		fileWrite = new FileWriter(indexPath);

		for (String keyword : map.keySet()) {
			toWrite = keyword + KEYWORD_DELIM;
			for (String messageID : map.get(keyword)) {
				toWrite += messageID + MESSAGE_ID_DELIM;
			}
			toWrite.setCharAt(toWrite.length() - 1, '\n');

			for (char nextChar : toWrite) {
				fileWrite.write(nextChar);
			}
		}
	} catch (IOException e) {
		result = false;
	} finally {
		fileWrite.close();
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
public Map<String, Set<String>> indexFileToMap(String indexPath) {
	Map<String, Set<String>> map;
	FileReader fileRead;
	String currString;
	int nextChar;
	String currKeyword;
	Set<String> currMessages;

	map = new HashMap<String, Set<String>>();

	try {
		fileRead = new FileReader(separatorsPath);
		currString = "";
		currKeyword = "";
		currMessages = new HashSet<String>();

		nextChar = fileRead.read();
		while (nextChar != -1) {
			if ((char) nextChar == KEYWORD_DELIM) {
				currKeyword = currString;
				currString = "";
			} else if ((char) nextChar == MESSAGE_ID_DELIM) {
				currMessages.add(currString);
				currString = "";
			} else if ((char) nextChar == '\n') {
				map.put(currKeyword, currMessages);
				currKeyword = "";
				currMessages = new HashSet<String>();
			} else {
				currString += (char) nextChar;
			}
			nextChar = fileRead.read();
		}
	} catch (IOException e) {
		map = null;
	} finally {
		if (fileRead != null) {
			fileRead.close();
		}
	}

	return map;
}
