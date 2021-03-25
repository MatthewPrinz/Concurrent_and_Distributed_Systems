import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
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
        // This is the only way for our switch statement to work
        exit:
        while (true) {
            String[] command;
            String rawCommand = null;
            if (ServerType.UDP == serverType) {
                rawCommand = new String(rPacket.getData(), 0, rPacket.getLength());
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
                if (din.hasNextLine())
                    rawCommand = din.nextLine();
            }
            if (rawCommand == null)
                break;
            command = rawCommand.split(" ");
            String response;
            switch (command[0]) {
                case "setmode":
                    // Need to trim otherwise get a lot of characters with ASCII value 0
                    // NOTE: IT MUST BE trim(). strip() DOES NOT WORK.
                    if (command[1].trim().equals("T")) {
                        this.serverType = ServerType.TCP;
                        try {
                            tcpSocket = serverSocket.accept();
                            din = new Scanner(tcpSocket.getInputStream());
                            pout = new PrintWriter(tcpSocket.getOutputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        response = "The communication mode is set to TCP";
                        sendTCP(response);
                    }
                    // Need to trim otherwise get a lot of characters with ASCII value 0
                    else if (command[1].trim().equals("U")) {
                        this.serverType = ServerType.UDP;
                        response = "The communication mode is set to UCP";
                        sendUDP(response);
                    }
                    break;
                case "borrow":
                    String student = command[1];
                    String title = rawCommand.substring(rawCommand.indexOf('"'), rawCommand.lastIndexOf('"') + 1);
                    response = ourLibrary.borrow(student, title);
                    if (serverType == ServerType.UDP)
                        sendUDP(response);
                    else
                        sendTCP(response);
                    break;
                case "return":
                    response = ourLibrary.returnBook(Integer.parseInt(command[1]));
                    if (serverType == ServerType.UDP)
                        sendUDP(response);
                    else
                        sendTCP(response);
                    break;
                case "inventory":
                    response = ourLibrary.inventory();
                    if (serverType == ServerType.UDP)
                        sendUDP(response);
                    else
                        sendTCP(response);
                    break;
                case "list":
                    response = ourLibrary.list(command[1]);
                    if (serverType == ServerType.UDP)
                        sendUDP(response);
                    else
                        sendTCP(response);
                    break;
                case "exit":
                    ourLibrary.exit();
                    break exit;
            }
            if (serverType == ServerType.UDP) {
                try {
                    datagramSocket.receive(rPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
