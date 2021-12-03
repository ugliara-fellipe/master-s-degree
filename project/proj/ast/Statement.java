/**
 *
 */
package ast;

import java.util.HashSet;
import java.util.Set;
import lexer.Symbol;
import meta.IStatement;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/** This class is the superclass of all classes that represent statements
 *
 * @author José
 *
 */
public abstract class Statement implements GenCyan, CodeWithError, ICalcInternalTypes, IStatement, ASTNode {

	public Statement() {
		this.shouldBeFollowedBySemicolon = true;
		setProducedByMetaobjectAnnotation(false);
	}

	public Statement(boolean shouldBeFollowedBySemicolon) {
		this.shouldBeFollowedBySemicolon = shouldBeFollowedBySemicolon;
	}
	
	
	@Override
	public void accept(ASTVisitor visitor) {
	}
		
	/**
	 * return true if this object may be a statement. For example, a literal integer is an expression but it cannot be a statement. 
	 * A identifier may be a statement if it is a unary method call. Or it may be not if it is an identifier.
	   @return
	 */
	public boolean mayBeStatement() {
		return true;
	}	
	
	/**
	 * return true if a ';' should be added at the end of code generation to Java
	 */
	public boolean addSemicolonJavaCode() {
		return false;
	}
	
	// abstract public void genCyan(PWInterface pw, CyanEnv cyanEnv);

	@Override
	final public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		if ( this.codeThatReplacesThisExpr != null ) {
			pw.print(this.codeThatReplacesThisExpr);
		}
		else {
			this.genCyanReal(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		}
	}

