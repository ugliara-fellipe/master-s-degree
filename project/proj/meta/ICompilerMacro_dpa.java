package meta;

import ast.CyanMetaobjectMacroCall;
import ast.Expr;
import ast.Statement;
import error.ErrorKind;
import lexer.Symbol;
import saci.CompilationStep;

/**
 * compiler interface for macros 
   @author José
 */
public interface ICompilerMacro_dpa extends IAbstractCyanCompiler {
	
	void setCyanMetaobjectMacro(CyanMetaobjectMacroCall macroCall);
	void next();
	Symbol getSymbol();
	Symbol getLastSymbol();
	boolean symbolCanStartExpr(Symbol symbol);
	Expr expr();
	Expr exprBasicTypeLiteral();
	Statement statement();
	Expr functionDec();
	void error(Symbol sym, String specificMessage, String identifier, ErrorKind errorKind, String ...furtherArgs);
	default void error(Symbol sym, String specificMessage) {
		error(sym, specificMessage, null, ErrorKind.metaobject_error);
	}
	void setThereWasErrors(boolean wasError);
	boolean getThereWasErrors();
	CompilationStep getCompilationStep();
	
}
