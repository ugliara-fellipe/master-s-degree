
package ast;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import error.CompileErrorException;
import error.ErrorKind;
import error.UnitError;
import lexer.Lexer;
import lexer.Symbol;
import meta.CyanMetaobject;
import meta.CyanMetaobjectLiteralObjectSeq;
import meta.CyanMetaobjectLiteralString;
import meta.CyanMetaobjectMacro;
import meta.CyanMetaobjectNumber;
import meta.CyanMetaobjectWithAt;
import meta.IStaticTyping;
import meta.cyanLang.CyanMetaobjectStaticTyping;
import refactoring.Action;
import saci.Compiler;
import saci.Env;
import saci.MyFile;
import saci.NameServer;

/**
 * This class represents a generic compilation unit. It is used for both Cyan source files and for Pyan files (the project file).
   @author José
 */
public class CompilationUnitSuper implements Cloneable {

	public CompilationUnitSuper(String filename, String canonicalPathUpDir) {
		this.filename = filename;
		this.canonicalPathUpDir = canonicalPathUpDir;
		errorList = new ArrayList<UnitError>();
		actionList = new ArrayList<Action>();
		if ( canonicalPathUpDir.endsWith(NameServer.fileSeparatorAsString) ) {
			this.fullFileNamePath = this.canonicalPathUpDir + this.filename;
		}
		else 
			this.fullFileNamePath = this.canonicalPathUpDir + NameServer.fileSeparatorAsString + this.filename;
		this.codegList = null;
		importPackageSet = new HashSet<>();
	}

	@Override
	public CompilationUnitSuper clone() {
		 
		try {
		    return (CompilationUnitSuper ) super.clone();
		}
		catch ( CloneNotSupportedException e ) {
			return null;
		}
	}
	
	
	/**
	 * reset the state of this compilation unit eliminating any traces it has already been compiled.
	 */
	
	public void reset() {
		errorList.clear();
		actionList.clear();
		
		/*
		 * if any of the instance variables that appear below is null, then this compilation unit
		 * was created in step 3, ati. Then this compilation unit has never been compiled before
		 */
		resetMetaobjectTables();
	}

	/**
	   
	 */
	private void resetMetaobjectTables() {
		if ( metaObjectTable != null ) {
			metaObjectTable.clear();
		}
		if ( metaObjectLiteralNumberTable != null ) { 
			metaObjectLiteralNumberTable.clear();
			metaobjectLiteralStringTable.clear();
			metaObjectLiteralObjectSeqTable.clear();
			metaObjectMacroTable.clear();
		}
	}
	
	public void addAction(Action action) {
		actionList.add(action);
	}


	public void addError(UnitError error) {
		errorList.add(error);
	}


	/**
	 * Add text from "input" before the text of this compilation unit that starts
	 * at offset fromOffset. This is used to add methods to all 
	 * program units. The methods are generated as a string that is converted
	 * into a char array passed as parameter to this method. 
	 * 
	 * After the "input" text is added to the current compilation unit, 
	 * the lexical analyzer is called on this input. 
	 * 
	   @param input should end with a '\0' which should not be copied
	   @param from
	   @return
	 */
	public boolean addTextToCompilationUnit(char []input, int inputSize,
			int fromOffset) {
		
		
		// add "input" to offset fromOffset of the text of this compilation unit
		char []newText = new char[text.length + inputSize];
		int i;
		int offset = fromOffset;
		for (i = 0; i < offset; ++i)
			newText[i] = text[i];
		  // inputSize -1 because '\0' should not be copied
		for (i = 0; i < inputSize - 1; ++i )
			newText[i + offset] = input[i];
		int j = offset;
		for (i = inputSize -1 + offset; i < text.length + inputSize - 1; ++i) {
			newText[i] = text[j];
			++j;
		}
		text = newText;
		
		return true;
	}

	/**
	 * clear all errors and actions of this compilation unit
	 */
	public void clearErrorsActions() {
		errorList = new ArrayList<UnitError>();
		actionList = new ArrayList<Action>();
	}

