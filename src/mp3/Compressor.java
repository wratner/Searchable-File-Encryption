package mp3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compressor {
    public static byte[] compress(String input) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(stream);
                byte[] buffer = new byte[2048];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    gzipOutputStream.write(buffer, 0, len);
                }
                gzipOutputStream.finish();
                byte[] compressedBytes = stream.toByteArray();
                return compressedBytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static String decompress(byte[] input) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            String output = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void main(String[] args) {
        String temp;
//        String temp = "This is an example of a string that is going to get the mess compressed out of it!";
        try {
            temp = FileHash.readFile("./main.xml", Charset.forName("UTF-8"));
            try {
                System.out.println("The length of the string is: " + temp.getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            byte[] output = compress(temp);
            System.out.println("output length is: " + output.length);
            String outputString = decompress(output);
            System.out.println(outputString);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
