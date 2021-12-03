package meta;

import java.util.ArrayList;
import ast.CyanMetaobjectLiteralObjectAnnotation;
import saci.Tuple4;

/**
 * represents a user-defined literal object. There are several kinds of literal objects 
 * defined by users in Cyan: those delimited by a sequence of symbols such as
 *       [* (1, 2), (2, 3), (3, 1) *]
 *       
 * literal object numbers such as 1011bin, literal strings such as r"a+", and 
 * those that start by '@' followed by an identifier immediately followed by a sequence of symbols:
 *       @graph[* (1, 2), (2, 3), (3, 1) *]
 *       
 * All but the last type are represented by subclasses of this class
 *         
   @author José
 */

abstract public class CyanMetaobjectLiteralObject extends CyanMetaobject 
                      implements  IAction_dsa {
	

	public CyanMetaobjectLiteralObject() {
		super();
	}

	
	/**
	 *  
	 * This method should be called by a IDE plugin to show the text associated to the metaobject annotation
	 * <code>metaobjectAnnotation</code> in several colors (text highlighting). 

	 * 
	 * Each tuple (color number, line number, column number, size). <br>
	 * The characters starting at line number, column number till column number
	 * + size - 1 should be highlighted in color "color number".
	 *  <code>metaobjectAnnotation</code> is redundant nowadays because this class already has an instance variable 
	 *  with the same contents.
	   @return
	 */
	public ArrayList<Tuple4<Integer, Integer, Integer, Integer>> getColorTokenList(CyanMetaobjectLiteralObjectAnnotation metaobjectLiteralObjectAnnotation) {
		return null;
	}
	
	

	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	@Override
	public String getPrototypeOfType() { return "Nil"; }
	
}
