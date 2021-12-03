package ast;

import java.util.ArrayList;
import java.util.Hashtable;
import ast.GenericParameter.GenericParameterKind;
import error.CompileErrorException;
import error.ErrorKind;
import lexer.Lexer;
import lexer.Token;
import meta.CyanMetaobjectWithAt;
import meta.IActionAssignment_cge;
import meta.ICompileTimeDoesNotUnderstand_dsa;
import meta.ICompiler_dsa;
import meta.MetaInfoServer;
import saci.CompilationStep;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple6;

public class ObjectDec extends ProgramUnit {


	public ObjectDec( ObjectDec outerObject, Token visibility,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList, Lexer lexer) {
		super(visibility, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, outerObject);
		lexer.setProgramUnit(this);

		isAbstract = false;
		isFinal = false;

		interfaceList = new ArrayList<Expr>();
		//contextParameterArray = new ArrayList<ContextParameter>();
		instanceVariableList = new ArrayList<InstanceVariableDec>();
		methodDecList = new ArrayList<MethodDec>();
		initNewMethodDecList = new ArrayList<MethodDec>();
		functionList = new ArrayList<ExprFunction>();
		superobjectExpr = null;
		slotList = new ArrayList<>();
		beforeInnerObjectNonAttachedMetaobjectAnnotationList = null;
		beforeInnerObjectAttachedMetaobjectAnnotationList = null;
		exprFunctionForThisPrototype = null;
		abstractMethodList = new ArrayList<>();
		javaInterfaceList = null;
	}



	@Override
	public void accept(ASTVisitor visitor) {

		visitor.preVisit(this);

		if ( this.superContextParameterList != null ) {
			for ( ContextParameter cp : this.superContextParameterList ) {
				cp.accept(visitor);
			}
		}
		for ( MethodDec m : this.methodDecList ) {
			m.accept(visitor);
		}
		for ( InstanceVariableDec iv : this.instanceVariableList ) {
			iv.accept(visitor);
		}
		visitor.visit(this);
	}


	public void addSlot(SlotDec slot) {
		slotList.add(slot);
	}


	public void addInstanceVariable(InstanceVariableDec instanceVariable) {

		instanceVariableList.add(instanceVariable);
	}

	public void addMethod( MethodDec methodDec ) {
		String name = methodDec.getNameWithoutParamNumber();
		if ( name.compareTo("init") == 0 ||
			 name.compareTo("init:") == 0 ||
			 name.compareTo("new") == 0 ||
			 name.compareTo("new:") == 0 )
			 initNewMethodDecList.add(methodDec);
		else {
			methodDecList.add(methodDec);
			if ( methodDec.isAbstract() ) {
				this.abstractMethodList.add(methodDec);
			}
		}
	}



	public void setSuperobjectExpr(Expr superobject) {
		this.superobjectExpr = superobject;
	}
	public Expr getSuperobjectExpr() {
		return superobjectExpr;
	}

	/**
	 * returns the super-prototype of this object. Returns null if this prototype is "Any"
	 * @return
	 */

	public ObjectDec getSuperobject() {
		return (ObjectDec ) superobject;

	}


	public void setInterfaceList(ArrayList<Expr> interfaceList) {
		this.interfaceList = interfaceList;
	}
	public ArrayList<Expr> getInterfaceList() {
		return interfaceList;
	}

	/**
	 * returns true if this is a context object. Since a context object
	 * may have no parameters, as in
	 *      object List()
	 *         ...
	 *      end
	 * we do not demand that the list have at least one element.
	 * @return
	 */
	public boolean isContextObject() {
		return contextParameterArray != null;
	}


	@Override
	public void genCyan(PWInterface pw, CyanEnv cyanEnv, boolean genFunctions) {

		cyanEnv.atBeginningOfProgramUnit(this);


		ExprGenericPrototypeInstantiation exprGPI = cyanEnv.getExprGenericPrototypeInstantiation();
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.println("@genericPrototypeInstantiationInfo(\"" +
		         cyanEnv.getPackageNameInstantiation() + "\", \"" + cyanEnv.getPrototypeNameInstantiation()
			  + "\", " + exprGPI.getFirstSymbol().getLineNumber() + ", " + exprGPI.getFirstSymbol().getColumnNumber() + ")");
		}

		super.genCyan(pw, cyanEnv, genFunctions);


		pw.println("");
		pw.print(NameServer.getVisibilityString(visibility) + " ");
		if ( isAbstract )
			pw.print("abstract ");
		if ( isFinal )
			pw.print("final ");
		pw.print("object ");
		this.genCyanProgramUnitName(pw, cyanEnv);

		if ( contextParameterArray != null ) {
			pw.print("(");
			int size = contextParameterArray.size();
			for ( ContextParameter p : contextParameterArray ) {
				p.genCyan(pw, false, cyanEnv, true);
				--size;
				if ( size > 0 )
					pw.print(", ");
			}
			pw.print(")");

		}

