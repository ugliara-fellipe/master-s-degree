/**
 *
 */
package ast;

import lexer.Symbol;
import lexer.SymbolIntLiteral;
import lexer.Token;
import saci.Env;
import saci.NameServer;

/** Represents an int literal such as
 *      1, 2, 3I, 5I
 * @author José
 *
 */
public class ExprLiteralInt extends ExprLiteralNumber {

	/**
	 * @param symbol
	 */
	public ExprLiteralInt(Symbol symbol) {
		super(symbol);
	}

	public ExprLiteralInt(Symbol symbol, Symbol prefix) {
		super(symbol);
		this.prefix = prefix;
	}

	
	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		type = Type.Int;
	}
	
	
	private String genJavaString() {
		int n = ((SymbolIntLiteral ) symbol).getIntValue();
		
		if ( prefix != null && prefix.token == Token.MINUS  ) {
			return "(new " + NameServer.IntInJava + "( (int ) " + "-"  
					+ ((SymbolIntLiteral) symbol).getIntValue() + "))";
		}
		else if ( n == 0 )
			return NameServer.IntInJava + ".zero";
		else if ( n == 1 ) 
			return NameServer.IntInJava + ".one";
		else if ( n == 2 ) 
			return NameServer.IntInJava + ".two";
		else 
			return "(new " + NameServer.IntInJava + "( (int ) "  
					+ ((SymbolIntLiteral) symbol).getIntValue() + "))";
	}
		

	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		/*
		String s = genJavaString(env);
		String varName = NameServer.nextLocalVariableName();
		pw.printlnIdent(varName + " = " + s + ";");
		return varName;
		*/
		return genJavaString();
	}

	/*
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString());
	}
	*/


	@Override
	public Object getJavaValue() {
		int n = Integer.parseInt(symbol.getSymbolString());
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
		return "Integer";
	}



}
