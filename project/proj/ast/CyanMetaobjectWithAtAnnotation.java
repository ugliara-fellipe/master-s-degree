/**

 */
package ast;

import java.util.ArrayList;
import java.util.Hashtable;
import error.ErrorKind;
import lexer.CompilerPhase;
import lexer.Symbol;
import lexer.SymbolCharSequence;
import lexer.SymbolCyanMetaobjectAnnotation;
import lexer.Token;
import meta.Compiler_dsa;
import meta.CyanMetaobjectWithAt;
import meta.IActionVariableDeclaration_dsa;
import meta.IAction_cge;
import meta.IAction_dsa;
import meta.ICodeg;
import meta.IParseWithCyanCompiler_dpa;
import meta.ReplacementPolicyInGenericInstantiation;
import meta.cyanLang.CyanMetaobjectCompilationContextPop;
import meta.cyanLang.CyanMetaobjectCompilationContextPush;
import meta.cyanLang.CyanMetaobjectCompilationMarkDeletedCode;
import meta.cyanLang.CyanMetaobjectJavaCode;
import saci.CompilerManager;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple4;

/** Represents a metaobject annotation such as </p>
 * <code>
 *       {@literal @}feature("author", "José") </p>
 *  </code>
 *  ou </p> <code>
 *       {@literal @}text<<* <br>
 *            This is a text which can <br>
 *            have anything but '* > >' <br>
 *            without spaces <br>
 *       *>> </p>
 *       </code>
 *
 *
   @author José

 */
public class CyanMetaobjectWithAtAnnotation extends CyanMetaobjectAnnotation {

	public CyanMetaobjectWithAtAnnotation( CompilationUnit compilationUnit,
			SymbolCyanMetaobjectAnnotation symbolCyanMetaobjectAnnotation,
			                   CyanMetaobjectWithAt cyanMetaobject, boolean inExpr ) {
		super(compilationUnit, inExpr);
		this.symbolCyanMetaobjectAnnotation = symbolCyanMetaobjectAnnotation;
		this.cyanMetaobject = cyanMetaobject;
		this.declaration = null;
		this.leftDelimArgs = null;
		this.rightDelimArgs = null;
		this.text = null;
	}

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		String cyanMetaobjectName;
		cyanMetaobjectName = symbolCyanMetaobjectAnnotation.getSymbolString();

