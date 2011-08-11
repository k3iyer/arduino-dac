package decatest;

import java.util.Timer;
import java.util.TimerTask;

import decatest.gui.TextBoxEvents;
import decatest.serial.SerialDriver;

/**
 * watchdog/heartbeat class. if this doesnt get "stroked" within 5 seconds of
 * the previous stroke, it will attempt to reconnect to the arduino in question
 * 
 * @author MIGIT
 * 
 */
public class WatchDog {
	Timer t;
	boolean started;
	String com;
	SerialDriver sd;
	/*
	 * the timer task is a task that has a delay before executing. if we don't
	 * stop this timer before the set value, the watchdog event is triggered and
	 * we try to reconnect to the specific Arduino
	 */
	TimerTask tt = new TimerTask() {
		@Override
		public void run() {
			// if this is not canceled before the time limit, it will enter this
			// run command
			System.out
					.println("~~~"
							+ sd.getCom()
							+ "~~~~~~~~~~~~~~~~~~~~~~~PROBLEM. WATCHDOG TIMER WENT OFF");
			TextBoxEvents.println(com + "'s WATCHDOG TRIGGERED!  OH NOESSSSSS");
			sd.reconnectToArd();
		}

	};

	/**
	 * create it
	 * 
	 * @param serial
	 */
	public WatchDog(SerialDriver serial) {
		this.sd = serial;
		this.com = serial.getCom();
		started = false;
		t = new Timer();
	}

	/**
	 * kill it
	 */
	public void killTimer() {
		if (started) {
			started = false;
			t.cancel();
		}
	}

	/**
	 * start it
	 */
	public void startTimer() {
		started = true;
		t = new Timer();
		t.schedule(tt, 5000); // run the task in 5 seconds
	}

}
