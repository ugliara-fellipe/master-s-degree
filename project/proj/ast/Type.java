/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import meta.IType;
import saci.Env;
import saci.NameServer;

/** Represents a type of a variable, parameter, return type, or expression.
 *  It may be a Cyan prototype or a Java class. 
 *  The subclasses of Type represent basic types and objects. An object
 *  also represents a type since one can be used as the type of a
 *  variable, parameter, etc:
 *        :p  Person; // Person is an object
 *  An instantiation of a generic object is also a type:
 *        :s Stack<Person>;
 *
 * @author José
 *
 */
abstract public class Type implements IType {

	public static Type Byte = null;
	public static Type Short = null;
	public static Type Int = null;
	public static Type Long = null;
	public static Type Float = null;
	public static Type Double = null;
	public static Type Char = null;
	public static Type Boolean = null;
	public static Type CySymbol = null;
	public static Type String = null;
	public static Type Nil = null;
	public static Type Any = null;
	public static ProgramUnit IMapName;
	public static ProgramUnit ISetName;
	/**
	 * this is the type of an message send with backquote like
	 *      person `str
	 * This type is compatible with any other type. The compiler should
	 * never issue an error when comparing this type with any other
	 */
	final public static Type Dyn = new TypeDynamic();

	/**
	 * returns the name of this type
	 */
	@Override
	abstract public String getName();
	
	/**
	 * returns the full name of this type, including its package
	 */
	@Override
	abstract public String getFullName();
	
	/**
	 * returns the full name of this type, including its package. If it is
	 * a generic prototype, it includes the packages of each of the
	 * parameters.
	 */
	abstract public String getFullName(Env env);	
	/**
	 * Returns the unique Java name associated to this type. An object
	 * Person should have Java name "_Person". But object Stack<Int>
	 * should have a Java  _Stack_LT_GP_CyInt_GT. See
	 * this method in subclasses of Type. If the type is already a Java class,
	 * this method returns the name of the class (of course).
	 * @return
	 */
	abstract public String getJavaName();
	
	/**
	 * returns the method signature of this prototype with name methodName.
	 * The signatures of all methods found are returned. 
	 * The searches includes private, public, and protected methods of
	 * this prototype and public and protected methods of super-prototypes.
	 * @param methodName
	 * @return
	 */
	abstract public ArrayList<MethodSignature> searchMethodPrivateProtectedPublicSuperProtectedPublic(String methodName, Env env);
	
	/**
	 * returns the method signatures of this type with name methodName.
	 * The searches includes only public methods of
	 * this type and public methods of super-types, if the
	 * method is not found in this type.
	 * @param methodName
	 * @return
	 */
	abstract public ArrayList<MethodSignature> searchMethodPublicSuperPublic(String methodName, Env env);	
	
	/**
	 * searches for a method called methodName in this type and all its super-types. 
	 * Public and protected methods are considered. The signatures of all methods with name "methodName"
	 * are returned.
	 * 
	 * @param methodName
	 * @param env
	 * @return
	 */
	abstract public ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublic(String methodName, Env env);
	
	abstract public boolean getIsFinal();
	
	/**
	 * return true if this type is supertype of 'other'
	 */
	abstract public boolean isSupertypeOf(Type other, Env env);
	
	/**
	 * return the type as an expr which may be ExprIdentStar or ExprGenericPrototypeInstantiation.
	 *  <code>seed</code> is used to create new Symbols with the same line number as <code>seed</code>
	 */
	public Expr asExpr(Symbol sym) {
		return NameServer.stringToExprIdentStar(this.getFullName(), sym);
	}
	
	@Override
	public boolean isSupertypeOf(IType other, Env env) {
		if ( !(other instanceof Type) ) {
			env.error(null, "Internal error: found an IType that is not instance of Type", true, true);
			return false;
		}
		else {
			Type otherType = (Type ) other;
			return this.isSupertypeOf(otherType, env);
		}
	}
	
	/**
	 * return true if the receiver is an inner prototype of Cyan. Such prototypes are created for anonymous functions of the outer prototype
	 */
	public boolean isInnerPrototype() { return false; }
	
}
