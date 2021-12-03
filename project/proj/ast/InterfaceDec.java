package ast;



import java.util.ArrayList;
import error.ErrorKind;
import lexer.Lexer;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;
import meta.ICompiler_dsa;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
 * Represents the declaration of an interface
 * @author José
 *
 */

public class InterfaceDec extends ProgramUnit {

	public InterfaceDec(ObjectDec outerObject, Symbol interfaceSymbol, SymbolIdent symbol, Token visibility,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList, 
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList, Lexer lexer) {
		super(visibility, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, outerObject);
		lexer.setProgramUnit(this);
		this.interfaceSymbol = interfaceSymbol;
		this.symbol = symbol;
		methodSignatureList = new ArrayList<MethodSignature>();
		allMethodSignatureList = null;
		javaInterfaceName = NameServer.getJavaName(symbol.getSymbolString());
	}

	@Override
	public void accept(ASTVisitor visitor) {
		
		visitor.preVisit(this);
		
		if ( superInterfaceList != null ) {
			for ( InterfaceDec inter : this.superInterfaceList ) {
				inter.accept(visitor);
			}
		}
		if ( methodSignatureList != null ) {
			for ( MethodSignature ms : this.methodSignatureList ) {
				ms.accept(visitor);
			}
		}
		visitor.visit(this);
	}
	
	
	
	@Override
	public InstanceVariableDec searchInstanceVariableDec(String varName) {
		return null;
	}


	@Override
	public InstanceVariableDec searchInstanceVariable(String name) {
		return null;
	}

	@Override
	public InstanceVariableDec searchInstanceVariablePrivateProtectedSuperProtected(java.lang.String varName) {
		return null;
	}


	@Override
	public InstanceVariableDec searchInstanceVariableDecProtected(java.lang.String varName) {
		return null;
	}

	
	
	@Override
	public void genCyan(PWInterface pw, CyanEnv cyanEnv, boolean genFunctions) {

		cyanEnv.atBeginningOfProgramUnit(this);


		ExprGenericPrototypeInstantiation exprGPI = cyanEnv.getExprGenericPrototypeInstantiation();
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.println("@genericPrototypeInstantiationInfo(\"" + cyanEnv.getPackageNameInstantiation() + "\", \"" + cyanEnv.getPrototypeNameInstantiation()
			  + "\", " + exprGPI.getFirstSymbol().getLineNumber() + ", " + exprGPI.getFirstSymbol().getColumnNumber() + ")");
		}
		
		super.genCyan(pw, cyanEnv, genFunctions);
		pw.println("");
		pw.print(NameServer.getVisibilityString(visibility) + " ");
		pw.print("interface "); 
		
		genCyanProgramUnitName(pw, cyanEnv);
		
