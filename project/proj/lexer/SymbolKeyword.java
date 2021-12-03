package lexer;

import ast.CompilationUnitSuper;

/** 
 * Keeps information on a symbol that is a keyword of Cyan such as 
 * proc, public or object. The name of the symbol is inherited from Symbol
 * 
 * @author José
 *
 */
public class SymbolKeyword extends Symbol {

	public SymbolKeyword(Token token, String symbolString, int startLine, int lineNumber, int columnNumber, 
			int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
	}

	@Override
	public int getColor() {
		return HighlightColor.keyword;
	}	
	
	
}
