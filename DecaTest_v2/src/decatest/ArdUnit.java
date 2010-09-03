package decatest;

import java.util.LinkedList;

import decatest.gui.StatusLight;
import decatest.gui.StatusLight.Status;
import decatest.serial.DacConnection;
import decatest.serial.packet.DacPacketEvent;
import decatest.serial.packet.WatchDogPacket;

/**
 * There is one of these classes for each arduino we are connected to. All
 * incoming packets and button events eventually get here for processing
 * 
 * @author MIGIT
 * 
 */
public class ArdUnit implements DacPacketEvent {
	
	public enum ConStat{
		CONNECTED, NOT_CONNECTED, PROBLEM;
	}
	//location of the boxes to draw and the ID number associated with this ardunit
	private int x1, y1, id;
	//height and width of the boxes to draw
	private static final int width = 125;
	private final static int height = 75;
	//parent object
	private DecaTest dt;
	private StatusLight sLight;
	//com belonging to this ardUnit
	private String com;
	private WatchDog wd;
	private DacConnection dCon;
	public volatile ConStat st;
	private FileIO fio;
	long lastTime = 0;
	controlP5.Button b;
	private LinkedList<String[]> bat1Data;
	private LinkedList<String[]> bat2Data;

	public ArdUnit(DecaTest dt, String str, int x1, int y1) {
		this.x1 = x1;
		this.y1 = y1;
		this.dt = dt;
		this.com = str.toUpperCase();
		this.lastTime = System.currentTimeMillis();
		this.st=ConStat.NOT_CONNECTED;
		this.bat1Data = new LinkedList<String[]>();
		this.bat2Data = new LinkedList<String[]>();
		this.fio = new FileIO(com);
	
	}

	/**
	 * draw this ardUnit's gui elements and set the ID to the inputed number.
	 * this ID is used for routing button events
	 * 
	 * @param id
	 */
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
	/**
	 * instatiate the dacConnection which will start a connection to the arduino. 
	 * also start the gui updater thread to update the lights on the screen
	 */
	public void SerialConnect() {
		dCon= new DacConnection(this, com);
		new Thread(guiUpdater).start();
	}
	/**
	 * status light updating thread - one per ardUnit - should refactor this to be a single thread updating all of them
	 */
	private Runnable guiUpdater = new Runnable(){
	@Override
		public void run() {
			//System.out.println("In Run Command");
			sLight.setStatus(Status.YELLOW);
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
		/**
		 * button handler.  if the button controller calls this method then we have a button event for this ardUnit
		 *THIS CURRENTLY ONLY CONTAINS TEST CODE!
		 */
	public void bEvent() {
		System.out.println("Ard id# " + id + " event handled");
		try {
			//sLink.writeData(0xAC);
			dCon.sd.killPort();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	int callCount=0;
	@Override
	public void onWatchDogData(WatchDogPacket packet) {
		//This commented out code prints out the time elapsed from the arduinos 
		//perspective and the java program perspective if setup on the arduino in question
		
//		System.out.println("**"+ com+ "**-----------onWatchDogData called-------------( tDif: "
//						+ (System.currentTimeMillis() - lastTime)
//						+ ")---("
//						+ packet.time1 + ")----(" + packet.time2 + ")----");
//		System.out.println("**"+ com+ "**-----" + ((packet.time1 << 8) + packet.time2)
//				+ "-----");
		//lastTime = System.currentTimeMillis();
		
		System.out.println("; batID="+packet.batID);
		if(packet.batID == 0){
			bat1Data.add(packet.values);
		}else{
			bat2Data.add(packet.values);
		}
		// currently it outputs to a file once every 100 times this is called (50 points per battery
		// eventually needs to be switched to if we read in a 16 as a transType from the program file
		callCount++;
		if(callCount >= 100){
			callCount=0;
			fio.writeUnitToFile(bat1Data, bat2Data);
			bat1Data.clear();
			bat2Data.clear();
		}
				
		//watchdog housekeeping
		if (wd != null)
			wd.killTimer();
		wd = new WatchDog(dCon.sd);
		wd.startTimer();
		

	}

	public String getCom() {
		return com;
	}
}
