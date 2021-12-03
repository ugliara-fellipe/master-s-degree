/**
 *
 */
package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;
import saci.Env;
import saci.NameServer;

/**
 * @author Jos�
 *
 */
abstract public class ExprMessageSendUnaryChain extends ExprMessageSend {

	public ExprMessageSendUnaryChain(Symbol nextSymbol) {
		super(nextSymbol);
		backquote = false;
	}

	public ExprMessageSendUnaryChain() {
		super();
		backquote = false;
	}
	
	
		

	/**
	   @param env
	   @param tokenFirstSelector
	 */
	public void calcInternaltTypesWithBackquote(Env env,  Symbol ... arrayUnarySymbols ) {
		type = Type.Dyn;
		
		if ( arrayUnarySymbols[0].token != Token.IDENT ) {
			env.error(getFirstSymbol(), "The backquote ` should not be followed by '?' or '?.'", true, true);
		}
		
		quotedVariableList = new ArrayList<>();
		   // something like   f1 `first `second  in which first and second should be variables
		   // of type String or CySymbol
		for ( Symbol sym : arrayUnarySymbols ) {
			String name = sym.getSymbolString();
			VariableDecInterface varDec = env.searchVariable(name);
			if ( varDec == null ) {
				if ( env.getEnclosingObjectDec() == null ) {
					/*
					 * inside a regular prototype that is NOT inside another prototype
					 */
					varDec = env.searchVariable(name);
				}
				else {
					/*
					 * inside an inner prototype
					 */
					if ( NameServer.isMethodNameEval(env.getCurrentMethod().getNameWithoutParamNumber()) ) {
						/*
						 * inside an 'eval' or 'eval:eval: ...' method of an inner prototype 
						 */

						varDec = env.searchVariableInEvalOfInnerPrototypes(name);
					}
					else {
						/*
						 * inside a method of an inner prototype that is not 'eval', 'eval:eval: ...'
						 */
						

						varDec = env.searchVariableIn_NOT_EvalOfInnerPrototypes(name);
					}
				}
			}
			if ( varDec == null ) {
				varDec = env.searchInstanceVariable(name);
				env.error(true, sym,
						"Variable " + sym.getSymbolString() + " was not declared",
						sym.getSymbolString(), ErrorKind.variable_was_not_declared);
			}
			else {
				if ( ! Type.String.isSupertypeOf(varDec.getType(), env)  && varDec.getType() != Type.Dyn )
					env.error(true, sym,
							"Variable " + sym.getSymbolString() + " should be of type String or Dyn",
							sym.getSymbolString(), ErrorKind.backquote_not_followed_by_a_string_variable);
					
			}
			quotedVariableList.add(varDec);
		}
		return;
	}



	public boolean getBackQuote() {
		return backquote;
	}

	public void setBackQuote(boolean backQuote) {
		this.backquote = backQuote;
	}

	/**
	 * true if this unary message send is preceded by backquote, `. This means
	 * the method to be called is contained in the variable unarySymbol[0].
	 * There should be just one element in unarySymbol.
	 */
	protected boolean backquote;
	

	/**
	 * if backquote is true, this is the list of variables in the message send. That is,
	 * if the message is
	 *           f1 `first `second;
	 * then quotedVariableList contains references to variables first and second.
	 */
	protected ArrayList<VariableDecInterface> quotedVariableList;

	/**
	 * add an unary message selector at the end of the chain. That is,
	 * if this object represents 
	 *        super getClub
	 * and we want to add unary message "size" (because the code is
	 * "super getClub size"), then we call 
	 *       addUnarySymbol(symbol for "size");
	 *       
	 * @param unarySymbol1
	 */
	public void setUnarySymbol(SymbolIdent unarySymbol1) {
		this.unarySymbol = unarySymbol1;
	}
	
	public SymbolIdent getUnarySymbol() {
		return unarySymbol;
	}
	
	public String getMessageName() {
		return this.unarySymbol.getSymbolString();
	}
	/**
	
	 * the unary message symbols (each one will result in an unary method
	 * at runtime).
	 */
	protected SymbolIdent unarySymbol;

}
