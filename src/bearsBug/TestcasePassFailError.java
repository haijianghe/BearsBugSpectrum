/**
 * 
 */
package bearsBug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author ccsu
 *
 */
public class TestcasePassFailError {
	
	//0=pass 1=failure 2=error 3=Skipped
	//return -1, read file error,or run testcase error.
	public static int getTestPassFailError(String filename)
	{
		int result = -1;
		try {
			File file = new File(filename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				
				String lineTxt = null;
				//运行过程中，会在文件中加入一些数据，不利于调试，所以改为此种方式。
				while ((lineTxt = br.readLine()) != null) 
				{
					if( !lineTxt.startsWith("Tests run:") )
						continue;
					//Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
					String[] parsed = lineTxt.split(",");
					int testsRun = -1,testFailures=-1,testErrors=-1,testSkipped=-1;
					if( parsed.length<4 )
						break;  //error
					String[] testItem = parsed[0].split(":");
					testsRun = Integer.valueOf(testItem[1].trim());
					if( testsRun!=1 )
						break; //error,   Tests run: 1
					testItem = parsed[1].split(":");
					testItem[0] = testItem[0].trim();
					if( !testItem[0].contentEquals("Failures") )
						break;//error
					testFailures = Integer.valueOf(testItem[1].trim());
					if( testFailures==0 )
					{}
					else if( testFailures==1 )
						result = 1;
					else
						break;//error
					testItem = parsed[2].split(":");
					testItem[0] = testItem[0].trim();
					if( !testItem[0].contentEquals("Errors") )
						break;//error
					testErrors = Integer.valueOf(testItem[1].trim());
					if( testErrors==0 )
					{}
					else if( testErrors==1 )
						result = 2;
					else
						break;//error
					testItem = parsed[3].split(":");
					testItem[0] = testItem[0].trim();
					if( !testItem[0].contentEquals("Skipped") )
						break;//error
					testSkipped = Integer.valueOf(testItem[1].trim());
					if( testSkipped==0 )
					{}
					else if( testSkipped==1 )
						result = 3;
					else
						break;//error
					if( testFailures==0 && testErrors==0 && testSkipped==0 )
						result = 0;
					break;
				}//end of while
				read.close();
			}//end of if(file.isFile() && 
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}
		return result;
	}
}
