package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
*   represents the access to an instance variable of self__.
*   
* @author José
*
*/
public class ExprSelf__PeriodIdent extends Expr implements LeftHandSideAssignment  {

	/**
	 *
	 */
	public ExprSelf__PeriodIdent(Symbol selfSymbol, Symbol identSymbol) {
		this.selfSymbol = selfSymbol;
		this.identSymbol = identSymbol;
	}


	/*
	 * should not be called
	   @see ast.Expr#accept(ast.ASTVisitor)
	 */
	@Override
	public void accept(ASTVisitor visitor) {
		
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


	public void setIdentSymbol(Symbol identSymbol) {
		this.identSymbol = identSymbol;
	}


	public Symbol getIdentSymbol() {
		return identSymbol;
	}


	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
	
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print("self__." + cyanEnv.formalGenericParamToRealParam(identSymbol.getSymbolString()));
		}
		else {
			String name = identSymbol.getSymbolString();
			pw.print("self." + name);
		}		
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		String tmpVar;
		tmpVar = NameServer.getJavaName(identSymbol.getSymbolString())
				+ (instanceVariableDec.getRefType() ? ".elem" : "");
		return tmpVar;
	}

	@Override
	public void genJavaCodeVariable(PWInterface pw, Env env) {
		pw.print(NameServer.getJavaName(identSymbol.getSymbolString()));
		if ( instanceVariableDec.getRefType() )
			pw.print(".elem");
	}
	

	@Override
	public Symbol getFirstSymbol() {
		return selfSymbol;
	}


	@Override
	public void calcInternalTypes(Env env) {
		
		
		InstanceVariableDec iv = env.searchInstanceVariable(NameServer.selfNameInnerPrototypes);
		if ( iv != null ) {
			ObjectDec obj = (ObjectDec ) iv.getType();
			instanceVariableDec = obj.searchInstanceVariable(identSymbol.getSymbolString());
			if ( instanceVariableDec == null ) { 
				env.error(this.selfSymbol, "Instance variable " + identSymbol.getSymbolString() + " was not found", true, true);
				type = Type.Dyn;
			}
			else 
				type = instanceVariableDec.getType();
		}
		else
			env.error(selfSymbol, "Internal error: '" + NameServer.selfNameInnerPrototypes + "' has no type", true, true);
		
		super.calcInternalTypes(env);
		
	}
	
	public InstanceVariableDec getInstanceVariableDec() {
		return instanceVariableDec;
	}

	private InstanceVariableDec instanceVariableDec;
	private Symbol selfSymbol;
	private Symbol identSymbol;
}
