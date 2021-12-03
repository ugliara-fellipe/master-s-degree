package meta;

import lexer.SymbolLiteralObject;

/**
 * This metaobject represents strings that are preceded by  a single letter such as
 *    <code>r"[A-Za-z_][A-Za-z_0-9]*"</code>
   @author José
 */
abstract public class CyanMetaobjectLiteralString extends CyanMetaobjectLiteralObject {

	@Override
	public CyanMetaobjectLiteralString clone() {
		try {
			return (CyanMetaobjectLiteralString ) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
	/**
	 * return the letters or symbols that may start a string of a metaobject annotation
	 * of this metaobject. For example, getName would return 
	 *      new String[] { "r" }
	 * in a metaobject that represent regular expressions such as 
	 *     r"[A-Za-z_][A-Za-z_0-9]*" 
	   @return
	 */
	abstract public String []getPrefixNames();	
	
	/**
	 * if the string can have the prefixes "r" and "R" the name is "string(r, R)".
	 */
	
	@Override
	public String getName() {
		String name = "string(";
		String []prefixes = this.getPrefixNames();
		int size = prefixes.length;
		for( String s : prefixes ) {
			name += s;
			if ( --size > 0 )
				name += ", ";
		}
		return name + ")";
	}
	

	public SymbolLiteralObject getSymbolLiteralObject() {
		return symbolLiteralObject;
	}
	public void setSymbolLiteralObject(SymbolLiteralObject symbolLiteralObject) {
		this.symbolLiteralObject = symbolLiteralObject;
	}
	
	
	public Object getInfo() {
		return info;
	}

	public void setInfo(Object info) {
		this.info = info;
	}


	/**
	 * the symbol that represents this literal string
	 */
	private SymbolLiteralObject symbolLiteralObject;

	/**
	 * information on the string gathered during parsing. It is usually the Cyan code that
	 * should replace the number during semantic analysis 
	 */
	private Object info;
		
}
