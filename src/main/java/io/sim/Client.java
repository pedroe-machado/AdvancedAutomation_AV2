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

    Client(String ip, int port) {
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
            byte[] jsonBytes = CryptoUtils.encrypt(jsonObject).getBytes();
            System.out.println("sentmessage: " + jsonObject.toString());
            if(jsonBytes!=null){
                writer.write(jsonBytes);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject Listen() throws Exception {        
        byte[] message = reader.readAllBytes();        
        JSONObject jsonObject = new JSONObject(CryptoUtils.decrypt(new String(message)));
        System.out.println("receivedmessage: " + jsonObject.toString());
        return jsonObject;
    }
    public void Close() throws IOException {
        socket.close();
    }
}