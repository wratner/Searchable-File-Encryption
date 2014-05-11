package mp3;

import java.io.File;

/**
 * Created by Read on 5/10/2014.
 */
public class CreateBlindStore {
    public static void main(String[] args) {
        addFilesFromDirectory(args[0]);
//        storage.addFile()
    }

    public static void addFilesFromDirectory(String directory) {
        BlindStorage storage = new BlindStorage(2048, true, false);
        String datasetDirectory = directory;
        File file = new File( datasetDirectory);
        String[] subDirs = file.list();
        for (String fileName: subDirs) {
            if (fileName.equals("keywords")) {
                String[] keywords = (new File( datasetDirectory + "/" + fileName)).list();
                for(String keyword: keywords) {
                    File keyWordFile = new File( datasetDirectory + "/" + fileName + "/" + keyword);
                    storage.addFile(keyWordFile);
                }
            } else {
                String[] userDirs  = (new File( datasetDirectory + "/" + fileName)).list();
                for (String user: userDirs) {
                    String[] emails = (new File( datasetDirectory + '/' + fileName + '/' + user)).list();
                    for(String emailName: emails) {
                        if (!(emailName == null)) {
                            File email = new File( datasetDirectory + '/' + fileName + '/' + user + "/" + emailName);
                            storage.addFile(email);
                        }
                        else {
                            System.out.println("Invalid email");
                        }
                    }
                }
            }
        }
        System.out.printf("Files Added: " + storage.getFilesAdded());
    }
}
