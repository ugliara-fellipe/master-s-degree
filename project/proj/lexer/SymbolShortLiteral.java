/**
 * 
 */
package lexer;

import ast.CompilationUnitSuper;

/**
 * This class represents symbols that are short literals
 * @author José
 *
 */
public class SymbolShortLiteral extends Symbol {

	/**
	 * @param symbolString
	 */
	public SymbolShortLiteral(Token token, String symbolString, short shortLiteral,
            int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber,offset, compilationUnit);
		this.shortLiteral = shortLiteral;
	}

	public void setShortLiteral(short shortLiteral) {
		this.shortLiteral = shortLiteral;
	}

	public short getShortLiteral() {
		return shortLiteral;
	}
	@Override
	public int getColor() {
		return HighlightColor.shortLiteral;
	}	

	private short shortLiteral;
}
