package lexer;

import ast.CompilationUnitSuper;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;

/**
 * Describes an compile-time metaobject annotation.
 *
 * @author José
 *
 */

public class SymbolCyanMetaobjectAnnotation extends Symbol {

	/**
	 * @param token  is Token.annotation
	 * @param annotationName  is the name of the metaobject, "color" in @color(blue)
	 * @param postfix is bti, ati, or dsa. If it is "bti", for example, and the metaobject name, annotationName,
	 * is "init", then the annotation is "init#bti" instead of just "init" --- we did not
	 * show the parameters to this metaobject annotation.
	 * @param lineNumber
	 * @param columnNumber
	 */
	public SymbolCyanMetaobjectAnnotation(Token token, String annotationName, CompilerPhase postfix, 
			int startLine, int lineNumber, int columnNumber, int offset, boolean leftParAfterMetaobjectAnnotation, CompilationUnitSuper compilationUnit) {
		super(token, annotationName, startLine, lineNumber, columnNumber, offset, compilationUnit);
		this.name = annotationName;
		this.postfix = postfix;
		this.leftParAfterMetaobjectAnnotation = leftParAfterMetaobjectAnnotation;
	}


	public String getName() {
		return name;
	}

	public CompilerPhase getPostfix() {
		return postfix;
	}


	public boolean getLeftParAfterMetaobjectAnnotation() {
		return leftParAfterMetaobjectAnnotation;
	}

	public CyanMetaobjectWithAtAnnotation getMetaobjectAnnotation() {
		return metaobjectAnnotation;
	}


	public void setMetaobjectAnnotation(CyanMetaobjectWithAtAnnotation metaobjectAnnotation) {
		this.metaobjectAnnotation = metaobjectAnnotation;
	}
	@Override
	public int getColor() {
		return HighlightColor.cyanMetaobjectAnnotation;
	}	

	@Override
	public CyanMetaobjectAnnotation getCyanMetaobjectAnnotation() {
		return this.metaobjectAnnotation;
	}	

	/**
	 * the name of the metaobject. The same as this.getSymbolString()
	 */
	private String name;
	
	/**
	 * the metaobject name may be postfixed by postfix.getName() which is "dpa", "ati", "dsa", or "cge".
	 */
	private CompilerPhase postfix;
	
	/**
	 * true if there is a '(' just after the metaobject annotation as in <code>{@literal @}text(trim){* ... *}</code>.
	 * false otherwise as in <code>{@literal @}text (trim){* ... *}</code>. There is a space before the '('.
	 */
	private boolean leftParAfterMetaobjectAnnotation;
	
	/**
	 * the metaobject annotation associated to this symbol
	 */
	private CyanMetaobjectWithAtAnnotation metaobjectAnnotation;

}