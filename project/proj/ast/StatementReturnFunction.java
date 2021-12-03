package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/**
 * represents a return statement, like
 * 	   public proc get -> int {
 *         return n;
 *     }
 * @author José
 *
 */
public class StatementReturnFunction extends Statement {

	public StatementReturnFunction(Symbol returnSymbol, Expr expr, ExprFunction currentFunction) {
		super(false);
		this.expr = expr;
		this.returnSymbol = returnSymbol;
	}

	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}	
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		if ( cyanEnv.getCreatingInnerPrototypeFromFunction() ) {
			pw.print("return ");
		}
		else 
			pw.print("^ ");
		expr.genCyan(pw, false, cyanEnv, genFunctions);
	}

	@Override
	public void genJava(PWInterface pw, Env env) {
		env.error(this.getFirstSymbol(), "Internal error: genJava of StatementReturnFunction should never be called", true, true);
	}

	@Override
	public Symbol getFirstSymbol() {
		return returnSymbol;
	}


	@Override
	public boolean statementDoReturn() {
		return true;
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
