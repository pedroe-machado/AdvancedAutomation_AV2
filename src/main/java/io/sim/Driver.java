package io.sim;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONObject;

import de.tudresden.sumo.subscription.SubscriptionObject;
import de.tudresden.sumo.util.Observable;
import it.polito.appeal.traci.SumoTraciConnection;
import sim.traci4j.src.java.it.polito.appeal.traci.Vehicle;
import sim.traci4j.src.java.it.polito.appeal.traci.VehicleLifecycleObserver;
@SuppressWarnings("unused")
public class Driver extends Thread {
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
        Thread.currentThread().setName("Driver "+idDriver);
        System.out.println("{DRIVER:37} Driver iniciou em "+ System.currentTimeMillis());
        while (carro.isAlive()) {
            //System.out.println("carro vivo");
            if(carro.theresNewRoute()){
                try {
                    done.add(Integer.parseInt(currentRoute.getId()), currentRoute); //adiciona rota finalizada
                    currentService.setOn(false);
                    currentService.join();
                } catch (Exception e) {
                    System.out.println("{DRIVER:46} Nenhuma rota finalizada");
                }
                currentRoute = carro.getCurrenRoute();
                
                currentService = new TransportService(true, idConta, carro, sumo);
                currentService.start();
                carro.ackNewRoute();
                //Excel.doLine();
                System.out.println("{DRIVER:54} "+ idDriver + " Iniciou nova rota: "+ currentRoute.getId() + " em " + System.currentTimeMillis());
            }
            // while (!carro.doesNeedFuel() && !carro.theresNewRoute()) {
            //     try {
            //         System.out.println("esperando sincronia");
            //         carro.sincronizaWaitCar();
            //     } catch (Exception e) {
            //         System.out.println("driver sleep error");
            //     }
            // }
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
        System.out.println("{DRIVER:77} - carro morreu em " + System.currentTimeMillis());
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
                System.out.println("{DRIVER:BOTPAY:97} Falha no pagamento da fuelStation -"+ System.currentTimeMillis());
                e.printStackTrace();
            }
        }
    }

    public String getIdDriver() {
        return this.idDriver;
    }

}


