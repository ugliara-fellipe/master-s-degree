/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.Token;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/** Represents a selector and its real parameters of a message send. For example, the message send
 *       anObject with: anotherObject do: aFunction1, aFunction2
 * needs two objects of this class. The first one represents
 *     with: anotherObject
 * and the other represents
 *     do: aFunction1, aFunction2
 *
 * This class also represents selectors that represent dynamic calls such as
 *     ?put: "Peter"
 *  or
 *      ?do:  function1, function2
 *
 * @author José
 *
 */
public class SelectorWithRealParameters implements GenCyan, ASTNode {

	public SelectorWithRealParameters(Symbol selector, boolean backquote,
			ArrayList<Expr> exprList) {
		this.selector = selector;
		this.backquote = backquote;
		this.exprList = exprList;
		selectorNameWithoutSpecialChars = null;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		if ( exprList != null ) {
			for ( Expr e : this.exprList ) 
				e.accept(visitor);
		}
		visitor.visit(this);
	}
	
	public void setExprList(ArrayList<Expr> exprList) {
		this.exprList = exprList;
	}

	public ArrayList<Expr> getExprList() {
		return exprList;
	}
	public void setSelector(Symbol selectorNameWithoutSpecialChars) {
		this.selector = selectorNameWithoutSpecialChars;
	}
	public Symbol getSelector() {
		return selector;
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
	
	/**
	 * return the selector name without the characters  '?',  '.'
	   @return
	 */
	public String getSelectorName() {
		if ( selectorName == null ) {
			String s = this.selector.getSymbolString();
			if ( s.startsWith("?.") )
				s = s.substring(2);
			else if ( s.charAt(0) == '?' )
				s = s.substring(1);
			selectorName = s;
		}
		return selectorName;
	}
	
	
	
	/**
	 * there are several kinds of selectors:
	 *      box ?set: 0    // dynamic call
	 *      box ?get
	 *      box ?.set: 0   // nil-safe message send
	 *      box ?.get      
	 */

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		String name;
		
		String str = selector.getSymbolString();
		String prefix = "";
		Token token = selector.token;
		if  ( token == Token.INTER_ID || token == Token.INTER_ID_COLON )
			prefix = "?";
		else if ( token == Token.INTER_DOT_ID )
			prefix = "?.";
		

		
		
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			name = prefix + cyanEnv.formalGenericParamToRealParam(str);
		}
		else {
			name = prefix + str;
		}
		
		// Token.INTER_DOT_ID, Token.INTER_DOT_ID_COLON, Token.INTER_ID_COLON, Token.INTER_ID
		if ( printInMoreThanOneLine ) {
			if ( backquote )
				pw.print("`");
			pw.print(name + " ");
			if ( exprList != null ) {
				int n = exprList.size();
				for (Expr e : exprList) {
					e.genCyan(pw, PWCounter.printInMoreThanOneLine(e), cyanEnv, genFunctions);
					--n;
					if (n > 0)
						pw.print(", ");
				}
			}

		}
		else {
			if ( backquote )
				pw.print("`");
			pw.print(name + " ");
			if ( exprList != null ) {
				int n = exprList.size();
				for (Expr e : exprList) {
					e.genCyan(pw, false, cyanEnv, genFunctions);
					--n;
					if (n > 0)
						pw.print(", ");
				}
			}
		}
	}

	
	public void calcIntenalTypes(Env env) {
		for ( Expr expr : exprList ) 
			expr.calcInternalTypes(env);
	}



	
	public boolean getBackquote() {
		return backquote;
	}


	public String asString(CyanEnv cyanEnv) {
		PWCharArray pwChar = new PWCharArray();
		genCyan(pwChar, true, cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}
	
	@Override
	public String asString() {
		return asString(NameServer.cyanEnv);
	}


	/**
	 * the selector name (or method name). In the example above, it is "with:" for
	 * the first case
	 */
	private Symbol selector;

	/**
	 * represents the real arguments
	 */
	private ArrayList<Expr> exprList;

	/**
	 * true if this selector was preceded by `, backquote, like in
	 *     elem `message;
	 * in which message is a String variable.
	 */
	private boolean backquote;

	/**
	 * the selector name without any of the characters: ':', '?', '.'
	 */
	private String selectorNameWithoutSpecialChars;
	/**
	 * the selector name
	 */
	private String selectorName;
}
