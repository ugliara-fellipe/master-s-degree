/**
 * 
 */
package lexer;

import ast.CompilationUnitSuper;

/** This class represents a literal string 
 * @author José
 *
 */
public class SymbolStringLiteral extends Symbol {

	/**
	 * @param token
	 * @param symbolString
	 * @param lineNumber
	 * @param columnNumber
	 */
	public SymbolStringLiteral(Token token, String symbolString,
			int startLine, int lineNumber, int columnNumber, int offset, String javaString, 
			CompilationUnitSuper compilationUnit, boolean tripleQuote) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.javaString = javaString;
		this.tripleQuote = tripleQuote;
	}

	public String getJavaString() {
		return javaString;
	}
	
	/**
	 * return true if this symbol represents a string that starts and ends with """
	   @return
	 */
	public boolean getTripleQuote() {
		return tripleQuote;
	}
	@Override
	public int getColor() {
		return HighlightColor.stringLiteral;
	}	

	private String javaString;
	private boolean tripleQuote;
}
