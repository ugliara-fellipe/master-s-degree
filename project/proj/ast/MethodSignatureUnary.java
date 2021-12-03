/**
   Represents a method signature of a unary method such as
          fun getName -> String
          fun set

   The method name cannot be an operator. For operators, use class
   MethodSignatureOperator instead.

 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
 * @author José
 *
 */
public class MethodSignatureUnary extends MethodSignature {

	public MethodSignatureUnary(Symbol methodName, MethodDec method) {
		super(method);
		this.methodName = methodName;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.preVisit(this);
		
		visitor.visit(this);
	}	
	
	
	public void setMethodName(Symbol methodName) {
		this.methodName = methodName;
	}

	public Symbol getMethodName() {
		return methodName;
	}

	@Override 
	public void calcInternalTypes(Env env) {	
		super.calcInterfaceTypes(env);
	}
	

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(cyanEnv.formalGenericParamToRealParam(methodName.getSymbolString()));
		}
		else {
			pw.print(methodName.getSymbolString());
		}
		
		
		super.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}

	@Override
	public void genJava(PWInterface pw, Env env, boolean isMultiMethod) {
		super.genJava(pw, env, isMultiMethod);
		pw.print(NameServer.getJavaNameOfSelector(methodName.getSymbolString()));
		pw.print("()");
	}	
	
	@Override
	public void genJava(PWInterface pw, Env env) {
		genJava(pw, env, false);
	}
	
	@Override
	public void genJavaAsConstructor(PWInterface pw, Env env, String javaNameDeclaringObject) {
		pw.printIdent( javaNameDeclaringObject + "()");
	}
	
	
	@Override
	public String getSingleParameterType() {
		return NameServer.getJavaName("Nil");
	}

	@Override
	public String getFullName(Env env) {
		return fullName = getNameWithoutParamNumber();
	}
	
	@Override
	public String getNameWithoutParamNumber() {
		return methodName.getSymbolString();
	}

	
	@Override
	public String getPrototypeNameForMethod() {
		return getNameWithoutParamNumber() + "_un";		
	}
	
	@Override
	public String getSuperprototypeNameForMethod() {
		String s = "UFunction";
		if ( this.getReturnTypeExpr() == null ) 
			s += "<Nil>";
		else
			s += "<" + this.getReturnTypeExpr().ifPrototypeReturnsItsName() + ">";
		return s;
	}
	
	
	@Override
	public void genCyanEvalMethodSignature(StringBuffer s) {
		String ret;
		if ( this.getReturnTypeExpr() == null ) 
			ret = "Nil";
		else
			ret = this.getReturnTypeExpr().ifPrototypeReturnsItsName();
		s.append("eval -> " + ret);
	}
	
	

	@Override
	public Symbol getFirstSymbol() {
		return methodName;
	}

	
	@Override
	public ArrayList<ParameterDec> getParameterList() {
		ArrayList<ParameterDec> paramList = new ArrayList<>();
		return paramList;
	}

	
	@Override
	public String getFunctionName() {
		String s = "Function<";
		if ( this.getReturnTypeExpr() != null && this.getReturnTypeExpr().getType() != null ) {
			s += this.getReturnTypeExpr().getType().getFullName() + ">";
		}
		else {
			s += "Nil>";
		}
		return s;
	}
	
	
	@Override
	public String getFunctionNameWithSelf(String fullName2) {
		String s = "Function<" + fullName2 + ", ";
		
		if ( this.getReturnTypeExpr() != null && this.getReturnTypeExpr().getType() != null ) {
			s += this.getReturnTypeExpr().getType().getFullName() + ">";
		}
		else {
			s += "Nil>";
		}
		return s;
	}	
	
	@Override
	public String getSignatureWithoutReturnType() {
		return methodName.getSymbolString();
	}

	
	
	/**
	 * name of the unary method, such as getName, set, ++, or !
	 */
	private Symbol methodName;


	@Override
	public String getJavaName() {
		return NameServer.getJavaName(methodName.getSymbolString());
	}

	


}
