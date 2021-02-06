/**
 * 
 */
package bearsBug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import common.ProjectConfiguration;

/**
 * @author ccsu
 *
 */
public class POMAddinstrument {

	/**
	 * @param bearsProjectItem 
	 * @param clazzInstrument  
	 * @return
	 */
	public static boolean addInstrumentClass(String bearsProjectItem,List<String> clazzInstrument )
	{
		String pomXMLFilename = ProjectConfiguration.BearsBugRawDir+"/"+bearsProjectItem+"/pom.xml";
		boolean result = true;
		
		try {
			SAXReader saxReader=new SAXReader();
			Document doc=saxReader.read(new File(pomXMLFilename));
   
			Element root=doc.getRootElement(); //project
			//System.out.println(root.getName());
			Element build = root.element("build");
			Map<String,String> map = new HashMap<String,String>();
			// 获得命名空间
			String nsURI = doc.getRootElement().getNamespaceURI();
			map.put("xmlns", nsURI);
			/*<pluginManagement>
	        <plugins>
	            <plugin>
	                <groupId>org.codehaus.mojo</groupId>
	                <artifactId>cobertura-maven-plugin</artifactId>
	                <configuration>
	                    <instrumentation>
							<include>com/fasterxml/jackson/databind/introspect/Object*</include>
							<include>com/fasterxml/jackson/databind/JavaType*</include>
			*/
			//add to <build>  </build>
			Element management=null;
			boolean willAdd = false; 
			if( !isExist(build,"pluginManagement") )
			{
				management=build.addElement("pluginManagement");//no pluginManagement
				willAdd = true; //will add "//pluginManagement//plugin//instrumentation/include"
			}
			else
				management = build.element("pluginManagement");
			if( willAdd || !isExistInstrument(map,doc) )
			{
				Element plugins = management.addElement("plugins");
				Element plugin = plugins.addElement("plugin");
				Element groupId = plugin.addElement("groupId");
				groupId.setText("org.codehaus.mojo");
				Element artifactId = plugin.addElement("artifactId");
				artifactId.setText("cobertura-maven-plugin");
				Element configuration = plugin.addElement("configuration");
				Element instrumentation = configuration.addElement("instrumentation");
				//instrument ~30 class
				for( String coverageClass : clazzInstrument)
				{
					Element include = instrumentation.addElement("include");
					String ist = coverageClass.replace('.', '/');
					include.setText(ist+"*");
				}
				//update pom.xml
				OutputFormat opf=new OutputFormat("\t",true,"UTF-8");
				opf.setTrimText(true);
				XMLWriter writer=new XMLWriter(new FileOutputStream(pomXMLFilename),opf);
				writer.write(doc);
				writer.close();
			}
		} 
		catch (DocumentException e) {
			result = false;
			e.printStackTrace();
		} 
		catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	//nodeName is in buildElementv?
	private static boolean isExist(Element buildElement,String nodeName)
	{
		List<Element> nodes = buildElement.elements(nodeName);
		if( nodes.size()>0 )
			return true;
		else
			return false;
	}
	
	//instrument information is in build?
	private static boolean isExistInstrument(Map<String,String> map,Document doc)
	{
		// 创建解析路径，就是在普通的解析路径前加上map里的key值
		XPath xpath = doc.createXPath("//xmlns:instrumentation/xmlns:include");
		xpath.setNamespaceURIs(map);

		//String xpath ="//instrumentation/include";
		List<Node> includeNodes = xpath.selectNodes(doc);
		
		if( includeNodes.size()>0 )
			return true;
		else
			return false;
	}
}
