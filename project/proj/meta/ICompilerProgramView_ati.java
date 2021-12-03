package meta;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.ExprAnyLiteral;
import ast.Program;
import saci.Tuple2;

public interface ICompilerProgramView_ati {
	void callVisitor( ASTVisitor visitor );
	Program getProgram();
	/**
	 * return the feature list of the program
	 */
	
	ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList();

}
