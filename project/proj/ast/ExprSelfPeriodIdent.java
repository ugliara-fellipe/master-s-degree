/**
 *
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
 *   represents the access to an instance variable of self.
 *   
 * @author José
 *
 */
public class ExprSelfPeriodIdent extends Expr implements LeftHandSideAssignment {

	/**
	 *
	 */
	public ExprSelfPeriodIdent(Symbol selfSymbol, Symbol identSymbol) {
		this.selfSymbol = selfSymbol;
		this.identSymbol = identSymbol;
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


	public void setIdentSymbol(Symbol identSymbol) {
		this.identSymbol = identSymbol;
	}


	public Symbol getIdentSymbol() {
		return identSymbol;
	}


	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		


		
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print("self." + cyanEnv.formalGenericParamToRealParam(identSymbol.getSymbolString()));
		}
		else {
			String name = identSymbol.getSymbolString();
			String iv = "." + name; // NameServer.getJavaNameIdentifier(name);
			if ( cyanEnv.getCreatingInnerPrototypesInsideEval() ) {
				/*
				 * in this case, 'self' is being used inside a prototype created from a function or a outer prototype method. 
				 */
				pw.print( NameServer.selfNameInnerPrototypes + iv );
			}
			else if ( cyanEnv.getCreatingContextObject() ) { 
				/*
				 * in this case, 'self' is being used inside a prototype created from a context function  
				 */
				//pw.print(NameServer.selfNameContextObject + " ");
				pw.print( NameServer.selfNameContextObject + iv );
			}
			else
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
		pw.print("this." + NameServer.getJavaName(identSymbol.getSymbolString()));
		if ( instanceVariableDec.getRefType() )
			pw.print(".elem");
	}
	

	@Override
	public Symbol getFirstSymbol() {
		return selfSymbol;
	}

	public InstanceVariableDec getInstanceVariableDec() {
		return instanceVariableDec;
	}

	@Override 
	public void calcInternalTypes(Env env, boolean leftHandSideAssignment) {
		instanceVariableDec = env.getCurrentObjectDec().searchInstanceVariable(identSymbol.getSymbolString());
		if ( instanceVariableDec == null ) {
			type = Type.Dyn;
			env.error(this.selfSymbol, "Instance variable '" + identSymbol.getSymbolString() + "' was not found", true, true);
		}
		
		type = instanceVariableDec.getType();
		if ( ! instanceVariableDec.isShared() && 
				 ! env.getCurrentMethod().getAllowAccessToInstanceVariables() ) {
				/*
				 * access to instance variables is not allowed
				 */
				env.error(this.getFirstSymbol(), "Instance variables are not allowed in this method. Probable cause: "
						+ "metaobject 'prototypeCallOnly' is attached to it"
						);
			}
		if ( ! leftHandSideAssignment && ! instanceVariableDec.isShared()) {
			String currentMethodName = env.getCurrentMethod().getNameWithoutParamNumber();
			if ( currentMethodName.equals("init") || currentMethodName.equals("init:") ) {
				env.error(this.getFirstSymbol(),  "Access to an instance variable in an expression inside an 'init' or 'init:' method. This is illegal because the "
						+ "Cyan compiler is not able yet to discover if the instance variable have been initialized or not");
			}
		}
	}
	
	
	@Override
	public void calcInternalTypes(Env env) {
		
		this.calcInternalTypes(env, false);
		super.calcInternalTypes(env);
	}
	

	private InstanceVariableDec instanceVariableDec;
	private Symbol selfSymbol;
	private Symbol identSymbol;
}
