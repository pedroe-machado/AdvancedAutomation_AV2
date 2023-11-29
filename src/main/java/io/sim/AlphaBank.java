package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * - Classe (Thread) que implementa o AlphaBank -
 * 
 * Seu funcionamento é basicamente de um servidor que controla um HashMap 
 * com todas as contas bancárias. Quando um BotPayment se conecta ao banco, é 
 * lançada uma thread responsável por lidar com aquela transferência.
 * 
 * Os BotPayments devem enviar um JSONObject criptografado na porta 20180 com os campos:
 * "idConta"{"String: id do remetente"}
 * "senha"{"String: senha do remetente - default senha=idConta"}
 * "idBeneficiario"{String: id de quem irá receber a transferência}
 * ¹"valor"{"Double: valor a ser transferido"}
 * ¹ Drivers que transferirão todo seu dinheiro não adicionam chave "valor" 
*/
public class AlphaBank extends Service{
    private Transferencia fuelStationConection;
    private HashMap<String, Account> accounts;
    
    public AlphaBank(ArrayList<String> users) throws UnknownHostException, IOException {
        super(20180);
        accounts = new HashMap<>();
        for (String id : users) {
            accounts.put(id, new Account(id, 0));
        }
        System.out.println("{BANK:35} AlphaBank inaugurado -"+ System.currentTimeMillis());
    }
    @Override
    public Server CreateServerThread(Socket conn){
        return new Transferencia(conn);
    }
    public synchronized Account getAccount(String idConta, String senha) {
        Account conta = accounts.get(idConta);
        if (conta != null && conta.autenticar(senha)) {
            return conta;
        }
        return null;
    }
    public synchronized void transferePara(String idConta, double valor) {
        accounts.get(idConta).recebe(valor);
    }

    private class Transferencia extends Server{

        private String idConta, senha;
        private String idBeneficiario;
        private double valor;

        public Transferencia(Socket conn) {
            super(conn);
            System.out.println("{BANK:60} Transferencia iniciada -"+ System.currentTimeMillis());
        }

        @Override
        protected void ProcessMessage(String messageReceived) {  
            try {
                JSONObject jsonObject;
                if (useEncryption) {
                    jsonObject = CryptoUtils.decrypt(messageReceived);
                } else {
                    jsonObject = new JSONObject(messageReceived);
                }
                this.idConta = jsonObject.getString("idConta");
                this.senha = jsonObject.getString("senha");
                this.idBeneficiario = jsonObject.getString("idBeneficiario");

                if(jsonObject.getString("idConta").equals("fuelStation")){
                    fuelStationConection = this;
                }                 
                try {
                    this.valor = jsonObject.getDouble("valor");
                } catch (JSONException | NullPointerException e) {
                    this.valor = getAccount(idConta, senha).getSaldo();
                    if(fuelStationConection!=null){
                        fuelStationConection.SendMessage(jsonObject);
                    }
                }
            } catch (Exception e) {
                System.out.println("{BANK:88} ErroProcessMessage -"+ System.currentTimeMillis());
                this.valor = 0;
            } finally {
                getAccount(idConta, senha).debita(valor);
                transferePara(idBeneficiario, valor);
                System.out.println("{BANK:93} Transferencia de " + idConta + " para " + idBeneficiario + " no valor de " + valor + "- saldo: " + getAccount(idBeneficiario, idBeneficiario).getSaldo() + " -"+ System.currentTimeMillis());
            }
        }
    }
    
    @SuppressWarnings("unused")
    private class Account{
        private String idConta;
        private String senha;
        private double saldo;
        private boolean autenticado;
        
        public Account(String idConta, double saldo){
            this.idConta = this.senha = idConta;
            this.saldo = saldo;
        }
        public synchronized double getSaldo(){
            return saldo;
        }
        public synchronized void recebe(double valor){
            saldo += valor;
        }
        public synchronized void debita(double valor){
            if(autenticado) saldo -= valor;
            else System.out.println("{BANK:117} Erro de autenticacao -"+ System.currentTimeMillis());
        }
        public synchronized boolean autenticar(String _senha){
            if( _senha.equalsIgnoreCase(senha)){
                autenticado = true;
                return true;
            }
            return false;
        }
    }
}
