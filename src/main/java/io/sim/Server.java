package io.sim;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONObject;

public abstract class Server extends Thread {
    private InputStream reader;
    private OutputStream writer;
    public boolean useEncryption;

    public Server(Socket conn, boolean useEncryption) {
        this.useEncryption = useEncryption;
        try {
            reader = conn.getInputStream();
            writer = conn.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Server(Socket conn) {
        this.useEncryption = false;
        try {
            reader = conn.getInputStream();
            writer = conn.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long expireTime = System.currentTimeMillis()+4000;
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
                if (System.currentTimeMillis() > expireTime) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void SendMessage(JSONObject jsonObject) {
        try {
            String message = jsonObject.toString();
            byte[] messageBytes;
            if (useEncryption) {
                messageBytes = CryptoUtils.encrypt(jsonObject).getBytes();
            } else {
                messageBytes = message.getBytes();
            }
            System.out.println("{SERVER:64/"+(Thread.currentThread().getName())+"} ServerSentmessage: " + message + " at " + System.currentTimeMillis());
            if(messageBytes!=null){
                writer.write(messageBytes);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract protected void ProcessMessage(String message);
}
