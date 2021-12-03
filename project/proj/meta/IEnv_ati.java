package meta;

import java.util.ArrayList;
import java.util.Set;
import ast.CompilationUnit;
import ast.CyanPackage;
import ast.MethodSignature;
import ast.ObjectDec;
import ast.ProgramUnit;
import lexer.Symbol;
import saci.Env;

public interface IEnv_ati {

	ObjectDec getCurrentObjectDec();
	ProgramUnit searchPackagePrototype(String packagePrototypeName, Symbol symUsedInError); 
	default ArrayList<MethodSignature> searchMethodProtectedPublicSuperProtectedPublic(ObjectDec objDec, String methodName) {
		return objDec.searchMethodProtectedPublicSuperProtectedPublic(methodName, (Env) this);
	}
	default ArrayList<MethodSignature> searchMethodPublicSuperPublic(ProgramUnit pu, String methodName) {
		return pu.searchMethodPublicSuperPublic( methodName, (Env ) this );
	}
	
	CompilationUnit getCurrentCompilationUnit();
	ProgramUnit getCurrentProgramUnit();
	Set<CyanPackage> getImportedPackageSet();
}
