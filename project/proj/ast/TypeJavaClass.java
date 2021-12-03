/**
 *
 */
package ast;

import java.util.ArrayList;
import saci.Env;

/** Represents a Java class imported in one of Cyan source files.
 *
 *
 * @author José
 *
 */
public class TypeJavaClass extends TypeJavaRef {

	public TypeJavaClass(Class<? extends Object> aClass) {
		super(aClass);
	}


	@Override
	public boolean checkMethod(String methodName,  Expr ... realParameterList) {
		return false;
	}

	/**
	 * Return true if this is a supertype of otherClass
	 * Comment: only classes can be supertypes of classes
	 *
	 * @param other
	 * @return
	 */
	@Override
	public boolean isSupertypeOf(Type other, Env env) {
		if ( other instanceof TypeJavaClass ) {
			Class<?> c = ((TypeJavaClass ) other).aClass;
			while ( c != null ) {
				if ( c.equals(aClass) )
					return true;
				c = c.getSuperclass();
			}}

		return false;

	}


	@Override
	public ArrayList<MethodSignature> searchMethodPrivateProtectedPublicSuperProtectedPublic(
			java.lang.String methodName, Env env) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<MethodSignature> searchMethodPublicSuperPublic(
			java.lang.String methodName, Env env) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublic(
			java.lang.String methodName, Env env) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean getIsFinal() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public java.lang.String getFullName(Env env) {
		return this.getFullName();
	}

}
