package meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import ast.CompilationUnit;
import ast.CompilationUnitSuper;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Expr;
import ast.ExprAnyLiteral;
import ast.ExprIdentStar;
import ast.ExprMessageSend;
import ast.GenericParameter;
import ast.IdentStarKind;
import ast.InstanceVariableDec;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureUnary;
import ast.ObjectDec;
import ast.ParameterDec;
import ast.ProgramUnit;
import ast.Type;
import ast.VariableDecInterface;
import error.CompileErrorException;
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

public class Compiler_dsa implements ICompiler_dsa {


	public Compiler_dsa(Env env) {
		originalEnv = env;
		this.env = env.clone();
	}
	public Compiler_dsa(Env env, CyanMetaobjectAnnotation cyanMetaobjectAnnotation) {
		originalEnv = env;
		this.env = env.clone();
		this.cyanMetaobjectAnnotation = cyanMetaobjectAnnotation;
	}
	
	@Override
	public int getColumnNumberCyanMetaobjectAnnotation() {
		return this.cyanMetaobjectAnnotation.getFirstSymbol().getColumnNumber();
	}
	@Override
	public int getLineNumberCyanMetaobjectAnnotation() {
		return this.cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber();
	}	
	
	
	@Override
	public InstanceVariableDec searchInstanceVariable(String strParam) {
		return env.searchInstanceVariable(strParam);
	}
	
	@Override
	public VariableDecInterface searchLocalVariableParameter(String varName) {
		return env.searchLocalVariableParameter(varName);
	}

	@Override
	public VariableDecInterface searchLocalVariable(String varName) {
		VariableDecInterface v = env.searchLocalVariableParameter(varName);
		if ( v instanceof ParameterDec )
			return null;
		else
			return v;
	}

	@Override
	public VariableDecInterface searchParameter(String varName) {
		VariableDecInterface v = env.searchLocalVariableParameter(varName);
		if ( v instanceof ParameterDec )
			return v;
		else
			return null;	
	}
	