	/**
	 * delete sizeToDelete characters from position fromOffset of the text of this compilation unit.
	 * Return false if fromOffset + sizeToDelete is greater than the input size.  
	   @param fromOffset
	   @param sizeToDelete
	   @return
	 */
	public boolean deleteTextFromCompilationUnit(int fromOffset, int sizeToDelete) {
		int i = fromOffset;
		int j = fromOffset + sizeToDelete;
		while ( text[j] != '\0' ) {
			text[i] = text[j];
			++i;
			++j;
		}
		text[i] = '\0';
		if ( i < fromOffset + sizeToDelete )
			return true;
		else {
			return false;
		}
	}

	public void doActionList(PrintWriter printWriter) {
		
	}
	public void doActionList2(PrintWriter printWriter) {
	
		int offset = 0;
		int sizeErrorList = this.errorList.size();
		
		if ( actionList.size() > 0 ) {
			MyFile f;
			f = new MyFile(this.fullFileNamePath);
			 /*
			  * it is necessary to read the file again because of
			  * the methods added by the compiler to text. At this
			  * point, text has the compiler-added methods. 
			  */
			text = originalText;
	
			for( Action a : actionList ) {
				a.addOffset(offset);
				a.doIt();
				offset += a.getNumberCharsInserted();
			}
			f = new MyFile(this.fullFileNamePath);
			if ( ! f.writeFile(text) ) {
				Compiler compiler = null;
				error(null, 0, "error in writing to file " + fullFileNamePath, compiler, (Env ) null);
			}
			
		}
		
		for(int i = sizeErrorList; i < errorList.size(); i++)
			errorList.get(i).print(printWriter);
	
	}

	public void error(Symbol symbol, int lineNumber, int columnNumber, String msg, saci.Compiler compiler, Env env) {
		error(true, symbol, lineNumber, columnNumber, msg, compiler, env);
	}

	
	public void error(Symbol symbol, int lineNumber, String msg, saci.Compiler compiler, Env env) {
		error(symbol, lineNumber, 0, msg, compiler, env);
		// error(true, symbol, lineNumber, msg, compiler, env);
	}
	
	public void error(boolean throwException, Symbol symbol, int lineNumber, String msg, Compiler compiler, Env env) {
		warning(symbol, lineNumber, msg, compiler, env);
		if ( throwException ) {
			throw new CompileErrorException();
		}
	}

	public void error(boolean throwException, Symbol symbol, int lineNumber, int columnNumber, String msg, saci.Compiler compiler, Env env) {
		warning(symbol, lineNumber, columnNumber, msg, compiler, env);
		if ( throwException ) {
			throw new CompileErrorException();
		}
	}
	
	
	public void error(Symbol symbol, int lineNumber, String specificMessage, ErrorKind errorKind, saci.Compiler compiler) {
		error(true, symbol, lineNumber, specificMessage, errorKind, compiler);
	}
	
	public void error(boolean throwException, Symbol symbol, int lineNumber, String specificMessage, ErrorKind errorKind, saci.Compiler compiler) {
		
		warning(symbol, lineNumber, specificMessage, compiler, null);
		
		ArrayList<String> sielCode = new ArrayList<String>();
		sielCode.add("error = \"" + specificMessage + "\"");
		if ( errorKind.getFieldList().length > 0 ) {
				error(null, lineNumber, "Internal error in Env::error: field " + errorKind.getFieldList()[0] + 
						" of the error was not expected", compiler, null);
		}

		
		if ( throwException ) {
			throw new CompileErrorException();
		}
	}

	public ArrayList<Action> getActionList() {
		return actionList;
	}


	public String getEntityName() {
		if ( publicObjectInterfaceName == null ) {
			publicObjectInterfaceName = this.getFileNameWithoutExtension();
		}
		return publicObjectInterfaceName;
	}

	public void setObjectInterfaceName(String name) {
		this.publicObjectInterfaceName = name;
	}
	
	
	public ArrayList<UnitError> getErrorList() {
		return errorList;
	}
	

