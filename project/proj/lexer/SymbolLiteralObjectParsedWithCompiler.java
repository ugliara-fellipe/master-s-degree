package lexer;

import ast.CompilationUnitSuper;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectLiteralObjectAnnotation;

/**
 * represents a literal object such as
 *  <br>
* <code>
*     {* 1:2, 2:3, 3:1 *} <br>
*     [* "one":1, "two":2 *] <br>
* </code><br>
   * that is parsed with the help of the Cyan compiler. This mean this class 
   * only keeps information on the starting sequence of symbols. In the 
   * examples, <code>{*</code> and <code>[*</code>. 
   @author José
 */
public class SymbolLiteralObjectParsedWithCompiler extends Symbol implements IWithCompilerPhase {

	public SymbolLiteralObjectParsedWithCompiler(Token token, CyanMetaobjectLiteralObjectAnnotation cyanMetaobjectLiteralObjectAnnotation, 
			CompilerPhase compilerPhase,
			String symbolString,
			int startLine, int lineNumber, int columnNumber, int offset, CompilationUnitSuper compilationUnit) {
		/*
		 * 	public SymbolLiteralObject(Token token, CyanMetaobjectLiteralObjectAnnotation cyanMetaobjectLiteralObjectAnnotation,
			CompilerPhase postfix,
			String symbolString,
			String usefulString,
			char []text,
			int startOffsetLine, int lineNumber, int columnNumber, int offset) {

		 
		super(token, cyanMetaobjectLiteralObjectAnnotation, compilerPhase, symbolString, null, null, )  */
		super(token, symbolString, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.cyanMetaobjectLiteralObjectAnnotation = cyanMetaobjectLiteralObjectAnnotation;
		this.postfix = compilerPhase;
	}
	

	public CyanMetaobjectLiteralObjectAnnotation getCyanMetaobjectLiteralObjectAnnotation() {
		return cyanMetaobjectLiteralObjectAnnotation;
	}

	@Override
	public CompilerPhase getPostfix() {
		return postfix;
	}
	@Override
	public int getColor() {
		return HighlightColor.literalObjectParsedWithCompiler;
	}	

	@Override
	public CyanMetaobjectAnnotation getCyanMetaobjectAnnotation() {
		return this.cyanMetaobjectLiteralObjectAnnotation;
	}	

	
	
	/**
	 * represents the metaobject associated to this literal object 
	 */
	private CyanMetaobjectLiteralObjectAnnotation cyanMetaobjectLiteralObjectAnnotation;
	/**
	 * the metaobject name may be postfixed by postfix.getName() which is "dpa", "ati", "dsa", or "cge".
	 */
	private CompilerPhase postfix;
}
