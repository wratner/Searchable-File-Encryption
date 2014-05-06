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
	// private String messsage;
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
	public static String temp = "lol";
	public static ArrayList<String> documentIdList = new ArrayList<String>();
	public static ArrayList<String> messageList = new ArrayList<String>();
	public static ArrayList<String> serverOutputList = new ArrayList<String>();

	static ArrayAdapter<String> adapter;

	// For Part-1
	// USE Local Index (cached) and find the documentId from there
	// For Part-2
	// (a) if index is present in local cache use it to find the document id
	// (otherwise) use the download functionality to get the appropriate
	// document-ids from the server
	// and use them to download the files: This would be done by
	// implementing SSE.Search functionality
	// Get the output and show to the user
	public static void Lookup(String keyword, PrintWriter output)
			throws IOException {

		String documentId = "";
		InputStreamReader inputStreamReader;
		BufferedReader bufferedReader;
		String serverOutput = null;
		MP3Encryption encryption;
		int length;
		/*
		 * fileRead = new FileReader("/storage/sdcard0/index.txt"); documentId =
		 * ""; currKeyword = ""; currString = ""; nextChar = fileRead.read();
		 * while ((nextChar != -1) && (documentId == "")) { if (((char) nextChar
		 * == KEYWORD_DELIM) || ((char) nextChar == MAPPING_DELIM)) { if
		 * (currKeyword == "") { currKeyword = currString; currString = ""; }
		 * else if (currKeyword == keyword) { documentId = currString; }
		 * 
		 * if ((char) nextChar == MAPPING_DELIM) { currKeyword = ""; } } else {
		 * currString += (char) nextChar; } nextChar = fileRead.read(); }
		 * fileRead.close();
		 */

		// output.write(LOOKUP_CMD + documentId);
		try {
			FileInputStream fstream = new FileInputStream(
					"/storage/sdcard0/index.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] tokens = strLine.split(" ");
				if (tokens[0].equals(keyword)) {
					documentId = tokens[1];
					length = Array.getLength(tokens);
					for (int i = 1; i < length; i++) {
						documentIdList.add(tokens[i]);
					}
					break;
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String id : documentIdList) {
			output.write(LOOKUP_CMD + id);
			inputStreamReader = new InputStreamReader(client.getInputStream());
			bufferedReader = new BufferedReader(inputStreamReader);
			serverOutputList.add(bufferedReader.readLine());
		}

		encryption = new MP3Encryption(KEY);
		// documentIdList.add(encryption.decrypt(documentId));
		for (String s : serverOutputList) {
			messageList
					.add(encryption.decrypt(s.substring(REPLY_DATA.length())));
		}
		adapter.notifyDataSetChanged();

		return;

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