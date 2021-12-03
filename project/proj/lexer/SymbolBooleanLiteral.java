/**
 * 
 */
package lexer;

import ast.CompilationUnitSuper;

/** This class represents boolean literals
 * @author José
 *
 */
public class SymbolBooleanLiteral extends Symbol {

	/**
	 * @param symbolString
	 */
	public SymbolBooleanLiteral(Token token, String symbolString, boolean booleanLiteral,
			                    int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.booleanLiteral = booleanLiteral;
	}

	public void setBooleanLiteral(boolean booleanLiteral) {
		this.booleanLiteral = booleanLiteral;
	}

	public boolean isBooleanLiteral() {
		return booleanLiteral;
	}

	@Override
	public int getColor() {
		return HighlightColor.booleanLiteral;
	}	
	
	private boolean booleanLiteral;
}

