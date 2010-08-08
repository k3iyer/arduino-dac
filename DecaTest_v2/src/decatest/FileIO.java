package decatest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class FileIO {
	private static final String beginningPart = "data\\";
	private static final String runCountFileName = "runCount.info";
	private static final String fileCountFileName = "NumFiles.info";
	private static final String endl = System.getProperty("line.separator");
	private String ardID;
	private String path;
	// private File file;
	// private BufferedWriter writer;
	private int fileNumber;
	private File curFileName;
	private int runCount;

	public FileIO(String ardID) {
		this.ardID = ardID;

		// init folder structure if it isn't present yet
		File f = new File("data");
		if (!f.exists()) {
			f.mkdir();
		}
		runCount = this.getRunCountFromFile();
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

	public void writeDataToFile(LinkedList<String[]> strs) {
		// find currentFilenNumber
		File saveToLoc = this.getCurFile();
		// update file to point to the next file
		incSaveToLocFile();
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
				System.out.print("; i=" + i);
				writeString = writeString + strArray[i] + ", ";
			}
			System.out.println(" i=" + i);
			writeString = writeString + strArray[i] + endl;
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveToLoc));
			bw.write(writeString);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int getCurFileNum() {
		int returnVal = 0;
		File f = new File(path + fileCountFileName);
		if (f.exists()) {
			returnVal = 1;
			try {
				returnVal = readFromFile(curFileName);
				incNumInFile(curFileName, returnVal);
				// System.out.println("After buffer writer...");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				writeNumToFile(curFileName, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return returnVal;
	}

	private File getCurFile() {
		File f = new File(path + fileCountFileName);
		File curFile = null;
		try {
			int fileNumber = 1;
			if (f.exists()) {
				fileNumber = readFromFile(f);
			}
			curFile = new File(path + "process_" + fileNumber + ".csv");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return curFile;
	}

	private void incSaveToLocFile() {
		File f = new File(path + fileCountFileName);
		int fileNumber = 0;
		try {
			if (f.exists())
				fileNumber = readFromFile(f);
			writeNumToFile(f, ++fileNumber);
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
