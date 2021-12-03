package meta.cyanLang;

/**
 * Use this metaobject as in
 * <code>
    @demands{*
        T belongs [ "Int", "Short", "Byte", "Char", "Long" ],
        U implements Savable,
        R sub-prototype Person
    *}
    object Proto<T, U, R>
        ...
    end
    </code>
    
 * 
 * 
 * 
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ast.CompilationUnit;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Declaration;
import ast.Expr;
import ast.ExprIdentStar;
import ast.ExprTypeof;
import ast.GenericParameter;
import ast.InterfaceDec;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureOperator;
import ast.MethodSignatureUnary;
import ast.MethodSignatureWithSelectors;
import ast.ObjectDec;
import ast.ParameterDec;
import ast.ProgramUnit;
import ast.SelectorWithParameters;
import ast.VariableKind;
import error.CompileErrorException;
import error.FileError;
import lexer.Lexer;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.SymbolKeyword;
import lexer.Token;
import meta.CompilerGenericProgramUnit_dpa;
import meta.Compiler_dpa;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionGenericProgramUnit_dpa;
import meta.ICheckProgramUnit_before_dsa;
import meta.ICompilerGenericProgramUnit_dpa;
import meta.ICompiler_ati;
import meta.ICompiler_dpa;
import meta.ICompiler_dsa;
import meta.IExpr;
import meta.IListAfter_ati;
import meta.IParseWithCyanCompiler_dpa;
import meta.IType;
import saci.CompilationStep;
import saci.Compiler;
import saci.CompilerManager;
import saci.DirectoryPackage;
import saci.Env;
import saci.Function0;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple5;

class ProtoInfo {
	public ProtoInfo(String name) {
		this.name = name;
	}
	public String name;
	public boolean isPrototype = false;
	public boolean isInterface = false;
	public boolean isSymbol = false;
	public ArrayList<String> superprototypeList = null;
	public ArrayList<String> subprototypeList = null;
	public ArrayList<String> interfaceImplementList = null;
	public ArrayList<MethodSignature> methodSignatureList = null;
	   // methods that should be preceded by 'override'
	public ArrayList<MethodSignature> overrideMethodSignatureList = null;
	public ArrayList<String> inList = null;
	public ArrayList<String> axiomList = null;
	   /*
	    * if isList is not null, the generic parameter appears in the the left-hand side of an equation such as T in<br>
	    * <code> T is A_Prototype</code><br>
	    * In this case, no prototype in the test cases should be created for T: it already exists. 
	    */
	public ArrayList<IExpr> isList = null;
	/**
	 * all method signatures, including those of 'would-to-be' super-prototypes and 
	 * super-interfaces. That is, if we have formal parameters A, B, and C and restrictions<br>
	 * <code>
	 *     A extends Matriz<br>
	 *     B extends A, <br>
	 *     A implements I1, <br>
	 *     C extends B, <br>
	 *     B implements I2, <br>
	 *     C implements I3<br>
	 *     
	 * </code> <br>
	 * 
	 * Then instance variable allMethodSignatures of C would contain all method signatures of Matriz, A, B, I1, B, I2, and I3 
	 */
	public Set<MethodSignature> allMethodSignatures;
	/**
	 * all method signatures as strings
	 */
	public Set<String> allFullNameMethodSignatures;
	
}

abstract class Node {
	/**
	 * typeOrSymbol may be a type or just a symbol 
	 */
	public IExpr typeOrSymbol;
	/**
	 * true if typeOrSymbol is a symbol
	 */
	public boolean isSymbol = false;
	/*
	 * true if the expression is preceded by '!' as in<br>
	 * <code>
	 *     ! T subtype Company
	 * </code>
	 */
	public boolean precededByNot = false;
	public String errorMessage;
	/**
	 * true if the first element in a node is an identifier such as T in <br>
	 * <code>
	 * T implements ICompany<br>
	 * </code>
	 */
	public boolean isIdentifier;
	/**
	 * if isIdentifier is true, the name of the identifier
	 */
	public String name;
	/**
	 * return false if there was an error
	   @param annotation
	   @param compiler
	   @return
	 */
	abstract public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler);
	
	public Tuple2<StringBuffer, Integer> axiomTestCode_dsa(int numberNextMethod) {
		return null;
	}

	public ArrayList<Function0> createFunctionList(Function0 f) {
		ArrayList<Function0> functionList = new ArrayList<>();
		functionList.add(f);
		return functionList;
	}

	/**
	 * collect data on generic parameters during semantic analysis. Then semantic information is 
	 * available. This data is collected only on instantiated generic prototypes. 
	 * 
	   @param map
	   @return
	 */

	abstract public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map);
	/**
	 * collect data on generic parameters during parsing. Then semantic information is 
	 * not available. This data is collected only on generic prototypes. That does not
	 * include instantiated generic prototypes. 
	 * 
	   @param map
	   @return
	 */
	public ArrayList<String> collectGPData_dpa(HashMap<String, ProtoInfo> map) {
		return null;
	}

	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		if ( ! gpSet.contains(this.name) ) {
			if ( gpSet.size() == 0 || !(this.typeOrSymbol instanceof ast.ExprTypeof) ) {
				((Expr ) this.typeOrSymbol).calcInternalTypes(env);
			}
		}
	}
}

class IsNode extends Node {

	public IsNode() {  }
	
	public IExpr isTypeExpr;
	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		
		IType t = typeOrSymbol.getIType();
		IType isType = isTypeExpr.getIType();
		if ( precededByNot ) {
			if ( t == isType ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation(
								//"'" + t.getFullName() + "' is equal to "
								//+ isType.getFullName() + ". The concept associated to this generic prototype expected them to be equal."
								
									"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
									"' expected that '" + t.getFullName() + "' were NOT equal to '" 
									+ isType.getFullName() + "'"); } );
			}
		}
		else {
			if ( t != isType ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation(
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" + t.getFullName() + "' were equal to '" 
								+ isType.getFullName() + "'"); } );
			}
		}
		return null;
	}

	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);
		if ( pi != null ) {
			if ( pi.isList == null ) {
				pi.isList = new ArrayList<>();
			}
			pi.isList.add(isTypeExpr);
			/*
			String s = ((Expr ) isTypeExpr).asString();
			if ( map.containsKey(s) ) {
				pi.isList.add( s );
			}
			else {
				pi.isList.add( ((Expr ) isTypeExpr).getType().getFullName() );
			}
			*/
		}
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
		if ( gpSet.size() == 0 || !(this.isTypeExpr instanceof ast.ExprTypeof) ) {
			((Expr ) isTypeExpr).calcInternalTypes(env);
		}
		/*
		if ( ! gpSet.contains(isTypeExpr.asString()) ) {
		}
		*/
	}
}




class IsInterfaceNode extends Node {

	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		
		IType t = typeOrSymbol.getIType();
		if ( precededByNot ) {
			if ( (t instanceof InterfaceDec) ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation( 
								
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" + 
								"'" + t.getFullName() + "' were NOT an interface. "
								); } );
			}
		}
		else {
			if ( !(t instanceof InterfaceDec) ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation(								
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" + 
								"'" + t.getFullName() + "' were an interface. "
								); } );

			}
		}
		return null;
	}

	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);
		if ( pi != null ) {
			pi.isInterface = true;
		}
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
	}
}


class IsPrototypeNode extends Node {

	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		
		IType t = typeOrSymbol.getIType();
		if ( precededByNot ) {
			if ( (t instanceof ObjectDec) ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation(
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" +
								"'" + t.getFullName() + "' were NOT a prototype (it may be an interface)"); } );
			}
		}
		else {
			if ( !(t instanceof ObjectDec) ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation(
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" +
								"'" + t.getFullName() + "' were a prototype (it may not be an interface)"); } );
			}
		}
		return null;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);
		if ( pi != null ) {
			pi.isPrototype = true;
		}
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
	}	
}

class IsSymbolNode extends Node {

	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {

		String typeName = typeOrSymbol.ifPrototypeReturnsItsName();
		isSymbol = Lexer.isSymbol(typeName);
		if ( precededByNot ) {
			if ( isSymbol ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation(
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" + typeName + "' were NOT a symbol"); } );
			}
		}
		else {
			if ( ! isSymbol ) {
				return createFunctionList( 
						() -> { compiler.errorAtGenericPrototypeInstantiation( 
								"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
								"' expected that '" + typeName + "' were a symbol"); } );
			}
		}
		return null;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);
		if ( pi != null ) {
			pi.isSymbol = true;
		}
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
	}	
}

class AxiomParameter {
	public String name;
	public String strType;
}


class AxiomNode extends Node {
	public char []funcText;
	public ArrayList<AxiomParameter> paramList;
	public String methodName;
	// public MethodSignature methodSignature;

	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		return null;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		
		/*ProtoInfo pi = map.get(name);
		if ( pi != null ) {
			if ( pi.axiomList == null ) {
				pi.axiomList = new ArrayList<>();
			}			
			pi.axiomList.add( new String(funcText) );
		}
		*/
		return null;
	}

	@Override
	public Tuple2<StringBuffer, Integer>  axiomTestCode_dsa(int numberNextMethod) {
		StringBuffer s = new StringBuffer();
		
		s.append("\n");
		s.append("    func " + methodName.substring(0, methodName.length()-1) + "_" + numberNextMethod + ": ");
		int size = this.paramList.size();
		for ( AxiomParameter p : this.paramList ) {
			s.append(p.strType + " " + p.name);
			if ( --size > 0 ) {
				s.append(", ");
			}
		}
		s.append(" -> String|Nil { \n");
		s.append(this.funcText);
		s.append("\n");
		s.append("    }\n\n");
		
		return new Tuple2<StringBuffer, Integer>(s, numberNextMethod + 1);
	}
	
	
	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
	}	
	
}

class TypeInNode extends Node {
	//public ArrayList<String> typeList;
	/*
	 * typeList may be a list of types or simple a list of symbols.
	 */
	public ArrayList<IExpr> typeList;
	
	@Override
	public ArrayList<Function0>  check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		
		IType t = typeOrSymbol.getIType();
		if ( !(t instanceof ProgramUnit) ) {
			return createFunctionList(
					() -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 							
							"'" + t.getFullName() + "' were a prototype"); } );
		}
		boolean found = false;
		String nameLeft = t.getFullName();
		if ( nameLeft.startsWith(NameServer.cyanLanguagePackageName) ) {
			nameLeft = nameLeft.substring(NameServer.cyanLanguagePackageDirectory.length() + 1);
		}
		for ( IExpr exprType : typeList ) {
			if ( exprType.getIType()  == t  ) {
				found = true;
				break;
			}
		}
		if ( (!precededByNot && ! found) || (precededByNot && found) ) {
			int size = typeList.size();
			String strList = "";
			for ( IExpr exprType : typeList )  {
				strList += exprType.getIType().getFullName();
				if ( --size > 0 ) {
					strList += ", ";
				}
			}
			if ( errorMessage != null ) {
				return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
			}
			else {
				if ( !precededByNot ) {
					final String message = 	"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + nameLeft + "' were one element of the following list: " + strList;
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(message); } );
				}
				else {
					final String message = 	"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + nameLeft + "' were NOT one element of the following list: " + strList;
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(message); } );
				}
			}
		}
		return null;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);

		if ( pi != null ) {
			if ( pi.inList == null ) {
				pi.inList = new ArrayList<>();
			}
			for ( IExpr exprType : typeList ) {
				String s = exprType.asString();
				if ( map.containsKey(s) ) {
					pi.inList.add( s );
				}
				else {
					pi.inList.add( exprType.getIType().getFullName() );
				}
			}
		}
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
		for ( IExpr exprType : typeList ) {
			if ( gpSet.size() == 0 || !(exprType instanceof ast.ExprTypeof) ) {
				((Expr ) exprType).calcInternalTypes(env);
			}
		}
	}	
	
	
}


