/**
 *
 */
package ast;

import lexer.Symbol;
import lexer.SymbolByteLiteral;
import lexer.Token;
import saci.Env;
import saci.NameServer;

/** Represents a byte literal such as 1B
 * @author José
 *
 */
public class ExprLiteralByte extends ExprLiteralNumber {

	/**
	 * @param symbol
	 */
	public ExprLiteralByte(Symbol symbol) {
		super(symbol);
		prefix = null;
	}
	public ExprLiteralByte(Symbol symbol, Symbol prefix) {
		super(symbol);
		this.prefix = prefix;
	}


	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		
		type = Type.Byte;
	}

	private String genJavaString() {

		
		return "(new " + NameServer.ByteInJava + "( (byte ) " + (prefix != null && prefix.token == Token.MINUS ? "-" : "") +  
		   ((SymbolByteLiteral ) symbol).getByteValue() + "))";
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

	/*public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString());
	}*/
	

	@Override
	public Object getJavaValue() {
		
		byte n = Byte.parseByte(symbol.getSymbolString());
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
		return "Byte";
	}

	
}
