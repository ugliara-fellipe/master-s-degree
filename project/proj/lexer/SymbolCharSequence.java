/**
  
 */
package lexer;

import ast.CompilationUnitSuper;

/** represents the char sequence of a metaobject annotation. It is
 * '<**' or '**>' in 
 *        @javacode<**  ...   **>
 *        
   @author Jos?
   
 */
public class SymbolCharSequence extends Symbol {

	public SymbolCharSequence(Token token, String symbolString, int startLine,
			int lineNumber, int columnNumber, int offset, char[] charSequence,
			int sizeCharSequence, CompilationUnitSuper compilationUnit) {
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.charSequence = charSequence;
		this.sizeCharSequence = sizeCharSequence;
	}

	public char[] getCharSequence() {
		return charSequence;
	}
	public void setCharSequence(char[] charSequence) {
		this.charSequence = charSequence;
	}
	public int getSizeCharSequence() {
		return sizeCharSequence;
	}
	public void setSizeCharSequence(int sizeCharSequence) {
		this.sizeCharSequence = sizeCharSequence;
	}
	
	
	private char []charSequence;

	private int sizeCharSequence;
}
