package decatest.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.TooManyListenersException;


import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class SerialDriver implements SerialPortEventListener {
	private static final boolean debug=false;
	InputStream inStr;
	OutputStream outStr;
	// byte buffer[] = new byte[32768];
	int bufferIndex;
	int bufferLast;
	SerialPort port;
	int rate;
	int parity;
	int databits;
	int stopbits;
	String com;
	public String getCom(){return com;}
	static int numTimesInPortConnect = 0;
	boolean monitor = false;
	public boolean connected = false;
	volatile LinkedList<Byte> buffer;
	Thread thread;
	Serial_Event se;
	//static AvalComs aCom;
	public SerialDriver(String com, int rate) {
		sInit(com,rate);
	}
	public SerialDriver(Serial_Event se, String com, int rate) {
		this.se=se;
		//aCom=new AvalComs();
		AvalComs.startPolling();
		sInit(com,rate);
		
	}
	private void sInit(String com, int rate){
		//System.out.println("SerialDriver Init");
		this.com = com;
		this.rate = rate;
		this.parity = SerialPort.PARITY_NONE;
		this.databits = 8;
		stopbits = SerialPort.STOPBITS_1;
		buffer = new LinkedList<Byte>();
		try {
			port = null;
//			@SuppressWarnings("rawtypes")
//			Enumeration portList = CommPortIdentifier.getPortIdentifiers();
//			while (portList.hasMoreElements()) {
//				CommPortIdentifier portId = (CommPortIdentifier) portList
//						.nextElement();
//
//				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
//					System.out.println(portId.getName());
//					// System.out.println("found " + portId.getName());
//					if (portId.getName().equals(com)) {
//						// System.out.println("looking for "+iname);
//						portConnect(portId);
//						// System.out.println("opening, ready to roll");
//					}
//				}
//			}
			CommPortIdentifier portId = AvalComs.checkComs(this.com);
			if(portId!=null){ //if the comm exists
				portConnect(portId);
			}
		} catch (PortInUseException e) {
			// throw new SerialException("Serial port '" + iname +
			// "' already in use.  Try quiting any programs that may be using it.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			// throw new SerialException("Error opening serial port '" + iname +
			// "'.", e);
			// //errorMessage("<init>", e);
			// //exception = e;
			// //e.printStackTrace();
		}

		if (port == null){
			System.out.println(com + " says *Port null*");
			new Thread(reconnect).start();
		}else {
			System.out.println("INIT FINISHED");
			connected = true;
		}
	}

	synchronized private void portConnect(CommPortIdentifier portId)
			throws PortInUseException, IOException,
			UnsupportedCommOperationException, TooManyListenersException {
		port = null;
		// portId.removePortOwnershipListener("Serializationlol");
		// portId.
		System.out.print("IN PORT CONNECT. port.open; ");
		port = (SerialPort) portId.open("Serializationlol" + numTimesInPortConnect,
				2000);
		System.out.print("port.getIn; ");
		inStr = port.getInputStream();
		System.out.print("port.getout; ");
		outStr = port.getOutputStream();
		System.out.print("port.restOfIt; ");
		port.setSerialPortParams(rate, databits, stopbits, parity);
		port.addEventListener(this);
		port.notifyOnDataAvailable(true);
		numTimesInPortConnect++;
	}

	private Runnable reconnect = new Runnable() {
		public void run() {
			CommPortIdentifier portId;
			System.out.println(com+"--in reconnect");
			connected=false;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			breakOut: while (true) {
				//System.out.print("scanning inputs: ");
				//@SuppressWarnings("rawtypes")
//				Enumeration portList = CommPortIdentifier.getPortIdentifiers();
//				while (portList.hasMoreElements()) {
//					CommPortIdentifier portId = (CommPortIdentifier) portList
//							.nextElement();
//					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
//						String x = portId.getName();
//						if (x.equals(com)) {
//							// reconnected.
//							try {
//								portConnect(portId);
//								connected=true;
//								thread.destroy();
//								//break breakOut;
//							} catch (PortInUseException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (UnsupportedCommOperationException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (TooManyListenersException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						System.out.print(x+ "; ");
//					}
//				}
//				System.out.println("");
				System.out.println(com + " checking inputs;");
				portId = AvalComs.checkComs(com);
				try {
					if(portId!=null){ //if the comm exists
						portConnect(portId);
						break breakOut;
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
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
	};
public void reconnectToArd(){
	thread= new Thread(reconnect);
	thread.start();
	port.close();
}
	@Override
	synchronized public void serialEvent(SerialPortEvent serialEvent) {
		boolean error = false;
		try {
			inStr.available();
		} catch (IOException e) {
			
			System.out.println("IO EXCEPTION - Disconnected from arduino");
			//System.out.println("EXCEPTION SUBCLASS NAME: "+ .getClass().getName());
			error = true;
			//thread = new Thread(reconnect);
		//	thread.start();
			port.close();
			System.out.println("AFTER CLOSE");
		}
		
		if (error == false) {
			if (serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				if(debug){
					System.out.println("");
					System.out.print("SerialPort DataEvent");
				}
				try {
					int av = inStr.available();
					if(debug)
						System.out.print("Num AV: " + av + "; ");
					while (av > 0) {
						synchronized (buffer) {
							byte b = (byte) inStr.read();
							if(debug)
								System.out.print("; Val: " + ((int)b&0x000000ff));
							buffer.addLast(b);
							av = inStr.available();
						}
					}
					//if listener is enabled and we've gotten 4 packets...
					if(se!=null && buffer.size()>5){
						//call serial listener
						se.serialListener();
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

	public int available() {
		return buffer.size();
	}

	public boolean availableBool() {
		boolean temp = false;
		if (buffer.size() > 0) {
			temp = true;
		}
		return temp;
	}

	public int readInt() {
		// if (bufferIndex == bufferLast) return -1;
		// int outgoing=0;
		int val = -1;
		synchronized (buffer) {
			if (buffer.size() > 0) {
				val = (int)(buffer.pop()) & 0x000000ff;
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

	public void write(byte b) throws IOException, NotConnectedException {
		if (connected) {
			byte[] bb = new byte[1];
			bb[0] = b;
				outStr.write(bb);
			//	System.out.println("Sent byte");
		}else{
			throw new NotConnectedException("Not connected to arduino...");
		}
	}
}
