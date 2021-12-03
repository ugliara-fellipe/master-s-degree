/**

 */
package meta;

import java.util.ArrayList;
import java.util.HashSet;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Expr;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.MessageWithSelectors;
import ast.MethodSignature;
import ast.ObjectDec;
import ast.Type;
import error.ErrorKind;
import lexer.Symbol;
import saci.Env;
import saci.Tuple2;

/**
 * This class keeps information on everything related to metaobjects and supply methods used in several other classes
   @author José

 */
public class MetaInfoServer {

	private static String metaobjectAnnotationMethodNameList[] = { "writeCode" };
	public static HashSet<String> metaobjectAnnotationMethodNameSet;
	static {
		metaobjectAnnotationMethodNameSet = new HashSet<>();
		for ( String methodName : metaobjectAnnotationMethodNameList ) {
			metaobjectAnnotationMethodNameSet.add(methodName);
		}
	}


	/**
	 * check whether an non-unary message send is correct according to the metaobjects of non-unary methods that
	 * potentially could be called. These are collected in  methodSignatureList. <code>expr</code> is
	 * the expression that received the unary message. It is null in case of calls to super. receiverType
	 * is the type <code>expr</code>. symForError is the symbol used when calling an error message.
	   @param methodSignatureList
	   @param exprType
	   @param expr
	   @param env
	   @param symForError
	 */
	public static void checkMessageSendWithMethodMetaobject(
			ArrayList<MethodSignature> methodSignatureList, Type exprType,
			Expr expr, ExprReceiverKind receiverKind,
			MessageWithSelectors message,
			Env env, Symbol symForError) {
		/*
		 * call methods of metaobjects whose annotations are associated to the methods. First the lower in the hierarchy.
		 */
		if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
			boolean inSubprototype = true;
			for ( MethodSignature ms : methodSignatureList ) {
				ArrayList<CyanMetaobjectWithAtAnnotation>  ctmetaobjectAnnotationList;
				if ( ms.getMethod() == null ) {
					// it is a method of an interface
					ctmetaobjectAnnotationList = ms.getAttachedMetaobjectAnnotationList();
				}
				else
					ctmetaobjectAnnotationList = ms.getMethod().getAttachedMetaobjectAnnotationList();
				if ( ctmetaobjectAnnotationList != null ) {


					int size = ctmetaobjectAnnotationList.size();
					//ArrayList<CyanMetaobjectWithAtAnnotation> list = ms.getMethod().getMetaobjectAnnotationList();
					for (int i = size -1; i >= 0 ; --i) {
						CyanMetaobjectWithAtAnnotation annotation = ctmetaobjectAnnotationList.get(i);
						CyanMetaobjectWithAt cyanMO = annotation.getCyanMetaobject();
						if ( cyanMO instanceof ICheckMessageSend_dsa ) {

							try {
								((ICheckMessageSend_dsa ) cyanMO).dsa_checkSelectorMessageSend(expr, exprType, receiverKind, message, env, ms);
							}
							catch ( error.CompileErrorException e ) {
							}
							catch ( RuntimeException e ) {
								// e.printStackTrace();
								env.thrownException(annotation, annotation.getFirstSymbol(), e);
							}
							finally {
								env.errorInMetaobjectCatchExceptions(cyanMO, annotation);
							}


							if ( inSubprototype ) {
								inSubprototype = false;
								Type mostSpecificReceiver = methodSignatureList.get(0).getMethod().getDeclaringObject();


								try {
									((ICheckMessageSend_dsa ) cyanMO).dsa_checkSelectorMessageSendMostSpecific(expr, exprType,
											receiverKind, message, env, ms, mostSpecificReceiver);								}
								catch ( error.CompileErrorException e ) {
								}
								catch ( RuntimeException e ) {
									// e.printStackTrace();
									env.thrownException(annotation, annotation.getFirstSymbol(), e);
								}
								finally {
									env.errorInMetaobjectCatchExceptions(cyanMO, annotation);
								}


							}

						}

					}
				}
			}
		}
	}


	/**
	 * check whether an non-unary message send is correct according to the metaobjects of non-unary methods that
	 * potentially could be called. These are collected in  methodSignatureList. <code>expr</code> is
	 * the expression that received the unary message. It is null in case of calls to super. receiverType
	 * is the type <code>expr</code>. symForError is the symbol used when calling an error message.
	 * This method is called before the message is typed. Therefore the types of parameters are not
	 * available.
	   @param methodSignatureList
	   @param exprType
	   @param expr
	   @param env
	   @param symForError
	 */
	public static void checkMessageSendWithMethodMetaobjectBeforeTypingMessage(
			ArrayList<MethodSignature> methodSignatureList, Type exprType,
			Expr expr, ExprReceiverKind receiverKind,
			MessageWithSelectors message,
			Env env, Symbol symForError) {
		/*
		 * call methods of metaobjects whose annotations are associated to the methods. First the higher in the hierarchy.
		 *
		 */
		if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
			for ( MethodSignature ms : methodSignatureList ) {
				ArrayList<CyanMetaobjectWithAtAnnotation>  ctmetaobjectAnnotationList;
				if ( ms.getMethod() == null ) {
					// it is a method of an interface
					ctmetaobjectAnnotationList = ms.getAttachedMetaobjectAnnotationList();
				}
				else
					ctmetaobjectAnnotationList = ms.getMethod().getAttachedMetaobjectAnnotationList();
				if ( ctmetaobjectAnnotationList != null ) {
					int size = ctmetaobjectAnnotationList.size();
					//ArrayList<CyanMetaobjectWithAtAnnotation> list = ms.getMethod().getMetaobjectAnnotationList();
					for (int i = size -1; i >= 0 ; --i) {
						CyanMetaobjectWithAtAnnotation annotation = ctmetaobjectAnnotationList.get(i);
						CyanMetaobjectWithAt cyanMO = annotation.getCyanMetaobject();
						if ( cyanMO instanceof ICheckMessageSendBeforeTypingMessage_dsa ) {
							try {
								((ICheckMessageSendBeforeTypingMessage_dsa ) cyanMO).dsa_checkSelectorMessageSend(expr, exprType, receiverKind, message, env, ms);
							}
							catch ( error.CompileErrorException e ) {
							}
							catch ( RuntimeException e ) {
								// e.printStackTrace();
								env.thrownException(annotation, annotation.getFirstSymbol(), e);
							}
							finally {
								env.errorInMetaobjectCatchExceptions(cyanMO, annotation);
							}

						}

					}
				}
			}
		}
	}



	/**
	 * check whether an unary message send is correct according to the metaobjects of unary methods that
	 * potentially could be called. These are collected in  methodSignatureList. <code>expr</code> is
	 * the expression that received the unary message. It is null in case of calls to super. receiverType
	 * is the type <code>expr</code>. symForError is the symbol used when calling an error message.
	   @param methodSignatureList
	   @param exprType
	   @param expr
	   @param env
	   @param symForError
	 */
	public static void checkMessageSendWithMethodMetaobject(ArrayList<MethodSignature> methodSignatureList, Type exprType,
			Expr expr, ExprReceiverKind receiverKind,  Env env, Symbol symForError) {
		/*
		 * call methods of metaobjects whose annotations are associated to the methods. First the higher in the hierarchy.
		 */
		if ( methodSignatureList != null  && methodSignatureList.size() > 0 ) {
			boolean inSubprototype = true;

			for ( MethodSignature ms : methodSignatureList ) {
				ArrayList<CyanMetaobjectWithAtAnnotation> list;
				if ( ms.getMethod() != null ) {
					// a method of a prototype
					list = ms.getMethod().getAttachedMetaobjectAnnotationList();
				}
				else {
					// a method of an interface
					list = ms.getAttachedMetaobjectAnnotationList();
				}

				if ( list != null ) {
					int size = list.size();

					for (int i = size -1; i >= 0 ; --i) {
						CyanMetaobjectWithAtAnnotation annotation = list.get(i);
						CyanMetaobjectWithAt cyanMO = annotation.getCyanMetaobject();
						if ( cyanMO instanceof ICheckMessageSend_dsa ) {
							ArrayList<CyanMetaobjectError> errorList = null;


							try {
								((ICheckMessageSend_dsa ) cyanMO).dsa_checkUnaryMessageSend(expr, exprType,  receiverKind);
							}
							catch ( error.CompileErrorException e ) {
							}
							catch ( RuntimeException e ) {
								// e.printStackTrace();
								env.thrownException(annotation, annotation.getFirstSymbol(), e);
							}
							finally {
								env.errorInMetaobjectCatchExceptions(cyanMO, annotation);
							}




							if ( inSubprototype ) {
								inSubprototype = false;
								Type mostSpecificReceiver = methodSignatureList.get(0).getMethod().getDeclaringObject();

								try {
									((ICheckMessageSend_dsa ) cyanMO).dsa_checkUnaryMessageSendMostSpecific(expr, exprType,  receiverKind, mostSpecificReceiver);
								}
								catch ( error.CompileErrorException e ) {
								}
								catch ( RuntimeException e ) {
									// e.printStackTrace();
									env.thrownException(annotation, annotation.getFirstSymbol(), e);
								}
								finally {
									env.errorInMetaobjectCatchExceptions(cyanMO, annotation);
								}

							}
						}

					}
				}
			}
		}
	}


	/**
	 *  check whether there is a metaobject that implements IActionAssignment_cge associated to type {@code leftType}.
	 *  A tuple is returned if a metaobject is found or null otherwise.
	 *  This tuple has two elements. The first one is the first metaobject found in a search starting in prototype
	 *  {@code leftType}. The second tuple element is the prototype in which the metaobject was found.
	 *
	 */
	public static Tuple2<IActionAssignment_cge, ObjectDec> getChangeAssignmentCyanMetaobject(Env env, Type leftType) {

		IActionAssignment_cge changeCyanMetaobject = null;
		if ( leftType instanceof ObjectDec ) {
        	ObjectDec proto = (ObjectDec ) leftType;
        	while ( proto != null ) {
            	ArrayList<CyanMetaobjectWithAtAnnotation> ctmetaobjectAnnotationList = proto.getAttachedMetaobjectAnnotationList();
            	if ( ctmetaobjectAnnotationList != null ) {
                	boolean found = false;
                	for ( CyanMetaobjectWithAtAnnotation annotation : ctmetaobjectAnnotationList ) {
                		CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
                		if ( cyanMetaobject instanceof IActionAssignment_cge ) {
                			if ( found ) {
                				env.error(annotation.getFirstSymbol(), "There is more than one metaobject annotation attached to '" + proto.getFullName()
                				+ "' that implements interface IActionAssignment_cge. That is illegal", true, true);
                			}
                			changeCyanMetaobject = (IActionAssignment_cge ) cyanMetaobject;
                			found = true;
                		}
                	}
                	if ( found ) {
                		return new Tuple2<IActionAssignment_cge, ObjectDec>(changeCyanMetaobject, proto);
                	}

            	}
        		proto = proto.getSuperobject();
        	}
        }
		return null;
	}


	public static Type replaceMessageSendIfAsked(MethodSignature ms,
			ExprMessageSendWithSelectorsToExpr messageSendExpr,
			Env env, Symbol symForError, Type originalType) {

		ArrayList<CyanMetaobjectWithAtAnnotation> list;
		if ( ms.getMethod() != null ) {
			// a method of a prototype
			list = ms.getMethod().getAttachedMetaobjectAnnotationList();
		}
		else {
			// a method of an interface
			list = ms.getAttachedMetaobjectAnnotationList();
		}

		if ( list != null ) {

			for (CyanMetaobjectWithAtAnnotation annotation : list ) {
				CyanMetaobjectWithAt cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject instanceof IActionMessageSend_dsa ) {


					IActionMessageSend_dsa doesNot = (IActionMessageSend_dsa ) cyanMetaobject;
					// MessageWithSelectors  message = (MessageWithSelectors ) this.getMessage();
					// message.calcInternalTypes(env);

					Tuple2<StringBuffer, Type> codeType = null;
					StringBuffer sb = null;
					try {
						codeType = doesNot.dsa_analyzeReplaceMessageWithSelectors(messageSendExpr, env);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, symForError, e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, annotation);
					}

					if ( codeType != null  ) {
						sb = codeType.f1;

						if ( messageSendExpr.getCodeThatReplacesThisExpr() != null ) {
							/*
							 * this message send has already been replaced by another expression
							 */
							if ( messageSendExpr.getCyanMetaobjectAnnotationThatReplacedMSbyExpr() != null ) {
								env.warning(symForError, "Metaobject annotation '" + cyanMetaobject.getName() +
										"' at line " + annotation.getFirstSymbol().getLineNumber()  +
										" of prototype " + annotation.getPackageOfAnnotation() + "." +
										annotation.getPackageOfAnnotation() +
										" is trying to replace message send '" + messageSendExpr.asString() +
										"' by an expression. But this has already been asked by metaobject annotation '" +
										messageSendExpr.getCyanMetaobjectAnnotationThatReplacedMSbyExpr().getCyanMetaobject().getName() + "'" +
										" at line " + messageSendExpr.getCyanMetaobjectAnnotationThatReplacedMSbyExpr().getFirstSymbol().getLineNumber() +
										" of prototype " + messageSendExpr.getCyanMetaobjectAnnotationThatReplacedMSbyExpr().getPackageOfAnnotation() + "." +
										messageSendExpr.getCyanMetaobjectAnnotationThatReplacedMSbyExpr().getPackageOfAnnotation());
							}
							else {
								env.warning(symForError, "Metaobject annotation '" + cyanMetaobject.getName() +
										"' at line " + annotation.getFirstSymbol().getLineNumber()  +
										" of prototype " + annotation.getPackageOfAnnotation() + "." +
										annotation.getPackageOfAnnotation() +
										" is trying to replace message send '" + messageSendExpr.asString() +
										"' by an expression. But this has already been asked by someone else");
							}
						}

						  // if there is any errors, signals them
						env.errorInMetaobject(cyanMetaobject, symForError);

						Type typeOfCode = codeType.f2;

						env.removeAddCodeExprMessageSend(messageSendExpr, env.getCurrentCompilationUnit(), annotation, sb, typeOfCode,
								symForError.getOffset());

						messageSendExpr.setCyanMetaobjectAnnotationThatReplacedMSbyExpr(annotation);

						if ( typeOfCode == null )
							env.error(true,
									symForError,
											"This message send was replaced by a message send that has type '" + cyanMetaobject.getPackageOfType() + "." +
													cyanMetaobject.getPrototypeOfType() + "' which was not found", cyanMetaobject.getPrototypeOfType(), ErrorKind.prototype_was_not_found_inside_method);
						return typeOfCode;
					}

					break;
				}
			}


		}

		return originalType;
	}

}
