package meta;

import ast.CyanMetaobjectLiteralObjectAnnotation;

/**
 * This metaobject represents a literal number that ends with letters. 
 * That is, something that starts with a digit but ends with letters.  
   @author José
 */
abstract public class CyanMetaobjectNumber extends CyanMetaobjectLiteralObject 
                implements IParseWithoutCyanCompiler_dpa {
	
	@Override
	public CyanMetaobjectNumber clone() {
		try {
			return (CyanMetaobjectNumber ) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public CyanMetaobjectNumber() {
		super();
	}
	/**
	 * return the names that may end the number. For example, a metaobject for binary 
	 * numbers of the kind 0101bin or 011Bin should return an array containing 
	 * "bin" and "Bin". The name is found by starting at the end of the number and 
	 * collecting all letters till a underscore or number is found. So, the name
	 * of  0A5CF_Hex is "Hex".
	   @return
	 */
	abstract public String []getSuffixNames();

	/**
	 * if the number can have the suffixes "bin" and "Bin" the name is "number(bin, Bin)".
	 */
	@Override
	public String getName() {
		String name = "number(";
		String []suffixes = this.getSuffixNames();
		int size = suffixes.length;
		for( String s : suffixes ) {
			name += s;
			if ( --size > 0 )
				name += ", ";
		}
		return name + ")";
	}
	
	public Object getInfo() {
		return ((CyanMetaobjectLiteralObjectAnnotation ) this.getMetaobjectAnnotation()).getInfo();
	}
	public void setInfo(Object info) {
		((CyanMetaobjectLiteralObjectAnnotation ) this.getMetaobjectAnnotation()).setInfo(info);
	}

	
}
