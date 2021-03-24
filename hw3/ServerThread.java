import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    DatagramPacket rPacket;
    Library library;
    DatagramSocket datagramSocket;
    ServerSocket serverSocket;
    Socket tcpSocket;
    ServerType serverType;

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
            PrintWriter pout = new PrintWriter(tcpSocket.getOutputStream());
            pout.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerThread(ServerSocket serverSocket, DatagramSocket datagramSocket, DatagramPacket packet, Library library) {
        this.datagramSocket = datagramSocket;
        this.rPacket = packet;
        this.library = library;
        this.serverSocket = serverSocket;
        this.serverType = ServerType.UDP;
    }

    public void run() {
        byte[] buf = new byte[1024];

        String rawCommand = new String(buf);
        String[] command = rawCommand.split(" ");
        String response;
        if (command[0].equals("setmode")) {
            if (command[1].equals("T")) {
                this.serverType = ServerType.TCP;
                Socket s;
                try {
                    // while?
                    if ((s = serverSocket.accept()) != null) {
                        tcpSocket = s;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (command[0].equals("borrow")) {
            String student = command[1];
            String book = command[2];
            int code = library.borrow(student, book);
            if (code == -1) {
                response = "Request Failed - We do not have this book";
            } else if (code == -2) {
                response = "Request Failed - Book not available";
            } else {
                response = "Your request has been approved, " + code + " " + student + " " + book;
            }
            if (serverType == ServerType.UDP)
                sendUDP(response);
            else
                sendTCP(response);
        } else if (command[0].equals("return")) {
            response = library.returnBook(Integer.parseInt(command[1]));
            if (serverType == ServerType.UDP)
                sendUDP(response);
            else
                sendTCP(response);
        } else if (command[0].equals("inventory")) {
            response = library.inventory();
            if (serverType == ServerType.UDP)
                sendUDP(response);
            else
                sendTCP(response);
        } else if (command[0].equals("list")) {
            response = library.list(command[1]);
            if (serverType == ServerType.UDP)
                sendUDP(response);
            else
                sendTCP(response);
        } else if (command[0].equals("exit")) {
            library.exit();
            // System.exit?
        } else {
            System.out.println("ERROR: No such command");
        }

    }
}
