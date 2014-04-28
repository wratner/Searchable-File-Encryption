import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class Index {

	static int spc_count = -1;
	static String dirLoc = "C:\\Users\\Will\\Desktop\\Spring 2014\\ECE 424\\MP3"
			+ "\\";
	static String dirName = "";
	static String bFileName = "";
	
	static void Process(File aFile) {
		spc_count++;
		String spcs = "";
		for (int i = 0; i < spc_count; i++)
			spcs += " ";
		if (aFile.isFile()) {
			System.out.println(spcs + "[FILE] " + aFile.getName());
			try {
				bFileName = getMD5CheckSum(aFile.getPath());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			File bFile = new File(dirLoc + dirName + "\\" + bFileName);
			try {
				copyFile(aFile, bFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (aFile.isDirectory()) {
			System.out.println(spcs + "[DIR] " + aFile.getName());
			if (aFile.getName().equals("farmer-d")
					|| aFile.getName().equals("fischer-m")
					|| aFile.getName().equals("forney-j")
					|| aFile.getName().equals("fossum-d")) {
				dirName = aFile.getName();
				File newDir = new File(dirLoc + dirName);
				if (!newDir.exists()) {
					System.out.println("creating directory: "
							+ newDir.getName());
					newDir.mkdir();
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

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
	
	public static String getMD5CheckSum(String filePath) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(filePath);
 
        byte[] dataBytes = new byte[1024];
 
        int nread = 0; 
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        };
        byte[] mdbytes = md.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        //System.out.println("Digest(in hex format):: " + sb.toString());
 
        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    		String hex=Integer.toHexString(0xff & mdbytes[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
    	//System.out.println("Digest(in hex format):: " + hexString.toString());
    	return hexString.toString();
    }

	public static void main(String[] args) {
		String nam = "C:\\Users\\Will\\Desktop\\Spring 2014\\ECE 424\\CodeSkeleton\\small";
		File aFile = new File(nam);
		Process(aFile);
	}
}