	public void clearErrorList() {
		this.errorList.clear();
	}
	

	public String getFilename() {
		return filename;
	}

	/**
	 * return the filename without the extension. That should be the name of the 
	 * public prototype, if it is not generic, or something like "Stack(1)" if 
	 * it is a generic prototype Stack<:T>
	 * @param importPackageList
	 */
	public String getFileNameWithoutExtension() {
		if ( filename.endsWith("." + NameServer.cyanSourceFileExtension) )
			return filename.substring(0, filename.length() - NameServer.cyanSourceFileExtension.length() - 1);
		else
			return filename;
	}

	public String getFullFileNamePath() {
		return fullFileNamePath;
	}

	
	public String getCanonicalPathUpDir() {
		return canonicalPathUpDir;
	}
	
	public HashMap<String, CyanMetaobjectNumber> getMetaObjectLiteralNumber() {
		return metaObjectLiteralNumberTable;
	}

	public HashMap<String, CyanMetaobjectLiteralObjectSeq> getMetaObjectLiteralObjectSeqTable() {
		return metaObjectLiteralObjectSeqTable;
	}

	public HashMap<String, CyanMetaobjectLiteralString> getMetaobjectLiteralObjectString() {
		return metaobjectLiteralStringTable;
	}

	/**
	 * search and returns a program unit in this compilation unit and in the imported packages.
	 * If more than one is found, more than one is returned.
	   @return
	 
	public ArrayList<ProgramUnit> searchProgramUnit(String prototypeName) {
		for (ProgramUnit pu : programUnitList ) {
			if ( pu.getName().equals(prototypeName) ) {
				ArrayList<ProgramUnit> puList = new ArrayList<ProgramUnit>(1);
				puList.add(pu);
				return puList;
			}
		}
		
	}
	*/
	public HashMap<String, CyanMetaobjectMacro> getMetaObjectMacroTable() {
		return metaObjectMacroTable;
	}
	
	public void setMetaObjectMacroTable(HashMap<String, CyanMetaobjectMacro> metaObjectMacroTable) {
		this.metaObjectMacroTable = metaObjectMacroTable;
	}
	

	public HashMap<String, CyanMetaobjectWithAt> getMetaObjectTable() {
		return metaObjectTable;
	}

	public char [] getText() {
		return text;
	}

	/**
	 * return true if this compilation unit has compilation errors
	 */
	public boolean hasCompilationError() {
		return getErrorList().size() > 0;
	}


