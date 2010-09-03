package decatest;
//import processing.serial.*;
import gnu.io.CommPortIdentifier;

import java.util.Enumeration;
import java.util.LinkedList;
//import ArdUnit;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Textarea;
import decatest.gui.StatusLight.Status;
import decatest.gui.TextBoxEvents;
import decatest.serial.AvalComs;
import decatest.serial.DacConnection;
//import processing.app.Preferences;
import processing.core.PApplet;

/**This class contains the processing main applet.  
 * if you are familiar with processing, you will notice the setup/loop methods in here
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

/**
 * The setup method is the first method which is called by the processing core.
 * This method starts the initilization of the system and creates a linked list of ArdUnits, one for each Arduino connected to the system
 * 
 * NOTE: THE FIRST ARDUNIT DEFINED (AND CONNECTED TO) WILL NOT SUPPORT A RECONNECT - THE ACT OF RECONNECTING WILL CRASH THE JVM
 * THE TEMPORARY FIX FOR THIS IS TO SET THE FIRST ONE IN THE ArdUnit ARRAY TO A BLUETOOTH COM OR ATTACH AN EXTRA ARDUINO TO THE SYSTEM
 * WHICH WOULD REMAIN IDLE FOR THE DURATION OF THE TESTS.
 */
	@SuppressWarnings("unchecked")
	public void setup() {
		//init screen size and the frame rate
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
		
		//hard coded arduino units and the top left hand corner 
		//of where to place the dark grey boxes on the screen
		ards.add(new ArdUnit(this, "COM12", 10,		20 ));
		ards.add(new ArdUnit(this, "COM15", 140,	100));
		ards.add(new ArdUnit(this, "COM3", 	10, 	100));
		ards.add(new ArdUnit(this, "COM14", 140,	20 ));
		//TODO This hard coded value will need to be setup on each PC.
		
		//init the 
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
			
			//for each loops dont work in the processing environment and templates
			//also dont work, so this method was made "processing proof"
			//for (ArdUnit au : ards) {
            for (int j =0; j<ards.size(); j++){
                ArdUnit au= (ArdUnit)ards.get(j);
				au.SerialConnect();
				TextBoxEvents.println("Connecting to Arduino on : " + au.getCom());
				String textInBox = TextBoxEvents.getContents();
				int counter = 0;
				//this next while loop puts a variable length of dots next to the connecting text 
				//so the user knows the program is working.
				while (au.st == ArdUnit.ConStat.NOT_CONNECTED) {
					// enter connecting status code here
					String temp = "";
					for (int i = 0; i <= counter; i++) {
						temp += ".";
					}
					TextBoxEvents.setText(textInBox + temp);
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
				//once it's out of the not connected state, add text to the console inside the program informing the user of the status
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
	
	/**
	 * draw the ardUnit dark grey boxes on the gui
	 */
	private void initGUIs() {
		
		for (int x = 0; x < ards.size(); x++) {
			((ArdUnit)(ards.get(x))).drawGUI(x);
		}
	}

	public void controlEvent(ControlEvent theEvent) {
		//route the event to the correct ard unit.  note that when there are mutliple buttons this will end up being slightly more complex!
		((ArdUnit)(ards.get(theEvent.controller().id()))).bEvent();
		// System.out.println("Event Handled: " + theEvent.controller().name());
	}

	
//this is to turn it from a applet to a java application, but it seems to cause it to go full screen
       // public static void main(String _args[]) {
	//	PApplet.main(new String[] { this.class.getName() });
	//}
}
