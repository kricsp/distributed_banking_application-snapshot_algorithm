import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

/**
* 
*
* @author  Surendrakumar Koneti
* @since   2017-11-16
*/

public class FileProcessor{

	private	FileReader file = null;
	private BufferedReader fileRead = null;
	private int lineIndex = 0;

	public FileProcessor(String fileName){
		
		try{
			file = new FileReader(fileName);
			fileRead = new BufferedReader(file);
		}
		catch(FileNotFoundException f){
			System.err.println(" file not found - " + fileName);
			System.exit(0); 
		}
	}

	/**
	* Reads target file and returns line by line
 	* @return line current line in the file pointed by file descriptor
	*/

	public String readLine(){
		String currentLine;
		String nextLine;
		try{
			currentLine = fileRead.readLine();
			if(currentLine == null){
				return null;
			}
			lineIndex++;
			return currentLine;
		}
		catch(IOException i){
			System.out.println(" Read Failed ");
		}
		return null;
	}

    	/**
     	* Closes target file
     	*/

    	public void close() {
			try{
        	    		fileRead.close();
			}
			catch(IOException i){
				System.err.println("close failed");
           		 System.exit(0); 
			}
    	}
}
	
