package meta;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.CyanPackage;
import ast.ExprAnyLiteral;
import saci.Tuple2;

public interface ICompilerPackageView_ati {
	void callVisitor( ASTVisitor visitor );
	CyanPackage getPackage();
	/**
	 * return the feature list of the current package
	 */
	
	ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList();
	
}
