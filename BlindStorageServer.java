/*
  Blind Storage Server
*/
//package mp3;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.lang.System;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BlindStorageServer {

    public static final int SERVER_PORT = 8888;   /* Make sure the port number is sufficiently high */
    public static final String LOOKUP_CMD = "LOOKUP ";
    public static final String BYE_CMD = "BYE";
    public static final String DOWNLOAD_CMD = "DOWNLD ";
    public static final String REPLY_DATA = "REPLY ";

    public static void Lookup(String documentId, PrintWriter out) {
        // THIS SHOULD BE IMPLEMENTED FOR PART-1
        /* NOTE:  The document id can be in any format you can come up with*/
        // Lookup the document-id in the encrypted documents
        // return the document contents (which are encrypted) to the client
        // out.println("REPLY "+contents);
        java.io.File file = new File("./EncryptedFiles");
        String contents;
        File[] userDirectories = file.listFiles();
        List<File> emails = new ArrayList<File>();
        for (File user : userDirectories) {
            for (File email : user.listFiles()) {
                emails.add(email);
            }
        }
        for (File email : emails) {
            if (email.getName().equals(documentId)) {

                try {
                    Scanner scanner = new Scanner(email);
                    StringBuilder builder = new StringBuilder();
                    while (scanner.hasNext()) {
                        builder.append(scanner.nextLine());
                    }
                    System.out.println(REPLY_DATA+builder.toString()+"\n"+"\n");
                } catch (FileNotFoundException e) {
                    out.println(e.getMessage());
                }

            }
        }
    }

    public static void Download(String blockIndex, PrintWriter out) {
        // THIS SHOULD BE IMPLEMENTED FOR PART-2
        // NOTE:  The blockIndex can be in any format you like
        // Lookup the block-index in the encrypted documents and return it to the clients
        // out.println("REPLY "+contents);
    }

    public static void main(String[] args) {
        PrintWriter out1 = new PrintWriter(System.out);
        //Lookup("fffe47d9d214fb2b9300028713ca4da0", out1);
        ServerSocket serverSocket;
        Socket clientSocket;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;

        // NOTE:  Here you need to send arguments for the encrypted store location and any additional
        // parameters you may need

        try {
            serverSocket = new ServerSocket(SERVER_PORT);  //Server socket
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on server port: " + SERVER_PORT);
            return;
        }
        while (true) {
            // NOTE:  For implementation efficiency, you can handle the client connection (or parts of the client operation) as threads

            try {
                clientSocket = serverSocket.accept();   //accept the client connection
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader); //get the client message
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                String message = bufferedReader.readLine();
                if (message.equalsIgnoreCase(BYE_CMD)) {
                    System.out.println(BYE_CMD);
                    inputStreamReader.close();
                    clientSocket.close();
                    break;
                } else if (message.startsWith(LOOKUP_CMD)) {
                    System.out.println(LOOKUP_CMD);
                    String documentId = message.substring(LOOKUP_CMD.length());
                    Lookup(documentId, out);
                } else if (message.startsWith(DOWNLOAD_CMD)) {
                    System.out.println(DOWNLOAD_CMD);
                    String blockIndex = message.substring(DOWNLOAD_CMD.length());
                    Download(blockIndex, out);
                } else {
                    System.err.println("ERROR: Unknown command sent to server " + message);
                }

            } catch (IOException ex) {
                System.err.println("ERROR: Problem in reading a message" + ex);
            }
        }

    }
}
