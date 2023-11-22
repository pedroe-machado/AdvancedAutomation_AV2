package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONObject;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class Car extends Thread{
    private final Object monitor = new Object();
    private boolean refresh=false;

    private boolean needFuel;
    private boolean abastecendo;
    private boolean theresNewRoute;
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
        try {
            new AskRoute(); //solicita rota e altera currentRoute
        } catch (UnknownHostException e) {System.out.println("askroute error");}
        catch (IOException e) {e.printStackTrace();}

        auto.start();
        while(true){
            try {
                auto.esperaSensores();
                notificaRefresh();
                double fuelConsumption = (Double) sumo.do_job_get(Vehicle.getFuelConsumption(idAuto));
                this.fuelTank -= (((float)fuelConsumption)*((float)acquisitionRate/1000))/(750000); // conversão mg/s -> L
                new SendInfo();
                
                if(fuelTank<=3 && !abastecendo){
                    System.out.println(idAuto+" precisa abastecer");
                    needFuel = true;
                    sumo.do_job_set(Vehicle.setSpeed(idAuto, 0.0));
                }
                if(onFinalSpace()){
                    new AskRoute(currentRoute);
                    auto.setOn_off(false);
                }
            } catch (UnknownHostException e) {
                System.out.println("sendinfo error");
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("car wait error");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void notificaRefresh(){
        synchronized(monitor){
            refresh = true;
            monitor.notify();
        }
    }

    public boolean onFinalSpace() throws Exception{
        if(currentRoute==null) return false;
        SumoStringList edges = currentRoute.getEdges();
        return sumo.do_job_get(Vehicle.getRoadID(idAuto)).equals(edges.get(edges.size()-2));        
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
            this.jsonFlag.put("finishedRoute",finished);
            this.start();        
        }
        @Override
        public void run() {
            try {
                clientComp.SendMessage(jsonFlag);

                Thread.sleep(100);
                
                JSONObject jsonPackage = clientComp.Listen();

                this.newRoute = new Route(jsonPackage.getString("idRota"),(SumoStringList) jsonPackage.get("edges"));
                System.out.println(jsonPackage.getString("idRota")+jsonPackage.get("edges"));

                currentRoute = newRoute;
                theresNewRoute = true;
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
                    System.out.println("car sending: " + jsonPack.toString());                
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
        sumo.do_job_set(Vehicle.setSpeed(idAuto, speed));
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

    public io.sim.Route getCurrenRoute(){return currentRoute;}

    public String getIdAuto(){return idAuto;}

    public int getPersonCapacity() {return auto.getPersonCapacity();}

    public int getPersonNumber() {return auto.getPersonNumber();}

    public SumoColor getColorAuto() {return auto.getColorAuto();}
    
    public boolean theresNewRoute(){return theresNewRoute;}

    public void ackNewRoute(){
        theresNewRoute = false;
        System.out.println("ack newRoute " + idAuto);
    }

    public synchronized void sincronizaWaitCar() throws InterruptedException{
		synchronized (monitor){
			while(!refresh){
				monitor.wait();
			}
			refresh = false;
		}
    }
}
