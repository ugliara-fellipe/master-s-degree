package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/**
 *  represents a 'break' statement
   @author jose
 */
public class StatementBreak extends Statement {

	public StatementBreak(Symbol breakSymbol) {
		this.breakSymbol = breakSymbol;
	}
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		if ( printInMoreThanOneLine ) 
			pw.printlnIdent("break;");
		else
			pw.printIdent("break;");
	}

	@Override
	public Symbol getFirstSymbol() {
		return breakSymbol;
	}

	@Override
	public void genJava(PWInterface pw, Env env) {
		pw.printlnIdent("break;");
	}

	public Symbol getBreakSymbol() {
		return breakSymbol;
	}
	
	@Override
	public boolean demandSemicolon() { return false; }
	
	private Symbol breakSymbol;
}
