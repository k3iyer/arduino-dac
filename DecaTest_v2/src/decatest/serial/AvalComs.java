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
	static boolean alreadyActive = false;
//	static boolean pause = true;
//	static public void setPause(boolean p) {
//		pause = p;
//	}
	
	/**
	 * allows other threads to check if this class is already polling
	 */
	public static boolean getThreadState(){
		return alreadyActive;
	}
	/**
	 * start's the thread and sets already active to true.
	 */
	synchronized static public void startPolling() {
		if (!alreadyActive) {
			coms = new LinkedList<CommPortIdentifier>();
			alreadyActive = true;
			t = new Thread(r);
			t.start();
			
		}
	}
	
	/**
	 * return the thread running this - do not believe this is ever used or needed
	 * @return
	 */
	public static Thread getCommCheckingThread(){
		return t;
	}
	
	/**
	 * this is the thread which updates the linked list of comm port identifiers
	 */
	static Runnable r = new Runnable() {
		@Override
		public void run() {
			while (true) {
//				if (!pause) {
					System.out.print("scanning inputs: ");
					//don't want to be writing to the linked list of another thread is currently reading from it
					//this is because we wipe the linked list clean each time we run this thread
					synchronized (coms) {
						coms = new LinkedList<CommPortIdentifier>();
						Enumeration portList = CommPortIdentifier
								.getPortIdentifiers();
						while (portList.hasMoreElements()) {
							CommPortIdentifier portId = (CommPortIdentifier) portList
									.nextElement();
							//breakFlag = false;
							
							if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
								coms.add(portId);
							}
						}
//					}
					dataAv = true;
					//print avail coms
					System.out.print("AvailComs: ");
					printLLStrings(getAllComs());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	};
/**
 * input a string containing a com port (like - "COM13"), return the comm port identifier
 * @param str  - com port name
 * @return     - if the com port exists on the list, return the associated comm port identifier, else, return null
 */
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
		//don't want to run this code if the linked list is being updated.
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
/**
 * @return a linked list of strings containing all available com ports
 */
	public static LinkedList<String> getAllComs() {
		LinkedList<String> strs = new LinkedList<String>();
		synchronized (coms) {
			for (int i = 0; i < coms.size(); i++) {
				strs.add(((CommPortIdentifier) (coms.get(i))).getName());
			}
		}
		return strs;
	}
	/**
	 * print all the strings in the inputed linked list
	 * @param strs
	 */

	private static void printLLStrings(LinkedList<String> strs) {
		for (String s : strs) {
			System.out.print(s + "; ");
		}
		System.out.println("");
	}

}
