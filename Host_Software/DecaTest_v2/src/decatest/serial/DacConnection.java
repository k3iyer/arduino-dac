package decatest.serial;

import java.io.IOException;

import decatest.gui.StatusLight;
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
	
	//dead code
//	enum ARD_MODE{
//		IDLE((byte)0),
//		PROFILE1((byte)10);
//		private byte m;
//		ARD_MODE(byte b){m=b;}
//		public byte getMode() {return m;}
//	}
//static final int[] packetSize = { 0,4,0,0,0,0,0,0,0,0,0,0,0 };
	
	private int[] curData;
	public SerialDriver sd;
	private DacPacketEvent parent;

	

	public DacConnection(DacPacketEvent wpe, String com) {
		//System.out.println("DAC Con Init");
		sd = new SerialDriver(this, com, 115200);
		curData = new int[128];
		parent = wpe;
	}
	/**
	 * send the inputed byte the the serial driver class which will send it to the arduino
	 * @param b
	 */
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
	//dead code
///**
// * change the mode of the arduino
// */
//	public void sendMode(ARD_MODE m){
//		sendByte((byte)0xAC);
//		sendByte(m.getMode());
//		sendByte((byte)0xff);
//	}
	/**
	 * monitor the serial connection for incoming packets when we receive a
	 * packet, make a WiiPacket out of it and ship it to the parent
	 * 
	 */
	@Override
	public void serialListener(int endPacketLoc) {
		// TODO Auto-generated method stub
		System.out.println(sd.getCom()+"---DAC_CON Serial Listener called");
		int startByte;
		//read the first byte and subtract 1 from the end packet location as it has just moved
		startByte = sd.readInt();
		endPacketLoc-=1;
		byte packetMode = getPacketMode(startByte);
		System.out.println("PacketMode: "+ packetMode);
		
		int readUntil = endPacketLoc-SerialDriver.endTrans.length;
		curData = new int[readUntil];
		for (int i = 0; i < readUntil; i++) {
			curData[i] = sd.readInt();
		}
		//delete the endTransmit codes from the buffer
		for(int i = 0; i<SerialDriver.endTrans.length; i++){
			sd.readInt();
		}
		//so far only 1 packet mode.  might change later on
			switch (packetMode) {
			case 1:// watchdog
				parent.onWatchDogData(new WatchDogPacket(curData));
				break;
			default:
				System.out.println("INVALID PACKET MODE - SOME SORT OF ERROR OCCURED");
				break;
			}
	}

	
	private byte getPacketMode(int firstPacket) {
		return (byte) (firstPacket & 0x0000000F);
	}

}
