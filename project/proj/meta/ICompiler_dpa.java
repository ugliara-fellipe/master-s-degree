package meta;

import ast.Expr;
import ast.ExprIdentStar;
import lexer.Symbol;
import lexer.Token;

/**
 * Compiler interface for metaobjects during parsing. These metaobjects can retrieve 
 * tokens using the Cyan compiler but only tokens that are inside the metaobject annotation. 
 * Therefore the metaobject annotation should be delimited by a sequence of symbols or otherwise. 
 * See {@link IParseWithCyanCompiler_dpa}.  
 * 
   @author José
 */
public interface ICompiler_dpa extends ICompilerAction_dpa {

	
	void next();
	Symbol getSymbol();
	boolean symbolCanStartExpr(Symbol symbol);
	/**
	 * all expressions and statements analyzed by methods {@link ICompiler_dpa#expr()}, {@link ICompiler_dpa#type()}, and
	 * {@link ICompiler_dpa#statement()} should be kept by the compiler in a list. In the semantic analysis, 
	 * the compiler calculates the types of the expressions and statements of this list.
	 * 
	 *  Method removeLastExprStat removes the last element of this list. This is necessary when
	 *  the last expression was mistakenly taken as an expression when it is in fact an element of
	 *  the DSL that follows the DSL. For exemple,<br>
	 *  <code><br>
	 *  {@literal @}shouldbe(R, S){*<br>
	 *      1 + 1 == 2,<br>
	 *      symbol R,<br>
	 *      localVariable S<br>
	 *  *}<br>
	 *  </code>
	 *  Here <code>"symbol"</code> and <code>"localVariable"</code> are initially taken
	 *  to be expressions but they are in fact keywords of the DSL whose code in between
	 *  <code>{*</code> and <code>*}</code>. In this case, the metaobject compile for 
	 *  this DSL should use removeLastExprStat to remove <code>"symbol"</code> and <code>"localVariable"</code>
	 *  from the list.  
	 */
	void removeLastExprStat();
	
	IExpr type();
	IExpr expr();
	boolean startType(Token t);
	IStatement statement();
	ExprIdentStar parseSingleIdent();


	
	boolean isOperator(Token token);

	
	void pushRightSymbolSeq(String rightSymbolSeq);
	Expr parseIdent();
	
	boolean isBasicType(Token t); 
}
