/**
 *
 */
package saci;

import java.util.Hashtable;
import ast.TypeJavaClass;

/**
 * This class has a hashtable
 *  containing all loaded Java classes (objects of Class<? extends Object>).
 *  Only one JavaEnv object is created for the whole program.
 *  It should be passed to TypeJavaClass and
 *  MethodJava to load every Java class that is needed. If a class
 *  "A" is needed (as a parameter of an existing method, an inheritance,
 *  a return value, etc), then JavaEnv should provide a reference
 *  to an object of Class<...> corresponding to "A". If this class
 *  has not been loaded, it should be. Then JavaEnv should know
 *  where to load classes.
 * @author José
 *
 */
public class JavaEnv {

	public JavaEnv() {
		classTypeJavaClassTable = new Hashtable<Class<?>, TypeJavaClass>();
	}

	/**
	 * create a TypeJavaClass object on demand.
	 * @param aClass
	 * @return
	 */
	public TypeJavaClass getTypeJavaClass(Class<?> aClass) {
		TypeJavaClass ret = this.classTypeJavaClassTable.get(aClass);
		if ( ret == null ) {
			// TypeJavaClass does not exist in the table, create one
			ret = new TypeJavaClass(aClass);
			this.classTypeJavaClassTable.put(aClass, ret);
		}
		return ret;
	}

	/**
	 * table containing pairs of Class<?> and TypeJavaClass object. To each
	 * Class<?> object retrived from a Java class loaded from the disk (load
	 * the .class file then use getClass to obtain a Class<?> object) the
	 * JavaEnv class creates a corresponding TypeJavaClass object that
	 * represents a Java class in the Cyan Compiler. The pair is inserted
	 * into the hashtable.
	 */
	private Hashtable<Class<?>, TypeJavaClass> classTypeJavaClassTable;

}
