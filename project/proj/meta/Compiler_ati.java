package meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import ast.ASTVisitor;
import ast.CyanMetaobjectAnnotation;
import ast.CyanPackage;
import ast.ExprAnyLiteral;
import ast.GenericParameter;
import ast.InstanceVariableDec;
import ast.InterfaceDec;
import ast.ObjectDec;
import ast.ProgramUnit;
import error.ErrorKind;
import error.FileError;
import lexer.Symbol;
import saci.CompilationStep;
import saci.DirectoryPackage;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple4;
import saci.Tuple5;

public class Compiler_ati implements ICompiler_ati {
	
	public Compiler_ati(Env env) {
		this.env = env.clone();
	}

	@Override
	public void callVisitor(ASTVisitor visitor) {
		env.getProject().getProgram().accept(visitor);
	}
	
	
	@Override
	public ArrayList<IInstanceVariableDec_ati> getInstanceVariableList() {
		ObjectDec obj = env.getCurrentObjectDec();
		if ( obj == null || obj.getInstanceVariableList() == null ) {
			return null;
		}
		else {
			ArrayList<IInstanceVariableDec_ati> ret = new ArrayList<>();
			for ( InstanceVariableDec iv : obj.getInstanceVariableList() ) {
				ret.add(iv);
			}
			return ret;
		}
	}
	
	@Override
	public IInstanceVariableDec_ati searchInstanceVariable(String strParam) {
		ProgramUnit pu = env.getCurrentProgramUnit();
		if ( pu != null ) {
			if ( pu instanceof ObjectDec ) {
				ObjectDec objDec = (ObjectDec ) pu;
				InstanceVariableDec instVarDec = objDec.searchInstanceVariableDec(strParam);
				if ( instVarDec != null ) {
					return new InstanceVariableDec_ati(instVarDec);
				}
			}
		}
		return null;
	}

	@Override
	public String getUniqueInstanceVariableName(String packageName,
			String prototypeName) {

		Integer N = mapPackagePrototypeSelector.get(packageName + " " + prototypeName);
		if ( N == null ) {
			N = 0;
			mapPackagePrototypeSelector.put(packageName + " " + prototypeName, N);
		}
		mapPackagePrototypeSelector.put(packageName + " " + prototypeName, N+1);
		return "__id" + N;	
	}
	
	/**
	 * return a unique method name of <code>numberOfSelectors</code> selectors to
	 * prototype <code>prototypeName</code> of package <code>packageName</code>.
	 * The selectors start with "__". User identifiers cannot 
	 * start with two underscores.
	   @param numberOfSelectors
	   @param packageName
	   @param prototypeName
	   @return an array with the selectors of the method
	 */
	@Override
	public String []getUniqueMethodName(int numberOfSelectors,
			String packageName, String prototypeName) {

		Integer N = mapPackagePrototypeSelector.get(packageName + " " + prototypeName);
		if ( N == null ) {
			N = 0;
			mapPackagePrototypeSelector.put(packageName + " " + prototypeName, N);
		}
		int numberId = N;
		String []methodName = new String[numberOfSelectors];
		for ( int i = 0; i < numberOfSelectors; ++i ) {
			methodName[i] = "__s" + numberId++;
		}
		N += numberOfSelectors;
		mapPackagePrototypeSelector.put(packageName + " " + prototypeName, N);
		return methodName;
	}

	
	
	@Override
	public void error(Symbol sym, String specificMessage, String identifier,
			ErrorKind errorKind, String... furtherArgs) {
		env.error(true, sym, specificMessage, identifier, errorKind, furtherArgs);
	}
	
	@Override
	public void error(Symbol symbol, String message) {
		env.error(symbol, message);
	}
	
	
	@Override
	public Env getEnv() {
		return env;
	}

