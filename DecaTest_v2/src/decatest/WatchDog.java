package decatest;

import java.util.Timer;
import java.util.TimerTask;

import decatest.gui.TextBoxEvents;
import decatest.serial.SerialDriver;

public class WatchDog {
Timer t;
boolean started;
String com;
SerialDriver sd;
TimerTask tt= new TimerTask(){
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//if this is not canceled before the time limit, it will enter this run command
		System.out.println("~~~"+sd.getCom()+"~~~~~~~~~~~~~~~~~~~~~~~PROBLEM. WATCHDOG TIMER WENT OFF");
		TextBoxEvents.println(com+"'s WATCHDOG TRIGGERED!  OH NOESSSSSS");
		sd.reconnectToArd();
	}
	
};
	public WatchDog(SerialDriver serial) {
		//t.schedule(tt, 2000); //run the task in 2 seconds
		this.sd=serial;
		this.com=serial.getCom();
		started=false;
		t= new Timer();
	}
	public void killTimer(){
		if(started){
			started=false;
			t.cancel();
		}
	}
	public void startTimer(){
		//System.out.println("RESTARTING WATCHDOG");
		started = true;
		//t=null;
		//this.killTimer();
		
		t= new Timer();
		t.schedule(tt, 5000); //run the task in 2 seconds
	}
	/*WatchDog x = new WatchDog();
	
	public static void main(String args[]){
		//	x.startTimer();
		new Thread(r).start();
		System.out.println("started");
	}
	Runnable r = new Runnable(){
		public void run(){
			for(int i = 0; i<=10; i++){
				System.out.println("i: "+ i);
				//x.killTimer();
				x.startTimer();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Last one now");
			x.startTimer();
		}
	};*/
}
