package io.sim;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class CryptoUtilsTest {
    
    @Test
    public void cryptoWorks_ReturnsTrue_WhenJsonRecovered() {
        String operacao = "1+1";
        int resultado = 2;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(operacao, resultado);

        try {
            String stringTeste = CryptoUtils.encrypt(jsonObject);

            JSONObject jsonPackage = CryptoUtils.decrypt(stringTeste);

            int resultadoObtido = jsonPackage.getInt(operacao);

            assertEquals(resultado, resultadoObtido);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
