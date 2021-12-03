/**
 *
 */
package ast;

import lexer.Symbol;
import lexer.SymbolLongLiteral;
import lexer.Token;
import saci.Env;
import saci.NameServer;

/** Represents a long literal such as
 *       1L,  5636363636L
 * @author José
 *
 */
public class ExprLiteralLong extends ExprLiteralNumber {

	/**
	 * @param symbol
	 */
	public ExprLiteralLong(Symbol symbol) {
		super(symbol);
	}

	public ExprLiteralLong(Symbol symbol, Symbol prefix) {
		super(symbol);
		this.prefix = prefix;
	}


	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		type = Type.Long;
	}
	
	
	private String genJavaString(Env env) {
		
		return "(new " + NameServer.LongInJava + "( (long ) " + (prefix != null && prefix.token == Token.MINUS ? "-" : "") 
				+ ((SymbolLongLiteral ) symbol).getLongValue() + "))";
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
		long n = Long.parseLong(symbol.getSymbolString());
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
		return "Long";
	}


}
