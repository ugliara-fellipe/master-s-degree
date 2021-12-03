package meta;

import java.util.ArrayList;
import java.util.Set;
import ast.CompilationUnit;
import ast.CyanMetaobjectAnnotation;
import ast.CyanPackage;
import ast.ExprAnyLiteral;
import ast.GenericParameter;
import ast.MethodDec;
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

public class CompilerAction_dpa implements ICompilerAction_dpa {
	
	public CompilerAction_dpa(Compiler compiler) {
		this.compiler = compiler.clone();
	}
	
	@Override
	public ArrayList<ArrayList<String>> getGenericPrototypeArgListList() {
		ProgramUnit pu = compiler.getCurrentProgramUnit();
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
		if ( compiler.getCurrentProgramUnit() != null ) {
			return compiler.getCurrentProgramUnit().getName();
		}
		else
			return null;
	}

	@Override
	public String getCurrentPrototypeId() {
		if ( compiler.getCurrentProgramUnit() != null ) {
			return compiler.getCurrentProgramUnit().getIdent();
		}
		else
			return null;
	}

	@Override
	public MethodDec getCurrentMethod() {
		return compiler.getCurrentMethod();
	}
	

	@Override
	public Tuple2<Object, String> getProjectVariable(String variableName) {
		return compiler.getProject().getProjectVariable(variableName);
	}

	@Override
	public Set<String> getProjectVariableSet(String variableName) {
		return compiler.getProject().getProjectVariableSet(variableName);
	}

	/**
	 * if the current program unit was created from a generic prototype instantiation, 
	 * the instantiation is in package packageNameInstantiation, prototype prototypeNameInstantiation,
	 * line number lineNumberInstantiation, and column number columnNumberInstantiation. 
	 * The methods below are the getters and setters for these variables. In regular
	 * prototypes packageNameInstantiation and prototypeNameInstantiation are null.
	 */
	@Override
	public String getPackageNameInstantiation() {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			return null;
		}
		return cunit.getPackageNameInstantiation();
	}

	@Override
	public void setPackageNameInstantiation(String packageNameInstantiation) {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			compiler.error2(null, "Attempt to set package name of a prototype instantiation outside a compilation unit");
			return ;
		}
		cunit.setPackageNameInstantiation(packageNameInstantiation);
	}

	@Override
	public String getPrototypeNameInstantiation() {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			return null;
		}
		return cunit.getPrototypeNameInstantiation();	
	}

	@Override
	public void setPrototypeNameInstantiation(String prototypeNameInstantiation) {
		//CompilationUnit cunit = this.compiler.
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			compiler.error2(null, "Attempt to set prototype name of a prototype instantiation outside a compilation unit");
			return ;
		}
		cunit.setPrototypeNameInstantiation(prototypeNameInstantiation);	
	}

	@Override
	public int getLineNumberInstantiation() {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			return -1;
		}
		return cunit.getLineNumberInstantiation();	
	}

	@Override
	public void setLineNumberInstantiation(int lineNumberInstantiation) {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			compiler.error2(null, "Attempt to set the line number of a prototype instantiation outside a compilation unit");
			return ;
		}
		cunit.setLineNumberInstantiation(lineNumberInstantiation);	
	}

	@Override
	public int getColumnNumberInstantiation() {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			return -1;
		}
		return cunit.getColumnNumberInstantiation();	
	}

	@Override
	public void setColumnNumberInstantiation(int columnNumberInstantiation) {
		CompilationUnit cunit = this.compiler.getCompilationUnit();
		if ( cunit == null ) {
			compiler.error2(null, "Attempt to set the column number of a prototype instantiation outside a compilation unit");
			return ;
		}
		cunit.setColumnNumberInstantiation(columnNumberInstantiation);	
	}

	/**
	 * return the feature list of the current prototype, if there is one. Otherwise return null
	 */
	@Override
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		ProgramUnit pu = compiler.getCurrentProgramUnit();
		if ( pu != null ) {
			return pu.getFeatureList();
		}
		else
			return null;
	}

	@Override
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(String fileName, String extension, String packageName, DirectoryPackage hiddenDirectory, int numParameters,
			ArrayList<String> realParamList) {
				return compiler.getProject().getCompilerManager().
						readTextFileFromPackage(fileName, extension, packageName, hiddenDirectory, numParameters, realParamList);
				
			}

	@Override
	public Tuple4<FileError, char[], String, String> readTextFileFromProject(String fileName, String extension, DirectoryPackage hiddenDirectory, int numParameters, ArrayList<String> realParamList) {
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
	public saci.Compiler getCompiler() {
		return compiler;
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
				ProgramUnit pppu = compiler.searchPackagePrototype(previousCompUnit.getPackageNameInstantiation(), previousCompUnit.getPrototypeNameInstantiation());
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
	public FileError writeTestFileTo(StringBuffer data, String fileName, String dirName) {
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
	public char[] getText(int offsetLeftCharSeq, int offsetRightCharSeq) {
		return compiler.getText(offsetLeftCharSeq, offsetRightCharSeq);
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return compiler.getCompilationUnit();
	}

	@Override
	public ProgramUnit searchPackagePrototype(String packageNameInstantiation, String prototypeNameInstantiation) {
		return compiler.searchPackagePrototype(packageNameInstantiation, prototypeNameInstantiation);
	}

	@Override
	public FileError writeTextFile(char[] charArray, String fileName, String prototypeFileName, String packageName, DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().writeTextFile(charArray, fileName, prototypeFileName, packageName, hiddenDirectory);
	}

	@Override
	public FileError writeTextFile(String str, String fileName, String prototypeFileName, String packageName, DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().writeTextFile(str, fileName, prototypeFileName, packageName, hiddenDirectory);
	}

	@Override
	public String getPathFileHiddenDirectory(String prototypeFileName, String packageName, DirectoryPackage hiddenDirectory) {
		return compiler.getProject().getCompilerManager().getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		
	}

	@Override
	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
		compiler.errorInsideMetaobjectAnnotation(metaobjectAnnotation, lineNumber, columnNumber, message);
	}

	@Override
	public void error(int lineNumber, String message) {
		compiler.error2(lineNumber, message, false);
	}

	@Override
	public void error(Symbol sym, String message) {
		compiler.error2(sym, message);
	}


	public Compiler compiler;

}

