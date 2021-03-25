import java.net.*;
import java.util.Scanner;
import java.io.*;

public class BookClient {
    public static void main(String[] args) {
        // UDP variables
        String hostAddress;
        int udpPort;

        // TCP variables
        int tcpPort;
        Socket server = null;
        PrintStream pout = null;
        Scanner din = null;

        FileWriter fileWriter;
        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append("out_").append(args[1]).append(".txt");
        String commandFile = args[0];
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port
        try {
            fileWriter = new FileWriter(fileName.toString());
            OurServerThread.ServerType serverType = OurServerThread.ServerType.UDP;
            Scanner sc = new Scanner(new FileReader(commandFile));
            InetAddress ia = InetAddress.getByName(hostAddress);
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket sPacket, rPacket;
            byte[] rbuffer = new byte[1024];
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            while (sc.hasNextLine()) {
                // parse file for command to send
                if (serverType == OurServerThread.ServerType.TCP)
                {
                    server = new Socket(ia, tcpPort);
                    pout = new PrintStream(server.getOutputStream());
                    din = new Scanner(server.getInputStream());
                }
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                byte[] bytes = cmd.getBytes();
                // send command to server
                if (tokens[0].equals("setmode")) {
                    if (tokens[1].equals("T")) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        serverType = OurServerThread.ServerType.TCP;
                        // set up TCP socket
                        server = new Socket(ia, tcpPort);
                        pout = new PrintStream(server.getOutputStream());
                        din = new Scanner(server.getInputStream());
                    } else {
                        serverType = OurServerThread.ServerType.UDP;
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                    }
                } else if (tokens[0].equals("borrow") || tokens[0].equals("return") || tokens[0].equals("list") ||
                        tokens[0].equals("inventory") || tokens[0].equals("exit")) {
                    if (serverType == OurServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                    } else {
                        pout.println(cmd);
                        pout.flush();
                    }
                    if (tokens[0].equals("exit"))
                        break;
                } else {
                    System.out.println("ERROR: No such command");
                }
                // receive message from the server
                if (serverType == OurServerThread.ServerType.UDP) {
                    datagramSocket.receive(rPacket);
                    fileWriter.write(new String(rPacket.getData(), 0, rPacket.getLength()) + "\n");
                } else {
                    while (din.hasNextLine())
                        fileWriter.write(din.nextLine() + "\n");
                    server.close();
                }
                fileWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