		if ( moListBeforeExtendsMixinImplements != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : moListBeforeExtendsMixinImplements ) {
				annotation.genCyan(pw, false, cyanEnv, genFunctions);
				pw.print(" ");
			}
		}
		
		
		if ( superInterfaceExprList != null ) {
    		pw.add();   pw.add();
	    	int size = this.superInterfaceExprList.size();
	    	pw.printIdent("  ");
		    if ( size > 0 )
			    pw.print("extends ");
		    for ( Expr si : this.superInterfaceExprList ) {
			    si.genCyan(pw, false, cyanEnv, genFunctions);
			    if ( --size > 0 )
    				pw.print(", ");
	    	}
		    pw.sub();   pw.sub();
		}
	    pw.println("");
		
		pw.add();
		
		
		for (MethodSignature ms : this.methodSignatureList) {
			ms.genCyanMetaobjectAnnotations(pw, true, cyanEnv, genFunctions);
			pw.printIdent("func ");
			ms.genCyan(pw, true, cyanEnv, genFunctions);
			pw.println();
		}
		pw.sub();

		if ( beforeEndNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation c : this.beforeEndNonAttachedMetaobjectAnnotationList )
				c.genCyan(pw, true, cyanEnv, genFunctions);
		}
		
		pw.printlnIdent("end");
		pw.println("");
		
		cyanEnv.atEndOfCurrentProgramUnit();
		
	}
	
	@Override
	public void calcInterfaceTypes(Env env) {


		env.atBeginningOfObjectDec(this);


		super.calcInterfaceTypes(env);

		for ( ArrayList<GenericParameter> genericParameterList : genericParameterListList ) 
			for ( GenericParameter genericParameter : genericParameterList )
				genericParameter.calcInternalTypes(env);		
		String thisInterfaceName = this.getName();
		if ( superInterfaceExprList != null && superInterfaceExprList.size() > 0 ) {
			superInterfaceList = new ArrayList<>(); 
			for ( Expr anInterface : superInterfaceExprList ) {
				anInterface.calcInternalTypes(env);
				if ( anInterface.getType(env) == null ) {
					env.error(anInterface.getFirstSymbol(), "Interface '" + anInterface.asString() + "' was not found");
					return;
				}
				InterfaceDec superInterface = (InterfaceDec ) anInterface.getType(env) ; 
				superInterfaceList.add( superInterface );
				
				String superInterfaceName = anInterface.ifPrototypeReturnsItsName(env);
				if ( !( anInterface.getType(env) instanceof InterfaceDec ) )
					env.error(anInterface.getFirstSymbol(), superInterfaceName + " is not an interface", true, true);
			
				if ( this == superInterface ) 
					env.error(anInterface.getFirstSymbol(), "Interface " + thisInterfaceName + " is inheriting from itself", true, true);
				/*
				for ( InterfaceDec superInter : superInterfaceList ) {
					if ( superInter == superInterface ) {
						env.error(anInterface.getFirstSymbol(), "Duplicate super interface", true);
						break;
					}
				}
				*/
				int i = 0;
				int size = superInterfaceList.size();
				while ( i < size - 1 ) {
					if ( superInterfaceList.get(i) == superInterface ) {
						env.error(anInterface.getFirstSymbol(), "Duplicate super interface '" + superInterface.getName() + "'", true, true);
						break;
					}
					++i;
				}
				if ( i != size - 1 )
					break;
			}
		}
		 
		ArrayList<String> methodNameList = new ArrayList<String>(); 
		for ( MethodSignature methodSignature: methodSignatureList ) {
			   // atEndMethodDec clear the lists of parametes
			env.atEndMethodDec();
			methodSignature.calcInterfaceTypes(env);
			String methodName = methodSignature.getFullName(env);
			for ( int i = 0; i < methodNameList.size(); ++i ) {
				if ( methodName.compareTo(methodNameList.get(i)) == 0 )
					env.error(methodSignature.getFirstSymbol(), "Duplicate method signature with that of line " 
							+ methodSignatureList.get(i).getFirstSymbol().getLineNumber(), true, true);
			}
			methodNameList.add(methodName);
		}
		if ( beforeEndNonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : beforeEndNonAttachedMetaobjectAnnotationList ) {
				annotation.calcInternalTypes(env);
			}
		}		
			
		env.atEndOfObjectDec();
	}
	
	
	@Override
	public void calcInternalTypes(ICompiler_dsa compiler_dsa, Env env) {
		
		makeMetaobjectAnnotationsCommunicateInPrototype(env);		
		
		super.calcInternalTypes(compiler_dsa, env);
		
		for ( MethodSignature ms : this.methodSignatureList ) {
			String name = ms.getNameWithoutParamNumber();
			if ( name.compareTo("init") == 0 || name.compareTo("init:") == 0 || name.compareTo("new") == 0 ||
					name.compareTo("new:") == 0 ) {
				env.error(true, ms.getFirstSymbol(), "'init', 'init:', 'new', or 'new:' methods cannot be declared in interfaces", 
						ms.getNameWithoutParamNumber(), ErrorKind.init_new_methods_cannot_be_declared_in_interfaces);
			}
		}

	}
		
	@Override
	public void genJava(PWInterface pw, Env env) {

		env.atBeginningOfObjectDec(this);
		
		
		genJavaCodeBeforeClassMetaobjectAnnotations(pw, env);

		
		pw.println();
		
		if ( this.visibility == Token.PRIVATE )
			pw.print("private ");
		else if ( this.visibility != Token.PACKAGE )
			pw.print("public ");
		
		pw.printIdent("interface " + getJavaNameWithoutPackage());
		
		if ( moListBeforeExtendsMixinImplements != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : moListBeforeExtendsMixinImplements ) {
				annotation.genJava(pw, env);
				pw.print(" ");
			}
		}		
		
		
		if ( superInterfaceExprList == null ) {
		    pw.print(" extends " + NameServer.IAny);
		}
		else {
    		pw.add();   pw.add();  pw.add();
	    	int size = this.superInterfaceExprList.size();
		    if ( size > 0 )
			    pw.print(" extends ");
		    for ( Expr si : this.superInterfaceExprList ) {
			    // si.genJavaExprWithoutTmpVar(pw, env);
		    	pw.print(si.getType().getJavaName());
			    pw.print(" ");
			    if ( --size > 0 )
    				pw.print(", ");
	    	}
		    pw.sub();   pw.sub();  pw.sub();
		}
		pw.println(" {");
		pw.add();
		for (MethodSignature ms : this.methodSignatureList) {
			/*  default CyInt  _sum_2_mult_1( CyInt _a, CyInt _b, CyInt tmp4662 ) {         
    				throw new ExceptionContainer__(_ExceptionCannotCallInterfaceMethod.prototype);
    			}			  
			 */
			pw.printIdent("default ");
			ms.genJava(pw, env);
			pw.println("{ throw new ExceptionContainer__(_ExceptionCannotCallInterfaceMethod.prototype); } ");
		}
		
		// genJavaCodeStaticSectionMOCalls(pw);
		

		// genJavaClassBodyMOCall(pw);
		
		
		pw.sub();
		pw.printlnIdent("}");
		
		env.atEndOfObjectDec();
		
	}
	
	public void genCyanProtoInterface(PW pw) {

		String name = getName();
		pw.println("");
		pw.println("object " + NameServer.prototypeFileNameFromInterfaceFileName(Lexer.addSpaceAfterComma(name)) + 
				" implements " + Lexer.addSpaceAfterComma(name) );
		pw.println("");
		/*
		pw.add();

		for (MethodSignature ms : this.methodSignatureList) {
			pw.printIdent("func ");
			ms.genCyan(pw, true, cyanEnv, true);
			pw.println(" {");
			pw.add();
			pw.printlnIdent("throw: ExceptionCannotCallInterfaceMethod");
			pw.sub();
			pw.printlnIdent("}");
			pw.println();
		}
		
		pw.sub();
		*/
		pw.println("end");
		
	}
	
	
	public void setSuperInterfaceExprList(ArrayList<Expr> superInterfaceList) {
		this.superInterfaceExprList = superInterfaceList;
	}

	public ArrayList<Expr> getSuperInterfaceExprList() {
		return superInterfaceExprList;
	}

	public void addMethodSignature(MethodSignature ms) {
		methodSignatureList.add(ms);
	}

	public void setMethodSignatureList(ArrayList<MethodSignature> methodSignatureList) {
		this.methodSignatureList = methodSignatureList;
	}

	public ArrayList<MethodSignature> getMethodSignatureList() {
		return methodSignatureList;
	}

	public ArrayList<MethodSignature> getAllMethodSignatureList() {
		if ( allMethodSignatureList == null ) {
			allMethodSignatureList = new ArrayList<MethodSignature>();
			this.allMethodSignatureList.addAll(this.methodSignatureList);
			if ( superInterfaceList != null ) {
				for ( InterfaceDec superInterface : this.superInterfaceList ) {
					allMethodSignatureList.addAll(superInterface.getAllMethodSignatureList());
				}
			}
		}
		return this.allMethodSignatureList;
	}


	@Override
	public Symbol getFirstSymbol() {
		return interfaceSymbol;
	}

	@Override
	public ArrayList<MethodSignature> searchMethodPublicSuperPublic(
			String methodName, Env env) {
		ArrayList<MethodSignature> foundMethodSignatureList = searchMethodPublicSuperPublicOnlyInterfaces(methodName, env); 
		if ( foundMethodSignatureList.size() == 0 ) {
			/*
			 * search in Any
			 */
			ObjectDec any = (ObjectDec ) Type.Any;
			return any.searchMethodPublicSuperPublic(methodName, env);
		}
		return foundMethodSignatureList;
	}
	
	
	
	public ArrayList<MethodSignature> searchMethodPublicSuperPublicOnlyInterfaces(
			String methodName, Env env) {
		ArrayList<MethodSignature> foundMethodSignatureList = new ArrayList<MethodSignature>();
		for ( MethodSignature m : methodSignatureList ) {
			if ( m.getName().equals(methodName) )
				foundMethodSignatureList.add(m);
		}
		if ( foundMethodSignatureList.size() != 0 ) 
			return foundMethodSignatureList;
		else {
			
			if ( superInterfaceExprList != null ) {
				for ( Expr expr : superInterfaceExprList ) {
					ProgramUnit superInterface = (ProgramUnit ) expr.getType(env);
					foundMethodSignatureList = superInterface.searchMethodPublicSuperPublic(methodName, env);
					if ( foundMethodSignatureList.size() > 0 )
						return foundMethodSignatureList;
				}
			}
			return foundMethodSignatureList;
		}
	}
	
	@Override
	public ArrayList<MethodSignature> searchMethodPrivateProtectedPublicSuperProtectedPublic(
			String methodName, Env env) {
		return searchMethodPublicSuperPublic(methodName, env);
	}
	
	
	
	@Override
	public ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublic(
			String methodName, Env env) {
		return searchMethodPublicSuperPublic(methodName, env);
	}
	

	public ArrayList<MethodSignature> searchMethodPrivateProtectedPublicSuperProtectedPublicOnlyInterfaces(
			String methodName, Env env) {
		return searchMethodPublicSuperPublicOnlyInterfaces(methodName, env);
	}
	
	
	
	public ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublicOnlyInterfaces(
			String methodName, Env env) {
		return searchMethodPublicSuperPublicOnlyInterfaces(methodName, env);
	}
	
	
	
	
	@Override
	public boolean isSupertypeOf(Type otherType, Env env) {
		
		if ( otherType instanceof TypeDynamic ) 
			return true;
		// String thisName = this.getName();
		// String otherTypeName = otherType.getName();
		if ( this == otherType ) 
			return true;
		if ( otherType instanceof InterfaceDec ) {
			InterfaceDec otherInter = (InterfaceDec ) otherType;
			if ( otherInter.getSuperInterfaceExprList() != null ) {
				for ( Expr superInterfaceExpr : otherInter.getSuperInterfaceExprList() ) {
					ProgramUnit superInterface = (ProgramUnit ) superInterfaceExpr.ifRepresentsTypeReturnsType(env);
					if ( this.isSupertypeOf(superInterface, env) )
						return true;
				}
			}
		}
		else if ( otherType instanceof ObjectDec ) {
			ObjectDec otherProto = (ObjectDec ) otherType;
			if ( otherProto.getInterfaceList() != null ) {
				for ( Expr superInterfaceExpr : otherProto.getInterfaceList() ) {
					ProgramUnit superInterface = (ProgramUnit ) superInterfaceExpr.ifRepresentsTypeReturnsType(env);
					if ( this.isSupertypeOf(superInterface, env) )
						return true;
				}
			}
			if ( otherProto.getSuperobject() == null ) 
				return false;
			else 
				return isSupertypeOf(otherProto.getSuperobject(), env);
		}
		else
			env.error(null,  "Internal error in InterfaceDec::isSupertypeOf: unknown type", true, true);
		return false;
	}

	
	@Override
	public boolean getIsFinal() {
		return false;
	}

	
	@Override
	void check_cin(Env env) {
		super.check_cin(env);
		for ( MethodSignature ms : methodSignatureList ) {
			ms.check_cin(env);
		}
	}

	

	public ArrayList<InterfaceDec> getSuperInterfaceList() {
		return superInterfaceList;
	}
	
	
	/**
	 * the symbol of keyword 'interface'
	 */
	private Symbol interfaceSymbol;


	/**
	 * the super-interfaces of this interface as Expr
	 */
	private ArrayList<Expr> superInterfaceExprList;
	
	/**
	 * the super-interfaces of this interface.
	 */
	private ArrayList<InterfaceDec> superInterfaceList;	
	/**
	 * list of method signatures
	 */
	private ArrayList<MethodSignature> methodSignatureList;
	
	
	/**
	 * list of all method signatures, including of the super interfaces
	 */
	private ArrayList<MethodSignature> allMethodSignatureList;
	
	/**
	 * Name in Java of the interface
	 */
	private String javaInterfaceName;
}
