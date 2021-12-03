/**
   Represents a method signature which may be:
     - a unary method such as
          fun getName -> String
          fun set
     - a regular keyword method such as
          fun width: (:w int)  height: (:h int) [ ... ]
     - a grammar method such as
          fun (add: int)+
     - a method signature of an interface, which may not have
       parameter names (only the types are demanded)

    There are subclasses representing each of these possibilities.
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

/**
 * @author José
 *
 */
abstract public class MethodSignature implements GenCyan, ASTNode {
	
	public MethodSignature(MethodDec method) {
		this.method = method;
		attachedMetaobjectAnnotationList = null;
		hasCalculatedInterfaceTypes = false;
		fullName = null;
	}

	
	
	public void setReturnTypeExpr(Expr returnType) {
		this.returnTypeExpr = returnType;
	}

	public Expr getReturnTypeExpr() {
		return returnTypeExpr;
	}
	
	public Type getReturnType(Env env) {
		if ( returnType == null )
			calcInterfaceTypes(env);
		return returnType;
	}
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}
	
	abstract public String getJavaName();
	/**
	 * For each method there should be created an inner prototype that represents the method. After all, each
	 * method is an object too. This inner prototype should declare a single method called "eval", "eval:", or 
	 * "eval: eval: ..." (if the method has multiple selectors such as "fun at: Int put: Int").
	 * 
	 * This method returns the signature of this 'eval' method. 
	   @param s
	 */

	abstract public void genCyanEvalMethodSignature(StringBuffer s);
	
	/**
	 * generates only the return value type
	 */
	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		if ( returnTypeExpr != null ) {
			pw.print(" -> ");
			returnTypeExpr.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			pw.print(" ");
		}
		else
			pw.print(" ");

	}

	final public void genCyanMetaobjectAnnotations(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : this.nonAttachedMetaobjectAnnotationList ) {
				annotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			}
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : this.attachedMetaobjectAnnotationList ) {
				annotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			}
		}

	}
	
	
	public void genJavaAsConstructor(PWInterface pw, Env env, String javaNameDeclaringObject) { }	
	
	/**
	 * generates only the return value type
	 */
	public void genJava(PWInterface pw, Env env, boolean isMultiMethod) {
		if ( returnTypeExpr != null ) {
			pw.print(returnTypeExpr.getType().getJavaName());
			//pw.print(NameServer.getJavaNameGenericPrototype(returnTypeExpr.ifPrototypeReturnsItsName()));
		}
		else
			pw.print(NameServer.getJavaName("Nil"));
		pw.print(" ");
	}


	public void genJava(PWInterface pw, Env env) {
		genJava(pw, env, false);
	}

	abstract public String getSingleParameterType();

	//abstract public String getName();

	public void check(Env env) {
	}
	
	public boolean isGrammarMethod() {
		return false;
	}
	
	abstract public Symbol getFirstSymbol();

	public void calcInterfaceTypes(Env env) { 
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);		
		}
		
		if ( returnTypeExpr != null ) {
			boolean ok = true;
			try {
				returnTypeExpr.calcInternalTypes(env);
			}
			catch ( error.CompileErrorException e ) {
				returnType = Type.Dyn;
				ok = false;
			}
			if ( ok && returnTypeExpr.getType() != null ) {
				returnType = returnTypeExpr.ifRepresentsTypeReturnsType(env);
			}
		}
		else {
			returnType = Type.Nil;
		}
	}
	

	/**
	 * just push the parameter in the list of declared variables
	   @param env
	 */
	abstract public void calcInternalTypes(Env env);
	
	
	
	/**
	 * does this method is declared with [] before at: or at:put: ? 
	 * @return
	 */
	public boolean isIndexingMethod() {
		return false;
	}
	
	
	/**
	 * the Java name of the method with this signature. For example, if the
	 * method is declared as
	 *      public fun run: [ ]
	 * the Java name is "run". If it is declared as
	 * 	    public fun at: (:index int) put: (:value  String) [ ... ]
	 * the Java name is
	 *         at_p_int_s_put_p_CyString
	 * If it is declared as
	 * 	    public fun person_name -> String
	 * its Java name is
	 *     person__name
	 * In the Java name, all _ are duplicated. That differentiated underscores
	 * put by the Compiler, as in the at_p_int_s_put_p_CyString example, and
	 * underscores that were in the original name.
	 *
	 * 	 * In the general case:
	 *         selectorName + ("_p_" + typeName)* +
	 *         ("_s_" + selectorName + ("_p_" + typeName)* )+
	 *
	 */
	//abstract public String getJavaName();

	/**
	 * return the method signature as a string. But with the parameter names 
	 * @return
	 */
	public String getMethodSignatureWithParametersAsString() {
		PWCharArray pwChar = new PWCharArray();
        genCyan(pwChar,  false, NameServer.cyanEnv, true);
		return pwChar.getGeneratedString().toString();		
	}

	

	
	public MethodDec getMethod() {
		return method;
	}

	public void setMethod(MethodDec method) {
		this.method = method;
	}
	
	
	/**
	 * return the method name. See specific help for each of the different method signatures: unary, operator, grammar, regular
	   @return
	 */
	abstract public String getNameWithoutParamNumber();
	/**
	 * For unary, grammar, and operator methods, it returns the same as {@link #getNameWithoutParamNumber()}. For regular methods, it
	 * returns the names of all selectors plus its number of parameters concatenated. 
	 * That is, the return for method<br>
	 * <code>with: Int n, Char ch plus: Float f</code><br>
	 * would be <code>with:2 plus:1</code> 
	 */
	public String getName() {
		return getNameWithoutParamNumber();
	}
	
	/**
	 * return the name of the Cyan inner prototype that will represent this method.
	 * For each method the compiler creates an inner prototype, declared inside this prototype.
	 * The name of this prototype is returned by this method.
	 */
	abstract public String getPrototypeNameForMethod();
	/**
	 * methods are objects in Cyan that inherit from UFunction super-prototype. This method
	 * return the super-prototype as String. For example, method
	 *      fun with: Int i, Char ch put: String s -> Person
	 *  is an object that inherits from UFunction<Int, Char><String, Person> 
	 */
	abstract public String getSuperprototypeNameForMethod();

	/**
	 * returns the full name of this method signature. That includes the 
	 * full names of the parameter types as <br>
	 *        "at: cyan.lang.Int put: cyan.lang.String" <br>
	 *        "add: people.Person"<br>
	 * It does not include the return type
	   @return
	 */
	abstract public String getFullName(Env env);

    public String getFullNameWithReturnType(Env env) {
    	return this.getFullName(env) + " -> " + this.getReturnType(env).getFullName();
    }



	/**
	 * return the list of parameters of this signature. It includes all parameters of all selectors. 
	 * Unary methods return null. Grammar methods return their single parameter
	 */
	abstract public ArrayList<ParameterDec> getParameterList();

	public ArrayList<CyanMetaobjectWithAtAnnotation> getAttachedMetaobjectAnnotationList() {
		return attachedMetaobjectAnnotationList;
	}

	public void setMetaobjectAnnotationNonAttachedAttached(ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList, 
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList) {
		this.nonAttachedMetaobjectAnnotationList = nonAttachedMetaobjectAnnotationList;
		this.attachedMetaobjectAnnotationList = attachedMetaobjectAnnotationList;
	}

	public String asString(CyanEnv cyanEnv) {
		PWCharArray pwChar = new PWCharArray();
		genCyan(pwChar, false, cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}
	
	@Override
	public String asString() {
		return asString(NameServer.cyanEnv);
	
	}

	public void check_cin(Env env) {
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
						env.thrownException(annotation, this.getFirstSymbol(), e);
					}					
					finally {
						env.errorInMetaobjectCatchExceptions(annotation.getCyanMetaobject(), annotation);
					}
									
					
					
				}
			}
		}
	}
		
	
	public InterfaceDec getDeclaringInterface() {
		return declaringInterface;
	}

	public void setDeclaringInterface(InterfaceDec declaringInterface) {
		this.declaringInterface = declaringInterface;
	}


	public boolean getHasCalculatedInterfaceTypes() {
		return hasCalculatedInterfaceTypes;
	}

	public void setHasCalculatedInterfaceTypes(boolean hasCalculatedTypes) {
		this.hasCalculatedInterfaceTypes = hasCalculatedTypes;
	}

	
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

	
	
	/**
	 * return the function name of a function that has the same parameters as this method signature. For example, 
	 * if this  method signature is<br>
	 * <code>
	 *     func at: Int a put: Char ch, Float b  with: String s1, String s2 -> Array<Char>
	 * </code><br>
	 * then the function name returned is <br>
	 * <code>
	 *     Function{@literal <}Int>{@literal <}Char, Float>{@literal <}String, String, Array{@literal <}Char>>
	 * </code><br>
	   @return
	 */
	abstract public String getFunctionName();

	
	public abstract String getFunctionNameWithSelf(String fullName2);

	
	public abstract String getSignatureWithoutReturnType();


	/**
	 * the list of features associated to this signature
	 */
	private ArrayList<Tuple2<String, ExprAnyLiteral>> featureList;	
	

	protected String javaName;


	/** the return type of the method corresponding to this method signature */
	protected Type returnType;
	
	protected Expr returnTypeExpr;


	/**  method to which this signature belongs to
	 * 
	 */
	private MethodDec method;

	/**
	 * list of metaobject annotations attached to this signature. It is only used with method signatures of interfaces
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList;
	
	private ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList;

	/**
	 * the interface in which this method signature is. null if this method signature is in a prototype
	 */
	private InterfaceDec declaringInterface;
	
	/**
	 * true if the type of this method signature have already been calculated
	 */
	protected boolean hasCalculatedInterfaceTypes;

	/**
	 * the full name of this signature
	 */
	protected String fullName;


}
