package meta;

import java.util.ArrayList;
import java.util.Set;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectMacroCall;
import ast.CyanPackage;
import ast.Expr;
import ast.ICalcInternalTypes;
import ast.Statement;
import error.ErrorKind;
import error.FileError;
import lexer.Symbol;
import saci.CompilationStep;
import saci.Compiler;
import saci.DirectoryPackage;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple4;
import saci.Tuple5;

public class CompilerMacro_dpa implements ICompilerMacro_dpa {

	public CompilerMacro_dpa(Compiler compiler) {
		this.compiler = compiler.clone();
		this.wasErrors = false;
		exprStatList = new ArrayList<>();
		lastSymbol = null;
	}
	
	
	public CyanMetaobjectMacroCall getMacroCall() {
		return macroCall;
	}

	@Override
	public void setCyanMetaobjectMacro(CyanMetaobjectMacroCall macroCall) {
		this.macroCall = macroCall;
	}

	@Override
	public void next() {
		lastSymbol = compiler.getSymbol(); 
		compiler.next();
	}

	@Override
	public Symbol getSymbol() {
		return compiler.getSymbol();
	}

	@Override
	public boolean symbolCanStartExpr(Symbol symbol) {
		return Compiler.canStartMessageSelector(symbol.token);
	}

	@Override
	public Expr expr() {
		Expr e = compiler.expr();
		exprStatList.add(e);
		return e;
	}

	@Override
	public Expr exprBasicTypeLiteral() {
		Expr e = compiler.exprLiteral();
		exprStatList.add(e);
		return e;
	}
	
	
	@Override
	public Statement statement() {
		Statement s = compiler.statement();
		exprStatList.add(s);
		return s;
	}
	
	@Override
	public Expr functionDec() {
		Expr e = compiler.functionDec();
		exprStatList.add(e);
		return e;
	}

	@Override
	public void error(Symbol sym, String specificMessage, String identifier,
			ErrorKind errorKind, String... furtherArgs) {
		wasErrors = true;
		compiler.error(true, sym, specificMessage, identifier, errorKind, furtherArgs);
	}

	@Override
	public boolean getThereWasErrors() {
		return wasErrors;
	}


	@Override
	public void setThereWasErrors(boolean wasError) {
		this.wasErrors = wasError;
	}


	public ArrayList<ICalcInternalTypes> getExprStatList() {
		return exprStatList;
	}

	
	@Override
	public Tuple2<Object, String> getProjectVariable(String variableName) {
		return compiler.getProject().getProjectVariable(variableName);
	}
	
	@Override
	public Set<String> getProjectVariableSet(String variableName) {
		return compiler.getProject().getProjectVariableSet(variableName);
	}

	@Override
	public Symbol getLastSymbol() {
		return lastSymbol;
	}

	
	
	@Override
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(
			String fileName,
			String extension,
			String packageName, 
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return compiler.getProject().getCompilerManager().
				readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
		
	}	
	

	@Override
	public Tuple4<FileError, char[], String, String> readTextFileFromProject(
			String fileName,
			String extension,
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return compiler.getProject().getCompilerManager().readTextFileFromProject(fileName, extension, hiddenDirectory, numParameters, realParamList);
	}
	

	@Override
	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(String fileName, String packageName, DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().
				getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
	}
	
	
	@Override
	public Tuple2<FileError, byte[]> readBinaryDataFileFromPackage(String fileName, String packageName) {
		return compiler.getProject().getCompilerManager().
				readBinaryDataFileFromPackage(fileName, packageName);
		
	}
	
	
	@Override
	public boolean deleteDirOfTestDir(String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return compiler.getProject().getCompilerManager().deleteDirOfTestDir(dirName, testPackageName);
	}
	
	/**
	 * write 'data' to file 'fileName' that is created in the test directory of package packageName.
	 * Return an object of FileError indicating any errors.
	 */
	
	@Override
	public
	FileError  writeTestFileTo(StringBuffer data, String fileName, String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return compiler.getProject().getCompilerManager().
				writeTestFileTo(data, fileName, dirName, testPackageName);
	}

	@Override
	public String getPackageNameTest() {
		return compiler.getCompilationUnit().getPackageName() + NameServer.suffixTestPackageName;
	}
	

	@Override
	public CompilationStep getCompilationStep() {
		return compiler.getCompilationStep();
	}


	@Override
	public FileError writeTextFile(char[] charArray, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
	}
		

	

	@Override
	public FileError writeTextFile(String str, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
	}
	
	
	@Override
	public
	String getPathFileHiddenDirectory(String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		
	}	

	@Override
	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
		compiler.errorInsideMetaobjectAnnotation(metaobjectAnnotation, lineNumber, columnNumber, message);
	}
	
	
	
	public Compiler compiler;
	private CyanMetaobjectMacroCall macroCall;
	
	private boolean wasErrors;

	/**
	 * list of expressions and statements returned by calls to {@link Compiler_dpa#expr()} and, in the future, to method statement()
	 */
	private ArrayList<ICalcInternalTypes> exprStatList;
	/**
	 * last symbol of the macro
	 */
	private Symbol lastSymbol;

}
