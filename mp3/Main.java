package com.mp3;

import com.mp3.MP3Encryption;
public class Main {

    public static void main(String[] args) {
        /*MP3Encryption enc = new MP3Encryption("illinois");
        System.out.println(enc.encrypt("abcde"));
        System.out.println(enc.decrypt("E74C16F61583C2D93F33B6F0E2616627"));*/
        //This needs to be an argument ***
        String nam = "C:\\Users\\Will\\Desktop\\Spring 2014\\ECE 424\\CodeSkeleton\\small";
		FileHash i = new FileHash(nam);
    }
}
