/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.Token;
import saci.CyanEnv;
import saci.Env;


/** Represents a selector of a grammar method that is, in most cases,
 * ended by +, *, or ? such as
 *    (add: Int)+
 *    (case: Char do: Function<Nil>)+
 *    (gas: Float | alcohol: Float)
 *    ( (case: Char do: Function<Nil>)+ (else: Function<Nil>)? )
 *
 * In the last case, the selector is not ended by +, *, or ? Nonetheless,
 * it is represented a SelectorGrammar object.
 *
 * In any case, the selector starts with "(".
 *
 * This abstract class has two subclasses. One, SelectorGrammarList represents
 * composite selectors such as
 *    (case: char do: Function<Nil>)+
 * and SelectorGrammarOrList represents selectors separated by | such as
 *    (gas: float | alcohol: float)
 *
 * @author José
 *
 */
abstract public class SelectorGrammar extends Selector {


	public SelectorGrammar(ArrayList<Selector> selectorArray, Symbol firstSymbol) {
		super();
		this.firstSymbol = firstSymbol;
		this.selectorArray = selectorArray;
		this.regularOperator = null;
	}

	@Override
	public void accept(ASTVisitor visitor) {
	}

	
	
	@Override
	public void genCyan(PWInterface pw, CyanEnv cyanEnv) { genCyan(pw, false, cyanEnv, true); }
	


	
	public void setRegularOperator(Symbol regularOperator) {
		this.regularOperator = regularOperator;
	}
	public Symbol getRegularOperator() {
		return regularOperator;
	}

	public void setSelectorArray(ArrayList<Selector> selectorArray) {
		this.selectorArray = selectorArray;
	}
	public ArrayList<Selector> getSelectorArray() {
		return selectorArray;
	}


	public Symbol getFirstSymbol() {
		return firstSymbol;
	}

	@Override
	public void calcInterfaceTypes(Env env) {
		for ( Selector selector : selectorArray ) 
			selector.calcInterfaceTypes(env);
	}

	@Override
	public String getFullName(Env env)  {
		StringBuffer sb = new StringBuffer();
		int size = selectorArray.size();
		for ( Selector s : selectorArray ) {
			sb.append(s.getFullName(env));
			if ( --size > 0 ) 
				sb.append(" ");
		}
		if ( regularOperator != null ) 
			sb.append(regularOperator.getSymbolString());
		return sb.toString();
	}
	

	@Override
	public boolean matchesEmptyInput() {
		if ( regularOperator != null && (regularOperator.token == Token.MULT || regularOperator.token == Token.QUESTION_MARK) ) 
			return true;
		for ( Selector sel : this.selectorArray ) {
			if ( !sel.matchesEmptyInput() )
				return false;
		}
		return true;
	}
	
	
	/**
	 * return the parameter type of the grammar method based on the method declaration. 
	 * This is necessary if the user does not supply himself the type as in
	 *      fun (add: (Int)*) :t [ ... ] 
	   @param env
	 */
	abstract public ProgramUnit getParameterType(Env env);
	/**
	 * the regular language operator that appear after the list of
	 * selectors. In
	 *    (case: char do: Function<Nil>)+
	 * regularOperator  is the symbol corresponding to Token.PLUS.
	 */
	protected Symbol regularOperator;
	/**
	 * the list of selectors. In
	 *    (case: char do: Function<Nil>)+
	 * selectorArray contains two selector objets, one for "case: char" and one
	 * for "do: Function<Nil>".
	 */
	protected ArrayList<Selector>  selectorArray;

	/**
	 * first symbol of this selector
	 */
	private Symbol firstSymbol;

}
