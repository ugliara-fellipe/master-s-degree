/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/**
 * represents an if statement. Its grammar is
 * 
    IfStat} ::= ``if"\/ ``("\/ Expr ``)"\/ \{ StatementList \}   \\
\rr \{ ``else"\/ ``if"\/ ``("\/ Expr ``)"\/ \{ StatementList \}VoidVoid  \}\\
\rr [ ``else"\/ \{ StatementList \} ]

 * @author José
 *
 */
public class StatementIf extends Statement {

    // 	return new StatementIf(ifSymbol, ifExprList, ifStatementList, elseStatementList);

	public StatementIf(Symbol ifSymbol, ArrayList<Expr> ifExprList, 
			           ArrayList<StatementList> ifStatementList, 
                       StatementList elseStatementList, Symbol rightCBEndsIf, Symbol lastElse) {
		this.ifSymbol = ifSymbol;
		this.ifExprList = ifExprList;
		this.ifStatementList = ifStatementList;
		this.elseStatementList = elseStatementList;
		this.rightCBEndsIf = rightCBEndsIf;
		this.lastElse = lastElse;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		
		for ( Expr ifExpr : this.ifExprList ) {
			ifExpr.accept(visitor);
		}
		for ( StatementList statList : this.ifStatementList ) {
			statList.accept(visitor);
		}
		if ( elseStatementList != null ) 
			elseStatementList.accept(visitor);
		visitor.visit(this);
	}	
	

