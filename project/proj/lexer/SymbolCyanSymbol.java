/**
 *
 */
package lexer;

import ast.CompilationUnitSuper;

/** Represents a Cyan symbol such as
 *     #get
 *     #at:put:
 *     #"Hi, this is a symbol too!"
 *
 * @author José
 *
 */
public class SymbolCyanSymbol extends Symbol {

	public SymbolCyanSymbol(Token token, boolean betweenQuotes, String symbolString, int startLine,
			int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.betweenQuotes = betweenQuotes;
	}

	public boolean isBetweenQuotes() {
		return betweenQuotes;
	}
	@Override
	public int getColor() {
		return HighlightColor.cyanSymbol;
	}	

	/**
	 * true if the Symbol is between quotes, as in
	 *     #"this is a Cyan symbol"
	 *     #"+"
	 * false if the Cyan symbol is like
	 *     #at:put:
	 *     #get
	 */
	private boolean betweenQuotes;
}
