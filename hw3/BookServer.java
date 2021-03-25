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
            String[] data = reader.nextLine().split("\" ");
            inventory.put(data[0].substring(1), Integer.parseInt(data[data.length-1]));
        }
        System.out.println(inventory);
        OurLibrary ourLibrary = new OurLibrary(inventory);
        try {
            ServerSocket tcpListener = new ServerSocket(tcpPort);
            DatagramSocket datagramSocket = new DatagramSocket(udpPort);
            // parallelize UDP (spin off new thread to handle it, in case of multiple UDPpackets coming in at same time)
            // do we want 2 different classes? serverudpthread and servertcpthread?

            // pretty sure we want 1 class, that way we can switch back and forth easier. so i think we must pass
            // tcpListener over
            //
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket rPacket = new DatagramPacket(buf, buf.length);
                System.out.println("About to block");
                datagramSocket.receive(rPacket);
                System.out.println("BookServer: " + Arrays.toString(rPacket.getData()));
                Thread t = new OurServerThread(tcpListener, datagramSocket, rPacket, ourLibrary);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server exits");
    }
}
