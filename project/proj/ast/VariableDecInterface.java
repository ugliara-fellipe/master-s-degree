package ast;

import lexer.Symbol;
import saci.NameServer;

public interface VariableDecInterface extends ASTNode {
	
	Type getType();
	void setType(Type type);
	String getName();
	String getJavaName();
	void setJavaName(String javaName);
	default String javaNameWithRef() {
		if ( getTypeWasChanged() )
			return getJavaName();
		else if ( getRefType() ) 
			return NameServer.getJavaName(this.getName()) + ".elem"; 
		else 
			return getJavaName(); 
	}
	void setTypeInDec(Expr typeInDec);
    Expr getTypeInDec();
    Symbol getVariableSymbol();
	Symbol getFirstSymbol();
	boolean isReadonly();
	/**
	 * return true if the variable was used where a reference was
	 * expected. For example, if there is statement 
	 *       Sum(sum)
	 *  in which the parameter of Sum is "Int &s", then sum was
	 *  used where a reference was expected. This method should
	 *  return true for "sum".
	   @return
	 */
	boolean getRefType();
	void setRefType(boolean refType);
	/**
	 * if called with 'true', this means that the variable type was changed. 
	 * This change already considers that the variable is from a ref type or not.
	 * That is, suppose the type of a variable 'v' was changed from {@code Union<Int, String>} to {@code Int} inside
	 * a anonymous function. Then the name in java of the variable should be <br>
	 * {@code     ((CyInt ) _v.elem._elem)}<br>
	 * The first 'elem' is because of the ref type (see method javaNameWithRef). The second, {@code _elem} is because 
	 * of the union.  
	 */
	void setTypeWasChanged(boolean typeWasChanged);
	/**
	 * see {@link #setTypeWasChanged(boolean)}.
	   @return
	 */
	boolean getTypeWasChanged();
}
