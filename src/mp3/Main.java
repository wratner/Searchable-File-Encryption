package mp3;

//import java.util.*; // USED IN TESTPADDING()

public class Main {

    public static void main(String[] args) {
        /*MP3Encryption enc = new MP3Encryption("illinois");
        System.out.println(enc.encrypt("abcde"));
        System.out.println(enc.decrypt("E74C16F61583C2D93F33B6F0E2616627"));*/
        //This needs to be an argument ***
    	String name = args[0];
    	String key = args[1];
		new FileHash(name,key);
    }

/*    public static void testPadding() {
        List<Byte> test = new ArrayList<Byte>();
        BlindStorage bs = new BlindStorage(256);

        for (Byte b : "abcd".getBytes()) {
            test.add(b);
        }

        System.out.println(test.size());
        test = bs.addPadding(test);
        System.out.println(test.size());
        test = bs.removePadding(test);
        System.out.println(test.size());
    }*/
}
