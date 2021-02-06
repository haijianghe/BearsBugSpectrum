/**
 * 
 */
package bearsBug;

/**
 * @author ccsu
 *
 */
public class InstrumentItem implements Comparable<InstrumentItem>{
	private String clazzName; //class name
	private String fileName;  //file name
	private int coverageLine; //total line by coverage

	/**
	 * @param clazzName  class name
	 * @param fileName    file name
	 * @param coverageLine
	 */
	public InstrumentItem(String clazzName, String fileName, int coverageLine) {
		super();
		this.clazzName = clazzName;
		this.fileName = fileName;
		this.coverageLine = coverageLine;
	}

	/**
	 * @return the clazzName
	 */
	public String getClazzName() {
		return clazzName;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the coverageLine
	 */
	public int getCoverageLine() {
		return coverageLine;
	}

	/**
	 * @param clazzName the clazzName to set
	 */
	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param coverageLine the coverageLine to set
	 */
	public void setCoverageLine(int coverageLine) {
		this.coverageLine = coverageLine;
	}

	 //重写Comparable接口的compareTo方法，
	@Override
	public int compareTo(InstrumentItem iitem) {
		//  根据coverage lines降序排列，升序修改相减顺序即可
		return iitem.coverageLine-this.coverageLine;
	}
	
	/** 使用contains()方法
     * 重写equals()方法 当两个人得身份证号相同以及姓名相同时，表示这两个人是同一个人。 
     */  
/*	@Override
    public boolean equals(Object o) {  
        if (o == this) {  
            return true;  
        }  
        if (o instanceof Person) {  
            Person p = (Person) o;  
            boolean b = this.code.equals(p.code) && this.name.equals(p.name);  
            return b;  
        }  
        return false;  
    }*/  

}
