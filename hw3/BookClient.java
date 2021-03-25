import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;

public class BookClient {
    public static void main(String[] args) {
        BookClient bookClient = new BookClient();
        // UDP stuff
        String hostAddress;
        int udpPort;
        int clientId;

        // TCP stuff
        int tcpPort;
        Socket server;
        PrintStream pout;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port
        try {
            ServerThread.ServerType serverType = ServerThread.ServerType.UDP;
            Scanner sc = new Scanner(new FileReader(commandFile));
            InetAddress ia = InetAddress.getByName(hostAddress);
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket sPacket, rPacket;
            byte[] rbuffer = new byte[1024];
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            while (sc.hasNextLine()) {
                // parse file for command to send
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                byte[] bytes = cmd.getBytes();

                // send command to server
                if (tokens[0].equals("setmode")) {
                    if (tokens[1].equals("T"))
                        serverType = ServerThread.ServerType.TCP;

                        // set up TCP socket
                        Socket server = new Socket()
                    else
                        serverType = ServerThread.ServerType.UDP;
                } else if (tokens[0].equals("borrow")) {
                    if (serverType == ServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);

                    } else if (serverType == ServerThread.ServerType.TCP) {

                    }
                } else if (tokens[0].equals("return")) {
                    if (serverType == ServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                    }
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else if (tokens[0].equals("inventory")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else if (tokens[0].equals("list")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else if (tokens[0].equals("exit")) {
                    // TODO: send appropriate command to the server
                } else {
                    System.out.println("ERROR: No such command");
                    if (serverType == ServerThread.ServerType.UDP) {
                        datagramSocket.receive(rPacket);
                        String retString = new String(rPacket.getData(), 0, rPacket.getLength());
                        System.out.println(retString);
                    } else {

                    }
                }

                // receive message from the server
                if (serverType == ServerThread.ServerType.UDP) {
                    datagramSocket.receive(rPacket);
                    String retString = new String(rPacket.getData(), 0, rPacket.getLength());
                    System.out.println(retString);
                } else {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