		pw.print(" ");
		if ( moListBeforeExtendsMixinImplements != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : moListBeforeExtendsMixinImplements ) {
				annotation.genCyan(pw, false, cyanEnv, genFunctions);
				pw.print(" ");
			}
		}

		if ( superobjectExpr != null ) {
			pw.print("extends ");
			superobjectExpr.genCyan(pw, false, cyanEnv, true);
		}
		pw.println("");
		int size = interfaceList.size();
		if ( size > 0 ) {
			pw.print("          implements ");
			for ( Expr t : interfaceList ) {
				t.genCyan(pw, false, cyanEnv, true);
				--size;
				if ( size > 0 )
					pw.print(", ");
			}
			pw.println("");
		}
		pw.add();


		for ( MethodDec m : initNewMethodDecList )
			m.genCyan(pw, false, cyanEnv, true);
		for ( MethodDec m : methodDecList ) {
			m.genCyan(pw, false, cyanEnv, true);
		}
		pw.println("");
		for ( InstanceVariableDec v : instanceVariableList ) {
			if ( ! v.isContextParameter() )
				v.genCyan(pw, false, cyanEnv, genFunctions);
		}


		if ( this.getCompilationUnit().getProgram().getProject().getCompilerManager().getCompilationStep() == CompilationStep.step_10 ) {
			for (ObjectDec objDec : innerPrototypeList ) {
				objDec.genCyan(pw, cyanEnv, genFunctions);
			}
		}

		if ( beforeEndNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation c : this.beforeEndNonAttachedMetaobjectAnnotationList )
				c.genCyan(pw, true, cyanEnv, genFunctions);
		}

		pw.sub();
		pw.println("");
		pw.println("end");
		pw.println("");
		cyanEnv.atEndOfCurrentProgramUnit();

	}


	public void setContextParameterArray(ArrayList<ContextParameter> contextParameterArray) {
		this.contextParameterArray = contextParameterArray;
	}

	public ArrayList<ContextParameter> getContextParameterArray() {
		return contextParameterArray;
	}

	/**
	 * generate an interface in Java containing all public methods of
	 * this prototype. The interface name in Java will be <code>iname</code>. The
	 * file will be put in the same directory as the prototype itself.
	 *
	 */

	public void generateInterface(PWInterface pw, Env env) {


		env.atBeginningOfObjectDec(this);


		String thisPackageName = this.compilationUnit.getPackageIdent().getName();
		pw.print("package " + thisPackageName + ";");


		pw.println();

		if ( this.visibility == Token.PRIVATE )
			pw.print("private ");
		else if ( this.visibility != Token.PACKAGE )
			pw.print("public ");

		pw.printIdent("interface " + NameServer.getJavaName("I" + this.getName()));

		pw.println(" {");
		pw.add();
		for ( MethodDec meth : this.methodDecList ) {
			MethodSignature ms = meth.getMethodSignature();

			ms.genJava(pw, env);
			pw.println(";");
		}

		pw.sub();
		pw.printlnIdent("}");

		env.atEndOfObjectDec();


	}

	@Override
	public void genJava(PWInterface pw, Env env) {

		if ( isGeneric() ) {
			// should not generate code for generic prototypes. In fact, genJava should not even have have been
			// called in this case
			return ;
		}



		env.atBeginningOfObjectDec(this);

		/**
		 * generate private classes for all functions declared in this prototype

		for (ExprFunction function : functionList ) {
			function.genJavaClassForFunction(pw, env);
		}
		*/

		genJavaCodeBeforeClassMetaobjectAnnotations(pw, env);



		int size;

		pw.printlnIdent("");

		pw.printlnIdent("@SuppressWarnings( { \"unused\", \"cast\", \"hiding\" } )");
		if ( this.outerObject == null ) {
			if ( this.visibility == Token.PRIVATE )
				pw.print("private ");
			else if ( this.visibility != Token.PACKAGE )
				pw.print("public ");

			/* if ( this.isAbstract )
				pw.print("abstract ");
			else */
			/*
			 * The String prototype of Cyan is final but it has a sub-prototype, CySymbol.
			 * Then the Java class for String, CyString, cannot be final
			 */
			if ( this.isFinal && this != Type.String )
				pw.print("final ");
		}
		else {
			// inner classes are always private
			pw.print("private ");
		}

		pw.print("class ");
		String className = getJavaNameWithoutPackage();


		pw.print( className );
		pw.print(" ");

		if ( moListBeforeExtendsMixinImplements != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : moListBeforeExtendsMixinImplements ) {
				annotation.genJava(pw, env);
				pw.print(" ");
			}
		}

		if ( superobject != null ) {
			pw.print("extends " + NameServer.getJavaName(superObjectName));
		}

		pw.println("");



		size = interfaceList.size();
		if ( size > 0 ) {
			pw.print("  implements ");
			for ( Expr t : interfaceList ) {
				pw.print(" " + t.getType().getJavaName());
				--size;
				if ( size > 0 )
					pw.print(", ");
			}
			if ( superobject == null ) {
				   // Any or Nil
				for ( String iname : ObjectDec.interfacesImplementedByAny ) {
					pw.print(", " + iname);
				}
				// pw.print(", Cloneable");
			}
			if ( javaInterfaceList != null ) {
				for ( String s : javaInterfaceList ) {
					pw.print(", " + s);
				}
			}
		}
		else {
			if ( superobject == null ) {
				   // Any or Nil
				// pw.print("      implements Cloneable");
				pw.print("  implements ");
				int sizeiiba = ObjectDec.interfacesImplementedByAny.length;
				for ( String iname : ObjectDec.interfacesImplementedByAny ) {
					pw.print(iname);
					if ( --sizeiiba > 0 )
						pw.print(", ");
				}
			}

			if ( javaInterfaceList != null ) {
				if ( superobject != null )
					pw.print("  implements");
				else
					pw.print(",");
				int size2 = this.javaInterfaceList.size();
				if ( javaInterfaceList != null ) {
					for ( String s : javaInterfaceList ) {
						pw.print(" " + s);
						if ( --size2 > 0 )
							pw.print(", ");
					}
				}
			}
		}
		pw.println(" {");


		pw.println("");
		pw.println("	private static final long serialVersionUID = " + NameServer.nextLong() + "L;");

		// start of Java class generation

		StringBuffer refVarInitStr = new StringBuffer();
		StringBuffer refVarInitStrForInitPrototype = new StringBuffer();

		String outs;
		for ( InstanceVariableDec v : instanceVariableList ) {
			if ( ! v.isShared() ) {
				if ( v.getRefType() ) {
					if ( !(v instanceof ast.ContextParameter) ) {
						outs = "        this." + v.getJavaName() + " = new Ref<" + v.getType().getJavaName() + ">();\n";
						refVarInitStr.append(outs);
						refVarInitStrForInitPrototype.append(outs);
					}
					else {
						// v is a context parameter. Then the Ref object should be created only for initializing the prototype
						// itself
						refVarInitStrForInitPrototype.append("        this." + v.getJavaName() + " = new Ref<" + v.getType().getJavaName() + ">();\n");
					}
					if ( v.getExpr() != null ) {
						Type leftType = v.getType();
						Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, leftType);
						IActionAssignment_cge changeCyanMetaobject = null;
				        ObjectDec prototypeFoundMetaobject = null;
				        if ( cyanMetaobjectPrototype != null ) {
				        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
				        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
				        }
				        Expr rightExpr = v.getExpr();
				        Tuple2<String, String> t = rightExpr.genTmpVarJavaAsString(env);
				        String rightExprTmpVar = t.f1;
						if ( changeCyanMetaobject != null ) {
				   			/*
				   			 * assignment is changed by the metaobject attached to the prototype that is
				   			 * the type of the right-hand side
				   			 */

							try {
								rightExprTmpVar = changeCyanMetaobject.cge_changeRightHandSideTo(
					        			prototypeFoundMetaobject,
					        			rightExprTmpVar, rightExpr.getType(env));
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
		       			// regular assignment
						outs = "";
						if ( t.f2 != null && t.f2.length() > 0 )
							outs += "    " + t.f2;
						outs += "\n    this." + v.getJavaName() + ".elem = " + rightExprTmpVar + ";\n";
						refVarInitStr.append(outs);

						refVarInitStrForInitPrototype.append(outs);
						/*

						refVarInitStr.append("    /*");

						refVarInitStrForInitPrototype.append("    /*");


						Tuple2<String, String>  t = v.getExpr().genTmpVarJavaAsString(env); // .genJavaExpr(pw, env);
						refVarInitStr.append(t.f2);
						outs = "    this." + v.getJavaName() + ".elem = " + t.f1 + ";\n";
						refVarInitStr.append(outs);

						refVarInitStrForInitPrototype.append(t.f2);
						refVarInitStrForInitPrototype.append(outs);

						refVarInitStr.append( " *" + "/\n ");
						refVarInitStrForInitPrototype.append( " *" + "/\n ");
						*/


					}
				}
				else if ( v.getExpr() != null ) {


					Type leftType = v.getType();
					Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, leftType);
					IActionAssignment_cge changeCyanMetaobject = null;
			        ObjectDec prototypeFoundMetaobject = null;
			        if ( cyanMetaobjectPrototype != null ) {
			        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
			        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
			        }
			        Expr rightExpr = v.getExpr();
			        Tuple2<String, String> t = rightExpr.genTmpVarJavaAsString(env);
			        String rightExprTmpVar = t.f1;

					if ( changeCyanMetaobject != null ) {
			   			/*
			   			 * assignment is changed by the metaobject attached to the prototype that is
			   			 * the type of the right-hand side
			   			 */

						try {
							rightExprTmpVar = changeCyanMetaobject.cge_changeRightHandSideTo(
				        			prototypeFoundMetaobject,
				        			rightExprTmpVar, rightExpr.getType(env));
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
					else {
						if ( leftType == Type.Any && rightExpr.getType() instanceof InterfaceDec ) {
							rightExprTmpVar = "( " + NameServer.AnyInJava + " ) " + rightExprTmpVar;
						}
					}
	       			// regular assignment
					outs = "";
					if ( t.f2 != null && t.f2.length() > 0 )
						outs += "    " + t.f2;
					outs += "\n    this." + v.getJavaName() + " = " + rightExprTmpVar + ";\n";
					refVarInitStr.append(outs);
					refVarInitStrForInitPrototype.append(outs);

	           		/*
					refVarInitStr.append("    /"
							+ "* ");
					refVarInitStrForInitPrototype.append("    /"
							+ "*");

					Tuple2<String, String>  t = v.getExpr().genTmpVarJavaAsString(env); // .genJavaExpr(pw, env);
					refVarInitStr.append(t.f2);
					outs = "    this." + v.getJavaName() + " = " + t.f1 + ";\n";
					refVarInitStr.append(outs);

					refVarInitStrForInitPrototype.append(t.f2);
					refVarInitStrForInitPrototype.append(outs);

					refVarInitStr.append( " *"
							+ "/\n ");
					refVarInitStrForInitPrototype.append( " *"
							+ "/\n ");
					*/


				   /*


					Tuple2<String, String>  t = v.getExpr().genTmpVarJavaAsString(env); // .genJavaExpr(pw, env);
					refVarInitStr.append(t.f2);
					outs = "    this." + v.getJavaName() + " = " + t.f1 + ";\n";
					refVarInitStr.append(outs);
					refVarInitStrForInitPrototype.append(t.f2);
					refVarInitStrForInitPrototype.append(outs);

					*/
				}
				else {
					String value = "";
					Type t = v.getType();
					if ( t == Type.Boolean )
						value = "CyBoolean.cyFalse";
					else if ( t == Type.Byte )
						value = "new CyByte(0)";
					else if ( t == Type.Char )
						value = "new CyChar('\0')";
					else if ( t == Type.CySymbol )
						value = "new _CySymbol(\"\")";
					else if ( t == Type.Double )
						value = "new CyDouble(0)";
					else if ( t == Type.Float )
						value = "new CyFloat(0)";
					else if ( t == Type.Int )
						value = "CyInt.zero";
					else if ( t == Type.Long )
						value = "new CyLong(0)";
					else if ( t == Type.Short )
						value = "new CyShort(0)";
					else if ( t == Type.String )
						value = "new CyString(\"\")";
					else if ( t == Type.Dyn )
						value = "_Nil.prototype";
					else {
						if ( t instanceof InterfaceDec ) {
							value = NameServer.getJavaName(NameServer.prototypeFileNameFromInterfaceFileName(t.getFullName())) + ".prototype";
						}
						else {
							value = t.getJavaName() + ".prototype";
						}
					}

					refVarInitStrForInitPrototype.append( "this." + v.javaNameWithRef()  + " = " + value + ";");
				}
			}
		}

		env.setStrInitRefVariables(refVarInitStr.toString());
		boolean hasInitOnceMethod = this.searchMethodPrivate(NameServer.initOnce) != null;


		pw.add();
		pw.printlnIdent("public " + className + "(NonExistingJavaClass doNotExit) {");
		pw.add();
		if ( this.superobject != null ) {
			pw.printlnIdent("super(doNotExit);");
		}
		if ( this.outerObject == null ) {
			for ( ObjectDec innerObject : this.innerPrototypeList ) {
				pw.printlnIdent("prototype" + innerObject.getName() +
						" = this.new " + innerObject.getJavaNameWithoutPackage() + "();");
			}

			if ( hasInitOnceMethod ) {
				pw.printlnIdent( NameServer.getJavaName(NameServer.initOnce) + "();");
			}

		}
		pw.sub();
		pw.printlnIdent("}");

		if ( this.outerObject == null )
			createInitPrototypeMethod(pw, refVarInitStrForInitPrototype);


		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation c : attachedMetaobjectAnnotationList )
				c.genJava(pw, env);
		}

		/*
		pw.printlnIdent("public " + getJavaName() + "() { ");
		pw.add();
		for ( InstanceVariableDec v : instanceVariableList ) {
			if ( v.getRefType() )
				pw.printlnIdent(v.getJavaName() + " = new Ref<" + v.getType().getJavaName() + ">();");
		}
		pw.sub();
		pw.printlnIdent("}");
		*/
		/*String classNameWithOuter = className;
		if ( outerObject != null )
			classNameWithOuter = outerObject.getJavaName() + "." + classNameWithOuter;
		env.setCurrentClassNameWithOuter(classNameWithOuter);  */
		for ( InstanceVariableDec v : instanceVariableList )
			v.genJava(pw, env);


		ArrayList<MethodSignature> msInitList = this.searchInitNewMethod("init");
		if ( msInitList.size() == 0 ) {
			pw.printlnIdent("public " + this.getJavaNameWithoutPackage() + "() { }");
		}


		env.setIsInsideInitMethod(true);


		for ( MethodDec m : initNewMethodDecList )
			m.genJava(pw, env);

		env.setIsInsideInitMethod(false);

		for ( MethodDec m : methodDecList ) {
			m.genJava(pw, env);
		}
		if ( this.multiMethodListList != null ) {
			for ( ArrayList<MethodDec> multiMethodList : this.multiMethodListList ) {
				multiMethodList.get(0).genJavaOverloadedMethod(pw, env, multiMethodList);
			}
		}

		if ( this.outerObject == null ) {
			//pw.printlnIdent("public static " + className + " prototype = new " + className + "();");
			pw.printlnIdent("public static " + className + " prototype;");

			for ( ObjectDec innerObject : this.innerPrototypeList ) {
				pw.printlnIdent("private static " + innerObject.getJavaNameWithoutPackage() + " prototype" + innerObject.getName() + ";" );
						//" = prototype.new " + innerObject.getJavaNameWithoutPackage() + "();");

				genJavaVariables(pw, env, true, innerObject.getName(), innerObject);
			}
			genJavaVariables(pw, env, false, "", this);
		}




		boolean hasSharedVariable = false;
		/*
		if ( instanceVariableList.size() > 0 ) {
			pw.printlnIdent("// initialize the instance variables");
			pw.printlnIdent("public void initObject() { ");
			pw.add();
			for ( InstanceVariableDec v : instanceVariableList ) {
				if ( v.isShared() )
					hasSharedVariable = true;
				else if ( v.getExpr() != null ) {
					String exprStr = v.getExpr().genJavaExpr(pw, env);
					if ( v.getRefType() )
						pw.printlnIdent(v.getJavaName() + ".elem = " + exprStr + ";");
					else
						pw.printlnIdent(v.getJavaName() + " = " + exprStr + ";");
				}

			}
			pw.sub();
			pw.printlnIdent("}");
		}
		*/
		if ( this.outerObject == null ) {
			/*
			 * inner classes in Java cannot have static fields
			 */
			boolean hasSharedNonInitializedVariable = false;
			String nonInitStrList = "";
			for ( InstanceVariableDec v : instanceVariableList ) {
				if ( v.isShared() ) {
					hasSharedVariable = true;
					if ( v.getExpr() == null ) {
						hasSharedNonInitializedVariable = true;
						if ( nonInitStrList.length() == 0 )
							nonInitStrList += v.getName();
						else
							nonInitStrList += ", " + v.getName();
					}
					break;
				}
			}
			// boolean hasInitOnceMethod = this.searchMethodPrivate(NameServer.initOnce) != null;

			if ( hasSharedNonInitializedVariable && ! hasInitOnceMethod ) {
				env.error(this.getSymbol(), "This prototype has at least one shared instance "
						+ "variable (" + nonInitStrList + ") that is not initialized in its declaration and it does not "
						+ "have an 'initOnce' method. This is illegal.", true, false);
			}
			if ( hasSharedVariable || hasInitOnceMethod ) {
				pw.printlnIdent("static { ");
				pw.add();
				if ( hasSharedVariable ) {
					for ( InstanceVariableDec v : instanceVariableList ) {
						if ( v.isShared() ) {
							if ( v.getRefType() ) {
								pw.printlnIdent(v.getJavaName() + " = new Ref<" + v.getType().getJavaName() + ">();");
							}
							if ( v.getExpr() != null ) {
								String exprStr = v.getExpr().genJavaExpr(pw, env);
								if ( v.getRefType() )
									pw.printlnIdent(v.getJavaName() + ".elem = " + exprStr + ";");
								else
									pw.printlnIdent(v.getJavaName() + " = " + exprStr + ";");
							}
						}
					}
				}
				//
				pw.sub();
				pw.printlnIdent("}");
			}
		}


		if ( beforeEndNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation c : this.beforeEndNonAttachedMetaobjectAnnotationList )
				c.genJava(pw, env);
		}
		/**
		 * list of full method names
		 */


		/*
		boolean foundFunctionProto = false;
		if ( this.superobject instanceof ObjectDec ) {
			ObjectDec p = (ObjectDec ) this.superobject;
			while ( p != null && p != Type.Any ) {
				if ( p.getIdent().equals("Function") ) {
					foundFunctionProto = true;
					break;
				}
				else
					p = p.getSuperobject(env);
			}
		}
		*/

		/*
		 * methods as objects was temporarily removed from the language
		 */
		/*
		if ( ! foundFunctionProto ) {
			// not Function of sub-prototype of it. Then methods are objects
			/**
			 * list of inner prototypes that represent the methods of this prototype
			 * /

			pw.printlnIdent("static " + Type.Any.getJavaName() + " []prototypeMethodList = { ");
			pw.add();
			int sizePML = methodDecList.size();
			for ( MethodDec m : methodDecList ) {
				pw.printIdent(m.getPrototypeNameForMethod() + ".prototype");
				if ( --sizePML > 0 )
					pw.print(",");
				pw.println();
			}
			pw.printlnIdent("};");
			pw.sub();

		}


		pw.printlnIdent(Type.Any.getJavaName() + " getPrototypeForMethod(String s) { ");
		pw.add();


		if ( foundFunctionProto ) {
			  /*
			   * if this prototype inherits from any of the Function (or is a Function), its methods
			   * are not objects. Therefore this method should always return null
			   * /
			pw.printlnIdent("return null;");
		}
		else {
			pw.printlnIdent("for( int i = 0; i < methodNameList.length; ++i) ");
			pw.add();
			pw.printlnIdent("if ( methodNameList[i].s.equal(s) ) return prototypeMethodList[i];");
			pw.sub();
			//pw.printlnIdent("}");
			pw.sub();
			if ( symbol.getSymbolString().compareTo("Any") != 0 )
				pw.printlnIdent("super.getPrototypeForMethod(s);");
			else
				pw.printlnIdent("return null;");
		}

		pw.printlnIdent("}");
		*/

		/*
		 * for each public or protected method in the super-prototype, generate a private Java method that calls the super method.
		 * Then the method <br>
		 * <code>
		 *    fun m: Int n -> Int { ... } <br>
		 * <code>
		 * in the super-prototype will cause the creation of<br>
		 * <code>
		 *     CyInt  __super_m(CyInt _n) { return super._m(_n); }
		 * </code><br>
		 * in this prototype. This is necessary because for each function is created an inner Java class (in the future,
		 * for each method too). The code of method 'eval:' or 'eval' of this class may use 'super'. But
		 * here 'super' would mean the superclass of this inner Java class, which would be wrong.
		 * Therefore it is necessary to call a private method that calls super.
		 */
		if ( superobject != null && (superobject instanceof ObjectDec) ) {
			ObjectDec superObj = (ObjectDec ) superobject;
			ArrayList<MethodDec> superMethodList = new ArrayList<>();
			for ( MethodDec m : superObj.methodDecList ) {
				if ( m.getVisibility() == Token.PUBLIC || m.getVisibility() == Token.PROTECTED )  {
					superMethodList.add(m);
				}
			}

			if ( this.getOuterObject() == null ) {
				createMethodsToCallSuperMethodsInInnerClasses(pw, env, superMethodList);
			}
		}
		// ! foundFunctionProto &&
		if ( innerPrototypeList != null && innerPrototypeList.size() > 0 ) {
			/*
			 * if this is an inner prototype that inherits from Function, it should not have
			 * inner prototypes corresponding to its methods. These inner
			 * prototypes start their names with NameServer.methodProtoName
			 */
			for ( ObjectDec innerObject : this.innerPrototypeList ) {
				if ( ! NameServer.isNameInnerProtoForMethod(innerObject.getName()) ) {
					innerObject.genJava(pw, env);
					pw.println();
				}
			}
		}
		if ( this != Type.Nil ) {
			if ( this.outerObject == null )
				genJavaMethods(pw, env, false, "");
			else
				genJavaMethods(pw, env, true, getName());
		}

		if ( this.outerObject == null ) {
			/*
			 * inner Java classes cannot have static sections
			 */
			genJavaCodeStaticSectionMetaobjectAnnotations(pw, env);
		}


		genJavaClassBodyDemandedByMetaobjectAnnotations(pw, env);


		pw.sub();
		pw.println("");
		pw.println("}");
		env.atEndOfObjectDec();
	}

	/** create method initPrototype that initializes all instance variables of the prototype with
	 *  default values.
	   @param pw
	   @param refVarInitStr
	 */
	private void createInitPrototypeMethod(PWInterface pw, StringBuffer refVarInitStrForInitPrototype) {
		pw.printlnIdent("public void initPrototype() {");
		pw.add();
		if ( superobject != null ) {
			pw.printlnIdent("super.initPrototype();");
		}
		pw.printlnIdent(refVarInitStrForInitPrototype);
		// pw.printlnIdent(refVarInitStr);


		pw.sub();
		pw.printlnIdent("}");
	}

	/**
	   @param pw
	   @param env
	   @param superMethodList
	 */
	@SuppressWarnings("static-method")
	private void createMethodsToCallSuperMethodsInInnerClasses(PWInterface pw, Env env,
			ArrayList<MethodDec> superMethodList) {
		/*
		 *  Given
		 *         func m: Int n = n
		 *  in the super-prototype, generate
		 * 	     private CyInt _m_super__(CyInt n)  { return super.m(n); }

		 */
		for ( MethodDec m : superMethodList ) {
			if ( m.isAbstract() )
				continue;

			MethodSignature ms = m.getMethodSignature();
			pw.printIdent("private " + ms.getReturnType(env).getJavaName() + " " );

			if ( ms instanceof MethodSignatureWithSelectors ) {

				MethodSignatureWithSelectors msng = (MethodSignatureWithSelectors ) ms;
				pw.print( NameServer.getNamePrivateMethodForSuperclassMethod(msng.getJavaName()) + "( ");
				int sizesa = msng.getParameterList().size();
				int i = 0;
				for ( ParameterDec p : msng.getParameterList() ) {
					pw.print(NameServer.getJavaName(p.getType(env).getFullName(env)) + " p" + i);
					if ( --sizesa > 0 )
						pw.print(", ");
					++i;

				}
				pw.print(" ) { return super." + msng.getJavaName() + "( ");

				sizesa = msng.getParameterList().size();
				i = 0;
				for ( ParameterDec p : msng.getParameterList() ) {
					pw.print("p" + i);
					if ( --sizesa > 0 )
						pw.print(", ");
					++i;
				}

				pw.println(" ); }");

			}
			else if ( ms instanceof MethodSignatureUnary ) {
				/*
				 *  Given
				 *         fun m -> Int = 0
				 *  in the super-prototype, generate
				 * 	     private CyInt __super_m()  { return super.m(); }

				 */

				MethodSignatureUnary msng = (MethodSignatureUnary ) ms;
				pw.print(NameServer.getNamePrivateMethodForSuperclassMethod(msng.getJavaName()) + "() { return super.");
				pw.println(msng.getJavaName() + "(); }");
			}
			else if ( ms instanceof MethodSignatureOperator ) {
				MethodSignatureOperator mso = (MethodSignatureOperator ) ms;
				if ( mso.getOptionalParameter() == null ) {
					// unary operator
					pw.print( NameServer.getNamePrivateMethodForSuperclassMethod(mso.getJavaName()) + "() { return super.");
					pw.println(mso.getJavaName() + "(); }");
				}
				else {
					// binary
					ParameterDec paramDec = mso.getOptionalParameter();
					pw.print( NameServer.getNamePrivateMethodForSuperclassMethod(mso.getJavaName()) + "(" + paramDec.getType().getJavaName() +
							" " + paramDec.getJavaName() + ") { return super.");
					pw.println(mso.getJavaName() + "(" + paramDec.getJavaName() +  "); }");
				}

			}
		}
	}

	protected void genJavaMethods(PWInterface pw, Env env, boolean isInnerProto, String innerProtoName) {

		pw.print("    public String []getInstanceVariableTypeList() { \n");
		pw.print("        return instanceVariableTypeList");
		if ( isInnerProto ) pw.print(innerProtoName);
		pw.print(";\n");
		pw.print("    }\n");

		pw.print("    public String []getInstanceVariableList() { \n");
		pw.print("        return instanceVariableList");
		if ( isInnerProto ) pw.print(innerProtoName);
		pw.print(";\n");
		pw.print("    }\n");

		String currentPrototypeName = getName();
		String currentPrototypeTypeName;
		if ( getCompilationUnit().getIsPrototypeInterface() ) {
			// was created from an interface by the compiler. Use the interface
			// name as parameter
			currentPrototypeTypeName = NameServer.interfaceNameFromPrototypeName(currentPrototypeName);
		}
		else
			currentPrototypeTypeName = currentPrototypeName;

		//ArrayList<MethodSignature> methodSignatureList;
		//methodSignatureList = searchMethodPrivateProtectedPublic("asString:1");
		String defaultIdentNumberJavaName = NameServer.javaNameObjectAny + "." + "defaultIdentNumber__";


		pw.println("    public String asString(int ident) {");
		pw.println("        String s =  \"" + currentPrototypeTypeName + " {\\n\";");
		if ( this.superobject != null ) {
			pw.println("        s = s + \"super(" + superObjectName
					+ "):\"  + super.asStringThisOnly" //+ NameServer.javaName_asStringThisOnly
					+ "( ident + " + defaultIdentNumberJavaName
					+ " );");
		}
		pw.println("        s = s + asStringThisOnly( ident + " + defaultIdentNumberJavaName + ");");
		pw.print("        s = s + getWhiteSpaces(ident) + \"}\\n\";\n");
		pw.print("        return s;\n");
		pw.print("    } \n");



		if ( this.superobject != null ) {
			pw.print("    @Override ");
		}
		else
			pw.print("    ");
		pw.print("    protected String asStringThisOnly(int ident) {\n");
		pw.print("        String s = getWhiteSpaces(ident);");

		for (InstanceVariableDec iv : getInstanceVariableList()) {
			String ivJavaName = NameServer.getJavaName(iv.getName());
			pw.print("        s = s + getWhiteSpaces(ident)" + " + \"" + iv.getName() + ": \" + " + ivJavaName);
			if ( iv.getRefType() ) pw.print(".elem ");
			/*
			 * if ( NameServer.isBasicType(iv.getType()) ) {
			 *
			 * } pw.print(".asString(ident + " + defaultIdentNumberJavaName +
			 * ")");
			 */
			pw.print("+ \"\\n\";\n");
		}
		pw.print("        return s;\n");
		pw.print("    } \n");

		/**
		 * Define only the Java method 'parent'
		 */
		pw.print("    protected " + NameServer.AnyInJava + " parent() {\n");
		String parentRetValue;

		if ( currentPrototypeName.compareTo("Any") == 0 ) {
			parentRetValue = NameServer.AnyInJava;
		}
		else {

			/*
			 * prototype is not Any
			 */
			String superName;
			if ( getSuperobjectExpr() == null )
				superName = "Any";
			else
				superName = getSuperobjectExpr().ifPrototypeReturnsItsName(env);
			parentRetValue = NameServer.getJavaName(superName);
		}
		pw.print("        return " + parentRetValue + ".prototype;\n");
		pw.print("    }\n");


		/**
		 * Define only the Java method 'parent'
		 */
		pw.print("    protected " + NameServer.StringInJava + " prototypePackage() {\n");

		pw.print("        return new " + NameServer.StringInJava + "( \"" +
		    this.compilationUnit.getPackageName() + "\" );\n");
		pw.print("    }\n");



		if ( NameServer.isPrototypeFromInterface(currentPrototypeName) ) {
			pw.print("    static final String prototypeName = \"" + NameServer.interfaceNameFromPrototypeName(currentPrototypeName) + "\";\n");
		}
		else {
			pw.print("    static final String prototypeName = \"" + currentPrototypeName + "\";\n");
		}

		/*
		 * add method getWhiteSpaces to prototype Any
		 */
		if ( getName().compareTo("Any") == 0 ) {
			/*
			pw.print("    protected String getWhiteSpaces(int n) { \n");
			pw.print("        String s = \"\";\n");
			pw.print("        for (int i = 0; i < n; ++i) \n");
			pw.print("            s = s + \" \";\n");
			pw.print("        return s;\n");
			pw.print("    }\n");
			pw.print("    ");
			*/
		}
		else
			pw.print("    @Override ");


		pw.print("    public String getPrototypeName() { return prototypeName; }\n");

		// pw.print(" static final boolean isInterfaceVar = " +
		// ((currentProgramUnit instanceof InterfaceDec) ? "true" : "false") +
		// ";\n");
		pw.print("    protected boolean isInterface() { return "
				+ (getCompilationUnit().getIsPrototypeInterface() ? "true" : "false") + "; }\n\n");

		pw.print("    ");
		if ( this.getSuperobject() != null ) pw.print("@Override");
		pw.print(" public " + NameServer.featureListTypeJavaName + " getFeatureList() { return featureList");
		if ( isInnerProto ) pw.print(innerProtoName);

		pw.println("; }");

		pw.print("    ");
		if ( this.getSuperobject() != null ) pw.print("@Override");
		pw.print(
				" public " + NameServer.slotFeatureListTypeJavaName + " getSlotFeatureList() { return slotFeatureList");
		if ( isInnerProto ) pw.print(innerProtoName);

		pw.println("; }");

		pw.print("    ");
		if ( this.getSuperobject() != null ) pw.print("@Override");
		pw.print(" public " + NameServer.annotListTypeJavaName + " getAnnotList() { return annotList");
		if ( isInnerProto ) pw.print(innerProtoName);
		pw.println("; }");

	}

	protected void genJavaVariables(PWInterface pw, Env env, boolean isInnerProto, String innerProtoName, ObjectDec programUnit) {
		pw.print("\n    static final String []instanceVariableList");
		if ( isInnerProto )
			pw.print(innerProtoName);
		pw.print(" = { ");
		int size = getInstanceVariableList().size();
		for ( InstanceVariableDec iv : getInstanceVariableList() ) {
			pw.print("\"" + iv.getName() + "\"");
			if ( --size > 0 )
				pw.print(", ");
		}
		pw.print(" };\n");

		pw.print("    static final String []instanceVariableTypeList");
		if ( isInnerProto )
			pw.print(innerProtoName);

		pw.print(" = { ");
		size = getInstanceVariableList().size();
		if ( this. getInstanceVariableList() != null ) {
			for ( InstanceVariableDec iv : getInstanceVariableList() ) {
				pw.print("\"" + iv.getType().getFullName() + "\"");
				if ( --size > 0 )
					pw.print(", ");
			}
		}
		pw.print(" };\n");

		pw.print("    public static " + NameServer.featureListTypeJavaName + " featureList");
		if ( isInnerProto )
			pw.print(innerProtoName);
		pw.print(" = new " + NameServer.featureListTypeJavaName +
				"();\n");
		pw.print("    public static " + NameServer.annotListTypeJavaName + " annotList");
		if ( isInnerProto )
			pw.print(innerProtoName);

		pw.print(" = new " + NameServer.annotListTypeJavaName + "();\n");

		pw.print("\n");
		pw.print("    static final " + NameServer.slotFeatureListTypeJavaName
				+ " slotFeatureList");
		if ( isInnerProto )
			pw.print(innerProtoName);

		pw.print(" = new " + NameServer.slotFeatureListTypeJavaName + "();\n");

		pw.printIdent("static CyString []methodNameList");
		if ( isInnerProto )
			pw.print(innerProtoName);
		pw.println(" = { ");
		pw.add();
		int sizeML = programUnit.getMethodDecList().size();
		for ( MethodDec m : programUnit.getMethodDecList() ) {
			pw.printIdent("new CyString(\"" + m.getMethodInterface(env) + "\")");
			if ( --sizeML > 0 )
				pw.print(",");
			pw.println();
		}
		pw.printlnIdent("};");
		pw.sub();

		pw.print("\n");

	}


	/**
	 * return the instance variable whose name is "name". null if not found
	 */
	@Override
	public InstanceVariableDec searchInstanceVariable(String name) {
		for ( InstanceVariableDec iv :  instanceVariableList )
			if ( iv.getName().compareTo(name) == 0 )
				return iv;
		return null;
	}

	public void setIsAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean getIsAbstract() {
		return isAbstract;
	}

	public void setIsFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	@Override
	public boolean getIsFinal() {
		return isFinal;
	}

	public ArrayList<ExprFunction> getFunctionList() {
		return functionList;
	}

	public void addToBeCreatedFunction(ExprFunction function) {
		functionList.add(function);
	}
	/**
	 * returns the instance variable whose name is varName of this prototype and
	 * protected instance variables of super-prototypes
	 */
	@Override
	public InstanceVariableDec searchInstanceVariableDec(String varName) {
		for (InstanceVariableDec instanceVariableDec : this.instanceVariableList)
			if ( instanceVariableDec.getName().compareTo(varName) == 0 )
				return instanceVariableDec;
		return null;
	}


	/**
	 * returns the instance variable whose name is varName of this prototype and
	 * protected instance variables of super-prototypes
	 */
	@Override
	public InstanceVariableDec searchInstanceVariablePrivateProtectedSuperProtected(String varName) {
		for (InstanceVariableDec instanceVariableDec : this.instanceVariableList)
			if ( instanceVariableDec.getName().compareTo(varName) == 0 )
				return instanceVariableDec;
		if ( this.superobject != null && this.superobject instanceof ObjectDec ) {
			return ((ObjectDec ) superobject).searchInstanceVariableDecProtected(varName);
		}
		return null;
	}

	/**
	 * returns the protected instance variable of this prototype whose name is varName.
	 * It includes inherited ivs.
	 */
	@Override
	public InstanceVariableDec searchInstanceVariableDecProtected(String varName) {
		for (InstanceVariableDec instanceVariableDec : this.instanceVariableList)
			if ( instanceVariableDec.getVisibility() == Token.PROTECTED &&
			     instanceVariableDec.getName().compareTo(varName) == 0 )
				return instanceVariableDec;
		if ( this.superobject != null && this.superobject instanceof ObjectDec ) {
			return ((ObjectDec ) superobject).searchInstanceVariableDecProtected(varName);
		}
		return null;
	}


	/**
	 * search methods with name 'methodName'. This method name says all. First in the list are the sub-prototype methods.
	 */

	@Override
	public ArrayList<MethodSignature> searchMethodPrivateProtectedPublicSuperProtectedPublic(String methodName, Env env) {
		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();

		String s = "";
		for ( MethodDec m : methodDecList ) {
			s += "'" + m.getName() + "'  ";
			if ( m.getName().equals(methodName) )
				methodSignatureList.add(m.getMethodSignature());
		}


		for ( MethodDec m : initNewMethodDecList ) {
			if ( m.getVisibility() == Token.PUBLIC && m.getName().equals(methodName) )
				methodSignatureList.add(m.getMethodSignature());
		}

		ObjectDec proto = this.getSuperobject();
		while ( proto != null ) {
			ArrayList<MethodSignature> superMSList = new ArrayList<MethodSignature>();
			superMSList = proto.searchMethodProtectedPublic(methodName);
			if ( superMSList != null )
				methodSignatureList.addAll(superMSList);
			proto = proto.getSuperobject();
		}
		return methodSignatureList;
	}

	/**
	 * search methods with name 'methodName'. The method name says it all. First in the list are the sub-prototype methods.
	 */
	@Override
	public ArrayList<MethodSignature> searchMethodPublicSuperPublic(
			String methodName, Env env) {
		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();

		String s = "";
		for ( MethodDec m : methodDecList ) {
			if ( m.getVisibility() == Token.PUBLIC && m.getName().equals(methodName) ) {
				methodSignatureList.add(m.getMethodSignature());
			}
			s += "'" + m.getName() + "'  ";
		}

		for ( MethodDec m : initNewMethodDecList ) {
			if ( m.getVisibility() == Token.PUBLIC && m.getName().equals(methodName) )
				methodSignatureList.add(m.getMethodSignature());
		}


		ObjectDec proto = this.getSuperobject();
		if ( proto != null ) {
			ArrayList<MethodSignature> superMSList = new ArrayList<MethodSignature>();
			superMSList = proto.searchMethodPublicSuperPublic(methodName, env);
			if ( superMSList != null )
				methodSignatureList.addAll(superMSList);
		}
		return methodSignatureList;
	}


	/**
	 * search methods with name 'methodName' in super-prototypes and in implemented interfaces
	 */
	public ArrayList<MethodSignature> searchMethodPublicSuperPublicProtoAndInterfaces(
			String methodName, Env env) {
		ArrayList<MethodSignature> methodSignatureList = this.searchMethodPublicSuperPublic(methodName, env);
		if ( methodSignatureList == null ) {
			methodSignatureList = new ArrayList<MethodSignature>();

		}
		if ( interfaceList != null ) {
			for ( Expr exprInter : this.interfaceList ) {
				InterfaceDec inter = (InterfaceDec ) exprInter.getType();
				for ( MethodSignature ms : inter.getAllMethodSignatureList() ) {
					if ( ms.getName().equals(methodName) ) {
						methodSignatureList.add(ms);
					}
				}
			}
		}
		return methodSignatureList;
	}

	/**
	 * search methods with name 'methodName' in implemented interfaces
	 */
	public ArrayList<MethodSignature> searchMethodPublicSuperPublicImplementedInterfaces(
			String methodName, Env env) {

		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();
		if ( interfaceList != null ) {
			for ( Expr exprInter : this.interfaceList ) {
				InterfaceDec inter = (InterfaceDec ) exprInter.getType(env);
				for ( MethodSignature ms : inter.getAllMethodSignatureList() ) {
					if ( ms.getName().equals(methodName) ) {
						methodSignatureList.add(ms);
					}
				}
			}
		}
		return methodSignatureList;
	}



	/**
	 * searches for a method called methodName in this prototype and all its super-prototypes.
	 * Public and protected methods are considered. The signatures of all methods with name "methodName"
	 * are returned. First in the list are the sub-prototype methods.
	 *
	 * @param methodName
	 * @param env
	 * @return
	 */
	@Override
	public ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublic(String methodName, Env env) {

		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();
		for ( MethodDec m : getMethodDecList() ) {
			if (  (m.getVisibility() == Token.PUBLIC || m.getVisibility() == Token.PROTECTED)
					&& m.getName().equals(methodName) )
				methodSignatureList.add(m.getMethodSignature());
		}

		for ( MethodDec m : initNewMethodDecList ) {
			if ( (m.getVisibility() == Token.PUBLIC || m.getVisibility() == Token.PROTECTED) && m.getName().equals(methodName) )
				methodSignatureList.add(m.getMethodSignature());
		}


		ObjectDec superObjectDec = this.getSuperobject();
		if ( superObjectDec != null ) {
			ArrayList<MethodSignature> methodSignatureListSuper =
					superObjectDec.searchMethodProtectedPublicSuperProtectedPublic(methodName, env);
			methodSignatureList.addAll(methodSignatureListSuper);
		}
		return methodSignatureList;
	}

	/**
	 * searches for methods with the same signature as methodSig but with different parameter types (at least one
	 * parameter type should be different). The search is made in this prototype and in super-prototypes
	 * called methodName in this prototype and all its super-prototypes.
	 * Public and protected methods are considered. It is returned a list with signatures of all methods with the same name
	 * as methodSig but with different signature.
	 *
	 * @param methodName
	 * @param env
	 * @return
	 */

	public ArrayList<MethodSignature> searchMethodDiffNameProtectedPublicSuperProtectedPublic(MethodSignature methodSig, Env env) {
		ArrayList<MethodSignature> methodSignatureList = this.searchMethodProtectedPublicSuperProtectedPublic(methodSig.getName(), env);
		ArrayList<MethodSignature> methodSignatureListNonEqual = new ArrayList<>();
		String methodFullName = methodSig.getFullName(env);
		for ( MethodSignature ms : methodSignatureList ) {
			if ( ! ms.getFullName(env).equals(methodFullName) ) {
				methodSignatureListNonEqual.add(ms);
			}
		}
		return methodSignatureListNonEqual;
	}

	/**
	 * return a list of methods in the implemented interfaces that have name methodName. That includes
	 * super-interfaces of implemented interfaces. First in the list are the sub-prototype methods.
	   @param methodName
	   @param env
	   @return
	 */
	public ArrayList<MethodSignature> searchMethodImplementedInterface(String methodName, Env env) {
		ArrayList<MethodSignature> msList = new ArrayList<>();
		for ( Expr exprInterface : this.interfaceList ) {
			InterfaceDec interDec = (InterfaceDec ) exprInterface.getType(env);
			ArrayList<MethodSignature> interList = interDec.searchMethodPublicSuperPublicOnlyInterfaces(methodName, env);
			if ( interList != null ) {
				msList.addAll(interList);
			}
		}
		return msList;
	}
	/**
	 * returns the methods of this prototype with name methodName.
	 * The searches includes public and protected methods.
	 * Super-prototypes are not considered.
	 * @param methodName
	 * @return
	 */

	public ArrayList<MethodSignature> searchMethodProtectedPublic(String methodName) {
		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();
		for ( MethodDec m : methodDecList ) {
			if ( m.getName().equals(methodName)  &&
					(m.getVisibility() == Token.PUBLIC ||
					 m.getVisibility() == Token.PROTECTED) )
				methodSignatureList.add(m.getMethodSignature());
		}
		return methodSignatureList;
	}

	/**
	 * returns the method of this prototype with name methodName.
	 * The searches includes private, protected, and public methods.
	 * Super-prototypes are not considered.
	 * @param methodName with the selectors and number of parameters as <code>"with:2 do:1"</code>
	 * @return
	 */

	public ArrayList<MethodSignature> searchMethodPrivateProtectedPublic(String methodName) {
		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();
		for ( MethodDec m : methodDecList ) {
			String s = m.getName();
			if ( m.getName().equals(methodName) )
				methodSignatureList.add(m.getMethodSignature());
		}
		return methodSignatureList;
	}


	/**
	 * returns all methods of this prototype with interface equal to methodInterface.
	 * An interface is the method signature without the return value type and parameter names.
	 * It is the value returned by method getMethodInterface() of MethodDec.
	 * The searches includes private, protected, and public methods.
	 * Super-prototypes are not considered.
	 * @param methodName
	 * @return
	 */

	public ArrayList<MethodDec> search_Method_Private_Protected_Public_By_Interface(Env env, String methodInterface) {
		ArrayList<MethodDec> methodDecArray = new ArrayList<MethodDec>();
		for ( MethodDec m : methodDecList ) {
			if ( m.getMethodInterface(env).compareTo(methodInterface) == 0 )
				methodDecArray.add(m);
		}
		return methodDecArray;
	}

	/**
	 * returns the method of this prototype with name methodName.
	 * The searches includes public and protected methods.
	 * Super-prototypes are not considered.
	 * @param methodName
	 * @return
	 */

	public MethodDec searchMethodPrivate(String methodName) {
		for ( MethodDec m : methodDecList ) {
			if ( m.getName().equals(methodName)  &&
					(m.getVisibility() == Token.PRIVATE) )
				return m;
		}
		return null;
	}

	/**
	 * Search for a init, init:, new, or new: method in this prototype only
	   @return
	 */
	public ArrayList<MethodSignature> searchInitNewMethod(String name) {
		ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();
		for ( MethodDec m : initNewMethodDecList ) {
			String other = m.getNameWithoutParamNumber();
			if ( other.compareTo(name) == 0 )
				methodSignatureList.add(m.getMethodSignature());
		}
		return methodSignatureList;
	}



	public ArrayList<InstanceVariableDec> getInstanceVariableList() {
		return instanceVariableList;
	}

	public ArrayList<MethodDec> getMethodDecList() {
		return methodDecList;
	}


	@Override
	public boolean isInnerPrototype() {
		return this.outerObject != null;
	}

	@SuppressWarnings("null")
	@Override
	public void calcInternalTypes(ICompiler_dsa compiler_dsa, Env env) {

		env.atBeginningOfObjectDec(this);

		makeMetaobjectAnnotationsCommunicateInPrototype(env);


		//# possible error
		super.calcInternalTypes(compiler_dsa, env);

		if ( env.isThereWasError() ) {
			  /*
			   * there was some error signalled by attached metaobjects (probably
			   * implementing interface ICheckProgramUnit_before_dsa such as
			   * CyanMetaobjectConcept
			   */
			return ;
		}

		if ( superobject != null ) {
			if ( superobject.getIsFinal() && ! (getName().compareTo("CySymbol") == 0 &&
					superobject.getName().compareTo("String") == 0 ) )
				env.error(symbol,
						"Prototype or type " + superobject.getFullName() +
								" is final. It cannot be inherited",
						true, false);

		}

		if ( interfaceList.size() > 0 && env.getProject().getCompilerManager().getCompilationStep().ordinal() >= CompilationStep.step_5.ordinal()
				// prototypes created from interface use the 'default' methods from interfaces.
				// they do not need to implement the interface methods
			 && ! NameServer.isPrototypeFromInterface(this.getName())
				) {
			for ( Expr interfaceExpr : interfaceList )  {
				InterfaceDec anInterface = (InterfaceDec ) interfaceExpr.getType(env);
				for ( MethodSignature ms : anInterface.getMethodSignatureList() ) {
					ArrayList<MethodSignature> other_msList = this.searchMethodProtectedPublicSuperProtectedPublic(ms.getName(), env);
					boolean found = false;
					// ms.calcInterfaceTypes(env);
					String fullName = ms.getFullName(env);
					for ( MethodSignature other_ms : other_msList ) {
						if ( other_ms.getFullName(env).equals(fullName) && ms.getReturnType(env).isSupertypeOf(other_ms.getReturnType(env), env)) {
							found = true;
							if (  other_ms.getMethod().getDeclaringObject() == this &&
									! other_ms.getMethod().getHasOverride() ) {
								env.error(other_ms.getFirstSymbol(), "Prototype '" + this.getName() + "' implements interface '"
										+ anInterface.getFullName() + "' and defines method '"
										+ ms.getFullNameWithReturnType(env) + "' of this interface. This method declaration should be preceded by 'override'",
										true, false);
							}
							break;
						}
					}
					if ( ! found ) {
						env.error(this.symbol, "Prototype '" + this.getName() + "' implements interface '"
								+ anInterface.getFullName() + "' but method '"
								+ ms.getFullNameWithReturnType(env) + "' of this interface is not defined in this prototype", true, false);
					}
				}

			}
		}




		Hashtable<String, MethodDec> methodTable = new Hashtable<String, MethodDec>();
		Hashtable<String, MethodDec> selectorsOnlyTable = new Hashtable<String, MethodDec>();
		   // only the names of the selectors are put in this list.

		MethodDec lastMethodDec = null;
		ArrayList<MethodDec> allMethodList = new ArrayList<MethodDec>();
		allMethodList.addAll(methodDecList);
		allMethodList.addAll(initNewMethodDecList);


		/**
		 * if the prototype declares context parameters, every instance variable that is not a context parameter should be
		 * initialized in its declaration
		 */
		if ( this.hasContextParameter ) {
			for ( InstanceVariableDec varDec : this.instanceVariableList ) {
				if ( ! varDec.isContextParameter() ) {
					if ( varDec.getExpr() == null ) {
						String s = "";
						for ( InstanceVariableDec cp : this.instanceVariableList ) {
							if ( cp.isContextParameter() )
								s = s + cp.getName() + " ";
						}
						env.error(varDec.getFirstSymbol(),  "Instance variable '" + varDec.getName() + "' is not being initialized by the default "
					       + "constructor built from the context parameters " + s, true, false
								);
					}
				}
			}
		}



		for ( SlotDec s : this.slotList ) {
			MethodDec methodDec;
			if ( s instanceof InstanceVariableDec ) {
				//if ( !(s instanceof ContextParameter) )
				//s.calcInternalTypesCTMOCallsPreced(env);
				s.calcInternalTypes(env);
			}
			else if ( s instanceof MethodDec ) {
				methodDec = (MethodDec ) s;

				try {

					methodDec.calcInternalTypes(env);

					String methodSignatureString = methodDec.getMethodSignature().getFullName(env);

					if ( methodDec.getVisibility() == Token.PRIVATE ) {
						if ( methodDec.getHasOverride() )
							env.error(methodDec.getFirstSymbol(),
									"Private methods cannot be declared with 'override' ",
									true, false);
						if ( methodDec.getIsFinal() )
							env.error(methodDec.getFirstSymbol(),
									"Private methods cannot be declared with 'final' ",
									true, false);
						if ( methodDec.isAbstract() )
							env.error(methodDec.getFirstSymbol(),
									"Private methods cannot be abstract",
									true, false);

					}
					if ( methodDec.getHasOverride() ) {
						if ( superobject == null )
							env.error(methodDec.getFirstSymbol(),
									"'override' cannot be used without a supertype",
									true, false);
						else {
							String methodName = methodDec.getName();
							ArrayList<MethodSignature> superMethodSignatureList =
									((ObjectDec ) superobject).searchMethodPublicSuperPublicProtoAndInterfaces(methodName, env);
							ArrayList<MethodSignature> superInterMethodSignatureList = this.searchMethodPublicSuperPublicImplementedInterfaces(methodName, env);
							if ( methodDec.getVisibility() == Token.PROTECTED &&
									(superMethodSignatureList == null || superMethodSignatureList.size() == 0)) {
								if ( this.superobject != null && this.superobject != Type.Any && this.superobject != Type.Nil ) {
									superMethodSignatureList = this.superobject.searchMethodProtectedPublicSuperProtectedPublic(methodName, env);
								}
							}
							if ( (superMethodSignatureList == null || superMethodSignatureList.size() == 0) &&
									(superInterMethodSignatureList == null || superInterMethodSignatureList.size() == 0) ) {
								env.error(methodDec.getFirstSymbol(),
										"This is no method with this same name in super-prototypes. Therefore it should not be preceded by keyword 'override'",
										true, false);
							}
							else {

								if ( superMethodSignatureList != null && superMethodSignatureList.size() > 0 &&
										methodDec.getVisibility() != superMethodSignatureList.get(0).getMethod().getVisibility() )
									env.error(methodDec.getFirstSymbol(),
											"Method is overridden a method with a different visibility (public, protected)",
											true, false);
							}
						}
					}

					if ( methodDec.isAbstract() &&  methodDec.getIsFinal() )
						env.error(methodDec.getFirstSymbol(),
								"Abstract methods cannot be final",
								true, false);


					MethodDec other = methodTable.put(methodSignatureString, methodDec);
					if ( other != null ) {
						if ( other.getCompilerCreatedMethod() )
							env.error(methodDec.getFirstSymbol(), "Internal error at ObjectDec::calcInternalTypes",
									true, false);
						else
							env.error(methodDec.getFirstSymbol(), "Method of line " + other.getFirstSymbol().getLineNumber() +
									" is being duplicated in line " + methodDec.getFirstSymbol().getLineNumber(),
									true, false);
					}
					//String selectorsOnly = methodDec.getMethodSignature().getName();
					String selectorsOnly = methodDec.getMethodSignature().getName();
					String selectorsNameOnly = methodDec.getMethodSignature().getNameWithoutParamNumber();
					if ( methodDec.isIndexingMethod() ) {
						if ( ! selectorsNameOnly.equals("at:") && ! selectorsNameOnly.equals("at:put:") )
							env.error(methodDec.getFirstSymbol(),
									"This method cannot be an indexing method. Only 'at:' and 'at:put:' methods can be indexing methods and be preceded by '[]'",
									true, false);
					}

					// end

					if ( selectorsNameOnly.equals("new") || selectorsNameOnly.equals("new:") ) {
						if ( methodDec.getMethodSignature().getReturnType(env) != this ) {
							boolean foundError = true;
							if ( NameServer.isPrototypeFromInterface(getName()) ) {
								String interfaceName = NameServer.interfaceNameFromPrototypeName(getName());
								ProgramUnit interfaceProto = this.getCompilationUnit().getCyanPackage().searchPublicNonGenericProgramUnit(interfaceName);
								if ( methodDec.getMethodSignature().getReturnType(env) == interfaceProto )
									foundError = false;
							}
							if ( foundError )
								env.error(methodDec.getFirstSymbol(), "'new' with return type different from the prototype",
										true, false);
						}
						if ( methodDec.getVisibility() != Token.PUBLIC )
							env.error(methodDec.getFirstSymbol(), "'new' methods should be public",
									true, false);
						if ( methodDec.getHasOverride() )
							env.error(methodDec.getFirstSymbol(), "'new' methods cannot be declared with keyword 'override'",
									true, false);
						if ( methodDec.isAbstract() )
							env.error(
									methodDec.getFirstSymbol(), "'new' methods cannot be declared with keyword 'abstract'",
									true, false);
						if ( methodDec.getIsFinal() )
							env.error(
									methodDec.getFirstSymbol(), "'new' methods cannot be declared with keyword 'final'",
									true, false);


					}
					else if ( selectorsNameOnly.equals("init") || selectorsNameOnly.equals("init:") ) {
						Type returnTypeInit = methodDec.getMethodSignature().getReturnType(env);
						if (  returnTypeInit != null && returnTypeInit != Type.Nil ) {
							env.error(
									methodDec.getFirstSymbol(), "'init' with return type different from 'Nil'",
									true, false);
						}
						if ( methodDec.getVisibility() != Token.PUBLIC )
							env.error(
									methodDec.getFirstSymbol(), "'init' methods should be public",
									true, false);
						if ( methodDec.getHasOverride() )
							env.error(
									methodDec.getFirstSymbol(), "'init' methods cannot be declared with keyword 'override'",
									true, false);
						if ( methodDec.isAbstract() )
							env.error(
									methodDec.getFirstSymbol(), "'init' methods cannot be declared with keyword 'abstract'",
									true, false);
						if ( methodDec.getIsFinal() )
							env.error(
									methodDec.getFirstSymbol(), "'init' methods cannot be declared with keyword 'final'",
									true, false);
					}


					other = selectorsOnlyTable.put(selectorsOnly, methodDec);
					if ( other != null ) {
						if ( other.getHasOverride() != methodDec.getHasOverride() )
							env.error(methodDec.getFirstSymbol(), "Methods of lines " +  other.getFirstSymbol().getLineNumber() +
											" and " + methodDec.getFirstSymbol().getLineNumber() + " have the same selectors. Both should either " +
											"be declared with keyword 'override' or without it",
									//methodSignatureString,
									true, false
									/*ErrorKind.methods_with_the_same_selectors_with_and_without_override,
									"method0 = \"" + other.getMethodSignatureAsString() + "\"", "method1 = \"" + methodDec.getMethodSignatureAsString() + "\"" */
									);

						if ( other.getIsFinal() != methodDec.getIsFinal() )
							env.error(methodDec.getFirstSymbol(), "Methods of lines " + other.getFirstSymbol().getLineNumber() +
											" and " +  methodDec.getFirstSymbol().getLineNumber() + " have the same selectors. Both should either " +
											"be declared with keyword 'final' or without it",
									/*methodSignatureString,
									ErrorKind.methods_with_the_same_selectors_with_and_without_override,
									"method0 = \"" + other.getMethodSignatureAsString() + "\"", "method1 = \"" + methodDec.getMethodSignatureAsString() + "\""
									*/
									true, false
									);

						if ( other.getVisibility() != methodDec.getVisibility() )
							env.error(methodDec.getFirstSymbol(),
									"Methods of lines " + other.getFirstSymbol().getLineNumber() +
											" and " +  methodDec.getFirstSymbol().getLineNumber() + " have the same selectors." +
											" They should be declared with the same visibility (public,protected, private)",
											true, false /*
									methodSignatureString,
									ErrorKind.methods_with_the_same_selectors_and_different_visibilities,
									"method0 = \"" + other.getMethodSignatureAsString() + "\"", "method1 = \"" + methodDec.getMethodSignatureAsString() + "\""
									*/ );


						if ( lastMethodDec.getMethodSignature().getName().compareTo(selectorsOnly) != 0 ) {
							boolean foundError = true;
							if ( methodDec.getMethodSignature() instanceof MethodSignatureOperator &&
								 other.getMethodSignature() instanceof MethodSignatureOperator ) {
								boolean methodDecHasParameter = ((MethodSignatureOperator ) methodDec.getMethodSignature()).getOptionalParameter() == null;
								boolean otherHashParameter = ((MethodSignatureOperator) other.getMethodSignature()).getOptionalParameter() == null;
								foundError = methodDecHasParameter == otherHashParameter;
							}
							if ( foundError ) {
								if ( other.getCompilerCreatedMethod() )
									env.error(methodDec.getFirstSymbol(), "Internal error at ObjectDec::calcInternalTypes",
											true, false);
								else if ( ! methodDec.getCompilerCreatedMethod() )
									env.error(methodDec.getFirstSymbol(), "Method of line " + methodDec.getFirstSymbol().getLineNumber() +
											" should be declared right after the method of line " + other.getFirstSymbol().getLineNumber(),
										/*
										methodSignatureString,
										ErrorKind.method_should_be_declared_after_previous_method_with_the_same_selectors,
										"method0 = \"" + methodDec.getMethodSignatureAsString() + "\"", "method1 = \"" + other.getMethodSignatureAsString() + "\""
										*/
										true, false);
							}

						}
						Type methodDecReturnType = methodDec.getMethodSignature().getReturnType(env);
						Type otherReturnType = other.getMethodSignature().getReturnType(env);
						if ( methodDecReturnType != null && otherReturnType != null ) {

							if ( methodDecReturnType.getFullName().compareTo(otherReturnType.getFullName()) != 0 ) {
								if ( other.getCompilerCreatedMethod() || methodDec.getCompilerCreatedMethod() )
									env.error(true, methodDec.getFirstSymbol(), "Internal error at ObjectDec::calcInternalTypes",
											methodSignatureString, ErrorKind.internal_error);

								else
									env.error(methodDec.getFirstSymbol(), "Method of line " +  methodDec.getFirstSymbol().getLineNumber() +
													" should be declared with the same return type as the method of line " +  other.getFirstSymbol().getLineNumber(),
											/*
											methodSignatureString,
											ErrorKind.methods_with_the_same_selectors_and_different_return_types,
											"method0 = \"" + other.getMethodSignatureAsString() + "\"", "method1 = \"" + methodDec.getMethodSignatureAsString() + "\""
											*/
											true, false
											);

							}

						}
						else {
							if ( methodDecReturnType != otherReturnType )
								env.error(methodDec.getFirstSymbol(), "Method of line " + other.getFirstSymbol().getLineNumber() +
												" should be declared with the same return type as the method of line " +  methodDec.getFirstSymbol().getLineNumber(),
										/*
										methodSignatureString, ErrorKind.methods_with_the_same_selectors_and_different_return_types, other.getMethodSignatureAsString(), methodDec.getMethodSignatureAsString()
										*/
										true, false												);
						}
					}
					lastMethodDec = methodDec;

				}
				catch ( CompileErrorException e ) {
					return ;
				}
			}
			else {
				env.error(this.getSymbol(), "Internal error at MethodDec::calcInternalTypes: unknown slot class", true, true);
			}
		}

		if ( beforeInnerObjectNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : beforeInnerObjectNonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}

		if ( beforeInnerObjectAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : beforeInnerObjectAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}

		if ( this.innerPrototypeList != null ) {
			if ( this.compilationUnit.getErrorList() == null || this.compilationUnit.getErrorList().size() == 0 ) {
				calcInternalTypesInnerPrototypes(compiler_dsa, env);
			}
		}

		if ( beforeEndNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : beforeEndNonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}

		//super.calcInternalTypes(compiler_dsa, env);

		int envLineShift = env.getLineShift();
		env.setLineShift(0);
		checkInitMethods(env);


		for ( InstanceVariableDec iv : this.instanceVariableList ) {
			String name = iv.getName();
			if ( name.equals("init") || name.equals("new") ) {
				env.error(iv.getFirstSymbol(), "This instance variable has the name 'init' or 'new'. Both are illegal",
						true, false);
			}
			else {
				for ( MethodDec method : this.methodDecList ) {
					if ( method.getMethodSignature() instanceof ast.MethodSignatureUnary && method.getName().equals(name) ) {
						env.error(iv.getFirstSymbol(), "Instance variable '" + name + "' has the same name "
								+ "as the unary method of line " + method.getMethodSignature().getFirstSymbol().getLineNumber(),
								true, false);
					}
				}
			}
		}

		/* if ( this.getName().equals("Program") &&
			this.getCompilationUnit().getProgram().getProject().getCompilerManager().getCompilationStep() == CompilationStep.step_9 )
			MyFile.write(this.getCompilationUnit());  */

		env.setLineShift(envLineShift);
		env.atEndOfObjectDec();

	}

	/**
	 * check whether there are two 'init:' methods with the same number of parameters and a parameter type that is supertype
	 * of the corresponding parameter type of the other 'init:' method.
	 */
	private void checkInitMethods(Env env) {

		ArrayList<MethodDec> initDotList = new ArrayList<>();
		for ( MethodDec method : initNewMethodDecList ) {
			if ( method.getNameWithoutParamNumber().equals("init:") ) {
				initDotList.add(method);
			}
		}
		int size = initDotList.size();
		int analyzed = 0;
		int numParam = 1;
		while ( analyzed < size ) {
			ArrayList<MethodDec> initListSameParamNumber = new ArrayList<>();
			for (int i = 0; i < size; ++i) {
				MethodDec anInit = initDotList.get(i);
				if ( anInit.getMethodSignature() instanceof MethodSignatureWithSelectors) {
					ast.MethodSignatureWithSelectors ms = (ast.MethodSignatureWithSelectors ) anInit.getMethodSignature();
					if ( ms.getSelectorArray().get(0).getParameterList().size() == numParam ) {
						initListSameParamNumber.add(anInit);
					}
				}
			}
			if ( initListSameParamNumber.size() > 1 ) {
				for (int k = 0; k < initListSameParamNumber.size(); ++k ) {
					MethodDec firstInit =  initListSameParamNumber.get(k);
					ArrayList<ParameterDec> firstParamList = ((MethodSignatureWithSelectors )
							firstInit.getMethodSignature()).getSelectorArray().get(0).getParameterList();
					for (int j = k + 1; j < initListSameParamNumber.size(); ++j ) {
						MethodDec secondInit = initListSameParamNumber.get(j);
						ArrayList<ParameterDec> secondParamList = ((MethodSignatureWithSelectors )
								secondInit.getMethodSignature()).getSelectorArray().get(0).getParameterList();
						for (int p = 0; p < numParam; ++p ) {
							Type firstType = firstParamList.get(p).getType(env);
							Type secondType = secondParamList.get(p).getType(env);
							if ( firstType != secondType ) {
								if ( firstType.isSupertypeOf(secondType, env) )  {
									env.error( firstInit.getFirstSymbol(),
											"The types of the " + NameServer.ordinal(p+1) + " parameter of methods 'init:' of lines " +
									       firstInit.getFirstSymbol().getLineNumber() + " and " +
									         secondInit.getFirstSymbol().getLineNumber() + " are incompatible. "
									         		+ "Type '" + firstType.getFullName() + "' is supertype of "
															+ "type '" + secondType.getFullName() + "'. That is illegal because it can cause "
											+ "ambiguity when method 'new:' is called. Use Unions instead of two 'init:' methods. For "
											+ "example, replace 'func init: Animal p { }' and 'func init: Cow p { }' with "
											+ "'func init: Cow|Animal p { }'",
											true, false, false);
								}
								else if ( secondType.isSupertypeOf(firstType, env) ) {
									env.error( firstInit.getFirstSymbol(),
											"The types of the " + NameServer.ordinal(p+1) + " parameter of methods 'init:' of lines " +
									       firstInit.getFirstSymbol().getLineNumber() + " and " +
									         secondInit.getFirstSymbol().getLineNumber() + " are incompatible. "
									         		+ "Type '" + secondType.getFullName() + "' is supertype of "
															+ "type '" + firstType.getFullName() + "'. That is illegal because it can cause "
											+ "ambiguity when method 'new:' is called. Use Unions instead of two 'init:' methods. For "
											+ "example, replace 'func init: Animal p { }' and 'func init: Cow p { }' with "
											+ "'func init: Cow|Animal p { }'",
											true, false, false);
								}
							}
						}
					}
				}

			}
			/*
			 * firstParamList.get(p).getType(env) != null && firstParamList.get(p).getType(env) != null
			 */

			analyzed += initListSameParamNumber.size();
			++numParam;
		}

	}

	/** several types of parameters of 'init:' and 'new:' methods and instance variables are just 'Any'
	 * because the compiler, during parsing, did not have sufficient information to discover
	 * the types. Now, during 'calcInternalTypes', these types are calculated --- they are the
	 * types that are in the parameters and external variables used by the function that caused
	 * the creation of the inner prototype. This method retrieves these types and initializes
	 * the 'init:', 'new:', and instance variables (context parameters) with their correct types.
	   @param env
	 */
	private void calcInternalTypesInnerPrototypes(ICompiler_dsa compiler_dsa, Env env) {
		//ArrayList<Tuple6<String, VariableDecInterface, Type, Type, String, String>> infoFunList;
		int funIndex = 0;
		/* set the return type of 'eval' or 'eval:' methods  */
		for ( ObjectDec innerProto : this.innerPrototypeList ) {
			if ( NameServer.isNameInnerProtoForFunction(innerProto.getName()) ) {


				innerProto.setExprFunctionForThisPrototype(functionList.get(funIndex));

				/* if ( count == 0 ) {
					MyFile.write(this.compilationUnit);
				}
				++count;  */

				ExprFunction exprFunc = functionList.get(funIndex);
				if ( exprFunc.getAccessedVariableParameters() != null ) {
					/*
					 * for each local variable that the function uses there is a context parameter
					 * in the inner prototype representing the function. These context parameters
					 * have type Any (all of them) initially. After calculating the types of
					 * local instance variables, which is made in ObjectDec::calcInternalTypes,
					 * the compiler should change the types of the context parameters and of the
					 * init: and new: methods.
					 */
					ArrayList<MethodSignature> msList;
					MethodSignatureWithSelectors msInit, msNew;
					msList = innerProto.searchInitNewMethod("init:");
					if ( msList.size() != 1 )
						env.error(msList.get(0).getFirstSymbol(), "Internal error in calcInternalTypesInnerPrototypes: just one 'init:' method was expected");
					msInit = (MethodSignatureWithSelectors ) msList.get(0);
					msList = innerProto.searchInitNewMethod("new:");
					if ( msList.size() != 1 )
						env.error(msList.get(0).getFirstSymbol(), "Internal error in calcInternalTypesInnerPrototypes: just one 'new:' method was expected");
					msNew = (MethodSignatureWithSelectors ) msList.get(0);


					for ( VariableDecInterface v : exprFunc.getAccessedVariableParameters() ) {

						for ( ContextParameter cp : innerProto.getContextParameterArray() ) {

							if ( cp.getName().equals(v.getName()) ) {
								cp.setType(v.getType());
								cp.setJavaName(v.getJavaName());
								cp.calcInterfaceTypes(env);
								break;
							}

						}
						for ( ParameterDec p : msInit.getParameterList() ) {
							if ( p.getName().equals(v.getName()) ) {
								p.setType(v.getType());
								p.setJavaName(v.getJavaName());
								break;
							}
						}
						for ( ParameterDec p : msNew.getParameterList() ) {
							if ( p.getName().equals(v.getName()) ) {
								p.setType(v.getType());
								p.setJavaName(v.getJavaName());
								break;
							}
						}

					}

				}

				ArrayList<MethodSignature> methodSignatureList = new ArrayList<MethodSignature>();
				/*
				 * search for an 'eval' or 'eval:' method in inner objects created from functions.
				 */
				for ( MethodDec m : innerProto.getMethodDecList() ) {
					if ( NameServer.isMethodNameEval(m.getNameWithoutParamNumber()) )
						methodSignatureList.add(m.getMethodSignature());
				}
				Type newReturnType = this.functionList.get(funIndex).getReturnType();
				Expr newReturnTypeExpr = newReturnType.asExpr(
						innerProto.getSuperobjectExpr().getFirstSymbol());

				if ( methodSignatureList.size() != 1 ) {
					env.error(null,  "Internal error at ObjectDec::calcInterfaceTypes", true, true);
				}
				else {
					/*
					 * change the return type of the 'eval' or 'eval:' method.
					 */
					//methodSignatureList.get(0).setReturnType(this.functionList.get(funIndex).getReturnType());
					methodSignatureList.get(0).setReturnType(newReturnType);
					methodSignatureList.get(0).setReturnTypeExpr(newReturnTypeExpr);
				}

				/*
				if ( infoFunList != null ) {
					/*
					 * change the type of new: method

					methodSignatureList = innerProto.searchInitNewMethod("new:");
					for ( MethodSignature ms : methodSignatureList ) {
						for ( ParameterDec param : ms.getParameterList() ) {
							for ( Tuple6<String, VariableDecInterface, Type, Type, String, String> t : infoFunList ) {
								if ( param.getName().equals(t.f1) ) {
									/*
									 * found a context parameter with name equal to a local variable or parameter
									 * that had its type changed just for the literal function whose index is funIndex.
									 * The type of this local variable or parameter has been changed in the literal
									 * function. It should be changed in the object representing the function too.

									param.setType(t.f3);
									param.setJavaName(t.f5);
								}
							}

						}
					}
				}
				*/
				/*
				 * now change the supertype of the inner prototype. It is Function<A1, ... Ak>...<X1, ... Xn, Any>. "Any" is changed
				 * to the real return type, which is the return type of functionList.get(funIndex).getReturnType.
				 */
				ExprGenericPrototypeInstantiation superProto = (ExprGenericPrototypeInstantiation ) innerProto.getSuperobjectExpr();
				ArrayList<ArrayList<Expr>> realListList = superProto.getRealTypeListList();
				ArrayList<Expr> realList = realListList.get(realListList.size()-1);

				realList.set( realList.size()-1, newReturnTypeExpr);
				++funIndex;
				superProto.clearNameWithPackageAndType();
				/*
				 * the superobjectExpr of innerProto is correct. But instance variable superobject, of type Type, is
				 * unchanged. We change it by calling calcInterfaceTypeSuperobject.
				 */
				innerProto.calcInterfaceTypeSuperobject(env);

			}

			if ( innerProto.exprFunctionForThisPrototype != null ) { // && NameServer.isMethodNameEval(methodDec.getNameWithoutParamNumber()) ) {
				/**
				 * this prototype was created for a function. Change the types of instance variables that should be changed.
				 * The types were changed because a metaobject asked for it.
				 */

				ArrayList<Tuple6<String, VariableDecInterface, Type, Type, String, String>> infoFunList =
						innerProto.exprFunctionForThisPrototype.getVarNameNewCodeOldCodeList();
				if ( infoFunList != null ) {
					for ( ContextParameter cp : innerProto.contextParameterArray ) {
						for ( Tuple6<String, VariableDecInterface, Type, Type, String, String> t : infoFunList ) {
							if ( cp.getName().equals(t.f1) ) {
								/*
								 * found a context parameter with name equal to a local variable or parameter
								 * that had its type changed just for the literal function whose index is funIndex.
								 * The type of this local variable or parameter has been changed in the literal
								 * function. It should be changed in the object representing the function too.
								*/
								// t.f2 = cp;
								t.f4 = cp.getType();
								t.f6 = cp.getJavaName();

								cp.setType(t.f3);
								cp.setJavaName(t.f5);
								// cp.setTypeWasChanged(false);
							}
						}
					}
				}

			}

		}
		/*
		 * calculates the interfaces of inner prototypes before calculating their internal types (semantic analysis)
		 */
		for ( ObjectDec innerProto : this.innerPrototypeList ) {
			innerProto.calcInterfaceTypes(env);
			if ( ! NameServer.isNameInnerProtoForContextFunction(innerProto.getName()) ) {
				innerProto.getContextParameterArray().get(0).setType(this);
				innerProto.getContextParameterArray().get(0).setTypeInDec(this.asExpr(this.getSymbol()));
			}


		}


		/*
		 * the first context parameter of each inner object has name "self__" and has
		 * the type of the outer object.
		 * <code> <br>
		 * object F0(B self__, Int p1) ... end <br>
		 * </code>
		 * In this example, 'B' is the name of the outer object.
		 */
		funIndex = 0;
		for ( ObjectDec innerProto : this.innerPrototypeList ) {
			innerProto.calcInternalTypes(compiler_dsa, env);

			/*
			if ( NameServer.isNameInnerProtoForFunction(innerProto.getName()) ) {
				infoFunList = functionList.get(funIndex).getVarNameNewCodeOldCodeList();
				if ( infoFunList != null ) {
					for ( ContextParameter cp : innerProto.getContextParameterArray() ) {
						for ( Tuple6<String, VariableDecInterface, Type, Type, String, String> t : infoFunList ) {
							if ( cp.getName().equals(t.f1) ) {
								/*
								 * found a context parameter with name equal to a local variable or parameter
								 * that had its type changed just for the literal function whose index is funIndex.
								 * The type of this local variable or parameter has been changed in the literal
								 * function. The code below restores the previous type.
								 * /
								cp.setType(t.f4);
								cp.setJavaName(t.f6);
							}
						}
					}
				}
				++funIndex;

			}
			*/
		}

		/**
		 * change again the type of context parameters of inner prototypes created for functions. This is necessary
		 * for code generation
		 */
		for ( ObjectDec innerProto : this.innerPrototypeList ) {
			if ( innerProto.exprFunctionForThisPrototype != null ) {
				/**
				 * this prototype was created for a function. Change the types of instance variables to their original type
				 * The types were changed because a metaobject asked for it.
				 */

				ArrayList<Tuple6<String, VariableDecInterface, Type, Type, String, String>> infoFunList =
						innerProto.exprFunctionForThisPrototype.getVarNameNewCodeOldCodeList();
				if ( infoFunList != null ) {
					for ( ContextParameter cp : innerProto.contextParameterArray ) {
						for ( Tuple6<String, VariableDecInterface, Type, Type, String, String> t : infoFunList ) {
							if ( cp.getName().equals(t.f1) ) {
								/*
								 * found a context parameter with name equal to a local variable or parameter
								 * that had its type changed just for the literal function whose index is funIndex.
								 * The type of this local variable or parameter has been changed in the literal
								 * function. It should be changed in the object representing the function too.
								*/
								cp.setType(t.f4);
								// cp.setJavaName(t.f6);

							}
						}
					}
				}

			}

		}
	}


	private static int count = 0;

	public void calcInterfaceSuperTypes(Env env) {
		if ( this.superobject == null ) {
			if ( this.superobjectExpr != null ) {
				if ( this.superobjectExpr.type == null )  {
					this.superobjectExpr.calcInternalTypes(env);
  			    }
				this.superobject = this.superobjectExpr.type;
			}
		}
	}

	/**
	 * see comment on Program::calculatesTypes(Env env)
	 */
	@Override
	public void calcInterfaceTypes(Env env) {

		env.atBeginningOfObjectDec(this);

		super.calcInterfaceTypes(env);

		//if ( superobjectExpr != null ) superobjectExpr.calcInternalTypes(env);
		for ( ArrayList<GenericParameter> genericParameterList : genericParameterListList )
			for ( GenericParameter genericParameter : genericParameterList )
				genericParameter.calcInternalTypes(env);


		calcInterfaceTypeSuperobject(env);
		for ( Expr anInterface : interfaceList )
			anInterface.calcInternalTypes(env);



		/**
		 * the context parameters are in fact instance variables so
		 * we don�t have to call calcInternalTypes over them.
		 */

		for ( SlotDec s : this.slotList ) {
			s.calcInterfaceTypes(env);
		}

		/*
		for ( InstanceVariableDec v : this.instanceVariableList )
			v.calcInternalTypes(env);
		for ( MethodDec methodDec : initNewMethodDecList )
			methodDec.calcInterfaceTypes(env);
		for ( MethodDec methodDec : methodDecList )
			methodDec.calcInterfaceTypes(env);

		*/
		env.atEndOfObjectDec();

	}

	/**
	   @param env
	 */
	private void calcInterfaceTypeSuperobject(Env env) {
		if ( superobjectExpr == null ) {
			String prototypeName = symbol.getSymbolString();
			if ( ! prototypeName.equals("Any") && ! prototypeName.equals("Nil") ) {
				superobject = Type.Any;
				superObjectName = "Any";
			}
		}
		else {
			try {
				env.setAllowCreationOfPrototypesInLastCompilerPhases(true);
				superobjectExpr.calcInternalTypes(env);
			}
			finally {
				env.setAllowCreationOfPrototypesInLastCompilerPhases(false);
			}
			//superObjectName = superobjectExpr.ifPrototypeReturnsItsName();
			superobject = superobjectExpr.getType();
			superObjectName = superobject.getFullName();

			if ( superobject instanceof InterfaceDec ) {
				env.error( true, superobjectExpr.getFirstSymbol(),
						"Prototype cannot inherit from an interface", superObjectName, ErrorKind.prototype_cannot_inherit_from_an_interface);
			}
			/*
			if ( superobject instanceof ObjectDec ) {
				if ( this.isAbstract && ! ((ObjectDec) superobject).isAbstract ) {

				}

			}
			*/

		}
	}



	@Override
	public boolean isSupertypeOf(Type otherType, Env env) {

		if ( otherType instanceof TypeDynamic )
			return true;
		if ( this == otherType )
			return true;


		if ( symbol.getSymbolString().equals("Union") ) {
			/*
			 * Union<A, B> is considered, in practice, a supertype of both A and B
			 */
			if ( genericParameterListList.size() > 0  ) {
				for ( GenericParameter gp : genericParameterListList.get(0) ) {
					if ( gp.getKind() != GenericParameterKind.LowerCaseSymbol && gp.getKind() != GenericParameterKind.FormalParameter ) {
						if ( gp.getParameter().getType(env).isSupertypeOf(otherType, env) )
							return true;
					}
				}
				return false;
			}
		}
		if ( otherType == Type.Nil || this == Type.Nil ) {
			   // Nil is the only subtype or supertype of Nil
			return this == otherType;
		}

		// String thisName = this.getName();
		// String otherTypeName = otherType.getName();

		if ( otherType instanceof InterfaceDec ) {
			return this == Type.Any;
		}
		else if ( otherType instanceof ObjectDec ) {
			ObjectDec otherProto = (ObjectDec ) otherType;
			while ( otherProto != null && this != otherProto )
				otherProto = otherProto.getSuperobject();
			return otherProto != null;
		}
		else
			env.error(null,  "Internal error in InterfaceDec::isSupertypeOf: unknown type", true, true);
		return false;
	}


	public ArrayList<CyanMetaobjectWithAtAnnotation> getBeforeInnerObjectNonAttachedMetaobjectAnnotationList() {
		return beforeInnerObjectNonAttachedMetaobjectAnnotationList;
	}

	public void setBeforeInnerObjectNonAttachedMetaobjectAnnotationList(
			ArrayList<CyanMetaobjectWithAtAnnotation> beforeInnerObjectNonAttachedMetaobjectAnnotationList) {
		this.beforeInnerObjectNonAttachedMetaobjectAnnotationList = beforeInnerObjectNonAttachedMetaobjectAnnotationList;
	}


	public ArrayList<CyanMetaobjectWithAtAnnotation> getBeforeInnerObjectAttachedMetaobjectAnnotationList() {
		return beforeInnerObjectAttachedMetaobjectAnnotationList;
	}

	public void setBeforeInnerObjectAttachedMetaobjectAnnotationList(
			ArrayList<CyanMetaobjectWithAtAnnotation> beforeInnerObjectAttachedMetaobjectAnnotationList) {
		this.beforeInnerObjectAttachedMetaobjectAnnotationList = beforeInnerObjectAttachedMetaobjectAnnotationList;
	}

	public ExprFunction getExprFunctionForThisPrototype() {
		return exprFunctionForThisPrototype;
	}

	public void setExprFunctionForThisPrototype(ExprFunction exprFunctionForThisPrototype) {
		this.exprFunctionForThisPrototype = exprFunctionForThisPrototype;
	}

	@Override
	void check_cin(Env env) {
		super.check_cin(env);
		for ( SlotDec slot : slotList ) {
			slot.check_cin(env);
		}
	}

	public void addOverloadMethodList(ArrayList<MethodDec> multiMethodList) {
		if ( this.multiMethodListList == null ) {
			this.multiMethodListList = new ArrayList<>();
		}
		this.multiMethodListList.add(multiMethodList);
	}



	public ArrayList<ArrayList<MethodDec>> getMultiMethodListList() {
		return multiMethodListList;
	}

	public boolean getHasContextParameter() {
		return this.hasContextParameter;
	}

	public void setHasContextParameter(boolean hasContextParameter) {
		this.hasContextParameter = hasContextParameter;
	}

	public ArrayList<ContextParameter> getSuperContextParameterList() {
		return superContextParameterList;
	}

	public void setSuperContextParameterList(ArrayList<ContextParameter> superContextParameterList) {
		this.superContextParameterList = superContextParameterList;
	}


	public ArrayList<MethodDec> getAbstractMethodList() {
		return abstractMethodList;
	}

	public void addAbstractMethod(MethodDec abstractMethod) {
		this.abstractMethodList.add(abstractMethod);
	}

	/**
	 * add an interface to the list of interfaces that the Java class for this prototype should implement
	   @param param
	 */

	public boolean addJavaInterface(String param) {
		if ( javaInterfaceList == null ) {
			javaInterfaceList = new ArrayList<>();
		}
		for ( String iname : ObjectDec.interfacesImplementedByAny ) {
			if ( iname.equals(param) )
				return false;
		}
		for ( String iname : javaInterfaceList ) {
			if ( iname.equals(param) )
				return false;
		}

		javaInterfaceList.add(param);
		return true;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getMetaobjectAnnotationThisAndSuperCTDNUList() {
		if ( metaobjectAnnotationListThisAndSuperCTDNU == null ) {
			metaobjectAnnotationListThisAndSuperCTDNU = new ArrayList<>();
			ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectAnnotationList = this.getAttachedMetaobjectAnnotationList();
			if ( metaobjectAnnotationList != null ) {
				for ( CyanMetaobjectWithAtAnnotation annotation : metaobjectAnnotationList ) {
					if ( annotation.getCyanMetaobject() instanceof ICompileTimeDoesNotUnderstand_dsa ) {
						metaobjectAnnotationListThisAndSuperCTDNU.add(annotation);
					}
				}
			}
			for ( MethodDec aMethod : this.methodDecList ) {
				metaobjectAnnotationList = aMethod.getAttachedMetaobjectAnnotationList();
				if ( metaobjectAnnotationList != null ) {
					for ( CyanMetaobjectWithAtAnnotation annotation : metaobjectAnnotationList ) {
						if ( annotation.getCyanMetaobject() instanceof ICompileTimeDoesNotUnderstand_dsa ) {
							metaobjectAnnotationListThisAndSuperCTDNU.add(annotation);
						}
					}
				}
			}

			if ( this.superobject != null && superobject instanceof ObjectDec ) {
				ObjectDec superObj = (ObjectDec ) superobject;
				metaobjectAnnotationListThisAndSuperCTDNU.addAll(superObj.getMetaobjectAnnotationThisAndSuperCTDNUList());
			}
		}
		return metaobjectAnnotationListThisAndSuperCTDNU;
	}


	/**
	 * returns the first method of this prototype that has a feature called 'name' (in the first field of the tuple). Returns the
	 * value associated with this feature in the second element of the tuple
	   @param name
	   @return
	 */
	public Tuple2<MethodDec, ExprAnyLiteral> searchMethodByFeature(String name) {
		for ( MethodDec method : this.methodDecList ) {
			if ( method.getFeatureList() != null ) {
				for ( Tuple2<String, ExprAnyLiteral> t : method.getFeatureList()) {
					if ( t.f1.equals(name) ) {
						return new Tuple2<MethodDec, ExprAnyLiteral>(method, t.f2);
					}
				}
			}
		}
		return null;
	}



	public ArrayList<MethodDec> getInitNewMethodDecList() {
		return initNewMethodDecList;
	}


	/**
	 * list of metaobject annotations attached to this prototype and
	 * super-prototypes that implement interface {@link meta#ICompileTimeDoesNotUnderstand_dsa}
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectAnnotationListThisAndSuperCTDNU;

	private ArrayList<String> javaInterfaceList;


	/**
	 * true if this object is abstract
	 */
	private boolean isAbstract;
	/**
	 * true if this object is final
	 */
	private boolean isFinal;

	/**
	 * the superobjectExpr of this object. Similar to superclass.
	 * Types are represented as expressions.
	 */
	private Expr superobjectExpr;

	/**
	 * the superobject of this object.
	 */
	private Type superobject;

	/**
	 * list of interfaces implemented by this object
	 */
	private ArrayList<Expr> interfaceList;

	/**
	 * list of context parameters of this object
	 */
	private ArrayList<ContextParameter> contextParameterArray;

	/**
	 * list of all instance variables of this object
	 */
	private ArrayList<InstanceVariableDec> instanceVariableList;



	/**
	 * list of all method declarations of this object except those with
	 * names init, init:, new, and new:
	 */
	private ArrayList<MethodDec> methodDecList;

	/**
	 * list of all methods with names init, init:, new, and new:
	 */
	private ArrayList<MethodDec> initNewMethodDecList;

	/**
	 * list of all grammar methods
	 */
	private ArrayList<MethodDec> grammarMethodList;

	/**
	 * list of functions declared in this object.
	 */
	private ArrayList<ExprFunction> functionList;

	/**
	 * name of the super prototype
	 */
	private String superObjectName;


	/**
	 * slot list of this object. A slot is a instance variable or method.
	 * This list is important because it preserves the order of the slot declaration
	 */
	protected ArrayList<SlotDec> slotList;


	/**
	 * metaobject annotations placed before the declaration of inner objects.
	 * These metaobject annotations are NOT attached to any declaration.
	 * </code>
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> beforeInnerObjectNonAttachedMetaobjectAnnotationList;

	/**
	 * metaobject annotations placed before the declaration of inner objects.
	 * These metaobject annotations are attached to the next declaration.
	 * </code>
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> beforeInnerObjectAttachedMetaobjectAnnotationList;

	/**
	 * If this is an inner prototype created for a literal function, this variable
	 * refers to the literal function. Otherwise it is null
	 */
	private ExprFunction exprFunctionForThisPrototype;


	/**
	 * list of lists. The inner list contains all multi-methods with the same name in
	 * the prototype. In Cyan syntax and using method signatures, it could be <br>
	 * <code>
	 *    [  [ "at: Int", "at: String" ],  [ "print: Int", "print: Shape" ] ]
	 * </code>
	 */
	private ArrayList<ArrayList<MethodDec>> multiMethodListList;

	/**
	 * true if this prototype declares at least one context parameter. If true
	 * all instance variables that are not context parameters should be initialized in their declarations
	 */
	private boolean hasContextParameter = false;
	/**
	 * the context parameter list of the super-prototype. It would contain 'aa' and 'bb' in<br>
	 * {@code <br>
	 * object B(Int aa, Int bb, Int cc) extends A(aa, bb) <br>
	 *   ...<br>
	 * end <br>
	 * }
	 */
	private ArrayList<ContextParameter> superContextParameterList;

	/**
	 * list of methods declared as 'abstract'
	 */
	private ArrayList<MethodDec> abstractMethodList;
	public ArrayList<MethodDec> getGrammarMethodList() {
		return grammarMethodList;
	}

	static final public String []interfacesImplementedByAny = { "Cloneable", "java.io.Serializable" };

}

