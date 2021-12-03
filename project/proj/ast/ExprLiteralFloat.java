/**
 *
 */
package ast;

import lexer.Symbol;
import lexer.SymbolFloatLiteral;
import lexer.Token;
import saci.Env;
import saci.NameServer;

/** Represents a float literal such as 2.71F
 * @author José
 *
 */
public class ExprLiteralFloat extends ExprLiteralNumber {

	/**
	 * @param symbol
	 */
	public ExprLiteralFloat(Symbol symbol) {
		super(symbol);
	}

	public ExprLiteralFloat(Symbol symbol, Symbol prefix) {
		super(symbol);
		this.prefix = prefix;
	}
	
	
	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		type = Type.Float;
	}
	
	
	private String genJavaString(Env env) {
		
		return "(new " + NameServer.FloatInJava + "( (float ) " + (prefix != null && prefix.token == Token.MINUS ? "-" : "") + 
		((SymbolFloatLiteral ) symbol).getOriginalFloatString() + "))";
	}
	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		/*
		String s = genJavaString(env);
		String varName = NameServer.nextLocalVariableName();
		pw.printlnIdent(varName + " = " + s + ";");
		return varName; */
		return genJavaString(env);
	}

	/*
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString(env));
	}
	*/

	
	@Override
	public Object getJavaValue() {
		float n = Float.parseFloat(symbol.getSymbolString());
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
		return "Float";
	}

		
}
