package meta;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.CyanPackage;
import ast.ExprAnyLiteral;
import saci.Env;
import saci.Tuple2;

public class CompilerPackageView_ati implements ICompilerPackageView_ati {

	public CompilerPackageView_ati(Env env) {
		this.env = env;
	}

	@Override
	public void callVisitor(ASTVisitor visitor) {
		visitor.visit(this.thePackage);
	}

	@Override
	public CyanPackage getPackage() {
		return this.thePackage;
	}
	public void setPackage(CyanPackage thePackage) {
		this.thePackage = thePackage;
	}
	
	

	public Env getEnv() {
		return env;
	}
	
	@Override
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		return thePackage.getFeatureList();
	}
	
	
	private Env env;

	private CyanPackage thePackage;


	
}
