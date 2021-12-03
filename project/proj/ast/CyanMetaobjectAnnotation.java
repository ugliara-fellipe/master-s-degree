/**

 */
package ast;


import java.util.ArrayList;
import lexer.CompilerPhase;
import lexer.Symbol;
import meta.CyanMetaobject;
import saci.Env;
import saci.Tuple4;

/**
 * Represents a metaobject annotation such as
 *       @feature("green")
 *  or
 *       r"[A-Z]+[0-9]$"
 *
   @author José

 */

abstract public class CyanMetaobjectAnnotation extends Expr {

	public CyanMetaobjectAnnotation( CompilationUnitSuper compilationUnit, boolean inExpr ) {
		this.compilationUnit = compilationUnit;
		this.inExpr = inExpr;
		metaobjectAnnotationNumberByKind = -1;
		exprStatList = null;
		codeMetaobjectAnnotationParseWithCompiler = null;
		insideProjectFile = false;
	}


	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}


	/**
	 * the first symbol that starts the metaobject annotation
	   @return
	 */
	@Override
	abstract public Symbol getFirstSymbol();

	/**
	 * get the postfix associated to this metaobject annotation. For example, we can have<br>
	 * <code>
	 * var g = {@literal @}graph#ati{* 1:2, 2:3 *}<br>
	 * </code><br>
	 * The postfix is "ati".
	 */
	abstract public CompilerPhase getPostfix();


	abstract public CyanMetaobject getCyanMetaobject();


	/**
	 * symbol after the metaobject annotation. If the annotation is
	 *     <code>
	 *     @text(option)<** this is a text **>
	 *     i = 0;
	 *     </code>
	 *  then this symbol is "i"
	 */
	public Symbol getNextSymbol()  {
		return nextSymbol;
	}

	public void setNextSymbol(Symbol nextSymbol) {
		this.nextSymbol = nextSymbol;
		char []text = compilationUnit.getText();
		StringBuffer sb = new StringBuffer();
		int n = nextSymbol.getOffset();
		for ( int i = this.getFirstSymbol().getOffset(); i < n; ++ i ) {
			sb.append(text[i]);
		}
		this.originalText = sb.toString();
	}



	/**
	 * return true if this is the first metaobject annotation, in textual order, of the prototype in which the metaobject annotation is.
	 * It includes metaobject annotations put before the metaobject but attached to it.
	 */
	public boolean isFirstCall() {
		return this.metaobjectAnnotationNumber == CyanMetaobjectAnnotation.firstMetaobjectAnnotationNumber;
	}


	/**
	 * package and prototype in which the metaobject annotation is, separated by spaces such
	 * as in "cyan.lang Any". For literal objects the file name is used instead of the prototype name.
	 */

	public String getPackagePrototypeOfAnnotation() {
		return getPackageOfAnnotation() + " " + getPrototypeOfAnnotation();
	}

	/**
	 * return the package in which the metaobject annotation is OR the project name in which the annotation is
	   @return
	 */
	public String getPackageOfAnnotation() {
		return compilationUnit.getPackageName();
	}

	public String getPrototypeOfAnnotation() {
		return this.programUnit.getName();
	}



	public CompilationUnitSuper getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(CompilationUnitSuper compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public ProgramUnit getProgramUnit() {
		return programUnit;
	}

	public void setProgramUnit(ProgramUnit programUnit) {
		this.programUnit = programUnit;
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		return "";
	}

	abstract public boolean isParsedWithCompiler();

	@Override
	public void calcInternalTypes(Env env) {

		if ( isParsedWithCompiler() )
			env.pushMetaobjectAnnotationParseWithCompiler(this);
		if ( exprStatList != null ) {
			for ( ICalcInternalTypes es : exprStatList ) {
				es.calcInternalTypes(env);
			}
		}
		super.calcInternalTypes(env);
	}

	public void finalizeCalcInternalTypes(Env env) {
		if ( this.isParsedWithCompiler()  )
			env.popMetaobjectAnnotationParseWithCompiler();
	}


	public boolean getInExpr() {
		return inExpr;
	}

	public int getMetaobjectAnnotationNumber() {
		return metaobjectAnnotationNumber;
	}

	public void setMetaobjectAnnotationNumber(int metaobjectAnnotationNumber) {
		this.metaobjectAnnotationNumber = metaobjectAnnotationNumber;
	}


	/**
	 * number of the metaobject annotation in the source code, in textual order.
	 * The first metaobject annotation has number 1.
	   @return
	 */
	public int getMetaobjectAnnotationNumberByKind() {
		return metaobjectAnnotationNumberByKind;
	}

	public void setMetaobjectAnnotationNumberByKind(int metaobjectAnnotationNumberByKind) {
		this.metaobjectAnnotationNumberByKind = metaobjectAnnotationNumberByKind;
	}

	public ArrayList<ICalcInternalTypes> getExprStatList() {
		return exprStatList;
	}

	public void setExprStatList(ArrayList<ICalcInternalTypes> exprStatList) {
		this.exprStatList = exprStatList;
	}

	public Object getInfo_dpa() {
		return info_dpa;
	}

	public void setInfo_dpa(Object metaobjectAnnotationInfo_dpa) {
		this.info_dpa = metaobjectAnnotationInfo_dpa;
	}



	public StringBuffer getCodeMetaobjectAnnotationParseWithCompiler() {
		return codeMetaobjectAnnotationParseWithCompiler;
	}

	public void setCodeMetaobjectAnnotationParseWithCompiler(StringBuffer codeMetaobjectAnnotationParseWithCompiler) {
		this.codeMetaobjectAnnotationParseWithCompiler = codeMetaobjectAnnotationParseWithCompiler;
	}

	/**
	 * true if this metaobject annotation was made inside the project (.pyan) file.
	 *
	   @return
	 */
	public boolean getInsideProjectFile() {
		return insideProjectFile;
	}


	public void setInsideProjectFile(boolean insideProjectFile) {
		this.insideProjectFile = insideProjectFile;
	}


	/**
	 *
	 * This method should be called by a IDE plugin to show the text associated to this metaobject annotation
	 * in several colors (text highlighting).

	 *
	 * Each tuple (color number, line number, column number, size). <br>
	 * The characters starting at line number, column number till column number
	 * + size - 1 should be highlighted in color "color number".	 *
	 * @return
	 */
	abstract public ArrayList<Tuple4<Integer, Integer, Integer, Integer>> getColorTokenList();

	/**
	 * number of the first number of a metaobject annotation in a prototype
	 */
	public static final int firstMetaobjectAnnotationNumber = 1;

	/**
	 * the compilation unit in which this metaobject annotation is, null if none
	 */
	protected CompilationUnitSuper compilationUnit;


	/**
	 * the program unit in which this metaobject annotation is, null if none
	 */
	protected ProgramUnit programUnit;

	/**
	 * the symbol just after this metaobject annotation
	 */
	private Symbol nextSymbol;
	/**
	 * true if the metaobject annotation is inside an expression
	 */
	private boolean inExpr;

	/**
	 * The number of this metaobject annotation. To each metaobject annotation in a prototype is associated a number starting with 1.
	 * This number is used for metaobject annotation to communicate with each other. This number is for all kinds of
	 * metaobjects
	 */

	private int	metaobjectAnnotationNumber;

	/**
	 * The number of this metaobject annotation considering only metaobjects with the same name as this.
	 * To each metaobject annotation in a prototype is associated a number starting with 1.
	 * This number is used for metaobject annotation to communicate with each other.
	 */

	private int	metaobjectAnnotationNumberByKind;

	/**
	 * this metaobject annotation may refer to expressions and statements of Cyan. These are grouped in exprStatList. This list is necessary
	 * in order to do semantic analysis on them in step dsa.
	 */
	private ArrayList<ICalcInternalTypes> exprStatList;

	/**
	 * information on the DSL of the metaobject annotation gathered during parsing. It is usually
	 * the AST of the DSL associated to the metaobject. But sometimes it is not necessary to keep the AST and
	 * this variable could keep the code that should be generated during semantic analysis.
	 */
	protected Object info_dpa;

	/**
	 * if this metaobject annotation is from a metaobject that implement IParseWithCyanCompiler_dpa or IParseMacro_dpa, the code produced
	 * during phase dsa is put in this instance variable
	 */
	protected StringBuffer codeMetaobjectAnnotationParseWithCompiler;

	/**
	 * original text of some metaobject annotations. It is between first symbol and next symbol
	 */
	protected String originalText;

	/**
	 * a list of colors
	 */
	protected ArrayList<Tuple4<Integer, Integer, Integer, Integer>> colorTokenList;

	/**
	 * true if this metaobject annotation was made inside the project (.pyan) file.
	 */
	protected boolean insideProjectFile;


}

