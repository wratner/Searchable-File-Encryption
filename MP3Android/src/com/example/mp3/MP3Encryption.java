package com.example.mp3;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Read on 4/27/14.
 * REFERENCE:
 *
 * ## This showed us a general layout and the classes involved
 * http://www.javamex.com/tutorials/cryptography/symmetric.shtml
 *
 * ## This gives some info on the differences between various encryption providers
 * https://stackoverflow.com/questions/20581169/java-crypto-api-how-to-choose-a-cipher-provider
 *
 * ## This shows how to pad the key
 * https://stackoverflow.com/questions/2375541/password-verification-with-pbkdf2-in-java
 *
 * This talks about initialization vectors for the padding provider
 * https://stackoverflow.com/questions/6669181/why-does-my-aes-encryption-throws-an-invalidkeyexception
 *
 * ## NOTE: I have code copy and pasted from these 2
 * ## Android doesn't have bytes2Hex and vice versa functions - I could have used an Apache
 * ## library here, but this was simpler and kept our code self contained.
 * https://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
 * https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
 */
public class MP3Encryption {
    private Cipher cipher;
    private Key key;
    IvParameterSpec ivspec;

    MP3Encryption(String keyString) {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(keyString.toCharArray(), "abcd".getBytes(), 1024, 128);
            SecretKey secret = factory.generateSecret(keySpec);
            //key = new SecretKeySpec(keyString.getBytes(), "AES");
            key = new SecretKeySpec(secret.getEncoded(), "AES");
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            ivspec = new IvParameterSpec(iv);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("ENCRYPTION: AES ISN'T ON THIS SYSTEM");
            System.out.println(e.getMessage());
        } catch (NoSuchPaddingException e) {
            System.out.println("PADDING: PKCS5Padding ISN'T ON THIS SYSTEM");
            System.out.println(e.getMessage());

        } catch (InvalidKeySpecException e) {
            System.out.println("KEY SPEC: It's invalid.");
            System.out.println(e.getMessage());
        }
    }

    public byte[] encrypt(String input) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
            byte[] encrypted_bytes = cipher.doFinal(input.getBytes());
            return encrypted_bytes;//bytesToHex(encrypted_bytes);
        } catch (IllegalBlockSizeException e) {
            System.out.println("BLOCK SIZE: Block size is bad");
            System.out.println(e.getMessage());
        } catch (BadPaddingException e) {
            System.out.println("PADDING: Padding doesn't line up");
            System.out.println(e.getMessage());
        } catch (InvalidKeyException e) {
            System.out.println("KEY: Invalid key being used.");
            System.out.println(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("ALG: Invalid parameters");
            System.out.println(e.getMessage());
        }
        return new byte[0];
    }

    public byte[] decrypt(byte[] input) {
        try {
            byte[] inputBytes = input;//hexStringToByteArray(input);
            //System.out.println(inputBytes.toString());
            cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
            byte[] decryptedBytes = cipher.doFinal(inputBytes);
            return decryptedBytes;//new String(decryptedBytes, "UTF-8");
        } catch (IllegalBlockSizeException e) {
            System.out.println("BLOCK SIZE: Block size is bad");
            System.out.println(e.getMessage());
        } catch (BadPaddingException e) {
            System.out.println("PADDING: Padding doesn't line up");
            System.out.println(e.getMessage());
        } catch (InvalidKeyException e) {
            System.out.println("KEY: Invalid key being used.");
            System.out.println(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("ALG: Invalid parameters");
            System.out.println(e.getMessage());
        } /*catch (UnsupportedEncodingException e) {
            System.out.println("STRING: Invalid encoding");
            System.out.println(e.getMessage());
        }*/

        return new byte[0];
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
