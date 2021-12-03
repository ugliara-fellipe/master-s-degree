/**
 *
 */
package ast;

import java.util.ArrayList;
import saci.Env;

// Philippe: se achar melhor, crie uma subclasse para cada tipo básico.

/**
 * Represents a basic Java type such as int or float.
 * There is a static public variable for each basic type.
 * @author José
 *
 */
public class TypeJavaBasic extends TypeJava {

	public static TypeJavaBasic intTypeJavaBasic = new TypeJavaBasic("java.lang", "int");

	public TypeJavaBasic(String packageName, String name) {
		this.packageName = packageName;
		this.name = name;
	}
	@Override
	public String getJavaPackage() {
		return packageName;
	}

	@Override
	public boolean isSupertypeOf(Type otherClass, Env env) {
		return false;
	}

	@Override
	public String getFullName() {
		return packageName + "." + getName();
	}
	
	@Override
	public java.lang.String getFullName(Env env) {
		return getFullName();
	}
	

	@Override
	public String getName() {
		return name;
	}

	public String getPackageName() {
		return this.packageName;
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
		return null;
	}
	@Override
	public boolean getIsFinal() {
		// TODO Auto-generated method stub
		return false;
	}

	
	private java.lang.String packageName;
	private String name;
	
}
