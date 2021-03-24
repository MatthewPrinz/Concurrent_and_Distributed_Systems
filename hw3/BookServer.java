import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
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
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;
        // Book -> Count
        Map<String, Integer> inventory = new ConcurrentHashMap<>();

        // parse the inventory file
        File inventoryText = new File("inventory.txt");
        Scanner reader = new Scanner(inventoryText);
        while (reader.hasNextLine()) {
            String[] data = reader.nextLine().split(" ");
            inventory.put(data[0], Integer.parseInt(data[1]));
        }
        Library library = new Library(inventory);
        // TODO: handle request from clients
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
                datagramSocket.receive(rPacket);
                Thread t = new ServerThread(tcpListener, datagramSocket, rPacket, library);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
