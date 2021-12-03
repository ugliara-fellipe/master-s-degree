/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.SymbolOperator;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/** Represents an expression preceded by an unary operator
 * @author José
 *
 */
public class ExprUnary extends Expr {

	public ExprUnary(SymbolOperator symbolOperator, Expr expr) {
		this.symbolOperator = symbolOperator;
		this.expr = expr;
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		expr.accept(visitor);
		visitor.visit(this);
	}
	
	
	
	@Override
	public boolean mayBeStatement() {
		return false;
	}
	
	
	@Override
	public boolean isNRE(Env env) {
		return expr instanceof ExprLiteral;
	}	
	
	@Override
	public boolean isNREForInitOnce(Env env) {
		return expr instanceof ExprLiteral;
		//return expr.isNREForInitOnce(env);
	}	


	public void setSymbolOperator(SymbolOperator symbolOperator) {
		this.symbolOperator = symbolOperator;
	}
	public SymbolOperator getSymbolOperator() {
		return symbolOperator;
	}

	public void setExpr(Expr expr) {
		this.expr = expr;
	}

	public Expr getExpr() {
		return expr;
	}

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print(symbolOperator.getSymbolString());
		expr.genCyan(pw, false, cyanEnv, genFunctions);
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		String tmpVarName = NameServer.nextJavaLocalVariableName();
		String exprVarName = expr.genJavaExpr(pw, env);
		
		if ( expr.getType(env) != Type.Dyn ) {
			pw.printlnIdent(type.getJavaName() + " " + tmpVarName + " = " + exprVarName + "." +
					NameServer.getJavaNameOfSelector(this.symbolOperator.getSymbolString()) + "();");
		}
		else {
			tmpVarName = Statement.genJavaDynamicUnaryMessageSend(pw, exprVarName, 
					NameServer.getJavaNameOfSelector(this.symbolOperator.getSymbolString()),
					env, expr.getFirstSymbol().getLineNumber());
		}
		
		return tmpVarName;
	}

	
	@Override
	public Symbol getFirstSymbol() {
		return symbolOperator;
	}


	@Override
	public void calcInternalTypes(Env env) {
		
		
		expr.calcInternalTypes(env);
		Type receiverType = expr.getType(env);
		String methodName = symbolOperator.getSymbolString();
		
		if ( receiverType == Type.Dyn ) {
			type = Type.Dyn;
		}
		else {
			ArrayList<MethodSignature> methodSignatureList;
			if ( expr instanceof ExprSelf ) {
				methodSignatureList = receiverType.searchMethodPrivateProtectedPublicSuperProtectedPublic(methodName, env);
			}
			else {
				methodSignatureList = receiverType.searchMethodPublicSuperPublic(methodName, env);
			}
			if ( methodSignatureList == null || methodSignatureList.size() == 0 ) { 
				env.error(getFirstSymbol(), "Method " + methodName + " was not found in " + receiverType.getName(), true, true);
				type = Type.Dyn;
			}
			else
				type = methodSignatureList.get(0).getReturnType(env);
		}
		super.calcInternalTypes(env);
		
	}

	public String javaNameAsType(Env env) {
		return "Error in ExprUnary::javaNameAsType";
	}

	
	protected SymbolOperator symbolOperator;
	protected Expr expr;

}
