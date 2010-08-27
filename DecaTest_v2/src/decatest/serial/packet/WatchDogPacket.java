package decatest.serial.packet;

public class WatchDogPacket{// extends DacPacket {
	public int id;
	public int mode;
	public int batID;
	public int time1;
	public int time2;
	public String[] values;
	public WatchDogPacket(int[] data) {
		values = new String[6];
		this.id		= data[0];//ardId
		this.batID	= data[1];//batId
		this.mode	= data[2];//mode
		
		
		/* make this a variable length*/
		values[0]	= Integer.toString(data[3]);
		values[1]	= Integer.toString(data[4]);
		values[2]	= Integer.toString(data[5]);
		values[3]	= Integer.toString(data[6]);
		values[4]	= Integer.toString(data[7]);
		values[5]	= Integer.toString(data[8]);
		this.time1	= data[9];
		this.time2	= data[10];
	}
}
