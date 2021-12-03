/**
  
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/**
   @author José
   
 */
public class StatementReturnMethod extends Statement {

	public StatementReturnMethod(Symbol returnSymbol, Expr expr) {
		super(false);
		this.expr = expr;
		this.returnSymbol = returnSymbol;
	}

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print("return_method__ ");
		expr.genCyan(pw, false, cyanEnv, genFunctions);
	}

	@Override
	public void genJava(PWInterface pw, Env env) {

		String tmpVar = expr.genJavaExpr(pw, env);
		
		
		pw.printlnIdent("return " + tmpVar + ";");
		
	}

	@Override
	public Symbol getFirstSymbol() {
		return returnSymbol;
	}


	
	@Override
	public void calcInternalTypes(Env env) {
		expr.calcInternalTypes(env);
		super.calcInternalTypes(env);
	}

	public Expr getExpr() {
		return expr;
	}


	private Symbol returnSymbol;
	private Expr expr;	
}
