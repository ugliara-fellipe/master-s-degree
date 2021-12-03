/**
 *
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
 * Represents a literal  such as 1b, 23, 3.1415, 45E+12
 * This is the superclass of all classes that represent literal numbers,
 * characters etc.
 *
 * @author José
 *
 */
abstract public class ExprLiteral extends ExprAnyLiteral {

	/**
	 *
	 */
	public ExprLiteral(Symbol symbol) {
		this.symbol = symbol;
	}


	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}


	public Symbol getSymbol() {
		return symbol;
	}

	@Override
	public boolean isNRE(Env env) {
		return true;
	}	

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

	    pw.print(symbol.symbolString);
	}


	@Override
	public Symbol getFirstSymbol() {
		return symbol;
	}

	@Override
	public String getJavaName() {
		return NameServer.getJavaNameOfSelector(symbol.symbolString);
	}
	
	/**
	 * remove underscore and suffixes in numbers. Then if the input is
	 * "3.1415D" the return is "3.1415". If the input is
	 *      "1_000_000Long" the return is  "1000000"
	   @param strnum
	   @return
	 */
	static String removeUnderscoreAndSuffix(String strnum) {
		String ret = "";
		for (int i = 0; i < strnum.length(); ++i) {
			char ch = strnum.charAt(i);
			if ( Character.isDigit(ch) ) 
				ret = ret + ch;
		}
		return ret;
	}

	protected Symbol symbol;


}
