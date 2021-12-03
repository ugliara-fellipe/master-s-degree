/**
 * 
 */
package lexer;

import ast.CompilationUnitSuper;

/** This class represents double literals
 * @author José
 *
 */
public class SymbolDoubleLiteral extends Symbol {

	/**
	 * @param symbolString
	 */
	public SymbolDoubleLiteral(Token token, String symbolString, String originalDoubleString, double doubleLiteral,
            int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.originalDoubleString = originalDoubleString;
		this.doubleLiteral = doubleLiteral;
	}
	
	public void setDoubleValue(double doubleValue) {
		this.doubleLiteral = doubleValue;
	}

	public double getDoubleValue() {
		return doubleLiteral;
	}

	private double doubleLiteral;


	public String getOriginalDoubleString() {
		return originalDoubleString;
	}

	@Override
	public int getColor() {
		return HighlightColor.doubleLiteral;
	}	
	
	private String originalDoubleString;
}
