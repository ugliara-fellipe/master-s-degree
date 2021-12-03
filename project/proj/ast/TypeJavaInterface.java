/**
 *
 */
package ast;

import java.util.ArrayList;
import saci.Env;

/**
 * Describes a Java interface
 * @author José
 *
 */
public class TypeJavaInterface extends TypeJavaRef {

	public TypeJavaInterface(Class<? extends Object> aClass) {
		super(aClass);
	}

	// Philippe: implemente este método
	// Veja a descrição dele em TypeJavaRef
	// note que uma interface pode ser supertipo de interfaces e
	// classes

	/**
	 * Return true if this is a supertype of otherInterface
	 * @param otherInterface
	 * @return
	 */
	@Override
	public boolean isSupertypeOf(Type other, Env env) {
		return false;
	}

	// Philippe: implemente este método. Veja a descrição dele
	// em TypeJavaRef
	@Override
	public boolean checkMethod(String methodName, Expr... realParameterList) {
		// TODO Auto-generated method stub
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
