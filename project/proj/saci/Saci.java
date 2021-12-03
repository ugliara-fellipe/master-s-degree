package saci;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * This program takes several arguments:
 */
import ast.CompilationUnit;
import ast.CompilationUnitSuper;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Program;
import ast.ProgramUnit;
import ast.Type;
import error.CompileErrorException;
import error.ProjectError;
import error.UnitError;
import lexer.Symbol;
import meta.Compiler_ded;
import meta.CyanMetaobjectWithAt;
import meta.ICodeg;
import meta.SourceCodeChangeByMetaobjectAnnotation;

/**
 * This is the Cyan compiler. See more about it in the Cyan manual.
 *
 *
 *    @author jose
 */


public class Saci {

	/**
	 * this method returns the metaobject annotation that is in line <code>line</code> and column <code>column</code>.
	 * The line of the annotation is identical to <code>line</code>. The column should be inside the annotation.
	 */
	public CyanMetaobjectWithAtAnnotation searchCodegAnnotation(int line, int column) {

		if ( codegList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : this.codegList ) {
				int lineAnnotation = annotation.getFirstSymbol().getLineNumber();
				int columnAnnotationStart = annotation.getFirstSymbol().getColumnNumber();
				int lineAnnotationEnds = annotation.getLastSymbol().getLineNumber();
				int columnAnnotationEnds = annotation.getLastSymbol().getColumnNumber();
				if ( line >= lineAnnotation && line <= lineAnnotationEnds && column >= columnAnnotationStart && column <= columnAnnotationEnds ) {
					/*
					 * found a codeg annotation that is in (line, column)
					 */
					return annotation;
				}
			}
		}
		return null;
	}

	public CyanMetaobjectWithAtAnnotation searchCodegAnnotation(int offset) {

		if ( codegList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : this.codegList ) {
				int start = annotation.getFirstSymbol().getOffset();
				int end = annotation.getLastSymbol().getOffset();
				if ( offset >= start && offset <= end )
					return annotation;

			}
		}
		return null;
	}

	/**
	 * this method shows the menu associated to the codeg annotation <code>codegAnnotation</code>.
	 * It should be called by the IDE when the mouse is over the codeg or through a menu in the IDE.
	 * In the first case method {@link Saci#searchCodegAnnotation(int, int)} should be called first to
	 * get the codeg annotation.
	 *
	 * Return false in error.
	   @param codegAnnotation
	   @return
	 */
	public boolean eventCodegMenu(Saci saci, CyanMetaobjectWithAtAnnotation codegAnnotation) {

		CyanMetaobjectWithAt codeg = codegAnnotation.getCyanMetaobject();
		if ( !(codeg instanceof ICodeg) )
			return false;
		ICodeg icodeg = (ICodeg ) codeg;
		byte []codegFileData = null;

		Tuple2<String, byte[]> foundTuple = null;
		/*
		 * completeName may be "color(red)"
		 */
		String completeName = codegAnnotation.getCompleteName();
		int i = 0;
		if ( codegNameWithCodegFile == null ) {
			/*
			 * parseSingleSource has not been called or parseSingleSource has not been called after
			 * the whole program has been compiled. {@link Saci#eventCodegMenu} should return
			 * false to give a change to the IDE call parseSingleSource.
			 */
			return false;
		}
		else {
			for ( Tuple2<String, byte[]> t : this.codegNameWithCodegFile ) {
				if ( completeName.equals(t.f1) ) {
					codegFileData = t.f2;
					foundTuple = t;
					break;
				}
				++i;
			}
		}

		/*
		CompilationUnit compUnit = this.getLastCompilationUnitParsed();
		ProgramUnit pu = null;
		ObjectDec prototype = null;
		if ( compUnit != null ) {
			pu = compUnit.getPublicPrototype();
			if ( pu instanceof ObjectDec )
				prototype = (ObjectDec ) pu;
		}
		*/


		Compiler_ded compiler_ded = new Compiler_ded(saci.project, codegAnnotation);

		codegFileData = icodeg.getUserInput(compiler_ded, foundTuple == null ? null : foundTuple.f2);

		if ( codegFileData == null ) { codegFileData = new byte[1]; }


		/**
		 * user really give information to the codeg which created a text to be stored in
		 * a file. This text was returned by {@link ICodeg#getUserInput}.
		 */

		/*
		 * update list codegNameWithCodegFile so it is not necessary to read the file again
		 * when calling {@link Saci#parseSingleSource}. During parsing, the compiler retrieves
		 * the information from codegNameWithCodegFile, which is passed as parameter.
		 */
		codegNameWithCodegFile.set(i,  new Tuple2<String, byte[]>(completeName, codegFileData));
		codegAnnotation.setCodegInfo(codegFileData);
		String filename = codegAnnotation.filenameMetaobjectAnnotationInfo();
		if ( filename == null ) {
			this.error("Internal error: cannot find filename associated to codeg annotation of line " + codegAnnotation.getFirstSymbol().getLineNumber() +
					" and column " + codegAnnotation.getFirstSymbol().getColumnNumber());
		}
		else {



			Path path = Paths.get(filename);



		    try {

		    	Path parentDir = path.getParent();
		    	if (!Files.exists(parentDir))
		    	    Files.createDirectories(parentDir);
				Files.write(path, codegFileData);
			}
			catch (IOException e) {
				this.error("Error writing information on codeg '" + codegAnnotation.getCompleteName() + "' to file " + filename );
			}

		    /*
			MyFile f = new MyFile(filename);
			f.writeFile(codegFileText.toString().toCharArray());
			if ( f.getError() == MyFile.do_not_exist_e ) {
				this.error("Error creating file '" + filename + "'");
			}
			else {
				if ( f.getError() != MyFile.ok_e )
					this.error("Error writing information on codeg '" + codegCall.getCompleteName() + "' to file " + filename );
			}
			*/

		}


		return true;
	}

	/**
	 * The IDE plugin should call this method whenever the source code being edited is changed to a new one.
	 * That is, the user was editing a file <code>Program.cyan</code> and she changes the focus to file <code>Test.cyan</cyan>.
	 * It is necessary to discard some data related to the previous file and parse the new file.
	 */
	@SuppressWarnings("hiding")
	public void eventChangeSourceCodeBeingEdited( String cyanLangDir, String javaLibDir,
    		String packageName, String prototypeName, char []sourceCodeToParse,
    		String projectFileName,
    		char []sourceCodeProject,
    		boolean loadProjectFromFile) {
		codegNameWithCodegFile = null;
		lastCompilationUnitParsed = null;
		this.parseSingleSource(cyanLangDir, javaLibDir, packageName, prototypeName, sourceCodeToParse, projectFileName,
				sourceCodeProject, loadProjectFromFile);
	}


	/**
	 * pairs of codeg name and the data stored in the file associated to the codeg.
	 * In the annotation
	 * <br>
	 * <code> var c = ({@literal @}color(red)); </code><br>
	 * the tuple would be, using Cyan syntax:
	 * <code> [. "color(red)", [ 3B, 4B, 3B ] .] </code>
	 */
	private ArrayList<Tuple2<String, byte[]>> codegNameWithCodegFile = null;


    public static void main( String []args ) {

    	Saci aSaci = new Saci();
    	aSaci.compilerCalledFromCommandLine = true;

        if ( args.length < 1 )  {
            System.out.println("Usage:\n   CC.Saci projectDirectoryOrName -cyanlang CyanLangDir -javalib JavaLibDir compilerOptions ");
            System.out.println("projectDirectoryOrName is the file name of the project or the directory name in which the project is");
            System.out.println("CyanLangDir is the directory in which the basic prototypes are (Int, Array, etc)");
            System.out.println("JavaLibDir is the directory of the Cyan runtime libraries");
            System.out.println("compilerOptions may be:");
            System.out.println("    '-noexec' for not executing the compiled Java code");
            System.out.println("    '-nojavac' for not calling the Java compiler");
            System.out.println("    '-args argList' for arguments to the Cyan program. The arguments " +
               "that follow '-args', argList, will be passed to the Cyan program if it is to be executed");
            System.out.println("    '-cp aPath' for supplying 'aPath' for the Java compiler. This option" +
                " can appear any number of times and multiple paths, separated by ';', can be given using a single '-cp'");

            System.exit(1);
        }


        aSaci.parseCmdLineArgs(args);


    	aSaci.run();


    	/*
		PrintWriter printWriter = new PrintWriter(System.out, true);
    	if ( aSaci.getProjectErrorList() != null ) {
            for ( ProjectError p : aSaci.getProjectErrorList() )
            	p.print(printWriter);
    	}
    	if ( aSaci.getCyanErrorList() != null ) {
            for ( UnitError ue : aSaci.getCyanErrorList() )
            	ue.print(printWriter);
    	}
    	*/
		System.out.println("Saci is over");
    	System.exit(0);



    }

    public static void main2( String []args ) {

    	Saci aSaci = new Saci();
    	aSaci.compilerCalledFromCommandLine = false;

        aSaci.parseCmdLineArgs(args);

        MyFile fp = new MyFile("C:\\Dropbox\\Cyan\\cyanTests\\codeg\\project.pyan");
        char []sourceProject = fp.readFile();
        MyFile fs = new MyFile("C:\\Dropbox\\Cyan\\cyanTests\\codeg\\main\\Program.cyan");
        char []sourceProgram = fs.readFile();

		PrintWriter printWriter = new PrintWriter(System.out, true);

    	/*
    	 * test only
    	 */

    	for ( int kk = 0; kk < 1; ++kk ) {
            // System.exit( aSaci.run(args) );
            aSaci.parseSingleSource( aSaci.cyanLangDir, aSaci.javaLibDir,
            		 "main", "Program",
            		sourceProgram,
            		// "package main  object Program    func run {  let G<Int> g; g m println;     } end\0".toCharArray(),
            		"C:\\Dropbox\\Cyan\\cyanTests\\codeg\\project.pyan", sourceProject,
            		false);
        	if ( aSaci.getProjectErrorList() != null && aSaci.getProjectErrorList().size() > 0 ) {
                for ( ProjectError p : aSaci.getProjectErrorList() )
                	p.print(printWriter);
                break;
        	}
        	if ( aSaci.getCyanErrorList() != null && aSaci.getCyanErrorList().size() > 0 ) {
                for ( UnitError ue : aSaci.getCyanErrorList() )
                	ue.print(printWriter);
                break;
        	}

    	}

    	/*
    	if ( aSaci.getCodegList() != null && aSaci.getCodegList().size() > 0 ) {
        	Symbol sym = aSaci.getCodegList().get(0).getFirstSymbol();
        	//System.out.println(sym.getLineNumber() + " " + sym.getColumnNumber() + " " + sym.getSymbolString());
        	sym = aSaci.getCodegList().get(0).getLastSymbol();
        	//System.out.println(sym.getLineNumber() + " " + sym.getColumnNumber() + " " + sym.getSymbolString());
    	}
    	*/

    	System.exit(0);

    	/*
    	 * end test
    	 */

    }



    /**
     * compile the Cyan source file <code>sourceCodeToParse</code> that is prototype <code>packageName.prototypeName</code>.
     * The project for the Cyan program is in file <code>projectFileName</code> of directory <code>projectCanonicalPath</code>.
     * Its source code is <code>sourceCodeProject</code>. The project file (.pyan) is compiled for the first time only
     * unless <code>loadProjectFromFile</code> is true. Then if an object of <code>Saci</code> is created
     * and this method is called several times with <code>loadProjectFromFile</code> set to false, only in the first
     * time is the project compiled.
     *
     *  Note that <code>projectFileName</code> may be a directory of <code>projectCanonicalPath</code>. In this
     *  case, a project file <code>"project.pyan"</code> is created inside <code>projectFileName</code>.
     *
     *  <code>cyanLangDir</code> is the directory of the cyan.lang package and <code>javaLibDir</code> is the directory
     *  of the Cyan runtime libraries.
     *
     *  After calling parseSingleSource, use
     *      getCodegList().get(0).getSymbolMetaobjectAnnotation().getLineNumber();
		    getCodegList().get(0).getSymbolMetaobjectAnnotation().getColumnNumber();
		    getCodegList().get(0).getJavaParameterList();

		to get information on the line number, column number, and parameters of each codeg of the source compiled.
     *
     *
     */
    @SuppressWarnings("hiding")
	public boolean parseSingleSource( String cyanLangDir, String javaLibDir,
    		String packageName, String prototypeName, char []sourceCodeToParse,
    		String projectDirectoryOrName,
    		char []sourceCodeProject,
    		boolean loadProjectFromFile ) {


        long before = System.nanoTime();

    	this.cyanLangDir= cyanLangDir;
    	this.javaLibDir = javaLibDir;
    	this.packageName = packageName;
    	this.prototypeName = prototypeName;
    	this.sourceCodeToParse = sourceCodeToParse;
    	this.projectDirectoryOrName = projectDirectoryOrName;
    	this.sourceCodeProject = sourceCodeProject;


		if ( codegNameWithCodegFile == null ) {
			/*
			 * codegNameWithCodegFile is null when parseSingleSource has never been called
			 */
			codegNameWithCodegFile = new ArrayList<>();
		}


    	if ( packageName == null || prototypeName == null || sourceCodeToParse == null ) {
   			return false;
    	}
    	if ( sourceCodeToParse[sourceCodeToParse.length-1] != '\0' )
    		this.error("The source code should end with character '\\0'");

    	if ( sourceCodeProject != null && sourceCodeProject[sourceCodeProject.length-1] != '\0' )
    		this.error("The source code of the project should end with character '\\0'");

    	compilerCalledFromCommandLine = false;





    	PrintWriter printWriter = new PrintWriter(System.out, true);

    	//MyFile projectFile = new MyFile(args[0]);

    	setErrorList = false;
    	setProjectErrorList = false;

    	HashSet<CompilationInstruction> compInstSet;
    	if ( project == null || loadProjectFromFile || sourceCodeProject == null ) {
        	errorList = new ArrayList<>();
        	projectErrorList = new ArrayList<>();
        	program = new Program();

        	project = parseProject();
        	if ( project.hasErrors() )
        		return false;
        	project.setCallJavac(callJavac);
        	project.setExec(exec);
        	project.setCmdLineArgs(cmdLineArgs);
        	program.setProject(project);
        	project.setProgram(program);
        	program.setCyanMetaobjectTable();
        	program.setJavaLibDir(javaLibDir);

    	}
    	else {
        	program.setProject(project);
        	project.setProgram(program);
        	if ( projectErrorList == null ) {
        		projectErrorList = new ArrayList<>();
        	}
        	else {
            	projectErrorList.clear();
        	}
        	if ( errorList == null ) {
        		errorList = new ArrayList<>();
        	}
        	else {
            	errorList.clear();
        	}
    	}
    	HashMap<String, String> compilerOptions = new HashMap<>();

        // createJavaClasses(cyanLangDir);

		CompilerManager compilerManager = new CompilerManager(project, program,
				printWriter, compilerOptions );
		project.setCompilerManager(compilerManager);


		program.init();


		/*
		 * step 1 only
		 */
		compilerManager.setCompilationStep(CompilationStep.step_1);
		compInstSet = new HashSet<>();
		compInstSet.add(CompilationInstruction.dpa_actions);
		compInstSet.add(CompilationInstruction.pp_addCode);
		compInstSet.add(CompilationInstruction.dpa_originalSourceCode);


		/*
		 * parse only packageName.prototypeName
		 */

        ast.CyanPackage cyanPackage = project.searchPackage(this.packageName);
		if ( cyanPackage == null ) {
			this.error("Package " + this.packageName + " was not found");
			return false;
		}
		lastCompilationUnitParsed = cyanPackage.searchCompilationUnit(this.prototypeName);
		lastCompilationUnitParsed.setText(this.sourceCodeToParse);
		lastCompilationUnitParsed.setOriginalText(this.sourceCodeToParse);

		lastCompilationUnitParsed.clearErrorList();

		Compiler compiler = new Compiler(lastCompilationUnitParsed, compInstSet, CompilationStep.step_1, project, codegNameWithCodegFile);

		// long before2 = System.nanoTime();
		// long beforeMili = System.currentTimeMillis();

		compiler.parse();

		this.symbolList = Compiler.getSymbolList();
		this.sizeSymbolList = Compiler.getSizeSymbolList();
		this.codegList = compiler.getCodegList();

		/*
		double diffNano = (System.nanoTime() - before2)/1000000.0;
		long diffMili = System.currentTimeMillis() - beforeMili;
    	//System.out.println("parse only: " + diffNano);
    	//System.out.println("parse only: " + diffMili );
    	*/


    	// System.out.println("parse project : " + (System.nanoTime() - before)/1000000.0 );
    	return true;
    }

    /**
     * the last compilation unit compiled by {@link Saci#parseSingleSource(String, String, String, String, char[], String, char[], boolean)}.
     */
    private CompilationUnit lastCompilationUnitParsed;
	boolean exec = true;
	boolean callJavac = true;
	ArrayList<String> classPathList = null;
	ArrayList<String> sourcePathList = null;

	String packageName = null;
	String prototypeName = null;
	char []sourceCodeToParse = null;
	char []sourceCodeProject = null;
	String projectDirectoryOrName = null;
	String cyanLangDir = null;
	String javaLibDir = null;

	// String projectCanonicalPath = null;


	@SuppressWarnings("hiding")
	public void compileProject( String projectDirectoryOrName, String cyanLangDir, String javaLibDir,
    		boolean exec, boolean callJavac) {
		this.projectDirectoryOrName = projectDirectoryOrName;
		this.cyanLangDir = cyanLangDir;
		this.javaLibDir = javaLibDir;
		this.exec = exec;
		this.callJavac = callJavac;
		this.cmdLineArgs = null;

    	compilerCalledFromCommandLine = true;
		run();

	}
	private static Set<String> optionsSet;
	static {
		optionsSet = new HashSet<String>();
		optionsSet.add("-cyanlang");
		optionsSet.add("-javalib");
		optionsSet.add("-noexec");
		optionsSet.add("-nojavac");
		optionsSet.add("-args");
		optionsSet.add("-cp");
		optionsSet.add("-sourcepath");
	}
    private int parseCmdLineArgs( String []args ) {

    	int ret = 1;

    	cmdLineArgs = null;
    	exec = true;
    	callJavac = true;
		int i = 0;
		classPathList = new ArrayList<>();
		sourcePathList = new ArrayList<>();
		while ( i < args.length ) {
			if ( args[i].equalsIgnoreCase("-cyanlang") ) {
				if ( cyanLangDir != null ) {
					error("Duplicate option -cyanlang");
					return ret;
				}
				if ( i >= args.length - 1 ) error("Missing directory after '-cyanlang'");
				cyanLangDir = args[i+1];
				++i;
			}
			else if ( args[i].equalsIgnoreCase("-javalib") ) {
				if ( javaLibDir != null ) {
					error("Duplicate option -javalib");
					return ret;
				}
				if ( i >= args.length - 1 ) error("Missing directory after '-javalib'");
				javaLibDir = args[i+1];
				++i;
			}
			else if ( args[i].equalsIgnoreCase("-noexec") ) {
				exec = false;
			}
			else if ( args[i].equalsIgnoreCase("-nojavac") ) {
				callJavac = false;
			}
			else if ( args[i].equalsIgnoreCase("-cp") ) {
				if ( i >= args.length - 1 ) error("Missing Java class path after '-cp'");
				classPathList.add(args[i+1]);
				++i;
			}
			else if ( args[i].equalsIgnoreCase("-sourcepath") ) {
				if ( i >= args.length - 1 ) error("Missing Java source path after '-sourcepath'");
				sourcePathList.add(args[i+1]);
			}
			else if ( args[i].equalsIgnoreCase("-args") ) {
				cmdLineArgs = "";
				for (int j = i + 1; j < args.length; ++j) {
					if ( optionsSet.contains(args[j]) ) {
						error("The compiler option '" + args[j] + "' cannot appear after option '-args'");
					}
					cmdLineArgs += args[j] + " ";
				}
				break;
			}
			else if ( args[i].charAt(0) != '-' ) {
				if ( projectDirectoryOrName == null )
					projectDirectoryOrName = args[i];
				else {
					error("Found two project names: '" + projectDirectoryOrName + "' and '" + args[i] + "'");
					return ret;
				}
			}
			else {
				error("unknown option: '" + args[i] + "'");
				return ret;
			}
				++i;
		}
    	if ( cyanLangDir == null ) {
    	    error("Missing compiler option -cyanlang. I am unable to find the cyan.lang package");
    	    return ret;
    	}
    	if ( javaLibDir == null ) {
    	    error("Missing compiler option -javalib. I am unable to find the Cyan runtime libraries");
    	    return ret;
    	}
    	if ( projectDirectoryOrName == null ) {
    		error("Missing project name");
    		return ret;
    	}

    	return ret;
    }




    public int run( ) {


    	lastCompilationUnitParsed = null;
    	codegNameWithCodegFile = null;

    	int ret = 1;


    	PrintWriter printWriter = new PrintWriter(System.out, true);

    	//MyFile projectFile = new MyFile(args[0]);
    	program = new Program();

    	errorList = new ArrayList<>();
    	setErrorList = false;
    	setProjectErrorList = false;

    	project = parseProject();
		if ( project.hasErrors() ) {
			project.printErrorList(printWriter);
			return ret;
		}

    	project.setCallJavac(callJavac);
    	project.setExec(exec);
    	project.setCmdLineArgs(cmdLineArgs);
    	program.setProject(project);
    	//project.setProgram(program);
    	program.setCyanMetaobjectTable();
    	program.setJavaLibDir(javaLibDir);
    	program.setCyanLangDir(cyanLangDir);
    	program.setClassPathList(classPathList);
    	program.setSourcePathList(sourcePathList);


    	/*
    	 * load Java classes
    	 */
    	ArrayList<String> errorMessageList = program.loadJavaPackages();
    	if ( errorMessageList != null && errorMessageList.size() > 0 ) {
    		for ( String errorMessage : errorMessageList ) {
    			error(errorMessage);
    		}
			printErrorList(printWriter, new Env(project));
    	}

    	HashMap<String, String> compilerOptions = new HashMap<>();

        // createJavaClasses(cyanLangDir);

		CompilerManager compilerManager = new CompilerManager(project, program,
				printWriter, compilerOptions );
		project.setCompilerManager(compilerManager);


		program.init();

		errorsHaveBeenPrinted = false;
		/*
		 * step 1
		 */
		compilerManager.setCompilationStep(CompilationStep.step_1);
		HashSet<saci.CompilationInstruction> compInstSet = new HashSet<>();
		compInstSet.add(CompilationInstruction.dpa_actions);
		compInstSet.add(CompilationInstruction.pp_addCode);
		compInstSet.add(CompilationInstruction.dpa_originalSourceCode);

		Env env = new Env(project);

		/*
		 * should parse all files. Then read all sources from disk
		 */
		if (  ! compilerManager.readSourceFiles() ) {
			printErrorList(printWriter, null);
		}



		if ( ! compilerManager.compile(compInstSet)  )  {
			printErrorList(printWriter, env);
		}
		else {
			/*
			 * step 2
			 */
    		compilerManager.setCompilationStep(CompilationStep.step_2);

			env = new Env(project);
			  // initializes variables of {@link ast.Type}
			findBasicTypes(program.getProject(), env);

			program.calcInterfaceTypes(env);

			if (  env.isThereWasError() ) {
    			printErrorList(printWriter, env);
    		}
    		else {

    			/*
    			 * step 3
    			 */
        		compilerManager.setCompilationStep(CompilationStep.step_3);

    			compInstSet.clear();
        		compInstSet.add(CompilationInstruction.ati_actions);
    			env.setCompInstSet(compInstSet);
    			if ( ! program.ati_actions(env) ) {
        			printErrorList(printWriter, env);
        		}
        		else {
        			/*
        			 * step 4
        			 */
            		compilerManager.setCompilationStep(CompilationStep.step_4);

        			compInstSet.clear();
            		compInstSet.add(CompilationInstruction.dpa_actions);
            		compInstSet.add(CompilationInstruction.new_addCode);
            		/*
            		 * reset the state of all compilation units so to clear any traces
            		 * they have already been compiled
            		 */
            		program.reset();

            		if ( ! compilerManager.compile(compInstSet) ) {
            			printErrorList(printWriter, env);
            		}
            		else {
            			/*
            			 * step 5
            			 */
                		compilerManager.setCompilationStep(CompilationStep.step_5);

                		compInstSet.clear();
                		compInstSet.add(CompilationInstruction.ati2_check);

            			env = new Env(project);
    	        		env.setCompInstSet(compInstSet);


            			findBasicTypes(program.getProject(), env);
            			program.calcInterfaceTypes(env);
            			if ( env.isThereWasError() ) {
                			printErrorList(printWriter, env);
                		}
                		else {
            				/*
            				 * step 6
            				 */
                    		compilerManager.setCompilationStep(CompilationStep.step_6);
                			compInstSet.clear();
                    		compInstSet.add(CompilationInstruction.dsa_actions);
                    		env.setCompInstSet(compInstSet);
                			program.calcInternalTypes(env);
                			if ( env.isThereWasError() ) {
                    			printErrorList(printWriter, env);
                    		}
                    		else {
                    			/*
                    			 * replace some message sends by expressions. This can be used  for
                    			 * introduce new methods to basic types such as Double:\
                    			 *     value = 3.14 sin;
                    			 * env.
                    			 */

                    			//  execute the dsa actions

                    			env.dsa_actions();
                    			/*
                    			 * step 7
                    			 */
                        		compilerManager.setCompilationStep(CompilationStep.step_7);

                	    		compInstSet.clear();
                	    		compInstSet.add(CompilationInstruction.inner_addCode);
                	    		compInstSet.add(CompilationInstruction.createPrototypesForInterfaces);
                	    		compInstSet.add(CompilationInstruction.pp_new_inner_addCode);

                	    		program.resetNonGeneric();

                	    		if ( ! compilerManager.compile(compInstSet) ) {
                	    			printErrorList(printWriter, env);
                	    		}
                	    		else {

                	    			/*
                	    			 * step 8
                	    			 */
                	        		compilerManager.setCompilationStep(CompilationStep.step_8);

                            		compInstSet.clear();
                            		compInstSet.add(CompilationInstruction.ati3_check);



                	        		env = new Env(project);
                	        		env.setCompInstSet(compInstSet);


                        			findBasicTypes(program.getProject(), env);
                        			program.calcInterfaceTypes(env);

                        			program.ati3_check(env);

                        			if ( env.isThereWasError() ) {
                            			printErrorList(printWriter, env);
                            		}
                            		else {
                        				/*
                        				 * step 9
                        				 */
                                		compilerManager.setCompilationStep(CompilationStep.step_9);

                                		compInstSet.clear();
                            			compInstSet.add(CompilationInstruction.matchExpectedCompilationErrors);
                            			compInstSet.add(CompilationInstruction.dsa_check);
                                		env.setCompInstSet(compInstSet);

                                		program.calcInternalTypes(env);


                            			if ( env.isThereWasError() ) {
                                			printErrorList(printWriter, env);
                                		}
                                		else {
                                			program.dsa2_check(env);
                                			/**
                                			 * check if any metaobject that implements interface 'IInformCompilationError'
                                			 * pointed some error that was not signaled in this compilation
                                			 */
                                			program.checkErrorMessagesAllCompilationUnits(env);

                            				/*
                            				 * step 10
                            				 */
                                    		compilerManager.setCompilationStep(CompilationStep.step_10);
                                		    //program.genJava(env);

                                    		compInstSet.clear();
                                    		env.setCompInstSet(compInstSet);
                                			program.genJava(env);

                                			if ( ! env.isThereWasError() ) {
                                				/*
                                				 * save to each package directory information saying the package was compiled
                                				 * Successfully by this compiler version
                                				 */
                                				compilerManager.saveCompilationInfoPackages(env);
                                			}
                                			if ( env.isThereWasError() ) {
                                    			printErrorList(printWriter, env);
                                    		}
                                			else {
                                				program.compileGeneratedJavaCode(env);
                                    			if ( env.isThereWasError() ) {
                                        			printErrorList(printWriter, env);
                                        		}
                                    			else {
                                    				ret = 0;
                                    			}
                                			}
                                			//MyFile.write(env.searchPackagePrototype("main", "Program").getCompilationUnit());

                            			}
                        			}
                	    		}
                			}
            			}
            		}
    			}
			}

		}
		program.writePrototypesToFile(env);
		if ( env.isThereWasError() && ! this.errorsHaveBeenPrinted ) {
			printErrorList(printWriter, env);
		}




        return ret;
    }



	public ArrayList<ProjectError> getProjectErrorList() {
		if ( ! setProjectErrorList ) {
			if ( project.hasErrors() ) {
				projectErrorList.addAll(project.getProjectErrorList());
			}
			setProjectErrorList = true;
		}
		return this.projectErrorList;
	}

    public ArrayList<UnitError> getCyanErrorList() {
    	if ( ! this.setErrorList ) {
    		for ( CompilationUnit compUnit : program.getCompilationUnitList() ) {
    			if ( compUnit.getErrorList() != null ) {
        			errorList.addAll(compUnit.getErrorList());
    			}
    		}
    		setErrorList = true;
    	}
    	return errorList;
    }


	private void printErrorList(PrintWriter printWriter, Env env) {

    	if ( ! this.compilerCalledFromCommandLine )
    		return ;

    	errorsHaveBeenPrinted = true;
    	int i = 0;
		for ( CompilationUnit compUnit : program.getCompilationUnitList() ) {
			if ( compUnit.hasCompilationError() ) {
				compUnit.printErrorList(printWriter);

			}
			++i;
			if ( env != null ) {
				int numFalse = 0;
				for ( Tuple3<Integer, String, Boolean> t : compUnit.getLineMessageList() ) {
					if ( !t.f3 )
						++numFalse;
				}

				if ( numFalse == compUnit.getLineMessageList().size() ) {
					for ( Tuple3<Integer, String, Boolean> t : compUnit.getLineMessageList() ) {
						if ( ! t.f3 ) {
							try {
								env.error(null,  "A metaobject implementing interface 'IInformCompilationError' points an error at line " + t.f1 +
										" of file '" + compUnit.getFullFileNamePath() + "' with message '" + t.f2 + "' although this error is not signaled by the compiler." +
										" If the error really is in the source code, what happens was similar to the following: there is a parser error and " +
										"a semantic error in this source code, each pointed by a metaobject. This error was caused by the semantic error. This error" +
										" wound be pointed by the compiler in a later compiler phase. However, it will not because the compilation will stop because " +
										"of the parsing error",
										false, true
										);
							}
							catch ( CompileErrorException e ) {
							}
						}
					}

				}

			}
			if ( compUnit.getActionList().size() > 0 )
				compUnit.doActionList(printWriter);
		}
    	if ( project.getProjectErrorList() != null ) {
        	for ( ProjectError pe : project.getProjectErrorList() ) {
        		pe.print(printWriter);
        	}
    	}
    }



    /**
     * Initializes public static variables of class {@link ast.Type} with the correct
     * prototypes of the program
     *
       @param project
       @param env
     */
	private static void findBasicTypes(Project project, Env env) {
		boolean found = false;
		for ( CyanPackage aPackage : project.getPackageList() ) {
			if ( aPackage.getPackageName().compareTo("cyan.lang") == 0 ) {
				found = true;
				for ( CompilationUnit compilationUnit : aPackage.getCompilationUnitList() ) {
					for ( ProgramUnit programUnit : compilationUnit.getProgramUnitList() ) {
						switch ( programUnit.getName() ) {
						case "Byte"    : Type.Byte = programUnit; break;
						case "Short"   : Type.Short = programUnit; break;
						case "Int"     : Type.Int = programUnit; break;
						case "Long"    : Type.Long = programUnit; break;
						case "Float"   : Type.Float = programUnit; break;
						case "Double"  : Type.Double = programUnit; break;
						case "Char"    : Type.Char = programUnit; break;
						case "Boolean" : Type.Boolean = programUnit; break;
						case "CySymbol" : Type.CySymbol = programUnit; break;
						case "String"  : Type.String = programUnit; break;
						case "Any"     : Type.Any = programUnit; break;
						case "Nil"     : Type.Nil = programUnit; break;
						}
						//if ( programUnit instanceof ObjectDec )
						//	((ObjectDec ) programUnit).addSpecificMethods(env);
					}
				}
			}
		}
		if ( ! found )
			env.error(null, "Package 'cyan.lang' was not found", true, true);
		if ( Type.Byte == null || Type.Short == null || Type.Int == null || Type.Long == null ||
			 Type.Float == null || Type.Double == null || Type.Char == null || Type.Boolean == null ||
			 Type.CySymbol == null || Type.String == null || Type.Nil == null ) {
			env.error(null,  "One of the basic prototypes (Byte, Int, String etc) was not found", true, true);
		}

	}


	void error(String str) {
		if ( this.compilerCalledFromCommandLine ) {
	    	System.out.println(str);
	    	System.exit(1);
		}
		else {
			program.getProject().error(str);
		}
    }

	/**
	 * recursively delete all directories with name "--tmp" (NameServer.temporaryDirName) from directory directoryPath
	 * @param directoryPath
	 * @return
	 */
	public static boolean deleteAllTmpDirectories(String directoryPath) {

		try {
			File currentDir = new File(directoryPath);
			if ( currentDir.isDirectory() ) {
				if ( directoryPath.equalsIgnoreCase(NameServer.temporaryDirName) )
					currentDir.delete();
				else {
					for ( File f : currentDir.listFiles() ) {
						if ( f.isDirectory() ) {
							if ( f.getName().equalsIgnoreCase(NameServer.temporaryDirName) ) {
								if ( ! MyFile.deleteFileDirectory(f) ) {
									System.out.println("Cannot delele " + f.getCanonicalPath() + " or some of its files");
								}
							}
							else {
								deleteAllTmpDirectories(f.getCanonicalPath());
							}
						}
					}
				}
			}

		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
     *
     */

    private Project parseProject() {

    	/*
    	 * delete all files of directory "cyanLangDir\tmp"
    	 */
		deleteAllTmpDirectories(cyanLangDir);

		String projectCanonicalPath = projectDirectoryOrName + NameServer.fileSeparatorAsString;

		File projectFile = new File(projectDirectoryOrName);
		if ( ! projectFile.exists() ) {
			error("Name '" + projectDirectoryOrName + "' should be either a directory of a program or a file ending with '.pyan'. But it does not exist");
			return null;
		}

		String pyanFilename = projectDirectoryOrName;
		String fullPyanFilename;
		if ( ! projectFile.isDirectory() ) {
			/*
			 * projectDirectoryOrName is really the name of a project ending in ".pyan"
			 */
			fullPyanFilename = pyanFilename;
			File f = new File(projectDirectoryOrName);
			pyanFilename = f.getName();
			if ( ! pyanFilename.endsWith(".pyan") ) {
				error("'projectDirectoryOrName' should be the project file of a program. It should end with '.pyan'");
				return null;
			}
			projectCanonicalPath = "";
			try {
				projectCanonicalPath = f.getParentFile().getCanonicalPath() + NameServer.fileSeparatorAsString;
			}
			catch (IOException e) {
				error("Error handling " + fullPyanFilename );
				return null;
			}
		}
		else {
			/*
			 * projectDirectoryOrName is the name of a directory. Create the '.pyan' file
			 */
			   // delete all "--tmp" directories recursivelly
			deleteAllTmpDirectories(projectDirectoryOrName);
			// dreate the projet file.
			if ( ! createProjectFile(projectDirectoryOrName, "", "", "", "") )
				return null;
			pyanFilename = "project." + NameServer.pyanSourceFileExtension;
			fullPyanFilename = projectCanonicalPath + pyanFilename;
		}




		MyFile myCyanProjectFile = new MyFile(fullPyanFilename);
		char []projectText = myCyanProjectFile.readFile();
		if ( projectText == null || myCyanProjectFile.getError() != MyFile.ok_e ) {
			error("Error opening/reading file " + fullPyanFilename);
			return null;
		}



		CompilationUnitSuper projectCompilationUnit = new CompilationUnit(pyanFilename, projectCanonicalPath, null, null);
		projectCompilationUnit.readSourceFile();
		HashSet<CompilationInstruction> compInstSet = new HashSet<>();
		compInstSet.add(CompilationInstruction.pyanSourceCode);
		Compiler pc = new Compiler( projectCompilationUnit, compInstSet, CompilationStep.step_1, null, null );

		// name of the executable file, a file that calls the Java interpreter after a successful compilation
		String execFileName;

		String tmpPath = projectCanonicalPath;
		if ( tmpPath.endsWith(NameServer.fileSeparatorAsString) ) {
			tmpPath = tmpPath.substring(0, tmpPath.length()-1);
		}

		int indexLastSlash = tmpPath.lastIndexOf(NameServer.fileSeparator);
		if ( indexLastSlash > 0 ) {
			execFileName = tmpPath.substring(indexLastSlash + 1);
		}
		else {
			execFileName = tmpPath;
		}
		if ( NameServer.fileSeparator == '\\' ) {
			// should be in Windows
			execFileName += ".bat";
		}

		Project newProject = new Project(program, projectCanonicalPath, execFileName);
		try {
			pc.parseProject(newProject, program, projectCompilationUnit, pyanFilename, projectCanonicalPath,
					projectText, cyanLangDir );
		}
		catch ( Exception e ) {
			e.printStackTrace();
			error("Internal error when reading a project file or creating one");
			return newProject;
		}

		newProject.setText(projectText);
		int indexDot = pyanFilename.indexOf('.');
		if ( indexDot >= 0 )
			newProject.setProjectName( pyanFilename.substring(0, indexDot) );
		else
			error("Internal error: project name does not have a '.'");


		if ( projectCompilationUnit.getErrorList() != null && projectCompilationUnit.getErrorList().size() > 0 ) {
			for ( UnitError anError : projectCompilationUnit.getErrorList() ) {

				StringBuffer s = new StringBuffer();
				if ( anError.getFilename() != null )
					s.append("In project file '" + anError.getFilename() + "' (line " + anError.getLineNumber() +
							 " column " + anError.getColumnNumber() + ") \n");
				s.append(anError.getMessage() + "\n");
				if ( anError.getLine() != null )
					s.append(anError.getLine() + "\n");

				newProject.error(s.toString());
			}
		}

		return newProject;
    }


	/**
	 * create a project file with name "project.pyan" in directory projDirName.
	 */
	public boolean createProjectFile(String dirName, String author,
			String options, String mainPackage, String mainObject) {


		// fileSeparator = System.getProperty("file.separator");
		File projDir = new File(dirName);

		String canPathOfTheProject = null;
		PrintWriter outp = null;
		try {
			canPathOfTheProject = projDir.getCanonicalPath();
			String cyanpFilePath = canPathOfTheProject + NameServer.fileSeparator +  "project.pyan";

			outp = new PrintWriter(cyanpFilePath);

			if ( author != null && author.length() != 0 )
				outp.print("@author(\"" + author + "\") ");
			if ( options != null && options.length() != 0 )
				outp.print("@options(\"" + options + "\") ");

			outp.print("program");
			if ( dirName != null && dirName.length() != 0 )
				outp.println(" at \"" + dirName + "\"");
			outp.println("");

			if ( mainObject != null && mainPackage != null && mainObject.length() != 0 &&
					mainPackage.length() != 0 )
				outp.println("    " + mainPackage + "." + mainObject);

			ArrayList<String> projPath = new ArrayList<String>();
			projPath.add(canPathOfTheProject);
			ArrayList<String> projCyanName = new ArrayList<String>();
			projCyanName.add("");
			String strError = getAllProjects(projPath, projCyanName, 0);
			if ( strError != null ) {
				this.error(strError);
				return false;
			}
			strError = generatePackageSourceList(projPath, projCyanName, outp);
			if ( strError != null ) {
				this.error(strError);
				return false;
			}

			outp.close();
		} catch (IOException e) {
			error("Can´t write to file " + canPathOfTheProject);
			return false;
		}
		finally {
			if ( outp != null )
				outp.close();
		}
		return true;
	}



	/**
	 *  add to projPath all sub-directories of every sub-directory of the paths of projPath. Add to projCyanName the
	 *  name of the Cyan name of the corresponding package.
	   @param projPath, a list of directories
	   @param projCyanName, a list of project names, ending with '.pyan', of the directories of projPath. Or "" if none.
	   @param start, index of projPath to start processing
	   @return
	 */
	public static String getAllProjects(ArrayList<String> projPath,
			ArrayList<String> projCyanName, int start) {

		 int size = projPath.size();
		 int i;
		 for(i = start; i < size; i++) {
	         String s = projPath.get(i);
	    	 String projectName = projCyanName.get(i);
	    	 File f = new File(s);
	    	 if ( f.isDirectory() ) {
	    	     String []subDirList = f.list();
	    	     for ( String p : subDirList ) {
	    	    	 if ( ! p.startsWith(NameServer.prefixNonPackageDir) ) {
		    	    	 File g = new File(s + NameServer.fileSeparator + p);
		    	    	 if ( g.isDirectory() ) {
		    	    		 try {
								projPath.add(g.getCanonicalPath());
							} catch (IOException e) {
								return "error in handling file " + p;
							}
		    	    		if ( projectName.length() == 0 )
		    	    		    projCyanName.add(p);
		    	    		else
		    	    		    projCyanName.add(projectName + "." + p);
		    	    	 }
	    	    	 }
	    	     }
	    	 }
		 }
		 if ( projPath.size() > size )
			 return getAllProjects(projPath, projCyanName, size);
		 return null;
	}


	/**
	 *
	 *
	   @param projPath
	   @param projCyanName
	   @param outp
	 */
	public static String generatePackageSourceList(
			ArrayList<String> projPath,
			ArrayList<String> projCyanName,  PrintWriter outp ) {

		for (int i = 1; i < projPath.size(); i++) {
			File projFile = new File(projPath.get(i));
			String canProjFileName = null;
			try {
				canProjFileName = projFile.getCanonicalPath();
			} catch (IOException e) {
				return "error handling file " + projFile.getName();
			}
			int numCyanSourceFilesFoundInPackage = 0;
			if ( ! projFile.exists() ) {
				return "File " + projPath.get(i) + " does not exist";
			}
			String cyanSource[] = projFile.list();
			for ( String source : cyanSource ) {
			    String canCyanSource = canProjFileName + NameServer.fileSeparator +
		                               source;
			    File f = new File(canCyanSource);
				if ( source.endsWith(".cyan") && ! f.isDirectory() ) {
					++numCyanSourceFilesFoundInPackage;
					if ( numCyanSourceFilesFoundInPackage == 1 ) {
						outp.println("    package " + projCyanName.get(i) + " at \"" + canProjFileName + "\"");
					}
				}
			}
		}
		return null;
	}



	static void printText(String message, char []text, int size) {
		System.out.println("*** " + message + "****");
		for (int i = 0; i < size; ++i)
			if ( text[i] == '\n' )
				System.out.print("\\n");
			//else if ( text[i] == ' ')
			//	System.out.print("_");
			else if ( text[i] == '\r')
				System.out.print("\\r");
			else if ( text[i] == '\0')
				System.out.println("**********************  Found \\0 at index " + i );
			else
				System.out.print(text[i]);
		System.out.println("*** end ****");
	}
	/**
	 * for each compilation unit there is a list of changes to be made which may be:
	 * <ul>
      <li> change the metaobject annotation from something like "<code>{@literal @}annotation(1)</code>" to
      <code>"{@literal @}annotation#ati(1)"</code>. That is,
      add a suffix or change a suffix;
      <li> add text to the compilation unit. It may be instance variables, methods, or
      code after a metaobject annotation;
       <li> delete text. Macro calls and literal objects such as <code>101bin</code> and <code>{@literal @}graph{% 1:2 %}</code>
         are always removed from the source code in phase dsa.
       </ul>
     <p>
     These changes may affect each other because each one should be made at a pre-defined position
     in the text of the compilation unit and this position changes after each insertion or deletion of code.
     For example, suppose metaobject annotation "<code>{@literal @}annotation(1)</code>" is attached to a prototype
     <code>Test</code> of file "Test.cyan". This
     metaobject annotation adds an instance variables "iv0001" and "c0001" to this prototype
     at phase ati. Before doing any changes in "Test.cyan", method {@link #makeChange} calculates the positions
     in the file where the changes will be made. Assume "<code>{@literal @}annotation(1)</code>" is at position 45,
     the instance variable "iv0001" should be inserted at position 150 and the instance variable "c0001" should be added at position
     150 too. <br>
     Suppose now that {@link #makeChange} first changes "<code>{@literal @}annotation(1)</code>" to
     <code>"{@literal @}annotation#ati(1)"</code>. Four characters were inserted and the positions where the instance variables
     should be inserted should be changed to 154. If the code added to the instance variable "iv0001" is
     <code>"Int iv0001\n"</code>, now the other instance variable should be added at position 161. The size of
     <code>"Int iv0001\n"</code> is 11 characters.

      The code below sorts the changes of each compilation unit by offset and then apply the
      changes.
	 */
	public static void makeChanges(
			HashMap<CompilationUnitSuper, ArrayList<SourceCodeChangeByMetaobjectAnnotation>> setOfChanges, Env env) {

		for ( Map.Entry<CompilationUnitSuper, ArrayList<SourceCodeChangeByMetaobjectAnnotation>> entry: setOfChanges.entrySet() ) {

			ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = entry.getValue();
			Collections.sort(changeList);
			CompilationUnitSuper eachCompUnit = entry.getKey();
			char []text = eachCompUnit.getText();

			// printText("original", text, text.length);
			int shiftSizeText = 0;
			for ( SourceCodeChangeByMetaobjectAnnotation change : changeList ) {
				shiftSizeText += change.getSizeToAdd();
				shiftSizeText -= change.getSizeToDelete();
			}

			int newTextSize = text.length + shiftSizeText;
			char []newText = new char[newTextSize];
			int nextStop;
			int i = 0;
			int p = 0;
			int j = 0;

			if ( changeList.size() > 0 ) {
				//int shiftOffsetChange = 0;
				while ( true ) {
					if ( i < changeList.size() )
						nextStop = changeList.get(i).offset;
					else {
						/*
						 * copy the characters after the last change
						 */
						while ( p < text.length ) {
							newText[j] = text[p];
							++j;
							++p;
						}
						break;
					}
					/*
					 * copy the characters till the next change
					 */
					while ( p < nextStop ) {
						newText[j] = text[p];
						++j;
						++p;
					}
					// printText("after copy", newText, j);

					int useInError = i;
					/*
					 * The changes of one specific offset (nextStop) should consist of at most one delete and any
					 * number of text additions
					 */
					int numberOfDeletionsOperations = 0;
					while ( i < changeList.size() && changeList.get(i).offset == nextStop ) {
						int sizeToDelete = changeList.get(i).getSizeToDelete();
						if ( sizeToDelete > 0 ) {
							++numberOfDeletionsOperations;
							if ( numberOfDeletionsOperations > 1 ) {
								String all = "[";
								while ( useInError < changeList.size() && changeList.get(useInError).offset == nextStop ) {
									if ( changeList.get(useInError).getCyanMetaobjectAnnotation() != null ) {
										CyanMetaobjectAnnotation annotation = changeList.get(useInError).getCyanMetaobjectAnnotation();
										all = "(name: " + annotation.getCyanMetaobject().getName() + " package of annotation: " +
												annotation.getPackageOfAnnotation() + " prototype of annotation: " + annotation.getPrototypeOfAnnotation() +
												" line number of annotation: " + annotation.getFirstSymbol().getLineNumber() + ") ";

									}
								}
								all = "]\n";
								env.error(null, "Two or more metaobjects of the following list are trying to delete code in exactly the same offset in file "
									 + "'" + eachCompUnit.getFullFileNamePath() + "'. " + all);
							}
						}
						p += sizeToDelete;
						StringBuffer textToAdd = changeList.get(i).getTextToAdd();
						if ( textToAdd != null ) {
							// copy text to add to newText
							for (int k = 0; k < textToAdd.length(); ++k) {
								newText[j] = textToAdd.charAt(k);
								++j;
							}
						}
						++i;
						// printText("after adding/removing", newText, j);
					}

				}
				eachCompUnit.setText(newText);
				/*
				if ( eachCompUnit.getFullFileNamePath().contains("Program") ) {
					printText("Program", newText, newText.length);
				}
				*/

			}


		}
	}

	public Project getProject() {
		return project;
	}


	/**
	 * true if the compiler was called from the command line. That is, it was called from method 'main'.
	 */
	boolean compilerCalledFromCommandLine = false;

	private Program program;
	private Project project;
	/**
	 * true if variable errorList has been initialized.
	 */
	private boolean setErrorList;

    /**
     *  list of errors in the Cyan source code and in the Cyan project
     */
    private ArrayList<UnitError> errorList;

	/**
	 * true if variable projectErrorList has been initialized.
	 */

	private boolean setProjectErrorList;

    private ArrayList<ProjectError> projectErrorList;

	public Symbol[] getSymbolList() {
		return symbolList;
	}


	public int getSizeSymbolList() {
		return sizeSymbolList;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getCodegList() {
		return this.codegList;
	}



	/**
	 * list of all symbols found in the compilation done with parseSingleSource
	 */
	private Symbol []symbolList = null;


	/**
	 * size of symbolList
	 */
	private int sizeSymbolList = 0;

	/**
	 * list of codegs of the last compilation done with parseSingleSource
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> codegList;

	public CompilationUnit getLastCompilationUnitParsed() {
		return lastCompilationUnitParsed;
	}

	/**
	 * the arguments that should be passed to the Cyan program
	 */
	private String cmdLineArgs = null;

	boolean errorsHaveBeenPrinted = false;
}
