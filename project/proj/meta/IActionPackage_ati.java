package meta;

import java.util.ArrayList;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple6;

/**
 * This interface should be implemented by all metaobjects that should add code at phase ati and that should
 * be attached to a package in the .pyan project file.
   @author José
 */


public interface IActionPackage_ati {

	/**
	 * add code to a specific prototype. The tuple is composed by a prototype name and the code to be added, in this order. 
	 * @param compiler 
	 */
	default ArrayList<Tuple2<String, StringBuffer>> ati_CodeToAdd(ICompilerPackageView_ati compiler) { return null; }
	

	/**
	 * list of methods to add to the specific prototypes of the program. Each tuple is composed by  
	 * a prototype name, a method name, and a method code, in this order. The code of the method should
	 * be added to the given prototype of the given package.
	 * @param compiler 
	 */
	default ArrayList<Tuple3<String, String, StringBuffer>> ati_methodCodeList(
			ICompilerPackageView_ati compiler) { return null; }
	
	/**
	 * list of statements to add to specific methods of the program. Each tuple is composed by  
	 * a prototype name, a method name, and code of statements, in this order. The statements should
	 * be added to all methods with that name in the prototype. The name means "selectors and number of parameters".
	 * Then a name could be <code>with:2 do:1</code>. 
	 * @param compiler 
	 */
	default ArrayList<Tuple3<String, String, StringBuffer>> ati_beforeMethodCodeList(
			ICompilerPackageView_ati compiler) { return null; }

	// 	boolean addBeforeMethod(CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName, String methodName, StringBuffer statementCode);

	/**
	 * list of instance variables to add to specific prototypes of the program. 
	 * Each tuple is composed by  
	 * a prototype name, a boolean value true if this variable is public, true if this variable is shared,
	 * true if this variable is read-only (declared with 'let'), a type name, and an instance variable name, in this order. 
	 * The instance variable should be added to the given prototype of the current package
	 * @param compiler 
	 */
	default ArrayList<Tuple6<String, Boolean, Boolean, Boolean, String, String>> ati_instanceVariableList(
			ICompilerPackageView_ati compiler) { return null; }

	/**
	 * this method should return a list of tuples. Each tuple is composed by  
	 * a prototype name and the code of the compilation unit in which the prototype is ---
	 * the full text of the file. 
	 * The compiler will create this prototype in the current package. 
	 * @param compiler 
	 */
	default ArrayList<Tuple2<String, StringBuffer>> ati_NewPrototypeList(
			ICompilerPackageView_ati compilerPackageView_ati) { return null; }	
}
