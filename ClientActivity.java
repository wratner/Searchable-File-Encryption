package com.example.mp3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ClientActivity extends Activity {

	private static Socket client;
	private PrintWriter output;
	private EditText textField;
	private Button button;
	// private String messsage;
	public static final String KEY = "illinois";
	public static final String SERVER_IP = ""; /* Set your VM's server IP here */
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
		FileReader fileRead;
		char KEYWORD_DELIM = ' ';
		char MAPPING_DELIM = '\n';
		String documentId;
		String currString;
		int nextChar;
		String currKeyword;

		InputStreamReader inputStreamReader;
		BufferedReader bufferedReader;
		String serverOutput;
		MP3Encryption encryption;

		fileRead = new FileReader("D:\\temp_CS463\\MP3\\index.txt");
		documentId = "";
		currKeyword = "";
		currString = "";
		nextChar = fileRead.read();
		while ((nextChar != -1) && (documentId == "")) {
			if (((char) nextChar == KEYWORD_DELIM)
					|| ((char) nextChar == MAPPING_DELIM)) {
				if (currKeyword == "") {
					currKeyword = currString;
					currString = "";
				} else if (currKeyword == keyword) {
					documentId = currString;
				}

				if ((char) nextChar == MAPPING_DELIM) {
					currKeyword = "";
				}
			} else {
				currString += (char) nextChar;
			}
			nextChar = fileRead.read();
		}
		fileRead.close();

		output.write(LOOKUP_CMD + documentId);

		inputStreamReader = new InputStreamReader(client.getInputStream());
		bufferedReader = new BufferedReader(inputStreamReader);
		serverOutput = bufferedReader.readLine();

		encryption = new MP3Encryption(KEY);
		encryption.decrypt(serverOutput.substring(REPLY_DATA.length()));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textField = (EditText) findViewById(R.id.editText1); // reference to the
																// text field
		button = (Button) findViewById(R.id.button1); // reference to the send
														// button
		// read in stopwords file
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