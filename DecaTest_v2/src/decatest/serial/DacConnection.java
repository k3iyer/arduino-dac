package decatest.serial;

import java.io.IOException;

/**
 * Implements Serial_Event.  The serial listener will be called when we get a new packet which is greater than X digits long
 * @author MIGIT
 *
 */
public class DacConnection implements Serial_Event {
	private DacPacket dacData;
	private int[] curData;
	//private boolean validData;
	public SerialDriver sd;
	private DacPacketEvent parent;

	public DacConnection(DacPacketEvent wpe, String com) {
		sd = new SerialDriver(this, com, 115200);
		curData = new int[3];
		parent = wpe;
		//new Thread(translateData).start();
		//validData=false;
	}
	public void sendByte(byte b){
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
 * monitor the serial connection for incoming packets
 * when we receive a packet, make a WiiPacket out of it and ship it to the parent
 * 
 */
//	private Runnable translateData = new Runnable() {
//		@Override
//		public void run() {
//			while (true) {
//				if (sd.available() > 4) { //hopefully a complete packet is ready for processing
//					while (sd.readInt() != 1) { // 1 marks the start of a new packet
//						if (sd.available() == 0) { //stuck in a while loop here - add an exit condition
//							break;
//						}
//					}
//					validData=false;
//					//read in the three values
//					for(int i=0; i<3; i++){
//						curData[i]=sd.readInt();
//					}
//					if(sd.readInt()==255){//end of packet value which we should recieve
//						wiiData= new WiiPacket(curData[0],curData[1],(byte)curData[2]);
//						parent.onNewData(wiiData);
//					}//else incomplete packet.  throw it out.
//						
//				}
//				// parent.onNewData(curData);
//			}
//
//		}
//
//	};

	@Override
	public void serialListener() {
		// TODO Auto-generated method stub
		if (sd.available() > 4) { // hopefully a complete packet is ready for
									// processing
			while (sd.readInt() != 1) { // 1 marks the start of a new packet
				if (sd.available() == 0) { // stuck in a while loop here - add
											// an exit condition
					break;
				}
			}
			//validData = false;
			// read in the three values
			for (int i = 0; i < 3; i++) {
				curData[i] = sd.readInt();
			}
			if (sd.readInt() == 255) {// end of packet value which we should
										// recieve
				dacData = new DacPacket(curData[0], curData[1],
									   (byte) curData[2]);
				parent.onNewData(dacData);
			}// else incomplete packet. throw it out.
		}
	}

}
