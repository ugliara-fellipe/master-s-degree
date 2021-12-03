/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.SymbolOperator;
import lexer.Token;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;


/** Represents a message send without the receiver (just the message).
 *  That is, in a message send
 *        Out println: (n + 1), 10
 *  an object of this class would represent "println: (n + 1),  10".
 *  This complete message send would be represented by an object of
 *  ExprMessageSendWithSelectorsToExpr.
 *  This class also represents message sends with a selector ended with ':' but
 *  that does not take parameters such as
 *       file open: read: ;
 *       
 * @author José
 *
 */
public class MessageWithSelectors extends Message implements ASTNode {

	public MessageWithSelectors() { }
	
	public MessageWithSelectors(
			ArrayList<SelectorWithRealParameters> selectorParameterList) {
		super();
		this.selectorParameterList = selectorParameterList;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		if ( selectorParameterList != null ) {
			for ( SelectorWithRealParameters s : this.selectorParameterList ) {
				s.accept(visitor);
			}
		}
		visitor.visit(this);
	}	
	/**
	 * return true if this is a dynamic message send, those whose selectors are
	 * preceded by ? as in 
	 *       person ?setAge: 10;
	 *       n = person ?age;
	 */
	@Override
	public boolean isDynamicMessageSend() {
		if ( this.selectorParameterList != null && 
			 this.selectorParameterList.get(0).getSelector().token == Token.IDENTCOLON )
			return false;
		else
			return true;
	}


	/*
	 * return true if there is a backquote, `, before one of the selectors. If there is
	 * ` in one selector, all of them should be preceded by `
	 */
	public boolean getBackquote() {
		return selectorParameterList.get(0).getBackquote();
	}


	public ArrayList<SelectorWithRealParameters> getSelectorParameterList() {
		return selectorParameterList;
	}
	
	public void setSelectorParameterList(ArrayList<SelectorWithRealParameters> selectorParameterList) {
		this.selectorParameterList = selectorParameterList;
	}
	

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		if ( printInMoreThanOneLine ) {
			int i = 0;
			int size = selectorParameterList.size();
			pw.add();
			for ( SelectorWithRealParameters p : selectorParameterList ) {
				++i;
				if ( i > 1  )
					pw.printIdent("");
				p.genCyan(pw, PWCounter.printInMoreThanOneLine(p), cyanEnv, genFunctions);
				if ( --size > 0 )
					pw.println();
			}
			pw.sub();
		}
		else {
			int size = selectorParameterList.size();
			for ( SelectorWithRealParameters p : selectorParameterList ) {
				p.genCyan(pw, false, cyanEnv, genFunctions);
				if ( --size > 0 ) 
					pw.print( " " );
			}
		}
	}

	/**
	 * 	 * In the general case:
	 *         selectorName + ("_p" + typeName)* +
	 *         ("_s" + selectorName + ("_p" + typeName)* )+
	 *
	 *
	 */
	@Override
	public String getJavaMethodName() {
		return NameServer.getJavaMethodNameOfMessageSend(this);
	}



	@Override
	public void calcInternalTypes(Env env) {
		for ( SelectorWithRealParameters selectorWithRealParameter:  selectorParameterList ) 
			selectorWithRealParameter.calcIntenalTypes(env);
	}

	/**
	 * returns the name of the method that should be called by this message send.
	 * The name includes only the selectors, without the parameter types, but with the
	 * number of parameters. This method returns the names of all selectors plus its number of parameters concatenated. 
	 * That is, the return for the call <br>
	 * <code>obj with: n, ch plus: f;</code><br>
	 * would be <code>with:2 plus:1</code	 * 
	 * @return
	 */
	public String getMethodNameWithParamNumber() {
		String s = "";
		
		if ( selectorParameterList.get(0).getSelector() instanceof SymbolOperator ) {
			  // operators with selectors are always binary so they have one parameter
			return selectorParameterList.get(0).getSelector().getSymbolString() + "1";
		}
		else {
			int size = selectorParameterList.size();
			for ( SelectorWithRealParameters selectorWithRealParameters: selectorParameterList ) {
				s = s + selectorWithRealParameters.getSelector().getSymbolString() + 
						selectorWithRealParameters.getExprList().size();
				if ( --size > 0 ) 
					s += " ";
			}
			return s;
		}
	}

	
	/**
	 * returns the name of the method that should be called by this message send.
	 * The name includes only the selectors, without the parameter types 
	 * @return
	 */
	public String getMethodName() {
		String s = "";
		for ( SelectorWithRealParameters selectorWithRealParameters: selectorParameterList ) 
			s = s + selectorWithRealParameters.getSelector().getSymbolString(); 
		return s;
	}
	
	

	public Symbol getFirstSymbol() {
		return this.selectorParameterList.get(0).getSelector();
	}
	
	
	/**
	 * return the token of the first selector 
	 */
	public Token getTokenFirstSelector() {
		return this.selectorParameterList.get(0).getSelector().token;
	}
	
	/**
	 *  represents the message name and arguments. In message send
	 *           anObject with: anotherObject do: aFunction1 aFunction2
	 *  there would be created two objects of SelectorWithRealParameters
	 *  for list  selectorParameterList. One represents
	 *           with: anotherObject
	 *  and the other represents
	 *           do: aFunction1 aFunction2
	 */
	private ArrayList<SelectorWithRealParameters> selectorParameterList;




}
