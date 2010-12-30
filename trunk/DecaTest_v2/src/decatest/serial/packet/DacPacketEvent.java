package decatest.serial.packet;

/**
 * an interface to pass into the lower tier as a callback method
 * @author MIGIT
 *
 */
public interface DacPacketEvent {
 //public void onNewData(DacPacket packet);
 public void onWatchDogData(WatchDogPacket packet);
}
