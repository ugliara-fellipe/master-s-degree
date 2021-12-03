/**
 *
 */
package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.Token;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/** Represents an indexed expression such as
 *    anArray[i]
 *    (list getArray)[i]
 *
 * @author José
 *
 */
public class ExprIndexed extends Expr {

	public ExprIndexed(Expr indexedExpr, Expr indexOfExpr, Symbol firstIndexOperator) {
		this.indexedExpr = indexedExpr;
		this.indexOfExpr = indexOfExpr;
		this.firstIndexOperator = firstIndexOperator;
		leftHandSideAssignment = false;
		this.declaredType_at_Parameter = null;
		this.declaredType_put_Parameter = null;
			
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		this.indexedExpr.accept(visitor);
		this.indexOfExpr.accept(visitor);
		visitor.visit(this);
	}	

	@Override
	public boolean mayBeStatement() {
		return false;
	}
	
	
	
	public void setIndexedExpr(Expr indexedExpr) {
		this.indexedExpr = indexedExpr;
	}
	public Expr getIndexedExpr() {
		return indexedExpr;
	}
	public void setIndexOfExpr(Expr indexOfExpr) {
		this.indexOfExpr = indexOfExpr;
	}
	public Expr getIndexOfExpr() {
		return indexOfExpr;
	}


	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		indexedExpr.genCyan(pw, false, cyanEnv, genFunctions);
		pw.print( this.firstIndexOperator.token == Token.INTER_LEFTSB ? "?[" : "[" );
		indexOfExpr.genCyan(pw, false, cyanEnv, genFunctions);
		pw.print( this.firstIndexOperator.token == Token.INTER_LEFTSB ? "]?" : "]" );
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {

		String exprTmpVar = NameServer.nextJavaLocalVariableName();

		pw.printlnIdent(type.getJavaName() + " " + exprTmpVar + ";");
	    
    	String indexedExprTmp = indexedExpr.genJavaExpr(pw, env);
    	
    	boolean nilSafeIndexing = firstIndexOperator.token == Token.INTER_LEFTSB;
    	if ( nilSafeIndexing ) {
    		pw.printlnIdent("if ( " + indexedExprTmp + " != " + NameServer.NilInJava + ".prototype" + " && " + 
    	        indexedExprTmp + " != null ) {");
    		pw.add();
    	}
    	
    	String indexOfTmp = indexOfExpr.genJavaExpr(pw, env);
    	String s = indexOfTmp;
    	if ( indexOfExpr.getType() == Type.Dyn ) {
    		String javaNameIndex = this.declaredType_at_Parameter.getJavaName();
			pw.printlnIdent("if ( !(" + indexOfTmp + " instanceof " + javaNameIndex + " ) ) ");
			pw.printlnIdent("throw new ExceptionContainer__("
					+ env.javaCodeForCastException(indexOfExpr, Type.Dyn) + " );");
			
			
			s = "(" + javaNameIndex + " )" + s;
    	}
    	if ( indexedExpr.getType() != Type.Dyn ) {
    		
            if ( this.declaredType_at_Parameter == Type.Any && indexOfExpr.getType() instanceof InterfaceDec ) {
            	s = " (" + NameServer.AnyInJava + " ) " + s;
            }
            
    		
        	pw.printlnIdent(exprTmpVar + " = " + indexedExprTmp + "." + NameServer.javaNameAtMethod + "(" + s + ");");
    	}
    	else {
    		// type of indexed expr is Dyn
    		exprTmpVar = Statement.genJavaDynamicSelectorMessageSend(pw, indexedExprTmp, NameServer.javaNameAtMethod, 
    				s, 1, env, indexedExpr.getFirstSymbol().getLineNumber()) ;

    	}
    	if ( nilSafeIndexing ) {
    		pw.sub();
    		pw.printlnIdent("}");
    		pw.printlnIdent("else");
    		pw.add();
    		pw.printlnIdent(exprTmpVar + " = " + NameServer.NilInJava + ".prototype" + ";");
    		pw.sub();    	
    	}

        /*
        String indexedTmpVar = indexedExpr.genJavaExpr(pw, env);
		String indexTmpVar   = indexOfExpr.genJavaExpr(pw, env);
		if ( this.firstIndexOperator.token == Token.INTER_LEFTSB ) {
			pw.printlnIdent("if ( " + indexTmpVar + " != " + 
		    NameServer.NilInJava + "  && " + indexTmpVar + " != null ) " );
			pw.add();
		}
	    pw.printlnIdent(exprTmpVar + " = " + indexedTmpVar + "[" + indexTmpVar + "] ");
		if ( this.firstIndexOperator.token == Token.INTER_LEFTSB ) {
			pw.sub();
			pw.printlnIdent("else");
			pw.add();
			pw.printlnIdent(exprTmpVar + " = " + NameServer.NilInJava + ";");
			pw.sub();
		}
		*/
		return exprTmpVar;
	}


