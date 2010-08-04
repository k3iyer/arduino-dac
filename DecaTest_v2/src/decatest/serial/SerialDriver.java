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
	private static final boolean debug=false;
	InputStream inStr;
	public void setInputStream(InputStream is){inStr=is;}
	OutputStream outStr;
	public void setOutputStream(OutputStream os){outStr=os;}
	// byte buffer[] = new byte[32768];
	int bufferIndex;
	int bufferLast;
	SerialPort port;
	public void setPort(SerialPort p){port=p;}
	int rate;
	public int getRate(){return rate;}
	static final int parity =SerialPort.PARITY_NONE;
	static final int databits=8;
	static final int stopbits=SerialPort.STOPBITS_1;
	String com;
	public String getCom(){return com;}
	static int numTimesInPortConnect = 0;
	boolean monitor = false;
	public boolean connected = false;
	volatile LinkedList<Byte> buffer;
	Thread thread;
	Serial_Event se;
	//StatusLight statusLight;
	//static AvalComs aCom;
//	public SerialDriver(String com, int rate) {
//		sInit(com,rate);
//	}
	public SerialDriver(Serial_Event se, String com, int rate) {
		this.se=se;
		//this.statusLight=sl;
		//aCom=new AvalComs();
		
		sInit(com,rate);
		
	}
	private void sInit(String com, int rate){
		//System.out.println("SerialDriver Init");
		this.com = com;
		this.rate = rate;
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
				portConnect(portId, this);
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
static boolean pause=false;
/**
 * need to pass in itself since this only forwards the command to the static method
 * 
 * @param pID
 * @throws PortInUseException
 * @throws IOException
 * @throws UnsupportedCommOperationException
 * @throws TooManyListenersException
 */
private void portConnect1(CommPortIdentifier pID) throws PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException{
	portConnect(pID, this);
}
/**
 * Had issues with this method not being static or synchronized.  Only one thread is allowed in here at a time
 * because we were originally having issues with the drivers not being able to support
 * multiple threads trying to connect at the same time. 
 * @param portId
 * @param sd  --this needs to use a lot of class variables - pass in this
 * @throws PortInUseException  ---something else is already using this port.
 * @throws IOException
 * @throws UnsupportedCommOperationException 
 * @throws TooManyListenersException  ---multiple listeners attached to the same object
 */
	synchronized static private void portConnect(CommPortIdentifier portId, SerialDriver sd )
			throws PortInUseException, IOException,
			UnsupportedCommOperationException, TooManyListenersException {
		sd.setPort(null);
		// portId.removePortOwnershipListener("Serializationlol");
		// portId.
		
		pause=true;
		System.out.print("com: "+ sd.getCom() + "IN PORT CONNECT. port.open; ");
		SerialPort tempPort = (SerialPort) portId.open("Serializationlol",2000);
		System.out.print("port.getIn; ");
		sd.setInputStream(tempPort.getInputStream());
		System.out.print("port.getout; ");
		sd.setOutputStream(tempPort.getOutputStream());
		System.out.println("port.restOfIt; ");
		tempPort.setSerialPortParams(sd.getRate(), databits, stopbits, parity);
		tempPort.addEventListener(sd); //tell the dll where the SerialEvent method is located
		tempPort.notifyOnDataAvailable(true); //driver will call the serialEvent method on dataAv
		
		sd.setPort(tempPort);
		pause=false;
		
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
						portConnect1(portId);
						connected=true;
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
			error = false;
		}
	};
public void reconnectToArd(){
	error=true;
	
	thread= new Thread(reconnect);
	thread.start();
	try{
		port.notifyOnDataAvailable(false);
		port.removeEventListener();
		port.close();
		port=null;
	}catch(NullPointerException e){
		System.out.println(com+" is trying to close a port that is already closed.");
	}
}
/**
 * for testing purposes only
 */
public void killPort(){
	port.close();
	port=null;
}
boolean error = false;
	@Override
	synchronized public void serialEvent(SerialPortEvent serialEvent) {
		//error=false;
		
		try {
			inStr.available();
		} catch (IOException e) {
			
			System.out.println("IO EXCEPTION - Disconnected from arduino");
			//System.out.println("EXCEPTION SUBCLASS NAME: "+ .getClass().getName());
			error = true;
			//thread = new Thread(reconnect);
		//	thread.start();
			port.close();
		//	System.out.println("AFTER CLOSE");
		}
		
		if (!error && !pause) {
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
