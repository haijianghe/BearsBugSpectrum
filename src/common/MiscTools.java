package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MiscTools {
	/** 执行Shell命令 注意：有 > ，输出内容会保存到指定文件，而不会输出到console
	 * @param shellCommand
	 * @return 0 成功；otherwise 失败
	 */
	public static int runShellCommand(String shellCommand)
	{
		Process process;
        BufferedReader br;
        int returnCode = -1;
        try {
  	      	//执行命令
        	String[] command = {"/bin/bash","-c",shellCommand}; 
			process = Runtime.getRuntime().exec(command);
			//用缓冲器读行    
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String strLine=null;    
            //直到读完为止    
            while((strLine=br.readLine())!=null)    
            {    
                System.out.println(strLine);    
            }    
	        br.close();
	        returnCode = process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return returnCode;    
	}
	
	/**
	 * @return
	 */
	private static String getBearsMavenEnvironmentString()
	{
		String strEnvp = "export JAVA_HOME=/home/ccsu/jdk1.8.0 && "+
		         "export PATH=$JAVA_HOME/bin:$PATH && "+
		         "export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar && ";
		return strEnvp;
	}
	
	/**  执行Maven command , and write result to file : filename.
	 * @param mavenCommand  Maven命令
	 * @param redirect 是否保存d4j命令执行的结果到文件  重定向指令(存储shell命令执行的结果)
	 * @param redirectFilename 存储d4j命令执行的结果 d4j需要的执行结果文件名，要加入目录信息。
	 * @return
	 */
	private static int runMavenEnvironmentCommandBase(String mavenCommand,boolean redirect,String redirectFilename)
	{
		Process process;
        BufferedReader br;
        int returnCode = -1;

        try {
        	String command = getBearsMavenEnvironmentString() + mavenCommand;
        	if( redirect )
				command = command+" >& "+ redirectFilename;
        	String[] arycommand = {"/bin/bash","-c",command}; 
       		process = Runtime.getRuntime().exec(arycommand);
			
			//用缓冲器读行    
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String strLine=null;    
            //直到读完为止    
            while((strLine=br.readLine())!=null)    
            {    
        		//System.out.println(strLine);
            }    
	        br.close();
	        returnCode = process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return returnCode;    
	}
	
	/** 重定向输出  >&可以，而>通不过
	 * 该方法虽然调试通过，但是调用此方法后，导致runD4jsCommand失败。   失败原因，rtnCode=1；没有执行权限
	 * @param mavenCommand
	 * @param shellFilename 存储SHELL命令的执行结果。重定向输出文件,不要加入目录信息。
	 * @return
	 */
	public static int runMavenRedirect(String mavenCommand,String shellFilename)
	{
		return runMavenEnvironmentCommandBase(mavenCommand,true,ProjectConfiguration.DatasetStoreDirectory+"/"+shellFilename);
	}

	/** 重定向输出  >&可以，而>通不过
	 * 该方法虽然调试通过，但是调用此方法后，导致runD4jsCommand失败。   失败原因，rtnCode=1；没有执行权限
	 * @param mavenCommand
	 * @return
	 */
	public static int runMavenCommand(String mavenCommand)
	{
		return runMavenEnvironmentCommandBase(mavenCommand,false,null);
	}
	
	/** get project's bug work directory
	 * @param object object name
	 * @return
	 */
	public static String getBuggyWorkDir(String object)
	{
		return ProjectConfiguration.DatasetStoreDirectory+"/"+object+"_buggy";
	}

	/** get project's fixed work directory
	 * @param object object name
	 * @return
	 */
	public static String getFixedWorkDir(String object)
	{
		return ProjectConfiguration.DatasetStoreDirectory+"/"+object+"_fixed";
	}

	//return:  coverage.xml and its path.
	public static String getCoverageXMLFilename(String bearsProjectItem)
	{
		return ProjectConfiguration.BearsBugRawDir+"/"+bearsProjectItem+"/target/site/cobertura/coverage.xml";
	}

	
	/** 创建<project>,info,profile,v(xxx),buggy,fixed等文件夹。存储生成的谱数据及错误定位相关的.java文件
	 * @return
	 */
	public static boolean mkdirSpectrumDirectory(String projectName,int bugid)
	{
		String dirProject = ProjectConfiguration.DatasetStoreDirectory+"/"+projectName;
		if( !createDirectory(dirProject) )
			return false;
		String directory = dirProject+"/info";
		if( !createDirectory(directory) )
			return false;
		directory = dirProject+"/profile";
		if( !createDirectory(directory) )
			return false;
		directory = dirProject+"/SourceCode";
		if( !createDirectory(directory) )
			return false;
		String version = "/SourceCode"+"/v"+bugid;
		directory = dirProject + version;
		if( !createDirectory(directory) )
			return false;
		directory = dirProject+version+"/buggy";
		if( !createDirectory(directory) )
			return false;
		directory = dirProject+version+"/fixed";
		if( !createDirectory(directory) )
			return false;
		return true;
	}
		
	/** 创建1个目录
	 * @param directory  绝对路径名
	 * @return true, 文件夹创建成功或者该文件夹已经存在
	 */
	private static boolean createDirectory(String directory)
	{
		boolean result = true;
		File file = new File(directory);
		if ( !file.exists() )
		{	//directory is not exist
			String shellCommand = "mkdir "+directory;
			int rtnCode = MiscTools.runShellCommand(shellCommand);
			if( 0!= rtnCode)
			{
				result = false;
				System.out.println("Shell command  "+shellCommand+ " is error.");
			}
			else
				System.out.println("Shell command  "+shellCommand+ " is OK.");
		}
		return result;
	}

	/** copy file by shell command to buggy directory (instrumention class, include buggy file).
	 * @param pathFilename : file name with absolute path.
	 * @param bugid bug id
	 * @param projectName : objectName
	 * @return
	 */
	public static boolean shellCopyFileToBuggy(String pathFilename,String projectName,int bugid)
	{
		boolean result = true;
		String shellCommand = "cp "+pathFilename+"  " +
				ProjectConfiguration.DatasetStoreDirectory+"/"+projectName+"/SourceCode"+"/v"+bugid+"/buggy";
		int rtnCode = MiscTools.runShellCommand(shellCommand);
		if( 0!= rtnCode)
		{
			result = false;
			System.out.println(shellCommand+ " is error.");
		}
		return result;
	}

	/** copy file by shell command to fixed directory (only buggy file, .java_#Fixed).
	 * @param pathFilename : file name with absolute path.
	 * @param bugid bug id
	 * @param projectName : objectName
	 * @return
	 */
	public static boolean shellCopyFileToFixed(String pathFilename,String projectName,int bugid)
	{
		boolean result = true;
		int lastSlash = pathFilename.lastIndexOf('/');
		String newFilename = pathFilename.substring(lastSlash);
		newFilename = newFilename.replace(".java_#Fixed", ".java");
		String shellCommand = "cp "+pathFilename+"  " +
				ProjectConfiguration.DatasetStoreDirectory+"/"+projectName+"/SourceCode"+"/v"+bugid+"/fixed/"+newFilename;
		int rtnCode = MiscTools.runShellCommand(shellCommand);
		if( 0!= rtnCode)
		{
			result = false;
			System.out.println(shellCommand+ " is error.");
		}
		return result;
	}
}
