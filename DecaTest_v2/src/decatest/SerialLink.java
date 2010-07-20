package decatest;

import decatest.gui.StatusLight;
import decatest.gui.StatusLight.Status;
//import processing.serial.Serial;
import decatest.serial.DacConnection;
import decatest.serial.DacPacketEvent;
import decatest.serial.NotConnectedException;

public class SerialLink implements Runnable {
	//private SerialDriver myPort;
//	private DecaTest dt;
	private DacConnection dCon;
	private DacPacketEvent dpe;
	private String com;
	public String getCom(){return com;}
	private StatusLight sl;
	public enum ConStat{
		CONNECTED, NOT_CONNECTED, PROBLEM;
	}
	public volatile ConStat st;
	public SerialLink(DacPacketEvent d, StatusLight sl, String com) {
		//this.dt = PM;
		this.dpe = d;
		this.com = com;
		this.sl = sl;
		st=ConStat.NOT_CONNECTED;
	}
	public boolean getConnectedState(){
		return dCon.sd.connected;
	}
	// public SerialLink(PApplet pa, String str) {
	// // TODO Auto-generated constructor stub
	// myPort = new Serial(pa, str, 9600);
	// }

	public boolean dataWaiting() {
		return dCon.sd.availableBool();		
	}

	public int readData() {
		return dCon.sd.readInt();
	}

	//public void writeData(String str) {
	//	myPort.write(str);
	//}

	public void writeData(int i) throws Exception, NullPointerException, NotConnectedException {
		// TODO Auto-generated method stub
		//myPort.write((byte) i);
		dCon.sendByte((byte) i);
	}

	@Override
	public void run() {
		System.out.println("In Run Command");
		// TODO Auto-generated method stub
		// set light to connecting
		sl.setStatus(Status.YELLOW);
		dCon=new DacConnection(dpe,com);
		//myPort = new SerialDriver(com, 115200);
		
		if (dCon.sd.connected){
			sl.setStatus(Status.GREEN);
			st=ConStat.CONNECTED;
		}else{
			sl.setStatus(Status.RED);
			st=ConStat.PROBLEM;
			
		}
		
	}
}
