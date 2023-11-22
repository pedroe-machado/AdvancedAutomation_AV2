package io.sim;

import java.io.IOException;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Simulation extends Thread{
    private SumoTraciConnection sumo;
    private Company company;
    private AlphaBank alphaBank;
    private FuelStation fuelStation;

    public Simulation(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new PeriodicTask(), 2000, 1000, TimeUnit.MILLISECONDS);

		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end
    }
	public void run() {
		try {
			sumo.runServer(12345);
					
			company = new Company(sumo);
			company.join();
			ArrayList<String> users = company.getCLTs(); //Cria todas as contas no banco
			users.add("company");
			users.add("fuelStation");

			alphaBank = new AlphaBank(users);
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
				System.out.println("erro timestep");
				e.printStackTrace();
			}
		}
	}
}