class SymbolInNode extends Node {
	//public ArrayList<String> typeList;
	/*
	 * typeList may be a list of types or simple a list of symbols.
	 */
	public ArrayList<ExprIdentStar> symbolList;
	
	@Override
	public ArrayList<Function0>  check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {

		ExprIdentStar left = (ExprIdentStar ) this.typeOrSymbol;
		String leftString = left.getName();
		boolean found = false;
		for ( ExprIdentStar id : symbolList ) {
			if ( id.getName().equals(leftString) ) {
				found = true;
				break;
			}
		}
		if ( (! found && ! precededByNot) || (found && precededByNot) ) {
			int size = symbolList.size();
			String strList = "";
			for ( ExprIdentStar id : symbolList )  {
				strList += id.getName();
				if ( --size > 0 ) {
					strList += ", ";
				}
			}
			if ( errorMessage != null ) {
				return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
			}
			else {
				if ( ! precededByNot ) {
					final String message = "A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + leftString + 
							"' were one of the following symbols: " + strList;
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(message); } );
				}
				else {
					final String message = "A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + leftString + 
							"' were NOT one of the following symbols: " + strList;
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(message); } );
				}
			}
		}
		return null;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);

		if ( pi != null ) {
			if ( pi.inList == null ) {
				pi.inList = new ArrayList<>();
			}
			for ( ExprIdentStar id : symbolList ) {
				pi.inList.add( id.asString() );
			}
		}
		
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
	}	
	
	
}


class ImplementsNode extends Node {
	public IExpr exprInterface;
	
	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		IType t = typeOrSymbol.getIType();
		if ( !(t instanceof ObjectDec) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 							
							"'" + t.getFullName() + "' were a prototype"); } );
		}
		IType interType = exprInterface.getIType();
		if ( !(interType instanceof InterfaceDec) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation("'" + interType.getFullName() + "' is not an interface"); } );
		}
		if ( precededByNot ) {
			if ( interType.isSupertypeOf(t, compiler.getEnv()) ) {
				if ( errorMessage != null ) {
					return createFunctionList( 
							() -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
				}
				else 
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 							
							
							"'" + t.getFullName() + "' did NOT implement interface '" + interType.getFullName() + "'"); } );
			}
		}
		else {
			if ( ! interType.isSupertypeOf(t, compiler.getEnv()) ) {
				if ( errorMessage != null ) {
					return createFunctionList( 
							() -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
				}
				else 
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 							
							
							"'" + t.getFullName() + "' implemented interface '" + interType.getFullName() + "'"); } );
			}
		}
		return null;
	}
	
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);

		if ( pi != null ) {
			if ( pi.interfaceImplementList == null ) {
				pi.interfaceImplementList = new ArrayList<>();
			}
			String s = exprInterface.asString();
			if ( map.containsKey(s) ) {
				ProtoInfo other = map.get(s);
				pi.interfaceImplementList.add(s);
				/*
				 * then s should be marked as an interface
				 */
				other.isInterface = true;
			}
			else {
				pi.interfaceImplementList.add(exprInterface.getIType().getFullName());
			}
		}
		
		return null;
	}


	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
		if ( gpSet.size() == 0 || !(exprInterface instanceof ast.ExprTypeof) ) {
			((Expr ) this.exprInterface).calcInternalTypes(env);
		}
	}	
	
	
}

class SubprototypeNode extends Node {
	public IExpr exprSuperprototype;
	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		IType t = typeOrSymbol.getIType();
		   // dangerous
		if ( !(t instanceof ObjectDec) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							t.getFullName() + "' were a prototype"
							); } );
		}
		IType supertype = exprSuperprototype.getIType();
		if ( !(supertype instanceof ProgramUnit) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							supertype.getFullName() + "' were a prototype"); } );
		}
		
		ObjectDec subType = (ObjectDec ) t;
		subType.calcInterfaceSuperTypes(compiler.getEnv());
		
		if ( precededByNot ) { 
			if ( supertype.isSupertypeOf(subType, compiler.getEnv()) ) {
				if ( errorMessage != null ) {
					return createFunctionList( 
							() -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
				}
				else 
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							"'" + t.getFullName() + "' were NOT a subtype of '"+ supertype.getFullName() + "'"); } );
			}
		}
		else {
			if ( ! supertype.isSupertypeOf(subType, compiler.getEnv()) ) {
				if ( errorMessage != null ) {
					return createFunctionList( 
							() -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
				}
				else 
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							"'" + t.getFullName() + "' were a subtype of '"+ supertype.getFullName() + "'"); } );
			}
		}
		return null;
	}
	
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);

		if ( pi != null ) {
			if ( pi.superprototypeList == null ) {
				pi.superprototypeList  = new ArrayList<>();
			}
			String s = exprSuperprototype.asString();
			if ( map.containsKey(s) ) {
				ProtoInfo other = map.get(s);
				other.isPrototype = true;
				pi.superprototypeList.add(s);
			}
			else {
				pi.superprototypeList.add(exprSuperprototype.getIType().getFullName());
			}
		}
		
		return null;
	}


	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
		if ( gpSet.size() == 0 || !(exprSuperprototype instanceof ast.ExprTypeof) ) {
			((Expr ) this.exprSuperprototype).calcInternalTypes(env);
		}
	}	
	
}

class SupertypeNode extends Node {
	public IExpr exprSubtype;
	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		IType t = typeOrSymbol.getIType();
		   // dangerous
		if ( !(t instanceof ObjectDec) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							"'" + t.getFullName() + "' were a prototype"
							); } );
		}
		IType subType = exprSubtype.getIType();
		if ( !(subType instanceof ProgramUnit) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation("'" + subType.getFullName() + "' is not a prototype"); } );
		}
		
		ObjectDec superType = (ObjectDec ) t;
		superType.calcInterfaceSuperTypes(compiler.getEnv());
		
		if ( precededByNot ) { 
			if ( superType.isSupertypeOf(subType, compiler.getEnv()) ) {
				if ( errorMessage != null ) {
					return createFunctionList( 
							() -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
				}
				else 
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							"'" + t.getFullName() + "' were NOT a supertype of '"+ subType.getFullName() + "'"); } );
			}
		}
		else {
			if ( ! superType.isSupertypeOf(subType, compiler.getEnv()) ) {
				if ( errorMessage != null ) {
					return createFunctionList( 
							() -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
				}
				else {
					return createFunctionList( () -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 									
							"'" + t.getFullName() + "' were a supertype of '"+ subType.getFullName() + "'"); } );
				}
			}
		}
		return null;
	}
	
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);

		if ( pi != null ) {
			if ( pi.subprototypeList == null ) {
				pi.subprototypeList  = new ArrayList<>();
			}
			String s = exprSubtype.asString();
			if ( map.containsKey(s) ) {
				ProtoInfo other = map.get(s);
				other.isPrototype = true;
				pi.subprototypeList.add(s);
			}
			else {
				pi.subprototypeList.add(exprSubtype.getIType().getFullName());
			}
		}
		
		return null;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
		if ( gpSet.size() == 0 || !(exprSubtype instanceof ast.ExprTypeof) ) {
			((Expr ) this.exprSubtype).calcInternalTypes(env);
		}
	}	
	
}


class ConceptFileNode extends Node {
	
	public String conceptFilename;
	public ArrayList<String> paramNameList;
	public ArrayList<Node> conceptFileNodeList;
	public String conceptPackageName;
	public Symbol firstSymbol;
	
	@Override
	public ArrayList<Function0> check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {
		ArrayList<Function0> functionList = null;
		if ( conceptFileNodeList != null ) {
			for ( Node node : conceptFileNodeList ) {
				ArrayList<Function0> nodeFunctionList = node.check( annotation, compiler);
				if ( nodeFunctionList != null ) {
					if ( functionList == null ) {
						functionList = new ArrayList<>();
					}
					functionList.addAll(nodeFunctionList);
				}
			}
		}
		return functionList;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		
		ArrayList<String>  errorMessageList = null;
		
		if ( conceptFileNodeList != null ) {
			for ( Node node : conceptFileNodeList ) {
				ArrayList<String>  emList = node.collectGPData_dsa(map);
				if ( emList != null ) {
					if ( errorMessageList == null ) {
						errorMessageList = new ArrayList<>();
					}
					errorMessageList.addAll(emList);
				}
			}
		}
		return errorMessageList;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		for ( Node node : conceptFileNodeList ) {
			node.calcInternalTypes(env, gpSet);
		}
	}	

	@Override
	public Tuple2<StringBuffer, Integer> axiomTestCode_dsa(int numberNextMethod) {
		StringBuffer s = null;
		for ( Node node : conceptFileNodeList ) {
			Tuple2<StringBuffer, Integer> t = node.axiomTestCode_dsa(numberNextMethod);
			if ( t != null ) {
				StringBuffer other = t.f1;
				numberNextMethod = t.f2;
				if ( other != null ) {
					if ( s == null ) {
						s = new StringBuffer();
					}
					s.append(other);
				}
			}
		}
		return new Tuple2<StringBuffer, Integer>(s, numberNextMethod);
	}
	
}


class TypeHasNode extends Node {

	public ArrayList<MethodSignature> methodSignatureList;
	public ArrayList<String> errorMessageList;
	
