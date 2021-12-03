/**
 *
 */
package ast;

import java.util.ArrayList;
import saci.Env;

/** when the Compiler finds an error while compiling a type, it may return
 * an object of TypeNonType to mean "there was an error and I don´t know
 * what to return".
 * @author José
 *
 */
public class TypeNonType extends Type {

	@Override
	public String getJavaName() {
		return "This should not have been printed. See class TypeNonType of package ast";
	}

	@Override
	public String getFullName() {
		return getName();
	}


	@Override
	public java.lang.String getFullName(Env env) {
		return getName();
	}

	
	@Override
	public java.lang.String getName() {
		return "NonType";
	}

	@Override
	public ArrayList<MethodSignature> searchMethodPrivateProtectedPublicSuperProtectedPublic(
			java.lang.String methodName, Env env) {
		return null;
	}

	@Override
	public ArrayList<MethodSignature> searchMethodPublicSuperPublic(
			java.lang.String methodName, Env env) {
		return null;
	}

	@Override
	public boolean isSupertypeOf(Type other, Env env) {
		return true;
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

}
