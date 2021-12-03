/**
 *
 */
package ast;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import saci.JavaEnv;


/** Represents a Java method. It does not support generic methods or methods
 * with generic parameters or return value. The annotations are not considered
 * too.
 *
 * @author José
 *
 */
public class MethodJava {

	public MethodJava(Method aMethod) {
		this.aMethod = aMethod;
		this.returnType = null;
		this.parameterTypeArray = null;
		this.exceptionTypeArray = null;
		this.declaringJavaClass = null;
	}

	public String getName() {
		return aMethod.getName();
	}
	public Method getMethod() {
		return aMethod;
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(aMethod.getModifiers());
	}

	public boolean isPublic() {
		return Modifier.isPublic(aMethod.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(aMethod.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(aMethod.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(aMethod.getModifiers());
	}

	public boolean isStatic() {
		return Modifier.isStatic(aMethod.getModifiers());
	}

	public TypeJavaClass getReturnType(JavaEnv je) {
		if ( returnType == null ) {
			returnType = je.getTypeJavaClass(aMethod.getReturnType());
		}
		return returnType;
	}

	public TypeJavaClass getDeclaringClass(JavaEnv je) {
		if ( declaringJavaClass == null ) {
			declaringJavaClass = je.getTypeJavaClass(aMethod.getDeclaringClass());
		}
		return declaringJavaClass;
	}

	public TypeJavaClass[] getParameterTypeArray(JavaEnv je) {
		Class<?> paramType[] = aMethod.getParameterTypes();
		parameterTypeArray = new TypeJavaClass[paramType.length];
		int i = 0;
		for ( Class<?> c : paramType ) {
			parameterTypeArray[i] = je.getTypeJavaClass(c);
			++i;
		}
		return parameterTypeArray;
	}

	public TypeJavaClass[] getExceptionTypeArray(JavaEnv je) {
		Class<?> exceptionTypes[] = aMethod.getExceptionTypes();
		exceptionTypeArray = new TypeJavaClass[exceptionTypes.length];
		int i = 0;
		for ( Class<?> c : exceptionTypes ) {
			exceptionTypeArray[i] = je.getTypeJavaClass(c);
			++i;
		}
		return exceptionTypeArray;
	}

	private Method aMethod;
	/**
	 * return type of the method
	 */
	private TypeJavaClass returnType;
	/**
	 * formal parameter types of the method
	 */
	private TypeJavaClass[] parameterTypeArray;
	/**
	 * exception types that can be thrown by this method
	 */
	private TypeJavaClass[] exceptionTypeArray;
	/**
	 * declaring class of this method
	 */
	private TypeJavaClass declaringJavaClass;

}
