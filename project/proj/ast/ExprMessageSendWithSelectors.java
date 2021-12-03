/**
 * 
 */
package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.Token;
import saci.Env;
import saci.NameServer;


/**
 * @author jose
 *
 */
abstract public class ExprMessageSendWithSelectors extends ExprMessageSend {

	public ExprMessageSendWithSelectors(MessageWithSelectors message, Symbol nextSymbol) {
		super(nextSymbol);
		this.message = message;
		this.doNotReturn = false;
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		this.message.accept(visitor);
	}
		
	
	@Override
	public Symbol getFirstSymbol() {
		return message.getSelectorParameterList().get(0).getSelector();
	}

	public boolean getBackquote() {
		return message.getBackquote();
	}	
	/**
	 * returns the method found  if in methodSignatureList there is a method that can accept the selectors and 
	 * parameters described in this message send (the receiver of this message)
	   @param methodSignatureList
	   @param env
	   @return
	 */
	public MethodSignature checkMessageSend(ArrayList<MethodSignature> methodSignatureList, Env env) {
		
		if ( message instanceof MessageBinaryOperator ) {
			SelectorWithRealParameters selectorWithRealParam = message.getSelectorParameterList().get(0);
			Type typeRealParameter = selectorWithRealParam.getExprList().get(0).getType(env);
			for ( MethodSignature ms : methodSignatureList ) {
				if ( ms instanceof MethodSignatureOperator )  {
					MethodSignatureOperator mso = (MethodSignatureOperator ) ms;
					if (  mso.getOptionalParameter() == null ) {
						env.error(getFirstSymbol(), "Method '" + this.message.getMethodName() + "' does not take parameters");
					}
					else if ( mso.getOptionalParameter().getType().isSupertypeOf(typeRealParameter, env) ) {
						return mso;
					}
					else {
						env.error(getFirstSymbol(), "The type of the real argument, '"+  typeRealParameter.getFullName()
								+ "', of expression '" + selectorWithRealParam.getExprList().get(0).asString() +  "' is not sub-prototype of the type of the formal parameter, '" + mso.getOptionalParameter().getType().getFullName() + "' in message send with selector '"
								+ mso.getSymbolOperator().getSymbolString() + "'");
					}
				}
			}
		}
		else  {
			ArrayList<SelectorWithRealParameters> selectorWithRealParamList = message.getSelectorParameterList();
			/**
			 * selectorWithRealParamList has the seletors and parameters that were used in the message as
			 * 'key: "one"  value: 1' in the message send
			 *        hash key: "one"  value: 1
			 * methodSignatureList contains a list of method signatures of the receiver. Each
			 * one is from a method whose name is "key:value:". The code below checks whether
			 * there is one method in methodSignatureList that accepts the real parameters.
			 */
			for ( MethodSignature ms : methodSignatureList ) {
				if ( ! (ms instanceof MethodSignatureWithSelectors) ) {
					env.error(message.getFirstSymbol(), "Internal error in ExprMessageSendWithSelectorsToExpr::genJavaExpr: a non-grammar method", true, true);
				}
				else {
					int selectorIndex = 0;
					boolean typeErrorInParameterPassing = false;
					MethodSignatureWithSelectors gm = (MethodSignatureWithSelectors ) ms;
					ArrayList<SelectorWithParameters> selWithFormalParamList = gm.getSelectorArray();
					for ( SelectorWithParameters selWithFormalParam : selWithFormalParamList ) {
						if ( selWithFormalParam.getParameterList().size() != selectorWithRealParamList.get(selectorIndex).getExprList().size() )
							typeErrorInParameterPassing = true;
						else {
							SelectorWithRealParameters selectorWithRealParam = selectorWithRealParamList.get(selectorIndex);
							int parameterIndex = 0;
							for (ParameterDec paramDec : selWithFormalParam.getParameterList() ) {
								Expr realParam = selectorWithRealParam.getExprList().get(parameterIndex);
								if ( ! paramDec.getType(env).isSupertypeOf(realParam.getType(env), env)) {
									typeErrorInParameterPassing = true;
									break;
								}
								
								
								if ( paramDec.getVariableKind() == VariableKind.LOCAL_VARIABLE_REF ) {
									if ( realParam instanceof ExprIdentStar ) {
										ExprIdentStar e = (ExprIdentStar ) realParam;
										 
										VariableDecInterface varDec = e.getVarDeclaration();
										
										  // env.searchVariable( ((ExprIdentStar ) realParam).getName());
										if ( varDec == null || 
												(! (varDec instanceof StatementLocalVariableDec) && 
												 ! (varDec instanceof InstanceVariableDec) &&
												 ! (varDec instanceof ParameterDec)
												 ) )
											env.error(realParam.getFirstSymbol(), "A local variable or instance variable was expected because the formal parameter was declared with '&'", true, true);
										else {
											if ( varDec instanceof StatementLocalVariableDec ) {
												if ( ! varDec.isReadonly() ) {
													varDec.setRefType(true);
												}
											}
											else if ( varDec instanceof InstanceVariableDec )
												varDec.setRefType(true);
											else {
												if ( ! ((ParameterDec ) varDec).getRefType() ) {
													env.error(realParam.getFirstSymbol(), "A parameter with reference type, declared with '&', was expected because the formal parameter was declared with '&'", true, true);
												}
											}
											realParam.calcInternalTypes(env);
										}
								    }
									else
										env.error(realParam.getFirstSymbol(), "A local variable or instance variable was expected because the formal parameter was declared with '&'", true, true);
								    break;
								}
								
								
								
								
								++parameterIndex;
							}
							++selectorIndex;
							if ( typeErrorInParameterPassing )
								break;
							
						}
					}
					if ( ! typeErrorInParameterPassing ) {
						return ms;
					}
				}
					
			}
		}
		return null;
	}
	
	
	/**
	 * generates Java code to test whether the message send had a 'return' statement for a method that is in
	 * the stack of called methods. That is, if some function passed as parameter executed a non-local 'return'
	 * statement. This code is only necessary if the parameters are of types Any, Dyn, or Function and its 
	 * subtypes. Nowadays it is being generated for all method calls, what is a shame. 
	   @param pw
	   @param env
	 */
	public void genJavaTestForReturn(PWInterface pw, Env env) {
		
		
		
	    pw.printlnIdent("if ( " + NameServer.systemJavaName + ".numMethodToReturn >= 0 ) {");
	    pw.add();
	    pw.printlnIdent("if ( " + NameServer.systemJavaName + 
	          ".numMethodToReturn == ___numThisMethod ) {");
	    pw.add();
	    pw.printlnIdent(NameServer.systemJavaName + ".currentNumMethod = ___numThisMethod;");
	    pw.printlnIdent(NameServer.systemJavaName + ".numMethodToReturn = -1;");
	    pw.printlnIdent("return (" + env.getCurrentMethod().getMethodSignature().getReturnType(env).getJavaName() 
	    		+ " ) " + NameServer.systemJavaName +
	             ".returnedValue;");
	    pw.sub();
	    pw.printlnIdent("}");
	    pw.printlnIdent("else");
	    pw.add();
	    pw.printlnIdent("return null;");
	    pw.sub();
	    pw.sub();
	    pw.printlnIdent("}");
		
	}
	
