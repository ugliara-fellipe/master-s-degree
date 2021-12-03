package saci;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import ast.CompilationUnit;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Program;
import error.FileError;
import error.ProjectError;
import meta.ICompiler_dpp;

public class Project implements ICompiler_dpp {

	
	public Project( Program program, String projectCanonicalPath, String execFileName ) {
		this(program, null, null, null, null, null, null, null, null, projectCanonicalPath, execFileName);
		
	}


	private String projectCanonicalPath;
	private String execFileName;

	public Project( Program program, String mainPackage, String mainObject,
			        ArrayList<String> authorArray,
			        ArrayList<String> cyanPathArray,
			        ArrayList<String> javaPathArray,
			        ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
			        ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList, 
			        ArrayList<String> importList, String projectCanonicalPath, String execFileName ) {
		
		this.program = program;
		this.setMainPackage(mainPackage);
		this.setMainObject(mainObject);
		this.authorArray = authorArray;
		this.setCyanPathArray(cyanPathArray);
		this.javaPathArray = javaPathArray;
		this.nonAttachedMetaobjectAnnotationList = nonAttachedMetaobjectAnnotationList;
		this.attachedMetaobjectAnnotationList= attachedMetaobjectAnnotationList;
		setCompilerManager(null);
		tmpPackageTable = new Hashtable<String, CyanPackage>();
		this.importList = importList;
		keyValueHash = new HashMap<>();
		keyToSetMap = new HashMap<>();
		this.projectCanonicalPath = projectCanonicalPath;
		this.execFileName = execFileName;
	}

	public void addCyanPackage( CyanPackage aPackage ) {
		program.addCyanPackage(aPackage);
		tmpPackageTable.put(aPackage.getPackageName(),  aPackage);
	}

	public ArrayList<CyanPackage> getPackageList() {
		return program.getPackageList();
	}


	public void setMainPackage(String mainPackage) {
		this.mainPackage = mainPackage;
	}

	public String getMainPackage() {
		return mainPackage;
	}


	public void setMainObject(String mainObject) {
		this.mainObject = mainObject;
	}

	public String getMainObject() {
		return mainObject;
	}

	public void setCompilerOptions(String compilerOptions) {
		this.compilerOptions = compilerOptions;
	}

	public String getCompilerOptions() {
		return compilerOptions;
	}

	/**
	 * prints all the project information (which was extracted from the project file)
	 */
	public void print() {
		for ( String author : authorArray )
		   System.out.println("author: " + author + "\n");
		System.out.println(
		  "Compiler Options: " + compilerOptions + "\n" +
		  "main package: " + mainPackage + "\n" +
		  "main object: " + mainObject + "\n"
		);
		for ( CyanPackage ps : this.getPackageList() )
			ps.print();
	}

	public void setAuthorArray(ArrayList<String> authorArray) {
		this.authorArray = authorArray;
	}

	public ArrayList<String> getAuthorArray() {
		return authorArray;
	}


	/**
	 * search the package with name "name"
	 */
	public CyanPackage searchPackage(String name) {
		for (CyanPackage aPackage : program.getPackageList() )
			if ( aPackage.getPackageName().compareTo(name) == 0 )
				return aPackage;
		return null;
	}	
	
	/**
	 * search and returns the first package that has a prototype whose name  is the parameter 
	 */
	public CyanPackage searchPackageOfCompilationUnit(String prototypeName) {
		for ( CyanPackage ps : program.getPackageList() ) {
			for ( CompilationUnit compilationUnit : ps.getCompilationUnitList() ) {
				if ( compilationUnit.getFileNameWithoutExtension().equals(prototypeName) )
					return ps;
			}
		}
		return null;
	}


	public void printErrorList(PrintWriter printWriter) {
		
		if ( projectErrorList != null ) 
			for ( ProjectError projectError : projectErrorList ) 
				projectError.print(printWriter);

	}
	
	public void error(String message) {
		if ( this.projectErrorList == null ) 
			this.projectErrorList = new ArrayList<>();
		this.projectErrorList.add( new ProjectError(message));
	}



	public String getProjectDir() {
		return projectDir;
	}

	public void setProjectDir(String projectDir) {
		this.projectDir = projectDir;
	}

	
	public ArrayList<String> getCyanPathArray() {
		return cyanPathArray;
	}

	public void setCyanPathArray(ArrayList<String> cyanPathArray) {
		this.cyanPathArray = cyanPathArray;
	}


	public CyanPackage getCyanLangPackage() {
		return cyanLangPackage;
	}

