/**
 *
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/**
 * represents a while statement
 * @author José
 *
 */
public class StatementWhile extends Statement {

	public StatementWhile(Symbol whileSymbol, Expr booleanExpr,
			   StatementList statementList, Symbol rightCBEndsIf) {
		this.whileSymbol = whileSymbol;
		this.booleanExpr = booleanExpr;
		this.statementList = statementList;
		this.rightCBEndsIf = rightCBEndsIf;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		this.booleanExpr.accept(visitor);
		this.statementList.accept(visitor);
		visitor.visit(this);
	}	
	

	/* (non-Javadoc)
	 * @see ast.Statement#genCyan(ast.PWInterface, boolean)
	 */
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		pw.print("while ");
		booleanExpr.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions );
		if ( printInMoreThanOneLine )
		    pw.println(" {");
		else
			pw.print(" { ");
		pw.add();
		if ( statementList != null )
		    statementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.sub();
		if ( printInMoreThanOneLine )
		    pw.printIdent("}");
		else
			pw.print(" }");
	}

	@Override
	public Symbol getFirstSymbol() {
		return whileSymbol;
	}


	@Override
	public void genJava(PWInterface pw, Env env) {

		pw.printlnIdent("while ( true ) { ");
		pw.add();
	    String tmpVarString = booleanExpr.genJavaExpr(pw, env);
	    
		if ( booleanExpr.getType() == Type.Dyn ) {
			// add convertion from Dyn
			pw.printlnIdent("if ( !(" + tmpVarString + " instanceof CyBoolean) ) {");
			pw.add();
			pw.printlnIdent("throw new ExceptionContainer__("
					+ env.javaCodeForCastException(booleanExpr, Type.Boolean) + " );");
			
			pw.sub();
			pw.println("}");
			pw.printlnIdent("if ( ! ((CyBoolean ) " + tmpVarString + ").b ) break;");
		}
		else {
	        pw.printlnIdent("if ( !" + tmpVarString + ".b ) break;");
		}
	    
	    
	    
 		if ( statementList != null )
		    statementList.genJava(pw, env);
		pw.sub();
		pw.printlnIdent("}");
	}


	@Override
	public void calcInternalTypes(Env env) {
		
		
		booleanExpr.calcInternalTypes(env);
		if ( booleanExpr.getType() != Type.Boolean && booleanExpr.getType() != Type.Dyn )
			env.error(booleanExpr.getFirstSymbol(), "A boolean or Dyn expression is expected in a 'while' statement");
		if ( booleanExpr instanceof ExprWithParenthesis ) {
			env.warning(booleanExpr.getFirstSymbol(), "Parentheses are not necessary around the boolean expression of command 'while'");
		}
		

		int numLocalVariables = env.numberOfLocalVariables();
		
		statementList.calcInternalTypes(env);
		
		int numLocalVariablesToPop = env.numberOfLocalVariables() - numLocalVariables;
		
		env.popNumLocalVariableDec(numLocalVariablesToPop); //parameterList.size());
		super.calcInternalTypes(env);
		
	}

	public StatementList getStatementList() {
		return statementList;
	}
	
	public Symbol getRightCBEndsIf() {
		return rightCBEndsIf;
	}
		

	/**
	 * the symbol 'while'
	 */
	private Symbol whileSymbol;
	/**
	 * boolean expression of the while
	 */
	private Expr booleanExpr;
	/**
	 * list of statements
	 */
	private StatementList statementList;
	
	/**
	 * the '}' symbol that ends a while
	 */
	private Symbol rightCBEndsIf;
	
}
