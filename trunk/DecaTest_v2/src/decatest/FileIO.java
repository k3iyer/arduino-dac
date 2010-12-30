package decatest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import decatest.gui.TextBoxEvents;
/**
 * This class is responsible for all fileIO
 * @author MIGIT
 *
 */
public class FileIO {
	//constants
	private static final String beginningPart = "data\\"; 
	private static final String runCountFileName = "runCount.info"; // contains the number of times the program has run. Used to handle restarts of test and increments files names between tests.
	private static final String fileCountFileName = "NumFiles.info"; //contains a running counter of the number of csv files saved so far
	private static final String bat1Folder = "battery1\\";
	private static final String bat2Folder = "battery2\\";
	private static final String endl = System.getProperty("line.separator");
	private String ardID;
	private String path;
	// private File file;
	// private BufferedWriter writer;
	private int fileNumber;
	private static int runCount;
static boolean initAlready=false;

	public FileIO(String ardID) {
		this.ardID = ardID;

		// init folder structure if it isn't present yet
		File f = new File("data");
		if (!f.exists()) {
			f.mkdir();
		}
		//initAlready is to prevent the program from creating separate test folders for each connected arduino
		//(this class is instantiated more than once, so this is required)
		if (initAlready==false){
			runCount = this.getRunCountFromFile();
			initAlready=true;
		}
		//amend path
		path = beginningPart + "Test_" + runCount;
		//path exist? if not, make it exist!
		f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		path = path + "\\Ard_" + this.ardID + "\\";
		//path exist? if not, make it exist!
		f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		//path exist? if not, make it exist!
		f= new File(path+bat1Folder);
		if (!f.exists()) {
			f.mkdir();
		}
		//path exist? if not, make it exist!
		f= new File(path+bat2Folder);
		if (!f.exists()) {
			f.mkdir();
		}
		
	}

