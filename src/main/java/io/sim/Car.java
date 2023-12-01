package io.sim;

import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONObject;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class Car extends Thread{
    private final Object resetLock = new Object();
    private final Object monitorDriver = new Object();
    private boolean refresh=false;

    private boolean needFuel;
    private boolean abastecendo;
    private boolean theresNewRoute;
    private boolean needNewRoute;
    private float fuelTank;
    private Route currentRoute;
    private SumoTraciConnection sumo;
	private Auto auto;
    private String idAuto;
    private long acquisitionRate;
	
	public Car( boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection sumo, long _acquisitionRate, int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) throws Exception {    
        this.auto = new Auto(_on_off, _idAuto, _colorAuto, _driverID, sumo, _acquisitionRate, _fuelType,_fuelPreferential, _fuelPrice, _personCapacity, _personNumber);
        this.fuelTank = 10;
        this.sumo = sumo;
        this.idAuto = _idAuto;
        this.acquisitionRate = _acquisitionRate;
        this.abastecendo = false;
	}
    
    @Override
    public void run() {
        System.out.println("{CAR:40} Carro iniciou em "+ System.currentTimeMillis());
        try {
            new AskRoute(); //solicita rota e altera currentRoute
            Thread.sleep(1000);
        } catch (UnknownHostException e) {
            System.out.println("{CAR:45} Askroute error");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        auto.start();
        while(true){
            try {
                auto.esperaSensores();
                //notificaRefresh();
                double fuelConsumption = (Double) sumo.do_job_get(Vehicle.getFuelConsumption(idAuto));
                this.fuelTank -= (((float)fuelConsumption)*((float)acquisitionRate/1000))/(750000); // conversão mg/s -> L
                //System.out.println( idAuto + " tanque: " + fuelTank);
                if(auto.getLastDistance()<40.0 && !needNewRoute) new SendInfo().setPriority(7); //o carro não deve passar de 40m/s
                
                if(fuelTank<=3 && !abastecendo){
                    System.out.println(idAuto+" precisa abastecer");
                    needFuel = true;
                    sumo.do_job_set(Vehicle.setSpeed(idAuto, 0.0));
                }
                if (needNewRoute && !theresNewRoute) {
                    sumo.do_job_set(Vehicle.remove(idAuto, (byte)0));
                    new AskRoute(currentRoute);
                    synchronized (resetLock) {
                        if (!theresNewRoute) {
                            System.out.println("{CAR:72} Esperando nova rota - "+ System.currentTimeMillis());
                            try {
                                resetLock.wait();
                                System.out.println("{CAR:75} Nova rota recebida - "+ System.currentTimeMillis());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            } catch (UnknownHostException e) {
                System.out.println("Car82 - sendinfo error");
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Car85 - car wait error");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void notificaRefresh(){
        synchronized(monitorDriver){
            refresh = true;
            monitorDriver.notify();
        }
    }

    public boolean askNewRoute() throws Exception{
        needNewRoute = true;
        return true;
    }

    public boolean onFinalSpace() throws Exception{
        if(currentRoute==null) return false;
        SumoStringList edges = currentRoute.getEdges();
        //System.out.println("verificando rua ");
        return sumo.do_job_get(Vehicle.getRoadID(idAuto)).equals(edges.get(edges.size()-1));        
    }

    // Comunicação com a Company e RouteHandler

    private class AskRoute extends Thread {
        private Client clientComp;
        private JSONObject jsonFlag;
        private Route newRoute;

        public AskRoute() throws UnknownHostException, IOException {
            this.clientComp = new Client("127.0.0.1", 20181);
            this.jsonFlag = new JSONObject();
            this.jsonFlag.put("servico", "REQUEST_ROUTE");
            this.start();
        }
        public AskRoute(Route finished) throws UnknownHostException, IOException{
            this.clientComp = new Client("127.0.0.1", 20181);
            this.jsonFlag = new JSONObject();
            this.jsonFlag.put("servico", "REQUEST_ROUTE");
            this.jsonFlag.put("finishedRoute", finished.getId());
            this.jsonFlag.put("edges", finished.getEdges());
            this.start();        
        }
        @Override
        public void run() {
            Thread.currentThread().setName("AskRoute");
            try {
                clientComp.SendMessage(jsonFlag);
                
                JSONObject jsonPackage = clientComp.Listen();

                JSONArray jsonArray = jsonPackage.getJSONArray("edges");
                SumoStringList sumoStringList = new SumoStringList();
                for (int i = 0; i < jsonArray.length(); i++) {
                    sumoStringList.add(jsonArray.getString(i));
                }
                this.newRoute = new Route(jsonPackage.getString("idRota"),sumoStringList);
                //System.out.println(jsonPackage.getString("idRota") + " rota recebida");

                currentRoute = newRoute;
                theresNewRoute = true;
                System.out.println("{CAR:146} There is a new Route: " + theresNewRoute + " -"+ System.currentTimeMillis());
            } catch (Exception e) {
                System.out.println("aksroute com problema");
                e.printStackTrace();
            }
        }
    }
    private class SendInfo extends Thread {
        private Client infoClient;
        private JSONObject jsonPack;

        public SendInfo() throws IOException {
            try {
                this.infoClient = new Client("127.0.0.1", 20181);
                this.jsonPack = new JSONObject();
                this.jsonPack.put("servico", "SEND_INFO");
                this.jsonPack.put("idAuto", idAuto);
                this.jsonPack.put("km", Double.toString(auto.getLastDistance()));
                this.jsonPack.put("report", auto.getLastRepport());
                this.start();
            } catch (Exception e) {
                System.out.println("error company server-sendinfo");
            }
        }
        @Override
        public void run() {
            if(auto.isOn_off()){
                try {
                    //System.out.println("car sending: " + jsonPack.toString());                
                    infoClient.SendMessage(jsonPack);
                } catch (Exception e){
                    System.out.println("sendinfo timing error");
                }
            }
        }
    }

    // Métodos de abastecimento - somente FuelStation acessa
    public double abastece(float litros) throws Exception{
        double lastSpeed = (double) sumo.do_job_get(Vehicle.getSpeed(idAuto));
        sumo.do_job_set(Vehicle.setSpeed(idAuto, 0.0));
        fuelTank += litros;
        return lastSpeed;
    }
    public void terminaAbastecimento(double speed) throws Exception{
        setNeedFuel(false);
        sumo.do_job_set(Vehicle.setSpeedMode(idAuto, 31));
    }

    // Getters and Setters
    public boolean doesNeedFuel() {return needFuel;}
    
    public void setNeedFuel(boolean _needFuel){needFuel = _needFuel;}

    public boolean abastecendo(){return abastecendo;}

    public void abastecendo(boolean _abastecendo){
        abastecendo = _abastecendo;
        System.out.println("abastecendo car"+idAuto + " recebe "+_abastecendo);
    }
    
    public long getAcquisitionRate(){return acquisitionRate;}

    public io.sim.Route getCurrenRoute(){
        //System.out.println("{CAR:217} CurrentRoute: " + currentRoute.getId() + " -"+ System.currentTimeMillis());
        return currentRoute;
    }

    public String getIdAuto(){return idAuto;}

    public int getPersonCapacity() {return auto.getPersonCapacity();}

    public int getPersonNumber() {return auto.getPersonNumber();}

    public SumoColor getColorAuto() {return auto.getColorAuto();}
    
    public boolean theresNewRoute(){return theresNewRoute;}

    public void setAutoOnOff(boolean _on_off){
        if(auto.isOn_off()){
            try {
                sumo.do_job_set(Vehicle.setSpeed(idAuto, 0.0));
                auto.setOn_off(_on_off);
            } catch (Exception e) {
                System.out.println("setAutoOnOff error");
            }
        } else {
            auto.setOn_off(_on_off);
            auto.start();
        }
    }

    public void ackNewRoute(){
        this.theresNewRoute = false;
        this.needNewRoute = false;
        synchronized(resetLock){
            resetLock.notify();
        }
        System.out.println("{CAR:240} Ack newRoute " + idAuto + " -"+ System.currentTimeMillis());
    }

    public synchronized void sincronizaWaitCar() throws InterruptedException{
		synchronized (monitorDriver){
			while(!refresh){
				monitorDriver.wait();
			}
			refresh = false;
		}
    }

    public boolean onSumo() {
        return auto.onSumo();
    }
}
