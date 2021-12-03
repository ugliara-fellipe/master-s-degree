package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.Token;
import meta.CyanMetaobjectWithAt;
import meta.ExprReceiverKind;
import meta.ICompileTimeDoesNotUnderstand_dsa;
import meta.MetaInfoServer;
import saci.CompilationInstruction;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple;
import saci.Tuple2;

public class ExprMessageSendUnaryChainToExpr extends ExprMessageSendUnaryChain {



	public ExprMessageSendUnaryChainToExpr(Expr expr) {
		super();
		this.receiverExprOrFirstUnary = expr;
	}


	@Override
	public void accept(ASTVisitor visitor) {
		this.receiverExprOrFirstUnary.accept(visitor);
		visitor.visit(this);
	}


	@Override
	public boolean isNRE(Env env) {
		return this.unarySymbol.symbolString.equals("new");
	}

	/**
	 * return true if this expression is non-recursive considering the point of view of method initOnce.
	   @param env
	   @return
	 */
	@Override
	public boolean isNREForInitOnce(Env env) {
		String name = receiverExprOrFirstUnary.asString();
		ProgramUnit pu = env.getProject().getCyanLangPackage().searchPublicNonGenericProgramUnit(name);
		return pu != null && this.unarySymbol.symbolString.equals("new");
	}


	/**
	 *
	 */
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		receiverExprOrFirstUnary.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.print(" ");
		if ( backquote )
			pw.print("`");

