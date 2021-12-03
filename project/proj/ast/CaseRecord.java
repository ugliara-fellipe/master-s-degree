package ast;

import lexer.Symbol;
import saci.CyanEnv;

public class CaseRecord {

	public CaseRecord(Symbol caseSymbol, Expr exprType, StatementLocalVariableDec caseVariable, 
			StatementList statementList, Symbol rightCBEndsIf) {
		super();
		this.caseSymbol = caseSymbol;
		this.exprType = exprType;
		this.caseVariable = caseVariable;
		this.statementList = statementList;
		this.rightCBEndsIf = rightCBEndsIf;
	}	

	public void accept(ASTVisitor visitor) {
		this.statementList.accept(visitor);
		visitor.visit(this);
	}
	
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine,
			CyanEnv cyanEnv, boolean genFunctions) {
		pw.printIdent("case ");
		exprType.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		if ( this.caseVariable != null ) {
			pw.print(" ");
			pw.print(this.caseVariable.getName());
		}
		pw.println(" {");
		pw.add();
		statementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.sub();
		pw.printlnIdent("}");
		
		
	}
	
	public Symbol getFirstSymbol() {
		return caseSymbol;
	}
	

	private Symbol caseSymbol;

	public Expr exprType;
	public StatementLocalVariableDec caseVariable;
	/**
	 * list of statements
	 */
	public StatementList statementList;
	public Symbol rightCBEndsIf;
}