	@Override
	public Symbol getFirstSymbol() {
		return indexedExpr.getFirstSymbol();
	}

	@Override
	public void calcInternalTypes(Env env) {
		
		
		indexedExpr.calcInternalTypes(env);
		indexOfExpr.calcInternalTypes(env);
		Type indexedExprType = indexedExpr.getType(env);
		
		/*
		 * if the type of 'v' is Dyn, 'v[0] = 1' and 'x = v[0]' are already correctly typed. 
		 */
		if ( indexedExprType == Type.Dyn ) { 
			type = Type.Dyn;
			this.declaredType_at_Parameter = Type.Dyn;
			this.declaredType_put_Parameter = Type.Dyn;
		} 
		else {
			String methodName = leftHandSideAssignment ? "at:1 put:1" : "at:1";
			ArrayList<MethodSignature> methodSignatureList = indexedExprType.searchMethodPublicSuperPublic(
					methodName, env);
			if ( methodSignatureList == null ) {
				if ( env.getCurrentMethod() != null ) 
					env.error(true, indexedExpr.getFirstSymbol(),
						"Expression cannot be indexed. Method '" + methodName + "' was not found", "", ErrorKind.indexing_method_was_not_found_inside_method);
				else
					env.error(true, indexedExpr.getFirstSymbol(),
							"Expression cannot be indexed. Method '" + methodName + "' was not found", "", ErrorKind.indexing_method_was_not_found_outside_method);
				//env.error(indexedExpr.getFirstSymbol(), "Expression cannot be indexed. Method '" + methodName + "' was not found");
			}
			else {
				boolean foundMethod = false;
				for ( MethodSignature ms : methodSignatureList ) {
					if ( ms instanceof MethodSignatureWithSelectors ) {
						MethodSignatureWithSelectors nonGrammarMethodSignature = (MethodSignatureWithSelectors ) ms;
						if ( nonGrammarMethodSignature.isIndexingMethod() ) {
							
							if ( leftHandSideAssignment ) {
								if ( nonGrammarMethodSignature.getSelectorArray().size() == 2 ) {
									SelectorWithParameters selAt = nonGrammarMethodSignature.getSelectorArray().get(0);
									SelectorWithParameters selPut = nonGrammarMethodSignature.getSelectorArray().get(1);
									if ( selAt.getParameterList().size() == 1 && selPut.getParameterList().size() == 1 ) {
										Type parameterType = selAt.getParameterList().get(0).getType();
										if ( parameterType.isSupertypeOf(indexOfExpr.getType(env), env) ) {
											foundMethod = true;
											  // in a method at: put:, the type of this indexing expression
											// should be the type of put because in an assignment
											//  a[i] = 0 the compiler will check whether 0 is a subtype of
											// the type of parameter put 
											type = selPut.getParameterList().get(0).getType();
											this.declaredType_at_Parameter = nonGrammarMethodSignature.getParameterList().get(0).getType(env);
											this.declaredType_put_Parameter = nonGrammarMethodSignature.getParameterList().get(1).getType(env);
										}
									}
								}
								
							}
							else {
								if ( nonGrammarMethodSignature.getSelectorArray().size() == 1 ) {
									SelectorWithParameters sel = nonGrammarMethodSignature.getSelectorArray().get(0);
									if ( sel.getParameterList().size() == 1 ) {
										Type parameterType = sel.getParameterList().get(0).getType();
										if ( parameterType.isSupertypeOf(indexOfExpr.getType(env), env) ) {
											foundMethod = true;
											type = methodSignatureList.get(0).getReturnType(env);
											this.declaredType_at_Parameter = nonGrammarMethodSignature.getParameterList().get(0).getType(env);
											this.declaredType_put_Parameter = null;
										}
									}
								}
								
							}
						}
					}
				}
				if ( ! foundMethod ) {
					if ( env.getCurrentMethod() != null ) 
						env.error(true, indexedExpr.getFirstSymbol(),
								"Expression cannot be indexed by an expression of this type", "", ErrorKind.expression_cannot_be_indexed_by_this_index_inside_method);

					else
						env.error(true, indexedExpr.getFirstSymbol(),
								"Expression cannot be indexed by an expression of this type", "", ErrorKind.expression_cannot_be_indexed_by_this_index_outside_method);

			
				}
			}		
			
		}
		super.calcInternalTypes(env);

	}