	/**
	 * This method is called by the code to write both batteries data to a file.
	 * @param bat1
	 * @param bat2
	 */
	public void writeUnitToFile(LinkedList<String[]> bat1, LinkedList<String[]> bat2){
		writeDataToFile(bat1, this.getCurFile(1));
		writeDataToFile(bat2, this.getCurFile(2));
		incSaveToLocFile();
	}
	/**
	 * 
	 * @param strs - linked list of strings containing the collected battery data from the test
	 * @param f - file location to save it to
	 */
	public void writeDataToFile(LinkedList<String[]> strs, File f) {
		//debug text
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~WRITING DATA TO FILE!!!!!!!!!!11!~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Path to save file: " +f.getAbsolutePath()+"; listSize: "+ strs.size());
		String writeString = "";
		// tempTitles - once we have the packet format in place we'll know what to hard code to this section:
		int j;
		for (j = 0; j < strs.get(0).length - 1; j++) {
			writeString = writeString + "DataPoint" + j + ",";
		}
		writeString = writeString + "DataPoint" + j + endl;
		//grab one string array at a time from the linked list of string arrays and make one gigantic string out of it
		for (String[] strArray : strs) {
			int strArrayLength = strArray.length;
			int i;
			for (i = 0; i < strArrayLength-1; i++) {
				writeString = writeString + strArray[i] + ", ";
			}
			writeString = writeString + strArray[i] + endl;
		}
		//test code - print it to the screen.
		System.out.println(writeString);
		//output it to the file and close the file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.write(writeString);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	private int getCurFileNum() {
//		int returnVal = 0;
//		File f = new File(path + fileCountFileName);
//		if (f.exists()) {
//			returnVal = 1;
//			try {
//				returnVal = readFromFile(curFileName);
//				incNumInFile(curFileName, returnVal);
//				// System.out.println("After buffer writer...");
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} else {
//			try {
//				writeNumToFile(curFileName, 1);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//
//		return returnVal;
//	}
	/**
	 * Check the number of times we've ran the program and return a file object
	 * for the NEXT location to save data too 
	 * 
	 * input the battery number - we only
	 * want to increment this counter every other time it is called and
	 * depending on the battery number it will be saved to different places
	 */
	private File getCurFile(int batNum) {
		File f = new File(path + fileCountFileName);
		File curFile = null;
		try {
			int fileNumber = 1;
			if (f.exists()) {
				fileNumber = readFromFile(f);
			}
			//get the file path(dependent on what battery it pertains to)
			curFile = new File(path + ((batNum==1) ? bat1Folder : bat2Folder) + "process_" + fileNumber + ".csv");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return curFile;
	}
	
//CHANGE BATNUM TO REAL THING
	private void incSaveToLocFile() {
			File f = new File(path + fileCountFileName);
			int fileNumber = 1;
			try {
				if (f.exists()){
					fileNumber = readFromFile(f);
				}else{
					//file doesnt exist, use default value.
					System.out.println("NO FILE IN EXISTANCE, CREATING IT");
				}
				fileNumber++;
				writeNumToFile(f, fileNumber);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * change to private after jUnit tests will need resume code
	 * note: is this even used anymore?
	 * @return
	 */
	public int getRunCountFromFile() {
		int returnVal = 0;
		File f = new File(beginningPart + runCountFileName);
		// if the file exists, read the number from it. if not, create the file
		// and write the number 1 to it
		if (f.exists()) {
			try {
				returnVal = readFromFile(f);
				incNumInFile(f, returnVal);

			} catch (FileNotFoundException e) {
				// should never get here
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				writeNumToFile(f, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ++returnVal;
	}
/**
 * read the first int from the inputed file
 * @param f
 * @return
 * @throws NumberFormatException
 * @throws IOException
 */
	private int readFromFile(File f) throws NumberFormatException, IOException {
		int returnVal;
		BufferedReader br = new BufferedReader(new FileReader(f));
		returnVal = Integer.parseInt(br.readLine());
		// System.out.println("return value: " + returnVal);
		br.close();
		return returnVal;
	}
/**
 * increment the number in the file
 * @param f
 * @param num
 * @throws IOException
 */
	private void incNumInFile(File f, int num) throws IOException {
		BufferedWriter bw = (new BufferedWriter(new FileWriter(f)));
		Integer tempInt = num + 1;
		String temp = tempInt.toString();
		bw.write(temp);
		bw.close();
	}

	private void writeNumToFile(File f, Integer i) throws IOException {
		BufferedWriter bw = (new BufferedWriter(new FileWriter(f)));
		bw.write(i.toString());
		bw.close();
	}
/**
 * dont think this is used yet
 */
	public void clearSettingsAndData() {

		File f = new File(beginningPart + runCountFileName);
		f.delete();
		f = new File(beginningPart);
		f.delete();

	}
	/**
	 * This method will parse a scheduler file and return a linked list of SchedEntries to whatever called it
	 * @param sched
	 * @return
	 * @throws IOException
	 */
	public static LinkedList parseSchedFile(File sched) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(sched));
		String line=null;
		LinkedList<SchedEntry> schedule = new LinkedList<SchedEntry>();
		while((line=br.readLine())!=null){
			StringTokenizer st = new StringTokenizer(line,",");
			SchedEntry se = new SchedEntry();
			if(st.hasMoreTokens())
				se.progName = st.nextToken().trim();
			if(st.hasMoreTokens())
				se.num2Run  = Integer.parseInt(st.nextToken().trim());
			schedule.add(se);
		}
		br.close();
		return schedule;
	}
	/**
	 * check the inputed list for errors
	 * checks currently include ensuring there is a program file for each entry
	 * and ensuring the number of times to run the profile is greater than 0
	 * @param sched
	 * @return
	 */
	public static int checkSched(LinkedList<SchedEntry> sched){
		int size = sched.size();
		for(int line=0; line < size; line++){
			//this next part is checking to see if the program file named in the schedule actually exists
			String fileName = sched.get(line).progName + ".txt";
			String fileLoc = beginningPart+"program_files\\"+fileName;
			File testFile = new File(fileLoc);
			if(!testFile.canRead()){
				System.out.println("can't read file from line# #"+ line);
				return ++line; // if the file did not check out, program returns the line number where there is a problem
			}
			//if the number of times to run is less than 1, return an error
			if(sched.get(0).num2Run < 1){
				return ++line;	// if the file did not check out, program returns the line number where there is a problem
			}
		}
		//if file checks out, return -1 to state it works to the caller
		return -1;
	}
	static int lineNum = 0;
	/**
	 * This method parses a program file.  It excludes comments and parses the files based on the transition type
	 * 
	 * @param progFile - file object belonging to the progFile
	 * @return linked list of ProgEntries 
	 * @throws NumberFormatException
	 * @throws NoSuchElementException
	 * @throws IOException
	 */
	public static LinkedList parseProgramFile(File progFile)
			throws NumberFormatException, NoSuchElementException, IOException {
		// get number of lines in the file
		int numLines = numLinesInFile(progFile);
		// buffered reader for ease of input
		BufferedReader br = new BufferedReader(new FileReader(progFile));
		String line = null;
		// location to store the data
		LinkedList<ProgEntry> program = new LinkedList<ProgEntry>();
		// for each line in the file, read the line and store it
		for (int i = 0; i < numLines; i++) {
			line = br.readLine();
			lineNum++;
			log("LineNumber " + lineNum + ": " + line);
			// if it isn't a blank line or the line isn't a complete comment then read it in, otherwise skip it
			if ((line.trim().length() > 0) && (!line.startsWith("//"))) {
				StringTokenizer commentRemoval = new StringTokenizer(line, "//");
				String lineWithoutComment = commentRemoval.nextToken().trim();
				log("Line excluding Comments: " +lineWithoutComment);
				StringTokenizer st = new StringTokenizer(lineWithoutComment,
						",");
				ProgEntry pe = new ProgEntry();
				pe.transType = Integer.parseInt(st.nextToken().trim());
				switch (pe.transType) {
				
				// basic request - only 2 numbers
				case 0:
					if(st.hasMoreTokens())  // note you cant use nextToken as it could crash if there is no next token.
						pe.varType = Integer.parseInt(st.nextToken().trim());
					break;
				
				// variable update request or profile request
				case 1:
				case 2:
					if(st.hasMoreTokens())
						pe.varType = Integer.parseInt(st.nextToken().trim());
					if(st.hasMoreTokens())
						pe.data = Integer.parseInt(st.nextToken().trim());
					break;
				
				// end of test, save data to the file in this string
				case 16:
					if(st.hasMoreTokens())
						pe.strVal = st.nextToken().trim();
					break;
				default:
					// should never get here
					System.out.println("UNSUPPORTED TRANSMISSION TYPE");
					// TextBoxEvents.println("UNSUPPORTED TRANSMISSION TYPE");
					break;
				}
				program.add(pe);

			} else {
				// blank line
				log("BLANK LINE");
			}

		}
		br.close();
		return program;
	}
	
	/**
	 * check the program file for errors.  not very robust but it does some very small tests on the inputted progentry array
	 * @param PEs
	 * @return
	 */
	public static int checkProgFile(LinkedList<ProgEntry> PEs){
		int size = PEs.size(); //The size of the linked list.
		for(int i = 0; i< size; i++){
			ProgEntry p = PEs.get(i);
			if (p.transType !=16){
				if (p.transType>=8 || p.transType < 0){
					//PROBLEM WITH THIS LINEs TRANS TYPE
					return ++i;
				}
			}
			switch (p.transType){
			case 0:
				if (p.varType>=256 || p.varType < 0){
					return ++i;
				}
				break;
			case 1:
			case 2:
				if (p.varType>=256 || p.varType < 0){
					return ++i;
				}
				if (p.data>=65536 || p.data < 0){
					return ++i;
				}
				break;
			case 16: 
				if (p.strVal == null || p.strVal.length() ==0){
					return ++i;
				}
				break;
			default:
				//shouldnt get here
			}			
		}		
		//return -1 to tell the caller we've passed the test
		return -1;
	}
	/**
	 * Finds the number of lines in a file
	 * @param f  - file to check the number of lines of
	 * @return  the number of lines in the inputed file
	 */
	public static int numLinesInFile(File f){
		int returnVal= -2;  //if -1 or -2 at the end, there was a problem
		try {
			Scanner scanner = new Scanner(f);
			returnVal=0;
			//log("Starting count");
			while(scanner.hasNextLine()){
				scanner.nextLine();
				//log("ReturnVal: " + returnVal + ";");
				returnVal++;
			}			
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log("Final ReturnVal: " + returnVal + ";");
		return returnVal;
	}
private static void log(String str){
	System.out.println(str);
}
	/*
	 * methods for jUnit tests to get variables
	 */
	public int getFileNum() {
		return fileNumber;
	}

	public int getRunCountVar() {
		return runCount;
	}
}
