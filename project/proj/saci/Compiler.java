package saci;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;
import ast.CaseRecord;
import ast.CompilationUnit;
import ast.CompilationUnitDSL;
import ast.CompilationUnitSuper;
import ast.ContextParameter;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectLiteralObjectAnnotation;
import ast.CyanMetaobjectMacroCall;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Expr;
import ast.ExprAnyLiteral;
import ast.ExprAnyLiteralIdent;
import ast.ExprBooleanAnd;
import ast.ExprBooleanOr;
import ast.ExprFunction;
import ast.ExprFunctionRegular;
import ast.ExprFunctionWithSelectors;
import ast.ExprGenericPrototypeInstantiation;
import ast.ExprIdentStar;
import ast.ExprIndexed;
import ast.ExprLiteralArray;
import ast.ExprLiteralBooleanFalse;
import ast.ExprLiteralBooleanTrue;
import ast.ExprLiteralByte;
import ast.ExprLiteralChar;
import ast.ExprLiteralCyanSymbol;
import ast.ExprLiteralDouble;
import ast.ExprLiteralFloat;
import ast.ExprLiteralInt;
import ast.ExprLiteralLong;
import ast.ExprLiteralMap;
import ast.ExprLiteralNil;
import ast.ExprLiteralShort;
import ast.ExprLiteralString;
import ast.ExprLiteralTuple;
import ast.ExprMessageSendUnaryChain;
import ast.ExprMessageSendUnaryChainToExpr;
import ast.ExprMessageSendUnaryChainToSuper;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.ExprMessageSendWithSelectorsToSuper;
import ast.ExprNonExpression;
import ast.ExprObjectCreation;
import ast.ExprSelf;
import ast.ExprSelfPeriodIdent;
import ast.ExprSelf__;
import ast.ExprSelf__PeriodIdent;
import ast.ExprSurroundedByContext;
import ast.ExprTypeof;
import ast.ExprUnary;
import ast.ExprWithParenthesis;
import ast.GenericParameter;
import ast.GenericParameter.GenericParameterKind;
import ast.InstanceVariableDec;
import ast.InterfaceDec;
import ast.JVMPackage;
import ast.MessageBinaryOperator;
import ast.MessageSendToMetaobjectAnnotation;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureGrammar;
import ast.MethodSignatureOperator;
import ast.MethodSignatureUnary;
import ast.MethodSignatureWithSelectors;
import ast.NotNilRecord;
import ast.ObjectDec;
import ast.ParameterDec;
import ast.Program;
import ast.ProgramUnit;
import ast.Selector;
import ast.SelectorGrammar;
import ast.SelectorGrammarList;
import ast.SelectorGrammarOrList;
import ast.SelectorWithMany;
import ast.SelectorWithParameters;
import ast.SelectorWithRealParameters;
import ast.SelectorWithTypes;
import ast.Statement;
import ast.StatementAssignmentList;
import ast.StatementBreak;
import ast.StatementFor;
import ast.StatementIf;
import ast.StatementList;
import ast.StatementLocalVariableDec;
import ast.StatementLocalVariableDecList;
import ast.StatementMetaobjectAnnotation;
import ast.StatementMinusMinusIdent;
import ast.StatementNotNil;
import ast.StatementNull;
import ast.StatementPlusPlusIdent;
import ast.StatementReturn;
import ast.StatementReturnFunction;
import ast.StatementType;
import ast.StatementWhile;
import ast.TypeJavaRef;
import ast.VariableDecInterface;
import ast.VariableKind;
import error.CompileErrorException;
import error.ErrorKind;
import error.UnitError;
import lexer.CompilerPhase;
import lexer.Lexer;
import lexer.Symbol;
import lexer.SymbolCharSequence;
import lexer.SymbolCyanMetaobjectAnnotation;
import lexer.SymbolCyanSymbol;
import lexer.SymbolIdent;
import lexer.SymbolKeyword;
import lexer.SymbolLiteralObject;
import lexer.SymbolLiteralObjectParsedWithCompiler;
import lexer.SymbolOperator;
import lexer.Token;
import meta.CompilerAction_dpa;
import meta.CompilerGenericProgramUnit_dpa;
import meta.CompilerMacro_dpa;
import meta.Compiler_dpa;
import meta.CyanMetaobject;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectLiteralString;
import meta.CyanMetaobjectMacro;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionAssignment_cge;
import meta.IActionGenericProgramUnit_dpa;
import meta.IActionNewPrototypes_dpa;
import meta.IAction_cge;
import meta.IAction_dpa;
import meta.IAction_dpp;
import meta.IAction_dsa;
import meta.ICodeg;
import meta.ICompilerAction_dpa;
import meta.ICompilerInfo_dpa;
import meta.IInformCompilationError;
import meta.IListAfter_ati;
import meta.IParseWithCyanCompiler_dpa;
import meta.IParseWithoutCyanCompiler_dpa;
import meta.IStaticTyping;
import meta.MetaInfoServer;
import meta.cyanLang.CyanMetaobjectCompilationContextPop;
import meta.cyanLang.CyanMetaobjectCompilationContextPush;
import meta.cyanLang.CyanMetaobjectCompilationMarkDeletedCode;
import refactoring.ActionDelete;
import refactoring.ActionInsert;


/**
 * This class compiles a single compile unit which can be an object or an interface.
 *
 * @author José
 *
 */
public final class Compiler implements Cloneable {

	static private Hashtable<String, String> falseKeywordsTable;

	static private Set<String> basicTypesSet;



	/**
	 *
	   @param compilationUnit, the compilation unit (source code) to be compiled
	   @param compInstSet, the instructions to the compilation
	 */
	public Compiler(CompilationUnitSuper compilationUnit, HashSet<saci.CompilationInstruction> compInstSet,
			        CompilationStep compilationStep, Project project, ArrayList<Tuple2<String, byte[]>> codegNameWithCodegFile
			        //, Env notNullIfCreatingGenericPrototypeInstantiation
			        ) {


		this.compilationUnitSuper = compilationUnit;

		if ( compilationUnit instanceof CompilationUnit ) {
			this.compilationUnit = (CompilationUnit ) compilationUnit;
		}

		this.compInstSet = compInstSet;
		this.compilationStep = compilationStep;
		this.project = project;
		if ( project != null ) {
			this.program = this.project.getProgram();
		}
		this.codegNameWithCodegFile = codegNameWithCodegFile;

		String fileName = compilationUnit.getFilename();
		if ( fileName.endsWith(NameServer.ScriptCyanExtension) ) {
			this.scriptCyan = true;
			/*
			 * initially consider that the source code has only statements. If keyword <code>func</code> is found,
			 * this variable is set to <code>false</code>.
			 */
			scriptCyanStatementsOnly = true;
		}
		else
			this.scriptCyan = false;
		forceUseOfStaticTyping = false;
		//redo
		/* symbolArray = compilationUnit.getSymbolArray();
		isa = 0;
		sizeSymbolArray = compilationUnit.getSizeSymbolArray();  */

		nextSymbolList = new Symbol[3];
		nextSymbolList[0] = nextSymbolList[1] = nextSymbolList[2] = null;

		compilationUnit.prepareLexicalAnalysis();
		metaObjectMacroTable = compilationUnit.getMetaObjectMacroTable();
		char []text = compilationUnit.getText();

		sizeSymbolList = 0;
		if ( symbolList == null || symbolListAllocatedSize < text.length ) {
			/*
			 * if symbolList has not been allocated before, allocate it now. If the new text is larger
			 * than before, allocate again
			 */
			symbolList = new Symbol[text.length + 1];
			symbolListAllocatedSize = text.length;
		}

		lexer = new Lexer(text, compilationUnit, compInstSet, this);
		symbol = lexer.symbol;


		symbolList[sizeSymbolList] = symbol;
		++sizeSymbolList;



		parameterDecStack = new Stack<ParameterDec>() ;
		localVariableDecStack = new Stack<StatementLocalVariableDec>();
		functionCounter = 0;
		functionStack = new Stack<ExprFunction>();
		objectDecStack = new Stack<ObjectDec>();
		cyanMetaobjectContextStack = new Stack<>();
		lineShift = 0;
		codegList = new ArrayList<>();
		insideCyanMetaobjectCompilationContextPushAnnotation = false;
		mayBeWrongVarDeclaration = false;
		prohibitTypeof = false;
		allowCreationOfPrototypesInLastCompilerPhases = false;
		this.lineNumberStartCompilationContextPush = -1;

		// this.notNullIfCreatingGenericPrototypeInstantiation = notNullIfCreatingGenericPrototypeInstantiation;
		/*
		 * change method clone too
		 */
	}

	@SuppressWarnings("unchecked")
	@Override
	public Compiler clone() {

		try {
		    Compiler clone = (Compiler ) super.clone();

		    // do not clone compilationUnit
		    /*
		    if ( this.compilationUnit != null ) {
			    clone.compilationUnit = this.compilationUnit.clone();
		    }
		    */
		    if ( compilationUnit != null ) {
				clone.compilationUnitSuper = compilationUnit;
		    }

			clone.parameterDecStack = (Stack<ParameterDec>) clone.parameterDecStack.clone();
			clone.localVariableDecStack = (Stack<StatementLocalVariableDec>) localVariableDecStack.clone();
			clone.functionStack = (Stack<ExprFunction>) functionStack.clone();
			clone.objectDecStack = (Stack<ObjectDec>) objectDecStack.clone();
			clone.cyanMetaobjectContextStack = (Stack<Tuple5<String, String, String, String, Integer>>) cyanMetaobjectContextStack.clone();
			clone.codegList = (ArrayList<CyanMetaobjectWithAtAnnotation>) codegList.clone();
		    return clone;
		}
		catch ( CloneNotSupportedException e ) {
			return null;
		}
	}


	static {
		falseKeywordsTable = new Hashtable<String, String>();
		falseKeywordsTable.put("byte", "Byte");
		falseKeywordsTable.put("short", "Short");
		falseKeywordsTable.put("int", "Int");
		falseKeywordsTable.put("long", "Long");
		falseKeywordsTable.put("float", "Float");
		falseKeywordsTable.put("double", "Double");
		falseKeywordsTable.put("char", "Char");
		falseKeywordsTable.put("boolean", "Boolean");

		basicTypesSet = new HashSet<String>();
		basicTypesSet.add("Byte");
		basicTypesSet.add("Short");
		basicTypesSet.add("Int");
		basicTypesSet.add("Long");
		basicTypesSet.add("Float");
		basicTypesSet.add("Double");
		basicTypesSet.add("Char");
		basicTypesSet.add("Boolean");
		basicTypesSet.add("String");

	}

	public void setInitialPositionLexer(int offset) {
		lexer.setInitialPositionLexer(offset);
	}
	private int lineNumberLastSymbolInThisText = 0;

	private int lineNumberStartCompilationContextPush;



	/**
	 * return the line shift, the number of lines that the compiler added before the current symbol.
	 * However, if the current symbol is inside code introduced by a metaobject annotation then
	 * the value returned is -1.
	   @return
	 */
	public int getLineShift() {
		if ( cyanMetaobjectContextStack.isEmpty() && ! insideCyanMetaobjectCompilationContextPushAnnotation )
			return lineShift;
		else
			return -1;
	}

	/**
	 * copy all variables related to lexical analysis from 'from' to this
	   @param from
	 */
	public void copyLexerData(Compiler from) {
		this.previousSymbol = from.previousSymbol;
		this.symbol = from.symbol;
		this.nextSymbolList = from.nextSymbolList;
	}

	public void next() {
		//redo
		previousSymbol = symbol;
		if ( nextSymbolList[0] == null ) {
			lexer.next();
			symbol = lexer.symbol;

			/*
			if ( symbol.getSymbolString().equals("fff3:") ) {
				System.out.print("");
			}
			*/

			this.lineNumberLastSymbolInThisText = symbol.getLineNumber();
			if ( cyanMetaobjectContextStack.isEmpty() && ! insideCyanMetaobjectCompilationContextPushAnnotation ) {
				symbol.setLineNumber(symbol.getLineNumber() - lineShift);
			}
		}
		else {
			symbol = nextSymbolList[0];
			nextSymbolList[0] = nextSymbolList[1];
			nextSymbolList[1] = nextSymbolList[2];
			nextSymbolList[2] = null;
		}

		/*
		symbol = symbolArray[isa];
		if ( isa < sizeSymbolArray )
			isa++;
		*/
	}

	/**
	 * return the n-th symbol from the current symbol. If n is 0, the next symbol
	 * is returned. n should be smaller than 3
	 * @param n
	 * @return
	 */
	private Symbol next(int n) {

		switch ( n ) {
		case 0:
			if ( nextSymbolList[0] == null ) {
				lexer.next();
				nextSymbolList[0] = lexer.symbol;
			}
			return nextSymbolList[0];
		case 1:
			if ( nextSymbolList[0] == null ) {
				lexer.next();
				nextSymbolList[0] = lexer.symbol;
			}
			if ( nextSymbolList[1] == null ) {
				lexer.next();
				nextSymbolList[1] = lexer.symbol;
			}
			return nextSymbolList[1];
		case 2:
			if ( nextSymbolList[0] == null ) {
				lexer.next();
				nextSymbolList[0] = lexer.symbol;
			}
			if ( nextSymbolList[1] == null ) {
				lexer.next();
				nextSymbolList[1] = lexer.symbol;
			}
			if ( nextSymbolList[2] == null ) {
				lexer.next();
				nextSymbolList[2] = lexer.symbol;
			}
			return nextSymbolList[2];
		default:
			error(true, symbol, "Internal error at Compiler::next(int): n is " + n, null, ErrorKind.internal_error);
			return null;
		}
		//redo

		/*
		if ( isa + n < sizeSymbolArray )
			return symbolArray[isa + n];
		else
			return null;  */
	}

	private void  getLeftCharSequence() {
		try {
			lexer.getLeftCharSequence();
		}
		catch ( CompileErrorException e ) {
			lexer.next();
			throw e;
		}
		finally {
			symbol = lexer.symbol;
		}
	}

	//redo
	/**
	 * returns the symbol that precedes the current one
	 * @return

	private Symbol previousSymbol() {
		if ( isa > 1 )
		    return symbolArray[isa - 2];
		else
			return symbolArray[isa];
	}

	*/

	/**
	 * insert a symbol at the current position of the array of symbols produced by the
	 * lexer
	 */
	private void insertSymbol(Symbol s) {
		nextSymbolList[2] = nextSymbolList[1];
		nextSymbolList[1] = nextSymbolList[0];
		nextSymbolList[0] = s;
	}



	public void parseProject(Project newProject, Program program, CompilationUnitSuper projectCompilationUnit, String projectFilename2,
			String canonicalPath,
            char []text, String cyanLangDir) {
		/*
		public CompilationUnit(String filename, String packageCanonicalPath,
		             CompilerOptions compilerOptions,
		             CyanPackage cyanPackage) {
		*
		*/
		// CompilationUnit fakeCompilationUnit = new CompilationUnit(projectFilename2, canonicalPath, compilerOptions, null);

		lineShift = 0;
 		this.projectFilename = projectFilename2;
		lexer = new Lexer(text, projectCompilationUnit, compInstSet, this);
		try {
			parseProject(newProject, program, cyanLangDir);
			newProject.setProjectDir(canonicalPath);
		}
		catch ( CompileErrorException e ) {
		}
	}


	/**
        Program ::= { ImportList } [ CTmetaobjectAnnotationList ] “program” [ AtFolder ]
            [ “main” QualifId ]
            { CTmetaobjectAnnotationList Package }
        ImportList ::= “import” QualifId AtFolder
        Package ::= “package” QualifId [ AtFolder ]
        AtFolder ::= “at” FileName
        CTmetaobjectAnnotationList ::= { annotation }
        annotation ::= “@” Id
            [ “(” ExprLiteral [ “,” ExprLiteral ] “)” ]
            [ LeftCharString TEXT RightCharString ]
        QualifId ::= { Id “.” } Id


	 */
	public void parseProject(Project newProject, Program program, String cyanLangDir) {

		String fileSeparator = System.getProperty("file.separator");

		compInstSet = new HashSet<>();
		compInstSet.add(CompilationInstruction.dpa_actions);
		compInstSet.add(CompilationInstruction.pyanSourceCode);


		ArrayList<String> importList = new ArrayList<>();


		project = newProject;


		/**
		 * load metaobjects from package cyan.lang
		 */
		ArrayList<CyanMetaobject> cyanLangMetaobjectList = new ArrayList<>();
		String fullCyanLangDir = cyanLangDir;
		if ( cyanLangDir.endsWith(fileSeparator) )
			fullCyanLangDir += NameServer.cyanLanguagePackageDirectory;
		else
			fullCyanLangDir += fileSeparator + NameServer.cyanLanguagePackageDirectory;

		try {
			CompilerManager.loadMetaobjectsFromPackage(fullCyanLangDir, NameServer.cyanLanguagePackageName, cyanLangMetaobjectList, this);
		}
		catch ( RuntimeException e ) {
			// e.printStackTrace();
			for ( UnitError error : this.compilationUnit.getErrorList() ) {
				project.error(error.getMessage());
			}
			return ;
		}


		// read  the metaobjects of cyan.lang just one time.
		// get rid of Program.setCyanMetaobjectTable

		compilationUnit.loadCyanMetaobjects( cyanLangMetaobjectList, symbol, this);
		lexer.addMetaObjectLiteralObjectSeqTable(compilationUnit.getMetaObjectLiteralObjectSeqTable());
		lexer.addMetaobjectLiteralNumberTable(compilationUnit.getMetaObjectLiteralNumber());
		lexer.addMetaobjectLiteralStringTable(compilationUnit.getMetaobjectLiteralObjectString());




		if ( symbol.token == Token.IMPORT  ) {
			// importPackagePyan(fileSeparator);
			this.importPackagePyan(fileSeparator, (String packageName2, String atPackage2) -> {
				project.addNamePackageImportList(packageName2);
				project.addPathPackageImportList(atPackage2);
			   },
			   (ArrayList<CyanMetaobject> metaobjectList2, Symbol importSymbol2) -> {
					compilationUnit.loadCyanMetaobjects(metaobjectList2, importSymbol2, this);
			   }

			);

		}


		/*
		 * the metaobject annotations may or may not be attached to the program
		 */


		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList = null;
		ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList = null;

		Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tc = parseMetaobjectAnnotations_NonAttached_Attached();
		if ( tc != null ) {
			nonAttachedMetaobjectAnnotationList = tc.f1;
			attachedMetaobjectAnnotationList = tc.f2;
		}

		String atProgram = null;
		if ( symbol.token != Token.IDENT && ! symbol.getSymbolString().equals("program") )
			error(true, symbol, "'program' expected. Found '" + symbol.getSymbolString() + "'", "", ErrorKind.keyword_program_expected );
		else
			next();


		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation :  attachedMetaobjectAnnotationList ) {
				CyanMetaobjectWithAt cyanMetaobject = annotation.getCyanMetaobject();
				if ( ! cyanMetaobject.mayBeAttached(DeclarationKind.PROGRAM_DEC) ) {
					this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
							"This metaobject annotation cannot be attached to a program. It can be attached to " +
								       " one entity of the following list: [ "+
								       cyanMetaobject.attachedListAsString() + " ]", null, ErrorKind.metaobject_error);


				}
				if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
					ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
					Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
					if ( t != null )
						program.addFeature(t);
				}
			}
		}
		program.setAttachedMetaobjectAnnotationList(attachedMetaobjectAnnotationList);
		program.setNonAttachedMetaobjectAnnotationList(nonAttachedMetaobjectAnnotationList);




		Symbol atSymbol = null;
		if ( symbol.token == Token.IDENT && symbol.getSymbolString().equals("at") ) {
			atSymbol = symbol;
			next();
			if ( symbol.token != Token.LITERALSTRING )
				error(true, symbol, "A literal string with a directory (folder) was expected." + foundSuch(),
						"", ErrorKind.literal_string_expected);
			atProgram = symbol.getSymbolString();
			next();
		}
		String projectDir;
		if ( atProgram != null ) {
			File f = new File(atProgram);
			if ( ! f.exists() ) {
				error(true, atSymbol, "File '" + atProgram + "' does not exist", "",
						ErrorKind.file_does_not_exist, "filename = " + atProgram);
			}
			if ( f.isDirectory() ) {
				projectDir = atProgram;
				if ( ! projectDir.endsWith(fileSeparator) )
					projectDir += fileSeparator;
			}
			else {

				project.error("In the project, file '" + atProgram + "' should be a directory");
				return ;
			}
		}
		else {
			projectDir = this.compilationUnitSuper.getCanonicalPathUpDir();
		}

		String mainObject;
		String mainPackage;

		if ( symbol.token == Token.IDENT && symbol.getSymbolString().equals("main") ) {
			next();
			Symbol mainSymbol = symbol;
			ExprIdentStar mainPrototype = ident();
			String s = mainPrototype.asString();
			int indexLastDot = s.lastIndexOf('.');
			if ( indexLastDot < 1 || indexLastDot == s.length() - 1 ) {
				this.error2(mainSymbol, "After 'main' it was expected the complete name of the main prototype, with the package, as in 'br.main.Program'");
			}
			mainPackage = s.substring(0, indexLastDot);
			mainObject = s.substring(indexLastDot + 1);
		}
		else {
			mainObject = "Program";
			mainPackage = "main";
		}
		// Program fakeProgram = new Program();
		/*
		Project project = new Project( program, mainPackage, mainObject,
	               null, null, null, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, importList);
		 *
		 */
		project.setMainPackage(mainPackage);
		project.setMainObject(mainObject);
		project.setNonAttachedMetaobjectAnnotationList(nonAttachedMetaobjectAnnotationList);
		project.setAttachedMetaobjectAnnotationList(attachedMetaobjectAnnotationList);
		project.setImportList(importList);

		project.setCompilerOptions(null);

		program.setCyanMetaobjectTable();

		while ( symbol.token == Token.PACKAGE || symbol.token == Token.METAOBJECT_ANNOTATION || symbol.token == Token.IMPORT ||
				(symbol.getSymbolString().equals("jvmimport") && symbol.token == Token.IDENT )
				) {

			if ( symbol.token == Token.IDENT ) {
				// jvmimport
				next();
				if ( symbol.token != Token.LITERALSTRING )
					error(true, symbol, "A literal string with a directory (folder) was expected." + foundSuch(),
							"", ErrorKind.literal_string_expected);
				String jvmDir = symbol.getSymbolString();
				next();

				continue;
			}

			final ArrayList<String> packageNamePackageImportList = new ArrayList<>();
			final ArrayList<String> packagePathPackageImportList = new ArrayList<>();
			final ArrayList<ArrayList<CyanMetaobject>> packageMetaobjectList = new ArrayList<>();
			/*
			 * the tables are restored to their original values after this package
			 */
			HashMap<String, CyanMetaobjectMacro>	metaObjectMacroTable1 = this.compilationUnit.getMetaObjectMacroTable();
			HashMap<String, CyanMetaobjectWithAt>	metaObjectTable = this.compilationUnit.getMetaObjectTable();

			if ( symbol.token == Token.IMPORT  ) {

				this.importPackagePyan(fileSeparator, (String packageName2, String atPackage2) -> {
					packageNamePackageImportList.add(packageName2);
					packagePathPackageImportList.add(atPackage2);
				   },
				   (ArrayList<CyanMetaobject> metaobjectList2, Symbol importSymbol2) -> {
					   packageMetaobjectList.add(metaobjectList2);

						compilationUnit.loadCyanMetaobjects(metaobjectList2, importSymbol2, this);

				   }

				);

			}





			/*
			 * the metaobject annotations may or may not be attached to the package
			 */

			attachedMetaobjectAnnotationList = null;
			nonAttachedMetaobjectAnnotationList = null;

			tc = parseMetaobjectAnnotations_NonAttached_Attached();
			if ( tc != null ) {
				nonAttachedMetaobjectAnnotationList = tc.f1;
				attachedMetaobjectAnnotationList = tc.f2;
			}

			if ( attachedMetaobjectAnnotationList != null ) {
				for ( CyanMetaobjectWithAtAnnotation annotation :  attachedMetaobjectAnnotationList ) {
					CyanMetaobjectWithAt cyanMetaobject = annotation.getCyanMetaobject();
					if ( ! cyanMetaobject.mayBeAttached(DeclarationKind.PACKAGE_DEC) ) {
						this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
								"This metaobject annotation cannot be attached to a package. It can be attached to " +
									       " one entity of the following list: [ "+
									       cyanMetaobject.attachedListAsString() + " ]",
										null, ErrorKind.metaobject_error);

					}
				}
			}
			if ( symbol.token != Token.PACKAGE )
				error(true, symbol, "'package' expected." + foundSuch(), "", ErrorKind.keyword_package_expected);
			else
				next();


			ExprIdentStar packageId = ident();
			String packageName = packageId.asString();
			String s2 = "";
			int size = packageName.length();
			char ch;
			for (int i = 0; i < size; ++i)
				s2 = s2 + ((ch = packageName.charAt(i)) == '.' ? fileSeparator : "" + ch);
			String atPackage = projectDir + s2;
			if ( symbol.token == Token.IDENT && symbol.getSymbolString().equals("at") ) {
				next();
				if ( symbol.token != Token.LITERALSTRING )
					error(true, symbol, "A literal string with a directory (folder) was expected." + foundSuch(),
							"", ErrorKind.literal_string_expected);
				atPackage = symbol.getSymbolString();
				next();
			}
			String packageCanonicalPath = atPackage + fileSeparator;  // C:\Dropbox\Cyan\lib\    packageName: "cyan.util"
			/*
			 * if packageName is "cyan.util", npn is "cyan\\util"
			 */

			if ( packageName.equalsIgnoreCase(NameServer.cyanLanguagePackageName) ) {
				error2(packageId.getFirstSymbol(), "Package cyan.lang cannot be specified in the project file");
			}

			if ( packageCanonicalPath.contains(NameServer.temporaryDirName) ) {
				error(true, symbol, "package '" + packageName + "' is in directory '" + packageCanonicalPath
						+ "' which is illegal because it has the string '" + NameServer.temporaryDirName + "' in it. " +
						"This may occur because the compiler was not able to delete the temporary directory of the previous compilation. Delete" +
						" it yourself",
						"", ErrorKind.file_error);
			}

			// **************************

			ArrayList<CyanMetaobject> metaobjectList = new ArrayList<>();

			try {
				CompilerManager.loadMetaobjectsFromPackage(packageCanonicalPath, packageName, metaobjectList, this);
			}
			catch ( RuntimeException e ) {
				// e.printStackTrace();
				for ( UnitError error : this.compilationUnit.getErrorList() ) {
					project.error(error.getMessage());
				}
				return ;
			}
			// **************************

			CyanPackage cyanPackage = createCyanPackage(program, project, nonAttachedMetaobjectAnnotationList,
					attachedMetaobjectAnnotationList, packageName,
					packageCanonicalPath, metaobjectList);




			if ( project.searchPackage(packageName) != null )
				project.error("Package '" + packageName + "' is duplicated in this project");

			project.addCyanPackage(cyanPackage);


			if ( attachedMetaobjectAnnotationList != null ) {
				for ( CyanMetaobjectAnnotation annotation : attachedMetaobjectAnnotationList ) {

					CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
					if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
						ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
						Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
						if ( t != null )
							cyanPackage.addFeature(t);
					}
				}
			}
			/**
			 * restore the metaobject tables to the values of the Program, eliminating any metaobjects specific
			 * to this package
			 */
			compilationUnit.setMetaObjectMacroTable(metaObjectMacroTable1);
			compilationUnit.setMetaObjectTable(metaObjectTable);


			/*
			if ( cyanPackage.getCompilationUnitList().size() > 0 ) {
				// directories without .cyan files are not considered packages
			}
			*/
   	    }

		/*
		 * add packages of projectDir to the project. But only those that have not been added explicitly
		 * in the .pyan file
		 */
		ArrayList<String> projPath = new ArrayList<String>();
		String projectDirWithoutSlash = projectDir;
		if ( projectDir.endsWith(fileSeparator) )
			projectDirWithoutSlash = projectDir.substring(0, projectDir.length()-1);
		projPath.add(projectDirWithoutSlash);

		ArrayList<String> projCyanName = new ArrayList<String>();
		projCyanName.add("");
		String strError = Saci.getAllProjects(projPath, projCyanName, 0);
		if ( strError != null ) {
			project.error(strError);
		}
		projPath.remove(0); //
		projCyanName.remove(0);

		for ( String packageName : projCyanName ) {

			if ( !packageName.contains(NameServer.prefixNonPackageDir) &&
					!packageName.endsWith(NameServer.temporaryDirName) && project.searchPackage(packageName) == null ) {
				String packageCanonicalPath = projectDir;  // C:\Dropbox\Cyan\lib\    packageName: "cyan.util"

				String npn = packageName.replace(".", fileSeparator) + fileSeparator;
				if ( !packageCanonicalPath.endsWith(npn) )
					packageCanonicalPath += npn;

				if ( packageName.equalsIgnoreCase(NameServer.cyanLanguagePackageName) ) {
					project.error("Package cyan.lang cannot be specified in the project file. It is in '" + packageCanonicalPath + "'");
				}

				// **************************

				ArrayList<CyanMetaobject> metaobjectList = new ArrayList<>();

				try {
					CompilerManager.loadMetaobjectsFromPackage(packageCanonicalPath, packageName, metaobjectList, this);
				}
				catch ( RuntimeException e ) {
					// e.printStackTrace();
					for ( UnitError error : this.compilationUnit.getErrorList() ) {
						project.error(error.getMessage());
					}
					return ;
				}
				// **************************

				CyanPackage cyanPackage = createCyanPackage(program, project, null,
						null, packageName,
						packageCanonicalPath, metaobjectList);


				//if ( cyanPackage.getCompilationUnitList().size() > 0 ) {
					// directories without .cyan files are not considered packages
					project.addCyanPackage(cyanPackage);
				//}

				//project.addCyanPackage(cyanPackage);

			}

		}


		if ( symbol.token != Token.EOF ) {
			this.error2(symbol,  "Unidentified symbol: '" + symbol.getSymbolString() + "'");
		}
		// String fullCyanLangDir = cyanLangDir + fileSeparator + NameServer.cyanLanguagePackageDirectory;

		CyanPackage cyanLangPackage = createCyanPackage(program, project, new ArrayList<CyanMetaobjectWithAtAnnotation>(),
				new ArrayList<CyanMetaobjectWithAtAnnotation>(),
				NameServer.cyanLanguagePackageName, fullCyanLangDir, cyanLangMetaobjectList);


		//# add metaobjects of cyan.lang
		project.setProjectVariable("cyanLangDir", cyanLangDir);
		project.setProjectVariable("fullCyanLangDir", fullCyanLangDir);

		project.addCyanPackage(cyanLangPackage);
		project.setCyanLangPackage(cyanLangPackage);
		program.setCyanLangPackage(cyanLangPackage);


		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation :  attachedMetaobjectAnnotationList ) {
				CyanMetaobjectWithAt cyanMetaobject = annotation.getCyanMetaobject();
				if ( ! cyanMetaobject.mayBeAttached(DeclarationKind.PROGRAM_DEC) ) {
					this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
							"This metaobject annotation cannot be attached to a program. It can be attached to " +
								       " one entity of the following list: [ "+
								       cyanMetaobject.attachedListAsString() + " ]", null, ErrorKind.metaobject_error);

				}
				else {
					annotation.setDeclaration(program);
				}
			}
		}

	}

	private void importPackagePyan(String fileSeparator, Function2<String, String> nameAt,
			Function2<ArrayList<CyanMetaobject>, Symbol> moListSymbol) {
		ArrayList<CyanMetaobject> metaobjectList;
		while ( symbol.token == Token.IMPORT ) {
			next();
			Symbol importSymbol = symbol;
			ExprIdentStar packageId = ident();
			String packageName = packageId.asString();
			String s2 = "";
			int size = packageName.length();
			char ch;
			for (int i = 0; i < size; ++i)
				s2 = s2 + ((ch = packageName.charAt(i)) == '.' ? fileSeparator : "" + ch);
			String atPackage = null;
			if ( symbol.token == Token.IDENT && symbol.getSymbolString().equals("at") ) {
				next();
				if ( symbol.token != Token.LITERALSTRING )
					error(true, symbol, "A literal string with a directory (folder) was expected." + foundSuch(),
							"", ErrorKind.literal_string_expected);
				atPackage = symbol.getSymbolString();
				if ( atPackage.contains(NameServer.metaobjectPackageName) ) {
					this.error2(symbol, "The directory of a package should not include '" + NameServer.metaobjectPackageName + "'");
				}
				next();
				nameAt.eval(packageName, atPackage);



				/*
				project.addNamePackageImportList(packageName);
				project.addPathPackageImportList(atPackage);
				*/
				/*
				 * load metaobjects from the HDD
				 */
				metaobjectList = new ArrayList<>();
				try {
					CompilerManager.loadMetaobjectsFromPackage(atPackage, packageName, metaobjectList, this);
				}
				catch ( RuntimeException e ) {
					// e.printStackTrace();
					throw e;
				}

				/*
				 * load metaobjects to this compilation unit
				 */
				moListSymbol.eval(metaobjectList,  importSymbol);

				/*
				compilationUnit.loadCyanMetaobjects(metaobjectList, importSymbol, this);
				lexer.addMetaObjectLiteralObjectSeqTable(compilationUnit.getMetaObjectLiteralObjectSeqTable());
				lexer.addMetaobjectLiteralNumberTable(compilationUnit.getMetaObjectLiteralNumber());
				lexer.addMetaobjectLiteralStringTable(compilationUnit.getMetaobjectLiteralObjectString());
				*/

			}
			else {
				this.error2(symbol,  "'at' expected");
			}
		}
	}


	/**
	 * Create a Cyan package of program <code>program</code> and project <code>project1</code>. This
	 * package uses the metaobject annotations <code>nonAttachedMetaobjectAnnotationList</code> and
	 * <code>attachedMetaobjectAnnotationList</code>. The package
	 * name is <code>packageName</code> of directory <code>packageCanonicalPath</code>. All the
	 * source files ending with <code>.cyan</code> of the directory are included in the package.
	   @param program
	   @param fileSeparator
	   @param project1
	   @param packageName
	   @param packageCanonicalPath
	   @return
	 */
	private CyanPackage createCyanPackage(Program program, Project project1,
	        ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
	        ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
			String packageName, String packageCanonicalPath, ArrayList<CyanMetaobject> metaobjectList) {

		String fileSeparator = NameServer.fileSeparatorAsString;

		/*
		if ( packageCanonicalPath.contains(NameServer.prefixNonPackageDir) ) {
			error2(symbol, "A package directory cannot contain '" + NameServer.prefixNonPackageDir + "'");
		}
		*/

		CyanPackage cyanPackage = new CyanPackage(program, packageName, project1,
				packageCanonicalPath, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, metaobjectList);

		File possiblePackageDir = new File(
				(packageCanonicalPath.endsWith(fileSeparator) ? packageCanonicalPath : packageCanonicalPath + fileSeparator)
				+ packageName.replace('.', fileSeparator.charAt(0)));
		if ( possiblePackageDir.exists() && possiblePackageDir.isDirectory() ) {
			try {
				this.error2(symbol, "To package '" + packageName + "' was associated in the .pyan file a wrong directory. "
						+ "It should be '" + possiblePackageDir.getCanonicalPath().toString() + "'");
			}
			catch (IOException e) {
			}
		}

		/* ArrayList<CyanMetaobject> metaobjectList = new ArrayList<>();

		CompilerManager.loadMetaobjectsFromPackage(packageCanonicalPath, packageName, metaobjectList, compiler);
		cyanPackage.setMetaobjectList(metaobjectList); */

		File packageDir = new File(packageCanonicalPath);
		if ( ! packageDir.exists() )
			error(true, symbol, "Directory '" + packageCanonicalPath + "' cited in project '" + this.projectFilename +
							"' does not exist", "", ErrorKind.file_does_not_exist, "filename = " + packageCanonicalPath);
		if ( ! packageDir.isDirectory() )
			error(true, symbol, "File '" + packageCanonicalPath + "' cited in project '" + this.projectFilename +
							"' is not a directory. It should be a package directory",
					"", ErrorKind.file_should_be_directory, "filename = " + packageCanonicalPath);

		ArrayList<String> filesPackage = new ArrayList<String>();

		/**
		 * first load all files from directory --dsl with the DSL code
		 */
		String packageCanonicalPathWithSlash = packageCanonicalPath;
		if ( ! packageCanonicalPathWithSlash.endsWith(fileSeparator) ) {
			packageCanonicalPathWithSlash += fileSeparator;
		}
		File dslPackageDir = new File( packageCanonicalPath + NameServer.directoryNamePackageDSL);
		if ( dslPackageDir.exists() ) {
			if ( ! dslPackageDir.isDirectory() ) {
				error2(symbol, "File '" + dslPackageDir + "' should be a directory. But it is not");
			}
			for ( String dslFilename : dslPackageDir.list() ) {



				// it is considered a DSL if the extension do not start with ~ or has 'bak' in the name
				boolean isBackup = false;
				for ( String extension : NameServer.backupExtensionList ) {
					if ( dslFilename.endsWith(extension) ) {
						isBackup = true;
						break;
					}
				}
				if ( ! isBackup ) {


				    String pathSourceFile = packageDir + fileSeparator +
				    		dslFilename;
				    File f = new File(pathSourceFile);


					if (  ! f.isDirectory() ) {

						int indexOfPoint = dslFilename.lastIndexOf(".");
						if ( indexOfPoint > 0  ) {

							CompilationUnitDSL compilationUnitDSL = new CompilationUnitDSL(dslFilename,
									                            packageCanonicalPath,
									                            cyanPackage);
							cyanPackage.addCompilationUnitDSL(compilationUnitDSL);
						}

					}

				}


			}
		}


		filesPackage = new ArrayList<String>();

		String sourceFileList[] = packageDir.list();



		for ( String sourceFilename : sourceFileList ) {
		    String pathSourceFile = packageDir + fileSeparator +
		                           sourceFilename;
		    File f = new File(pathSourceFile);


			if ( sourceFilename.endsWith(NameServer.dotCyanSourceFileExtension) && ! f.isDirectory()) {
				filesPackage.add(sourceFilename);

				int indexOfPoint = pathSourceFile.lastIndexOf(".");
				if ( indexOfPoint < 0  ) {
					pathSourceFile = pathSourceFile + ".cyan";
				    indexOfPoint = pathSourceFile.lastIndexOf(".");
				}

				CompilationUnit compilationUnit3 = new CompilationUnit(sourceFilename,
						                            packageCanonicalPath,
						                            null, cyanPackage);
				cyanPackage.addCompilationUnit(compilationUnit3);


				int lastIndexBar = pathSourceFile.lastIndexOf(fileSeparator);
				if ( lastIndexBar < 0 )
					lastIndexBar = -1;
				String objectOrInterfaceName = "";
				if ( indexOfPoint > 0 )
					objectOrInterfaceName = pathSourceFile.substring(lastIndexBar+1, indexOfPoint);
				else
					error(true,
							symbol, "Filename " + pathSourceFile + " does not have '.cyan' extension",
							"", ErrorKind.file_does_not_have_cyan_extension, "filename = " + pathSourceFile);
				compilationUnit3.setObjectInterfaceName(cyanPackage.getPackageName() + "." + objectOrInterfaceName);

			}


		}

		return cyanPackage;
	}



	private boolean checkIf(Token t, String name) {

		if ( symbol.token == t )
			return true;
		else  {
			if ( symbol.getSymbolString().length() > 1 ) {
				if ( computeLevenshteinDistance(name, symbol.getSymbolString()) >= 0.5 ) {

					if ( ask(symbol, "'"+ symbol.getSymbolString() + "' seems to be mistyped. Can I change it to '" + name + "' ? (y, n)") ) {
						compilationUnit.addAction(
								new ActionDelete(compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getSymbolString().length(),
										symbol.getLineNumber(),
										symbol.getColumnNumber()));
						compilationUnit.addAction(
								new ActionInsert(name, compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getLineNumber(),
										symbol.getColumnNumber()));
						symbol.setSymbolString(name);
						symbol.token = t;
						return true;
					}
				}

			}
			return false;
		}
	}


	/**  Parse the compilation unit
	 *
	 * CompilationUnit ::=  PackageDec ImportDec { CTmetaobjectAnnotationList  ProgramUnit }

	 */

	//public ArrayList<ProgramUnit> parse() {
	public void parse() {

		lineShift = 0;
		parseSourceFileName();

		//ArrayList<ProgramUnit> programUnitArray = new ArrayList<ProgramUnit>();
		currentProgramUnit = null;

		CyanPackage importedPackage = compilationUnit.getCyanPackage().getProject()
				.searchPackage(NameServer.cyanLanguagePackageName);

		compilationUnit.setHasGenericPrototype(false);
		compilationUnit.loadCyanMetaobjects( importedPackage.getMetaobjectList(), symbol, this);
		lexer.addMetaObjectLiteralObjectSeqTable(compilationUnit.getMetaObjectLiteralObjectSeqTable());
		lexer.addMetaobjectLiteralNumberTable(compilationUnit.getMetaObjectLiteralNumber());
		//lexer.setMetaObjectLiteralObjectIdentSeqTable(compilationUnit.getMetaObjectLiteralObjectIdentSeqTable());
		lexer.addMetaobjectLiteralStringTable(compilationUnit.getMetaobjectLiteralObjectString());


		try {
			ExprIdentStar packageIdent = null;
			if ( scriptCyan ) {

				/**
				 * TODO
				 */
				/*
				 * the source file is of language ScriptCyan, not Cyan.

				String directoryFullFileName = compilationUnit.getFileNameWithoutExtension();
				String fileSeparator = System.getProperty("file.separator");
				int indexLastSeparator = directoryFullFileName.lastIndexOf(fileSeparator);
				#
				int indexPackage = compilationUnit.getProgram().getProject().getProjectDir().indexOf();
				if ( indexPackage < 0 ) {
					this.error2(null, "ScriptCyan source file (" + compilationUnit.getPackageCanonicalPath() + ") is not in the project directory("
							+ compilationUnit.getProgram().getProject().getProjectDir() + ")");
				}
				else {

				}
				*/
				this.error2(null,  "ScriptCyan is not supported yet");
			}
			else {
			}
			if ( symbol.token == Token.METAOBJECT_ANNOTATION ) {
				Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tctmo = this.parseMetaobjectAnnotations_NonAttached_Attached();
				if ( tctmo != null ) {
					ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList = tctmo.f1;
					ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList = tctmo.f2;
					if ( attachedMetaobjectAnnotationList != null && attachedMetaobjectAnnotationList.size() > 0 ) {
						for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
							this.error2(annotation.getFirstSymbol(),  "Metaobject '" + annotation.getCyanMetaobject().getName() +
									"' should be attached to a declaration. It cannot appear before 'package'");
						}
					}
					this.compilationUnit.setNonAttachedMetaobjectAnnotationListBeforePackage(nonAttachedMetaobjectAnnotationList);
				}


			}

			if ( symbol.token != Token.PACKAGE )
				error(true, symbol,
						"keyword 'package' expected." + foundSuch(), symbol.getSymbolString(), ErrorKind.keyword_package_expected);
			next();
			if ( symbol.token != Token.IDENT )
				error(true, symbol,
						"package name expected." + foundSuch(), symbol.getSymbolString(), ErrorKind.package_name_expected);


			Symbol packageSymbol = symbol;

			int ch = packageSymbol.getSymbolString().charAt(0);
			//if ( ! Character.isLowerCase(packageSymbol.getSymbolString().charAt(0)) )
			if ( ch < 'a' || ch > 'z' )
					error(true, symbol,
							"The package name should start with a lower case letter ", symbol.getSymbolString(), ErrorKind.package_name_not_start_with_lower_case_letter);

			packageIdent = ident();


			if ( symbol.token == Token.COMMA ) {
				if ( ask(symbol, "'"+ symbol.getSymbolString() + "' is illegal here. Can I remove it? (y, n)") ) {
					int sizeIdentSymbol = 1;
					compilationUnit.addAction(
							new ActionDelete(compilationUnit,
									symbol.startOffsetLine + symbol.getColumnNumber() - 1,
									sizeIdentSymbol,
									symbol.getLineNumber(),
									symbol.getColumnNumber()));
				}
			}
			if ( symbol.token == Token.SEMICOLON )
				next();

			/**
			 * load metaobjects of this package
			 */
			String packageName = compilationUnit.getCyanPackage().getPackageName();
			if ( ! packageName.equals(NameServer.cyanLanguagePackageName) ) {
				compilationUnit.loadCyanMetaobjects( compilationUnit.getCyanPackage().getMetaobjectList(), packageSymbol, this);
				lexer.addMetaObjectLiteralObjectSeqTable(compilationUnit.getMetaObjectLiteralObjectSeqTable());
				lexer.addMetaobjectLiteralNumberTable(compilationUnit.getMetaObjectLiteralNumber());
				// lexer.setMetaObjectLiteralObjectIdentSeqTable(compilationUnit.getMetaObjectLiteralObjectIdentSeqTable());
				lexer.addMetaobjectLiteralStringTable(compilationUnit.getMetaobjectLiteralObjectString());
			}

			ArrayList<ExprIdentStar> importPackageList = importDecList();
			compilationUnit.setPackageIdent(packageIdent);
			compilationUnit.setImportPackageList(importPackageList);

			if ( ! compilationUnit.getCyanPackage().getPackageName().equals(packageIdent.getName()) ) {
				this.error(true, packageIdent.getFirstSymbol(),
						"Package name should be '" + packageName + "'. Maybe you gave the wrong directory to compile. Maybe it should be a"
								+ " father or a child of '" +
				        this.getProject().getProjectCanonicalPath() + "'" ,
						packageName, ErrorKind.package_has_a_wrong_name);
			}

			this.numPublicPackageProgramUnits = 0;
			while ( true ) {


				if ( symbol.token != Token.EOF ) {


					 try {
						currentProgramUnit = programUnit();
						//programUnitArray.add(currentProgramUnit);

					}
					catch ( CompileErrorException e ) {
						/*
						 * skip to the end of prototype of to the end of the file
						 *
						while ( symbol.token != Token.EOF && symbol.token != Token.END )
							next();
						if ( symbol.token == Token.END )
							next();
						*/
						break;
					}

				}
				else
					break;
			}

			if ( ! compilationUnit.hasCompilationError() )  {

				if ( this.numPublicPackageProgramUnits == 0 )
					error(true,
							currentProgramUnit != null? currentProgramUnit.getSymbol() : null,
									"This source file should declare at least a public prototype named " +
											NameServer.fileNameToPrototypeName(compilationUnit.getFilename()),
											currentProgramUnit != null? currentProgramUnit.getName() : null,
											ErrorKind.no_public_protected_prototype_found_in_source_file, "identifier = \"" + (currentProgramUnit != null? currentProgramUnit.getName() : "") + "\"");

				// there should be exactly one public or protected prototype in the source file
				if ( this.numPublicPackageProgramUnits != 1 ) {
					int n = 0, i = 0;
					while ( i < compilationUnit.getProgramUnitList().size() ) {
						ProgramUnit pu = compilationUnit.getProgramUnitList().get(i);
						if ( pu.getVisibility() == Token.PUBLIC || pu.getVisibility() == Token.PACKAGE ) {
							++n;
							if ( n == 1 ) break;
						}
						++i;
					}
					error(true,
							compilationUnit.getProgramUnitList().get(1).getSymbol(),
							"There should be exactly one 'public' or 'package' prototype in every source file. This is the second one",
							compilationUnit.getProgramUnitList().get(1).getName(), ErrorKind.two_or_more_public_protected_prototype_found_in_source_file);
				}

				// If this compilation unit has a generic prototype, it should be the only one in the file.
				boolean hasGenericPrototype = compilationUnit.getHasGenericPrototype();
				//if ( hasGenericPrototype ) {}

				for ( ProgramUnit pu : compilationUnit.getProgramUnitList() ) {


					for ( CyanMetaobjectAnnotation annotation : pu.getCompleteMetaobjectAnnotationList() ) {
						CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
						if ( cyanMetaobject instanceof IActionGenericProgramUnit_dpa ) {
							IActionGenericProgramUnit_dpa iaction = (IActionGenericProgramUnit_dpa ) cyanMetaobject;
							cyanMetaobject.setMetaobjectAnnotation(annotation);
							CompilerGenericProgramUnit_dpa compilerGenericProgramUnit_dpa = new CompilerGenericProgramUnit_dpa(this, pu);


							try {
								iaction.dpa_actionGenericProgramUnit(compilerGenericProgramUnit_dpa);
							}
							catch ( error.CompileErrorException e ) {
							}
							catch ( RuntimeException e ) {
								e.printStackTrace();
								thrownException(annotation, annotation.getFirstSymbol(), e);
							}
							finally {
								this.metaobjectError(cyanMetaobject,  annotation);
							}



						}
					}

					/*
					if ( pu.isGeneric() ) {}
					else {
						// found a regular prototype in a file that has a generic prototype
						error(true,
								pu.getSymbol(),
								"Non-generic prototype " + pu.getName() + " was declared in a file that has a generic prototype",
								pu.getName(), ErrorKind.non_generic_prototype_in_the_same_source_file_with_generic_prototype);
					}
					*/
				}


				/**
				 * check the codegs
				 * All codegs should have an identifier as the first parameter
				 */
				if ( codegList.size() > 0 ) {
					for ( CyanMetaobjectWithAtAnnotation annotation : codegList ) {
						ArrayList<ExprAnyLiteral>  paramList = annotation.getRealParameterList();
						/*
						 * A code should have exactly one parameter and this should be an identifier
						 */
						if ( paramList == null || paramList.size() < 1 ) { // != 1 ||! (paramList.get(0) instanceof ExprAnyLiteralIdent) ) {
							error2(annotation.getFirstSymbol(), "A codeg annotation should take at least one parameter that is an identifier");
							break;
						}
					}
					/* A codeg stores information in a file during editing time. This information is retrieved
					 * now from the file which is stored in a directory that is in the same directory as the source file.
					 */
					if ( compInstSet.contains(CompilationInstruction.dpa_actions) ) {
						for ( CyanMetaobjectWithAtAnnotation annotation : codegList ) {
							/*
							 * read codeg information from files or from codegNameWithCodegFile
							 */
							if ( annotation.getFirstSymbol().getLineNumber() != annotation.getLastSymbol().getLineNumber() ) {
								this.error2(annotation.getFirstSymbol(), "The last symbol of a codeg annotation, "
										+ "usually ')', should be in the same line as the first symbol of the annotation, the codeg name");
							}
							String codegPath = this.compilationUnit.getCanonicalPathUpDir() +
									NameServer.getCodegDirFor( NameServer.prototypeNameToFileName(annotation.getPrototypeOfAnnotation()) );
							String id = ((ExprAnyLiteralIdent ) annotation.getRealParameterList().get(0)).getIdentExpr().getName();
							String codegInfoFilename = codegPath + NameServer.fileSeparatorAsString + annotation.getCyanMetaobject().getName() +
									"(" + id + ")." + ((ICodeg ) annotation.getCyanMetaobject()).getFileInfoExtension();

							byte []codegFileData = null;
							Tuple2<String, byte[]> foundTuple = null;
							String codegCompleteName = annotation.getCompleteName();
							if ( codegNameWithCodegFile != null ) {
								for ( Tuple2<String, byte[]> t : this.codegNameWithCodegFile ) {
									if ( codegCompleteName.equals(t.f1) ) {
										codegFileData = t.f2;
										foundTuple = t;
										/*
										 * this call is redundant (at least I think) because if the codeg annotation
										 * is found in codegNameWithCodegFile then the annotation has been
										 * previously set with codegFileText
										 */
										annotation.setCodegInfo( codegFileData == null ? "".getBytes() : codegFileData );
										break;
									}
								}
								/*
								 * next steps: if foundTuple is null, no codeg with name codegCompleteName was found.
								 * Then this is the first time {@link Saci#parseSingleSource} is called. Two things
								 * may have happened:
								 *    (a) codegCompleteName may have just been added by the user during edition and the IDE
								 *        has not called {@link Saci#eventCodegMenu}. The codeg menu has not been called.
								 *        In this case the file should not be read from the HD. But we don´t know
								 *        how to differentiate this from case (b);
								 *    (b) this source file has been compiled before but the user changed the edition
								 *        to another source code and then returned to this. Then all codeg
								 *        info has to be read from files again.
								 *
								 *
								 * if foundTuple is not null, {@link Saci#parseSingleSource} has been called before
								 * and:
								 *   (a) this same piece of code has initialized  codegNameWithCodegFile with a codeg
								 *       named codegCompleteName (by reading info from a file --- see code below);
								 *   (b) {@link Saci#eventCodegMenu} has been called (mouse over the codeg text) and
								 *       it has initialized foundTuple.
								 *
								 *  codegFileText may be null or "". Maybe the file has "" as content. Maybe
								 *  the mouse has been over the codeg but the user gave no input.
								 *  If foundTuple is not null, nothing should be done. The user, using the IDE,
								 *  will update the codeg info and the file.
								 */

							}
							if ( foundTuple == null || codegNameWithCodegFile == null ) {
								/*
								 * either codegNameWithCodegFile is null (the compiler is not being called by {@link Saci#parseSingleSource})
								 * or codegNameWithCodegFile is not null and does not contains a codeg with name codegCompleteName
								 * or or codegNameWithCodegFile is not null and the codegFileText is null or contains "".
								 * This middle case happens when {@link Saci#parseSingleSource} is called for the first time
								 * and the program has been previously compiled. Then the codeg information is in the file.
								 * The last case happens when  {@link Saci#parseSingleSource}  has been called but
								 * there is no information on the codeg. Maybe the information is in the file. Maybe it is not.
								 */

								Path aPath = Paths.get(codegInfoFilename);
								codegFileData = null;
							    try {
									 codegFileData = Files.readAllBytes(aPath);
								}
								catch (IOException e) {
									try {
										this.error(true, annotation.getFirstSymbol(),
											     "Error reading information on codeg '" +
											         annotation.getCyanMetaobject().getName() + "(" + ((String ) annotation.getJavaParameterList().get(0)) +
											         (annotation.getJavaParameterList().size() > 1 ? ", ..." : "") +
														")'. File " + codegInfoFilename + " was not found",
														annotation.getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error_reading_codeg_info_file);

									}
									catch ( error.CompileErrorException e1 ) {
									}
								}

							    /*
								MyFile f = new MyFile(codegInfoFilename);
								char []charArray = f.readFile();
								*/
								if ( codegFileData != null  )
									annotation.setCodegInfo( codegFileData );
								else
									annotation.setCodegInfo( "".getBytes() );
								if ( foundTuple == null && codegNameWithCodegFile != null ) {
									/*
									 * no codeg was found. Add it to codegNameWithCodegFile
									 */
									this.codegNameWithCodegFile.add( new Tuple2<String, byte[]>(codegCompleteName, annotation.getCodegInfo()));
								}

								/*
								if ( f.getError() != MyFile.do_not_exist_e ) {
									if ( f.getError() != MyFile.ok_e )
										this.error(true, annotation.getFirstSymbol(),
									     "Error reading information on codeg '" +
									         annotation.getCyanMetaobject().getName() + "(" + ((String ) annotation.getJavaParameterList().get(0)) +
									         (annotation.getJavaParameterList().size() > 1 ? ", ..." : "") +
												")'. File " + codegInfoFilename + " was not found",
												annotation.getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error_reading_codeg_info_file);
								}
								*/

							}

						}
					}
					this.compilationUnit.setCodegList(codegList);
				}

			}
			int numFalse = 0;
			for ( Tuple3<Integer, String, Boolean> t : compilationUnit.getLineMessageList() ) {
				if ( !t.f3 )
					++numFalse;
			}

			if ( numFalse != compilationUnit.getLineMessageList().size() ) {
				for ( Tuple3<Integer, String, Boolean> t : compilationUnit.getLineMessageList() ) {
					if ( ! t.f3 ) {
						try {
							error2(null,  "A metaobject implementing interface 'IInformCompilationError' points an error at line " + t.f1 +
									" with message '" + t.f2 + "' although this error is not signaled by the compiler." +
									" If the error really is in the source code, what happens was similar to the following: there is a parser error and " +
									"a semantic error in this source code, each pointed by a metaobject. This error was caused by the semantic error. This error" +
									" wound be pointed by the compiler in a later compiler phase. However, it will not because the compilation will stop because " +
									"of the parsing error",
									false
									);
						}
						catch ( CompileErrorException e ) {
						}
					}
				}

			}

		} catch ( CompileErrorException e ) {
			//return programUnitArray;
		}


		//return programUnitArray;
	}



    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * taken from http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
       @param str1
       @param str2
       @return
     */
    public static int computeLevenshteinDistance(String str1, String str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= str2.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= str1.length(); i++)
            for (int j = 1; j <= str2.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

        return distance[str1.length()][str2.length()];
    }


	/**
	 * \p{ImportDec} ::= \{ ``import"\/ IdList \}

	 * @return
	 */

	private ArrayList<ExprIdentStar> importDecList() {
		ArrayList<ExprIdentStar> importList = new ArrayList<ExprIdentStar>();

		Set<CyanPackage> importPackageSet = new HashSet<CyanPackage>();
		Set<JVMPackage>  importJVMPackageSet = new HashSet<JVMPackage>();
		Set<TypeJavaRef>  importJVMJavaRefSet = new HashSet<>();


		while ( symbol.token == Token.IMPORT ) {
			Symbol importSymbol = symbol;
			next();
			if ( symbol.token != Token.IDENT ) {
				error(true, symbol,
						"package name expected in import declaration." + foundSuch(), null, ErrorKind.package_name_expected);
			}
			else{


				ExprIdentStar importPackage = ident();
				importList.add(importPackage);
				if ( importPackage.getName().startsWith(NameServer.cyanLanguagePackageName) ) {
					if ( importPackage.getName().equals(NameServer.cyanLanguagePackageName) ) {
						error2(importPackage.getFirstSymbol(), "Package 'cyan.lang' is automatically imported. It cannot be imported by the user");
					}
					else {
						error2(importPackage.getFirstSymbol(), "It is not legal to have a package that starts with 'cyan.lang'");
					}
				}


				/*
				 * load metaobjects of the imported package
				 */
				String importedPackageName = importPackage.getName();
				CyanPackage importedPackage = compilationUnit.getCyanPackage().getProject()
						.searchPackage(importedPackageName);
				if ( importedPackage == null ) {
					importJava(importJVMPackageSet, importJVMJavaRefSet,
							importPackage, importedPackageName);
				}
				else {
					compilationUnit.loadCyanMetaobjects(importedPackage.getMetaobjectList(), importSymbol, this);
					lexer.addMetaObjectLiteralObjectSeqTable(compilationUnit.getMetaObjectLiteralObjectSeqTable());
					lexer.addMetaobjectLiteralNumberTable(compilationUnit.getMetaObjectLiteralNumber());
					// lexer.setMetaObjectLiteralObjectIdentSeqTable(compilationUnit.getMetaObjectLiteralObjectIdentSeqTable());
					lexer.addMetaobjectLiteralStringTable(compilationUnit.getMetaobjectLiteralObjectString());

					importPackageSet.add(importedPackage);
				}


				while ( symbol.token == Token.COMMA || symbol.token == Token.IDENT ) {
					if ( symbol.token == Token.IDENT ) {
						error2(symbol, "',' expected between imported packages." + foundSuch());
					}
					else {
						lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
						next();
					}
					if ( symbol.token != Token.IDENT )
						error(true, symbol, "Package name expected." + foundSuch(), null, ErrorKind.package_name_expected);
					Symbol packageSymbol = symbol;
					importPackage = ident();
					importList.add(importPackage);
					/*
					 * load metaobjects of the imported package
					 */
					importedPackageName = importPackage.getName();
					importedPackage = compilationUnit.getCyanPackage().getProject()
							.searchPackage(importedPackageName);
					if ( importedPackage == null ) {
						importJava(importJVMPackageSet, importJVMJavaRefSet,
								importPackage, importedPackageName);
					}
					else {
						compilationUnit.loadCyanMetaobjects(importedPackage.getMetaobjectList(), packageSymbol, this);
						lexer.addMetaObjectLiteralObjectSeqTable(compilationUnit.getMetaObjectLiteralObjectSeqTable());
						lexer.addMetaobjectLiteralNumberTable(compilationUnit.getMetaObjectLiteralNumber());
						// lexer.setMetaObjectLiteralObjectIdentSeqTable(compilationUnit.getMetaObjectLiteralObjectIdentSeqTable());
						lexer.addMetaobjectLiteralStringTable(compilationUnit.getMetaobjectLiteralObjectString());
						importPackageSet.add(importedPackage);
					}
				}
			}
			if ( symbol.token == Token.SEMICOLON )
				next();
		}

		compilationUnit.setImportPackageSet(importPackageSet);
		compilationUnit.setImportJVMPackageSet(importJVMPackageSet);
		compilationUnit.setImportJVMJavaRefSet(importJVMJavaRefSet);
		return importList;
	}

	/**
	   @param importJVMPackageSet
	   @param importJVMClassSet
	   @param importPackage
	   @param importedPackageName
	 */
	private void importJava(Set<JVMPackage> importJVMPackageSet,
			Set<TypeJavaRef> importJVMClassSet, ExprIdentStar importPackage,
			String importedPackageName) {
		String si = importPackage.getIdentSymbolArray().get(importPackage.getIdentSymbolArray().size() - 1).symbolString;
		int i = 0;
		while ( i < si.length() && si.charAt(i) == '_' ) { ++i; }


		if ( i < si.length() && Character.isUpperCase(si.charAt(i)) ) {
			// assume it is a JVM class
			int j = importedPackageName.lastIndexOf('.');
			if ( j <= 0 ) {
				error( true, importPackage.getFirstSymbol(),
						"Package '" + importedPackageName + "' was not found", importPackage.getName(), ErrorKind.package_was_not_found_outside_prototype);

			}
			else {
				String jvmClassName = importedPackageName;
				importedPackageName = importedPackageName.substring(0, j);
				JVMPackage jvmPackage = program.searchJVMPackage(importedPackageName);
				if ( jvmPackage == null ) {
					error( true, importPackage.getFirstSymbol(),
							"Package '" + importedPackageName + "' was not found", importPackage.getName(), ErrorKind.package_was_not_found_outside_prototype);
				}
				else {
					TypeJavaRef javaRef = jvmPackage.searchJVMClass(jvmClassName);
					if ( javaRef == null ) {
						error( true, importPackage.getFirstSymbol(),
								"Java class '" + importPackage.getName() + "' was not found", importPackage.getName(), ErrorKind.package_was_not_found_outside_prototype);
					}
					else {
						importJVMClassSet.add(javaRef);
					}
				}

			}
		}
		else {
			// assume it is a package
			JVMPackage jvmPackage = program.searchJVMPackage(importedPackageName);
			if ( jvmPackage == null ) {
				error( true, importPackage.getFirstSymbol(),
						"Package " + importPackage.getName() + " was not found", importPackage.getName(), ErrorKind.package_was_not_found_outside_prototype);
			}
			else {
				importJVMPackageSet.add(jvmPackage);
			}

		}
	}

	/**
	 * return an object that groups several identifiers separated by . such as
	 *     cyan.util.Stack
	 * It is assumed that the current symbol, given by variable symbol, is the
	 * first identifier in the list ("cyan" in "cyan.util.Stack").
	 *
	 * @param identOne
	 * @return
	 */

	private ExprIdentStar ident() {

		ArrayList<Symbol> identSymbolArray = new ArrayList<Symbol>();
		identSymbolArray.add(symbol);
		next();
		while ( symbol.token == Token.PERIOD ) {
			next();
			if ( symbol.token != Token.IDENT ) {
				error(true, symbol, "Package, object name or slot (variable or method) expected."  + foundSuch(),
						null, ErrorKind.identifier_expected_inside_method);
			}
			identSymbolArray.add(symbol);
			next();
		}
		return new ExprIdentStar(identSymbolArray, symbol);
	}

	private ExprIdentStar identColon() {

		ArrayList<Symbol> identSymbolArray = new ArrayList<Symbol>();
		if ( symbol.token == Token.IDENTCOLON ) {
			identSymbolArray.add(symbol);
			next();
			return new ExprIdentStar(identSymbolArray, symbol);
		}
		return null;
	}

	private CyanMetaobjectWithAtAnnotation annotation(boolean inExpr) {
		String metaobjectName = symbol.getSymbolString();
		CyanMetaobjectWithAt cyanMetaobject = this.compilationUnit.getMetaObjectTable().get(metaobjectName);
		return annotation(cyanMetaobject, metaobjectName, inExpr);
	}

	/**
	 * analyzes a metaobject annotation. The current token should be CyanMetaobjectWithAtAnnotation
	   @return
	 */
	private CyanMetaobjectWithAtAnnotation annotation(CyanMetaobjectWithAt cyanMetaobject, String metaobjectName, boolean inExpr) {

		SymbolCyanMetaobjectAnnotation metaobjectSymbol = (SymbolCyanMetaobjectAnnotation ) symbol;
		// String metaobjectName = symbol.getSymbolString();




		if ( cyanMetaobject == null ) {

			error(true,  symbol, "Metaobject " + metaobjectName + " was not found",
					metaobjectName, ErrorKind.metaobject_was_not_found);
			return null;
		}

		if ( cyanMetaobject instanceof IAction_cge &&
			 ! cyanMetaobject.getPackageName().equals(NameServer.cyanLanguagePackageName) &&
			this.compilationStep == CompilationStep.step_1 && this.cyanMetaobjectContextStack.empty() ) {
			/*
			 * Metaobject that implement interface IAction_cge generate Java code. These metaobjects
			 * can only be used inside package cyan.lang. Unless they are introduced by the compiler itself
			 * in phases >= 2 (CompilationStep.step_2 and beyond)
			 */
			this.error(true, metaobjectSymbol,
					"Metaobject '" + cyanMetaobject.getName() + "' can only be declared inside package cyan.lang because it implements interface 'IAction_cge'", metaobjectName, ErrorKind.metaobject_error);

		}

		Symbol metaobjectAnnotationSymbol = symbol;
		cyanMetaobject = cyanMetaobject.clone();
		CyanMetaobjectWithAtAnnotation annotation = new CyanMetaobjectWithAtAnnotation(
				compilationUnit,
				(SymbolCyanMetaobjectAnnotation ) symbol, cyanMetaobject, inExpr);

		cyanMetaobject.setMetaobjectAnnotation(annotation);

		annotation.setInsideProjectFile( this.compInstSet.contains(CompilationInstruction.pyanSourceCode) );


		metaobjectSymbol.setMetaobjectAnnotation(annotation);

		if ( cyanMetaobject instanceof ICodeg ) {
			this.codegList.add(annotation);
		}


		if ( currentProgramUnit != null ) {
			currentProgramUnit.addMetaobjectAnnotation(annotation);
			annotation.setProgramUnit(currentProgramUnit);
			annotation.setMetaobjectAnnotationNumber(currentProgramUnit.getIncMetaobjectAnnotationNumber());
		}
		annotation.setCompilationUnit(compilationUnit);

		/*
		 * there are many possibilities of passing parameters to a metaobject annotation:
		 *  a) the annotation is in an expression; that is, <code>inExpr</code> is true. <br>
		 *     correct:   <br>
		 *        <code> var n = @annotation(hi) 2; </code> <br>
		 *        there is no white space before '(' in the annotation and the metaobject takes arguments. <br>
		 *        <code> var n = @annotation (hi); </code> <br>
		 *        no white space before and the metaobject does not take arguments.<br>
		 *     wrong: <br>
		 *        <code>
		 *        var hi = 0; <br>
		 *        var n = @annotation  (hi); </code> <br>
		 *        white space before: ambiguous to the user. If the annotation may take
		 *        arguments, the compiler should sign an error. <br>
		 *        <code>
		 *        var hi = 0; <br>
		 *        var n = @annotation  (hi); </code> <br>
		 *
		 * b) the annotation is not in an expression; that is, <code>inExpr</code> is false. <br>
		 *     correct:<br>
		 *     <code> @style(plain) func get -> Int { ... }</code><br>
		 *     No white space before '('<br>
		 *     wrong:<br>
		 *     <code> @style (plain) func get -> Int { ... }</code><br>
		 *     The compiler should sign an error but continue if the annotation may take
		 *     arguments.
		 *
		 *  <br>
		 *  Currently we show only the most basic message: if the next symbol is '(' and
		 *  the metaobject may take any arguments the compiler issue an error message. Using
		 *  the comments above one could show more information to the user.
		 *
		 */
		boolean action_dpa = false;
		if ( // ! cyanMetaobject.shouldTakeText() &&
			   cyanMetaobject instanceof IAction_dpa &&
			   (annotation.getPostfix() == null || annotation.getPostfix().lessThan(CompilerPhase.DPA)) &&
			   (this.currentProgramUnit == null ||! this.currentProgramUnit.isGeneric()) ) {


			// symbol is a metaobject annotation
			action_dpa = true;
			   // always use compilation context
			insertPhaseChange(CompilerPhase.DPA, annotation);

			/*
			if ( cyanMetaobject.useCompilationContext_keepTheMetaobjectAnnotation() ) {
				insertPhaseChange(CompilerPhase.DPA, annotation);
			}
			else {
				offsetStartDelete = symbol.getOffset();
			}
			*/
		}


		ArrayList<ExprAnyLiteral> exprList = new ArrayList<ExprAnyLiteral>();
		ArrayList<Object> javaObjectList = null;

		if ( ! metaobjectSymbol.getLeftParAfterMetaobjectAnnotation() ) {
			// no '(' immediatelly after the metaobject name. Then there should be no parameters
			if ( cyanMetaobject.shouldTakeText() ) {

				try {
					getLeftCharSequence();
				}
				catch ( CompileErrorException e ) {
					return annotation;
				}

			}
			else {
				next();
				if ( symbol instanceof SymbolCharSequence ) {
					this.error2(symbol, "This metaobject should not take a DSL after its name and parameters. If it should, its method 'shouldTakeText' should return 'true'");
				}
			}
		}
		else {
			/*
			 * the metaobject has a '(' after its name, without any spaces before the '('. Therefore the
			 * metaobject annotation should have parameters
			 */
			next();
			if ( symbol.token != Token.LEFTPAR ) {
				this.error(true, metaobjectAnnotationSymbol,
						"'(' used just after a metaobject annotation. It was expected just the character '('", "", ErrorKind.metaobject_error);
			}
			annotation.setLeftDelimArgs(symbol);
			next();
			lexer.setOneCharSymbols(true);
			if ( startExpr(symbol) || symbol.token == Token.IDENTCOLON ) {
				exprList.add(exprBasicTypeLiteral());
				while ( symbol.token == Token.COMMA )  {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");

					next();
					exprList.add(exprBasicTypeLiteral());
				}
			}
			lexer.setOneCharSymbols(false);

			if ( symbol.token != Token.RIGHTPAR )
				error(true, symbol, "')' expected after the parameters of this metaobject annotation",
						"", ErrorKind.metaobject_annotation_error_missing_args_symbols);
			else {

				if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
					error2(symbol, "letter, number, or '_' after ')'");
				}

				annotation.setRightDelimArgs(symbol);
				if ( cyanMetaobject.shouldTakeText() )
					getLeftCharSequence();
				else
					next();
			}

		}
		annotation.setLastSymbol(this.previousSymbol);
		annotation.setRealParameterList(exprList);
		if ( cyanMetaobject.getParameterKind() == MetaobjectArgumentKind.OneParameter &&
			 exprList.size() != 1 ) {
			this.error(true, metaobjectAnnotationSymbol,
					"Metaobject " + cyanMetaobject.getName() + " accepts exactly one parameter", "", ErrorKind.metaobject_wrong_number_of_parameters);
		}
		else if ( cyanMetaobject.getParameterKind() == MetaobjectArgumentKind.TwoParameters &&
			      exprList.size() != 2 ) {
				this.error(true, metaobjectAnnotationSymbol,
						"Metaobject " + cyanMetaobject.getName() + " accepts two parameters", "", ErrorKind.metaobject_wrong_number_of_parameters);
			}
		else if ( cyanMetaobject.getParameterKind() == MetaobjectArgumentKind.OneOrMoreParameters &&
			      exprList.size() == 0 ) {
			this.error(true, metaobjectAnnotationSymbol,
					"Metaobject " + cyanMetaobject.getName() + " accepts at least one parameter", "", ErrorKind.metaobject_wrong_number_of_parameters);
		}
		else if ( cyanMetaobject.getParameterKind() == MetaobjectArgumentKind.ZeroParameter &&
			      exprList.size() != 0 ) {
			this.error(true, metaobjectAnnotationSymbol,
					"Metaobject " + cyanMetaobject.getName() + " does not accept parameters", "", ErrorKind.metaobject_wrong_number_of_parameters);
		}


		javaObjectList = new ArrayList<Object>();
		for ( Expr e : exprList ) {
			javaObjectList.add(  ((ExprAnyLiteral) e).getJavaValue() );
		}
		annotation.setJavaParameterList(javaObjectList);

		boolean nonGenericPrototype = ! compilationUnit.hasGenericPrototype();

		if ( cyanMetaobject instanceof IInformCompilationError ) {
			IInformCompilationError cyanMetaobjectCompilationError = (IInformCompilationError ) cyanMetaobject;

			if ( cyanMetaobjectCompilationError.activeInGenericPrototype() ) {
				nonGenericPrototype = true;
			}
		}

		if ( nonGenericPrototype ) {
			ArrayList<CyanMetaobjectError> errorList = cyanMetaobject.check();
			if ( errorList != null ) {
				for ( CyanMetaobjectError cyanMetaobjectError : errorList ) {
					this.error(true, metaobjectAnnotationSymbol, cyanMetaobjectError.getMessage(),
							metaobjectAnnotationSymbol.getSymbolString(), ErrorKind.metaobject_error);
				}
			}
		}

		ICompilerAction_dpa compilerAction_dpa = null;
		/*
		 * true if a text between a sequence of characters was found after the metaobject name/parameters
		 */
		boolean foundtextBetweenSeq = false;
		if ( ! cyanMetaobject.shouldTakeText() ) {
			/*
			 * there should not be a text between a sequence of characters like in
				    * <code> <br>
				    *     var g = @graph#dpa{* 1:2 2:3 *}  <br>
				    * </code> <br>
			  That is, the metaobject annotation is as in the following examples:
				    * <code> <br>
				    *     var list = @compilationInfo("instancevariablelist");
				    *     @genSomething(10);
				    * </code>

			 */
			if ( action_dpa ) {
				IAction_dpa codeGen = (IAction_dpa ) cyanMetaobject;
				Compiler_dpa compiler_dpa = new Compiler_dpa(this, lexer, null);
				compilerAction_dpa = compiler_dpa;
				StringBuffer sb = null;

				try {
					sb = codeGen.dpa_codeToAdd(compiler_dpa);
				}
				catch ( CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					thrownException(annotation, annotation.getFirstSymbol(), e);

				}
				finally {
					ArrayList<CyanMetaobjectError> errorList = cyanMetaobject.getErrorMessageList();
					if ( errorList != null ) {
						for ( CyanMetaobjectError moError : errorList ) {
							error2(annotation.getFirstSymbol(), moError.getMessage());
						}
					}
					this.copyLexerData(compiler_dpa.compiler);
				}



				if ( sb != null ) {

					if ( this.compilationStep.ordinal() >= CompilationStep.step_7.ordinal() ) {
						if ( ! allowCreationOfPrototypesInLastCompilerPhases ) {
							this.error2(metaobjectAnnotationSymbol, "This metaobject annotation is trying to generate code in phase 7 of the compilation. This is illegal"
									+ " Probably this metaobject annotation was inserted in this source code in phases 5, 6, or 7 of the compilation by another metaobject annotation");
						}
					}

					StringBuffer s = new StringBuffer();
					  // always use compilation context
					if ( cyanMetaobject.isExpression() ) {
						s.append(" @" + NameServer.pushCompilationContextName + "(dpa" + Compiler.contextNumber + ", \"" + metaobjectName + "\", \""
								+ this.compilationUnit.getPackageName() + "\", \"" + compilationUnit.getFullFileNamePath() +  "\", "
								+ metaobjectSymbol.getLineNumber() );
						s.append(") ");
						s.append(sb);
						s.append(" @" + NameServer.popCompilationContextName + "(dpa" + Compiler.contextNumber
								+ ", \"" + cyanMetaobject.getPackageOfType() + "\", \"" + cyanMetaobject.getPrototypeOfType() + "\") \n");
					}
					else {
						s.append(" @" + NameServer.pushCompilationContextStatementName + "(dpa" + Compiler.contextNumber + ", \"" + metaobjectName + "\", \""
								+ this.compilationUnit.getPackageName() + "\", \"" + compilationUnit.getFullFileNamePath() +  "\", "
								+ metaobjectSymbol.getLineNumber() );
						s.append(") ");
						s.append(sb);
						s.append(" @" + NameServer.popCompilationContextName + "(dpa" + Compiler.contextNumber + ") \n");
					}

					/*
					 * pushCompilationContext(id, moName, packageName, prototypeName, sourceFileName, lineNumber
					 */

					++Compiler.contextNumber;
					int offset = symbol.getOffset();
					char []text = this.compilationUnit.getText();
					offset = Lexer.findIndexInsertText(text, offset);
					insertTextInput(s, offset);
					insideCyanMetaobjectCompilationContextPushAnnotation = true;
					next();

					hasMade_dpa_actions = true;

				}

			}
			annotation.setNextSymbol(symbol);
		}
		else {
			/*
			 * metaobject should take text between sequences such as {* and *}.
			 */
			if ( symbol.token != Token.LEFTCHAR_SEQUENCE )
				this.error(true, metaobjectAnnotationSymbol,
						"After this metaobject annotation there should appear a text between two sequences of symbols", "", ErrorKind.metaobject_annotation_error_missing_text);

			SymbolCharSequence leftCharSeqSymbol = (SymbolCharSequence ) symbol;

			/*
			if ( ! compInstSet.contains(saci.CompilationInstruction.dpa_actions) && !(cyanMetaobject instanceof IAction_cge) ) {
				   /*  test if the metaobject annotation ends with #dpa such as in
				    * <code> <br>
				    *     var g = @graph#dpa{* 1:2 2:3 *}  <br>
				    * </code>
				    * /
				if ( annotation.getPostfix() != CompilerPhase.DPA  ) {
					/*
					 * found a literal object in a compiler step that does not allow literal objects. See the Figure
					 * in Chapter "Metaobjects" of the Cyan manual
					 * /
					this.error(true, symbol, "Literal object in a compiler step that does not allow literal objects",
							symbol.getSymbolString(), ErrorKind.dpa_compilation_phase_literal_objects_and_macros_are_not_allowed);

				}
			}
			else
			*/
			if ( cyanMetaobject instanceof IParseWithCyanCompiler_dpa ) {
				/*
				 * represents DSL such as in
				 *  <br>
				* <code>
				*     @graph{* 1:2, 2:3, 3:1 *} <br>
				*     @hashTable[* "one":1, "two":2 *] <br>
				* </code><br>
				 * that is parsed with the help of the Cyan compiler.
				*/

				cyanMetaobject.setMetaobjectAnnotation(annotation);
				Compiler_dpa compiler_dpa = new Compiler_dpa(this, lexer,
						leftCharSeqSymbol.getSymbolString());
				int offsetLeftCharSeq = this.symbol.getOffset() + symbol.getSymbolString().length();

				compilerAction_dpa = compiler_dpa;

				/*
				 * the errors found in the compilation are introduced in object 'this'. They will be
				 * processed as regular compiler errors.
				 */


				try {
					((IParseWithCyanCompiler_dpa ) cyanMetaobject).dpa_parse(compiler_dpa);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					e.printStackTrace();
					thrownException(annotation, annotation.getFirstSymbol(), e);
				}
				finally {
					this.metaobjectError(cyanMetaobject, annotation);
					this.copyLexerData(compiler_dpa.compiler);
				}

				annotation.setExprStatList(compiler_dpa.getExprStatList());

				if ( symbol.token == Token.RIGHTCHAR_SEQUENCE ) {
					int offsetRightCharSeq = this.symbol.getOffset();
					/*
					 * store the text between the left and right char sequence in the metaobject annotation
					 */
					char []text = lexer.getText(offsetLeftCharSeq, offsetRightCharSeq);
					annotation.setText(text);
					annotation.setLeftCharSeqSymbol(leftCharSeqSymbol);
					cyanMetaobject.setMetaobjectAnnotation(annotation);
					annotation.setRightCharSeqSymbol( (SymbolCharSequence ) symbol);

					next();
					annotation.setNextSymbol(symbol);

				}
				else {
					if ( lexer.expectedRightSymbolSequence() == null ) {
						this.error(true, symbol,
								"Internal error in Compiler::annotation", null, ErrorKind.internal_error);
					}
					else
						error(true, symbol,
							"The right symbol sequence '" + lexer.expectedRightSymbolSequence() + "' was expected." + foundSuch(),
							null, ErrorKind.right_symbol_sequence_expected);
				}
				annotation.setNextSymbol(symbol);
				lexer.setNewLineAsToken(false);
				foundtextBetweenSeq = true;


			}
			else if ( cyanMetaobject instanceof IParseWithoutCyanCompiler_dpa || cyanMetaobject instanceof IAction_dsa ) {
					/*
					 * found a literal object that is parsed with a user-defined compiler. That is,
					 *  the text inside the literal object is not parsed with the help of the Cyan compiler.
					 */


					if ( cyanMetaobject instanceof IActionNewPrototypes_dpa )  {
						compilerAction_dpa = new CompilerAction_dpa(this);
					}


					char []rightCharSeq = lexer.getRightSymbolSeq(leftCharSeqSymbol.getCharSequence(), leftCharSeqSymbol.getSizeCharSequence());
					char []text = lexer.getTextTill(rightCharSeq, leftCharSeqSymbol.getOffset() + leftCharSeqSymbol.getSizeCharSequence());
					SymbolCharSequence rightCharSeqSymbol = lexer.getSymbolRightCharSeq();

					annotation.setLeftCharSeqSymbol(leftCharSeqSymbol);
					annotation.setText(text);
					cyanMetaobject.setMetaobjectAnnotation(annotation);
					annotation.setRightCharSeqSymbol(rightCharSeqSymbol);
					next();
					annotation.setNextSymbol(symbol);

					foundtextBetweenSeq = true;


					if ( cyanMetaobject instanceof IParseWithoutCyanCompiler_dpa ) {
						/**
						 * if not, cyanMetaobject is an instance of IAction_dsa and it will be parsed without the compiler in
						 * phase dsa
						 */
						try {
							ICompilerAction_dpa compilerAction = new CompilerAction_dpa(this);
							((IParseWithoutCyanCompiler_dpa ) cyanMetaobject).dpa_parse( compilerAction, new String(annotation.getText()) );
						}
						catch ( error.CompileErrorException e ) {
						}
						catch ( RuntimeException e ) {
							thrownException(annotation, annotation.getFirstSymbol(), e);
						}
						finally {
							this.metaobjectError(cyanMetaobject, annotation);
						}

					}

			}
			else {
				this.error(true,  symbol,
							"metaobject annotation '" + metaobjectName + "' has a Java class " +
					" that does not implement any of the following interfaces: 'IParseWithoutCyanCompiler_dpa', 'IParseWithCyanCompiler_dpa', 'IAction_cge', IAction_dsa",
					null, ErrorKind.metaobject_error);

			}
		}

		if ( cyanMetaobject instanceof IActionNewPrototypes_dpa && compilerAction_dpa != null ) {
			IActionNewPrototypes_dpa actionNewPrototype = (IActionNewPrototypes_dpa ) cyanMetaobject;
			ArrayList<Tuple2<String, StringBuffer>> prototypeNameCodeList = null;
			try {
				prototypeNameCodeList = actionNewPrototype.dpa_NewPrototypeList(compilerAction_dpa);
			}
			catch ( error.CompileErrorException e ) {
			}
			catch ( RuntimeException e ) {
				thrownException(annotation, annotation.getFirstSymbol(), e);
			}
			finally {
				metaobjectError(cyanMetaobject, annotation);
			}
			if ( prototypeNameCodeList != null ) {
				for ( Tuple2<String, StringBuffer> prototypeNameCode : prototypeNameCodeList ) {
					Tuple2<CompilationUnit, String> t = this.project.getCompilerManager().createNewPrototype(prototypeNameCode.f1, prototypeNameCode.f2,
							this.compilationUnit.getCompilerOptions(), this.compilationUnit.getCyanPackage());
					if ( t != null && t.f2 != null ) {
						this.error2(metaobjectAnnotationSymbol, t.f2);
					}
				}
			}

		}


		/*
		 * this is necessary because getLeftCharSequence, called in some previous 'if', does not follow
		 * the regular rules of lexical analysis.
		 */
		//if ( ! cyanMetaobject.mayTakeArguments() && ! cyanMetaobject.mayTakeText() )
		//	next();

		cyanMetaobject.setMetaobjectAnnotation(annotation);

		//# checkError = ...   was here

		/**
		 * the following steps only make sense in real prototypes. The generic ones should not be checked,
		 * no action should be taken on them.
		 *
		 * It is used method {@link CompilationUnit#hasGenericPrototype} because it checks the file
		 * name of the source code. Metaobjects may be used before the compiler knows, by the source
		 * file, that there is a generic prototype.
		 */
		if ( ! compilationUnit.hasGenericPrototype() )  {


			if ( cyanMetaobject.shouldTakeText() && ! foundtextBetweenSeq ) {
				this.error(true, metaobjectAnnotationSymbol,
						"Metaobject '" + cyanMetaobject.getName() +
								"' should take a text between two sequences of " +
								"characters after the metaobject name (maybe followed by parameters). Example: @graph{* 1:2 2:3 *}",
						metaobjectAnnotationSymbol.getSymbolString(), ErrorKind.metaobject_error);
			}


			if ( cyanMetaobject instanceof CyanMetaobjectCompilationContextPush ) {
				if ( compInstSet.contains(CompilationInstruction.dpa_originalSourceCode) &&
						!this.hasAdded_pp_new_Methods && !this.hasAddedInnerPrototypes
						&& ! hasMade_dpa_actions )
					this.error(true, metaobjectSymbol,
							"'@" + NameServer.pushCompilationContextName + "' can only be annotated by the compiler", null, ErrorKind.metaobject_error);


				if ( javaObjectList.size() == 2 ) {
					cyanMetaobjectContextStack.push(new Tuple5<>((String ) javaObjectList.get(0), (String )
						       javaObjectList.get(1), null, null, null));

				}
				else if ( javaObjectList.size() == 5 ) {
					cyanMetaobjectContextStack.push(new Tuple5<>((String ) javaObjectList.get(0), (String )
						       javaObjectList.get(1), (String ) javaObjectList.get(2),
						       (String ) javaObjectList.get(3), (Integer ) javaObjectList.get(4)));
				}
				insideCyanMetaobjectCompilationContextPushAnnotation = false;
				if ( lineNumberStartCompilationContextPush < 0 ) {
					  /*
					   * The '< 0' avoids that the line below is executed inside nested
					   * metaobject annotations of CompilationContextPush
					   */
					this.lineNumberStartCompilationContextPush = metaobjectSymbol.getLineNumber();
				}

			}
			else if ( cyanMetaobject instanceof CyanMetaobjectCompilationContextPop ) {
				if ( compInstSet.contains(CompilationInstruction.dpa_originalSourceCode) &&
						!this.hasAdded_pp_new_Methods && !this.hasAddedInnerPrototypes &&
						! hasMade_dpa_actions )
					this.error(true, metaobjectSymbol,
							"'@popCompilationContext' can only be annotated by the compiler", null, ErrorKind.metaobject_error);
				if ( cyanMetaobjectContextStack.empty() )
					this.error(true, metaobjectSymbol,
							"Attempt to pop a context through '@popCompilationContext' in an empty stack", null, ErrorKind.internal_error);
				else {

					String id = (String ) javaObjectList.get(0);
					if ( ! id.equals(cyanMetaobjectContextStack.peek().f1) ) {
						this.error(true, metaobjectSymbol,
								"Attempt to pop a context through '@popCompilationContext' with a wrong id. It should be '" +
										cyanMetaobjectContextStack.peek().f1 + "'", null, ErrorKind.internal_error);
					}
					else {
						cyanMetaobjectContextStack.pop();
						if ( cyanMetaobjectContextStack.isEmpty() && ! insideCyanMetaobjectCompilationContextPushAnnotation ) {
							boolean replace = true;
							String symName = symbol.symbolString;
							if ( symbol instanceof lexer.SymbolCyanMetaobjectAnnotation && (symName.equals(NameServer.pushCompilationContextName) ||
									symName.equals(NameServer.pushCompilationContextStatementName) )) {
								replace = false;
							}
							if ( replace ) {
								symbol.setLineNumber(symbol.getLineNumber() - lineShift);
							}
						}
						// lineShift += cyanMetaobject.getMetaobjectAnnotation().getSymbolCTMOCall().getLineNumber() - lineStartCompilationContextPush + 1;
						/*
						if ( cyanMetaobjectContextStack.empty() ) {
							lineShift -= metaobjectSymbol.getLineNumber() - lineStartCompilationContextPush + 1;
						}
						*/
					}
				}
				lineShift += metaobjectSymbol.getLineNumber() - this.lineNumberStartCompilationContextPush + 1;
				this.lineNumberStartCompilationContextPush = -1;
			}
			else if ( cyanMetaobject instanceof CyanMetaobjectCompilationMarkDeletedCode ) {
				if ( compInstSet.contains(CompilationInstruction.dpa_originalSourceCode) &&
						!this.hasAdded_pp_new_Methods && !this.hasAddedInnerPrototypes &&
						! hasMade_dpa_actions )
					this.error(true, metaobjectSymbol,
							"'@markDeletedCodeName' can only be annotated by the compiler", null, ErrorKind.metaobject_error);

				lineShift -= ((CyanMetaobjectCompilationMarkDeletedCode) cyanMetaobject).getNumLinesDeleted();

			}


			if ( cyanMetaobject instanceof IStaticTyping )
				this.forceUseOfStaticTyping = true;

			if ( cyanMetaobject instanceof IAction_dpp ) {
				if ( ! this.compInstSet.contains(CompilationInstruction.pyanSourceCode) ) {
					this.error2(metaobjectSymbol, "This metaobject can only be used inside a project file, one with '.pyan' extension");
				}
				IAction_dpp icp = (IAction_dpp ) cyanMetaobject;

				try {
					icp.dpp_action(project);

				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					thrownException(annotation, annotation.getFirstSymbol(), e);
				}
				finally {
					this.metaobjectError(cyanMetaobject, annotation);
				}

			}

		}

		if ( cyanMetaobject instanceof IInformCompilationError ) {
			IInformCompilationError cyanMetaobjectCompilationError = (IInformCompilationError ) cyanMetaobject;

			if ( ! compilationUnit.hasGenericPrototype() || cyanMetaobjectCompilationError.activeInGenericPrototype() ) {
				this.compilationUnit.addLineMessageList(new Tuple3<Integer, String, Boolean>(cyanMetaobjectCompilationError.getLineNumber(),
						cyanMetaobjectCompilationError.getErrorMessage(), false));
			}
		}
		/*
		if ( metaobjectName.equals("createTuple") && ! this.currentProgramUnit.isGeneric() &&
				this.currentProgramUnit.getName().equals("Tuple<key,String,value,Any>") ) {
			MyFile.write(this.compilationUnit);
		}
		*/

		if ( localVariableDecStack != null && localVariableDecStack.size() > 0 ) {
			ArrayList<Tuple2<String, String>> localVariableNameList = new ArrayList<>();
			for (StatementLocalVariableDec aVar : this.localVariableDecStack ) {
				String strType = null;
				if ( aVar.getTypeInDec() != null ) {
					strType = aVar.asString();
				}
				localVariableNameList.add( new Tuple2<String, String>(aVar.getName(), strType));
			}
			annotation.setLocalVariableNameList(localVariableNameList);
		}

		if ( ! ( cyanMetaobject instanceof meta.cyanLang.CyanMetaobjectCompilationContextPop ||
				 cyanMetaobject instanceof meta.cyanLang.CyanMetaobjectCompilationContextPush ||
				 cyanMetaobject instanceof meta.cyanLang.CyanMetaobjectCompilationMarkDeletedCode ) ) {

			String packageOfType = cyanMetaobject.getPackageOfType();
			String prototypeOfType = cyanMetaobject.getPrototypeOfType();

			if ( cyanMetaobject.isExpression() ) {
				if ( this.currentProgramUnit == null ) {
					// metaobject produce code that can be used inside an expression but it is being used outside a program unit
					this.error2(annotation.getFirstSymbol(),
							"Method 'isExpression' of this metaobject returns true meaning this object is an expression. " +
					        "Therefore it can only be used inside a prototype");
					return null;
				}
				if ( packageOfType == null || prototypeOfType == null )  {
					this.error2(annotation.getFirstSymbol(),
							"Method 'isExpression' of this metaobject returns true meaning this object is an expression. " +
					        "However, either method 'getPackageOfType' or 'getPrototypeOfType' return null");
					return null;

				}

				if ( packageOfType.indexOf(' ') >= 0 ) {
					this.error2(annotation.getFirstSymbol(),
							"Method 'getPackageOfType' of this metaobject returns a package name that has spaces in it");
					return null;
				}
				if ( prototypeOfType.indexOf(' ') >= 0 ) {
					this.error2(annotation.getFirstSymbol(),
							"Method 'getPrototypeOfType' of this metaobject returns a name that has spaces in it. This is illegal."
							+ " Instead of 'Tuple<Int, String>', use 'Tuple<Int,String>'");
					return null;
				}

				/*
					the expression below is associated to this compilation unit. In the phase ati,
					the compiler will assure that the type is created if necessary.
				 */
				Expr programUnitAsExpr = Compiler.parseSingleTypeFromString(packageOfType + "." + prototypeOfType, annotation.getFirstSymbol(),
						"The type of this metaobject annotation is '" + packageOfType + "." + prototypeOfType +
						"'. This type is not syntactically correct",
						this.compilationUnit, this.currentProgramUnit /*, this */);
				if ( programUnitAsExpr instanceof ExprGenericPrototypeInstantiation ) {
					this.currentProgramUnit.addGpiList( (ExprGenericPrototypeInstantiation ) programUnitAsExpr );
				}
			}
			else {
				if ( packageOfType != null || prototypeOfType != null )  {
					this.error2(annotation.getFirstSymbol(),
							"Method 'isExpression' of this metaobject returns false meaning this object is NOT an expression. " +
							"However, either method 'getPackageOfType' or 'getPrototypeOfType' return a non-null string");
					return null;
				}
				if ( inExpr ) {
					this.error2(annotation.getFirstSymbol(),
							"Method 'isExpression' of this metaobject returns false meaning this object is NOT an expression. " +
							"However, this metaobject is being used inside an expression");
				}
			}

		}

		return annotation;
	}


	/**
	   @param cyanMetaobject
	   @param annotation
	 */
	public void metaobjectError(CyanMetaobject cyanMetaobject, CyanMetaobjectAnnotation annotation) {
		ArrayList<CyanMetaobjectError> errorList = cyanMetaobject.getErrorMessageList();
		if ( errorList != null ) {
			for ( CyanMetaobjectError moError : errorList ) {
				error2(annotation.getFirstSymbol(), moError.getMessage());
			}
		}
	}


	private ProgramUnit programUnit() {

		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList = null;
		ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList = null;


		Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tc = parseMetaobjectAnnotations_NonAttached_Attached();
		if ( tc != null ) {
			nonAttachedMetaobjectAnnotationList = tc.f1;
			attachedMetaobjectAnnotationList = tc.f2;
		}

		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject().attachedToSomething() ) {
					if ( ! annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.PROTOTYPE_DEC) )
						this.error2(annotation.getFirstSymbol(), "This metaobject annotation cannot be attached to a prototype. It can be attached to " +
					       " one entity of the following list: [ "+
								annotation.getCyanMetaobject().attachedListAsString() + " ]");


				}
			}





		}
		Token visibility = Token.PUBLIC;

		if ( symbol.token == Token.PUBLIC ||
			 symbol.token == Token.PACKAGE ) {
			visibility = symbol.token;
			if ( symbol.token == Token.PACKAGE ) {
				this.warning(symbol, "'package' visibility for prototypes is not yet supported. Changed to 'public'");
				visibility = Token.PUBLIC;
			}
			next();
		}
		if ( visibility == Token.PUBLIC || visibility == Token.PACKAGE ) {
			++this.numPublicPackageProgramUnits;
		}
		if ( symbol.token == Token.INTERFACE )
			interfaceDec(visibility, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, null);
		else
			objectDec(visibility, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, null);

		if ( attachedMetaobjectAnnotationList != null && attachedMetaobjectAnnotationList.size() > 0 ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.PROTOTYPE_DEC) ) {
					annotation.setDeclaration(this.currentProgramUnit);
				}
			}
		}

		if ( ! cyanMetaobjectContextStack.empty() && ! this.compilationUnitSuper.hasCompilationError() )
			this.error(true, null,
					"'@" + NameServer.pushCompilationContextName + "(" + cyanMetaobjectContextStack.peek().f1 +
							", ...)' was used inside the source code without the '@" +
							NameServer.popCompilationContextName + "(" + cyanMetaobjectContextStack.peek().f1
							+ ")'", null, ErrorKind.metaobject_error);
		/*
		 * set the number of the metaobject annotations considering only the metaobjects with the same name. That is,
		 * if there are annotations to metaobjects "moA", "moB", "moA", "moA", and "moB", in this textual order,
		 * the code below associate to these annotations the number 1, 1, 2, 3, 2.
		 */
		HashMap<String, Integer> mapMetaobjectNameToNumber = new HashMap<>();
		for ( CyanMetaobjectAnnotation annotation : currentProgramUnit.getCompleteMetaobjectAnnotationList() ) {
			String name = annotation.getCyanMetaobject().getName();
			Integer metaobjectNumber = mapMetaobjectNameToNumber.get(name);
			int numberByKind;
			numberByKind = metaobjectNumber != null ? metaobjectNumber + 1 : CyanMetaobjectAnnotation.firstMetaobjectAnnotationNumber;
			mapMetaobjectNameToNumber.put(name,  numberByKind);
			annotation.setMetaobjectAnnotationNumberByKind(numberByKind);

		}



		return currentProgramUnit;

	}

	/**
	 *
	 * @param visibility
	 * @param metaobjectAnnotationList
	 * @return
	 */
	private ProgramUnit interfaceDec(Token visibility,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList, ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
			ObjectDec outerObject) {

		//		ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList, ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList

		InterfaceDec interfaceDec = null;
		Symbol interfaceSymbol = symbol;

		next();
		if ( symbol.token != Token.IDENT ) {
			error(true, symbol, "interface name expected." + foundSuch(), "", ErrorKind.interface_name_expected);
		}
		else {
			methodNumber = 0;
			currentProgramUnit = interfaceDec = new InterfaceDec(outerObject, interfaceSymbol,
					(SymbolIdent ) symbol, visibility, nonAttachedMetaobjectAnnotationList,
					attachedMetaobjectAnnotationList,
					lexer);
			currentProgramUnit.setCompilationUnit(compilationUnit);


			compilationUnit.addProgramUnit(currentProgramUnit);


			currentProgramUnit.setFirstSymbol(interfaceSymbol);

			if ( attachedMetaobjectAnnotationList != null ) {
				/*
				 * the number of a metaobject annotation that is textually before a prototype
				 * is not set in method {@link Compiler#annotation()} before when this
				 * method is called {@link currentProgramUnit} is null.
				 */
				for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
					currentProgramUnit.addMetaobjectAnnotation(annotation);
					annotation.setCompilationUnit(compilationUnit);
					annotation.setProgramUnit(currentProgramUnit);
					annotation.setMetaobjectAnnotationNumber(currentProgramUnit.getIncMetaobjectAnnotationNumber());
					annotation.setDeclaration(currentProgramUnit);
					if ( annotation.getCyanMetaobject() instanceof IActionAssignment_cge ) {
						this.error2(annotation.getFirstSymbol(), "Metaobject " + annotation.getCyanMetaobject().getName() + " implements " +
					     "interface IActionAssignment_cge and can only be attached to prototypes that are not interfaces");
					}
				}
			}


			next();

			if ( symbol.token != Token.LT_NOT_PREC_SPACE ) {
				// check if the file name of the source file is correct
				if ( currentProgramUnit.getVisibility() == Token.PUBLIC ) {
					String fileNameCurrentProgramUnit = currentProgramUnit.getName() + "." + NameServer.cyanSourceFileExtension;
					if ( fileNameCurrentProgramUnit.compareTo(compilationUnit.getFilename()) != 0 )
						error(true,
								currentProgramUnit.getSymbol(),
								"The file name of this compilation unit has an incorret name. It should be " + fileNameCurrentProgramUnit, "", ErrorKind.file_name_incorrect_in_compilation_unit);
				}
			}
			else {
				if ( currentProgramUnit.getVisibility() != Token.PUBLIC ) {
					error(true, currentProgramUnit.getSymbol(),
							"Generic prototypes should be declared 'public'", currentProgramUnit.getName(), ErrorKind.non_public_generic_prototype);
				}
				if ( compilationUnit.getHasGenericPrototype() ) {
					error(true,
						 currentProgramUnit.getSymbol(),
						 "Two generic prototypes cannot be declared in the same source file",
						 currentProgramUnit.getName(), ErrorKind.two_or_more_generic_prototype_in_the_same_source_file);
				}
				while ( symbol.token == Token.LT_NOT_PREC_SPACE )
					currentProgramUnit.addGenericParameterList(templateDec());

				/*
				 * check if real and formal parameters are mixed in the declaration of
				 * the generic prototype. It is illegal to declare
				 *        object Function<T, Int> ... end
				 *        object Struct<Boolean, Int, U>


				boolean hasRealParameter = false;
				boolean hasTypeParameter = false;
				GenericParameter firstTypeGenericParameter = null;
				GenericParameter firstRealPrototypeGenericParameter = null;
				for ( ArrayList<GenericParameter> genericParameterList : currentProgramUnit.getGenericParameterListList() ) {
					for ( GenericParameter gp : genericParameterList ) {
						if ( gp.isRealPrototype() ) {
							hasRealParameter = true;
							firstRealPrototypeGenericParameter = gp;
							if ( hasTypeParameter )
								error(currentProgramUnit.getSymbol(),
										"Generic parameters and real parameters cannot be mixed in generic prototype declaration. That is, 'Function<T, Int>' is illegal in which T is a formal parameter",
										"", ErrorKind.mixins_of_generic_and_non_generic_parameters,
										"parameter0 = " + firstRealPrototypeGenericParameter.getName(),
										"parameter1 = " + firstTypeGenericParameter.getName());
						}
						else {
							firstTypeGenericParameter = gp;
							hasTypeParameter = true;
							if ( hasRealParameter )
								error(currentProgramUnit.getSymbol(),
										"Generic parameters and real parameters cannot be mixed in generic prototype declaration. That is, 'Function<T, Int>' is illegal in which T is a formal parameter",
										"", ErrorKind.mixins_of_generic_and_non_generic_parameters,
										"parameter0 = " + firstRealPrototypeGenericParameter.getName(),
										"parameter1 = " + firstTypeGenericParameter.getName());
						}
					}
				}
				*/

				boolean hasTypeParameter = checkFormalParameterListGenericPrototype(currentProgramUnit);

				currentProgramUnit.setGenericPrototype(hasTypeParameter);
				compilationUnit.setHasGenericPrototype(hasTypeParameter);
				compilationUnit.setPrototypeIsNotGeneric( ! hasTypeParameter);
				currentProgramUnit.setPrototypeIsNotGeneric( ! hasTypeParameter);
				/**
				 * check if the source file has the correct name. It should be "Stack(Int).cyan" if
				 * the generic prototype is "Stack<Int>" and "Function(2)(1).cyan" if the generic
				 * prototype is "Function<T1, T2><R>".
				 *
				 */

				filename = currentProgramUnit.getNameSourceFile() + "." + NameServer.cyanSourceFileExtension;
				if ( filename.compareTo(compilationUnit.getFilename()) != 0 ) {
					currentProgramUnit.getNameSourceFile();
					error2(currentProgramUnit.getSymbol(), "The file name of this compilation unit has an incorret name. It should be " + filename);
				}


			}

			ArrayList<CyanMetaobjectWithAtAnnotation> beforeExtendsAttachedMetaobjectAnnotationList = null;
			ArrayList<CyanMetaobjectWithAtAnnotation> beforeExtendsNonAttachedMetaobjectAnnotationList = null;

			if (  symbol.token == Token.METAOBJECT_ANNOTATION ) {
				Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tc = parseMetaobjectAnnotations_NonAttached_Attached();
				if ( tc != null ) {
					beforeExtendsNonAttachedMetaobjectAnnotationList = tc.f1;
					beforeExtendsAttachedMetaobjectAnnotationList = tc.f2;
				}
				if ( symbol.token == Token.EXTENDS ) {
					if ( beforeExtendsAttachedMetaobjectAnnotationList != null ) {
						CyanMetaobject cyanMetaobject = beforeExtendsAttachedMetaobjectAnnotationList.get(0).getCyanMetaobject();
						this.error(true, beforeExtendsAttachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
								"There is a metaobject annotation to metaobject '" +
										cyanMetaobject.getName() + "' that should be attached to a declaration " +
										"(prototype, method, instance variable, etc). However, no declaration follows the metaobject annotation", null, ErrorKind.metaobject_error);
					}
					this.currentProgramUnit.setCyanMetaobjectListBeforeExtendsMixinImplements(beforeExtendsNonAttachedMetaobjectAnnotationList);
					beforeExtendsNonAttachedMetaobjectAnnotationList = null;
				}
			}

			if ( symbol.token == Token.IMPLEMENTS ) {
				this.error2(symbol,  "Use 'extends' instead of 'implements'");
			}
			if ( symbol.token == Token.EXTENDS ) {
				next();
				interfaceDec.setSuperInterfaceExprList(typeList());
			}
			else {
				if ( beforeExtendsAttachedMetaobjectAnnotationList != null || beforeExtendsNonAttachedMetaobjectAnnotationList != null ) {
					nonAttachedMetaobjectAnnotationList = beforeExtendsNonAttachedMetaobjectAnnotationList;
					attachedMetaobjectAnnotationList = beforeExtendsAttachedMetaobjectAnnotationList;
				}
			}

			Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tc;
			if ( beforeExtendsAttachedMetaobjectAnnotationList == null && beforeExtendsNonAttachedMetaobjectAnnotationList == null ) {
				tc = parseMetaobjectAnnotations_NonAttached_Attached();
				if ( tc != null ) {
					nonAttachedMetaobjectAnnotationList = tc.f1;
					attachedMetaobjectAnnotationList = tc.f2;
				}
			}


			while ( symbol.token == Token.FUNC || symbol.token == Token.PUBLIC || symbol.token == Token.PRIVATE
					|| symbol.token == Token.PROTECTED || symbol.token == Token.OVERRIDE
					|| symbol.token == Token.ABSTRACT  || symbol.token == Token.METAOBJECT_ANNOTATION
					) {

				if ( symbol.token == Token.OVERRIDE ) {
					if ( ask(symbol, "Keyword 'override' cannot appear in a method signature of an interface. Can I remove it? (y, n)") ) {
						compilationUnit.addAction(
								new ActionDelete(compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getSymbolString().length(),
										symbol.getLineNumber(),
										symbol.getColumnNumber()));
					}
					else {
						this.error(true, symbol,
								"Keyword 'override' cannot appear in a method signature of an interface", symbol.getSymbolString(),
								ErrorKind.qualifier_cannot_preced_method_signature_in_interfaces, "qualifier = override");
					}

				}
				else if ( symbol.token == Token.ABSTRACT ) {
					if ( ask(symbol, "Keyword 'abstract' cannot appear in a method signature of an interface. Can I remove it? (y, n)") ) {
						compilationUnit.addAction(
								new ActionDelete(compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getSymbolString().length(),
										symbol.getLineNumber(),
										symbol.getColumnNumber()));
					}
					else {
						this.error(true, symbol,
								"Keyword 'abstract' cannot appear in a method signature of an interface", symbol.getSymbolString(), ErrorKind.abstract_cannot_preced_method_signature_in_interfaces);
					}

				}
				else if ( symbol.token == Token.PUBLIC || symbol.token == Token.PRIVATE
					|| symbol.token == Token.PROTECTED ) {
					String qualifier = symbol.getSymbolString();
					next();
					if ( ask(symbol, "Qualifier '"+ qualifier + "' is illegal here. Can I remove it? (y, n)") ) {
						compilationUnit.addAction(
								new ActionDelete(compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getSymbolString().length(),
										symbol.getLineNumber(),
										symbol.getColumnNumber()));
					}
					else {
						this.error(true, symbol,
								"'public',  'protected',  or 'private' cannot appear before a method signature in an interface", symbol.getSymbolString(),
								ErrorKind.qualifier_cannot_preced_method_signature_in_interfaces, "qualifier = " + qualifier);
					}

				}
				else {
					next();

					MethodSignature ms = null;
					try {
						this.prohibitTypeof = true;
						ms = methodSignature();
					}
					finally {
						this.prohibitTypeof = false;
					}


					interfaceDec.addMethodSignature(ms);
					ms.setDeclaringInterface(interfaceDec);

					if ( ms.getNameWithoutParamNumber().equals(NameServer.initOnce) )
						error2(ms.getFirstSymbol(), "'initOnce' methods cannot be declared in interfaces");


					ms.setMetaobjectAnnotationNonAttachedAttached(nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList);

					if ( attachedMetaobjectAnnotationList != null ) {
						for ( CyanMetaobjectAnnotation annotation : attachedMetaobjectAnnotationList ) {

							CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
							if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
								ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
								Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
								if ( t != null )
									ms.addFeature(t);
							}
						}
					}


					nonAttachedMetaobjectAnnotationList = null;
					attachedMetaobjectAnnotationList = null;



					tc = parseMetaobjectAnnotations_NonAttached_Attached();
					if ( tc != null ) {
						nonAttachedMetaobjectAnnotationList = tc.f1;
						attachedMetaobjectAnnotationList = tc.f2;
					}
				}

			}
			if ( attachedMetaobjectAnnotationList != null ) {
				CyanMetaobject cyanMetaobject = attachedMetaobjectAnnotationList.get(0).getCyanMetaobject();
				this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
						"There is a metaobject annotation to metaobject '" +
								cyanMetaobject.getName() + "' that should be attached to a declaration " +
								"(prototype, method, instance variable, etc). However, no declaration follows the metaobject annotation", null, ErrorKind.metaobject_error);

			}
			this.currentProgramUnit.setBeforeEndNonAttachedMetaobjectAnnotationList(nonAttachedMetaobjectAnnotationList);

			if ( symbol.token != Token.END )
				error(true, symbol, "keyword 'end'." + foundSuch(), "", ErrorKind.keyword_end_expected);
			this.currentProgramUnit.setEndSymbol(symbol);
			/*
			if ( symbol.getColumnNumber() != this.currentProgramUnit.getFirstSymbol().getColumnNumber() ) {
				error2(symbol, "'end' should be in the same column as 'object'");
			}
			*/


			next();

		}
		lexer.setProgramUnit(null);

		return interfaceDec;

	}


	/**
	 *
	   @param visibility
	   @param metaobjectAnnotationList
	   @param outerObject  If this object is declared inside another object, 'outerObject' is this external object
	   @return
	 */

	private ObjectDec objectDec(Token visibility,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList, ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
			    ObjectDec outerObject) {


		methodNumber = 0;
		ObjectDec currentObject = new ObjectDec(outerObject, visibility,
				nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList,
				lexer);
		if ( currentObject.getOuterObject() == null ) {
			compilationUnit.addProgramUnit(currentObject);
		}
		currentObject.setCompilationUnit(compilationUnit);
		objectDecStack.push(currentObject);


		currentObject.setCompilationUnit(this.compilationUnit);


		currentProgramUnit = currentObject;


		if ( attachedMetaobjectAnnotationList != null ) {
			/*
			 * the number of a metaobject annotation that is textually before a prototype
			 * is not set in method {@link Compiler#annotation()} before when this
			 * method is called {@link currentProgramUnit} is null.
			 */
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				currentProgramUnit.addMetaobjectAnnotation(annotation);
				annotation.setCompilationUnit(compilationUnit);
				annotation.setProgramUnit(currentProgramUnit);
				annotation.setMetaobjectAnnotationNumber(currentProgramUnit.getIncMetaobjectAnnotationNumber());
				annotation.setDeclaration(currentProgramUnit);
			}


			/*
			 // to be used to do checkings of metaobject concept before sy

			if ( this.notNullIfCreatingGenericPrototypeInstantiation != null ) {
				notNullIfCreatingGenericPrototypeInstantiation.atBeginningOfCurrentCompilationUnit(this.compilationUnit);
				notNullIfCreatingGenericPrototypeInstantiation.atBeginningOfObjectDec(this.currentProgramUnit);
				try {
		            for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
		                annotation.calcInternalTypes(notNullIfCreatingGenericPrototypeInstantiation);

		                CyanMetaobject metaobject = annotation.getCyanMetaobject();
		                metaobject.setMetaobjectAnnotation(annotation);
		                if ( metaobject instanceof ICheckProgramUnit_before_dsa ) {
		                    ICheckProgramUnit_before_dsa fp = (ICheckProgramUnit_before_dsa ) metaobject;

		                    Compiler_dsa compiler_dsa = new Compiler_dsa(this.notNullIfCreatingGenericPrototypeInstantiation);
		                    try {
		                        fp.before_dsa_checkProgramUnit(compiler_dsa);
		                    }
		                    catch ( error.CompileErrorException e ) {
		                    }
		                    catch ( RuntimeException e ) {
		                        e.printStackTrace();
		                        notNullIfCreatingGenericPrototypeInstantiation.thrownException(annotation, annotation.getFirstSymbol(), e);
		                    }
		                    finally {
		                    	notNullIfCreatingGenericPrototypeInstantiation.errorInMetaobjectCatchExceptions(metaobject, annotation);
		                    }


		                }


		            }

				}
				finally {
					notNullIfCreatingGenericPrototypeInstantiation.atEndOfObjectDec();
					notNullIfCreatingGenericPrototypeInstantiation.atEndOfCurrentCompilationUnit();
				}


			}
			*/

		}

		if ( symbol.token == Token.ABSTRACT) {
			next();
			currentObject.setIsAbstract(true);
		}
		if ( symbol.token == Token.FINAL) {
			next();
			currentObject.setIsFinal(true);
		}

		if ( ! checkIf(Token.OBJECT, "object") ) {
			error(true, symbol,
					"A prototype declaration was expected." + foundSuch(),
					symbol.getSymbolString(), ErrorKind.keyword_object_expected);
			this.skipTo(Token.OBJECT, Token.INTERFACE);
		}

		if ( symbol.token != Token.OBJECT )
			error2(symbol, "keyword 'object' or 'interface' expected." + foundSuch());

		currentObject.setSymbolObjectInterface(symbol);
		currentObject.setFirstSymbol(symbol);
		next();
		if ( symbol.token != Token.IDENT && ! isBasicType(symbol.token) && symbol.token != Token.NIL
			 && symbol.token != Token.STRING )
			error2(symbol, "Object name expected." + foundSuch());
		currentObject.setSymbol( symbol );

		next();
		if ( symbol.token != Token.LT_NOT_PREC_SPACE ) {
			// check if the file name of the source file is correct
			if ( currentObject.getVisibility() == Token.PUBLIC ) {
				String fileNameCurrentObjectDec = currentObject.getName() + "." + NameServer.cyanSourceFileExtension;
				  // if outerObject != null then this is an inner object. It name can be different from
				  // the source file.
				if ( outerObject == null &&
						fileNameCurrentObjectDec.compareTo(compilationUnit.getFilename()) != 0 ) {
					error2(currentObject.getSymbol(), "The file name of this compilation unit has an incorret name. It should be " + fileNameCurrentObjectDec);
				}
			}
		}
		else {

			   // a generic prototype. It should be public and it should be the only one in the file
			if ( currentObject.getVisibility() != Token.PUBLIC ) {
				error2(currentObject.getSymbol(), "Generic prototypes should be declared 'public'");
			}
			if ( compilationUnit.getHasGenericPrototype() ) {
				error2(currentObject.getSymbol(), "Two generic prototypes cannot be declared in the same source file");
			}
			while ( symbol.token == Token.LT_NOT_PREC_SPACE )
				currentObject.addGenericParameterList(templateDec());



			/*
			 * check if real and formal parameters are mixed in the declaration of
			 * the generic prototype. It is illegal to declare any of the objects below
			 *        object Function<T, Int> ... end
			 *        object Struct<Boolean, Int, U>


			boolean hasRealParameter = false;
			boolean hasTypeParameter = false;
			for ( ArrayList<GenericParameter> genericParameterList : currentObject.getGenericParameterListList() ) {
				for ( GenericParameter gp : genericParameterList ) {
					if ( gp.isRealPrototype() ) {
						hasRealParameter = true;
						if ( hasTypeParameter )
							error2(currentObject.getSymbol(), "generic parameters and real parameters cannot be mixed in generic prototype declaration. That is, 'Function<T, Int>' is illegal if T is a formal parameter");
					}
					else {
						hasTypeParameter = true;
						if ( hasRealParameter )
							error2(currentObject.getSymbol(), "generic parameters and real parameters cannot be mixed in generic prototype declaration. That is, 'Function<T, Int>' is illegal if T is a formal parameter");
					}
				}
			}
			*/
			boolean hasTypeParameter = checkFormalParameterListGenericPrototype(currentObject);

			currentProgramUnit.setGenericPrototype(hasTypeParameter);
			compilationUnit.setHasGenericPrototype(hasTypeParameter);
			compilationUnit.setPrototypeIsNotGeneric( ! hasTypeParameter);
			currentProgramUnit.setPrototypeIsNotGeneric( ! hasTypeParameter);
			/**
			 * check if the source file has the correct name. It should be "Stack(Int).cyan" if
			 * the generic prototype is "Stack<Int>" and "Function(2)(1).cyan" if the generic
			 * prototype is "Function<T1, T2><R>".
			 *
			 */

			filename = currentObject.getNameSourceFile() + "." + NameServer.cyanSourceFileExtension;
			if ( filename.compareTo(compilationUnit.getFilename()) != 0 ) {
				currentObject.getNameSourceFile();
				error2(currentObject.getSymbol(), "The file name of this compilation unit has an incorret name. It should be " + filename);
			}

		}
		if ( symbol.token == Token.LEFTPAR )
			currentObject.setContextParameterArray(contextDec());


		Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tc = parseMetaobjectAnnotations_NonAttached_Attached();
		if ( tc != null ) {
			nonAttachedMetaobjectAnnotationList = tc.f1;
			attachedMetaobjectAnnotationList = tc.f2;
		}


		boolean hadExtendsMixinImplements = false;


		if ( symbol.token == Token.EXTENDS ) {
			if ( attachedMetaobjectAnnotationList != null ) {
				CyanMetaobject cyanMetaobject = attachedMetaobjectAnnotationList.get(0).getCyanMetaobject();
				this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
						"There is a metaobject annotation to metaobject '" +
								cyanMetaobject.getName() + "' that should be attached to a declaration " +
								"(prototype, method, instance variable, etc). However, no declaration follows the metaobject annotation", null, ErrorKind.metaobject_error);

			}
			next();
			Expr superPrototype;

			try {
				this.prohibitTypeof = true;
				superPrototype = type();
			}
			finally {
				this.prohibitTypeof = false;
			}


			currentObject.setSuperobjectExpr(superPrototype);
			this.currentProgramUnit.setCyanMetaobjectListBeforeExtendsMixinImplements(nonAttachedMetaobjectAnnotationList);
			hadExtendsMixinImplements = true;

			ObjectDec currentProto = (ObjectDec ) this.currentProgramUnit;
			if ( currentProto.getContextParameterArray() != null && currentProto.getContextParameterArray().size() > 0 ) {
				/*
				 * there are context parameters. Then there may be '(' in the super-prototype as in <br>
				 * {@code
				 *    object Worker(String name, Company company) extends Person(name) <br>
				 *        ...<br>
				 *    end<br>
				 * }
				 */
				if ( symbol.token == Token.LEFTPAR ) {
					next();
					ArrayList<ContextParameter> cpList = currentProto.getContextParameterArray();
					ArrayList<ContextParameter> superContextParameterList = new ArrayList<>();
					if ( symbol.token != Token.IDENT ) {
						error2(symbol, "A context parameter was expected." + foundSuch());
					}
					else {
						while ( symbol.token == Token.IDENT ) {
							Symbol idSym = symbol;
							String cpName = idSym.getSymbolString();
							next();
							boolean foundCP = false;
							for ( ContextParameter cp : cpList ) {
								if ( cpName.equals(cp.getName()) ) {
									foundCP = true;
									superContextParameterList.add(cp);
								}
							}
							if ( !foundCP ) {
								error2(idSym, "Identifier '" + cpName + "' should be one of the context parameter of this prototype");
							}
							if ( symbol.token == Token.COMMA ) {
								lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
								next();
								if ( symbol.token != Token.IDENT ) {
									error2(symbol, "A context parameter was expected." + foundSuch());
								}
							}
							else {
								break;
							}
						}
						currentProto.setSuperContextParameterList(superContextParameterList);
					}
					if ( symbol.token != Token.RIGHTPAR ) {
						this.error2(symbol,  "')' expected." + foundSuch());
					}
					else {
						if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
							error2(symbol, "letter, number, or '_' after ')'");
						}
						next();
					}
				}
			}

		}
		if ( symbol.token == Token.IMPLEMENTS ) {
			next();
			currentObject.setInterfaceList(typeList());
			hadExtendsMixinImplements = true;
			this.currentProgramUnit.setCyanMetaobjectListBeforeExtendsMixinImplements(nonAttachedMetaobjectAnnotationList);

		}

		if ( hadExtendsMixinImplements ) {
			attachedMetaobjectAnnotationList = null;
			nonAttachedMetaobjectAnnotationList = null;

			tc = parseMetaobjectAnnotations_NonAttached_Attached();
			if ( tc != null ) {
				nonAttachedMetaobjectAnnotationList = tc.f1;
				attachedMetaobjectAnnotationList = tc.f2;
			}
		}


		hasMade_dpa_actions = false;
		hasAdded_pp_new_Methods = false;
		/**
		 * true if the method being compiled was created by the compiler
		 */
		boolean compilerCreatedMethod = false;

		/*
		 * number of objects created from functions that have already been parsed
		 */
		int numObjForFunctionParsed = 0;

		hasAddedInnerPrototypes = false;


		/*
		 * true if hasSetBeforeInnerObjectNonAttachedMetaobjectAnnotationList of currentObjectDec has been set already
		 *
		*/

		boolean hasSetBeforeInnerObjectNonAttachedMetaobjectAnnotationList = false;


		while ( symbol.token == Token.PRIVATE ||
				symbol.token == Token.PUBLIC  ||
				symbol.token == Token.PROTECTED ||
				symbol.token == Token.METAOBJECT_ANNOTATION ||
				symbol.token == Token.FINAL   ||
				symbol.token == Token.SHARED ||
				symbol.token == Token.VAR    ||
				symbol.token == Token.FUNC    ||
				symbol.token == Token.OVERLOAD ||
				symbol.token == Token.OVERRIDE  ||
				symbol.token == Token.ABSTRACT ||
				symbol.token == Token.LET   ||
				symbol.token == Token.END ||
				symbol.token == Token.OBJECT ||
				startType(symbol.token) ) {

			if ( symbol.token == Token.OBJECT ) {
				if ( ! hasAddedInnerPrototypes )
				    error(true, symbol,
					    	"Keyword 'object' cannot be used here. Attempt to declare an inner prototype", symbol.getSymbolString(), ErrorKind.syntax_error_object);
				else {
						// found the declaration of an object inside the user-declared object. These
						// objects were introduced by the compiler.
					/**
					 * it is assumed that the first object created by this compiler correspond to
					 * a function (if any). It was created by method addObjectDecForFunctions().
					 */
					if ( ! hasSetBeforeInnerObjectNonAttachedMetaobjectAnnotationList ) {
						currentObject.setBeforeInnerObjectNonAttachedMetaobjectAnnotationList(nonAttachedMetaobjectAnnotationList);
						currentObject.setBeforeInnerObjectAttachedMetaobjectAnnotationList(attachedMetaobjectAnnotationList);

						hasSetBeforeInnerObjectNonAttachedMetaobjectAnnotationList = true;

						nonAttachedMetaobjectAnnotationList = null;
						attachedMetaobjectAnnotationList = null;

						tc = parseMetaobjectAnnotations_NonAttached_Attached();
						if ( tc != null ) {
							nonAttachedMetaobjectAnnotationList = tc.f1;
							attachedMetaobjectAnnotationList = tc.f2;
						}
					}

					ObjectDec innerObjectDec = objectDec( Token.PUBLIC,
							nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList,
							currentObject);
					if ( numObjForFunctionParsed < currentObject.getNextFunctionNumber() ) {
						currentObject.getFunctionList().get(numObjForFunctionParsed).setInnerObjectForThisFunction(innerObjectDec);
						++numObjForFunctionParsed;
					}

					this.currentProgramUnit = currentObject = this.objectDecStack.peek();

					nonAttachedMetaobjectAnnotationList = null;
					attachedMetaobjectAnnotationList = null;

					tc = parseMetaobjectAnnotations_NonAttached_Attached();
					if ( tc != null ) {
						nonAttachedMetaobjectAnnotationList = tc.f1;
						attachedMetaobjectAnnotationList = tc.f2;
					}
				}
			}
			else {
				Symbol firstSlotSymbol = symbol;

				if ( symbol.token != Token.END ) {
					try {
						slotDec(currentObject, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, firstSlotSymbol, compilerCreatedMethod );
					}
					catch ( CompileErrorException e ) {
						this.skipTo(Token.FUNC, Token.OVERLOAD, Token.PUBLIC, Token.END, Token.PRIVATE, Token.PROTECTED);
					}
					nonAttachedMetaobjectAnnotationList = null;
					attachedMetaobjectAnnotationList = null;

					tc = parseMetaobjectAnnotations_NonAttached_Attached();
					if ( tc != null ) {
						nonAttachedMetaobjectAnnotationList = tc.f1;
						attachedMetaobjectAnnotationList = tc.f2;
					}
				}


				if ( symbol.token == Token.END ) {


					if ( hasAdded_pp_new_Methods ) {

						if ( hasAddedInnerPrototypes )
							break;
						else {
							if ( currentProgramUnit instanceof ObjectDec && ! currentProgramUnit.isGeneric()
								  //&& ! currentProgramUnit.getName().equals("Nil")
									 ) {

								/*
								 * for each interface the compiler create a new prototype to represent the
								 * interface when it is used in expressions (the interface as an object).
								 * This new prototype is only created after the semantic analysis with
								 * dsa actions, which is phase 7 of the Figure of chapter metaobjects
								 * of the Cyan manual. Therefore code for methods prototype,
								 * defaultValue, inner prototypes, etc were NOT added to these new prototypes.
								 * They are added in just one phase of the compilation, which is phase 7,
								 * the first phase in which they appear. Then <code>compilationUnit.getIsInterfaceAsObject()</code>
								 * is only true in phase 7.
								 */

								if ( compInstSet.contains(CompilationInstruction.inner_addCode) ||
									 compilationUnit.getIsInterfaceAsObject() ) {

									if ( currentProgramUnit.getAttachedMetaobjectAnnotationList() != null ) {
										for ( CyanMetaobjectAnnotation annotation : currentProgramUnit.getAttachedMetaobjectAnnotationList() ) {
											CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
											if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
												ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
												Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
												if ( t != null )
													currentProgramUnit.addFeature(t);
											}
										}
									}




									/*
									 * if outerObject is null, then this is the user-created object and
									 * a prototype should be created for each method. This happen even for
									 * 'Function' prototypes and its sub-prototypes. When generating code
									 * these inner prototypes will be discarded. At this point we cannot
									 * know whether a prototype inherits from Function or not. Then we generate
									 * prototypes for every method.
									 *
									 * if outerObject is not null then this method call (of objectDec) was made to compile
									 * an inner prototype which was created by the compiler by addInnerObjectDec
									 * and addProtoForMethods. Since these methods create sub-prototypes of Function,
									 * their methods are not objecs. Therefore addInnerObjectDec and addProtoForMethods
									 * should not be called.
									 */
									if ( outerObject == null ) {
										/**
										 * add prototypes corresponding to anonymous functions
										 */


										StringBuffer s = new StringBuffer();
										s.append(" @" + NameServer.pushCompilationContextName + "(inner" + Compiler.contextNumber + ", inner) ");
										if ( ((ObjectDec) currentProgramUnit).getFunctionList().size() > 0 )
											addObjectDecForFunctions(s);

										/*
										 * add a method that return the features of this program unit and
										 * of the methods of it
										 */
										this.addMethodsForFeatures(s);


										/**
										 * add a inner prototype for each method
										 */
										// addObjectDecForMethods(s);
										s.append(" @" + NameServer.popCompilationContextName + "(inner" + Compiler.contextNumber + ") \n");
										++Compiler.contextNumber;
										insertTextInput(s, symbol.getOffset());
										insideCyanMetaobjectCompilationContextPushAnnotation = true;
										next();


									}
								}
							}
							hasAddedInnerPrototypes = true;
						}
						//hasAddedInnerPrototypes = true;
					}
					else {
						if ( currentProgramUnit instanceof ObjectDec && ! currentProgramUnit.isGeneric()
								&& ! currentProgramUnit.getName().equals("Nil")
								) {

							/**
							 * methods 'new', 'new:', 'prototype', 'prototypeName', etc should be added
							 * to inner prototypes. addCodeForInnerPrototypes is true if these
							 * methods should be added to the current prototype, which should start
							 * with NameServer.functionProtoName or NameServer.methodProtoName.
							 */
							boolean addCodeForInnerPrototypes = compInstSet.contains(CompilationInstruction.pp_new_inner_addCode) &&
									NameServer.isNameInnerPrototype(currentProgramUnit.getName());

							// see comment above on inner_addCode
							if ( compInstSet.contains(CompilationInstruction.pp_addCode)  ||
								 compilationUnit.getIsInterfaceAsObject() || addCodeForInnerPrototypes ) {

								StringBuffer s = new StringBuffer();
								s.append(" @" + NameServer.pushCompilationContextStatementName + "(pp" + Compiler.contextNumber + ", pp) \n");
								this.addMethodsEveryPrototypeHas(s);
								s.append(" @" + NameServer.popCompilationContextName + "(pp" + Compiler.contextNumber + ") \n");
								++Compiler.contextNumber;
								insertTextInput(s, symbol.getOffset());
								insideCyanMetaobjectCompilationContextPushAnnotation = true;
								next();

								   // all methods compiled from this point onward were created by the compiler
								compilerCreatedMethod = true;
							}

							// see comment above on inner_addCode
							if ( compInstSet.contains(CompilationInstruction.new_addCode)  ||
							     compilationUnit.getIsInterfaceAsObject() || addCodeForInnerPrototypes ) {

								StringBuffer s = new StringBuffer();
								s.append(" @" + NameServer.pushCompilationContextStatementName + "(new" + Compiler.contextNumber + ", new) ");
								addMethodsToPrototypeIfNotDefined(s);
								s.append(" @" + NameServer.popCompilationContextName + "(new" + Compiler.contextNumber + ") \n");
								++Compiler.contextNumber;
								insertTextInput(s, symbol.getOffset());
								insideCyanMetaobjectCompilationContextPushAnnotation = true;
								next();

								   // all methods compiled from this point onward were created by the compiler
								compilerCreatedMethod = true;
							}

						}
						hasAdded_pp_new_Methods = true;
					}

					tc = parseMetaobjectAnnotations_NonAttached_Attached();
					if ( tc != null ) {
						if ( tc.f1 != null ) {
							if ( nonAttachedMetaobjectAnnotationList == null )
								nonAttachedMetaobjectAnnotationList = tc.f1;
							else
								nonAttachedMetaobjectAnnotationList.addAll(tc.f1);
						}
						if ( tc.f2 != null ) {
							if ( attachedMetaobjectAnnotationList == null )
								attachedMetaobjectAnnotationList = tc.f2;
							else
								attachedMetaobjectAnnotationList.addAll(tc.f2);
						}
					}

				}
			}

		}
		if ( symbol.token != Token.END ) {
			if ( currentProgramUnit instanceof ObjectDec ) {
				ObjectDec objDec = (ObjectDec ) this.currentProgramUnit;
				ArrayList<InstanceVariableDec> ivList = objDec.getInstanceVariableList();
				if ( ivList.size() > 0 ) {
					Expr expr = ivList.get(ivList.size()-1).getExpr();
					if ( expr instanceof ExprMessageSendUnaryChainToExpr ) {

					    ExprMessageSendUnaryChainToExpr unary = (ExprMessageSendUnaryChainToExpr ) expr;
					    if ( unary.getReceiver() instanceof ExprIdentStar && ((ExprIdentStar ) unary.getReceiver()).getName().equals("new") ) {
					    	if ( Character.isUpperCase(unary.getUnarySymbol().getSymbolString().charAt(0) ) ) {
				    			this.error2(unary.getFirstSymbol(), "It seems you are trying to create an object of " + unary.getUnarySymbol().getSymbolString()
				    					+ " . Since 'new' is a method name, put it after '" +
				    					unary.getUnarySymbol().getSymbolString() + "', not before",
									true);

					    	}
					    	else {
					    		if ( symbol.token == Token.PERIOD || symbol.token == Token.LEFTPAR ) {
					    			this.error2(unary.getFirstSymbol(), "It seems you are trying to create an object using 'new'."
					    					+ " However, 'new' is a method and therefore it should appear after the object name",
										true);

					    		}
					    	}
					    }
					    /*if ( unary.getr)
					    receiverOrExpr new
					    unarySymbol something that starts with upper case */
					}
				}
			}
			error2(symbol, "keyword 'end' expected." + foundSuch());
		}
		this.currentProgramUnit.setEndSymbol(symbol);

		/*
		if ( symbol.getColumnNumber() != this.currentProgramUnit.getFirstSymbol().getColumnNumber() ) {
			error2(symbol, "'end' should be in the same column as 'object'");
		}
		*/

		if ( attachedMetaobjectAnnotationList != null ) {
			CyanMetaobject cyanMetaobject = attachedMetaobjectAnnotationList.get(0).getCyanMetaobject();
			this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
					"There is a metaobject annotation to metaobject '" +
							cyanMetaobject.getName() + "' that should be attached to a declaration " +
							"(prototype, method, instance variable, etc). However, no declaration follows the metaobject annotation", null, ErrorKind.metaobject_error);

		}
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			this.currentProgramUnit.setBeforeEndNonAttachedMetaobjectAnnotationList(nonAttachedMetaobjectAnnotationList);
		}

		next();

		if ( outerObject != null ) {
			outerObject.addInnerPrototype(currentObject);
		}

		objectDecStack.pop();
		return currentObject;
	}

	/**
	 * add methods to the end of the text of an object. These methods return the list of features
	 * of the prototype and the list of features of each method and instance variable.
	   @param s
	 */
	private void addMethodsForFeatures(StringBuffer s) {
		s.append("\n");
		String puName = this.currentProgramUnit.getName();
		if ( puName.equals("Nil") )
			return;
		boolean isAny = puName.equals("Any");
		if ( ! isAny ) {
			s.append("    override\n");
		}
		ArrayList<String> annotList = null;
		s.append("    func getFeatureListNameDoesNotCollide__ -> Array<Tuple<key, String, value, Any>> {\n");
		if ( currentProgramUnit.getFeatureList() != null && currentProgramUnit.getFeatureList().size() > 0 ) {
			// s.append("featureList_name_does_not_collide__;\n");
			s.append("    let featureList_name_does_not_collide__ = Array<Tuple<key, String, value, Any>> new;\n");
			//s.append("        [ ");
			// int size = currentProgramUnit.getFeatureList().size();
			for ( Tuple2<String, ExprAnyLiteral> t : this.currentProgramUnit.getFeatureList() ) {
				s.append("        featureList_name_does_not_collide__ add: ");

				String strValue;
				strValue = t.f2.metaobjectParameterAsString(
						() -> { this.error2(t.f2.getFirstSymbol(), "This expression cannot be used in a parameter in a metaobject annotation"); }
						); // Lexer.valueToFeatureString(t);

				String key = NameServer.removeQuotes(t.f1);
				s.append("[. key = \"" + key + "\", value = Any toAny: " + strValue + " .];\n");
				if ( key.equals("annot") ) {
					if ( annotList == null ) {
						annotList = new ArrayList<>();
					}
					annotList.add(strValue);
				}
				/* if ( --size > 0 )
					s.append(", ");
				s.append("\n"); */
			}
			// s.append("        ];\n");
			s.append("        ^featureList_name_does_not_collide__;\n");
		}
		else {
			s.append("        ^Array<Tuple<key, String, value, Any>> new;\n");
		}
		s.append("    }\n");

		if ( ! isAny ) {
			s.append("    override\n");
		}
		s.append("    func getAnnotListNameDoesNotCollide__ -> Array<Any> {\n");

		if ( annotList != null ) {
			s.append("    let annotList_name_does_not_collide__ = Array<Any> new;\n");
			for ( String value : annotList ) {
				s.append("        annotList_name_does_not_collide__ add: " + value + ";\n");
			}
			s.append("        ^annotList_name_does_not_collide__;\n");
		}
		else {
			s.append("        ^Array<Any> new;\n");
		}
		s.append("    }\n");

		s.append("\n");
		if ( ! isAny ) {
			s.append("    override\n");
		}
		s.append("    func getSlotFeatureListNameDoesNotCollide__ -> Array<Tuple<slotName, String, key, String, value, Any>> {\n");
		// s.append("slotFeatureList_name_does_not_collide__;\n");


		s.append("        let slotFeatureList_name_does_not_collide__ = Array<Tuple<slotName, String, key, String, value, Any>> new;\n");

		/* int sizeS = s.length();
		s.append("        [ \n"); */
		//s.append("          ");

		//s.append("[. slotName = \"\", key = \"\", value = Any cast: \"\" .]");
		if ( this.currentProgramUnit instanceof ObjectDec ) {
			ObjectDec currentObj = (ObjectDec ) this.currentProgramUnit;
			for ( InstanceVariableDec iv : currentObj.getInstanceVariableList() ) {
				String ivName = iv.getName();
				if ( iv.getFeatureList() != null ) {
					for ( Tuple2<String, ExprAnyLiteral> t : iv.getFeatureList() ) {
						// s.append(",\n          ");
						s.append("        slotFeatureList_name_does_not_collide__ add: ");
						String strValue;
						//strValue = Lexer.valueToFeatureString(t);
						strValue = t.f2.metaobjectParameterAsString(
								() -> { this.error2(t.f2.getFirstSymbol(), "This expression cannot be used in a parameter in a metaobject annotation"); }
								);
						s.append("[. slotName = \"" +  ivName + "\", key = \"" +
						    NameServer.removeQuotes(t.f1) + "\", value = Any toAny: " + strValue + " .];\n");
					}
				}
			}
			for ( MethodDec method : currentObj.getMethodDecList() ) {
				String methodName = method.getSignatureWithoutReturnType();
				if ( method.getFeatureList() != null ) {
					for ( Tuple2<String, ExprAnyLiteral> t : method.getFeatureList() ) {
						//s.append(",\n          ");
						s.append("        slotFeatureList_name_does_not_collide__ add: ");
						String strValue;
						//strValue = Lexer.valueToFeatureString(t);
						strValue = t.f2.metaobjectParameterAsString(
								() -> { this.error2(t.f2.getFirstSymbol(), "This expression cannot be used in a parameter in a metaobject annotation"); }
								);

						s.append("[. slotName = \"" +  methodName + "\", key = \"" +
						    NameServer.removeQuotes(t.f1) + "\", value = Any toAny: " + strValue + " .];\n");
					}
				}
			}
		}
		else {
			// interface
			InterfaceDec interDec = (InterfaceDec ) this.currentProgramUnit;
			for ( MethodSignature ms : interDec.getMethodSignatureList() ) {
				String signature = ms.getName();
				if ( ms.getFeatureList() != null ) {
					for ( Tuple2<String, ExprAnyLiteral> t : ms.getFeatureList() ) {
						//s.append(",\n          ");
						s.append("        slotFeatureList_name_does_not_collide__ add: ");
						String strValue;
						// strValue = Lexer.valueToFeatureString(t);
						strValue = t.f2.metaobjectParameterAsString(
								() -> { this.error2(t.f2.getFirstSymbol(), "This expression cannot be used in a parameter in a metaobject annotation"); }
								);

						s.append("[. slotName = \"" +  signature + "\", key = \"" +
						    NameServer.removeQuotes(t.f1) + "\", value = Any toAny: " + strValue + " .];\n");
					}
				}
			}
		}
		s.append("        ^slotFeatureList_name_does_not_collide__;\n");
		s.append("    }\n");

		/*
		if ( numFeatures > 0 ) {
			s.delete(s.length()-2, s.length()-1);
			s.append("        ];\n");
		}
		else {
			s.delete(sizeS,  s.length());
			s.append(" Array<Tuple<slotName, String, key, String, value, Any>>();\n");
		}
		*/

	}

	/*
	 * check if the real parameters and formal parameters of a program unit are correct.
	 * Formal and real parameters cannot be mixed as in
	 *      object Bad<Int><T> ... end
	 *
	 *  Return true if the parameter is a generic prototype (with formal parameters),
	 *  false otherwise.
	 */
	private boolean checkFormalParameterListGenericPrototype(
			ProgramUnit currentProgramUnit2) {
		/*
		 * use real parameters such as in
		 *     object Stack<main.Person> ... end
		 *     object Inter<add> ... end
		 *     object Stack<Int> ... end
		 */
		int useRealParameters = 0;
		/*
		 * use formal parameter such as in
		 *     object Stack<T> ... end
		 */
		int useFormalParameterWITHOUT_Plus = 0;
		/*
		 * use formal parameter followed by + such as in
		 *     object Union<T+> ... end
		 */
		int useFormalParameterWithPlus = 0;
		for ( ArrayList<GenericParameter> genericParameterList : currentProgramUnit2.getGenericParameterListList() ) {
			for ( GenericParameter gp : genericParameterList ) {
				if ( gp.getKind() == GenericParameterKind.FormalParameter ) {
					if ( gp.getPlus() ) {
						useFormalParameterWithPlus = 1;
						if ( genericParameterList.size() != 1 ) {
							error(  true, gp.getParameter().getFirstSymbol(),
									"A formal parameter with '+' should appear alone between '<' and '>' in a generic prototype. There is another parameter with " + gp.getName() + "+ between '<' and '>'",
									gp.getParameter().asString(), ErrorKind.more_than_one_formal_plus_parameter_in_generic_prototype);
						}
					}
					else
						useFormalParameterWITHOUT_Plus = 1;
				}
				else if ( gp.isRealPrototype() )
					useRealParameters = 1;
				else
					error2(gp.getParameter().getFirstSymbol(), "Internal error in Compiler::objectDec");
			}

		}

		if ( useRealParameters + useFormalParameterWITHOUT_Plus + useFormalParameterWithPlus > 1 )
			error(true, currentProgramUnit2.getSymbol(),
					"Different kinds of parameters (real parameters, formal parameters, formal parameters with +) cannot apper together in the declaration of generic prototype", currentProgramUnit2.getSymbol().getSymbolString(), ErrorKind.mixing_of_different_parameter_kinds_in_generic_prototype);

		if ( useFormalParameterWithPlus == 1 ) {
			if ( currentProgramUnit2.getGenericParameterListList().size() != 1 ||
					currentProgramUnit2.getGenericParameterListList().get(0).size() != 1	)
				error(true,
						currentProgramUnit2.getSymbol(),
						"A generic prototype with varying number of parameters should have just one set of pairs '<' and '>' and just one parameter", currentProgramUnit2.getSymbol().getSymbolString(), ErrorKind.mixing_of_different_parameter_kinds_in_generic_prototype);
		}


		return useFormalParameterWITHOUT_Plus + useFormalParameterWithPlus > 0;
	}



	/**
	 * analyzes a list of metaobject annotations. It returns tuple (L1, L2) in which L1 is a list
	 * of metaobject annotations that should not be attached to any declaration and
	 * L2 is a list of metaobject annotations that should be attached to something.
	 * If the order is not Non-Attached followed by Attached, an error is issued.
	 *
	 * We discover if a metaobject should be attached to a declaration or statement
	 * by calling method  attachedToSomething
	   @return
	 */
	private Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>>
	    parseMetaobjectAnnotations_NonAttached_Attached() {

		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList = null;
		ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList = null;


		CyanMetaobjectWithAtAnnotation annotation = null;
		int step = 0; // getting non-attached first
		while ( symbol.token == Token.METAOBJECT_ANNOTATION ) {

			Symbol ctmoSymbol = symbol;
			try {
				annotation = annotation(false);
			}
			catch ( error.CompileErrorException e ) {
			}
			catch ( RuntimeException e ) {
				this.error2(ctmoSymbol, "Runtime exception in metaobject annotation");
			}

			if ( annotation == null ) {
				return new Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>>(nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList);
			}

			switch ( step ) {
			case 0:
				if ( annotation.getCyanMetaobject().attachedToSomething() ) {
					step = 1;
					if ( attachedMetaobjectAnnotationList == null )
						attachedMetaobjectAnnotationList = new ArrayList<>();
					attachedMetaobjectAnnotationList.add(annotation);
			    }
				else {
					if ( nonAttachedMetaobjectAnnotationList == null )
						nonAttachedMetaobjectAnnotationList = new ArrayList<>();
					nonAttachedMetaobjectAnnotationList.add(annotation);
				}
				break;
			case 1:
				if ( annotation.getCyanMetaobject().attachedToSomething() ) {
					if ( attachedMetaobjectAnnotationList == null )
						attachedMetaobjectAnnotationList = new ArrayList<>();
					attachedMetaobjectAnnotationList.add(annotation);
				}
				else {
					/*
					 * found something like
					 *      @feature("author", "José")
					 *      @prototypeCallOnly
					 *      @javacode<<*  static int num = 0  *>>
					 *      func myMethod -> Boolean [ ... ]
					 *  This is illegal because javacode is not linked to any declaration.
					 *  However, the code
					 *      @javacode<<*  static int num = 0  *>>
					 *      @feature("author", "José")
					 *      @prototypeCallOnly
					 *      func myMethod -> Boolean [ ... ]
					 *
					 *  is legal and javacode would not be linked to myMethod
					 *      */
					String name = annotation.getCyanMetaobject().getName();
					this.error2(symbol, "metaobject annotation to " + name +
							" follows some other metaobject annotations that should be attached" +
							" to a declaration (variable, method, prototype). However, metaobject " + name
							+ " should not be attached to a declaration");

				}
			}


		}

		return new Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>>(nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList);
	}




	/**
	 * add one inner prototype for each method declared in this prototype
	 */
	private void addObjectDecForMethods(StringBuffer s) {
		ObjectDec currentPrototype = (ObjectDec) currentProgramUnit;
		CyanEnv cyanEnv = new CyanEnv(false, false);

		cyanEnv.atBeginningOfProgramUnit(currentProgramUnit);

		// String currentPrototypeName = currentPrototype.getName();

		// StringBuffer s = new StringBuffer();
		s.append("\n");
		for ( MethodDec method : currentPrototype.getMethodDecList() ) {
			method.genProtoForMethod(s, cyanEnv);
		}
		s.append("\n\n");
		cyanEnv.atEndOfCurrentProgramUnit();
		/*
		 * the lines below add string 's' to the text of the current compilation unit. The
		 * string is added just before 'symbol'.
		 */



	}

	private void insertPhaseChange(CompilerPhase phase, CyanMetaobjectAnnotation annotation) {

		StringBuffer s = new StringBuffer("#" + phase.getName() + "\0");
		char[] input = s.toString().toCharArray();
		int inputSize = input.length;
		Symbol ctmoSymbol = annotation.getFirstSymbol();
		  // the "+1" accounts for the '@' before the metaobject name
		compilationUnit.addTextToCompilationUnit(input, inputSize, ctmoSymbol.getOffset() +
				ctmoSymbol.getSymbolString().length() + 1);
		lexer.setInput(compilationUnit.getText());
		/*
		 * the index should be shift by #phaseName which is s.length()
		 */
		lexer.shiftInputIndex(s.length() - 1);
	}


	/**
	 * add text <code>s</code> in the text of this compilation unit at <code>offset</code>.
	 * If allowEndOfLine is true, the text to be inserted may have '\n'. If allowEndOfLine
	 * is false and {@code s} has a '\n' in it then this method return false.
	   @param s
	 */

	private boolean insertTextInput(StringBuffer s, int offset) {
		s.append("\0");
		char[] input = s.toString().toCharArray();
		int inputSize = input.length;

		compilationUnit.addTextToCompilationUnit(input, inputSize, offset);
		lexer.setInput(compilationUnit.getText());
		/*
		 * the index of the next symbol to be scanned by lexer should be decreased by
		 * the size of the last symbol found
		 */
		//lexer.shiftInputIndex(-symbol.getSymbolString().length());
		lexer.setInputIndex(offset);


		this.lineShift += Env.numLinesOf(s);

		return true;
	}

	/**
	 * add inner prototypes to the current prototype. Each anonymous function that appears in any method
	 * of the current prototype gives origin to a inner prototype.
	 */
	private void addObjectDecForFunctions(StringBuffer s) {


		ObjectDec currentPrototype = (ObjectDec) currentProgramUnit;

		String currentPrototypeName = currentPrototype.getName();

		s.append("\n");
		s.append("        // a prototype for each of the anonymous functions of prototype " + currentPrototypeName + " \n\n");

		CyanEnv cyanEnv = new CyanEnv(true, false);
		cyanEnv.atBeginningOfProgramUnit(currentProgramUnit);

		for ( ExprFunction function : currentPrototype.getFunctionList() ) {
			s.append(function.genContextObjectForFunction(cyanEnv));
		}
		s.append("\n\n");
		cyanEnv.atEndOfCurrentProgramUnit();

	}

	/**
	 * add to parameter <code>s</code> methods that every prototype should have and that the user cannot define.
	 * For a prototype <code>Proto</code>, these methods are:
	 * </p>
	 * <code>
	 * override
	 * func prototype -> Proto</p>
	 * </code>
	   @param s
	 */
	private void addMethodsEveryPrototypeHas(StringBuffer s) {

			ObjectDec currentPrototype = (ObjectDec) currentProgramUnit;
			String currentPrototypeName = currentPrototype.getName();
			String currentPrototypeTypeName;

			ArrayList<MethodSignature> methodSignatureList;

			/**
			 * For each interface "Inter" the compiler creates a prototype
			 * "Proto_Inter" that is used whenever the interface appears,
			 * in the Cyan code, as an expression. Compilation units
			 * that has these compiler-created prototypes return true
			 * in method getIsPrototypeInterface(). All methods added
			 * by the compiler  that use the prototype name should use
			 * "Inter" in prototype "Proto_Inter".
			 * Then, for example, method
             *      override
			 *      func prototype -> P
			 * is added to prototype P. But in the compiler-created
			 * prototype "Proto_Inter", this method is
			 *      override
			 *      func prototype -> Inter
			 *
			 *
			 *  In regular prototypes, currentPrototypeTypeName is equal to
			 *  the prototype name. In a compiler-created prototype "Proto_Inter"
			 *  created because of an interface "Inter", currentPrototypeTypeName
			 *  is equal to the interface name, "Inter".
			 *
			 */
			if ( NameServer.isPrototypeFromInterface(currentPrototype.getName()) ) {

			//if ( compilationUnit.getIsPrototypeInterface() ) {
				// was created from an interface by the compiler. Use the interface
				// name as parameter
				currentPrototypeTypeName = NameServer.interfaceNameFromPrototypeName(currentPrototypeName);
			}
			else
				currentPrototypeTypeName = currentPrototypeName;

			StringBuffer nameWithSpaces = new StringBuffer();
			for (int ii = 0; ii < currentPrototypeTypeName.length(); ++ii) {
				char ch = currentPrototypeTypeName.charAt(ii);
				nameWithSpaces.append(ch);
				if ( ch == ',' )
					nameWithSpaces.append(' ');
			}
			currentPrototypeTypeName = nameWithSpaces.toString();
			// S ystem.out.p rintln("addMethods: " + currentPrototypeName);

			s.append("\n");
			s.append("    // Methods added by the compiler\n");



			// prototype cannot be user-defined
			/**
			 * override
			 * public func prototype -> Proto { return Proto; }
			 *
			*/

			methodSignatureList = currentPrototype
					.searchMethodPrivateProtectedPublic("prototype");
			if ( methodSignatureList.size() == 0 ) {
				s.append("    override");
				s.append("    func prototype -> " + Lexer.addSpaceAfterComma(currentPrototypeTypeName) + " {\n");
				s.append("        @javacode{* return ");
				if ( this.currentProgramUnit.getOuterObject() == null )
					s.append("prototype");
				else
					s.append("prototype" + currentPrototypeName);
				s.append(";\n");
			    s.append("        *}\n");
				s.append("    } \n");
			}
			else if ( currentPrototypeName.compareTo("Any") != 0 )
				error(true,
						currentPrototype.getSymbol(),
						"Method 'prototype' cannot be user-defined", "prototype", ErrorKind.method_cannot_be_user_defined);

	}


	/**
	 * add to parameter <code>s</code> methods that every prototype should have and that were not defined in
	 * the source code. For every <code>init</code> and <code>init:</code> method the compiler adds a <code>new</code>
	 * and <code>new:</code> method. If the source code does not define method
	 * <code>clone</code> {@link addMethodsToPrototype} adds this method to the
	 * current prototype.
	   @param s
	 */
	private void addMethodsToPrototypeIfNotDefined(StringBuffer s ) {

		ObjectDec currentPrototype = (ObjectDec) currentProgramUnit;


		String currentPrototypeName = currentPrototype.getName();
		String currentPrototypeTypeName;
		String currentPrototypeJavaName = NameServer.getJavaName(currentPrototypeName);

		ArrayList<MethodSignature> methodSignatureList;

		/**
		 * For each interface "Inter" the compiler creates a prototype
		 * "Proto_Inter" that is used whenever the interface appears,
		 * in the Cyan code, as an expression. Compilation units
		 * that has these compiler-created prototypes return true
		 * in method getIsPrototypeInterface(). All methods added
		 * by the compiler  that use the prototype name should use
		 * "Inter" in prototype "Proto_Inter".
		 * Then, for example, method
		 *      override
		 *      func prototype -> P
		 * is added to prototype P. But in the compiler-created
		 * prototype "Proto_Inter", this method is
		 *      override
		 *      func prototype -> Inter
		 *
		 *
		 *  In regular prototypes, currentPrototypeTypeName is equal to
		 *  the prototype name. In a compiler-created prototype "Proto_Inter"
		 *  created because of an interface "Inter", currentPrototypeTypeName
		 *  is equal to the interface name, "Inter".
		 *
		 */
		if ( NameServer.isPrototypeFromInterface(currentPrototype.getName()) ) {
		//if ( compilationUnit.getIsPrototypeInterface() ) {
			// was created from an interface by the compiler. Use the interface
			// name as parameter
			currentPrototypeTypeName = NameServer.interfaceNameFromPrototypeName(currentPrototypeName);
		}
		else
			currentPrototypeTypeName = currentPrototypeName;

		// S ystem.out.p rintln("addMethods: " + currentPrototypeName);

		s.append("\n");
		s.append("    // Methods added by the compiler\n");


		// *******************************************************************************
		// the compiler adds the following methods if they have not already been
		// defined in
		// the prototype


		// add clone method
		methodSignatureList = currentPrototype
				.searchMethodPrivateProtectedPublic("clone");
		if ( methodSignatureList.size() == 0 ) {
			// add clone method
			/**
			 * func clone -> Proto { ... }
			 */
			s.append("\n    override");
			s.append("    func clone -> " + Lexer.addSpaceAfterComma(currentPrototypeTypeName) + " {\n");


			s.append("        @javacode<<*\n");
			s.append("        try {\n");
			s.append("            return (" + currentPrototypeJavaName + " ) ");
			/* s.append("prototype");
			if ( this.currentProgramUnit.getOuterObject() != null )
				s.append(this.currentProgramUnit.getName());  */
		    s.append("this.clone(); \n");
			s.append("        } catch (CloneNotSupportedException e) { }\n");
			s.append("        return null;\n");
			s.append("        *>>");
			s.append("    } \n");
		}
		else if ( methodSignatureList.size() == 1 ) {
			// check whether the return type of close is equal to the current
			// prototype.
			// A clone should have the signature "close -> T" in which T is the
			// current prototype
			if ( methodSignatureList.get(0).getReturnTypeExpr()
					.ifPrototypeReturnsItsName()
					.compareTo(currentPrototypeName) != 0 &&
					methodSignatureList.get(0).getReturnTypeExpr()
					.ifPrototypeReturnsItsName()
					.compareTo(this.compilationUnit.getPackageName() + "." + currentPrototypeName) != 0
				)
				this.error2(currentPrototype.getSymbol(),
						"Method 'clone' has a wrong signature. It should be 'clone -> " + currentPrototypeName + "'");
		}
		else if ( methodSignatureList.size() > 1 )
			error2(currentPrototype.getSymbol(),
					"Two or more 'clone' methods. There should be only one");



		/*
		// add cast: method
		methodSignatureList = currentPrototype
				.searchMethodPrivateProtectedPublic("cast:1");
		if ( methodSignatureList.size() == 0 ) {
			// add method cast:
			/**
			 * func cast: Any other -> Proto { ... }
			 * /
			s.append("\n");
			s.append("    override\n");
			s.append("    func cast: Any other -> " + Lexer.addSpaceAfterComma(currentPrototypeName) + " {\n");

			s.append("        @javacode{*\n");
			s.append("        if ( _other instanceof " + currentPrototypeJavaName + " ) { return (" + currentPrototypeJavaName + " ) _other; }\n");

			s.append("        else {\n");
			s.append("               Object ue = _other.getUnionElem();\n");
			s.append("               if ( ue != null && ue instanceof _Any ) return _cast_1( (_Any ) ue);\n");
			s.append("        }\n");
			s.append("        *}\n");

			s.append("        throw: ExceptionCast(\"Cannot cast \" ++ (other prototypeName) ++ \" to '" + currentPrototypeName + "'\");\n");
			s.append("    }\n\n");
		}
		else {
			for ( MethodSignature ms : methodSignatureList ) {
				if ( ms.getMethod().getVisibility() != Token.PUBLIC )
					error(true, ms.getFirstSymbol(), "'cast:' method should be public",
							ms.getNameWithoutParamNumber(), ErrorKind.non_public_cast_method);
				if ( ms.getMethod().isAbstract() )
					error(true, ms.getFirstSymbol(), "method 'cast:' cannot be abstract",
							ms.getNameWithoutParamNumber(), ErrorKind.abstract_cast_method);
				if ( ms.getMethod().getIsFinal() )
					error(true, ms.getFirstSymbol(), "method 'cast:' cannot be final",
							ms.getNameWithoutParamNumber(), ErrorKind.final_cast_method);
			}
		}
		*/

		// add 'new' method
		methodSignatureList = currentPrototype.searchInitNewMethod("init");
		boolean hasAtLeastOneInitMethod = methodSignatureList.size() > 0;
		if ( methodSignatureList.size() == 1 ) {
			MethodSignature initSignature = methodSignatureList.get(0);

			//MethodDec initMethod = initSignature.getMethod();


			Expr returnTypeExpr = initSignature.getReturnTypeExpr();
			// the return value should be null or Void
			if ( returnTypeExpr != null
					&& returnTypeExpr.ifPrototypeReturnsItsName().compareTo(
							"Nil") != 0 &&
					returnTypeExpr.ifPrototypeReturnsItsName()
							.compareTo(this.compilationUnit.getPackageName() + "." + currentPrototypeName) != 0
					) {
				error(true,
						initSignature.getFirstSymbol(), "Methods 'init' and 'init:' should have 'Nil' as return value or no return value type",
						"init", ErrorKind.init_should_return_Nil);
			}

			methodSignatureList = currentPrototype
					.searchMethodPrivateProtectedPublic("new");
			if ( methodSignatureList.size() != 0 )
				error2(initSignature.getFirstSymbol(),
						"You cannot declare both methods 'init' and 'new' in the same prototype");
			// add new method
			s.append("\n    func new -> " + Lexer.addSpaceAfterComma(currentPrototypeTypeName) + " {\n");

			s.append("         @javacode<**< \n");
			s.append( "            return new ");
			s.append(currentPrototypeJavaName + "();");
			s.append("         >**>\n");

			s.append("    }\n");

		}
		else if ( methodSignatureList.size() > 1 )
			error(true,
					currentPrototype.getSymbol(), "There is a 'init' method declared in line " +
			          methodSignatureList.get(0).getFirstSymbol().getLineNumber() + " and at least another declared in line " +
			          methodSignatureList.get(1).getFirstSymbol().getLineNumber(),
					"init", ErrorKind.two_or_more_init_methods);

		// add 'new:' methods
		methodSignatureList = currentPrototype.searchInitNewMethod("init:");
		hasAtLeastOneInitMethod = hasAtLeastOneInitMethod || methodSignatureList.size() > 0;
		int initMethodNumber = 0;
		if ( methodSignatureList.size() > 0 )  {
			initMethodNumber = methodSignatureList.get(0).getMethod().getMethodNumber();

		}

		if ( currentPrototype.getContextParameterArray() != null ) {
			/*
			 * Is there any init method with the parameters types given in the prototype head? For
			 * example, in
			 *        object Sum(Int s) ... end
			 * We check if there is an init method with an "Int" as parameter.
			 */
			/*
			 * ArrayList<ContextParameter> cpArray = currentPrototype.getContextParameterArray();
			boolean foundInit = true;
			for (MethodSignature methodSignature : methodSignatureList) {
				int indexcp = 0;
				MethodSignatureWithSelectors ms = (MethodSignatureWithSelectors ) methodSignature;
				if ( ms.getSelectorArray().get(0).getParameterList().size() != cpArray.size() )
					foundInit = false;
				else {
					for ( ParameterDec paramDec : ms.getSelectorArray().get(0).getParameterList() ) {
						if ( indexcp >= cpArray.size() )
							break;
						ContextParameter cp = cpArray.get(indexcp);
						if ( ! paramDec.getTypeInDec().ifPrototypeReturnsItsName().equals(
								cp.getTypeInDec().ifPrototypeReturnsItsName()) ) {
							foundInit = false;
						}
					}
				}
				if ( foundInit ) {
					error(true, null,
							"Prototype with an 'init:' method with the same parameter types as those in the prototype head", "", ErrorKind.two_or_more_init_methods);
				}
			}
			*/

			// add 'init:' method
			s.append("\n");
			s.append("    func init: ");
			StringBuffer initBody = new StringBuffer();
			initBody.append("        @javacode{*\n");

			int sizecp = currentPrototype.getContextParameterArray().size();
			for ( ContextParameter cp : currentPrototype.getContextParameterArray() ) {
				s.append( cp.getTypeInDec().ifPrototypeReturnsItsName() + " " );
				if ( cp.getVariableKind() != VariableKind.COPY_VAR )
					s.append(cp.getVariableKind().toString());
				String ivName;
				if ( cp.getVisibility() == Token.PUBLIC )
					ivName = "_" + cp.getName();
				else
					ivName = cp.getName();
				s.append(ivName);
				String ivJavaName = NameServer.getJavaName(ivName);
				initBody.append("        this." + ivJavaName + " = " + ivJavaName + ";\n");
				if ( --sizecp > 0 )
					s.append(", ");
			}
			s.append(" {\n");
			if ( currentPrototype.getSuperContextParameterList() != null && currentPrototype.getSuperContextParameterList().size() > 0 ) {
				/*
				 * super init: aa, bb;
				 */
				s.append("    super init: ");
				int sizescpl = currentPrototype.getSuperContextParameterList().size();
				for (ContextParameter cp : currentPrototype.getSuperContextParameterList() ) {
					s.append(cp.getName());
					if ( --sizescpl > 0 )
						s.append(", ");
				}
				s.append(";\n");
			}
			initBody.append("        *}\n");
			s.append(initBody);
			s.append("    }\n");


			// add 'new' method
			s.append("\n");
			s.append("    func new: ");

			sizecp = currentPrototype.getContextParameterArray().size();
			for ( ContextParameter cp : currentPrototype.getContextParameterArray() ) {
				s.append( Lexer.addSpaceAfterComma(cp.getTypeInDec().ifPrototypeReturnsItsName()) + " ");
				if ( cp.getVariableKind() != VariableKind.COPY_VAR )
					s.append( cp.getVariableKind().toString() );
				s.append(cp.getName());
				if ( --sizecp > 0 ) s.append(", ");
			}
			s.append(" -> " + Lexer.addSpaceAfterComma(currentPrototypeTypeName) );
			s.append(" {\n");

			s.append("        @javacode<**< \n");
			s.append( "            return new ");
			s.append(currentPrototypeJavaName + "(");


			int sizecpa = currentPrototype.getContextParameterArray().size();
			for ( ContextParameter cp : currentPrototype.getContextParameterArray() ) {
				String ivJavaName = NameServer.getJavaName(cp.getName());
				s.append( ivJavaName);
				if ( --sizecpa > 0) {
					s.append(", ");
				}
			}
			s.append(");\n");
			s.append("        >**>\n");
			s.append("    }\n");



		}

		if ( ! currentPrototype.getIsAbstract() ) {
			for (MethodSignature methodSignature : methodSignatureList) {
				/*
				 * for each 'init:' method, create a 'new:' method
				 */

				if ( initMethodNumber != methodSignature.getMethod().getMethodNumber() )
					error(true,
							methodSignature.getFirstSymbol(), "Method of line " + methodSignature.getFirstSymbol().getLineNumber() +
							" should be declared right after the previous method 'init:'",
							"init:", ErrorKind.method_should_be_declared_after_previous_method_with_the_same_selectors);
				++initMethodNumber;

				MethodSignatureWithSelectors initSignature = (MethodSignatureWithSelectors) methodSignature;

				if ( initSignature.getMethod().getHasOverride() )
					error(true,
						initSignature.getFirstSymbol(), "Method 'init:' cannot be declared with 'override'",
						"init", ErrorKind.init_should_not_be_declared_with_override);


				Expr returnTypeExpr = initSignature.getReturnTypeExpr();
				// the return value should be null or Void
				if ( returnTypeExpr != null && returnTypeExpr.ifPrototypeReturnsItsName().compareTo("Nil") != 0
						&& returnTypeExpr.ifPrototypeReturnsItsName().compareTo("cyan.lang.Nil") != 0
						)
					error(true,
							initSignature.getFirstSymbol(), "Methods 'init' and 'init:' should have 'Nil' as return value or no return value type",
							"init", ErrorKind.init_should_return_Nil);

								// add new method
				s.append("\n");
				s.append("    func new: ");

				ArrayList<SelectorWithParameters> selectorArray = initSignature
						.getSelectorArray();
				SelectorWithParameters initSelector = selectorArray.get(0);
				ArrayList<ParameterDec> parameterList = initSelector
						.getParameterList();
				int size = parameterList.size();
				if ( size > 0 ) s.append("( ");
				int indexParam = 0;
				for (ParameterDec p : parameterList) {
					String typeParam;
					if ( p.getTypeInDec() == null )
						typeParam = "Dyn";
					else
						typeParam = Lexer.addSpaceAfterComma(p.getTypeInDec().ifPrototypeReturnsItsName());
					s.append(typeParam + " p" + indexParam);
					if ( --size > 0 ) s.append(", ");
					++indexParam;
				}
				if ( parameterList.size() > 0 ) s.append(" )");

				s.append(" -> " + Lexer.addSpaceAfterComma(currentPrototypeTypeName) );
				s.append(" {\n");

				s.append("        @javacode<**< \n");
				s.append( "            return new ");
				s.append(currentPrototypeJavaName + "(");
				size = parameterList.size();
				for (int ii = 0; ii < parameterList.size(); ++ii) {
					s.append("_p" + ii);
					if ( --size > 0 )
						s.append(", ");
				}

				s.append(");\n");
				s.append("        >**>\n");
				s.append("    }\n");

			}

		}


		if (       !hasAtLeastOneInitMethod
				&& (currentPrototype.getContextParameterArray() == null || currentPrototype.getContextParameterArray().size() == 0)

				) {
			if ( currentPrototype.getInstanceVariableList().size() > 0 ) {
				/*
				 * there is no init or init: method and the prototype has instance variables.
				 */
				ArrayList<InstanceVariableDec> nonInitializedInstanceVariable = new ArrayList<>();
				for ( InstanceVariableDec iv : currentPrototype.getInstanceVariableList() ) {
					if ( iv.getExpr() == null && ! iv.isShared() ) {
						nonInitializedInstanceVariable.add(iv);
						break;
					}
				}
				if ( nonInitializedInstanceVariable.size() > 0 ) {
					int size = nonInitializedInstanceVariable.size();
					String strList = "";
					for (InstanceVariableDec v : nonInitializedInstanceVariable ) {
						strList += v.getName();
						if ( --size > 0 )
							strList += ", ";
					}
					if ( nonInitializedInstanceVariable.size() == 1 )
						strList = "The instance variable " + strList + " is not initialized in its declaration ";
					else
						strList = "The instance variables " + strList + " are not initialized in their declarations ";
					this.error2(this.currentProgramUnit.getSymbol(),
							strList + "and the prototype does not declare any 'init' or 'init:' method. This is illegal");
				}
			}
			// create an empty init method
			s.append("    func init { } \n");
			// add new method
			if ( !currentPrototype.getIsAbstract() ) {
				s.append("\n    func new -> " + Lexer.addSpaceAfterComma(currentPrototypeTypeName) + " {\n");

				s.append("        @javacode<**< \n");
				s.append( "            return new ");
				s.append(currentPrototypeJavaName + "();");
				s.append("        >**>\n");
				s.append("    }\n");
			}

		}



	}

	private ArrayList<GenericParameter> templateDec() {

		  /**
		   * true if the file name of this compilation unit is something like
		   *    Set(1).cyan
		   * That is, there is a number after '('. This means that the identifier
		   * that follows '<' in the declaration of Set is a *generic parameter*
		   * and it is not a real prototype. Note that if mixed generic
		   * prototypes were allowed, with file names
		   *         Hashtable(1, String).cyan
		   * and declarations like
		   *        object Hashtable<T, String> ... end
		   * then it will be necessary to use an array of boolean. Each position
		   * could be a generic parameter or a real prototype.
		   *

		int indexLeftPar = compilationUnit.getFilename().indexOf('(');
		if ( indexLeftPar >= 0 ) {
			char ch = compilationUnit.getFilename().charAt(indexLeftPar + 1);
			if ( Character.isDigit(ch) )
				hasGenericParameter = true;
		}
		*/

		ArrayList<GenericParameter> genericParameterList =
			new ArrayList<GenericParameter>();
		next();
		genericParameterList.add(templateVarDec());
		while ( symbol.token == Token.COMMA ) {
			lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
			next();
			genericParameterList.add(templateVarDec());
		}
		if ( symbol.token != Token.GT ) {
		    error2(symbol, "> expected after the types of a generic object." + foundSuch());
		}
		next();

		return genericParameterList;
	}


	private GenericParameter templateVarDec() {

		GenericParameter genericParameter;




		Expr type;

		try {
			this.prohibitTypeof = true;
			type = type();
		}
		finally {
			this.prohibitTypeof = false;
		}


		if ( symbol.token == Token.IDENT ) {
			/*
			 * something like
			 *     Stack< Person T >
			 */
			error2(symbol, "A ',' or '>' expected instead of an identifier." + foundSuch());
			genericParameter = null;
		}
		else {
			/*
			 * type may be:
			 *    (a) a real parameter such as Int or main.Person:
			 *             object Stack<Int> ... end
			 *             object Stack<main.Person> ... end
			 *    (b) or a formal parameter such as T in
			 *             object Stack<T> ... end
			 *        or
			 *             object Stack<T+> ... end
			 *    (c) a real parameter such as 'add' in
			 *             object Inter<add> ... end
			 */

			/*if ( ! (type instanceof ExprIdentStar) )
				error(type.getFirstSymbol(), "Generic parameter expected",
						type.asString(), ErrorKind.generic_parameter_expected); */

			ArrayList<Symbol> paramSymbolArray;
			if ( type instanceof ExprIdentStar )
				paramSymbolArray = ((ExprIdentStar ) type).getIdentSymbolArray();
			else if ( type instanceof ast.ExprGenericPrototypeInstantiation )
				paramSymbolArray =  ((ast.ExprGenericPrototypeInstantiation) type).getTypeIdent().getIdentSymbolArray();
			else {
				error(true, type.getFirstSymbol(),
						"Generic parameter expected." + foundSuch(), type.asString(), ErrorKind.generic_parameter_expected);
				return null;
			}
			Symbol sym = paramSymbolArray.get(0);

			if ( paramSymbolArray.size() == 1 ) {
				String s = sym.getSymbolString();
				int indexLessThan = s.indexOf('<');
				if ( indexLessThan > 0 )
					  // "Tuple<Char, Int>" becomes "Tuple"
					s = s.substring(0, indexLessThan);

				if ( this.compilationUnit.getProgram().isInPackageCyanLang(s) || s.equals(NameServer.dynName) ) {
					/*
          			 *    (a) a real parameter such as Int or main.Person:
			         *             object Stack<Int> ... end
			         *             object Stack<Tuple<Char, Int>> ... end
					 */
					genericParameter = new GenericParameter(type, GenericParameterKind.PrototypeCyanLang);

				}
				else if ( Character.isLowerCase(sym.getSymbolString().charAt(0)) ) {
					/*
        			 *    (c) a real parameter such as 'add' in
		        	 *             object Inter<add> ... end
					 */
					genericParameter = new GenericParameter(type, GenericParameterKind.LowerCaseSymbol);
				}
				else {
					/*
					 *    (b) or a formal parameter such as T in
					 *            object Stack<T> ... end
					 *        or
					 *             object Stack<T+> ... end
					 */
					genericParameter = new GenericParameter(type, GenericParameterKind.FormalParameter);
					if ( symbol.token == Token.PLUS ) {
						next();
						genericParameter.setPlus(true);
					}
				}
			}
			else {
				/*
				 *    (a) a real parameter such as cyan.lang.Int or main.Person:
				 *             object Stack<cyan.lang.Int> ... end
				 *             object Stack<main.Person> ... end
		         *             object Stack<cyan.lang.Tuple<Char, Int>> ... end
				 */
				String s = type.asString();
				int indexCyanLang = s.indexOf(NameServer.cyanLanguagePackageName);
				if ( indexCyanLang == 0 ) {
					int indexLessThan = s.indexOf('<');
					if ( indexLessThan > 0 )
						  // "cyan.lang.Tuple<Char, Int>" becomes "cyan.lang.Tuple"
						s = s.substring(0, indexLessThan);
					String protoName = s.substring(NameServer.cyanLanguagePackageName.length()+1, s.length());
					if ( this.compilationUnit.getProgram().isInPackageCyanLang(protoName) || protoName.equals(NameServer.dynName) ) {
						/*
	          			 *    (a) a real parameter such as
				         *             object Stack<cyan.lang.Int> ... end
						 */
						genericParameter = new GenericParameter(type, GenericParameterKind.PrototypeCyanLang);

					}
					else
						genericParameter = new GenericParameter(type, GenericParameterKind.PrototypeWithPackage);

				}
				else
					genericParameter = new GenericParameter(type, GenericParameterKind.PrototypeWithPackage);
			}
		}
		return genericParameter;
	}


	/*
	private GenericParameter templateVarDecDelete(boolean hasGenericParameter) {


		GenericParameter genericParameter;


		if ( hasGenericParameter ) {
			Expr t = type();
			Symbol genericParameterSymbol;
			if ( symbol.token == Token.IDENT ) {
				genericParameter = new GenericParameter(symbol);
				genericParameter.setParameterType(t);
				next();
			}
			else {

				 //the type is in fact the parameter name

				if ( ! (t instanceof ExprIdentStar) || ((ExprIdentStar )t).getIdentSymbolArray().size() != 1 )
					error2( t.getFirstSymbol(), "Generic parameter expected" );
				genericParameterSymbol = ((ExprIdentStar ) t).getIdentSymbolArray().get(0);
				genericParameter = new GenericParameter(genericParameterSymbol);
			}
			currentProgramUnit.setGenericPrototype(true);
		}
		else {

			// a real prototype as formal parameter such as Boolean in
			//       object MyGeneric<Int><Boolean> ... end

			if ( ! startType(symbol.token) )
				error2(symbol, "type expected in generic object declaration");
			Expr realParameter = exprPrimary();
			genericParameter = new GenericParameter(realParameter);
		}
		return genericParameter;
	}
	*/

	/**
	 * ContextDec ::=  "(" CtxtObjParamDec  { "," CtxtObjParamDec } ")"
	 * @return
	 */
	private ArrayList<ContextParameter> contextDec() {
		ArrayList<ContextParameter> contextParameterArray = new ArrayList<ContextParameter>();
		next();
		CtxtObjParamDec(contextParameterArray);
		while ( symbol.token == Token.COMMA ) {
			lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
			next();
			CtxtObjParamDec(contextParameterArray);
		}
		if ( symbol.token != Token.RIGHTPAR )
			error2(symbol, "')' expected after the declaration of variables of a context object." + foundSuch());
		if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
			error2(symbol, "letter, number, or '_' after ')'");
		}

		next();
		return contextParameterArray;
	}

	/*
	 * CtxtObjParamDec ::=  [ ``public"\/ \verb@|@ ``protected"\/ \verb@|@ ``private"\/ ] Type  \\
                            [ ``\%"\/ \verb"|" ``\&"\/ \verb"|" ``*"\/ ] Id
	 */
	private void CtxtObjParamDec(ArrayList<ContextParameter> contextParameterArray) {


		VariableKind parameterType;
		Token visibility = Token.PRIVATE;

		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList = null;
		ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList = null;

		Tuple2<ArrayList<CyanMetaobjectWithAtAnnotation>, ArrayList<CyanMetaobjectWithAtAnnotation>> tc = parseMetaobjectAnnotations_NonAttached_Attached();
		if ( tc != null ) {
			nonAttachedMetaobjectAnnotationList = tc.f1;
			attachedMetaobjectAnnotationList = tc.f2;
		}
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			this.error(true, nonAttachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
					"This metaobject annotation of metaobject '" +
							nonAttachedMetaobjectAnnotationList.get(0).getCyanMetaobject().getName() + "' cannot be attached to a context object or any other declaration." +
							"just before the specification of a Program.", null, ErrorKind.metaobject_error);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation :  attachedMetaobjectAnnotationList ) {
				CyanMetaobjectWithAt cyanMetaobject = annotation.getCyanMetaobject();
				if ( ! cyanMetaobject.mayBeAttached(DeclarationKind.INSTANCE_VARIABLE_DEC) ) {
					this.error(true, attachedMetaobjectAnnotationList.get(0).getFirstSymbol(),
							"This metaobject annotation cannot be attached to an instance variable. It can be attached to " +
								       " one entity of the following list: [ "+
								       cyanMetaobject.attachedListAsString() + " ]", null, ErrorKind.metaobject_error);

				}
			}
		}


		Symbol firstSymbol = symbol;
		if ( symbol.token == Token.PUBLIC || symbol.token == Token.PROTECTED || symbol.token == Token.PRIVATE ) {
			error2(symbol, "Context parameters cannot have qualifier. Currently they are always private. That will change someday");
			visibility = symbol.token;
			next();
		}
		Expr type;

		try {
			this.prohibitTypeof = true;
			type = type();
		}
		finally {
			this.prohibitTypeof = false;
		}


		if ( symbol.token == Token.BITAND ) {
	    	parameterType = VariableKind.LOCAL_VARIABLE_REF;
	    	next();
		}
		else {
			parameterType = VariableKind.COPY_VAR;
		}
		if ( symbol.token != Token.IDENT ) {
			error2(symbol, "identifier expected." + foundSuch());
		}
		ContextParameter contextParameter = new ContextParameter( (SymbolIdent) symbol, parameterType, type, visibility, firstSymbol,
				nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList);

	    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(symbol.getSymbolString());
	    if ( cyanMacro != null ) {
	    	this.error2(symbol, "This instance variable has the name of a macro keyword of a macro imported by this compilation unit");
	    }


		if ( attachedMetaobjectAnnotationList != null ) {
	  		for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.INSTANCE_VARIABLE_DEC) ) {
					annotation.setDeclaration(contextParameter);
				}
				else {
					/*
					 * the metaobject cannot be attached to an instance variable
					 */
					this.error(true, annotation.getFirstSymbol(),
							"This metaobject annotation cannot be attached to an instance variable. It can be attached to " +
								       " one entity of the following list: [ "+
											annotation.getCyanMetaobject().attachedListAsString() + " ]", null, ErrorKind.metaobject_error);

				}
				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
					ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
					Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
					if ( t != null )
						contextParameter.addFeature(t);
				}
			}
		}



		next();
		contextParameterArray.add(contextParameter);
		((ObjectDec ) this.currentProgramUnit).addInstanceVariable(contextParameter);
		((ObjectDec ) this.currentProgramUnit).addSlot(contextParameter);
		((ObjectDec ) this.currentProgramUnit).setHasContextParameter(true);
	}

	/**
	 * parse a type that may have a "|" in it as
	 *     Int | Char
	   @return
	 */
	public Expr type() {
		Expr t = singleType();
		if ( symbol.token != Token.BITOR || (symbol.token == Token.BITOR && next(0).token == Token.IDENTCOLON) ) {
			return t;
		}
		else {
			ArrayList<Expr> typeArray = new ArrayList<Expr>();
			if ( t instanceof ExprTypeof )
				error(true, t.getFirstSymbol(), "'typeof' cannot be used in union types", "", ErrorKind.typeof_used_in_union);
			typeArray.add(t);
			while ( symbol.token == Token.BITOR ) {
					next();
					t = singleType();
					if ( t instanceof ExprTypeof )
						error(true, t.getFirstSymbol(), "'typeof' cannot be used in union types", "", ErrorKind.typeof_used_in_union);
					typeArray.add(t);
			}
			ArrayList<ArrayList<Expr>> typeArrayArray = new ArrayList<ArrayList<Expr>>();
			typeArrayArray.add(typeArray);
			Symbol first = typeArray.get(0).getFirstSymbol();
			ExprIdentStar unionId = new ExprIdentStar(new SymbolIdent(Token.IDENT, "Union",
					first.getStartLine(), first.getLineNumber(), first.getColumnNumber(), first.getOffset(), this.compilationUnit));
			ExprGenericPrototypeInstantiation unionExpr = new ExprGenericPrototypeInstantiation(  unionId, typeArrayArray, this.currentProgramUnit, null);

			MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation = null;
			if ( symbol.token == Token.DOT_OCTOTHORPE ) {
				// if ( symbol.token == Token.CYANSYMBOL ) {
				messageSendToMetaobjectAnnotation = this.parseMessageSendToMetaobjectAnnotation();
			}

			if ( messageSendToMetaobjectAnnotation != null )
				if (! messageSendToMetaobjectAnnotation.action(compilationUnit.getProgram(), unionExpr) ) {
					error2(first, "No action associated to message '" + messageSendToMetaobjectAnnotation.getMessage()
					+ "'");
				}


			return unionExpr;
		}

	}

	/**
	 * parse a type that is not a union type. That is, there is no "|" in it as
	 *      Int | Char
	   @return
	 */

	private Expr singleType() {
		Symbol identOne;

		switch ( symbol.token ) {
		case IDENT:
			Expr identExpr;
			identOne = symbol;
			next();
			if ( symbol.token != Token.PERIOD ) {
				String newName;
				if ( (newName = falseKeywordsTable.get(identOne.getSymbolString())) != null ) {
					// found something like a "int", which a lower-case letter.
					if ( ask(identOne, "Should change " + identOne.getSymbolString() + " to " + newName +
							" ? (y, n)") ) {
						int sizeIdentSymbol = identOne.getSymbolString().length();
						compilationUnit.addAction(
								new ActionDelete(compilationUnit,
										identOne.startOffsetLine + identOne.getColumnNumber() - 1,
										sizeIdentSymbol,
										identOne.getLineNumber(),
										identOne.getColumnNumber()));
						compilationUnit.addAction(
								new ActionInsert(newName, compilationUnit,
										identOne.startOffsetLine + identOne.getColumnNumber() - 1,
										identOne.getLineNumber(),
										identOne.getColumnNumber()));
					}
				}

				identExpr = new ExprIdentStar(identOne);
			}
			else {
				ArrayList<Symbol> identSymbolArray = new ArrayList<Symbol>();
				identSymbolArray.add(identOne);
				while ( symbol.token == Token.PERIOD ) {
					next();
					if ( ! startType(symbol.token) ) {
						error2(symbol, "package, object name or slot (variable or method) expected." + foundSuch());
					}
					identSymbolArray.add(symbol);
					next();
				}
				identExpr = new ExprIdentStar(identSymbolArray, symbol);
			}
			if ( symbol.token == Token.LT_NOT_PREC_SPACE ) {

				ArrayList<ArrayList<Expr>> arrayOfTypeList = new ArrayList<ArrayList<Expr>>();
				while ( symbol.token == Token.LT_NOT_PREC_SPACE  ) {
					next();
					ArrayList<Expr> aTypeList = typeList();
					if ( symbol.token != Token.GT ) {
					    error2(symbol, "> expected after the types of a generic object." + foundSuch());
					}
					next();
					arrayOfTypeList.add(aTypeList);
				}
				if ( arrayOfTypeList.size() == 0 )
					error2(identExpr.getFirstSymbol(), "Missing parameter for generic prototype instantiation. Something like 'Stack<>'");
				MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation = null;
				if ( symbol.token == Token.DOT_OCTOTHORPE ) {
					// if ( symbol.token == Token.CYANSYMBOL ) {
					messageSendToMetaobjectAnnotation = this.parseMessageSendToMetaobjectAnnotation();
				}
				identExpr = new ExprGenericPrototypeInstantiation( (ExprIdentStar ) identExpr, arrayOfTypeList, currentProgramUnit,
						messageSendToMetaobjectAnnotation);

				if ( messageSendToMetaobjectAnnotation != null )
					if (! messageSendToMetaobjectAnnotation.action(compilationUnit.getProgram(), (ExprGenericPrototypeInstantiation ) identExpr) ) {
						error2(identExpr.getFirstSymbol(), "No action associated to message '" + messageSendToMetaobjectAnnotation.getMessage()
						+ "'");
					}
			}
			else {
				if ( identExpr instanceof ExprIdentStar ) {
					MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation = null;
					if ( symbol.token == Token.DOT_OCTOTHORPE ) {
						// if ( symbol.token == Token.CYANSYMBOL ) {
						messageSendToMetaobjectAnnotation = this.parseMessageSendToMetaobjectAnnotation();
					}
					((ExprIdentStar ) identExpr).setMessageSendToMetaobjectAnnotation(messageSendToMetaobjectAnnotation);
					if ( messageSendToMetaobjectAnnotation != null ) {
						if (! messageSendToMetaobjectAnnotation.action(compilationUnit.getProgram(), (ExprIdentStar ) identExpr) ) {
							error2(identExpr.getFirstSymbol(), "No action associated to message '" + messageSendToMetaobjectAnnotation.getMessage()
							+ "'");
						}
					}
				}

			}
			return identExpr;
		case TYPEOF:
			if ( prohibitTypeof ) {
				/*
				 * 'typeof' is not allowed inside a method signature or as a type of an instance variable
				 */
				error2(symbol, "'typeof' can only be used inside methods or in the DSL attached to metaobjects that start with @");
			}
			Symbol typeofSymbol = symbol;
			next();
			if ( symbol.token != Token.LEFTPAR )
				error2(symbol, "'(' expected after keyword Expr." + foundSuch());
			next();
			Expr exprType = expr();
			if ( symbol.token != Token.RIGHTPAR )
				error2(symbol, "')' expected after function 'typeof'." + foundSuch());
			if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
				error2(symbol, "letter, number, or '_' after ')'");
			}
			next();
			return new ExprTypeof(typeofSymbol, exprType);
		default:
			if ( isBasicType(symbol.token) || symbol.token == Token.STRING || symbol.token == Token.DYN ) {
				Symbol s = symbol;
				next();
				return new ExprIdentStar(s);
			}
			else {
				error2(symbol, "type expected." + foundSuch() + ". Note that in the declaration of an instance variable the order of keywords is fixed: [ private | public ] [ shared ] [ var | let ] Type Id ';'");
				return null;
			}


		}
	}

	/**
	 * the current symbol is Token.DOT_OCTOTHORPE
	 */
	private MessageSendToMetaobjectAnnotation parseMessageSendToMetaobjectAnnotation() {

		//# added
		next();
		if ( symbol.token != Token.IDENT ) {
			this.error2(symbol,  "Identifier expected. The identifier should be the name of the message sent at compile-time to the prototype that precedes it."
					 + foundSuch());
		}
		//# end added
		MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation;
		String messageName = symbol.getSymbolString();
		Symbol firstSymbol = symbol;
		messageSendToMetaobjectAnnotation = new MessageSendToMetaobjectAnnotation(messageName);
		if ( ! MetaInfoServer.metaobjectAnnotationMethodNameSet.contains(messageName) ) {
			error2(symbol, "Unknown message send to generic prototype instantiation: '" + messageName + "'");
		}
		else {

			next();
			int numParam = 0;
			if ( symbol.token == Token.LEFTPAR && ! Lexer.hasSpaceBefore(symbol, compilationUnit) ) {
				next();
				while ( symbol.token == Token.IDENT || symbol.token == Token.LITERALSTRING ||
						symbol.token == Token.CYANSYMBOL ) {
					messageSendToMetaobjectAnnotation.addExpr(symbol.getSymbolString());
					++numParam;
					next();
					if ( symbol.token == Token.COMMA ) {
						lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
						next();
					}
					else
						break;
				}
				if ( symbol.token != Token.RIGHTPAR )
					error2(symbol, "')' expected after a list of parameters." + foundSuch());
				if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
					error2(symbol, "letter, number, or '_' after ')'");
				}
				next();
			}
			if ( messageName.equals("writeCode") ) {
				if ( numParam != 0 ) {
					this.error2(firstSymbol, "'writeCode' takes zero or one parameter");
				}
			}

			/*
			if ( Lexer.hasSpaceBefore(symbol, compilationUnit) ) {
				error2(symbol, "Space between the '>' of a generic prototype instantiation and the message send to the metaobject annotation. Something like Function<Int>   .#writeCode");
		    }
			else {}
			*/
		}

		return messageSendToMetaobjectAnnotation;
	}

	/**
	 * error in the static methods below
	 */
	static public void staticError(Symbol errSymbol, String msg, CompilationUnitSuper compUnit, String errorMessage) {
		Compiler compiler = null;
		compUnit.error(errSymbol,  errSymbol.getLineNumber(), errorMessage + msg, compiler, null);

	}


	/**
	 * parse the package/prototype given by <code>typeAsString</code>.
	 * The type may be a simple name (Person, Int, main.Program) or a generic prototype instantiation. Any error messages will use
	 * the line and column number of <code>symUsedInError</code>.
	 * The error message will be prefixed by parameter <code>message</code>. The current compilation unit is <code>compUnit</code>
	 * and the current program unit is <code>currentPU</code>.
	   @param typeAsString
	   @return
	 */
	static public Expr parseSingleTypeFromString(String typeAsString,
			Symbol symUsedInError, String message, CompilationUnitSuper compUnit, ProgramUnit currentPU
			//, Compiler compiler
			) {

		/*
		Symbol sym = this.getFirstSymbol();

		SymbolIdent symbolIdent = new SymbolIdent(Token.IDENT, prototypeName, sym.getStartLine(),
				sym.getLineNumber(), sym.getColumnNumber(), sym.getOffset() );
		ExprIdentStar typeIdent = new ExprIdentStar(symbolIdent);

		ExprGenericPrototypeInstantiation gpi = new ExprGenericPrototypeInstantiation( typeIdent,
				realTypeListList, env.getCurrentProgramUnit());
		return CompilerManager.createGenericPrototype(gpi, env);		 *
		 */

		//String typeAsString = packageName + "." + prototypeName + "\0";
		typeAsString += "\0";

		lexerFromString = new Lexer( typeAsString.toCharArray(), compUnit, new HashSet<saci.CompilationInstruction>(), null);
		Expr programUnitAsExpr = null;
		try {
			programUnitAsExpr = singleTypeFromStringRec(symUsedInError, compUnit, currentPU, message);
		} catch ( CompileErrorException cee ) {
			/*
			 * an horrible thing to do. No better implementation in sight.
			 * /
			UnitError lastError = compUnit.getErrorList().get(compUnit.getErrorList().size()-1);
			errorMessage = lastError.getMessage();
			lastError.setMessage(message + " " + errorMessage);
			*/
			throw new CompileErrorException();
		}
		return programUnitAsExpr;
	}

	static public ProgramUnit singleTypeFromString(String typeAsString,
			Symbol symUsedInError, String message, CompilationUnit compUnit, ProgramUnit currentPU, Env env) {

		ProgramUnit p = null;
		try {
			p = Compiler.singleTypeFromStringThrow(typeAsString, symUsedInError, message, compUnit, currentPU, env);
		}
		catch (CompileErrorException e) {
			env.setThereWasError(true);
			throw e;
		}
		return p;
	}

	/**
	 * return the program unit  corresponding to the package/prototype given by <code>typeAsString</code>.
	 * The type may be a simple name (Person, Int, main.Program) or a generic prototype instantiation. Any error messages will use
	 * the line and column number of <code>symUsedInError</code>.
	 * The error message will be prefixed by parameter <code>message</code>. The current compilation unit is <code>compUnit</code>
	 * and the current program unit is <code>currentPU</code>.
	   @param typeAsString
	   @return
	 */
	static public ProgramUnit singleTypeFromStringThrow(String typeAsString,
			Symbol symUsedInError, String message, CompilationUnit compUnit, ProgramUnit currentPU, Env env) {

		/*
		Symbol sym = this.getFirstSymbol();

		SymbolIdent symbolIdent = new SymbolIdent(Token.IDENT, prototypeName, sym.getStartLine(),
				sym.getLineNumber(), sym.getColumnNumber(), sym.getOffset() );
		ExprIdentStar typeIdent = new ExprIdentStar(symbolIdent);

		ExprGenericPrototypeInstantiation gpi = new ExprGenericPrototypeInstantiation( typeIdent,
				realTypeListList, env.getCurrentProgramUnit());
		return CompilerManager.createGenericPrototype(gpi, env);		 *
		 */

		//String typeAsString = packageName + "." + prototypeName + "\0";
		typeAsString += "\0";

		lexerFromString = new Lexer( typeAsString.toCharArray(), compUnit, new HashSet<saci.CompilationInstruction>(), null);
		Expr programUnitAsExpr = null;
		try {
			programUnitAsExpr = singleTypeFromStringRec(symUsedInError, compUnit, currentPU, message);
		} catch ( CompileErrorException cee ) {
			/*
			 * an horrible thing to do. No better implementation in sight.
			 * /
			UnitError lastError = compUnit.getErrorList().get(compUnit.getErrorList().size()-1);
			String errorMessage = lastError.getMessage();
			lastError.setMessage(message + " " + errorMessage);
			*/
			throw new CompileErrorException();
		}
		if ( programUnitAsExpr instanceof ExprGenericPrototypeInstantiation ) {
			return CompilerManager.createGenericPrototype( (ExprGenericPrototypeInstantiation ) programUnitAsExpr, env);
		}
		else if ( programUnitAsExpr instanceof ExprIdentStar ) {
			ExprIdentStar idStar = (ExprIdentStar ) programUnitAsExpr;
			ArrayList<Symbol> symList = idStar.getIdentSymbolArray();
			int size = symList.size();
			String packageName = "";
			String prototypeName;
			int k = size - 1;
			for (int i = 0; i < size - 1; ++i ) {
				packageName = packageName + symList.get(i).getSymbolString();
				if ( --k > 0 )
					packageName += ".";
			}
			prototypeName = symList.get(size-1).getSymbolString();

			ProgramUnit pu;
			if ( packageName.length() == 0 ) {
				pu = env.searchVisibleProgramUnit(prototypeName, symUsedInError, true);
			}
			else {
				pu = env.searchPackagePrototype(packageName, prototypeName);
			}
			if ( pu == null ) {
				pu = env.searchVisibleProgramUnit(prototypeName, symUsedInError, true);
				if ( packageName.length() == 0 ) {
					compUnit.error(symUsedInError, symUsedInError.getLineNumber(),  "Prototype '" + prototypeName +
							"' was not found", null, env);
				}
				else {
					compUnit.error(symUsedInError, symUsedInError.getLineNumber(),  "Prototype '" + packageName + "." + prototypeName +
							"' was not found", null, env);
				}
				return null;
			}
			else
				return pu;
		}
		else {
			compUnit.error(symUsedInError, symUsedInError.getLineNumber(),  "Internal error at Compiler::singleTypeFromString", null, env);
			return null;
		}

	}
	/**
	 * return an expression corresponding to the type typeAsString given as a string
	   @param typeAsString
	   @return
	 */
	static public Expr singleTypeFromStringRec(Symbol symUsedInError, CompilationUnitSuper compUnit,
			ProgramUnit currentPU, String errorMessage) {
		Symbol identOne;

		// 	public Lexer(char[] in, CompilationUnitSuper compilationUnit, HashSet<saci.CompilationInstruction> compInstSet) {

		switch ( lexerFromString.symbol.token ) {
		case TYPEOF:
			staticError(symUsedInError, "'typeof' is not supported", compUnit, errorMessage);
			return null;
		case IDENT:
			Expr identExpr;
			identOne = lexerFromString.symbol;
			lexerFromString.next();
			if ( lexerFromString.symbol.token != Token.PERIOD ) {
				identExpr = new ExprIdentStar(identOne);
			}
			else {
				ArrayList<Symbol> identSymbolArray = new ArrayList<Symbol>();
				identSymbolArray.add(identOne);
				while ( lexerFromString.symbol.token == Token.PERIOD ) {
					lexerFromString.next();
					if ( ! startType(lexerFromString.symbol.token) ) {
						staticError(symUsedInError,
								" package, object name or slot (variable or method) expected." + foundSuch(lexerFromString.symbol),
								compUnit, errorMessage);
					}
					identSymbolArray.add(lexerFromString.symbol);
					lexerFromString.next();
				}
				identExpr = new ExprIdentStar(identSymbolArray, null);
			}
			if ( lexerFromString.symbol.token == Token.LT_NOT_PREC_SPACE ) {

				ArrayList<ArrayList<Expr>> arrayOfTypeList = new ArrayList<ArrayList<Expr>>();
				while ( lexerFromString.symbol.token == Token.LT_NOT_PREC_SPACE  ) {
					lexerFromString.next();
					ArrayList<Expr> aTypeList = typeListFromString(symUsedInError, compUnit, currentPU, errorMessage);
					if ( lexerFromString.symbol.token != Token.GT ) {
						staticError(symUsedInError, " '>' expected after the types of a generic object." + foundSuch(lexerFromString.symbol),
								compUnit, errorMessage);
					}
					lexerFromString.next();
					arrayOfTypeList.add(aTypeList);
				}



				identExpr = new ExprGenericPrototypeInstantiation( (ExprIdentStar ) identExpr, arrayOfTypeList, currentPU, null);
			}
			return identExpr;
		default:
			if ( isBasicType(lexerFromString.symbol.token) || lexerFromString.symbol.token == Token.STRING || lexerFromString.symbol.token == Token.DYN ) {
				Symbol s = lexerFromString.symbol;
				lexerFromString.next();
				return new ExprIdentStar(s);
			}
			else {
				staticError(symUsedInError, " type expected." + foundSuch(lexerFromString.symbol), compUnit, errorMessage);
				return null;
			}
		}
	}


	private ArrayList<Expr> genericPrototypeArgList() {

		ArrayList<Expr> aTypeList = new ArrayList<Expr>();

		aTypeList.add(type());
		while ( symbol.token == Token.COMMA ) {
			lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
			next();
			aTypeList.add(type());
		}
		return aTypeList;

	}

	private ArrayList<Expr> typeList() {

		ArrayList<Expr> aTypeList = new ArrayList<Expr>();

		Expr t;

		try {
			this.prohibitTypeof = true;
			t = type();
		}
		finally {
			this.prohibitTypeof = false;
		}


		aTypeList.add(t);
		while ( symbol.token == Token.COMMA ) {
			if ( this.currentProgramUnit.getOuterObject() == null ) { lexer.checkWhiteSpaceParenthesisAfter(symbol, ","); }
			next();
			try {
				this.prohibitTypeof = true;
				t = type();
			}
			finally {
				this.prohibitTypeof = false;
			}
			aTypeList.add(t);
		}
		return aTypeList;

	}


	private static ArrayList<Expr> typeListFromString(Symbol symUsedInError, CompilationUnitSuper compUnit,
			ProgramUnit currentPU, String errorMessage) {

		ArrayList<Expr> aTypeList = new ArrayList<Expr>();

		aTypeList.add(singleTypeFromStringRec(symUsedInError, compUnit, currentPU, errorMessage));
		while ( Compiler.lexerFromString.token == Token.COMMA ) {
			/*
			String ermsg = lexerFromString.retMessageCheckWhiteSpaceParenthesisAfter(",");
			if ( ermsg != null ) {
				staticError(symUsedInError, ermsg, compUnit, "");
			}
			*/
			Compiler.lexerFromString.next();
			aTypeList.add(singleTypeFromStringRec(symUsedInError, compUnit, currentPU, errorMessage));
		}
		return aTypeList;

	}


	public void slotDec(ObjectDec currentObject, ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
	    		ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
    		  Symbol firstSlotSymbol, boolean compilerCreatedMethod ) {

		boolean isFinal = false;

		Token visibility = null;
		if ( symbol.token == Token.PUBLIC ||
			 symbol.token == Token.PROTECTED ||
			 symbol.token == Token.PRIVATE ) {
			visibility = symbol.token;
			next();
		}
		if ( symbol.token == Token.FINAL ) {
			isFinal = true;
			next();
			if ( symbol.token != Token.IDENTCOLON &&
				 symbol.token != Token.FUNC &&
				 symbol.token != Token.OVERLOAD &&
				 symbol.token != Token.OVERRIDE )
				error2(symbol, "'final' applies only to methods");
		}
		if ( isBasicType(symbol.token) || symbol.token == Token.STRING || symbol.token == Token.DYN ) {
			if ( visibility == null )
				visibility = Token.PRIVATE;

			objectVariableDec(currentObject, visibility, nonAttachedMetaobjectAnnotationList,
					attachedMetaobjectAnnotationList, firstSlotSymbol, false, true);

		}
		else {
			switch ( symbol.token ) {
			case SHARED:
				next();
				if ( isBasicType(symbol.token) || symbol.token == Token.STRING || symbol.token == Token.DYN ) {
					if ( visibility == null )
						visibility = Token.PRIVATE;
					objectVariableDec(currentObject, visibility, nonAttachedMetaobjectAnnotationList,
							attachedMetaobjectAnnotationList, firstSlotSymbol, true, true);
				}
				else {

					if ( symbol.token != Token.VAR && symbol.token != Token.IDENT && symbol.token != Token.LET ) {
						this.error2(symbol,  "'var', 'let', or a type expected." + foundSuch() + ". Note that in the declaration of an instance variable the order of keywords is fixed: [ private | public ] [ shared ] [ var | let ] Type Id ';'");
					}
					else  {
						if ( visibility == null )
							visibility = Token.PRIVATE;
						if ( symbol.token == Token.VAR ) {
							next();
							objectVariableDec(currentObject, visibility, nonAttachedMetaobjectAnnotationList,
								attachedMetaobjectAnnotationList, firstSlotSymbol, true, false);
						}
						else  {
							if ( symbol.token == Token.LET )
								next();
							objectVariableDec(currentObject, visibility, nonAttachedMetaobjectAnnotationList,
									attachedMetaobjectAnnotationList, firstSlotSymbol, true, false);
						}

					}

				}
				break;
			case VAR:
				if ( visibility == null )
					visibility = Token.PRIVATE;
				next();
				objectVariableDec(currentObject, visibility, nonAttachedMetaobjectAnnotationList,
						attachedMetaobjectAnnotationList, firstSlotSymbol, false, false);
				break;
			case IDENTCOLON:
				// user declared something like
				//   public add: (Int item) [ ... ]
					if ( ask(symbol, "Should I insert 'func' before " + symbol.getSymbolString() +
					" ? (y, n)") ) {
						compilationUnit.addAction(
								new ActionInsert("func ", compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getLineNumber(), symbol.getColumnNumber()));
					// String symbolString, int startOffsetLine, int lineNumber, int columnNumber)
					insertSymbol(new Symbol(Token.FUNC, "func", symbol.getStartLine(), symbol.getLineNumber(),
							        symbol.getColumnNumber(), symbol.getOffset(), this.compilationUnit));
					}
					else {
						error2(symbol, "keyword 'func' expected." + foundSuch());
					}

					//$FALL-THROUGH$
			case FUNC:
			case OVERLOAD:
			case OVERRIDE:
			case ABSTRACT:
				if ( visibility == null )
					visibility = Token.PUBLIC;
				methodDec(currentObject, visibility, isFinal, nonAttachedMetaobjectAnnotationList,
						attachedMetaobjectAnnotationList, compilerCreatedMethod);
				break;
			case IDENT:
			case LET:
				if ( visibility == null )
					visibility = Token.PRIVATE;
				if ( symbol.token == Token.LET )
					next();
				objectVariableDec(currentObject, visibility, nonAttachedMetaobjectAnnotationList,
						attachedMetaobjectAnnotationList, firstSlotSymbol, false, true);

				break;
			default:
				error2(symbol, "variable declaration or method declaration declaration expected." + foundSuch());
			}

		}
	}

	/**
         ObjectVariableDec} ::=  [ ``shared"\/ ] [ ``var"\/ ] Type Id \{ ``,"\/  Id \}  [ ``="\/ Expr ]  [ ``;"\/ ]
	 * @param currentObject
	 * @param visibility
	 * @param ctmoCallArray
	 */

	private void objectVariableDec(ObjectDec currentObject, Token visibility,
			                       ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
			                       ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
			                       Symbol firstSymbol, boolean shared, boolean isReadonly) {
		Expr typeInDec = null;
		Expr expr = null;
		InstanceVariableDec instVarDec;

		if ( visibility == Token.PROTECTED ) {
			this.error2(firstSymbol, "Instance variables can only be private");
		}
		if ( visibility == Token.PUBLIC ) {
			this.error2(firstSymbol, "Instance variables can only be private");
		}


		try {
			this.prohibitTypeof = true;
			typeInDec = type();
		}
		finally {
			this.prohibitTypeof = false;
		}

		int numVariableInThisDeclaration = 0;

		if ( symbol.token != Token.IDENT ) {
			if ( symbol.token == Token.ASSIGN )
				error2(symbol, "A type should always be supplied for an instance variable");
			else
				error2(symbol, "variable name expected." + foundSuch());

		}
		else {
			boolean hasExpr = false;
			while ( symbol.token == Token.IDENT ) {
				SymbolIdent variableSymbol = (SymbolIdent ) symbol;
				next();
				++numVariableInThisDeclaration;
				if (symbol.token == Token.ASSIGN ) {
					next();

					// Tuple2<Expr, Boolean> t = exprBasicTypeLiteral_Ident();
					expr = expr();
					hasExpr = true;
					/*
					if ( t.f2 ) {
						// initialized with an identifier or array of tuple containing an identifier
						error2(expr.getFirstSymbol(), "Expression with an identifier used to initialize an instance variable or shared variable");
					}
					*/
				}
				else
					expr = null;

				if ( Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
					this.error2(variableSymbol, "Variables cannot start with an uppercase letter");



				instVarDec = new InstanceVariableDec( variableSymbol, typeInDec, expr,
						visibility, shared, nonAttachedMetaobjectAnnotationList,
						attachedMetaobjectAnnotationList, firstSymbol, isReadonly );

			    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(variableSymbol.getSymbolString());
			    if ( cyanMacro != null ) {
			    	this.error2(variableSymbol, "This instance variable has the name of a macro keyword of a macro imported by this compilation unit");
			    }


				if ( attachedMetaobjectAnnotationList != null ) {
			  		for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
						if ( annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.INSTANCE_VARIABLE_DEC) ) {
							annotation.setDeclaration(instVarDec);
						}
						else {
							/*
							 * the metaobject cannot be attached to an instance variable
							 */
							this.error(true, annotation.getFirstSymbol(),
									"This metaobject annotation cannot be attached to an instance variable. It can be attached to " +
										       " one entity of the following list: [ "+
													annotation.getCyanMetaobject().attachedListAsString() + " ]", null, ErrorKind.metaobject_error);

						}
						CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
						if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
							ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
							Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
							if ( t != null )
								instVarDec.addFeature(t);
						}


					}
				}
				currentObject.addInstanceVariable(instVarDec);
				currentObject.addSlot(instVarDec);
				if ( symbol.token == Token.COMMA ) {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
					next();
				}
				else
					break;

			}
			if ( numVariableInThisDeclaration > 1  && hasExpr ) {
				this.error2(firstSymbol, "A declaration of several variables with a single type with one of the "
						+ "variables receiving a value. This is illegal. Put the variable or variables that receive a value in a separate declaration");
			}

		}
		if ( symbol.token == Token.SEMICOLON )
			next();


		if ( attachedMetaobjectAnnotationList != null ) {
			if ( numVariableInThisDeclaration > 1  ) {
				this.error2(firstSymbol, "A metaobject annotation cannot be attached to a list of instance variables. Use one annotation for each variable");
			}

			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject().attachedToSomething() ) {
					if ( ! annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.INSTANCE_VARIABLE_DEC) ) {
						this.error2(annotation.getFirstSymbol(), "This metaobject annotation cannot be attached to an instance variable.. It can be attached to " +
							       " one entity of the following list: [ "+
										annotation.getCyanMetaobject().attachedListAsString() + " ]");
					}


				}
			}

		}


	}

	/**
	 *
	   @param currentObject
	   @param visibility
	   @param isFinal
	   @param ctmoCallArray
	   @param firstSymbol is the symbol that starts the method declaration. It is
	 */

	private void methodDec(ObjectDec currentObject, Token visibility, boolean isFinal,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
			boolean compilerCreatedMethod ) {


		currentMethod = new MethodDec(currentObject, visibility, isFinal,
				nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, currentObject, methodNumber++,
				compilerCreatedMethod);
		functionStack.clear();
		this.parameterDecStack.clear();

		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject().attachedToSomething() ) {
					if ( ! annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.METHOD_DEC) )
					this.error2(annotation.getFirstSymbol(), "This metaobject annotation cannot be attached to a method. It can be attached to " +
						       " one entity of the following list: [ "+
									annotation.getCyanMetaobject().attachedListAsString() + " ]");

				}
			}

		}

		//ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList = null;

		MethodSignature ms = null;
		if ( symbol.token == Token.OVERRIDE ) {
			currentMethod.setHasOverride(true);
			next();


		}
		if ( symbol.token == Token.ABSTRACT ) {
			currentMethod.setAbstract(true);

			next();

		}
		if ( attachedMetaobjectAnnotationList != null ) {
	  		for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.METHOD_DEC) ) {
					 annotation.setDeclaration(this.currentMethod);
				}
				else {
					/*
					 * the metaobject cannot be attached to a method
					 */
					this.error(true, annotation.getFirstSymbol(),
							"This metaobject annotation cannot be attached to a method. It can be attached to " +
								       " one entity of the following list: [ "+
											annotation.getCyanMetaobject().attachedListAsString() + " ]", null, ErrorKind.metaobject_error);

				}
			}
		}

		if ( symbol.token == Token.OVERLOAD ) {
			this.currentMethod.setOverload(true);
			this.currentMethod.setPrecededBy_overload(true);
			next();
		}

		if ( symbol.token == Token.FUNC ) {
			currentMethod.setFirstSymbol(symbol);
			next();
		}
		else {
			warning(symbol, symbol.getLineNumber(), "'func' expected before a method declaration (I assumed this is a method declaration). " +
		         "Maybe you forget that there is an order of keywords that can appear before a method declaration. This order is "
					+ "'public/private/protected', 'final', 'override', 'abstract', 'overload'");
			if ( ask(symbol, "Should I insert 'func' before " + symbol.getSymbolString() +
					" ? (y, n)") )
				compilationUnit.addAction(
						new ActionInsert("func", compilationUnit,
								symbol.startOffsetLine + symbol.getColumnNumber() - 1,
								symbol.getLineNumber(), symbol.getColumnNumber()));
		}
		try {
			this.prohibitTypeof = true;
			ms = methodSignature();
		} catch ( CompileErrorException e ) {
			skipTo( Token.FUNC, Token.OVERLOAD, Token.PUBLIC, Token.PRIVATE,
					Token.PROTECTED, Token.END );
			this.prohibitTypeof = false;
			return;
		}
		finally {
			   // probably unnecessary but ...
			this.prohibitTypeof = false;
		}
		currentMethod.setMethodSignature(ms);

		currentObject.addMethod(currentMethod);
		currentObject.addSlot(currentMethod);


		if ( symbol.token == Token.LEFTCB && currentMethod.isAbstract() ) {
			error2(symbol, "Abstract methods cannot have a body");
		}

		boolean initOnce = false;
		if ( ms != null && currentMethod.getNameWithoutParamNumber().equals(NameServer.initOnce) ) {
			if ( currentMethod.getVisibility() != Token.PRIVATE ) {
				error2(ms.getFirstSymbol(), "'initOnce' methods should be 'private'");
			}
			if ( currentMethod.getIsFinal() || currentMethod.isAbstract() || currentMethod.isIndexingMethod() )
				error2(ms.getFirstSymbol(), "'initOnce' methods cannot be 'final', 'abstract', or indexing method");
			if ( ms.getReturnTypeExpr() != null ) {
				error2(ms.getReturnTypeExpr().getFirstSymbol(), "'initOnce' methods cannot declare return value type, even if it is 'Nil'");
			}
			initOnce = true;
		}

		if ( ! currentMethod.isAbstract() ) {
			if ( symbol.token == Token.ASSIGN ) {
				next();
				currentMethod.setFirstSymbolExpr(symbol);
				currentMethod.setExpr(expr());
				currentMethod.setLastSymbolExpr(previousSymbol);
				if ( symbol.token == Token.SEMICOLON || symbol.token == Token.FUNC
						|| symbol.token == Token.LET || symbol.token == Token.VAR ) {
					if ( symbol.token == Token.SEMICOLON )
						next();
				}
				else {
					error2(symbol, "';' expected." + foundSuch());
				}
			}
			else {
				// methodBody();
				if ( symbol.token != Token.LEFTCB ) {
					error2(symbol, "'{' expected in a method body." + foundSuch());
				}
				else {
					if ( ! Lexer.hasSpaceBefore(symbol, compilationUnit) ) {
						if ( ask(symbol, "Should I insert a space before " + symbol.getSymbolString() +
						         " ? (y, n)") )
				    			compilationUnit.addAction(
					    			new ActionInsert(" ", compilationUnit,
										symbol.startOffsetLine + symbol.getColumnNumber() - 1,
										symbol.getLineNumber(), symbol.getColumnNumber()));
					}
					currentMethod.setLeftCBsymbol(symbol);

					next();
					nestedIfWhile = 0;
					whileForCount = 0;

					/*
					 * initOnce methods can have only assignment statements
					 */
					if ( initOnce ) {
						currentMethod.setStatementList(initOnceBody());
					}
					else {
						currentMethod.setStatementList(statementList());
					}


					this.localVariableDecStack.clear();
					if ( symbol.token != Token.RIGHTCB ) {
						error2(symbol, "'}' expected at the end of a method body." + foundSuch());
					}
					else {
						if ( ! Lexer.hasSpaceAfter(symbol, compilationUnit) ) {
							// char ch = compilationUnit.getText()[symbol.getOffset() + symbol.getSymbolString().length()];
							if ( ask(symbol, "Should I insert a space after " + symbol.getSymbolString() +
							         " ? (y, n)") )
					    			compilationUnit.addAction(
						    			new ActionInsert(" ", compilationUnit,
											symbol.startOffsetLine + symbol.getColumnNumber() + symbol.getSymbolString().length(),
											symbol.getLineNumber(), symbol.getColumnNumber()));
						}
						currentMethod.setRightCBsymbol(symbol);
						next();

					}
				}
			}
		}

		if ( currentMethod.getAttachedMetaobjectAnnotationList() != null ) {
			for ( CyanMetaobjectAnnotation annotation : this.currentMethod.getAttachedMetaobjectAnnotationList() ) {

				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				if ( cyanMetaobject instanceof ICompilerInfo_dpa ) {
					ICompilerInfo_dpa moInfo = (ICompilerInfo_dpa ) cyanMetaobject;
					Tuple2<String, ExprAnyLiteral> t = moInfo.infoToAddProgramUnit();
					if ( t != null )
						currentMethod.addFeature(t);
				}
			}
		}

	}


/**
 *  ParamList ::= ParamDec { "," ParamDec }  |
                "(" ParamDec { "," ParamDec } ")"

    ParamDec} ::=  Type Id

 */

	private void parameterDecList(ArrayList<ParameterDec> parameterList) {

		if ( symbol.token == Token.LEFTPAR ) {
			next();
			if ( symbol.token == Token.LEFTPAR )
				warning(symbol, symbol.getLineNumber(), "two or more ( in a parameter declaration");
			parameterDecList(parameterList);
			if ( symbol.token != Token.RIGHTPAR )
				error2(symbol, "')' expected after parameter declaration." + foundSuch());
			else {
				if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
					error2(symbol, "letter, number, or '_' after ')'");
				}
				next();
			}
		}
		else {
				paramDec(parameterList);
				while ( symbol.token == Token.COMMA ) {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
					next();
					paramDec(parameterList);
				}
			}
	}


	/**   Parameter list of a function. It can have a first parameter which is 'self'
	 *  ParamList ::= ParamDec { "," ParamDec }  |
	                "(" ParamDec { "," ParamDec } ")"

	    ParamDec} ::=  Type Id

	 */

		private void parameterDecListFunction(ArrayList<ParameterDec> parameterList) {

			if ( symbol.token == Token.LEFTPAR ) {
				next();
				if ( symbol.token == Token.LEFTPAR )
					warning(symbol, symbol.getLineNumber(), "two or more ( in a parameter declaration");
				parameterDecListFunction(parameterList);
				if ( symbol.token != Token.RIGHTPAR )
					error2(symbol, "')' expected after parameter declaration." + foundSuch());
				else {
					if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
						error2(symbol, "letter, number, or '_' after ')'");
					}
					next();
				}
			}
			else {
				/*
				 * it may not have a type. It may be
				 *      { (: eval: eval: :) ^0 }
				 */
				if ( Compiler.startType(symbol.token) ) {
				    ParameterDec p;
				    paramDecFunction(parameterList);
					while ( symbol.token == Token.COMMA ) {
						lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
						next();
						p = paramDecFunction(parameterList);
						if ( p.getName().compareTo("self") == 0 )
							error2(p.getFirstSymbol(), "'self' can only be used as the first parameter of a context function.");

					}

				}
			}
		}

	/** This method parses rule
	 *        ParamDec ::=  [ Type ] Id
	 */
	private void paramDec(ArrayList<ParameterDec> parameterList) {


		Symbol variableSymbol;
		Expr typeInDec;

		try {
			this.prohibitTypeof = true;
			typeInDec = type();
		}
		finally {
			this.prohibitTypeof = false;
		}




		VariableKind parameterType;
		if ( symbol.token == Token.BITAND ) {
	    	parameterType = VariableKind.LOCAL_VARIABLE_REF;
	    	next();
		}
		else {
			parameterType = VariableKind.COPY_VAR;
		}

		if ( symbol.token != Token.IDENT ) {

			if ( isType(typeInDec) ) {
				variableSymbol = null;
			}
			else {
				// type is in fact the variable, which was given without the type as in
				//     func  at: x {  }
				variableSymbol = typeInDec.getFirstSymbol();
				typeInDec = null;
			}

		}
		else {
			variableSymbol = symbol;
			if ( Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
				this.error2(variableSymbol, "Variables and parameters cannot start with an uppercase letter");

			if ( ! Lexer.hasSpaceBefore(symbol, compilationUnit) && ! Lexer.isCharBeforeSymbolEqualTo(symbol, '&', compilationUnit)) {
				this.error2(symbol,  "there should be a space before the parameter name");
			}
			next();
		}



		if ( variableSymbol != null  &&
			 equalToFormalGenericParameter(variableSymbol.getSymbolString()) ) {
			error2(symbol, "Parameter names cannot be equal to one of the formal parameters of the generic prototype");
		}
		ParameterDec parameterDec = new ParameterDec( variableSymbol, typeInDec, currentMethod );
		parameterDec.setVariableKind(parameterType);
		if ( this.functionStack.size() > 0 )
			parameterDec.setDeclaringFunction(functionStack.peek());
		parameterList.add( parameterDec );


		parameterDecStack.push(parameterDec);
	}




	/** The parameter can have 'self' as name.
	 *        ParamDec ::=  [ Type ] Id
	 */
	private ParameterDec paramDecFunction(ArrayList<ParameterDec> parameterList) {

		Symbol variableSymbol;
		Expr typeInDec = type();


		if ( symbol.token == Token.IDENTCOLON && ! symbol.getSymbolString().equals("eval:") ) {
			error2(symbol, "Maybe you forgot to put an space before ':)' as in '{ (: Int n:) ^n}'. The correct would be '{ (: Int n :) ^n}'");
		}
		if ( symbol.token != Token.IDENT && symbol.token != Token.SELF ) {
			variableSymbol = null;
		}
		else {
			if ( searchIdent( symbol.getSymbolString() ) != null ) {
				error(false, true, symbol, "Parameter " + symbol.getSymbolString() + " is being redeclared",
						symbol.getSymbolString(), ErrorKind.parameter_is_being_redeclared);
			}
			variableSymbol = symbol;
			next();
		}
		if ( variableSymbol != null  && variableSymbol.token != Token.SELF &&
			equalToFormalGenericParameter( variableSymbol.getSymbolString()) ) {
			error2(false, symbol, "Parameter names cannot be equal to one of the formal parameters of the generic prototype");
		}

		if ( variableSymbol != null && Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
			this.error2(false, variableSymbol, "Variables and parameters cannot start with an uppercase letter");

		if ( variableSymbol == null && Character.isLowerCase(typeInDec.getFirstSymbol().getSymbolString().charAt(0)) ) {
			this.error2(false, typeInDec.getFirstSymbol(), "Type for variable '" + typeInDec.getFirstSymbol().getSymbolString() + "' is missing");

		}

		ParameterDec parameterDec = new ParameterDec( variableSymbol, typeInDec, currentMethod );
		if ( this.functionStack.size() > 0 ) {
			ExprFunction currentFunction = functionStack.peek();
			parameterDec.setDeclaringFunction(currentFunction);
			if ( variableSymbol != null && variableSymbol.token != Token.SELF )
				currentFunction.addLocalVariableDec(parameterDec);
		}
		parameterList.add( parameterDec );

		parameterDecStack.push(parameterDec);

		return parameterDec;
	}

	private StatementList initOnceBody() {
		ArrayList<Statement> statList = new ArrayList<Statement>();

		while ( symbol.token == Token.IDENT || symbol.token == Token.SELF ) {
			Expr e = null;

			if ( symbol.token == Token.IDENT ) {
				ExprIdentStar eis = ident();
				if ( eis.getIdentSymbolArray().size() != 1 ) {
					error2(eis.getFirstSymbol(), "An identifie without '.' expected");
				}
				e = eis;
			}
			else {
				Symbol selfSymbol = symbol;
				next();
				if ( symbol.token == Token.PERIOD ) {
					  // something like "self.x"
					next();
					if ( symbol.token != Token.IDENT ) {
						error2(symbol, "identifier expected after 'self.'" + foundSuch());
					}
					else {
						e = new ExprSelfPeriodIdent(selfSymbol, symbol);
						next();
					}
				}
				else {
					error2(selfSymbol, "'self.id = expr' expected");
				}
			}
			if ( symbol.token != Token.ASSIGN )
				error2(symbol, "'=' expected. 'initOnce' methods can have only assignments" + foundSuch());
			next();

			StatementAssignmentList assignmentList = new StatementAssignmentList();
			assignmentList.add(e);
			// Tuple2<Expr, Boolean> t = exprBasicTypeLiteral_Ident();
			/*
			if ( t.f2 ) {
				error2(t.f1.getFirstSymbol(), "A literal expression containing only literal values was expected");
			}
			*/
			Expr expr = expr();
			assignmentList.add(expr);
			statList.add(assignmentList);
			if ( symbol.token == Token.SEMICOLON )
				next();
			else if ( symbol.token != Token.RIGHTCB )
				error2(symbol, "';' expected");
		}
		return new StatementList(statList);
	}

	private StatementList statementList() {

		ArrayList<Statement> statList = new ArrayList<Statement>();
		Symbol previousSym;
		Statement lastStatement = null;


		if  ( symbol.token != Token.RIGHTCB &&
				symbol.token != Token.PUBLIC &&
				symbol.token != Token.PRIVATE &&
				symbol.token != Token.PROTECTED ) {
			try {
				statList.add(lastStatement = statement());
			} catch ( CompileErrorException e ) {
				skipTo( Token.SEMICOLON, Token.END, Token.PUBLIC,
						Token.PRIVATE, Token.PROTECTED, Token.FUNC, Token.OVERLOAD );
				lastStatement = new StatementNull(symbol);
				if ( symbol.token == Token.SEMICOLON )
					next();
				else if ( ! startExpr(symbol)) {
					return new StatementList(statList);
				}
			}
			while ( symbol.token != Token.RIGHTCB && symbol.token != Token.END ) {

				if ( lastStatement.demandSemicolon() ) {
					if ( symbol.token == Token.SEMICOLON )
						next();
					else if ( !( previousSymbol instanceof SymbolCharSequence ) && previousSymbol.token != Token.SEMICOLON ) {
						previousSym = previousSymbol;
						if ( symbol.token == Token.ASSIGN ) {
							error2(symbol, "assignments are statements in Cyan. They cannot appear inside an expression");
						}
						this.error2(previousSym, "';' expected." + foundSuch());
						if ( ask(previousSym, "Should I insert ';' after " + previousSym.getSymbolString() +
						         " ? (y, n)") )
				    			compilationUnit.addAction(
					    			new ActionInsert(";", compilationUnit,
					    					previousSym.getOffset() + previousSym.getSymbolString().length(),
					    					previousSym.getLineNumber(), previousSym.getColumnNumber()));
					}
				}
				else {
					if ( symbol.token == Token.SEMICOLON )
						next();
				}


				//boolean thereWasCompilationError = false;
				if ( symbol.token != Token.RIGHTCB ) {
					try {
						statList.add(lastStatement = statement());
					}
					catch ( CompileErrorException e ) {
						//thereWasCompilationError = true;
						skipTo( Token.SEMICOLON, Token.END, Token.PUBLIC,
								Token.PRIVATE, Token.PROTECTED, Token.FUNC, Token.OVERLOAD );
						lastStatement = new StatementNull(symbol);
						if ( symbol.token == Token.SEMICOLON )
							next();
						else if ( ! startExpr(symbol)) {
							return new StatementList(statList);
						}
					}
					/*
					if ( ! thereWasCompilationError && symbol.token != Token.SEMICOLON &&
						   symbol.token != Token.RIGHTCB ) {
							previousSym = previousSymbol();
							this.warning(previousSym, "; expected");

							if ( ask(symbol, "Should I insert ';' after " + previousSym.getSymbolString() +
							          " ? (y, n)") )
								compilationUnit.addAction(
										new ActionInsert(";", compilationUnit,
						    					previousSym.getOffset() + previousSym.getSymbolString().length(),
												previousSym.getLineNumber(), previousSym.getColumnNumber()));
						} */
				}
			}
		}
		if ( this.compilationStep == CompilationStep.step_1 && statList.size() > 0 && this.cyanMetaobjectContextStack.empty() ) {
			int columnNumber = statList.get(0).getFirstSymbol().getColumnNumber();
			int lineNumber = statList.get(0).getFirstSymbol().getLineNumber();
			for ( Statement s : statList ) {
				if ( s instanceof ast.StatementMetaobjectAnnotation ) {
					/**
					 * metaobjects CyanMetaobjectCompilationContextPush are allowed to be non-indented
					 */
					StatementMetaobjectAnnotation annotation = (StatementMetaobjectAnnotation ) s;
					if ( annotation.getMetaobjectAnnotation().getCyanMetaobject() instanceof meta.cyanLang.CyanMetaobjectCompilationContextPush )
						continue;
				}
				/*
				 * check whether the statements of statList are aligned (with correct indentation).
				 */
				Symbol firstSymbol = s.getFirstSymbol();
				if ( firstSymbol.getColumnNumber() != columnNumber && firstSymbol.getLineNumber() != lineNumber  &&
				     ! s.getProducedByMetaobjectAnnotation()
						) {
					columnNumber = statList.get(0).getFirstSymbol().getColumnNumber();
					this.error2(s.getFirstSymbol(), "This statement is not correctly indented. It should be in column '" + columnNumber +
							"' but it is in column '" + s.getFirstSymbol().getColumnNumber() + "'. Check if the problem was caused by tab characters");
				}
				lineNumber = firstSymbol.getLineNumber();
			}
		}
		int i = 0;
		for ( Statement s : statList ) {
			if ( s instanceof StatementReturn ) {
				if ( i != statList.size() - 1 ) {
					boolean foundNonNullStatement = false;
					for (int j = i+1; j < statList.size(); ++j )
						if ( !( statList.get(j) instanceof StatementNull) )
							foundNonNullStatement = true;
					if ( foundNonNullStatement )
						this.error(true, statList.get(i+1).getFirstSymbol(), "Unreachable code",
							statList.get(i+1).getFirstSymbol().getSymbolString(), ErrorKind.unreachable_code);
				}
			}
			++i;
		}
		return new StatementList(statList);
	}


	public Statement statement() {

		Statement ret = null;

		startSymbolCurrentStatement = symbol;

		switch ( symbol.token ) {
		case BREAK:
			if ( whileForCount <= 0 ) {
				try {
					this.error2(symbol, "'break' outside any 'while' or 'for' command");
				}
				catch ( error.CompileErrorException e ) {
				}
			}
			Symbol breakSymbol = symbol;
			next();
			ret = new StatementBreak(breakSymbol);
			break;
		case PLUSPLUS:
			Symbol plusPlus = symbol;
			next();
			Expr idExpr = expr();
			if ( ! (idExpr instanceof ExprIdentStar) || ((ExprIdentStar ) idExpr).getIdentSymbolArray().size() != 1 ) {
				this.error2(symbol, "'++' is an operator that can only be applied to identifiers (instance variables, variables)");
			}
			ExprIdentStar id = (ExprIdentStar ) idExpr;
			ret = new StatementPlusPlusIdent(plusPlus, id);
			break;
		case MINUSMINUS:
			Symbol minusMinus = symbol;
			next();
			Expr idExpr2 = expr();
			if ( ! (idExpr2 instanceof ExprIdentStar) || ((ExprIdentStar ) idExpr2).getIdentSymbolArray().size() != 1 ) {
				this.error2(symbol, "'++' is an operator that can only be applied to identifiers (instance variables, variables)");
			}
			ExprIdentStar id2 = (ExprIdentStar ) idExpr2;
			ret = new StatementMinusMinusIdent(minusMinus, id2);
			break;
		case VAR:
			ret =  localVariableDec(false);
			break;
		case LET:
			ret =  localVariableDec(true);
			break;
		case RETURN:
			ret = returnStatement();
			break;
		case RETURN_FUNCTION:
			if ( functionStack.isEmpty() )
				ret = returnStatement();
			else
				ret = returnFunctionStatement();
			break;
		case METAOBJECT_ANNOTATION:

			String metaobjectName = symbol.getSymbolString();
			CyanMetaobjectWithAt cyanMetaobject = this.compilationUnit.getMetaObjectTable().get(metaobjectName);

			try {
				if ( cyanMetaobject == null ) {
					error(true,  symbol, "Metaobject " + metaobjectName + " was not found",
							metaobjectName, ErrorKind.metaobject_was_not_found);
					return null;
				}
			}
			catch ( RuntimeException e ) {
				if ( cyanMetaobject != null ) {
					e.printStackTrace();
					error2(symbol, "Metaobject '" + cyanMetaobject.getName() + "' " +
							" has thrown exception '" + e.getClass().getName() + "'");
				}
				throw e;
			}

			if ( cyanMetaobject.isExpression() || cyanMetaobject.getName().equals(NameServer.pushCompilationContextName) ) {

				// metaobject is inside an expression. Maybe it is alone an expression
				ret = expr();
			}
			else {

				// metaobject has no type, it is a statement

				CyanMetaobjectWithAtAnnotation regularMetaobjectAnnotation = annotation(cyanMetaobject, metaobjectName, false);


				if ( cyanMetaobject.mayBeAttached(DeclarationKind.LOCAL_VAR_DEC) ) {
					StatementLocalVariableDecList localVarDecList;
					switch ( symbol.token ) {
					case VAR:
						ret =  localVarDecList = localVariableDec(false);
						localVarDecList.setBeforeMetaobjectAnnotation(regularMetaobjectAnnotation);
						regularMetaobjectAnnotation.setDeclaration(localVarDecList);
						break;
					case LET:
						ret =  localVarDecList = localVariableDec(true);
						localVarDecList.setBeforeMetaobjectAnnotation(regularMetaobjectAnnotation);
						regularMetaobjectAnnotation.setDeclaration(localVarDecList);
						break;
					default:
						ret = new StatementMetaobjectAnnotation(regularMetaobjectAnnotation);
					}
				}
				else {
					ret = new StatementMetaobjectAnnotation(regularMetaobjectAnnotation);
				}


			}
			break;
		case IF:
			StatementIf sif;
			ret = sif = ifStatement();
			sif.setShouldBeFollowedBySemicolon(false);
			/*
			if ( (sif.getFirstSymbol().getColumnNumber() == sif.getRightCBEndsIf().getColumnNumber()) ||
					(sif.getFirstSymbol().getLineNumber() == sif.getRightCBEndsIf().getLineNumber()) ||
				 (sif.getLastElse() != null && (sif.getLastElse().getColumnNumber() == sif.getRightCBEndsIf().getColumnNumber() ||
				     sif.getLastElse().getLineNumber() == sif.getRightCBEndsIf().getLineNumber()
				     )
				 )
					 ) {
				// if ( this.compilationStep == CompilationStep.step_1 )
				sif.setShouldBeFollowedBySemicolon(false);
			}
			else {
				if ( this.project.getCompilerManager().getCompilationStep() == CompilationStep.step_1 )
					error2(sif.getRightCBEndsIf(), "The '}' that closes an 'if' statement should be either in the same line as the 'if' or in the same column");
				else
					sif.setShouldBeFollowedBySemicolon(false);
			}
			*/
			break;
		case WHILE:
			StatementWhile sw = whileStatement();
			ret = sw;
			if ( (sw.getFirstSymbol().getColumnNumber() == sw.getRightCBEndsIf().getColumnNumber()) ||
					(sw.getFirstSymbol().getLineNumber() == sw.getRightCBEndsIf().getLineNumber()) ) {
				sw.setShouldBeFollowedBySemicolon(false);
			}
			else {
				if ( this.project.getCompilerManager().getCompilationStep() == CompilationStep.step_1 )
					error2(sw.getRightCBEndsIf(), "The '}' that closes a 'while' statement should be either in the same line as the 'while' or in the same column");
			}
			break;
		case FOR:
			StatementFor sf = forStatement();
			ret = sf;
			if ( (sf.getFirstSymbol().getColumnNumber() == sf.getRightCBEndsIf().getColumnNumber()) ||
					(sf.getFirstSymbol().getLineNumber() == sf.getRightCBEndsIf().getLineNumber()) ) {
				sf.setShouldBeFollowedBySemicolon(false);
			}
			else {
				if ( this.project.getCompilerManager().getCompilationStep() == CompilationStep.step_1 )
					error2(sf.getRightCBEndsIf(), "The '}' that closes a 'for' statement should be either in the same line as the 'for' or in the same column");
			}
			break;
		case TYPE:
			ret = typeStatement();
			ret.setShouldBeFollowedBySemicolon(false);
			break;
		case NOTNIL:
			ret = notNilStatement();
			ret.setShouldBeFollowedBySemicolon(false);
			break;
		case SEMICOLON:
			Symbol s = symbol;
			next();
			ret = new StatementNull(s);
			break;
		default:

			if ( symbol.token == Token.IDENT ) {
			    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(symbol.getSymbolString());
				if ( cyanMacro != null ) {

					if ( ! compInstSet.contains(saci.CompilationInstruction.dpa_actions) ) {
						/*
						 * found a macro in a compiler step that does not allow macros. See the Figure
						 * in Chapter "Metaobjects" of the Cyan manual
						 */
						this.error(true, symbol, "macro call in a compiler step that does not allow macros",
								symbol.getSymbolString(), ErrorKind.dpa_compilation_phase_literal_objects_and_macros_are_not_allowed);
						ret = new ExprNonExpression();
					}
					else {
						ret = macroCall(cyanMacro, false);
					}
				}
				else {
					// just to improve error messages
					String strId = symbol.getSymbolString();
					if ( NameServer.isBasicType(strId) || Character.isUpperCase(strId.charAt(0)) ) {
						this.mayBeWrongVarDeclaration = true;
					}
				}
			}
			if ( ret == null ) {
				ret = exprAssign();
				this.mayBeWrongVarDeclaration = false;
			}
		}
		ret.setLastSymbol(previousSymbol);
		startSymbolCurrentStatement = null;

		if ( symbol.token == Token.METAOBJECT_ANNOTATION ) {
			if ( symbol.getSymbolString().equals(NameServer.popCompilationContextName) ) {
				if ( ret.getAfterMetaobjectAnnotation() == null ) {
					CyanMetaobjectWithAtAnnotation annotation = annotation(false);
					ret.setAfterMetaobjectAnnotation(annotation);
				}
			}
		}

		ret.setSymbolAfter(symbol);

		ret.setProducedByMetaobjectAnnotation( ! this.cyanMetaobjectContextStack.empty() );
		return ret;
	}

	/**
	 *
	   @param cyanMacro
	   @param inExpr, true if the macro is being called inside an expression
	   @return
	 */
	private CyanMetaobjectMacroCall macroCall(CyanMetaobjectMacro cyanMacro, boolean inExpr) {

		Symbol startSymbol = symbol;
		CompilerMacro_dpa compilerMacro_dpa = new CompilerMacro_dpa(this);
		// CompilationUnit compilationUnit, ProgramUnit programUnit, Symbol firstSymbol
		cyanMacro = cyanMacro.clone();
		CyanMetaobjectMacroCall cyanMetaobjectMacroCall = new CyanMetaobjectMacroCall(cyanMacro,
				this.compilationUnit, this.currentProgramUnit, startSymbol, inExpr);
		compilerMacro_dpa.setCyanMetaobjectMacro(cyanMetaobjectMacroCall);

		cyanMacro.setMetaobjectAnnotation(cyanMetaobjectMacroCall);



		try {
			cyanMacro.dpa_parseMacro(compilerMacro_dpa);
		}
		catch ( error.CompileErrorException e ) {
			throw e;
		}
		catch ( RuntimeException e ) {
			thrownException(cyanMetaobjectMacroCall, cyanMetaobjectMacroCall.getFirstSymbol(), e);
		}
		finally {
			this.metaobjectError(cyanMacro,  cyanMetaobjectMacroCall);
		}


		cyanMetaobjectMacroCall.setExprStatList(compilerMacro_dpa.getExprStatList());

		cyanMetaobjectMacroCall.setLastSymbolMacroCall(compilerMacro_dpa.getLastSymbol());

		this.copyLexerData(compilerMacro_dpa.compiler);
		cyanMetaobjectMacroCall.setNextSymbol(symbol);
		return cyanMetaobjectMacroCall;
	}



	private StatementNotNil notNilStatement() {

		/*
		 StatementNotNil(Symbol notNilSymbol, ArrayList<Symbol> notNilVariableSymbolList, ArrayList<Expr> notNilExprList,
					StatementList notNilStatementList,
		            ArrayList<StatementList> ifStatementList,
                 StatementList elseStatementList, Symbol rightCBEndsIf, Symbol lastElse) */
		Symbol notNilSymbol = symbol;
		StatementList notNilStatementList = null;
        StatementList elseStatementList = null;
        Symbol rightCBEndsIf = null, lastElse = null;
        ArrayList<NotNilRecord> notNilRecordList = new ArrayList<>();

		next();


		int numberOfLocalVariables = this.localVariableDecStack.size();
		int sizeLocalVariableDecList = -1;

		SymbolIdent variableSymbol;
		Expr typeInDec = type();
		while ( true ) {

			if ( symbol.token == Token.IDENT ) {
				variableSymbol = (SymbolIdent ) symbol;

				if ( Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
					this.error2(variableSymbol, "Variables cannot start with an uppercase letter");

				next();
			}
			else {
				/*
				 * no type at the declaration
				 */
				if ( !(typeInDec instanceof ExprIdentStar)  ) {
					error2(symbol, "Variable name expected." + foundSuch());
				}
				if ( ((ExprIdentStar ) typeInDec).getIdentSymbolArray().size() > 1 )
					error2(typeInDec.getFirstSymbol(), "Identifier expected." + foundSuch());
				Symbol ident = ((ExprIdentStar ) typeInDec).getIdentSymbolArray().get(0);
				if ( ! (ident instanceof SymbolIdent) ) {
					if ( symbol instanceof lexer.SymbolKeyword )
						error2(ident, "Keyword '" + symbol.getSymbolString() + "' used as an Identifier");
					else
						error2(ident, "Identifier expected." + foundSuch());
				}
				typeInDec = null;
				variableSymbol = (SymbolIdent ) ident;
			}

			// ****************

			// ***********************************

			if ( symbol.token != Token.ASSIGN ) {
				error2(symbol, "'=' expected." + foundSuch());
			}
			next();
			Expr e =  expr();

			StatementLocalVariableDec localVariableDec = new StatementLocalVariableDec( variableSymbol, typeInDec, e,
	                currentMethod, functionStack.size() + 1, true );

		    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(variableSymbol.getSymbolString());
		    if ( cyanMacro != null ) {
		    	this.error2(variableSymbol, "This variable has the name of a macro keyword of a macro imported by this compilation unit");
		    }

			if ( functionStack.size() > 0 ) {
				localVariableDec.setDeclaringFunction(functionStack.peek());
			}
			this.localVariableDecStack.push(localVariableDec);


			if ( ! functionStack.isEmpty() ) {
				ExprFunction currentFunction = functionStack.peek();
				sizeLocalVariableDecList = currentFunction.getSizeLocalVariableDecList();
				currentFunction.addLocalVariableDec(localVariableDec);
			}


			/*
			 * NotNilRecord(Expr typeInDec, StatementLocalVariableDec localVar, Expr expr)
			 */
		    NotNilRecord nnr = new NotNilRecord(typeInDec, localVariableDec, e);
		    notNilRecordList.add(nnr);

			if ( symbol.token != Token.COMMA )
				break;
			else {
				lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
				next();
				typeInDec = type();
			}
		}




		if ( symbol.token != Token.LEFTCB ) {
			error2(symbol, "'{' expected after if expression." + foundSuch());
		}
		else {
			next();


			notNilStatementList = statementList();

			while ( localVariableDecStack.size() > numberOfLocalVariables )
				localVariableDecStack.pop();

			if ( symbol.token != Token.RIGHTCB ) {
				this.error2(symbol, "'}' expected in an if statement." + foundSuch());
			}
			else {
				rightCBEndsIf = symbol;
				next();
			}


			if ( ! functionStack.isEmpty() && sizeLocalVariableDecList > 0 ) {
				ExprFunction currentFunction = functionStack.peek();
				currentFunction.trimLocalVariableDecListToSize(sizeLocalVariableDecList);
			}



			if ( symbol.token == Token.ELSE ) {
				lastElse = symbol;

				next();

				if ( symbol.token != Token.LEFTCB ) {
					error2(symbol, "'{' expected after if expression." + foundSuch());
				}
				else
					next();

				elseStatementList = statementList();
				if ( symbol.token != Token.RIGHTCB ) {
					error2(symbol, "'}' expected after if statements." + foundSuch());
				}
				else {
					rightCBEndsIf = symbol;
					next();
				}
			}

	     }



		/*
 	public StatementNotNil(Symbol notNilSymbol, ArrayList<SymbolIdent> notNilVariableSymbolList,
						StatementList notNilStatementList,
                        StatementList elseStatementList, Symbol rightCBEndsIf, Symbol lastElse) {
		 */
		return new StatementNotNil(notNilSymbol, notNilRecordList,
				notNilStatementList,
                elseStatementList, rightCBEndsIf, lastElse);



	}

	/**
	 *     IfStat} ::= ``if"\/ ``("\/ Expr ``)"\/ \{ StatementList \} \\
\rr \{ ``else"\/ ``if"\/ ``("\/ Expr ``)"\/ \{ StatementList \} \}\\
\rr [ ``else"\/ \{ StatementList \} ]



	 * @return
	 */
	private StatementIf ifStatement() {
		StatementList thenStatementList, elseStatementList = null;

		ArrayList<Expr> ifExprList = new ArrayList<Expr>();
		ArrayList<StatementList> ifStatementList = new ArrayList<StatementList>();
		Symbol rightCBEndsIf = null;
		Symbol previousRightCBEndsIf = null;
		Symbol lastElse = null;
		int lineIf = -1;
		int columnIf = -1;
		int lineElse = -1;
		int columnElse = -1;

		boolean firstCompilationStep = this.compilationStep == CompilationStep.step_1;

		Symbol ifSymbol = symbol;
		while ( symbol.token == Token.IF ) {
			lineIf = symbol.getLineNumber();
			columnIf = symbol.getColumnNumber();
			next();

			Expr booleanExpr = expr();
			ifExprList.add(booleanExpr);

			if ( symbol.token != Token.LEFTCB ) {
				error2(symbol, "'{' expected after if expression." + foundSuch());
			}
			else {
				if ( firstCompilationStep && columnIf > symbol.getColumnNumber() ) {
					error2(symbol, "The column of '{' that follows an 'if' statement should be in a column greater than the 'if' keyword column");
				}
				next();

				if ( firstCompilationStep && columnIf > booleanExpr.getFirstSymbol().getColumnNumber() ) {
					error2(symbol, "The first symbol of the expression that follows the 'if' statement should be in a column greater that the 'if' keyword column");
				}


				int numberOfLocalVariables = this.localVariableDecStack.size();

				++nestedIfWhile;
				thenStatementList = statementList();
				--nestedIfWhile;

				while ( localVariableDecStack.size() > numberOfLocalVariables )
					localVariableDecStack.pop();

				ifStatementList.add(thenStatementList);
				if ( symbol.token != Token.RIGHTCB ) {
					this.error2(symbol, "'}' expected in an if statement." + foundSuch());
					return null;
				}
				else {
					previousRightCBEndsIf = rightCBEndsIf;
					rightCBEndsIf = symbol;
					if ( firstCompilationStep && previousRightCBEndsIf != null ) {
						int line = symbol.getLineNumber();
						int column = symbol.getColumnNumber();
						if ( lineIf != line && columnIf != column &&
							 lineElse != line && columnElse != column ) {
							if ( previousRightCBEndsIf.getColumnNumber() != column && previousRightCBEndsIf.getLineNumber() != line )
								error2(symbol, "The '}' that closes the 'if' statements should be in the same line or same column as the 'if' or 'else' keywords");
						}
					}

					next();
				}
				if ( symbol.token != Token.ELSE )
				    break;
				else {
					lastElse = symbol;

					if ( firstCompilationStep ) {
						int line = symbol.getLineNumber();
						int column = symbol.getColumnNumber();
						if ( lineIf != line && columnIf != column &&
							 lineElse != line && columnElse != column ) {
							if ( columnIf != rightCBEndsIf.getColumnNumber() || line != rightCBEndsIf.getLineNumber() ) {
								error2(symbol, "'else' must be in the same line or in the same column as the previous 'if' or 'else' keywords");
							}
						}
						lineElse = line;
						columnElse = column;
					}

					next();
					if ( symbol.token != Token.IF ) {
						if ( symbol.token != Token.LEFTCB ) {
							error2(symbol, "'{' expected after if expression." + foundSuch());
						}
						else
							next();

						numberOfLocalVariables = this.localVariableDecStack.size();

						++nestedIfWhile;
						elseStatementList = statementList();
						--nestedIfWhile;

						while ( localVariableDecStack.size() > numberOfLocalVariables )
							localVariableDecStack.pop();

						if ( symbol.token != Token.RIGHTCB ) {
							error2(symbol, "'}' expected after if statements." + foundSuch());
						}
						else {
							previousRightCBEndsIf = rightCBEndsIf;
							rightCBEndsIf = symbol;
							if ( firstCompilationStep && lineElse != symbol.getLineNumber() && columnElse != symbol.getColumnNumber() ) {
								if ( previousRightCBEndsIf.getColumnNumber() != rightCBEndsIf.getColumnNumber() ||
										previousRightCBEndsIf.getLineNumber() != lineElse ) {
									error2(symbol, "The '}' that closes the 'else' statements should be in the same line or same column as the 'else' keyword");
								}
							}

							next();
						}

						break;
					}

				}

		     }
		}
		return new StatementIf(ifSymbol, ifExprList, ifStatementList, elseStatementList, rightCBEndsIf, lastElse);

	}

	private StatementWhile whileStatement() {
		StatementList statementList = null;
		Symbol rightCBEndsIf = null;

		Symbol whileSymbol = symbol;
		next();
		Expr booleanExpr = expr();

		if ( symbol.token != Token.LEFTCB ) {
			error2(symbol, "'{' expected after while expression." + foundSuch());
		}
		else {

			next();

			int numberOfLocalVariables = this.localVariableDecStack.size();

			++nestedIfWhile;
			++whileForCount;
			statementList = statementList();
			--whileForCount;
			--nestedIfWhile;

			while ( localVariableDecStack.size() > numberOfLocalVariables )
				localVariableDecStack.pop();

			if ( symbol.token != Token.RIGHTCB ) {
				error2(symbol, "'}' expected at the end of a while statement." + foundSuch());
			}
			else {
				rightCBEndsIf = symbol;
				next();
			}

		}
		return new StatementWhile(whileSymbol,
				      booleanExpr, statementList, rightCBEndsIf);
	}

	private StatementFor forStatement() {
		StatementList statementList = null;
		Symbol forSymbol = symbol;
		Symbol rightCBEndsIf = null;

		next();

		/*
		Expr typeInDec = type();
		while ( true ) {

			if ( symbol.token != Token.COMMA )
				break;
			else {
				lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
				next();
				if ( typeInDec != null )
					lastTypeInDec = typeInDec;
				typeInDec = type();
			}
		}
				 *
		 */
		Expr typeInDec = type();
		ExprIdentStar id;
		SymbolIdent variableSymbol;

		if ( symbol.token == Token.IDENT ) {
			variableSymbol = (SymbolIdent ) symbol;
			id = this.ident();
			if ( id.getIdentSymbolArray().size() != 1 ) {
				this.error2(id.getFirstSymbol(),  "An identifier was expected");
			}

			if ( Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
				this.error2(variableSymbol, "Variables cannot start with an uppercase letter");

		}
		else {
			if ( !(typeInDec instanceof ExprIdentStar)  ) {
				error2(symbol, "Variable name expected." + foundSuch());
			}
			if ( ((ExprIdentStar ) typeInDec).getIdentSymbolArray().size() > 1 )
				error2(typeInDec.getFirstSymbol(), "Identifier expected." + foundSuch());
			Symbol ident = ((ExprIdentStar ) typeInDec).getIdentSymbolArray().get(0);
			if ( ! (ident instanceof SymbolIdent) ) {
				if ( symbol instanceof lexer.SymbolKeyword )
					error2(ident, "Keyword '" + symbol.getSymbolString() + "' used as an Identifier");
				else
					error2(ident, "Identifier expected." + foundSuch());
			}
			typeInDec = null;
			variableSymbol = (SymbolIdent ) ident;
		}




		if ( symbol.token != Token.IN ) {
			error2(symbol, "'in' expected");
		}
		next();
		Expr forExpr = expr();


		StatementLocalVariableDec localVariableDec = new StatementLocalVariableDec( variableSymbol, null, null,
                currentMethod, functionStack.size() + 1, true );

	    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(variableSymbol.getSymbolString());
	    if ( cyanMacro != null ) {
	    	this.error2(variableSymbol, "This variable has the name of a macro keyword of a macro imported by this compilation unit");
	    }



		if ( functionStack.size() > 0 ) {
			localVariableDec.setDeclaringFunction(functionStack.peek());
		}
		this.localVariableDecStack.push(localVariableDec);

		int sizeLocalVariableDecList = -1;

		if ( ! functionStack.isEmpty() ) {
			ExprFunction currentFunction = functionStack.peek();
			sizeLocalVariableDecList = currentFunction.getSizeLocalVariableDecList();
			currentFunction.addLocalVariableDec(localVariableDec);
		}

		//#
		int numberOfLocalVariables = this.localVariableDecStack.size();

		if ( symbol.token != Token.LEFTCB ) {
			error2(symbol, "'{' expected after 'for' expression." + foundSuch());
		}
		else {
			next();
			++nestedIfWhile;
			++whileForCount;



			statementList = statementList();
			--whileForCount;
			--nestedIfWhile;
			if ( symbol.token != Token.RIGHTCB ) {
				error2(symbol, "'}' expected at the end of a 'for' statement." + foundSuch());
			}
			else {
				rightCBEndsIf = symbol;
				next();
			}
			//#
			while ( localVariableDecStack.size() > numberOfLocalVariables )
				localVariableDecStack.pop();
			this.localVariableDecStack.pop();

			if ( ! functionStack.isEmpty() && sizeLocalVariableDecList > 0 ) {
				ExprFunction currentFunction = functionStack.peek();
				currentFunction.trimLocalVariableDecListToSize(sizeLocalVariableDecList);
			}


		}

		return new StatementFor(forSymbol, typeInDec, localVariableDec, forExpr, statementList, rightCBEndsIf);
	}

	/*
	 *   type str
            case String str2 {
               str2[0] println
            }
            case Nil nil2 {
            }

	 */
	private StatementType typeStatement() {

		Symbol rightCBEndsIf = null;

		Symbol typeSymbol = symbol;
		next();
		Expr expr = expr();
		StatementType statementType = new StatementType(typeSymbol, expr);
		if ( this.symbol.token != Token.CASE ) {
			this.error2(this.symbol, "'case' expected after the expression of 'type'");
		}
		while ( symbol.token == Token.CASE ) {
			Symbol caseSymbol = symbol;
			next();

			StatementList statementList = null;

			Expr caseExprType = type();

			Symbol idSymbol = null;
			StatementLocalVariableDec localVariableDec = null;
			if ( symbol.token == Token.IDENT ) {
				idSymbol = symbol;
				next();
				localVariableDec = new StatementLocalVariableDec( (SymbolIdent ) idSymbol, null, null,
		                currentMethod, functionStack.size() + 1, true );

			    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(idSymbol.getSymbolString());
			    if ( cyanMacro != null ) {
			    	this.error2(idSymbol, "This variable has the name of a macro keyword of a macro imported by this compilation unit");
			    }

				if ( functionStack.size() > 0 ) {
					localVariableDec.setDeclaringFunction(functionStack.peek());
				}
				this.localVariableDecStack.push(localVariableDec);

			}




			int sizeLocalVariableDecList = -1;

			if ( ! functionStack.isEmpty() ) {
				ExprFunction currentFunction = functionStack.peek();
				sizeLocalVariableDecList = currentFunction.getSizeLocalVariableDecList();
				if ( localVariableDec != null ) {
					currentFunction.addLocalVariableDec(localVariableDec);
				}
			}

			//#
			int numberOfLocalVariables = this.localVariableDecStack.size();

			if ( symbol.token != Token.LEFTCB ) {
				error2(symbol, "'{' expected after 'case Type variable' ." + foundSuch());
			}
			else {
				next();


				statementList = statementList();
				if ( symbol.token != Token.RIGHTCB ) {
					error2(symbol, "'}' expected at the end of a 'type-case' statement." + foundSuch());
				}
				else {
					rightCBEndsIf = symbol;
					next();
				}
				//#
				while ( localVariableDecStack.size() > numberOfLocalVariables )
					localVariableDecStack.pop();
				if ( idSymbol != null ) {
					this.localVariableDecStack.pop();
				}

				if ( ! functionStack.isEmpty() && sizeLocalVariableDecList > 0 ) {
					ExprFunction currentFunction = functionStack.peek();
					currentFunction.trimLocalVariableDecListToSize(sizeLocalVariableDecList);
				}

				statementType.addCaseRecord( new CaseRecord(caseSymbol, caseExprType, localVariableDec, statementList, rightCBEndsIf) );

			}

		}
		StatementList elseStatementList = null;
		if ( symbol.token == Token.ELSE ) {
			next();

			int sizeLocalVariableDecList = -1;
			if ( ! functionStack.isEmpty() ) {
				ExprFunction currentFunction = functionStack.peek();
				sizeLocalVariableDecList = currentFunction.getSizeLocalVariableDecList();
			}

			int numberOfLocalVariables = this.localVariableDecStack.size();

			if ( symbol.token != Token.LEFTCB ) {
				error2(symbol, "'{' expected after 'else'." + foundSuch());
			}
			else {
				next();


				elseStatementList = statementList();
				if ( symbol.token != Token.RIGHTCB ) {
					error2(symbol, "'}' expected at the end of a 'type-case-else' statement." + foundSuch());
				}
				else {
					rightCBEndsIf = symbol;
					next();
				}
				//#
				while ( localVariableDecStack.size() > numberOfLocalVariables )
					localVariableDecStack.pop();

				if ( ! functionStack.isEmpty() && sizeLocalVariableDecList > 0 ) {
					ExprFunction currentFunction = functionStack.peek();
					currentFunction.trimLocalVariableDecListToSize(sizeLocalVariableDecList);
				}

			}
			statementType.setElseStatementList(elseStatementList);
		}
		return statementType;
	}



	/**
	 * VariableDec ::=  [ ``var"\/ ] [ Type ] Id [ ``="\/ Expr ] \{ ``,"\/  [ Type ] Id   [ ``="\/ Expr ] \} [ ``;"\/ ]

	 * @return
	 */
	private StatementLocalVariableDecList localVariableDec(boolean isReadonly) {
		Expr typeInDec = null;
		StatementLocalVariableDecList localVariableDecList = new StatementLocalVariableDecList(symbol);
		SymbolIdent variableSymbol;
		next();

		/*
		CyanMetaobjectWithAtAnnotation annotation = null;
		if ( symbol.token == Token.annotation ) {
			annotation = annotation(false);
			if ( ! annotation.getCyanMetaobject().mayBeAttached(DeclarationKind.LOCAL_VAR_DEC) ) {
				this.error2(annotation.getFirstSymbol(), "Metaobject '" + annotation.getCyanMetaobject().getName()
						+ "' cannot be attached to declaration of variables");
			}
		}

		*/

		Expr lastTypeInDec = null;
		typeInDec = type();
		while ( true ) {

			if ( symbol.token == Token.IDENT ) {
				variableSymbol = (SymbolIdent ) symbol;

				if ( Character.isUpperCase(variableSymbol.getSymbolString().charAt(0)) )
					this.error2(variableSymbol, "Variables cannot start with an uppercase letter");

				next();
				localVariableDecList.add(singleLocalVariableDec(typeInDec, variableSymbol, isReadonly));
			}
			else {
				/*
				 * no type at the declaration
				 */
				if ( !(typeInDec instanceof ExprIdentStar)  ) {
					error2(symbol, "Variable name expected." + foundSuch());
				}
				if ( ((ExprIdentStar ) typeInDec).getIdentSymbolArray().size() > 1 )
					error2(typeInDec.getFirstSymbol(), "Identifier expected." + foundSuch());
				Symbol ident = ((ExprIdentStar ) typeInDec).getIdentSymbolArray().get(0);
				if ( ! (ident instanceof SymbolIdent) ) {
					if ( symbol instanceof lexer.SymbolKeyword )
						error2(ident, "Keyword '" + symbol.getSymbolString() + "' used as an Identifier");
					else
						error2(ident, "Identifier expected." + foundSuch());
				}
				typeInDec = null;
				variableSymbol = (SymbolIdent ) ident;
				localVariableDecList.add(singleLocalVariableDec(lastTypeInDec, variableSymbol, isReadonly));
			}
			if ( symbol.token != Token.COMMA )
				break;
			else {
				lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
				next();
				if ( typeInDec != null )
					lastTypeInDec = typeInDec;
				typeInDec = type();
			}
		}

		if ( ! functionStack.isEmpty() ) {
			ExprFunction currentFunction = functionStack.peek();
			for ( StatementLocalVariableDec localDec : localVariableDecList.getLocalVariableDecList() ) {
				currentFunction.addLocalVariableDec(localDec);
			}
		}

		if ( localVariableDecList.getLocalVariableDecList().size() > 1 ) {
			for ( StatementLocalVariableDec varDec : localVariableDecList.getLocalVariableDecList() ) {
				if ( varDec.getExpr() != null ) {
					this.error2(varDec.getFirstSymbol(), "A declaration of several variables with a single type with one of the "
						+ "variables receiving a value. This is illegal. Put the variable or variables that receive a value in a separate declaration");
				}
			}

		}
		/*
		if ( annotation != null ) {
			localVariableDecList.setBeforeMetaobjectAnnotation(annotation);
			annotation.setDeclaration(localVariableDecList);
		}
		*/
		return localVariableDecList;
	}

	/**
	 * [ ``="\/ Expr ]
	   @param typeInDec
	   @param variableSymbol
	   @return
	 */
	private StatementLocalVariableDec singleLocalVariableDec(Expr typeInDec, SymbolIdent variableSymbol, boolean isReadonly) {
		Expr expr;

		if (symbol.token == Token.ASSIGN ) {
			next();
			expr = expr();
		}
		else {
			if ( isReadonly ) {
				error2(variableSymbol, "A read-only variable should be followed by '= expr'");
			}
			expr = null;
		}

		if ( equalToFormalGenericParameter(variableSymbol.getSymbolString()) ) {
			error2(variableSymbol, "Local variable names cannot be equal to one of the formal parameters of the generic prototype");
		}


		StatementLocalVariableDec localVariableDec = new StatementLocalVariableDec( variableSymbol, typeInDec, expr,
				                                        currentMethod, functionStack.size() + 1, isReadonly );

	    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(variableSymbol.getSymbolString());
	    if ( cyanMacro != null ) {
	    	this.error2(variableSymbol, "This variable has the name of a macro keyword of a macro imported by this compilation unit");
	    }



		if ( functionStack.size() > 0 ) {
			localVariableDec.setDeclaringFunction(functionStack.peek());
		}
		this.localVariableDecStack.push(localVariableDec);
		return localVariableDec;
	}


	private StatementReturn returnStatement() {

		Symbol returnSymbol = symbol;
		next();

		Expr returnedExpr = expr();
		StatementReturn statementReturn= new StatementReturn(returnSymbol, returnedExpr, currentMethod);
		if ( ! functionStack.isEmpty() ) {
			this.error2(returnSymbol, "Currently return statements inside anonymous function are not allowed");
			/*
			functionStack.peek().addStatementReturn(statementReturn);
			for ( ExprFunction ef : functionStack ) {
				ef.setHasMethodReturnStatement(true);
			}
			*/
		}

		return statementReturn;
	}

	private StatementReturnFunction returnFunctionStatement() {
		Symbol returnSymbol = symbol;


		next();
		Expr returnedExpr = expr();
		if ( functionStack.size() == 0 && nestedIfWhile > 0 )
			this.error(true,
					returnSymbol,
					"Return of a function with '^ " + returnedExpr.asString() +
					"'. But this statement is not inside a function. This statement cannot be used as in 'if i < 10 { ^ 0 }'. The same applies to the 'while' statement", null, ErrorKind.return_with_caret_outside_a_function);

		StatementReturnFunction statementReturnFunction = new StatementReturnFunction(returnSymbol, returnedExpr, functionStack.peek());
		functionStack.peek().addStatementReturnFunction(statementReturnFunction);
		return statementReturnFunction;
	}


	/*  ExprAssign ::= Expr [ Assign ]
	 *  Expr ::= OrExpr [ MessageSendNonUnary ]  | MessageSendNonUnary
	 *  Assign     ::= { "," OrExpr }  "=" OrExpr
	 */
	/**
	 * analyzes an expression or an assignment. The return value is of type
	 * Statement but it may also be of type Expr, subtype of Statement
	   @return
	 */
	private Statement exprAssign() {

		Expr e = expr();
		StatementAssignmentList assignmentList = null;
		Statement stat;

		if ( symbol.token != Token.COMMA && symbol.token != Token.ASSIGN )
		    stat = e;
		else {
			checkIfAssignable(e);
			assignmentList = new StatementAssignmentList();
			assignmentList.add(e);
			if ( symbol.token == Token.COMMA ) {
				while ( symbol.token == Token.COMMA ) {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
					next();
					e = expr();
					checkIfAssignable(e);
					assignmentList.add(e);
				}
			}
			if ( symbol.token != Token.ASSIGN )
				error2(symbol, "'=' expected after a list of expressions separated by commas." + foundSuch());
			for ( Expr leftExpr : assignmentList.getExprList() ) {
				if  ( leftExpr instanceof ExprIndexed ) {
					((ExprIndexed ) leftExpr).setLeftHandSideAssignment(true);
				}
			}
			next();
			assignmentList.add(expr());
			stat = assignmentList;
		}
		if ( symbol.token == Token.METAOBJECT_ANNOTATION ) {
			if ( symbol.getSymbolString().equals(NameServer.popCompilationContextName) ) {
				if ( stat.getAfterMetaobjectAnnotation() == null ) {
					CyanMetaobjectWithAtAnnotation annotation = annotation(false);
					stat.setAfterMetaobjectAnnotation(annotation);
				}
			}
		}

		return stat;
	}

	private void checkIfAssignable(Expr e) {
		if ( ! (e instanceof ExprIdentStar) && ! (e instanceof ExprIndexed) && ! (e instanceof ExprSelfPeriodIdent)
				&& !(e instanceof ExprSelf__PeriodIdent) ) {
			if ( symbol.token == Token.COMMA )
				error2(symbol, "This seems a variable declaration. But there is a 'var' or 'let' keyword missing");
			else {
				warning(symbol, symbol.getLineNumber(), "Expression on the left is not assignable. If this is a variable declaration, you are forgetting to put keyword 'var' before it.");
				Symbol firstSymbol = e.getFirstSymbol();
				if ( ask(symbol, "Should I insert 'var' before " + firstSymbol.getSymbolString() +	" ? (y, n)") )
					compilationUnit.addAction(
							new ActionInsert("var ", compilationUnit,
									firstSymbol.startOffsetLine + firstSymbol.getColumnNumber() - 1,
									firstSymbol.getLineNumber(), firstSymbol.getColumnNumber()));

			}
		}
	}

	public Expr expr() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprXor();


		while ( symbol.token == Token.OR ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprXor();
			leftExpr = new ExprBooleanOr(leftExpr, symbolOperator, rightExpr);

		}
		return leftExpr;
	}

	private Expr exprXor() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprAnd();
		while ( symbol.token == Token.XOR ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			// leftExpr = new ExprXor(leftExpr, symbolOperator, exprAnd(firstExpr));
			Expr rightExpr = exprAnd();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}

	private Expr exprAnd() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprEqGt();
		while ( symbol.token == Token.AND ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprEqGt();
			leftExpr = new ExprBooleanAnd(leftExpr, symbolOperator, rightExpr);

		}
		return leftExpr;
	}



	private Expr exprEqGt() {
		Expr leftExpr;
		SymbolOperator symbolOperator;


		leftExpr = exprBinExclamation();
		while ( symbol.token == Token.EQGT || symbol.token == Token.EQEQGT ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprBinExclamation();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}


	private Expr exprBinExclamation() {

		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprRel();
		while ( symbol.token == Token.NOT ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprRel();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;

	}


	private Expr exprRel() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprMSNonUnary();
		if ( symbol.token == Token.LT_NOT_PREC_SPACE ) {
			//if ( ask(symbol, "Should I insert space before '<' ? (y, n)") ) {
						/*compilationUnit.addAction(
								new ActionInsert(" ", compilationUnit,
									symbol.startLine + symbol.getColumnNumber() - 1,
									symbol.getLineNumber(), symbol.getColumnNumber()));  */
				symbol.token = Token.LT;
			//}

		}
		/*
		if ( symbol.token == Token.LT && ! Lexer.hasSpaceAfter(symbol, compilationUnit) )
			compilationUnit.addAction(
					new ActionInsert(" ", compilationUnit,
						symbol.startLine + symbol.getColumnNumber() + symbol.getSymbolString().length() - 1,
						symbol.getLineNumber(), symbol.getColumnNumber()));


		if ( symbol.token == Token.GT && ! Lexer.hasSpaceAfter(symbol, compilationUnit) )
			compilationUnit.addAction(
					new ActionInsert(" ", compilationUnit,
						symbol.startLine + symbol.getColumnNumber() + symbol.getSymbolString().length() - 1,
						symbol.getLineNumber(), symbol.getColumnNumber()));


		if ( symbol.token == Token.GT && ! Lexer.hasSpaceBefore(symbol, compilationUnit) )
			compilationUnit.addAction(
					new ActionInsert(" ", compilationUnit,
						symbol.startLine + symbol.getColumnNumber(),
						symbol.getLineNumber(), symbol.getColumnNumber()));

		*/

		if ( isRelationalOperator(symbol.token) ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			// leftExpr = new ExprRel(leftExpr, symbolOperator, exprInter(firstExpr));
			Expr rightExpr = exprMSNonUnary();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}


	public Expr exprMSNonUnary() {

		MessageWithSelectors messageWithSelectors;



		if ( symbol.token == Token.SUPER &&  canStartMessageSendNonUnary(next(0)) ) {
			Symbol superSymbol = symbol;
			next();
			messageWithSelectors = messageSendNonUnary();
			ExprMessageSendWithSelectorsToSuper msSuper = new ExprMessageSendWithSelectorsToSuper(superSymbol, messageWithSelectors, symbol);
			this.currentProgramUnit.addMessageSendWithSelectorsToSuper(msSuper);
			return msSuper;
		}
		else {
			if ( canStartMessageSendNonUnary(symbol) ||
					 (symbol.token == Token.BACKQUOTE && canStartMessageSendNonUnary(next(0)) ) ) {
					   // message send with selectors to "self"
					messageWithSelectors = messageSendNonUnary();
					return new ExprMessageSendWithSelectorsToExpr(null, messageWithSelectors, symbol);
				}
			else {
					Expr e = exprOrGt();
					if ( canStartMessageSendNonUnary(symbol) ||
							 (symbol.token == Token.BACKQUOTE && canStartMessageSendNonUnary(next(0)) )) {
						messageWithSelectors = messageSendNonUnary();
						return new ExprMessageSendWithSelectorsToExpr(e, messageWithSelectors, symbol);
					}
					return e;
			}
		}
	}


	private Expr exprOrGt() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprBinPlusPlus_MinusMinus();
		while ( symbol.token == Token.ORGT  ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			//leftExpr = new ExprAdd(leftExpr, symbolOperator, exprMult(firstExpr));
			Expr rightExpr = exprBinPlusPlus_MinusMinus();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}

	private Expr exprBinPlusPlus_MinusMinus() {
		Expr leftExpr;
		SymbolOperator symbolOperator;


		leftExpr = exprInter();
		while ( symbol.token == Token.PLUSPLUS || symbol.token == Token.MINUSMINUS) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprInter();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}


	private Expr exprInter() {
		Expr leftExpr;
		SymbolOperator symbolOperator;


		leftExpr = exprAdd();
		if ( symbol.token == Token.TWOPERIOD || symbol.token == Token.TWOPERIODLT ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			// leftExpr = new ExprRel(leftExpr, symbolOperator, exprAdd(firstExpr));
			Expr rightExpr = exprAdd();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}




	private Expr exprAdd() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprMult();
		while ( symbol.token == Token.PLUS ||
				symbol.token == Token.MINUS ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			//leftExpr = new ExprAdd(leftExpr, symbolOperator, exprMult(firstExpr));
			Expr rightExpr = exprMult();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}

	private Expr exprMult() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprBit();
		while ( symbol.token == Token.MULT || symbol.token == Token.DIV ||
				symbol.token == Token.REMAINDER ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			// leftExpr = new ExprMult(leftExpr, symbolOperator, exprBit(firstExpr));
			Expr rightExpr = exprBit();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}

	private Expr exprBit() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprShift();
		while ( symbol.token == Token.BITAND ||
				symbol.token == Token.BITOR ||
				symbol.token == Token.BITXOR
		) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprShift();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);
			/*
			switch ( symbolOperator.token ) {
			case BITAND:
				leftExpr = new ExprBitAnd(leftExpr, symbolOperator, exprShift(firstExpr));
				break;
			case BITOR:
				leftExpr = new ExprBitOr(leftExpr, symbolOperator, exprShift(firstExpr));
				break;
			case BITXOR:
				leftExpr = new ExprBitXor(leftExpr, symbolOperator, exprShift(firstExpr));
				break;
			default:
				error(symbol, "internal error at exprBit");
			}
			*/
		}
		return leftExpr;
	}

	/**
	 * \p{ExprShift} ::= ExprDotOp [ ShiftOp ExprDotOp ]

	   @return
	 */

	private Expr exprShift() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprColonColonOp();
		if ( symbol.token == Token.LEFTSHIFT ||
				symbol.token == Token.RIGHTSHIFT ||
				symbol.token == Token.RIGHTSHIFTTHREE ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprColonColonOp();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}

	/**
	 * \p{ExprColonColon} ::= ExprDotOp \{ ``::"\/ ExprDotOp \}
	 */
	private Expr exprColonColonOp() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprDotOp();
		if ( symbol.token == Token.COLONCOLON ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprDotOp();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}

	/**
	 * \p{ExprDotOp} ::= ExprUnaryUnMS \{ DotOp ExprUnaryUnMS \}

        \p{DotOp} ::= ``\verb|.*|"\/ \verb"|"  ``\verb|.+|"\/
	 */

	private Expr exprDotOp() {
		Expr leftExpr;
		SymbolOperator symbolOperator;

		leftExpr = exprUnaryUnMS2();
		if ( symbol.token == Token.DOT_STAR ||
				symbol.token == Token.DOT_PLUS ) {
			symbolOperator = (SymbolOperator) symbol;
			next();
			Expr rightExpr = exprUnaryUnMS2();
			leftExpr = new ExprMessageSendWithSelectorsToExpr(leftExpr, new MessageBinaryOperator(symbolOperator, rightExpr), symbol);

		}
		return leftExpr;
	}




	/**
	 * @param firstExpr
	 * @param mayBeMessageSendWithSelectors
	 * @return
	 */
	private Expr exprUnaryUnMS2() {


		Expr e = exprUnary();


		if ( symbol.token == Token.IDENT ||
				symbol.token == Token.INTER_ID ||
				symbol.token == Token.INTER_DOT_ID  ||
						(symbol.token == Token.BACKQUOTE && next(0).token == Token.IDENT )) {

			int numUnarySends = 0;
			if ( e instanceof ExprMessageSendUnaryChainToSuper ) {
				++numUnarySends;
			}
			ExprMessageSendUnaryChainToExpr chain = new ExprMessageSendUnaryChainToExpr(e);

			int numBackquotes = 0;
			while ( symbol.token == Token.IDENT ||
					symbol.token == Token.INTER_ID ||
					symbol.token == Token.INTER_DOT_ID  ||
					(symbol.token == Token.BACKQUOTE && next(0).token == Token.IDENT )) {


				if ( symbol.token == Token.BACKQUOTE ) {
					if ( numUnarySends != 0 ) {
						if ( numBackquotes > 0 )
							    //   receiver `s `p
						    error(true, symbol,
								"Two or more backquotes (`) in a chain of unary messages", symbol.getSymbolString(), ErrorKind.two_or_more_backquotes_in_unary_chain);
						else
							    // receiver s `p
						    error(true, symbol,
								"Illegal use of backquote. Unary message chain should have only one unary message when character ` is used", symbol.getSymbolString(), ErrorKind.illegal_use_of_backquote);

					}
					else {
						chain.setBackQuote(true);
						++numBackquotes;
						next();

						/**
						 * each function should have a list of accessed local variables and parameters
						 * of outer scope.
						 */
						/** it is a parameter, local variable, instance variable, unary method */
						String varName = symbol.getSymbolString();
						Object localVarParameter = searchIdent(varName);
						if ( localVarParameter != null )  {

							int i = functionStack.size() - 1;
							// Iterator<ExprFunction> functionIter = functionStack.iterator();
							while (  i >= 0 ) {
								ExprFunction function = functionStack.get(i);
								--i;
								if ( compilationStep == CompilationStep.step_7 &&
										function.searchLocalVariableDec(varName) == null  )  {

									if ( localVarParameter instanceof ParameterDec ) {
										function.addAccessedParameter( (ParameterDec ) localVarParameter );
										((ParameterDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
									}
									else if ( localVarParameter instanceof StatementLocalVariableDec ) {
										function.addAccessedVariableDec( (StatementLocalVariableDec ) localVarParameter );
										((StatementLocalVariableDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
									}
								}
								else
									   /*
									    * if the variable was found, it is not necessary to consider it as
									    * an external variable for this and all upper level functions
									    */
									break;
							}

						}



					}
				}
				chain.setUnarySymbol( (SymbolIdent ) symbol);
				next();
				chain.setNextSymbol(symbol);
				if ( symbol.token == Token.IDENT ||
					symbol.token == Token.INTER_ID ||
					symbol.token == Token.INTER_DOT_ID  ||
					(symbol.token == Token.BACKQUOTE && next(0).token == Token.IDENT) ) {
					chain = new ExprMessageSendUnaryChainToExpr(chain);
				}

				++numUnarySends;
			}
			e = chain;
		}

		return e;
	}


	private Expr exprUnary() {
		SymbolOperator unarySymbolOperator = null;
		Expr e;

		if ( symbol.token == Token.PLUSPLUS || symbol.token == Token.MINUSMINUS ) {
			error2(symbol, "++ and -- start a statement in Cyan. They do not return an expression and therefore cannot be used here");
		}
		if ( isUnaryOperator(symbol.token) ) {
			unarySymbolOperator = (SymbolOperator ) symbol;
			next();
		}

		e = exprPrimary();
		while ( symbol.token == Token.LEFTSB
				|| symbol.token == Token.INTER_LEFTSB ) {
			Symbol firstIndexOperator = symbol;
			next();
			Expr indexOfExpr = expr();
			if ( firstIndexOperator.token == Token.LEFTSB ) {
				if ( symbol.token != Token.RIGHTSB ) {
					error2(symbol, "']' expected after the end of an array index. " + foundSuch());
				}
			}
			else if ( firstIndexOperator.token == Token.INTER_LEFTSB ) {
				if ( symbol.token != Token.RIGHTSB_INTER ) {
					error2(symbol, "']?' expected after the end of an array index." + foundSuch());
				}
			}
			next();
			e = new ExprIndexed(e, indexOfExpr, firstIndexOperator );
		}
		if ( unarySymbolOperator != null )
			e = new ExprUnary(unarySymbolOperator, e);
		/*
		if ( mayBeMessageSendWithSelectors && canStartMessageSendNonUnary(symbol) ) {
			// this is a non-unary message send
			MessageWithSelectors messageWithSelectors = messageSendNonUnary();
			throw new FoundMessageSendExpression(new ExprMessageSendWithSelectorsToExpr(e, messageWithSelectors));
		}  */
		return e;
	}

	private Expr exprPrimary() {

		CyanMetaobjectWithAtAnnotation regularMetaobjectAnnotation;
		Symbol receiverSymbol, selfSymbol;
		switch ( symbol.token ) {
		case SELF:
			selfSymbol = symbol;
			next();
			Expr e = null;
			if ( symbol.token == Token.PERIOD ) {
				  // something like "self.x"
				next();
				if ( symbol.token != Token.IDENT ) {
					error2(symbol, "identifier expected after 'self.'" + foundSuch());
				}
				else {
					//ExprIdentStar exprIdentStar = new ExprIdentStar(symbol);
					//Iterator<ExprFunction> functionIter = functionStack.iterator();
					//while (  functionIter.hasNext() ) {
						//ExprFunction function = functionIter.next();
						//function.addPossiblyAcccessedIdentifier(exprIdentStar);
					//}
					e = new ExprSelfPeriodIdent(selfSymbol, symbol);
					next();
				}
			}
			else
				e = new ExprSelf(selfSymbol, currentProgramUnit);
			return e;

		case SUPER:
			receiverSymbol = symbol;
			next();

			if ( this.insideContextFunction() )
				error2(receiverSymbol, "'super' cannot be used inside a context function. " +
			           "A context function is an anonymous function in which the name of the first parameter is 'self'");
			if ( symbol.token == Token.IDENT ||
					symbol.token == Token.INTER_ID ||
					symbol.token == Token.INTER_DOT_ID ||
					(symbol.token == Token.BACKQUOTE && next(0).token == Token.IDENT ) ) {
				ExprMessageSendUnaryChain chain;
				chain = new ExprMessageSendUnaryChainToSuper(receiverSymbol, symbol);

				if ( symbol.token == Token.BACKQUOTE ) {
					chain.setBackQuote(true);
					next();

					/**
					 * each function should have a list of accessed local variables and parameters
					 * of outer scope.
					 */
					/** it is a parameter, local variable, instance variable, unary method */
					String varName = symbol.getSymbolString();
					Object localVarParameter = searchIdent(varName);

					if ( localVarParameter != null ) {
						int i = functionStack.size() - 1;
						// Iterator<ExprFunction> functionIter = functionStack.iterator();
						while (  i >= 0 ) {
							ExprFunction function = functionStack.get(i);
							--i;
							if ( compilationStep == CompilationStep.step_7 &&
									function.searchLocalVariableDec(varName) == null  )  {

								if ( localVarParameter instanceof ParameterDec ) {
									function.addAccessedParameter( (ParameterDec ) localVarParameter );
									((ParameterDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
								}
								else if ( localVarParameter instanceof StatementLocalVariableDec ) {
									function.addAccessedVariableDec( (StatementLocalVariableDec ) localVarParameter );
									((StatementLocalVariableDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
								}
							}
							else
								   /*
								    * if the variable was found, it is not necessary to consider it as
								    * an external variable for this and all upper level functions
								    */
								break;
						}
					}


				}
				chain.setUnarySymbol( (SymbolIdent ) symbol);
				next();

				/*while (symbol.token == Token.IDENT ||
						symbol.token == Token.INTER_ID ||
						symbol.token == Token.INTER_DOT_ID ) {
					chain.addUnarySymbol(symbol);
					next();
				} */

				return chain;
			}
			else {
				error2(receiverSymbol, "message send expected after 'super'." + foundSuch() +
						". Be aware of the precedence order. Use '(' and ')' around non-unary message sends. For example, " +
						"'1 == super m: 0' should be written '1 == (super m: 0)'");
				return null;
			}

		case IDENT:
			if ( symbol.symbolString.equals(NameServer.selfNameInnerPrototypes) ) {
				selfSymbol = symbol;
				next();
				e = null;
				if ( symbol.token == Token.PERIOD ) {
					  // something like "self__.x"
					next();
					if ( symbol.token != Token.IDENT ) {
						error2(symbol, "identifier expected after 'self__.'. " + foundSuch());
					}
					else {
						e = new ExprSelf__PeriodIdent(selfSymbol, symbol);
						next();
					}
				}
				else
					e = new ExprSelf__(selfSymbol);
				return e;
			}
			else {
				return parseIdent();
			}

		case TYPEOF:
			if ( prohibitTypeof ) {
				/*
				 * 'typeof' is not allowed inside a method signature or as a type of an instance variable
				 */
				error2(symbol, "'typeof' can only be used inside methods or in the DSL attached to metaobjects that start with @");
			}
			Symbol typeofSymbol = symbol;
			next();
			if ( symbol.token != Token.LEFTPAR )
				error2(symbol, "'(' expected after keyword Expr." + foundSuch());
			next();
			Expr exprType = expr();
			if ( symbol.token != Token.RIGHTPAR )
				error2(symbol, "')' expected after function typeof." + foundSuch());
			if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
				error2(symbol, "letter, number, or '_' after ')'");
			}
			next();
			return new ExprTypeof(typeofSymbol, exprType);

		case METAOBJECT_ANNOTATION:
			/*
			 *  The grammar for this expression may be one of the following, in which metaobjectAnnotation is a regular metaobject annotation,
			 *  it is not an annotation of markDeletedCode or pushCompilationContext.
			 *
			 * 	metaobjectAnnotation
			 * 	metaobjectAnnotation markDeletedCode pushCompilationContext code popCompilationContext
			 * 	metaobjectAnnotation pushCompilationContext code popCompilationContext
			 * 	markDeletedCode pushCompilationContext code popCompilationContext
			 * 	pushCompilationContext code popCompilationContext
			 */

			/*
			 * EXTREMELY redundant code. I know that. It will be correctly some day. Maybe never.
			 */
			regularMetaobjectAnnotation = annotation(true);

			CyanMetaobjectWithAtAnnotation nextMetaobjectAnnotation = null, markDeletedCode = null, pushAnnotation = null, popAnnotation = null;

			CyanMetaobjectWithAt cyanMetaobject = regularMetaobjectAnnotation.getCyanMetaobject();
			if ( cyanMetaobject instanceof CyanMetaobjectCompilationMarkDeletedCode ||
				 cyanMetaobject instanceof CyanMetaobjectCompilationContextPush	) {
				/*
				 * 	markDeletedCode pushCompilationContext code popCompilationContext
				 * 	pushCompilationContext code popCompilationContext
				 */

				if ( cyanMetaobject instanceof CyanMetaobjectCompilationMarkDeletedCode ) {

					/*
					 * 	markDeletedCode pushCompilationContext code popCompilationContext
					 */

					markDeletedCode = regularMetaobjectAnnotation;
					regularMetaobjectAnnotation = null;

					if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
						this.error2(symbol, "An annotation of metaobject '" + NameServer.pushCompilationContextName
						         + "' was expected. Probably this is error was caused by incorrect use of metaobject annotations such as "
						         + " using two of them in sequence inside an expression: 'k = @pi @other;'");
							return null;
					}
					pushAnnotation = annotation(true);
					if ( !(pushAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPush ) ) {
						this.error2(pushAnnotation.getFirstSymbol(), "A metaobject annotation '" + NameServer.pushCompilationContextName
					         + "' was expected. Probably this is error was caused by incorrect use of metaobject annotations such as "
					         + " using two of them in sequence inside an expression: 'k = @pi @other;'");
						return null;
					}
					Expr insideExpr = expr();

					if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
						this.error(true, symbol, "Expected an annotation of metaobject '" +
					              NameServer.popCompilationContextName +
					           "'." + foundSuch(), null, ErrorKind.metaobject_error);
						return null;
					}
					else {
						popAnnotation = annotation(true);
						if ( ! (popAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPop) ) {
							this.error(true, symbol, "Expected an annotation of metaobject '" +
						              NameServer.popCompilationContextName +
						           "'." + foundSuch(), null, ErrorKind.metaobject_error);
							return null;
						}
						return new ExprSurroundedByContext(null, markDeletedCode, pushAnnotation, insideExpr, popAnnotation);
					}

				}
				else {
					/*
					 * 	pushCompilationContext code popCompilationContext
					 */
					pushAnnotation = regularMetaobjectAnnotation;
					regularMetaobjectAnnotation = null;

					Expr insideExpr = expr();

					if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
						this.error(true, symbol, "Expected an annotation of metaobject '" +
					              NameServer.popCompilationContextName +
					           "'." + foundSuch(), null, ErrorKind.metaobject_error);
						return null;
					}
					else {
						popAnnotation = annotation(true);
						if ( ! (popAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPop) ) {
							this.error(true, symbol, "Expected an annotation of metaobject '" +
						              NameServer.popCompilationContextName +
						           "'." + foundSuch(), null, ErrorKind.metaobject_error);
							return null;
						}
						return new ExprSurroundedByContext(null, null, pushAnnotation, insideExpr, popAnnotation);
					}


				}
			}
			else {
				/*
			      * 	metaobjectAnnotation
			      * 	metaobjectAnnotation markDeletedCode pushCompilationContext code popCompilationContext
			      * 	metaobjectAnnotation pushCompilationContext code popCompilationContext
				 *
				 */
				if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
					/*
				      * 	metaobjectAnnotation
					 */
					return regularMetaobjectAnnotation;
				}
				else {
					/*
				      * 	metaobjectAnnotation markDeletedCode pushCompilationContext code popCompilationContext
				      * 	metaobjectAnnotation pushCompilationContext code popCompilationContext
					 *
					 */
					nextMetaobjectAnnotation = annotation(true);
					if ( nextMetaobjectAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationMarkDeletedCode ) {
						/*
					      * 	metaobjectAnnotation markDeletedCode pushCompilationContext code popCompilationContext
						 *
						 */

						markDeletedCode = nextMetaobjectAnnotation;
						if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
							this.error2(symbol, "An annotation of metaobject '" + NameServer.pushCompilationContextName
							         + "' was expected. Probably this is error was caused by incorrect use of metaobject annotations such as "
							         + " using two of them in sequence inside an expression: 'k = @pi @other;'");
								return null;
						}
						pushAnnotation = annotation(true);
						if ( !(pushAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPush ) ) {
							this.error2(pushAnnotation.getFirstSymbol(), "An annotation of metaobject '" + NameServer.pushCompilationContextName
						         + "' was expected. Probably this is error was caused by incorrect use of metaobject annotations such as "
						         + " using two of them in sequence inside an expression: 'k = @pi @other;'");
							return null;
						}
						Expr insideExpr = expr();

						if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
							this.error(true, symbol, "Expected an annotation of metaobject '" +
						              NameServer.popCompilationContextName +
						           "'." + foundSuch(), null, ErrorKind.metaobject_error);
							return null;
						}
						else {
							popAnnotation = annotation(true);
							if ( ! (popAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPop) ) {
								this.error(true, symbol, "Expected an annotation of metaobject '" +
							              NameServer.popCompilationContextName +
							           "'." + foundSuch(), null, ErrorKind.metaobject_error);
								return null;
							}
							return new ExprSurroundedByContext(regularMetaobjectAnnotation, markDeletedCode, pushAnnotation, insideExpr, popAnnotation);
						}
					}
					else if ( nextMetaobjectAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPush ) {
						/*
					      * 	metaobjectAnnotation pushCompilationContext code popCompilationContext
						 *
						 */
						pushAnnotation = nextMetaobjectAnnotation;

						Expr insideExpr = expr();

						if ( symbol.token != Token.METAOBJECT_ANNOTATION ) {
							this.error(true, symbol, "Expected an annotation of metaobject '" +
						              NameServer.popCompilationContextName +
						           "'." + foundSuch(), null, ErrorKind.metaobject_error);
							return null;
						}
						else {
							popAnnotation = annotation(true);
							if ( ! (popAnnotation.getCyanMetaobject() instanceof CyanMetaobjectCompilationContextPop) ) {
								this.error(true, symbol, "Expected an annotation of metaobject '" +
							              NameServer.popCompilationContextName +
							           "'." + foundSuch(), null, ErrorKind.metaobject_error);
								return null;
							}
							return new ExprSurroundedByContext(regularMetaobjectAnnotation, null, pushAnnotation, insideExpr, popAnnotation);
						}

					}
					else {
						this.error2(nextMetaobjectAnnotation.getFirstSymbol(), "An annotation of metaobject '" + NameServer.pushCompilationContextName
						         + "' was expected. Probably this is error was caused by incorrect use of metaobject annotations such as "
						         + " using two of them in sequence inside an expression: 'k = @pi @other;'");
						return null;
					}

				}
			}

		case LEFTPAR:
				Symbol leftParSymbol = symbol;
				Symbol rightParSymbol = null;
				Expr exprPar;
				next();

				exprPar = expr();
				if ( symbol.token != Token.RIGHTPAR ) {
					if ( symbol.token == Token.ASSIGN ) {
						error2(symbol, "assignments are statements in Cyan. They cannot appear inside an expression");
					}
					error2(symbol, "')' expected." + foundSuch());
				}
				else {
					if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
						error2(symbol, "letter, number, or '_' after ')'");
					}
					rightParSymbol = symbol;
					next();
				}
				exprPar = new ExprWithParenthesis(leftParSymbol, exprPar, rightParSymbol);
				return exprPar;


		default:
			if ( isBasicType(symbol.token) || symbol.token == Token.STRING ) {
				Symbol s = symbol;
				next();
				Expr retExpr = new ExprIdentStar(s);

				if ( symbol.token == Token.LEFTPAR ) {
					/* object creation such as in
					 *      Person("jose")
					 *      Stack<Int>(10)
					*/
					leftParSymbol = symbol;
					next();
					ArrayList<Expr> exprArray = realParameters();
					if ( symbol.token != Token.RIGHTPAR )
						error2(symbol, "')' expected after passing parameters to a context object." + foundSuch());
					rightParSymbol = symbol;
					if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
						error2(symbol, "letter, number, or '_' after ')'");
					}

					next();
					retExpr = new ExprObjectCreation(retExpr, exprArray, leftParSymbol,
							rightParSymbol, (ExprIdentStar ) retExpr);
				}
				return retExpr;

			} //redo
			/*
			else if ( symbol.token == Token.IDENTCOLON ||
				      symbol.token == Token.INTER_ID_COLON ||
					  symbol.token == Token.INTER_DOT_ID_COLON  ) {
				  /*
				   * message send to implicit self as in
				   *      b = eq: other;

				MessageWithSelectors m = messageSendNonUnary();
				return new ExprMessageSendWithSelectorsToExpr(null, m);
			} */
			else
				return exprLiteral();
		}
	}


	private String foundSuch() {
		String s = " Found '" + symbol.getSymbolString() + "'";
		if ( symbol instanceof SymbolKeyword ) {
			s = s + " which is a Cyan keyword";
		}
		return s;
	}

	private static String foundSuch(Symbol sym) {
		String s = " Found '" + sym.getSymbolString() + "'";
		if ( sym instanceof SymbolKeyword ) {
			s = s + " which is a Cyan keyword";
		}
		return s;
	}

	/**
	 * parse an identifier
	 * @return
	 */

	public Expr parseIdent() {
		Expr identExpr = null;
		Symbol identOne = symbol;
		next();
		/* It can be a local variable, an
		  instance variable, a literal object, a macro call, or a unary method name.
		  prototypes may be preceded by a package name such as in
		      math.Sin
		  And generic object should be followed by an Expr list between < and >
		 */

		if ( symbol.token != Token.PERIOD ) {

		    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(identOne.getSymbolString());
			if ( cyanMacro != null ) {

				if ( ! compInstSet.contains(saci.CompilationInstruction.dpa_actions) ) {
					/*
					 * found a macro in a compiler step that does not allow macros. See the Figure
					 * in Chapter "Metaobjects" of the Cyan manual
					 */
					this.error(true, symbol, "macro call in a compiler step that does not allow macros",
							identOne.getSymbolString(), ErrorKind.dpa_compilation_phase_literal_objects_and_macros_are_not_allowed);
					return new ExprNonExpression();
				}
				else {
					return macroCall(cyanMacro, true);
				}
			}


			identExpr = new ExprIdentStar(identOne);
			/** it is a parameter, local variable, instance variable, unary method */
			VariableDecInterface localVarParameter = searchIdent(identOne.getSymbolString());

			if ( localVarParameter != null ) {

				addAccessedLocalVariableToFunctions(identOne, localVarParameter);
			}
		}
		else {

			ArrayList<Symbol> identSymbolArray = new ArrayList<Symbol>();
			identSymbolArray.add(identOne);
			while ( symbol.token == Token.PERIOD ) {
				next();
				if ( symbol.token != Token.IDENT && ! isBasicType(symbol.token) ) {
					error2(symbol, "package, object name or slot (variable or method) expected." + foundSuch());
				}
				identSymbolArray.add(symbol);
				next();
			}
			identExpr = new ExprIdentStar(identSymbolArray, symbol);
		}

		ExprIdentStar firstPart = (ExprIdentStar ) identExpr;
		if ( symbol.token == Token.LT_NOT_PREC_SPACE ) {

			ArrayList<ArrayList<Expr>> arrayOfTypeList = new ArrayList<ArrayList<Expr>>();
			while ( symbol.token == Token.LT_NOT_PREC_SPACE ) {
				next();
				ArrayList<Expr> aTypeList = genericPrototypeArgList();
				if ( symbol.token != Token.GT ) {
				    error2(symbol, "'>' expected after the types of a generic object." + foundSuch());
				}
				next();
				arrayOfTypeList.add(aTypeList);
			}

			MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation = null;
			if ( symbol.token == Token.DOT_OCTOTHORPE ) {
				// if ( symbol.token == Token.CYANSYMBOL ) {
				messageSendToMetaobjectAnnotation = this.parseMessageSendToMetaobjectAnnotation();
			}


			identExpr = new ExprGenericPrototypeInstantiation( (ExprIdentStar ) identExpr, arrayOfTypeList,
					currentProgramUnit, messageSendToMetaobjectAnnotation);

			if ( messageSendToMetaobjectAnnotation != null )
				if (! messageSendToMetaobjectAnnotation.action(compilationUnit.getProgram(), (ExprGenericPrototypeInstantiation ) identExpr) ) {
					error2(identExpr.getFirstSymbol(), "No action associated to message '" + messageSendToMetaobjectAnnotation.getMessage()
					+ "'");
				}
		}
		else {
			if ( identExpr instanceof ExprIdentStar ) {
				MessageSendToMetaobjectAnnotation messageSendToMetaobjectAnnotation = null;
				if ( symbol.token == Token.DOT_OCTOTHORPE ) {
					// if ( symbol.token == Token.CYANSYMBOL ) {
					messageSendToMetaobjectAnnotation = this.parseMessageSendToMetaobjectAnnotation();
				}
				((ExprIdentStar ) identExpr).setMessageSendToMetaobjectAnnotation(messageSendToMetaobjectAnnotation);
				if ( messageSendToMetaobjectAnnotation != null ) {
					if (! messageSendToMetaobjectAnnotation.action(compilationUnit.getProgram(), (ExprIdentStar ) identExpr) ) {
						error2(identExpr.getFirstSymbol(), "No action associated to message '" + messageSendToMetaobjectAnnotation.getMessage()
						+ "'");
					}
				}
			}
		}

		if ( symbol.token == Token.LEFTPAR ) {
			/* object creation such as in
			 *      Person("jose")
			 *      Stack<Int>(10)
			*/
			Symbol leftParSymbol = symbol;
			next();
			ArrayList<Expr> exprArray = realParameters();
			if ( symbol.token != Token.RIGHTPAR )
				error2(symbol, "')' expected after passing parameters to a context object." + foundSuch());
			Symbol rightParSymbol = symbol;
			if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
				error2(symbol, "letter, number, or '_' after ')'");
			}

			next();
			identExpr = new ExprObjectCreation(identExpr, exprArray, leftParSymbol,
					rightParSymbol, firstPart);
		}

		return identExpr;

	}

	/**
	   @param identOne
	   @param localVarParameter
	 */
	private void addAccessedLocalVariableToFunctions(Symbol identOne, VariableDecInterface localVarParameter) {
		/**
		 * each function should have a list of accessed local variables and parameters
		 * of outer scope.
		 */


		String varName = localVarParameter.getName();

		int i = functionStack.size() - 1;
		// Iterator<ExprFunction> functionIter = functionStack.iterator();
		while (  i >= 0 ) {
			ExprFunction function = functionStack.get(i);
			--i;

			if ( function.isContextFunction() && (localVarParameter instanceof StatementLocalVariableDec) ) {
				if ( ((StatementLocalVariableDec ) localVarParameter).getLevel() < function.getFunctionLevel() ) {
					/*
					 * an external local variable is used inside a context function
					 */
					error2(identOne, "External local variable used inside a context function");
				}
			}
			/*
			 * if varName was not found in the list of parameters and local variables
			 * of 'function', insert it into the list of accessed parameters or local
			 * variables
			 */

			if ( function.searchLocalVariableDec(varName) == null &&
				 function.searchParameter(varName) == null &&
				 compilationStep == CompilationStep.step_7 )  {

				if ( localVarParameter instanceof ParameterDec ) {
					function.addAccessedParameter( (ParameterDec ) localVarParameter );
					((ParameterDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
				}
				else if ( localVarParameter instanceof StatementLocalVariableDec ) {
					function.addAccessedVariableDec( (StatementLocalVariableDec ) localVarParameter );
					((StatementLocalVariableDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
				}
			}
			else
				   /*
				    * if the variable was found, it is not necessary to consider it as
				    * an external variable for this and all upper level functions
				    */
				break;
		}
	}



	public Expr exprLiteral() {
		Expr ret;

		switch (symbol.token) {
		case BYTELITERAL:
			ret =  new ExprLiteralByte(symbol);
			next();
			break;
		case SHORTLITERAL:
			ret =  new ExprLiteralShort(symbol);
			next();
			break;
		case INTLITERAL:
			ret =  new ExprLiteralInt(symbol);
			next();
			break;
		case LONGLITERAL:
			ret =  new ExprLiteralLong(symbol);
			next();
			break;
		case FLOATLITERAL:
			ret =  new ExprLiteralFloat(symbol);
			next();
			break;
		case DOUBLELITERAL:
			ret =  new ExprLiteralDouble(symbol);
			next();
			break;
		case CHARLITERAL:
			ret =  new ExprLiteralChar(symbol);
			next();
			break;
		case FALSE:
			ret =  new ExprLiteralBooleanFalse(symbol);
			next();
			break;
		case TRUE:
			ret =  new ExprLiteralBooleanTrue(symbol);
			next();
			break;
		case LITERALSTRING:
			  // this rule represents both strings of the kind "hello" or """ multiple lines """

			ExprLiteralString els;
			ret =  els = new ExprLiteralString(symbol);
			if ( els.getVarNameList()  != null ) {
				for ( String varName : els.getVarNameList() ) {
					/** it is a parameter, local variable, instance variable, unary method */
					VariableDecInterface localVarParameter = searchIdent(varName);

					if ( localVarParameter != null ) {

						addAccessedLocalVariableToFunctions(els.getFirstSymbol(), localVarParameter);
					}
				}
			}

			next();
			break;
		case CYANSYMBOL:
			ret = new ExprLiteralCyanSymbol((SymbolCyanSymbol ) symbol);
			next();
			break;
		case NIL:
			ret = new ExprLiteralNil(symbol);
			next();
			break;
		case LEFTSB:

			lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, "[");
			Symbol startSymbol, endSymbol;
			startSymbol = symbol;
			ArrayList<Expr> exprList = new ArrayList<Expr>();
			next();
			boolean foundLiteralArray = true;
			if ( startExpr(symbol) ) {
				exprList.add(expr());

				if ( symbol.token == Token.RETURN_ARROW ) {
					lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);
					// a literal map
					foundLiteralArray = false;
					next();
					exprList.add(expr());
					while ( symbol.token == Token.COMMA )  {
						lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
						next();
						exprList.add(expr());
						if ( symbol.token != Token.RETURN_ARROW ) {
							error2(symbol, "'->' was expected");
						}
						lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);

						next();
						exprList.add(expr());
					}
				}
				else {
					// a literal array
					while ( symbol.token == Token.COMMA )  {
						lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
						next();
						exprList.add(expr());
					}

				}
			}
			else {
				if ( symbol.token == Token.RIGHTSB )
					error(true, symbol, "Empty literal arrays are illegal", null, ErrorKind.empty_literal_array);
				else
					error(true, symbol, "Expression expected." + foundSuch(), null, ErrorKind.expression_expected_inside_method);
			}
			if ( symbol.token != Token.RIGHTSB ) {
				error2(symbol, "']' expected at the end of literal array or map" + foundSuch());
			}
			lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, "]");
			endSymbol = symbol;
			next();
			if ( foundLiteralArray ) {
				ret = new ExprLiteralArray(startSymbol, endSymbol, exprList);
			}
			else {
				ret = new ExprLiteralMap(startSymbol, endSymbol, exprList);
			}
			break;
		case LEFTSB_DOT:
			startSymbol = symbol;
			next();

			exprList = new ArrayList<Expr>();

			Expr firstExpr;
			boolean namedTuple = false;

			firstExpr = expr();
			if ( symbol.token == Token.ASSIGN ) {
				if ( !(firstExpr instanceof ExprIdentStar) || ((ExprIdentStar ) firstExpr).getIdentSymbolArray().size() != 1 ) {
					error2(firstExpr.getFirstSymbol(), "An identifier was expected as the field name of a tuple." + foundSuch());
				}
				namedTuple = true;
				next();
				exprList.add(firstExpr);
				Expr secondExpr = expr();
				exprList.add(secondExpr);
				while ( symbol.token == Token.COMMA ) {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
					next();
					firstExpr = expr();
					exprList.add(firstExpr);
					if ( symbol.token != Token.ASSIGN ) {
						error2(symbol, "It was expected a field name, an identifier, followed by '=' and an expression." + foundSuch());
					}
					next();
					if ( !(firstExpr instanceof ExprIdentStar) || ((ExprIdentStar ) firstExpr).getIdentSymbolArray().size() != 1 ) {
						error2(firstExpr.getFirstSymbol(), "An identifier was expected as the field name of a tuple." + foundSuch());
					}
					secondExpr = expr();
					exprList.add(secondExpr);
				}
			}
			else {
				namedTuple = false;
				exprList.add(firstExpr);
				while ( symbol.token == Token.COMMA ) {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
					next();
					firstExpr = expr();
					exprList.add(firstExpr);
				}
			}

			if ( symbol.token != Token.RIGHTDOT_SB )
				error(true, symbol, "'.]' expected" + foundSuch(), null, ErrorKind.dot_square_backet_expected);
			endSymbol = symbol;
			next();
			ret = new ExprLiteralTuple(startSymbol, endSymbol, exprList, namedTuple);
			break;
		case LEFTCB:
			// found the declaration of a function
			ret = functionDec();
			break;
		case LITERALOBJECT:

			CyanMetaobjectLiteralObjectAnnotation metaobjectAnnotation = null;
			if ( ! compInstSet.contains(saci.CompilationInstruction.dpa_actions) ) {
				/*
				 * found a literal object in a compiler step that does not allow literal objects. See the Figure
				 * in Chapter "Metaobjects" of the Cyan manual
				 */
				this.error(true, symbol, "Literal object in a compiler step that does not allow literal objects",
						symbol.getSymbolString(), ErrorKind.dpa_compilation_phase_literal_objects_and_macros_are_not_allowed);
				ret = new ExprNonExpression();
			}
			else {

				if ( symbol instanceof SymbolLiteralObject ) {
					/*
					 * found a literal object that is parsed with a user-defined compiler. That is,
					 *  the text inside the literal object is not parsed with the help of the Cyan compiler.
					 *  For example, usually literal strings that start with a letter and user-defined
					 *  literal numbers do not use the Cyan compiler.
					 *  <code><br>
					 *  var regexpr = r"0+[a-z]*"; <br>
					 *  var five = 101bin;<br>
					 *  </code>
					 */

					SymbolLiteralObject symbolLiteralObject = (SymbolLiteralObject ) symbol;
					metaobjectAnnotation = symbolLiteralObject.getCyanMetaobjectLiteralObjectAnnotation();
					CyanMetaobjectLiteralObject cyanMetaobject = metaobjectAnnotation.getCyanMetaobjectLiteralObject();
					cyanMetaobject.setMetaobjectAnnotation(metaobjectAnnotation);
					metaobjectAnnotation.setOriginalCode(symbolLiteralObject.getSymbolString());
					ret = metaobjectAnnotation;

					if ( !( cyanMetaobject instanceof IParseWithoutCyanCompiler_dpa ) ) {
						this.error(true, symbolLiteralObject,
								"Internal error: the Cyan metaobject should implement interface 'IParseWithoutCyanCompiler_dpa'", symbolLiteralObject.getSymbolString(), ErrorKind.internal_error);
						ret = new ExprNonExpression();
					}
					else {

						ICompilerAction_dpa compilerAction = new CompilerAction_dpa(this);

						try {
							((IParseWithoutCyanCompiler_dpa ) cyanMetaobject).dpa_parse( compilerAction, symbolLiteralObject.getUsefulString() );
						}
						catch ( error.CompileErrorException e ) {
						}
						catch ( RuntimeException e ) {
							thrownException(metaobjectAnnotation, metaobjectAnnotation.getFirstSymbol(), e);
						}
						finally {
							this.metaobjectError(cyanMetaobject, metaobjectAnnotation);
						}

						next();
					}

				}
				else if ( symbol instanceof SymbolLiteralObjectParsedWithCompiler ) {
					/*
					 * represents a literal object such as
					 *  <br>
					* <code>
					*     {* 1:2, 2:3, 3:1 *} <br>
					*     [* "one":1, "two":2 *] <br>
					* </code><br>
					 * that is parsed with the help of the Cyan compiler.
					*/
					SymbolLiteralObjectParsedWithCompiler symbolLiteralObject = (SymbolLiteralObjectParsedWithCompiler ) symbol;
					metaobjectAnnotation = symbolLiteralObject.getCyanMetaobjectLiteralObjectAnnotation();
					CyanMetaobjectLiteralObject cyanMetaobject = metaobjectAnnotation.getCyanMetaobjectLiteralObject();
					cyanMetaobject.setMetaobjectAnnotation(metaobjectAnnotation);
					ret = metaobjectAnnotation;


					if ( ! (cyanMetaobject instanceof IParseWithCyanCompiler_dpa) ) {
						this.error(true, symbolLiteralObject,
								"Internal error: the Cyan metaobject should implement interface 'IParseWithCyanCompiler_dpa'", symbolLiteralObject.getSymbolString(), ErrorKind.internal_error);

					}
					else {
						next();
						Compiler_dpa compiler_dpa = new Compiler_dpa(this, lexer,
								symbolLiteralObject.getSymbolString());
						/*
						 * the errors found in the compilation are introduced in object 'this'. They will be
						 * processed as regular compiler errors.
						 */


						try {
							((IParseWithCyanCompiler_dpa ) cyanMetaobject).dpa_parse(compiler_dpa);
						}
						catch ( error.CompileErrorException e ) {
						}
						catch ( RuntimeException e ) {
							thrownException(metaobjectAnnotation, metaobjectAnnotation.getFirstSymbol(), e);
						}
						finally {
							this.metaobjectError(cyanMetaobject, metaobjectAnnotation);
							this.copyLexerData(compiler_dpa.compiler);
						}

						metaobjectAnnotation.setExprStatList(compiler_dpa.getExprStatList());

						if ( symbol.token == Token.RIGHTCHAR_SEQUENCE ) {
							next();
						}
						else {
							if ( lexer.expectedRightSymbolSequence() == null ) {
								this.error(true, symbol,
										"Internal error in Compiler::annotation", null, ErrorKind.internal_error);
							}
							else
								error(true, symbol,
									"The right symbol sequence '" + lexer.expectedRightSymbolSequence() + "' was expected." + foundSuch(),
									null, ErrorKind.right_symbol_sequence_expected);
						}

						lexer.setNewLineAsToken(false);
					}
				}
				else {
					this.error(true,  symbol,
							"Literal object '" + symbol.getSymbolString() + "' has a Java class " +
							       " that does not implement either 'IParseWithoutCyanCompiler_dpa' or 'IParseWithCyanCompiler_dpa'", null, ErrorKind.metaobject_error);

					ret = new ExprNonExpression();
				}
				if ( metaobjectAnnotation != null )
					metaobjectAnnotation.setNextSymbol(symbol);
			}
			break;
		default:
			if ( symbol.getSymbolString().equals("Dyn") ) {
				error2(symbol, "'Dyn' is a virtual type. It cannot be used in an expression. It can only be used as a type of variable/parameter/return value type of method");
			}
			else
				error2(symbol, "Expression expected." + foundSuch());
			ret = null;
		}
		return ret;
	}


	/**
	 * analyzes basic types literals that are arguments to metaobject annotations. That includes arrays of basic types
	 * and tuples.
	   @return
	 */
	private ExprAnyLiteral exprBasicTypeLiteral() {
		Tuple2<ExprAnyLiteral, Boolean> t = exprBasicTypeLiteral_Ident();
		return t.f1;
	}

	/**
	 * analyzes basic types literals that are arguments to metaobject annotations. That includes arrays of basic types and tuples.
	 * Return a tuple composed by the expression and a boolean value. If this boolean value is true then an identifier
	 * was used in the expression. Then expression   "i" returns a tuple <code>[. e, true .]</code> using the Cyan syntax. Expression
	 * "0" returns <code>[. e, false .]</code> and <code>"[ 0, 1, i ]"</code> returns <code>[. e, true .]</code>
	 * <br>
	 * The type of an identifier is considered String. That includes the basic types such as Int. Then the type of 'Int' is String.
	   @return
	 */

	private Tuple2<ExprAnyLiteral, Boolean> exprBasicTypeLiteral_Ident() {
		ExprAnyLiteral ret;
		boolean foundIdent = false;
		Tuple2<ExprAnyLiteral, Boolean> t;

		if ( isBasicType(symbol.token) ) {
			Symbol s = symbol;
			next();
			ret = new ExprAnyLiteralIdent(new ExprIdentStar(s));
		}
		else {
			Symbol prefix = null;
			if ( symbol.token == Token.PLUS || symbol.token == Token.MINUS ) {
				prefix = symbol;
				next();
				if ( symbol.token != Token.BYTELITERAL && symbol.token != Token.INTLITERAL &&
					  symbol.token != Token.LONGLITERAL && symbol.token != Token.FLOATLITERAL &&
					  symbol.token != Token.DOUBLELITERAL ) {
					error2(symbol, "A number was expected after symbol '" + prefix.getSymbolString() + "'");
				}
			}
			switch (symbol.token) {
			case IDENT:
				foundIdent = true;
				ret =  new ExprAnyLiteralIdent(ident());
				break;
			case IDENTCOLON:
				foundIdent = true;
				ret =  new ExprAnyLiteralIdent(identColon());
				break;
			case BYTELITERAL:
				ret =  new ExprLiteralByte(symbol, prefix);
				next();
				break;
			case SHORTLITERAL:
				ret =  new ExprLiteralShort(symbol, prefix);
				next();
				break;
			case INTLITERAL:
				ret =  new ExprLiteralInt(symbol, prefix);
				next();
				break;
			case LONGLITERAL:
				ret =  new ExprLiteralLong(symbol, prefix);
				next();
				break;
			case FLOATLITERAL:
				ret =  new ExprLiteralFloat(symbol, prefix);
				next();
				break;
			case DOUBLELITERAL:
				ret =  new ExprLiteralDouble(symbol, prefix);
				next();
				break;
			case CHARLITERAL:
				ret =  new ExprLiteralChar(symbol);
				next();
				break;
			case FALSE:
				ret =  new ExprLiteralBooleanFalse(symbol);
				next();
				break;
			case TRUE:
				ret =  new ExprLiteralBooleanTrue(symbol);
				next();
				break;
			case LITERALSTRING:
				  // this rule represents both strings of the kind "hello" or """ multiple lines """
				ExprLiteralString els;
				ret =  els = new ExprLiteralString(symbol);
				if ( els.getVarNameList() != null ) {
					for ( String varName : els.getVarNameList() ) {
						/** it is a parameter, local variable, instance variable, unary method */
						VariableDecInterface localVarParameter = searchIdent(varName);

						if ( localVarParameter != null ) {

							addAccessedLocalVariableToFunctions(els.getFirstSymbol(), localVarParameter);
						}
					}
				}
				next();
				break;
			case CYANSYMBOL:
				ret = new ExprLiteralCyanSymbol((SymbolCyanSymbol ) symbol);
				next();
				break;
			case NIL:
				ret = new ExprLiteralNil(symbol);
				next();
				break;

			case LEFTSB:

				lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, "[");
				Symbol startSymbol, endSymbol;
				startSymbol = symbol;
				ArrayList<Expr> exprList = new ArrayList<Expr>();
				next();
				boolean foundLiteralArray = true;
				if ( startExpr(symbol) ) {

					t = exprBasicTypeLiteral_Ident();
					if ( t.f2 )
						foundIdent = true;
					exprList.add(t.f1);


					if ( symbol.token == Token.RETURN_ARROW ) {
						lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);
						// a literal map
						foundLiteralArray = false;
						next();

						t = exprBasicTypeLiteral_Ident();
						if ( t.f2 )
							foundIdent = true;
						exprList.add(t.f1);

						// exprList.add(expr());
						while ( symbol.token == Token.COMMA )  {
							lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
							next();

							t = exprBasicTypeLiteral_Ident();
							if ( t.f2 )
								foundIdent = true;
							exprList.add(t.f1);

							//exprList.add(expr());
							if ( symbol.token != Token.RETURN_ARROW ) {
								error2(symbol, "'->' was expected");
							}
							lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);

							next();

							t = exprBasicTypeLiteral_Ident();
							if ( t.f2 )
								foundIdent = true;
							exprList.add(t.f1);
							//exprList.add(expr());
						}
					}
					else {
						// a literal array
						while ( symbol.token == Token.COMMA )  {
							lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
							next();
							t = exprBasicTypeLiteral_Ident();
							if ( t.f2 )
								foundIdent = true;
							exprList.add(t.f1);
							//exprList.add(expr());
						}

					}
				}
				else {
					if ( symbol.token == Token.RIGHTSB )
						error(true, symbol, "Empty literal arrays are illegal", null, ErrorKind.empty_literal_array);
					else
						error(true, symbol, "Expression expected." + foundSuch(), null, ErrorKind.expression_expected_inside_method);
				}
				if ( symbol.token != Token.RIGHTSB ) {
					error2(symbol, "']' expected at the end of literal array or map" + foundSuch());
				}
				lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, "]");
				endSymbol = symbol;
				next();
				if ( foundLiteralArray ) {
					ret = new ExprLiteralArray(startSymbol, endSymbol, exprList);
				}
				else {
					ret = new ExprLiteralMap(startSymbol, endSymbol, exprList);
				}


				/*
				lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, "[");
				Symbol startSymbol, endSymbol;
				startSymbol = symbol;
				ArrayList<Expr> exprList = new ArrayList<Expr>();
				next();
				if ( startExpr(symbol) ) {
					t = exprBasicTypeLiteral_Ident();
					if ( t.f2 )
						foundIdent = true;
					exprList.add(t.f1);
					while ( symbol.token == Token.COMMA )  {
						lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
						next();
						t = exprBasicTypeLiteral_Ident();
						if ( t.f2 )
							foundIdent = true;
						exprList.add(t.f1);
					}
				}
				else {
					if ( symbol.token == Token.RIGHTSB )
						error(true, symbol, "Empty literal arrays are illegal", null, ErrorKind.empty_literal_array);
					else
						error(true, symbol, "Expression expected." + foundSuch(), null, ErrorKind.expression_expected_inside_method);
				}
				if ( symbol.token != Token.RIGHTSB ) {
					error2(symbol, "']' expected at the end of literal array." + foundSuch());
				}
				lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, "]");
				endSymbol = symbol;
				next();
				ret = new ExprLiteralArray(startSymbol, endSymbol, exprList);
				*/
				break;
			case LEFTSB_DOT:
				startSymbol = symbol;
				next();

				exprList = new ArrayList<Expr>();

				Expr firstExpr;
				// boolean namedTuple = false;


				t = exprBasicTypeLiteral_Ident();
				if ( t.f2 )
					foundIdent = true;
				firstExpr = t.f1;

				exprList.add(firstExpr);
				while ( symbol.token == Token.COMMA ) {
					lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
					next();
					t = exprBasicTypeLiteral_Ident();
					if ( t.f2 )
						foundIdent = true;
					firstExpr = t.f1;
					exprList.add(firstExpr);
				}

				if ( symbol.token != Token.RIGHTDOT_SB ) {
					if ( symbol.token == Token.ASSIGN ) {
						error(true, symbol, "named tuples, with '=', are not allowed here. Use a non-named tuple instead", null, ErrorKind.dot_square_backet_expected);
					}
					else {
						error(true, symbol, "'.]' expected." + foundSuch(), null, ErrorKind.dot_square_backet_expected);
					}
				}
				endSymbol = symbol;
				next();
				ret = new ExprLiteralTuple(startSymbol, endSymbol, exprList, false);
				break;

			default:
				error2(symbol, "A literal expression of basic types was expected." + foundSuch());
				ret = null;
			}

		}

		return new Tuple2<ExprAnyLiteral, Boolean>(ret, foundIdent);
	}


	public Expr functionDec() {

		Symbol startSymbol = symbol;
		Expr selfType = null;
		next();

		ExprFunction aFunctionDec;
		int numberOfParameters = parameterDecStack.size();


		if ( symbol.token == Token.LEFTPARCOLON ) {
			lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol,  symbol.symbolString);
			next();
			aFunctionDec = functionSignature(startSymbol);
			if ( symbol.token != Token.COLONRIGHTPAR ) {
				error2(symbol, "':)' expected after function signature." + foundSuch());
			}
			next();
		}
		else
			aFunctionDec = new ExprFunctionRegular(startSymbol);

		aFunctionDec.setFunctionLevel(functionStack.size());

		if ( aFunctionDec instanceof ExprFunctionRegular ) {
			ExprFunctionRegular ef = (ExprFunctionRegular ) aFunctionDec;
			ArrayList<ParameterDec> parameterList = ef.getParameterList();
			if ( parameterList != null && parameterList.size() > 0 &&
					(parameterList.get(0) == null || parameterList.get(0).getName() == null ) ) {
				this.error2(symbol, "Internal error in functionDec");
			}

		}


		aFunctionDec.setFunctionPrototypeName(
                (aFunctionDec.isContextFunction() ? NameServer.contextFunctionProtoName : NameServer.functionProtoName) +
                functionCounter++ + NameServer.endsInnerProtoName
				);

		aFunctionDec.setNumber(currentProgramUnit.getNextFunctionNumber());
		currentProgramUnit.addNextFunctionNumber();

		((ObjectDec ) currentProgramUnit).addToBeCreatedFunction(aFunctionDec);


		functionStack.push(aFunctionDec);
		int numberOfLocalVariables = this.localVariableDecStack.size();

		StatementList statementList = statementList();
		if ( symbol.token != Token.RIGHTCB )
			error2(symbol, "'}' expected at the end of a function." + foundSuch());
		aFunctionDec.setEndSymbol(symbol);

		/*
		if ( (symbol.getColumnNumber() == this.startSymbolCurrentStatement.getColumnNumber() ) ) {
			/*
			 * insert a ';'
			 * /
			symbol = new SymbolOperator(Token.SEMICOLON, ";", symbol.getStartLine(),
					symbol.getLineNumber(), symbol.getColumnNumber() + 1, symbol.getOffset());

		}
		else {
		}
		*/
		next();


		functionStack.pop();

		while ( localVariableDecStack.size() > numberOfLocalVariables )
			localVariableDecStack.pop();

		while ( parameterDecStack.size() > numberOfParameters )
			parameterDecStack.pop();

		aFunctionDec.setStatementList(statementList);
		aFunctionDec.setSelfType(selfType);
		aFunctionDec.setCurrentMethod(currentMethod);
		aFunctionDec.setCurrentProgramUnit(currentProgramUnit);
		return aFunctionDec;
	}

	/**
        CloSignature ::=  \{  Type Id \} \{ ``,"\/ Type Id \}   [ ``\verb@->@"\/ Type ] \verb"|"
                       \{ IdColon  \{  Type Id \} \{ ``,"\/ Type Id \} \}  \} [ ``\verb@->@"\/ Type ]

	   @param startSymbol
	   @return
	 */

	private ExprFunction functionSignature(Symbol startSymbol) {

		ExprFunction exprFunction;
		ArrayList<SelectorWithParameters> selectorWithParametersList =
				new ArrayList<SelectorWithParameters>();
		if ( symbol.token == Token.IDENTCOLON ) {
			while ( symbol.token == Token.IDENTCOLON ) {
				// an unusual function that has selectors such as
				//    var b = { (: eval: (Int f1) eval: (Int y) -> Int :)  ^f1*y };
				//   k = b eval: 10 eval: 5;

				if ( symbol.getSymbolString().compareTo("eval:") != 0 )
					error2(symbol, "'eval:' expected." + foundSuch());
				SelectorWithParameters selectorWithParameters = new SelectorWithParameters(symbol);
				next();
				parameterDecListFunction( selectorWithParameters.getParameterList() );
				selectorWithParametersList.add(selectorWithParameters);
			}
			for ( SelectorWithParameters evalSelector : selectorWithParametersList ) {
				if ( evalSelector.getName().compareTo("eval:") != 0 )
					error2(evalSelector.getSelector(), "Function with multiple selectors may only have 'eval:' selectors");
			}

			exprFunction = new ExprFunctionWithSelectors(startSymbol, selectorWithParametersList );

		}
		else {
			ExprFunctionRegular aFunctionDec = new ExprFunctionRegular(startSymbol);
			if ( symbol.token != Token.RETURN_ARROW ) {
				parameterDecListFunction(aFunctionDec.getParameterList());
			}
			lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);

			exprFunction = aFunctionDec;
			if ( exprFunction.isContextFunction() ) {
				error2( aFunctionDec.getParameterList().get(0).getFirstSymbol(), "Context function are not supported in this version of the compiler");
			}
		}
		if ( symbol.token == Token.RETURN_ARROW ) {
			lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);
			next();

			exprFunction.setReturnTypeExpr(type());
		}
		return exprFunction;
	}

	/**
	 * analyzes a non-unary message, therefore without the receiver. In
	 *        circle f1: 10 y: 30 r: 5
	 * this method would be called to analyze "f1: 10 y: 30 r: 5"
	 *
	 * @return
	 */
	private MessageWithSelectors messageSendNonUnary() {

		ArrayList<SelectorWithRealParameters> selectorParameterList;
		Symbol selector;
		SelectorWithRealParameters selectorWithRealParameters;


		if ( symbol.token == Token.IDENTCOLON ||
				symbol.token == Token.INTER_ID_COLON ||
				symbol.token == Token.INTER_DOT_ID_COLON ||
				symbol.token == Token.BACKQUOTE ) {

			selectorParameterList = new ArrayList<SelectorWithRealParameters>();
			Symbol firstSelector = null;
			while ( symbol.token == Token.IDENTCOLON ||
					symbol.token == Token.INTER_ID_COLON ||
					symbol.token == Token.INTER_DOT_ID_COLON ||
					symbol.token == Token.BACKQUOTE ) {
				boolean backquote = false;
				if ( symbol.token == Token.BACKQUOTE ) {
					backquote = true;
					next();

					if ( symbol.token == Token.IDENTCOLON ) {

						/**
						 * each function should have a list of accessed local variables and parameters
						 * of outer scope.
						 */
						/** it is a parameter, local variable, instance variable, unary method */
						String varName = symbol.getSymbolString();
						varName = varName.substring(0, varName.length()-1);
						Object localVarParameter = searchIdent(varName);

						if ( localVarParameter != null ) {
							int i = functionStack.size() - 1;
							// Iterator<ExprFunction> functionIter = functionStack.iterator();
							while (  i >= 0 ) {
								ExprFunction function = functionStack.get(i);
								--i;
								if ( compilationStep == CompilationStep.step_7 &&
										function.searchLocalVariableDec(varName) == null  )  {

									if ( localVarParameter instanceof ParameterDec ) {
										function.addAccessedParameter( (ParameterDec ) localVarParameter );
										((ParameterDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
									}
									else if ( localVarParameter instanceof StatementLocalVariableDec ) {
										function.addAccessedVariableDec( (StatementLocalVariableDec ) localVarParameter );
										((StatementLocalVariableDec ) localVarParameter).addInnerObjectNumberList(function.getNumber());
									}
								}
								else
									   /*
									    * if the variable was found, it is not necessary to consider it as
									    * an external variable for this and all upper level functions
									    */
									break;
							}

						}
					}



					if ( symbol.token != Token.IDENTCOLON )
						error2(symbol, "A message selector like 'add:' was expected after '`' (backquote)." + foundSuch());
				}
				lastSelector = selector = symbol;
				if ( firstSelector == null ) firstSelector = symbol;
				if ( symbol.token != firstSelector.token )
					error2(symbol, "mixing of different types of selectors in the same message (regular, dynamic call, Nil-safe)");
				next();
				ArrayList<Expr> exprList = realParameters();
				selectorWithRealParameters = new SelectorWithRealParameters(selector, backquote,
						                             exprList);
				selectorParameterList.add(selectorWithRealParameters);
			}
			return new MessageWithSelectors(selectorParameterList);
		}
		else {
			error2(symbol, "message send expected." + foundSuch());
			return null;
		}

	}
	private Symbol lastSelector;

	/**
	 * return true if the symbol s can start
	 * @return
	 */
	private static boolean canStartMessageSendNonUnary(Symbol s) {
		return s.token == Token.IDENTCOLON ||
			   s.token == Token.INTER_ID_COLON ||
			    s.token == Token.INTER_DOT_ID_COLON;
		    // it was ||  isBinaryOperator(s.token)
	}


	private ArrayList<Expr> realParameters() {

		ArrayList<Expr> exprList = new ArrayList<Expr>();
		if ( startExpr(symbol) ) {
			exprList.add(exprOrGt());
			while ( symbol.token == Token.COMMA ) {
				lexer.checkWhiteSpaceParenthesisAfter(symbol, ",");
				next();
				if ( symbol.token == Token.IDENTCOLON || symbol.token == Token.INTER_DOT_ID_COLON || symbol.token == Token.INTER_ID_COLON ) {
					error2(symbol, "This message send is being passed as parameter to selector '" + lastSelector.getSymbolString() + "'. It should be put between parentheses");
				}
				exprList.add(exprOrGt());
			}
		}
		return exprList;
	}


	/**
	 * parser a method signature
	 * @return
	 */
	private MethodSignature methodSignature() {

		MethodSignature methodSignature = null;
		ArrayList<SelectorWithParameters> selectorList;
		boolean indexingMethod = false;
		Expr returnType = null;


		/* if ( startType(symbol.token) ) {
			returnType = type();
		}
		*/

		if ( symbol.token == Token.LEFTRIGHTSB ) {
			indexingMethod = true;
			next();
		}
		Symbol identSymbol = symbol;

		/*
		if ( symbol.token != Token.IDENT && symbol.token != Token.IDENTCOLON &&
			 symbol.token != Token.LEFTPAR && ! isOperator(symbol.token) ) {
			if ( returnType != null ) {
				boolean anError = false;
				if ( ! (returnType instanceof ExprIdentStar) ) {
					anError = true;
			    }
				else {
					ExprIdentStar idstar = (ExprIdentStar ) returnType;
					if ( idstar.getIdentSymbolArray().size() != 1 )
						anError = true;
				}
				if ( anError )
					error(identSymbol, "Identifier expected as method name", "",
							ErrorKind.identifier_expected_inside_method);
			}
			returnType = null;
			methodSignature = new MethodSignatureUnary(identSymbol, currentMethod);
			refactorChangeId_to_IdDot(identSymbol);
		}
		else {
		}
		*/
		boolean isInit = false;
		boolean isInitOnce = false;
		String name = "";

		switch ( symbol.token ) {
		case IDENT:
			name = symbol.symbolString;


		    CyanMetaobjectMacro cyanMacro = metaObjectMacroTable.get(name);
		    if ( cyanMacro != null ) {
		    	this.error2(symbol, "This unary method has the name of a macro keyword of a macro imported by this compilation unit");
		    }



			methodSignature = new MethodSignatureUnary(symbol, currentMethod);

			if ( indexingMethod )
				error2(symbol, "unary methods cannot be indexing methods (declared with '[]')");

			if ( name.equals("new") ) {
				if ( cyanMetaobjectContextStack.empty() && ! this.currentProgramUnit.isInnerPrototype() ) {
					this.error2(symbol, "'new' cannot be user-declared");
				}
			}
			if ( name.equals("initOnce") )
				isInitOnce = true;

			next();
			refactorChangeId_to_IdDot(identSymbol);

			break;

		case IDENTCOLON:
			selectorList = new ArrayList<SelectorWithParameters>();
			while ( symbol.token == Token.IDENTCOLON ) {
				SelectorWithParameters selectorWithParameters = new SelectorWithParameters(symbol);
				// if ( name.length() > 0 )
				name = name +  symbol.symbolString;
				next();

				/* if ( indexingMethod && ! selectorWithParameters.getName().equals("at:") )
					this.error(symbol, "'[]' can only be used with selector 'at:'",
							 selectorWithParameters.getName(), ErrorKind.method_cannot_be_indexing_method); */
				// this if is necessary because it is legal something like
				//  public func file: open: (String name) [ ... ]


				if ( symbol.token == Token.LEFTPAR || startType(symbol.token) ) {
					parameterDecList(selectorWithParameters.getParameterList());
				}
				selectorList.add(selectorWithParameters);

			}
			methodSignature = new MethodSignatureWithSelectors(selectorList, indexingMethod, currentMethod);
			if ( selectorList.size() == 1 ) {
				if ( selectorList.get(0).getName().equals("new:") ) {
					if ( cyanMetaobjectContextStack.empty() && ! this.currentProgramUnit.isInnerPrototype() ) {
						this.error2(symbol, "'new:' cannot be user-declared");
					}
				}
				if ( name.equals("init:") ) {
					if ( selectorList.get(0).getParameterList() == null || selectorList.get(0).getParameterList().size() == 0 ) {
						error2(selectorList.get(0).getSelector(), "'init:' methods should take parameters");
					}
				}
			}
			else {
				/*
				 * check if any selector is 'init:' or 'new:'
				 */
				for ( SelectorWithParameters sel : selectorList ) {
					if ( sel.getName().equals("init:") || sel.getName().equals("new:") )
						error2(sel.getSelector(), "It is illegal to have a selector with name 'init:' or 'new:'");
				}
			}


			/*String methodName = methodSignature.getNameWithoutParamNumber(); //.getMethodSignatureWithParametersAsString();
			if ( methodName.compareTo("init:") == 0 ) {
				if ( currentMethod.getHasOverride() )
					error(true, idSymbol, "'init:' methods cannot be preceeded by 'override'",
							"init:", ErrorKind.init_should_not_be_declared_with_override);
				if ( currentMethod.isAbstract() )
					error(true, idSymbol, "'init:' methods cannot be abstract",
							"init:", ErrorKind.init_should_not_be_abstract);
				if ( indexingMethod )
					error(true, idSymbol, "'init:' methods cannot preceded by '[]'",
							"init:", ErrorKind.init_new_cannot_be_preceded_by_indexing_operator);
				if ( currentMethod.getVisibility() != Token.PUBLIC )
					error(true, idSymbol, "'init:' methods should be public",
							"init:", ErrorKind.init_new_should_be_public);
				isInit = true;
			}
			else if ( methodName.compareTo("new:") == 0 ) {
				if ( currentMethod.getHasOverride() )
					error(true, idSymbol, "'new:' methods cannot be preceeded by 'override'",
							"new:", ErrorKind.new_cannot_be_declared_with_override);
				if ( currentMethod.isAbstract() )
					error(true, idSymbol, "'new:' methods cannot be abstract",
							"new:", ErrorKind.new_cannot_be_abstract);
				if ( indexingMethod )
					error(true, idSymbol, "'new:' methods cannot preceded by '[]'",
							"new:", ErrorKind.init_new_cannot_be_preceded_by_indexing_operator);
				if ( currentMethod.getVisibility() != Token.PUBLIC )
					error(true, idSymbol, "'new:' methods should be public",
							"new:", ErrorKind.new_methods_should_be_public);
				}
			*/
			break;
		default:
			// should be an operator
			if ( isOperator(symbol.token) ) {
				if ( symbol.token == Token.AND || symbol.token == Token.OR ) {
					this.error2(symbol, "'&&' and '||' cannot be method names. They can only be used with Boolean values");
					return null;
				}
				Symbol operatorSymbol = symbol;
				MethodSignatureOperator mso = new MethodSignatureOperator(symbol, currentMethod);
				methodSignature = mso;
				next();
				if ( startType(symbol.token) ||
					 symbol.token == Token.LEFTPAR ||
					 symbol.token == Token.IDENT ) {  // this last is not really necessary but ...

					boolean leftpar = false;
					if ( symbol.token == Token.LEFTPAR ) {
						leftpar = true;
						next();
					}
					SymbolIdent parameterSymbol;
					Expr typeInDec = type();


					if ( symbol.token != Token.IDENT ) {

						if ( typeInDec instanceof ExprIdentStar ) {
							ExprIdentStar eisType = (ExprIdentStar ) typeInDec;
							if ( eisType.getIdentSymbolArray().size() > 1 ) {
								// found a prototype preceded by a package
								parameterSymbol = null;
							}
							else if ( Character.isUpperCase(eisType.getIdentSymbolArray().get(0).getSymbolString().charAt(0)) ) {
								/*
								 *  typeInDec is a type really
								 */
								parameterSymbol = null;
							}
							else {
								/*
								 * no package, starts with a lower case letter. It must be a parameter
								 */
								parameterSymbol = (SymbolIdent) eisType.getIdentSymbolArray().get(0);
								typeInDec = null;
							}

						}
						else {
							// typeInDec may be a generic prototype instantiation
							parameterSymbol = null;
						}
					}
					else {
						parameterSymbol = (SymbolIdent ) symbol;

						if ( Character.isUpperCase(parameterSymbol.getSymbolString().charAt(0)) ) {
							this.error2(parameterSymbol, "Variables and parameters cannot start with an uppercase letter");
							return null;
						}


						next();
					}
					if ( leftpar ) {
						if ( symbol.token != Token.RIGHTPAR ) {
							error2(symbol, "')' expected." + foundSuch());
							return null;
						}
						else {
							if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
								error2(symbol, "letter, number, or '_' after ')'");
								return null;
							}
							next();
						}
					}
					ParameterDec parameterDec = new ParameterDec( parameterSymbol, typeInDec, currentMethod);
					if ( this.functionStack.size() > 0 )
						parameterDec.setDeclaringFunction(functionStack.peek());

					mso.setOptionalParameter( parameterDec );
					parameterDecStack.push(parameterDec);
					if ( operatorSymbol.token == Token.BITNOT ) {
						this.error2(operatorSymbol, "This operator cannot be used as a binary method. It should not take a parameter");
						return null;
					}
				}
				else {
					// without parameters: then it should be a unary operator
					if ( ! Compiler.isUnaryOperator(operatorSymbol.token) ) {
						this.error2(operatorSymbol, "This operator cannot be used as a unary method. It should take a parameter");
						return null;
					}
				}
			}
			else {
				this.error2(symbol,  "A method name was expected. " + this.foundSuch());
				return null;
			}
		}
		isInit = name.equals("init") || name.equals("init:");
		if ( isInit || isInitOnce || name.equals("new") || name.equals("new:") ) {
			if ( currentProgramUnit instanceof InterfaceDec ) {
				error(true, symbol, "Methods 'init', 'init:', and 'initOnce' cannot be declared in interfaces",
						"init", ErrorKind.init_new_methods_cannot_be_declared_in_interfaces);
			}

			if ( this.currentMethod.getHasOverride() )
				error(true,
						symbol, "Methods 'init', 'init:', and 'initOnce' cannot be declared with 'override'",
						"init", ErrorKind.init_should_not_be_declared_with_override);
			if ( currentMethod.isAbstract() )
				error(true,
						symbol, "Methods 'init', 'init:', and 'initOnce' cannot be declared 'abstract'",
						"init", ErrorKind.init_should_not_be_abstract);
			if ( isInit && currentMethod.getVisibility() != Token.PUBLIC )
				error(true,
						symbol, "'init' and 'init:' methods should be public",
						"init", ErrorKind.init_new_should_be_public);

			if ( ! isInit && ! isInitOnce ) {
				if ( this.cyanMetaobjectContextStack.isEmpty() ) {
					// user-made code
					this.error2(identSymbol, "'new' and 'new:' cannot be user-defined");
				}
			}


		}


		if ( symbol.token == Token.RETURN_ARROW ) {
			lexer.checkWhiteSpaceParenthesisBeforeAfter(symbol, symbol.symbolString);


			next();

			returnType = type();


			if ( (isInit || isInitOnce) && returnType != null ) {
				error( true, returnType.getFirstSymbol(),
						"'init:', 'init', and 'initOnce' methods cannot have a return value",
						identSymbol.getSymbolString(), ErrorKind.init_should_return_Nil);
			}
		}

		methodSignature.setReturnTypeExpr(returnType);
		return methodSignature;
	}

	private void refactorChangeId_to_IdDot(Symbol identSymbol) {
		if ( symbol.token == Token.COLON ) {
			warning(identSymbol, identSymbol.getLineNumber(), "':' unexpected right after '" + identSymbol.getSymbolString() + "'");
			if ( ask(symbol, "Should I glue ':' right after " + identSymbol.getSymbolString() +
					" ? (y, n)") ) {
				int sizeIdentSymbol = identSymbol.getSymbolString().length();
				compilationUnit.addAction(
						new ActionDelete(compilationUnit,
								symbol.startOffsetLine + symbol.getColumnNumber() - 1,
								1,
								symbol.getLineNumber(),
								symbol.getColumnNumber()+ 1));
				compilationUnit.addAction(
						new ActionInsert(":", compilationUnit,
								identSymbol.startOffsetLine + identSymbol.getColumnNumber() - 1 +
								sizeIdentSymbol,
								identSymbol.getLineNumber(),
								identSymbol.getColumnNumber()+ sizeIdentSymbol));

			}

		}
	}


	public MethodSignatureGrammar methodSignatureGrammarForMetaobject() {


		Symbol first = symbol;
		SelectorGrammar sg = selectorGrammar();

		if ( sg.matchesEmptyInput() ) {
			this.error2(first, "This regular expression matches an empty input, which is illegal");
		}
		return new MethodSignatureGrammar(sg, currentMethod);
	}

	private SelectorGrammar selectorGrammar() {

		Symbol firstSymbol = symbol;

		SelectorGrammar aSelectorGrammar;

		if ( symbol.token != Token.LEFTPAR ) {
			this.error2(symbol, "'(' expected");
		}
		else {
			next();
		}
		Selector selector = selectorUnit();
		ArrayList<Selector>  selectorArray = new ArrayList<Selector>();
		selectorArray.add(selector);
		if ( symbol.token == Token.BITOR ) {
			while ( symbol.token == Token.BITOR ) {
				next();
				selector = selectorUnit();
				selectorArray.add(selector);
			}
			aSelectorGrammar = new SelectorGrammarOrList(selectorArray, firstSymbol);
		}
		else {
			while ( symbol.token == Token.IDENTCOLON ||
					symbol.token == Token.LEFTPAR ) {
				selector = selectorUnit();
				selectorArray.add(selector);
			}
			aSelectorGrammar = new SelectorGrammarList(selectorArray, firstSymbol);
		}
		if ( symbol.token != Token.RIGHTPAR ) {
			error2(symbol, "')' expected." + foundSuch());
			skipTo(Token.RETURN_ARROW, Token.LEFTCB);
		}
		else {
			if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
				error2(symbol, "letter, number, or '_' after ')'");
			}
			next();
		}
		switch ( symbol.token ) {
		case PLUS:
			aSelectorGrammar.setRegularOperator(symbol);
			next();
			break;
		case MULT:
			aSelectorGrammar.setRegularOperator(symbol);
			next();
			break;
		case QUESTION_MARK:
			aSelectorGrammar.setRegularOperator(symbol);
			next();
			break;
		default:
			break;
		}

		return aSelectorGrammar;
	}


	private Selector selectorUnit() {
		if ( symbol.token == Token.LEFTPAR )
			return selectorGrammar();
		else {
			// here comes the analysis of the rule SelecGrammarElem
			if ( symbol.token != Token.IDENTCOLON ) {
				error2(symbol, "Id:  expected." + foundSuch());
				skipTo(Token.RETURN_ARROW, Token.LEFTCB);
			}
			String nameSelector = symbol.getSymbolString();
			if ( nameSelector.compareTo("init:") == 0 || nameSelector.compareTo("new:") == 0 ) {
				error(true,
						symbol, "'init:' and 'new:' selectors cannot appear in a grammar method",
						nameSelector, ErrorKind.init_new_should_not_appear_in_grammar_method);
			}
			Symbol symbolIdent = symbol;
			next();
			if ( startType(symbol.token) ) {
				// IdColon TypeOneManyList
				ArrayList<Expr> typeList = new ArrayList<Expr>();
				Expr t;

				try {
					this.prohibitTypeof = true;
					t = type();
				}
				finally {
					this.prohibitTypeof = false;
				}


				typeList.add(t);
				while ( symbol.token == Token.COMMA ) {
					if ( this.currentProgramUnit.getOuterObject() == null ) { lexer.checkWhiteSpaceParenthesisAfter(symbol, ","); }
					next();

					try {
						this.prohibitTypeof = true;
						t = type();
					}
					finally {
						this.prohibitTypeof = false;
					}

					typeList.add(t);
				}
				// it is easy to debug by first putting the object in a
				// variable such as selectorWithTypes and only after
				// that returning it
				SelectorWithTypes selectorWithTypes =
					new SelectorWithTypes(symbolIdent, typeList);
				return selectorWithTypes;
			}
			else if ( symbol.token == Token.LEFTPAR ) {
				if ( startType(next(0).token) ) {
				next();
				Expr t;

				try {
					this.prohibitTypeof = true;
					t = type();
				}
				finally {
					this.prohibitTypeof = false;
				}


				SelectorWithMany selectorWithMany =
					new SelectorWithMany(symbolIdent, t);
				if ( symbol.token != Token.RIGHTPAR ) {
					error2(symbol, "')' expected." + foundSuch());
					skipTo(Token.RETURN_ARROW, Token.LEFTCB);
				}
				if ( Lexer.hasIdentNumberAfter(symbol, compilationUnit) ) {
					error2(symbol, "letter, number, or '_' after ')'");
				}
				next();
				if ( symbol.token != Token.MULT &&
						symbol.token != Token.PLUS ) {
					error2(symbol, "'+' or '*' expected." + foundSuch());
					skipTo(Token.RETURN_ARROW, Token.LEFTCB);
				}
				else {
					selectorWithMany.setRegularOperator(symbol);
					next();
				}
				return selectorWithMany;
				}
				else
					return new SelectorWithTypes(symbolIdent);

			}
			else {
				// a selector without types such as "read:" in
				// public func (open: String read:) :t UTuple<String, void>
				SelectorWithTypes selectorWithTypes =
					new SelectorWithTypes(symbolIdent, new ArrayList<Expr>());
				return selectorWithTypes;
			}
		}
	}


	/**
	 * searchs the list of local variables and parameters for identString. Returns the local variable or parameter found
	 */
	private VariableDecInterface searchIdent(String identString) {
		for ( StatementLocalVariableDec var : this.localVariableDecStack ) {
			if ( var.getName().compareTo(identString) == 0 )
				return var;
		}
		for ( ParameterDec p : parameterDecStack ) {
			//if ( p.getName() == null || identString == null )
				//System.exit(1);
			if ( p.getName() != null && p.getName().compareTo(identString) == 0 )
				return p;
		}
		return null;
	}




	/**
	 * parameters, local variables, and instance variables cannot
	 * have name equal to one of the formal parameters of a generic prototype.
	 * Then it is illegal to write:
	 *    object Wrong<:T>
	 *        private let Int T
	 *        private String T
	 *        ...
	 *    end
	 *
	 *  If the current prototype is generic, this method takes an identifier
	 *  and checks whether it is equal to one of the formal parameters. It
	 *  returns true if it is and false otherwise
	 */

	public boolean equalToFormalGenericParameter(String ident) {
		if ( this.currentProgramUnit.isGeneric() ) {
			for ( ArrayList<GenericParameter> genericParameterList : currentProgramUnit.getGenericParameterListList() ) {
				for ( GenericParameter genericParameter : genericParameterList ) {
					if ( ident.compareTo(genericParameter.getName()) == 0 )
						return true;
				}
			}
		}
		return false;
	}

	/**
	 *  returns true if token t can start a unary method or an operator method.
	 *  Example of uses of unary methods:
	 *       f1 get
	 *       f1 # get
	 */
	public static boolean startUnaryOperatorMethodName( Token t ) {
		return t == Token.IDENT ;
	}

	/**
	 * return true if token t can start a method name
	 * @param t
	 * @return
	 */
	public static boolean startMethodName( Token t ) {
		return t == Token.IDENT ||
		       t == Token.IDENTCOLON ||
		       isOperator(t);

	}

	/**
	 * return true if 'e' is a type, which should start with an upper-case letter without any dots as
	 * "Int", "Program" or it should be a package name followed by a prototype name as
	 * "main.Program", "cyan.lang.Int"
	 */
    static public boolean isType(Expr e) {

    	if ( e instanceof ExprIdentStar ) {
    		ExprIdentStar eis = (ExprIdentStar) e;
    		String firstName = eis.getIdentSymbolArray().get(0).getSymbolString();
    		return eis.getIdentSymbolArray().size() > 1 ||
    				Character.isUpperCase(firstName.charAt(0));
    	}
    	else
    		return true;
    }

	/**
	 *  returns true if token t can be a selector of a message send such as
	 *       get
	 *       #get
	 *       put:
	 *       print:
	 *       #print:
	 *       +
	 *       <<
	 */
	public static boolean canStartMessageSelector( Token t ) {
		return t == Token.IDENT        || t == Token.IDENTCOLON  ||
		       t == Token.INTER_DOT_ID  || t == Token.INTER_DOT_ID_COLON;
	}



	public static boolean isOperator( Token t ) {

		return
				t == Token.AND ||
				t == Token.BITAND ||
				t == Token.BITNOT ||
				t == Token.BITOR ||
				t == Token.BITXOR ||
				t == Token.DIV ||
				t == Token.DOT_PLUS ||
				t == Token.DOT_STAR ||
				t == Token.EQ ||
				t == Token.GE ||
				t == Token.EQEQEQ ||
				t == Token.GT ||
				t == Token.LE ||
				t == Token.LEG ||
				t == Token.LEFTSHIFT ||
				t == Token.LT ||
				t == Token.MINUS ||
				t == Token.MULT ||
				t == Token.NEQ ||
				t == Token.NOT ||
				t == Token.OR ||
				t == Token.PLUS ||
				t == Token.REMAINDER ||
				t == Token.RIGHTSHIFT ||
				t == Token.RIGHTSHIFTTHREE ||
				t == Token.TWOPERIOD ||
				t == Token.TWOPERIODLT ||
				t == Token.XOR ||
				t == Token.TILDE_EQUAL ||
				t == Token.EQGT ||
				t == Token.EQEQGT ||
				t == Token.PLUSPLUS ||
				t == Token.MINUSMINUS ||
				t == Token.ORGT;

	}

	/*
	 *       ==   <=   <   >   >=   !=   ===   <=>   ~=
	 */
	public static boolean isRelationalOperator(Token t) {
		return t == Token.EQ ||
		t == Token.GE ||
		t == Token.GT ||
		t == Token.LE ||
		t == Token.LT ||
		t == Token.NEQ ||
		t == Token.LEG ||
		t == Token.EQEQEQ ||
		t == Token.TILDE_EQUAL;
	}

	public static boolean isUnaryOperator(Token t) {
		return t == Token.PLUS ||
		t == Token.MINUS ||
		t == Token.NOT ||
		t == Token.BITNOT;
	}

	public static boolean isBinaryOperator(Token t) {
		return
				t == Token.AND ||
				t == Token.BITAND ||
				t == Token.BITOR ||
				t == Token.BITXOR ||
				t == Token.DIV ||
				t == Token.EQ ||
				t == Token.GE ||
				t == Token.GT ||
				t == Token.LE ||
				t == Token.LEFTSHIFT ||
				t == Token.LT ||
				t == Token.MINUS ||
				t == Token.MULT ||
				t == Token.NEQ ||
				t == Token.OR ||
				t == Token.PLUS ||
				t == Token.REMAINDER ||
				t == Token.RIGHTSHIFT ||
				t == Token.RIGHTSHIFTTHREE ||
				t == Token.TWOPERIOD ||
				t == Token.XOR;

	}

	public static boolean startProgramUnit(Token t) {
		return t == Token.METAOBJECT_ANNOTATION ||
		       t == Token.PUBLIC ||
		       t == Token.PRIVATE ||
		       t == Token.PROTECTED ||
		       t == Token.MIXIN ||
		       t == Token.OBJECT ||
		       t == Token.INTERFACE;
	}


	public static boolean startExpr(Symbol sym) {

		Token t = sym.token;
		return
				t == Token.SUPER ||
				t == Token.SELF  ||
				t == Token.IDENT ||
				t == Token.LITERALSTRING ||
				t == Token.STRING ||
				t == Token.LEFTCB ||
				t == Token.LEFTPAR ||
				t == Token.LEFTSB_DOT ||
				t == Token.CYANSYMBOL ||
				t == Token.PLUS ||
				t == Token.MINUS ||
				t == Token.NOT ||
				t == Token.BITNOT ||
				t == Token.LEFTSB ||
				startType(t) ||
				isBasicTypeLiteral(t) ||
				t == Token.LITERALOBJECT ||
				t == Token.METAOBJECT_ANNOTATION;
				// (t == Token.LITERALOBJECT && Lexer.checkLeftCharSeq(sym.getSymbolString()));
	}

	public static boolean isBasicTypeLiteral(Token t) {

		return t == Token.BYTELITERAL || t == Token.SHORTLITERAL ||
		t == Token.INTLITERAL  || t == Token.LONGLITERAL ||
		t == Token.FLOATLITERAL || t == Token.DOUBLELITERAL ||
		t == Token.CHARLITERAL || t == Token.TRUE || t == Token.FALSE || t == Token.NIL ||
		t == Token.LEFTSB;
	}


	public static boolean startType(Token t) {
		return t == Token.IDENT ||
		       t == Token.TYPEOF  ||
		       t == Token.STRING ||
		       t == Token.DYN ||
		       isBasicType(t);

	}



	public static boolean isBasicType(Token t) {
		return 	t == Token.BYTE || t ==  Token.SHORT || t ==  Token.INT ||
		t ==  Token.LONG || t ==  Token.FLOAT || t ==  Token.DOUBLE ||
		t ==  Token.CHAR || t ==  Token.BOOLEAN || t == Token.NIL || t == Token.STRING;
	}

	/**
	 * skip tokens till symbol.token is one of the tokens of array tokenArray
	 * @param tokenArray
	 * @return
	 */
	private boolean skipTo( Token ... tokenArray) {
		int i;
		while ( symbol.token != Token.EOF ) {
			i = 0;
			while ( i < tokenArray.length ) {
				if ( symbol.token == tokenArray[i++] )
					return true;
			}
			next();
		}
		return false;

	}


	@SuppressWarnings("static-method")
	private boolean ask(Symbol sym, String message) {
		return false;
		/*
		boolean ret = false;
		String yesNo;
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("File " + compilationUnit.getFilename() +
					" (" + sym.getLineNumber() + ", " + sym.getColumnNumber() + ")");
			System.out.println(message);
			while ( true ) {
				yesNo = null;
				while ( yesNo == null ) {
					try {
						yesNo = sc.nextInt() == 'y' ? "y" : "n";
					} catch ( NoSuchElementException e) {
					}

				}
				if ( yesNo.compareTo("y") == 0 || yesNo.compareTo("n") == 0 )
					break;
				else
					System.out.println("type y or n");
			}
			ret = yesNo.compareTo("y") == 0;
			sc.close();
			return ret;
		} catch (Exception e) {
			return false;
		}
	*/
	}

	/**
	 * the context of the code generated by a metaobject annotation. The elements of each tuple are: an identifier, the
	 * metaobject name, the package name of the metaobject annotation or the project name (.pyan file),
	 * the prototype name of the metaobject annotation, and
	 * the line number of the annotation. If this stack is not empty and there is a compilation error, then the
	 * code that caused the error was introduced by a metaobject annotation. This code was generated by a
	 * metaobject annotation in the prototype specified in the tuple. That is, the statement </p>
	 * <code>cyanMetaobjectContextStack.push(new Tuple5<...>(...))</code></p>
	 * is called before the compilation of the code generated by the metaobject and </p>
	 * <code>cyanMetaobjectContextStack.pop()</code></p>
	 * is called after the compilation of the code generated by the metaobject. The compiler itself,
	 * when generating code for a metaobject, inserts annotations <code>{@literal @}pushCompilationContext</code>
	 * and  <code>{@literal @}popCompilationContext</code>.
	 *
	 */
	private Stack<Tuple5<String, String, String, String, Integer>> cyanMetaobjectContextStack;

	public void warning(Symbol sym, int lineNumber, String msg) {
		compilationUnit.warning(sym, lineNumber, msg, this, null);
	}

	public void warning(Symbol sym, String msg) {
		try {
			error2(sym, msg);
		}
		catch ( RuntimeException e ) {

		}
	}

	public void error2(Symbol sym, String msg) {
		error2(true, sym, msg, true);
	}

	public void error2(boolean throwException, Symbol sym, String msg) {
		error2(throwException, sym, msg, true);
	}

	public void error2(Symbol sym, String msg, boolean checkMessage) {
		error2(true, sym, msg, checkMessage);
	}

	public void error2(boolean throwException, Symbol sym, String msg, boolean checkMessage) {
		error2(throwException, sym, -1, -1, msg, checkMessage);
	}

	public void error2(boolean throwException, Symbol sym, int lineNumber, int columnNumber, String msg, boolean checkMessage) {

		if ( mayBeWrongVarDeclaration ) {
			// this may be an error caused by a missing 'var' or 'let' before
			// a variable declaration
			msg = msg + ". If your intension was to declare a variable, there is a missing 'var' or 'let' like in 'var Int n'";
			mayBeWrongVarDeclaration = false;
		}

		int lineNum = sym == null ? lineNumber : sym.getLineNumber();

		if ( !cyanMetaobjectContextStack.isEmpty() || insideCyanMetaobjectCompilationContextPushAnnotation ) {
			if ( sym != null && lineNum > 0 ) {
				lineNum -= lineShift;
			}
		}


		if ( checkMessage )
			if ( checkErrorMessage(sym, lineNum, msg) )
				return;


		msg = addContextMessage(cyanMetaobjectContextStack, msg);

		compilationUnitSuper.error(throwException, sym,  lineNum, columnNumber, msg, this, null);
	}


	public void error2(int lineNum, String msg, boolean checkMessage) {

		if ( checkMessage )
			if ( checkErrorMessage(null, lineNum, msg) )
				return;


		msg = addContextMessage(cyanMetaobjectContextStack, msg);
		compilationUnitSuper.error(null, lineNum, msg, this, null);
	}

	public void error2(boolean throwException, int lineNumber, int columnNumber, String msg) {


		compilationUnitSuper.error(lineNumber, columnNumber, msg);
		if ( throwException ) {
			throw new CompileErrorException();
		}

	}

	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
		CyanMetaobject metaobject = metaobjectAnnotation.getCyanMetaobject();
		if ( metaobject instanceof CyanMetaobjectLiteralString || metaobject instanceof CyanMetaobjectWithAt ) {
			error2(true, null, metaobjectAnnotation.getFirstSymbol().getLineNumber() + lineNumber - 1, columnNumber, message, true);
		}
	}



	/** If necessary, add a context message to the error message. This context informs that the error was caused
	 * by code introduced by such and such metaobject annotations OR by code introduced by the compiler.
	 *
	   @param msg
	   @return
	 */
	public static String addContextMessage(Stack<Tuple5<String, String, String, String, Integer>> contextStack, String msg) {


		if ( ! contextStack.isEmpty() ) {
			/*
			 * there is a context. Then the code that caused this compilation error was
			 * introduced by some metaobject annotation or by the compiler
			 */
			Tuple5<String, String, String, String, Integer> t = contextStack.peek();

			if ( t.f3 == null && t.f4 == null && t.f5 == null ) {
				if ( !msg.endsWith(".") )
					msg = msg + ".";
				msg = msg + " This error was caused by code introduced by the compiler in step '" + t.f2 +
						"'. Check the documentation of CyanMetaobjectCompilationContextPush";
			}
			else {
				String cyanMetaobjectName = t.f2;
				String packageName = t.f3;
				String sourceFileName = t.f4;
				int lineNumber = t.f5;
				if ( !msg.endsWith(".") )
					msg = msg + ".";
				msg = msg + " This error was caused by code introduced initially by metaobject annotation '" + cyanMetaobjectName +
						"' at line " + lineNumber + " of file " + sourceFileName + " of package " + packageName ;

				if ( contextStack.size() > 1) {
					String s =  ". The complete stack of " +
							"context (metaobject name, package.prototype, line number) is: ";
					for(int kk = 1; kk < contextStack.size(); ++kk) {
						t = contextStack.get(kk);
						cyanMetaobjectName = t.f2;
						packageName = t.f3;
						sourceFileName = t.f4;
						lineNumber = t.f5;
						s += "(" + cyanMetaobjectName + ", " + packageName + "." + sourceFileName + ", " + lineNumber + ") ";
					}
					msg = msg + s;
				}
			}

		}
		return msg;
	}

	/**
	 * A metaobject may have implemented interface {@link meta#IInformCompilationError} and informed the compiler
	 * that an error should be signaled. If any did, this method
	 * checks whether the error message passed as parameter was foreseen. If it wasn´t, a warning is signaled.
	 *
	 * @param sym
	   @param lineNumber
	   @param specificMessage
	   @return true if there was a previous call to compilationError
	 */
	private boolean checkErrorMessage(Symbol sym, int lineNumber, String specificMessage) {

		if ( compilationUnit == null )
			return false;

		if ( compilationUnit.getLineMessageList().size() == 0 )
			return false;
		else
		{
			/*
			 * A metaobject have implemented interface {@link meta#IInformCompilationError} and informed the compiler
	 			that an error should be signaled
			 */
			int i = 0;
			boolean found = false;
			for ( Tuple3<Integer, String, Boolean> t: compilationUnit.getLineMessageList() ) {
				if ( (lineNumber < 0 && t.f1 < 0) || ( t.f1 == lineNumber ) ) {
					// found the correct line number
					int correctLineNumber;
					if ( lineNumber < 0 )
						correctLineNumber = -1;
					else
						correctLineNumber = lineNumber;

					this.warning(sym, correctLineNumber,
							"The expected error message was '" + t.f2 + "'. The message given by the compiler was '" +
					   specificMessage + "'");
					compilationUnit.getLineMessageList().get(i).f3 = true;
					found = true;
					throw new CompileErrorException();
				}
				++i;
			}
			if ( ! found ) {  // # how lineshift is 42 in file p1.E.cyan ?
				this.warning(sym, lineNumber,
						"The compiler issued the error message '" +
				   specificMessage + "'. However, no metaobject implementing 'IInformCompilationError' has foreseen this error");
			}
			return true;
		}
	}
	public void error(boolean checkMessage, Symbol sym, String specificMessage, String identifier, ErrorKind errorKind, String ...furtherArgs  ) {
		error(true, checkMessage, sym, specificMessage, identifier, errorKind, furtherArgs );
	}

	public void error(boolean throwException, boolean checkMessage, Symbol sym, String specificMessage, String identifier, ErrorKind errorKind, String ...furtherArgs  ) {
		error2(throwException, sym, specificMessage, checkMessage );
	}
	public void error(boolean useEMS, boolean throwException, boolean checkMessage, Symbol sym, String specificMessage, String identifier, ErrorKind errorKind, String ...furtherArgs  ) {

		/*if ( checkErrorMessage(sym, sym == null ? -1 : sym.getLineNumber(), specificMessage) )
			return;
		*/

		error2(throwException, sym, specificMessage, checkMessage );

		if ( ! useEMS )
			return;

		/*
		 * if not call error2 above, call addContextMessage with specificMessage
		 */
		ArrayList<String> sielCode = new ArrayList<String>();
		sielCode.add("error = \"" + specificMessage + "\"");
		for ( String field : errorKind.getFieldList() ) {
			switch ( field ) {
			case "implementedInterfaces" :
				if ( currentProgramUnit != null && currentProgramUnit instanceof ObjectDec &&
          				((ObjectDec ) currentProgramUnit).getSuperobjectExpr() != null ) {
					ObjectDec currentProto = ((ObjectDec ) currentProgramUnit);
					ArrayList<Expr> exprList = currentProto.getInterfaceList();
					int sizeExprList = exprList.size();
					String s = "";
					for ( Expr ee : exprList ) {
						s = s + ee.asString();
						if ( --sizeExprList > 0 )
							s = s + ", ";
					}
					sielCode.add("implementedInterfaces = \"" + s + "\"");
					error2(null, "Internal error in Compiler::error: field '" + field + "' of Siel cannot be used here");
		        }
				break;

			case "supertype" :
				if ( currentProgramUnit != null && currentProgramUnit instanceof ObjectDec &&
						((ObjectDec ) currentProgramUnit).getSuperobjectExpr() != null ) {
					sielCode.add("supertype = \"" + ((ObjectDec ) currentProgramUnit).getSuperobjectExpr().asString() + "\"");
					error2(null, "Internal error in Compiler::error: field '" + field + "' of Siel cannot be used here");
				}
				break;
			case "identifier":
				sielCode.add("identifier = \"" + identifier + "\"");
				break;
			case "statementText":
				if ( ! (currentProgramUnit instanceof InterfaceDec) ) {
					sielCode.add("statementText = \"" + lexer.stringStatementFromTo(this.startSymbolCurrentStatement, sym) + "\"");
				}
				break;
			case "methodSignature":
				if ( currentMethod != null ) {
					sielCode.add("methodSignature = \"" + currentMethod.stringSignature() + "\"");
				}
				break;
			case "prototypeName":
				sielCode.add("prototypeName = \"" +currentProgramUnit.getName() + "\"");
				break;
			case "interfaceName":
				sielCode.add("interfaceName = \"" + identifier + "\"");
				break;
			case "packageName":
				sielCode.add("packageName = \"" + compilationUnit.getPackageName());
				break;
			case "importList":
				String strImportList = "";
				for ( ExprIdentStar e : compilationUnit.getImportPackageList() )
					strImportList = strImportList + " " + e.getName();
				sielCode.add("importList = \"" + strImportList + "\"");
				break;
			case "metaobject":
				sielCode.add("metaobject = \"" + identifier + "\"");
				break;
			case "returnType":
				if ( currentMethod != null ) {
					sielCode.add("returnType = \"" + currentMethod.getMethodSignature().getReturnTypeExpr().asString() + "\"");
				}
				break;
			default:
				String keyValue = null;
				String fieldName;
				for ( String other : furtherArgs ) {
					int i = other.indexOf("=");
					if ( i > 0 ) {
						while ( i > 0 && other.charAt(i-1) == ' ')
							--i;
						if ( i > 0 )  {
							fieldName = other.substring(0, i);
							if ( fieldName.equals(field) ) {
								keyValue = other;
								break;
							}
						}

					}
					else
						error2(null, "Internal error in Env::error: error called without a key/value pair");

				}
				if ( keyValue != null )
					sielCode.add(keyValue);
				else
					error2(null, "Internal error in Env::error: field '" + field + "' of Siel was not recognized");
				return;
			}
		}

	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public void setProgramUnit(ProgramUnit programUnit) {
		this.currentProgramUnit = programUnit;
	}


	public ProgramUnit getCurrentProgramUnit() {
		return currentProgramUnit;
	}

	public MethodDec getCurrentMethod() {
		return this.currentMethod;
	}


	/**
	 * checks whether the name of the source file is according to the Cyan rules (see the manual).
	 */
	private void parseSourceFileName() {

	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Project getProject() {
		return project;
	}

	/**
	 * if the parameter is true the lexer should consider the character new line as a token.
	 * See {@link Lexer#setNewLineAsToken(boolean)}.
	   @param newLineAsToken
	 */
	public void setNewLineAsToken(boolean newLineAsToken) {
		lexer.setNewLineAsToken(newLineAsToken);
	}


	/**
	 * return true if the current token is inside a context function
	   @return
	 */
	private boolean insideContextFunction() {
		 for ( ExprFunction f :  functionStack ) {
			 if ( f.isContextFunction() )
				 return true;
		 }
		 return false;
	}


	public Stack<Tuple5<String, String, String, String, Integer>> getCyanMetaobjectContextStack() {
		return cyanMetaobjectContextStack;
	}


	public static Symbol[] getSymbolList() {
		cleanSymbolList = new Symbol[symbolList.length];
		boolean insidePushCompilationContext = false;
		sizeCleanSymbolList = 0;
		for (int i = 0; i < sizeSymbolList; ++i ) {
			Symbol sym = symbolList[i];
			if ( sym instanceof lexer.SymbolCyanMetaobjectAnnotation ) {
				SymbolCyanMetaobjectAnnotation symMetaobjectAnnotation = (SymbolCyanMetaobjectAnnotation ) sym;
				if ( symMetaobjectAnnotation.getCyanMetaobjectAnnotation().getCyanMetaobject()
						instanceof meta.cyanLang.CyanMetaobjectCompilationContextPush ) {
					insidePushCompilationContext = true;
				}
			}
			if ( ! insidePushCompilationContext ) {
				cleanSymbolList[sizeCleanSymbolList] = sym;
				++sizeCleanSymbolList;
			}
			if ( sym instanceof lexer.SymbolCyanMetaobjectAnnotation ) {
				SymbolCyanMetaobjectAnnotation symMetaobjectAnnotation = (SymbolCyanMetaobjectAnnotation ) sym;
				if ( symMetaobjectAnnotation.getCyanMetaobjectAnnotation().getCyanMetaobject()
						instanceof meta.cyanLang.CyanMetaobjectCompilationContextPop ) {
					insidePushCompilationContext = false;
				}
			}

		}
		return cleanSymbolList;
	}


	public static int getSizeSymbolList() {
		return sizeSymbolList;
	}


	public ArrayList<CyanMetaobjectWithAtAnnotation> getCodegList() {
		return codegList;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void addToListAfter_ati(IListAfter_ati annotation) {
		this.project.getProgram().addToListAfter_ati(annotation);
	}



	private void thrownException(CyanMetaobjectAnnotation annotation, Symbol firstSymbol, RuntimeException e) {
		String prototypeName = annotation.getPrototypeOfAnnotation();
		String packageName = annotation.getPackageOfAnnotation();
		int lineNumber = annotation.getFirstSymbol().getLineNumber();
		error2(firstSymbol, "Metaobject '" + annotation.getCyanMetaobject().getName() + "' " +
			"called at line number " + lineNumber + " in " +
				packageName + "." + prototypeName + " has thrown exception '" + e.getClass().getName() + "'");

	}

	public ProgramUnit searchPackagePrototype(String packageNameInstantiation, String prototypeNameInstantiation) {
		CyanPackage cyanPackage = this.getProject().searchPackage(packageNameInstantiation);
		if ( cyanPackage == null )
			return null;
		ProgramUnit pu = cyanPackage.searchPublicNonGenericProgramUnit(prototypeNameInstantiation);

		return pu;
	}

	public CompilationStep getCompilationStep() {
		return compilationStep;
	}

	public char[] getText(int offsetLeftCharSeq, int offsetRightCharSeq) {
		return lexer.getText(offsetLeftCharSeq, offsetRightCharSeq);
	}


	public void pushRightSymbolSeq(String rightSymbolSeq) {
		lexer.pushRightSymbolSeq(rightSymbolSeq);
	}

	public void setAllowCreationOfPrototypesInLastCompilerPhases(boolean allowCreationOfPrototypesInLastCompilerPhases) {
		this.allowCreationOfPrototypesInLastCompilerPhases = allowCreationOfPrototypesInLastCompilerPhases;
	}


	/** next symbol of the input    */

	public Symbol symbol;

	/**
	 * This class is used for compiling Cyan code and Project code (method parseProject).
	 * In this last case, instance variable projectFilename contains the project file
	 * name.
	 */
	private String projectFilename;

	/**
	 * name of the filename in which objectName is declared
	 */
	private String filename;
	/**
	 * The compilation unit that is currently being compiled. It is composed
	 * by all program units declared in the same source file.
	 */
	private CompilationUnit compilationUnit;

	/**
	 * The compilation unit that is currently being compiled. It may be either
	 * a Cyan source file or a project file. In the first case, {@link #compilationUnit}
	 * refer to the same object as {@link #compilationUnitSuper}.
	 */
	private CompilationUnitSuper compilationUnitSuper;


	/**
	 * the current program unit (object or interface) being compiled
	 */
	private ProgramUnit currentProgramUnit;

	/**
	 * a stack of prototypes. When the compiler starts to analyze a prototype
	 * it pushes it into the stack. There is at most one level of inner prototypes
	 * (prototype declared inside prototype) so this stack has at most two elements.
	 */
	private Stack<ObjectDec> objectDecStack;
	/**
	 * the current method being compiled. Or the last compiled if the Compiler
	 * is not compiling a method when this variable is accessed.
	 */
	private MethodDec currentMethod;

	/**
	 * number of public or protected prototypes (objects or interfaces) in the current source file
	 */
	private int numPublicPackageProgramUnits;


	/**
	 * a stack of functions. In point 1 below there will be two functions in the stack
	 *     func test {
	 *        var b = {   var c = {  // 1
	 *        } };
	 *     ]
	 */
	private Stack<ExprFunction> functionStack;

	/**
	 * stack of visible parameters
	 */
	private Stack<ParameterDec> parameterDecStack;
	/**
	 * stack of visible local variables
	 */
	private Stack<StatementLocalVariableDec> localVariableDecStack;

	/*#
	 * stack used for compiling expressions. If
	 * the top of isMessageSendStack is true then
	 * the current expression (call to method expr() )
	 * may be a message send.

	private Stack<Boolean> isMessageSendStack; #*/


	/**
	 * a counter of the number of functions inside a compilation unit
	 */
	private int functionCounter;

	/**
	 * the lexer
	 */
	private Lexer lexer;

	/**
	 * previous symbol
	 */
	private Symbol previousSymbol;
	/**
	 * array with the next symbols. Each entry is null if the
	 * next symbols were not found yet
	 */
	private Symbol nextSymbolList[];

	/**
	 * number of nested if´s and while´s
	 */
	private int nestedIfWhile;
	/**
	 * number of nested while´s and for´s. If this number is greater than 0,
	 * then a command 'break' is legal.
	 */
	private int whileForCount;
	/**
	 * symbol that starts the current statement
	 *
	 */
	public Symbol startSymbolCurrentStatement;
	/**
	 * the number of the current method. The first method declared has number 0.
	 */
	private int methodNumber;

	/**
	 * a table with all macros imported by the current compilation units. The 'key' of the table is
	 * the start macro keyword such as "assert" in macro assert:
	 *        assert n >= 0;
	 */
	private HashMap<String, CyanMetaobjectMacro> metaObjectMacroTable;

	/**
	 * the instruction set to the compilation
	 */
	private HashSet<saci.CompilationInstruction> compInstSet;

	/**
	 * context number used to generated unique identifiers to {@literal @}pushCompilationContext and {@literal @}popCompilationContext
	 */
	private static int contextNumber = 0;
	/**
	 *  Code may be inserted into a compilation unit by the compiler itself (see Figure of chapter Metaobjects of the Cyan manual)
	 *  or by metaobject annotations. Code may be deleted from a compilation unit by metaobject annotations. Therefore when the compiler
	 *  finds an error it may point to the wrong line. If code was inserted before the error the message would point to a line
	 *  number greater than it is. The opposite happens when code was deleted. Variable lineShift keeps how many lines were inserted
	 *  in the code. If negative, -lineShift is how many lines were deleted.
	 */
	private int lineShift;

	/**
	 * To each anonymous function inside the prototype and for each method the compiler
	 * adds an inner prototype. This variable is true if this method,
	 * {@link Compiler#objectDec}, has already added these inner prototypes to
	 * the current prototype
	 */
	private boolean hasAddedInnerPrototypes;
	/**
	 * true if methods such as prototype, clone, cast:, etc have already
	 * been added to this prototype by this method
	 */
	private boolean hasAdded_pp_new_Methods;
	/**
	 * true if the compiler has added code in dpa actions
	 */
	private boolean hasMade_dpa_actions;
	/**
	 * true if the file being compiled is a ScriptCyan file
	 */
	private boolean scriptCyan;
	/**
	 * true if {@link #scriptCyan} is true and the source file being compiled has statements only. That is,
	 * it does not have methods, shared variables, and instance variables.
	 */
	private boolean scriptCyanStatementsOnly;

	/**
	 * use static typing in the whole compilation unit. This should not be changed by any metaobject annotation.
	 * Otherwise we should get a compilation error. Then if this variable is true and a metaobject annotation
	 * <code>{@literal @}dynAlways</code> or <code>{@literal @}dynOnce</code>  is used there should be
	 * a compilation error.
	 */
	private boolean	forceUseOfStaticTyping;


	/**
	 * keeps all metaobject annotations of the current compilation unit whose metaobject classes are codegs
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> codegList;

	/**
	 * the compilation step, of course
	 */
	private CompilationStep compilationStep;
	/**
	 * lexer used only to do parsing of types given as strings
	 */
	private static Lexer lexerFromString;
	/**
	 * the project. During the compilation of the '.pyan' source code this object is created and initialized.
	 * During the compilation of Cyan source code the value of this variable is received when
	 * the object Compiler is created.
	 */
	private Project project;


	/**
	 * true if the lexer is scanning a metaobject annotation to {literal @}compilationContextPush. If this is true,
	 * the tokens found should not have their line numbers subtracted from lineShift. If this variable
	 * is true, stack cyanMetaobjectContextStack should still be empty.
	 */
	boolean insideCyanMetaobjectCompilationContextPushAnnotation;

	/**
	 * true if a statement starts with an identifier that may be a type. This usually implies
	 * that this is a wrong variable declaration. The user forgot to put 'var' or 'let'
	 * before the declaration like in<br>
	 * {@code Int n;<br>
	 * Person p;<br>
	 * }<br>
	 */
	private boolean mayBeWrongVarDeclaration;

	/**
	 * list of all symbols found in this compilation
	 */
	public static Symbol []symbolList = null;

	/**
	 * real size of symbolList
	 */
	public static int symbolListAllocatedSize = 0;

	/**
	 * size of symbolList
	 */
	public static int sizeSymbolList = 0;

	/**
	 * list of all symbols found in the parsing with symbols introduced by metaobjects and the compiler removed.
	 */
	public static Symbol []cleanSymbolList;

	public static int sizeCleanSymbolList;

	/**
	 * pairs of codeg name and the text stored in the file associated to the codeg.
	 * In the annotation
	 * <br>
	 * <code> var c = {@literal @}color(red) </code><br>
	 * the tuple would be, using Cyan syntax:
	 * <code> [. "color(red)", "343" .] </code>
	 */
	private ArrayList<Tuple2<String, byte[]>> codegNameWithCodegFile = null;

	public CompilationUnitSuper getCompilationUnitSuper() {
		return compilationUnitSuper;
	}


	/**
	 * true if the compiler is parsing a method signature. This is used
	 * to prohibit the use of 'typeof' inside a method signature
	 */
	private boolean prohibitTypeof;



	/**
	 * true if the compiler is allowed to create instantiations of generic prototypes in
	 * compilation phases >= 7.
	 */
	private boolean allowCreationOfPrototypesInLastCompilerPhases;


	private Program program;

	// private Env notNullIfCreatingGenericPrototypeInstantiation;

}



