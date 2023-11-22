// package io.sim;

// import java.io.*;
// import java.net.*;
// import org.json.simple.*;

// public class Cliente extends Thread {

//     private Socket socket;
//     private byte[] key;  // Chave de criptografia
//     private byte[] iv;   // Vetor de inicialização
//     private JSONObject jsonObject;  //informação transmitida

//     public Cliente(Socket socket, byte[] key, byte[] iv, JSONObject jsonObject) {
//         this.socket = socket;
//         this.key = key;
//         this.iv = iv;
//         this.jsonObject = jsonObject;
//     }

// 	@Override
//     public void run() {
//         try {
//             OutputStream output = socket.getOutputStream();

//             // Converte o JSON em bytes
//             byte[] jsonBytes = jsonObject.toJSONString().getBytes("UTF-8");

//             // Criptografa os dados
//             byte[] encryptedData = CryptoUtils.encrypt(key, iv, jsonBytes);

//             // Envia os dados criptografados para o servidor
//             output.write(encryptedData);

//             output.close();
//             socket.close();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

// }
