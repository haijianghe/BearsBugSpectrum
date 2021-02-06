/**
 * 
 */
package bearsBug;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import common.FileToolkit;
import common.ProjectConfiguration;

/**
 * @author ccsu
 *产生.../(bearsItem)/target/surefire-reports/TEST-com.*.xml文件，可从中读取到测试类及其测试方法
 */
public class ReadAllTestCases {
	/**
	 * @param bearsProjectItem       Bears Item
	 * @param allTestCasesLst  the string list of TestClass#TestMethod
	 * @return  true: successfule.   false: fail.
	 */
	public static boolean  getAllTestCases(String bearsProjectItem,Map<String,Integer> allTestCasesMap)
	{
		boolean result = true;
		//allTestCasesMap = new HashMap<>();
		List<File> xmlFiles = FileToolkit.GetAllFiles(ProjectConfiguration.BearsBugRawDir+"/"+bearsProjectItem+"/target/surefire-reports", "xml");
		for( File file : xmlFiles )
		{
			String fileName = file.getName();
			if( !fileName.startsWith("TEST-") )
				continue;
			if( !parseTestXmlFile(file.getPath(),allTestCasesMap) )
			{
				System.out.println("Parse TEST-(???).xml file is error : "+fileName);	
				result = false;
				break;
			}
		}//end of for...
		if( result )
		{
			testMe(allTestCasesMap);
		}
		return result;
	}
	
	/** 将解析的结果存入allTestCasesLst
	 * @param testXMLFilename   带目录信息的TEST-com.fasterxml.jackson.databind.util.TestTokenBuffer.xml
	 * @return
	 */
	/**
	 * @param testXMLFilename
	 * @param allTestCasesLst
	 * @return
	 */
	private static boolean parseTestXmlFile(String testXMLFilename,Map<String,Integer> allTestCasesMap)
	{
		boolean result = true;
		try {
			//1.创建SAXReader对象
			SAXReader saxReader=new SAXReader();
			//2.调用read的方法
			Document xmlDocument;
			xmlDocument = saxReader.read(new File(testXMLFilename));
			//3.获取根元素
			Element rootCoverage=xmlDocument.getRootElement();
			//4.使用迭代器遍历集合直接子节点
			for(Iterator<Element> iterRoot=rootCoverage.elementIterator();iterRoot.hasNext();) {
				Element testsuiteElement=iterRoot.next();//testsuite的直接子节点
				if( testsuiteElement.getName().equals("properties") )
					continue;
				if( testsuiteElement.getName().equals("testcase") ) 
				{
					String testName="x",clazzName="y"; //testcase name & class name 
					//得到testcase的属性
					for(Iterator<Attribute> iteAtt=testsuiteElement.attributeIterator();iteAtt.hasNext();) {
						Attribute attr=iteAtt.next();
						if( attr.getName().equals("name") ) //testcase name
							testName = attr.getValue();
						else if( attr.getName().equals("classname") ) //classname.
							clazzName = attr.getValue();
						else
							continue;
					}
					int testResult = 0;//0=pass 1=failure 2=error 3=Skipped
					for(Iterator<Element> subIter=testsuiteElement.elementIterator();subIter.hasNext();) {
						Element testcaseElement=subIter.next();//testcase的直接子节点
						if( testcaseElement.getName().equals("error") )
							testResult =2;
						else if( testcaseElement.getName().equals("failure") )
							testResult =1;
						else if( testcaseElement.getName().equals("skipped") ) //no test it!!!!!
							testResult =3;
						else 
							continue;
					}//end of for...
					//FasterXML Jackson Databind 3 error : 
					//class name = com.fasterxml.jackson.databind.ext.TestDOM
					//test method name = testDeserializeNonNS,testSerializeSimpleNonNS,testDeserializeNS
					//if( clazzName.contentEquals("com.fasterxml.jackson.databind.ext.TestDOM") )  //FasterXML Jackson Databind
					if( clazzName.contains("CtTypeInformationTest") && testName.contentEquals("testGetSuperclass") ) //INRIASpoon
					{
						System.out.println("Exclude test for Fasterxml: "+clazzName+"::::"+testName);
					}
					else if( testName.contains("[") )//INRIASpoon
					{
						System.out.println("Exclude test for Fasterxml: "+clazzName+"::::"+testName);
					}
					else
						allTestCasesMap.put(clazzName+"#"+testName,testResult);
				}//end of if...
				if( false==result )
					break;
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	//show information
	//测试结果 0=pass 1=failure 2=error 3=Skipping
	private static void testMe(Map<String,Integer> allTestCasesMap)
	{
		System.out.println("Parse TEST-(???).xml file is ok. Total testcases = "+allTestCasesMap.size());
		//遍历Map的方式		第一种方式：Iterator的方式
				/*Iterator<Map.Entry<String, Integer>> itr = map.entrySet().iterator();
				//等价于Set<Map.Entry<String, Integer>> set = map.entrySet();  
				//Iterator<Map.Entry<String, Integer>> itr = set.iterator();
				while(itr.hasNext()){
					Map.Entry<String, Integer> entry = itr.next();
					String key = entry.getKey();
					Integer value = entry.getValue();
					System.out.println(key+" : "+value);
				}*/
		//第二种：增强型for循环
		for(Map.Entry<String, Integer>  entry  :  allTestCasesMap.entrySet()){
			if( entry.getValue()==1 )
				System.out.println("failure:     "+entry.getKey() +"=" +entry.getValue());
			else if( entry.getValue()==2 )
				System.out.println("error:     "+entry.getKey() +"=" +entry.getValue());
			else {}
		}
	}
}
