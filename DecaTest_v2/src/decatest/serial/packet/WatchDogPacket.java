package decatest.serial.packet;

public class WatchDogPacket{// extends DacPacket {
	public int id;
	public int mode;
	public int time1;
	public int time2;
	public WatchDogPacket(int[] data) {
		this.id=data[0];
		this.mode=data[1];
		this.time1=data[2];
		this.time2=data[3];
		
	}
}
