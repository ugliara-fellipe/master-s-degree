package meta;

import ast.Expr;
import ast.MessageWithSelectors;
import ast.Type;
import lexer.Symbol;
import saci.Env;
import saci.Tuple2;

public interface ICompileTimeDoesNotUnderstand_dsa {

	/**
	 * analyze the message send with selectors and return a tuple composed by: 
	 *    a) the source code that should replace the message send and 
	 *    b) the type of the source code returned 
	 */
	@SuppressWarnings("unused")
	default Tuple2<StringBuffer, Type> dsa_analyzeReplaceMessageWithSelectors(Expr receiver, MessageWithSelectors message, Env env) { 
		return null;
	}

	
	/**
	 * analyze the unary message send  and return a tuple composed by: 
	 *    a) the source code that should replace the message send and 
	 *    b) the type of the source code returned 
	 */
	@SuppressWarnings("unused")
	default Tuple2<StringBuffer, Type> dsa_analyzeReplaceUnaryMessage(Expr receiver, Symbol unarySymbol, Env env) {
		return null;
	}
	
}
