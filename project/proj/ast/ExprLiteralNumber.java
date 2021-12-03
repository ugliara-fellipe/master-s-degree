package ast;

import lexer.Symbol;
import lexer.Token;
import saci.CyanEnv;
import saci.Function0;

abstract public class ExprLiteralNumber extends ExprLiteral {

	
	public ExprLiteralNumber(Symbol symbol) {
		super(symbol);
	}
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

	    pw.print((prefix != null && prefix.token == Token.MINUS ? "-" : "") + symbol.symbolString);
	}

	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		return asString();
	}	

	/**
	 * it may be Token.MINUS if the literal is preceded by '-'. If not, prefix is null
	 */
	protected Symbol prefix;

}
