package decatest.serial;

import java.io.IOException;

import decatest.serial.packet.DacPacket;
import decatest.serial.packet.DacPacketEvent;
//need a method to cross reference the packet type bit with the buffer size
import decatest.serial.packet.WatchDogPacket;

/**
 * Implements Serial_Event. The serial listener will be called when we get a new
 * packet which is greater than X digits long
 * 
 * @author MIGIT
 * 
 */
public class DacConnection implements Serial_Event {
	
	enum ARD_MODE{
		IDLE((byte)0),
		PROFILE1((byte)10);
		private byte m;
		ARD_MODE(byte b){m=b;}
		public byte getMode() {return m;}
	}
	//private DacPacket dacData;
	private int[] curData;
	// private boolean validData;
	public SerialDriver sd;
	private DacPacketEvent parent;

	static final int[] packetSize = { 0,4,0,0,0,0,0,0,0,0,0,0,0 };

	public DacConnection(DacPacketEvent wpe, String com) {
		//System.out.println("DAC Con Init");
		sd = new SerialDriver(this, com, 115200);
		curData = new int[128];
		parent = wpe;
		// new Thread(translateData).start();
		// validData=false;
	}

	public void sendByte(byte b) {
		try {
			sd.write(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
	}
/**
 * change the mode of the arduino
 */
	public void sendMode(ARD_MODE m){
		sendByte((byte)0xAC);
		sendByte(m.getMode());
		sendByte((byte)0xff);
	}
	/**
	 * monitor the serial connection for incoming packets when we receive a
	 * packet, make a WiiPacket out of it and ship it to the parent
	 * 
	 */
	// int startByte;
	//int origStartByte;

	@Override
	public void serialListener() {
		// TODO Auto-generated method stub
		System.out.println(sd.getCom()+"---DAC_CON Serial Listener called");
		int startByte;
		startByte = sd.readInt();
		while ((startByte & 0x000000F0) >>> 4 != 0xA) { // until the higher 4 bits of the start
									// packet = 0xA
			if (sd.available() == 0) { // stuck in a while loop here - add
										// an exit condition
				break;
			}
		}
		// validData = false;
		// read in the three values
		byte packetMode = getPacketMode(startByte);
		System.out.println("PacketMode: "+ packetMode);
		for (int i = 0; i < packetSize[packetMode]; i++) {
			curData[i] = sd.readInt();
		}
		int lastBit = sd.readInt();
		//System.out.println(sd.getCom()+ "---Last Bit of packetMode is: "+ lastBit);
		if (lastBit == 255) {// end of packet value which we should
									// receive. if not dont create a packet
			switch (packetMode) {
			case 1:// watchdog
				parent.onWatchDogData(new WatchDogPacket(curData));
				break;
			default:
				System.out.println("INVALID PACKET MODE");
				break;
			}

		}else{
			
			// else incomplete packet. throw it out.
		}

	}

//	private boolean getStartPacket() {
//		origStartByte = sd.readInt();
//		
//		System.out.println("OrigStartByte: " + origStartByte + "; after math: "+ (int)((origStartByte & 0x000000F0) >>> 4));
//		// startByte=origStartByte;
//		// startByte = ;
//		if ((origStartByte & 0x000000F0) >>> 4 == 0xA) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	private byte getPacketMode(int firstPacket) {
		return (byte) (firstPacket & 0x0000000F);
	}

}
