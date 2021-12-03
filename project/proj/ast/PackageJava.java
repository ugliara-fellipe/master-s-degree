/**
 *
 */
package ast;

import java.util.ArrayList;

/**
 * This class represents a Java package, a store for classes and interfaces
 * @author José
 *
 */
public class PackageJava {

	private ArrayList<TypeJava> typeJavaList;

	public void setTypeJavaList(ArrayList<TypeJava> typeJavaList) {
		this.typeJavaList = typeJavaList;
	}

	public ArrayList<TypeJava> getTypeJavaList() {
		return typeJavaList;
	}

}
