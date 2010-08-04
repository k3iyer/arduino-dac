package decatest.gui;

import decatest.DecaTest;

public class StatusLight {
	private int x1, y1, w, h;
	private Status st;
	private DecaTest dt;
	public enum Status {
		GREEN, RED, YELLOW, BLINK, DISABLED;
	}

	public StatusLight(DecaTest dt, int x1, int y1, int w, int h) {
		st = Status.DISABLED;
		this.x1 = x1;
		this.y1 = y1;
		this.w = w;
		this.h = h;
		this.dt=dt;
	}
	public void setStatus(Status status){
		st=status;
		switch (status){
		case GREEN:
			dt.fill(50,205,50);
			dt.rect(x1, y1, w, h);
			break;
		case RED:
			dt.fill(255,0,0);
			dt.rect(x1, y1, w, h);
			break;
		case YELLOW:
			dt.fill(255,215,0);
			dt.rect(x1, y1, w, h);
			break;
		case DISABLED:
			dt.fill(DecaTest.backR,DecaTest.backG,DecaTest.backB);
			dt.rect(x1, y1, w, h);
			break;
		default:
			break;	
		}
	}

}