	public void setCyanLangPackage(CyanPackage cyanLangPackage) {
		this.cyanLangPackage = cyanLangPackage;
	}


	public CompilerManager getCompilerManager() {
		return compilerManager;
	}

	public void setCompilerManager(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
	}


	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public Hashtable<String, CyanPackage> getTmpPackageTable() {
		return tmpPackageTable;
	} 
	

	public boolean hasErrors() {
		return projectErrorList != null && projectErrorList.size() > 0;
	}

	public ArrayList<String> getImportList() {
		return importList;
	}

	public void setImportList(ArrayList<String> importList) {
		this.importList = importList;
	}

	
	@Override
	public void setProjectVariable(String variableName, String value) {
		keyValueHash.put(variableName, new Tuple2<Object, String>(value, "String"));
	}

	@Override
	public void setProjectVariable(String variableName, int value) {
		keyValueHash.put(variableName, new Tuple2<Object, String>(value, "int"));
	}

	@Override
	public void setProjectVariable(String variableName, boolean value) {
		keyValueHash.put(variableName, new Tuple2<Object, String>(value, "boolean"));
	}


	@Override
	public Tuple2<Object, String> getProjectVariable(String variableName) {
		return keyValueHash.get(variableName);
	}
	
	
	private ArrayList<String> authorArray;
	/**
	 * Compiler options applicable to the whole program. These options may be
	 * overridden in packages and in files.
	 */
	private String compilerOptions;
	private String mainPackage;
	private String mainObject;
	
	
	/**
	 * path of directories in which there are Java packages imported by
	 * the Cyan program.
	 */
	private ArrayList<String> javaPathArray;
	/**
	 * path of directories in which there are Cyan packages. These
	 * packages are not in the directory of the project as usual.
	 */
	private ArrayList<String> cyanPathArray;

	private ArrayList<ProjectError> projectErrorList;
	
	/**
	 * the directory in which the project is
	   @param projectDir
	 */
	private String projectDir;
	/**
	 * the package cyan.lang
	 */
	private CyanPackage	cyanLangPackage;
	
	private CompilerManager compilerManager;
	/**
	 * list of metaobjects attached to the program. These metaobjects are attached
	 * to the keyword 'program' in the project file. The project file has extension ".pyan".
	 * 
	 * 
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList;
	
	private ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList;
	
	private Program program;
	
	  /*
	   * set with the "--tmp" packages that should be created. A prototype
	   * "Stack{@literal <}Int>" that is the instantiation of "Stack{@literal <}T>" of package "util"
	   * is put in a package "util.tmp". This package, created by the Compiler,
	   * is added to packageTable. All of these packages should be compiled
	   * after all generic instantiations like "Stack{@literal <}Int>" are created.
	   * Prototype "Stack{@literal <}Int>" is created in a file "util\--tmp\Stack(Int).cyan"
	   */
	private Hashtable<String, CyanPackage> tmpPackageTable;

	/**
	 * the import list of this project
	 */
	private ArrayList<String> importList;

	public ArrayList<CyanMetaobjectWithAtAnnotation> getNonAttachedMetaobjectAnnotationList() {
		return nonAttachedMetaobjectAnnotationList;
	}

