package decatest;
//import processing.serial.*;
import gnu.io.CommPortIdentifier;

import java.util.Enumeration;
import java.util.LinkedList;
//import ArdUnit;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Textarea;
import decatest.SerialLink.ConStat;
import decatest.gui.ArdUnit;
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
	private static final int screenWidth = 1000;
	private static final int screenHeight = 800;
	private Thread init;
	private Textarea textbox;
	public Textarea getTextBox(){return textbox;}
	private Thread t2;

	@SuppressWarnings("unchecked")
	public void setup() {
		getComPorts();
		size(screenWidth, screenHeight);
		frameRate(30);
		cp5 = new ControlP5(this);
		cp5.addTextlabel("label", "DecaTest", screenWidth / 2 - 30, 5);
		textbox = cp5.addTextarea("box", "", (screenWidth / 3) * 2, 5,
				(screenWidth / 3), screenHeight / 2 + 5);
		// textbox.showScrollbar();
		textbox.enableColorBackground();
		ards.add(new ArdUnit(this, "COM3", 10, 20));
		ards.add(new ArdUnit(this,"COM14", 140,20));
		ards.add(new ArdUnit(this,"COM11", 10,100));
		
		initGUIs();
		t2 = new Thread(initSerialConnection);
		t2.start();

	}

	public void getComPorts() {
		// boolean empty = true;

		try {
			for (Enumeration enumeration = CommPortIdentifier
					.getPortIdentifiers(); enumeration.hasMoreElements();) {
				CommPortIdentifier commportidentifier = (CommPortIdentifier) enumeration
						.nextElement();
				if (commportidentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					// String curr_port =
					System.out.println(commportidentifier.getName());
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * This thread will connect to the arduinos defined in the setup method and
	 * display status in the text box
	 */
	private Runnable initSerialConnection = new Runnable() {
		public void run() {
			
			textbox.setText(textbox.text() + "\n Serial init method starting ");
			//for (ArdUnit au : ards) {
            for (int j =0; j<ards.size(); j++){
                ArdUnit au= (ArdUnit)ards.get(j);
				au.SerialConnect();
				textbox.setText(textbox.text() + "\n Connecting to Arduino on : " + au.sLink.getCom());
				String textInBox = textbox.text();
				int counter = 0;
				while (au.sLink.st == ConStat.NOT_CONNECTED) {
					// enter connecting status code here
					String temp = "";
					for (int i = 0; i <= counter; i++) {
						temp += ".";
					}
					textbox.setText(textInBox + temp);
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
				if (au.sLink.st == ConStat.PROBLEM) {
					// send out a warning
					textbox.setText(textbox.text()
							+ "\n Problem connecting to arduino on: "
							+ au.sLink.getCom());
				} else if (au.sLink.st == ConStat.CONNECTED) {
					textbox.setText(textbox.text()
							+ "\n Connected to arduino on: "
							+ au.sLink.getCom());
				
				}
			}
			textbox.setText(textbox.text() + "\n Serial init method finished ");
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
