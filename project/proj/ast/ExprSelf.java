/**
 *
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/** Represents an expression that is just "self" as in
 *     anObject = self;
 * @author José
 *
 */
public class ExprSelf extends Expr {

	/**
	 *
	 */
	public ExprSelf(Symbol selfSymbol, ProgramUnit currentProgramUnit) {
		this.selfSymbol = selfSymbol;
		type = currentProgramUnit; 
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
	
	
	
	@Override
	public boolean mayBeStatement() {
		return false;
	}
	
	

	public void setSelfSymbol(Symbol selfSymbol) {
		this.selfSymbol = selfSymbol;
	}

	public Symbol getSelfSymbol() {
		return selfSymbol;
	}


	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		if ( cyanEnv.getCreatingInnerPrototypesInsideEval() ) {
			pw.print(NameServer.selfNameInnerPrototypes);
		}
		else if ( cyanEnv.getCreatingContextObject() ) 
			/*
			 * in this case, 'self' is being used inside a prototype created from a context function  
			 */
			pw.print(NameServer.selfNameContextObject + " ");
		else
			/*
			 * in this case, 'self' is being used inside a prototype created from a function or a outer prototype method. 
			 */
			pw.print("self");
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		
		
		if ( env.getCreatingInnerPrototypesInsideEval() ) {
			return NameServer.javaSelfNameInnerPrototypes;
		}
		else 
			return "this";
	}

	@Override
	public Symbol getFirstSymbol() {
		return selfSymbol;
	}

	
	@Override
	public void calcInternalTypes(Env env) {
		
		
		type = env.getCurrentProgramUnit();
		

		String currentMethodName = env.getCurrentMethod().getNameWithoutParamNumber();
		if ( currentMethodName.equals("init") || currentMethodName.equals("init:") ) {
			/**
			 * inside an init or init: method it is illegal to access 'self' 
			 */
			env.error(this.getFirstSymbol(),  "Access to 'self' inside an 'init' or 'init:' method. This is illegal because some "
					+ "instance variables may not have been initialized");
		}
		
		super.calcInternalTypes(env);
		
	}

	
	private Symbol selfSymbol;



}
