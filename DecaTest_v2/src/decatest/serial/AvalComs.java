package decatest.serial;

import gnu.io.CommPortIdentifier;

import java.util.Enumeration;
import java.util.LinkedList;

/**
 * This class only keeps track of available com ports (So we don't have multiple
 * threads checking for available com ports when multiple arduinos are
 * disconnected)
 * 
 * @author MIGIT
 * 
 */
public abstract class AvalComs {// implements Runnable {
	volatile static LinkedList<CommPortIdentifier> coms;
	static int numChecking = 0;
	static boolean dataAv = false;
	static Thread t;
	static int charles = 0;
	static boolean alreadyActive = false;

	synchronized static public void startPolling() {
		charles++;
		if (!alreadyActive) {
			alreadyActive=true;
			coms = new LinkedList<CommPortIdentifier>();
			// System.out.println("AvalComs Init("+ charles+")");
			t = new Thread(r);
			t.start();
		}
	}

	static Runnable r = new Runnable() {
		@Override
		public void run() {
			System.out.println("AvalComs Run(" + charles + ")");
			// TODO Auto-generated method stub
			while (true) {
				System.out.print("scanning inputs: ");
				synchronized (coms) {
					coms = new LinkedList<CommPortIdentifier>();
					boolean breakFlag;
					Enumeration portList = CommPortIdentifier
							.getPortIdentifiers();
					while (portList.hasMoreElements()) {
						CommPortIdentifier portId = (CommPortIdentifier) portList
								.nextElement();
						breakFlag = false;
						if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
							coms.add(portId);
						}
					}
				}
				dataAv = true;
				printLLStrings(getAllComs());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public static CommPortIdentifier checkComs(String str) {
		CommPortIdentifier temp, x = null;
		// System.out.println("IN CHECK COMS");
		// wait till there is data
		while (dataAv == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		synchronized (coms) {
			for (int i = 0; i < coms.size(); i++) {
				temp = (CommPortIdentifier) (coms.get(i));
				if (temp.getName().equals(str)) {
					x = temp;
					break;
				}
			}
		}
		return x;
	}

	public static LinkedList<String> getAllComs() {
		LinkedList<String> strs = new LinkedList<String>();
		synchronized (coms) {
			for (int i = 0; i < coms.size(); i++) {
				strs.add(((CommPortIdentifier) (coms.get(i))).getName());
			}
		}
		return strs;
	}

	private static void printLLStrings(LinkedList<String> strs) {
		for (String s : strs) {
			System.out.print(s + "; ");
		}
		System.out.println("");
	}

}
