package lexer;

import ast.CompilationUnitSuper;

/**
 * This class represents byte literals
 * @author José
 *
 */

public class SymbolByteLiteral extends Symbol {

	public SymbolByteLiteral(Token token, String symbolString, byte byteLiteral,
            int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.byteLiteral = byteLiteral;
	}
	
	public void setByteValue(byte byteValue) {
		this.byteLiteral = byteValue;
	}

	public byte getByteValue() {
		return byteLiteral;
	}
	

	@Override
	public int getColor() {
		return HighlightColor.byteLiteral;
	}	
		

	private byte byteLiteral;

}
