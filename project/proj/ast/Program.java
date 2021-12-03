/**
 *
 */
package ast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import error.CompileErrorException;
import error.ErrorKind;
import error.UnitError;
import lexer.Symbol;
import lexer.Token;
import meta.CompilerManager_ati;
import meta.CompilerPackageView_ati;
import meta.CompilerProgramView_ati;
import meta.Compiler_ati;
import meta.Compiler_dsa;
import meta.CyanMetaobject;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgram_ati;
import meta.ICheckDeclaration_ati2;
import meta.ICheckDeclaration_ati3;
import meta.ICheckProgram_ati3;
import meta.ICheckProgram_dsa2;
import meta.ICommunicateInPackage_ati_dsa;
import meta.ICompilerProgramView_ati;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import meta.IListAfter_ati;
import saci.CompilationInstruction;
import saci.CompilationStep;
import saci.Env;
import saci.MyFile;
import saci.NameServer;
import saci.Project;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple4;
import saci.Tuple6;
import saci.Tuple7;

/**
 * represents a Cyan program
 * @author José
 *
 */
public class Program implements ASTNode, Declaration {

	public Program() {
		inCalcInterfaceTypes = false;
		packageList = new ArrayList<>();
		this.jvmPackageList = new ArrayList<>();
		compilationUnitList = new ArrayList<>();
		receiverToWriteList = new HashSet<>();
		jvmTypeJavaList = new HashMap<>();
	}

	@Override
	public void accept(ASTVisitor visitor) {

		visitor.preVisit(this);
		for ( CyanPackage cp : this.packageList ) {
			cp.accept(visitor);
		}
		for ( JVMPackage cp : this.jvmPackageList ) {
			cp.accept(visitor);
		}
		visitor.visit(this);
	}


