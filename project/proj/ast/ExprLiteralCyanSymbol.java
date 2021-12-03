/**
 *
 */
package ast;

import lexer.Symbol;
import lexer.SymbolCyanSymbol;
import saci.CyanEnv;
import saci.Env;
import saci.Function0;

/** Represents a Cyan Symbol when used in an expression
 * @author José
 *
 */
public class ExprLiteralCyanSymbol extends ExprLiteral {

	public ExprLiteralCyanSymbol(SymbolCyanSymbol symbol) {
		super(symbol);
		this.symbol = symbol;
	}


	/* (non-Javadoc)
	 * @see ast.Expr#genCyan(ast.PWInterface, boolean)
	 */
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		String s;
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			s = cyanEnv.formalGenericParamToRealParam(symbol.getSymbolString());
		}
		else {
			s = symbol.getSymbolString();
		}

		
		if ( ((SymbolCyanSymbol ) symbol).isBetweenQuotes() ) {
			pw.print("#\"" + s + "\"");
		}
		else 
			pw.print("#" + s);
	}

	

	
	/* (non-Javadoc)
	 * @see ast.Expr#genJavaExpr(ast.PWInterface, saci.Env)
	 */
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		/*
		String tmpVar = NameServer.nextLocalVariableName();
		pw.printlnIdent(tmpVar + " = new " + NameServer.getJavaNameIdentifier("String") + "(\"" + symbol.symbolString + "\");");
		return tmpVar;
		*/
		return "(new _CySymbol(" + "\"" + symbol.symbolString + "\"))";
	}

	/*
	@Override
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaExpr(pw, env));
	}
	*/
	
	
	@Override
	public void calcInternalTypes(Env env) {
		super.calcInternalTypes(env);
		
		type = Type.CySymbol;
	}
	
	
	/* (non-Javadoc)
	 * @see ast.Statement#getFirstSymbol()
	 */
	@Override
	public Symbol getFirstSymbol() {
		return symbol;
	}

	@Override
	public Object getJavaValue() {
		return "\"" + symbol.getSymbolString() + "\"";
	}
	
	
	public void setSymbol(SymbolCyanSymbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public StringBuffer getStringJavaValue() {
		return new StringBuffer( (String ) getJavaValue());
	}


	@Override
	public String getJavaType() {
		return "String";
	}

	
	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		return asString();
	}	
	
	
}