	public void setNonAttachedMetaobjectAnnotationList(ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList) {
		this.nonAttachedMetaobjectAnnotationList = nonAttachedMetaobjectAnnotationList;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getAttachedMetaobjectAnnotationList() {
		return attachedMetaobjectAnnotationList;
	}

	public void setAttachedMetaobjectAnnotationList(ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList) {
		this.attachedMetaobjectAnnotationList = attachedMetaobjectAnnotationList;
	}

	/**
	 * 
	   @see meta.ICompiler_dpp#addToProjectSet(java.lang.String, java.lang.String)
	 */

	@Override
	public void addToProjectSet(String variableName, String value) {
		HashSet<String> set = keyToSetMap.get(variableName);
		if ( set == null ) {
			set = new HashSet<>();
		}
		set.add(value);
		keyToSetMap.put(variableName, set);
	}
	
	@Override
	public Set<String> getProjectVariableSet(String variableName) {
		return keyToSetMap.get(variableName);
	}

	public ArrayList<ProjectError> getProjectErrorList() {
		return projectErrorList;
	}

	
	public boolean getCallJavac() {
		return callJavac;
	}

	public void setCallJavac(boolean callJavac) {
		this.callJavac = callJavac;
	}


	public boolean getExec() {
		return exec;
	}

	public void setExec(boolean exec) {
		this.exec = exec;
	}


	
	public String getParseOnlyFile() {
		return parseOnlyFile;
	}

	public void setParseOnlyFile(String parseOnlyFile) {
		this.parseOnlyFile = parseOnlyFile;
	}

	public ArrayList<String> getNamePackageImportList() {
		return namePackageImportList;
	}

	public ArrayList<String> getPathPackageImportList() {
		return pathPackageImportList;
	}
	
	
	public void addNamePackageImportList(String name) {
		namePackageImportList.add(name);
	}

	public void addPathPackageImportList(String path) {
		pathPackageImportList.add(path);
	}
	
	
	public String getCmdLineArgs() {
		return cmdLineArgs;
	}
	
	public void setCmdLineArgs(String cmdLineArgs) {
		this.cmdLineArgs = cmdLineArgs;
	}

	
	@Override
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(
			String fileName,
			String extension,
			String packageName, 
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return this.compilerManager.
				readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
		
	}	
	
	

	@Override
	public Tuple4<FileError, char[], String, String> readTextFileFromProject(
			String fileName,
			String extension,
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		return this.compilerManager.readTextFileFromProject(fileName, extension, hiddenDirectory, numParameters, realParamList);
	}
	
	@Override
	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(String fileName, String packageName, DirectoryPackage hiddenDirectory) {
		return this.compilerManager.
				getAbsolutePathHiddenDirectoryFile(fileName, packageName, hiddenDirectory);
	}
	
	
	@Override
	public Tuple2<FileError, byte[]> readBinaryDataFileFromPackage(String fileName, String packageName) {
		
		return compilerManager.
				readBinaryDataFileFromPackage(fileName, packageName);
	}	
	
	
	@Override
	public boolean deleteDirOfTestDir(String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return compilerManager.deleteDirOfTestDir(dirName, testPackageName);
	}
	

	/**
	 * write 'data' to file 'fileName' that is created in the test directory of package packageName.
	 * Return an object of FileError indicating any errors.
	 */

	@Override
	public
	FileError  writeTestFileTo(StringBuffer data, String fileName, String dirName) {
		String testPackageName = this.getPackageNameTest(); 
		return compilerManager.writeTestFileTo(data, fileName, dirName, testPackageName);
	}

	@Override
	public String getPackageNameTest() {
		return "project" + NameServer.suffixTestPackageName;
	}

	@Override
	public FileError writeTextFile(char[] charArray, String fileName, String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return compilerManager.writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
	}
	
	@Override
	public FileError writeTextFile(
			String str,
			String fileName,
			String prototypeFileName,
			String packageName, 
			DirectoryPackage hiddenDirectory) {
		return compilerManager.writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
	}

	
		//return CompilerManager.writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);		
	
	@Override
	public String getPathFileHiddenDirectory(String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		return compilerManager.getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
	}

	/**
	 * does nothing
	 */
	@Override
	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
	}
	

	
	
	/**
	 * the source code of the .pyan file that keeps the project. It should end with '\0'
	 */
	private char []text;

	/**
	 * list of pairs (variableName, Tuple2(value, type)) in which type is "String", "boolean", or "int"
	 */
	private HashMap<String, Tuple2<Object, String>> keyValueHash;
	
	/**
	 * list of pairs (variableName, set). To variableName is associated the values of the set
	 */
	private HashMap<String, HashSet<String>> keyToSetMap;

	/**
	 * true if the Java compiler should be called after compiling the Cyan code
	 */
	private boolean callJavac;
	/**
	 * true if the compiled Java code should be executed after it successfully compiles
	 */
	private boolean exec;

	/**
	 * contains the filename of the only file that should be parsed. 
	 */
	private String parseOnlyFile = null;

	public char[] getText() {
		return text;
	}

	public void setText(char[] text) {
		this.text = text;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return this.projectName;
	}	
	
	public String getProjectCanonicalPath() {
		return projectCanonicalPath;
	}



	/**
	 * the name of the project file without the extension. For example, it can be 'project'
	 */
	private String projectName;


	/**
	 * name of the imported packages (to be used when there is a metaobject annotation inside the .pyan file)
	 */
	ArrayList<String> namePackageImportList = new ArrayList<>();
	/**
	 * path of the imported packages (to be used when there is a metaobject annotation inside the .pyan file)
	 */
	ArrayList<String> pathPackageImportList = new ArrayList<>();

	
	/**
	 * the arguments that should be passed to the Cyan program
	 */
	private String cmdLineArgs = null;

	public String getExecFileName() {
		return execFileName;
	}


}