package com.example.mp3;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ClientActivity extends Activity {

	private static Socket client;
	private PrintWriter output;
	private EditText textField;
	private Button button;
	public ListView myListView;
	public static final String INDEXFILEPATH = "/storage/sdcard0/index.txt";
	public static final int CACHEMAXLINES = 30;
	public static final String KEY = "illinois";
	public static final String SERVER_IP = "172.22.152.61"; /*
															 * Set your VM's
															 * server IP here
															 */
	public static final int SERVER_PORT = 8888; /*
												 * Set your VM's server port
												 * here: Make sure the port
												 * number is sufficiently high
												 * for your program to listen on
												 * that number
												 */
	public static final String LOOKUP_CMD = "LOOKUP ";
	public static final String BYE_CMD = "BYE";
	public static final String DOWNLOAD_CMD = "DOWNLD ";
	public static final String REPLY_DATA = "REPLY ";
	public static ArrayList<String> messageList = new ArrayList<String>();

	static ArrayAdapter<String> adapter;

	// For Part-1
	// USE Local Index (cached) and find the documentId from there
	public static void Lookup(String keyword, PrintWriter output) throws IOException {
		ArrayList<String> documentIdList;

		FileInputStream fileStream;
		DataInputStream dataIn;
		BufferedReader buffRead;

		String indexLine;
		String[] tokens;

		MP3Encryption encryption;
		InputStreamReader inStreamRead;
		String serverOutput;

		documentIdList = new ArrayList<String>();
		try {
			fileStream = new FileInputStream(INDEXFILEPATH);
			dataIn = new DataInputStream(fileStream);
			buffRead = new BufferedReader(new InputStreamReader(dataIn));
			while ((indexLine = buffRead.readLine()) != null) {
				tokens = indexLine.split(" ");
				if (tokens[0].equals(keyword)) {
					for (int i = 1; i < tokens.length; i++) {
						documentIdList.add(tokens[i]);
					}
					break;
				}
			}
			dataIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		encryption = new MP3Encryption(KEY);
		for (String id : documentIdList) {
			output.write(DOWNLOAD_CMD + id);

			inStreamRead = new InputStreamReader(client.getInputStream());
			buffRead = new BufferedReader(inputStreamReader);
			serverOutput = buffRead.readLine();

			messageList.add(encryption.decrypt(serverOutput.substring(REPLY_DATA.length())));
		}

		adapter.notifyDataSetChanged();

		return;

	}

	// For Part-2
	// (a) if index is present in local cache use it to find the document id
	// (otherwise) use the download functionality to get the appropriate
	// document-ids from the server
	// and use them to download the files: This would be done by
	// implementing SSE.Search functionality
	// Get the output and show to the user
	public static void Part2Lookup(String keyword, PrintWriter output) throws IOException {
		ArrayList<String> documentIdList;

		MP3Encryption encryption;
		InputStreamReader inStreamRead;
		String serverOutput;
		String indexLine;

		FileOutputStream fileOutStream;
		DataOutputStream dataOut;
		BufferedWriter buffWrite;

		encryption = new MP3Encryption(KEY);

		documentIdList = cacheLookup(keyword);
		if (documentIdList.isEmpty()) {
			// NEED TO HASH KEYWORD BEFORE SENDING REQUEST
			output.write(LOOKUP_CMD + keyword);

			inStreamRead = new InputStreamReader(client.getInputStream());
			buffRead = new BufferedReader(inputStreamReader);
			serverOutput = buffRead.readLine();

			indexLine = encryption.decrypt(serverOutput.substring(REPLY_DATA.length()));

			tokens = indexLine.split(" ");
			for (int i = 1; i < tokens.length; i++) {
				documentIdList.add(tokens[i]);
			}

			try {
				fileOutStream = new FileOutputStream(INDEXFILEPATH);
				dataOut = new DataOutputStream(fileOutStream);
				buffWrite = new BufferedWriter(new OutputStreamWriter(dataOut));
				buffWrite.append(indexLine);
				dataIn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (String id : documentIdList) {
			output.write(DOWNLOAD_CMD + id);

			inStreamRead = new InputStreamReader(client.getInputStream());
			buffRead = new BufferedReader(inputStreamReader);
			serverOutput = buffRead.readLine();

			messageList.add(encryption.decrypt(serverOutput.substring(REPLY_DATA.length())));
		}

		adapter.notifyDataSetChanged();

		return;

	}

	private static ArrayList<String> cacheLookup(String keyword) throws IOException {
		ArrayList<String> documentIdList;
		int indexIndex;
		ArrayList<String> indexLines;

		FileInputStream fileInStream;
		DataInputStream dataIn;
		BufferedReader buffRead;

		String indexLine;
		String[] tokens;

		FileOutputStream fileOutStream;
		DataOutputStream dataOut;
		BufferedWriter buffWrite;

		File oldCache;
		File newCache;

		documentIdList = new ArrayList<String>();
		indexIndex = 0;
		indexLines = new ArrayList<String>();
		try {
			fileInStream = new FileInputStream(INDEXFILEPATH);
			dataIn = new DataInputStream(fileInStream);
			buffRead = new BufferedReader(new InputStreamReader(dataIn));
			while ((indexLine = buffRead.readLine()) != null) {
				tokens = indexLine.split(" ");
				if (tokens[0].equals(keyword)) {
					for (int i = 1; i < tokens.length; i++) {
						documentIdList.add(tokens[i]);
					}
				} else {
					indexLines.add(indexLine);
					if (documentIdList.isEmpty()) {
						indexIndex++;
					}
				}
			}
			dataIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (documentIdList.isEmpty()) {
			indexIndex = -1;
		}

		try {
			fileOutStream = new FileOutputStream(INDEXFILEPATH + "temp");
			dataOut = new DataOutputStream(fileOutStream);
			buffWrite = new BufferedWriter(new OutputStreamWriter(dataOut));
			if (indexLines.size() >= CACHEMAXLINES) {
				if (indexIndex == -1) {
					indexLines.remove(0);
				} else {
					indexLine = indexLines.get(indexIndex);
					indexLines.set(indexIndex, indexLines.get(indexLines.size() - 1));
					indexLines.set(indexLines.size() - 1, indexLine);
				}
			}
			for (indexLine : indexLines) {
				buffWrite.write(indexLine, 0, indexLine.length());
				buffWrite.newLine()
			}
			dataIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		oldCache = new File(INDEXFILEPATH);
		oldCache.delete();
		newCache = new File(INDEXFILEPATH + "temp");
		newCache.renameTo(INDEXFILEPATH);

		return documentIdList;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textField = (EditText) findViewById(R.id.editText1); // reference to the
																// text field
		button = (Button) findViewById(R.id.button1); // reference to the send
														// button
		myListView = (ListView) findViewById(R.id.messageListView);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, messageList);
		myListView.setAdapter(adapter);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Button press event listener
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String keyword = textField.getText().toString(); // get the
																	// keyword
																	// from the
																	// textfield
				textField.setText(""); // Reset the text field to blank

				try {
					client = new Socket(SERVER_IP, SERVER_PORT); // connect to
																	// server
					output = new PrintWriter(client.getOutputStream(), true);
					Lookup(keyword, output);
					// printwriter.flush();
					// printwriter.close();
					// client.close(); //closing the connection

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}
}
