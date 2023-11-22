package io.sim;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.json.JSONObject;

/**
 * Classe utilitária para criptografia e descriptografia de dados usando o algoritmo AES.
 * Esta classe utiliza o modo de operação CBC (Cipher Block Chaining) com padding PKCS5.
 * As chaves e IVs são fornecidos como constantes estáticas para fins de teste.
 */
public class CryptoUtils implements Serializable {

    private static final String KEY = "minhakeydecriptografia";

    private static SecretKey getKey() {
        return new SecretKeySpec(KEY.getBytes(), "AES");
    }

    public static String encrypt(JSONObject dado) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] iv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(dado.toString().getBytes());
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static JSONObject decrypt(String textoCriptografado) throws Exception {
        byte[] combined = Base64.getDecoder().decode(textoCriptografado);
        byte[] iv = new byte[16];
        if (combined.length >= 16) {
            byte[] dadoCriptografado = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, dadoCriptografado, 0, combined.length - 16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new IvParameterSpec(iv));
            byte[] decryptedData = cipher.doFinal(dadoCriptografado);
            String decryptedJSONString = new String(decryptedData);
            return new JSONObject(decryptedJSONString);
        } else {
            throw new IllegalArgumentException("Dado Perdido na Criptografia");
        }
    }
}