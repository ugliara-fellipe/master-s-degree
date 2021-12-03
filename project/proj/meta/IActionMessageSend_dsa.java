package meta;

import ast.Expr;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.Type;
import lexer.Symbol;
import saci.Env;
import saci.Tuple2;

/**
 * this interface is identical to {@link meta#ICompileTimeDoesNotUnderstand_dsa}. It is only 
 * used when a method for a message send WAS FOUND at compile-time.  
   @author jose
 */
public interface IActionMessageSend_dsa {

	/**
	 * analyze the message send with selectors and return a tuple composed by: 
	 *    a) the source code that should replace the message send and 
	 *    b) the type of the source code returned 
	 */
	@SuppressWarnings("unused")
	default Tuple2<StringBuffer, Type> dsa_analyzeReplaceMessageWithSelectors( 
			ExprMessageSendWithSelectorsToExpr messageSendExpr, Env env) { 
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
