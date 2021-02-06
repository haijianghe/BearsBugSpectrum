/**
 * 
 */
package common;

import java.io.File;

/**
 * @author ccsu
 *
 */
public class ProjectInfoFile {
	/**1，若文件(project).testcase不存在，则生成它。并且添加表头信息。
	 * 2，将当前bugid对应的测试用例情况，存储到文件。
	 * @return
	 */
	public static boolean processTestCaseFile(String projectName,int bugid,int tcPassed,int tcFailed)
	{
		boolean result = true;
		String filename = ProjectConfiguration.MyWorkDir+"/"+projectName+"/"+projectName+".testcase";
		try {
			File file = new File(filename);
			if ( !file.exists() ) { //文件不存在
					StringBuilder sb = new StringBuilder();
					sb.append("ver_numbers      ");
					sb.append( "300" );
					sb.append( "\n" );
					sb.append("testcase   0000\n");
					sb.append("version    passed    failed\n");
					FileToolkit.OutputToFile(file,sb.toString(),true);
			}
			if ( file.exists() && file.isFile() ) { //文件存在
				String strInfo = "      v"+bugid+"      "+tcPassed+"      "	+tcFailed+"\n";
				//String strInfo = "      v"+bugid+"      "+0+"      "+0+"\n"; //for test.
				FileToolkit.OutputToFile(file, strInfo, true);
			}//end of 文件存在
			else
				result = false;
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	/**1，若文件(project)_order.bugid不存在，则生成它。并且添加表头信息。
	 * 2，将当前bugid对应的order，存储到文件。
	 * @return
	 */
	public static boolean processBugidOrderFile(String projectName,int bugid,int order)
	{
		boolean result = true;
		String filename = ProjectConfiguration.MyWorkDir+"/"+projectName+"/"+projectName+"_order.bugid";
		try {
			File file = new File(filename);
			if ( !file.exists() ) { //文件不存在
					FileToolkit.OutputToFile(file,"This file lists the corresponding relation between bugid and order(natural number serial). \n",true);
					FileToolkit.OutputToFile(file,"bugid     order\n",true);
			}
			if ( file.exists() && file.isFile() ) { //文件存在
				String strInfo = "    "+bugid+"      "+order+"\n";
				FileToolkit.OutputToFile(file, strInfo, true);
			}//end of 文件存在
			else
				result = false;
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

}
