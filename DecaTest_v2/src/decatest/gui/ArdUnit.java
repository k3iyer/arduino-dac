package decatest.gui;

import decatest.DecaTest;
import decatest.SerialLink;
import decatest.gui.StatusLight.Status;

public class ArdUnit {
	private int x1, y1, id;
	private static final int width=125;
	private final static int height=75;
	private DecaTest dt;
	private StatusLight sLight;
	public SerialLink sLink;
	private String com;
	public Thread t;
	//private String btnStr;
	controlP5.Button b;
	public ArdUnit(DecaTest dt, String str, int x1, int y1) {
		this.x1=x1;
		this.y1=y1;
//		this.width = w;
//		this.height = h;
		this.dt = dt;
		this.com=str.toUpperCase();
	}
	public void drawGUI(int id){
		this.id=id;
		dt.fill(139,137,137);
		dt.rect(x1, y1, width, height);
		dt.cp5.addTextlabel("ard1", "Ard"+id, x1+2, y1+2);
		dt.cp5.addButton("btn", 0, x1+3, y1+height-15, width/2, 10).setId(this.id);
		dt.fill(238,238,224);
		
		sLight=new StatusLight(dt, x1+width-12, y1+2, 10,10);
		sLight.setStatus(Status.DISABLED);
		
	}
	public void SerialConnect(){
		sLink=new SerialLink(sLight, com);
		t=new Thread(sLink);
		t.start();
	}
	public void bEvent(){
		System.out.println("Ard id# "+id+" event handled");
		//sLight.setStatus(Status.RED);
		try {
			sLink.writeData(0x64);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
