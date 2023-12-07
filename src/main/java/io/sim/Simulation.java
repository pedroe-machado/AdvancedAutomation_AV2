package io.sim;

import java.io.IOException;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Simulation extends Thread{
    private SumoTraciConnection sumo;
	private long rate;
    private Company company;
    private AlphaBank alphaBank;
    private FuelStation fuelStation;

    public Simulation(long rate){
		this.rate = rate;
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		this.sumo.addOption("start", "1"); // auto-run on GUI show
		this.sumo.addOption("quit-on-end", "1"); // auto-close on end
		try {
			this.sumo.runServer(12345);
			System.out.println("SUMO iniciado na porta 12345");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao iniciar SUMO");
		}
		new Out("data/Log.txt");
    }

	public void run() {
		try {
			while(this.sumo.isClosed()){
				System.out.println("{SIM:37} SUMO não iniciado - "+ System.currentTimeMillis());
				Thread.sleep(100);
			};

			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        	scheduler.scheduleAtFixedRate(new PeriodicTask(), 1000, rate, TimeUnit.MILLISECONDS);

			company = new Company(sumo);
			company.join();

			ArrayList<String> users = company.getCLTs(); //Cria todas as contas no banco
			users.add("company");
			users.add("fuelStation");

			alphaBank = new AlphaBank(users);
			alphaBank.start();

			try { Thread.sleep(500); } catch (Exception e) {}		
			fuelStation = FuelStation.getInstance(2);
			fuelStation.start();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class PeriodicTask implements Runnable {
		@Override
		public void run() {
			try {
				sumo.do_timestep();
			} catch (Exception e) {
				System.out.println("{SIM:PERIODIC:70} Erro timestep - "+ System.currentTimeMillis());
				e.printStackTrace();
			}
		}
	}
}