	@Override
	public CyanPackage searchPackage(String packageName) {
		return env.searchPackage(packageName);
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
	public void error(int lineNumber, int columnNumber, String message) {
		env.error(lineNumber,  columnNumber, message);
	}
	
	
	@Override
	public Env getEnv() {
		return env;
	}
	
	@Override
	public ProgramUnit searchProgramUnit(String packageName,
			String prototypeName) {
		return env.searchPackagePrototype(packageName, prototypeName);
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
	public ArrayList<String> getUnaryMethodNameList() {
		ObjectDec currentProto = env.getCurrentObjectDec();
		if ( currentProto == null ) 
			return null;
		else {
			ArrayList<String> ret = new ArrayList<String>();
			ArrayList<MethodDec> methodList = currentProto.getMethodDecList();
			for ( MethodDec methodDec : methodList ) {
				MethodSignature ms = methodDec.getMethodSignature();
				if ( ms instanceof MethodSignatureUnary ) {
					ret.add(  ((MethodSignatureUnary ) ms).getName() );
				}
			}
			return ret;
		}
	}

	@Override
	public boolean isInPackageCyanLang(String name) {
		return env.isInPackageCyanLang(name);
	}

	@Override
	public void errorAtGenericPrototypeInstantiation(String errorMessage) {
		CompilationUnit cunit = this.env.getCurrentCompilationUnit();
		if ( cunit == null ) {
			env.error(null,  "Internal error: current compilation unit does not exist at Compiler_dsa");
			return ;
		}
		String packageNameInstantiation = cunit.getPackageNameInstantiation();
		if ( packageNameInstantiation == null ) {
			/*
			 * an error was signalled and the program unit is not generic. This may happen when a metaobject
			 * such as 'concept' is used in non-generic prototypes. This is legal. 
			 */
			cunit.error(cunit.getPublicPrototype().getFirstSymbol().getLineNumber(), cunit.getPublicPrototype().getFirstSymbol().getColumnNumber(), errorMessage);
			env.setThereWasError(true);
			return ;
		}
		String prototypeNameInstantiation = cunit.getPrototypeNameInstantiation();
		ProgramUnit programUnitInstantiation = env.searchPackagePrototype(packageNameInstantiation, prototypeNameInstantiation);
		if ( programUnitInstantiation == null ) {
			env.error(null,  "Internal error: prototype '" + prototypeNameInstantiation + 
					"' of package '" + packageNameInstantiation + "' was not found");
			return ;
		}
		
		String s = "";
		CompilationUnit previousCompUnit = cunit; // programUnitInstantiation.getCompilationUnit();
		if ( previousCompUnit.getPackageNameInstantiation() != null && 
				previousCompUnit.getPrototypeNameInstantiation() != null ) {
			s = "\n" + "Stack of generic prototype instantiations: \n" ;
			while ( previousCompUnit != null && 
					previousCompUnit.getPackageNameInstantiation() != null && 
					previousCompUnit.getPrototypeNameInstantiation() != null ) {
				s += "    " + previousCompUnit.getPackageNameInstantiation() + "." + previousCompUnit.getPrototypeNameInstantiation() + " line " +
						previousCompUnit.getLineNumberInstantiation() + " column " + previousCompUnit.getColumnNumberInstantiation() + "\n";
				ProgramUnit pppu = env.searchPackagePrototype(previousCompUnit.getPackageNameInstantiation(), previousCompUnit.getPrototypeNameInstantiation());
				if ( pppu != null ) {
					previousCompUnit = pppu.getCompilationUnit();
				}
				else {
					break;
				}
			}
		}
		
		int lineNumberInstantiation = cunit.getLineNumberInstantiation();
		
		
		int columnNumberInstantiation = cunit.getColumnNumberInstantiation();
		programUnitInstantiation.getCompilationUnit().error(lineNumberInstantiation, columnNumberInstantiation, errorMessage + s);
		env.setThereWasError(true);
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
	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(String fileName, String packageName, DirectoryPackage hiddenDirectory) {
		return env.getProject().getCompilerManager().
				getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
	}

	/**
	 * remove the code of message send exprMessageSend and replace it by codeToAdd. 
	 * This is being asked by metaobject annotation annotation.  
	 */
	
	@Override
	public boolean removeAddCodeExprMessageSend(ExprMessageSend exprMessageSend, CyanMetaobjectWithAtAnnotation annotation,
			StringBuffer codeToAdd, Type codeType) {
		
		Symbol firstSymbol = exprMessageSend.getFirstSymbol();
		return env.removeAddCodeExprMessageSend(exprMessageSend, firstSymbol.getCompilationUnit(), annotation, codeToAdd, codeType,
				firstSymbol.getOffset());
	}


	/**
	 * remove the code of message send exprMessageSend and replace it by codeToAdd. 
	 * This is being asked by metaobject annotation annotation. The type of the expression codeToAdd is
	 * codeType.  Return false if the replacement was
	 * not possible. In particular, if unaryMessageSend is not a message send, false is returned.
	 */
	
	@Override
	public boolean removeAddCodeExprIdentStar(ExprIdentStar unaryMessageSend, CyanMetaobjectWithAtAnnotation annotation,
			StringBuffer codeToAdd, Type codeType) {
		
		if ( unaryMessageSend.getIdentStarKind() != IdentStarKind.unaryMethod_t )
			return false;
		
		Symbol firstSymbol = unaryMessageSend.getFirstSymbol();
		return env.removeAddCodeExprIdentStar(unaryMessageSend, firstSymbol.getCompilationUnit(), annotation, codeToAdd, codeType,
				firstSymbol.getOffset());
	}
	
	
	
	/**
	 * return a map with a key for each prototype or interface. The value for the key is a  
	 * set with all direct subtypes of the prototype or interface. 
	 * This map is only created on demand. The key has the format: the package name, a single space, prototype name. 
	 * It can be, for example,<br>
	 * <code>
	 * "br.main Program"
	 * </code><br>
	 * The package name is "br.main" and the prototype name is "Program". 
	 * 
	 */
	
	
	@Override
	public HashMap<String, Set<ProgramUnit>> getMapPrototypeSubtypeList() {
		return env.getMapPrototypeSubtypeList();
	}

	
	@Override
	public Type createNewGenericPrototype(Symbol symUsedInError, CompilationUnitSuper compUnit, ProgramUnit currentPU,
			String fullPrototypeName, String errorMessage) {
		try {
			this.originalEnv.setPrefixErrorMessage(errorMessage);
			Expr newProgramUnit = saci.Compiler.parseSingleTypeFromString(fullPrototypeName, 
					symUsedInError, errorMessage, compUnit, currentPU);
			newProgramUnit.calcInternalTypes(originalEnv);
			return newProgramUnit.getType();

		}
		catch ( CompileErrorException cee ) {
			this.originalEnv.setThereWasError(true);
		}
		finally {
			this.originalEnv.setPrefixErrorMessage(null);
		}
		return null;
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

	

	private CyanMetaobjectAnnotation	cyanMetaobjectAnnotation;
	
	private Env env, originalEnv;

	
}
