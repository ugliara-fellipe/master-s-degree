/**
 * 
 */
package lexer;

import ast.CompilationUnitSuper;

/** This class represents float literals
 * @author José
 *
 */
public class SymbolFloatLiteral extends Symbol {

	/**
	 * @param symbolString
	 */
	public SymbolFloatLiteral(Token token, String symbolString, String originalFloatString, float floatLiteral,
            int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.originalFloatString = originalFloatString;
		this.floatLiteral = floatLiteral;
	
	}

	public void setFloatLiteral(float floatLiteral) {
		this.floatLiteral = floatLiteral;
	}

	public float getFloatLiteral() {
		return floatLiteral;
	}

	public String getOriginalFloatString() {
		return originalFloatString;
	}

	@Override
	public int getColor() {
		return HighlightColor.floatLiteral;
	}	

	private float floatLiteral;
	
	private String originalFloatString;


}
