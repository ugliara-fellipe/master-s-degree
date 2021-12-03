/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
   Represents a method signature of a grammar method such as
          fun (add: Int)+  Array<Int> v [ ... ]
          fun (format: String  println: (String)+)) t [ ... ]


 * @author José
 *
 */
public class MethodSignatureGrammar extends MethodSignature implements GenCyan {

	public MethodSignatureGrammar(SelectorGrammar selectorGrammar, MethodDec method) {
		super(method);
		this.selectorGrammar = selectorGrammar;
		this.paramType = null;
		this.cyanName = null;
	}
	
	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.preVisit(this);
		
		visitor.visit(this);
	}	


	public void setSelectorGrammar(SelectorGrammar selectorGrammar) {
		this.selectorGrammar = selectorGrammar;
	}

	public SelectorGrammar getSelectorGrammar() {
		return selectorGrammar;
	}


	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		selectorGrammar.genCyan(pw, cyanEnv);
		if ( paramType != null )
			paramType.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.print(" " + parameterDec.getName() + " ");
		super.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}

	@Override
	public void genJava(PWInterface pw, Env env) {
		pw.printlnIdent("/*");
		pw.printlnIdent(" " + selectorGrammar.getStringType() );
		pw.printlnIdent("*/");
		super.genJava(pw, env);
		pw.print( " " + this.getJavaName() + "( ");
		if ( paramType != null )
			pw.print(paramType.genJavaExpr(pw, env));
		else
    		pw.print(getSingleParameterType());
		pw.println("  " + NameServer.getJavaName(parameterDec.getName())
				  + " )");
	}


	@Override
	public String getSingleParameterType() {
		return selectorGrammar.getStringType();
	}

	
	@Override
	public String getPrototypeNameForMethod() {
		String s = getNameWithoutParamNumber();
		String protoName = "";
		int i = 0;
		int size = s.length();
		while ( i < size ) {
			char ch = s.charAt(i);
			if ( ch == ' ' )
				protoName += "_";
			else {
				if ( Character.isAlphabetic(ch) || Character.isDigit(ch) )
					protoName += ch;
				else
					protoName += NameServer.alphaName("" + ch) + "_";
			}
			++i;
		}
		return protoName;
	}
	
	
	@Override
	public String getSuperprototypeNameForMethod() {
		String s = "UFunction<" + this.getSingleParameterType() + ", ";
		String ret;
		if ( this.getReturnTypeExpr() == null ) 
			ret = "Nil";
		else
			ret = this.getReturnTypeExpr().ifPrototypeReturnsItsName();
		return s + ret + ">";
	}
	
	
	
	@Override
	public String getNameWithoutParamNumber() {
		return PWCounter.toStringBuffer(this).toString();
	}

	
	@Override
	public void genCyanEvalMethodSignature(StringBuffer s) {
		s.append("eval: " + selectorGrammar.getStringType() );
		if ( parameterDec.getName() != null ) 
			s.append(" " + parameterDec.getName());
		if ( this.getReturnTypeExpr() != null ) {
			s.append(" -> " + this.getReturnTypeExpr().ifPrototypeReturnsItsName() );
		}
	}
		
	
	@Override
	public Symbol getFirstSymbol() {
		return selectorGrammar.getFirstSymbol();
	}


	@Override
	public boolean isGrammarMethod() {
		return true;
	}
	
	
	@Override
	public void calcInterfaceTypes(Env env) {
		super.calcInterfaceTypes(env);
		selectorGrammar.calcInterfaceTypes(env);
		if ( paramType != null )
			paramType.calcInternalTypes(env);
	}
		
	@Override
	public void calcInternalTypes(Env env) {
		/**if ( parameterDec != null )
			parameterDec.calcInternalTypes(env);
		*/
	}
	
	public ParameterDec getParameterDec() {
		return parameterDec;
	}

	public void setParameterDec(ParameterDec parameterDec) {
		this.parameterDec = parameterDec;
	}
	
	@Override
	public void check(Env env) {

	}

	@Override
	public String getFullName(Env env) {
		return this.selectorGrammar.getFullName(env);
	}
	
	@Override
	public ArrayList<ParameterDec> getParameterList() {
		ArrayList<ParameterDec> paramList = new ArrayList<>();
		paramList.add(parameterDec);
		return paramList;
	}

	@Override
	public String getJavaName() {
		return NameServer.getJavaName(cyanName);
	}
	
	public String getCyanName() {
		return cyanName;
	}

	public void setCyanName(String cyanName) {
		this.cyanName = cyanName;
	}

	public Type getAstRootType() {
		return this.selectorGrammar.getAstRootType();
	}


	public void setAstRootType(ObjectDec astRootType, Env env, Symbol first) {
		selectorGrammar.setAstRootType(astRootType, env, first);
	}

	/**
	 * it should not be called 
	   @see ast.MethodSignature#getFunctionName()
	 */
	@Override
	public String getFunctionName() {
		return null;
	}
	
	@Override
	public String getFunctionNameWithSelf(String fullName2) { return null; }

	/**
	 * should never be used
	 */
	@Override
	public String getSignatureWithoutReturnType() {
		return null;
	}


	
	/**
	 * the composite selector. In
	 *    (case: char do: Function)+
	 * selectorGrammar references an object of SelectorGrammarList representing
	 *     case: char do: Function
	 *
	 */
	private SelectorGrammar  selectorGrammar;

	/**
	 * the parameter type of the grammar method. There is only one. It is
	 *      Array<Tuple<String, int>>
	 * in
	 *     public fun (key: String value: int)+  Array<Tuple<String, Int>> t { ... }
	 */
	private Expr paramType;
	
	/**
	 * the sole parameter of this grammar method
	 */
	private ParameterDec parameterDec;

	/**
	 * the name of this grammar method
	 */
	private String cyanName;


}
