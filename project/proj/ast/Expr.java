package ast;

import meta.IExpr;
import meta.IType;
import saci.Env;
import saci.Tuple;
import saci.Tuple2;

/**
 * Superclass of all classes that describe expressions.
 * @author José
 *
 */

public abstract class Expr extends Statement implements GenCyan, GenJavaInterface, ICalcInternalTypes, IExpr, ASTNode {
	
	public Expr() {
		super();
		type = null;
		codeThatReplacesThisExpr = null;
	}


	
	@Override
	public IType getIType() {
		return type;
	}


	@Override
	abstract public void accept(ASTVisitor visitor);



	/**
	 * generate code for the expression and assign it to
	 * a temporary variable. The temporary variable name
	 * is returned. For example, when genJavaExpr is called to generate
	 * code for "1+2", it generates
	 *        int t1 = 1;
	 *        int t2 = 2;
	 *        int t3 = t1 + t2;
	 * The value returned is "t3".
	 * @return the value of a temporary variable that will holp, at runtime, the value of the expression.
	 */

	abstract public String genJavaExpr(PWInterface pw, Env env);

	public void genJavaCodeVariable(PWInterface pw, Env env) {
		
		env.error(getFirstSymbol(), "Internal error: method genJavaExprWithoutTmpVar called on an inappropriate object");
	}
	

	@Override
	public void genJava(PWInterface pw, Env env) {
		genJavaExpr(pw, env);
	}


	/**
	 * If the expression represents a type, this method returns
	 * the Java name of that expression. The table below shows
	 * the Cyan identifier and the corresponding value returned
	 * by this method.
	 *           Cyan                Java
	 *           Int                 CyInt
	 *           main	             _main
	 *           cyan.lang.Int       cyan.lang.CyInt
	 *           Array<Any>			 Array_left_gp_Any_right
	 *           
	 * Please check if the Java name is exactly what is in the
	 * right column. This is correct at the time of this writing. 
	 * After many Compiler changes, the Java names may have been
	 * changed a little bit.
	 * 
	 * This method returns null if the expressions does not
	 * correspond to an identifier such as "1 + 2" or "a add: 10".
	 * @return
	 */
	public String getJavaName() {
		return null;
	}


	/**
	 *
	 * @param env
	 * @return the type associated to this expression, if this expression
	 * is a type. null otherwise. For example, if the expression is a ExprIdent
	 * representing identifier "Person" and there is an object called "Person",
	 * asType returns the type of object Person, which is an object of
	 * TypeObjectInterface. This object of TypeObjectInterface has a reference
	 * to the object representing the declaration of object Person.
	 *
	 * If this expression represents identifier "int", asType returns an object
	 * of TypeBasicInt.

	public Type asType(Env env) {
		return null;
	}
	*/

	/**
	 * return true if "this", the object that received the message isSubtypeOf,
	 * is a type that is subtype of "expr", the parameter. Here subtype means
	 * that "this" represents a type and "expr" has a type that can be
	 * assigned to a variable of the type represented by "this. In other words,
	 * if the receiver is an object of ExprIdent that represents an object
	 * Person and "expr" has a type "Worker", then isSubtypeOf(expr) will
	 * return true if Worker is a subobject of Person.
	 *
	 * If "this" represents type int and expr result in a type int, isSubtypeOf
	 * will return true too.
	 *
	 * This method should be redefined in all subclasses of Expr that
	 * can represent a type, such as ExprIdent and ExprGenericType.
	 * @param expr
	 * @return
	 */
	public boolean isSubtypeOf(Expr expr) {
		throw new error.CompileErrorException();
		// return false;
	}
	
	/**
	 * if this expression can be a type, the name of the prototype is returned. 
	 * Then if the expression is an identifier "Person", "Person" is returned. If the expression
	 * is a generic prototype instantiation "{@code Stack<Person>}", "{@code Stack<Person>}" is returned. Otherwise,
	 * null is returned. This method can only be used before the semantic analysis. It does not consider 
	 * the package of the prototype. 
	 * @return
	 */  
	@Override
	public String ifPrototypeReturnsItsName() {
		String ret = null;
		  // this cascaded if´s seem better than polymorphism
		if ( this instanceof Identifier ) {
			ret = ((Identifier ) this).getName();
		}
		else if ( this instanceof ExprGenericPrototypeInstantiation ) {
			ret = ((ExprGenericPrototypeInstantiation) this).getName();
		}
		else if ( this instanceof  ExprTypeof ) {
			ret = "typeof(" + ((ExprTypeof) this).getArgument().ifPrototypeReturnsItsName() + ")";
		}
		return ret;
	}
	
	
	/** This is a version of method ifPrototypeReturnsItsName that should be called in the
	 * semantic analysis.
	 * if this expression can be a type, the name of the prototype is returned. If this expression is 'typeof', then the type of
	 * the parameter is returned. 
	 * Then if the expression is an identifier "Person", "Person" is returned. If the expression
	 * is a generic prototype instantiation {@code "Stack<Person>"}, {@code "Stack<people.Person>"} is returned. Otherwise,
	 * null is returned.
	 * @return
	 */  
	final public String ifPrototypeReturnsItsName(Env env) {
		
		if ( type == null ) 
			this.calcInternalTypes(env);
		return type.getFullName(env);
		
		/*
		Tuple<String, Type> t = this.ifPrototypeReturnsNameWithPackageAndType(env);
		if ( t == null || t.f2 == null ) {
			env.error(this.getFirstSymbol(),  "Prototype '" + this.asString() + "' was not found");
			return null;
		}
		else {  
			return t.f1;
		}
		*/
	}
	/** The receiver of this message can be three things: {@link ast.ExprIdentStar}, {@link ast.ExprGenericPrototypeInstantiation}, or
	 * {@link ast.ExprTypeOf}. It is assumed that the receiver is an expression that represents a type or a lower-case symbol. If this method is 
	 * called with an expression that, by the parsing phase, is not a type or a lower-case symbol, the result is not guaranteed. Then be aware that 
	 * this method can only be called when all other options for the expression has been discarded. <br><br> 
	 *   
	 * The receiver of this method can be expressions that represent things like<br>
	 * f1, Client, main.Program, OR <br>
	 * Function{@literal <}cyan.lang.Int, Function{@literal <}Union{@literal <}Person, company.Worker>, Stack{@literal <}Int>>>, OR <br>
	 * typeof(Client) OR<br>
	 * {@code Fun_7__}<br>
	 * 
	 * This last one represents an inner prototype. 
	 * 
	 * This method returns a tuple consisting of:<br>
	 *   "f1" and null if 'this' (the receiver) is "f1". "f1" starts with a lower-case letter and then it cannot be a prototype; <br>
	 *   "bank.Client" and its type if 'this' is "Client"; <br>
	 *   "main.Program" and and its type if 'this' is "main.Program"; <br>
	 *   "Function{@literal <}Int, Function{@literal <}Union{@literal <}company.Person, company.Worker>, cyan.util.Stack{@literal <}Int>>>" 
	 *     and its type if 'this' is
	 *   Function{@literal <}cyan.lang.Int, Function{@literal <}Union{@literal <}Person, company.Worker>, Stack{@literal <}Int>>>; <br>
	 *   "bank.Client" and its type if 'this' is "typeof(Client)"<br>
	 *   {@code "Fun_7__"} and its type if 'this' is {@code Fun_7__}<br><br>
	 *   
	 *   Note that, if the receiver is a type, the first tuple element, a String, contains the full name of the type.
	 * 
	 * 
	 */
	
