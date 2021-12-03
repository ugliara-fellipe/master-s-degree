package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.Token;
import meta.ExprReceiverKind;
import meta.MetaInfoServer;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

public class ExprMessageSendUnaryChainToSuper extends ExprMessageSendUnaryChain {

	public ExprMessageSendUnaryChainToSuper(Symbol superSymbol, Symbol nextSymbol) {
		super(nextSymbol);
		this.superSymbol = superSymbol;

	}

	public ExprMessageSendUnaryChainToSuper(Symbol superSymbol) {
		super();
		this.superSymbol = superSymbol;

	}
	
	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
	

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print("super ");
		if ( backquote )
			pw.print("`");
		
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(cyanEnv.formalGenericParamToRealParam(unarySymbol.getSymbolString()));
		}
		else {
			pw.print(unarySymbol.getSymbolString());
		}
		
		
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		
		if ( backquote ) {
			env.error(this.getFirstSymbol(), "code generation for backquote with 'super' is illegal", true, true);
		}
		String tmp;
		
		String javaCallToSuper = NameServer.getJavaNameOfSelector(unarySymbol.getSymbolString()); 
		if ( env.getCurrentObjectDec().getOuterObject() != null ) {
			javaCallToSuper = NameServer.getNamePrivateMethodForSuperclassMethod(javaCallToSuper);
			tmp = NameServer.nextJavaLocalVariableName();
			pw.printlnIdent(type.getJavaName() + " " + tmp + " = " +  
			    javaCallToSuper + "()");
		}
		else if ( env.getIsInsideInitMethod() && unarySymbol.getSymbolString().equals("init") ) {
			tmp = "";
			pw.printlnIdent("super();");
		}
		else {
			javaCallToSuper = "super." + javaCallToSuper;
			tmp = NameServer.nextJavaLocalVariableName();
			pw.printlnIdent(type.getJavaName() + " " + tmp + " = " +  
			    javaCallToSuper + "();");
		}
		return tmp;
	}


	/*
	@Override 
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		
		if ( backquote ) {
			env.error(this.getFirstSymbol(), "code generation for backquote with 'super' has not been implemented", true);
		}		
		
		if ( env.getCurrentObjectDec().getOuterObject() != null ) {
			pw.printIdent("__super_");
		}
		pw.print(NameServer.getJavaNameOfSelector(unarySymbol.getSymbolString()) + "()");
	}
	*/
	
	@Override
	public void calcInternalTypes(Env env) {
		String methodName;
		MethodSignature methodSignature;
		ArrayList<MethodSignature> methodSignatureList = null;
		
		Type receiverType;
		
		Token tokenFirstSelector = unarySymbol.token;
		
		
		
		ObjectDec currentObj = env.getCurrentObjectDec();
		ObjectDec superObjectDec;
		receiverType = superObjectDec = currentObj.getSuperobject();
		if ( receiverType == null ) {
			env.error(true, getFirstSymbol(),
					"Prototype " + env.getCurrentProgramUnit().getName() + " does not have a super-prototype",
					env.getCurrentProgramUnit().getName(), ErrorKind.use_of_super_without_a_super_prototype);				
			return ;
		}
		methodName = unarySymbol.getSymbolString();

		
		String currentMethodName = env.getCurrentMethod().getNameWithoutParamNumber();
		if ( currentMethodName.equals("init") || currentMethodName.equals("init:") ) {
			/**
			 * inside an init or init: method it is illegal to access 'self' using 'super'. 
			 * The only message send allowed is 'super init' or 'super init:' as the first statement.
			 */
			if ( ! methodName.equals("init") && ! methodName.equals("init:") ) {
					env.error(this.getFirstSymbol(),  "Message send to 'super' inside an 'init' or 'init:' method and the method to be called "
							+ " is not 'init' or 'init:'");
			}
		}
		if (  currentMethodName.equals("initOnce") ) {
			/**
			 * inside an initOnce method it is illegal to access 'self' using 'super'. 
			 */
			env.error(this.getFirstSymbol(),  "Message send to 'super' inside an 'initOnce' method");
		}
		

		// if `  was used, there is no search for the method at compile-time.
		if ( backquote ) {
			env.error(this.getFirstSymbol(), "backquote (`) with 'super' is illegal", true, true);
			return;
		}
		else if ( tokenFirstSelector == Token.INTER_DOT_ID || tokenFirstSelector == Token.INTER_ID ) {
			/*
			INTER_ID_COLON("~InterIdColon"),          // ?name:
			INTER_ID("~InterId"),                     // ?name
			INTER_DOT_ID_COLON("~InterDotIdColon"),   // ?.name:
			INTER_DOT_ID("~InterDotId"),              // ?.name
		   */
			env.error(this.getFirstSymbol(), "Dynamic message send with message starting with '?' is illegal when the 'receiver' is 'super'", true, true);
			return ;
			
		}
		else {
			
			methodSignatureList = receiverType.searchMethodProtectedPublicSuperProtectedPublic(methodName, env); 
			if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {  
				env.error(true, getFirstSymbol(), "Method " + methodName + " was not found in prototype " + receiverType.getName() +
						        " or its super-prototypes",
						methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);				
			
			}
			else {
				if ( methodName.equals("new") ) {
					env.error(true, unarySymbol, "Message '" + methodName + "'  can only be sent to prototypes",
							methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
					return ;
				}
				if ( methodName.equals("init") ) {
					if ( methodSignatureList.get(0).getMethod().getDeclaringObject() != superObjectDec ) {
						env.error(true, unarySymbol, "'init' and 'init:' methods can only be called by the direct sub-prototypes. "
										+ "There is at least one more prototype, in the hierarchy, between '" + currentObj.getName() 
										+ "' and '" + superObjectDec.getName() + "'",
								methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
						return ;
					}
					MethodSignature ms = methodSignatureList.get(0);
					methodSignatureList.clear();
					methodSignatureList.add(ms);  // only the first one, of the super-prototype, counts.
					
					if ( env.getCurrentMethod() != null ) {
						String initName = env.getCurrentMethod().getNameWithoutParamNumber();
						if ( !initName.equals("init") && !initName.equals("init:") ) {
							env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods");
						}
						if ( env.getFunctionStack().size() > 0 ) {
							env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions");
						}
					}
					else {
						env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent outside a method 'init' or 'init:'");
					}
					if ( env.peekCode() != this ) {
						env.error(this.getFirstSymbol(),  "Calls 'super init' cannot be inside another expression. That is, the return value should not be used for anything");
					}
					if ( ! env.getFirstMethodStatement() ) {
						env.error(this.getFirstSymbol(),  "Calls 'super init' should be the first statement of an 'init' or 'init:' method");
					}
					
				}
				methodSignature = methodSignatureList.get(0);
				
				methodSignature.calcInterfaceTypes(env);
				if ( methodSignature.getMethod().isAbstract() )
					env.error(this.getFirstSymbol(), "'super' used to call an abstract method");
				receiverType = methodSignature.getReturnType(env);
			}
			
			MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, currentObj, null, ExprReceiverKind.SUPER_R, env, unarySymbol);
			type = receiverType;
		}
		super.calcInternalTypes(env);
		
	}
	
	
	@Override
	public Symbol getFirstSymbol() {
		return superSymbol;
	}
	
	public Expr getReceiver() {
		return null;
	}
	

	private Symbol superSymbol;
	
	 
}
