/**
 *
 */
package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.Token;
import meta.CyanMetaobjectWithAt;
import meta.ExprReceiverKind;
import meta.IActionAssignment_cge;
import meta.ICompileTimeDoesNotUnderstand_dsa;
import meta.MetaInfoServer;
import saci.CompilationInstruction;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple;
import saci.Tuple2;

/** Represents a message sent to an expression. Example:
 *     nil println;
 *     (get + 1) println;
 *
 *  The receiver may be an implicit self. In this case, the first parameter to the
 *  constructor should be null.
 * @author José
 *
 */
public class ExprMessageSendWithSelectorsToExpr extends ExprMessageSendWithSelectors {


	/**
	 * @param the receiver and message. If the receiver is an implicit self, it should be null
	 */
	public ExprMessageSendWithSelectorsToExpr(Expr receiverExpr, MessageWithSelectors message, Symbol nextSymbol) {
		super(message, nextSymbol);
		this.receiverExpr = receiverExpr;
		methodSignatureForMessage = null;
	}


	@Override
	public void accept(ASTVisitor visitor) {
		super.accept(visitor);
		if ( receiverExpr != null ) {
			this.receiverExpr.accept(visitor);
		}
		visitor.visit(this);
	}

	@Override
	public boolean isNRE(Env env) {
		if ( ! message.getMethodName().equals("new:") ) {
			return false;
		}
		for ( SelectorWithRealParameters s : message.getSelectorParameterList() ) {
			for ( Expr e : s.getExprList() ) {
				if ( !e.isNRE(env) )
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean isNREForInitOnce(Env env) {
		String name = receiverExpr.asString();
		ProgramUnit pu = env.getProject().getCyanLangPackage().searchPublicNonGenericProgramUnit(name);
		if ( pu == null || ! message.getMethodName().equals("new:") ) {
			return false;
		}
		for ( SelectorWithRealParameters s : message.getSelectorParameterList() ) {
			for ( Expr e : s.getExprList() ) {
				if ( !e.isNREForInitOnce(env) )
					return false;
			}
		}
		return true;
	}

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		/*
		if ( this.codeThatReplacesThisExpr != null ) {
			pw.print(this.codeThatReplacesThisExpr);
			return ;
		}
		*/

		if ( printInMoreThanOneLine ) {
			if ( receiverExpr != null ) {
			    receiverExpr.genCyan(pw, PWCounter.printInMoreThanOneLine(receiverExpr), cyanEnv, genFunctions );
			    pw.print(" ");
			}
			else if ( cyanEnv.getCreatingInnerPrototypesInsideEval() )
				pw.print(NameServer.selfNameInnerPrototypes + " ");
			else if ( cyanEnv.getCreatingContextObject() )
				pw.print(NameServer.selfNameContextObject + " ");
			message.genCyan(pw, true, cyanEnv, genFunctions);
		}
		else {
			  // print in just one line
			if ( receiverExpr != null ) {
    			receiverExpr.genCyan(pw, false, cyanEnv, genFunctions);
			    pw.print(" ");
			}
			else if ( cyanEnv.getCreatingInnerPrototypesInsideEval() )
				pw.print(NameServer.selfNameInnerPrototypes + " ");
			else if ( cyanEnv.getCreatingContextObject() )
				pw.print(NameServer.selfNameContextObject + " ");
			message.genCyan(pw, false, cyanEnv, genFunctions);
		}
	}



	/**
	 * MessageWithSelectors send<br>
	 * <code>
	 *      (receiverExpr) m: p1  k: p2
	 * </code><br>
	 * should generate<br>
	 * <code>
	 *    tmp1 = receiverExpr; <br>
	 *    tmp2 = p1;  <br>
	 *    tmp3 = p2;  <br>
	 *    tmp4 = tmp1.m_s_k(tmp2, tmp3); <br>
	 *</code>
	 * and return tmp4.
	 * <br>
	 * If any of the parameters has type Dyn or Function and its supertypes (currently, only Any), then
	 * the compiler should check, after the call, if a return statement (from a method, not from
	 * the function itself) was executed. If it was, the current method should return to the correct method. See the example
	 * <code> <br>
	 * object Test<br>
	 *     Int n<br>
	 *     fun m -> Int {<br>
	 *         p: { if n == 0 { return 0 } };<br>
	 *         return 1;  <br>
	 *     }<br>
	 *     fun p: Function<Nil> f {<br>
	 *         t: f;<br>
	 *     }<br>
	 *     fun t: Function<Nil> f {<br>
	 *         f eval;<br>
	 *     } <br>
	 * end<br>
	 * </code>
	 * In this example, the compiler inserts code after the call to p: to check whether the return statement was executed. Idem for the call to t:.
	 * The code inserted is:
	 * <code> <br>
	 * if ( System.hasMethodReturned && ) { (JavaProtoName ) System.returnedValue; }
	 * </code>	 *
	 */
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		String messageSendTmpVar;


		boolean isNew = message.getMethodName().equals("new:");
		boolean isThrow = message.getMethodNameWithParamNumber().equals("throw:1");

		if ( receiverExpr == null ) {
			/*
			 * inside inner prototypes (corresponding to functions and methods),
			 * calls to 'self' are changed to call to 'self__"
			 */
			if ( env.getCurrentObjectDec().getOuterObject() != null ) {
				messageSendTmpVar = NameServer.javaSelfNameInnerPrototypes;
			}
			else
				messageSendTmpVar = "this";
		}
		else {


			if ( isNew ) {
				messageSendTmpVar = "";
			}
			else {
				messageSendTmpVar = receiverExpr.genJavaExpr(pw, env);
			}
		}

		Token tokenFirstSelector = message.getTokenFirstSelector();
		boolean precededbyInter = tokenFirstSelector == Token.INTER_DOT_ID_COLON || tokenFirstSelector == Token.INTER_ID_COLON;

		boolean isMessageToDynExpr = receiverExpr != null && this.receiverExpr.getType() == Type.Dyn;
		boolean checkParameters = !isMessageToDynExpr && ! this.getBackquote() && ! precededbyInter;
		ArrayList<ParameterDec> paramDecList = null;
		if ( this.methodSignatureForMessage != null ) {
			paramDecList = this.methodSignatureForMessage.getParameterList();
		}

		String resultTmpVar = NameServer.nextJavaLocalVariableName();

		ArrayList<String> stringArray = new ArrayList<String>();
		StringBuffer paramPassing = new StringBuffer();
		if ( paramDecList == null ) {
			/*
			 * a dynamic message send
			 */
			for ( SelectorWithRealParameters selector : message.getSelectorParameterList() ) {
				for ( Expr e : selector.getExprList() ) {
					String tmpVar = e.genJavaExpr(pw, env);
					stringArray.add( tmpVar );
				}
		   }
			int size = stringArray.size();
			for ( String tmp : stringArray ) {
				paramPassing.append(tmp);
				if ( --size > 0 )
					paramPassing.append(", ");
			}
		}
		else {
			/*
			 * if the type of the real argument is Dyn and the type of the formal parameter is not Dyn,
			 * first generate code that cast the real argument to the correct type
			 */

			int ii = 0;
			ArrayList<Expr> realParamExprList = new ArrayList<>();
			for ( SelectorWithRealParameters selector : message.getSelectorParameterList() ) {
				for ( Expr e : selector.getExprList() ) {
					realParamExprList.add(e);
					String tmpVar = e.genJavaExpr(pw, env);
					if ( checkParameters && paramDecList.get(ii).getType() != Type.Dyn && e.getType() == Type.Dyn ) {
						String otherTmpVar = NameServer.nextJavaLocalVariableName();
						pw.printlnIdent(paramDecList.get(ii).getType().getJavaName() + " " + otherTmpVar + ";");
						pw.printlnIdent("if ( " + tmpVar + " instanceof " + paramDecList.get(ii).getType().getJavaName() + " ) ");
						pw.printlnIdent("    " + otherTmpVar + " = (" + paramDecList.get(ii).getType().getJavaName() + " ) " + tmpVar + ";");
						pw.printlnIdent("else");

						pw.printlnIdent("    throw new ExceptionContainer__("
								+ env.javaCodeForCastException(e, paramDecList.get(ii).getType()) + " );");

						stringArray.add(otherTmpVar);
					}
					else {
						/*
                if ( leftType == Type.Any && rightType instanceof InterfaceDec ) {
               		pw.println(" = (" + NameServer.AnyInJava + " ) " + rightExprTmpVar + ";");
                }
                else {
               		pw.println(" = " + rightExprTmpVar + ";");
                }
						 *
						 */
						if ( paramDecList.get(ii).getType() == Type.Any && e.getType() instanceof InterfaceDec ) {
							tmpVar = " (" + NameServer.AnyInJava + " ) " + tmpVar;
						}
						stringArray.add( tmpVar );
					}
					++ii;
				}
			}

			/*
			 * A metaobject attached to the type of the formal parameter may demand that the real argument be
			 * changed. The new argument is the return of method  changeRightHandSideTo
			 */
			ii = 0;
			ParameterDec param;
			int size = stringArray.size();
			for ( String tmp : stringArray ) {
				param = paramDecList.get(ii);
				Type leftType = param.getType(env);
				Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, leftType);
				IActionAssignment_cge changeCyanMetaobject = null;
		        ObjectDec prototypeFoundMetaobject = null;
		        if ( cyanMetaobjectPrototype != null ) {
		        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
		        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;


					try {
						tmp = changeCyanMetaobject.cge_changeRightHandSideTo(
			        			prototypeFoundMetaobject,
			        			tmp, realParamExprList.get(ii).getType(env));
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


				paramPassing.append(tmp);
				//pw.print(tmp);
				if ( --size > 0 )
					paramPassing.append(", ");
				++ii;
			}

		}

		int numParamJavaMethod = 0;
		for ( SelectorWithRealParameters sel : this.message.getSelectorParameterList() ) {
			numParamJavaMethod += sel.getExprList().size();
		}
		String paramListAsString = (stringArray.size() == 0 ? "" : ", ") +
				paramPassing.toString();


		if ( isNew && checkParameters ) {
			pw.printIdent( type.getJavaName() + " " + resultTmpVar + " = ");
			pw.print( "new " + receiverExpr.getType().getJavaName() + "( " +
					paramPassing.toString()  + ");");
		}
		else {
			String javaNameMethod = message.getJavaMethodName();
			if (  this.getBackquote() ) {


				String cyanMethodNameTmpVar = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("String " + cyanMethodNameTmpVar + " = \"\";");

				String paramArray = "";
				int size2 = quotedVariableList.size();
				for ( VariableDecInterface varDec :  quotedVariableList ) {
					String s0;
					if ( varDec.getType() == Type.Dyn ) {
						s0 = "((CyString ) " + varDec.javaNameWithRef() + ").s";
						// if ( varDec.getRefType() ) s0 += "._elem";
						// s0 = s0 + ").s";

						paramArray += s0;
						pw.printIdent("if ( ! (" + varDec.javaNameWithRef() + " instanceof CyString) ) ");

						pw.printlnIdent("    throw new ExceptionContainer__("
								+ env.javaCodeForCastException(varDec, Type.String) + " );");
					}
					else {
						s0 = "((CyString ) " + varDec.javaNameWithRef() + ").s";
						paramArray += s0;
					}
					pw.printlnIdent(cyanMethodNameTmpVar + " += " + s0 + ";");
					String tmpChar = NameServer.nextJavaLocalVariableName();

					pw.printlnIdent("char " + tmpChar + " = " + cyanMethodNameTmpVar + ".charAt(0);");
					pw.printlnIdent("if ( (" + tmpChar + " == '_' || Character.isAlphabetic(" + tmpChar + ") ) && "
							+ "!" + cyanMethodNameTmpVar + ".endsWith(\":\") ) { " + cyanMethodNameTmpVar + " += \":\"; }" );
					if ( --size2 > 0 )
						paramArray += ", ";
				}
				String numParamList = "";
				int size3 = this.message.getSelectorParameterList().size();
				for ( SelectorWithRealParameters sel : this.message.getSelectorParameterList() ) {
					int sizeExprList = sel.getExprList().size();
					numParamList += sizeExprList;
					if ( --size3 > 0 )
						numParamList += ", ";
				}



				String javaNameMethodTmpVar = NameServer.nextJavaLocalVariableName();
				pw.printIdent("String " + javaNameMethodTmpVar + " = ");
				pw.println(" CyanRuntime.getJavaNameOfMethod(new String[] { " + paramArray + " }, new int[] {" + numParamList + "} );");

			    resultTmpVar = genJavaDynMessageSend(pw, messageSendTmpVar, "new CyString(" + cyanMethodNameTmpVar + ")",
				     javaNameMethodTmpVar, numParamJavaMethod, paramListAsString, stringArray);

			}
			else {
				if ( isMessageToDynExpr || precededbyInter ) {
					String cyanMethodName = message.getMethodName();
  				    resultTmpVar = genJavaDynMessageSend(pw, messageSendTmpVar, "new CyString(\"" + cyanMethodName + "\")",
					    "\"" + javaNameMethod + "\"", numParamJavaMethod, paramListAsString, stringArray);

				}
				else {
					pw.printIdent( type.getJavaName() + " " + resultTmpVar + " = ");
					pw.println(messageSendTmpVar + "." + javaNameMethod + "( " + paramPassing.toString()  + ");");
				}
			}

		}


		if ( isThrow ) {
			/*
			 * to prevent the Java compiler from issuing an error message like 'method should return a value',
			 * a return statement is added every message send 'throw: obj'
			 */
			pw.printlnIdent("return null;");
		}
		// super.genJavaTestForReturn(pw, env);

		return resultTmpVar;
	}


	@SuppressWarnings("static-method")
	private String genJavaDynMessageSend(PWInterface pw, String receiverAsString, String cyanMethodName,
			String selectorJavaName, int numParameters, String paramListAsString, ArrayList<String> paramStrList) {
		String tmp;
		String aMethodTmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("java.lang.reflect.Method " + aMethodTmp + " = null;");
		pw.printlnIdent("if ( " + selectorJavaName + " != null ) ");
		pw.printlnIdent("    " + aMethodTmp + " = CyanRuntime.getJavaMethodByName(" + receiverAsString + ".getClass(), "
				+ selectorJavaName + ", " + numParameters +  ");");
		tmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("Object " + tmp + " = null;");
		pw.printlnIdent("if ( " + aMethodTmp + " != null ) { ");
		pw.add();
		pw.printlnIdent("try {");
		pw.add();
		pw.printlnIdent(aMethodTmp + ".setAccessible(true);");

		pw.printlnIdent(tmp + " = " + aMethodTmp + ".invoke(" + receiverAsString + paramListAsString + ");");
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
		for ( String param : paramStrList ) {
			pw.printlnIdent("arrayParam._add_1(" + param + ");");
		}
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
		if ( receiverExpr == null )
			return super.getFirstSymbol();
		else
			return receiverExpr.getFirstSymbol();
	}


	@Override
	public void calcInternalTypes(Env env) {


		Token tokenFirstSelector = message.getTokenFirstSelector();

		Type receiverType;

		if ( receiverExpr != null ) {
			receiverExpr.calcInternalTypes(env);
			receiverType = receiverExpr.getType(env);
		}
		else {
			receiverType = env.getCurrentProgramUnit();
		}



		if ( message.getBackquote() ) {
			message.calcInternalTypes(env);

			calcInternalTypesWithBackquote(env, tokenFirstSelector);
			return;

		}
		else if ( tokenFirstSelector == Token.INTER_DOT_ID_COLON || tokenFirstSelector == Token.INTER_ID_COLON
				|| receiverType == Type.Dyn) {
			/*
			INTER_ID_COLON("~InterIdColon"),          // ?name:
			INTER_ID("~InterId"),                     // ?name
			INTER_DOT_ID_COLON("~InterDotIdColon"),   // ?.name:
			INTER_DOT_ID("~InterDotId"),              // ?.name
		   */
			type = Type.Dyn;
			message.calcInternalTypes(env);

			return ;

		}
		else {

			ArrayList<MethodSignature> methodSignatureList = null;
			String methodNameWithParamNumber = message.getMethodNameWithParamNumber();
			String methodName = message.getMethodName();


			ExprReceiverKind receiverKind = ExprReceiverKind.EXPR_R;
			/*
			 * check if there is a method for this message send
			 */
			if ( methodName.equals("new:") ) {
				Tuple<String, Type> t = receiverExpr.ifPrototypeReturnsNameWithPackageAndType(env);

				if ( t == null || t.f2 == null ) {
					env.error(true,
							this.message.getFirstSymbol(), "Message '" + methodName + "'  can only be sent to prototypes",
							methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes);
				}
				else {
					receiverKind = ExprReceiverKind.PROTOTYPE_R;
					methodSignatureList = ((ObjectDec) receiverType).searchInitNewMethod(methodName);
				}
			}
			else if ( methodName.equals("init:") ) {
				env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be sent to 'super' inside an 'init' or 'init:' method");
			}
			else if ( receiverExpr == null || receiverExpr instanceof ExprSelf ) {
				/*
				 * message send to self
				 */

				if ( env.getEnclosingObjectDec() == null || ! NameServer.isMethodNameEval(env.getCurrentMethod().getNameWithoutParamNumber()) ) {
					/*
					 * inside a regular prototype that is NOT inside another prototype  OR
					 * inside an inner prototype and inside a method that is not 'eval', 'eval:'
					 */
					if ( methodName.equals("init:") ) {
						methodSignatureList = env.getCurrentObjectDec().searchInitNewMethod("init:");
					}
					else {
						// may call private and protected methods
						methodSignatureList = env.getCurrentObjectDec().searchMethodPrivateProtectedPublicSuperProtectedPublic(
								methodNameWithParamNumber, env);
					}
					receiverType = env.getCurrentProgramUnit();
				}
				else {
					/*
					 * inside a method 'eval', 'eval:' of an inner prototype
					 */
					ObjectDec outer = env.getEnclosingObjectDec();
					if ( methodName.equals("init:") ) {
						methodSignatureList = outer.searchInitNewMethod("init:");
					}
					else {
						// may call private and protected methods
						methodSignatureList = outer.searchMethodPrivateProtectedPublicSuperProtectedPublic(
								methodNameWithParamNumber, env);
					}
					receiverType = outer;
				}
				receiverKind = ExprReceiverKind.SELF_R;


				if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {

					if ( env.getCompInstSet().contains(CompilationInstruction.dsa_actions) && lookForMethodAtCompileTime(env, receiverType) ) {
						return ;
					}
					else {
						env.error(true, getFirstSymbol(),
								"Method " + methodName + " was not found in the current prototype or its super-prototypes",
								methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes, receiverExpr == null ? "self" : receiverExpr.asString());
					}
				}

				if ( receiverExpr == null )
					receiverExpr = new ExprSelf(message.getFirstSymbol(), (ObjectDec ) receiverType);
			}
			else {
				methodSignatureList = receiverExpr.getType(env).searchMethodPublicSuperPublic(methodNameWithParamNumber, env);
				if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {


					ObjectDec currentProto = env.getCurrentObjectDec();
					if ( currentProto != null && currentProto.getOuterObject() != null ) {
						/*
						 * inner prototypes can access private and protected members of the outer prototype
						 */
						methodSignatureList = receiverExpr.getType(env).searchMethodPrivateProtectedPublicSuperProtectedPublic(methodNameWithParamNumber, env);
					}
					if ( methodSignatureList == null || methodSignatureList.size() == 0 )  {



						if ( env.getCompInstSet().contains(CompilationInstruction.dsa_actions) && lookForMethodAtCompileTime(env, receiverType) ) {
							return ;
						}
						else {
							env.error(true, getFirstSymbol(),
									"Method " + methodName + " was not found in prototype " + receiverExpr.getType(env).getName() + " or its super-prototypes",
									methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes, receiverExpr.asString());
							methodSignatureList = receiverExpr.getType(env).searchMethodPublicSuperPublic(methodNameWithParamNumber, env);
						}


					}
				}
				receiverType = receiverExpr.getType();
			}
			Tuple<String, Type> t = receiverExpr.ifPrototypeReturnsNameWithPackageAndType(env);

			if ( t != null && t.f2 != null ) {
				receiverKind = ExprReceiverKind.PROTOTYPE_R;
			}


			if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {
				env.error(true, getFirstSymbol(),
						"Method " + methodName + " was not found in the type of the receiver object or in its super-prototypes",
						methodName, ErrorKind.method_was_not_found_in_prototype_or_super_prototypes, receiverExpr == null ? "self" : receiverExpr.asString());
			}



			MetaInfoServer.checkMessageSendWithMethodMetaobjectBeforeTypingMessage(methodSignatureList, receiverType, receiverExpr,
					receiverKind, message, env, this.message.getFirstSymbol());

			message.calcInternalTypes(env);
			if ( receiverExpr instanceof ExprIdentStar ) {
				ExprIdentStar e = (ExprIdentStar ) receiverExpr;
				if ( e.getVarDeclaration() != null ) {
					e.getVarDeclaration().setTypeWasChanged(false);
				}
			}

			methodSignatureForMessage = checkMessageSend(methodSignatureList, env);
			if ( methodSignatureForMessage == null ) {
				// checkMessageSend(methodSignatureList, env);
				env.error(message.getFirstSymbol(), "Type error in message send with method '" + message.getMethodName() + "'", true, true);
			}
			else {
				MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, receiverType, receiverExpr,
						receiverKind, message, env, this.message.getFirstSymbol());


				type = methodSignatureForMessage.getReturnType(env);

				/*
				 *
				 */
				type = MetaInfoServer.replaceMessageSendIfAsked(methodSignatureForMessage,
						this,
						env, message.getFirstSymbol(), type);
			}


		}
		doNotReturn = message.getMethodNameWithParamNumber().equals("throw:1");

		super.calcInternalTypes(env);

	}



	/**
	   @param env
	   @param receiverType
	 */
	public boolean lookForMethodAtCompileTime(Env env, Type receiverType) {
		if ( !(receiverType instanceof ObjectDec) ) {
			return false;
		}


		message.calcInternalTypes(env);

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
				// MessageWithSelectors  message = (MessageWithSelectors ) this.getMessage();
				// message.calcInternalTypes(env);

				Tuple2<StringBuffer, Type> codeType = null;
				StringBuffer sb = null;
				try {
					codeType = doesNot.dsa_analyzeReplaceMessageWithSelectors(receiverExpr, message, env);
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


	public Expr getReceiverExpr() {
		return receiverExpr;
	}

	private Expr receiverExpr;

}
