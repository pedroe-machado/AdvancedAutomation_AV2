package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.json.JSONObject;

public class Client {
    private Socket socket;
    private OutputStream writer;    
    private InputStream reader;
    public boolean useEncryption;

    Client(String ip, int port, boolean useEncryption) {
        this.useEncryption = useEncryption;
        try {
            socket = new Socket(ip, port);
            writer = socket.getOutputStream();            
            reader = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    Client(String ip, int port) {
        this.useEncryption = false;
        try {
            socket = new Socket(ip, port);
            writer = socket.getOutputStream();            
            reader = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
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
            //System.out.println("{CLIENT:44} ClientSentMessage: " + message + " - " + System.currentTimeMillis());
            if(messageBytes!=null){
                writer.write(messageBytes);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject Listen() throws Exception {
        JSONObject jsonObject = null;
        System.out.println("{CLIENT:55} Listening - " + System.currentTimeMillis());
        byte[] buffer = new byte[1024];
        int bytesRead = reader.read(buffer);
        if (bytesRead > 0) {
            byte[] messageBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, messageBytes, 0, bytesRead);
            String message = new String(messageBytes);
            if (useEncryption) {
                jsonObject = CryptoUtils.decrypt(message);
            } else {
                jsonObject = new JSONObject(message);
            }
            System.out.println("{CLIENT:67} Receivedmessage: " + message + " - " + System.currentTimeMillis());
        }
        return jsonObject;
    }

    public void Close() throws IOException {
        socket.close();
    }
}