package meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import ast.CompilationUnitSuper;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.ExprAnyLiteral;
import ast.ExprIdentStar;
import ast.ExprMessageSend;
import ast.InstanceVariableDec;
import ast.ProgramUnit;
import ast.Type;
import ast.VariableDecInterface;
import error.ErrorKind;
import lexer.Symbol;
import saci.CompilationStep;
import saci.Env;
import saci.Tuple2;

/**
 * 
   @author José
 */
public interface ICompiler_dsa extends IAbstractCyanCompiler  {
	
	boolean isInPackageCyanLang(String name);
	
	ProgramUnit searchProgramUnit(String packageName, String prototypeName);
	CyanPackage searchPackage(String packageName);
	
	InstanceVariableDec searchInstanceVariable(String strParam);
	/**
	 * search for local variable <code>varName</code>. Return null if not found.
	   @param varName
	   @return
	 */
	VariableDecInterface searchLocalVariable(String varName);
	/**
	 * search for local variable or parameter <code>varName</code>. Return null if not found.
	   @param varName
	   @return
	 */
	VariableDecInterface searchLocalVariableParameter(String varName);
	/**
	 * search for parameter <code>varName</code>. Return null if not found. The parameter may 
	 * be of the method or any function that is in the scope.
	   @param varName
	   @return
	 */
	
	VariableDecInterface searchParameter(String varName);
	
	/**
	 * return the list of all unary methods declared in the current prototype. Returns null if 
	 * the metaobject annotation is not inside a prototype
	 */
	ArrayList<String> getUnaryMethodNameList();
	/**
	 * signal an error
	   @param sym
	   @param specificMessage
	   @param identifier
	   @param errorKind
	   @param furtherArgs
	 */
	
	void error(Symbol sym, String specificMessage, String identifier, ErrorKind errorKind, String ...furtherArgs);
	void error(Symbol sym, String message);
	/**
	 * line number is 1 if the error is in the line of the metaobject annotation:<br>
	 * {@literal @}myDSL{* // line 1<br>   
	 *      ...  // line 2 <br>  
	 *      ...  // line 3 <br>
	 *      *}<br>
	 * 
	   @param lineNumber
	   @param message
	 */
	void error(int lineNumber, int columnNumber, String message);

	/**
	 * the environment gives access to the data of the current compilation
	 * 
	   @return the environment of the compilation
	 */
			
	Env getEnv();
	/**
	 * return the column number of the metaobject annotation
	   @return
	 */
	int getColumnNumberCyanMetaobjectAnnotation();
	/**
	 * return the line number of the metaobject annotation
	   @return
	 */
	int getLineNumberCyanMetaobjectAnnotation();

	/**
	 * signs an error at the site of the generic prototype instantiation. That is, 
	 * there should be an error in an generic prototype and the error is shown to the
	 * compiler user in the  generic prototype instantiation which is something like<br>
	 * <code> var SortedList<Person> personList;</code><br>
	 * The error, in this example, could be "Person' does not support the comparison methods such as '<=>'"
	   @param errorMessage
	 */
	void errorAtGenericPrototypeInstantiation(String errorMessage);

	/**
	 * return the feature list of the current prototype, if there is one. Otherwise return null
	 */

	ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList();

	CompilationStep getCompilationStep();

	
	/**
	 * remove the code of message send exprMessageSend and replace it by codeToAdd. 
	 * This is being asked by metaobject annotation annotation.  The type of the expression codeToAdd is
	 * codeType.
	 */

	boolean removeAddCodeExprMessageSend(ExprMessageSend exprMessageSend, CyanMetaobjectWithAtAnnotation annotation,
			StringBuffer codeToAdd, Type codeType);

	/**
	 * remove the code of message send exprMessageSend and replace it by codeToAdd. 
	 * This is being asked by metaobject annotation annotation. The type of the expression codeToAdd is
	 * codeType.  Return false if the replacement was
	 * not possible. In particular, if unaryMessageSend is not a message send, false is returned.
	 */
	boolean removeAddCodeExprIdentStar(ExprIdentStar unaryMessageSend, CyanMetaobjectWithAtAnnotation annotation,
			StringBuffer codeToAdd, Type codeType);	
	/**
	 * return a set of all subtype of each program prototype or interface. 
	 * This list is only created on demand. The key to this map is the package name, space, prototype name. 
	 * It can be, for example,<br>
	 * <code>
	 * "br.main Program"
	 * </code><br>
	 * The package name is "br.main" and the prototype name is "Program". 
	 * 
	 */
	
	HashMap<String, Set<ProgramUnit>> getMapPrototypeSubtypeList();
	

	/**
	 * create a new prototype in phase dsa of the compilation. This method should be used if 
	 * code is produced in this phase that uses a generic prototype that does not appear in 
	 * previous code. For example, suppose some metaobject in phase DSA produces the anonymous function<br>
	 * <code>
	 *     { (: Float a, Double b :) ^a ++ " " ++ bb }<br>
	 * </code><br>
	 * There will be an error in step 7 of the compilation if this function does not appear
	 * in previous compilation steps: the compiler cannot create a new generic prototype instantiation
	 * in this step. Then you should use the method below to create, in step 6, phase DSA, a
	 * instantiation <br>
	 * <code>     Function{@literal <}Float, Double, String></code><br>
	 * @return 
	 */
	Type createNewGenericPrototype(Symbol symUsedInError, CompilationUnitSuper compUnit, ProgramUnit currentPU,
			String fullPrototypeName, String errorMessage);


	/**
	 * return a list with all arguments of the current generic prototype. If
	 * the current prototype is not generic, return null
	   @return
	 */	
	public ArrayList<ArrayList<String>> getGenericPrototypeArgListList();	
}
