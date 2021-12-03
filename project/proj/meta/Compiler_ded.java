package meta;

import java.util.ArrayList;
import java.util.Set;
import ast.CompilationUnit;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.InstanceVariableDec;
import ast.ObjectDec;
import ast.ProgramUnit;
import error.FileError;
import saci.DirectoryPackage;
import saci.NameServer;
import saci.Project;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple4;
import saci.Tuple5;

public class Compiler_ded implements ICompiler_ded {
	

	public Compiler_ded(Project project, CyanMetaobjectWithAtAnnotation codegAnnotation) {
		this.project = project;
		this.codegAnnotation = codegAnnotation;
	}

	@Override
	public Tuple2<Object, String> getProjectVariable(String variableName) {
		return project.getProjectVariable(variableName);
	}

	@Override
	public Set<String> getProjectVariableSet(String variableName) {
		return project.getProjectVariableSet(variableName);
	}
	
	
	@Override
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(
			String fileName,
			String extension,
			String packageName, 
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return project.getCompilerManager().
				readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
		
	}	
	
	

	@Override
	public Tuple4<FileError, char[], String, String> readTextFileFromProject(
			String fileName,
			String extension,
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return project.getCompilerManager().readTextFileFromProject(fileName, extension, hiddenDirectory, numParameters, realParamList);
	}


	@Override
	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(String fileName, String packageName, DirectoryPackage hiddenDirectory) {
		return project.getCompilerManager().
				getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
	}
	
	
	
	@Override
	public Tuple2<FileError, byte[]> readBinaryDataFileFromPackage(String fileName, String packageName) {
		return project.getCompilerManager().
				readBinaryDataFileFromPackage(fileName, packageName);
		
	}

	@Override
	public FileError saveBinaryDataFileToPackage(byte[] data, String fileName, String packageName) {
		return project.getCompilerManager().
				saveBinaryDataFileToPackage(data, fileName, packageName);
	}

	
	@Override
	public boolean deleteDirOfTestDir(String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return project.getCompilerManager().deleteDirOfTestDir(dirName, testPackageName);
	}
	
	
	/**
	 * write 'data' to file 'fileName' that is created in the test directory of package packageName.
	 * Return an object of FileError indicating any errors.
	 */

	@Override
	public
	FileError  writeTestFileTo(StringBuffer data, String fileName, String dirName) {
		return project.getCompilerManager().
				writeTestFileTo(data, fileName, dirName, getPackageNameTest());
	}
		
	@Override
	public String getPackageNameTest() {
		return "project" + NameServer.suffixTestPackageName;
	}
	
	@Override
	public String pathDataFilePackage(String fileName, String packageName) {
		return project.getCompilerManager().pathDataFilePackage(fileName, packageName);
	}
	

	@Override
	public ArrayList<Tuple2<String, String>> getLocalVariableList() {
		return this.codegAnnotation.getLocalVariableNameList();
	}

	@Override
	public ArrayList<Tuple2<String, String>> getInstanceVariableList() {
		if ( this.codegAnnotation.getCompilationUnit() instanceof CompilationUnit ) {
			CompilationUnit cunit = (CompilationUnit ) this.codegAnnotation.getCompilationUnit();
			ProgramUnit pu = cunit.getPublicPrototype();
			if ( pu == null || !(pu instanceof ObjectDec) ) {
				return null;
			}
			else {
				ObjectDec proto = (ObjectDec ) pu;
				ArrayList<Tuple2<String, String>> ivList = new ArrayList<>();
				for ( InstanceVariableDec iv : proto.getInstanceVariableList() ) {
					String strType = null;
					if ( iv.getTypeInDec() != null ) {
						strType = iv.getTypeInDec().asString();
					}
					ivList.add( new Tuple2<String, String>(iv.getName(), strType));
				}
				return ivList;
			}
		}
		else {
			return null;
		}
	}


	@Override
	public FileError writeTextFile(char[] charArray, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return project.getCompilerManager().writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
	}
	
	

	@Override
	public FileError writeTextFile(String str, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return project.getCompilerManager().writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
	}
	
	
	@Override
	public
	String getPathFileHiddenDirectory(String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return project.getCompilerManager().getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		
	}

	/**
	 * does nothing
	 */
	@Override
	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
	}
	
	
	
	private Project project;

	private CyanMetaobjectWithAtAnnotation codegAnnotation;

}