	/**
	   @param env
	   @param tokenFirstSelector
	 */
	protected void calcInternalTypesWithBackquote(Env env, Token tokenFirstSelector) {
		if ( tokenFirstSelector != Token.IDENTCOLON ) {
			env.error(getFirstSymbol(), "The backquote ` should not be followed by '?' or '?.'", true, true);
		}
		
		type = Type.Dyn;
		quotedVariableList = new ArrayList<VariableDecInterface>();
		   // something like   f1 `first: p1 `second: p2, p3  in which first and second should be variables
		   // of type String or CySymbol
		for ( SelectorWithRealParameters sel : message.getSelectorParameterList() ) {
			String varName = sel.getSelectorNameWithoutSpecialChars();
			
			VariableDecInterface varDec = env.searchVariable(varName);
			if ( varDec == null ) {
				if ( env.getEnclosingObjectDec() == null ) {
					/*
					 * inside a regular prototype that is NOT inside another prototype
					 */
					varDec = env.searchVariable(varName);
				}
				else {
					/*
					 * inside an inner prototype
					 */
					if ( NameServer.isMethodNameEval(env.getCurrentMethod().getNameWithoutParamNumber()) ) {
						/*
						 * inside an 'eval' or 'eval:eval: ...' method of an inner prototype 
						 */
	
						varDec = env.searchVariableInEvalOfInnerPrototypes(varName);
					}
					else {
						/*
						 * inside a method of an inner prototype that is not 'eval', 'eval:eval: ...'
						 */
						
	
						varDec = env.searchVariableIn_NOT_EvalOfInnerPrototypes(varName);
					}
				}
			}
			if ( varDec == null ) 
				env.error(true, sel.getSelector(),
						"Variable " + varName + " was not declared",
						varName, ErrorKind.variable_was_not_declared);
			else {
				if ( ! Type.String.isSupertypeOf(varDec.getType(), env)  )
					env.error(true, sel.getSelector(),
							"Variable " + varName + 
									" should be of type String. Only String variables can follow the backquote ` character",
							varName, ErrorKind.backquote_not_followed_by_a_string_variable);
					
				quotedVariableList.add(varDec);
			}
			if ( sel.getExprList() != null ) {
				for ( Expr e : sel.getExprList() )
					e.calcInternalTypes(env);
			}
		}
		
		return ;
	}


	@Override
	public boolean alwaysReturn() {
		return doNotReturn;
	}

	@Override
	public boolean statementDoReturn() {
		return doNotReturn;
	}

	public MessageWithSelectors getMessage() {
		return message;
	}
	
	protected MessageWithSelectors message;
	

	/**
	 * if this is a message send with backquote, this is the list of variables in the message send. That is,
	 * if the message is
	 *           f1 `first: 0  `second: "Hi"
	 * then quotedVariableList contains references to variables first and second.
	 */
	protected ArrayList<VariableDecInterface> quotedVariableList;

	/**
	 * true if this message send do not return. It is true if it is a message send
	 * to 'throw:' or other method that do not return
	 */
	protected boolean doNotReturn;


	/**
	 * the method signature of the method found in a search for an adequate method for this message.
	 */
	protected MethodSignature methodSignatureForMessage;
}
