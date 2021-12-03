package ast;

import saci.Env;

/**
 * This class represents a Java class or interface which may be:
 * 1. a class, represented by objects of TypeJavaClass
 * 2. an interface, represented by objects of TypeJavaInterface
 *
 * Of course, TypeJavaRef is a superclass of both TypeJavaClass
 * and TypeJavaInterface
 *  @author José
 *
 */
public abstract class TypeJavaRef extends TypeJava {

	public TypeJavaRef(Class<? extends Object> aClass) {
		this.aClass = aClass;
	}

	@Override
	public String getFullName() {
		return getPackageName() + "." + getName();
	}

	@Override
	public String getName() {
		return aClass.getName();
	}

	public String getPackageName() {
		return aClass.getPackage().getName();
	}

	/**
	 * The source code being compiled has a message send
	 *        obj methodName: e1, e2, ... en
	 * If the declared type of obj is a Java class represented by
	 * aJavaClass, the call
	 *      aJavaClass.checkMethod(methodName, e1, e2, ... en)
	 * returns true if the Java class represented by aJavaClass
	 * declares a method called methodName that accepts as parameters
	 * objects e1, e2, ... en. Of course, the declared type of ei
	 * should either be a Java class or should be a basic type such
	 * as Int, Char, Float, etc. Method checkMethod assumes that
	 * a basic Java type such as int is compatible with the Cyan type
	 * Int. A future improvement in Cyan will be to allow the passing
	 * of Cyan objects to Java code. That will not take long to happen.
	 *
	 *
	 * @param methodName, the name of the method
	 * @param realParameterList, the parameter types of the
	 * @return
	 */
	abstract public boolean checkMethod(String methodName, Expr ... realParameterList);



	@Override
	public String getJavaPackage() {
		return aClass.getPackage().getName();
	}

	/**
	 * return true if this is a supertype of otherClass.
	 */

	@Override
	abstract public boolean isSupertypeOf(Type otherClass, Env env);


	/**
	 * two objects of TypeJavaRef are equal, that is, two classes or interfaces
	 * are considered equal if the have the same name and belong to
	 * the same package.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof TypeJavaRef) )
			return false;
		else {
			TypeJavaRef other = (TypeJavaRef ) obj;
			return aClass.getName().compareTo(other.getName()) == 0 &&
			       aClass.getPackage().getName().compareTo(
			    		   other.getClass().getPackage().getName()) == 0;
		}
	}

	public Class<?> getJavaClass() { return aClass; }

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * the information on the class.
	 */
	protected Class<?> aClass;

}