	/**
	 * load all the metaobjects of list metaobjectList into the current compilation unit.
	 * If importSymbol is null, it is assumed that the metaobjects are from 
	 * package cyan.lang
	   @param metaobjectList
	   @param importSymbol, the symbol of the import statement. The metaobjects this method loads to memory 
	     are from a package imported in an import statement. This symbol is from keyword 'import' or 
	     from the first symbol of the imported package. This is used when signaling errors. 
	 */
	public void loadCyanMetaobjects(ArrayList<CyanMetaobject> metaobjectList, Symbol importSymbol, saci.Compiler compiler) {

		
		for ( CyanMetaobject cyanMetaobject : metaobjectList ) {
			if ( cyanMetaobject instanceof CyanMetaobjectWithAt ) {
				
				/*
				 * check if some metaobjects are defined correctly.
				 */
				if ( cyanMetaobject instanceof IStaticTyping ) {
					if ( !(cyanMetaobject instanceof CyanMetaobjectStaticTyping) ) {
						this.error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), 
								cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber(),  
								"Metaobject CyanMetaobjectStaticTyping  is the only allowed to implement interface IStaticTyping",
								 ErrorKind.metaobject_error, compiler);
					}
				}
				CyanMetaobjectWithAt cyanMetaobjectWithAt = (CyanMetaobjectWithAt ) cyanMetaobject;
				CyanMetaobjectWithAt cyanMetaobjectWithAtOther = metaObjectTable.get(cyanMetaobjectWithAt.getName());  
				if ( cyanMetaobjectWithAtOther != null ) {
					error(importSymbol, importSymbol.getLineNumber(), "Metaobject with name '" + 
							cyanMetaobjectWithAt.getName() + "' is imported from package "
						       + cyanMetaobjectWithAtOther.getPackageName() + " and from package " + 
							cyanMetaobjectWithAt.getPackageName() + " The names of the .class files are '" +
							cyanMetaobjectWithAt.getClass().getName() + "' and '" +    
							cyanMetaobjectWithAtOther.getClass().getName() + "'", 
							compiler, null);
					
				}
				metaObjectTable.put( cyanMetaobjectWithAt.getName(), 
						cyanMetaobjectWithAt);
			}
			else if ( cyanMetaobject instanceof CyanMetaobjectLiteralObjectSeq ) {
				CyanMetaobjectLiteralObjectSeq cmlos = (CyanMetaobjectLiteralObjectSeq ) cyanMetaobject;
				String leftCharSeq = cmlos.leftCharSequence();
				if ( metaObjectLiteralObjectSeqTable.get(leftCharSeq) != null ) {
					CyanMetaobject other = metaObjectLiteralObjectSeqTable.get(cmlos.leftCharSequence());
					error(importSymbol, importSymbol.getLineNumber(), "Literal object delimited by sequence '" + 
							cmlos.leftCharSequence() + "' is imported from metaobject '" + other.getFileName() + " of package "
						       + other.getPackageName() + " and from metaobject " + cyanMetaobject.getFileName() + 
						       " of package " + cyanMetaobject.getPackageName() + " The names of the .class files are '" +
						       cyanMetaobject.getClass().getName() + "' and '" +    
						       other.getClass().getName() + "'", compiler, null);
				}
				if ( ! Lexer.checkLeftCharSeq(leftCharSeq) ) {
					error(importSymbol, importSymbol.getLineNumber(), "Left char sequence for delimiting a literal object of metaobject " +
				        "imported from " + cyanMetaobject.getFileName() + " of package " + cyanMetaobject.getPackageName() + 
				        " is illegal", compiler, null
							);
				}
				metaObjectLiteralObjectSeqTable.put( leftCharSeq, cmlos );
			}
			/* else if ( cyanMetaobject instanceof CyanMetaobjectLiteralObjectIdentSeq ) {
				CyanMetaobjectLiteralObjectIdentSeq cmlos = (CyanMetaobjectLiteralObjectIdentSeq ) cyanMetaobject;
				metaObjectLiteralObjectIdentSeqTable.put( cmlos.getName(), cmlos );
			} */
			else if ( cyanMetaobject instanceof CyanMetaobjectNumber ) {
				CyanMetaobjectNumber cmlos = (CyanMetaobjectNumber ) cyanMetaobject;
				for ( String name : cmlos.getSuffixNames() ) {
					CyanMetaobjectNumber other = metaObjectLiteralNumberTable.put( name, cmlos );
					if ( other != null ) {
						error(importSymbol, importSymbol.getLineNumber(), "Number extension '" + 
					       name + "' is imported from metaobject '" + other.getFileName() + " of package "
					       + other.getPackageName() + " and from metaobject " + cyanMetaobject.getFileName() + 
					       " of package " + cyanMetaobject.getPackageName() + " The names of the .class files are '" +
					       cyanMetaobject.getClass().getName() + "' and '" +    
					       other.getClass().getName() + "'", compiler, null);
					}
				}
			}
			else if ( cyanMetaobject instanceof CyanMetaobjectLiteralString ) {
				CyanMetaobjectLiteralString litStr = (CyanMetaobjectLiteralString ) cyanMetaobject;
				for ( String name : litStr.getPrefixNames() ) {
					CyanMetaobjectLiteralString other = metaobjectLiteralStringTable.put( name, litStr );
					if ( other != null ) {
						error(importSymbol, importSymbol.getLineNumber(), "String prefix '" + 
					       name + "' is imported from metaobject '" + other.getFileName() + " of package "
					       + other.getPackageName() + " and of metaobject " + cyanMetaobject.getFileName() + 
					       " of package " + cyanMetaobject.getPackageName() + " The names of the .class files are '" +
					       cyanMetaobject.getClass().getName() + "' and '" +    
					       other.getClass().getName() + "'", compiler, null);
					}
				}
			}
			else if ( cyanMetaobject instanceof CyanMetaobjectMacro ) {
				CyanMetaobjectMacro cyanMacro = (CyanMetaobjectMacro ) cyanMetaobject;
				for ( String startKeyword : cyanMacro.getStartKeywords() ) {
					CyanMetaobjectMacro other = metaObjectMacroTable.put(startKeyword, cyanMacro);
					if ( other != null )
						error(importSymbol, importSymbol.getLineNumber(), "Macro '" + startKeyword + 
								"' is imported from metaobject '" + other.getFileName() + " of package "
							       + other.getPackageName() + " and from metaobject " + cyanMetaobject.getFileName() + 
							       " of package " + cyanMetaobject.getPackageName() + " The names of the .class files are '" +
							       cyanMetaobject.getClass().getName() + "' and '" +    
							       other.getClass().getName() + "'", compiler, null);
				}
			}
			else {
				/*if ( errorListMetaobject == null ) 
					errorListMetaobject = new ArrayList<String>();
				errorListMetaobject.add( */
				error(importSymbol, importSymbol.getLineNumber(), 
						"Metaobject of class '" + cyanMetaobject.getClass().getName() + "' is not allowed", compiler, null);
			}
				
		}
		//return errorListMetaobject;
	}

	/**
	 * 
	 * @param compilationUnit
	 *            , the unit to be compiled, an object or interface
	 * @return true if there was no fatal error.
	 */
	public boolean prepareLexicalAnalysis() {
	
		/*
		 * table with pre-defined metaobjects
		 */
		metaObjectTable = new HashMap<String, CyanMetaobjectWithAt>();
		metaObjectLiteralObjectSeqTable = new HashMap<>();
		// metaObjectLiteralObjectIdentSeqTable = new HashMap<>();
		metaObjectLiteralNumberTable = new HashMap<>();
		metaobjectLiteralStringTable = new HashMap<>();
		metaObjectMacroTable = new HashMap<String, CyanMetaobjectMacro>();
	
		return true;
	
	}

	public void printActionList(PrintWriter printWriter) {
		for ( Action action : actionList)
			action.print(printWriter);
	
	}

	public void printErrorList(PrintWriter printWriter) {
		for ( UnitError unitError : errorList ) {
			unitError.print(printWriter);
			System.out.println(); // ok
		}
	
	}

	public char[] readSourceFile() {
		String filenamePath = getFullFileNamePath();
		MyFile myFile = new MyFile(filenamePath);
		

		char[] input = myFile.readFile();
		if ( input == null ) {
			addError(new UnitError(null, getEntityName(), filenamePath, "",
					"File does not exist", 0, 0, this));
			return null;
		}
		else 
			originalText = text = input;
		return input;
	}

	public void setErrorList(ArrayList<UnitError> errorList) {
		this.errorList = errorList;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setMetaObjectTable(HashMap<String, CyanMetaobjectWithAt> metaObjectTable) {
		this.metaObjectTable = metaObjectTable;
	}

	public void setText(char [] text) {
		this.text = text;
	}

	public void sortActionList() {
		int size = actionList.size();
		Action []actionArray = new Action[size];
		int i = 0;
		for ( Action action : actionList)
			actionArray[i++] = action;
		Arrays.sort(actionArray);
		for (i = 0; i < size; i++)
			actionList.set(i, actionArray[i]);
	}

	public void sortErrorList() {
		int size = errorList.size();
		UnitError []unitErrorArray = new UnitError[size];
		int i = 0;
		for ( UnitError unitError : errorList )
			unitErrorArray[i++] = unitError;
		Arrays.sort(unitErrorArray);
		for (i = 0; i < size; i++)
			errorList.set(i, unitErrorArray[i]);
	}

	public void warning(Symbol symbol, int lineNumber, String msg, saci.Compiler compiler, Env env) {
		warning(symbol, lineNumber, 0, msg, compiler, env);
	}
	
	public void warning(Symbol symbol, int lineNumber, int columnNumber, String msg, saci.Compiler compiler, Env env) {
		StringBuffer line = new StringBuffer();
		if ( compiler != null ) {
			msg += this.strStackGenericPrototypeInstantiations(compiler, lineNumber, symbol != null ? symbol.getColumnNumber() : columnNumber);
		}
		else if ( env != null ) {
			msg += this.strStackGenericPrototypeInstantiations(env, lineNumber, symbol != null ? symbol.getColumnNumber() : columnNumber);
		}
		if ( symbol != null ) {
			int i = symbol.getOffset();
			while ( i >= 0 && text[i] != '\n' && text[i] != '\r' ) 
				--i;
			++i;
			
			//int i = symbol.startLine;
			while ( i < text.length && text[i] != '\n' && text[i] != '\r' && text[i] != '\0' )
				line.append(text[i++]);
			
			errorList.add(new UnitError(symbol, this.getEntityName(), fullFileNamePath, line
					.toString(), msg, lineNumber, symbol
					.getColumnNumber(), this));
		}
		else
			errorList.add( new UnitError(null, this.getEntityName(), filename, "",
					msg, lineNumber, columnNumber, this) );
	
	}

	public void error(int lineNumber, int columnNumber, String msg) {

		StringBuffer line = new StringBuffer();

		int lineCount = 1;
		int j = 0;
		while ( text[j] != '\0' && j < text.length ) {
			if ( lineCount == lineNumber )
				break;
			if ( text[j] == '\n' )
				lineCount++;
			++j;
		}
		int offset = j + columnNumber;
		int i = offset;
		while ( i >= 0 && text[i] != '\n' && text[i] != '\r' ) 
			--i;
		++i;
		
		//int i = symbol.startLine;
		while ( i < text.length && text[i] != '\n' && text[i] != '\r' && text[i] != '\0' )
			line.append(text[i++]);
		
		
		
		errorList.add(new UnitError(null, this.getEntityName(), fullFileNamePath, line
				.toString(), msg, lineNumber, columnNumber, this));
			
	}
	
	
	public String strStackGenericPrototypeInstantiations(saci.Compiler compiler, int lineNumber, int columnNumber) {
		
		String s = "";
		if ( this instanceof CompilationUnit ) {
			CompilationUnit previousCompUnit = (CompilationUnit ) this; // programUnitInstantiation.getCompilationUnit();
			if ( previousCompUnit.getPackageNameInstantiation() != null && 
					previousCompUnit.getPrototypeNameInstantiation() != null ) {
				s = "\n" + "Stack of generic prototype instantiations: \n" ;
				String protoName = previousCompUnit.getPublicPrototype().getNameWithOuter();
				s += "    " + previousCompUnit.getPackageName() + "." + protoName + " line " + 
						+  lineNumber + " column " + columnNumber + "\n";
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
			
		}
		return s;
	}
	
	
	public String strStackGenericPrototypeInstantiations(Env env, int lineNumber, int columnNumber) {
		
		String s = "";
		if ( this instanceof CompilationUnit ) {
			CompilationUnit previousCompUnit = (CompilationUnit ) this; // programUnitInstantiation.getCompilationUnit();
			if ( previousCompUnit.getPackageNameInstantiation() != null && 
					previousCompUnit.getPrototypeNameInstantiation() != null ) {
				s = "\n" + "Stack of generic prototype instantiations: \n" ;
				s += "    " + previousCompUnit.getPackageName() + "." + previousCompUnit.getPublicPrototype().getName() + " line " + 
						+  lineNumber + " column " + columnNumber + "\n";

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
			
		}
		return s;
	}
	
	
	public String getPackageName() {
		// packageIdent == null in .pyan files
		return packageIdent == null ? "" : packageIdent.getName();
	}

	public void setPackageIdent(ExprIdentStar packageIdent) {
		this.packageIdent = packageIdent;
	}

	public ExprIdentStar getPackageIdent() {
		return packageIdent;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getCodegList() {
		return codegList;
	}

	public void setCodegList(ArrayList<CyanMetaobjectWithAtAnnotation> codegList) {
		this.codegList = codegList;
	}

	public void setOriginalText(char[] originalText) {
		this.originalText = originalText;
	}
	

	public Set<CyanPackage> getImportPackageSet() {
		return importPackageSet;
	}

	public void setImportPackageSet(Set<CyanPackage> importPackageSet) {
		this.importPackageSet = importPackageSet;
	}
	
	
	/**
	 * A table with metaobjects imported by this package. It contains the metaobjects
	 * of cyan.lang and of all packages imported by this compilation unit.
	 */
	private HashMap<String, CyanMetaobjectWithAt>	metaObjectTable;
	/**
	 * a table with literal metaobjects that start with a number and ends with a sequence of 
	 * letters such as 
	 *          0101Bin     03FG4_hex
	 */
	private HashMap<String, CyanMetaobjectNumber>	metaObjectLiteralNumberTable;
	/**
	 * a table with literal metaobjects that represent literal strings preceded by 
	 * a single letter or symbol such as:
	 *        r"[A-Za-z_][A-Za-z_0-9]*" 
	 *        %"letter and underscore followed by letter and underscore and number"  
	 */
	private HashMap<String, CyanMetaobjectLiteralString>	metaobjectLiteralStringTable;
	/**
	 * table with literal metaobjects that is delimited by a sequence of symbols such as
	 * 
	 *       [*  "one":1, "two":2 *]
	 *       /[A-Za-z]+/
	 */
	private HashMap<String, CyanMetaobjectLiteralObjectSeq>	metaObjectLiteralObjectSeqTable;
	/**
	 * a table with all macros imported by this compilation units. The 'key' of the table is 
	 * the start macro keyword such as "assert" in macro assert:
	 *        assert n >= 0;
	 */
	private HashMap<String, CyanMetaobjectMacro>	metaObjectMacroTable;
	/**
	 * List of errors found in this compilation unit --- in file filename
	 */
	protected ArrayList<UnitError>	errorList;

	/**
	 * If the name of the file of this compilation unit is "Name.cyan",
	 * then the only public object or interface declared in it should be "Name".
	 *  
	 */
	protected String	publicObjectInterfaceName;
	/**
	 * text of the compilation unit - complete source code of the file. The text
	 * suffer additions and changes through the compilation units --- see the Figure of 
	 * chapter "Metaobjects" of the Cyan manual. Therefore the text changes during
	 * the compilation process. 
	 */
	private char []	text;
	/**
	 * the original source code of this compilation unit. This text is not changed
	 * by the compiler or metaobjects (see {@See #text}).
	 */
	protected char []	originalText;
	/**
	 * file name of this compilation unit
	 */
	protected String	filename;
	/**
	 * complete path of the filename
	 */
	protected String	fullFileNamePath;
	/**
	 * list of actions to be performed on this compilation unit. The file
	 * itself is modified.
	 */
	protected ArrayList<Action>	actionList;


	/**
	 * the canonical path of the up directory. In a compilation unit, it is the path of the package.
	 * In a project, it is the project path
	 */
	private String canonicalPathUpDir;
	/**
	 * identifier of the package of this compilation unit
	 * It may be a composite identifier such as
	 *      cyan.util.Stack
	 *      
	 * If the compilation unit is a Pyan source file, it should be null. Bad thing, it will be corrected
	 * some time.
	 */
	private ExprIdentStar	packageIdent;
	
	/**
	 * keeps all metaobject annotations of the current compilation unit whose metaobject classes are codegs
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> codegList;

	/**
	 * set with the imported packages of this compilation unit
	 */
	Set<CyanPackage> importPackageSet;

}