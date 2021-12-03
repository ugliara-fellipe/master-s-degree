/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

/** This class represents a selector declared with ":" at the end
 *   and its parameters in a method declaration.
 *
 * For example, if the method is
 *     public fun to: (Int max)  do: Function<Nil> aFunc { /* method body * / }
 * Then there should be two objects of SelectorWithParameters:
 *   1. one for "to:" with one parameter
 *   2. one for "do:" with one parameter
 *
 *  A selector may not have parameters. For example:
 *      public fun read: { ... }
 *      public fun amount: gas: Float 
 *  In the last line, "amount:" does not have any parameters.
 *
 *
 * @author José
 *
 */
public class SelectorWithParameters extends Selector {

	public SelectorWithParameters(Symbol selector) {
		super();
		this.selector = selector;
		parameterList = new ArrayList<ParameterDec>();
		selectorNameWithoutSpecialChars = null;
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		for (ParameterDec p : this.parameterList) {
			p.accept(visitor);
		}
		visitor.visit(this);
	}
	
	public void addParamDec( ParameterDec paramDec ) {
		parameterList.add(paramDec);
	}

	public void setParameterList(ArrayList<ParameterDec> parameterList) {
		this.parameterList = parameterList;
	}
	public ArrayList<ParameterDec> getParameterList() {
		return parameterList;
	}

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(cyanEnv.formalGenericParamToRealParam(selector.getSymbolString()));
		}
		else {
			pw.print(selector.getSymbolString());
		}
		int size = parameterList.size();
		boolean onlyOneTypeWithoutParameterName = false;
		if ( size == 1 && parameterList.get(0).getVariableSymbol() == null ) {
				    // there is one parameter and the first one does not have a type
				onlyOneTypeWithoutParameterName = true;
		}
		else if ( size == 0 )
			onlyOneTypeWithoutParameterName = true;

		pw.print(" ");
		if ( ! onlyOneTypeWithoutParameterName ) 
			pw.print("(");
		for ( ParameterDec p : parameterList ) {
			p.genCyan(pw, false, cyanEnv, genFunctions);
			--size;
			if ( size > 0 )
				pw.print(", ");
		}
		if ( ! onlyOneTypeWithoutParameterName ) 
    		pw.print(")");
		pw.print(" ");
	}


	/**
	 * considering the method has just one parameter, which is its type?
	 * If the method is
	 *      public fun add: Int, String with: Float { }
	 * its type is Tuple<int, String, float>
	 * An object of this class represents "add: int, String" or "with: float".
	 * It would return "int, String" and "float" in these cases.
	 */
	@Override 
	public String getStringType() {
		int size = parameterList.size();
		if ( size == 1 )
			return PWCounter.toStringBuffer(parameterList.get(0).getTypeInDec()).toString();
		else {
			String s = "Tuple<";
			for ( ParameterDec p : parameterList ) {
				s = s + PWCounter.toStringBuffer(p.getTypeInDec()).toString();
				if ( --size > 0 )
					s = s + ", ";
			}
			return s + ">";
		}
	}

	/**
	 * the Java name of the method with this selector. For example, a selector
	 *      public fun run: [ ]
	 * the Java name is "run". A selector
	 * 	    format: String print: int
	 * has Java name
	 *      format_s_print
	 * Underscores are duplicated to avoid confusion with underscores added
	 * by the Compiler. Therefore selector
	 * 	    person_name:
	 * has Java name
	 *     person__name
	 */

	@Override
	public String getJavaName() {
		return NameServer.getJavaNameOfSelector(selector.getSymbolString());
	}

	
	@Override
	public String getFullName(Env env) {
		if ( parameterList == null || parameterList.size() == 0 )
			return selector.getSymbolString();
		else {
			String s = selector.getSymbolString();
			int size = parameterList.size();
			if ( size > 0 )
				s = s + " ";
			for ( ParameterDec p : parameterList ) {
				if ( p.getType(env) == null ) {
					s = s + NameServer.dynName;
				}
				else {
					s = s + p.getType(env).getFullName(env);
				}
				if ( --size > 0 )
					s = s + ", ";
				
			}
			return s;
		}
	}
	
	
	@Override
	public String getName() {
		return selector.getSymbolString();
	}
	
	/**
	 * return the selector name without any of the characters: ':', '?', '.'
	   @return
	 */
	public String getSelectorNameWithoutSpecialChars() {  
		if ( selectorNameWithoutSpecialChars == null ) {  
			String s = this.selector.getSymbolString();
			if ( s.startsWith("?.") )
				s = s.substring(2);
			else if ( s.charAt(0) == '?' )
				s = s.substring(1);
			if ( s.endsWith(":") )
				s = s.substring(0, s.length() - 1);
			/*
			int size = s.length();
			selectorNameWithoutSpecialChars = "";
			for (int i = 0; i < size; ++i) {
				char ch = s.charAt(i);
				if ( ch != ':' && ch != '?' && ch != '.' ) 
					selectorNameWithoutSpecialChars += ch;
			}
			*/
			selectorNameWithoutSpecialChars = s;
		}
		return selectorNameWithoutSpecialChars;
	}	
	
	public Symbol getSelector() {
		return selector;
	}

	public void calcInternalTypes(Env env) {
		

		for ( ParameterDec parameter: parameterList ) {
			String parameterName = parameter.getName();
			if ( parameterName != null ) { 
				if ( env.searchLocalVariableParameter(parameterName) != null ) {
					env.searchLocalVariableParameter(parameterName);
					env.error(parameter.getFirstSymbol(), "Parameter '" + parameterName + "' is being redeclared", true, true);
				}
			}
			parameter.calcInternalTypes(env);
		}
	}
	
	@Override
	public void calcInterfaceTypes(Env env) {
		for ( ParameterDec parameterDec : parameterList )
			parameterDec.calcInternalTypes(env);
	}
	
	/**
	 * generate the parameter declarations of this method.
	 * @param env
	 */
	public void genJava(PWInterface pw, Env env) {
		int size = parameterList.size();		
		for ( ParameterDec paramDec : parameterList ) {
			paramDec.genJava(pw, env);
			if ( --size > 0 )
				pw.print(", ");
		}
	}

	@Override
	public Tuple2<String, String> parse(SelectorLexer lexer, Env env) {
		return null;
	}

	@Override
	public boolean matchesEmptyInput() {
		return false;
	}
	
	
	/**
	 * the selector. It is "to:" in
	 *     to: (:max int)
	 * and "amount:" in
	 *     amount:  // no parameters
	 */
	private Symbol selector;
	/**
	 * list of the parameters associated with selectorName. It may be empty
	 * for a selector may not have parameters.
	 */
	private ArrayList<ParameterDec> parameterList;
	/**
	 * the name of the selector without ':', '?.' and the like
	 */
	private String selectorNameWithoutSpecialChars;
	@Override
	void setAstRootType(ObjectDec astRootType, Env env, Symbol first) {
		// TODO Auto-generated method stub
		
	}
}
