package decatest.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.TooManyListenersException;

import decatest.gui.StatusLight;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class SerialDriver implements SerialPortEventListener {
	private static final boolean debug = true;
	InputStream inStr;

	public void setInputStream(InputStream is) {
		inStr = is;
	}

	OutputStream outStr;

	public void setOutputStream(OutputStream os) {
		outStr = os;
	}

	int bufferIndex;
	int bufferLast;
	SerialPort port;

	public void setPort(SerialPort p) {
		port = p;
	}

	int rate;

	public int getRate() {
		return rate;
	}

	static final int parity = SerialPort.PARITY_NONE;
	static final int databits = 8;
	static final int stopbits = SerialPort.STOPBITS_1;
	String com;

	public String getCom() {
		return com;
	}

	static int numTimesInPortConnect = 0;
	boolean monitor = false;
	public boolean connected = false;
	volatile LinkedList<Byte> buffer;

	// this is the end transmit array which the arduino will send to the PC.
	// We can change this to whatever we want as long as it's the same on the
	// arduino
	public static final byte[] endTrans = { (byte) 0xAA, (byte) 0xBB,
			(byte) 0xCC, (byte) 0xDD };
	Thread thread;
	Serial_Event se;

	public SerialDriver(Serial_Event se, String com, int rate) {
		this.se = se;
		sInit(com, rate);
	}

	/**
	 * init the object. if the com port is in AvalCom's list of com ports,
	 * connect to it otherwise, start the reconnect thread to wait for it
	 * 
	 * @param com
	 * @param rate
	 */
	private void sInit(String com, int rate) {
		// System.out.println("SerialDriver Init");
		this.com = com;
		this.rate = rate;
		buffer = new LinkedList<Byte>();
		try {
			port = null;
			CommPortIdentifier portId = AvalComs.checkComs(this.com);
			if (portId != null) { // if the comm exists
				portConnect(portId, this);
			}
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();

		}

		if (port == null) {
			System.out.println(com + " says *Port null*");
			new Thread(reconnect).start();
		} else {
			System.out.println("INIT FINISHED");
			connected = true;
		}
	}

	static boolean pause = false;

	/**
	 * need to pass in itself since this only forwards the command to the static
	 * method
	 * 
	 * @param pID
	 * @throws PortInUseException
	 * @throws IOException
	 * @throws UnsupportedCommOperationException
	 * @throws TooManyListenersException
	 */
	private void portConnect1(CommPortIdentifier pID)
			throws PortInUseException, IOException,
			UnsupportedCommOperationException, TooManyListenersException {
		portConnect(pID, this);
	}

	/**
	 * Had issues with this method not being static or synchronized. Only one
	 * thread is allowed in here at a time because we were originally having
	 * issues with the drivers not being able to support multiple threads trying
	 * to connect at the same time.
	 * 
	 * @param portId
	 * @param sd
	 *            --this needs to use a lot of class variables - pass in this
	 * @throws PortInUseException
	 *             ---something else is already using this port.
	 * @throws IOException
	 * @throws UnsupportedCommOperationException
	 * @throws TooManyListenersException
	 *             ---multiple listeners attached to the same object
	 */
	synchronized static private void portConnect(CommPortIdentifier portId,
			SerialDriver sd) throws PortInUseException, IOException,
			UnsupportedCommOperationException, TooManyListenersException {
		sd.setPort(null);
		pause = true;
		System.out
				.print("com: " + sd.getCom() + "IN PORT CONNECT. port.open; ");
		SerialPort tempPort = (SerialPort) portId
				.open("Serializationlol", 2000);
		System.out.print("port.getIn; ");
		sd.setInputStream(tempPort.getInputStream());
		System.out.print("port.getout; ");
		sd.setOutputStream(tempPort.getOutputStream());
		System.out.println("port.restOfIt; ");
		tempPort.setSerialPortParams(sd.getRate(), databits, stopbits, parity);
		tempPort.addEventListener(sd); // tell the dll where the SerialEvent
										// method is located
		tempPort.notifyOnDataAvailable(true); // driver will call the
												// serialEvent method on dataAv

		sd.setPort(tempPort);
		pause = false;

	}

	/**
	 * the reconnect thread. the program will enter this thread of we have lost
	 * the connection to the arduino or never had it to begin with
	 */
	private Runnable reconnect = new Runnable() {
		public void run() {
			CommPortIdentifier portId;
			System.out.println(com + "--in reconnect");
			connected = false;
			// wait a couple seconds before continuing
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			breakOut: while (true) {
				System.out.println(com + " checking inputs;");
				// check if our com port is in the list
				portId = AvalComs.checkComs(com);
				try {
					if (portId != null) { // if the comm exists
						flushLocal(); // clear local buffer
						portConnect1(portId); // reconnect to the unit in
												// question
						connected = true;
						break breakOut; // break out of the while loop
					}
				} catch (PortInUseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedCommOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TooManyListenersException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// com port wasnt on the list, wait a second before checking
				// again
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			error = false;
		}
	};

	/**
	 * this method is called by the watchdog timer to reconnect to the arduino
	 * when the timer goes off
	 */
	public void reconnectToArd() {
		error = true;

		thread = new Thread(reconnect);
		thread.start();
		try {
			port.notifyOnDataAvailable(false);
			port.removeEventListener();
			port.close();
			port = null;
		} catch (NullPointerException e) {
			System.out.println(com
					+ " is trying to close a port that is already closed.");
		}
	}

	/**
	 * for testing purposes only
	 */
	public void killPort() {
		port.close();
		port = null;
	}

	/**
	 * delete everything in the buffer. if we reconnect to it, the data in the
	 * buffer will only cause problems
	 */
	private void flushLocal() {
		buffer.clear();
	}

	boolean error = false;

	/**
	 * the serialEvent method is called by the dll to process a newly received
	 * byte
	 */
	@Override
	synchronized public void serialEvent(SerialPortEvent serialEvent) {
		// first off, try to read from the in stream to see if we even can
		// if we can, it will pass by this, if we can't and it throws an
		// exception, we need to kill the port object to stop red text spammage
		// while waiting for the watchdog timer to go off
		// also, set the error state to true so we do not run the next section
		// of code.
		try {
			inStr.available();
		} catch (IOException e) {
			System.out.println("IO EXCEPTION - Disconnected from arduino");
			error = true;
			port.close();
		}
		// if the previous section didnt give us an error do this next bit
		// i do not believe the pause variable does anything but it wouldnt get
		// in the way
		if (!error && !pause) {
			// in case we have told the DLL to return more than just data
			// events,
			// check to see if this is in fact a data event before executing the
			// code
			if (serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				if (debug) {
					System.out.println("");
					System.out.print("COMMMMMMMMMMMM: " + com
							+ "; SerialPort DataEvent");
				}
				try {
					// get the number of bytes waiting in the input stream
					int av = inStr.available();
					if (debug)
						System.out.print("Num AV: " + av + "; ");
					// keep adding the bytes in the input stream to the linked
					// list of bytes until
					// we've cleared out the input stream
					while (av > 0) {
						synchronized (buffer) {
							byte b = (byte) inStr.read();
							if (debug)
								System.out.print("; Val: "
										+ ((int) b & 0x000000ff));
							buffer.addLast(b);
							av = inStr.available();
						}
					}
					// if listener is enabled and we've received more than 4
					// bytes...
					int bufferSize = buffer.size();
					if (se != null && bufferSize > 5) {
						int endTransLocation = 0;
						// create and process packets until no more complete
						// packets are in the buffer
						while (endTransLocation != -1) {
							// check for end transmission
							// if the find EndTrans method returns -1, there
							// isnt a complete packet in the buffer. keep doing
							// this until it returns -1 and the buffer is
							// cleared
							endTransLocation = findEndTransmit();
							if (endTransLocation != -1) {
								se.serialListener(endTransLocation);
							}
						}
						// call serial listener
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Some other type of event: "
						+ serialEvent.getEventType());
			}
		}

	}

	/**
	 * searches for the bytes defined in the "endTrans" constant variable
	 * defined at the top of the file if it finds it, it will return the
	 * location of it, if not, returns -1 to say there aint a complete packet in
	 * there
	 * 
	 * @return
	 */
	private int findEndTransmit() {
		synchronized (buffer) {
			int iterateOver = buffer.size() - endTrans.length + 1;
			int i;
			for (i = 0; i < iterateOver; i++) {
				boolean t = false;
				for (int j = 0; j < endTrans.length; j++) {
					// System.out.println("buffer["+(i+j)+
					// "]: 0x"+Integer.toHexString(buffer.get(i+j)) +
					// "\tendTrans["+j+"]: 0x"+Integer.toHexString(endTrans[j]));
					if (buffer.get(i + j) == endTrans[j]) {
						// System.out.println("Match at buffer["+(i+j)+ "]: "+
						// Integer.toHexString(buffer.get(i+j))+";");
						t = true;
					} else {
						// t = false;
						break;
					}
				}
				if (t == true)
					return i + endTrans.length;
			}
			return -1;
		}
	}

	/**
	 * 
	 * @return the number of bytes in the linked list
	 */
	public int available() {
		return buffer.size();
	}

	/**
	 * 
	 * @return a boolean value for if the buffer is not empty
	 */
	public boolean availableBool() {
		boolean temp = false;
		if (buffer.size() > 0) {
			temp = true;
		}
		return temp;
	}

	/**
	 * translate the byte in the front of the buffer to an integer and return it
	 * to the caller (also deletes it from the linked list)
	 * 
	 * @return the integer requested
	 */
	public int readInt() {
		int val = -1;
		synchronized (buffer) {
			if (buffer.size() > 0) {
				val = (int) (buffer.pop()) & 0x000000ff;
			}
		}
		return val;
	}

	/**
	 * Returns the next byte in the buffer as a char. Returns -1, or 0xffff, if
	 * nothing is there.
	 */
	public char readChar() {
		if (availableBool())
			return (char) ((byte) readInt());
		return (char) (-1);
	}
	/**
	 * writes the inputed byte to the outgoing stream if we are in a connected state
	 * @param b
	 * @throws IOException
	 * @throws NotConnectedException
	 */
	public void write(byte b) throws IOException, NotConnectedException {
		if (connected) {
			byte[] bb = new byte[1];
			bb[0] = b;
			outStr.write(bb);
			// System.out.println("Sent byte");
		} else {
			throw new NotConnectedException("Not connected to arduino...");
		}
	}
}