	@Override
	public ArrayList<ArrayList<String>> getGenericPrototypeArgListList() {
		ProgramUnit pu = env.getCurrentProgramUnit();
		if ( pu == null || pu.getGenericParameterListList() == null || pu.getGenericParameterListList().size() == 0 ) 
			return null;
		else {
			// current prototype is generic
			ArrayList<ArrayList<String>> strListList = new ArrayList<>();
			for ( ArrayList<GenericParameter> gpList: pu.getGenericParameterListList() ) {
				ArrayList<String> strList = new ArrayList<>();
				for ( GenericParameter gp: gpList ) {
					strList.add(gp.getParameter().asString());
				}
				strListList.add(strList);
			}
			return strListList;
		}
	}	
	
	@Override
	public String getCurrentPrototypeName() {
		if ( env.getCurrentProgramUnit() != null ) {
			return env.getCurrentProgramUnit().getName();
		}
		else
			return null;
	}
	
	@Override
	public Tuple2<Object, String> getProjectVariable(String variableName) {
		return this.env.getProject().getProjectVariable(variableName);
	}
	
	@Override
	public Set<String> getProjectVariableSet(String variableName) {
		return env.getProject().getProjectVariableSet(variableName);
	}

	@Override
	public boolean isCurrentProgramUnitInterface() {
		ProgramUnit pu = env.getCurrentProgramUnit();
		if ( pu != null && (pu instanceof InterfaceDec) )
			return true;
		else
			return false;
	}

	/**
	 * return the feature list of the current prototype, if there is one. Otherwise return null
	 */
	@Override
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		ProgramUnit pu = env.getCurrentProgramUnit();
		if ( pu != null ) {
			return pu.getFeatureList();
		}
		else
			return null;
	}

	
	
	@Override
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(
			String fileName,
			String extension,
			String packageName, 
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return env.getProject().getCompilerManager().
				readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
		
	}

	@Override
	public Tuple4<FileError, char[], String, String> readTextFileFromProject(
			String fileName,
			String extension,
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return env.getProject().getCompilerManager().readTextFileFromProject(fileName, extension, hiddenDirectory, numParameters, realParamList);
	}
	

	@Override
	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(String fileName, String packageName, DirectoryPackage hiddenDirectory) {
		return env.getProject().getCompilerManager().
				getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
	}
	
	@Override
	public Tuple2<FileError, byte[]> readBinaryDataFileFromPackage(String fileName, String packageName) {
		return env.getProject().getCompilerManager().
				readBinaryDataFileFromPackage(fileName, packageName);
		
	}
	
	@Override
	public boolean deleteDirOfTestDir(String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return env.getProject().getCompilerManager().deleteDirOfTestDir(dirName, testPackageName);
	}
	
	/**
	 * write 'data' to file 'fileName' that is created in the test directory of package packageName.
	 * Return an object of FileError indicating any errors.
	 */

	@Override
	public
	FileError  writeTestFileTo(StringBuffer data, String fileName, String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return env.getProject().getCompilerManager().
				writeTestFileTo(data, fileName, dirName, testPackageName);
	}

	@Override
	public String getPackageNameTest() {
		return env.getCurrentCompilationUnit().getPackageName() + NameServer.suffixTestPackageName;
	}

	@Override
	public CompilationStep getCompilationStep() {
		return env.getProject().getCompilerManager().getCompilationStep();
	}
	
	@Override
	public ProgramUnit getProgramUnit() {
		return env.getCurrentProgramUnit();
	}

	@Override
	public FileError writeTextFile(char[] charArray, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return env.getProject().getCompilerManager().writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
	}
	
	@Override
	public FileError writeTextFile(
			String str,
			String fileName,
			String prototypeFileName,
			String packageName, 
			DirectoryPackage hiddenDirectory) {
		return env.getProject().getCompilerManager().writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
	}

	
	@Override
	public String getPathFileHiddenDirectory(String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return env.getProject().getCompilerManager().getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
	}
	
	@Override
	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
		env.errorInsideMetaobjectAnnotation(metaobjectAnnotation, lineNumber, columnNumber, message);
	}
	

	
	private Env env;

	static {
		mapPackagePrototypeSelector = new HashMap<>();
	}
	private static HashMap<String, Integer> mapPackagePrototypeSelector;
	
}
