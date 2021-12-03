/**
 *
 */
package ast;

import lexer.Symbol;
import lexer.SymbolShortLiteral;
import lexer.Token;
import saci.Env;
import saci.NameServer;

/** Represents a short literal such as
 *     1S
 * @author José
 *
 */
public class ExprLiteralShort extends ExprLiteralNumber {

	/**
	 * @param symbol
	 */
	public ExprLiteralShort(Symbol symbol) {
		super(symbol);
	}

	
	public ExprLiteralShort(Symbol symbol, Symbol prefix) {
		super(symbol);
		this.prefix = prefix;
	}	
	
	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		type = Type.Short;
	}
	
	
	private String genJavaString(Env env) {
		
		return "(new " + NameServer.ShortInJava + "( (short ) " + (prefix != null && prefix.token == Token.MINUS ? "-" : "") + 
				((SymbolShortLiteral) symbol).getShortLiteral() + "))";
	}

	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		/*
		String s = genJavaString(env);
		String varName = NameServer.nextLocalVariableName();
		pw.printlnIdent(varName + " = " + s + ";");
		return varName;
		*/
		return genJavaString(env);
	}

	/*
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString(env));
	}
	*/


	@Override
	public Object getJavaValue() {
		short n = Short.parseShort(symbol.getSymbolString());
		if ( prefix != null && prefix.token == Token.MINUS ) {
			return -n;
		}
		else
			return n;
		
	}

	@Override
	public StringBuffer getStringJavaValue() {
		return new StringBuffer("\"" + getJavaValue() + "\"");
	}


	@Override
	public String getJavaType() {
		return "Short";
	}

	
	
}
