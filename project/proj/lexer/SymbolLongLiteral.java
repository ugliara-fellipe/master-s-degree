/**
 * 
 */
package lexer;

import ast.CompilationUnitSuper;

/** This class represents long literals
 * @author José
 *
 */
public class SymbolLongLiteral extends Symbol {

	/**
	 * @param symbolString
	 */
	public SymbolLongLiteral(Token token, String symbolString, long longLiteral,
            int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.longLiteral = longLiteral;
	}
	
	public void setLongValue(long longValue) {
		this.longLiteral = longValue;
	}

	public long getLongValue() {
		return longLiteral;
	}
	@Override
	public int getColor() {
		return HighlightColor.longLiteral;
	}	

	private long longLiteral;

}
