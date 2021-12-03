/**
 *
 */
package ast;

import java.util.ArrayList;
import error.CompileErrorException;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**  Represents a list of statements as those of a method or function
 *
 * @author José
 *
 */
public class StatementList implements GenCyan, ASTNode {

	public StatementList(ArrayList<Statement> statementList) {
		super();
		this.statementList = statementList;
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		for ( Statement stat : this.statementList ) {
			stat.accept(visitor);
		}
		visitor.visit(this);
	}
		
	
	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		/*int numChars = 0;
		for ( Statement s : statementList ) {
			numChars += PWCounter.getNumChars(s);
		} */

		
		int size = statementList.size();
		if ( size > 1 ) 
			printInMoreThanOneLine = true;
		for ( Statement s : statementList ) {
			if ( printInMoreThanOneLine )
				pw.printIdent("");  
			boolean moreThanOneLine = PWCounter.printInMoreThanOneLine(s); 
			s.genCyan(pw, moreThanOneLine, cyanEnv, genFunctions );
			--size;
			if ( size > 0 ) {
				if ( printInMoreThanOneLine )
					pw.println("; ");
				else
					pw.print("; ");
			}
		}
		if ( printInMoreThanOneLine )
			pw.println("");
	}
	

	public void genJava(PWInterface pw, Env env) {
		for ( Statement s : statementList ) {
			env.pushCode(s);

			s.genJava(pw, env);
			if ( s.addSemicolonJavaCode() )
				pw.println(";");

			/*
			if ( s instanceof ExprIdentStar ) {
				pw.printlnIdent(((ExprIdentStar) s).genJavaExpr(pw, env) + ";");
			}
			else {
			}
			*/
			env.popCode();
		}
	}


	public void setStatementList(ArrayList<Statement> statementList) {
		this.statementList = statementList;
	}

	public ArrayList<Statement> getStatementList() {
		return statementList;
	}


	public void calcInternalTypes(Env env) {
		
		foundError = false;
		env.addLexicalLevel();
		boolean topLevelStatements = env.getTopLevelStatements();
		for ( Statement statement : statementList ) {
			
			try {
				env.pushCode(statement);
				env.setTopLevelStatements(false);
				statement.calcInternalTypes(env);
				env.setTopLevelStatements(topLevelStatements);
				env.setFirstMethodStatement(false);
				
				/*
				 * if it is an initialization of an instance variable inside 
				 * an 'init' or 'init:' method, set the variable as such
				 */
				if ( env.getCurrentMethod() != null && env.getCurrentMethod().isInitMethod() ) {
					if ( statement instanceof ast.StatementAssignmentList ) {
						ArrayList<Expr> exprList = ((StatementAssignmentList) statement).getExprList();
						for (int j = 0; j < exprList.size() - 1; ++j) {
							Expr anExpr = exprList.get(j);
							if ( anExpr instanceof ExprIdentStar ) {
								ExprIdentStar id = (ExprIdentStar) anExpr;
								if ( id.getIdentStarKind() == IdentStarKind.instance_variable_t ) {
									InstanceVariableDec iv = (InstanceVariableDec ) id.getVarDeclaration();
									if ( iv.isShared() ) {
										env.error(statement.getFirstSymbol(), "Shared instance variables cannot be initialized in 'init' or 'init:' methods");
									}
									if ( topLevelStatements ) {
										iv.setWasInitialized(true);
									}
								}
							}
							else if (  anExpr instanceof ExprSelfPeriodIdent ) {
								ExprSelfPeriodIdent exprSelf = (ExprSelfPeriodIdent ) anExpr;
								InstanceVariableDec iv = exprSelf.getInstanceVariableDec();
								if ( iv.isShared() ) {
									env.error(statement.getFirstSymbol(), "Shared instance variables cannot be initialized in 'init' or 'init:' methods");
								}
								if ( topLevelStatements ) {
									iv.setWasInitialized(true);
								}
							}
						}
					}
					
				}

				if ( ! statement.mayBeStatement() ) {
					env.error(statement.getFirstSymbol(), "Statement does nothing");
				}
				/*
				if ( statement instanceof ExprIdentStar ) {
					ExprIdentStar e = (ExprIdentStar ) statement;
					if ( e.getIdentStarKind() != IdentStarKind.unaryMethod_t )
						env.error(e.getFirstSymbol(), "Statement does nothing");
				}
				*/
				
			}
			catch ( CompileErrorException e ) {
				foundError = true;
			}
			catch (RuntimeException e) {
				foundError = true;
				e.printStackTrace();
				env.error(statement.getFirstSymbol(), "Internal error in StatementList");
			}
			finally {
				env.popCode();
			}
			
		}
		env.removeVariablesLastLevel();
		env.subLexicalLevel();

		if ( ! foundError ) {
			for ( int i = 1; i < statementList.size(); ++i ) {
				if ( statementList.get(i-1).alwaysReturn() ) 
					env.error(statementList.get(i).getFirstSymbol(), "unreachable statement");
			}
		}
	}

	public String asString(CyanEnv cyanEnv) {
		PWCharArray pwChar = new PWCharArray();
		genCyan(pwChar, true, cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}
	
	@Override
	public String asString() {
		return asString(NameServer.cyanEnv);
	}

	public boolean alwaysReturn() {
		
		if ( statementList.size() == 0 ) 
			return false;
		else 
			return statementList.get(statementList.size()-1).alwaysReturn();
	}

	public boolean alwaysReturnFromFunction() {
		
		if ( statementList.size() == 0 ) 
			return false;
		else 
			return statementList.get(statementList.size()-1).statementDoReturn();
	}
	

	public boolean getFoundError() {
		return foundError;
	}

	
	private ArrayList<Statement> statementList;
	
	/**
	 * true if some semantic error was found in some statement
	 */
	private boolean foundError;


}
