/**
  
 */
package meta;

import java.util.ArrayList;
import ast.Expr;
import ast.ExprIdentStar;
import ast.ICalcInternalTypes;
import ast.Statement;
import lexer.Lexer;
import lexer.Symbol;
import lexer.Token;
import saci.Compiler;

/**
 * An abstraction of the Cyan compiler used by regular users to parse literal objects and "at" metaobjects with a DSL 
   @author José
   
 */
public class Compiler_dpa extends CompilerAction_dpa implements ICompiler_dpa {
	

	/**
	 * constructor
	   @param compiler
	   @param cyanMetaobjectAnnotation
	   @param lexer
	   @param leftSeqSymbols, the sequence of symbols that start literal object or the DSL of the "at" metaobject. It may be null
	 */
	public Compiler_dpa(saci.Compiler compiler, Lexer lexer, String leftSeqSymbols) {
		//this.compiler = compiler.clone();
		super(compiler);
		//seqSymbolStack = new Stack<>();
		if ( leftSeqSymbols != null ) {
			rightSeqSymbols = Lexer.rightSymbolSeqFromLeftSymbolSeq(leftSeqSymbols);
			lexer.pushRightSymbolSeq(rightSeqSymbols);
			//seqSymbolStack.push(rightSeqSymbols);
		}
		else
			rightSeqSymbols = null;
		foundEOLO = false;
		endOfLiteralObject = null;
		exprStatList = new ArrayList<>();
	}
	
	/**
	 * Consider character <code>\n</code> as a token if the parameter is true. See {@link Lexer#setNewLineAsToken(boolean)}.
	 */
	public void setNewLineAsToken(boolean newLineAsToken) {
		compiler.setNewLineAsToken(newLineAsToken);
	}
	
	@Override
	public void next() {
		if ( ! foundEOLO ) 
			compiler.next();
	}

	@Override
	public Symbol getSymbol() {
		if ( foundEOLO )
			return this.endOfLiteralObject;
		Symbol sym = compiler.getSymbol();
		String symStr = sym.getSymbolString();
		

		
		if ( symStr.equals(this.rightSeqSymbols) || sym.token == Token.EOF ) {  // sym.token == Token.RIGHTCHAR_SEQUENCE ) { // 
			foundEOLO = true;
			this.endOfLiteralObject =  new Symbol(Token.EOLO, "", sym.getStartLine(), sym.getLineNumber(), 
					sym.getColumnNumber(), sym.getOffset(), sym.getCompilationUnit());
			return endOfLiteralObject; 
		}
		else
			return sym;
	}
	
	@Override
	public void removeLastExprStat() {
		int last = exprStatList.size();
		if ( last > 0 ) 
			exprStatList.remove(last-1);
	}
	
	@Override
	public IExpr expr() {
		Expr e = compiler.expr();
		exprStatList.add(e);
		return e;
	}
	
	@Override
	public IExpr type() {
		Expr t = compiler.type();
		exprStatList.add(t);
		return t;
	}
	
	@Override
	public boolean startType(Token t) {
		return Compiler.startType(t);
	}

	
	@Override
	public IStatement statement() {
		Statement s = compiler.statement();
		exprStatList.add(s);
		return s;
	}
	
	@Override
	public ExprIdentStar parseSingleIdent() {
		Expr e = compiler.parseIdent();
		if ( e instanceof ExprIdentStar ) {
			if ( ((ExprIdentStar ) e).getIdentSymbolArray().size() > 1 ) 
				return null;
			else 
				return (ExprIdentStar ) e;
		}
		else {
			return null;
		}
	}

	@Override
	public Expr parseIdent() {
		return compiler.parseIdent();
	}
	

	@Override
	public boolean symbolCanStartExpr(Symbol symbol) {
		return Compiler.startExpr(symbol);
	}
	

	public void addExprStat(ICalcInternalTypes exprStat) {
		exprStatList.add(exprStat);
	}
	
	public ArrayList<ICalcInternalTypes> getExprStatList() {
		return exprStatList;
	}

	@Override
	public boolean isOperator(Token token) {
		return Compiler.isOperator(token);
	}

	
	@Override
	public void pushRightSymbolSeq(String rightSymbolSeq) {
		compiler.pushRightSymbolSeq(rightSymbolSeq);
	}
	
	@Override
	public boolean isBasicType(Token t) {
		return Compiler.isBasicType(t);
	}

	private String rightSeqSymbols;
	//private Stack<String> seqSymbolStack;
	/**
	 * true if the right char sequence was found. Subsequent calls to 'next' will return EOLO
	 */
	private boolean foundEOLO;
	/**
	 * the end-of-literal-object symbol
	 */
	private Symbol endOfLiteralObject;
	/**
	 * list of expressions and statements returned by calls to {@link Compiler_dpa#expr()} and, in the future, to method statement()
	 */
	private ArrayList<ICalcInternalTypes> exprStatList;
}
