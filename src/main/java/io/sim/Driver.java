package io.sim;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONObject;

import it.polito.appeal.traci.SumoTraciConnection;

public class Driver extends Thread{  
    private String idDriver;
    private String idConta;
    private String senha;
    private Car carro;
    private SumoTraciConnection sumo;
    private TransportService currentService;
    private Route currentRoute;
    private ArrayList<Route> toDo;
    private ArrayList<Route> done;

    public Driver(SumoTraciConnection sumo, String id, Car _car){
        this.sumo = sumo;
        this.toDo = new ArrayList<>();
        this.done = new ArrayList<>(); 
        this.carro = _car;
        this.idDriver = this.idConta = this.senha = id;
    }

    @Override
    public void run() {
        while (carro.isAlive()) {
            if(carro.theresNewRoute()){
                try {
                    done.add(Integer.parseInt(currentRoute.getId()), currentRoute); //adiciona rota finalizada
                    currentService.setOn(false);
                } catch (Exception e) {System.out.println("nehuma rota finalizada");}
                currentRoute = carro.getCurrenRoute();
                carro.ackNewRoute();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentService = new TransportService(true, idConta, carro, sumo);
                currentService.start();
                System.out.println(idDriver + " iniciou nova rota: "+ currentRoute.getId());
            }
            while (!carro.doesNeedFuel() && !carro.theresNewRoute()) {
                try {
                    carro.sincronizaWaitCar();
                } catch (Exception e) {
                    System.out.println("driver sleep error");
                }
            }
            try {
                if(carro.doesNeedFuel() && !carro.abastecendo()){
                    carro.abastecendo(true);
                    FuelStation.getInstance(2).new FuelPumpThread(carro);
                    new BotPayment(idDriver);
                }
            } catch (UnknownHostException e) {
                System.out.println("driver-bank connection");
            } catch (IOException e) {
                System.out.println("driver payment error");
                e.printStackTrace();
            }
        }
    }
    
    private class BotPayment extends Thread {
        private Client client;
        private JSONObject jsonObject;

        public BotPayment(String idDriver) throws UnknownHostException, IOException{
            this.client = new Client("127.0.0.1", 20180);
            this.jsonObject = new JSONObject();
            this.jsonObject.put("idConta", idConta);
            this.jsonObject.put("senha", senha);
            this.jsonObject.put("idBeneficiario", "FuelStation");
            this.start();
        }
        @Override
        public void run() {
            try {
                client.SendMessage(jsonObject);
            } catch (Exception e) {
                System.out.println("falha no pagamento da fuelStation");
                e.printStackTrace();
            }
        }
    }

    public String getIdDriver() {
        return this.idDriver;
    }
}


