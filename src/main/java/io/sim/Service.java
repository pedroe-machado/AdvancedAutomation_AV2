package io.sim;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public abstract class Service extends Thread{
    protected ArrayList<Server> connections;
    protected ServerSocket socket;
    protected int port;
    private boolean printed = false;

    public Service(int port) {
        this.port = port;
        connections = new ArrayList<Server>();
        Out.writeLine("a_i Service: "+ System.nanoTime());
    }

    public abstract Server CreateServerThread(Socket conn);

    public void run() {
        if(port==20181) Out.writeLine("s_t Service: "+ System.nanoTime());
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
                if(port==20181 && !printed) {
                    Out.writeLine("c_t Service: "+ System.nanoTime());
                    printed = true;
                }
            }
        }
    }
}
