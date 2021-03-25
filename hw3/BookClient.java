import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

public class BookClient {
    public static void main(String[] args) {
        BookClient bookClient = new BookClient();
        // UDP stuff
        String hostAddress;
        int udpPort;
        int clientId;

        // TCP stuff
        int tcpPort;
        Socket server = null;
        PrintStream pout = null;
        Scanner din = null;

        FileWriter fileWriter = null;
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
                    System.out.println("Reinitializing server, pout, din");
                }
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                byte[] bytes = cmd.getBytes();
                System.out.println("cmd is " + cmd + " tokens are: " + Arrays.toString(tokens));
                // send command to server
                if (tokens[0].equals("setmode")) {
                    if (tokens[1].equals("T")) {
                        System.out.println("setting to TCP");
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        serverType = OurServerThread.ServerType.TCP;
                        // set up TCP socket
                        server = new Socket(ia, tcpPort);
                        pout = new PrintStream(server.getOutputStream());
                        din = new Scanner(server.getInputStream());
                        System.out.println("Done setting to TCP");
                    } else {
                        System.out.println("setting to UDP");
                        serverType = OurServerThread.ServerType.UDP;
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        System.out.println("Done setting to UDP");
                    }
                } else if (tokens[0].equals("borrow")) {
                    System.out.println("BookClient: in borrow");
                    if (serverType == OurServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        System.out.println("borrow: Sending: " + Arrays.toString(bytes));
                    } else {
                        pout.println(cmd);
                        pout.flush();
                        System.out.println("Sending cmd: " + cmd + " over Tcp");
                    }
                } else if (tokens[0].equals("return")) {
                    System.out.println("BookClient: in return");
                    if (serverType == OurServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        System.out.println("return: Sending: " + Arrays.toString(bytes));
                    }
                    else
                    {
                        pout.println(cmd);
                        pout.flush();
                    }
                } else if (tokens[0].equals("inventory")) {
                    System.out.println("BookClient: in inventory");
                    if (serverType == OurServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        System.out.println("inventory: Sending: " + Arrays.toString(bytes));
                    }
                    else
                    {
                        pout.println(cmd);
                        pout.flush();
                    }
                } else if (tokens[0].equals("list")) {
                    System.out.println("BookClient: in list");
                    if (serverType == OurServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        System.out.println("list: Sending: " + Arrays.toString(bytes));
                    }
                    else
                    {
                        pout.println(cmd);
                        pout.flush();
                    }
                } else if (tokens[0].equals("exit")) {
                    if (serverType == OurServerThread.ServerType.UDP) {
                        sPacket = new DatagramPacket(bytes, bytes.length, ia, udpPort);
                        datagramSocket.send(sPacket);
                        System.out.println("exit: Sending: " + Arrays.toString(bytes));
                    }
                    else
                    {
                        pout.println(cmd);
                        pout.flush();
                    }
                    break;
                } else {
                    System.out.println("ERROR: No such command");
                }
                String retString;
                // receive message from the server
                System.out.println("Waiting for response from server");
                if (serverType == OurServerThread.ServerType.UDP) {
                    datagramSocket.receive(rPacket);
                    System.out.println("Received UDP response from server");
                    retString = new String(rPacket.getData(), 0, rPacket.getLength());
                } else {
                    retString = din.nextLine();
                    server.close();
                    System.out.println("Received TCP response from server");
                }
                System.out.println("Response is: " + retString);
                fileWriter.write(retString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
