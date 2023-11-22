// package io.sim;

// import java.io.*;
// import java.net.*;
// import org.json.simple.*;
// import org.json.simple.parser.*;

// import net.CryptoUtils;

// public class Servidor extends Thread {

//     private Socket clientSocket;
//     private byte[] key;  // Chave de criptografia
//     private byte[] iv;   // Vetor de inicialização

//     public Servidor(Socket socket, byte[] key, byte[] iv) {
//         this.clientSocket = socket;
//         this.key = key;
//         this.iv = iv;
//     }

//     @Override
//     public void run() {
//         try {
//             InputStream input = clientSocket.getInputStream();
//             byte[] encryptedData = input.readAllBytes();

//             // Descriptografa os dados
//             byte[] decryptedData = CryptoUtils.decrypt(key, iv, encryptedData);

//             JSONParser parser = new JSONParser();
//             Object obj = parser.parse(new String(decryptedData));
//             JSONObject jsonObject = (JSONObject) obj;

//             // Processa o arquivo JSON aqui
//             System.out.println("Recebido do cliente: " + jsonObject.toJSONString());

//             input.close();
//             clientSocket.close();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

// }
