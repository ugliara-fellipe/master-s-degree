package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Lexer;
import lexer.Symbol;
import meta.ExprReceiverKind;
import meta.MetaInfoServer;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple;

/**
 * 
 * 
 * 
 * Represents an instance variable, a local variable, a literal prototype, 
 * or an unary method of the current object in an expression. In the following examples,
 * both sides of the assignment are represented by this class.
 *    n = size; // implicit self
 *    s = Store; // Store is an object
 *    s = t;     // t is a local variable
 *    s = pi;    // pi is a read only variable
 *    s = get;   // get is a unary method.
 * 
 * The literal prototypes may be preceded by a list of package
 * names separated by dots as in "cyan.lang.Int".    
 * 
 * More examples:
 *      var stack = util.ds.Stack; // util.ds is a package name and Stack a literal object name
 *      var w = cyan.awt.lib.Window new;
 *
 * The literal object may be generic and therefore, in the source code, it may be
 * followed by type parameters such as in
 *      var intStack = util.ds.Stack<Int>;
 * In this case, "util.ds.Stack<Int>" will be represented by an object of
 * class ExprGenericType.
 *
 * @author José
 *
 */


public class ExprIdentStar extends Expr 
          implements Identifier, LeftHandSideAssignment, IReceiverCompileTimeMessageSend, INextSymbol  {

	public ExprIdentStar(ArrayList<Symbol> identSymbolArray, Symbol nextSymbol ) {
		this.identSymbolArray = identSymbolArray;
		this.nextSymbol = nextSymbol;
		precededByRemaninder = false;
		this.identStarKind = null;
		nameWithPackageAndType = null;
		messageSendToMetaobjectAnnotation = null;
		originalJavaName = null;
	}
	
	@Override
	public String asString() {
		if ( this.codeThatReplacesThisExpr != null ) {
			return this.codeThatReplacesThisExpr.toString();
		}
		else {
			return getName();
		}		
	}
	
	
	
	@Override
	public void accept(ASTVisitor visitor) {
		
		visitor.visit(this);
	}	
	
	
	/*
	@Override
	public boolean isNREForInitOnce(Env env) {
		saci.Tuple<String, Type> t = ifPrototypeReturnsNameWithPackageAndType(env);
		return t != null && this.identStarKind == IdentStarKind.prototype_t && t.f1.equals(NameServer.cyanLanguagePackageName) &&
				env.searchPackagePrototype(NameServer.cyanLanguagePackageName, t.f2.getName()) != null;
	}
	*/


	public ExprIdentStar(Symbol ... symbolArray) {
		precededByRemaninder = false;
		this.identSymbolArray = new ArrayList<Symbol>(symbolArray.length);
		for ( Symbol s : symbolArray ) 
			identSymbolArray.add(s);
	}
	
	@Override
	public boolean mayBeStatement() {
		return identStarKind == IdentStarKind.unaryMethod_t;
	}
	
	@Override
	public boolean addSemicolonJavaCode() {
		return true;
	}
	

	@Override
	public boolean isNRE(Env env) {
		return this.identSymbolArray.size() == 1 && saci.Compiler.isBasicType(this.identSymbolArray.get(0).token);
		/*else {
			saci.Tuple<String, Type> t = ifPrototypeReturnsNameWithPackageAndType(env);
			return t != null && this.identStarKind == IdentStarKind.prototype_t && t.f1.equals(NameServer.cyanLanguagePackageName) &&
					env.searchPackagePrototype(NameServer.cyanLanguagePackageName, t.f2.getName()) != null;
		}
		*/
	}	
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			int size = identSymbolArray.size();
			for ( Symbol s : identSymbolArray ) {
				pw.print( Lexer.addSpaceAfterComma(cyanEnv.formalGenericParamToRealParam(s.getSymbolString())) );
				--size;
				if ( size > 0 )
					pw.print(".");
			}
			
		}
		else {
			pw.print(getName());
		}		
		
	}

	@Override
	public void genJavaCodeVariable(PWInterface pw, Env env) {
		// pw.print(genJavaExpr(pw, env));
		if ( this.varDeclaration == null ) 
			pw.print(genJavaExpr(pw, env));
		else {
			String jn = NameServer.getJavaName(this.varDeclaration.getName());
			if ( this.varDeclaration.getRefType() ) 
				jn = jn + ".elem";
			pw.print( jn );
		}
	}

	
	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {

		if ( identStarKind == IdentStarKind.instance_variable_t ||
				 identStarKind == IdentStarKind.variable_t ) {
				
				if ( this.varDeclaration.getTypeWasChanged() ) {
					javaName = varDeclaration.getJavaName(); 
				}
				else {
					javaName = varDeclaration.javaNameWithRef();
				}
		}
		
		return  javaName;
	}

	@Override
	public final void genJava(PWInterface pw, Env env) {
		
		if ( identStarKind == IdentStarKind.instance_variable_t ||
			 identStarKind == IdentStarKind.variable_t ) {
			
			if ( this.varDeclaration.getTypeWasChanged() ) {
				javaName = varDeclaration.getJavaName(); 
			}
			else {
				javaName = varDeclaration.javaNameWithRef();
			}
		}
		
		pw.printIdent( javaName);
	}

	
	
	@Override
	public Symbol getFirstSymbol() {
		return identSymbolArray.get(0);
	}


	public ArrayList<Symbol> getIdentSymbolArray() {
		return identSymbolArray;
	}

	public void setIdentSymbolArray(ArrayList<Symbol> identSymbolArray) {
		this.identSymbolArray = identSymbolArray;
	}

	@Override
	public String getName() {
		String ret = "";
	
		int size = identSymbolArray.size();
		for ( Symbol s : identSymbolArray ) {
			ret = ret + s.getSymbolString();
			--size;
			if ( size > 0 )
				ret = ret + ".";
		}
		return ret;
		
	}

	/**
	 * the the last name of the identifier. If it is "main.util.Stack", 
	 * this method returns "Stack"
	 * @return
	 */
	public String getLastName() {
		return identSymbolArray.get(identSymbolArray.size() - 1).getSymbolString();
	}
	
	@Override
	public String getJavaName() {
		return NameServer.getJavaNameQualifiedIdentifier(identSymbolArray);
	}



	public boolean getPrecededByRemaninder() {
		return precededByRemaninder;
	}

	public void setPrecededByRemaninder(boolean precededByRemaninder) {
		this.precededByRemaninder = precededByRemaninder;
	}

	@Override
	public void calcInternalTypes(Env env) {
		
		calcInternalTypes(env, false);
		super.calcInternalTypes(env);
	}
	
	/**
	 * an object of this class may represent a variable, parameter, unary method etc. 
	 * In this case, calcType will set the type as the type of variable, parameter etc. 
	 * However, an object of this class may also represent a prototype such as
	 * Person, Int, String, cyan.lang.Char. In this case, the type will be set
	 * as Person, Int, String etc.
	 * 
	 *  If leftHandSideAssignment is true, this identifier is in the left-hand side of a
	 *  assignment as "f1" and "y" in 
	 *         f1 = y = other;
	 *  In this case, f1 and y should be local variables. They cannot be read only variables, unary
	 *  methods, and parameters.
	 */
	@Override
	public void calcInternalTypes(Env env, boolean leftHandSideAssignment) {
		/*
		 * 
		 * search to discover if this is a local variable, parameter, unary method, 
		 * etc. If it is not, it may be a prototype
		 */
		
		ProgramUnit currentProgramUnit = env.getCurrentProgramUnit();
		String name = getName();
		
		
		if ( name.equals(NameServer.dynName) ) {
			type = Type.Dyn;
			javaName = NameServer.javaDynName;
			identStarKind = IdentStarKind.prototype_t; 
		}
		else if ( identSymbolArray.size() == 1  ) {
			  /*
			   * just one Id, no dots 
			   */
			if ( currentProgramUnit == null ) {
				/** the identifier is outside a prototype or interface declaration.
				 */
				ProgramUnit programUnit = env.searchVisibleProgramUnit(name, this.getFirstSymbol(), false);
				// try to find a prototype
				if ( programUnit != null ) {
					identStarKind = IdentStarKind.prototype_t;
					
					type = programUnit;
					// foundIdent = true;
					if ( programUnit instanceof InterfaceDec ) {
						javaName = NameServer.getJavaName(NameServer.prototypeFileNameFromInterfaceFileName(programUnit.getName())) + ".prototype";
					}
					else
						javaName = NameServer.getJavaName(name) + ".prototype";
				}
				else {
					type = Type.Dyn;
					env.error(getFirstSymbol(), "Identifier '" + name + "' was not found", true, false);
					return;
				}

			}
			else if ( env.getCurrentMethod() == null ) {
				calcInternalTypes_single_id_outside_method(env, currentProgramUnit, name);
			}
			else {  
				calcInternalTypesSingleIdInsideMethod(env, leftHandSideAssignment, currentProgramUnit, name);				
			}
		}
		else {
			   // a composite identifier, with package as "math.Complex"
			if ( leftHandSideAssignment ) {
				type = Type.Dyn;
				env.error(getFirstSymbol(),
						"Identifier expected. Found '" + name + "'", true, false);
			}
			else {
				calcInternalTypesPackagePrototype(env, leftHandSideAssignment, currentProgramUnit, name);
				
			}
		}
	}

	/**
	   @param env
	   @param leftHandSideAssignment
	   @param currentProgramUnit
	   @param name
	 */
	private void calcInternalTypesPackagePrototype(Env env, boolean leftHandSideAssignment,
			ProgramUnit currentProgramUnit, String name) {
		ProgramUnit programUnit;
		// something like "math.Complex", "cyan.lang.Function"
		// the last identifier should be a prototype name. The others are the package name. 
		// For example, in "cyan.lang.Function", "Function" is a prototype name and "cyan.lang" is a package name
		int lastDot = name.lastIndexOf('.');
		String packageName = name.substring(0,  lastDot);
		String prototypeName = name.substring(lastDot + 1);
		CyanPackage aPackage = env.getProject().searchPackage(packageName);
		if ( aPackage == null ) {
			// did not found a prototype 
			//  error(Symbol symbol, String specificMessage, String identifier, ErrorKind errorKind ) {
			type = Type.Dyn;
			env.error(getFirstSymbol(),
					"Package " + packageName + " was not found", true, false);
			
		}
		else {
			   // found the package. Try to find the prototype
			programUnit = aPackage.searchPublicNonGenericProgramUnit(prototypeName);
			if ( programUnit != null ) {
				
				identStarKind = IdentStarKind.prototype_t;
				type = programUnit;
				if ( programUnit instanceof InterfaceDec ) {
					javaName = NameServer.getJavaName(NameServer.prototypeFileNameFromInterfaceFileName(this.getName())) + ".prototype";
				}
				else
					javaName = NameServer.getJavaNameQualifiedIdentifier(this.identSymbolArray) + ".prototype";
				if ( leftHandSideAssignment ) {
					env.error(true, getFirstSymbol(),
							"Prototype '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.prototype_cannot_be_used_in_the_left_hand_side_of_an_assignment);
				}
			}
			else {
				
				programUnit = env.searchPrivateProgramUnit(prototypeName);
				if ( programUnit == null ) {
					//env.searchVisibleProgramUnit(name, this.getFirstSymbol(), true);
				    //currentProgramUnit.searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env);
				    /* env.error(true, getFirstSymbol(),
						"Identifier '" + name + "' was not declared", name, ErrorKind.variable_was_not_declared); */
				    type = Type.Dyn;
				    env.error(getFirstSymbol(),
						"Identifier '" + name + "' was not declared", true, false);
				    
				}
				else {
					identStarKind = IdentStarKind.prototype_t;
					type = programUnit;
					if ( programUnit instanceof InterfaceDec ) {
						javaName = NameServer.getJavaName(NameServer.prototypeFileNameFromInterfaceFileName(prototypeName)) + ".prototype";
					}
					else
						javaName = NameServer.getJavaName(prototypeName) + ".prototype";
					if ( leftHandSideAssignment ) {
						env.error(true, getFirstSymbol(),
								"Prototype '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.prototype_cannot_be_used_in_the_left_hand_side_of_an_assignment);
					}
				}

				
				
				
				/*
				type = Type.Dyn;
				env.error(getFirstSymbol(),
						"Prototype '" + prototypeName + "' was not found in package '" + packageName + "'", 
						true, false);
				// programUnit == null, prototype was not found
				
				if ( env.getCurrentMethod() == null ) {

					if ( env.getCurrentProgramUnit() == null ) {
						env.error(true, getFirstSymbol(),
								"Prototype '" + prototypeName + "' was not found in package '" + packageName + "'", name, ErrorKind.prototype_was_not_found_outside_prototype);
					}
					else {
						env.error(true, getFirstSymbol(),
								"Prototype '" + prototypeName + "' was not found in package '" + packageName + "'", name, ErrorKind.prototype_was_not_found_inside_prototyped);
					}
					
				}
				else {
					env.error(true, getFirstSymbol(),
							"Prototype " + name + " was not found", name, ErrorKind.prototype_was_not_found_inside_method);
				}
				*/
				
			}
		}
	}

	/**
	   @param env
	   @param leftHandSideAssignment
	   @param currentProgramUnit
	   @param name
	 */
	private void calcInternalTypesSingleIdInsideMethod(Env env, boolean leftHandSideAssignment,
			ProgramUnit currentProgramUnit, String name) {


		ProgramUnit programUnit;
		// inside a method

		type = null;
		

		if ( env.getEnclosingObjectDec() == null ) {
			calcInternalTypes_single_id_inside_method_outer_prototype(env, leftHandSideAssignment, currentProgramUnit, name);
		}
		else {
			/*
			 * inside an inner prototype
			 */
			if ( NameServer.isMethodNameEval(env.getCurrentMethod().getNameWithoutParamNumber()) ) {
				calcInternalTypes_single_id_inside_method_inner_prototype_in_eval(env, leftHandSideAssignment, name);
			}
			else {
				calcInternalTypes_single_id_inside_method_inner_prototype_NOT_eval(env, leftHandSideAssignment,
						currentProgramUnit, name);
			}
		}
		if ( type == null ) {

			/*
			 * did not find an unary method. Search for a program unit
			 */
			programUnit = env.searchPrivateProgramUnit(name);
			if ( programUnit == null )
				programUnit = env.searchVisibleProgramUnit(name, this.getFirstSymbol(), true);
			if ( programUnit == null ) {
				
				type = Type.Dyn;
				env.error(getFirstSymbol(),
						"Identifier '" + name + "' was not declared", true, false);
				/*
				// did not find a unary method or prototype
				ArrayList<MethodSignature> unaryMethodList = currentProgramUnit.searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env); 
				if ( unaryMethodList != null && unaryMethodList.size() > 0 ) {
					env.error(this.getFirstSymbol(), "Unary method '" + name + "' cannot be used as an instance variable");
				}
				else {
					//calcInternalTypes_single_id_inside_method_outer_prototype(env, leftHandSideAssignment, currentProgramUnit, name);
					type = Type.Dyn;
					env.error(getFirstSymbol(),
							"Identifier '" + name + "' was not declared", true, false);
				}
				*/
				
			}
			else {
				identStarKind = IdentStarKind.prototype_t;
				type = programUnit;
				// foundIdent = true;
				if ( programUnit instanceof InterfaceDec ) {
					javaName = NameServer.getJavaName(NameServer.prototypeFileNameFromInterfaceFileName(programUnit.getName())) + ".prototype";
				}
				else
					javaName = NameServer.getJavaName(name) + ".prototype";
				if ( leftHandSideAssignment ) {
					env.error(getFirstSymbol(),
							"Prototype '" + name + "' cannot be used in the left-hand side of an assignment", true, false);
				}
			}
		}

	}

	/**
	   @param env
	   @param leftHandSideAssignment
	   @param currentProgramUnit
	   @param name
	 */
	private void calcInternalTypes_single_id_inside_method_inner_prototype_NOT_eval(Env env,
			boolean leftHandSideAssignment, ProgramUnit currentProgramUnit, String name) {
		/*
		 * inside a method of an inner prototype that is not 'eval', 'eval:eval: ...'
		 */

		VariableDecInterface varDec;
		if ( NameServer.isNameInnerProtoForContextFunction(currentProgramUnit.getName()) && 
				env.getCurrentMethod().getName().equals(NameServer.bindToFunctionWithParamNumber)	) {
			/*
		           The identifiers visible inside the function body are those declared in the function itself, those 
		           accessible through {\tt T}, external parameters, and local variables preceded by {\tt \%} (in this 
		           order --- the order is important if, for example, {\tt T} declares an unary method {\tt unMeth} 
		           and there is a local variable with this same name). 							 
			 */
			varDec = env.searchLocalVariableParameter(name);
			if ( varDec != null ) {
				if ( varDec instanceof StatementLocalVariableDec ) {
					StatementLocalVariableDec vd = (StatementLocalVariableDec ) varDec;
					if ( ! vd.getDeclaringFunction().isContextFunction() ) {

					}
				}
			}
			ArrayList<MethodSignature>  methodSignatureList;
			if ( varDec == null ) {
				String protoName = currentProgramUnit.getName();
				/*
				 * if the method is bindToFunction  in a context function, all message sends
				 * to self are considered as message sends to newSelf__ whose type is
				 * the type of the first parameter of bindToFunction
				 */
				if ( NameServer.isNameInnerProtoForContextFunction(protoName) && 
						env.getCurrentMethod().getName().equals(NameServer.bindToFunctionWithParamNumber)	) {
					Type t = env.getCurrentMethod().getMethodSignature().getParameterList().get(0).getType();
					if ( t == null ) {
						/*
						 * calculating the type of items of the return value type of method bindToFunction.
						 * So it is not necessary to search for methods. Look for prototypes (below)
						 */
						methodSignatureList = null; 
					}
					else {
						methodSignatureList = t.searchMethodPublicSuperPublic(name, env);
						
						if ( name.equals("init") ) {
							env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method", true, false);

							/*
							 // message sends to 'init' inside any methods, including 'init:' or 'init:', are currently illegal

							if ( env.getCurrentMethod() != null ) {
								String initName = env.getCurrentMethod().getNameWithoutParamNumber();
								if ( !initName.equals("init") && !initName.equals("init:") ) {
									env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods");
								}
							}
							if ( env.getFunctionStack().size() > 0 ) {
								env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions");
							}
							*/
						}
						
						
						
						if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
							MethodSignature methodSignature = methodSignatureList.get(0);
							methodSignature.calcInterfaceTypes(env);
							MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, t, null, 
									ExprReceiverKind.SELF_R, env, this.identSymbolArray.get(0));

							identStarKind = IdentStarKind.unaryMethod_t;

							type = methodSignature.getReturnType(env);
							javaName = NameServer.selfNameContextObject + "." + NameServer.getJavaNameOfUnaryMethod(name) + "()";
							if ( leftHandSideAssignment ) {
								env.error(getFirstSymbol(),
										"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", 
										true, false);
							}
						}

						/*
						 * if an unary method is not found, below there is a search for a prototype called 'name'
						 */

					}

					/*
					 * in 
					 *         fun bindToFunction: IColor newSelf__ -> UFunction<String> {
										return { (:  -> String :)
												^ colorTable[newSelf__  color]
										}
									}
							this code is searching for String in the type of newSelf__. At least t should be != null. But it is equal to null.

					 */
				}

			}
			varDec = env.searchVariableInBindToFunction(name);

		}
		else {
			varDec = env.searchVariableIn_NOT_EvalOfInnerPrototypes(name);
		}




		if ( varDec != null ) {
			if ( varDec instanceof InstanceVariableDec ) 
				identStarKind = IdentStarKind.instance_variable_t;
			else 
				identStarKind = IdentStarKind.variable_t;
			
			this.varDeclaration = varDec;
			
			type = varDec.getType();
			javaName = varDec.javaNameWithRef();
			setOriginalJavaName(varDec.getJavaName());
			
			if ( leftHandSideAssignment ) {
				
				if ( varDec instanceof InstanceVariableDec ) {
					if (((InstanceVariableDec ) varDec).isReadonly() && ! env.getCurrentMethod().isInitMethod() && ! env.getCurrentMethod().isInitOnce() ) {
						env.error(getFirstSymbol(),
								"Identifier '" + name + "' cannot be used in the left-hand side of an assignment", 
								true, false);
					}
				}
			}

		}
		else {
			/*
			 * search for an unary method in the CURRENT prototype only, which is an inner prototype.
			 */
			ArrayList<MethodSignature> methodSignatureList;
			ProgramUnit pu = currentProgramUnit;
			String protoName = currentProgramUnit.getName();
			/*
			 * if the method is bindToFunction  in a context function, all message sends
			 * to self are considered as message sends to newSelf__ whose type is
			 * the type of the first parameter of bindToFunction
			 */
			if ( NameServer.isNameInnerProtoForContextFunction(protoName) && 
					env.getCurrentMethod().getName().equals(NameServer.bindToFunctionWithParamNumber)	) {
				Type t = env.getCurrentMethod().getMethodSignature().getParameterList().get(0).getType();
				if ( t == null ) {
					/*
					 * calculating the type of items of the return value type of method bindToFunction.
					 * So it is not necessary to search for methods. Look for prototypes (below)
					 */
					methodSignatureList = null; 
				}
				else {
					methodSignatureList = t.searchMethodPublicSuperPublic(name, env);
					
					if ( name.equals("init") ) {
						env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method",
								true, false);

						/*
						 // message sends to 'init' inside any methods, including 'init:' or 'init:', are currently illegal

						
						if ( env.getCurrentMethod() != null ) {
							String initName = env.getCurrentMethod().getNameWithoutParamNumber();
							if ( !initName.equals("init") && !initName.equals("init:") ) {
								env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods");
							}
							if ( env.getFunctionStack().size() > 0 ) {
								env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions");
							}
						}
						*/
					}
					
					if ( methodSignatureList == null && (t instanceof ObjectDec) ) {
						methodSignatureList = ((ObjectDec ) t).searchInitNewMethod(name);
					}
					if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
						MethodSignature methodSignature = methodSignatureList.get(0);
						methodSignature.calcInterfaceTypes(env);
						MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, t, null, 
								ExprReceiverKind.SELF_R, env, this.identSymbolArray.get(0));

						identStarKind = IdentStarKind.unaryMethod_t;
						
						type = methodSignatureList.get(0).getReturnType(env);
						javaName = NameServer.selfNameContextObject + "." + NameServer.getJavaNameOfUnaryMethod(name) + "()";
						
						if ( leftHandSideAssignment ) {
							env.error(getFirstSymbol(),
									"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", 
									true, false);
							/*env.error(true, getFirstSymbol(),
									"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.unary_method_cannot_be_used_in_the_left_hand_side_of_an_assignment);*/
						}
					}
				}



				/*
				 * in 
				 *         fun bindToFunction: IColor newSelf__ -> UFunction<String> {
									return { (:  -> String :)
											^ colorTable[newSelf__  color]
									}
								}
						this code is searching for String in the type of newSelf__. At least t should be != null. But it is equal to null.

				 */
			}
			else {
				methodSignatureList = pu.searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env);
				
				if ( name.equals("init") ) {
					env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method",
							true, false);

					/*
					 // message sends to 'init' inside any methods, including 'init:' or 'init:', are currently illegal

					
					if ( env.getCurrentMethod() != null ) {
						String initName = env.getCurrentMethod().getNameWithoutParamNumber();
						if ( !initName.equals("init") && !initName.equals("init:") ) {
							env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods");
						}
						if ( env.getFunctionStack().size() > 0 ) {
							env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions");
						}
					}
					*/
				}
				
				if ( methodSignatureList == null && (pu instanceof ObjectDec) ) {
					methodSignatureList = ((ObjectDec ) pu).searchInitNewMethod(name);
				}

				if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
					MethodSignature methodSignature = methodSignatureList.get(0);
					methodSignature.calcInterfaceTypes(env);
					MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, pu, null, 
							ExprReceiverKind.SELF_R, env, this.identSymbolArray.get(0));
					
					identStarKind = IdentStarKind.unaryMethod_t;
					
					type = methodSignature.getReturnType(env);
					javaName = NameServer.getJavaNameOfUnaryMethod(name) + "()";
				}
			}
			if (  methodSignatureList != null && methodSignatureList.size() > 0 ) {
				if ( leftHandSideAssignment ) {
					env.error(getFirstSymbol(),
							"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", true, false);					
					/*env.error(true, getFirstSymbol(),
							"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.unary_method_cannot_be_used_in_the_left_hand_side_of_an_assignment);*/

				}
			}
			/*
			 * if an unary method is not found, below there is a search for a prototype called 'name'
			 */

		}
	}

	/**
	   @param env
	   @param leftHandSideAssignment
	   @param name
	 */
	private void calcInternalTypes_single_id_inside_method_inner_prototype_in_eval(Env env,
			boolean leftHandSideAssignment, String name) {
		/*
		 * inside an 'eval' or 'eval:eval: ...' method of an inner prototype 
		 */

		VariableDecInterface varDec = env.searchVariableInEvalOfInnerPrototypes(name);
		if ( varDec != null ) {
			
			if ( varDec instanceof InstanceVariableDec ) 
				identStarKind = IdentStarKind.instance_variable_t;
			else 
				identStarKind = IdentStarKind.variable_t;
			this.varDeclaration = varDec;
			type = varDec.getType();
			if ( varDec.getTypeWasChanged() ) {
				javaName = varDec.getJavaName(); // NameServer.getJavaName(varDec.getName());
			}
			else {
				javaName = varDec.javaNameWithRef();
			}
			setOriginalJavaName(varDec.getJavaName());

			if ( leftHandSideAssignment ) {
				
				if ( varDec instanceof InstanceVariableDec ) {
					if (((InstanceVariableDec ) varDec).isReadonly() && ! env.getCurrentMethod().isInitMethod() && ! env.getCurrentMethod().isInitOnce() ) {
						env.error(getFirstSymbol(),
						   "Identifier '" + name + "' cannot be used in the left-hand side of an assignment", 
						   true, false);
						/*env.error(true, getFirstSymbol(),
								"Identifier '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.identifier_cannot_be_used_in_the_left_hand_side_of_an_assignment);*/
					}
				}
			}

		}
		else {
			/*
			 * search for an unary method in the outer prototype
			 */

			ObjectDec outer = env.getCurrentObjectDec().getOuterObject();
			ArrayList<MethodSignature> methodSignatureList = outer.
					searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env);

			if ( name.equals("init") ) {
				
				env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method", true, false);

				/*
				 // message sends to 'init' inside any methods, including 'init:' or 'init:', are currently illegal

				
				if ( env.getCurrentMethod() != null ) {
					String initName = env.getCurrentMethod().getNameWithoutParamNumber();
					if ( !initName.equals("init") && !initName.equals("init:") ) {
						env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods");
					}
					if ( env.getFunctionStack().size() > 0 ) {
						env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions");
					}
				}
				*/
			}
			
			if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {
				methodSignatureList = outer.searchInitNewMethod(name);
			}

			if (  methodSignatureList != null && methodSignatureList.size() > 0 ) {

				MethodSignature methodSignature = methodSignatureList.get(0);
				methodSignature.calcInterfaceTypes(env);

				MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, outer, null, 
						ExprReceiverKind.SELF_R, env, this.identSymbolArray.get(0));

				identStarKind = IdentStarKind.unaryMethod_t;
				
				type = methodSignature.getReturnType(env);
				javaName = NameServer.javaSelfNameInnerPrototypes + "." + NameServer.getJavaNameOfUnaryMethod(name) + "()";

				if ( leftHandSideAssignment ) {
					env.error(getFirstSymbol(),
							"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", true, false);					
					/*env.error(true, getFirstSymbol(),
							"Unary method '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.unary_method_cannot_be_used_in_the_left_hand_side_of_an_assignment);*/
				}
			}
			/*
			 * if an unary method is not found, below there is a search for a prototype called 'name'
			 */

		}
	}

	/**
	   @param env
	   @param leftHandSideAssignment
	   @param currentProgramUnit
	   @param name
	 */
	private void calcInternalTypes_single_id_inside_method_outer_prototype(Env env, boolean leftHandSideAssignment,
			ProgramUnit currentProgramUnit, String name) {
		/*
		 * inside a regular prototype that is NOT inside another prototype
		 */
		VariableDecInterface varDec = env.searchVariable(name);
		if ( varDec != null ) {
			
			if ( varDec instanceof InstanceVariableDec ) 
				identStarKind = IdentStarKind.instance_variable_t;
			else 
				identStarKind = IdentStarKind.variable_t;
			
			this.varDeclaration = varDec;
			type = varDec.getType();
			javaName = varDec.javaNameWithRef();
			
			if ( (varDec instanceof InstanceVariableDec) && ! ((InstanceVariableDec) varDec).isShared() && 
					! env.getCurrentMethod().getAllowAccessToInstanceVariables() ) {
				/*
				 * access to instance variables is not allowed
				 */
				env.error(this.getFirstSymbol(), "Instance variables are not allowed in this method. Probable cause: "
						+ "metaobject 'prototypeCallOnly' is attached to it", true, false
						);
			}

			if ( leftHandSideAssignment ) {
				
				if ( varDec instanceof InstanceVariableDec ) {
					if (((InstanceVariableDec ) varDec).isReadonly() && ! env.getCurrentMethod().isInitMethod() &&
							! env.getCurrentMethod().isInitOnce()
							) {
						env.error(getFirstSymbol(),
								"Identifier '" + name + "' cannot be used in the left-hand side of an assignment", true, false);
						/*env.error(true, getFirstSymbol(),
								"Identifier '" + name + "' cannot be used in the left-hand side of an assignment", name, ErrorKind.identifier_cannot_be_used_in_the_left_hand_side_of_an_assignment);*/
					}
				}
			}
			else if ( identStarKind == IdentStarKind.instance_variable_t  && ! ((InstanceVariableDec) varDec).isShared()   ) {
				String currentMethodName = env.getCurrentMethod().getNameWithoutParamNumber();
				
				if ( (currentMethodName.equals("init") || currentMethodName.equals("init:")) && 
						! ((InstanceVariableDec ) this.varDeclaration).getWasInitialized() ) {
					env.error(this.getFirstSymbol(),  "Variable '" + varDec.getName() + "' may not have been initialized. "
							+ "The assignment to it should in the top level method statements. It cannot "
							+ "be inside an 'if' statement, for example", true, false);
				}
				else  
				if ( currentMethodName.equals("initOnce") ) {
					env.error( this.getFirstSymbol(),  "Illegal access to an instance variable in an expression inside an 'initOnce' method",
							true, false );
				}
				
			}

		}
		else {
			/*
			 * search for an unary method
			 */
			MethodSignature methodSignature;
			ArrayList<MethodSignature> methodSignatureList = currentProgramUnit.searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env);
			
			if ( name.equals("init") ) {
				env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method", true, false);
			}
			
			
			if ( methodSignatureList == null ) {
				if ( currentProgramUnit instanceof ObjectDec ) {
					methodSignatureList = ((ObjectDec ) currentProgramUnit).searchInitNewMethod(name);
				}
			}
			if ( methodSignatureList != null  && methodSignatureList.size() > 0) {
				// found an unary method

				String currentMethodName = env.getCurrentMethod().getNameWithoutParamNumber();
				if ( currentMethodName.equals("init") || currentMethodName.equals("init:") ) {
					
					String nameiv = "_" + name;
					InstanceVariableDec unaryMethodList = ((ObjectDec ) currentProgramUnit).searchInstanceVariable(nameiv);
					if ( unaryMethodList != null ) {
						env.error(getFirstSymbol(), "Message send to 'self' inside an 'init' or 'init:' method." + 
					      " Since there is an instance variable '" + nameiv + "', you probably wanted to write it instead of '" + name + "'",
					      true, false);
					}
					else {
						env.error(this.getFirstSymbol(),  "Message send to 'self' inside an 'init' or 'init:' method. "
								+ "This is illegal because it can call a "
								+ " subprototype method and this method can access an instance variable that has not been initialized", true, false);
					}
					
				}
				
				methodSignature = methodSignatureList.get(0);
				methodSignature.calcInterfaceTypes(env);

				MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, currentProgramUnit, null, 
						ExprReceiverKind.SELF_R, env, this.identSymbolArray.get(0));

				identStarKind = IdentStarKind.unaryMethod_t;
				
				type = methodSignature.getReturnType(env);
				javaName = NameServer.getJavaName(name) + "()";
			}

		}
	}

	/**
	   @param env
	   @param currentProgramUnit
	   @param name
	 */
	private void calcInternalTypes_single_id_outside_method(Env env, ProgramUnit currentProgramUnit, String name) {
		ProgramUnit programUnit;
		// outside a method
		
		
		VariableDecInterface varDec = env.searchVariable(name);
		if ( varDec != null ) {
			// found an instance variable 
			
			if ( varDec instanceof InstanceVariableDec ) 
				identStarKind = IdentStarKind.instance_variable_t;
			else 
				identStarKind = IdentStarKind.variable_t;
			
			this.varDeclaration = varDec;

			type = varDec.getType();
			javaName = varDec.getJavaName();
		}
		else {
			
			/*
			 * search for an unary method
			 */
			ArrayList<MethodSignature> methodSignatureList;
			
			
			if ( ! calcInternalTypesUnaryMethod(env, currentProgramUnit, name) ) { 
				programUnit = env.searchPrivateProgramUnit(name);
				if ( programUnit == null )
					programUnit = env.searchVisibleProgramUnit(name, this.getFirstSymbol(), false);
				
				// try to find a prototype
				if ( programUnit != null ) {
					identStarKind = IdentStarKind.prototype_t;
					
					type = programUnit;
					// foundIdent = true;
					if ( programUnit instanceof InterfaceDec ) {
						javaName = NameServer.getJavaName(NameServer.prototypeFileNameFromInterfaceFileName(programUnit.getName())) + ".prototype";
					}
					else
						javaName = NameServer.getJavaName(name) + ".prototype";
				}
				else {
					if ( currentProgramUnit instanceof ObjectDec ) {
						ObjectDec proto = (ObjectDec ) currentProgramUnit;
						methodSignatureList = proto.searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env);
						
						if ( name.equals("init") ) {
							if ( env.getCurrentMethod() != null ) {
								String initName = env.getCurrentMethod().getNameWithoutParamNumber();
								if ( !initName.equals("init") && !initName.equals("init:") ) {
									env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods", true, false);
								}
								if ( env.getFunctionStack().size() > 0 ) {
									env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions", true, false);
								}
							}
						}
						
						
						if ( methodSignatureList.size() > 0 ) {
							env.error(getFirstSymbol(),
									"Method '" + name + "' cannot be called here", true, false);
							/*
							env.error(true, getFirstSymbol(),
									"Method '" + name + "' cannot be called here", name, ErrorKind.method_is_not_visible_here);*/
						}
						else {
							InstanceVariableDec instVar = proto.searchInstanceVariablePrivateProtectedSuperProtected(name);
							if ( instVar != null ) {
								env.error(getFirstSymbol(),
										"Instance variable '" + name + "' is not visible here", true, false);								
								/*env.error(true, getFirstSymbol(),
										"Instance variable '" + name + "' is not visible here", name, ErrorKind.instance_variable_is_not_visible_here);*/
							}
							else {
								env.error(getFirstSymbol(),
										"Identifier '" + name + "' was not declared", true, false);
								/*
								env.error(true, getFirstSymbol(),
										"Identifier '" + name + "' was not declared", name, ErrorKind.identifier_was_not_declared);

								 * 
								 */
							}
						}
					}
					else {
						env.error(getFirstSymbol(),
								"Identifier '" + name + "' was not declared", true, false);
						/*
						 * env.error(true, getFirstSymbol(),
							"Identifier '" + name + "' was not declared", name, ErrorKind.identifier_was_not_declared);
						 */
					}
				}
			}
		}
	}

	/**
	   @param env
	   @param currentProgramUnit
	   @param name
	 */
	private boolean calcInternalTypesUnaryMethod(Env env, ProgramUnit currentProgramUnit, String name) {
		ObjectDec currentObjectDec = null;
		ArrayList<MethodSignature> methodSignatureList = null;
		
		if ( currentProgramUnit instanceof ObjectDec ) {
			
			currentObjectDec = (ObjectDec ) currentProgramUnit;
			MethodSignature methodSignature;
			methodSignatureList = currentObjectDec.searchMethodPrivateProtectedPublicSuperProtectedPublic(name, env);
			
			if ( name.equals("init") ) {
				env.error(this.getFirstSymbol(), "'init' and 'init:' messages can only be sent to 'super' inside an 'init' or 'init:' method", 
						true, false);

				/*
				 // message sends to 'init' inside any methods, including 'init:' or 'init:', are currently illegal

				
				if ( env.getCurrentMethod() != null ) {
					String initName = env.getCurrentMethod().getNameWithoutParamNumber();
					if ( !initName.equals("init") && !initName.equals("init:") ) {
						env.error(this.getFirstSymbol(), "'init' and 'init:' methods can only be called inside other 'init' or 'init:' methods");
					}
					if ( env.getFunctionStack().size() > 0 ) {
						env.error(this.getFirstSymbol(), "'init' and 'init:' messages cannot be sent inside anonymous functions");
					}
				}
				*/
			}
			
			
			if ( methodSignatureList == null ) {
				methodSignatureList = currentObjectDec.searchInitNewMethod(name);
			}
			if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
				// found an unary method
				
				methodSignature = methodSignatureList.get(0);
				methodSignature.calcInterfaceTypes(env);

				MetaInfoServer.checkMessageSendWithMethodMetaobject(methodSignatureList, currentObjectDec, null, 
						ExprReceiverKind.SELF_R, env, this.identSymbolArray.get(0));
				
				identStarKind = IdentStarKind.unaryMethod_t;
				
				type = methodSignature.getReturnType(env);
				javaName = NameServer.getJavaName(name) + "()";
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public saci.Tuple<String, CompilationUnit> returnsNameWithPackage(Env env)  {
		Tuple<String, Type> t =  ifPrototypeReturnsNameWithPackageAndType(env);
		if ( t == null || !(t.f2 instanceof ProgramUnit)) {
			return null;
		}
		else {
			ProgramUnit pu = (ProgramUnit ) t.f2;
			return new Tuple<String, CompilationUnit>(t.f1, pu.getCompilationUnit());
		}
	}
	
	
	@Override
	public saci.Tuple<String, Type> ifPrototypeReturnsNameWithPackageAndType(Env env) {
		
		if ( nameWithPackageAndType == null ) {
			String name = getName();
			//if ( Character.isLowerCase(name.charAt(0)) )
			//	return new saci.Tuple<String, CompilationUnit>(name, null);

			name = name.replace(NameServer.cyanLanguagePackageName + ".", "");
			int indexOfDot = name.lastIndexOf('.');
			if ( indexOfDot < 0 && Character.isLowerCase(name.charAt(0)) )
				  // symbol as 'joule' in Union<joule, Float, calorie, Float>
				nameWithPackageAndType = new saci.Tuple<String, Type>(name, null);
			
			// name has a package OR it starts with an upper-case letter
			
			if ( indexOfDot < 0 ) {
				  // no package preceding the name. It should be a prototype visible in 
					  // this compilation unit
				ProgramUnit pu3 = env.searchVisibleProgramUnit(name, this.getFirstSymbol(), true);
				if ( pu3 == null ) {
					if ( name.equals(NameServer.dynName) ) {
						nameWithPackageAndType = new saci.Tuple<String, Type>(name, Type.Dyn);
					}
					else {
						nameWithPackageAndType = new saci.Tuple<String, Type>(name, null);
					}
				}
				else {
					if ( pu3.getOuterObject() != null ) {
						// name = pu3.getOuterObject().getName() + "." + name;
						  // an inner prototype "Fun_7__" should have only the name "Fun_7__", without any package
						nameWithPackageAndType = new saci.Tuple<String, Type>(name, pu3);
					}
					else {
						String packageName = pu3.getCompilationUnit().getPackageName();
						if ( packageName.equals(NameServer.cyanLanguagePackageName) )
							   // do not put cyan.lang in the return value
							nameWithPackageAndType = new saci.Tuple<String, Type>(name, pu3);
						else 
							nameWithPackageAndType = new saci.Tuple<String, Type>(packageName + "." + name, pu3);
					}
				}
			}
			else {
				// package name
				String prototypeName = name.substring(indexOfDot + 1);
				String packageName = name.substring(0, indexOfDot);
				ProgramUnit pu4 = env.searchPackagePrototype(packageName, prototypeName);
				if ( pu4 == null ) {
					nameWithPackageAndType = null;
				}
				else
					nameWithPackageAndType = new saci.Tuple<String, Type>(packageName + "." + prototypeName, pu4);
			}
					
		}
		return nameWithPackageAndType;
	}
	
	
	
	public IdentStarKind getIdentStarKind() {
		return identStarKind;
	}



	public MessageSendToMetaobjectAnnotation getMessageSendToMetaobjectAnnotation() {
		return messageSendToMetaobjectAnnotation;
	}

	public void setMessageSendToMetaobjectAnnotation(MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation) {
		this.messageSendToMetaobjectAnnotation = messageSendToMetaobjectAnnotation;
	}
	
	/**
	 * if this qualified name represents a prototype preceded by a package name, return the package name. 
	 * Otherwise return null
	 */
	@Override
	public String getPackageName() {
		if ( this.identSymbolArray.size() <= 1 ) 
			return null;
		else {
			String s = "";
			int i = 0;
			while ( i < identSymbolArray.size() - 1 ) {
				s = s + identSymbolArray.get(i).getSymbolString();
				++i;
			}
			return s;
		}
	}	

	
	public String getOriginalJavaName() {
		if ( originalJavaName == null )
			return javaName;
		else 
			return originalJavaName;
	}

	public void setOriginalJavaName(String originalJavaName) {
		this.originalJavaName = originalJavaName;
	}
	
	/**
	 * if this qualified name represents a prototype preceded by a package name, return the prototype name. 
	 * Otherwise return null
	 */
	@Override
	public String getPrototypeName() {
		return this.identSymbolArray.get(this.identSymbolArray.size()-1).getSymbolString();
		
	}
	
	public void setRefType(boolean refType) {
		varDeclaration.setRefType(refType);
		javaName = varDeclaration.javaNameWithRef();
	}
	

	@Override
	public Symbol getNextSymbol() {
		return nextSymbol;
	}



	@Override
	public void setNextSymbol(Symbol nextSymbol) {
		this.nextSymbol = nextSymbol;
	}
	
	
	
	/**
	 * true if this is a variable that was preceded by symbol % used inside functions for "COPY_VAR access"
	 */
	private boolean precededByRemaninder;

	/**
	 * the java name of this identifier
	 */
	private String javaName;
	/**
	 * the original Java name for this identifier
	 */
	private String originalJavaName;

	/**
	 * the kind of this qualified identifier: variable, prototype, or unary method
	 */
	private IdentStarKind identStarKind;
	
	/**
	 * name with package and type. Only used if this expression is a type
	 */
	private saci.Tuple<String, Type> nameWithPackageAndType;
	/**
	 * if this expression is an instance variable, parameter, or local variable, varDeclaration points to it
	 */
	private VariableDecInterface varDeclaration;


	public VariableDecInterface getVarDeclaration() {
		return varDeclaration;
	}
	protected ArrayList<Symbol> identSymbolArray;


	/**
	 * message send at compile time attached to this qualified expression. It is only valid if 
	 * this expression represents a prototype. Example: <br>
	 * {@code var Person.#writeCode  p;}<br>
	 */
	private MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation;
	
	private Symbol nextSymbol;

	
}
