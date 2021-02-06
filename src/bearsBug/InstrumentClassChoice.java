/**
 * 
 */
package bearsBug;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import common.MiscTools;
import common.ProjectConfiguration;


/**
 * @author ccsu
 *
 */
public class InstrumentClassChoice {
	private List<InstrumentItem> classHitLinesLst;  //记录每个类以及它被覆盖的总行数。class and its total coverage line.
	//注意，classInstumentChosen里必须排除掉classModify。classInstumentChosen里面的内容既要拷贝文件，也要被注入；
	private List<InstrumentItem> classInstumentChosen; //被选中的Instrument类，选择依据是被覆盖代码行数最多的类
	//而classModify里面的内容不能拷贝文件，只注入
	private List<InstrumentItem> classModify; //因为code被修改，它也必须被注入，但是它的文件不能再被拷贝到/SourceFile/v(xx)/buggy. 前面的步骤已经拷贝了。 
	
	public InstrumentClassChoice()
	{
		classHitLinesLst = new ArrayList<InstrumentItem>();
		classInstumentChosen = null;
		classModify= null;
	}
	/** 将解析的结果存入classHitLinesMap
	 * @param coverageXMLFilename   带目录信息的coverage.xml
	 * @return
	 */
	public boolean parseFile(String bearsProjectItem)
	{
		String coverageXMLFilename = MiscTools.getCoverageXMLFilename(bearsProjectItem);
		boolean result = true;
		try {
			//1.创建SAXReader对象
			SAXReader saxReader=new SAXReader();
			//2.调用read的方法
			Document xmlDocument;
			xmlDocument = saxReader.read(new File(coverageXMLFilename));
			//3.获取根元素
			Element rootCoverage=xmlDocument.getRootElement();
			//4.使用迭代器遍历集合直接子节点
			for(Iterator<Element> iterRoot=rootCoverage.elementIterator();iterRoot.hasNext();) {
				Element covElement=iterRoot.next();//Coverage的直接子节点
				if( covElement.getName().equals("sources") )
					continue;
				if( covElement.getName().equals("packages") ) 
				{
					for(Iterator<Element> subIter=covElement.elementIterator();subIter.hasNext();) {
						Element packElement=subIter.next();//packages的直接子节点
						if( !packElement.getName().equals("package") )
							continue;
						//node name = "package"
						if( false==parsePackage(packElement) )
						{
							result = false;
							break;
						}
					}//end of for...
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
	
	/**  将filename的属性值，取不带目录的文件名。
	 * @param dirFilename  filename="org/jfree/data/general/Series.java"
	 * @return
	 */
	private String getLastFilename(String dirFilename)
	{
		String[] strAry = dirFilename.split("/");
		return strAry[strAry.length-1];
	}

	
	/**因为部分类里面有子类，只记录那些类名和文件名相同的类（排除子类）。
	 * @param clazz  class name
	 * @param filename  file name
	 * @return true:identifl false: 子类
	 */
	private boolean isClassSameAsFilename(String clazz,String filename)
	{
		int index = filename.lastIndexOf(".java");
		String newFilename = filename.substring(0, index);
		newFilename = newFilename.replace('/','.');
		if( clazz.contentEquals(newFilename) )
			return true;
		else
			return false;
	}
	
	/** parse XML node "package"  
	 * @param packElement 
	 * @return
	 */
	private  boolean parsePackage(Element packElement)
	{
		boolean result = true;
		for(Iterator<Element> packIter=packElement.elementIterator("classes");packIter.hasNext();) {
			Element clazzElement=packIter.next();//package的直接子节点
			//Element name = "classes"
			for(Iterator<Element> clsIter=clazzElement.elementIterator("class");clsIter.hasNext();) {
				Element clsElement = clsIter.next();//classes的直接子节点
				//Element name = "class"
				String curClazzName="x",curFilename="y"; //class name & file name
				//得到class的属性
				for(Iterator<Attribute> iteAtt=clsElement.attributeIterator();iteAtt.hasNext();) {
					Attribute attr=iteAtt.next();
					if( attr.getName().equals("name") ) //class name
						curClazzName = attr.getValue();
					else if( attr.getName().equals("filename") ) //filename which include this class.
						curFilename = attr.getValue();
					else
						continue;
				}
				if( !isClassSameAsFilename(curClazzName,curFilename) )
					continue; //文件名和类名不匹配，说明是子类，不考虑。
				int totalCoverage = 0;//total coverage lines,
				for (Iterator<Element> itLines = clsElement.elementIterator("lines"); itLines.hasNext();) 
				{
					Element linesElement = itLines.next();//class的直接子节点
				    //Element name = "methods"
					totalCoverage += parseLines(linesElement);
				}
				InstrumentItem iItem = new InstrumentItem(curClazzName,curFilename,totalCoverage);
				classHitLinesLst.add(iItem);//###add
			}//end of for...clsIter
		}//end of for...packIter
		return result;
	}

	/** parse <methods name=.....  classes的子XML节点有两类: methods & lines
	 * @param eleMethod
	 * @return
	 */
	private int parseLines(Element linesElement)
	{
		int totalLines = 0;  //hits>0 
		for (Iterator<Element> itLine = linesElement.elementIterator("line"); itLine.hasNext();) 
		{
			Element oneLine = itLine.next();//lines的直接子节点
			int lineNo = Integer.valueOf(oneLine.attributeValue("number"));
			int hits = Integer.valueOf(oneLine.attributeValue("hits"));
			if( hits>0 )
				totalLines++;
		}//end of for...
		return totalLines;
	}
	
	//print classHitLinesMap
	public void testMe()
	{
		for(InstrumentItem  entry  :  classHitLinesLst){
			System.out.println(entry.getClazzName() +"," +entry.getFileName() +"," +entry.getCoverageLine());
		}
	}
	
	/**
	 * @return the classHitLinesMap
	 */
	public  List<InstrumentItem> getInstrumentList() {
		return classHitLinesLst;
	}

	
	/**
	 * @return the classInstumentChosen
	 */
	public  List<InstrumentItem> getClassInstumentChosen() {
		return classInstumentChosen;
	}
	

	/**
	 * @return the classInstumentChosen
	 */
	public  List<InstrumentItem> getClassInstumentModify() {
		return classModify;
	}
	
	
	/**
	 * @param buggyClassLst
	 * @param item
	 * @return
	 */
	private boolean isInModifyList(List<String> buggyClassLst, InstrumentItem item)
	{
		boolean result = false;
		for( String str : buggyClassLst)
		{
			if( str.contentEquals(item.getClazzName()) )
			{
				result = true;
				break;
			}
		}
		return result;
	}
	/**
	 * @param buggyClassLst  : modify class in buggy version
	 */
	public void generateInstrumentClassList(List<String> buggyClassLst)
	{
		classInstumentChosen = new ArrayList<InstrumentItem>(); //要注入，并且要拷贝
		classModify = new ArrayList<InstrumentItem>();//被修改的类，buggy类；只要注入，不拷贝
		int numberOfClass = classHitLinesLst.size();
		if( numberOfClass<=ProjectConfiguration.NumberOfInstrumentClass )
		{ //copy classHitLinesLst to classInstumentChosen
			for( InstrumentItem item: classHitLinesLst )
			{
				if( item.getCoverageLine()>0 )  //=0,will not instrument.
					classInstumentChosen.add(item);
			}
			return;
		}//end of if...
		//sort it by coverage line
		Collections.sort(classHitLinesLst); // 按coverage lines排,降序
		for( int k=0;k<ProjectConfiguration.NumberOfInstrumentClass;k++ )
		{
			InstrumentItem iitem = classHitLinesLst.get(k);
			if( isInModifyList(buggyClassLst,iitem) )
				continue;
			if( iitem.getCoverageLine()<=0 )
				break;
			classInstumentChosen.add(iitem);
		}//end of for...
		for( InstrumentItem iitem: classHitLinesLst )
		{
			if( isInModifyList(buggyClassLst,iitem) )
				classModify.add(iitem);
		}//end of for...
	}
	
	//注意，classInstumentChosen里必须排除掉classModify。classInstumentChosen里面的内容既要拷贝文件，也要被注入；
	//而classModify里面的内容不能拷贝文件，只注入
	public void showChoiceResult()
	{
		System.out.println("@@@@@@@@@@showChoiceResult:classInstumentChosen");
		for( InstrumentItem iitem: classInstumentChosen )
			System.out.println(iitem.getClazzName());
		System.out.println("@@@@@@@@@@@showChoiceResult:classModify");
		for( InstrumentItem iitem: classModify )
			System.out.println(iitem.getClazzName());
	}
	
	/**
	 * @return 所有需要注入的类名
	 */
	public List<String> getAllInstrumentClazz()
	{
		List<String> allInstrument = new ArrayList<String>();
		for( InstrumentItem iitem: classInstumentChosen )
			allInstrument.add(iitem.getClazzName());
		for( InstrumentItem iitem: classModify )
			allInstrument.add(iitem.getClazzName());
		return allInstrument;
	}
}
