package decatest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class FileIO {
	private static final String beginningPart = "data\\";
	private static final String runCountFileName = "runCount.info";
	private static final String fileCountFileName = "NumFiles.info";
	private static final String bat1Folder = "battery1\\";
	private static final String bat2Folder = "battery2\\";
	private static final String endl = System.getProperty("line.separator");
	private String ardID;
	private String path;
	// private File file;
	// private BufferedWriter writer;
	private int fileNumber;
	private File curFileName;
	private static int runCount;
static boolean initAlready=false;
	public FileIO(String ardID) {
		this.ardID = ardID;

		// init folder structure if it isn't present yet
		File f = new File("data");
		if (!f.exists()) {
			f.mkdir();
		}
		//initAlready is to prevent the program from creating separate test folders for each arduino
		if (initAlready==false){
			runCount = this.getRunCountFromFile();
			initAlready=true;
		}
		
		// System.out.println(this.getRunCount());
		path = beginningPart + "Test_" + runCount;
		
		f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		path = path + "\\Ard_" + this.ardID + "\\";
		
		f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		f= new File(path+bat1Folder);
		if (!f.exists()) {
			f.mkdir();
		}
		f= new File(path+bat2Folder);
		if (!f.exists()) {
			f.mkdir();
		}
		// curFileName = new File(path + fileCountFileName);

		// int temp = ;
		// if (temp != 0) {
		// fileNumber = temp;
		// }
	}

	// public void openFile() {
	// file = new File(path + "entry_" + fileNumber + ".csv");
	// try {
	// writer = new BufferedWriter(new FileWriter(file, true));
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	// public void amendCurrentFile(String str) {
	// try {
	// writer.write(str);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
public void writeUnitToFile(LinkedList<String[]> bat1, LinkedList<String[]> bat2){
	writeDataToFile(bat1, this.getCurFile(1));
	writeDataToFile(bat2, this.getCurFile(2));
	incSaveToLocFile();
}
	public void writeDataToFile(LinkedList<String[]> strs, File f) {
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~WRITING DATA TO FILE!!!!!!!!!!11!~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		// find currentFilenNumber
		//File saveToLoc = this.getCurFile(batteryNum);
		System.out.println("Path to save file: " +f.getAbsolutePath()+"; listSize: "+ strs.size());
		// update file to point to the next file
		//incSaveToLocFile(batteryNum);
		String writeString = "";
		// tempTitles:
		int j;
		for (j = 0; j < strs.get(0).length - 1; j++) {
			writeString = writeString + "DataPoint" + j + ",";
		}
		writeString = writeString + "DataPoint" + j + endl;

		for (String[] strArray : strs) {
			int strArrayLength = strArray.length;
			//System.out.print("Length: "+ strArray.length);
			int i;
			for (i = 0; i < strArrayLength-1; i++) {
			//	System.out.print("; i=" + i);
				writeString = writeString + strArray[i] + ", ";
			}
			//System.out.println(" i=" + i);
			writeString = writeString + strArray[i] + endl;
		}
		System.out.println(writeString);
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
	 * 
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

	private int readFromFile(File f) throws NumberFormatException, IOException {
		int returnVal;
		BufferedReader br = new BufferedReader(new FileReader(f));
		returnVal = Integer.parseInt(br.readLine());
		// System.out.println("return value: " + returnVal);
		br.close();
		return returnVal;
	}

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

	public void clearSettingsAndData() {

		File f = new File(beginningPart + runCountFileName);
		f.delete();
		f = new File(beginningPart);
		f.delete();

	}
	public static LinkedList parseSchedFile(File sched) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(sched));
		String line=null;
		LinkedList<SchedEntry> schedule = new LinkedList<SchedEntry>();
		while((line=br.readLine())!=null){
			StringTokenizer st = new StringTokenizer(line,",");
			SchedEntry se = new SchedEntry();
			se.progName = st.nextToken().trim();
			se.num2Run  = Integer.parseInt(st.nextToken().trim());
			schedule.add(se);
		}
		
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
			String fileName = sched.get(line).progName + ".txt";
			String fileLoc = beginningPart+"program_files\\"+fileName;
			File testFile = new File(fileLoc);
			//if the file cannot be read or doesnt exist, return the problem line number
			if(!testFile.canRead()){
				System.out.println("can't read file from line# #"+ line);
				return ++line;
			}
			//if the number of times to run is less than 1, return an error
			if(sched.get(0).num2Run < 1){
				return ++line;
			}
		}
		//file checks out, return -1 to state it works
		return -1;
	}
	public static LinkedList readProgramFile(File progFile) throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(progFile));
		String line=null;
		LinkedList<ProgEntry> program = new LinkedList<ProgEntry>();
			while((line=br.readLine())!=null){ //blank lines... how does this handle that
				
				StringTokenizer commentRemoval = new StringTokenizer(line, "//");
				//should return the line up to a "//", or the entire line if that isnt present
				String lineWithoutComment = commentRemoval.nextToken().trim();
				StringTokenizer st = new StringTokenizer(lineWithoutComment,",");
				ProgEntry pe = new ProgEntry();
				pe.transType = Integer.parseInt(st.nextToken().trim());
				switch(pe.transType){
				case 0:
					pe.varType = Integer.parseInt(st.nextToken().trim());
					break;
				case 1:
					pe.varType = Integer.parseInt(st.nextToken().trim());
					pe.data = Integer.parseInt(st.nextToken().trim());
					break;
				case 2:
					pe.strVal = st.nextToken().trim();
					break;
				default:
					//should never get here
					break;
				}
				program.add(pe);
			}
		return program;
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
