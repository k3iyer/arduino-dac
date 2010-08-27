package decatest;
//import processing.serial.*;
import gnu.io.CommPortIdentifier;

import java.util.Enumeration;
import java.util.LinkedList;
//import ArdUnit;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Textarea;
import decatest.gui.ArdUnit;
import decatest.gui.StatusLight.Status;
import decatest.gui.TextBoxEvents;
import decatest.serial.AvalComs;
import decatest.serial.DacConnection;
//import processing.app.Preferences;
import processing.core.PApplet;

/**
 * 
 * @author MIGIT
 * 
 */
public class DecaTest extends PApplet {
	// SerialLink ard1;
	public ControlP5 cp5;
	public static final int backR = 245;
	public static final int backG = 245;
	public static final int backB = 245;
	
	private LinkedList ards = new LinkedList();
	public static final int screenWidth = 1000;
	public static final int screenHeight = 800;
	//private Thread init;
	//private static TextBoxEvents tb;
	//private Thread t2;

	@SuppressWarnings("unchecked")
	public void setup() {
		size(screenWidth, screenHeight);
		frameRate(30);
		//init controlp5 object
		cp5 = new ControlP5(this);
		//title
		cp5.addTextlabel("label", "DecaTest", screenWidth / 2 - 30, 5);
		//tb = new TextBoxEvents(cp5);
		TextBoxEvents.init(cp5);
		//start checking for available arduinos
		AvalComs.startPolling();
		//hard coded arduino units
		ards.add(new ArdUnit(this, "COM12", 10,		20 ));
		ards.add(new ArdUnit(this, "COM15", 140,	100));
		ards.add(new ArdUnit(this, "COM3", 	10, 	100));
		ards.add(new ArdUnit(this, "COM14", 140,	20 ));
		
		
		
		initGUIs();
		new Thread(initSerialConnection).start();;
		

	}

	/**
	 * This thread will connect to the arduinos defined in the setup method and
	 * display status in the text box
	 */
	private Runnable initSerialConnection = new Runnable() {
		public void run() {
			
			TextBoxEvents.println("Serial init method starting ");
			//for (ArdUnit au : ards) {
            for (int j =0; j<ards.size(); j++){
                ArdUnit au= (ArdUnit)ards.get(j);
				au.SerialConnect();
				TextBoxEvents.println("Connecting to Arduino on : " + au.getCom());
				String textInBox = TextBoxEvents.getContents();
				int counter = 0;
				while (au.st == ArdUnit.ConStat.NOT_CONNECTED) {
					// enter connecting status code here
					String temp = "";
					for (int i = 0; i <= counter; i++) {
						temp += ".";
					}
					TextBoxEvents.setText(textInBox + temp);
					//System.out.println(textInBox + temp);

					counter++;
					if (counter >= 4)
						counter = 0;
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Wait Statement failed");
						e.printStackTrace();
					}
				}
				// textbox.setText(textInBox);
				if (au.st == ArdUnit.ConStat.PROBLEM) {
					// send out a warning
					TextBoxEvents.println("Problem connecting to arduino on: "
							+ au.getCom());
				} else if (au.st == ArdUnit.ConStat.CONNECTED) {
					TextBoxEvents.println("Connected to arduino on: " + au.getCom());
				}
			}
			TextBoxEvents.println("Serial init method finished ");
		}
	};
	
	private void initGUIs() {
		// fill(0, 0, 0);
		// rect((screenWidth / 3) * 2 - 3, 2, (screenWidth / 3), screenHeight /
		// 2 + 5);
		for (int x = 0; x < ards.size(); x++) {
			((ArdUnit)(ards.get(x))).drawGUI(x);
		}
	}
public void draw() {
      //  System.out.print("k");
           int i=0;
          i=i+1;
	}
	public void controlEvent(ControlEvent theEvent) {
		// theEvent.controller().id
		((ArdUnit)(ards.get(theEvent.controller().id()))).bEvent();
		// System.out.println("Event Handled: " + theEvent.controller().name());
	}

	

       // public static void main(String _args[]) {
	//	PApplet.main(new String[] { this.class.getName() });
	//}
}
