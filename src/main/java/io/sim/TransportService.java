package io.sim;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class TransportService extends Thread {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;
	private Car car;

	public TransportService(boolean _on_off, String _idTransportService, Car car,
			SumoTraciConnection _sumo) {

		this.on_off = _on_off;
		this.idTransportService = _idTransportService;
		this.car = car;
		this.sumo = _sumo;
	}

	@Override
	public void run() {
		System.out.println("{TS:26} TransportService " + this.idTransportService + " started at time: " + System.currentTimeMillis());
		try {
			this.initializeRoute();
			while (this.on_off) {
				Thread.sleep(this.car.getAcquisitionRate());
				//System.out.println("{TS:31} TransportService " + this.idTransportService + " running at time: " + System.currentTimeMillis());
				if (this.getSumo().isClosed()) {
					this.on_off = false;
					System.out.println("{TS:34} SUMO is closed...");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//sumo.do_job_set(Vehicle.remove(this.car.getIdAuto(),(byte)0));
			System.out.println("{TS:42} Vehicle " + this.car.getIdAuto() + " removed at time: " + System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeRoute() {
		System.out.println("{TS:49/initRoute} Inicializando rota");
		
		int attempts = 3;
		while (attempts > 0) {
			try {
				if (!rotaRepetida()) {
					System.out.println("{TS:55/initRoute} Rota não repetida - adicionando rota "+System.currentTimeMillis());
					sumo.do_job_set(Route.add(this.car.getCurrenRoute().getId(), this.car.getCurrenRoute().getEdges()));
				}
				if (!car.onSumo()) {
					System.out.println("{TS:59/initRoute} Carro não está no sumo - adicionando carro "+System.currentTimeMillis());
					sumo.do_job_set(Vehicle.addFull(this.car.getIdAuto(), // vehID
							this.car.getCurrenRoute().getId(), // routeID
							"DEFAULT_VEHTYPE", // typeID
							"begin", // depart
							"0", // departLane
							"0", // departPos
							"0", // departSpeed
							"current", // arrivalLane
							"max", // arrivalPos
							"current", // arrivalSpeed
							"", // fromTaz
							"", // toTaz
							"", // line
							this.car.getPersonCapacity(), // personCapacity
							this.car.getPersonNumber()) // personNumber
					);
					sumo.do_job_set(Vehicle.setColor(car.getIdAuto(), car.getColorAuto()));
				}
				
				while(!car.onSumo()){
					System.out.println("{TS:80/initRoute} Waiting for car to be on sumo");
					Thread.sleep(1000);
				};

				if(car.onFinalSpace()){
					System.out.println("{TS:84/initRoute} Carro está na vaga final - teleportando para a vaga inicial");
					sumo.do_job_set(Vehicle.moveTo(car.getIdAuto(), (String)sumo.do_job_get(Vehicle.getLaneID(car.getIdAuto())), (Double)sumo.do_job_get(Route.getParameter(car.getCurrenRoute().getId(), "begin"))));
				}

				sumo.do_job_set(Vehicle.setRouteID(car.getIdAuto(), car.getCurrenRoute().getId()));
				System.out.println("{TS:89/initRoute} Vehicle " + car.getIdAuto() + " setRouteID at time: " + System.currentTimeMillis());

				sumo.do_job_set(Vehicle.setSpeedMode(car.getIdAuto(), 31));
				System.out.println("{TS:92/initRoute} Vehicle " + car.getIdAuto() + " setSpeedMode 31 at time: " + System.currentTimeMillis());
				System.out.println("{TS:93/initRoute} Vehicle " + car.getIdAuto() + " initialized at time: " + System.currentTimeMillis());
				
				break;

			} catch (Exception e1) {
				e1.printStackTrace();
				attempts--;
				System.out.println("{TS:93/initRoute} Erro ao inicializar rota - tentando novamente");
			}
		}
		if (attempts == 0) {
			System.out.println("{TS:97/initRoute} Erro ao inicializar rota - serviço não iniciado");
			this.on_off = false;
		}
	}

	private boolean rotaRepetida() {
		SumoStringList rotasAdd = null;
		try {
			System.out.println("{TS:112/rotaRepetida} Verificando se rota já foi adicionada");
			rotasAdd = (SumoStringList) sumo.do_job_get(Route.getIDList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotasAdd.contains(this.car.getCurrenRoute().getId());
	}

	public boolean isOn_off() {
		return on_off;
	}

	public void setOn(boolean _on_off) {
		System.out.println("{TS:125} TransportService " + this.idTransportService + " setOn: " + _on_off + " at time: " + System.currentTimeMillis());
		this.on_off = _on_off;
	}

	public String getIdTransportService() {
		return this.idTransportService;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public Car getCar() {
		return this.car;
	}

	public io.sim.Route getRoute() {
		return this.car.getCurrenRoute();
	}
}