	/* (non-Javadoc)
	 * @see ast.Statement#genCyan(ast.PWInterface, boolean)
	 */
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		printInMoreThanOneLine = true;
		int size = this.ifExprList.size();
		for (int i = 0; i < size; i++) {
			Expr booleanExpr = ifExprList.get(i);
			if ( i == 0 ) 
				pw.print("if ");
			else
				pw.printIdent("if ");
			booleanExpr.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions );
			if ( printInMoreThanOneLine )
				pw.println(" {");
			else
				pw.print(" { ");
			pw.add();
			StatementList thenStatementList = this.ifStatementList.get(i);
			thenStatementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			pw.sub();
			if ( printInMoreThanOneLine ) {
				if ( elseStatementList != null )
					pw.printlnIdent("}");
				else
					pw.printIdent("}");
			}
			else
				pw.print(" } ");
			if ( i < size - 1 ) 
				pw.printIdent("else");
		}

		if ( elseStatementList != null ) {
			if ( printInMoreThanOneLine )
				pw.printlnIdent("else {");
			else
				pw.print(" else {");
			pw.add();
			if ( elseStatementList != null )
				elseStatementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			pw.sub();
			if ( printInMoreThanOneLine )
				pw.printlnIdent("}");
			else
				pw.print(" }");
		}
	}


	@Override
	public Symbol getFirstSymbol() {
		return ifSymbol;
	}


	static int count = 0;

	@Override
	public void genJava(PWInterface pw, Env env) {
		int size = this.ifExprList.size();
		for (int i = 0; i < size; i++) {
			Expr booleanExpr = ifExprList.get(i);

			
			String tmpVarString = booleanExpr.genJavaExpr(pw, env);
			if ( booleanExpr.getType() == Type.Dyn ) {
				// add convertion from Dyn
				pw.printlnIdent("if ( !(" + tmpVarString + " instanceof CyBoolean) ) {");
				pw.add();
				
				pw.printlnIdent("throw new ExceptionContainer__("
						+ env.javaCodeForCastException(booleanExpr, Type.Boolean) + " );");
				
				pw.sub();
				pw.printlnIdent("}");
				pw.printlnIdent("if ( ((CyBoolean ) " + tmpVarString + ").b ) {");
			}
			else {
				pw.printlnIdent("if ( " + tmpVarString + ".b ) {");
			}
			pw.add();
			StatementList thenStatementList = this.ifStatementList.get(i);
			
			int localident = pw.getCurrentIndent();
			thenStatementList.genJava(pw, env);
			pw.set(localident);
			
			pw.sub();
			pw.printlnIdent("}");
			if ( i < size - 1 ) {
				pw.printlnIdent("else {");
				pw.add();
			}
			
		}

		if ( elseStatementList != null ) {
			pw.printlnIdent("else {");
			pw.add();
			elseStatementList.genJava(pw, env);
			pw.sub();
			pw.printlnIdent("}");
		}
		pw.sub();
		for ( int i = 0; i < size - 1; ++i ) {
			pw.printlnIdent("}");
			pw.sub();
		}
		pw.add();
		pw.printlnIdent("// end of if");
	}
		


	public ArrayList<Expr> getElseIfExprList() {
		return ifExprList;
	}

	public void setElseIfExprList(ArrayList<Expr> elseIfExprList) {
		this.ifExprList = elseIfExprList;
	}

	public ArrayList<StatementList> getElseIfStatementList() {
		return ifStatementList;
	}

	public void setElseIfStatementList(ArrayList<StatementList> elseIfStatementList) {
		this.ifStatementList = elseIfStatementList;
	}

	@Override
	public void calcInternalTypes(Env env) {
		
		
		for ( Expr e : ifExprList ) {
			e.calcInternalTypes(env);
			if ( e.getType() != Type.Boolean && e.getType() != Type.Dyn )
				env.error(e.getFirstSymbol(), "A Boolean or Dyn expression is expected in an 'if' statement");
			
			if ( e instanceof ExprWithParenthesis ) {
				env.warning(e.getFirstSymbol(), "Parentheses are not necessary around the boolean expression of command 'if'");
			}

		}
		

		
		for ( StatementList statement : ifStatementList ) {
			int numLocalVariables = env.numberOfLocalVariables();
			statement.calcInternalTypes(env);
			int numLocalVariablesToPop = env.numberOfLocalVariables() - numLocalVariables;
			
			env.popNumLocalVariableDec(numLocalVariablesToPop); //parameterList.size());
		}
		
		
		int numLocalVariables = env.numberOfLocalVariables();
		
		if ( elseStatementList != null ) {
			elseStatementList.calcInternalTypes(env);
			int numLocalVariablesToPop = env.numberOfLocalVariables() - numLocalVariables;
			
			env.popNumLocalVariableDec(numLocalVariablesToPop); //parameterList.size());			
		}
		super.calcInternalTypes(env);
	}

	public StatementList getElseStatementList() {
		return elseStatementList;
	}


	@Override
	public boolean alwaysReturn() {
		if ( elseStatementList == null )
			//  without 'else', may not return
			return false;
		else {
			// one or more 'if's. if one of the lists do not return, then do not return 
			for ( StatementList statementList : ifStatementList ) {
				if ( ! statementList.alwaysReturn() ) {
					return false;
				}
			}
			return elseStatementList.alwaysReturn();
		}
		
	}
	
	public boolean alwaysReturnFromFunction() {
		if ( elseStatementList == null )
			//  without 'else', may not return
			return false;
		else {
			// one or more 'if's. if one of the lists do not return, then do not return 
			for ( StatementList statementList : ifStatementList ) {
				if ( ! statementList.alwaysReturnFromFunction() ) {
					return false;
				}
			}
			return elseStatementList.alwaysReturnFromFunction();
		}
		
	}

	@Override
	public boolean statementDoReturn() {
		return alwaysReturnFromFunction();
	}	


	public Symbol getRightCBEndsIf() {
		return rightCBEndsIf;
	}
	
	/**
	 * the symbol 'if'
	 */
	private Symbol ifSymbol;
	/**
	 * list of else statements
	 */
	private StatementList  elseStatementList;
	/**
	 * list of "else if" boolean expressions 
	 */
	private ArrayList<Expr> ifExprList;
	/**
	 * list of "else if" statements
	 */
	private ArrayList<StatementList> ifStatementList;
	/**
	 * the '}' symbol that ends an if
	 */
	private Symbol rightCBEndsIf;
	/**
	 * the last 'else' symbol of an 'if' statement. Of null if none
	 */
	private Symbol lastElse;

	public Symbol getLastElse() {
		return lastElse;
	}
}
