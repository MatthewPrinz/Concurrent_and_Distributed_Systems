import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class BookServer {
    public static void main(String[] args) throws FileNotFoundException {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        tcpPort = 7000;
        udpPort = 8000;
        // Book -> Count
        Map<String, Integer> inventory = new ConcurrentHashMap<>();

        // parse the inventory file
        File inventoryText = new File(args[0]);
        Scanner reader = new Scanner(inventoryText);
        while (reader.hasNextLine()) {
            String in = reader.nextLine();
            String[] data = in.split(" ");
            String title = in.substring(in.indexOf('"'), in.lastIndexOf('"')+1);
            inventory.put(title, Integer.parseInt(data[data.length-1]));
        }
        OurLibrary ourLibrary = new OurLibrary(inventory);
        try {
            ServerSocket tcpListener = new ServerSocket(tcpPort);
            DatagramSocket datagramSocket = new DatagramSocket(udpPort);

            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket rPacket = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(rPacket);
                Thread t = new OurServerThread(tcpListener, datagramSocket, rPacket, ourLibrary);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server exits");
    }
}