	public Tuple<String, Type> ifPrototypeReturnsNameWithPackageAndType(Env env) {
		return null;
	}
	
	/**
	 * assuming that this expression represents a type, this method returns the object of Type that it represents. 
	 * Then if 'this' is 'Person', this method returns the object of ObjectDec that represents 'Person'.
	 * <br>
	 * This method returns an invalid value if 'this' does not represent a type. 
	   @param env
	   @return
	 */
	public Type ifRepresentsTypeReturnsType(Env env) {
		if ( type == null ) {
			calcInternalTypes(env);
		}
		return type;
	}	
	

	// static private int count = 0;
	
	@Override
	public void calcInternalTypes(Env env) {
		/*
		if ( count == 100 )
			// System.out.println(count);
		++count;
		*/
		super.calcInternalTypes(env);
	}
	
	public void calcInternalTypes(Env env, boolean leftHandSideAssignment) {
		/*
		if ( count == 100 )
			// System.out.println(count);
		++count;
		*/
		calcInternalTypes(env);
	}

	/**
	\begin{enumerate}[(i)]
	\item a basic type (such as {\tt Int}, {\tt Nil}, or {\tt String}) is a NRE;
	\item a value of a basic type (such as {\tt 0} or \verb|"Hello"|) is a NRE;
	\item an unary message to a literal value of a basic type (such as {\tt -0} or \verb|+3.14|) is a NRE;
	\item a literal array, a literal map, or a literal tuple whose elements are NRE is a NRE;
	\item an object creation of a prototype if the arguments used are NRE. For example, \\
	\verb|    Array<Int>(5)| \\
	\noindent or \\
	\verb|    Array<String> new: Int new|\\
	\noindent is a NRE. Unlike the {\tt initOnce} methods, there is no resctriction on the package of the prototype, it can be anyone.
	\end{enumerate}
	   @return
	 */	
	public boolean isNRE(Env env) {
		return false;
	}
	
	/**
	 * return true if this expression is non-recursive considering the point of view of method initOnce.
	   @param env
	   @return
	 */
	public boolean isNREForInitOnce(Env env) {
		return isNRE(env);
	}
		
	final public Type getType() {
		return type;
	}

	
	final public Type getType(Env env) {
		if ( type == null ) {
			this.calcInternalTypes(env);
			// env.error(getFirstSymbol(), "Internal error at Expr::getType: getType() called before calcInternalTypes in class " + this.getClass().getName(), true);
		}
		return type;
	}
	
	/**
	 * if this expression represents or may represent a type, this method returns a string that
	 * may be used for creating the Java name for it. Currently the result is identical to a call
	 * to 'asString' but without message sends to metaobjects. This is the case with<br>
	 * <code>     Function<Int, Nil>.#writeCode</code><br>
	 * The string returned by this function would be <code>Function<Int,Nil></code>. 
	 */
	public String asStringToCreateJavaName() {
		return asString();
	}

	/**
	 * generate the Java code for this expression as a string. Return the 
	 * temporary variable associated to it and the Java expression as string
	   @param env
	   @return
	 */
	public Tuple2<String, String> genTmpVarJavaAsString(Env env) {
		PWCharArray pwChar = new PWCharArray();
		String tmpVar = genJavaExpr(pwChar, env);
		return new Tuple2<String, String>(tmpVar, pwChar.getGeneratedString().toString());
	}
	

	public StringBuffer getCodeThatReplacesThisExpr() {
		return codeThatReplacesThisExpr;
	}

	public void setCodeThatReplacesThisExpr(StringBuffer codeThatReplacesThisExpr) {
		this.codeThatReplacesThisExpr = codeThatReplacesThisExpr;
	}
	
	
	
	
	protected Type type;
}