		String str = unarySymbol.getSymbolString();
		String prefix = "";
		Token token = unarySymbol.token;
		if  ( token == Token.INTER_ID )
			prefix = "?";
		else if ( token == Token.INTER_DOT_ID )
			prefix = "?.";

		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(prefix + cyanEnv.formalGenericParamToRealParam(str));
		}
		else {
			pw.print(prefix + str);
		}


	}


	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		String receiverAsString;
		String tmp = NameServer.nextJavaLocalVariableName();
		boolean isNew = false;

		if ( type == null ) {
			this.calcInternalTypes(env);
		}

		Token tokenFirstSelector = this.unarySymbol.token;
		boolean precededbyInter = tokenFirstSelector == Token.INTER_ID || tokenFirstSelector == Token.INTER_DOT_ID;

		if ( backquote ) {

			String paramArray = "";
			/*
			 * this for is not necessary. quotedVariableList must have just one element.
			 */
			VariableDecInterface varDec = quotedVariableList.get(0);
			String javaNameVariable = varDec.getJavaName();
			if ( varDec.getType() == Type.Dyn ) {
				paramArray += "((CyString ) " + varDec.getJavaName() + ").s";
				pw.printIdent("if ( ! (" + javaNameVariable + " instanceof CyString) ) ");

				pw.println("throw new ExceptionContainer__("
						+ env.javaCodeForCastException(varDec, Type.Dyn) + " );");

			}
			else
				paramArray += "((CyString ) " + varDec.getJavaName() + ").s";

			String selectorJavaName = NameServer.nextJavaLocalVariableName();
			pw.printIdent("String " + selectorJavaName + " = ");
			pw.println(" CyanRuntime.getJavaNameOfUnaryMethod(" + paramArray + ");");

			receiverAsString = receiverExprOrFirstUnary.genJavaExpr(pw, env);

			String resultTmp = genJavaDynUnaryMessageSend(pw, receiverAsString, javaNameVariable, selectorJavaName);


			return resultTmp;
		}
		else {
			if ( !precededbyInter && unarySymbol.getSymbolString().equals("new") ) {
				 // in "Person new" the generated code should be "new _Person()"
				Type aType = receiverExprOrFirstUnary.ifRepresentsTypeReturnsType(env);
				receiverAsString = aType.getJavaName();
				// t = NameServer.getJavaName(receiverExprOrFirstUnary.asString());
				isNew = true;
			}
			else {
				receiverAsString = receiverExprOrFirstUnary.genJavaExpr(pw, env);
			}
			if ( isNew ) {
				pw.printIdent(type.getJavaName() + " " + tmp + " = ");
				pw.println("new " + receiverAsString + "();");
			}
			else {
				String cyanMethodName = unarySymbol.getSymbolString();
				String selectorJavaName = NameServer.getJavaNameOfSelector(cyanMethodName);
				if ( this.receiverExprOrFirstUnary.getType() == Type.Dyn || precededbyInter ) {
					cyanMethodName = "new CyString(\"" + cyanMethodName + "\")";
					selectorJavaName = "\"" + selectorJavaName + "\"";
					tmp = genJavaDynUnaryMessageSend(pw, receiverAsString, cyanMethodName, selectorJavaName);


				}
				else {
					pw.printIdent(type.getJavaName() + " " + tmp + " = ");
					pw.print(receiverAsString + ".");
					pw.println(selectorJavaName + "();");
				}
			}
			return tmp;
		}
	}

	/**
	   @param pw
	   @param receiverAsString
	   @param cyanMethodName
	   @param selectorJavaName
	   @return
	 */
	@SuppressWarnings("static-method")
	private String genJavaDynUnaryMessageSend(PWInterface pw, String receiverAsString, String cyanMethodName,
			String selectorJavaName) {
		String tmp;
		String aMethodTmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("java.lang.reflect.Method " + aMethodTmp + " = CyanRuntime.getJavaMethodByName(" + receiverAsString + ".getClass(), "
				+ selectorJavaName + ", 0);");
		tmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("Object " + tmp + " = null;");
		pw.printlnIdent("if ( " + aMethodTmp + " != null ) { ");
		pw.add();
		pw.printlnIdent("try {");
		pw.add();
		pw.printlnIdent(aMethodTmp + ".setAccessible(true);");
		pw.printlnIdent(tmp + " = " + aMethodTmp + ".invoke(" + receiverAsString + ");");
		pw.sub();
		pw.printlnIdent("}");

		String ep = NameServer.nextJavaLocalVariableName();

		pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep +" ) {");
		pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
		pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
		pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
		pw.printlnIdent("	}");
		pw.printlnIdent("	else");
		pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
		pw.printlnIdent("}");
		pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
		pw.printlnIdent("        throw new ExceptionContainer__( new _ExceptionDoesNotUnderstand("
				+ cyanMethodName + " ) );");


		pw.printlnIdent("}");

		pw.sub();
		pw.printlnIdent("}");
		pw.printlnIdent("else { ");
		pw.add();
		String dnuTmpVar = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("//	func doesNotUnderstand: (CySymbol methodName, Array<Array<Dyn>> args)");
		pw.printlnIdent("java.lang.reflect.Method " + dnuTmpVar + " = CyanRuntime.getJavaMethodByName(" + receiverAsString + ".getClass(), \"" +
		         NameServer.javaNameDoesNotUnderstand + "\", 2);");
		pw.printlnIdent("if ( " + dnuTmpVar + " == null ) {");
		pw.printlnIdent("    throw new ExceptionContainer__( new _ExceptionDoesNotUnderstand(new CyString(\"doesNotUnderstand\") ) );");
		pw.printlnIdent("}");


		pw.printlnIdent("try {");
		pw.add();
		pw.printlnIdent(NameServer.ArrayArrayDynInJava + " arrayArrayParam = new " + NameServer.ArrayArrayDynInJava + "();");
		pw.printlnIdent(NameServer.ArrayDynInJava + " arrayParam = new " + NameServer.ArrayDynInJava + "();");
		pw.printlnIdent("arrayArrayParam._add_1( arrayParam );");
		pw.printlnIdent(dnuTmpVar + ".setAccessible(true);");

		pw.printlnIdent(tmp + " = " + dnuTmpVar + ".invoke(" + receiverAsString + ", " + cyanMethodName + ", arrayArrayParam);");
		pw.sub();

		pw.printlnIdent("}");
		ep = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep + " ) {");
		pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
		pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
		pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
		pw.printlnIdent("	}");
		pw.printlnIdent("	else");
		pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
		pw.printlnIdent("}");
		pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");

		pw.printlnIdent("        throw new ExceptionContainer__( new _ExceptionDoesNotUnderstand(new CyString("
				+ selectorJavaName + ") ) );");


		pw.printlnIdent("}");
		pw.sub();
		pw.printlnIdent("}");
		return tmp;
	}


	@Override
	public Symbol getFirstSymbol() {
		return receiverExprOrFirstUnary.getFirstSymbol();
	}


	public Expr getReceiver() {
		return receiverExprOrFirstUnary;
	}



	@Override
	public void calcInternalTypes(Env env) {
		String methodName;
		MethodSignature methodSignature;
		ArrayList<MethodSignature> methodSignatureList = null;

		Type exprType;

		Token tokenFirstSelector = unarySymbol.token;


		receiverExprOrFirstUnary.calcInternalTypes(env);
		exprType = receiverExprOrFirstUnary.getType(env);

		if ( env.getCurrentMethod() != null ) {
			String currentMethodName = env.getCurrentMethod().getNameWithoutParamNumber();
			if ( currentMethodName.equals("init") || currentMethodName.equals("init:") ) {
				/**
				 * inside an init or init: method it is illegal to access 'self' as in the following
				 * statements. 'iv' is used for an instance variable and 'im' for instance method.
				 *     iv m1;
				 *     im;
				 *     im m1 m2;
				 *     self im;
				 *     self.iv m1 m2;
				 *
				 */
				if ( this.receiverExprOrFirstUnary instanceof ExprIdentStar ) {
					ExprIdentStar e = (ExprIdentStar ) this.receiverExprOrFirstUnary;
					if ( e.getIdentStarKind() == IdentStarKind.instance_variable_t && ! ((InstanceVariableDec ) e.getVarDeclaration()).isShared() ) {
						env.error(this.getFirstSymbol(),  "Access to an instance variable in an expression inside an 'init' or 'init:' method. This is illegal because the "
								+ "Cyan compiler is not able yet to discover if the instance variable have been initialized or not");

					}
					else if ( e.getIdentStarKind() == IdentStarKind.unaryMethod_t ) {
						env.error(this.getFirstSymbol(),  "Message send to 'self' inside an 'init' or 'init:' method. This is illegal because it can call a "
								+ " subprototype method and this method can access an instance variable that has not been initialized");
					}
				}
				else if ( this.receiverExprOrFirstUnary instanceof ast.ExprSelf  ) {
					env.error(this.getFirstSymbol(),  "Message send to 'self' inside an 'init' or 'init:' method. This is illegal because it can call a "
							+ " subprototype method and this method can access an instance variable that has not been initialized");
				}
				else if ( this.receiverExprOrFirstUnary instanceof ExprSelfPeriodIdent &&
						! (((ExprSelfPeriodIdent ) this.receiverExprOrFirstUnary).getInstanceVariableDec().isShared() ) ) {
					env.error(this.getFirstSymbol(),  "Access to an instance variable in an expression inside an 'init' or 'init:' method. This is illegal because the "
							+ "Cyan compiler is not able yet to discover if the instance variable have been initialized or not");
				}
			}
			else if (  currentMethodName.equals("initOnce") ) {
				/**
				 * inside an initOnce method it is illegal to access 'self' as in the following
				 * statements. 'iv' is used for an instance variable and 'im' for instance method.
				 *     iv m1;
				 *     im;
				 *     im m1 m2;
				 *     self im;
				 *     self.iv m1 m2;
				 *
				 */
				if ( this.receiverExprOrFirstUnary instanceof ExprIdentStar ) {
					ExprIdentStar e = (ExprIdentStar ) this.receiverExprOrFirstUnary;
					if ( e.getIdentStarKind() == IdentStarKind.instance_variable_t  ) {
						env.error(this.getFirstSymbol(),  "Access to an instance variable in an expression inside an 'initOnce'. This is illegal because the "
								+ "the instance variable has not been initialized");

					}
					else if ( e.getIdentStarKind() == IdentStarKind.unaryMethod_t ) {
						env.error(this.getFirstSymbol(),  "Message send to 'self' inside an 'initOnce' method. This is illegal because 'self' is being used and "
								+ "through it some instance variable that has not been initialized may be accessed");
					}
				}
				else if ( this.receiverExprOrFirstUnary instanceof ast.ExprSelf  ) {
					env.error(this.getFirstSymbol(),  "Message send to 'self' inside an 'initOnce' or 'init:' method. This is illegal because "
								+ "through 'self' some instance variable that has not been initialized may be accessed");
				}
				else if ( this.receiverExprOrFirstUnary instanceof ExprSelfPeriodIdent &&
						! (((ExprSelfPeriodIdent ) this.receiverExprOrFirstUnary).getInstanceVariableDec().isShared() ) ) {
					env.error(this.getFirstSymbol(),  "Access to an instance variable in an expression inside an 'initOnce'. This is illegal because the "
							+ "the instance variable has not been initialized");
				}
			}

		}

		// if `  was used, there is no search for the method at compile-time.
		if ( backquote ) {
			calcInternaltTypesWithBackquote(env, unarySymbol);
			type = Type.Dyn;
			return;
		}
		else if ( tokenFirstSelector == Token.INTER_DOT_ID || tokenFirstSelector == Token.INTER_ID || exprType == Type.Dyn ) {
			/*
			INTER_ID_COLON("~InterIdColon"),          // ?name:
			INTER_ID("~InterId"),                     // ?name
			INTER_DOT_ID_COLON("~InterDotIdColon"),   // ?.name:
			INTER_DOT_ID("~InterDotId"),              // ?.name
		   */
			type = Type.Dyn;
			return ;

		}
		else {
			methodName = unarySymbol.getSymbolString();
			ExprReceiverKind receiverKind = ExprReceiverKind.EXPR_R;

			boolean isInit = methodName.equals("init") ;
			boolean isNew = methodName.equals("new");
			if ( isInit ) {
				env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method");

			}


			if ( isNew ) {

				boolean ok = false;
				if ( receiverExprOrFirstUnary instanceof ExprTypeof ) {
					if ( ((ExprTypeof ) receiverExprOrFirstUnary).getType() instanceof ObjectDec )
						ok = true;
				}
				else {
					Tuple<String, Type> t = receiverExprOrFirstUnary.ifPrototypeReturnsNameWithPackageAndType(env);
					ok = t != null && t.f2 != null;
				}

				if ( ! ok ) {
					env.error(true, unarySymbol, "Message '" + methodName + "'  can only be sent to prototypes",
							methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
				}
				else {
					receiverKind = ExprReceiverKind.PROTOTYPE_R;
					methodSignatureList = ((ObjectDec) exprType).searchInitNewMethod(methodName);
					//MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, receiverType, e, env, unarySymbol.get(i));
				}

			}
			else if ( receiverExprOrFirstUnary instanceof ExprSelf ) {
				if ( methodName.equals("init") ) {
					methodSignatureList = ((ObjectDec) exprType).searchInitNewMethod(methodName);
				}
				else {
					  // searches method in the prototype of 'self', the current prototype
					methodSignatureList = exprType.searchMethodPrivateProtectedPublicSuperProtectedPublic(
							methodName, env);
				}
				receiverKind = ExprReceiverKind.SELF_R;
			}
			else {
				if ( methodName.equals("init") ) {
					env.error(true, unarySymbol, "Message 'init' can only be sent to 'self' or 'super'",
							methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
				}

				methodSignatureList = exprType.searchMethodPublicSuperPublic(methodName, env);
			}

			if ( isNew || isInit ) {
				if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {
	    			env.error(getFirstSymbol(), "Method " + methodName + " was not found in prototype " + exprType.getName());
					return ;
				}
			}
			else {
				if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {

					methodSignatureList = exprType.searchMethodPublicSuperPublic(methodName, env);

					ObjectDec currentProto = env.getCurrentObjectDec();
					if ( currentProto != null && currentProto.getOuterObject() != null ) {
						/*
						 * inner prototypes can access private and protected members of the outer prototype
						 */
						methodSignatureList = exprType.searchMethodPrivateProtectedPublicSuperProtectedPublic(methodName, env);
					}

					if ( methodSignatureList == null || methodSignatureList.size() == 0 )  {

						if ( env.getCompInstSet().contains(CompilationInstruction.dsa_actions) &&
								 lookForMethodAtCompileTime(env, this.receiverExprOrFirstUnary.getType() ) ) {
							return ;
						}
						else {
							if ( receiverExprOrFirstUnary instanceof ExprIdentStar &&
									((ExprIdentStar) receiverExprOrFirstUnary).getName().equals("new") ) {
				    			env.error(true, getFirstSymbol(), "It seems you are trying to create an object of " + methodName
				    					+ " . Since 'new' is a method name, put it after '" + methodName + "', not before",
									methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
							}
							else {
				    			env.error(true, getFirstSymbol(), "Method " + methodName + " was not found in prototype " + exprType.getName() +
									    " or its super-prototypes",
									methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
							}
			    			return ;
						}
					}
				}

			}
			methodSignature = methodSignatureList.get(0);
			// methodSignature.calcInterfaceTypes(env);

			MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, exprType, receiverExprOrFirstUnary,
					receiverKind, env, unarySymbol);

			type = methodSignature.getReturnType(env);

		}
		super.calcInternalTypes(env);

	}


	public boolean lookForMethodAtCompileTime(Env env, Type receiverType) {
		if ( !(receiverType instanceof ObjectDec) ) {
			return false;
		}


		ObjectDec proto = (ObjectDec ) receiverType;

		ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectAnnotationList = proto.getMetaobjectAnnotationThisAndSuperCTDNUList();
		for ( CyanMetaobjectWithAtAnnotation annotation : metaobjectAnnotationList ) {
			CyanMetaobjectWithAt cyanMetaobject = annotation.getCyanMetaobject();
			if ( !(cyanMetaobject instanceof ICompileTimeDoesNotUnderstand_dsa) ) {
				env.error(this.getFirstSymbol(), "Internal error: metaobject '" + annotation.getCyanMetaobject().getName() + "' should implement "
						+ meta.ICompileTimeDoesNotUnderstand_dsa.class.getName());
			}
			else {
				ICompileTimeDoesNotUnderstand_dsa doesNot = (ICompileTimeDoesNotUnderstand_dsa ) cyanMetaobject;

				Tuple2<StringBuffer, Type> codeType = null;
				StringBuffer sb = null;
				try {
					codeType = doesNot.dsa_analyzeReplaceUnaryMessage(this.receiverExprOrFirstUnary, this.unarySymbol, env);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					e.printStackTrace();
					env.thrownException(annotation, this.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, annotation);
				}




				if ( codeType != null  ) {
					sb = codeType.f1;

					if ( this.codeThatReplacesThisExpr != null ) {
						/*
						 * this message send has already been replaced by another expression
						 */
						if ( cyanMetaobjectAnnotationThatReplacedMSbyExpr != null ) {
							env.warning(this.getFirstSymbol(), "Metaobject annotation '" + cyanMetaobject.getName() +
									"' at line " + annotation.getFirstSymbol().getLineNumber()  +
									" of prototype " + annotation.getPackageOfAnnotation() + "." +
									annotation.getPackageOfAnnotation() +
									" is trying to replace message send '" + this.asString() +
									"' by an expression. But this has already been asked by metaobject annotation '" +
									cyanMetaobjectAnnotationThatReplacedMSbyExpr.getCyanMetaobject().getName() + "'" +
									" at line " + cyanMetaobjectAnnotationThatReplacedMSbyExpr.getFirstSymbol().getLineNumber() +
									" of prototype " + cyanMetaobjectAnnotationThatReplacedMSbyExpr.getPackageOfAnnotation() + "." +
									cyanMetaobjectAnnotationThatReplacedMSbyExpr.getPackageOfAnnotation());
						}
						else {
							env.warning(this.getFirstSymbol(), "Metaobject annotation '" + cyanMetaobject.getName() +
									"' at line " + annotation.getFirstSymbol().getLineNumber()  +
									" of prototype " + annotation.getPackageOfAnnotation() + "." +
									annotation.getPackageOfAnnotation() +
									" is trying to replace message send '" + this.asString() +
									"' by an expression. But this has already been asked by someone else");
						}
					}

					  // if there is any errors, signals them
					env.errorInMetaobject(cyanMetaobject, this.getFirstSymbol());

					Type typeOfCode = codeType.f2;

					env.removeAddCodeExprMessageSend(this, env.getCurrentCompilationUnit(), annotation, sb, typeOfCode,
							this.getFirstSymbol().getOffset());

					cyanMetaobjectAnnotationThatReplacedMSbyExpr = annotation;

					if ( typeOfCode == null )
						env.error(true,
								this.getFirstSymbol(),
										"This message send was replaced by a message send that has type '" + cyanMetaobject.getPackageOfType() + "." +
												cyanMetaobject.getPrototypeOfType() + "' which was not found", cyanMetaobject.getPrototypeOfType(), ErrorKind.prototype_was_not_found_inside_method);
					else
						type = typeOfCode;


					return true;
				}


			}
		}


		return false;
	}



	/**
	 * unary message send chain sent to receiverExprOrFirstUnary. However, receiverExprOrFirstUnary may be a unary method of the current prototype
	 * as in
	 *      var memberName = memberList first name;
	 * In this case, memberList is packed as "receiverExprOrFirstUnary" and "first" and "name" as unary messages. But memberList
	 * is also a unary message to self.
	 */
	private Expr receiverExprOrFirstUnary;


}