	/**
	 * reset the state of all compilation units eliminating any traces they have already been compiled.
	 */
	public void reset() {
		init();
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			compilationUnit.reset();
		}
	}

	/**
	 * reset the state of all non-generic compilation units eliminating any traces they have already been compiled.
	 */
	public void resetNonGeneric() {
		init();
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			if ( ! compilationUnit.getHasGenericPrototype() ) {
				compilationUnit.reset();
			}
		}
	}



	/**
	 * prepare this program to be compiled
	 */
	public void init() {

		/** For each package, insert its compilation units (source code) into a list
		 * of compilation units.
		 */
		compilationUnitList.clear();
		for ( CyanPackage ps : packageList ) {
			for ( CompilationUnit compilationUnit : ps.getCompilationUnitList() ) {
				compilationUnitList.add(compilationUnit);
				compilationUnit.setProgram(this);
			}
		}
		if ( this.receiverToWriteList != null )
			this.receiverToWriteList.clear();
	}

	public void addCompilationUnit(CompilationUnit compilationUnit) {
		compilationUnitList.add(compilationUnit);
		compilationUnit.setProgram(this);
	}

	/** Types appear in the AST as objects of ExprIdentStar or ExprGenericPrototypeInstantiation.
	 *  This method sets the "type" instance variable of several AST objects to the
	 *  real ProgramUnit (prototype, interface, or Java class) that is being represented.
	 *  That is, if the ExprIdentStar object is "Person", this method sets the "type"
	 *  instance variable of this object to the program unit that is "Person".
	 *  But not all AST objects are changed. This method only sets the "type" variable
	 *  of objects of the AST representing method parameters and return values (including
	 *  private and protected ones), inherited prototypes and interfaces (an interface may
	 *  inherit from another interface), implemented interfaces, types of generic parameters
	 *  (as Person in "object Stack<Person T> ... end"), mixin types (as Person in
	 *  "mixin(Person) object Comparison ... end"), types of context parameters
	 *  (as Int in "object Sum(Int s) ... end"), types in mixin inheritance
	 *  (as Readable and Writable in "object File mixin Readable, Writable ... end"),
	 *  and instance variables.
	 *
	 *  Then not all expressions have their types calculated. For example, the types
	 *  of local variables and expressions are not set.
	 *
	 * @param env
	 */
	public void calcInterfaceTypes(Env env) {




		this.inCalcInterfaceTypes = true;



		/*
		 * cannot use 'for' command here because compilation units are added to compilationUnitList
		 */
		int i = 0;
		while ( i < compilationUnitList.size() ) {
			// int mySize = compilationUnitList.size();
			CompilationUnit compilationUnit = compilationUnitList.get(i);
			if ( ! compilationUnit.getHasGenericPrototype() )
				try {
					compilationUnit.calcInterfaceTypes(env);
				}
				catch ( CompileErrorException e ) {
				}
			++i;
		}
		/*
		 * calculate the types of the expressions that are parameters to the metaobjects in the project file
		 */
		if ( this.getProject().getCompilerManager().getCompilationStep() == CompilationStep.step_2 ) {
			/*			ProgramUnit puAny = env.searchPackagePrototype(NameServer.cyanLanguagePackageName, "Any");
			if ( puAny == null ) {
				env.error(null, "Internal error: 'Any' was not found");
				return ;
			}
			*/
			int oldSizeList = compilationUnitList.size();
			CompilationUnit anyCompilationUnit = ((ProgramUnit ) Type.Any).getCompilationUnit();
			/*
			 * do all the typing in the context of Any. It does not matter which
			 * prototype of cyan.lang is chosen. This is just to avoid a NPE.
			 */
			env.atBeginningOfCurrentCompilationUnit(anyCompilationUnit);
			if ( attachedMetaobjectAnnotationList != null ) {
				for ( CyanMetaobjectWithAtAnnotation annotation : this.attachedMetaobjectAnnotationList ) {
					annotation.calcInternalTypes(env);
				}
			}
			if ( nonAttachedMetaobjectAnnotationList != null ) {
				for ( CyanMetaobjectWithAtAnnotation annotation : this.nonAttachedMetaobjectAnnotationList ) {
					annotation.calcInternalTypes(env);
				}
			}
			for ( CyanPackage cp : this.packageList ) {
				if ( cp.getAttachedMetaobjectAnnotationList() != null ) {
					for ( CyanMetaobjectWithAtAnnotation annotation : cp.getAttachedMetaobjectAnnotationList() ) {
						annotation.calcInternalTypes(env);
					}
				}
				if ( cp.getNonAttachedMetaobjectAnnotationList() != null ) {
					for ( CyanMetaobjectWithAtAnnotation annotation : cp.getNonAttachedMetaobjectAnnotationList() ) {
						annotation.calcInternalTypes(env);
					}
				}
			}
			env.atEndOfCurrentCompilationUnit();
			/*
			 * new generic prototype instantiations may have been introduced to this list.
			 * Then it is necessary to calculate their interfaces
			 */
			i = oldSizeList;
			while ( i < compilationUnitList.size() ) {
				// int mySize = compilationUnitList.size();
				CompilationUnit compilationUnit = compilationUnitList.get(i);
				if ( ! compilationUnit.getHasGenericPrototype() )
					try {
						compilationUnit.calcInterfaceTypes(env);
					}
					catch ( CompileErrorException e ) {
					}
				++i;
			}

		}



		if ( env.getCompInstSet().contains(CompilationInstruction.ati3_check) ) {


			/**
			 * check whether the overloaded methods were correctly defined.
			 */
			calculateInterfaceChecks(env);

			ICompiler_ati compiler_ati = new Compiler_ati(env);
			Env newEnv = (Env ) compiler_ati.getEnv();
			for ( CompilationUnit cunit : compilationUnitList ) {
				if ( cunit.hasGenericPrototype() ) {
					continue;
				}
				newEnv.atBeginningOfCurrentCompilationUnit(cunit);
				for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
					newEnv.atBeginningOfObjectDec(pu);
					for ( CyanMetaobjectAnnotation cyanMetaobjectAnnotation : pu.getCompleteMetaobjectAnnotationList() ) {

						CyanMetaobject cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();

						/**
						 * macros do not have an object for "metaobject annotation". Then only macros will
						 * have "cyanMetaobject == null".
						 */
						if ( cyanMetaobject != null ) {

							cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);

							if ( cyanMetaobject instanceof ICheckDeclaration_ati3 ) {

								try {
									((ICheckDeclaration_ati3) cyanMetaobject).ati3_checkDeclaration(compiler_ati);
								}
								catch ( error.CompileErrorException e ) {
								}
								catch ( RuntimeException e ) {
									e.printStackTrace();
									env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
								}
								finally {
									env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
								}

							}
						}
					}
					newEnv.atEndOfObjectDec();
				}
				newEnv.atEndOfCurrentCompilationUnit();
			}

		}


		if ( env.getCompInstSet().contains(CompilationInstruction.ati2_check) ) {


			ICompiler_ati compiler_ati = new Compiler_ati(env);
			Env newEnv = (Env ) compiler_ati.getEnv();
			for ( CompilationUnit cunit : compilationUnitList ) {
				if ( cunit.hasGenericPrototype() ) {
					continue;
				}
				newEnv.atBeginningOfCurrentCompilationUnit(cunit);
				for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
					newEnv.atBeginningOfObjectDec(pu);
					for ( CyanMetaobjectAnnotation cyanMetaobjectAnnotation : pu.getCompleteMetaobjectAnnotationList() ) {

						CyanMetaobject cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();

						/**
						 * macros do not have an object for "metaobject annotation". Then only macros will
						 * have "cyanMetaobject == null".
						 */
						if ( cyanMetaobject != null ) {

							cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);

							if ( cyanMetaobject instanceof ICheckDeclaration_ati2 ) {

								try {
									((ICheckDeclaration_ati2) cyanMetaobject).ati2_checkDeclaration(compiler_ati);
								}
								catch ( error.CompileErrorException e ) {
								}
								catch ( RuntimeException e ) {
									// e.printStackTrace();
									env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
								}
								finally {
									env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
								}


							}
						}
					}
					newEnv.atEndOfObjectDec();
				}
				newEnv.atEndOfCurrentCompilationUnit();
			}

		}

		/* if ( env.getProject().getCompilerManager().getCompilationStep().ordinal() < CompilationStep.step_7.ordinal() ) {
			afterATImetaobjectAnnotationList = null;
		} */
		if ( afterATImetaobjectAnnotationList != null && env.getProject().getCompilerManager().getCompilationStep() == CompilationStep.step_8 ) {

			ICompiler_ati compiler_ati = new Compiler_ati(env);

			for ( IListAfter_ati metaobject : afterATImetaobjectAnnotationList ) {
				Env newEnv = (Env ) compiler_ati.getEnv();
				CompilationUnit cunit = metaobject.getCompilationUnit();
				newEnv.atBeginningOfCurrentCompilationUnit(cunit);


				try {
					cunit.prepareGenericCompilationUnit(newEnv);

					metaobject.after_ati_action(compiler_ati);

					newEnv.atEndOfCurrentCompilationUnit();
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					e.printStackTrace();
					env.thrownException( ((CyanMetaobject ) metaobject).getMetaobjectAnnotation(),
							(((CyanMetaobject ) metaobject).getMetaobjectAnnotation()).getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions((CyanMetaobject ) metaobject, ((CyanMetaobject ) metaobject).getMetaobjectAnnotation());
					newEnv.setProgramUnitForGenericPrototypeList(null);
				}


			}
		}



		this.inCalcInterfaceTypes = false;

	}


	/**
	   @param env
	 */
	private void calculateInterfaceChecks(Env env) {



		for ( CompilationUnit cunit : this.compilationUnitList ) {

			try {
				calculateInterfaceChecksEachCompilationUnit(env, cunit);
			}
			catch ( CompileErrorException e ) {
			}

		}
		/*
		 * call method 'check' of every metaobject attached to a program unit or a method that implements
		 * interface ICheck_cin. But only in step 5 of the compilation
		 */
		for ( CompilationUnit cunit : this.compilationUnitList ) {
			for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
				try {
					pu.check_cin(env);
				}
				catch ( CompileErrorException e ) {
				}
			}
		}
	}

	/*
	@SuppressWarnings("static-method")
	private void checkAbstractMethodsWereDeclared(Env env, CompilationUnit cunit) {
		for ( ProgramUnit pu : cunit.getProgramUnitList() )  {
			if ( pu instanceof ObjectDec ) {
				ObjectDec proto = (ObjectDec ) pu;

			}
		}
	}
	*/

	@SuppressWarnings("static-method")
	private void checkAbstractMethodsWereDeclared(Env env, ObjectDec proto, ObjectDec superProto) {
		if ( proto != Type.Any && proto != Type.Nil ) {
			// ArrayList<MethodDec> methodList = new ArrayList<MethodDec>();
			if ( ! proto.getIsAbstract() && superProto.getIsAbstract() ) {
				/*
				 * 'proto' is not abstract but 'superProto' is. Then
				 * 'proto' should define all abstract methods inherited from 'superProto'
				 * and all its super-prototypes
				 */
				ObjectDec p = superProto;
				while ( true ) {
					ArrayList<MethodDec> superAbstractMethodList = p.getAbstractMethodList();
					if ( superAbstractMethodList.size() > 0 ) {
						for ( MethodDec abstractMethod : superAbstractMethodList ) {
							/*
							 * check if this abstract method is defined in 'proto'
							 */
							String abstractMethodName = abstractMethod.getName();
							ArrayList<MethodSignature> methodList = proto.searchMethodProtectedPublicSuperProtectedPublic(abstractMethodName, env);
							boolean found = false;
							if ( methodList != null && methodList.size() > 0 ) {
								// check if the any of the methods is not abstract
								for (int j = methodList.size()-1; j >= 0; --j) {
									if ( ! methodList.get(j).getMethod().isAbstract() ) {
										found = true;
										break;
									}
								}
							}

							if ( ! found ) {
								try {
									env.error(proto.getFirstSymbol(), "Abstract method '" + abstractMethod.getName() + "' inherited from "
											+ "prototype '" + p.getFullName() + "' is not defined in prototype '" + proto.getName() + "'");
								}
								catch (CompileErrorException e ) {
								}
							}
						}
					}

					p = p.getSuperobject();
					if ( p == null || p == Type.Any ) {
						break;
					}
				}

			}

		}

	}

	/**
	 * check whether unary methods are preceded by keyword 'override'
	   @param env
	   @param proto
	   @param superProto
	 */
	@SuppressWarnings("static-method")
	private void checkUnaryMethods(Env env, ObjectDec proto, ObjectDec superProto) {

		if ( superProto == null )
			return ;
		for (MethodDec method : proto.getMethodDecList() ) {

			MethodSignature methodMS = method.getMethodSignature();
			if ( methodMS instanceof MethodSignatureUnary) {
				String methodName = methodMS.getName();
				ArrayList<MethodSignature> msList = superProto.searchMethodProtectedPublicSuperProtectedPublic(methodName, env);
				if ( msList != null && msList.size() > 0 ) {
					if ( ! method.getHasOverride() ) {
						String superName;
						if ( msList.get(0).getMethod() != null ) {
							superName = msList.get(0).getMethod().getDeclaringObject().getFullName();
							env.error(method.getFirstSymbol(),  "Method '" + methodName + "' overrides a method of super-prototype '" +
									superName + "'. It should be preceded by keyword 'override'");
						}
						else {
							superName = msList.get(0).getDeclaringInterface().getFullName();
							env.error(method.getFirstSymbol(),  "Method '" + methodName + "' overrides a method of interface '" +
									superName + "'. It should be preceded by keyword 'override'");
						}
					}
				}
			}
		}
	}
	/**
	   @param env
	   @param cunit
	 */
	@SuppressWarnings("static-method")
	private void calculateInterfaceChecksEachCompilationUnit(Env env, CompilationUnit cunit) {

		if ( ! cunit.getHasGenericPrototype() ) {

			env.atBeginningOfCurrentCompilationUnit(cunit);

			HashSet<Integer> alreadCheckedList = new HashSet<>();
			for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
				if ( pu instanceof ObjectDec ) {
					ObjectDec proto = (ObjectDec ) pu;
					ObjectDec superProto = proto.getSuperobject();


					checkAbstractMethodsWereDeclared(env, proto, superProto);
					checkUnaryMethods(env, proto, superProto);

					alreadCheckedList.clear();
					for (MethodDec method : proto.getMethodDecList() ) {

						MethodSignature methodMS = method.getMethodSignature();
						/*
						 * unary methods need not to be checked
						 */
						if ( (methodMS instanceof MethodSignatureUnary) || ((methodMS instanceof MethodSignatureOperator)
								&& methodMS.getParameterList() == null) )
							continue;

						int methodNumber = method.getMethodNumber();

						if ( ! alreadCheckedList.contains(methodNumber) ) {
							alreadCheckedList.add(methodNumber);

							String methodName = method.getName();
							ArrayList<MethodSignature> mspppList = proto.searchMethodPrivateProtectedPublic(methodName);
							ArrayList<Tuple2<Symbol, Integer>> tList = new ArrayList<>();
							for ( MethodSignature ms : mspppList ) {
								MethodDec other = ms.getMethod();
								int otherNumber = other.getMethodNumber();
								tList.add( new Tuple2<Symbol, Integer>(other.getFirstSymbol(), otherNumber));
							}
							/*
							 * check whether all methods with the same name are textually near each other
							 */
							if ( tList.size() > 1 ) {
								for ( Tuple2<Symbol, Integer> t : tList ) {
									boolean ok = false;
									for ( Tuple2<Symbol, Integer> w : tList ) {
										if ( t.f2 + 1 == w.f2 || t.f2 - 1 == w.f2 )
											ok = true;
									}
									if ( ! ok ) {
										env.error(t.f1, "Method declared at line " + t.f1.getLineNumber() + " is part of a overloaded method. Therefore it should be declared "
												+ " adjacent to a method with the same name");
									}
								}
							}

							ArrayList<MethodSignature> msList_PPP_This_And_SuperProto = proto.searchMethodPrivateProtectedPublicSuperProtectedPublic(methodName, env);
							ArrayList<MethodSignature> pmsList_PP_This_Proto = proto.searchMethodProtectedPublic(methodName);
							ArrayList<MethodSignature> interMSList_Impl_Interfaces = proto.searchMethodImplementedInterface(methodName, env);

							if ( method.getPrecededBy_overload() ) {

								/*

		                            \item no method with the same name should have been declared textually before it in the prototype hierarchy. That includes {\tt P} and its super-prototypes. That is, {\tt m} should not override a super-prototype method;
		                            \item no interface implemented by {\tt P} should declare a method with the same name as {\tt m};
		                            \item no method with the same name in the prototype should be abstract;
		                            \item if the method is final, all methods with the same name should be final too. If it is not final, no method with the same name can be final;
		                            \item the return value type of all methods with the same name as {\tt m} in {\tt P} should be the same. 							 *
								 */
								/*
								 * method declaration is preceded by 'overload' as in
								 *      overload
								 *      func print: String s { ... }
								 */
								boolean isFinal = method.getIsFinal();
								Type returnValueType = method.getMethodSignature().getReturnType(env);
								ArrayList<MethodDec> overloadMethodList = new ArrayList<>();
								for ( MethodSignature ms : mspppList ) {
									MethodDec other = ms.getMethod();

		                            // no method with the same name should have been declared textually before it in the prototype hierarchy. That includes {\tt P} and its super-prototypes. That is, {\tt m} should not override a super-prototype method;

									other.setOverload(true);
									overloadMethodList.add(other);
									alreadCheckedList.add(ms.getMethod().getMethodNumber());

									int otherNumber = other.getMethodNumber();
									if ( otherNumber < methodNumber ) {
										/*
										 * another method was textually declared before the method preceded by 'overload' as in
										 *     object A
										 *         fun print: String s { ... }
										 *         overload
										 *         fun print: Int n { ... }
										 *     end
										 */
										env.error(method.getFirstSymbol(),  "Keyword 'overload' should be used in the first textually "
												+ " declared method with this name in this prototype"
											     );
									}
									if ( otherNumber > methodNumber && other.getPrecededBy_overload() ) {
										/*
										 * another method with 'overload' was textually declared before the method preceded by 'overload' as in
										 *     object A
										 *     		overload
										 *          fun print: String s { ... }
										 *          overload
										 *          fun print: Int n { ... }
										 *     end
										 */
										env.error(other.getFirstSymbol(),  "Keyword 'overload' should be used ONLY in the first textually "
												+ " declared method with this name in this prototype"
											     );
									}


									if ( other.getVisibility() != Token.PUBLIC ) {
										env.error(other.getFirstSymbol(),  "This method is part of a overloaded method. Therefore it should be 'public'");
									}
									//  no method with the same name in the prototype should be abstract
									if ( other.isAbstract() )
										env.error(other.getFirstSymbol(),  "This method is part of a overloaded method. Therefore it cannot be abstract");
									// if the method is final, all methods with the same name should be final too. If it is not final, no method with the same name can be final;
									if ( other.getIsFinal() != isFinal ) {
										env.error(other.getFirstSymbol(),  "This method is part of a overloaded method. Therefore either all methods of this "
												+ " prototype are 'final' or none is");
									}
									if ( other.getMethodSignature().getReturnType(env) != returnValueType ) {
										env.error(other.getFirstSymbol(),  "This method is part of a overloaded method. Therefore all methods with this name of this "
												+ " prototype should have the same return value type");
									}

								}


								proto.addOverloadMethodList(overloadMethodList);
								// no interface implemented by {\tt P} should declare a method with the same name as {\tt m};
								if ( interMSList_Impl_Interfaces != null && interMSList_Impl_Interfaces.size() > 0 ) {
									env.error(method.getFirstSymbol(),  "Method '" + methodName + "' is declared in this object "
											+ "and in one of the implemented interfaces, ");
								}
								if ( superProto != null ) {
									ArrayList<MethodSignature> supermsList = superProto.searchMethodProtectedPublicSuperProtectedPublic(methodName, env);
									if ( supermsList != null && supermsList.size() > 0 ) {
										env.error(method.getFirstSymbol(), "This method is preceded by 'overload'. It should be the first "
												+ " method in the prototype hierarchy. But it is not. This method overrides a method of prototype '"
												+ supermsList.get(0).getMethod().getDeclaringObject().getFullName() + "' " );
									}

								}

							}
							else {
								boolean oneMethodIn_pmsList = true;
								boolean allSameSignature = true;
								boolean isVisibilityOk = true;
								boolean somePrecededByOverload = false;

								// method is not preceded by 'overload'
								if ( msList_PPP_This_And_SuperProto.size() == 1 ) {
									/*
									 * If {\tt msList} has just method {\tt m} and {\tt interMSList} is empty, the
									 * declaration of {\tt m} is correct. It {\tt interMSList} is not empty, it should
									 * have just one method since interfaces cannot declare overloaded methods. If the
									 * single method signature of {\tt interMSList} is different from the signature
									 * of {\tt m} then the declaration of {\tt m} is incorrect;
									 */
									if ( interMSList_Impl_Interfaces != null && interMSList_Impl_Interfaces.size() > 0 ) {
										if ( ! interMSList_Impl_Interfaces.get(0).getFullName(env).equals(method.getMethodSignature().getFullName(env)) ) {
											// System.out.println(interMSList.get(0).getFullName() + " != " + method.getMethodSignature().getFullName());
											env.error(method.getFirstSymbol(), "This method implements a method of an interface but with different parameter types");
										}
									}
								}
								else if ( msList_PPP_This_And_SuperProto.size() == pmsList_PP_This_Proto.size() &&
										  pmsList_PP_This_Proto.size() > 1 ) {
									/* all methods with the same name are in the prototype and this one is note preceded by 'overload'.
									 * Then the first method should be preceded by 'overload'




		                            \item Suppose all methods of {\tt msList} are in {\tt P} ({\tt pmsList} is equal to {\tt msList}) and
		                            {\tt pmsList} has more than one element.  The declaration of the methods of {\tt pmsList} are correct if:
		                            \begin{enumerate}[(i)]
		                            \item each two methods of {\tt pmsList} have different signatures;
		                            \item all methods of {\tt pmsList} have the same return value type;
		                            \item the first textually declared method is preceded by keyword {\tt overload}. No other method is preceded by this keyword;
		                            \item if one method of {\tt pmsList} is final, all methods of this list should be final too. If it is not final, no method of the list should be final;

		                           \item no method of {\tt pmsList} is protected or abstract;

		                            \item {\tt interMSList} should be empty.
		                            \end{enumerate}
									 *
									 */

									// each two methods of {\tt pmsList} have different signatures ?
									int size_pmsList = pmsList_PP_This_Proto.size();
									for (int ii = 0; ii < size_pmsList; ++ii ) {
										for ( int jj = ii + 1; jj < size_pmsList; ++jj) {
											if ( pmsList_PP_This_Proto.get(ii).getFullName(env).equals(pmsList_PP_This_Proto.get(jj).getFullName(env)) ) {
												env.error(pmsList_PP_This_Proto.get(ii).getFirstSymbol(), "Method '" + pmsList_PP_This_Proto.get(ii).getName() + "' "
														+ "of line " + pmsList_PP_This_Proto.get(ii).getFirstSymbol().getLineNumber() +
														" is being duplicated in line " + pmsList_PP_This_Proto.get(jj).getFirstSymbol().getLineNumber() );
											}
										}
										// no method of {\tt pmsList} should be protected or abstract;
										if ( pmsList_PP_This_Proto.get(ii).getMethod().getVisibility() == Token.PROTECTED ) {
											env.error(pmsList_PP_This_Proto.get(0).getMethod().getFirstSymbol(),
													"This method belongs to a overloaded method. It cannot be 'protected'");
										}
										if ( pmsList_PP_This_Proto.get(ii).getMethod().isAbstract() ) {
											env.error(pmsList_PP_This_Proto.get(0).getMethod().getFirstSymbol(),
													"This method belongs to a overloaded method. It cannot be 'abstract'");
										}

									}
									// the first textually declared method is preceded by keyword {\tt overload}. No other method is preceded by this keyword
									if ( ! pmsList_PP_This_Proto.get(0).getMethod().getPrecededBy_overload() ) {
										env.error(pmsList_PP_This_Proto.get(0).getFirstSymbol(),
												"The first method of a overloaded method in a prototype should be preceded by keyword 'overload'");
									}

									boolean isFirstFinal = pmsList_PP_This_Proto.get(0).getMethod().getIsFinal();

									// all methods of {\tt pmsList} have the same return value type;
									Type returnType = pmsList_PP_This_Proto.get(0).getReturnType(env);
									for (int ii = 1; ii < size_pmsList; ++ii )  {
										if ( returnType != pmsList_PP_This_Proto.get(ii).getReturnType(env)  ) {
											env.error(pmsList_PP_This_Proto.get(ii).getFirstSymbol(), "Methods of lines " +
										        pmsList_PP_This_Proto.get(0).getFirstSymbol().getLineNumber() +
												" and " + pmsList_PP_This_Proto.get(ii).getFirstSymbol().getLineNumber() +
												" have different return types but equal names");
										}
										// No other method should b preceded by 'overload'
										if ( pmsList_PP_This_Proto.get(ii).getMethod().getPrecededBy_overload() ) {
											env.error(pmsList_PP_This_Proto.get(0).getFirstSymbol(),
													"Only the first method of a overloaded method in a prototype should be preceded by keyword 'overload'");
										}
										// if one method of {\tt pmsList} is final, all methods of this list should be final too. If it is not final, no method of the list should be final
										if ( pmsList_PP_This_Proto.get(ii).getMethod().getIsFinal() != isFirstFinal ) {
											env.error(pmsList_PP_This_Proto.get(ii).getFirstSymbol(), "Either all methods of a overloaded method of a prototype are 'final' or none is. "
													+ " However, methods of lines " + pmsList_PP_This_Proto.get(0).getFirstSymbol().getLineNumber() + " and " +
													pmsList_PP_This_Proto.get(ii).getFirstSymbol().getLineNumber() + " have different qualifiers");
										}
									}
									// {\tt interMSList} should be empty.
									if ( interMSList_Impl_Interfaces != null && interMSList_Impl_Interfaces.size() > 0 ) {
										env.error(pmsList_PP_This_Proto.get(0).getFirstSymbol(), "This method belongs to a overloaded method and "
												+ " there is a method signature with the same name being declared in interface " +
												interMSList_Impl_Interfaces.get(0).getDeclaringInterface().getFullName() + " that is implemented by " +
												" the current object"
												);
									}
									ArrayList<MethodDec> overloadMethodList = new ArrayList<>();

									for ( MethodSignature ms : pmsList_PP_This_Proto ) {
										overloadMethodList.add(ms.getMethod());
										ms.getMethod().setOverload(true);
										alreadCheckedList.add(ms.getMethod().getMethodNumber());
									}
									proto.addOverloadMethodList(overloadMethodList);
								}
								else if ( msList_PPP_This_And_SuperProto.size() > pmsList_PP_This_Proto.size() ) {
									/*
									 * there is at least one method in the super-prototype.
		                                  \item Suppose there is at least one method in {\tt msList} that is in a super-prototype and:
		                                  \begin{enumerate}[(i)]
		                                  \item there is just one method in {\tt pmsList} which is preceded by 'override';
		                                  \item  the return value type of {\tt m} is a subtype of the return value type of
		                                  {\tt m1}, which is the first method with name equal to {\tt m} found in a search starting in the super-prototype of {\tt P} and continuing upwards;
		                                  \item all methods of {\tt msList} have the same signature except for the return value type. That would mean that each method of {\tt msList} is in a different prototype;
		                                  \item if the method of {\tt pmsList} is protected, so are all the methods of the list {\tt msList};
		                                  \item no method of {\tt msList} is preceded by {\tt overload}.
		                                  \end{enumerate}
		                                  Then the declaration of the method of {\tt pmsList} is correct even if {\tt interMSList} contains an element.
									 */
									oneMethodIn_pmsList = pmsList_PP_This_Proto.size() == 1;
									if ( ! method.getHasOverride() ) {

										env.error(method.getFirstSymbol(), "Method '" + method.getName() + "' overrides a super-prototype method of prototype '" +
												msList_PPP_This_And_SuperProto.get(msList_PPP_This_And_SuperProto.size()-1).getMethod().getDeclaringObject().getFullName()
												+ "'. It should be preceded by keyword 'override'");
									}
									if ( method.isAbstract() && ! msList_PPP_This_And_SuperProto.get(1).getMethod().isAbstract() ) {
										env.error(method.getFirstSymbol(), "Method '" + method.getName()
										   + "' overrides a super-prototype method that is not abstract. This is illegal" );
									}
									/*
		                             the return value type of {\tt m} is a subtype of the return value type of
		                              {\tt m1}, which is the first method with name equal to {\tt m} found in
		                              a search starting in the super-prototype of {\tt P} and continuing upwards;
									 */
									// superProto  must be different from null because msList.size() > pmsList.size()
									ObjectDec nextObj = superProto;
									ArrayList<MethodSignature> firstEqualNameList = nextObj.searchMethodProtectedPublic(methodName);
									while ( firstEqualNameList == null || firstEqualNameList.size() == 0 ) {
										nextObj = nextObj.getSuperobject();
										firstEqualNameList = nextObj.searchMethodProtectedPublic(methodName);
									}
									if ( !firstEqualNameList.get(0).getReturnType(env).isSupertypeOf(method.getMethodSignature().getReturnType(env), env)) {
										env.error(method.getFirstSymbol(), "This method should have a return value type that is subtype of the return type of "
												+ "method of line " + firstEqualNameList.get(0).getMethod().getFirstSymbol().getLineNumber() +
												" of prototype '" + firstEqualNameList.get(0).getMethod().getDeclaringObject().getFullName()
												);
									}
									/*
		                              all methods of {\tt msList} have the same signature except for the return value type.
		                              That would mean that each method of {\tt msList} is in a different prototype;
									 *
									 */

									String fullNameFirst = msList_PPP_This_And_SuperProto.get(0).getFullName(env);
									for (int ii = 1; ii < msList_PPP_This_And_SuperProto.size(); ++ii) {
										if ( ! msList_PPP_This_And_SuperProto.get(ii).getFullName(env).equals(fullNameFirst) ) {
											allSameSignature = false;
										}
									}
									Token firstVisibility = pmsList_PP_This_Proto.get(0).getMethod().getVisibility();
									for ( MethodSignature ms : msList_PPP_This_And_SuperProto ) {
										if ( ms.getMethod().getVisibility() != firstVisibility ) {
											isVisibilityOk = false;
										}
										if ( ms.getMethod().getPrecededBy_overload() )
											somePrecededByOverload = true;
									}
									if ( ! allSameSignature && interMSList_Impl_Interfaces != null && interMSList_Impl_Interfaces.size() > 0 ) {
										env.error(method.getFirstSymbol(),  "This is a overloaded method and its signature is being defined in interface '"
												+ interMSList_Impl_Interfaces.get(0).getDeclaringInterface().getFullName() + "'. This is illegal. This method is a overloaded method "
												+ "because there is a method in the same prototype or in super-prototypes with the same name but with different "
											    + "types for the parameters");
									}

									if ( somePrecededByOverload ) {
										ArrayList<MethodDec> overloadMethodList = new ArrayList<>();
										for ( MethodSignature ms : pmsList_PP_This_Proto ) {
											overloadMethodList.add(ms.getMethod());
											alreadCheckedList.add(ms.getMethod().getMethodNumber());
											ms.getMethod().setOverload(true);
										}
										proto.addOverloadMethodList(overloadMethodList);
									}
									else if ( ! allSameSignature && pmsList_PP_This_Proto.size() == 1 ) {
										env.error(method.getFirstSymbol(),
												"Method " + method.getName() + " overrides a super-prototype method but it has a different signature. That is "
												+ "the type of at least one parameter is different from the type of the corresponding parameter of "
														+ "one method of one of the super-prototypes.");
									}
								}
								else {
									if ( !oneMethodIn_pmsList || !allSameSignature || ! isVisibilityOk || somePrecededByOverload ) {
										/*
		                                  \item Suppose there is at least one method in {\tt msList} that is in a super-prototype and there are at least two methods of {\tt msList} that have different signatures. That includes the case in which {\tt pmsList} has two methods (since they have the same name,  they must have different signatures).
		                                  The declaration of the methods of {\tt pmsList} are correct if:
		                                 \begin{enumerate}[(i)]
		                                 \item each two methods of {\tt pmsList} have different signatures. Note that {\tt pmsList} may have just one element, {\tt m}, although {\tt msList} should have at least two elements;
										  \item all methods of {\tt pmsList} are preceded by keyword ``{\tt override}"\/;
		                                 \item all methods of {\tt pmsList} have the same return value type;
		                                 \item let {\tt Q} be the first direct or indirect super-prototype of {\tt P} that declares a
		                                 method with the same name as {\tt m} and {\tt directSMList} be the list of methods of
		                                 {\tt Q} that have the same name as {\tt m}. All methods of {\tt directSMList} have the same
		                                 return value type {\tt R}. The return type of all methods of {\tt pmsList} should be subtype of {\tt R};

		                                 \item let {\tt T} be the super-prototype of {\tt P} that declares a method with the same name
		                                 as {\tt m} and that is higher in the {\tt P} hierarchy. That is, no super-prototype of {\tt T}
		                                 declares a method with the same name as {\tt m}. Then the first textually declared method of
		                                 {\tt T} should be preceded by keyword {\tt overload}. No other method in the {\tt P}
		                                 hierarchy should be preceded by this keyword;

		                                 \item either none or all methods of {\tt pmsList} are final;
		                                 \item no method of {\tt pmsList} is protected or abstract;

		                                 \item {\tt interMSList} should be empty.
		                                 \end{enumerate}
										 *
										 */


										int size_pmsList = pmsList_PP_This_Proto.size();
										boolean areMethodsFinal = pmsList_PP_This_Proto.get(0).getMethod().getIsFinal();
										Type returnValueType = pmsList_PP_This_Proto.get(0).getReturnType(env);
										//  each two methods of {\tt pmsList} have different signatures. Note that {\tt pmsList} may have just one element, {\tt m}, although {\tt msList} should have at least two elements;
										for (int ii = 0; ii < size_pmsList; ++ii ) {
											for ( int jj = ii + 1; jj < size_pmsList; ++jj) {
												if ( pmsList_PP_This_Proto.get(ii).getFullName(env).equals(pmsList_PP_This_Proto.get(jj).getFullName(env)) ) {
													env.error(pmsList_PP_This_Proto.get(ii).getMethod().getFirstSymbol(), "Methods of lines " +
															pmsList_PP_This_Proto.get(ii).getMethod().getFirstSymbol().getLineNumber() + " and " +
															pmsList_PP_This_Proto.get(jj).getMethod().getFirstSymbol().getLineNumber() +
															" have equal signatures. The method is then being redeclared"
															);
												}
											}
											if ( ! pmsList_PP_This_Proto.get(ii).getMethod().getHasOverride() ) {
												env.error(method.getFirstSymbol(), "This method overrides a super-prototype method. It should be preceded"
														+ " by keyword 'override'");
											}
											if ( returnValueType != pmsList_PP_This_Proto.get(ii).getReturnType(env) ) {
												env.error(pmsList_PP_This_Proto.get(ii).getMethod().getFirstSymbol(), "Methods of lines " +
														pmsList_PP_This_Proto.get(0).getMethod().getFirstSymbol().getLineNumber() + " and " +
														pmsList_PP_This_Proto.get(ii).getMethod().getFirstSymbol().getLineNumber() +
														" have different return value types. They should be equal"
														);
											}
											// no method of {\tt pmsList} should be protected or abstract;
											if ( pmsList_PP_This_Proto.get(ii).getMethod().getVisibility() == Token.PROTECTED ) {
												env.error(pmsList_PP_This_Proto.get(0).getMethod().getFirstSymbol(), "This method belongs to a overloaded method. It cannot be 'protected'");
											}
											if ( pmsList_PP_This_Proto.get(ii).getMethod().isAbstract() ) {
												env.error(pmsList_PP_This_Proto.get(0).getMethod().getFirstSymbol(),
														"This method belongs to a overloaded method. It cannot be 'abstract'");
											}
											if ( pmsList_PP_This_Proto.get(ii).getMethod().getIsFinal() != areMethodsFinal ) {
												env.error(method.getFirstSymbol(),  "This method belong to a overloaded method. Either all methods with this "
														+ "same name in this prototype should be 'final' or none should be");
											}

										}
										if ( interMSList_Impl_Interfaces != null && interMSList_Impl_Interfaces.size() > 0 ) {
											env.error(method.getFirstSymbol(),  "This is a overloaded method and its signature is being defined in interface '"
													+ interMSList_Impl_Interfaces.get(0).getDeclaringInterface().getFullName() + "'. This is illegal");
										}
										/*
		                                \item let {\tt Q} be the first direct or indirect super-prototype of {\tt P} that declares a
		                                method with the same name as {\tt m} and {\tt directSMList} be the list of methods of
		                                {\tt Q} that have the same name as {\tt m}. All methods of {\tt directSMList} have the same
		                                return value type {\tt R}. The return type of all methods of {\tt pmsList} should be subtype of {\tt R};

										 */
										ObjectDec Q = superProto;
										while ( Q != null ) {
											ArrayList<MethodSignature> directSMList = Q.searchMethodProtectedPublic(methodName);
											if ( directSMList != null && directSMList.size() > 0 ) {
												if ( ! directSMList.get(0).getReturnType(env).isSupertypeOf(returnValueType, env) ) {
													env.error(method.getFirstSymbol(), "The return value type of this method should be equal or a subtype of "
															+ "the return value type of the first method with the same name found in a super-prototype. "
															+ "Then the return value type of this method should be equal or a subtype of "
															+ directSMList.get(0).getReturnType(env).getFullName());
												}
												break;
											}
											Q = Q.getSuperobject();
										}
										/*
		                                \item let {\tt T} be the super-prototype of {\tt P} that declares a method with the same name
		                                as {\tt m} and that is higher in the {\tt P} hierarchy. That is, no super-prototype of {\tt T}
		                                declares a method with the same name as {\tt m}. Then the first textually declared method of
		                                {\tt T} should be preceded by keyword {\tt overload}. No other method in the {\tt P}

		                                hierarchy should be preceded by this keyword;
										 *
										 */
										ObjectDec scanSuperProtos = superProto;
										ObjectDec lastWithSameNameAsMethod = null;
										ArrayList<MethodSignature> lastMSList = null;
										while ( scanSuperProtos != null ) {
											ArrayList<MethodSignature> directSMList = Q.searchMethodProtectedPublic(methodName);
											if ( directSMList != null && directSMList.size() > 0 ) {
												lastWithSameNameAsMethod = scanSuperProtos;
												lastMSList = directSMList;
											}
											scanSuperProtos = scanSuperProtos.getSuperobject();
										}
										if ( lastWithSameNameAsMethod == null )
											env.error(method.getFirstSymbol(),  "Internal error in calcInterfaceTypes of Program");
										else {
											int n = lastMSList.get(0).getMethod().getMethodNumber();
											int index = 0;
											for (int ii = 1; ii < lastMSList.size(); ++ii) {
												if ( lastMSList.get(ii).getMethod().getMethodNumber() < n ) {
													index = ii;
													n = lastMSList.get(ii).getMethod().getMethodNumber();
												}
											}
											if ( ! lastMSList.get(index).getMethod().getPrecededBy_overload() ) {
												env.error(lastMSList.get(index).getMethod().getFirstSymbol(),
														"This method should be preceded by keyword 'overload'");
											}
										}
										ArrayList<MethodDec> overloadMethodList = new ArrayList<>();
										for ( MethodSignature ms : pmsList_PP_This_Proto ) {
											overloadMethodList.add(ms.getMethod());
											alreadCheckedList.add(ms.getMethod().getMethodNumber());
											ms.getMethod().setOverload(true);
										}
										proto.addOverloadMethodList(overloadMethodList);
									}
								}


							}
							/*
							boolean overloadedMethod = false;
						    if ( superProto != null ) {
								/*
								 * if some method with the same name of any of the super-prototypes has been preceded by
								 * 'overloadedMethod' then all methods are multi-methods. Just set variable overloadedMethod
		    					 * of each method in the hierarchy from the current prototype onwards
			    				 * /
							    mspppList = superProto.searchMethodProtectedPublicSuperProtectedPublic(methodName, env);
							    for ( MethodSignature ms : mspppList ) {
								    MethodDec other = ms.getMethod();
								    if ( other.getPrecededBy_overloadedMethod() )
								    	overloadedMethod = true;
							    }
						    }
						    if ( overloadedMethod ) {
							    for ( MethodSignature ms : mspppList ) {
								    MethodDec other = ms.getMethod();
								    other.setoverloadedMethod(true);
							    }
						    }
						    */

						}
					}
				}
				else {
					// an interface
					InterfaceDec inter = (InterfaceDec ) pu;
					ArrayList<InterfaceDec> superInterfaceList = inter.getSuperInterfaceList();

					for ( MethodSignature ms : inter.getMethodSignatureList() ) {
						String nameMS = ms.getName();
						for ( MethodSignature ms2 : inter.getMethodSignatureList() )  {
							if ( nameMS == ms2.getName() && ms != ms2 ) {
								/*
								 * two equal method signatures
								 */
								env.error(cunit, ms.getFirstSymbol(), "There are two equal method signatures in this interface in lines "
										+ ms.getFirstSymbol().getLineNumber() + " and " + ms2.getFirstSymbol().getLineNumber());
							}
						}
						if ( superInterfaceList != null ) {
							for ( InterfaceDec superInterface : superInterfaceList ) {

								ArrayList<MethodSignature> msList = superInterface.searchMethodPublicSuperPublicOnlyInterfaces(nameMS, env);
								/*
								 * each method superMS of one of the super interfaces with the same name as nameMS should have
								 * exactly the same parameter types and the same return value type.
								 */
								for ( MethodSignature superMS : msList ) {
									if ( ms.getReturnType(env) != superMS.getReturnType(env) ) {
										env.error(cunit, ms.getFirstSymbol(), "This signature has a return type different from the signature declared "
											+ " in super interface '" + superInterface.getFullName() + "'"	);
									}
									if ( ms instanceof MethodSignatureWithSelectors ) {
										/**
										 * the parameter types should be equal
										 */
										MethodSignatureWithSelectors msng = (MethodSignatureWithSelectors ) ms;
										ArrayList<SelectorWithParameters> selectorList = msng.getSelectorArray();
										MethodSignatureWithSelectors msngSuper = (MethodSignatureWithSelectors ) superMS;
										ArrayList<SelectorWithParameters> selectorListSuper = msngSuper.getSelectorArray();
										int i = 0;
										for ( SelectorWithParameters sel : selectorList ) {
											SelectorWithParameters selSuper = selectorListSuper.get(i);
											int j = 0;
											for ( ParameterDec p : sel.getParameterList() ) {
												ParameterDec pSuper = selSuper.getParameterList().get(j);
												Type sub = p.getType();
												if ( sub == null ) sub = Type.Dyn;
												Type superT = pSuper.getType();
												if ( superT == null ) superT = Type.Dyn;
												if ( sub != superT ) {
													env.error(cunit, ms.getFirstSymbol(), "This signature is different from the signature declared "
															+ " in super interface '" + superInterface.getFullName() + "'"	);

												}
												++j;
											}
											++i;
										}
									}
									else if ( ms instanceof MethodSignatureOperator ) {
										MethodSignatureOperator msop = (MethodSignatureOperator ) ms;
										MethodSignatureOperator msopSuper = (MethodSignatureOperator ) superMS;
										if ( msop.getParameterList() == null || msop.getParameterList().size() == 0 ) {
											if ( msopSuper.getParameterList() != null ) {
												env.error(cunit, ms.getFirstSymbol(), "This signature is different from the signature declared "
														+ " in super interface '" + superInterface.getFullName() + "'"	);
											}
										}
										else if ( msopSuper.getParameterList() == null || msopSuper.getParameterList().size() == 0 ) {
											env.error(cunit, ms.getFirstSymbol(), "This signature is different from the signature declared "
													+ " in super interface '" + superInterface.getFullName() + "'"	);
										}
										else {
											// both non-null
											if ( msop.getParameterList().get(0).getType() != msopSuper.getParameterList().get(0).getType() ) {
												env.error(cunit, ms.getFirstSymbol(), "This signature is different from the signature declared "
														+ " in super interface '" + superInterface.getFullName() + "'"	);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		env.atEndOfCurrentCompilationUnit();
		}
	}

	/**
	 * make the metaobject annotations of this program unit communicate with each other
	 */
	protected void makeMetaobjectAnnotationsCommunicateInPackage(Env env) {


		/*
		 * every metaobject can supply information to other metaobjects.
		 * Every tuple in this set correspond to a metaobject annotation.
		 * Every tuple is composed by a metaobject name, the number of this metaobject
		 * considering all metaobjects in the prototype, the number of this metaobject
		 * considering only the metaobjects with the same name, the package name,
		 * the prototype name, and the information
		 * this metaobject annotation wants to share with other metaobject annotations.
		 */

		for ( CyanPackage cp : this.packageList ) {



			HashSet<Tuple6<String, Integer, Integer, String, String, Object>> moInfoSet = new HashSet<>();
			for ( CompilationUnit compUnit :  cp.getCompilationUnitList() ) {

				if ( ! compUnit.getHasGenericPrototype() ) {
					for ( ProgramUnit pu : compUnit.getProgramUnitList() ) {

						/*
						 * only prototypes that do not have a '<' in its name can communicate. For
						 * example, <code>Set{@literal <}Int></code> cannot communicate with anyone.
						 */
						ArrayList<CyanMetaobjectAnnotation> allMetaobjectAnnotationList = new ArrayList<>();
						ArrayList<CyanMetaobjectAnnotation> metaobjectAnnotationList = pu.getCompleteMetaobjectAnnotationList();
						if ( metaobjectAnnotationList != null )
							allMetaobjectAnnotationList.addAll(metaobjectAnnotationList);
						ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectWithAtAnnotationList = pu.getNonAttachedMetaobjectAnnotationList();
						if ( metaobjectWithAtAnnotationList != null )
							allMetaobjectAnnotationList.addAll(metaobjectWithAtAnnotationList);
						for ( CyanMetaobjectAnnotation annotation : allMetaobjectAnnotationList ) {
							CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
							cyanMetaobject.setMetaobjectAnnotation(annotation);
							if ( cyanMetaobject instanceof ICommunicateInPackage_ati_dsa ) {
								Object sharedInfo = ((ICommunicateInPackage_ati_dsa) cyanMetaobject).ati_dsa_shareInfoPackage();
								   //annotation.shareInfoPackage();
								if (  sharedInfo != null ) {
									if ( !cp.getCommunicateInPackage() ) {
										env.error(annotation.getFirstSymbol(), "This metaobject annotation is trying to communicate with "
												+ "other prototypes. "
												+ "This is illegal because package '" +
												cp.getPackageName() + "' does not allow that. To make that " +
													 "legal, attach '@feature(communicateInPackage, #on)' to the package in the project (.pyan) file", true, false);
									}

									if ( pu.getGenericParameterListList().size() == 0 )  {
										Tuple6<String, Integer, Integer, String, String, Object> t = new Tuple6<>(annotation.getCyanMetaobject().getName(),
												annotation.getMetaobjectAnnotationNumber(), annotation.getMetaobjectAnnotationNumberByKind(),
												annotation.getPackageOfAnnotation(), annotation.getPrototypeOfAnnotation(),
												sharedInfo);
										moInfoSet.add(t);
									}
									else {
										env.error(true, annotation.getFirstSymbol(),
												"metaobject annotation of metaobject '" +
												       annotation.getCyanMetaobject().getName() +
												       "' is trying to communicate with other metaobjects of the package." +
												       "This is prohibit because this metaobject is a generic prototype instantiation or has a '<' in its name", null, ErrorKind.metaobject_error);
									}
								}
							}
						}
					}
				}
			}

			/*
			 * send information to all metaobjects of the package that want to receive information. Let them communicate with each other
			 */
			if ( moInfoSet.size() > 0 ) {
				for ( CompilationUnit compUnit : cp.getCompilationUnitList() ) {
					if ( ! compUnit.getHasGenericPrototype() ) {
						for ( ProgramUnit pu : compUnit.getProgramUnitList() ) {
							ArrayList<CyanMetaobjectAnnotation> allMetaobjectAnnotationList = new ArrayList<>();
							ArrayList<CyanMetaobjectAnnotation> metaobjectAnnotationList = pu.getCompleteMetaobjectAnnotationList();
							if ( metaobjectAnnotationList != null )
								allMetaobjectAnnotationList.addAll(metaobjectAnnotationList);
							ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectWithAtAnnotationList = pu.getNonAttachedMetaobjectAnnotationList();
							if ( metaobjectWithAtAnnotationList != null )
								allMetaobjectAnnotationList.addAll(metaobjectWithAtAnnotationList);

							for ( CyanMetaobjectAnnotation annotation : allMetaobjectAnnotationList) {
								CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
								cyanMetaobject.setMetaobjectAnnotation(annotation);
								if ( cyanMetaobject instanceof ICommunicateInPackage_ati_dsa ) {
									((ICommunicateInPackage_ati_dsa ) cyanMetaobject).ati_dsa_receiveInfoPackage(moInfoSet);
								}

							}

}
					}
				}
			}
		}
	}


	/**
	 * call an action method of all metaobjects that should be called after typing the prototype interfaces
	 */
	public boolean ati3_check(Env env) {

		ICompiler_ati compiler_ati = new Compiler_ati(env);
		// makeMetaobjectAnnotationsCommunicateInPackage(env);

		ICompilerProgramView_ati compilerProgramView_ati = new CompilerProgramView_ati(env);
		CompilerPackageView_ati compilerPackageView_ati = new CompilerPackageView_ati(env);



		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			compilationUnit.ati3_check(compiler_ati);
		}

		/*
		 * do the ati checkings for metaobjects of each package
		 */
		for ( CyanPackage cyanPackage : this.packageList ) {
			compilerPackageView_ati.setPackage(cyanPackage);
			cyanPackage.ati3_check(env, compilerPackageView_ati);
		}

		/*
		 * call a method of all metaobjects of a Cyan program project to do checks
		 */
		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList1 = project.getNonAttachedMetaobjectAnnotationList(),
				nonAttachedMetaobjectAnnotationList1 = project.getAttachedMetaobjectAnnotationList();
		if ( attachedMetaobjectAnnotationList1 != null || nonAttachedMetaobjectAnnotationList1 != null ) {
			ArrayList<CyanMetaobjectWithAtAnnotation> programMetaobjectAnnotationList = new ArrayList<>();
			if ( attachedMetaobjectAnnotationList1 != null )
				programMetaobjectAnnotationList.addAll(attachedMetaobjectAnnotationList1);
			if ( nonAttachedMetaobjectAnnotationList1 != null )
				programMetaobjectAnnotationList.addAll(nonAttachedMetaobjectAnnotationList1);

			for ( CyanMetaobjectWithAtAnnotation annotation : programMetaobjectAnnotationList) {
				CyanMetaobjectWithAt metaobject = annotation.getCyanMetaobject();
				metaobject.setMetaobjectAnnotation(annotation);
				if ( metaobject instanceof ICheckProgram_ati3 ) {
					ICheckProgram_ati3 fp = (ICheckProgram_ati3 ) metaobject;


					try {
						fp.ati3_checkProgram(compilerProgramView_ati);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(metaobject, annotation);
					}

				}
			}

		}



		return true;


	}


	public boolean dsa2_check(Env env) {

		ICompiler_dsa compiler_dsa = new Compiler_dsa(env);

		ICompilerProgramView_ati compilerProgramView_ati = new CompilerProgramView_ati(env);
		CompilerPackageView_ati compilerPackageView_ati = new CompilerPackageView_ati(env);



		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			try {
				compilationUnit.dsa2_check(compiler_dsa, env);
			}
			catch ( error.CompileErrorException e ) {
			}
		}

		/*
		 * do the ati checkings for metaobjects of each package
		 */
		for ( CyanPackage cyanPackage : this.packageList ) {
			compilerPackageView_ati.setPackage(cyanPackage);
			try {
				cyanPackage.dsa2_check(env, compilerPackageView_ati);
			}
			catch ( error.CompileErrorException e ) {
			}

		}

		/*
		 * call a method of all metaobjects of a Cyan program project to do checks
		 */
		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList1 = project.getNonAttachedMetaobjectAnnotationList(),
				nonAttachedMetaobjectAnnotationList1 = project.getAttachedMetaobjectAnnotationList();
		if ( attachedMetaobjectAnnotationList1 != null || nonAttachedMetaobjectAnnotationList1 != null ) {
			ArrayList<CyanMetaobjectWithAtAnnotation> programMetaobjectAnnotationList = new ArrayList<>();
			if ( attachedMetaobjectAnnotationList1 != null )
				programMetaobjectAnnotationList.addAll(attachedMetaobjectAnnotationList1);
			if ( nonAttachedMetaobjectAnnotationList1 != null )
				programMetaobjectAnnotationList.addAll(nonAttachedMetaobjectAnnotationList1);

			for ( CyanMetaobjectWithAtAnnotation annotation : programMetaobjectAnnotationList) {
				CyanMetaobjectWithAt metaobject = annotation.getCyanMetaobject();
				metaobject.setMetaobjectAnnotation(annotation);
				if ( metaobject instanceof ICheckProgram_dsa2 ) {
					ICheckProgram_dsa2 fp = (ICheckProgram_dsa2 ) metaobject;


					try {
						fp.dsa2_checkProgram(compilerProgramView_ati);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(metaobject, annotation);
					}

				}
			}

		}



		return true;


	}




	/**
	 * call a method of all metaobjects. This method should be called after typing the prototype interfaces
	 */
	public boolean ati_actions(Env env) {

		ICompiler_ati compiler_ati = new Compiler_ati(env);
		CompilerManager_ati compilerManager = new CompilerManager_ati(env);

		makeMetaobjectAnnotationsCommunicateInPackage(env);

		ICompilerProgramView_ati compilerProgramView_ati = new CompilerProgramView_ati(env);
		CompilerPackageView_ati compilerPackageView_ati = new CompilerPackageView_ati(env);


		/*
		 * compiler_ati will store all demands of the metaobjects of all compilation units of
		 * the program. The metaobject annotations will ask to insert instance variables, and methods
		 * in prototypes. But for now no changes are made in the program source code.
		 * The demands are just stored in compiler_ati.
		 */
		/*
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			//  System.out.println(compilationUnit.getFilename());
			compilationUnit.ati_actions(compiler_ati, compilerManager);
		}
		*/

		int i = 0;
		while ( i < compilationUnitList.size() ) {
			CompilationUnit compilationUnit = compilationUnitList.get(i);
			compilationUnit.ati_actions(compiler_ati, compilerManager);
			++i;
		}


		/*
		 * do the ati actions for metaobjects attached to each package in the project file.
		 * These actions may add code to the prototypes.
		 */
		for ( CyanPackage cyanPackage : this.packageList ) {
			compilerPackageView_ati.setPackage(cyanPackage);
			cyanPackage.ati_actions(env, compilerPackageView_ati, compilerManager);
		}

		/*
		 * call a method of all metaobjects of a Cyan program project to do ati actions.
		 * Non-attached metaobjects cannot take actions
		 */

		if ( this.project.getAttachedMetaobjectAnnotationList() != null ) {
			/*CompilerManager_ati.addCodeAndSlotsTo(project.getAttachedMetaobjectAnnotationList(), env,
					compilerProgramView_ati, compilerManager);  */

			this.ati_actions(env, compilerProgramView_ati, compilerManager);
		}

		/*
		 * all changes demanded by metaobject annotations collected above are made in the call
		 * to CompilerManager_ati#changeCheckProgram.
		 */
	    if ( ! compilerManager.changeCheckProgram() || env.isThereWasError() )
	    	return false;



		return true;


	}


	public void ati_actions(Env env, ICompilerProgramView_ati compilerForProject_ati, CompilerManager_ati compilerManager) {

		if ( this.project.getAttachedMetaobjectAnnotationList()  == null )
			return ;

		for ( CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation : this.project.getAttachedMetaobjectAnnotationList() ) {


			CyanMetaobject cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();
			if ( cyanMetaobject == null )
				continue;

			cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);


			if ( cyanMetaobject instanceof IActionProgram_ati ) {

				CyanMetaobjectWithAt cyanMetaobjectWithAt = (CyanMetaobjectWithAt ) cyanMetaobject;



				ArrayList<Tuple3<String, String, StringBuffer>> codeToAddList = null;

				try {
					codeToAddList = ((IActionProgram_ati ) cyanMetaobjectWithAt).ati_CodeToAdd(compilerForProject_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}


				if ( codeToAddList != null ) {
					for ( Tuple3<String, String, StringBuffer> codeToAdd : codeToAddList ) {
						/*
						 * only a prototype that has a {@literal <} in its name can change itself.
						 * And a prototype that has a {@literal <} in its name can change only itself.
						 */
						String packageName = codeToAdd.f1;
						String prototypeName = codeToAdd.f2;
						if ( prototypeName.indexOf('<') >= 0 ) {
							env.error(true,
									cyanMetaobjectAnnotation.getFirstSymbol(),
									"This metaobject annotation is trying to add code to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
						}
						else
							compilerManager.addCode(cyanMetaobjectWithAt, packageName, prototypeName, codeToAdd.f3);
					}
				}



				/**
				 * add method to prototypes
				 */
				ArrayList<Tuple4<String, String, String, StringBuffer>> methodList = null;

				try {
					methodList = ((IActionProgram_ati ) cyanMetaobjectWithAt).ati_methodCodeList(compilerForProject_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}


				if ( methodList != null ) {
					for ( Tuple4<String, String, String, StringBuffer> t : methodList ) {
						String packageName = t.f1;
						String prototypeName = t.f2;
						if ( prototypeName.indexOf('<') >= 0 ) {
							env.error(true,
									cyanMetaobjectAnnotation.getFirstSymbol(),
									"This metaobject annotation is trying to add code to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
						}
						else
							compilerManager.addMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f3, t.f4);
					}
				}

				/**
				 * add statements to methods
				 */
				ArrayList<Tuple4<String, String, String, StringBuffer>> statsList = null;

				try {
					statsList = ((IActionProgram_ati ) cyanMetaobjectWithAt).ati_beforeMethodCodeList(compilerForProject_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}

				if ( statsList != null ) {
					for ( Tuple4<String, String, String, StringBuffer> t : statsList ) {
						String packageName = t.f1;
						String prototypeName = t.f2;
						if ( prototypeName.indexOf('<') >= 0 ) {
							env.error(true,
									cyanMetaobjectAnnotation.getFirstSymbol(),
									"This metaobject annotation is trying to add code to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
						}
						else
							compilerManager.addBeforeMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f3, t.f4);
					}
				}

				/**
				 * add instance variables to prototypes
				 */
				ArrayList<Tuple7<String, String, Boolean, Boolean, Boolean, String, String>> instanceVariableList = null;

				try {
					instanceVariableList = ((IActionProgram_ati ) cyanMetaobjectWithAt).ati_instanceVariableList(compilerForProject_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}


				if ( instanceVariableList != null ) {
					for ( Tuple7<String, String, Boolean, Boolean, Boolean, String, String> t : instanceVariableList ) {
						String packageName = t.f1;
						String prototypeName = t.f2;
						if ( prototypeName.indexOf('<') >= 0 ) {
							env.error(true,
									cyanMetaobjectAnnotation.getFirstSymbol(),
									"This metaobject annotation is trying to add code to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
						}
						else
							compilerManager.addInstanceVariable(cyanMetaobjectWithAt,
									packageName, prototypeName, t.f3, t.f4, t.f5, t.f6, t.f7);
					}
				}


			}

		}
	}



    /**
	 *

	   @param env
	 */
	public void genJava(Env env) {

		//ProgramUnit mainPrototype = env.searchPackagePrototype("main", "Program");
		// before generating code,

		String mainPackageName = project.getMainPackage();
		CyanPackage mainPackage = project.searchPackage(mainPackageName);
		if ( mainPackage == null ) {
			try {
				env.error(null, "According to the project file (.pyan) for this program, the main package is '"
			       + mainPackageName + "'. This package was not found");
			}
			catch ( error.CompileErrorException e) {
			}
			return ;
		}
		String mainPrototypeName = project.getMainObject();
		ProgramUnit mainPrototype = mainPackage.searchPublicNonGenericProgramUnit(mainPrototypeName);
		if ( mainPrototype == null ) {
			try {
				env.error(null,  "According to the project file (.pyan) for this program, the main prototype is '"
						+ mainPackageName + "." +  mainPrototypeName + "'. This prototype was not found");
			}
			catch ( CompileErrorException e ) { }
			return ;
		}
		else if ( ! (mainPrototype instanceof ObjectDec) ) {
			try {
			    env.error(null,  "According to the project file (.pyan) for this program, the main prototype is '" +
		             mainPackageName + "." +  mainPrototypeName + "'. But this is illegal because this is an interface");
			}
			catch ( CompileErrorException e ) { }
			return ;
		}
		else if ( ((ObjectDec) mainPrototype).getIsAbstract() ) {
			try {
			    env.error(null,  "According to the project file (.pyan) for this program, the main prototype is '" +
		             mainPackageName + "." +  mainPrototypeName + "'. But this is illegal because this prototype is abstract");
			}
			catch ( CompileErrorException e ) { }
			return ;
		}

		File javaLib = new File(javaLibDir);
		if ( ! javaLib.exists() || !javaLib.isDirectory() ) {
			env.error(null, "Directory '" + javaLibDir + "' does not exist. This was declared " +
		          "as the directory of the Cyan runtime libraries, option -javalib of the compiler");
		}
		boolean foundCyanruntime = false;
		for ( File f : javaLib.listFiles() ) {
			if ( f.getName().toLowerCase().endsWith("cyanruntime") ) {
				foundCyanruntime = true;
				break;
			}
		}
		if ( ! foundCyanruntime ) {
			env.error(null, "Directory '" + javaLibDir + NameServer.fileSeparatorAsString + "cyanruntime' does not exist. " +
		              "Directory '" + javaLibDir + "' is the directory of the Cyan runtime libraries, option -javalib of the compiler");
		}
		/*
		File []runtimeList = javaLib.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith("cyanruntime");
		    }
		    });
		if ( runtimeList == null || runtimeList.length == 0 ) {
			env.error(null, "Directory '" + javaLibDir + NameServer.fileSeparatorAsString + "cyanruntime' does not exist. " +
		              "Directory '" + javaLibDir + "' is the directory of the Cyan runtime libraries, option -javalib of the compiler");
		}
		*/

		if ( ! createPackageDirectories(env, project.getProjectDir()) )
			return ;

		if ( ! createMainJavaClass(mainPackageName, mainPrototypeName, (ObjectDec ) mainPrototype, mainPackage, env) )
			return ;
		//String projectName = project.getProjectName();
		javac += "\" \"" + this.javaForProjectPathLessSlash + NameServer.fileSeparator + mainPackageName + NameServer.fileSeparator +
				mainJavaClassWithoutExtensionName + ".java\"";
		execCode += " " + mainPackageName + "." + mainJavaClassWithoutExtensionName;
		String cmdLineArgs = this.project.getCmdLineArgs();
		if ( cmdLineArgs != null ) {
			execCode += " " + cmdLineArgs;
		}

		// MyFile.writeFileText(this.project.getProjectDir() + NameServer.fileSeparatorAsString + "1.bat", javac.toCharArray());

		String newFileName = null;
		for ( CyanPackage cyanPackage : this.packageList ) {
			String outputDirectory = cyanPackage.getOutputDirectory();
			if ( outputDirectory.charAt(outputDirectory.length() - 1) != NameServer.fileSeparator ) {
				outputDirectory += NameServer.fileSeparatorAsString;
			}

			for ( CompilationUnit compilationUnit : cyanPackage.getCompilationUnitList() ) {

				   // if there is a generic program unit in the compilation unit,
				   // then code is not generated for this compilation unit.
				   // a generic program unit should be the sole program unit in a compilation unit.
				if ( ! compilationUnit.getHasGenericPrototype() && compilationUnit.getErrorList().size() == 0 ) {
					FileOutputStream fos = null;
					try {
						//newFileName = outputDirectory + compilationUnit.getPublicPrototype().getJavaNameWithoutPackage() + ".java";
						newFileName = outputDirectory + compilationUnit.getPublicPrototype().getJavaNameWithoutPackage() + ".java";

						fos = new FileOutputStream(newFileName);

						PrintWriter printWriter = new PrintWriter(fos, true);
						PW pw = new PW();
						pw.set(printWriter);
						compilationUnit.genJava(pw, env);
						printWriter.close();

						if ( compilationUnit.getPublicPrototype() == Type.Any ) {
							// generate the interface for Any

							newFileName = outputDirectory + NameServer.IAny + ".java";

							fos = new FileOutputStream(newFileName);

							printWriter = new PrintWriter(fos, true);
							pw = new PW();
							pw.set(printWriter);
							((ObjectDec ) compilationUnit.getPublicPrototype()).generateInterface(pw, env);
							printWriter.close();


						}
					}
					catch ( CompileErrorException e ) {
					}
					catch ( FileNotFoundException e ) {
						e.printStackTrace();
						env.error(null, "Cannot create file " + newFileName);
					}
					catch ( NullPointerException e ) {
						e.printStackTrace();
						env.error(null, "Internal error in Program::genJava. NPE" + newFileName);
					}
					catch (Exception e ) {
						env.error(null, "Error in writing to file " + newFileName);
					}
					finally {
						try {
							if ( fos != null ) fos.close();
						}
						catch (IOException e) {
							env.error(null, "Error in closing file " + newFileName);
						}
					}
				}
			}


		}
		/*
		if ( ! foundError ) {

			 compileGeneratedJavaCode(env);
		}
		*/
	}


	/**
	   compile the generated Java code if the compiler option calljavac is on. After that, if compiler option 'exec' is on,
	   execute the generated code.
	 */
	public void compileGeneratedJavaCode(Env env) {
		try
		    {
			 if ( this.project.getCallJavac() ) {
				 	System.out.println("Calling the Java compiler");
		            Runtime rt = Runtime.getRuntime();
		            Process proc = null;
		            try {
		            	proc = rt.exec(javac);
		            }
		            catch(SecurityException e ) {
		            	env.error(null,  "Error in calling 'javac'. Probably this program is not in the PATH variable");
		            }
		            catch ( IOException e ) {
		            	env.error(null,  "Error in calling 'javac'. There was an input/output error");
		            }
		            catch ( NullPointerException e ) {
		            	env.error(null,  "Error in calling 'javac'. Probably an internal error of this program");
		            }
		            catch ( IllegalArgumentException e ) {
		            	env.error(null,  "Internal error in '" + this.getClass().getName() +
		            			"'. Arguments to 'javac' are not well built");
		            }
		            if ( proc == null )
		            	return ;
		            InputStream stderr = proc.getErrorStream();
		            InputStreamReader isr = new InputStreamReader(stderr);
		            BufferedReader br = new BufferedReader(isr);
		            String line = null;
		            ArrayList<String> outList = new ArrayList<>();
		            while ( (line = br.readLine()) != null) {
		            	outList.add(line);
		            }
		            br.close();
		            int exitVal = proc.waitFor();
		            if ( exitVal != 0 ) {
		            	System.out.println("Error when compiling the Java code generated by the Cyan compiler (exit code " + exitVal + ")");
		            	for ( String s : outList ) {
		            		System.out.println(s);
		            	}
		            }
		            else {
					 	MyFile.writeFileText(this.project.getProjectCanonicalPath() + this.project.getExecFileName(), execCode.toCharArray());
		            	if ( this.project.getExec() ) {
		            	// call the program
					 	System.out.println("Executing the code");

					 	Process p = null;


			            try {
						 	p = Runtime.getRuntime().exec(execCode);
			            }
			            catch( SecurityException e ) {
			            	env.error(null,  "Error in calling the compiled Cyan program. Probably 'java.exe' is not in the PATH variable");
			            }
			            catch ( IOException e ) {
			            	env.error(null,  "Input/output error when running the compiled Cyan program. Maybe 'java.exe' is not in the PATH variable");
			            }
			            catch ( NullPointerException e ) {
			            	env.error(null,  "NPE when calling the compiled Cyan program");
			            }
			            catch ( IllegalArgumentException e ) {
			            	env.error(null,  "Internal error in '" + this.getClass().getName() +
			            			"'. Arguments to 'java.exe' are not well built");
			            }

			            if ( p == null )
			            	return ;


					 	exitVal = p.waitFor();
					 	BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					 	while((line = error.readLine()) != null){
					 	    System.out.println(line);
					 	}
					 	error.close();

					 	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
					 	while ( (line = input.readLine()) != null ) {
					 	    System.out.println(line);
					 	}

					 	input.close();

					 	OutputStream outputStream = p.getOutputStream();
					 	PrintStream printStream = new PrintStream(outputStream);
					 	printStream.println();
					 	printStream.flush();
					 	printStream.close();


			            if ( exitVal != 0 ) {
			            	System.out.println("Error when executing the code generated by the Java compiler (exit code " + exitVal + ")");
			            }
			       }

		            }


			 }
		    } catch (Throwable t) {
		        env.error(null, "Internal error when compiling or executing the generated Java code");
		    }
	}


	private boolean createPackageDirectories(Env env, String projectDir) {

		char separator = NameServer.fileSeparatorAsString.charAt(0);

		String partialProjectDir;
		if ( projectDir.charAt(projectDir.length()-1) == separator )
			partialProjectDir = projectDir.substring(0, projectDir.length()-1);
		else
			partialProjectDir = projectDir;
		int lastSlash = partialProjectDir.lastIndexOf(separator);
		if ( lastSlash < 0 ) {
			env.error(null,  "The project directory cannot be the root directory. There should be at least one " + separator + " in it");
			return false;
		}
		// partialProjectDir = partialProjectDir.substring(0, partialProjectDir.length() - 1);
		lastSlash = partialProjectDir.lastIndexOf(separator);
		if ( lastSlash < 0 ) {
			env.error(null,  "The project directory cannot be the root directory. There should be at least one " + separator + " in it");
			return false;
		}
		// here partialProjectDir is the directory in which the project dir is.
		String projectDirName = partialProjectDir.substring(lastSlash+1);
		partialProjectDir = partialProjectDir.substring(0, lastSlash);
		String javaForProjectPath = partialProjectDir + separator +  "java-for-" + projectDirName + separator;

		/*
           javac  -sourcepath "C:\Dropbox\Cyan\lib\java-for-cyan_lang";
                   "C:\Dropbox\Cyan\lib\javalib";"C:\Dropbox\Cyan\cyanTests\java-for-master"
                   java-for-master/main/*.java		 *
		 */
		this.javac = "javac ";
		if ( this.sourcePathList != null && this.sourcePathList.size() > 0  ) {
			for ( String sourcePath : this.sourcePathList ) {
				sourcePath = sourcePath.replace('\\', '/');
				if ( sourcePath.length() > 0 ) {
					javac += " -sourcepath \"" + sourcePath + "\" ";
				}
			}
		}
		execCode = "java ";
		if ( classPathList != null ) {
			//execCode += "-cp ";
			//javac += "-cp ";
			String s = "";
			//int size = classPathList.size();
			for ( String classPath : classPathList ) {
				classPath = classPath.replace('\\', '/');
				if ( classPath.length() != 0 ) {
					s += "\"" + classPath + "\";";
				}
				/* execCode += "\"" + classPath + "\";";
				javac += "\"" + classPath + "\"";
				if ( --size > 0 ) {
					javac += ";";
				}
				*/
			}
			//javac += " ";
			if ( s.length() != 0 ) {
				execCode += "-cp " + s;
				javac += "-cp " + s;
			}
		}
		javac += " -sourcepath ";
		File projectOutputDir = new File(javaForProjectPath);
		if ( projectOutputDir.exists() ) {
			if ( ! MyFile.deleteFileDirectory(projectOutputDir) ) {
				env.error(null,  "Unable to delete directory " + javaForProjectPath + " or some of its files");
			}
		}
		if (  ! projectOutputDir.mkdirs() ) {
			env.error(null,  "Unable to create output directory '" + javaForProjectPath + "'");
		}

		execCode += "-cp ";
		for ( CyanPackage p : this.packageList ) {
			String outputDir;
			String packageName= p.getPackageName();
			String localPackagePath = packageName.replace('.', separator);
			if ( p.getPackageName().equals(NameServer.cyanLanguagePackageName) ) {
				String pcp = p.getPackageCanonicalPath();
				if ( pcp.charAt(pcp.length()-1) == separator)
					pcp = pcp.substring(0, pcp.length()-1);
				int indexSlash = pcp.lastIndexOf(separator);
				pcp = pcp.substring(0, indexSlash);
				indexSlash = pcp.lastIndexOf(separator);
				pcp = pcp.substring(0, indexSlash);
				String partialOutDir = pcp + separator + "java-for-" + packageName.replace('.', '_');
				outputDir = partialOutDir + separator +  localPackagePath;
				p.setOutputDirectory(outputDir);
				javac += "\"" + partialOutDir + "\";";
				execCode += "\"" + partialOutDir + "\";";
			}
			else {
				outputDir = javaForProjectPath + localPackagePath;
				p.setOutputDirectory(outputDir);
			}

			File f = new File(outputDir);
			boolean ok;
			String s = MyFile.deleteNonDirFiles(f);
			if ( s != null ) {
				env.error(null,  "Cannot delete the files of directory '" + outputDir + "'");
			}
			if ( ! f.exists() ) {
				ok = f.mkdirs();
				if ( !ok ) {
					env.error(null,  "Unable to create output directory '" + outputDir + "'");
				}
			}
		}
		javaForProjectPathLessSlash = javaForProjectPath;
		if ( javaForProjectPath.charAt(javaForProjectPath.length()-1) == separator ) {
			javaForProjectPathLessSlash = javaForProjectPathLessSlash.substring(0, javaForProjectPath.length()-1);
		}
		javac += "\"" + this.javaLibDir + "\";\"" + javaForProjectPathLessSlash; // + "" + " java-for-" + projectDirName;
		execCode += "\"" + this.javaLibDir + "\";\"" + javaForProjectPathLessSlash + "\""; // + "" + " java-for-" + projectDirName;
		return true;

	}


    private ArrayList<String> topologicalSortingProgramUnitList(Env env) {
    	/*
    	 * program unit, super-prototype list, sub-prototype list
    	 */
    	HashMap<ProgramUnit, Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> > protoNameAdjList = new HashMap<>();
    	ArrayList<ProgramUnit> noSuperList = new ArrayList<>();
    	ArrayList<ProgramUnit> programUnitList = new ArrayList<>();
    	/**
    	 * collect the program units
    	 */
        for ( CompilationUnit cunit : compilationUnitList ) {
        	if ( !cunit.hasGenericPrototype() ) {
            	for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
            		programUnitList.add(pu);
            		ArrayList<ProgramUnit> superList = new ArrayList<>();
            		ArrayList<ProgramUnit> subList = new ArrayList<>();
            		protoNameAdjList.put(pu,  new Tuple2<>(superList, subList));
            	}
        	}
        }
        ObjectDec anyPrototype = (ObjectDec ) Type.Any;
        Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> anySuperSub = protoNameAdjList.get(anyPrototype);
        /**
         * build the graph of sub-type and super-type relationships
         */
        for ( ProgramUnit pu : programUnitList ) {
        	Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> t = protoNameAdjList.get(pu);
        	if ( t == null ) {
        		env.error(null,  "Internal error: program unit '" + pu.getFullName() + "' was not found in topological sorting (Program.java)");
        		return null;
        	}
    		ArrayList<ProgramUnit> superList = t.f1;
    		if ( pu instanceof InterfaceDec ) {
    			/*
    			 * add Any in the list of super-prototypes of the interface
    			 */
    			superList.add( anyPrototype );

    			/*
    			 * add interface to the list of sub-prototypes of Any
    			 */
    			anySuperSub.f2.add(pu);

    			/*
    			 * an interface only has super-interfaces
    			 */
    			InterfaceDec inter = (InterfaceDec ) pu;
    			ArrayList<InterfaceDec> superInterList = inter.getSuperInterfaceList();
    			if ( superInterList != null && superInterList.size() > 0 )  {
        			superList.addAll(superInterList);
        			for ( InterfaceDec superInter : superInterList ) {
        				Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> superT = protoNameAdjList.get(superInter);
        				/*
        				 * add pu as a sub-type of superInter, one of the super-interfaces of pu
        				 */
        	        	if ( superT == null ) {
        	        		env.error(null,  "Internal error: program unit '" + superInter.getFullName() + "' was not found in topological sorting (Program.java)");
        	        		return null;
        	        	}
        				superT.f2.add(pu);
        			}
    			}
    		}
    		else {
    			ObjectDec proto = (ObjectDec ) pu;
    			ObjectDec superProto = proto.getSuperobject();
    			if ( superProto != null ) {
    				superList.add(superProto);
    				Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> superT = protoNameAdjList.get(superProto);
    				/*
    				 * add pu as a sub-type of superProto, the super-prototype of pu
    				 */
    	        	if ( superT == null ) {
    	        		env.error(null,  "Internal error: program unit '" + superProto.getFullName() + "' was not found in topological sorting (Program.java)");
    	        		return null;
    	        	}
    				superT.f2.add(pu);

    			}
    			else {
    				   // no super-prototype, first in the list
    				noSuperList.add(proto);
    			}
    			ArrayList<Expr> implInterExprList = proto.getInterfaceList();
    			if ( implInterExprList != null && implInterExprList.size() > 0 ) {
    				for ( Expr implInterExpr : implInterExprList ) {
    					InterfaceDec superInter = (InterfaceDec ) implInterExpr.getType();
    					superList.add( superInter );
        				Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> superT = protoNameAdjList.get(superInter);
        				/*
        				 * add pu as a sub-type of superInter, one of the super-interfaces of pu
        				 */
        	        	if ( superT == null ) {
        	        		env.error(null,  "Internal error: program unit '" + superInter.getFullName() + "' was not found in topological sorting (Program.java)");
        	        		return null;
        	        	}
        				superT.f2.add(pu);
    				}
    			}
    		}

        }
        /**
         * make sure the basic types and Any and Nil are put first in the list
         */
        ArrayList<ProgramUnit> noSuperListBasicCyanLangFirst = new ArrayList<>();
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Any );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Nil );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Byte );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Int );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Long );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Float );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Double );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Char );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.Boolean );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.CySymbol );
        noSuperListBasicCyanLangFirst.add( (ProgramUnit ) Type.String );
        /*
         * prototypes of package cyan.lang are put next in the list
         */
        for ( ProgramUnit pu : noSuperList ) {
        	if ( NameServer.cyanLanguagePackageName.equals(pu.getCompilationUnit().getPackageName()) &&
        			! NameServer.isBasicType(pu.getName()) ) {
        		noSuperListBasicCyanLangFirst.add(pu);
        	}
        }
        /*
         * then all other prototypes
         */
        for ( ProgramUnit pu : noSuperList ) {
        	if ( ! NameServer.cyanLanguagePackageName.equals(pu.getCompilationUnit().getPackageName()) ) {
        		noSuperListBasicCyanLangFirst.add(pu);
        	}
        }
        noSuperList = noSuperListBasicCyanLangFirst;
        /**
         * do the topological sorting
         */
        ArrayList<ProgramUnit> sortedProgramUnit = new ArrayList<>();
        while ( noSuperList.size() > 0 ) {
        	ProgramUnit pu = noSuperList.get(0);
        	noSuperList.remove(0);
        	sortedProgramUnit.add(pu);

        	Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> t = protoNameAdjList.get(pu);
    		ArrayList<ProgramUnit> subPUList = t.f2;
    		/*
    		 * remove all edges from sub-types to the super-type pu
    		 */
    		for ( ProgramUnit subProto : subPUList ) {
            	Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> subT = protoNameAdjList.get(subProto);
            	/*
            	 * remove pu from the list of super-types of subT
            	 */
            	int i = 0;
            	for ( ProgramUnit superSubProto : subT.f1 ) {
            		if ( superSubProto == pu ) {
                    	subT.f1.remove(i);
            			break;
            		}
            		++i;
            	}
            	/*
            	 * pu must have been found in list subT.f1 --- no test if it was really found
            	 */
            	if ( subT.f1.size() == 0 ) {
            		/*
            		 * now subProto has no super-types (the edge was removed).
            		 */
            		noSuperList.add(subProto);
            	}

    		}
    		/*
    		 * remove all edges from the super-type pu to its sub-types
    		 */
    		t.f2.clear();
        }

        for ( ProgramUnit pu : programUnitList ) {
        	Tuple2<ArrayList<ProgramUnit>, ArrayList<ProgramUnit>> t = protoNameAdjList.get(pu);
    		ArrayList<ProgramUnit> superList = t.f1;
    		if ( superList.size() > 0 ) {
        		String puName = pu.getFullName();
        		String s = puName;
        		for ( ProgramUnit superPU : superList ) {
        			String superFullName = superPU.getFullName();
        			if ( superFullName.equals(puName) ) {
        				break;
        			}
        			s += ", " + superFullName;
        		}
        		s += ", " + puName;
    			env.error(pu.getFirstSymbol(), "This program unit is part of a circular inheritance/interface implementation: '" + s + "'");
    		}

        }
        ArrayList<String> sortedStrProgramUnitList = new ArrayList<>();
        for ( ProgramUnit pu : sortedProgramUnit ) {
    		String protoJavaName;
    		if ( pu instanceof InterfaceDec ) {
    			protoJavaName = NameServer.getJavaName(
    					NameServer.prototypeFileNameFromInterfaceFileName( ((InterfaceDec) pu).getFullName() ) );
    		}
    		else
    			protoJavaName = pu.getJavaName();
    		sortedStrProgramUnitList.add(protoJavaName);

        }
        return sortedStrProgramUnitList;
    }



	private boolean createMainJavaClass(String mainPackageName, String mainPrototypeName, ObjectDec mainPrototype, CyanPackage mainPackage, Env env) {

		String newFileName = null;

		FileOutputStream fos = null;
		try {
			//newFileName = project.getProjectDir();


			String projectName = this.project.getProjectName();
			int sizeProjectName = projectName.length();
			mainJavaClassWithoutExtensionName = Character.toUpperCase(projectName.charAt(0)) + projectName.substring( 1, sizeProjectName );

			newFileName =  mainPackage.getOutputDirectory() + NameServer.fileSeparator +
					mainJavaClassWithoutExtensionName + ".java";

			fos = new FileOutputStream(newFileName);


			PrintWriter printWriter = new PrintWriter(fos, true);
			PW pw = new PW();
			pw.set(printWriter);

			pw.println("package " + mainPackageName + ";\n");
			pw.println("import cyan.lang.*;");
			pw.println("import cyanruntime.*;");
			pw.println("class " + mainJavaClassWithoutExtensionName + " { ");
			pw.add();
			pw.printlnIdent("public static void main(String []args) { ");
			pw.add();

	        pw.printlnIdent("NonExistingJavaClass doNotExist = new NonExistingJavaClass();");
			/*
        // for all prototypes of Java class _Prototype (Cyan Prototype)
             proto = new _Prototype(doNotExit);
        // for all prototypes of Java class _Prototype (Cyan Prototype)
            proto.initPrototype();
			 *
			 */
	        ArrayList<String> javaProgramUnitNameList = this.topologicalSortingProgramUnitList(env);
	        for ( String javaProgramUnitName : javaProgramUnitNameList ) {
        		pw.printlnIdent(javaProgramUnitName + ".prototype = new " + javaProgramUnitName + "(doNotExist);");
	        }


	        /*
	        for ( CompilationUnit cunit : this.compilationUnitList ) {
	        	if ( !cunit.hasGenericPrototype() ) {
		        	for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
		        		String protoJavaName;
		        		if ( pu instanceof InterfaceDec ) {
		        			protoJavaName = NameServer.getJavaName(
		        					NameServer.prototypeFileNameFromInterfaceFileName( ((InterfaceDec) pu).getFullName() ) );
		        		}
		        		else
		        			protoJavaName = pu.getJavaName();
		        		protoJavaNameList.add(protoJavaName);
		        		pw.printlnIdent(protoJavaName + ".prototype = new " + protoJavaName + "(doNotExist);");
		        	}
	        	}
	        }
			*/

	        for ( String javaProgramUnitName : javaProgramUnitNameList ) {
        		pw.printlnIdent(javaProgramUnitName + ".prototype.initPrototype();");

	        }
	        /*
	        for ( CompilationUnit cunit : this.compilationUnitList ) {
	        	if ( !cunit.hasGenericPrototype() ) {
		        	for ( ProgramUnit pu : cunit.getProgramUnitList() ) {
		        		String protoJavaName = pu.getJavaName();
		        		pw.printlnIdent(protoJavaName + ".initPrototype();");
		        	}
	        	}
	        }
	        */


			String realParameter = "";
			ArrayList<MethodSignature> runMSList = mainPrototype.searchMethodPrivateProtectedPublic("run:1");


			if ( runMSList != null && runMSList.size() > 0 ) {
				if ( runMSList.size() > 1 ) {
					try {
						env.error(null,  "According to the project file of this program (.pyan), the main prototype is '"
								+ mainPackageName + "." + mainPrototypeName + "'. However, the 'run' method of this prototype is a overloaded method, " +
								"which is illegal"
								);
					}
					catch ( CompileErrorException e ) { }
					return false;
				}
				MethodSignatureWithSelectors msRun = (MethodSignatureWithSelectors ) runMSList.get(0);
				String fullNameType = msRun.getSelectorArray().get(0).getParameterList().get(0).getType().getFullName();
				if ( !fullNameType.equals("cyan.lang.Array<String>") ) {
					env.error(msRun.getMethod().getFirstSymbol(), "This is the main method, the one in which "
							+ "the execution will start. It should have a parameter of type 'Array<String>' or no parameter at all (a unary method)");
				}

			}


			ArrayList<MethodSignature> unaryRunMSList = mainPrototype.searchMethodPrivateProtectedPublic("run");
			boolean ok = true;
			if ( runMSList == null || runMSList.size() == 0 ) {
				runMSList = unaryRunMSList;
				if ( runMSList == null || runMSList.size() == 0 ) {
					ok = false;
				}
			}
			else if (unaryRunMSList != null && unaryRunMSList.size() > 0 ) {
				/*
				 * both 'run:' and 'run' in the same main prototype
				 */
				try {
					env.error(null,  "According to the project file of this program (.pyan), the main prototype is '"
							+ mainPackageName + "." + mainPrototypeName + "'. This prototype has both a 'run' and an 'run:' method"
							);
				}
				catch ( CompileErrorException e ) { }
				return false;
			}
			if ( ! ok ) {
				try {
					env.error(null,  "According to the project file of this program (.pyan), the main prototype is '"
							+ mainPackageName + "." + mainPrototypeName + "'. However, this prototype does not have a appropriate 'run' " +
							"method. It should be without parameters or with just parameter 'Array<String>'"
							);
				}
				catch ( CompileErrorException e ) { }
				catch ( Throwable e ) {
					System.out.println("Internal error in Program. Class name: " + e.getClass().getName());
				}
				return false;
			}
			ArrayList<MethodSignature> initMSList = mainPrototype.searchInitNewMethod("init");
			if ( initMSList == null || initMSList.size() == 0 ) {
				try {
					env.error(null,  "According to the project file of this program (.pyan), the main prototype is '"
							+ mainPackageName + "." + mainPrototypeName + "'. However, there is no 'init' method in this prototype, " +
							"which is illegal"
							);
				}
				catch ( CompileErrorException e ) { }
				return false;
			}
			MethodSignature runMethodSignature = runMSList.get(0);
			ArrayList<ParameterDec> paramDecList = runMethodSignature.getParameterList();
			pw.printlnIdent("try { ");
			pw.add();
			if ( paramDecList != null && paramDecList.size() > 0 ) {
				// 'run: T t', with one parameter
				if ( ! paramDecList.get(0).getType().getFullName().equals(NameServer.cyanLanguagePackageName + ".Array<String>") ) {
					ok = false;
				}
				else {
					String javaAddName = NameServer.getJavaNameOfMethodWith("add:", 1);
					String arrayStringName = NameServer.getJavaName("Array<String>");
					pw.printlnIdent(arrayStringName + " cyanArgs = new " + arrayStringName + "();");

					pw.printlnIdent("for (int i = 0; i < args.length; ++i) {");
					pw.add();
					pw.printlnIdent("cyanArgs." + javaAddName + "( new CyString(args[i]) );");
					pw.sub();
					pw.printlnIdent("}");
					realParameter = "cyanArgs";
				}
				//         _Program.prototype._run(cyanArgs);
				pw.println("        " + mainPrototype.getJavaName() + ".prototype." + NameServer.getJavaNameOfMethodWith("run:", 1) +
				           "(" + realParameter + ");");

			}
			else
				pw.println("        (new " + mainPrototype.getJavaName() + "())._run();");
			pw.sub();
			pw.printlnIdent("}");
			pw.printlnIdent("catch ( IndexOutOfBoundsException  e ) {\n");
			pw.add();
			pw.printlnIdent("System.out.println(\"Index of array out of bounds. Remember that you cannot add \"\n");
			pw.printlnIdent("  + \" an element to an array using indexing like in\\n\"\n");
			pw.printlnIdent("  + \"    var v = Array<Int> new: 10;\\n    v[0] = 5;\\n\"\n");
			pw.printlnIdent("  + \"This results in the exception 'ExceptionIndexOutOfBounds'. You should use method 'add:' instead:\\n\"\n");
			pw.printlnIdent(" + \"    var v = Array<Int> new: 10;\\n    v add: 5;\\n\");\n");
			pw.printlnIdent("System.out.println(\"This same error occurs when using method 'add:' as in\\n    v add: 5, 0;\\n\");");
			pw.sub();
			pw.printlnIdent("}");





			pw.println("        catch (Throwable e) {");
			pw.println("            System.out.flush();");
			pw.println("            if ( e instanceof ExceptionContainer__ ) {");
			pw.println("                String messageToWrite = null;");
			pw.println("                if ( ((ExceptionContainer__) e).elem instanceof _ExceptionMethodNotFound ) {");
			pw.println("                    _ExceptionMethodNotFound e1 = (_ExceptionMethodNotFound ) ((ExceptionContainer__) e).elem;");
			pw.println("                    if ( e1._message() != null && e1._message().s.length() > 0 ) {");
			pw.println("                        messageToWrite = e1._message().s;");
			pw.println("                    }");
			pw.println("                    else {");
			pw.println("                        System.out.println(\"Method was not found. Exception "
					           + "_ExceptionMethodNotFound was thrown but not caught\");");
			pw.println("                    }");
			pw.println("                }");
			pw.println("                else if ( ((ExceptionContainer__) e).elem instanceof _ExceptionCast) {");
			pw.println("                    _ExceptionCast e1 = (_ExceptionCast ) ((ExceptionContainer__) e).elem;");
			pw.println("                    if ( e1._message() != null && e1._message().s.length() > 0 ) {");
			pw.println("                        messageToWrite = e1._message().s;");
			pw.println("                    }");
			pw.println("                }");
			pw.println("                if ( messageToWrite != null )");
			pw.println("                    System.out.println(messageToWrite);");
			pw.println("                else");
			pw.println("                    System.out.println(\"Exception \" + ((ExceptionContainer__)  e).elem.getClass().getName() + \" was thrown but not caught\");");
			pw.println("            }");
			pw.println("            else {");
			pw.println("                System.out.println(\"Java exception \" + e.getClass().getName() + \" was thrown but not caught\");");
			pw.println("            }");
			pw.println("            System.out.flush();");
			pw.println("            e.printStackTrace();");

			pw.println("        }");

			pw.sub();
			pw.printlnIdent("}");
			pw.println("}");
			pw.sub();

			printWriter.close();

		}
		catch ( FileNotFoundException e ) {
			env.error(null,  "Cannot create file " + newFileName);
		}
		catch ( NullPointerException e ) {
			// e.printStackTrace();
			env.error(null,  "null pointer exception" );
		}
		catch (Exception e ) {
			env.error(null, "error in writing to file " + newFileName);
		}
		finally {
			try {
				if ( fos != null ) fos.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	public Project getProject() {
		return project;
	}

	public ArrayList<CompilationUnit> getCompilationUnitList() {
		return compilationUnitList;
	}



	/** calculates the type of all method parameters, all return values of methods,
	 * and all instance variables of all compilation units of the program.
	 * The types depends on the packages imported each compilation unit
	 * */

	public void calcInternalTypes(Env env) {

		// env.searchPackage("cyan.lang").checkPublicNonGenericProgramUnit();

		Type.IMapName = env.searchPackagePrototype(NameServer.cyanLanguagePackageName, NameServer.IMapName);
		Type.ISetName = env.searchPackagePrototype(NameServer.cyanLanguagePackageName, NameServer.ISetName);
		makeMetaobjectAnnotationsCommunicateInPackage(env);

		ICompiler_dsa compiler_dsa = new Compiler_dsa(env);


		/*for ( CompilationUnit compilationUnit : compilationUnitList )
			if ( ! compilationUnit.getHasGenericPrototype() )
				compilationUnit.calcInternalTypes(env); */
		int i = 0;
		while ( i < compilationUnitList.size() ) {
			// int mySize = compilationUnitList.size();
			CompilationUnit compilationUnit = compilationUnitList.get(i);
			if ( ! compilationUnit.getHasGenericPrototype() && compilationUnit.getErrorList().size() == 0 ) {
				try {
					compilationUnit.calcInternalTypes(compiler_dsa, env);
				}
				catch ( CompileErrorException e ) {
				}
				catch ( Throwable e ) {
					e.printStackTrace();
					env.error(null, "Compiler internal error: exception '" + e.getClass() + "' was thrown "
							+ "but it was not caught. Its message is '" + e.getMessage() + "'");
				}
			}
			++i;
		}
		if ( env.getCompInstSet().contains(CompilationInstruction.matchExpectedCompilationErrors) )
			checkErrorMessages(env);
	}

	/**
	 * check whether all error messages demanded by calls to metaobject compilationError were really signaled.
	 * A metaobject may have implemented interface {@link meta#IInformCompilationError} and informed the compiler
	 * that an error should be signaled. If any did, this method
	 * checks whether the error message passed as parameter was foreseen. If it wasn´t, a warning is signaled.

	 */
	public void checkErrorMessages(Env env) {

		for ( CompilationUnit compUnit : compilationUnitList ) {

			if ( compUnit.getLineMessageList() != null && compUnit.getLineMessageList().size() > 0 ) {
				// Check whether
				// the compiler signaled any errors. It should.
				if ( compUnit.getErrorList() != null ) {
					for ( UnitError unitError : compUnit.getErrorList() ) {
						int line = unitError.getLineNumber();
						Tuple3<Integer, String, Boolean> found = null;
						for ( Tuple3<Integer, String, Boolean> t : compUnit.getLineMessageList() ) {
							if ( t.f1 == line ) {
								if ( found != null ) {
									env.error(null,  "More than one metaobject implementing interface 'IInformCompilationError' points that " +
											"there should be an error in line " + line, false, true );
								}
								found = t;
							}
						}
						if ( found == null ) {
							env.error(null,  "The compiler points an error at line " + line + " although no metaobject implementing interface 'IInformCompilationError' points an " +
									"error at this line " + line, false, true );
						}
						else
							found.f3 = true;
					}
				}
			}
		}
	}

	public void checkErrorMessagesAllCompilationUnits(Env env) {
		for ( CompilationUnit compUnit : compilationUnitList )  {
			for ( Tuple3<Integer, String, Boolean> t : compUnit.getLineMessageList() ) {
				if ( ! t.f3 ) {
					env.error(null,  "A metaobject implementing interface 'IInformCompilationError' points an error at line " + t.f1 +
							" with message '" + t.f2 + "' although this error is not signaled by the compiler", false, true);
				}
			}
		}


	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setCyanLangPackage(CyanPackage cyanLangPackage) {
		cyanLangPrototypeNameTable = new HashSet<String>();
		for ( CompilationUnit compilationUnit : cyanLangPackage.getCompilationUnitList() ) {
			String s = compilationUnit.getFileNameWithoutExtension();
			int i = s.indexOf('(');
			if ( i < 0 )
				i = s.length();
			String name = s.substring(0,  i);
			cyanLangPrototypeNameTable.add(name);
		}
	}

	/**
	 * return true if 'name' is a prototype name of package cyan.lang
	   @param name
	   @return
	 */

	public boolean isInPackageCyanLang(String name) {
		/*
		 * name can be something like "Tuple<main.Person, Int>" or
		 * "cyan.lang.tmp.Tuple<main.Person, Int>"
		 */
		String s = name;
		/*
		 * if name is something like "Proto_Interval<Int>", interfaceName is "Interval<Int>"
		 */
		if ( NameServer.isPrototypeFromInterface(name) ) {
			String interfaceName = NameServer.interfaceNameFromPrototypeName(name);
			return isInPackageCyanLang(interfaceName);
		}

		int indexLessThan = name.indexOf('<');
		if ( indexLessThan >= 0 )
			  // eliminates the parameters to the generic prototype
			  // "Tuple<main.Person, Int>" becomes "Tuple" and
			  // "cyan.lang.tmp.Tuple<main.Person, Int>" becomes "cyan.lang.tmp.Tuple"
			s = name.substring(0, indexLessThan);
		int i = s.lastIndexOf('.');
		if ( i >= 0 )
			s = s.substring(i+1);
		return cyanLangPrototypeNameTable.contains(s);
	}



	public HashMap<String, Class<?>> getCyanMetaobjectTable() {
		return cyanMetaobjectTable;
	}

	public void setCyanMetaobjectTable() {

		cyanMetaobjectTable = new HashMap<String, Class<?>>();


    	LinkedList<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    	classLoadersList.add(ClasspathHelper.contextClassLoader());
    	classLoadersList.add(ClasspathHelper.staticClassLoader());
    	Reflections reflections = new Reflections("meta", new SubTypesScanner(false));
    	/*Reflections reflections = new Reflections(new ConfigurationBuilder()
    	         .setScanners(new SubTypesScanner(false), new ResourcesScanner())
    	         .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))));  */
    	Set<Class<? extends CyanMetaobject>> cyanMetaobjectSubclassList = reflections.getSubTypesOf(CyanMetaobject.class);
    	for ( Class<? extends CyanMetaobject> cyanMetaobjectSubclass : cyanMetaobjectSubclassList ) {
    		String s = cyanMetaobjectSubclass.getName();
    		int i = s.lastIndexOf('.');
    		/* int i = s.indexOf('.');
    		int j = i;
    		while ( j >= 0 ) {
    			j = s.substring(i+1).indexOf('.');
    			if ( j >= 0 )
    				i += j;
    		} */
    		String name = s;
    		if ( i >= 0 )
    			name = s.substring(i+1);
    		cyanMetaobjectTable.put(name, cyanMetaobjectSubclass);
    	}
    	cyanMetaobjectTable.put("CyanMetaobject", CyanMetaobject.class);
    	// cyanMetaobjectTable.put("CyanMetaobjectAttachTo", CyanMetaobjectAttachTo.class);
	}

	public boolean getInCalcInterfaceTypes() {
		return inCalcInterfaceTypes;
	}

	public void addCyanPackage( CyanPackage aPackage ) {
		packageList.add(aPackage);
	}

	public ArrayList<CyanPackage> getPackageList() {
		return packageList;
	}


	public void addCompilationUnitToWrite(IReceiverCompileTimeMessageSend exprPrototype) {
		if ( receiverToWriteList == null )
			receiverToWriteList = new HashSet<>();
		this.receiverToWriteList.add(exprPrototype);
	}

	/**
	 * can only be called after step 5
	 */
	public void writePrototypesToFile(Env env) {
		if ( receiverToWriteList != null )
			MyFile.writePrototypesToFile(receiverToWriteList, env);
		receiverToWriteList = null;
	}



	@Override
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		return featureList;
	}

	public void addFeature(Tuple2<String, ExprAnyLiteral> feature) {
		if ( featureList == null )
			featureList = new ArrayList<>();
		else {
			int size = featureList.size();
			for ( int i = 0; i < size; ++i) {
				if ( featureList.get(i).f1.equals(feature.f1) ) {
					// replace
					featureList.set(i, feature);
					return;
				}
			}
		}
		featureList.add(feature);
	}



	public ArrayList<CyanMetaobjectWithAtAnnotation> getAttachedMetaobjectAnnotationList() {
		return attachedMetaobjectAnnotationList;
	}

	public void setAttachedMetaobjectAnnotationList(ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList) {
		this.attachedMetaobjectAnnotationList = attachedMetaobjectAnnotationList;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getNonAttachedMetaobjectAnnotationList() {
		return nonAttachedMetaobjectAnnotationList;
	}

	public void setNonAttachedMetaobjectAnnotationList(ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList) {
		this.nonAttachedMetaobjectAnnotationList = nonAttachedMetaobjectAnnotationList;
	}


	/*
	public void addProjectError(String message) {
		if ( this.projectErrorList == null )
			this.projectErrorList = new ArrayList<>();
		this.projectErrorList.add( new ProjectError(message));
	}

	public ArrayList<ProjectError> getProjectErrorList() {
		return this.projectErrorList;
	}

	 */

	@Override
	public String getName() {
		return this.project.getProjectName();
	}

	@Override
	public DeclarationKind getKind() {
		return DeclarationKind.PROGRAM_DEC;
	}

	public void setCyanLangDir(String cyanLangDir) {
		this.cyanLangDir = cyanLangDir;
	}

	public String getCyanLangDir() {
		return this.cyanLangDir;
	}

	public void addToListAfter_ati(IListAfter_ati annotation) {
		if ( this.afterATImetaobjectAnnotationList == null ) {
			this.afterATImetaobjectAnnotationList = new ArrayList<>();
		}
		this.afterATImetaobjectAnnotationList.add(annotation);
	}

	public ArrayList<String> getClassPathList() {
		return classPathList;
	}

	public void setClassPathList(ArrayList<String> classPathList) {
		this.classPathList = classPathList;
	}



	/**
	 * list of errors that are outside any compilation unit

	private ArrayList<ProjectError> projectErrorList;
	*/


	/**
	 * the list of features associated to this package
	 */
	private ArrayList<Tuple2<String, ExprAnyLiteral>> featureList;

	/**
	 * set of all classes that inherit from CyanMetaobject that were compiled with the compiler
	 */
	HashMap<String, Class<?>> cyanMetaobjectTable;

	/**
	 * table of names of all prototypes of package cyan.lang
	 */
	private Set<String> cyanLangPrototypeNameTable;

	/**
	 * a list of all compilation units of this program
	 */
	private ArrayList<CompilationUnit>  compilationUnitList;



	/**
	 * the project of the program. It has links to all packages of the program,
	 * including cyan.lang.
	 */
	private Project project;
	/**
	 * a list of packages of the program
	 */
	private ArrayList<CyanPackage> packageList;

	/**
	 * a list of JVM package of the program
	 */
	private ArrayList<JVMPackage> jvmPackageList;
	/**
	 * a list of JVM classes that can be imported by the program
	 */
	private Map<String, TypeJavaRef> jvmTypeJavaList;



	/*
	 * true if the method calcInterfaceTypes is executing
	 */
	private boolean	inCalcInterfaceTypes;


	/**
	 * A compile-time message passing to a prototype can be made with {@code .#} such as in <br>
	 * <code>
	 * Function<String>.#writeCode
	 * </code>
	 * this demand that the source code of {@code Function<String>} be written in the directory of the project.
	 * The set below keeps information on which prototypes should be written.
	 * Of course, this only makes sense because metaobjects and the Cyan compiler adds code to
	 * prototypes.
	 */

	private HashSet<IReceiverCompileTimeMessageSend> receiverToWriteList;


	public void setJavaLibDir(String javaLibDir) {
		this.javaLibDir = javaLibDir;
	}
	public ArrayList<String> getSourcePathList() {
		return sourcePathList;
	}

	public void setSourcePathList(ArrayList<String> sourcePathList) {
		this.sourcePathList = sourcePathList;
	}



	public ArrayList<JVMPackage> getJvmPackageList() {
		return jvmPackageList;
	}

	public void addJvmPackageList(JVMPackage jvmPackage) {
		this.jvmPackageList.add(jvmPackage);
	}

	public JVMPackage searchJVMPackage(String packageName) {
		for ( JVMPackage apack : this.jvmPackageList ) {
			if ( apack.getPackageName().equals(packageName) ) {
				return apack;
			}
		}
		return null;
	}
	/**
	 * load the Java packages of this project. Return a list of error messages
	   @return
	 */
	public ArrayList<String> loadJavaPackages() {

		ArrayList<String> errorMessageList = new ArrayList<>();
		for ( String classPath : this.classPathList ) {
			for ( String pathToJar : classPath.split(";") )
			try {
				String msg = loadSingleJarFile(pathToJar);
				if ( msg != null ) {
					errorMessageList.add(msg);
				}
			}
			catch ( ClassNotFoundException e) {
				String msg = "A class of the file '" + pathToJar + "' was not found";
				if ( e.getMessage() != null ) {
					msg += ". The detailed message is '" + e.getMessage() + "'";
				}
				errorMessageList.add(msg);
			}
			catch (IOException e ) {
				errorMessageList.add("Error in reading file '" + pathToJar + "'");
			}
		}
		return errorMessageList;

	}

	public String loadSingleJarFile(String pathToJar) throws IOException, ClassNotFoundException  {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(pathToJar);
			//System.out.println("jar size = " + jarFile.size());
			Enumeration<JarEntry> e = jarFile.entries();

			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls);
			while ( e.hasMoreElements() ) {
			    JarEntry je = e.nextElement();
			    if ( je.isDirectory() || !je.getName().endsWith(".class") ) {
			        continue;
			    }


			    // -6 because of .class
			    String className = je.getName().substring(0,je.getName().length()-6);
			    className = className.replace('/', '.');
				Class<?> aClass = cl.loadClass(className);
				TypeJavaClass javaClass = new TypeJavaClass(aClass);
				this.jvmTypeJavaList.put(aClass.getName(), javaClass);
				JVMPackage jvmPackage = this.searchJVMPackage(aClass.getPackage().getName());
				if ( jvmPackage == null ) {
					jvmPackage = new JVMPackage(aClass.getPackage().getName(), aClass.getPackage());
					this.jvmPackageList.add(jvmPackage);
				}
				else {
					if ( jvmPackage.searchJVMClass(aClass.getName()) != null ) {
						jarFile.close();
						cl.close();
						return "There are two classes with name '" + aClass.getName() + "' in package '" + jvmPackage.getPackageName() + "'";
					}
				}
				jvmPackage.addJVMClass(javaClass);
			}
			cl.close();

		}
		finally {
			if ( jarFile != null )
				jarFile.close();
		}
		return null;
	}




	/**
	 * the directory of the Cyan runtime libraries
	 */
	private String javaLibDir;

	/**
	 * directory of the package cyan.lang
	 */
	private String cyanLangDir;
	/**
	 * contains the call to the Java compiler
	 */
	private String javac;
	/**
	 * contains the call to the compiled Java code
	 */
	private String execCode;

	private String javaForProjectPathLessSlash;

	private String mainJavaClassWithoutExtensionName;

	private ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList;
	private ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList;


	private ArrayList<IListAfter_ati> afterATImetaobjectAnnotationList = null;

	/**
	 * list of class path to be passed to the Java interpreter
	 */
	private ArrayList<String> classPathList;
	/**
	 * list of source paths to be passed to the Java compiler
	 */
	private ArrayList<String> sourcePathList;


	public Map<String, TypeJavaRef> getJvmTypeJavaList() {
		return jvmTypeJavaList;
	}




}

