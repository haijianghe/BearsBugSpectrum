/**
 * 
 */
package common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import bearsBug.BearsNullLineClass;
import bearsBug.InstrumentClassChoice;
import bearsBug.InstrumentItem;
import bearsBug.POMAddinstrument;
import bearsBug.ReadAllTestCases;
import bearsBug.TestcasePassFailError;

/*Apache Commons IO (for Apache Directory Studio) » 2.4
 * 
<!-- https://mvnrepository.com/artifact/org.apache.directory.studio/org.apache.commons.io -->
<dependency>
    <groupId>org.apache.directory.studio</groupId>
    <artifactId>org.apache.commons.io</artifactId>
    <version>2.4</version>
</dependency>

 */
/**
 * @author ccsu
 *
 */
public class MainProcess {
	private static String bearsItem = "Bears-123";
	private static String objectName = "traccar"; 
	private static int bugid = 41; //bug id

	//String prefixPackage = "src/main/java/";//INRASpoon,length of "src/main/java/" is 14
	private static String prefixPackage = "src/"; //traccar

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		System.out.println("1, read Bears-id project and create Corresponding directory.");
		System.out.println("2, make cobertura Instrumention and get all Testcases.");
		System.out.println("3, test one testcase and check coverage.xml(does not).");
		System.out.println("4, testing of write profile file.");
		
