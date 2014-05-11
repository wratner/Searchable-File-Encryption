package mp3;

import java.io.File;

/**
 * Created by Read on 5/10/2014.
 */
public class CreateBlindStore {
    public static void main(String[] args) {
        BlindStorage storage = new BlindStorage(2048, true, false);
        File file = new File(args[0]);
        String[] subDirs = file.list();
        for (String fileName: subDirs) {
            if (fileName.equals("keywords")) {
                String[] keywords = (new File(args[0] + "/" + fileName)).list();
                for(String keyword: keywords) {
                    File keyWordFile = new File(args[0] + "/" + fileName + "/" + keyword);
                    storage.addFile(keyWordFile);
                }
            } else {
                String[] userDirs  = (new File(args[0] + "/" + fileName)).list();
                for (String user: userDirs) {
                    String[] emails = (new File(args[0] + '/' + fileName + '/' + user)).list();
                    for(String emailName: emails) {
                        if (!(emailName == null)) {
                            File email = new File(args[0] + '/' + fileName + '/' + user + "/" + emailName);
                            storage.addFile(email);
                        }
                        else {
                            System.out.println("WTF");
                        }
                    }
                }
            }
        }
        System.out.printf("Files Added: " + storage.getFilesAdded());
//        storage.addFile()
    }
}
