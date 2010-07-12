package decatest;

import decatest.gui.StatusLight;
import decatest.gui.StatusLight.Status;
//import processing.serial.Serial;
import decatest.serial.NotConnectedException;
import decatest.serial.SerialDriver;

public class SerialLink implements Runnable {
	private SerialDriver myPort;
//	private DecaTest dt;
	private String com;
	public String getCom(){return com;}
	private StatusLight sl;
	public enum ConStat{
		CONNECTED, NOT_CONNECTED, PROBLEM;
	}
	public volatile ConStat st;
	public SerialLink(StatusLight sl, String com) {
		//this.dt = PM;
		this.com = com;
		this.sl = sl;
		st=ConStat.NOT_CONNECTED;
	}
	public boolean getConnectedState(){
		return myPort.connected;
	}
	// public SerialLink(PApplet pa, String str) {
	// // TODO Auto-generated constructor stub
	// myPort = new Serial(pa, str, 9600);
	// }

	public boolean dataWaiting() {
		return myPort.availableBool();		
	}

	public int readData() {
		return myPort.readInt();
	}

	//public void writeData(String str) {
	//	myPort.write(str);
	//}

	public void writeData(int i) throws Exception, NullPointerException, NotConnectedException {
		// TODO Auto-generated method stub
		myPort.write((byte) i);
	}

	@Override
	public void run() {
		System.out.println("In Run Command");
		// TODO Auto-generated method stub
		// set light to connecting
		sl.setStatus(Status.YELLOW);
		myPort = new SerialDriver(com, 9600);
		if (myPort.connected){
			sl.setStatus(Status.GREEN);
			st=ConStat.CONNECTED;
		}else{
			sl.setStatus(Status.RED);
			st=ConStat.PROBLEM;
			
		}
		
	}
}
