
package meta;

import java.util.ArrayList;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple5;
import saci.Tuple6;

/**
 * This interface should be implemented by all metaobjects that should add code at phase ati and that should
 * be attached to a program unit or inside a program unit.
   @author José
 */
public interface IActionProgramUnit_ati {


	
	/**
	 * Return Cyan code to be added after the metaobject annotation after typing interfaces. If
	 * metaobject <code>foo</code> produces <code>i print;</code> (this method returns 
	 * <code>"i print;"</code> in the second tuple element)
	 * then the code
	   <code>
	   @foo
	   i = 1;
	   </code>
	   will be replaced by
	   <code>
	   @foo#ati
	   i print;
	   i = 1;
	   </code>	  
	
	   @param compiler_ati the compiler
	 */
	default StringBuffer ati_codeToAdd(
			ICompiler_ati compiler_ati) { return null; }	
	/**
	 * add code to a specific prototype. The tuple is composed by  
	 * a prototype name, and code to be added, in this order. 
	 * @param compiler 
	 */
	default ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
			ICompiler_ati compiler) { return null; }
	
	
	/**
	 * list of methods to add to the specific prototypes of the program. Each tuple is composed by  
	 * a prototype name, a method name, and a method code, in this order. The code of the method should
	 * be added to the given prototype of the given package.
	 * @param compiler 
	 */
	default ArrayList<Tuple3<String, String, StringBuffer>> ati_methodCodeList(
			ICompiler_ati compiler) { return null; }

	/**
	 * list of methods to add to the prototype in which the metaobject annotation is
	 * Each tuple is composed by a method name and a method code, in this order. 
	 * @param compiler 
	 */
	default ArrayList<Tuple2<String, StringBuffer>> ati_methodCodeListThisPrototype(
			ICompiler_ati compiler) { return null; }
	
	/**
	 * list of statements to add to specific methods of the program. Each tuple is composed by  
	 * a prototype name, a method name, and code of statements, in this order. The statements should
	 * be added to all methods with that name in the prototype. The name means "selectors and number of parameters".
	 * Then a name could be <code>with:2 do:1</code>. 
	 * @param compiler 
	 */
	default ArrayList<Tuple3<String, String, StringBuffer>> ati_beforeMethodCodeList(
			ICompiler_ati compiler) { return null; }

	// 	boolean addBeforeMethod(CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName, String methodName, StringBuffer statementCode);

	/**
	 * list of instance variables to add to specific prototypes of the program. 
	 * Each tuple is composed by  
	 * a prototype name, a boolean value true if this variable is public, true if this variable is shared,
	 * true if this variable is read only, a type name, and an instance variable name, in this order. 
	 * The instance variable should
	 * be added to the given prototype of the given package.
	 * @param compiler 
	 */
	default ArrayList<Tuple6<String, Boolean, Boolean, Boolean, String, String>> ati_instanceVariableList(
			ICompiler_ati compiler) { return null; }

	/**
	 * list of instance variables to add to the current prototype 
	 * Each tuple is composed by  a boolean value true if this variable is public, true if this variable is shared,
	 * true if this variable is read only, 
	 *  a type name and an instance variable name, in this order. 
	 * The instance variable should be added to the prototype in which the 
	 * metaobject annotation is.
	 * @param compiler 
	 */
	default ArrayList<Tuple5<Boolean, Boolean, Boolean, String, String>> ati_instanceVariableListThisPrototype(
			ICompiler_ati compiler) { return null; }
	
	

	
	/**
	 * this method should return a list of tuples. Each tuple is composed by  
	 * a prototype name and the code of the compilation unit in which the prototype is ---
	 * the full text of the file. 
	 * The compiler will create this prototype in the current package. 
	 * @param compiler_ati 
	 * 
	 */
	default ArrayList<Tuple2<String, StringBuffer>> ati_NewPrototypeList(
			ICompiler_ati compiler_ati) { return null; }	
	

	/**
	 * this method should return a list of tuples. Each tuple is composed by
	 * a prototype name, the old method name, 
	 * and an array with the selectors of the new method name. The method corresponding
	 * to the second tuple element is renamed according to the third tuple element.
	 * Of course, the number of the selectors should be the same 
	 * @param compiler_ati 
	 */
	default ArrayList<Tuple3<String, String, String []>> ati_renameMethod(
			ICompiler_ati compiler_ati) { return null; }	
	
}