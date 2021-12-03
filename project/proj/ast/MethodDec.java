/**
 *
 */

package ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import error.CompileErrorException;
import error.ErrorKind;
import lexer.Symbol;
import lexer.Token;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionAssignment_cge;
import meta.MetaInfoServer;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

/**
 * Represents the declaration of a method
 *
 * @author José
 *
 */
public class MethodDec extends SlotDec {


	public MethodDec(ObjectDec currentObject, Token visibility, boolean isFinal,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedSlotMetaobjectAnnotationList,
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedSlotMetaobjectAnnotationList, ObjectDec prototype, int methodNumber,
			boolean compilerCreatedMethod) {
		super(visibility, attachedSlotMetaobjectAnnotationList, nonAttachedSlotMetaobjectAnnotationList);
		this.declaringObject = currentObject;
		this.isFinal = isFinal;
		hasOverride = false;
		this.prototype = prototype;
		leftCBsymbol = null;
		rightCBsymbol = null;
		firstSymbolExpr = null;
		lastSymbolExpr = null;
		this.methodNumber = methodNumber;
		this.setCompilerCreatedMethod(compilerCreatedMethod);
		expr = null;
		statementList = null;
		overload = false;
		setAllowAccessToInstanceVariables(true);
		hasJavaCode = false;
		shouldInsertCallToConstructorWithoutParametes = false;
	}


	@Override
	public void accept(ASTVisitor visitor) {
		
		visitor.preVisit(this);
		
		this.methodSignature.accept(visitor);
		if ( statementList != null ) {
			this.statementList.accept(visitor);
		}
		if ( this.expr != null ) {
			expr.accept(visitor);
		}
		visitor.visit(this);
	}
	
	public void setExpr(Expr expr) {
		this.expr = expr;
	}

	public Expr getExpr() {
		return expr;
	}

	public void setStatementList(StatementList statementList) {
		this.statementList = statementList;
	}

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		cyanEnv.atBeginningOfMethodDec(this);
		pw.println("");
		super.genCyan(pw, false, cyanEnv, genFunctions);
		// pw.printlnIdent(" // " + methodSignature.getSingleParameterType());
		pw.printIdent(visibility == null ? NameServer.getVisibilityString(Token.PUBLIC)
				: NameServer.getVisibilityString(visibility));

