package io.sim;

import de.tudresden.sumo.cmd.Vehicle;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;

public class Auto extends Thread {
	private final Object monitor = new Object();
	private boolean sensoresAtualizados=false;
	private String idAuto;
	private SumoColor colorAuto;
	private String driverID;
	private SumoTraciConnection sumo;

	private boolean on_off;
	private long acquisitionRate;
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; 	// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; 		// price in liters
	private int personCapacity;		// the total number of persons that can ride in this vehicle
	private int personNumber;		// the total number of persons which are riding in this vehicle

	private ArrayList<DrivingData> drivingRepport;
	private CalculaKm km;
	private double infoDistanceCompany;
	private DrivingData _repport;
	//private RealTimeChart graph;

	public Auto(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) throws Exception {
		this.sumo = _sumo;
		this.on_off = _on_off;
		this.idAuto = _idAuto;
		this.driverID = _driverID;
		this.colorAuto = _colorAuto;
		this.fuelPrice = _fuelPrice;
		this.personNumber = _personNumber;
		this.personCapacity = _personCapacity;
		this.acquisitionRate = _acquisitionRate;
		this.fuelType = ((_fuelType < 0) || (_fuelType > 4)) ? 4 : _fuelType; 					
		this.fuelPreferential = ((_fuelPreferential < 0) || (_fuelPreferential > 4)) ? 4 : _fuelPreferential;

		this.drivingRepport = new ArrayList<DrivingData>();
		this.km = new CalculaKm();
		//this.graph = new RealTimeChart("Real Time CO2 Emission [mg/s]");
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("erro largada auto");
		}
		while (this.on_off) {
			try {				
				this.atualizaSensores();
				Thread.sleep(this.acquisitionRate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void atualizaSensores() throws InterruptedException {
		int attempt = 5;
		synchronized(monitor){
			while(attempt>0){
				try {
					if (!this.getSumo().isClosed() && isOn()) {
						
						SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
						infoDistanceCompany = km.calcular(sumoPosition2D.toString()); //EXIGÊNCIA CALCULO LONG/LAT
						
						_repport = new DrivingData(this.idAuto, this.driverID, 
								System.nanoTime(), //Timestamp 
								sumoPosition2D.x, //posX
								sumoPosition2D.y, //posY
								(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto)), //actual edge 
								(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)), //actual route
								(double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto)), //speed
								infoDistanceCompany, //traveled distance calculated from last step 
								(double) sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)), //Vehicle's fuel consumption in mg/s during step, to get the value for one step multiply with the step length						
								1, this.fuelType, this.fuelPrice,
								(double) sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto)), //Vehicle's CO2 emissions in mg/s during this time step, to get the value for one step multiply with the step length
								(double) sumo.do_job_get(Vehicle.getHCEmission(this.idAuto)), // Vehicle's HC emissions in mg/s during this time step, to get the value for one step multiply with the step length						
								this.personCapacity, // the total number of persons that can ride in this vehicle						
								this.personNumber // the total number of persons which are riding in this vehicle
						);
						this.drivingRepport.add(_repport);
						//new Excel(_repport);
						//graph.addData(_repport.getCo2Emission(), "CO2 Emission", "Time");

						sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
						sumo.do_job_set(Vehicle.setSpeed(this.idAuto, 6.95));

						sensoresAtualizados = true;
						monitor.notify();
						break;

					} else {
						System.out.println("SUMO is closed...");
					}
				} catch (Exception e) {
					Thread.sleep(20);
					System.out.println("sensor não atualizou");
					attempt--;
					e.printStackTrace();
				}
			}
		}
	}
	private boolean isOn(){
		SumoStringList vehiclesON = null;
		try {
			vehiclesON = (SumoStringList) this.sumo.do_job_get(Vehicle.getIDList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vehiclesON.contains(this.idAuto);
	}

	/*synchronized private void exportaTxt(String linha) throws IOException{
		BufferedWriter buffWrite = new BufferedWriter(new FileWriter("\\Users\\Usuario\\OneDrive\\Documentos\\Cursos\\Ufla\\11 periodo\\Sistemas Distribuídos\\report.txt", true));
		buffWrite.append(linha + "\n");
		buffWrite.close();
	}*/

	public void esperaSensores() throws InterruptedException{
		synchronized (monitor){
			while(!sensoresAtualizados){
				monitor.wait();
			}
			sensoresAtualizados = false;
		}
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdAuto() {
		return this.idAuto;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	public SumoColor getColorAuto() {
		return this.colorAuto;
	}

	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	public void setFuelPreferential(int _fuelPreferential) {
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}

	public double getLastDistance(){
		return infoDistanceCompany;
	}

	private class CalculaKm{
		private double longitude;
		private double latitude;
		private double cartesianoX;
		private double cartesianoY;
		private double[] historicoLatitude;
		private double[] historicoLongitude;
		private int aux;
		private double distancia;
	
		public CalculaKm(){
			historicoLatitude = new double[2];
			historicoLongitude = new double[2];
			aux = 0;
			distancia = 0;
		}
	
		public Double calcular(String posicao) throws Exception{
			String[] partes = posicao.split(",");
			try {
				cartesianoX = Double.parseDouble(partes[0]);
				cartesianoY = Double.parseDouble(partes[1]);
			} catch (NumberFormatException e) {}
			
			double latitudeOrigem = 40.0;  // Latitude da origem em graus
			double longitudeOrigem = -75.0;  // Longitude da origem em graus
			
			// Constante para conversão de graus para radianos
			double degToRad = Math.PI / 180.0;
			
			// Converter coordenadas cartesianas para latitude e longitude
			latitude = latitudeOrigem + (cartesianoY / 111320.0);
			longitude = longitudeOrigem + (cartesianoX / (111320.0 * Math.cos(latitudeOrigem * degToRad)));
			historico();
			return distancia;
		}
	
		private void historico(){
			if(aux == 1){
				historicoLatitude[1] = latitude;
				historicoLongitude[1] = longitude;
				atualizaDistancia();
			} else{
				historicoLatitude[0] = latitude;
				historicoLongitude[0] = longitude;
				aux++;
			}
		}
		
		public void atualizaDistancia(){
			double R = 6371;
	
			// Diferença de latitude e longitude
			double dlat = Math.toRadians(historicoLatitude[1]) - Math.toRadians(historicoLatitude[0]);
			double dlon = Math.toRadians(historicoLongitude[1]) - Math.toRadians(historicoLongitude[0]);
	
			// Fórmula de Haversine
			double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
					   Math.cos(Math.toRadians(historicoLatitude[0])) * Math.cos(Math.toRadians(historicoLatitude[1])) *
					   Math.sin(dlon / 2) * Math.sin(dlon / 2);
	
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	
			// Distância em metros
			distancia = R * c * 1000;
			historicoLatitude[0] = historicoLatitude[1];
			historicoLongitude[0] = historicoLongitude[1];
	
		}
	
	}

	public class RealTimeChart extends JFrame implements Runnable {
		private DefaultCategoryDataset dataset;

		public RealTimeChart(String title) {
			dataset = new DefaultCategoryDataset();
			JFreeChart chart = ChartFactory.createLineChart(
					title,
					"Time",
					"Value",
					dataset
			);

			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setPreferredSize(new Dimension(560, 370));
			add(chartPanel);

			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			pack();
			setVisible(true);
			
			new Thread(this).start();
    	}

		@Override
		public void run() {
			while(true){
			Timer timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// Atualize os dados aqui
					// Suponha que você tenha uma lista chamada 'dados' que está sendo atualizada em
					// algum lugar
					for (int i = 0; i < drivingRepport.size(); i++) {
						addData(drivingRepport.get(i).getCo2Emission(), "Série", "Categoria " + i);
					}
				}
			});

			timer.start();
			try {
				Thread.sleep(500);
			} catch (Exception e) {e.printStackTrace();}
			}
		}

		public void addData(double value, String seriesKey, String categoryKey) {
			dataset.addValue(value, seriesKey, categoryKey);
			repaint();
		}
	}

	public DrivingData getLastRepport() {
		return _repport;
	}

}