/**
  
 */
package ast;

import java.util.ArrayList;
import saci.Env;
import saci.NameServer;

/**	
	 * this is the type of an message send with backquote like
	 *      person `str
	 * This type is compatible with any other type. The compiler should
	 * never issue an error when comparing this type with any other
   @author José
   
 */
public class TypeDynamic extends Type {

	@Override
	public java.lang.String getName() {
		return NameServer.dynName;
	}

	@Override
	public java.lang.String getJavaName() {
		return NameServer.javaDynName;
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
	public java.lang.String getFullName() {
		return NameServer.dynName;
	}

	@Override
	public ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublic(
			java.lang.String methodName, Env env) {
		return null;
	}

	@Override
	public boolean getIsFinal() {
		return false;
	}

	@Override
	public String getFullName(Env env) {
		return getFullName();
	}

}
