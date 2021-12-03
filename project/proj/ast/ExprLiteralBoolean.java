/**
 *
 */
package ast;


import lexer.Symbol;
import saci.Env;
import saci.Function0;
import saci.NameServer;

/**
 * @author José
 *
 */
public class ExprLiteralBoolean extends ExprLiteral {

	/**
	 * @param symbol
	 */
	public ExprLiteralBoolean(Symbol symbol) {
		super(symbol);
	}

	
	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		
		type = Type.Boolean;
	}

	
	private String genJavaString() {
		return NameServer.BooleanInJava + "." + (symbol.getSymbolString().equals("true") ? "cyTrue" : "cyFalse") ;
	}
	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		String s = genJavaString();
		String varName = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent(NameServer.getJavaName("Boolean") + " " + varName + " = " + s + ";");
		return varName;
	}

	/*public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString());
	}*/


	@Override
	public Object getJavaValue() {
		return symbol.getSymbolString().compareTo("true") == 0 ? true : false;
	}


	@Override
	public StringBuffer getStringJavaValue() {
		return new StringBuffer(symbol.getSymbolString().compareTo("true") == 0 ? "true" : "false");
	}


	@Override
	public String getJavaType() {
		return "Boolean";
	}

	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		return symbol.getSymbolString().compareTo("true") == 0 ? "true" : "false";
	}
}