	abstract public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions);
	/**
	 * return the first symbol of this statement. For example, if the statement is
	 *     a[i] = 0;
	 * the symbol returned is  that corresponding to variable "a"
	 *
	 * @return
	 */
	@Override
	abstract public Symbol getFirstSymbol();
	
	@Override
	public Symbol getLastSymbol() {
		return lastSymbol;
	}


	abstract public void genJava(PWInterface pw, Env env);



	public void setLastSymbol(Symbol lastSymbol) {
		this.lastSymbol = lastSymbol;
	}
	
	@Override
	public void calcInternalTypes(Env env) { 
		if ( afterMetaobjectAnnotation != null ) {
			afterMetaobjectAnnotation.calcInternalTypes(env);
		}
	}

	/**
	 * return true if this statement demand a <code>'{@literal ;}'</code> after it
	 */
	public boolean demandSemicolon() { return shouldBeFollowedBySemicolon; }
	
	
	public String asString(CyanEnv cyanEnv) {
		PWCharArray pwChar = new PWCharArray();
		genCyan(pwChar, true, cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}
	
	@Override
	public String asString() {
		return asString(NameServer.cyanEnv);
	
	}
	
	public String genJavaAsString(Env env) {
		PWCharArray pwChar = new PWCharArray();
		genJava(pwChar, env);
		return pwChar.getGeneratedString().toString();
	}
	
	/**
	 * return true if this statement always execute a 'return' statement 
	   @return
	 */
	public boolean alwaysReturn() {
		return false;
	}

	
	public void prepareLiveAnalysis() {
		inLiveAnalysis = new HashSet<>();
		outLiveAnalysis = new HashSet<>();
		useLiveAnalysis = new HashSet<>();
		defLiveAnalysis = new HashSet<>();
	}
	
	public Set<Statement> successors() {
		return null;
	}
	
	public boolean getShouldBeFollowedBySemicolon() {
		return shouldBeFollowedBySemicolon;
	}

	public void setShouldBeFollowedBySemicolon(boolean shouldBeFollowedBySemicolon) {
		this.shouldBeFollowedBySemicolon = shouldBeFollowedBySemicolon;
	}
	
	/**
	 * return true if this statement do return; that is, the execution
	 * continue past it.
	 * For example, a message send with selector 'throw' do 
	 * not return.
	   @return
	 */
	public boolean statementDoReturn() {
		return false;
	}
	
	
	/**
	 * generate code for an unary message send to a variable whose name is nameVar of type Dyn (Object in Java).
	 * Return the name of the local variable in Java that keeps the return value of the method called. 
	 * 
	 */
	static public String genJavaDynamicUnaryMessageSend(PWInterface pw, String nameVar, String unaryMessage, Env env, int lineNumber) {

		
		String aMethodTmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("java.lang.reflect.Method " + aMethodTmp + " = CyanRuntime.getJavaMethodByName(" + nameVar + ".getClass(), \"" + 
				unaryMessage + "\", 0);");
		pw.printlnIdent("if (" + aMethodTmp + 
				" == null) throw new ExceptionContainer__( new _ExceptionMethodNotFound( new CyString(\"Method called at line \" + " + lineNumber +  
				"+ \" of prototype '" + env.getCurrentProgramUnit().getFullName() + "' was not found\") ) );"); 
		String tmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("Object " + tmp + " = null;");
		pw.printlnIdent("try {");
		pw.add();
		pw.printlnIdent(aMethodTmp + ".setAccessible(true);");
		
		pw.printlnIdent(tmp + " = " + aMethodTmp + ".invoke(" + nameVar + ");");
		pw.sub();
		pw.printlnIdent("}");
		
		String ep = NameServer.nextJavaLocalVariableName();
		
		pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep +" ) {");
        pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
        pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
        pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
        pw.printlnIdent("	}");
        pw.printlnIdent("	else"); 
        pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
        pw.printlnIdent("}");
		pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
		pw.add();
		
		String dnuTmpVar = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("//	func doesNotUnderstand: (CySymbol methodName, Array<Array<Dyn>> args)");
		pw.printlnIdent("java.lang.reflect.Method " + dnuTmpVar + " = CyanRuntime.getJavaMethodByName(" + nameVar + ".getClass(), \"" + 
		         NameServer.javaNameDoesNotUnderstand + "\", 2);");
		pw.printlnIdent(tmp + " = null;");
		pw.printlnIdent("try {");
		pw.add();
		pw.printlnIdent(aMethodTmp + ".setAccessible(true);");
		pw.printlnIdent(tmp + " = " + aMethodTmp + ".invoke(" + nameVar + ");");
		pw.sub();
		
		pw.printlnIdent("}");
		ep = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep + " ) {");
        pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
        pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
        pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
        pw.printlnIdent("	}");
        pw.printlnIdent("	else"); 
        pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
        pw.printlnIdent("}");
		pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
		pw.printlnIdent("        throw new ExceptionContainer__( new _ExceptionMethodNotFound( new CyString(\"Method called at line \" + " + lineNumber +  
				"+ \" of prototype '" + env.getCurrentProgramUnit().getFullName() + "' was not found\") ) );");
		
		
		pw.printlnIdent("}");
		pw.sub();
		pw.printlnIdent("}");
		
		return tmp;
	}
	
	/**
	 * generate code for a message send to a variable whose name is nameVar of type Dyn (Object in Java).
	 * Return the name of the local variable in Java that keeps the return value of the method called. 
	 * 
	 */
	static public String genJavaDynamicSelectorMessageSend(PWInterface pw, String nameVar, String methodJavaName, 
			String commaParameterList, int numParam, Env env, int lineNumber) {

			//  indexedExpr.getType() == Type.Dyn
			
		String aMethodTmp = NameServer.nextJavaLocalVariableName();
		
		pw.printlnIdent("java.lang.reflect.Method " + aMethodTmp + " = CyanRuntime.getJavaMethodByName(" + nameVar + 
				".getClass(), \"" + 
				methodJavaName  + "\", " + numParam + ");");
		pw.printlnIdent("if ( " + aMethodTmp + " == null ) throw new ExceptionContainer__( new _ExceptionMethodNotFound( new CyString(\"Method called at line \" + " + lineNumber +  
				"+ \" of prototype '" + env.getCurrentProgramUnit().getFullName() + "' was not found\") ) );");
		
 
		
		
		String resultTmpVar = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("Object " + resultTmpVar + " = null;");
		pw.printlnIdent("try {");
		pw.add();
		
		
		pw.printlnIdent(aMethodTmp + ".setAccessible(true);");
		pw.printlnIdent(resultTmpVar + " = " + aMethodTmp + ".invoke(" + nameVar  + ", " + commaParameterList +
				 ");");
		pw.sub();
		pw.printlnIdent("}");
		
		String ep = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep + " ) {");
        pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
        pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
        pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
        pw.printlnIdent("	}");
        pw.printlnIdent("	else"); 
        pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
        pw.printlnIdent("}");
		
		ep = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
		pw.add();
		
		String dnuTmpVar = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("//	func doesNotUnderstand: (CySymbol methodName, Array<Array<Dyn>> args)");
		pw.printlnIdent("java.lang.reflect.Method " + dnuTmpVar + " = CyanRuntime.getJavaMethodByName(" + 
				nameVar + ".getClass(), \"" + 
		       NameServer.javaNameDoesNotUnderstand  			+ "\", 2);");
		pw.printlnIdent("try {");
		pw.add();
		pw.printlnIdent(dnuTmpVar + ".setAccessible(true);");
		
		pw.printlnIdent(resultTmpVar + " = " + dnuTmpVar + ".invoke(" + nameVar + ", \"" + methodJavaName + "\", " + commaParameterList +
				 ");");
		pw.sub();
		
		pw.printlnIdent("}");
		ep = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep + " ) {");
        pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
        pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
        pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
        pw.printlnIdent("	}");
        pw.printlnIdent("	else"); 
        pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
        pw.printlnIdent("}");
		pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
		pw.printlnIdent("        throw new ExceptionContainer__( new _ExceptionMethodNotFound( new CyString(\"Method called at line \" + " + lineNumber +  
				"+ \" of prototype '" + env.getCurrentProgramUnit().getFullName() + "' was not found\") ) );");
		pw.printlnIdent("}");
		pw.sub();
		pw.printlnIdent("}");
		return resultTmpVar;
				
	}

	
	public CyanMetaobjectWithAtAnnotation getAfterMetaobjectAnnotation() {
		return afterMetaobjectAnnotation;
	}

	public void setAfterMetaobjectAnnotation(CyanMetaobjectWithAtAnnotation afterMetaobjectAnnotation) {
		this.afterMetaobjectAnnotation = afterMetaobjectAnnotation;
	}	
	
	public Symbol getSymbolAfter() {
		return symbolAfter;
	}

	public void setSymbolAfter(Symbol symbolAfter) {
		this.symbolAfter = symbolAfter;
	}

	public boolean getProducedByMetaobjectAnnotation() {
		return producedByMetaobjectAnnotation;
	}

	public void setProducedByMetaobjectAnnotation(boolean producedByMetaobjectAnnotation) {
		this.producedByMetaobjectAnnotation = producedByMetaobjectAnnotation;
	}

	/**
	 * last symbol of the statement
	 */
	private Symbol lastSymbol;
	
	/**
	 * symbol that follows the statement. It may be ';'.  
	 */
	private Symbol symbolAfter;

	/**
	 * used of live variable analysis. See https://www.cs.cornell.edu/courses/cs4120/2011fa/lectures/lec21-fa11.pdf
	 */
	public Set<String> inLiveAnalysis, outLiveAnalysis, useLiveAnalysis, defLiveAnalysis;

	/**
	 * true if this statement should have a semicolon after it
	 */
	private boolean shouldBeFollowedBySemicolon;

	/**
	 * metaobject annotation that follows this statement
	 */
	private CyanMetaobjectWithAtAnnotation afterMetaobjectAnnotation;

	/**
	 * true if this statement was produced by a metaobject annotation. That is, it is inside a contextPush and contextPop
	 */
	private boolean producedByMetaobjectAnnotation;

	/**
	 * the code that should replace this expression. Some metaobject or the compiler itself changed the
	 * expression by this code 
	 */
	StringBuffer codeThatReplacesThisExpr;

}
