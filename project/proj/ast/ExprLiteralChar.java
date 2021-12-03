/**
 *
 */
package ast;

import lexer.Lexer;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.Function0;
import saci.NameServer;


/** Represents a character literal such as
 *      'a',  'T'
 *
 * @author José
 *
 */
public class ExprLiteralChar extends ExprLiteral {

	/**
	 * @param symbol
	 */
	public ExprLiteralChar(Symbol symbol) {
		super(symbol);
	}


	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		
		type = Type.Char;
	}
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

	    pw.print("'" + symbol.symbolString + "'");
	}
	
	private String genJavaString(Env env) {
		return "(new " + NameServer.CharInJava + "('" + symbol.getSymbolString() + "'))";
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
		return "\'" + Lexer.unescapeJavaString(symbol.getSymbolString()).charAt(0) + "\'" ;
	}

	@Override
	public StringBuffer getStringJavaValue() {
		return new StringBuffer("\"" + symbol.getSymbolString() + "\"");
	}


	@Override
	public String getJavaType() {
		return "Character";
	}

	
	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		return asString();
	}	
	
}
