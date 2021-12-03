/**
 *
 */
package ast;

import lexer.Symbol;
import saci.Env;
import saci.Function0;
import saci.NameServer;

/** represents the object nil of Cyan
 * @author José
 *
 */
public class ExprLiteralNil extends ExprLiteral {

	/**
	 * @param symbol
	 */
	public ExprLiteralNil(Symbol symbol) {
		super(symbol);
	}

	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		type = Type.Nil;
	}
	

	@SuppressWarnings("static-method")
	private String genJavaString(Env env) {
		return NameServer.NilInJava + ".prototype"; 
	}
	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		return genJavaString(env);
	}

	/*
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString(env));
	}
	*/
	

	@Override
	public Object getJavaValue() {
		return symbol.getSymbolString() ;
	}

	@Override
	public StringBuffer getStringJavaValue() {
		return new StringBuffer("\"null\"");
	}


	@Override
	public String getJavaType() {
		return "Object";
	}

	
	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		// "\"" + t.f2.asString() + "\""
		return "\"" + asString() + "\"";
	}
	
}
