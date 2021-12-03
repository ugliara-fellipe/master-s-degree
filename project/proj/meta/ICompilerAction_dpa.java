package meta;

import java.util.ArrayList;
import ast.CompilationUnit;
import ast.ExprAnyLiteral;
import ast.MethodDec;
import ast.ProgramUnit;
import lexer.Symbol;
import saci.CompilationStep;
import saci.Compiler;
import saci.Tuple2;

public interface ICompilerAction_dpa extends IAbstractCyanCompiler {



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
	 * return the name of the current prototype without any generic parameters. That is, if the prototype
	 * is {@code "Int"}, the value returned is {@code "Int"}. If the prototype is {@code "Hashtable<String, Int>"},
	 * the value returned is {@code "Hashtable"}.
	 * 	
    */
	String getCurrentPrototypeId();
	/**
	 * get the name of the current method
	 */
	MethodDec getCurrentMethod();
	
	/**
	 * return the feature list of the current prototype, if there is one. Otherwise return null
	 */
	
	ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList();
	CompilationStep getCompilationStep();
	/**
	 * return the current compilation unit, if there is one
	   @return
	 */
	
	CompilationUnit getCompilationUnit();
	
	/**
	 * return the text between offsetLeftCharSeq and offsetRightCharSeq - 1. The last character
	 * is followed by '\0'
	 */
	char[] getText(int offsetLeftCharSeq, int offsetRightCharSeq);
	ProgramUnit searchPackagePrototype(String packageNameInstantiation, String prototypeNameInstantiation);
	void errorAtGenericPrototypeInstantiation(String errorMessage);
	String getPackageNameInstantiation();
	void setPackageNameInstantiation(String packageNameInstantiation);
	String getPrototypeNameInstantiation();
	void setPrototypeNameInstantiation(String prototypeNameInstantiation);
	int getLineNumberInstantiation();
	void setLineNumberInstantiation(int lineNumberInstantiation);
	int getColumnNumberInstantiation();
	void setColumnNumberInstantiation(int columnNumberInstantiation);
	Compiler getCompiler();

	
	void error(int lineNumber, String message);
	void error(Symbol sym, String message);
	
	
}
