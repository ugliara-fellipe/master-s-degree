/**
 *
 */
package ast;

/**
 * This class represents a Java class which may be:
 * 1. a class, represented by objects of TypeJavaClass
 * 2. an interface, represented by objects of TypeJavaInterface
 * 3. a basic type such as int, float, etc. These are
 *    represented by objects of class TypeJavaBasic
 *
 * @author José
 *
 */
abstract public class TypeJava extends Type {
	@Override
	abstract public String getName();
	abstract public String getJavaPackage();
	/**
	 * the unique name of the type which is equal to the
	 * Java type
	 * @return
	 */
	@Override
	public String getJavaName() {
		return getName();
	}
}
