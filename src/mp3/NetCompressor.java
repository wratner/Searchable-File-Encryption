package mp3;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class NetCompressor {
    private PrintWriter writer;
    private BufferedReader reader;
    private ArrayList<Byte> dataQueue;

    public NetCompressor(PrintWriter writer1, BufferedReader reader1) {
        writer = writer1;
        reader = reader1;
        dataQueue = new ArrayList<Byte>();
    }

    public void queue(byte[] data) {
        for (Byte b : data) {
            dataQueue.add(b);
        }
    }

    public void send() {
        Deflater comp;
        byte[] sendBuff;
        int compLength;

        sendBuff = new byte[dataQueue.size()];
        for (int i = 0; i < dataQueue.size(); i++) {
            sendBuff[i] = dataQueue.get(i);
        }

        comp = new Deflater();
        comp.setInput(sendBuff);
        comp.finish();
        compLength = comp.deflate(sendBuff);
        comp.end();

        writer.write(Integer.toString(dataQueue.size()) + "\n");
        writer.write(Integer.toString(compLength) + "\n");
        writer.write(sendBuff.toString() + "\n", 0, compLength + 1);

        dataQueue.clear();
    }

    public byte[] recv() throws IOException, DataFormatException {
        int decompLength;
        int compLength;
        char[] recvBuff;
        Inflater decomp;
        byte[] output;

        decompLength = Integer.parseInt(reader.readLine());
        compLength = Integer.parseInt(reader.readLine());
        recvBuff = new char[compLength];
        reader.read(recvBuff, 0, compLength);

        decomp = new Inflater();
        decomp.setInput(new String(recvBuff).getBytes());
        output = new byte[decompLength];
        decomp.inflate(output);
        decomp.end();

        return output;
    }
}
