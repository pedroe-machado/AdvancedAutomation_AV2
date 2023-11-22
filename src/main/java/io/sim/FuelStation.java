package io.sim;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;

public class FuelStation extends Thread {

    private Client client;
    private static FuelStation singleStation;
    private static Semaphore semaphore;
    private static HashMap<String,Double> flowControl;

    private FuelStation(int pumps){
        client = new Client("127.0.0.1", 20180);
        semaphore = new Semaphore(pumps);
        flowControl = new HashMap<>();
        System.out.println("FuelStation inaugurada");
    }
    public static FuelStation getInstance(int pumps){
        if(singleStation == null){
            singleStation = new FuelStation(pumps);
        }
        return singleStation;
    }

    @Override
    public void run(){
        while(true){
            try {
                JSONObject jsonObject = client.Listen();

                flowControl.put(jsonObject.getString("idConta"),jsonObject.getDouble("valor"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    synchronized double getValorCar(Car car){
        int attempts = 3;
        while(attempts>0){
            try {
                double pagamento = flowControl.get(car.getIdAuto());
                return pagamento;
            } catch (Exception e) {
                attempts--;
            }
        }
        System.out.println("erro ao abastecer - driver não pagou");
        return 0; 
    }
    public class FuelPumpThread extends Thread {

        private Car car;

        public FuelPumpThread(Car car){
            this.car = car;
        }

        @Override
        public void run() {
            try {
                float litros = (float) (getValorCar(car)/5.87);

                System.out.println("Carro chegou ao posto de gasolina.");
                semaphore.acquire();

                double lastSpeed = car.abastece(litros);

                System.out.println("Carro está abastecendo...");
                Thread.sleep(120000);

                car.terminaAbastecimento(lastSpeed);

                System.out.println("Carro terminou de abastecer.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }  
   
}
