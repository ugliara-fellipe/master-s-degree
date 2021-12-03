package meta;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.ExprAnyLiteral;
import ast.ProgramUnit;
import error.ErrorKind;
import lexer.Symbol;
import saci.CompilationStep;
import saci.Tuple2;

/**
 * An interface of the compiler as seen by metaobjects in the phase "after typing interfaces"
   @author José
 */
public interface ICompiler_ati extends IAbstractCyanCompiler {


	/**
	 * return a list of the instance variables of the current prototype. Null if 
	 * the metaobject is outside the prototype or if there is no instance variables.
	 * Note that if the metaobject is before the prototype the result is null: <br>
	 * <code>
	 * {@literal @}myMetaobject <br>
	 * object Test<br>
	 *     String testName<br>
	 * end<br>
	 * </code>
	 * In this example, if this method is called in myMetaobject it will return null.
	 * This may change in the future.
	   @return
	 */
	ArrayList<IInstanceVariableDec_ati> getInstanceVariableList();
	
	IInstanceVariableDec_ati searchInstanceVariable(String strParam);

	
	/**
	 * Create and returns a unique name for an instance variableof 
	 * prototype prototypeName of package packageName. 
	 * 
	   @param packageName  
	   @param prototypeName
	   @return the unique name such as "id1011" 
	 */	
	String getUniqueInstanceVariableName(String packageName, String prototypeName);
	/**
	 * Create and returns a unique method name for prototype prototypeName of package packageName
	 * 
	   @param numberOfSelectors number of selectors of the method
	   @param packageName  
	   @param prototypeName
	   @return an array with the selectors of the unique method name 
	 */
	String []getUniqueMethodName(int numberOfSelectors, String packageName, String prototypeName);
	
	/**
	 * signal an error described by specificMessage at symbol sym. errorKind is a constant that describes this errors.
	   @param sym the symbol of the declaration or statement in which the error is 
	   @param specificMessage an error message
	   @param identifier is the name of the variable or method that is associated to the error 
	   @param errorKind see class {@link ErrorKind} for a list of error constants
	   @param furtherArgs other arguments that describe this error
	 */
	void error(Symbol sym, String specificMessage, String identifier, ErrorKind errorKind, String ...furtherArgs);

	void error(Symbol sym, String message);
	/**
	 * the environment gives access to the data of the current compilation
	 * 
	   @return the environment of the compilation
	 */
			
	/**
	 * return a list with all arguments of the current generic prototype. If
	 * the current prototype is not generic, return null
	   @return
	 */
	ArrayList<ArrayList<String>> getGenericPrototypeArgListList();
	/**
	 * return the name of the current prototype. If the current symbol is outside a prototype, return null.
	 * This method cannot be called by a metaobject that is inside the prototype name as <code>{@literal @}wrong</code> in <br>
	 * <code>
	 * class G{@literal <}Int, {@literal @}wrong String{@literal >} <br>
	 * end<br>
	 * </code> 
	 */
	String getCurrentPrototypeName();
	/**
	 * return true if the current prototype is an interface. If getCurrentPrototypeName() returns null, this returns false
	   @return
	 */
	boolean isCurrentProgramUnitInterface();
	
	IEnv_ati getEnv();
	
	void callVisitor( ASTVisitor visitor );
	
	/**
	 * return the feature list of the current prototype, if there is one. Otherwise return null
	 */
	
	ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList();
	
	CompilationStep getCompilationStep();

	ProgramUnit getProgramUnit();
	
}

