/**
 *
 */
package ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;
import error.CompileErrorException;
import error.ErrorKind;
import lexer.CompilerPhase;
import lexer.Lexer;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;
import meta.CompilerManager_ati;
import meta.Compiler_ati;
import meta.Compiler_dsa;
import meta.CyanMetaobject;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.IAction_cge;
import meta.ICheckProgramUnit_ati3;
import meta.ICheckProgramUnit_before_dsa;
import meta.ICheckProgramUnit_dsa2;
import meta.ICheckSubprototype_ati3;
import meta.ICheckSubprototype_dsa2;
import meta.ICommunicateInPrototype_ati_dsa;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple4;
import saci.Tuple5;
import saci.Tuple6;


/**
 * This class is a superclass of ObjectDec and InterfaceDec
 * @author José
 *
 */
public abstract class ProgramUnit extends Type implements Declaration, ASTNode, Cloneable {


	public ProgramUnit(Token visibility, ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,  ObjectDec	outerObject) {
		this.visibility = visibility;
		this.nonAttachedMetaobjectAnnotationList = nonAttachedMetaobjectAnnotationList;
		this.attachedMetaobjectAnnotationList = attachedMetaobjectAnnotationList;
		genericParameterListList = new ArrayList<ArrayList<ast.GenericParameter>>();
		this.outerObject = outerObject;

		// genericProtoInstantiationList = new ArrayList<ExprGenericPrototypeInstantiation>();
		prototypeIsNotGeneric = true;
		this.completeMetaobjectAnnotationList = new ArrayList<CyanMetaobjectAnnotation>();
		genericPrototype = false;
		metaobjectAnnotationNumber = CyanMetaobjectAnnotation.firstMetaobjectAnnotationNumber;
		moListBeforeExtendsMixinImplements = null;

		messageSendWithSelectorsToSuperList = new ArrayList<>() ;
		nextFunctionNumber = 0;
		innerPrototypeList = new ArrayList<ObjectDec>();
		beforeEndNonAttachedMetaobjectAnnotationList = new ArrayList<>();
	}

	@Override
	public ProgramUnit clone() {

		try {
		    return (ProgramUnit ) super.clone();
		}
		catch ( CloneNotSupportedException e ) {
			return null;
		}
	}


