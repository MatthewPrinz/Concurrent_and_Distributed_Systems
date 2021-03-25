import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class OurServerThread extends Thread {
    public DatagramPacket rPacket;
    public OurLibrary ourLibrary;
    public DatagramSocket datagramSocket;
    public ServerSocket serverSocket;
    public Socket tcpSocket;
    public ServerType serverType;
    public PrintWriter pout;
    public Scanner din;
    public enum ServerType {
        TCP,
        UDP
    }

    public void sendUDP(String response) {
        byte[] responseBytes = response.getBytes();
        DatagramPacket sPacket = new DatagramPacket(responseBytes, responseBytes.length, rPacket.getAddress(),
                rPacket.getPort());
        try {
            datagramSocket.send(sPacket);
            //System.out.println("ServerThread:SendUDP: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTCP(String response)
    {
        try {
            pout = new PrintWriter(tcpSocket.getOutputStream());
            pout.println(response);
            pout.flush();
            //System.out.println("ServerThread:SendTCP: " + response);
            tcpSocket.close();
            din.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OurServerThread(ServerSocket serverSocket, DatagramSocket datagramSocket, DatagramPacket packet, OurLibrary ourLibrary) {
        this.datagramSocket = datagramSocket;
        this.rPacket = packet;
        this.ourLibrary = ourLibrary;
        this.serverSocket = serverSocket;
        this.serverType = ServerType.UDP;
    }

    public void run() {
        while (true) {
            String[] command;
            String rawCommand;
            if (ServerType.UDP == serverType) {
                rawCommand = new String(rPacket.getData());
                command = rawCommand.split(" ");
                //System.out.println("Over UDP, client: " + Arrays.toString(command));
            }
            else
            {
                try {
                    tcpSocket = serverSocket.accept();
                    din = new Scanner(tcpSocket.getInputStream());
                    pout = new PrintWriter(tcpSocket.getOutputStream());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                rawCommand = din.nextLine();
                command = rawCommand.split(" ");
                //System.out.println("Over TCP, client sent us: " + Arrays.toString(command));
            }
            String response;
            if (command[0].equals("setmode")) {
                //System.out.println("OurServerThread: in setmode");
                // Need to trim otherwise get a lot of characters with ASCII value 0
                // NOTE: IT MUST BE trim(). strip() DOES NOT WORK.
                if (command[1].trim().equals("T")) {
                    this.serverType = ServerType.TCP;
                    try {
                        //System.out.println("Trying to establish TCP Connection");
                        tcpSocket = serverSocket.accept();
                        //System.out.println("TCP Connection established");
                        din = new Scanner(tcpSocket.getInputStream());
                        pout = new PrintWriter(tcpSocket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    response = "The communication mode is set to TCP";
                    sendTCP(response);
                }
                // Need to trim otherwise get a lot of characters with ASCII value 0
                else if (command[1].trim().equals("U"))
                {
                    this.serverType = ServerType.UDP;
                    response = "The communication mode is set to UCP";
                    sendUDP(response);
                }
                else {
                    //System.out.println("In setmode, command1 != T and U, it is: "  Arrays.toString(command[1].toCharArray()));
                }
            } else if (command[0].equals("borrow")) {
                //System.out.println("OurServerThread: In borrow");
                String student = command[1];
                String title = rawCommand.substring(rawCommand.indexOf('"'), rawCommand.lastIndexOf('"')+1);
//                String book = command[2];
                response = ourLibrary.borrow(student, title);
                if (serverType == ServerType.UDP)
                    sendUDP(response);
                else
                    sendTCP(response);
            } else if (command[0].equals("return")) {
                //System.out.println("OurServerThread: In return");
                response = ourLibrary.returnBook(Integer.parseInt(command[1]));
                if (serverType == ServerType.UDP)
                    sendUDP(response);
                else
                    sendTCP(response);
            } else if (command[0].equals("inventory")) {
                //System.out.println("OurServerThread: In inventory");
                response = ourLibrary.inventory();
                if (serverType == ServerType.UDP)
                    sendUDP(response);
                else
                    sendTCP(response);
            } else if (command[0].equals("list")) {
                //System.out.println("OurServerThread: In list");
                response = ourLibrary.list(command[1]);
                if (serverType == ServerType.UDP)
                    sendUDP(response);
                else
                    sendTCP(response);
            } else if (command[0].equals("exit")) {
                //System.out.println("OurServerThread: In exit");
                ourLibrary.exit();
                break;
            } else {
                //System.out.println("ERROR: No such command");
            }
            //System.out.println("Waiting for response from client");
            if (serverType == OurServerThread.ServerType.UDP) {
                try {
                    datagramSocket.receive(rPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println("Received UDP response from client");
            } else {
                //System.out.println("Received TCP response from client");
            }
        }
    }
}
