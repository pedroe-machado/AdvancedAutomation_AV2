package io.sim;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public abstract class Service extends Thread{
    protected ArrayList<Server> connections;
    protected ServerSocket socket;
    protected int port;

    public Service(int port) {
        this.port = port;
        connections = new ArrayList<Server>();
    }

    public abstract Server CreateServerThread(Socket conn);

    public void run() {
        try {
            socket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (socket != null) {
            while (true) {
                try {
                    Socket con = socket.accept();
                    Server server = CreateServerThread(con);
                    server.start();
                    connections.add(server);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