	@Override
	public int hashCode() {
		return Objects.hash(this.getCompilationUnit().getPackageName(), this.getName());
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getAttachedMetaobjectAnnotationList() {
		return attachedMetaobjectAnnotationList;
	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void genCyan(PWInterface pw, CyanEnv cyanEnv, boolean genFunctions) {
		/*
		 *
attachedMetaobjectAnnotationList;
nonAttachedMetaobjectAnnotationList;
beforeEndNonAttachedMetaobjectAnnotationList;
		 */
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation c : nonAttachedMetaobjectAnnotationList )
				c.genCyan(pw, true, cyanEnv, genFunctions);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation c : attachedMetaobjectAnnotationList )
				c.genCyan(pw, true, cyanEnv, genFunctions);
		}
	}

	/**
	 * prints in pw the program unit name in Cyan. It is  Person if
	 * the prototype name is "Person" and "Stack<Person>" if
	 * the prototype is generic and it is being instantiated with
	 * a real parameter "Person"
	 * @param pw
	 * @param cyanEnv
	 */
	protected void genCyanProgramUnitName(PWInterface pw, CyanEnv cyanEnv) {

		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			// this is a generic object instantiation. Then genCyan was called to create a
			// prototype Stack<Int> because there is a "Stack<Int>" use in the program
			//pw.print(cyanEnv.getPrototypeName());
		    pw.print(symbol.getSymbolString());

			//if ( genericParameterListList.get(0).get(0).getPlus() ) {

		    if ( genericParameterListList.size() > 0 ) {
				/**
				 * generic prototype with varying number of parameters
				 */
		    	for ( ArrayList<String> realParamList : cyanEnv.getRealParamListList() ) {
					pw.print("<");
			    	int size = realParamList.size();
			    	if ( size > 0 ) {
			    		for ( String realParam : realParamList ) {

			    			pw.print(Lexer.addSpaceAfterComma(realParam));
							--size;
							if ( size > 0 )
								pw.print(", ");
			    		}
			    	}
			    	else {
			    		pw.print(NameServer.noneArgumentNameForFunctions);
			    	}
		    		pw.print(">");

		    	}
			}
			else {
				cyanEnv.error("Internal error at ProgramUnit::genCyan");
			}


		}
		else {
		    pw.print(symbol.getSymbolString());
			if ( genericParameterListList.size() > 0  ) {
				for ( ArrayList<GenericParameter> gtList : genericParameterListList ) {
					pw.print("<");
					int size = gtList.size();
					for ( GenericParameter p : gtList ) {
						p.genCyan(pw, false, cyanEnv, true);
						--size;
						if ( size > 0 )
							pw.print(", ");
					}
					pw.print(">");
				}
			}
		}


	}

	abstract public void genJava(PWInterface pw, Env env);
	/**
	 * returns the instance variable of this prototype whose name is varName
	 */
	abstract public InstanceVariableDec searchInstanceVariableDec(String varName);

	abstract public InstanceVariableDec searchInstanceVariablePrivateProtectedSuperProtected(String varName);
	abstract public InstanceVariableDec searchInstanceVariableDecProtected(String varName);


	/**
	 * return the instance variable whose name is "name". null if not found
	 */
	abstract public InstanceVariableDec searchInstanceVariable(String name);


	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}
	public Symbol getSymbol() {
		return symbol;
	}

	/**
	 * return the name of the prototype identifier. It is "Int" in prototype "Int" and "Stack" in prototype "Stack<T>".
	   @return
	 */
	public String getIdent() {
		return symbol.getSymbolString();
	}

	public ArrayList<ArrayList<GenericParameter>> getGenericParameterListList() {
		return genericParameterListList;
	}

	public void addGenericParameterList(
			ArrayList<GenericParameter> genericParameterList) {
		genericParameterListList.add(genericParameterList);
	}

	/**
	 *  return true if this program unit is generic like "object Stack{@literal <}T> ... end".
	 *  Return false otherwise, including if this program unit declares an
	 *  instantiation of a generic prototype like "object Stack{@literal <}Int> ... end".
	 * @return
	 */
	public boolean isGeneric() {
		return ! getPrototypeIsNotGeneric() && genericParameterListList.size() > 0;
	}


	public void setVisibility(Token visibility) {
		this.visibility = visibility;
	}

	public Token getVisibility() {
		return visibility;
	}

	@Override
	public String getFullName() {
		return getCompilationUnit().getPackageIdent().getName() + "." + getName();
	}


	@Override
	public String getFullName(Env env) {

		if ( this.fullName == null ) {
			String realName = symbol.getSymbolString();

			if ( genericParameterListList.size() > 0  ) {
				for ( ArrayList<GenericParameter> gtList : genericParameterListList ) {
					realName = realName + "<";
					int size = gtList.size();
					for ( GenericParameter p : gtList ) {
						String s = p.getFullName(env);
						if ( s == null )
							env.error(p.getParameter().getFirstSymbol(), "Type was not found: '" + p.getName() + "'");
						realName = realName + s;
						--size;
						if ( size > 0 )
							realName = realName + ",";
					}
					realName = realName + ">";
				}
			}
			CyanPackage cyanPackage = this.getCompilationUnit().getCyanPackage();
			if ( cyanPackage.getPackageName().equals(NameServer.cyanLanguagePackageName) )
				fullName = realName;
			else
				fullName = cyanPackage.getPackageName() + "." + realName;
		}
		return fullName;
	}

	/**
	 * return the name of the program unit. If it is a generic prototype, return the name without the parameters.
	 * Then if the program unit is <code>Stack{@literal <}Int></code>, this method returns <code>Stack</code>.
	   @return
	 */
	public String getSimpleName() {
		return this.symbol.getSymbolString();
	}

	/**
	 * returns the prototype name. If it is "Person", "Person" is returned. If it is a generic prototype
	 * "Stack{@literal <}T>", "Stack{@literal <}T>" is returned. If it is an instantiated generic prototype "Stack{@literal <}main.Person>",
	 * "Stack{@literal <}main.Person>" is returned.
	 * <br>
	 * This method should not be called during semantic analysis because it does not give the correct name, with the packages.
	 * @return
	 */
	@Override
	public String getName() {
		if ( realName == null ) {
			realName = symbol.getSymbolString();

			if ( genericParameterListList.size() > 0  ) {
				for ( ArrayList<GenericParameter> gtList : genericParameterListList ) {
					realName = realName + "<";
					int size = gtList.size();
					for ( GenericParameter p : gtList ) {
						realName = realName + p.getName();
						--size;
						if ( size > 0 )
							realName = realName + ",";
					}
					realName = realName + ">";
				}
			}
		}
		return realName;
	}

	/**
	 * return the name of this compilation unit preceded by its outer prototype, if any. The  inner
	 * and outer prototypes are separated by '.'	 */
	public String getNameWithOuter() {
		String protoName = getName();
		if ( getOuterObject() != null ) {
			protoName = getOuterObject().getName() + "." + protoName;
		}
		return protoName;
	}

	/** Assuming that this program unit is public, this method returns
	 * the name of the source file, without ".cyan", in which this
	 * program unit should be. Package cyan.lang is never used in the
	 * source file name.
	 *
	 *
	 * Examples:
	 *
	 *      object Test<Int, main.Person> ... end  returns "Test(Int,main.Person).cyan"
	 *      object Test<cyan.lang.Int, main.Person> ... end  returns "Test(Int,main.Person).cyan"
	 *      object Test<cyan.lang.Tuple<cyan.lang.Int>,
	 *                  util.Stack<cyan.lang.Tuple<cyan.lang.Int, main.Person>>> ... end
	 *      returns "Test(Tuple(Int),util.Stack(Tuple(Int,main.Person))).cyan"
	 *      object Test<Int, main.Person> ... end  returns "Test(Int,main.Person).cyan"
	 *      object Test<Int, main.Person> ... end  returns "Test(Int,main.Person).cyan"
	 *      object Test<Int, main.Person> ... end  returns "Test(Int,main.Person).cyan"
	 * @return
	 */

	//# stopped here.

	public String getNameSourceFile() {
		String realName = symbol.getSymbolString();

		if ( genericParameterListList.size() > 0 ) {
			for ( ArrayList<GenericParameter> gtList : genericParameterListList ) {
				int size = gtList.size();

				if ( gtList.get(0).isRealPrototype() ) {
					realName = realName + "(";
					for ( GenericParameter p : gtList ) {
						realName = realName + p.getNameSourceFile();
						--size;
						if ( size > 0 )
							realName = realName + ",";
					}
					realName = realName + ")";
				}
				else {
					realName = realName + "(" + gtList.size();
					if ( gtList.get(0).getPlus() )
						realName = realName + "+";
					realName = realName + ")";
				}

				/*
				String prototypeName = prototypeNameFromNameSourceFile(gtList.get(0).getNameSourceFile());
				if ( compilationUnit.getProgram().isInPackageCyanLang(prototypeName) ||
						prototypeName.contains(".") || Character.isLowerCase(prototypeName.charAt(0))) {
					// the first parameter is either a prototype of package cyan.lang or a prototype with its package as "main.Person"
					realName = realName + "(";
					for ( GenericParameter p : gtList ) {
						String s = prototypeNameFromNameSourceFile(p.getNameSourceFile());
						if ( compilationUnit.getProgram().isInPackageCyanLang(s) || s.contains(".") ) {
							realName = realName + p.getNameSourceFile();
						}
						else {
							// a symbol such as key in Tuple<key, String, value, Int>
							realName = realName + s;
						}
						--size;
						if ( size > 0 )
							realName = realName + ",";
					}
					realName = realName + ")";

				}
				else {
					realName = realName + "(" + gtList.size() + ")";
				} */
			}

		}

		/*
		if ( isGeneric() ) {
			if ( genericParameterListList.size() > 0  ) {
				for ( ArrayList<GenericParameter> gtList : genericParameterListList ) {
					realName = realName + "(" + gtList.size() + ")";
				}
			}
		}
		else {
			if ( genericParameterListList.size() > 0  ) {
				// this is a generic object instantiation. Then genCyan was called to create a
				// prototype Stack<Int> because there is a "Stack<Int>" use in the program
				for ( ArrayList<GenericParameter> gtList : genericParameterListList ) {
					realName = realName + "(";
					int size = gtList.size();
					for ( GenericParameter p : gtList ) {
						realName = realName + p.getNameSourceFile();
						--size;
						if ( size > 0 )
							realName = realName + ",";
					}
					realName = realName + ")";
				}
			}

		} */
		return realName;
	}


	@Override
	public String getJavaName() {

		if ( javaName == null ) {
			String name1 = getName();
			if ( this.compilationUnit.getPackageName().equals(NameServer.cyanLanguagePackageName) ) {
				if ( this instanceof InterfaceDec ) {
					name1 = NameServer.prototypeFileNameFromInterfaceFileName(name1);
				}
				javaName =  NameServer.getJavaName(name1);
			}
			else if ( this.outerObject == null ) {
				if ( this instanceof InterfaceDec ) {
					name1 = NameServer.prototypeFileNameFromInterfaceFileName(name1);
				}
				javaName = compilationUnit.getPackageName() + "." +  NameServer.getJavaName(name1);
				/*
				if ( this instanceof InterfaceDec ) {
					javaName = compilationUnit.getPackageName() + "." +  NameServer.javaPrototypeFileNameFromInterfaceFileName(NameServer.getJavaName(name1));
				}
				else {
					javaName = compilationUnit.getPackageName() + "." +  NameServer.getJavaName(name1);
				}
				*/
			}
			else {
				javaName = NameServer.getJavaName(name1);
			}

			// javaPrototypeFileNameFromInterfaceFileName
		}
		return javaName;
	}

	public String getJavaNameWithoutPackage() {
		if ( javaNameWithoutPackage == null ) {
			javaNameWithoutPackage =  NameServer.getJavaName(getName());
		}
		return javaNameWithoutPackage;
	}

	String javaNameWithoutPackage;


	public Type getType() {
		return this;
	}



	/** calculates the type of all method parameters, all return values of methods of
	 * this program unit, and all instance variables.
	 * The types depends on the packages imported by the compilation unit
	 * of this program unit
	 * */

	public void calcInternalTypes(ICompiler_dsa compiler_dsa, Env env) {

		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				annotation.calcInternalTypes(env);
			}

			ObjectDec objDec = null;
			if ( this instanceof ObjectDec ) {
				objDec = (ObjectDec ) this;
				compiler_dsa.getEnv().atBeginningOfObjectDec(objDec);
			}

			for ( CyanMetaobjectAnnotation annotation : attachedMetaobjectAnnotationList ) {

				CyanMetaobject metaobject = annotation.getCyanMetaobject();
				metaobject.setMetaobjectAnnotation(annotation);
				if ( metaobject instanceof ICheckProgramUnit_before_dsa ) {
					ICheckProgramUnit_before_dsa fp = (ICheckProgramUnit_before_dsa ) metaobject;


					try {
						fp.before_dsa_checkProgramUnit(compiler_dsa);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						e.printStackTrace();
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(metaobject, annotation);
					}


				}

			}
			if ( objDec != null ) {
				compiler_dsa.getEnv().atEndOfObjectDec();
			}

		}


		/*
		if ( beforeEndNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : beforeEndNonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}
		*/

	}

	public void calcInterfaceTypes(Env env) {

		if ( gpiList != null ) {
			for ( ExprGenericPrototypeInstantiation elem : gpiList ) {
				elem.calcInternalTypes(env);
			}
		}

		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}


		if ( this.outerObject == null )
			javaName = NameServer.getJavaName(this.getFullName(env));
		else
			javaName = NameServer.getJavaName(this.getName());
		/*
		String otherName = "";
		if ( this instanceof InterfaceDec ) {
			if ( this.outerObject == null )
				otherName = NameServer.getJavaName(this.getFullName(env));
			else
				otherName = NameServer.getJavaName(this.getName());

		}

		// just to set javaName
		this.getJavaName();
		*/
	}


	public boolean getPrototypeIsNotGeneric() {
		return prototypeIsNotGeneric;
	}

	public void setPrototypeIsNotGeneric(boolean hasPrototypeInstantiation) {
		this.prototypeIsNotGeneric = hasPrototypeInstantiation;

	}

	public void addMetaobjectAnnotation(CyanMetaobjectWithAtAnnotation annotation) {
		if ( this.completeMetaobjectAnnotationList == null )
			completeMetaobjectAnnotationList = new ArrayList<CyanMetaobjectAnnotation>();
		completeMetaobjectAnnotationList.add(annotation);
	}

	public ArrayList<CyanMetaobjectAnnotation> getCompleteMetaobjectAnnotationList() {
		return completeMetaobjectAnnotationList;
	}



	public void dsa2_check(ICompiler_dsa compiler_ati, Env env) {

		ObjectDec objDec = null;
		if ( this instanceof ObjectDec ) {
			objDec = (ObjectDec ) this;
			compiler_ati.getEnv().atBeginningOfObjectDec(objDec);
		}


		ArrayList<CyanMetaobjectAnnotation> puMetaobjectAnnotationList = new ArrayList<>();
		if ( completeMetaobjectAnnotationList != null )
			puMetaobjectAnnotationList.addAll(completeMetaobjectAnnotationList);
		if ( nonAttachedMetaobjectAnnotationList != null )
			puMetaobjectAnnotationList.addAll(nonAttachedMetaobjectAnnotationList);

		this.checkInheritance_dsa2(env);

		for ( CyanMetaobjectAnnotation annotation : puMetaobjectAnnotationList ) {

			CyanMetaobject metaobject = annotation.getCyanMetaobject();
			metaobject.setMetaobjectAnnotation(annotation);
			if ( metaobject instanceof ICheckProgramUnit_dsa2 ) {
				ICheckProgramUnit_dsa2 fp = (ICheckProgramUnit_dsa2 ) metaobject;


				try {
					fp.dsa2_checkProgramUnit(compiler_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					e.printStackTrace();
					env.thrownException(annotation, annotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(metaobject, annotation);
				}


			}

		}
		if ( objDec != null ) {
			compiler_ati.getEnv().atEndOfObjectDec();
		}
	}


	public void ati3_check(ICompiler_ati compiler_ati, Env env ) {


		ArrayList<CyanMetaobjectAnnotation> puMetaobjectAnnotationList = new ArrayList<>();
		if ( completeMetaobjectAnnotationList != null )
			puMetaobjectAnnotationList.addAll(completeMetaobjectAnnotationList);
		if ( nonAttachedMetaobjectAnnotationList != null )
			puMetaobjectAnnotationList.addAll(nonAttachedMetaobjectAnnotationList);


		/**
		 * Meta: call method checkSubprototype for each metaobject that implements interface
		 * ICheckSubprototype_ati3
		 */
		this.checkInheritance_ati3(env);


		for ( CyanMetaobjectAnnotation annotation : puMetaobjectAnnotationList ) {
			CyanMetaobject metaobject = annotation.getCyanMetaobject();
			metaobject.setMetaobjectAnnotation(annotation);
			if ( metaobject instanceof ICheckProgramUnit_ati3 ) {
				ICheckProgramUnit_ati3 fp = (ICheckProgramUnit_ati3 ) metaobject;


				try {
					fp.ati3_checkProgramUnit(compiler_ati);
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




	/**
	 * make the metaobject annotations of this program unit communicate with each other
	 * any prototype whose name has a '<' in it, as <code>Set{@literal <}Int></code>, cannot communicate with other prototypes
	 */
	protected void makeMetaobjectAnnotationsCommunicateInPrototype(Env env) {


		/*
		 * every metaobject can supply information to other metaobjects.
		 * Every tuple in this set correspond to a metaobject annotation.
		 * Every tuple is composed by a metaobject name, the number of this metaobject
		 * considering all metaobjects in the prototype, the number of this metaobject
		 * considering only the metaobjects with the same name, and the information
		 * this metaobject annotation wants to share with other metaobject annotations.
		 */
		HashSet<Tuple4<String, Integer, Integer, Object>> moInfoSet = new HashSet<>();
		for ( CyanMetaobjectAnnotation annotation : this.completeMetaobjectAnnotationList ) {
			CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
			if ( cyanMetaobject instanceof ICommunicateInPrototype_ati_dsa ) {
				cyanMetaobject.setMetaobjectAnnotation(annotation);

				Object sharedInfo = ((ICommunicateInPrototype_ati_dsa ) cyanMetaobject).ati_dsa_shareInfoPrototype();
				if (  sharedInfo != null ) {
					if ( this.genericParameterListList.size() == 0 ) {
						Tuple4<String, Integer, Integer, Object> t = new Tuple4<>(cyanMetaobject.getName(),
								annotation.getMetaobjectAnnotationNumber(), annotation.getMetaobjectAnnotationNumberByKind(),
								sharedInfo);
						moInfoSet.add(t);
					}
					else {
						env.error(true, annotation.getFirstSymbol(),
									"metaobject annotation of metaobject '" +
									           annotation.getCyanMetaobject().getName() + "' is trying to communicate with other metaobjects of the package. " +
											"This is prohibit because this metaobject is a generic prototype instantiation or has a '<' in its name", null, ErrorKind.metaobject_error);
					}

				}

			}
		}
		/*
		 * send information to all annotations of this program unit. Let them communicate with each other
		 */
		if ( moInfoSet.size() > 0 ) {
			for ( CyanMetaobjectAnnotation annotation : this.completeMetaobjectAnnotationList ) {
				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject instanceof ICommunicateInPrototype_ati_dsa ) {
					cyanMetaobject.setMetaobjectAnnotation(annotation);
					((ICommunicateInPrototype_ati_dsa ) cyanMetaobject).ati_dsa_receiveInfoPrototype(moInfoSet);

				}
			}
		}
	}


	public void ati_actions(ICompiler_ati compiler_ati, CompilerManager_ati compilerManager) {

		Env env = (Env ) compiler_ati.getEnv();
		env.atBeginningOfObjectDec(this);


		makeMetaobjectAnnotationsCommunicateInPrototype(env);


		/*
		 * true if this prototype has a {@literal <} in its name
		 */
		boolean hasLessThanInName = getName().indexOf('<') >= 0 ;

		ArrayList<CyanMetaobjectAnnotation> metaobjectAnnotationList = completeMetaobjectAnnotationList;


		for ( CyanMetaobjectAnnotation cyanMetaobjectAnnotation : metaobjectAnnotationList ) {

			/*
			 * if the metaobject annotation has a suffix greater or equal to "ati" then the actions below
			 * have already been taken (the compiler changed the suffix to "ati" or greater) or
			 * they should not be taken (the original program uses a suffix in the metaobject annotation
			 * of "ati" or greater).
			 */
			if ( cyanMetaobjectAnnotation.getPostfix() == null || cyanMetaobjectAnnotation.getPostfix().lessThan(CompilerPhase.ATI) ) {

				addCodeAndSlotsTo(compiler_ati, compilerManager, env, hasLessThanInName, cyanMetaobjectAnnotation);


			}

		}

		env.atEndOfObjectDec();
	}



	/**
	   @param compiler_ati
	   @param compilerManager
	   @param env
	   @param hasLessThanInName
	   @param cyanMetaobjectAnnotation
	 */
	public void addCodeAndSlotsTo(ICompiler_ati compiler_ati, CompilerManager_ati compilerManager, Env env,
			boolean hasLessThanInName, CyanMetaobjectAnnotation cyanMetaobjectAnnotation) {


		CyanMetaobject cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();

		/**
		 * macros do not have an object for "metaobject annotation". Then only macros will
		 * have "cyanMetaobject == null".
		 */
		if ( cyanMetaobject != null ) {

			String thisPrototypeName = this.getName();

			cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);


			if ( cyanMetaobject instanceof IActionProgramUnit_ati ) {

				/*
				 * create new prototypes
				 */
				try {
					IActionProgramUnit_ati metaobject = (IActionProgramUnit_ati) cyanMetaobject;
					ArrayList<Tuple2<String, StringBuffer>> prototypeNameCodeList = null;

					try {
						prototypeNameCodeList = metaobject.ati_NewPrototypeList(compiler_ati);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
					}



					if ( prototypeNameCodeList != null ) {
						for ( Tuple2<String, StringBuffer> prototypeNameCode : prototypeNameCodeList ) {
							Tuple2<CompilationUnit, String> t = env.getProject().getCompilerManager().createNewPrototype(prototypeNameCode.f1, prototypeNameCode.f2,
									this.compilationUnit.getCompilerOptions(), this.compilationUnit.getCyanPackage());
							if ( t != null && t.f2 != null ) {
								env.error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), t.f2);
							}
						}
					}
				}
				catch ( CompileErrorException e ) {

				}


				CyanMetaobjectWithAt cyanMetaobjectWithAt = (CyanMetaobjectWithAt ) cyanMetaobject;

				String packageName = this.compilationUnit.getCyanPackage().getPackageName();
				boolean canCommunicateInPackage = this.compilationUnit.getCyanPackage().getCommunicateInPackage();

				/**
				 * add code to prototypes. This was asked by the metaobjects of this program unit.
				 */
				ArrayList<Tuple2<String, StringBuffer>> codeToAddList = null;

				try {
					codeToAddList = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_codeToAddToPrototypes(compiler_ati);
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



				if ( codeToAddList != null ) {
					for ( Tuple2<String, StringBuffer> codeToAdd : codeToAddList ) {
						/*
						 * only a prototype that has a {@literal <} in its name can change itself.
						 * And a prototype that has a {@literal <} in its name can change only itself.
						 */
						String prototypeName = codeToAdd.f1;
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to add code to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						if ( !canCommunicateInPackage && ! prototypeName.equals(thisPrototypeName) ) {
							env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "This metaobject annotation is in prototype '" + packageName + "." +
						          thisPrototypeName + "' and it is trying to add code to another prototype, '" + packageName + "." +
								          prototypeName + "'. This is illegal because package '" + packageName + "' does not allow that. To make that " +
									 "legal, attach '@feature(communicateInPackage, #on)' to the package in the project (.pyan) file", true, false);
						}
						else {
							compilerManager.addCode(cyanMetaobjectWithAt, packageName, prototypeName, codeToAdd.f2);
						}
					}
				}

				/**
				 * add method to prototypes. This was asked by the metaobjects of this program unit.
				 */
				ArrayList<Tuple3<String, String, StringBuffer>> methodList = null;

				try {
					methodList = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_methodCodeList(compiler_ati);
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
					/*
					 * only a prototype that has a {@literal <} in its name can change itself.
					 * And a prototype that has a {@literal <} in its name can change only itself.
					 */
					for ( Tuple3<String, String, StringBuffer> t : methodList ) {
						String prototypeName = t.f1;
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to add a method to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						if ( !canCommunicateInPackage && ! prototypeName.equals(thisPrototypeName) ) {
							env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "This metaobject annotation is in prototype '" + packageName + "." +
						          thisPrototypeName + "' and it is trying to add code to another prototype, '" + packageName + "." +
								          prototypeName + "'. This is illegal because package '" + packageName + "' does not allow that. To make that " +
									 "legal, attach '@feature(communicateInPackage, #on)' to the package in the project (.pyan) file", true, false);
						}
						else {
							compilerManager.addMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f2, t.f3);
						}
					}
				}


				/**
				 * add statements to methods. This was asked by the metaobjects of this program unit.
				 */
				ArrayList<Tuple3<String, String, StringBuffer>> statsList = null;

				try {
					statsList = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_beforeMethodCodeList(compiler_ati);
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
					for ( Tuple3<String, String, StringBuffer> t : statsList ) {
						String prototypeName = t.f1;
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to code to a method of a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						if ( !canCommunicateInPackage && ! prototypeName.equals(thisPrototypeName) ) {
							env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "This metaobject annotation is in prototype '" + packageName + "." +
						          thisPrototypeName + "' and it is trying to add code to another prototype, '" + packageName + "." +
								          prototypeName + "'. This is illegal because package '" + packageName + "' does not allow that. To make that " +
									 "legal, attach '@feature(communicateInPackage, #on)' to the package in the project (.pyan) file", true, false);
						}
						else {
							compilerManager.addBeforeMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f2, t.f3 );
						}
					}
				}

				/**
				 * add instance variables to prototypes.
				 */
				ArrayList<Tuple6<String, Boolean, Boolean, Boolean, String, String>> instanceVariableList = null;

				try {
					instanceVariableList = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_instanceVariableList(compiler_ati);
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
					for ( Tuple6<String, Boolean, Boolean, Boolean, String, String> t : instanceVariableList ) {
						String prototypeName = t.f1;
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to add instance variables to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						if ( !canCommunicateInPackage && ! prototypeName.equals(thisPrototypeName) ) {
							env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "This metaobject annotation is in prototype '" + packageName + "." +
						          thisPrototypeName + "' and it is trying to add code to another prototype, '" + packageName + "." +
								          prototypeName + "'. This is illegal because package '" + packageName + "' does not allow that. To make that " +
									 "legal, attach '@feature(communicateInPackage, #on)' to the package in the project (.pyan) file", true, false);
						}
						else {
							compilerManager.addInstanceVariable(cyanMetaobjectWithAt,
									packageName, prototypeName, t.f2, t.f3, t.f4, t.f5, t.f6);
						}
					}
				}

				/**
				 * add code after the metaobject annotation
				 */
				StringBuffer code = null;

				try {
					code = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_codeToAdd(compiler_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}



				if ( code != null ) {
					compilerManager.addCodeAtMetaobjectAnnotation(cyanMetaobjectWithAt, code );
				}


				/**
				 * add methods to the current prototype.
				 */
				ArrayList<Tuple2<String, StringBuffer>> methodListThisPrototype = null;

				try {
					methodListThisPrototype = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_methodCodeListThisPrototype(compiler_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}

				if ( methodListThisPrototype != null ) {
					/*
					 * only a prototype that has a {@literal <} in its name can change itself.
					 * And a prototype that has a {@literal <} in its name can change only itself.
					 */
					for ( Tuple2<String, StringBuffer> t : methodListThisPrototype ) {
						String prototypeName = this.getName();
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to add a method to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						compilerManager.addMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f1, t.f2);
					}
				}

				/**
				 * add instance variables to this prototype.
				 */
				ArrayList<Tuple5<Boolean, Boolean, Boolean, String, String>> instanceVariableListThisPrototype = null;

				try {
					instanceVariableListThisPrototype = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_instanceVariableListThisPrototype(compiler_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}



				if ( instanceVariableListThisPrototype != null ) {
					for ( Tuple5<Boolean, Boolean, Boolean, String, String> t : instanceVariableListThisPrototype ) {
						String prototypeName = this.getName();
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to add instance variables to a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						compilerManager.addInstanceVariable(cyanMetaobjectWithAt,
									packageName, prototypeName, t.f1, t.f2, t.f3, t.f4, t.f5);
					}
				}
				/**
				 * rename methods
				 */

				ArrayList<Tuple3<String, String, String []>> renameMethodList = null;

				try {
					renameMethodList = ((IActionProgramUnit_ati ) cyanMetaobjectWithAt).ati_renameMethod(compiler_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
				}



				if ( renameMethodList != null ) {
					for ( Tuple3<String, String, String []> t : renameMethodList ) {
						String prototypeName = t.f1;
						if ( hasLessThanInName || prototypeName.indexOf('<') >= 0 ) {
							if ( ! this.getCompilationUnit().getPackageName().equals(packageName)  ||
								 ! this.getName().equals(prototypeName) ) {
								env.error(true,
										cyanMetaobjectAnnotation.getFirstSymbol(),
										"This metaobject annotation is trying to rename methods of a prototype it does not have permission to. Only a generic prototype can add code to itself and only to itself", cyanMetaobjectWithAt.getName(), ErrorKind.metaobject_error);
							}
						}
						if ( !canCommunicateInPackage && ! prototypeName.equals(thisPrototypeName) ) {
							env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "This metaobject annotation is in prototype '" + packageName + "." +
						          thisPrototypeName + "' and it is trying to rename a method of another prototype, '" + packageName + "." +
								          prototypeName + "'. This is illegal because package '" + packageName + "' does not allow that. To make that" +
									 "legal, attach '@feature(communicateInPackage, #on)' to the package in the project (.pyan) file", true, false);
						}
						else {
							compilerManager.renameMethods(cyanMetaobjectWithAt,
									packageName, prototypeName, t.f2, t.f3);
						}

					}
				}
			}
		}
	}



	/*
	 * return a list of all super-prototypes of this prototype. That includes all super-interfaces
	 * (if this is an interface), all super-prototypes (if this is not an interface), and
	 * all implemented interfaces.
	 */
	public ArrayList<ProgramUnit> getAllSuperPrototypes() {

		if ( allSuperPrototypes != null ) {
			return allSuperPrototypes;
		}

		HashSet<ProgramUnit> puSet = new HashSet<>();
		Stack<ProgramUnit> puStack = new Stack<>();
		puStack.add(this);
		while ( ! puStack.isEmpty() ) {
			ProgramUnit current = puStack.pop();
			  // mark current as visited
			puSet.add(current);

			if ( this instanceof InterfaceDec ) {
				if (  ((InterfaceDec ) this).getSuperInterfaceList() != null ) {
					for ( InterfaceDec inter : ((InterfaceDec ) this).getSuperInterfaceList()  ) {
						if ( ! puSet.contains(inter) ) {
							puStack.add(inter);
						}
					}
				}
			}
			else if ( this instanceof ObjectDec ) {
				if ( ((ObjectDec ) this).getSuperobject() != null ) {
					if ( ! puSet.contains(((ObjectDec ) this).getSuperobject())) {
						puStack.add( ((ObjectDec ) this).getSuperobject() );
					}
				}
				if ( ((ObjectDec) this).getInterfaceList() != null ) {
					for ( Expr inter : ((ObjectDec) this).getInterfaceList() ) {
						if ( ! puSet.contains(inter.getType()) ) {
							puStack.add( (ProgramUnit ) inter.getType() );
						}
					}
				}
			}
		}
		allSuperPrototypes = new ArrayList<>();
		for ( ProgramUnit p : puSet ) {
			if ( p != this ) {
				allSuperPrototypes.add(p);
			}
		}
		return allSuperPrototypes;
	}

	/**
	 * to be called only after step 9 of the compilation, dsa2
	   @param env
	   @param superPrototypeList
	 */
	private void checkInheritance_dsa2(Env env) {

		Compiler_dsa compiler_dsa = null;
		for ( ProgramUnit pu : getAllSuperPrototypes() ) {
			ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectAnnotationList = pu.getAttachedMetaobjectAnnotationList();
			if ( metaobjectAnnotationList != null ) {
				for ( CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation : metaobjectAnnotationList ) {
					CyanMetaobjectWithAt cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();
					if ( cyanMetaobject instanceof ICheckSubprototype_dsa2 ) {
						if ( compiler_dsa == null ) {
							compiler_dsa = new Compiler_dsa(env, cyanMetaobjectAnnotation);
						}
						cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);

						try {
							// void dsa_checkSubprototype(ICompiler_dsa compiler_dsa, Type t);
							((ICheckSubprototype_dsa2 ) cyanMetaobject).dsa2_checkSubprototype(compiler_dsa, this);
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
		}
	}


	/**
	 * to be called only after step 8 of the compilation, ati2
	   @param env
	   @param superPrototypeList
	 */
	private void checkInheritance_ati3(Env env) {

		Compiler_ati compiler_ati = null;
		for ( ProgramUnit pu : getAllSuperPrototypes() ) {
			ArrayList<CyanMetaobjectWithAtAnnotation> metaobjectAnnotationList = pu.getAttachedMetaobjectAnnotationList();
			if ( metaobjectAnnotationList != null ) {
				for ( CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation : metaobjectAnnotationList ) {
					CyanMetaobjectWithAt cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();

					if ( cyanMetaobject instanceof ICheckSubprototype_ati3 ) {

						if ( compiler_ati == null ) {
							compiler_ati = new Compiler_ati(env);
						}


						cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);

						try {
							((ICheckSubprototype_ati3 ) cyanMetaobject).ati3_checkSubprototype(compiler_ati, this);
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
		}
	}



	public void setEndSymbol(Symbol endSymbol) {
		this.endSymbol = endSymbol;
	}
	public Symbol getEndSymbol() {
		return endSymbol;
	}

	public boolean getGenericPrototype() {
		return genericPrototype;
	}


	public void setGenericPrototype(boolean genericPrototype) {
		this.genericPrototype = genericPrototype;
	}




	/**
	 * return the current program unit as an Expr.
	 * <code>seed</code> is used to create new Symbols with the same line number as <code>seed</code>
	 */
	@Override
	public Expr asExpr(Symbol seed) {

		ArrayList<ArrayList<Expr>> realTypeListList;
		ArrayList<Expr> realTypeList;
		ArrayList<Symbol> identSymbolArray = new ArrayList<>();



		if ( ! getCompilationUnit().getPackageName().equals(NameServer.cyanLanguagePackageName) ) {
			// insert package symbols first
			for ( Symbol sym : getCompilationUnit().getPackageIdent().getIdentSymbolArray() ) {
				identSymbolArray.add(new SymbolIdent(Token.IDENT, sym.getSymbolString(), seed.getStartLine(),
						seed.getLineNumber(), seed.getColumnNumber(), seed.getOffset(), seed.getCompilationUnit()) );
			}
		}
		// insert the program unit name
		identSymbolArray.add( new SymbolIdent(Token.IDENT, symbol.getSymbolString(), seed.getStartLine(),
				seed.getLineNumber(), seed.getColumnNumber(), seed.getOffset(), seed.getCompilationUnit()) );
		ExprIdentStar newIdentStar = new ExprIdentStar(identSymbolArray, null);


		if ( genericParameterListList != null && this.genericParameterListList.size() > 0 ) {
			/**
			 * an instantiation of a generic prototype such as "Stack<Int>".
			 */
			realTypeListList = new ArrayList<ArrayList<Expr>>();

			ArrayList<ArrayList<GenericParameter>> genParListList = getGenericParameterListList();

			for ( ArrayList<GenericParameter> genParList :  genParListList ) {
				realTypeList = new ArrayList<Expr>();
				for ( GenericParameter gp : genParList ) {
					// realTypeList.add( new ExprIdentStar(new SymbolIdent(Token.IDENT, gp.getName(), -1, -1, -1, -1) ));
					realTypeList.add( gp.getParameter() );
				}
				realTypeListList.add(realTypeList);
			}

			ExprGenericPrototypeInstantiation gpi = new ExprGenericPrototypeInstantiation(
					newIdentStar,
					realTypeListList, null, null);
			return gpi;
		}
		else {
			return newIdentStar;
		}

	}


	/**
	 * generate all Java code that the metaobject annotations of this program unit
	 * demand
	 */
	protected void genJavaClassBodyDemandedByMetaobjectAnnotations(PWInterface pw, Env env) {

		if ( this.completeMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectAnnotation annotation : this.completeMetaobjectAnnotationList ) {

				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject != null && cyanMetaobject instanceof IAction_cge ) {
					IAction_cge cyanMetaobject_cge = (IAction_cge ) cyanMetaobject;
					cyanMetaobject.setMetaobjectAnnotation(annotation);
					StringBuffer code = null;
					try {
						code = cyanMetaobject_cge.cge_javaCodeClassBody();
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, annotation);
					}
					if ( code != null ) {
						pw.print(code);
						pw.print("\n");
					}
				}
			}

		}

	}

	/**
	 * return the number of the next metaobject annotation. This number is incremented so this method has side effects.
	   @return
	 */
	public int getIncMetaobjectAnnotationNumber() {
		++metaobjectAnnotationNumber;
		return metaobjectAnnotationNumber - 1;
	}

	public void setCyanMetaobjectListBeforeExtendsMixinImplements(
			ArrayList<CyanMetaobjectWithAtAnnotation> moListBeforeExtends) {
		this.moListBeforeExtendsMixinImplements = moListBeforeExtends;
	}

	public void addMessageSendWithSelectorsToSuper(
			ExprMessageSendWithSelectorsToSuper msSuper) {
		messageSendWithSelectorsToSuperList.add(msSuper);
	}


	public int getNextFunctionNumber() {
		return nextFunctionNumber;
	}

	public void addNextFunctionNumber() {
		++nextFunctionNumber;
	}

	public ArrayList<ObjectDec> getInnerPrototypeList() {
		return innerPrototypeList;
	}


	public void addInnerPrototype(ObjectDec innerPrototype) {
		innerPrototypeList.add(innerPrototype);
	}


	public ObjectDec getOuterObject() {
		return outerObject;
	}

	public Expr asExpr2(Symbol sym) {
		ArrayList<ArrayList<Expr>> realTypeListList = new ArrayList<>();
		for ( ArrayList<GenericParameter> gpList : this.genericParameterListList ) {
			ArrayList<Expr> realTypeList = new ArrayList<>();
			for ( GenericParameter gp : gpList ) {
				realTypeList.add(gp.getParameter());
			}
			realTypeListList.add(realTypeList);
		}
		ExprIdentStar e = NameServer.stringToExprIdentStar(this.getFullName(), sym);
		return new ExprGenericPrototypeInstantiation(e, realTypeListList, outerObject, null);
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getBeforeEndNonAttachedMetaobjectAnnotationList() {
		return beforeEndNonAttachedMetaobjectAnnotationList;
	}



	public void setBeforeEndNonAttachedMetaobjectAnnotationList(ArrayList<CyanMetaobjectWithAtAnnotation> beforeEndNonAttachedMetaobjectAnnotationList) {
		this.beforeEndNonAttachedMetaobjectAnnotationList = beforeEndNonAttachedMetaobjectAnnotationList;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getNonAttachedMetaobjectAnnotationList() {
		return nonAttachedMetaobjectAnnotationList;
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

	public ExprAnyLiteral searchFeature(String name) {
		for ( Tuple2<String, ExprAnyLiteral> t : featureList ) {
			if ( t.f1.equals(name) ) {
				return t.f2;
			}
		}
		return null;
	}



	@Override
	public DeclarationKind getKind() {
		return DeclarationKind.PROTOTYPE_DEC;
	}


	void check_cin(Env env) {

		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation instanceof ICheck_cin ) {

					try {
						((ICheck_cin) annotation).check(env);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						// e.printStackTrace();
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(annotation.getCyanMetaobject(), annotation);
					}

				}
			}
		}
	}

	/**
	   @param pw
	 */
	protected void genJavaCodeBeforeClassMetaobjectAnnotations(PWInterface pw, Env env) {
		if ( this.attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject instanceof meta.IAction_cge ) {
					annotation.getCyanMetaobject().setMetaobjectAnnotation(annotation);
					StringBuffer sb = null;

					try {
						sb =  ((IAction_cge) annotation.getCyanMetaobject()).cge_codeToAdd();
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobject(cyanMetaobject, this.getFirstSymbol());
					}


					if ( sb != null ) {
						if ( sb.charAt(sb.length()-1) == '\0' )
							sb.deleteCharAt(sb.length()-1);
						pw.print( sb );
					}
				}
			}
		}
		if ( this.nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList ) {
				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject instanceof meta.IAction_cge ) {
					annotation.getCyanMetaobject().setMetaobjectAnnotation(annotation);
					StringBuffer sb = null;

					try {
						sb = ((IAction_cge) annotation.getCyanMetaobject()).cge_codeToAdd();
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobject(cyanMetaobject, this.getFirstSymbol());
					}



					if ( sb != null ) {
						if ( sb.charAt(sb.length()-1) == '\0' )
							sb.deleteCharAt(sb.length()-1);
						pw.print( sb );
					}
				}
			}
		}
		if ( this.completeMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectAnnotation annotation : this.completeMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject() instanceof meta.IAction_cge ) {
					annotation.getCyanMetaobject().setMetaobjectAnnotation(annotation);
					StringBuffer sb = null;

					try {
						sb = ((IAction_cge) annotation.getCyanMetaobject()).cge_javaCodeBeforeClass();
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						CyanMetaobject metaobject = annotation.getCyanMetaobject();
						env.errorInMetaobjectCatchExceptions(metaobject, annotation);
					}

					if ( sb != null ) {
						if ( sb.charAt(sb.length()-1) == '\0' )
							sb.deleteCharAt(sb.length()-1);
						pw.print( sb );
					}
				}
			}
		}
	}

	/**
	   @param pw
	 */
	protected void genJavaCodeStaticSectionMetaobjectAnnotations(PWInterface pw, Env env) {
		/*
		 * insert all code for the static sections asked by the metaobjects of this program unit
		 */
		pw.println("    static {");
		pw.add();
		if ( completeMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectAnnotation annotation : completeMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject() instanceof IAction_cge ) {
					CyanMetaobject metaobject = annotation.getCyanMetaobject();
					metaobject.setMetaobjectAnnotation(annotation);
					StringBuffer code = null;

					try {
						code = ((IAction_cge) metaobject).cge_javaCodeStaticSection();
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(metaobject, annotation);
					}

					if ( code != null ) {
						pw.println( code );
					}
				}
			}
		}


		pw.sub();
		pw.println("    }");
	}

	public Symbol getFirstSymbol() {
		return firstSymbol;
	}



	public void setFirstSymbol(Symbol firstSymbol) {
		this.firstSymbol = firstSymbol;
	}

	public ArrayList<ExprGenericPrototypeInstantiation> getGpiList() {
		return gpiList;
	}


	public void addGpiList(ExprGenericPrototypeInstantiation elem) {
		if ( gpiList == null ) {
			gpiList = new ArrayList<>();
		}
		gpiList.add(elem);
	}


	public String getRealName() {
		return realName;
	}


	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Symbol getSymbolObjectInterface() {
		return symbolObjectInterface;
	}

	public void setSymbolObjectInterface(Symbol symbolObjectInterface) {
		this.symbolObjectInterface = symbolObjectInterface;
	}

	/**
	 * the compiler should assure that these generic instantiations are created in
	 * phase ati. These are the types of metaobject annotations
	 */
	private ArrayList<ExprGenericPrototypeInstantiation> gpiList;


	/**
	 * the list of features associated to this program unit
	 */
	private ArrayList<Tuple2<String, ExprAnyLiteral>> featureList;

	/**
	 * symbol of the object name, "A" in
	 *     object A
	 *        ...
	 *     end
	 */
	protected Symbol symbol;

	/**
	 * list of generic parameters of this program unit if this program unit is a generic one.
	 * There is in fact one list
	 * for each part between < and >. For example, in
	 *       object Proto<T1, T2><R>
	 *  there are two list, the first with two elements and the second with one element.
	 */
	protected ArrayList<ArrayList<GenericParameter>> genericParameterListList;


	/**
	 * The compilation unit corresponding to this program unit. For
	 * example, object Stack (program unit) should be declared in file
	 * Stack.cyan (compilation unit)
	 */
	protected CompilationUnit compilationUnit;

	/**
	 * Visibility of this program unit. It may be PUBLIC, PROTECTED, or PRIVATE
	 */
	protected Token visibility;




	/*
	 * true if this is an instantiation of  generic prototype.
	 * Something like "object Stack<Int> ... end".
	 */
	private boolean prototypeIsNotGeneric;


	/**
	 * a list of all metaobject annotations inside and before this program unit.
	 * This list includes attachedMetaobjectAnnotationList and beforeEndNonAttachedMetaobjectAnnotationList.
	 * Every metaobject annotation inside the program unit is in this list.
	 */
	protected ArrayList<CyanMetaobjectAnnotation>  completeMetaobjectAnnotationList;


	/**
	 * metaobject annotations placed just before this program unit such as {@literal @}checkStyle in <br>
	 * <code>
	 * {@literal @}checkStyle object Proto<br>
	 *    ...<br>
	 * end<br>
	 * These metaobject annotations are attached to the program unit declaration
	 * </code>
	 */
	protected ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList;
	/**
	 * metaobject annotations placed just before this program unit such as {@literal @}javacode in <br>
	 * <code>
	 * {@literal @}javacode{* ... *} <br>
	 * object Proto<br>
	 *    ...<br>
	 * end<br>
	 * These metaobject annotations are NOT attached to the program unit declaration
	 * </code>
	 */
	protected ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList;

	/**
	 * metaobject annotations placed just before keyword 'end' of this program unit.
	 * These metaobject annotations are NOT attached to any declaration
	 * </code>
	 */
	protected ArrayList<CyanMetaobjectWithAtAnnotation> beforeEndNonAttachedMetaobjectAnnotationList;


	/**
	 * the symbol of 'end' at the end of the program unit
	 */
	protected Symbol endSymbol;

	/**
	 * true if this prototype is generic. False in prototypes like Stack{@literal <}Int>
	 */
	protected boolean genericPrototype;
	/**
	 * Each metaobject annotation in a prototype (not an interface) has a number. This instance variable keeps the
	 * number of the next metaobject annotation. This number is used when metaobjects are communicating with
	 * each other.
	 */

	private int	metaobjectAnnotationNumber;

	/**
	 * list of metaobjects that can appear before keyword 'extends', 'mixin', or 'implements
	 */
	protected ArrayList<CyanMetaobjectWithAtAnnotation>	moListBeforeExtendsMixinImplements;

	/**
	 * list of message sends to super in this program unit. It is used to generate code.
	 * Message sends to super are generated by calling a private Java method.
	 */
	protected ArrayList<ExprMessageSendWithSelectorsToSuper> messageSendWithSelectorsToSuperList;


	/**
	 * the next number to the associated to an anonymous functions. Functions inside a prototype receive numbers in textual order. Starts with 0
	 */
	protected int	nextFunctionNumber;

	/**
	 * list of inner objects of this object. These inner objects are created by the compiler.
	 * For each anonymous functions in the object the compiler creates one prototype that is inserted in
	 * this list. For each method in the object the compiler creates one prototype that is also
	 * inserted in this list.
	 */
	protected ArrayList<ObjectDec>	innerPrototypeList;

	/**
	 * if this object is inside another object, outerObject points to this outer object. Otherwise it is null
	 */
	protected ObjectDec	outerObject;

	/**
	 * full name including the package name. It may be somethink like <br>
	 * {@code Tuple<main.Program, people.bank.Client>}<br>
	 * This fullName is only set in the semantic analysis and after method getFullName is called.
	 */
	private String fullName;

	protected String javaName;

	private Symbol firstSymbol;

	/**
	 * the symbol 'object' or 'interface'
	 */
	private Symbol symbolObjectInterface;

	/**
	 * real name of this prototype, including its generic parameters, if any
	 */
	private String realName;

	/**
	 * all super-prototypes of this program unit. If this is ObjectDec, this list includes all super-prototypes and all
	 * implemented interfaces. If this is InterfaceDec, this list includes all super-interfaces.
	 */
	private ArrayList<ProgramUnit> allSuperPrototypes;


}
