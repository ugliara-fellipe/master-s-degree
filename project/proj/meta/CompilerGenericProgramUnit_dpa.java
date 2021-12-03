package meta;

import java.util.ArrayList;
import java.util.Set;
import ast.ASTVisitor;
import ast.CompilationUnit;
import ast.CyanMetaobjectAnnotation;
import ast.CyanPackage;
import ast.Program;
import ast.ProgramUnit;
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

public class CompilerGenericProgramUnit_dpa implements ICompilerGenericProgramUnit_dpa {


	public CompilerGenericProgramUnit_dpa(Compiler compiler, ProgramUnit programUnit) {
		this.compiler = compiler.clone();
		this.programUnit = programUnit;
	}
	
	@Override
	public void callVisitor(ASTVisitor visitor) {
		programUnit.accept(visitor);
	}

	@Override
	public Program getProgram() {
		return compiler.getProject().getProgram();
	}

	@Override
	public ProgramUnit getProgramUnit() {
		return programUnit;
	}

	@Override
	public void error(Symbol symbol, String message) {
		compiler.error2(symbol, message);
	}

	@Override
	public void errorAtGenericPrototypeInstantiation(String errorMessage) {
		CompilationUnit cunit = compiler.getCurrentProgramUnit().getCompilationUnit();
		if ( cunit == null ) {
			compiler.error2(null,  "Internal error: current compilation unit does not exist at Compiler_dsa");
			return ;
		}
		String packageNameInstantiation = cunit.getPackageNameInstantiation();
		if ( packageNameInstantiation == null ) {
			compiler.error2(null,  "Attempt to sign an error in a generic prototype instantiation outside a generic prototype instantiation");
		}
		String prototypeNameInstantiation = cunit.getPrototypeNameInstantiation();
		ProgramUnit programUnitInstantiation = compiler.searchPackagePrototype(packageNameInstantiation, prototypeNameInstantiation);
		if ( programUnitInstantiation == null ) {
			compiler.error2(null,  "Internal error: prototype '" + prototypeNameInstantiation + 
					"' of package '" + packageNameInstantiation + "' was not found");
			return ;
		}
		
		int lineNumberInstantiation = cunit.getLineNumberInstantiation();
		
		
		int columnNumberInstantiation = cunit.getColumnNumberInstantiation();
		programUnitInstantiation.getCompilationUnit().error(lineNumberInstantiation, columnNumberInstantiation, errorMessage);
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
	public void addToListAfter_ati(IListAfter_ati annotation) {
		compiler.addToListAfter_ati(annotation);
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return compiler.getCompilationUnit();
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
	
	
	
	private Compiler compiler;
	private ProgramUnit programUnit;

}
