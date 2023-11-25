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
            System.out.println("sentmessage: " + message);
            if(messageBytes!=null){
                writer.write(messageBytes);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject Listen() throws Exception {
        JSONObject jsonObject; 

        byte[] message = reader.readAllBytes();        
        if (useEncryption) {
            jsonObject = CryptoUtils.decrypt(new String(message));
        } else {
            jsonObject = new JSONObject(new String(message));
        }

        System.out.println("receivedmessage: " + jsonObject.toString());
        return jsonObject;
    }
    public void Close() throws IOException {
        socket.close();
    }
}