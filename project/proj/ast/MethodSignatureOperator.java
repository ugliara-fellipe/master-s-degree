/**
 * Represents a method signature whose name is an operator such as
 *      fun ++  [ ... ]
 *      fun + (:other int) [ ... ]
 *      fun + (:other int) -> int [ ... ]
 *      fun ! -> int [ ... ]
 *
 *  the method may be unary such as ! and ++ in the above examples.

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
public class MethodSignatureOperator extends MethodSignature {

	public MethodSignatureOperator(Symbol symbolOperator, MethodDec method) {
		super(method);
		this.symbolOperator = symbolOperator;
		parameter = null;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.preVisit(this);
		
		visitor.visit(this);
	}	
	

	public void setOptionalParameter(ParameterDec optionalParameter) {
		this.parameter = optionalParameter;
	}

	public ParameterDec getOptionalParameter() {
		return parameter;
	}

	public void setSymbolOperator(Symbol symbolOperator) {
		this.symbolOperator = symbolOperator;
	}

	public Symbol getSymbolOperator() {
		return symbolOperator;
	}


	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		
		pw.print(symbolOperator.getSymbolString() + " ");
		if ( parameter != null )
			parameter.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		super.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}



	@Override
	public void genJava(PWInterface pw, Env env, boolean isMultiMethod) {
		super.genJava(pw, env, isMultiMethod);
		javaNameMultiMethod = NameServer.alphaName(symbolOperator.getSymbolString());
		if ( parameter != null ) {
			if ( isMultiMethod ) {
				this.javaNameMultiMethod = javaNameMultiMethod + "_" + parameter.getType().getJavaName();
			}
		}
		pw.print(" " + javaNameMultiMethod + "(");
		
		if ( parameter != null ) {
			env.pushVariableDec(parameter);


			/*
			 * 			if ( this.getMethod() != null && this.getMethod().getMultiMethod() ) {
				pw.print("Object");
			}
			else {
				pw.print(parameter.getType().getJavaName());
			}
			 */
			pw.print(parameter.getType().getJavaName());
			
			pw.print(" " + NameServer.getJavaName(parameter.getName()));
		}
		pw.print(")");
		
	}	
	
	/**
	 * generate the signature of the method that will replace all multi-methods in
	 * a prototype
	   @param pw
	   @param env
	 */
	public void genJavaOverloadMethod(PWInterface pw, Env env) {
		super.genJava(pw, env, false);
		
		pw.print(" " + NameServer.alphaName(symbolOperator.getSymbolString()) + "(");
		if ( parameter != null ) {
			env.pushVariableDec(parameter);

			pw.print("Object " + NameServer.getJavaName(parameter.getName()));
		}
		pw.print(")");
		
	}
	
	
	@Override
	public String getSingleParameterType() {
		if ( parameter != null )
			return parameter.getTypeInDec().getFirstSymbol().getSymbolString();
		else
			return "Nil";
	}

	@Override
	public String getFullName(Env env) {
		if ( fullName == null ) {
			if ( parameter != null ) {
				fullName = getNameWithoutParamNumber() + " " + parameter.getType(env).getFullName(env);
			}
			else 
				fullName = getNameWithoutParamNumber();
		}
		return fullName;
	}
	
	@Override
	public String getName() {
		String ret = symbolOperator.getSymbolString();
		if ( parameter != null ) 
			ret = ret + "1";
		return ret;
	}
	
	@Override
	public String getNameWithoutParamNumber() {
		return symbolOperator.getSymbolString();
	}
	
	@Override
	public String getPrototypeNameForMethod() {
		String typeName;
		
		if ( parameter == null ) {
			typeName = "_un";
		}
		else if ( parameter.getType() != null ) {
			typeName = NameServer.getJavaName(parameter.getType().getFullName());
		}
		else if ( parameter.getTypeInDec() != null ) {
			typeName = NameServer.getJavaName(parameter.getTypeInDec().asString());
		}
		else
			typeName = NameServer.dynName;
		return NameServer.alphaName(symbolOperator.token.toString()) + typeName;	
	}
	
	
	@Override
	public String getSuperprototypeNameForMethod() {
		String s = "Function<";
		String ret;
		if ( this.getReturnTypeExpr() == null ) 
			ret = "Nil";
		else
			ret = this.getReturnTypeExpr().ifPrototypeReturnsItsName();
		
		if ( parameter != null ) {
			String paramType;
			if ( parameter.getTypeInDec() == null )
				paramType = NameServer.dynName;
			else
				paramType = parameter.getTypeInDec().ifPrototypeReturnsItsName();
			s += paramType + ", ";
		}
		s += ret + ">";
		return s;
	}
	
	
	@Override
	public void genCyanEvalMethodSignature(StringBuffer s) {
		String ret = "Nil";
		if ( this.getReturnTypeExpr() != null )
			ret = this.getReturnTypeExpr().ifPrototypeReturnsItsName();
		if ( parameter == null ) {
			// unary method
			s.append("eval -> " + ret);
		}
		else {
			   // binary method
			s.append( "eval: " + parameter.getTypeInDec().ifPrototypeReturnsItsName() +
					(parameter.getName() == null ? " " : (" " + parameter.getName())) + 
					" -> " + 
		              ret);
		}
	}
	

	
	@Override
	public void check(Env env) {
		super.check(env);
		if ( env.searchLocalVariableParameter(parameter.getName()) != null )
			env.error(parameter.getFirstSymbol(), "Parameter " +
					parameter.getName() + " is being redeclared", true, true);
		env.pushVariableDec(parameter);
	}

	@Override
	public Symbol getFirstSymbol() {
		return symbolOperator;
	}

	@Override
	public void calcInterfaceTypes(Env env) {
		
		if ( this.hasCalculatedInterfaceTypes ) 
			return ;
		
		super.calcInterfaceTypes(env);
		if ( parameter != null )
			parameter.calcInternalTypes(env);
		hasCalculatedInterfaceTypes = true;
	}
	
	@Override 
	public void calcInternalTypes(Env env) {
		if ( parameter != null )
			env.pushVariableDec(parameter);
	}	
	
	
	@Override
	public ArrayList<ParameterDec> getParameterList() {
		ArrayList<ParameterDec> paramList = new ArrayList<>();
		if ( parameter != null ) 
			paramList.add(parameter);
		return paramList;
	}

	@Override
	public String getFunctionName() {
		String s = "Function<";
		if ( parameter != null ) {
			s += parameter.getType().getFullName() + ", ";
		}
		if ( this.getReturnTypeExpr() != null && this.getReturnTypeExpr().getType() != null ) {
			s += this.getReturnTypeExpr().getType().getFullName() + ">";
		}
		else {
			s += "Nil>";
		}
		return s;
	}
	
	
	@Override
	public String getFunctionNameWithSelf(String receiverType) {
		String s = "Function<" + receiverType;
		if ( parameter != null ) {
			s += "><";
			s += parameter.getType().getFullName();
		}
		s += ", ";
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
		String s = this.symbolOperator.getSymbolString();
		if ( parameter != null ) {
			s += parameter.getTypeInDec().asString();
		}
		return s;
	}

	
	/**
	 * parameter represents the optional parameter of the method.
	 * Unary methods do not take parameters such as ++ and ! in the examples
	 * above. Method + in the examples take one parameter that should
	 * be represented by this instance variable. Note that
	 * a method whose name is an operator takes at most one parameter.
	 * That is, it is illegal to declare something like
	 *    fun + (:other int, :another int) [ ... ]
	 */
	private ParameterDec parameter;

	/**
	 * In
	 *     public fun + (:other int) -> int [ ... ]
	 *  symbolOperator is +
	 */
	private Symbol symbolOperator;

	@Override
	public String getJavaName() {
		return NameServer.getJavaMethodNameOfMessageSend(symbolOperator.getSymbolString());
	}


	public String getJavaNameOverloadMethod() {
		return javaNameMultiMethod;
	}

	private String javaNameMultiMethod;

}
