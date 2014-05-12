package mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class FileHash {

    static int spc_count = -1;
    static String version = "";
    static String outPutDir;
    // CHANGE TO DIRECTORY YOU WANT ENCRYPTED FILES *******
    static String dirLoc;// = "D:\\temp_CS463\\MP3_large2" + "\\";;
    static String dirName = "";
    static String bFileName = "";
    static MP3Encryption enc = null;
    // static String encContents = "";
    static byte[] encContents;
    static String decContents = "";
    static List<String> fileList = new ArrayList<String>();
    static List<String> dirList = new ArrayList<String>();
    static boolean encryptIt = false;

    static void Process(File aFile) {
        spc_count++;
        String spcs = "";
        for (int i = 0; i < spc_count; i++)
            spcs += " ";
        if (aFile.isFile()) {
//            System.out.println(spcs + "[FILE] " + aFile.getName());

            dirList.add(aFile.getPath());
            try {
                bFileName = getMD5CheckSum(aFile.getPath());
                String contents = readFile(aFile.getPath(),
                        Charset.defaultCharset());
                if (version.equals("1")) {
                    fileList.add(bFileName);
                    File bFile = new File(dirLoc + "/" + dirName + "/" + bFileName);
                    PrintWriter out = new PrintWriter(bFile.getPath());
                    if (encryptIt) {
                        encContents = enc.encrypt(contents);
                        out.println(enc.bytesToHex(encContents));
                    } else {
                       out.println(contents);
                    }

                    out.close();
                }
                if (version.equals("2")) {
                    fileList.add(bFileName);
                    File bFile = new File(dirLoc + "/" + "emails" + "/" + dirName + "/" + bFileName);
                    PrintWriter out = new PrintWriter(bFile.getPath());
                    out.println(contents);
                    out.close();
                }
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } else if (aFile.isDirectory()) {
//            System.out.println(spcs + "[DIR] " + aFile.getName());
            if (aFile.getName().contains("-")) {
                if (version.equals("2")) {
                    File emailDir = new File(dirLoc + "/" + "emails");
                    if (!emailDir.exists()) {
                        emailDir.mkdir();
                    }
                    dirName = aFile.getName();
                    File newDir = new File(dirLoc + "/" + "emails" + "/" + dirName);
                    if (!newDir.exists()) {
//                        System.out.println("creating directory: "
//                                + newDir.getName());
                        newDir.mkdir();
                    }
                } else {
                    dirName = aFile.getName();
                    File newDir = new File(dirLoc + "/" + dirName);
                    if (!newDir.exists()) {
//                        System.out.println("creating directory: " + newDir.getName());
                        newDir.mkdir();
                    }
                }
            }
            File[] listOfFiles = aFile.listFiles();
            if (listOfFiles != null) {
                for (int i = 0; i < listOfFiles.length; i++)
                    Process(listOfFiles[i]);
            } else {
                System.out.println(spcs + " [ACCESS DENIED]");
            }
        }
        spc_count--;
    }

    public static String getMD5CheckSum(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(filePath);

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        ;
        byte[] mdbytes = md.digest();

        // convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }

        // System.out.println("Digest(in hex format):: " + sb.toString());

        // convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            String hex = Integer.toHexString(0xff & mdbytes[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        // System.out.println("Digest(in hex format):: " +
        // hexString.toString());
        fis.close();
        return hexString.toString();
    }

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void addFiletoList(String dirPath) {
        File directory = new File(dirPath);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {

                fileList.add(file.getName());
                dirList.add(file.getPath());
            } else if (file.isDirectory()) {
                addFiletoList(file.getAbsolutePath());
            }
        }
    }

    FileHash(String targetDir, String key, String versionNum, boolean indexOnly, String outputDir, boolean encryptIt) {
        this.encryptIt = encryptIt;
        dirLoc = outputDir;
//        this.outPutDir = outputDir;
//        dirName = outputDir;
        File aFile = new File(targetDir);
        version = versionNum;
        enc = new MP3Encryption(key);

        if (!indexOnly)
            Process(aFile);

        addFiletoList(dirLoc);

//        System.out.println(fileList);
//        System.out.println(dirList);
        if (indexOnly) {
            if (versionNum.equals("1")) {
                try {
                    Indexer indexer = new Indexer(key);
//                indexer.mapToIndexFile(indexer.indexMessages(null, dirList,
//                        fileList, "D:\\temp_CS463\\seperators.txt",
//                        "D:\\temp_CS463\\stopwords.txt"), dirLoc + "index.txt");
                    indexer.mapToIndexFile(indexer.indexMessages(null, dirList,
                            fileList, "./seperators.txt",
                            "./stopwords.txt"), "./index.txt");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (versionNum.equals("2")) {
                try {
                    Indexer indexer = new Indexer(key);
//                indexer.mapToIndexFiles(indexer.indexMessages(null, dirList,
//                        fileList, "D:\\temp_CS463\\seperators.txt",
//                        "D:\\temp_CS463\\stopwords.txt"), dirLoc + "keywords");
                    indexer.mapToIndexFiles(indexer.indexMessages(null, dirList,
                            fileList, "./seperators.txt",
                            "./stopwords.txt"), dirLoc + "/" + "keywords");
                } catch (IOException e) { // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
