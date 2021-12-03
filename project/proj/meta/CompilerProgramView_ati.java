package meta;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.ExprAnyLiteral;
import ast.Program;
import saci.Env;
import saci.Tuple2;

public class CompilerProgramView_ati implements ICompilerProgramView_ati {

	public CompilerProgramView_ati(Env env) {
		this.env = env;
	}

	@Override
	public void callVisitor(ASTVisitor visitor) {
		env.getProject().getProgram().accept(visitor);
	}

	@Override
	public Program getProgram() {
		return env.getProject().getProgram();
	}
	
	

	public Env getEnv() {
		return env;
	}
	
	@Override
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		return env.getProject().getProgram().getFeatureList();
	}
	
	private Env env;
	
}
