package meta;

import java.util.ArrayList;
import java.util.Set;
import ast.CyanMetaobjectAnnotation;
import ast.CyanPackage;
import error.FileError;
import saci.Compiler;
import saci.CompilerManager;
import saci.DirectoryPackage;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple4;
import saci.Tuple5;

public class AbstractCyanCompiler implements IAbstractCyanCompiler {

	public AbstractCyanCompiler(Env env, CompilerManager cm, Compiler compiler) {
		this.env = env;
		this.compiler = compiler;
		this.cm = cm;
	}
	
	
	@Override
	public Tuple2<Object, String> getProjectVariable(String variableName) {
		if ( env != null ) {
			return this.env.getProject().getProjectVariable(variableName);
		}
		else {
			return cm.getProgram().getProject().getProjectVariable(variableName);
		}
	}
	
	@Override
	public Set<String> getProjectVariableSet(String variableName) {
		if ( env != null ) {
			return env.getProject().getProjectVariableSet(variableName);
		}
		else {
			
			return cm.getProgram().getProject().getProjectVariableSet(variableName);
		}
	}
	

	@Override
	public Tuple2<FileError, byte[]> readBinaryDataFileFromPackage(String fileName, String packageName) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().
					readBinaryDataFileFromPackage(fileName, packageName);
		}
		else {
			return cm.readBinaryDataFileFromPackage(fileName, packageName);
		}
	}

	@Override
	public FileError writeTestFileTo(StringBuffer data, String fileName, String dirName) {
		String testPackageName = this.getPackageNameTest();
		if ( env != null ) {
			return env.getProject().getCompilerManager().
					writeTestFileTo(data, fileName, dirName, testPackageName);
		}
		else {
			return cm.writeTestFileTo(data, fileName, dirName, testPackageName);
		}
	}

	@Override
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(String fileName,
			String extension, String packageName, DirectoryPackage hiddenDirectory, int numParameters,
			ArrayList<String> realParamList) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().
					readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
			
		}
		else {
			return cm.readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
			
		}
	}

	@Override
	public Tuple4<FileError, char[], String, String> readTextFileFromProject(String fileName, String extension,
			DirectoryPackage hiddenDirectory, int numParameters, ArrayList<String> realParamList) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().readTextFileFromProject(fileName, extension, hiddenDirectory, numParameters, realParamList);
			
		}
		else {
			return cm.readTextFileFromProject(fileName, extension, hiddenDirectory, numParameters, realParamList);
			
		}
	}


	@Override
	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(String fileName, String packageName, DirectoryPackage hiddenDirectory) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
		}
		else {
			return cm.getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
		}
	}

	@Override
	public String getPackageNameTest() {
		if ( env != null ) {
			return env.getCurrentCompilationUnit().getPackageName() + NameServer.suffixTestPackageName;
		}
		else {
			return compiler.getCompilationUnit().getPackageName() + NameServer.suffixTestPackageName;
			
		}
	}

	@Override
	public FileError writeTextFile(char[] charArray, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
		}
		else {
			return cm.writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
		}
	}

	@Override
	public FileError writeTextFile(String str, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
		}
		else {
			return cm.writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
		}
	}

	@Override
	public String getPathFileHiddenDirectory(String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		if ( env != null ) {
			return env.getProject().getCompilerManager().getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		}
		else {
			return cm.getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		}
	}

	@Override
	public boolean deleteDirOfTestDir(String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		if ( env != null ) {
			return env.getProject().getCompilerManager().deleteDirOfTestDir(dirName, testPackageName);
		}
		else {
			return cm.deleteDirOfTestDir(dirName, testPackageName);
		}
	}


	/**
	 * does nothing if it is called outside semantic analysis (dsa phase)
	 */
	@Override
	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
		if ( env != null ) {
			env.errorInsideMetaobjectAnnotation(metaobjectAnnotation, lineNumber, columnNumber, message);
		}
	}
	
	
	/*
	 * one of them should be null and the other non-null
	 */
	private Env env;
	private CompilerManager cm;
	private Compiler compiler;
}
