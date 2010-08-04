package decatest.gui;

import controlP5.ControlP5;
import controlP5.Textarea;
import decatest.DecaTest;

public abstract class TextBoxEvents {
static private Textarea textbox;
static private boolean started = false;
	public static void init(ControlP5 cp5){
		
		textbox = cp5.addTextarea("box", "", (DecaTest.screenWidth / 3) * 2, 5,
				(DecaTest.screenWidth / 3), DecaTest.screenHeight / 2 + 5);
		// textbox.showScrollbar();
		textbox.enableColorBackground();
		started = true;
	}
	public static void println(String str){
		if(started)
			textbox.setText(textbox.text() + "\n" + str);
	}
	public static void print(String str){
		if(started)
			textbox.setText(textbox.text() + str);
	}
	public static String getContents(){
		return textbox.text();
	}
	public static void setText(String str){
		textbox.setText(str);
	}
	
}