	public boolean isLeftHandSideAssignment() {
		return leftHandSideAssignment;
	}

	public void setLeftHandSideAssignment(boolean leftHandSideAssignment) {
		this.leftHandSideAssignment = leftHandSideAssignment;
	}

	public Symbol getFirstIndexOperator() {
		return firstIndexOperator;
	}

	/**
	 * the expression that is being indexed such as "anArray" or "(list getArray)"
	 * in 
	 *    anArray[i]
	 * 	  (list getArray)[i]
	 * 
	 */
	private Expr indexedExpr;
	/**
	 * the expression that is the index such as "i" in 
	 *    anArray[i]
	 * 	  (list getArray)[i]
	 */
	private Expr indexOfExpr;
	/**
	 * It is either LEFTSB if the indexed expression is of the kind "v[i]" (regular indexing) or  
	 * INTER_LEFTSB if the expression is "v?[i]?
	 */
	private Symbol firstIndexOperator;
	

	/**
	 * true if this indexed expression appears on the left-hand side of an assignment as in
	 *       v[i] = 0;
	 * false if it appears in the right-hand side as in
	 *       n = v[i];
	 * In the first case, method at: put: is used:   
	 *       v at: i put: 0
	 * In the second case, method at: is used:
	 *       n = v at: i    
	 */
	private boolean leftHandSideAssignment;

	/**
	 * The method to be called is "at: T"  or "at: R put: S". This instance variable holds the declared type of the at: parameter
	 * that should be used. It is either T if leftHandSideAssignment is false or R if leftHandSideAssignment is true.
	   @return
	 */
	private Type declaredType_at_Parameter;

	/**
	 * The method to be called is "at: T"  or "at: R put: S". This instance variable holds the declared type of the put: parameter
	 * that should be used. It is null if leftHandSideAssignment is false or S if leftHandSideAssignment is true.
	   @return
	 */
	private Type declaredType_put_Parameter;


	public Type getDeclaredType_at_Parameter() {
		return declaredType_at_Parameter;
	}

	public void setDeclaredType_at_Parameter(Type declaredType_at_Parameter) {
		this.declaredType_at_Parameter = declaredType_at_Parameter;
	}

	public Type getDeclaredType_put_Parameter() {
		return declaredType_put_Parameter;
	}

	public void setDeclaredType_put_Parameter(Type declaredType_put_Parameter) {
		this.declaredType_put_Parameter = declaredType_put_Parameter;
	}

}