		if ( codeMetaobjectAnnotationParseWithCompiler != null )
			pw.print(codeMetaobjectAnnotationParseWithCompiler);
		else {
			pw.print(" ");
			String at = "@";
			pw.printIdent(at + cyanMetaobjectName);
			if ( symbolCyanMetaobjectAnnotation.getPostfix() != null ) {
				pw.print("#" + symbolCyanMetaobjectAnnotation.getPostfix().getName());
			}
			if ( leftDelimArgs != null ) {
				pw.print(leftDelimArgs.getSymbolString());
				int size = realParameterList.size();
				for ( Expr e : this.realParameterList ) {

					if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
						if ( e instanceof ExprAnyLiteral  ) {
							((ExprAnyLiteral ) e).genCyanReplacingGenericParameters(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
						}
						/*else if ( e instanceof ExprIdentStar ) {
							 // replace a formal prototype parameter by a literal string containing the real parameter.
							String name = ((ExprIdentStar ) e).getName();
							String value = cyanEnv.getFormalParamToRealParamTable().get(name);
							if ( value != null )
								pw.print("\"" + value + "\"");
							else
								pw.print(name);
						} */
						else
							e.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
					}
					else
						e.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);

					if ( --size > 0 )
						pw.print(", ");
				}
				pw.print(rightDelimArgs.getSymbolString());
			}
			if ( leftCharSeqSymbol != null ) {
				pw.print( leftCharSeqSymbol.getSymbolString() );


				if ( text[text.length - 1] == '\0' )
					text[text.length - 1] = ' ';
				if ( ! cyanEnv.getCreatingInstanceGenericPrototype() )
				    pw.println(text);
				else {
					switch ( cyanMetaobject.getReplacementPolicy() ) {
					case NO_REPLACEMENT:
						pw.println(text);
						break;
					case REPLACE_BY_CYAN_VALUE:
						replacePrint(text, pw, cyanEnv.getFormalParamToRealParamTable(), "",
								ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE);
						break;
					case REPLACE_BY_JAVA_VALUE:
						String genProto = cyanEnv.getExprGenericPrototypeInstantiation().getName();
						replacePrint(text, pw, cyanEnv.getFormalParamToRealParamTable(), cyanEnv.getExprGenericPrototypeInstantiation().getName(),
								ReplacementPolicyInGenericInstantiation.REPLACE_BY_JAVA_VALUE);
					}
				}

				/*
				StringBuffer strText = new StringBuffer(text.toString());
				if ( strText != null && strText.length() > 0 ) {
					strText.trimToSize();
					int last = strText.length() - 1;
				    if ( strText.charAt(last) == '\0' )
					    strText.setCharAt(last, ' ');
					if ( cyanEnv == null )
					    pw.println(strText);
					else {
					    char []charText = new char[strText.length()];
						strText.getChars(0, strText.length(), charText, 0);
						replacePrint(charText, pw, cyanEnv.getFormalRealTable());
					}
				} */
				pw.printlnIdent(rightCharSeqSymbol.getSymbolString());
			}
			pw.println(" ");
		}


	}


	@Override
	public void genJava(PWInterface pw, Env env) {
		if ( cyanMetaobject instanceof IAction_cge ) {
			cyanMetaobject.setMetaobjectAnnotation(this);
			StringBuffer strText = null;


			try {
				strText = ( (IAction_cge ) cyanMetaobject).cge_codeToAdd();
			}
			catch ( error.CompileErrorException e ) {
			}
			catch ( RuntimeException e ) {
				env.thrownException(this, this.getFirstSymbol(), e);
			}
			finally {
				env.errorInMetaobjectCatchExceptions(cyanMetaobject, this);
			}


			if (  strText != null ) {
				int last = strText.length() - 1;
				if ( strText.charAt(last) == '\0' )
					strText.setCharAt(last, ' ');
				int size = strText.length();
				String s = "";
				for ( int i = 0; i < size; ++i ) {
					char ch = strText.charAt(i);
					if( Character.LINE_SEPARATOR == Character.getType(ch) ) {
						pw.printlnIdent(s);
						s = "";
					}
					s = s + ch;
				}
				pw.printlnIdent(s);
			}
		}

	}



	@Override
	public String genJavaExpr(PWInterface pw, Env env) {

		env.error(this.getFirstSymbol(),  "Internal error: a metaobject is being used as an expression and the compiler wants it to generate Java code."
				+ " The metaobject annotation should have been replaced by Cyan code in previous phases.");
		return "/* this should not be used as variable */";
	}


	@Override
	public boolean isParsedWithCompiler() {
		return this.cyanMetaobject instanceof IParseWithCyanCompiler_dpa;
	}


	@Override
	public void calcInternalTypes(Env env) {

		super.calcInternalTypes(env);

		if ( realParameterList != null ) {
			for ( Expr e : this.realParameterList ) {
				if ( e instanceof ExprLiteralArray || e instanceof ExprLiteralTuple ) {
					/*
					if (  e instanceof ExprLiteralTuple ) {
						ExprLiteralTuple t = (ExprLiteralTuple ) e;
					}
					*/
					e.calcInternalTypes(env);
				}
			}
		}


		if ( cyanMetaobject instanceof CyanMetaobjectCompilationContextPush ) {
			/**
			 * The metaobject annotation may have only two parameters. See {@link meta#CyanMetaobjectCompilationContextPush}. In this case the compiler
			 * has introduced <code>null</code> in the last three positions of
			 * the javaParameterList.
			 */
			String id = (String ) javaParameterList.get(0);


			String cyanMetaobjectName = (String ) javaParameterList.get(1);
			String packageName;
			String sourceFileName;
			int lineNumber = 0;
			if ( javaParameterList.size() <= 2 || javaParameterList.get(2) == null ) {
				if ( env.getCurrentProgramUnit() == null )
					packageName = "Unidentified";
				else
					packageName = env.getCurrentCompilationUnit().getPackageName();
			}
			else {
				packageName = (String ) javaParameterList.get(2);
			}

			if ( javaParameterList.size() <= 3 || javaParameterList.get(3) == null ) {
				if ( env.getCurrentProgramUnit() == null )
					sourceFileName = "Unidentified";
				else
					sourceFileName = env.getCurrentProgramUnit().getName();
			}
			else {
				sourceFileName = (String ) javaParameterList.get(3);
			}

			if ( javaParameterList.size() <= 4 || javaParameterList.get(4) == null ) {
                lineNumber = -1;
			}
			else {
                lineNumber = (Integer ) javaParameterList.get(4);
			}

			env.pushCompilationContext(id, cyanMetaobjectName, packageName, sourceFileName, lineNumber, this.symbolCyanMetaobjectAnnotation.getOffset());
		}
		else if ( cyanMetaobject instanceof CyanMetaobjectCompilationContextPop ) {
			String id = (String ) javaParameterList.get(0);
			env.popCompilationContext(id, this.symbolCyanMetaobjectAnnotation);
		}
		else if ( cyanMetaobject instanceof CyanMetaobjectCompilationMarkDeletedCode ) {
			env.addToLineShift(-((CyanMetaobjectCompilationMarkDeletedCode) cyanMetaobject).getNumLinesDeleted());
		}
		else if ( cyanMetaobject instanceof CyanMetaobjectJavaCode ) {
			env.setHasJavaCode(true);
		}
		/* else
		if ( cyanMetaobject instanceof CyanMetaobjectCompilationMarkDeletedCode ) {
			env.markDeletedCodeCompilation( ((CyanMetaobjectCompilationMarkDeletedCode) cyanMetaobject).getNumLinesDeleted() );
		} */

		if ( env.getCompInstSet().contains(saci.CompilationInstruction.dsa_actions) ) {
			if ( cyanMetaobject instanceof IAction_dsa ) {

				if ( env.getDuring_dsa_actions() ) {
					env.error(this.getFirstSymbol(), "A dsa action cannot occur inside another dsa actions. For example, you cannot have a macro expansion inside another macro expansion or even a literal object as r\"[a-z]+\" inside a macro");
				}
				try {
					env.begin_dsa_actions();
					IAction_dsa cyanMetaobjectCodeGen = (IAction_dsa ) cyanMetaobject;
					cyanMetaobject.setMetaobjectAnnotation(this);
					Compiler_dsa compiler_dsa = new Compiler_dsa(env, this);
					StringBuffer cyanCode = null;

					try {
						cyanCode = cyanMetaobjectCodeGen.dsa_codeToAdd(compiler_dsa);

					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						e.printStackTrace();
						env.thrownException(this, this.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, this);
					}

					/*

						env.error(this.getFirstSymbol(), "This metaobject implements interface '" + IAction_dsa.class.getName() + "' "
								+ "but it is not generating code in phase DSA");

										 *
					 */

					if ( cyanCode == null || cyanCode.length() == 0 || cyanCode.charAt(0) == '\0' ) {
						if ( this.symbolCyanMetaobjectAnnotation.getPostfix() == null ) {
							// this metaobject did not generated code in previous compilation phases.
							// Then it should generated in this phase.
							if ( this.cyanMetaobject.isExpression() ) {
								if ( this.cyanMetaobject instanceof meta.ICodeg ) {
									env.error(this.getFirstSymbol(),
											"Codeg metaobject '" + this.cyanMetaobject.getName() +
											    "' is not generating code in phase dsa as expected. This probably is because " +
											"this Codeg was not initialized at editing time. Just put the mouse over it to edit it");
								}
								else {
									env.error(this.getFirstSymbol(),  "Metaobject '" + this.cyanMetaobject.getName() + "' is not generating code in phase dsa as expected");
								}
							}
						}
					}
					else {

						if ( cyanCode.charAt(cyanCode.length() - 1) == '\0' )
							cyanCode.deleteCharAt(cyanCode.length()-1);

						if ( this.isParsedWithCompiler() &&
								env.sizeStackMetaobjectAnnotationParseWithCompiler() > 1 ) {
							/*
							 * this metaobject annotation is a literal object that is inside other literal object
							 */
							this.setCodeMetaobjectAnnotationParseWithCompiler(cyanCode);
						}
						else {
							env.removeCodeMetaobjectAnnotation(cyanMetaobject);
							env.addCodeAtMetaobjectAnnotation(cyanMetaobject, cyanCode, -1);
							this.codeThatReplacesThisExpr = cyanCode;

						}

					}

					ArrayList<Tuple2<String, StringBuffer>> prototypeNameCodeList = null;
					try {
						prototypeNameCodeList = cyanMetaobjectCodeGen.dsa_NewPrototypeList(compiler_dsa);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(this, this.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, this);
					}
					if ( prototypeNameCodeList != null ) {
						for ( Tuple2<String, StringBuffer> prototypeNameCode : prototypeNameCodeList ) {
							CompilationUnit cunit = (CompilationUnit ) this.compilationUnit;
							Tuple2<CompilationUnit, String> t = env.getProject().getCompilerManager().createNewPrototype(prototypeNameCode.f1, prototypeNameCode.f2,
									cunit.getCompilerOptions(), cunit.getCyanPackage());
							if ( t != null && t.f2 != null ) {
								env.error(this.getFirstSymbol(), t.f2);
							}
						}
					}
				}
				finally {
					env.end_dsa_actions();
				}

			}
			if ( cyanMetaobject instanceof IActionVariableDeclaration_dsa ) {
				/*
				 * add code after the local variable declaration
				 */
				IActionVariableDeclaration_dsa actionVar = (IActionVariableDeclaration_dsa ) cyanMetaobject;
				Declaration dec = this.getDeclaration();
				if ( !(dec instanceof StatementLocalVariableDecList) ) {
					env.error(this.getFirstSymbol(),  "Metaobject '" + cyanMetaobject.getName() + "' can only be attached to declaration of local variables");
				}
				StatementLocalVariableDecList stat = (StatementLocalVariableDecList ) dec;
				Symbol symbolAfter = stat.getSymbolAfter();
				int offset = symbolAfter.getOffset();
				if ( symbolAfter.token == Token.SEMICOLON ) {
					++offset;
				}
				StringBuffer code = null;


				try {
					code = actionVar.dsa_codeToAddAfter();
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(this, this.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, this);
				}
				env.addCodeAtMetaobjectAnnotation(cyanMetaobject, code, offset);
			}
		}

		ProgramUnit pu;
		if ( cyanMetaobject.getPackageOfType() == null || cyanMetaobject.getPrototypeOfType() == null ) {
			pu = env.searchPackagePrototype(NameServer.cyanLanguagePackageName, "Nil");
		}
		else {
			pu = env.searchPackagePrototype(cyanMetaobject.getPackageOfType(), cyanMetaobject.getPrototypeOfType());
		}

		if ( pu == null ) {
			pu = env.searchPackagePrototype(cyanMetaobject.getPackageOfType(), cyanMetaobject.getPrototypeOfType());
			env.error(true,
					this.getFirstSymbol(),
							"Metaobject has type '" + cyanMetaobject.getPackageOfType() + "." +
									cyanMetaobject.getPrototypeOfType() + "' which was not found", cyanMetaobject.getPrototypeOfType(), ErrorKind.prototype_was_not_found_inside_method);
		}
		else
			type = pu;

		finalizeCalcInternalTypes(env);
	}



	public static void replacePrint(char []text,  PWInterface pw, Hashtable<String, String> formalRealTable, String currentPrototypeName,
			ReplacementPolicyInGenericInstantiation replacementPolicy) {
		pw.print(CompilerManager.replaceOnly(text, formalRealTable, currentPrototypeName, replacementPolicy));
	}

	public static void copyCharArray(char []target, char []source) {
		System.arraycopy(source, 0, target, 0, source.length);
		/*
		for (int i = 0; i < source.length; ++i)
			target[i] = source[i];
		*/
	}


	@Override
	public CyanMetaobjectWithAt getCyanMetaobject() {
		return cyanMetaobject;
	}


	public void setCyanMetaobject(CyanMetaobjectWithAt cyanMetaobject) {
		this.cyanMetaobject = cyanMetaobject;
	}


	@Override
	public  Symbol getFirstSymbol() {
		return this.symbolCyanMetaobjectAnnotation;
	}

	public Declaration getDeclaration() {
		if ( !(declaration instanceof Declaration) )
			return null;
		else
			return (Declaration ) declaration;
	}

	public Expr getAttachedExpr() {
		if ( !(declaration instanceof Declaration) )
			return null;
		else
			return (Expr ) declaration;
	}

	public void setDeclaration(Declaration declaration) {
		this.declaration = declaration;
	}

	public ArrayList<ExprAnyLiteral> getRealParameterList() {
		return realParameterList;
	}


	public void setRealParameterList(ArrayList<ExprAnyLiteral> realParameterList) {
		this.realParameterList = realParameterList;
	}

	public ArrayList<Object> getJavaParameterList() {
		return javaParameterList;
	}

	/**
	 * Return the Java parameter of this metaobject annotation as a valid Java string.
	 * That is, if the parameter is an int, as 123, "123" is returned. If it
	 * is a string "ok",  "\"ok\"" is returned.
	   @param i
	   @return
	 */
	public String javaParameterAt(int i) {
		return convert(javaParameterList.get(i));
	}

	private static String convert(Object param) {
		if ( param instanceof String )
			return "\"" + ((String ) param) + "\"";
		else
			return ((Integer) param).toString();
	}

	public void setJavaParameterList(ArrayList<Object> javaParameterList) {
		this.javaParameterList = javaParameterList;
	}


	public SymbolCyanMetaobjectAnnotation getSymbolMetaobjectAnnotation() {
		return symbolCyanMetaobjectAnnotation;
	}

	public void setSymbolCyanMetaobjectAnnotation(SymbolCyanMetaobjectAnnotation symbolCyanMetaobjectAnnotation) {
		this.symbolCyanMetaobjectAnnotation = symbolCyanMetaobjectAnnotation;
	}

	public Symbol getLeftDelimArgs() {
		return leftDelimArgs;
	}

	public void setLeftDelimArgs(Symbol leftDelimArgs) {
		this.leftDelimArgs = leftDelimArgs;
	}

	public Symbol getRightDelimArgs() {
		return rightDelimArgs;
	}

	public void setRightDelimArgs(Symbol rightDelimArgs) {
		this.rightDelimArgs = rightDelimArgs;
	}

	public char[] getText() {
		return text;
	}

	public void setText(char[] text) {
		this.text = text;
	}

	public SymbolCharSequence getLeftCharSeqSymbol() {
		return leftCharSeqSymbol;
	}

	public void setLeftCharSeqSymbol(SymbolCharSequence leftCharSeqSymbol) {
		this.leftCharSeqSymbol = leftCharSeqSymbol;
	}

	public SymbolCharSequence getRightCharSeqSymbol() {
		return rightCharSeqSymbol;
	}

	public void setRightCharSeqSymbol(SymbolCharSequence rightCharSeqSymbol) {
		this.rightCharSeqSymbol = rightCharSeqSymbol;
	}


	@Override
	public CompilationUnitSuper getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	public ProgramUnit getProgramUnit() {
		return programUnit;
	}

	@Override
	public CompilerPhase getPostfix() {
		return symbolCyanMetaobjectAnnotation.getPostfix();
	}





	public byte []getCodegInfo() {
		return codegInfo;
	}

	public void setCodegInfo(byte []codegInfo) {
		this.codegInfo = codegInfo;
	}


	/**
	 * the complete name of the metaobject associated to this annotation. If the metaobject is a codeg,
	 * it is returned the metaobject name and the first parameter. Something like<br>
	 * <code>"color(red)"</code><br>
	 * If the metaobject is not a codeg, the metaobject name is returned.
	 */
	public String getCompleteName() {
		if ( this.cyanMetaobject instanceof ICodeg ) {
			String id = ((ExprAnyLiteralIdent ) realParameterList.get(0)).getIdentExpr().getName();

			String codegInfoFilename = this.cyanMetaobject.getName() +
					"(" + id + ")" ;
			return codegInfoFilename;
		}
		else {
			return this.cyanMetaobject.getName();
		}
	}

	/**
	 * return the complete path of the file that keeps information on this metaobject annotation.
	 * Usually this is only used for codegs. Returns null if this annotation is outside a compilation unit (this
	 * should never happens
	 */
	public String filenameMetaobjectAnnotationInfo() {
		if ( compilationUnit == null )
			return null;
		String ext = "txt";
		String id = "";
		if ( this.cyanMetaobject instanceof ICodeg ) {
			ext = ((ICodeg ) this.cyanMetaobject).getFileInfoExtension();
			if ( this.getRealParameterList().get(0) instanceof ExprAnyLiteralIdent ) {
				id = ((ExprAnyLiteralIdent ) this.getRealParameterList().get(0)).getIdentExpr().getName();
			}
		}
		String codegPath = this.compilationUnit.getCanonicalPathUpDir() + NameServer.getCodegDirFor(this.getPrototypeOfAnnotation());


		String codegInfoFilename = codegPath + NameServer.fileSeparatorAsString + this.cyanMetaobject.getName() +
				"(" + id + ")." + ext;
		return codegInfoFilename;
	}



	@Override
	public ArrayList<Tuple4<Integer, Integer, Integer, Integer>> getColorTokenList() {
		if ( colorTokenList == null ) {
			if ( this.cyanMetaobject != null ) {
				colorTokenList = this.cyanMetaobject.getColorList(this);
			}
		}
		return colorTokenList;
	}

	public void setColorTokenList(ArrayList<Tuple4<Integer, Integer, Integer, Integer>> colorTokenList) {
		this.colorTokenList = colorTokenList;
	}


	public ArrayList<Tuple2<String, String>> getLocalVariableNameList() {
		return localVariableNameList;
	}

	public void setLocalVariableNameList(ArrayList<Tuple2<String, String>> localVariableNameList2) {
		this.localVariableNameList = localVariableNameList2;
	}


	/**
	 * The Java class of this metaobject
	 */
	protected CyanMetaobjectWithAt	cyanMetaobject;
	/**
	 * the declaration or expression associated to this metaobject annotation or null if there is
	 * no one. A declaration can be a prototype, instance variable, or method.
	 * Therefore 'declaration' can be an object of ProgramUnit, InstanceVariableDec,
	 * MethodDec, OR an expression.
	 */

	protected Object declaration;

	/**
	 * the symbol 'javacode' in @javacode
	 */
	private SymbolCyanMetaobjectAnnotation symbolCyanMetaobjectAnnotation;


	/**
	 * the arguments to this metaobject annotation. The elements of
	 * ArrayList may be objects of String, Integer, Float etc.
	 * In<br>
	 * <code>
	 *      {@literal @}feature( "author", "José")<br>
	 * </code>
	 * there would be a list of two literal strings	 *
	 */

	private ArrayList<Object> javaParameterList;



	/**
	 * the arguments to this metaobject annotation as AST objects.
	 * In
	 *      @feature<<* "author", "José" *>>
	 * there would be a list of two literal strings, objects
	 * of ExprLiteralString
	 */

	private ArrayList<ExprAnyLiteral> realParameterList;

	/**
	 * if this metaobject annotation takes parameters, this symbol is the '(' '[' or '{'
	 * that comes before the parameters. It would be '(' in the annotation below.
	 *      @text(trim_spaces)<**  ... **>
	 *
	 */
	private Symbol leftDelimArgs;



	/**
	 * if this metaobject annotation takes parameters, this symbol is the ')' ']' or '}'
	 * that comes after the parameters. It would be ')' in the annotation below.
	 *      @text(trim_spaces)<**  ... **>
	 *
	 */
	private Symbol rightDelimArgs;

	/**
	 * this is the symbol '<**' in
	 *      @text(trim_spaces)<**  ... **>
	 */

	private SymbolCharSequence leftCharSeqSymbol;


	/**
	 * this is the symbol '<**' in
	 *      @text(trim_spaces)<**  ... **>
	 */
	private SymbolCharSequence rightCharSeqSymbol;

	/**
	 * the text of the metaobject annotation. It is the text between <** and **>
	 * in the annotation below.
	 *
	 *      @javacode<**
	 *           return _add_dot(_a);
	 *      **>
	 */
	private char []text;

	/**
	 * if the metaobject is a Codeg, variable {@link #codegInfo} is not null and receives the data of
	 * the file associated to this metaobject annotation. This data is produced at editing time and
	 * stored in a file. During compiling time this data is retrieved from the file and stored in
	 * this variable. The compiler discovers the name of this file using the current prototype name, its directory,
	 * the metaobject name, and the first parameter of the metaobject.
	 *
	 */
	private byte []codegInfo;


	/**
	 * the stack of local variables visible where the metaobject annotation is.
	 */
	private ArrayList<Tuple2<String, String>> localVariableNameList;

}

