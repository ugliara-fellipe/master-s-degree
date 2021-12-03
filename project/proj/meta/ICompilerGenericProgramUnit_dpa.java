package meta;

import ast.ASTVisitor;
import ast.CompilationUnit;
import ast.Program;
import ast.ProgramUnit;
import lexer.Symbol;
import saci.CompilationStep;

public interface ICompilerGenericProgramUnit_dpa extends IAbstractCyanCompiler {

	void callVisitor( ASTVisitor visitor );
	Program getProgram();
	ProgramUnit getProgramUnit();
	void error(Symbol symbol, String message);
	void errorAtGenericPrototypeInstantiation(String errorMessage);
	CompilationStep getCompilationStep();
	CompilationUnit getCompilationUnit();
	
	/*
	 * add to a list of metaobject annotations. Method {@link IListAfter_ati#after_ati_action(ICompiler_ati compiler)} 
	 * of the metaobject will be called after the next ati phase
	 */
	void addToListAfter_ati(IListAfter_ati annotation);

}
