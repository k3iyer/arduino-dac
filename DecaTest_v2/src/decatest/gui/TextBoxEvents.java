package decatest.gui;

import controlP5.ControlP5;
import controlP5.Textarea;
import decatest.DecaTest;
/**
 * this class is exactly like System.out.println, except it outputs to the black console inside the GUI
 * @author MIGIT
 *
 */
public abstract class TextBoxEvents {
static private Textarea textbox;
static private boolean started = false;
	public static void init(ControlP5 cp5){
		
		textbox = cp5.addTextarea("box", "", (DecaTest.screenWidth / 3) * 2, 5,
				(DecaTest.screenWidth / 3), DecaTest.screenHeight / 2 + 5);
		// textbox.showScrollbar();
		//without this next line it blinks like mad. no clue why!
		textbox.enableColorBackground();
		started = true;
	}
	/**
	 * amends the inputted LINE to the console
	 * @param str
	 */
	public static void println(String str){
		if(started)
			textbox.setText(textbox.text() + "\n" + str);
	}
	/**
	 * amend the inputed string to the console
	 * @param str
	 */
	public static void print(String str){
		if(started)
			textbox.setText(textbox.text() + str);
	}
	public static String getContents(){
		return textbox.text();
	}
	/**
	 * sets the text to whatever you want
	 * NOTE - clears the previous text when calling this
	 * @param str
	 */
	public static void setText(String str){
		textbox.setText(str);
	}
	
}