		if ( isFinal ) pw.print(" final");
		if ( hasOverride ) pw.print(" override");
		if ( isAbstract ) pw.print(" abstract");
		pw.print(" func ");
		methodSignature.genCyan(pw, PWCounter.printInMoreThanOneLine(methodSignature), cyanEnv, genFunctions);
		if ( expr != null ) {
			pw.print(" = ");
			expr.genCyan(pw, false, cyanEnv, genFunctions);
			pw.println(";");
		}
		else {
			if ( statementList != null ) {
				if ( !PWCounter.printInMoreThanOneLine(statementList)
						&& statementList.getStatementList().size() <= 2 ) {
					pw.print(" { ");
					statementList.genCyan(pw, false, cyanEnv, genFunctions);
					pw.printlnIdent("} ");
				}
				else {
					pw.println(" {");
					pw.add();
					statementList.genCyan(pw, true, cyanEnv, genFunctions);
					pw.sub();
					pw.printlnIdent("} ");
				}
			}
		}
		cyanEnv.atEndOfMethodDec();
	}

	@Override
	public void genJava(PWInterface pw, Env env) {

		env.atBeginningOfCurrentMethod(this);
		String name = getNameWithoutParamNumber();

		super.genJava(pw, env);
		pw.println("");
		// pw.printlnIdent(" // " + methodSignature.getSingleParameterType());

		pw.printIdent("");
		/*
		 * if ( name.equals("new") || name.equals("new:") ) pw.print("static ");
		 */
		String strVisibility;

		boolean isInit1 = this.isInitMethod();

		if ( visibility == null || isInit1 ) {
			strVisibility = "public";
			visibility = Token.PUBLIC;
		}
		else
			strVisibility = visibility.toString();
		
		if ( this.overload ) {
			strVisibility = "private";
		}
		pw.print(strVisibility + " ");

		if ( hasOverride && !overload ) pw.print("@Override ");


		if ( isInit1 ) {
			methodSignature.genJavaAsConstructor(pw, env, this.declaringObject.getJavaNameWithoutPackage());
		}
		else {
			methodSignature.genJava(pw, env, this.overload);
		}
		
		Type returnType = this.getMethodSignature().getReturnType(env);

		if ( isAbstract ) {
			pw.println("{");
			pw.add();
			pw.printlnIdent("throw new ExceptionContainer__(new _ExceptionCannotCallAbstractMethod());");
			pw.sub();
			pw.println("}");
		}
		else {
			pw.println(" {");
			pw.add();
			/*
			 * pw.printlnIdent(
			 * "int ___numThisMethod = System.currentNumMethod++;");
			 * pw.printlnIdent(this.getMethodSignature().getReturnType(env).
			 * getJavaName() + " ret__ = "); if (
			 * this.getMethodSignature().getReturnType(env) == Type.Nil )
			 * pw.println("_Nil;"); else pw.println("null;"); pw.printlnIdent(
			 * "ret: do {"); pw.add();
			 * 
			 */
			
			if ( isInit1 && shouldInsertCallToConstructorWithoutParametes ) {
				/*
				 * call super constructor without parameters
				 */
				pw.printlnIdent("super();");
			}
			
			if ( this.declaringObject.outerObject != null && NameServer.isMethodNameEval(name) ) {
				env.setCreatingInnerPrototypesInsideEval(true);
			}
			
			if ( expr != null ) {
				// pw.printlnIdent("--" + NameServer.systemJavaName +
				// ".currentNumMethod;");
				String tmpVar = expr.genJavaExpr(pw, env);
				
				
				/*
				 * A metaobject attached to the type of the formal parameter may demand that the real argument be
				 * changed. The new argument is the return of method  changeRightHandSideTo
				 */

				
				Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, returnType);
				IActionAssignment_cge changeCyanMetaobject = null;
		        ObjectDec prototypeFoundMetaobject = null;
		        if ( cyanMetaobjectPrototype != null ) {
		        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
		        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
		        	
						if ( changeCyanMetaobject != null ) {
							
							try {
								tmpVar = changeCyanMetaobject.cge_changeRightHandSideTo( prototypeFoundMetaobject, 
				   	           			tmpVar, expr.getType(env));
								
							} 
							catch ( error.CompileErrorException e ) {
							}
							catch ( RuntimeException e ) {
								CyanMetaobjectAnnotation annotation = ((CyanMetaobjectWithAt) changeCyanMetaobject).getMetaobjectAnnotation();
								env.thrownException(annotation, annotation.getFirstSymbol(), e);
							}			
							finally {
								env.errorInMetaobject( (meta.CyanMetaobject ) changeCyanMetaobject, this.getFirstSymbol());
							}
		   				}				
		        }
		        
				if ( expr.getType(env) == Type.Dyn && returnType != Type.Dyn ) {
					// first case
					/*
					 * 
					 */
					pw.printlnIdent("if ( " + tmpVar + " instanceof " + returnType.getJavaName() + " ) {");
					pw.add();
					pw.printlnIdent("return (" + returnType.getJavaName() + " ) " + tmpVar + ";");
					pw.sub();
					pw.printlnIdent("}");
					pw.printlnIdent("else {");
					pw.add();
					
					pw.printlnIdent("throw new ExceptionContainer__("
							+ env.javaCodeForCastException(expr, returnType) + " );");
					
					pw.sub();
					pw.printlnIdent("}");
				}
				else {
					if ( returnType == Type.Any && expr.getType() instanceof InterfaceDec ) {
						tmpVar = "(" + NameServer.AnyInJava + " ) " + tmpVar; 
					}
					pw.printlnIdent("return " + tmpVar + ";");
				}
			}
			else {
				int ident = pw.getCurrentIndent();
				int count = 0;
				boolean firstStatementIsSuperInit = false;
				ArrayList<Statement> statList = statementList.getStatementList();
				if ( statList.size() > 0 && isInit1 && env.getStrInitRefVariables() != null ) {
					Statement firstStat = statList.get(0);
					if ( (firstStat instanceof ast.ExprMessageSendUnaryChainToSuper) || (firstStat instanceof ast.ExprMessageSendWithSelectorsToSuper) ) {
						firstStatementIsSuperInit = true;
					}

				}
				/*
				 * insert initialization of instance variables at the start of the method 'init' or 'init:'. There
				 * is no call to super 'init' or 'init:'
				 * These instance variables were initialized in their declarations
				 * /
				*/
				
				String strInitRefVariable = env.getStrInitRefVariables();
				if ( ! firstStatementIsSuperInit && isInit1 && strInitRefVariable != null ) {
					pw.println(strInitRefVariable);
				}
				
				
				for ( Statement s : statList ) {
					env.pushCode(s);

					++counter;
					s.genJava(pw, env);
					if ( s.addSemicolonJavaCode() )
						pw.println(";");
					env.popCode();
					/*
					 * insert initialization of instance variables after call to super in an
					 * 'init' or 'init:' method. 
					 * These instance variables were initialized in their declarations
					 */
					if ( firstStatementIsSuperInit && count == 0 ) {
						pw.println(strInitRefVariable);
					}
					++count;
				}
				
				
				pw.set(ident);
				if ( returnType == Type.Nil && !statementList.alwaysReturn()
						&& !isInit1 ) {
					// method returns Nil but it does not have a return
					// statement.
					if ( ! hasJavaCode ) {
						pw.printlnIdent("return " + NameServer.NilInJava + ".prototype" + ";");
					}
				}
			}
			env.setCreatingInnerPrototypesInsideEval(false);
			// pw.sub();
			// pw.printlnIdent("} while (false);");
			// String fullMethodName = env.getCurrentObjectDec().getFullName() +
			// "::" + this.getName();
			// pw.printlnIdent("if ( ret__ == null ) " +
			// NameServer.systemJavaName + ".error(\"Method " + fullMethodName +
			// " is not returning a value\");");
			// pw.printlnIdent("return ret__;");

			pw.printlnIdent("} ");
			pw.sub();

			/*
			 * pw.println(" {"); pw.add(); pw.printlnIdent(
			 * "int ___numThisMethod = System.currentNumMethod++;");
			 * pw.printlnIdent(this.getMethodSignature().getReturnType(env).
			 * getJavaName() + " ret__ = null;"); pw.printlnIdent("ret: do {");
			 * pw.add(); if ( expr != null ) { pw.printlnIdent("--" +
			 * NameServer.systemJavaName + ".currentNumMethod;"); pw.printIdent(
			 * "return "); expr.genJava(pw, env); pw.println(";"); } else {
			 * statementList.genJava(pw, env); // if the last statement is not a
			 * 'return' statement, decrement System.currentNummMethod
			 * ArrayList<Statement> statList = statementList.getStatementList();
			 * if ( statList.size() > 0 ) { if ( ! (statList.get(statList.size()
			 * - 1) instanceof StatementReturn) ) { pw.printlnIdent("--" +
			 * NameServer.systemJavaName + ".currentNumMethod;"); } }
			 * 
			 * } pw.sub(); pw.printlnIdent("} while (false);");
			 * pw.printlnIdent("--" + NameServer.systemJavaName +
			 * ".currentNumMethod;"); String fullMethodName =
			 * env.getCurrentObjectDec().getFullName() + "::" +
			 * this.getNameWithoutParamNumber(); pw.printlnIdent(
			 * "if ( ret__ == null ) " + NameServer.systemJavaName +
			 * ".error(\"Method " + fullMethodName +
			 * " is not returning a value\");"); pw.printlnIdent(
			 * "} while (false);"); pw.printlnIdent("return ret__;");
			 * 
			 * pw.sub(); pw.printlnIdent("} ");
			 */
		}
		env.atEndMethodDec();

		// }
	}

	public void genJavaOverloadedMethod(PWInterface pw, Env env, ArrayList<MethodDec> overloadMethodList) {

		env.atBeginningOfCurrentMethod(this);

		boolean firstPrecededOverload = false;
		for (MethodDec md : overloadMethodList) {
			if ( md.getPrecededBy_overload() ) {
				firstPrecededOverload = true;
				break;
			}
		}
		pw.printIdent("");
		if ( !firstPrecededOverload ) pw.print("@Override ");
		pw.print("public ");

		ArrayList<ParameterDec> allParam = new ArrayList<>();

		if ( this.methodSignature instanceof MethodSignatureWithSelectors ) {
			MethodSignatureWithSelectors ms = (MethodSignatureWithSelectors) methodSignature;
			ms.genJavaOverloadMethod(pw, env);
			for (SelectorWithParameters s : ms.getSelectorArray()) {
				ArrayList<ParameterDec> parameterList = s.getParameterList();
				for (ParameterDec paramDec : parameterList) {
					allParam.add(paramDec);
				}
			}
		}
		else if ( this.methodSignature instanceof MethodSignatureOperator ) {
			MethodSignatureOperator ms = (MethodSignatureOperator) methodSignature;
			ms.genJavaOverloadMethod(pw, env);
			if ( ms.getParameterList() != null ) {
				allParam.add(ms.getParameterList().get(0));
			}
		}

		pw.print(" ");
		pw.println(" {");

		pw.add();
		int sizeOverloadMethodList = overloadMethodList.size();
		for (MethodDec md : overloadMethodList) {
			/*
	CyBoolean  _ampersand_ampersand(CyBoolean _other) _ampersand_ampersand(Object _other) )  {
        if ( _other instanceof cyan.lang.Boolean  ) { 
            return _ampersand_ampersand(_other);
        } else         if ( _other instanceof cyan.lang.Boolean  ) { 
            return _ampersand_ampersand__Function_LT_GP_CyBoolean_GT(_other);
        }
    } 

			 */
			ArrayList<ParameterDec> methodParamList = md.getMethodSignature().getParameterList();

			pw.printIdent("if ( ");
			int i = 0;
			int size = allParam.size();
			for (ParameterDec param : methodParamList) {
				pw.print(allParam.get(i).getJavaName() + " instanceof "
						+ param.getType().getJavaName() + " ");
				if ( --size > 0 ) pw.print("&& ");
				++i;
			}
			pw.println(" ) { ");
			pw.add();
			String nameOverloadMethod = "";
			MethodSignature methodSignature2 = md.getMethodSignature();
			if ( methodSignature2 instanceof MethodSignatureWithSelectors ) {
				nameOverloadMethod = ((MethodSignatureWithSelectors) methodSignature2).getJavaNameOverloadMethod(); 
			}
			else if ( methodSignature2 instanceof MethodSignatureOperator ) {
				nameOverloadMethod = ((MethodSignatureOperator) methodSignature2).getJavaNameOverloadMethod(); 
			}
			pw.printIdent("return " + nameOverloadMethod + "(");
			int indexMPL = 0;

			size = allParam.size();
			for (ParameterDec param : allParam) {
				pw.print( "(" + methodParamList.get(indexMPL).getType().getJavaName() + ") " + param.getJavaName());
				++indexMPL;
				if ( --size > 0 ) pw.print(", ");
			}
			pw.println(");");
			pw.sub();
			pw.printIdent("}");
			if (  --sizeOverloadMethodList == 0 ) {  
				if ( !firstPrecededOverload ) {
					pw.println();
					pw.printlnIdent("else");
					pw.add();
					pw.printIdent("return super." + this.methodSignature.getJavaName() + "(");
					size = allParam.size();
					indexMPL = 0;
					for (ParameterDec param : allParam) {
						pw.print( /* "(" + methodParamList.get(indexMPL).getType().getJavaName() + ") " + */ param.getJavaName());
						++indexMPL;
						if ( --size > 0 ) pw.print(", ");
					}

					pw.println(");");
					pw.sub();

				}
				else {
					pw.println("");
					pw.printlnIdent("return null;");
				}
			}
			pw.println("");
		}
		pw.sub();

		pw.printlnIdent("} ");

		env.atEndMethodDec();

	}

	public void setMethodSignature(MethodSignature methodSignature) {
		this.methodSignature = methodSignature;
	}

	public MethodSignature getMethodSignature() {
		return methodSignature;
	}

	@Override
	public String getName() {
		return methodSignature.getName();
	}

	public String getNameWithoutParamNumber() {
		return methodSignature.getNameWithoutParamNumber();
	}

	public void setHasOverride(boolean hasOverride) {
		this.hasOverride = hasOverride;
	}

	public boolean getHasOverride() {
		return hasOverride;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	/**
	 * returns true if this method is an "init" or "init:" method. 
	 */
	public boolean isInitMethod() {
		if ( isInit == null ) {
			String s = getNameWithoutParamNumber();
			isInit = s.compareTo("init") == 0 || s.compareTo("init:") == 0;
		}
		return isInit;
	}

	/**
	 * return true if this method has name "initOnce" 
	   @return
	 */
	public boolean isInitOnce() {
		String s = getNameWithoutParamNumber();
		return s.equals("initOnce");
	}
	
	@Override
	public Symbol getFirstSymbol() {
		return firstSymbol;
	}

	/**
	 * return the method name with the parameter types. If the method is
	 * declared as add: (Int i, String s) with: (Char ch, Float other) ->
	 * Boolean the return value is "add: Int, String with: Char, Float" there is
	 * a single space after ':' or ',' and before every selector (except for the
	 * first)
	 * 
	 * @return
	 */
	public String getMethodInterface(Env env) {
		return methodSignature.getFullName(env);
	}

	/**
	 * return the method signature as a string.
	 * 
	 * @return
	 */
	public String getMethodSignatureAsString() {
		return methodSignature.getMethodSignatureWithParametersAsString();
	}
	
	public String getSignatureWithoutReturnType() {
		return this.methodSignature.getSignatureWithoutReturnType();
	}

	/**
	 * calculates the types of everything internal to a method, excluding its
	 * signature
	 * 
	 * @param env
	 */
	@Override
	public void calcInternalTypes(Env env) {

		env.atBeginningOfCurrentMethod(this);
		methodSignature.calcInternalTypes(env);
		try {
			
			if ( expr != null ) {
				if ( this.isInitMethod() || this.isInitOnce() ) {
					env.error(this.getFirstSymbol(), "'init', 'init:', and 'initOnce' methods cannot be set with an expression");
				}
				 
				expr.calcInternalTypes(env);
				if ( ! methodSignature.getReturnType(env).isSupertypeOf(expr.getType(), env) ) {
					env.error(expr.getFirstSymbol(),  "The type of this expression is not subtype of the type of the method return type");
				}
				
			}

			if ( statementList != null ) {
				env.setHasJavaCode(false);

				env.setLexicalLevel(0);
				env.clearStackVariableLevel();

				
				/*
				 * inside an 'init' or 'init:' method, only the first statement 
				 * can be a call 'super init' or 'super init: a, b, c'. The flag
				 * firstMethodStatment is used to check this.
				 * 
				 */
				env.setFirstMethodStatement(true);
				env.setTopLevelStatements(true);
				statementList.calcInternalTypes(env);
				env.setTopLevelStatements(false);
				/*
				 * not a beautiful thing to do ...
				 */
				if ( statementList.getFoundError() ) {
					throw new CompileErrorException();
				}
				this.hasJavaCode = env.getHasJavaCode();
				if ( !hasJavaCode ) {
					/*
					 * if there is no call to metaobject javacode inside the
					 * method then the compiler can deduce whether or not the
					 * method always return
					 */
					if ( !statementList.alwaysReturn() && this.getMethodSignature().getReturnType(env) != Type.Nil ) {
						ArrayList<Statement> statementArray = statementList.getStatementList();
						if ( statementArray.size() == 0 ) {
							env.error(methodSignature.getFirstSymbol(), "Method does not return a value");
						}
						else {
							env.error(
									statementList.getStatementList().get(statementList.getStatementList().size() - 1)
											.getFirstSymbol(),
									"Statement does not return a value. Therefore this method does not return a value");
						}
					}
					/*
					 * search for variables that are used before being
					 * initialized
					 */
					// doLiveAnalysis();
				}
				if ( this.isInitMethod() ) {
					/*
					 * check whether all non-shared instance variables are initialized
					 */
					ObjectDec currentObject = env.getCurrentObjectDec();
					Set<InstanceVariableDec> wasInitializedSet = new HashSet<>();
					for ( Statement s : this.statementList.getStatementList() ) {
						if ( s instanceof ast.StatementAssignmentList ) {
							ArrayList<Expr> exprList = ((StatementAssignmentList) s).getExprList();
							for (int j = 0; j < exprList.size() - 1; ++j) {
								Expr anExpr = exprList.get(j);
								if ( anExpr instanceof ExprIdentStar ) {
									ExprIdentStar id = (ExprIdentStar) anExpr;
									if ( id.getIdentStarKind() == IdentStarKind.instance_variable_t ) {
										InstanceVariableDec iv = (InstanceVariableDec ) id.getVarDeclaration();
										if ( iv.isShared() )
											env.error(s.getFirstSymbol(), "Shared instance variables cannot be initialized in 'init' or 'init:' methods");
										wasInitializedSet.add( iv );
									}
								}
								else if (  anExpr instanceof ExprSelfPeriodIdent ) {
									ExprSelfPeriodIdent exprSelf = (ExprSelfPeriodIdent ) anExpr;
									InstanceVariableDec iv = exprSelf.getInstanceVariableDec();
									if ( iv.isShared() )
										env.error(s.getFirstSymbol(), "Shared instance variables cannot be initialized in 'init' or 'init:' methods");
									wasInitializedSet.add( iv );
								}
							}
						}
					}
					for ( InstanceVariableDec v : currentObject.getInstanceVariableList() ) {
						if ( ! v.isShared() && v.getExpr() == null && ! wasInitializedSet.contains(v) && ! this.hasJavaCode ) {
							String nameiv = v.getName();
							nameiv = nameiv.substring(1);
							if ( nameiv.length() > 0 ) {
								ArrayList<MethodSignature> unaryMethodList = currentObject.searchMethodPrivateProtectedPublic(nameiv);
								if ( unaryMethodList != null && unaryMethodList.size() > 0 && 
										unaryMethodList.get(0).getMethod().getVisibility() == Token.PUBLIC ) {
									env.error(getFirstSymbol(), "Instance variable '" + v.getName() + 
											"' is not being initialized in this 'init' or 'init:' method. " + 
											"Probably the reason is that there is a public instance variable named '" + nameiv + "'." + 
											" You should initialize '" + v.getName() + "'");
								}
							}
							env.error(getFirstSymbol(), "Instance variable '" + v.getName() + "' is not being initialized in this 'init' or 'init:' method");
						}
					}
					ObjectDec superObject = currentObject.getSuperobject();
					if ( superObject != null && superObject != Type.Any ) {
						/*
						 * the call 'super init' or 'super init: a' should only be the first statement of the method.
						 */
						/**
						 * if there is a super-prototype, check whether one of the super init method is called.
						 */
						ArrayList<Statement> statList = this.statementList.getStatementList();
						ArrayList<MethodSignature> superInitMethodList = superObject.searchInitNewMethod("init");
						MethodSignature superInitMethod = null;
						if ( superInitMethodList != null && superInitMethodList.size() > 0 )
							superInitMethod = superInitMethodList.get(0);
						if ( (statList == null || statList.size() == 0) ) {
							/*
							 * this init or 'init:' method does not have statements. If there is an 'init' 
							 * method in the super-prototype, it will be called by code introduced  by
							 * the compiler ( shouldInsertCallToConstructorWithoutParametes = true ).
							 * Otherwise, an error occurs because this init method should call the
							 * super init or init: method.
							 */
							if ( superInitMethod != null ) {
								this.shouldInsertCallToConstructorWithoutParametes = true;
							}
							else {
								env.error(firstSymbol, "This method should call an 'init' or 'init:' method of the super-prototype");
								return ;
							}
						}
						else {
							/*
							 * this 'init' or 'init:' method has statements.
							 */
							Statement firstStat = statList.get(0);
							if ( !(firstStat instanceof ast.ExprMessageSendUnaryChainToSuper) && !(firstStat instanceof ast.ExprMessageSendWithSelectorsToSuper) ) {
								/*
								 * first statement of this 'init' or 'init:' method is not a message send to super
								 */
								if ( superInitMethod != null ) {
									this.shouldInsertCallToConstructorWithoutParametes = true;
								}
								else {
									env.error(firstSymbol, "This method should call an 'init' or 'init:' method of the super-prototype");
									return ;
								}
							}
							else {
								if ( firstStat instanceof ast.ExprMessageSendUnaryChainToSuper ) {
									ExprMessageSendUnaryChainToSuper ms = (ExprMessageSendUnaryChainToSuper ) firstStat;
									if ( ! ms.getUnarySymbol().getSymbolString().equals("init") ) {
										env.error(firstSymbol, "This method should call an 'init' or 'init:' method of the super-prototype");
									}
								}
								else if (firstStat instanceof ast.ExprMessageSendWithSelectorsToSuper ) {
									ExprMessageSendWithSelectorsToSuper ms = (ExprMessageSendWithSelectorsToSuper ) firstStat;
									if ( ! ms.getMessage().getMethodName().equals("init:") ) {
										env.error(firstSymbol, "This method should call an 'init' or 'init:' method of the super-prototype");
									}
								}
							}
						}
							
					}
					
					
				}
				if ( this.isInitOnce() ) {
					/*
					 * check whether all shared instance variables are initialized
					 */
					ObjectDec currentObject = env.getCurrentObjectDec();
					Set<InstanceVariableDec> wasInitializedSet = new HashSet<>();
					for ( Statement s : this.statementList.getStatementList() ) {
						if ( s instanceof ast.StatementAssignmentList ) {
							ArrayList<Expr> exprList = ((StatementAssignmentList) s).getExprList();
							if ( exprList.size() != 2 ) {
								// error(Symbol symbol, String message, boolean checkMessage, boolean throwException)
								env.error(s.getFirstSymbol(), "Only one '=' symbol is allowed per statement in an 'initOnce' method", true, false);
							}
							for (int j = 0; j < exprList.size() - 1; ++j) {
								Expr anExpr = exprList.get(j);
								if ( anExpr instanceof ExprIdentStar ) {
									ExprIdentStar id = (ExprIdentStar) anExpr;
									if ( id.getIdentStarKind() == IdentStarKind.instance_variable_t ) {
										InstanceVariableDec iv = (InstanceVariableDec ) id.getVarDeclaration();
										if ( ! iv.isShared() )
											env.error(s.getFirstSymbol(), 
													"Non-shared instance variables cannot be initialized in 'initOnce' methods", true, false);
										wasInitializedSet.add( iv );
									}
									else {
										env.error(s.getFirstSymbol(), "Instance variable expected in the left side of '='", true, false);
									}
								}
								else if (  anExpr instanceof ExprSelfPeriodIdent ) {
									ExprSelfPeriodIdent exprSelf = (ExprSelfPeriodIdent ) anExpr;
									InstanceVariableDec iv = exprSelf.getInstanceVariableDec();
									if ( ! iv.isShared() )
										env.error(s.getFirstSymbol(), 
												"Non-shared instance variables cannot be initialized in 'initOnce' methods", true, false);
									wasInitializedSet.add( iv );
								}
							}
							Expr right = exprList.get(exprList.size()-1);
							if ( !right.isNREForInitOnce(env) ) {
								right.isNREForInitOnce(env);
								env.error(right.getFirstSymbol(), "The expression is not valid for initializing a shared variable. It should be"
										+ " a literal value or the creation of an object of a prototype of package cyan.lang."
										+ " See the Cyan manual for more information.", true, false);
								
							}
						}
						else {
							// not an assignment
							env.error(s.getFirstSymbol(), "Only assignments are allowed inside method 'initOnce'", true, false);
						}
					}
					for ( InstanceVariableDec v : currentObject.getInstanceVariableList() ) {
						if ( v.isShared() && v.getExpr() == null && ! wasInitializedSet.contains(v) ) {
							env.error(getFirstSymbol(), 
									"Shared instance variable '" + v.getName() + "' is not being initialized in the 'initOnce' method",
									true, false);
						}
					}
					
				}

			}
		}
		catch (CompileErrorException e) {
			return;
		}
		env.atEndMethodDec();


		ArrayList<MethodDec> equalMethodDecList = prototype
				.search_Method_Private_Protected_Public_By_Interface(env, this.getMethodInterface(env));
		if ( equalMethodDecList.size() > 1 ) {
			// there is at least this method in the list. If there is two, then
			// this one is duplicated.

			MethodDec anotherMethod = null;
			for (MethodDec aMethod : equalMethodDecList) {
				if ( aMethod != this ) {
					anotherMethod = aMethod;
					break;
				}
			}
			if ( anotherMethod == null ) {
				env.error(true, null, "Internal error at MethodDec::calcInternalTypes", null, ErrorKind.internal_error);
				return;
			}


			env.error(true, methodSignature.getFirstSymbol(), "Duplicated method", methodSignature.getFullName(env),
					ErrorKind.duplicate_method);
		}
		String name = getNameWithoutParamNumber();

		if ( name.compareTo("init") == 0 || name.compareTo("init:") == 0 ) {
			if ( this.overload ) {
				env.error(this.methodSignature.getFirstSymbol(),  "'init' or 'init:' methods cannot be overloaded");
			}
			Expr returnTypeExpr = methodSignature.getReturnTypeExpr();
			if ( returnTypeExpr != null ) {
				//# String returnName = returnTypeExpr.ifPrototypeReturnsItsName();
				String returnName = returnTypeExpr.getType(env).getFullName();
				if ( returnName.compareTo("Nil") != 0
						&& !returnName.equals(NameServer.cyanLanguagePackageName + ".Nil") ) {
					env.error(true, methodSignature.getFirstSymbol(),
							"constructor 'init' or 'init:' with a return type different from 'Nil'",
							methodSignature.getFullName(env), ErrorKind.init_should_return_Nil);
				}

			}
		}

		if ( name.compareTo("init") != 0 && name.compareTo("init:") != 0 ) {
			// not init or init:

			if ( isAbstract ) {
				if ( !prototype.getIsAbstract() ) {

					env.error(true, this.getFirstSymbol(), "Abstract method cannot belong to a non-abstract prototype",
							methodSignature.getFullName(env), ErrorKind.abstract_method_in_a_non_abstract_prototype);
				}
			}
			if ( !methodSignature.isGrammarMethod() ) {
				/**
				 * non-grammar method check whether the return value type is the
				 * same as the method of the super-prototype (if any)
				 */
				ObjectDec superPrototype = this.prototype.getSuperobject();
				if ( superPrototype != null ) {

					ArrayList<MethodSignature> methodSignatureList = superPrototype
							.searchMethodProtectedPublic(this.getName());
					if ( methodSignatureList.size() > 0 ) {
						Type superReturnType;
						if ( methodSignatureList.get(0).getReturnTypeExpr() == null )
							superReturnType = Type.Nil;
						else {
							
							superReturnType = methodSignatureList.get(0).getReturnTypeExpr()
									.ifRepresentsTypeReturnsType(env);
						}

						Type returnType;
						if ( methodSignature.getReturnTypeExpr() == null )
							returnType = Type.Nil;
						else
							returnType = methodSignature.getReturnTypeExpr().ifRepresentsTypeReturnsType(env);

						if ( !superReturnType.isSupertypeOf(returnType, env) ) {

							String stringMethodSignatureSuper = methodSignatureList.get(0)
									.getMethodSignatureWithParametersAsString();

							String stringMethodSignatureSub = this.getMethodSignatureAsString();

							superReturnType.isSupertypeOf(returnType, env);
							String s = methodSignatureList.get(0).getReturnType(env).getFullName();
							env.error(true, this.getFirstSymbol(), "Incompatible return type in sub-prototype method",
									stringMethodSignatureSub, ErrorKind.incompatible_return_type_in_subprototype_method,
									"method0 = \"" + stringMethodSignatureSub + "\"",
									"method1 = \"" + stringMethodSignatureSuper + "\"");
						}

					}

				}
			}
		}
		super.calcInternalTypes(env);

	}

	/**
	 * do a live analysis of the method code. It should issue errors if some
	 * variable is used before being initialized
	 */
	private void doLiveAnalysis() {
		/*
		 * 
		 * for all n, in[n] = out[n] = emptyset w = set of all nodes of the
		 * method repeat until w is empty n = w.pop() out[n] = union of in[n']
		 * for each n' that is sucessor of n in[n] = use[n] union with (out[n] -
		 * def[n]) if in[n] was changed then for all predecessors m of n,
		 * w.push(m)
		 * 
		 */
		int i = 0;
		Queue<Tuple2<Statement, Integer>> w = new LinkedList<>();
		for (Statement s : this.statementList.getStatementList()) {
			// empty sets
			s.prepareLiveAnalysis();
			w.add(new Tuple2<Statement, Integer>(s, i));
			++i;
		}

		while (w.isEmpty()) {
			Tuple2<Statement, Integer> n = w.remove();
			// n.f1.outLiveAnalysis =
		}
	}

	@Override
	public void calcInterfaceTypes(Env env) {
		env.atBeginningOfCurrentMethod(this);

		methodSignature.calcInterfaceTypes(env);

		env.atEndMethodDec();
	}

	/**
	 * returns the signature of the current method
	 * 
	 * @return
	 */
	public String stringSignature() {
		PWCharArray pwChar = new PWCharArray();
		if ( getHasOverride() ) pwChar.print("override ");
		if ( getVisibility() == Token.PUBLIC )
			pwChar.print("public ");
		else if ( getVisibility() == Token.PRIVATE )
			pwChar.print("private ");
		else if ( getVisibility() == Token.PROTECTED ) pwChar.print("protected ");
		if ( isAbstract() ) pwChar.print("abstract ");
		pwChar.print("func ");

		pwChar.print(getMethodSignatureAsString());
		return pwChar.getGeneratedString().toString();
	}

	public boolean isIndexingMethod() {
		return methodSignature.isIndexingMethod();
	}

	public Symbol getLeftCBsymbol() {
		return leftCBsymbol;
	}

	public void setLeftCBsymbol(Symbol leftCBsymbol) {
		this.leftCBsymbol = leftCBsymbol;
	}

	public Symbol getRightCBsymbol() {
		return rightCBsymbol;
	}

	public void setRightCBsymbol(Symbol rightCBsymbol) {
		this.rightCBsymbol = rightCBsymbol;
	}

	public Symbol getFirstSymbolExpr() {
		return firstSymbolExpr;
	}

	public void setFirstSymbolExpr(Symbol firstSymbolExpr) {
		this.firstSymbolExpr = firstSymbolExpr;
	}

	public Symbol getLastSymbolExpr() {
		return lastSymbolExpr;
	}

	public void setLastSymbolExpr(Symbol lastSymbolExpr) {
		this.lastSymbolExpr = lastSymbolExpr;
	}

	public int getMethodNumber() {
		return methodNumber;
	}

	public boolean getCompilerCreatedMethod() {
		return compilerCreatedMethod;
	}

	public void setCompilerCreatedMethod(boolean compilerCreatedMethod) {
		this.compilerCreatedMethod = compilerCreatedMethod;
	}

	public boolean getIsFinal() {
		return isFinal;
	}

	public void genProtoForMethod(StringBuffer s, CyanEnv cyanEnv) {
		s.append("\n");
		prototypeNameForMethod = NameServer.methodProtoName + methodSignature.getPrototypeNameForMethod()
				+ NameServer.endsInnerProtoName;
		s.append("    object " + prototypeNameForMethod + "(" + declaringObject.getName() + " "
				+ NameServer.selfNameInnerPrototypes + ")" + " extends "
				+ methodSignature.getSuperprototypeNameForMethod() + "\n\n");
		s.append("        func ");
		methodSignature.genCyanEvalMethodSignature(s);
		s.append(" {\n");

		PWCharArray pwChar = new PWCharArray();
		pwChar.add();
		pwChar.add();
		pwChar.add();

		if ( statementList == null ) {
			if ( expr == null ) {
				// an abstract method

				pwChar.printlnIdent("throw: ExceptionCannotCallAbstractMethod(\"" + this.prototype.getFullName() + "::"
						+ this.getMethodSignatureAsString() + "\")");

			}
			else {
				pwChar.printIdent("return ");
				expr.genCyan(pwChar, true, cyanEnv, false);
				pwChar.println(";");
			}
		}
		else
			statementList.genCyan(pwChar, true, cyanEnv, false);
		s.append(pwChar.getGeneratedString().toString());
		s.append("        }\n\n");

		s.append("    end\n");
	}

	public String getPrototypeNameForMethod() {
		return prototypeNameForMethod;
	}

	public StatementList getStatementList() {
		return statementList;
	}

	@Override
	public DeclarationKind getKind() {
		return DeclarationKind.METHOD_DEC;
	}

	public ObjectDec getDeclaringObject() {
		return declaringObject;
	}

	public boolean getOverload() {
		return overload;
	}

	public void setOverload(boolean overload) {
		this.overload = overload;
	}

	public boolean getPrecededBy_overload() {
		return precededBy_overload;
	}

	public void setPrecededBy_overload(boolean precededBy_overload) {
		this.precededBy_overload = precededBy_overload;
	}

	public boolean getAllowAccessToInstanceVariables() {
		return allowAccessToInstanceVariables;
	}

	public void setAllowAccessToInstanceVariables(boolean allowAccessToInstanceVariables) {
		this.allowAccessToInstanceVariables = allowAccessToInstanceVariables;
	}

	public void setFirstSymbol(Symbol firstSymbol) {
		this.firstSymbol = firstSymbol;
	}
	

	private MethodSignature	methodSignature;
	/**
	 * if the method is assigned an expression, it is in variable expr declared
	 * below. Example: public proc get -> int = anotherObject.{get -> int}.
	 */
	private Expr			expr;
	/**
	 * if the method has a body, the following variable points to it. Therefore
	 * expr == null if and only if statementList != null
	 */
	private StatementList	statementList;

	/**
	 * true if there is keyword "override" in the declaration of this method
	 */
	private boolean			hasOverride;

	/**
	 * true if this method is final
	 */
	private boolean			isFinal;

	/**
	 * true if this method is abstract
	 */
	private boolean			isAbstract;
	/**
	 * the prototype in which this method is
	 */
	private ObjectDec		prototype;

	/**
	 * the symbol '{' that opens the method declaration. It is null if the
	 * method is declared with an expression as in func zero -> Int = 0
	 * 
	 */
	private Symbol			leftCBsymbol;

	/**
	 * the symbol '}' that closes the method declaration. It is null if the
	 * method is declared with an expression as in func zero -> Int = 0
	 * 
	 */
	private Symbol			rightCBsymbol;

	/**
	 * if the method is declared with an expression as in func zero -> Int = 0
	 * 
	 * then firstSymbolExpr is the first symbol of the expression
	 */
	private Symbol			firstSymbolExpr;
	/**
	 * if the method is declared with an expression as in func zero -> Int = 0
	 * 
	 * then lastSymbolExpr is the last symbol of the expression
	 */
	private Symbol			lastSymbolExpr;

	/**
	 * the number of this method. The first method of an object has number 0.
	 * This numbering is used for init, init:, new, and new: methods too.
	 */
	private int				methodNumber;

	/**
	 * true if this method was created by the compiler
	 */
	private boolean			compilerCreatedMethod;

	/**
	 * methods are objects in Cyan. This is the name of the prototype created to
	 * represent this method.
	 */
	private String			prototypeNameForMethod;

	/**
	 * symbol of the 'func' keyword that starts this method
	 */
	private Symbol			firstSymbol;

	/**
	 * object in which the method is declared
	 */
	private ObjectDec		declaringObject;

	/**
	 * true if this method has been overloaded
	 */
	private boolean			overload;

	/**
	 * true if this method is preceded by keyword 'overload'
	 */
	private boolean			precededBy_overload;

	/**
	 * true if this method can access instance variables. Of course, 'true' is
	 * the default value. However, if metaobject prototypeCallOnly is attached
	 * to this method, it cannot access instance variables
	 */
	private boolean			allowAccessToInstanceVariables;
	/**
	 * true if this method has any metaobject @javacode inside it
	 */
	private boolean hasJavaCode;
	/**
	 * true if this is an init or init: method. null if it has not been initialized
	 */
	private Boolean isInit;
	/**
	 * the name says it all. If this method is an 'init' or 'init:' method, if
	 * this variable is true a call to the constructor should be inserted in the 
	 * generated Java code to call the super constructor that does not take parameters.
	 */
	private boolean shouldInsertCallToConstructorWithoutParametes;

	static int counter = 0;


}
