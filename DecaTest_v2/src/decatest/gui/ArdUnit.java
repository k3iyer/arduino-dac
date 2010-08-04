package decatest.gui;

import java.sql.Time;
import java.util.Date;

import controlP5.Textarea;
import decatest.DecaTest;
import decatest.WatchDog;
import decatest.gui.StatusLight.Status;
import decatest.serial.AvalComs;
import decatest.serial.DacConnection;
import decatest.serial.NotConnectedException;
import decatest.serial.packet.DacPacket;
import decatest.serial.packet.DacPacketEvent;
import decatest.serial.packet.WatchDogPacket;

public class ArdUnit implements DacPacketEvent {
	public enum ConStat{
		CONNECTED, NOT_CONNECTED, PROBLEM;
	}
	private int x1, y1, id;
	private static final int width = 125;
	private final static int height = 75;
	private DecaTest dt;
	private StatusLight sLight;
	//public SerialLink sLink;
	private String com;
	private WatchDog wd;
	private DacConnection dCon;
	public volatile ConStat st;
	
	// Date d;
	long lastTime = 0;
	// public Thread t;
	// private String btnStr;
	controlP5.Button b;

	public ArdUnit(DecaTest dt, String str, int x1, int y1) {
		this.x1 = x1;
		this.y1 = y1;
		this.dt = dt;
		this.com = str.toUpperCase();
		// d=new Date();
		this.lastTime = System.currentTimeMillis();
		//this.wd = new WatchDog(sLink.getDacCon().sd);
		this.st=ConStat.NOT_CONNECTED;
	
	}

	public void drawGUI(int id) {
		this.id = id;
		dt.fill(139, 137, 137);
		dt.rect(x1, y1, width, height);
		dt.cp5.addTextlabel("ard1", "Ard:" + com, x1 + 2, y1 + 2);
		dt.cp5.addButton("btn", 0, x1 + 3, y1 + height - 15, width / 2, 10)
				.setId(this.id);
		dt.fill(238, 238, 224);
		sLight = new StatusLight(dt, x1 + width - 12, y1 + 2, 10, 10);
		sLight.setStatus(Status.DISABLED);
	}

	public void SerialConnect() {
		dCon= new DacConnection(this, com);
		//sLink = new SerialLink(this, sLight, com);
		new Thread(guiUpdater).start();
		// t.start();
	}
	//private Runnable connectToArduino = new Runnable(){
	private Runnable guiUpdater = new Runnable(){
	@Override
		public void run() {
			//System.out.println("In Run Command");
			// TODO Auto-generated method stub
			// set light to connecting
			sLight.setStatus(Status.YELLOW);
			
			//dCon=new DacConnection(ar, com);
			//myPort = new SerialDriver(com, 115200);
			while (true) {
				if (dCon.sd.connected) {
					sLight.setStatus(Status.GREEN);
					st = ConStat.CONNECTED;
				} else {
					sLight.setStatus(Status.RED);
					st = ConStat.PROBLEM;
				}
				try {
					Thread.sleep(900);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		};
	public void bEvent() {
		System.out.println("Ard id# " + id + " event handled");
		// sLight.setStatus(Status.RED);
		try {
			//sLink.writeData(0xAC);
			dCon.sd.killPort();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	//	} catch (NotConnectedException e) {
		//	sLight.setStatus(Status.RED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onNewData(DacPacket packet) {
		// TODO Auto-generated method stub
		System.out.println("**"+com+"**onNewData called");
	}

	@Override
	public void onWatchDogData(WatchDogPacket packet) {
		// TODO Auto-generated method stub

		System.out.println("**"+ com+ "**-----------onWatchDogData called-------------( tDif: "
						+ (System.currentTimeMillis() - lastTime)
						+ ")---("
						+ packet.time1 + ")----(" + packet.time2 + ")----");
		System.out.println("**"+ com+ "**-----" + ((packet.time1 << 8) + packet.time2)
				+ "-----");
		lastTime = System.currentTimeMillis();
		if (wd != null)
			wd.killTimer();
		wd = new WatchDog(dCon.sd);
		wd.startTimer();
		

	}

	public String getCom() {
		// TODO Auto-generated method stub
		return com;
	}
}