	/**
	 * check if typeOrSymbol has all the method signatures of methodSignatureList
	 */
	@Override
	public ArrayList<Function0>  check(CyanMetaobjectWithAtAnnotation annotation, ICompiler_dsa compiler) {

		ArrayList<Function0> functionList = null;
		
		IType t = typeOrSymbol.getIType();
		if ( !(t instanceof ProgramUnit) ) {
			return createFunctionList( 
					() -> { compiler.errorAtGenericPrototypeInstantiation(
							"A concept associated to generic prototype '" + compiler.getEnv().getCurrentProgramUnit().getFullName() + 
							"' expected that '" + 										
							"'" + t.getFullName() + "' were a prototype"
							); } );
		}
		ProgramUnit pu = (ProgramUnit ) t;
		
		Env env = compiler.getEnv();
		int n = 0;
		for ( MethodSignature ms : this.methodSignatureList ) {
			ms.calcInterfaceTypes(env);
			env.removeAllLocalVariableDec();
			String methodName = ms.getName();
			boolean found = false;
			ArrayList<MethodSignature> msList = pu.searchMethodPublicSuperPublic(methodName, env);
			
			if ( methodName.equals("init") || methodName.equals("init:") ) {
				/*
				 * search is only valid if the method was found in the prototype, not in super-prototypes.
				 */
				// MethodSignature msList
				ArrayList<MethodSignature> msList2 = null;
				if ( msList != null && msList.size() > 0 ) {
					msList2 = new ArrayList<>();
					for ( MethodSignature ms2 : msList ) {
						Object declaringObj =  msList.get(0).getMethod().getDeclaringObject();
						if ( declaringObj != null && declaringObj == pu ) {
							msList2.add(ms2);
						}
					}
				}
				if ( msList2 != null && msList2.size() > 0 ) {
					if (  msList2.size() > 0 ) {
						if ( ms instanceof MethodSignatureUnary )  
							found = true;
						else {
							found = env.searchMethodSignature(ms, msList2) != null;
						}
					}
				}
				else {
					/*
					 * if the prototype does not declare any 'init' or 'init:' method, 
					 * the compiler will declare an 'init' method
					 */
					if ( methodName.equals("init") ) {
						msList = pu.searchMethodPublicSuperPublic("init:", env);
						msList2 = null;
						if ( msList != null && msList.size() > 0 ) {
							msList2 = new ArrayList<>();
							for ( MethodSignature ms2 : msList ) {
								Object declaringObj =  msList.get(0).getMethod().getDeclaringObject();
								if ( declaringObj != null && declaringObj == pu ) {
									msList2.add(ms2);
								}
							}
						}
						if ( msList2 == null || msList2.size() == 0 ) {
							/*
							 * no method 'init:' or 'init' was found. The compiler will create an 'init' method
							 */
							found = true;
						}
					}
				}
			}
			else {
				if ( msList != null && msList.size() > 0 ) {
					if ( ms instanceof MethodSignatureUnary )  
						found = true;
					else {
						found = env.searchMethodSignature(ms, msList) != null;
					}
				}
			}
			
				
			if ( precededByNot ) {
				if ( found ) {
					if ( errorMessageList.get(n) != null ) {
						final String message = errorMessageList.get(n);
						if ( functionList == null ) { functionList = new ArrayList<>(); }
						functionList.add( () -> { compiler.errorAtGenericPrototypeInstantiation(message); } );
					}
					else {
						if ( errorMessage != null ) {
							if ( functionList == null ) { functionList = new ArrayList<>(); }
							functionList.add( () -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
						}
						else 
							if ( functionList == null ) { functionList = new ArrayList<>(); }
							functionList.add( () -> { compiler.errorAtGenericPrototypeInstantiation(
									"A concept associated to generic prototype '" + env.getCurrentProgramUnit().getFullName() + 
									"' expected that method '" + ms.getFullName(env) + "' were in prototype '" 
									+ pu.getFullName() + "'"); } );
					}
				}
			}
			else {
				if ( !found ) {
					if ( errorMessageList.get(n) != null ) {
						final String message = errorMessageList.get(n);
						if ( functionList == null ) { functionList = new ArrayList<>(); }
						functionList.add( () -> { compiler.errorAtGenericPrototypeInstantiation(message); } );
					}
					else {
						found = env.searchMethodSignature(ms, msList) != null;
						if ( errorMessage != null ) {
							if ( functionList == null ) { functionList = new ArrayList<>(); }
							functionList.add( () -> { compiler.errorAtGenericPrototypeInstantiation(errorMessage); } );
						}
						else {
							if ( functionList == null ) { functionList = new ArrayList<>(); }
							functionList.add( () -> { compiler.errorAtGenericPrototypeInstantiation(
									"A concept associated to generic prototype '" + env.getCurrentProgramUnit().getFullName() + 
									"' expected that method '" + ms.getFullName(env) + "' were NOT in prototype '" 
									+ pu.getFullName() + "'"); } );
						}
					}
				}
			}
			++n;
			
		}
		return functionList;
	}
	
	@Override
	public ArrayList<String> collectGPData_dsa(HashMap<String, ProtoInfo> map) {
		ProtoInfo pi = map.get(name);
		ArrayList<String> emList = null;
		
		if ( pi != null ) {
			if ( pi.methodSignatureList == null ) {
				pi.methodSignatureList = new ArrayList<>();
			}
			for ( MethodSignature ms : this.methodSignatureList ) {
				String sigName = ms.getName();
				boolean error = false;
				for ( MethodSignature ms2 : pi.methodSignatureList ) {
					if ( sigName.equals(ms2.getName()) ) {
						if ( emList == null ) {
							emList = new ArrayList<String>();
						}
						error = true;
						emList.add("Duplicated method signature: '" + sigName + "'. Note that currently this metaobject does not support"
								+ " method overloading");
					}
				}
				if ( ! error ) {
					pi.methodSignatureList.add(ms);
				}
			}
		}
		return emList;
	}

	@Override
	public void calcInternalTypes(Env env, HashSet<String> gpSet) {
		super.calcInternalTypes(env, gpSet);
		for ( MethodSignature ms : this.methodSignatureList ) {
			String strMS = ms.asString();
			  /*
			   * using string strMS here is a very primitive way of discovering if the method signature
			   * uses 'typeof'. This hopefully will be changed some day
			   */
			if ( ! CyanMetaobjectConcept.hasTypeof(strMS) ) {
				ms.calcInterfaceTypes(env);
			}
		}
	}	
	
}



public class CyanMetaobjectConcept extends CyanMetaobjectWithAt 
       implements IParseWithCyanCompiler_dpa, ICheckProgramUnit_before_dsa, IActionGenericProgramUnit_dpa, IListAfter_ati {

	public CyanMetaobjectConcept() {
		super(MetaobjectArgumentKind.ZeroOrMoreParameters);
	}

	@Override
	public String getName() {
		return "concept";
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		ArrayList<Object> paramList = this.getMetaobjectAnnotation().getJavaParameterList();
		if ( paramList != null && paramList.size() != 0 ) {
			if ( paramList.size() != 1 || !(paramList.get(0) instanceof String) ) {
				return this.addError("This metaobject can take one parameter, which should be \"test\", with or without the quotes");
			}
			else {
				String p = NameServer.removeQuotes((String ) paramList.get(0));
				if ( !p.equals("test") ) {
					return this.addError("This metaobject can take one parameter, which should be \"test\", with or without the quotes");
				}
			}
		}
		return null;
	}	
	
	@Override
	public boolean shouldTakeText() { return true; }

	
	public void loadConceptFile(ICompiler_dpa comp, ICompiler_dpa originalCompiler_dpa, ConceptFileNode conceptNode, String conceptPackageName) {
		

		Tuple5<FileError, char[], String, String, CyanPackage> t = originalCompiler_dpa.readTextFileFromPackage(
				conceptNode.conceptFilename, "concept", conceptNode.conceptPackageName, DirectoryPackage.DATA, 
				conceptNode.paramNameList.size(), conceptNode.paramNameList);
		
		/*
		Tuple5<FileError, char[], String, String, CyanPackage> t = 
				comp.loadTextDSLFileFromPackage(conceptNode.conceptFilename, conceptNode.conceptPackageName, conceptNode.paramNameList.size());
		*/
		if ( t != null && t.f1 == FileError.package_not_found ) {
			comp.error( conceptNode.firstSymbol,  "Cannot find package '" + conceptNode.conceptPackageName + "'");
			return ;
		}
		if ( t == null  || t.f1 != FileError.ok_e ) {
			comp.error( conceptNode.firstSymbol,  "Cannot read concept from file '" + conceptNode.conceptFilename + ".concept" +
		        "' from package '" + conceptNode.conceptPackageName + "'. This file should be in directory '" + DirectoryPackage.DATA +
		        "' of the package directory"
		         );
			return ;
		}
		
		/*
		String filename = t.f3;
		int indexLeftPar = filename.indexOf('(');
		if ( indexLeftPar < 0 ) {
			comp.error(conceptNode.firstSymbol,  "Error in the concept node file '" + t.f4 + t.f3 + 
					"' : it does not take a parameter. It should take at least one");
			return ;
		}
		Tuple2<ArrayList<String>, String> messageFormalParamList = getFormalParamList(filename, indexLeftPar + 1); 
		if ( messageFormalParamList.f2 != null ) {
			comp.error(conceptNode.firstSymbol,  "Error in the concept node file '" + t.f4 + t.f3 + 
					"' : " + messageFormalParamList.f2 + ". Use something like 'comparison(T).concept' or 'comparable(R,S).concept'");
			return ;
		}
		ArrayList<String> formalParamList = messageFormalParamList.f1;
		if ( conceptNode.paramNameList.size() != formalParamList.size() ) {
			comp.error(conceptNode.firstSymbol,  "The concept file '" + t.f4 + t.f3+ 
					"' takes a number of parameters different from what was supplied");
			return ;
		}
		Hashtable<String, String> formalRealTable = new Hashtable<>();
		int k = 0;
		for ( String formalParam : formalParamList ) {
			if ( formalRealTable.put(formalParam, conceptNode.paramNameList.get(k)) != null ) {
				comp.error(conceptNode.firstSymbol,  "Error in the concept node file '" + t.f4 + t.f3 + 
						"' : its parameter list has two parameters with the same name");
			}
			++k;
		}
		char []text = CompilerManager.replaceOnly(t.f2, formalRealTable, ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE);
		*/
		char []text = t.f2;
		ICompiler_dpa icomp = CompilerManager.getCompilerToDSL(text, 
				t.f3, t.f4, t.f5);
		
		conceptNode.conceptFileNodeList = null;
		try {
			conceptNode.conceptFileNodeList = this.parseConceptSourceCode(icomp, originalCompiler_dpa, conceptPackageName);
			((Compiler_dpa ) comp).getExprStatList().addAll( ((Compiler_dpa ) icomp).getExprStatList() );
		}
		catch ( CompileErrorException e ) {
			/*
			 * convert exception from icomp to exception to comp
			 
			CompilationUnitSuper cus = ((Compiler_dpa ) icomp).getCompiler().getCompilationUnitSuper();
			ArrayList<UnitError> errorList = cus.getErrorList();
			if ( errorList.size() > 0 ) {
				UnitError ue = errorList.get(errorList.size()-1);
				comp.error(ue.getSymbol(),  ue.getMessage());
			}
			else {
				comp.error(conceptNode.firstSymbol, "Internal error in metaobject '" + this.getName() + "'");
			}
			*/
		}
		/*
		 * all expressions found by icomp should be added to comp. Then the Cyan compiler can type them correctly.
		 */
		
	}
	
	/**
	 * This metaobject is used in a generic prototype P. If P is a non-instantiated generic prototype (file name, for
	 * example, P(1).cyan), returns false. If P is an instantiated generic prototype (file name, for example,
	 * P(Int).cyan), returns true;  
	   @param compiler_dpa
	   @return
	 */
	private static boolean isInstantiatedGenericPrototype(ICompilerGenericProgramUnit_dpa compiler_dpa) {
		String filename = ((CompilerGenericProgramUnit_dpa) compiler_dpa).getCompilationUnit().getFilename();
		int index = filename.indexOf('(');
		return ! ( ( index >= 0 && index < (filename.length() - 1) && Character.isDigit(filename.charAt(index+1)) ) );
		
	}

	@Override
	public void dpa_parse(ICompiler_dpa compiler_dpa) {
		
		compiler_dpa.next();
		
		ArrayList<Node> nodeList = parseConceptSourceCode(compiler_dpa, compiler_dpa, compiler_dpa.getCompilationUnit().getCyanPackage().getPackageName());
		
		
		this.metaobjectAnnotation.setInfo_dpa(nodeList);

		
		return ;

	}
	
	public static final String conceptFileExtension = "concept";
	public static final String dotConceptFileExtension = ".concept";
	
	public ArrayList<Node> parseConceptSourceCode(ICompiler_dpa compiler_dpa, ICompiler_dpa originalCompiler_dpa, String currentPackageName) {
		/*
            object G<R, S, T>

                @concept{*
                    R in [ Int, Short, Byte, Char, Long ],  "The first parameter, 'R', should be Int, Short, Byte, Char, or Long",
                    S implements Openable,  "The second parameter, 'S', should implement interface Openable",
                    T subtype Program,      "The third parameter, 'T', should be subtype of Program"
                    T has [
                          func format: String, R
                          func with: String do: Function<S, Nil> -> R
                       ]
                    comparison(T)
                *}
			    ...
			end
		 * 
		 */
		ArrayList<Node> nodeList = new ArrayList<>();
		
		Symbol sym;
		while ( compiler_dpa.startType(compiler_dpa.getSymbol().token) || compiler_dpa.getSymbol().token == Token.NOT ) {
			boolean precededByNot = compiler_dpa.getSymbol().token == Token.NOT;
			if ( precededByNot ) {
				compiler_dpa.next();
			}
			
			IExpr typeOrSymbol = compiler_dpa.type();
			
			String gpName = typeOrSymbol.asString();
			boolean isIdentifier = Lexer.isIdentifier(gpName);
			
			
			sym = compiler_dpa.getSymbol();
			String symStr = compiler_dpa.getSymbol().getSymbolString();
			if ( symStr.equals("interface") ) {
				IsInterfaceNode node = new IsInterfaceNode();
				node.typeOrSymbol = typeOrSymbol;
				nodeList.add(node);
				compiler_dpa.next();
			}
			else if ( symStr.equals("prototype") ) {
				IsPrototypeNode node = new IsPrototypeNode();
				node.typeOrSymbol = typeOrSymbol;
				nodeList.add(node);
				compiler_dpa.next();
			}
			else if ( symStr.equals("symbol") ) {
				IsSymbolNode node = new IsSymbolNode();
				node.typeOrSymbol = typeOrSymbol;
				nodeList.add(node);
				compiler_dpa.next();
			}
			else if ( sym.getSymbolString().equals("is") ) {
				compiler_dpa.next();
				IExpr isTypeExpr = compiler_dpa.type();
				IsNode node = new IsNode();
				nodeList.add(node);
				node.typeOrSymbol = typeOrSymbol;
				node.isTypeExpr = isTypeExpr;
				
			} 
			else if ( typeOrSymbol.asString().equals("axiom") ) {
				if ( precededByNot ) {
					compiler_dpa.error(typeOrSymbol.getFirstSymbol(), "An axiom cannot be preceded by '!'");
				}
				compiler_dpa.removeLastExprStat();
				
				
				/*
                   axiom equalityTest: T a, T b, T c {%
                           if a == a && a != a && ! (a == a) || !(b == b) || !(c == c) || (a == b && a != b) ||
                              !(a == b && b == c && a != c) {
                               ^"method '==' or '!=' of T do not satisfy the axioms for equality and non-equality";
                           }
                       	if ( a == b && (a != b) ) || (a != a) || (b != b) || (a == b && b != a) ||
                       	   (b != a && b == a) {
                       	    ^" T do not obey the rules for equality"
                       	}

                           ^Nil
                   %}
					
				 * 
				 */
				if ( compiler_dpa.getSymbol().token != Token.IDENTCOLON ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "A selector name such as 'equalityTest:' was expected");
				}
				AxiomNode axiom = new AxiomNode();
				axiom.methodName = compiler_dpa.getSymbol().getSymbolString();
				compiler_dpa.next();
				if ( compiler_dpa.getSymbol().token != Token.IDENT &&
						! compiler_dpa.isBasicType(compiler_dpa.getSymbol().token)
						) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "An identifier or type was expected. It should be a formal parameter to this generic prototype");
				}
				axiom.paramList = new ArrayList<>();
				while ( compiler_dpa.getSymbol().token == Token.IDENT || 
						compiler_dpa.isBasicType(compiler_dpa.getSymbol().token)
						) {
					AxiomParameter p = new AxiomParameter();
					Expr ident = compiler_dpa.parseIdent();
					p.strType = ident.asString();
					if ( compiler_dpa.getSymbol().token != Token.IDENT && 
							! compiler_dpa.isBasicType(compiler_dpa.getSymbol().token)
							) {
						compiler_dpa.error(compiler_dpa.getSymbol(), "An identifier was expected. It should be a parameter to this axiom");
					}
					p.name = compiler_dpa.getSymbol().getSymbolString();
					axiom.paramList.add(p);
					compiler_dpa.next();
					if ( compiler_dpa.getSymbol().token == Token.COMMA ) {
						compiler_dpa.next();
						if ( compiler_dpa.getSymbol().token != Token.IDENT && 
								! compiler_dpa.isBasicType(compiler_dpa.getSymbol().token)
								) {
							compiler_dpa.error(compiler_dpa.getSymbol(), 
									"An identifier was expected. It should be a formal parameter to this generic prototype");
						}
					}
					else {
						break;
					}
				}
				
				if ( compiler_dpa.getSymbol().token != Token.LEFTCHAR_SEQUENCE ) {
					compiler_dpa.error(typeOrSymbol.getFirstSymbol(), "A left char sequence such as '{+' or '{%' was expected");
				}
				Symbol leftSymbol = compiler_dpa.getSymbol();
				String leftSeq = compiler_dpa.getSymbol().getSymbolString();
				compiler_dpa.next();
				String rightSeq = Lexer.rightSymbolSeqFromLeftSymbolSeq(leftSeq);
				compiler_dpa.pushRightSymbolSeq(rightSeq);
				
				while ( compiler_dpa.getSymbol().token != Token.EOLO && compiler_dpa.getSymbol().token != Token.RIGHTCHAR_SEQUENCE ) {
					compiler_dpa.next();
				}
				if ( ! compiler_dpa.getSymbol().getSymbolString().equals(rightSeq) ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "The right char sequence '" + rightSeq + "' was expected");
				}
				axiom.funcText = compiler_dpa.getText( leftSymbol.getOffset() + leftSeq.length(), compiler_dpa.getSymbol().getOffset());
				compiler_dpa.next();
				nodeList.add(axiom);
				/*
				
				// ******
				if ( ! compiler_dpa.getSymbol().getSymbolString().equals("func") ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "'func' was expected");
				}

				axiom = new AxiomNode();
				
				compiler_dpa.next();
				axiom.methodSignature = this.methodSignature(compiler_dpa);
				if ( compiler_dpa.getSymbol().token != Token.LEFTCB ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "'{' was expected");
				}
				
				while ( compiler_dpa.getSymbol().token != Token.EOLO && compiler_dpa.getSymbol().token != Token.RIGHTCHAR_SEQUENCE ) {
					compiler_dpa.next();
				}
				if ( ! compiler_dpa.getSymbol().getSymbolString().equals(rightSeq) ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "The right char sequence '" + rightSeq + "' was expected");
				}
				axiom.funcText = compiler_dpa.getText( leftSymbol.getOffset() + leftSeq.length(), compiler_dpa.getSymbol().getOffset());
				compiler_dpa.next();
				nodeList.add(axiom);
				*/
			}
			else if ( sym.token == Token.LEFTPAR ) {
				// found a link to a file with further demands --- a concept file

				if ( precededByNot ) {
					compiler_dpa.error(typeOrSymbol.getFirstSymbol(), "A concept file name cannot be preceded by '!'");
				}
				
				compiler_dpa.removeLastExprStat();
				
				
				ConceptFileNode conceptNode = new ConceptFileNode();
				
				conceptNode.firstSymbol = typeOrSymbol.getFirstSymbol();
				String fn = typeOrSymbol.asString();
				int indexdot = fn.lastIndexOf('.');
				if ( indexdot < 0 ) {
					// no '.' in the file name, use package of the metaobject
					conceptNode.conceptFilename = fn;
					conceptNode.conceptPackageName = currentPackageName; // compiler_dpa.getCompilationUnit().getCyanPackage().getPackageName();
				}
				else {
					conceptNode.conceptFilename = fn.substring(indexdot + 1);
					conceptNode.conceptPackageName = fn.substring(0, indexdot);
					if ( indexdot + 1 > fn.length() ) {
						compiler_dpa.error(typeOrSymbol.getFirstSymbol(), "A concept file name was expected");
						return null;
					}
				}
				
				
				compiler_dpa.next();
				ArrayList<String> paramNameList = new ArrayList<>();
				while ( compiler_dpa.getSymbol().token != Token.RIGHTPAR ) {
					String id = compiler_dpa.getSymbol().getSymbolString();
					compiler_dpa.next();
					while ( compiler_dpa.getSymbol().token == Token.PERIOD ) {
						compiler_dpa.next();
						id += ".";
						if ( compiler_dpa.getSymbol().token != Token.IDENT ) {
							compiler_dpa.error(typeOrSymbol.getFirstSymbol(), "An identifier was expected after '.'");
							return null;
						}
						id += compiler_dpa.getSymbol().getSymbolString();
						compiler_dpa.next();
					}
					paramNameList.add( id );
					if ( compiler_dpa.getSymbol().token == Token.COMMA ) {
						compiler_dpa.next();
					}
					else {
						break;
					}
				}
				if ( compiler_dpa.getSymbol().token != Token.RIGHTPAR ) {
					compiler_dpa.error(typeOrSymbol.getFirstSymbol(), "')' expected");
					return null;
				}
				compiler_dpa.next();
				conceptNode.paramNameList = paramNameList;
				loadConceptFile(compiler_dpa, originalCompiler_dpa, conceptNode, conceptNode.conceptPackageName);
				/* if ( isInstantiatedGenericPrototype(compiler_dpa) ) {
				} */
				nodeList.add(conceptNode);
			}
			else if ( sym.token == Token.IMPLEMENTS ) {
				compiler_dpa.next();
				IExpr exprInterface = compiler_dpa.type();
				ImplementsNode implNode = new ImplementsNode();
				implNode.typeOrSymbol = typeOrSymbol;
				implNode.exprInterface = exprInterface;
				nodeList.add(implNode);
			}
			else if ( sym.token == Token.IN ) {
				String typeName = typeOrSymbol.ifPrototypeReturnsItsName();
				boolean isSymbol = false;
				isSymbol = Lexer.isSymbol(typeName) && !typeName.startsWith(Token.TYPEOF.toString() + "(") ;
				if ( isSymbol ) {
					compiler_dpa.removeLastExprStat();
					compiler_dpa.next();
					ArrayList<ExprIdentStar> symbolList = new ArrayList<>();
					
					if ( compiler_dpa.getSymbol().token != Token.LEFTSB ) {
						compiler_dpa.error(compiler_dpa.getSymbol(), "'[' was expected");
						return null;
					}
					compiler_dpa.next();
					
					if ( compiler_dpa.getSymbol().token != Token.IDENT ) {
						compiler_dpa.error(compiler_dpa.getSymbol(), "An identifier was expected");
						return null;
					}
					
					while ( compiler_dpa.getSymbol().token == Token.IDENT) {
						
						sym = compiler_dpa.getSymbol();
						ExprIdentStar id = compiler_dpa.parseSingleIdent();
						if ( id == null ) {
							compiler_dpa.error(sym, "An identifier was expected after ','");
							return null;
						}
						symbolList.add(id);
						
						if ( compiler_dpa.getSymbol().token == Token.COMMA ) {
							compiler_dpa.next();
							if ( compiler_dpa.getSymbol().token != Token.IDENT ) {
								compiler_dpa.error(compiler_dpa.getSymbol(), "An identifier was expected after ','");
								return null;
							}
						}
						else {
							if ( compiler_dpa.getSymbol().token != Token.RIGHTSB ) {
								compiler_dpa.error(compiler_dpa.getSymbol(), "']' was expected. Found '" + compiler_dpa.getSymbol().getSymbolString() + "'");
								return null;
							}
							else {
								compiler_dpa.next();
							}
							break;
						}
					}
					
					SymbolInNode inNode = new SymbolInNode();
					inNode.typeOrSymbol = typeOrSymbol;
					inNode.symbolList = symbolList;
					inNode.isSymbol = true;
					nodeList.add(inNode);
				}
				else {
					compiler_dpa.next();
					ArrayList<IExpr> typeList = new ArrayList<>();
					
					if ( compiler_dpa.getSymbol().token != Token.LEFTSB ) {
						compiler_dpa.error(compiler_dpa.getSymbol(), "'[' was expected");
						return null;
					}
					compiler_dpa.next();
					
					if ( ! compiler_dpa.startType(compiler_dpa.getSymbol().token) ) {
						compiler_dpa.error(compiler_dpa.getSymbol(), "A type was expected");
						return null;
					}
					
					while ( compiler_dpa.startType(compiler_dpa.getSymbol().token) ) {
						
						IExpr exprType = compiler_dpa.type(); 
						typeList.add(exprType);
						
						if ( compiler_dpa.getSymbol().token == Token.COMMA ) {
							compiler_dpa.next();
							if ( ! compiler_dpa.startType(compiler_dpa.getSymbol().token)  ) {
								compiler_dpa.error(compiler_dpa.getSymbol(), "A type was expected after ','");
								return null;
							}
						}
						else {
							if ( compiler_dpa.getSymbol().token != Token.RIGHTSB ) {
								compiler_dpa.error(compiler_dpa.getSymbol(), "']' was expected. Found '" + compiler_dpa.getSymbol().getSymbolString() + "'");
								return null;
							}
							else {
								compiler_dpa.next();
							}
							break;
						}
					}
					TypeInNode inNode = new TypeInNode();
					inNode.typeOrSymbol = typeOrSymbol;
					inNode.typeList = typeList;
					inNode.isSymbol = false;
					nodeList.add(inNode);
					
				}
			}
			else if ( sym.getSymbolString().equals("subprototype") ) {
				compiler_dpa.next();
				IExpr subtype = compiler_dpa.type();
				SubprototypeNode subtypeNode = new SubprototypeNode();
				subtypeNode.typeOrSymbol = typeOrSymbol;
				subtypeNode.exprSuperprototype = subtype;
				nodeList.add(subtypeNode);
			}
			else if ( sym.getSymbolString().equals("superprototype") ) {
				compiler_dpa.next();
				IExpr supertype = compiler_dpa.type();
				SupertypeNode supertypeNode = new SupertypeNode();
				supertypeNode.typeOrSymbol = typeOrSymbol;
				supertypeNode.exprSubtype = supertype;
				nodeList.add(supertypeNode);
			}
			else if ( sym.getSymbolString().equals("has") ) {

				compiler_dpa.next();
				
				if ( compiler_dpa.getSymbol().token != Token.LEFTSB ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "'[' was expected");
					return null;
				}
				compiler_dpa.next();
				
				ArrayList<MethodSignature> methodSignatureList = new ArrayList<>();
				ArrayList<String> errorMessageList = new ArrayList<>();
				while ( compiler_dpa.getSymbol().getSymbolString().equals("func") ) {
					
					compiler_dpa.next();
					MethodSignature ms = this.methodSignature(compiler_dpa);
					methodSignatureList.add(ms);

					if ( compiler_dpa.getSymbol().token == Token.LITERALSTRING ) {
						errorMessageList.add(NameServer.removeQuotes(compiler_dpa.getSymbol().getSymbolString()) ); // NameServer.removeQuotes();
						compiler_dpa.next();
						if ( compiler_dpa.getSymbol().token == Token.COMMA ) {
							compiler_dpa.next();
						}
					}
					else {
						errorMessageList.add(null);
					}
				
				}
				if ( compiler_dpa.getSymbol().token != Token.RIGHTSB ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "']' was expected. Found '" + compiler_dpa.getSymbol().getSymbolString() + "'");
					return null;
				}
				else {
					compiler_dpa.next();
				}
				TypeHasNode inNode = new TypeHasNode();
				inNode.typeOrSymbol = typeOrSymbol;
				inNode.methodSignatureList = methodSignatureList;
				inNode.errorMessageList = errorMessageList;
				nodeList.add(inNode);
			}
			else {
				compiler_dpa.error(compiler_dpa.getSymbol(), "A concept restriction was expected. Found '" + 
			             compiler_dpa.getSymbol().getSymbolString() + "'");
			}
			nodeList.get(nodeList.size()-1).precededByNot = precededByNot;
			nodeList.get(nodeList.size()-1).isIdentifier = isIdentifier;
			nodeList.get(nodeList.size()-1).name = gpName;
			if ( compiler_dpa.getSymbol().token != Token.COMMA ) {
				if ( compiler_dpa.getSymbol().token != Token.EOLO ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "',' expected");
				}
				else
					break;
			}
			compiler_dpa.next();
			
			if ( compiler_dpa.getSymbol().token == Token.LITERALSTRING ) {
				nodeList.get(nodeList.size()-1).errorMessage = NameServer.removeQuotes(compiler_dpa.getSymbol().getSymbolString()); // NameServer.removeQuotes();
				compiler_dpa.next();
				if ( compiler_dpa.getSymbol().token != Token.COMMA ) {
					if ( compiler_dpa.getSymbol().token != Token.EOLO ) {
						compiler_dpa.error(compiler_dpa.getSymbol(), "',' expected");
					}
					else
						break;
				}
				compiler_dpa.next();
			}
			if ( !(compiler_dpa.startType(compiler_dpa.getSymbol().token) || 
					compiler_dpa.getSymbol().token == Token.NOT) ) {
				compiler_dpa.error(compiler_dpa.getSymbol(), "after a ',' it was expected a type or the end of the source code");
			}
		}
		if ( compiler_dpa.getSymbol().token != Token.EOLO ) {
			compiler_dpa.error(compiler_dpa.getSymbol(), "Unexpected symbol: '" + compiler_dpa.getSymbol().getSymbolString() + "'");
		}
		return nodeList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void before_dsa_checkProgramUnit(ICompiler_dsa compiler){
		ArrayList<Node> nodeList = (ArrayList<Node> ) this.metaobjectAnnotation.getInfo_dpa();
		
		for ( Node node : nodeList ) {
			node.calcInternalTypes(compiler.getEnv(), new HashSet<String>() );
		}
		
		
		ArrayList<Function0> functionList;
		for ( Node node : nodeList ) {
			functionList = node.check( (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation, compiler);
			if ( functionList != null ) {
				for ( Function0 f : functionList ) {
					f.eval();
				}
			}
		}
	}
	
	
	
	private void createTestCasesForGenericParameters(ArrayList<Node> nodeList, ICompiler_ati compiler, String dirName) {
		
		HashMap<String, ProtoInfo> map = new HashMap<>();
		
		Declaration dec = this.getMetaobjectAnnotation().getDeclaration();
		ProgramUnit pu = (ProgramUnit ) dec;
		ArrayList<ArrayList<GenericParameter>> gpListList = pu.getGenericParameterListList();
		//Hashtable<String, String> mapFormalToRealParameter = new Hashtable<>();
		for ( ArrayList<GenericParameter> gpList : gpListList ) {
			for ( GenericParameter gp : gpList ) {
				map.put(gp.getName(), new ProtoInfo(gp.getName()));
				
			}
		}
		
		/*
		 * 	public static char []replaceOnly(char []text,  Hashtable<String, String> formalRealTable, String currentPrototypeName,
			ReplacementPolicyInGenericInstantiation replacementPolicy) {

		 */
		
		ArrayList<String> errorMessageList = collectGenericParameterData(map,  nodeList);
		if ( errorMessageList.size() > 0 ) {
			for ( String errorMessage : errorMessageList ) {
				compiler.error(this.metaobjectAnnotation.getFirstSymbol(), errorMessage);
			}
		}
		else {
			String testPackageName = compiler.getPackageNameTest() + "." + dirName;
			ArrayList<ProtoInfo> piList = this.topologicalSortingProtoInfoList(map, (Env ) compiler.getEnv());
 			for ( ProtoInfo info : piList )  {
				// ProtoInfo info = map.get(param);
				if ( info.isSymbol ) 
					continue;
				if ( ! checkPrototypeInfo(info, compiler, map) )
					continue;
				StringBuffer code = createTestCase(compiler, info, map, testPackageName);
				if ( code != null ) {
					compiler.writeTestFileTo(code, info.name + "." + NameServer.cyanSourceFileExtension, dirName);
				}
			}
 			/*
 			StringBuffer allAxiomMethods = null;
 			for ( Node node : nodeList ) {
 				StringBuffer other = node.axiomTestCode_dsa();
 				if ( other != null ) {
 					if ( allAxiomMethods == null ) {
 						allAxiomMethods = new StringBuffer();
 						allAxiomMethods.append("package " + testPackageName + "\n\n");
 						allAxiomMethods.append("object AxiomTest\n");
 					}
 					allAxiomMethods.append(other);
 				}
 			}
 			if ( allAxiomMethods != null && allAxiomMethods.length() > 1 ) {
				allAxiomMethods.append("\nend\n");
 				compiler.writeTestFileTo(allAxiomMethods, "AxiomTest." + NameServer.cyanSourceFileExtension, dirName);
 			}
				
 			*/
		}
		
	}
	
	private boolean checkPrototypeInfo(ProtoInfo info, ICompiler_ati compiler, HashMap<String, ProtoInfo> map) {
		/*
    	public String name;
    	public boolean isPrototype = false;
    	public boolean isInterface = false;
    	public boolean isSymbol = false;
    	public ArrayList<String> superprototypeList = null;
    	public ArrayList<String> subprototypeList = null;
    	public ArrayList<String> interfaceImplementList = null;
    	public ArrayList<MethodSignature> methodSignatureList = null;
    	public ArrayList<String> inList = null;
    	public ArrayList<String> axiomList = null;
	 * 
	 */
		Env env = (Env ) compiler.getEnv();
		int isNumber = info.isPrototype ? 1 : 0;
		isNumber += info.isInterface ? 1 : 0;
		isNumber += info.isSymbol ? 1 : 0;
		if ( isNumber > 1 ) {
			compiler.error(this.metaobjectAnnotation.getFirstSymbol(),
					"'" + info.name + "' is considered by this concept two incompatible things in the following list: prototype, interface, and symbol");
		}
		info.allMethodSignatures = new HashSet<>();
		
		if ( info.isPrototype && info.superprototypeList != null && info.superprototypeList.size() > 1 ) {
			// interfaces can have more than one super-prototype
			String supername = info.superprototypeList.get(0);
			for (int i = 1; i < info.superprototypeList.size(); ++i) {
				if ( ! supername.equals(info.superprototypeList.get(i)) ) {
					compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "This concept "
							+ "demands that the generic parameter '" + info.name + "' have two superprototypes: '"
							+ supername + "' and '" + info.superprototypeList.get(i) + "'");
					break;
				}
			}
		}
		info.overrideMethodSignatureList = new ArrayList<>();
		if ( info.superprototypeList != null ) {
			for ( String supertypeName : info.superprototypeList ) {  // just one for ObjectDec. It may be several for InterfaceDe
				ProtoInfo supertypeInfo = map.get(supertypeName);
				if ( supertypeInfo != null ) {
					
					/* supertypeName is a generic parameter
					 * due to the topological ordering, supertypeName has already been processed by this method
					 */
					if ( info.methodSignatureList != null ) {
						ArrayList<MethodSignature> toBeRemovedList = new ArrayList<>();
						for ( MethodSignature ms : info.methodSignatureList ) {
							String fullName_ms = ms.getFullName(env);
							if ( supertypeInfo.allFullNameMethodSignatures.contains(fullName_ms) ) {
								info.overrideMethodSignatureList.add(ms);
								toBeRemovedList.add(ms);
							}
						}
						for ( MethodSignature ms : toBeRemovedList ) {
							info.methodSignatureList.remove(ms);
						}
						
					}
					
					info.allMethodSignatures.addAll(supertypeInfo.allMethodSignatures);
				}
				else {

					/*
					 * supertypeName is not a generic parameter
					 */
					ProgramUnit pu = env.searchPackagePrototype(supertypeName, this.metaobjectAnnotation.getFirstSymbol());
					if ( pu == null ) {
						compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "'" + supertypeName + "', used in the concept of this "
								+ "generic prototype, was not found");
						return false;
					}
					/*
					if ( !(pu instanceof ObjectDec) ) {
						compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "'" + supertypeName + "', used in the concept of this "
								+ "generic prototype, was expected to be a prototype (not an interface)");
					}
					*/
					if ( pu instanceof ObjectDec ) {
						ObjectDec supertype = (ObjectDec ) pu;
						if ( supertype.getIsAbstract() ) {
							/**
							 * collect the signatures of all abstract methods
							 */
							ObjectDec current = supertype;
							while ( current != null ) {
								if ( current.getIsAbstract() ) {
									for ( MethodDec method : current.getMethodDecList() ) {
										if ( method.isAbstract() ) {
											info.overrideMethodSignatureList.add(method.getMethodSignature());
										}
									}
								}
								current = current.getSuperobject();
							}
						}
						for ( MethodDec method : supertype.getMethodDecList() ) {
							info.allMethodSignatures.add(method.getMethodSignature());
						}

						/* get all method signatures */
						for ( ProgramUnit superPU : supertype.getAllSuperPrototypes() ) {
							ObjectDec superObj = (ObjectDec ) superPU;
							/*
							 * init and init: methods do not need to be preceded by 'override'
							 */
							for ( MethodDec method : superObj.getMethodDecList() ) {
								info.allMethodSignatures.add(method.getMethodSignature());
							}
						}
					}
					else if ( pu instanceof InterfaceDec ) {
						// an interface may have several super-prototypes
						InterfaceDec superInter = (InterfaceDec ) pu;
						for ( MethodSignature ms : superInter.getMethodSignatureList() ) {
							info.allMethodSignatures.add(ms);
						}
						for ( ProgramUnit superPU : superInter.getAllSuperPrototypes() ) {
							InterfaceDec superSuper = (InterfaceDec ) superPU;
							for ( MethodSignature ms : superSuper.getMethodSignatureList() ) {
								info.allMethodSignatures.add(ms);
							}
						}
					}
				}
			}
		}
		if ( info.interfaceImplementList != null && info.interfaceImplementList.size() > 1 ) {
			// delete interfaces that appear two times
			HashSet<String> interfaceSet = new HashSet<>();
			for ( String intername : info.interfaceImplementList ) {
				interfaceSet.add(intername);
			}
			if ( interfaceSet.size() != info.interfaceImplementList.size() ) {
				info.interfaceImplementList.clear();
				info.interfaceImplementList.addAll(interfaceSet);
			}
		}
		
		/*
		collect all super interfaces first because interfaces may repeat:
			 A implements I1
			 A implements I2
			 
	    and I2 implements I1
	    
	    // In overrideMethodSignatureList, put 
	    
	    */
		if ( info.interfaceImplementList != null ) {
			for ( String interfaceName : info.interfaceImplementList ) {
				ProtoInfo interInfo = map.get(interfaceName);
				if ( interInfo != null ) {
					/*
					 * interfaceName is a generic parameter already processed by this method
					 */
					info.allMethodSignatures.addAll(interInfo.allMethodSignatures);
					info.overrideMethodSignatureList.addAll(interInfo.allMethodSignatures);
				}
				else {
					ProgramUnit pu = env.searchPackagePrototype(interfaceName, this.metaobjectAnnotation.getFirstSymbol());
					if ( pu == null ) {
						compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "'" + interfaceName + "', used in the concept of this "
								+ "generic prototype, was not found");
						return false;
					}
					if ( !(pu instanceof InterfaceDec) ) {
						compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "'" + interfaceName + "', used in the concept of this "
								+ "generic prototype, was expected to be a prototype (not an interface)");
					}
					InterfaceDec inter = (InterfaceDec ) pu;
					/*
					 * collect the signatures of the interface
					 */
					for ( MethodSignature ms : inter.getMethodSignatureList() ) {
						info.overrideMethodSignatureList.add(ms);
						info.allMethodSignatures.add(ms);
					}
					/*
					 * collect the signatures of the super-interfaces 
					 */
					for ( ProgramUnit superPU : inter.getAllSuperPrototypes() ) {
						InterfaceDec superInter = (InterfaceDec ) superPU;
						for ( MethodSignature ms : superInter.getMethodSignatureList() ) {
							info.overrideMethodSignatureList.add(ms);
							info.allMethodSignatures.add(ms);
						}
						
					}
				}
			}
		}
		
		HashSet<String> overrideMethodSignatureSet = new HashSet<>();
		if ( info.overrideMethodSignatureList.size() > 0 ) {
			// delete signatures that appear two times
			ArrayList<MethodSignature> msList = new ArrayList<>();
			for ( MethodSignature ms : info.overrideMethodSignatureList ) {
				String name = ms.getFullName(env);
				if ( overrideMethodSignatureSet.add(name) ) {
					msList.add(ms);
				}
			}
			info.overrideMethodSignatureList = msList;
		}
		info.allFullNameMethodSignatures = new HashSet<>();
		for ( MethodSignature ms : info.allMethodSignatures ) {
			info.allFullNameMethodSignatures.add(ms.getFullName(env));
		}
		
		if ( info.methodSignatureList != null && info.methodSignatureList.size() > 0 ) {
			/*
			 * this list has signatures specified through 'has' clauses such as 
			 *    A has [ func run, func search: String -> String ]
			 */
			// delete signatures that appear two times
			// HashSet<String> msNameSet = new HashSet<>();
			ArrayList<MethodSignature> msList = new ArrayList<>();
			
			/*
			 * put in msList all method signatures of the 'has' list that do not appear in
			 * super-prototypes and implemented interfaces. Put in overrideMethodSignatureList
			 * all method signatures of the 'has' list that appear in super-prototypes
			 * and implemented interfaces.
			 */
			for ( MethodSignature ms : info.methodSignatureList ) {
				String fullName = ms.getFullName(env);
				if ( ! info.allFullNameMethodSignatures.contains(fullName) ) {
					msList.add(ms);
					info.allMethodSignatures.add(ms);
					info.allFullNameMethodSignatures.add(fullName);
				}
				else {
					info.overrideMethodSignatureList.add(ms);
				}
			}
			
			/*
			for ( String supertypeName : info.superprototypeList ) {
				
				ProtoInfo superInfo = map.get(supertypeName);
				if ( superInfo != null ) {
					// supertypeName is a generic parameter
					ProgramUnit superPU = env.searchPackagePrototype(supertypeName, this.metaobjectAnnotation.getFirstSymbol());
					if ( superPU instanceof ObjectDec ) {
						ObjectDec superObj = (ObjectDec ) superPU;

						
						 // list of real interfaces implemented by 'info'
						 
						ArrayList<InterfaceDec> implInterfaceList = new ArrayList<>();
						for ( String interfaceName : info.interfaceImplementList ) {
							if ( ! map.containsKey(interfaceName) ) {
								ProgramUnit pu = env.searchPackagePrototype(interfaceName, this.metaobjectAnnotation.getFirstSymbol());
								InterfaceDec inter = (InterfaceDec ) pu;
								implInterfaceList.add(inter);
							}
						}
						for ( MethodSignature ms : info.methodSignatureList ) {
							// for each signature specified using a 'has' clause
							String fullName = ms.getFullName(env);
							if ( msNameSet.add(fullName) && ! overrideMethodSignatureSet.contains(fullName) ) {
								String name = ms.getName();
								 // is the signature in the super prototype?
								ArrayList<MethodSignature> dec_ms_List = superObj.searchMethodPublicSuperPublicProtoAndInterfaces(name, env);
								boolean found = dec_ms_List != null && dec_ms_List.size() > 0;
								if ( ! found ) {
									// is the signature in the super interfaces?
									for ( InterfaceDec inter : implInterfaceList ) {
										dec_ms_List = inter.searchMethodPublicSuperPublic(name, env);
										if ( dec_ms_List != null && dec_ms_List.size() > 0 ) {
											found = true;
											break;
										}
									}
								}
								if ( ! found ) {
									msList.add(ms);
								}
								else {
									// add ms to overrideMethodSignatureList because it appear both in a 'has' restriction and
									// in a super-prototype
									info.overrideMethodSignatureList.add(ms);
								}
							}
						}
						
					}
					else if ( superPU instanceof InterfaceDec ) {
						
					}
				}
				else {
					// supertypeName is a real prototype
					
				}
				
			}
			*/
			
			
			info.methodSignatureList = msList;
		}


		
		if ( info.isList != null && info.isList.size() > 0 ) {
			for ( IExpr e : info.isList ) {
				if ( !(e instanceof ExprTypeof) ) {
					compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "Generic prototype '" + info.name 
							+ "' parameter is in the left-hand side of a 'is' predicate and the right-hand side is not 'typeof(aType)'." 
							+ " This is illegal"); 
				}
			}
		}
		return true;
	}

	public StringBuffer createTestCase(ICompiler_ati compiler, ProtoInfo p, HashMap<String, ProtoInfo> map, String packageName1) {
		
		if ( p.isSymbol ) 
			return null;
		
		String gpName = p.name;
		
		StringBuffer s = new StringBuffer();
		s.append("package " + packageName1 + "\n\n");
		
		/*
    	public String name;
    	public boolean isPrototype = false;
    	public boolean isInterface = false;
    	public boolean isSymbol = false;
    	public ArrayList<String> superprototypeList = null;
    	public ArrayList<String> subprototypeList = null;
    	public ArrayList<String> interfaceImplementList = null;
    	public ArrayList<MethodSignature> methodSignatureList = null;
    	public ArrayList<String> inList = null;
    	public ArrayList<String> axiomList = null;
	 * 
	 */
		if ( p.isInterface ) {
			s.append("interface ");
		}
		else 
		    s.append("object ");
		s.append(gpName + " ");
		/*
		if ( gpName.equals("A") ) {
			for ( MethodSignature ms : p.overrideMethodSignatureList ) {
				System.out.println("override " + ms.getName()); 
			}
			for ( MethodSignature ms : p.methodSignatureList ) {
				System.out.println(ms.getName()); 
			}
			
		}
		*/
		if ( p.superprototypeList != null ) {
			s.append("extends ");
			int size = p.superprototypeList.size();
			  // interfaces can have more than one super-prototypes
			for ( String superProto : p.superprototypeList ) {
				s.append(superProto);
				if ( --size > 0 )
					s.append(", ");
			}
		}
		s.append(" ");
		if ( p.interfaceImplementList != null ) {
			s.append("implements ");
			int size = p.interfaceImplementList.size();
			for ( String inter : p.interfaceImplementList ) {
				s.append(inter);
				if ( --size > 0 ) 
					s.append(", ");
			}
		}
		s.append("\n");
		if ( p.methodSignatureList != null || p.overrideMethodSignatureList.size() > 0 ) {
			ArrayList<MethodSignature> allMethodSignatureList = new ArrayList<>();
			allMethodSignatureList.addAll(p.overrideMethodSignatureList);
			int numMethodWithOverride = p.overrideMethodSignatureList.size();
			if ( p.methodSignatureList != null ) {
				allMethodSignatureList.addAll(p.methodSignatureList);
			}
			
			for ( MethodSignature ms : allMethodSignatureList ) {
				String strMS = ms.asString();
				  /*
				   * using string strMS here is a very primitive way of discovering if the method signature
				   * uses 'typeof'. This hopefully will be changed some day
				   */
				if ( hasTypeof(strMS) ) {
					s.append("    @error(\"Method '" + Lexer.escapeJavaString(strMS) +  " has 'typeof' in it. I cannot produce a method signature for it\")\n");
					continue;
				}
				if ( --numMethodWithOverride >= 0 ) {
					s.append("    override\n");
				}
				s.append("    func");
				if ( ms instanceof MethodSignatureWithSelectors ) {
					MethodSignatureWithSelectors mss = (MethodSignatureWithSelectors ) ms;
					for ( SelectorWithParameters sel : mss.getSelectorArray() ) {
						s.append(" " + sel.getName() + " ");
						int size = sel.getParameterList().size();
						for ( ParameterDec param : sel.getParameterList() ) {
							String typeStr;
							if ( param.getTypeInDec() != null ) {
								typeStr = param.getTypeInDec().asString();
								if ( ! map.containsKey(typeStr) ) {
									// a type, not a generic parameter
									typeStr = param.getType().getFullName();
									if ( typeStr.startsWith(NameServer.cyanLanguagePackageNameDot) ) {
										typeStr = typeStr.substring(NameServer.cyanLanguagePackageNameDot.length());
									}
								}	
							}
							else {
								typeStr = "Dyn";
							}
							/*
							String paramStr = "";
							if ( param.getName() != null ) 
								paramStr = " " + param.getName(); 
							s.append(typeStr + paramStr); */
							s.append(typeStr);
							if ( --size > 0 )
								s.append(", ");
						}
						
					}
					
				}
				else if ( ms instanceof MethodSignatureUnary ) {
					s.append( " " + ms.getName());
				}
				else if ( ms instanceof MethodSignatureOperator ) {
					MethodSignatureOperator msop = (MethodSignatureOperator ) ms;
					s.append(" " + msop.getSymbolOperator().getSymbolString() );
					if ( msop.getOptionalParameter() != null ) {
						
						ParameterDec param = msop.getOptionalParameter();
						String typeStr;
						if ( param.getTypeInDec() != null ) {
							typeStr = param.getTypeInDec().asString();
							if ( ! map.containsKey(typeStr) ) {
								// a type, not a generic parameter
								typeStr = param.getType().getFullName();
							}	
						}
						else {
							typeStr = "Dyn";
						}
						String paramStr = "";
						if ( param.getName() != null ) 
							paramStr = " " + param.getName();
						s.append(" " + typeStr + paramStr);
					}
				}
				else {
					compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "Internal error at metaobject class '" + this.getClass().getName() + "'");
				}
				if ( ms.getReturnTypeExpr() != null ) {
					String retTypeStr = ms.getReturnTypeExpr().asString();
					if ( ! map.containsKey(retTypeStr) ) {
						retTypeStr = ms.getReturnTypeExpr().getType().getFullName();
						
						if ( retTypeStr.startsWith(NameServer.cyanLanguagePackageNameDot) ) {
							retTypeStr = retTypeStr.substring(NameServer.cyanLanguagePackageNameDot.length());
						}
						
					}
					s.append(" -> " + retTypeStr 
							+ " = " + retTypeStr + ";\n" );
				}
				else {
					s.append(" {  } \n");
				}
				
			}
		}
		s.append("\nend\n");
		
		return s;
	}
	
	
	

	static boolean hasTypeof(String strMS) {
		int i = strMS.indexOf("typeof");
		if ( i < 0 ) {
			return false;
		}
		else {
			while ( i >= 0 ) {
				if ( (i > 0 && Character.isAlphabetic(strMS.charAt(i-1))) || 
						(i + 6 < strMS.length() && Character.isAlphabetic(strMS.charAt(i+6))) ) {
					i = strMS.indexOf("typeof", i + 6);
				}
				else {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * for each identifier T that appears in the first position of a concept, such as T in<br>
	 * <code>
	 * T has [ func get -> Int ]<br>
	 * T implements ICompany <br>
	 * </code><br>
	 * add information about it in the data structure ProtoInfo
	 */
	private static ArrayList<String> collectGenericParameterData( HashMap<String, ProtoInfo> map,  ArrayList<Node> nodeList) {
		ArrayList<String> errorMessageList = new ArrayList<>();
		if ( nodeList != null ) {
			for ( Node node : nodeList ) {
				if ( node.isIdentifier ) {
					ArrayList<String> emList = node.collectGPData_dsa(map);
					if ( emList != null ) {
						errorMessageList.addAll(emList);
					}
				}
			}
			
		}
		return errorMessageList;
	}

	private MethodSignature methodSignature(ICompiler_dpa compiler_dpa) {

		MethodSignature methodSignature = null;
		ArrayList<SelectorWithParameters> selectorList;
		boolean indexingMethod = false;
		Expr returnType = null;

				
		if ( compiler_dpa.getSymbol().token == Token.LEFTRIGHTSB ) {
			indexingMethod = true;
			compiler_dpa.next();
		}
		Symbol identSymbol = compiler_dpa.getSymbol();
		
		boolean isInit = false;
		boolean isInitOnce = false;
		String name = "";
		
		switch ( compiler_dpa.getSymbol().token ) {
		case IDENT:
			name = compiler_dpa.getSymbol().symbolString;
			methodSignature = new MethodSignatureUnary(compiler_dpa.getSymbol(), compiler_dpa.getCurrentMethod() );

			if ( indexingMethod )
				compiler_dpa.error(compiler_dpa.getSymbol(), "unary methods cannot be indexing methods (declared with '[]')");
			
			if ( name.equals("new") ) {
				compiler_dpa.error(compiler_dpa.getSymbol(), "'new' cannot be user-declared. Use 'init' instead");
			}
			
			compiler_dpa.next();
			break;

		case IDENTCOLON:
			selectorList = new ArrayList<SelectorWithParameters>();
			while ( compiler_dpa.getSymbol().token == Token.IDENTCOLON ) {
				SelectorWithParameters selectorWithParameters = new SelectorWithParameters(compiler_dpa.getSymbol());
				// if ( name.length() > 0 ) 
				name = name +  compiler_dpa.getSymbol().symbolString;
				compiler_dpa.next();

				if ( compiler_dpa.getSymbol().token == Token.LEFTPAR || compiler_dpa.startType(compiler_dpa.getSymbol().token) ) {
					parameterDecList(compiler_dpa, selectorWithParameters.getParameterList());
				}
				selectorList.add(selectorWithParameters);
				
			}
			methodSignature = new MethodSignatureWithSelectors(selectorList, indexingMethod, compiler_dpa.getCurrentMethod());
			if ( selectorList.size() == 1 ) {
				if ( selectorList.get(0).getName().equals("new:") ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "'new:' cannot be user-declared. Use 'init' instead");
				}
				if ( name.equals("init:") ) {
					if ( selectorList.get(0).getParameterList() == null || selectorList.get(0).getParameterList().size() == 0 ) {
						compiler_dpa.error(selectorList.get(0).getSelector(), "'init:' methods should take parameters");
					}
				}
			}
			else {
				/*
				 * check if any selector is 'init:' or 'new:'
				 */
				for ( SelectorWithParameters sel : selectorList ) {
					if ( sel.getName().equals("init:") || sel.getName().equals("new:") )
						compiler_dpa.error(sel.getSelector(), "It is illegal to have a selector with name 'init:' or 'new:'");
				}
			}
			break;
		default:
			// should be an operator
			if ( compiler_dpa.isOperator(compiler_dpa.getSymbol().token) ) {
				if ( compiler_dpa.getSymbol().token == Token.AND || compiler_dpa.getSymbol().token == Token.OR ) {
					compiler_dpa.error(compiler_dpa.getSymbol(), "'&&' and '||' cannot be method names. They can only be used with Boolean values");
				}
				Symbol operatorSymbol = compiler_dpa.getSymbol();
				MethodSignatureOperator mso = new MethodSignatureOperator(compiler_dpa.getSymbol(), compiler_dpa.getCurrentMethod());
				methodSignature = mso;
				compiler_dpa.next();
				if ( compiler_dpa.startType(compiler_dpa.getSymbol().token) ||
					 compiler_dpa.getSymbol().token == Token.LEFTPAR ||
					 compiler_dpa.getSymbol().token == Token.IDENT ) {  // this last is not really necessary but ...

					boolean leftpar = false;
					if ( compiler_dpa.getSymbol().token == Token.LEFTPAR ) {
						leftpar = true;
						compiler_dpa.next();
					}
					SymbolIdent parameterSymbol;
					Expr typeInDec = (Expr ) compiler_dpa.type();
					
					if ( compiler_dpa.getSymbol().token != Token.IDENT ) {
						
						if ( typeInDec instanceof ExprIdentStar ) {
							ExprIdentStar eisType = (ExprIdentStar ) typeInDec; 
							if ( eisType.getIdentSymbolArray().size() > 1 ) {
								// found a prototype preceded by a package
								parameterSymbol = null;
							}
							else if ( Character.isUpperCase(eisType.getIdentSymbolArray().get(0).getSymbolString().charAt(0)) ) {
								/*
								 *  typeInDec is a type really 
								 */
								parameterSymbol = null;
							}
							else {
								/*
								 * no package, starts with a lower case letter. It must be a parameter 
								 */
								parameterSymbol = (SymbolIdent) eisType.getIdentSymbolArray().get(0);
								typeInDec = null;
							}
							
						}
						else {
							// typeInDec may be a generic prototype instantiation
							parameterSymbol = null;
						}
					}
					else {
						parameterSymbol = (SymbolIdent ) compiler_dpa.getSymbol();
						
						if ( Character.isUpperCase(parameterSymbol.getSymbolString().charAt(0)) )
							compiler_dpa.error(parameterSymbol, "Variables and parameters cannot start with an uppercase letter");
						
						
						compiler_dpa.next();
					}
					if ( leftpar ) {
						if ( compiler_dpa.getSymbol().token != Token.RIGHTPAR ) {
							compiler_dpa.error(compiler_dpa.getSymbol(), "')' expected." + foundSuch(compiler_dpa));
						}
						else {
							compiler_dpa.next();
						}
					}
					ParameterDec parameterDec = new ParameterDec( parameterSymbol, typeInDec, compiler_dpa.getCurrentMethod());

					mso.setOptionalParameter( parameterDec );
				}
				else {
					// without parameters: then it should be a unary operator
					if ( ! Compiler.isUnaryOperator(operatorSymbol.token) ) {
						compiler_dpa.error(operatorSymbol, "This operator cannot be used as a unary method. It should take a parameter");
					}
				}
			}
			else {
				compiler_dpa.error(compiler_dpa.getSymbol(),  "A method name was expected. " + CyanMetaobjectConcept.foundSuch(compiler_dpa));
				return null;
			}
		}
		if ( compiler_dpa.getSymbol().token == Token.RETURN_ARROW ) {
			compiler_dpa.next();

			returnType = (Expr ) compiler_dpa.type();


			if ( (isInit || isInitOnce) && returnType != null ) { 
				compiler_dpa.error( returnType.getFirstSymbol(), 
						"'init:' and 'init' methods cannot have a return value");
			}
		}

		methodSignature.setReturnTypeExpr(returnType);
		return methodSignature;
	}

	
	private void parameterDecList(ICompiler_dpa compiler_dpa, ArrayList<ParameterDec> parameterList) {

		if ( compiler_dpa.getSymbol().token == Token.LEFTPAR ) {
			compiler_dpa.next();
			parameterDecList(compiler_dpa, parameterList);
			if ( compiler_dpa.getSymbol().token != Token.RIGHTPAR )
				compiler_dpa.error(compiler_dpa.getSymbol(), "')' expected after parameter declaration." + foundSuch(compiler_dpa));
			else {
				compiler_dpa.next();
			}
		}
		else {
				paramDec(compiler_dpa, parameterList);
				while ( compiler_dpa.getSymbol().token == Token.COMMA ) {
					compiler_dpa.next();
					paramDec(compiler_dpa, parameterList);
				}
			}
	}
	
	/**
	 * return true if 'e' is a type, which should start with an upper-case letter without any dots as 
	 * "Int", "Program" or it should be a package name followed by a prototype name as 
	 * "main.Program", "cyan.lang.Int"
	 */
    static public boolean isType(Expr e) {
    	
    	if ( e instanceof ExprIdentStar ) {
    		ExprIdentStar eis = (ExprIdentStar) e;
    		String firstName = eis.getIdentSymbolArray().get(0).getSymbolString();
    		return eis.getIdentSymbolArray().size() > 1 || 
    				Character.isUpperCase(firstName.charAt(0));
    	}
    	else
    		return true;
    }
	
	
	private static void paramDec(ICompiler_dpa compiler_dpa, ArrayList<ParameterDec> parameterList) {

		
		Symbol variableSymbol;
		Expr typeInDec = (Expr )  compiler_dpa.type();
		
		VariableKind parameterType;
		if ( compiler_dpa.getSymbol().token == Token.BITAND ) {
	    	parameterType = VariableKind.LOCAL_VARIABLE_REF;
	    	compiler_dpa.next();
		}
		else {
			parameterType = VariableKind.COPY_VAR;
		}
		
		if ( compiler_dpa.getSymbol().token != Token.IDENT ) {
			
			if ( isType(typeInDec) ) {  
				variableSymbol = null;
			}
			else {
				// type is in fact the variable, which was given without the type as in
				//     func  at: x {  }
				variableSymbol = typeInDec.getFirstSymbol();
				typeInDec = null;
			}
			
		}
		else {
			variableSymbol = compiler_dpa.getSymbol();
			if ( Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
				compiler_dpa.error(variableSymbol, "Variables and parameters cannot start with an uppercase letter");
			
			compiler_dpa.next();
		}
		
			
		
		ParameterDec parameterDec = new ParameterDec( variableSymbol, typeInDec, compiler_dpa.getCurrentMethod() );
		parameterDec.setVariableKind(parameterType);
		parameterList.add( parameterDec );
		
	}
	
	private static String foundSuch(ICompiler_dpa compiler_dpa) {
		String s = " Found '" + compiler_dpa.getSymbol().getSymbolString() + "'";
		if ( compiler_dpa.getSymbol() instanceof SymbolKeyword ) {
			s = s + " which is a Cyan keyword";
		}
		return s;
	}

	@Override
	public void dpa_actionGenericProgramUnit(ICompilerGenericProgramUnit_dpa compiler) {
		
		
		ArrayList<Object> paramList = this.getMetaobjectAnnotation().getJavaParameterList();
		if ( paramList != null && paramList.size() == 1 && paramList.get(0) instanceof String ) {
			String p = NameServer.removeQuotes((String ) paramList.get(0));
			if ( p.equals("test")  ) {
				if ( compiler.getCompilationStep() == CompilationStep.step_4 ) {
					/*
					 * the argument to the metaobject annotation is 'test' and the compiler is in the last parse phase
					 */
					/*
					 * if filename is the name of a file that holds a generic prototype, write test files in the test directory
					 */

					if ( ! isInstantiatedGenericPrototype(compiler)) {
						compiler.addToListAfter_ati(this);
					}
					
				}
				else if ( compiler.getCompilationStep() == CompilationStep.step_7 ) {

					if ( isInstantiatedGenericPrototype(compiler)) {
						compiler.addToListAfter_ati(this);
					}
									
				}
			}
		}
		
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		Declaration dec = annotation.getDeclaration();
		if ( !(dec instanceof ProgramUnit) ) {
			compiler.error(this.metaobjectAnnotation.getFirstSymbol(), "This metaobject should be attached to a prototype");
			return ;
		}
		ProgramUnit pu = (ProgramUnit ) dec;
		compilationUnitGenericPrototype = pu.getCompilationUnit();
		
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return compilationUnitGenericPrototype;
	}

	
	
	@Override
	public void after_ati_action(ICompiler_ati compiler) {
		@SuppressWarnings("unchecked")
		ArrayList<Node> nodeList = (ArrayList<Node> ) this.metaobjectAnnotation.getInfo_dpa();
		
		
		String filename = ((ProgramUnit ) this.getMetaobjectAnnotation().getDeclaration()).getCompilationUnit().getFilename();
		int index = filename.indexOf('(');
		if ( ( ( index >= 0 && index < (filename.length() - 1) && Character.isDigit(filename.charAt(index+1)) ) ) ) {
			/* 
			 * a generic prototype 
			 */
			
			
			
			CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
			Declaration dec = annotation.getDeclaration();
			ProgramUnit pu = (ProgramUnit ) dec;
			
			String protoName = pu.getSimpleName() + "_Test";
			StringBuffer s = new StringBuffer();
			String dirName = protoName.toLowerCase();
			String testPackageName = compiler.getPackageNameTest() + "." + dirName;
			s.append("package " + testPackageName + "\n\n");
			/*
			for ( String importedPackageName : cunit.getImportedPackageNameList() ) {
				s.append(importedPackageName + "\n");
			}
			*/
			s.append("object " + protoName + "\n");
			s.append("    func run {\n");
			String name = pu.getFullName();
			s.append("        var " + name + " testVar;\n");
			s.append("        \n");
			s.append("    }\n\n");
			s.append("end\n");

			compiler.deleteDirOfTestDir(dirName);
			compiler.writeTestFileTo(s, protoName + "." + NameServer.cyanSourceFileExtension, dirName);
			
			ArrayList<ArrayList<GenericParameter>> gpListList = pu.getGenericParameterListList();		
			HashSet<String> gpSet = new HashSet<>();
			for ( ArrayList<GenericParameter> gpList : gpListList ) {
				for ( GenericParameter gp : gpList ) {
					gpSet.add(gp.getName());
				}
			}		
			for ( Node node : nodeList ) {
				node.calcInternalTypes( (Env ) compiler.getEnv(), gpSet );
			}
					
			createTestCasesForGenericParameters(nodeList, compiler, dirName);

		}
		else {
			/*
			 * an instantiated generic prototype or a regular prototype
			 */
			 

			
			
			CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
			Declaration dec = annotation.getDeclaration();
			ProgramUnit pu = (ProgramUnit ) dec;
			
			String protoName = pu.getName() + "_Axiom_Test";
			protoName = protoName.replaceAll("<", "_lt_");
			protoName = protoName.replaceAll(">", "_gt_");
			protoName = protoName.replaceAll("\\.", "_d_");
			protoName = protoName.replaceAll(",", "_c_");
			StringBuffer s = new StringBuffer();
			String dirName = protoName.toLowerCase();
			String testPackageName = compiler.getPackageNameTest() + "." + dirName;
			s.append("package " + testPackageName + "\n\n");
			/*
			for ( String importedPackageName : cunit.getImportedPackageNameList() ) {
				s.append(importedPackageName + "\n");
			}
			*/
			s.append("object " + protoName + "\n");
			
			int numberNextMethod = 0;
 			StringBuffer allAxiomMethods = null;
 			for ( Node node : nodeList ) {
 				Tuple2<StringBuffer, Integer> t = node.axiomTestCode_dsa(numberNextMethod);
 				if ( t != null ) {
 	 				StringBuffer other = t.f1;
 	 				if ( other != null ) {
 	 	 				numberNextMethod = t.f2;
 	 					if ( allAxiomMethods == null ) {
 	 						allAxiomMethods = new StringBuffer();
 	 					}
 	 					allAxiomMethods.append(other);
 	 				}
 				}
 			}
 			if ( allAxiomMethods != null ) {
 				s.append(allAxiomMethods);
 			}
			s.append("end\n");
 			if ( allAxiomMethods != null && allAxiomMethods.length() > 1 ) {
 				compiler.writeTestFileTo(s, protoName + "." + NameServer.cyanSourceFileExtension, dirName);
 			}

			compiler.deleteDirOfTestDir(dirName);
			compiler.writeTestFileTo(s, protoName + "." + NameServer.cyanSourceFileExtension, dirName);
			
									
					
		}
	}

	
	private ArrayList<ProtoInfo> topologicalSortingProtoInfoList(HashMap<String, ProtoInfo> map, Env env) {
    	/*
    	 * program unit, super-prototype list, sub-prototype list
    	 */
    	HashMap<String, Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> > protoNameAdjList = new HashMap<>();
    	ArrayList<ProtoInfo> noSuperList = new ArrayList<>();
    	ArrayList<ProtoInfo> programUnitList = new ArrayList<>();
    	/**
    	 * collect the formal parameters
    	 */
    	for ( String key : map.keySet() ) {
    		ProtoInfo value = map.get(key);
    		programUnitList.add(value);
    		ArrayList<ProtoInfo> superList = new ArrayList<>();
    		ArrayList<ProtoInfo> subList = new ArrayList<>();
    		protoNameAdjList.put(value.name,  new Tuple2<>(superList, subList));
    	}
        /**
         * build the graph of sub-type and super-type relationships
         */
        for ( ProtoInfo pu : programUnitList ) {
        	Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> t = protoNameAdjList.get(pu.name);
        	if ( t == null ) {
        		env.error(null,  "Internal error: program unit '" + pu.name + "' was not found in topological sorting (Concept metaobject)");
        		return null;
        	}
    		ArrayList<ProtoInfo> superList = t.f1;
    		
    		boolean foundSuper = false;
    		if ( pu.superprototypeList != null && pu.superprototypeList.size() != 0 ) {
    			for ( String superName : pu.superprototypeList ) {
    				ProtoInfo superProto =  map.get(superName);
    				if ( superProto != null ) {
    					// superProto is a generic parameter
    					superList.add(superProto);
    					Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> superT = protoNameAdjList.get(superName);
    					superT.f2.add(pu);
    					foundSuper = true;
    				}
    			}
    		}
    		if ( pu.interfaceImplementList != null && pu.interfaceImplementList.size() != 0 ) {
    			for ( String interName : pu.interfaceImplementList ) {
    				ProtoInfo interProto = map.get(interName);
    				if ( interProto != null ) {
    					superList.add(interProto);
    					Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> superT = protoNameAdjList.get(interName);
    					superT.f2.add(pu);
    					foundSuper = true;
    				}
    			}
    		}    		
    		if ( ! foundSuper ) {
    			noSuperList.add(pu);
    		}
    		
        }
        /**
         * do the topological sorting
         */
        ArrayList<ProtoInfo> sortedProgramUnit = new ArrayList<>(); 
        while ( noSuperList.size() > 0 ) {
        	ProtoInfo pu = noSuperList.get(0);
        	noSuperList.remove(0);
        	sortedProgramUnit.add(pu);

        	Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> t = protoNameAdjList.get(pu.name);
    		ArrayList<ProtoInfo> subPUList = t.f2;
    		/*
    		 * remove all edges from sub-types to the super-type pu
    		 */
    		for ( ProtoInfo subProto : subPUList ) {
            	Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> subT = protoNameAdjList.get(subProto.name);
            	/*
            	 * remove pu from the list of super-types of subT
            	 */
            	int i = 0;
            	for ( ProtoInfo superSubProto : subT.f1 ) {
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
        
        for ( ProtoInfo pu : programUnitList ) {
        	Tuple2<ArrayList<ProtoInfo>, ArrayList<ProtoInfo>> t = protoNameAdjList.get(pu.name);
    		ArrayList<ProtoInfo> superList = t.f1;
    		if ( superList.size() > 0 ) {
        		String puName = pu.name;
        		String s = puName;
        		for ( ProtoInfo superPU : superList ) {
        			String superFullName = superPU.name; 
        			if ( superFullName.equals(puName) ) {
        				break;
        			}
        			s += ", " + superFullName;
        		}
        		s += ", " + puName;
        		env.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Circular subtype relationship among the generic parameters. " + 
     	    	       "Something like\nC subprototype B\nB subprototype A\nA subprototype C");
    		}
        	
        }

        return sortedProgramUnit;
    }
    
    	
	
	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.PROTOTYPE_DEC };


	CompilationUnit compilationUnitGenericPrototype;
}
