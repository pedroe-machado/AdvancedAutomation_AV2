package io.sim;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONObject;

public abstract class Server extends Thread {
    private Socket conn;
    private InputStream reader;
    private OutputStream writer;

    public Server(Socket conn) {
        this.conn = conn;
        try {
            reader = conn.getInputStream();
            writer = conn.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String message = "";
        while (!message.equals("STOP") && message != null) {
            try {
                if (reader.available() > 0) {
                    byte[] buffer = new byte[2048];
                    int read = reader.read(buffer);
                    if (read > 0) {
                        message = new String(buffer, 0, read);
                        ProcessMessage(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void SendMessage(JSONObject jsonObject) {
        try {
            String encryptedData = CryptoUtils.encrypt(jsonObject); 
            writer.write(encryptedData.getBytes());
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract protected void ProcessMessage(String message);
}
