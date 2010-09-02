package decatest.serial.packet;


public interface DacPacketEvent {
 //public void onNewData(DacPacket packet);
 public void onWatchDogData(WatchDogPacket packet);
}