		System.out.println("8, run one bugid,collect information and generate profile.");
    	System.out.println("\r\n Others, exit............ .");
    	System.out.println("Please key your choice.");
    	Scanner sc=new Scanner(System.in);
    	int choice = sc.nextInt();
    	sc.close();
    	switch( choice )
    	{
    	case 1:   
    		buggyFixedFileProcess();
    		//List<String> lsr = new ArrayList<String>();
    		//lsr.add("/media/ccsu/workspace/BearsBug/Bears-28/src/main/java/spoon/reflect/factory/tyya.java_#Fixed");
    		//List<String> ps = getBuggyClassNameList(lsr);
    		/*List<String> lsr = new ArrayList<String>();
    		lsr.add("a.java");
    		lsr.add("b.java");
    		lsr.add("c.java");
    		boolean resutl = POMAddinstrument.addInstrumentClass(bearsItem,lsr );
    		System.out.println(resutl);*/
    		break;
    	case 2:   
    		Map<String,Integer> allTestCasesMap=null;
    		allTestCasesMap = new HashMap<String,Integer>();
    		makeCoberturaInstrumentionGetAllTestcases(allTestCasesMap); 
    		break;    		
    	case 3:
    		//String testcase = "com.fasterxml.jackson.databind.objectid.AlwaysAsReferenceFirstTest#testIssue1607";
    		String testcase = "com.fasterxml.jackson.databind.util.TestObjectBuffer#testTyped";
    		int[] passFail = {-1};
    		runOneTestCase(testcase,passFail);
    		break;
    	case 4:
    		testingWriteProfile();
    		break;
    	case 8:
    		runParseCollection();
    		break;
		default:
			break;
    	}//end of switch
    	System.out.println("The task about (####"+objectName+"####)is over.");
	}

	//完整地执行一个bugid, 
	private static void  runParseCollection()
	{
		SimpleDateFormat dfStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		System.out.println("Task is start."+dfStart.format(System.currentTimeMillis()));
		
		if( !buggyFixedFileProcess() )  //创建(objectName)对应的所有文件夹,对应的buggy文件,fixed文件拷贝
			return;
		
		Map<String,Integer> allTestCasesMap=null;
		allTestCasesMap = new HashMap<String,Integer>();
		if( !makeCoberturaInstrumentionGetAllTestcases(allTestCasesMap) )
			return;
		
		int tcFailed = 0;
		int tcPassed = 0;
		int tcChanged = 0; //testcase result is changed.
		for(Map.Entry<String, Integer>  entry  :  allTestCasesMap.entrySet())
		{
			if( entry.getValue()==1 ) //failure
				tcFailed++;
			else if( entry.getValue()==0 ) //pass
				tcPassed++;
			else if( entry.getValue()==2 ) //error
				tcFailed++;   //failure + error = failed testcase
			else {}
		}
		if( !ProjectInfoFile.processTestCaseFile(objectName,bugid,tcPassed,tcFailed) ) //.testcase
			return;
		if( !ProjectInfoFile.processBugidOrderFile(objectName,bugid,bugid) )  //.bugid
			return;
		
		ProfileFile  matrixProfile = new ProfileFile(bugid,objectName,tcPassed,tcFailed);
		//Notice: tcFailed+tcPassed != size().
		int totalTcases = allTestCasesMap.size(); //该bugid总共有多少测试用例。
		System.out.println("The bugid="+bugid+",his total testcases = "+totalTcases+".");
		if( totalTcases>=3000 )
		{//花费时间太多，放弃了。
			System.out.println("The bugid="+bugid+",his total testcases is too much, abandon it.");
			return;
		}
		if( tcFailed<1 )
		{//There have not failed testcases。
			System.out.println("The bugid="+bugid+",his failed testcases  = 0, abandon it.");
			return;
		}
		if( tcPassed<1 )
		{//There have not passed testcases。
			System.out.println("The bugid="+bugid+",his passed testcases  = 0, abandon it.");
			return;
		}
		
		int addup = 0;//累计完成的测试用例个数。
		String nowTestClassMethod = null;//将记录导致我程序出错的测试用例
		System.out.print("Now, ....");
		boolean result = true;
		for( Map.Entry<String, Integer>  entry  :  allTestCasesMap.entrySet() )
		{
			//if( entry.getValue()!=0 && entry.getValue()!=1 )
				//continue;  //2=error,3=skipped
			if( entry.getValue()!=0 && entry.getValue()!=1 && entry.getValue()!=2 )
				continue;  //3=skipped
			//输出当前正在执行的测试用例
			addup++;
			//if( addup>=5 )//for test
			//	break;
			String testClassMethod = entry.getKey();
			nowTestClassMethod = testClassMethod;
			System.out.print(" "+addup+"#"+" ");
			if( addup%40 == 0 )
				System.out.println("       ");
			//some testcase will cause error.
			//Collections bugid=25: org.apache.commons.collections4.iterators.IteratorChainTest)::testEmptyChain
			//if( projectName.equals("Collections") )
			//	testClassMethod = removeChar(testClassMethod);
			//开始测试和解析
			int[] passFail = {-1};
			if( !runOneTestCase(testClassMethod,passFail) )
			{
				System.out.println("Now,object = "+objectName+", method= "+testClassMethod+", #="+addup);
				result = false;
				break;
			}
			if( passFail[0]!=entry.getValue() )
			{
				tcChanged++;
				System.out.println("\n passFail[0]!=entry.getValue() "+passFail[0]+","+entry.getValue());
				if( passFail[0]==0 )  //0 is pass 
					matrixProfile.incrementPassTestcase(true);  //pass+1,fail-1
				else 
				{ //1 or 2 is fail
					matrixProfile.removeTestcaseChangeToFail(); //pass-1,
					continue;
				}
				//result = false;
				//break;
			}
					
			CoverageXMLParse cxParse = new CoverageXMLParse();
			String coverageXMLFilename = MiscTools.getCoverageXMLFilename(bearsItem);
			if( !cxParse.parseFile(coverageXMLFilename) )
			{
				System.out.println("Parse XML file is fail.");
				result = false;
				break;
			}
			MatrixOfClasses clazzMatrix = cxParse.getClazzMatrix();
			
			matrixProfile.assembleFromParsedCoverage(clazzMatrix, passFail[0]==0?true:false);
		}//end of for...
		System.out.println("  ");
		if( result )
		{
			System.out.println("All test cases have been run, coverage report and parse is ok. bugid="+bugid);
			String nullLineClasses = BearsNullLineClass.getNullLineClasses();
			System.out.println(nullLineClasses);
			if( matrixProfile.writeProfileFile()==false )
				System.out.println("Write to profile file is error.");
			System.out.println("Test case result is changed = "+tcChanged +
					"  passed = "+matrixProfile.getPassed()+"  Failed="+matrixProfile.getFailed());
		}
		else
			System.out.println("bugid="+bugid+"   ,testcase "+nowTestClassMethod+" is error.");
		return;

	}
	/*
	 * 从bearsItem读入被修改代码的文件（后缀为.java_#Fixed），将对应的buggy文件拷贝到(objectName)/SourceCode/V(xx)/buggy文件夹下；
	 * 将对应的fixed文件拷贝到(objectName)/SourceCode/V(xx)/fixed文件夹下。
	 * 并且，在之前，要创建(objectName)对应的所有文件夹。
	 */
	private static boolean buggyFixedFileProcess()
	{
		if( !MiscTools.mkdirSpectrumDirectory(objectName, bugid) )
			return false;
		List<String> buggyFilenameLst = getModifyClassWithPath();
		for( String item : buggyFilenameLst )
		{
			String newFilename = item.replace(".java_#Fixed", ".java");
			if( !MiscTools.shellCopyFileToBuggy(newFilename,objectName, bugid) )
				return false;
			if( !MiscTools.shellCopyFileToFixed(item,objectName, bugid) )
				return false;
		}
		return true;
	}
	
	/*
	 * 读取该版本所有的被修改的类(buggy类)，也就是被修改代码的文件（后缀为.java_#Fixed）。将相应的文件名存入List
	 */
	private static List<String> getModifyClassWithPath()
	{
		List<String> modifyFilenameLst = new ArrayList<String>();
		List<File> fixedFiles = FileToolkit.GetAllFiles(ProjectConfiguration.BearsBugRawDir+"/"+bearsItem+"/"+prefixPackage, "java_#Fixed");
		for( File file : fixedFiles )
		{
			String filePathName = file.getAbsolutePath();
			System.out.println("Buggy class : "+filePathName);
			modifyFilenameLst.add(filePathName);
		}
		return modifyFilenameLst;
	}
	
	/** filename => class name 
	 * remove .java_#Fixed and   com.(...)之前的   
	 * @param modifyFilenameLst : .../(path)/xxx.java_#Fixed
	 * @return com.(...).java
	 */
	private static List<String> getBuggyClassNameList(List<String> modifyFilenameLst)
	{
		int posIndex = prefixPackage.length();
		List<String> buggyClassLst = new ArrayList<String>();
		for( String item : modifyFilenameLst )
		{
			int index = item.indexOf(prefixPackage); //length of "src/main/java/" is 14
			String filename = item.substring(index+posIndex);
			String clazzName = filename.replace('/', '.');   //directory is /; class name is .!
			index = clazzName.indexOf(".java_#Fixed");
			clazzName = clazzName.substring(0,index);
			buggyClassLst.add(clazzName);
		}
		return buggyClassLst;
	}
	
	/*1,mvn clean 清除Maven以前生成的结果，target
	 * 2，mvn cobertura:cobertura -V -B  -Dcobertura.report.format=xml
	 *        产生.../(bearsItem)/target/surefire-reports/TEST-com.*.xml文件，可从中读取到测试类及其测试方法；
	 *        产生.../(bearsItem)/target/site/cobertura/coverage.xml文件，可从中读取覆盖信息，从而决定哪些类该Instrument.
	 * 3,修改pom.xml文件，添加Instrument信息。
	 * 4，拷贝Instrument类对应文件到SourceCode/v(xx)/buggy/
	 *  
	 */
	private static boolean makeCoberturaInstrumentionGetAllTestcases(Map<String,Integer> allTestCasesMap)
	{
		//allTestCasesMap = null;
		//mvn clea  && mvn cobertura:cobertura -V -B  -Dcobertura.report.format=xml
		String mavenCommand="cd "+ProjectConfiguration.BearsBugRawDir+"/"+bearsItem;
		mavenCommand = mavenCommand+" && mvn clean";
		mavenCommand = mavenCommand+" && mvn cobertura:cobertura -V -B  -Dcobertura.report.format=xml";
		if( 0!=MiscTools.runMavenCommand(mavenCommand) )
		{
			System.out.println("Maven error : "+mavenCommand);
			return false;
		}
		//read: .../(bearsItem)/target/surefire-reports/TEST-com.*.xml
		if( false==ReadAllTestCases.getAllTestCases( bearsItem,allTestCasesMap) )
			return false;
		//read.../(bearsItem)/target/site/cobertura/coverage.xml文件，可从中读取覆盖信息，从而决定哪些类该Instrument.
		InstrumentClassChoice icChoice = new InstrumentClassChoice();
		if( !icChoice.parseFile(bearsItem) )
			return false;
		//icChoice.testMe();
		//Next, get buggy class( their code have been modify )
		List<String> buggyFilenameLst = getModifyClassWithPath();
		List<String> buggyClassLst = getBuggyClassNameList(buggyFilenameLst);
		icChoice.generateInstrumentClassList(buggyClassLst);
		//icChoice.showChoiceResult();
		//拷贝Instrument类对应文件到SourceCode/v(xx)/buggy/
		 List<InstrumentItem> chosenInstruments = icChoice.getClassInstumentChosen();
		 for( InstrumentItem iitem : chosenInstruments )
		 {
			String filename = iitem.getFileName();
			String newFilename = ProjectConfiguration.BearsBugRawDir+"/"+bearsItem +"/"+
					prefixPackage + filename;
			if( !MiscTools.shellCopyFileToBuggy(newFilename,objectName, bugid) )
				return false;
		 }//end of for...
		 System.out.println("copy chosen instrument class to Buggy  is ok. ");
		 //修改pom.xml文件，添加Instrument信息。
		 List<String> allInstrumentClazz =  icChoice.getAllInstrumentClazz();
		 if( !POMAddinstrument.addInstrumentClass(bearsItem,allInstrumentClazz) )
		 {
			 System.out.println("update pom.xml  is fail. ");
			 return false;
		 }
		 System.out.println("update pom.xml  is ok. ");
		 return true;
	}
	
	
	/**
	 * 检查测试用例的执行情况，检查文件coverage.xml
	 */
	private static boolean runOneTestCase(String testcase,int passFail[])
	{
		String shellResultFilename = "command_result.tmp";
		String mavenCommand="cd "+ProjectConfiguration.BearsBugRawDir+"/"+bearsItem;
		mavenCommand = mavenCommand + " && mvn cobertura:clean && rm " + MiscTools.getCoverageXMLFilename(bearsItem);
		if( 0!=MiscTools.runMavenCommand(mavenCommand) )
		{
			System.out.println("Maven error : "+mavenCommand);
			return false;
		}
		mavenCommand="cd "+ProjectConfiguration.BearsBugRawDir+"/"+bearsItem;
		//mavenCommand = mavenCommand +" && mvn cobertura:cobertura -V -B  -Dcobertura.report.format=xml -Dtest="+testcase;
		mavenCommand = mavenCommand +" && mvn cobertura:cobertura -V -B  -Dcobertura.report.format=xml "+
					" -Denforcer.skip=true -Dcheckstyle.skip=true  -DskipITs=true -Drat.skip=true -Dlicense.skip=true "+
					" -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true " +
							" -Dtest="+testcase;
		if( 0!=MiscTools.runMavenRedirect(mavenCommand,shellResultFilename) )
		{
			System.out.println("Maven error : "+mavenCommand);
			return false;
		}
		int testResult =  TestcasePassFailError.getTestPassFailError(ProjectConfiguration.DatasetStoreDirectory+"/"+shellResultFilename);
		if( testResult<0 )
		{
			System.out.println(testcase+": read command_result.tmp is error.");
			return false;
		}
		passFail[0] = testResult;
		//0=pass 1=failure 2=error 3=Skipped	
		//System.out.println(testcase + "  is running ok. = "+testResult);
		return true;
	}
	
		
	//测试写.profile文件是否成功。
	private static boolean testingWriteProfile()
	{
		Map<String,Integer> allTestCasesMap=null;
		allTestCasesMap = new HashMap<String,Integer>();

		if( !makeCoberturaInstrumentionGetAllTestcases(allTestCasesMap) )
			return false;
		
		int tcFailed = 0;
		int tcPassed = 0;
		//faild testcase = failure + error.
		for(Map.Entry<String, Integer>  entry  :  allTestCasesMap.entrySet())
		{
			if( entry.getValue()==1 )
				tcFailed++;
			else if( entry.getValue()==0 )
				tcPassed++;
			else {}
		}
		ProfileFile  matrixProfile = new ProfileFile(bugid,objectName,tcPassed,tcFailed);
		//Notice: tcFailed+tcPassed != size().
		int totalTcases = allTestCasesMap.size(); //该bugid总共有多少测试用例。
		System.out.println("The bugid="+bugid+",his total testcases = "+totalTcases+".");
		if( totalTcases>=3000 )
		{//花费时间太多，放弃了。
			System.out.println("The bugid="+bugid+",his total testcases is too much, abandon it.");
			return true;
		}
		if( tcFailed<1 )
		{//There have not failed testcases。
			System.out.println("The bugid="+bugid+",his failed testcases  = 0, abandon it.");
			return true;
		}
		if( tcPassed<1 )
		{//There have not passed testcases。
			System.out.println("The bugid="+bugid+",his passed testcases  = 0, abandon it.");
			return true;
		}
		
		int addup = 0;//累计完成的测试用例个数。
		String nowTestClassMethod = null;//将记录导致我程序出错的测试用例
		System.out.print("Now, ....");
		boolean result = true;
		for( Map.Entry<String, Integer>  entry  :  allTestCasesMap.entrySet() )
		{
			if( entry.getValue()!=0 && entry.getValue()!=1 )
				continue;  //2=error,3=skipped
			//输出当前正在执行的测试用例
			addup++;
			if( addup>=5 )//for test
				break;
			String testClassMethod = entry.getKey();
			nowTestClassMethod = testClassMethod;
			System.out.print(" "+addup+"#"+" ");
			if( addup%40 == 0 )
				System.out.println("       ");
			//some testcase will cause error.
			//Collections bugid=25: org.apache.commons.collections4.iterators.IteratorChainTest)::testEmptyChain
			//if( projectName.equals("Collections") )
			//	testClassMethod = removeChar(testClassMethod);
			//开始测试和解析
			int[] passFail = {-1};
			if( !runOneTestCase(testClassMethod,passFail) )
			{
				System.out.println("Now,object = "+objectName+", method= "+testClassMethod+", #="+addup);
				result = false;
				break;
			}
			if( passFail[0]!=entry.getValue() )
			{
				if( passFail[0]==0 )  //0 is pass 
					matrixProfile.incrementPassTestcase(true);  //pass+1,fail-1
				else  //1 or 2 is fail
					matrixProfile.incrementPassTestcase(false); //pass-1,fail+1
				System.out.println("passFail[0]!=entry.getValue() "+passFail[0]+","+entry.getValue());
				//result = false;
				//break;
			}
					
			CoverageXMLParse cxParse = new CoverageXMLParse();
			String coverageXMLFilename = MiscTools.getCoverageXMLFilename(bearsItem);
			if( !cxParse.parseFile(coverageXMLFilename) )
			{
				System.out.println("Parse XML file is fail.");
				result = false;
				break;
			}
			MatrixOfClasses clazzMatrix = cxParse.getClazzMatrix();
			
			matrixProfile.assembleFromParsedCoverage(clazzMatrix, passFail[0]==0?true:false);
		}//end of for...
		System.out.println("  ");
		if( result )
		{
			System.out.println("All test cases have been run, coverage report and parse is ok. bugid="+bugid);
			String nullLineClasses = BearsNullLineClass.getNullLineClasses();
			System.out.println(nullLineClasses);
			if( matrixProfile.writeProfileFile()==false )
				System.out.println("Write to profile file is error.");
		}
		else
			System.out.println("bugid="+bugid+"   ,testcase "+nowTestClassMethod+" is error.");
		return true;
	}
}
