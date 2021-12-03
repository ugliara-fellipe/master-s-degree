package saci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import ast.CompilationUnit;
import ast.CompilationUnitDSL;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Expr;
import ast.ExprGenericPrototypeInstantiation;
import ast.ExprIdentStar;
import ast.ICheck_cin;
import ast.InterfaceDec;
import ast.PW;
import ast.Program;
import ast.ProgramUnit;
import ast.Type;
import error.CompileErrorException;
import error.ErrorKind;
import error.FileError;
import error.UnitError;
import lexer.Symbol;
import meta.CompilerManager_ati;
import meta.Compiler_ati;
import meta.Compiler_dpa;
import meta.CyanMetaobject;
import meta.CyanMetaobjectLiteralObjectSeq;
import meta.CyanMetaobjectWithAt;
import meta.IActionAssignment_cge;
import meta.IActionMessageSend_dsa;
import meta.IActionPackage_ati;
import meta.IActionProgram_ati;
import meta.IActionVariableDeclaration_dsa;
import meta.IAction_cge;
import meta.IAction_dpa;
import meta.IAction_dsa;
import meta.ICheckProgramUnit_ati_dsa;
import meta.ICompileTimeDoesNotUnderstand_dsa;
import meta.ICompiler_ati;
import meta.ICompiler_dpa;
import meta.IParse_dpa;
import meta.ReplacementPolicyInGenericInstantiation;


/**
 * Compiles a Cyan program described by a project.
 * @author José
 *
 */
public class CompilerManager {



	public CompilerManager( Project project, Program program, PrintWriter printWriter, HashMap<String, String> compilerOptions ) {
		this.project = project;
		this.program = program;
		this.printWriter = printWriter;
		this.compilerOptions = compilerOptions;
		nameSet = new HashSet<>();
	}
	
	
	/**
	 * return a compiler for compiling <code>sourceCode</code> of file 'sourceCodeCanonicalPath/sourceCodeFilename' of package 'cyanPackage' 
	 */
	public static ICompiler_dpa getCompilerToDSL(char []sourceCode, String sourceCodeFilename, String sourceCodeCanonicalPath, CyanPackage cyanPackage) {

		CompilationUnitDSL dslCompilationUnit = new CompilationUnitDSL(sourceCodeFilename, sourceCodeCanonicalPath, cyanPackage);
		dslCompilationUnit.setText(sourceCode);
		HashSet<CompilationInstruction> compInstSet = new HashSet<>();
		Compiler comp = new Compiler( dslCompilationUnit, compInstSet, CompilationStep.step_1, null, null );
		Compiler_dpa compiler_dpa = new Compiler_dpa(comp, null, null);
		return compiler_dpa;
	}
	
	/**
	 * read all source files to their compilation units. Return true if no error.
	 * 
	 */
	public boolean readSourceFiles() {
		for ( CompilationUnit compilationUnit : program.getCompilationUnitList() ) {
			try {
				compilationUnit.readSourceFile();
			}
			catch (Exception e ) {
				return false;
			}
		}
		return true;
	}
	/**
	 * compile the project passed as parameter to the constructor. The basic 
	 * packages of Cyan, stored in project <code>cyanLangPackageProject</code>,  
	 * are added to the project. Currently there is only one basic package
	 * of the language: "cyan.lang". This should be  
	 * included in every compilation.
	 * 
	 * @return <code>false</code> in error 
	 */
	public boolean compile(HashSet<saci.CompilationInstruction> compInstSet) {
		
		Compiler compiler;
		
		try {
			

			// list of non-generic prototypes of the programc
			nonGenericCompilationUnitList = new ArrayList<CompilationUnit>();
			// list of generic prototypes of the program
			ArrayList<CompilationUnit> genericCompilationUnitList = new ArrayList<CompilationUnit>();

			
			   /* separates the compilation units (source files) that have generic prototypes from
                  those that don´t.
                  A prototype whose file has a digit after '(' is a generic prototype. For example,
                     Proto(1)(1).cyan has a generic prototype Proto<T><R>. And file
                  MyData<main.Person><Int> is put in file MyData(main.Person)(Int).cyan.   
               */ 
			
			for ( CompilationUnit compilationUnit : program.getCompilationUnitList() ) {
		
				/*
				   // if the file name has a '(' character followed by a digit, then it is a 
				   // generic prototype. Note that "Stack(Int).cyan" contains prototype "Stack<Int>"
				   // which is not considered generic
	    			 * 
	    			 */
				String filename = compilationUnit.getFilename();
				boolean foundDigit = false;
				int ifn = 0;
				int sizeFilename = filename.length();
				while ( ifn < sizeFilename ) {
					if ( filename.charAt(ifn) == '('  && Character.isDigit(filename.charAt(ifn + 1)) ) {
						foundDigit = true;
						break;
					}
					++ifn;
				}
				if ( foundDigit ) {
					compilationUnit.setHasGenericPrototype(true);
					genericCompilationUnitList.add(compilationUnit);
				}
				else
					nonGenericCompilationUnitList.add(compilationUnit);
				/*
				if ( indexOfLeftPar > 0 && Character.isDigit(compilationUnit.getFilename().charAt(indexOfLeftPar + 1))  )
					genericCompilationUnitList.add(compilationUnit);
				else 
					nonGenericCompilationUnitList.add(compilationUnit);
				*/
			}
			
			/**
			 * delete all files from "--tmp" directories. If a generic prototype is in a directory "D", 
			 * the Compiler creates the generic instantiations in "D:\--tmp". For example, if "Stack{@literal <}T>" is
			 * in directory "util", the Compiler creates prototypes "Stack{@literal <}Int>" and "Stack{@literal <}Person>" 
			 * (assuming the program uses "Stack{@literal <}Int>" and "Stack{@literal <}Person>") in directory
			 * "util\--tmp".
			 * /
			Set<String> pathCompilationUnitTable = new HashSet<String> ();
			for ( CompilationUnit c : genericCompilationUnitList ) {
				pathCompilationUnitTable.add(c.getPackageCanonicalPath());
				
				c.getCyanPackage().setHasGenericPrototype(true);
			}
			for ( String path : pathCompilationUnitTable ) {
				File dir = new File(path + NameServer.temporaryDirName);
				if ( dir.exists() )
				    for( File file: dir.listFiles() ) 
				    	file.delete();
			}
			*/

			String dotExtension = "." + NameServer.cyanSourceFileExtension;
			boolean thereWasErrorsGenericCompilationUnitList = false;

			/**
			 * first of all, parse all generic prototypes. This is not allowed in step 7 of the compilation
			 * because all generic prototype instantiation should have been created before that.
			 */
			if ( this.compilationStep.compareTo(CompilationStep.step_7) < 0 ) {
	    		for ( CompilationUnit compilationUnit : genericCompilationUnitList ) {
					compiler = new Compiler(compilationUnit, compInstSet, compilationStep, project, null);
					try {
						compiler.parse();
					}
					catch ( RuntimeException e ) {
						compilationUnit.error(1, 1, "Internal error: exception '" + e.getClass().getName() + "' was thrown");
					}
					/**
					 * print the errors found in the generic prototypes and apply all actions to them.
					 * An action is a small refactoring like insert a ";"
					 */
					if ( compilationUnit.hasCompilationError() ) {
						thereWasErrorsGenericCompilationUnitList = true;
					}
					
					if ( compilationUnit.getActionList().size() > 0 )
						compilationUnit.doActionList(printWriter);
				}				
			}

    		/* if ( hasCompilationError ) 
    			return false; 
    		*/

			/*
			 * in the first step of this while statement, all non-generic prototypes are compiled. 
			 * 
			 * In the second step of the while statement, the real prototypes created in the previous step 
			 * are compiled. They may instantiate new generic prototypes. For example, Stack<Int> may 
			 * declare a variable of type "Array<Int>". This new Cyan prototype should be created and 
			 * compiled. The process continues till no new prototypes should be created.   
			 */
			CompilationUnit compilationUnit;
			int numCompilationUnitsAlreadyCompiled = 0;
			int sizeNonGenericCompilationUnitList; 
			while ( numCompilationUnitsAlreadyCompiled < nonGenericCompilationUnitList.size() ) {
				
				sizeNonGenericCompilationUnitList = nonGenericCompilationUnitList.size();
				boolean thereWasErrors = thereWasErrorsGenericCompilationUnitList;

			    // parse of all source files that were not yet parsed. That may include some
				// generic prototypes that were instantiated in the previous round of the above
				// while statement.
				for (int i = numCompilationUnitsAlreadyCompiled; i < sizeNonGenericCompilationUnitList; i++) {
					compilationUnit = nonGenericCompilationUnitList.get(i);
					
					compiler = new Compiler(compilationUnit, compInstSet, compilationStep, project, null);
					compiler.parse(); 
					//if ( ! compilationUnit.hasCompilationError() ) {
					//}
					if ( compilationUnit.hasCompilationError() ) {
						thereWasErrors = true;
					    // compilationUnit.printErrorList(printWriter);
					}
					else if ( compInstSet.contains(CompilationInstruction.createPrototypesForInterfaces) &&
							
							  compilationUnit.getPrototypeIsNotGeneric()  &&
							  compilationUnit.getPublicPrototype() instanceof InterfaceDec ) {
						// if public program unit is an interface, create ProtoInterface
						CompilationUnit newCompilationUnit = compilationUnit.createProtoInterface();
						if ( newCompilationUnit == null ) {
							if ( compilationUnit.hasCompilationError() ) {
								thereWasErrors = true;
							    //compilationUnit.printErrorList(printWriter);
							}
						}
						else {
							CyanPackage thisCyanPackage = compilationUnit.getCyanPackage();
							
							thisCyanPackage.addCompilationUnit(newCompilationUnit);
							newCompilationUnit.setCyanPackage(thisCyanPackage);
							nonGenericCompilationUnitList.add(newCompilationUnit);
							
							String name = newCompilationUnit.getFilename();
							int indexDotCyan = name.indexOf(dotExtension);
							if ( indexDotCyan > 0 )
								name = name.substring(0, indexDotCyan);
							
							program.addCompilationUnit(newCompilationUnit);
							
							nameSet.add(newCompilationUnit.getFilename());
						}
					}
					  // if there was not any errors and there is a list of actions ...
					/*if ( compilationUnit.getActionList().size() > 0 )
						compilationUnit.doActionList(printWriter);
					compilationUnit.clearErrorsActions(); */
				}
				

				if ( thereWasErrors ) {
					return false;
				}
				numCompilationUnitsAlreadyCompiled = sizeNonGenericCompilationUnitList;
			}

		}
		catch ( Exception e ) {
			e.printStackTrace();
			project.error("Internal error at CompilerManager::compile(). e = " + 
		               e.getClass().getName());
			return false;
		}
		
		// project.printErrorList(printWriter);
		return true;
		
	}

	/**
	 * create a new prototype whose name is prototypeName and whose source code is <code>code</code>.
	 * The compiler options of this compilation unit should be prototypeCompilerOptions. Its
	 * package is cyanPackage.
	 *
	 * This method returns the compilation unit and an error message.
	 * 
	 */
	public Tuple2<CompilationUnit, String> createNewPrototype( String prototypeName, StringBuffer code,
			CompilerOptions prototypeCompilerOptions, CyanPackage cyanPackage) {
		
		if ( prototypeName.contains("<") || prototypeName.contains(">") ) {
			return new Tuple2<CompilationUnit, String>(null, "Cannot create generic prototype '" + prototypeName + "' using metaobjects");
		}
		String fileName, packageCanonicalPath1 = "";
		String newFileName;		
		FileOutputStream fos = null;
		newFileName = fileName = cyanPackage.getPackageCanonicalPath();
		char []newText = new char[code.length() + 1];
		code.getChars(0, code.length(), newText, 0);
		if ( code.charAt(code.length()-1) != '\0' ) {
			newText[newText.length-1] = '\0';
		}

		try {

			int indexOfStartFileName = fileName.lastIndexOf(File.separator);
			if ( indexOfStartFileName  > 0 ) {
				newFileName = fileName.substring(0, indexOfStartFileName);
				String dirName = newFileName + File.separator + NameServer.temporaryDirName;
				File dir = new File(dirName);
				if ( ! dir.exists() ) {
					dir.mkdirs();
				}
				packageCanonicalPath1 = dirName + File.separator;
				newFileName = packageCanonicalPath1 + prototypeName + NameServer.dotCyanSourceFileExtension;
			}
			fos = new FileOutputStream(newFileName);
			PrintWriter pWriter = new PrintWriter(fos, true);
			PW pw = new PW();
			pw.set(pWriter);
			if ( code.charAt(code.length()-1) == '\0' ) {
				code.deleteCharAt(code.length()-1);
			}
			pw.println(code);
			
			pWriter.close();
		}
		catch ( FileNotFoundException e1 ) {
			return new Tuple2<CompilationUnit, String>(null, "Cannot create file " + newFileName);
		}
		catch ( NullPointerException e3 ) {
			try { if ( fos != null ) fos.close(); } catch (IOException e) { }
			return new Tuple2<CompilationUnit, String>(null, "Internal error in CompilationUnit when writing to file " + newFileName);
		}
		catch (Exception e2 ) {
			try { if ( fos != null ) fos.close(); } catch (IOException e) { }
			return new Tuple2<CompilationUnit, String>(null, "error in writing to file " + newFileName);
		}
	
		CompilationUnit compilationUnit = new CompilationUnit(
				prototypeName + NameServer.dotCyanSourceFileExtension,
				packageCanonicalPath1,
				prototypeCompilerOptions,
				cyanPackage
				);
		
		
		cyanPackage.addCompilationUnit(compilationUnit);
		compilationUnit.setCyanPackage(cyanPackage);
		nonGenericCompilationUnitList.add(compilationUnit);
		
		
		
		program.addCompilationUnit(compilationUnit);

		
		compilationUnit.setText(newText);
		// newCompilationUnit.readSourceFile();
		
		/*
		HashSet<saci.CompilationInstruction> compInstSet = new HashSet<>();
		compInstSet.add(CompilationInstruction.dpa_actions);
		compInstSet.add(CompilationInstruction.pp_addCode);
		if ( compilationStep.compareTo(CompilationStep.step_5) >= 0 ) 
			compInstSet.add(CompilationInstruction.new_addCode);
		Compiler compiler = new Compiler(compilationUnit, compInstSet, compilationStep, project, null);
		compiler.parse();
		
		if ( compilationUnit.hasCompilationError() ) {
			throw new CompileErrorException();
		}
		*/
		return new Tuple2<CompilationUnit, String>(compilationUnit, null);
	}
	
	/**
	 * Create a generic prototype from parameter gpi. The new prototype is compiled. If it is an interface,
	 * the prototype Proto_IntefaceName is created too. Methods calcInterfaceTypes and calcInternalTypes are called 
	 * for both the prototype and its Proto_InterfaceName, if any.
	   @param gpi
	   @param env
	   @return
	 */
	public static ProgramUnit createGenericPrototype(ExprGenericPrototypeInstantiation gpi, Env env) {
		
		


		saci.Tuple<String, Type> t = gpi.ifPrototypeReturnsNameWithPackageAndType(env);

		// t.f1 is the name of the prototype
		if ( t != null && t.f2 != null ) {
			/*
			 * prototype has already been created before
			 */
			if ( t.f2 instanceof ProgramUnit ) {
				return (ProgramUnit ) t.f2;
			}
			else {
				env.error(gpi.getFirstSymbol(), "Internal error: a type that is not a program unit is used to instantiate a generic prototype");
				return null;
			}
				
		}
		else {
			/*
			 * prototype has not been created. Create it. But this is only allowed in compilation steps < 7
			 */
			
			
			//if ( env.getCurrentMethod() != null && env.getCurrentMethod().getName().contains("run2") )
			//	gpi.ifPrototypeReturnsItsNameWithPackage(env);

			/*
			 * first, create all prototypes that are real parameters to this generic prototype instantiation 
			 */
			for ( ArrayList<Expr> realTypeList : gpi.getRealTypeListList() ) {
				for ( Expr realType : realTypeList ) {
					if ( realType instanceof ExprGenericPrototypeInstantiation ) {  
						ExprGenericPrototypeInstantiation genRealType = (ExprGenericPrototypeInstantiation ) realType;
						genRealType.setType( 
								createGenericPrototype( genRealType, env) );
						// 		javaName = type.getJavaName();
						genRealType.setJavaName(genRealType.getType().getJavaName());
					}
				}
			}
			
			CompilationUnit compUnit = env.getProject().getCompilerManager().createGenericPrototypeInstantiation(gpi, env);
			



			Env newEnv = new Env(env.getProject());
			/*
			 * If a generic prototype is created when method calcInterfaceTypes of Program is being executed
			 * (env.getProject().getProgram().getInCalcInterfaceTypes() returns true), it is not necessary 
			 * to call its method calcInterfaceTypes. It will be called in the loop of
			 * method Program::calcInterfaceTypes.  

			 * If a generic prototype is created when method calcInternalTypes of Program is being executed,
			 * method calcInterfaceTypes of this generic prototype should be called because the interface
			 * of this prototype is necessary in methods calcInternalTypes. Method calcInternalTypes
			 * of this newly created generic prototype will be called in the loop of
			 * method Program::calcInternalTypes.  
			 * 
			 * calcInternalTypes cannot be called when a generic prototype is created when
			 * the compiler is calling calcInterfaceTypes. If this is allowed, some calcInternalType
			 * method could try to use the interface of some prototype P whose method calcInterfaceTypes
			 * have not been called. 
			 */
			if ( ! env.getProject().getProgram().getInCalcInterfaceTypes() ) {
				try {
					compUnit.calcInterfaceTypes(newEnv);
				}
				finally {
					if ( newEnv.isThereWasError() ) {
						env.setThereWasError(true);
						// copyErrorsFromTo(newEnv, env);
					}
				}
				// compUnit.calcInternalTypes(newEnv);
			}
			CompilationUnit interfaceCompilationUnit = compUnit.getInterfaceCompilationUnit();
			if (  interfaceCompilationUnit != null ) {
				newEnv = new Env(env.getProject());
				if ( ! env.getProject().getProgram().getInCalcInterfaceTypes() ) {
					
					try {
						interfaceCompilationUnit.calcInterfaceTypes(newEnv);
					}
					catch ( CompileErrorException e ) {
						if ( newEnv.isThereWasError() ) {
							copyErrorsFromTo(newEnv, env);
						}
						throw e;
					}
					if ( newEnv.isThereWasError() ) {
						copyErrorsFromTo(newEnv, env);
					}
				}
			}
			/*
			 * if the generic prototype was created from phase (4), included, onwards, then execute
			 * the ati actions. A generic prototype instantiation can only change itself and
			 * no other prototype can change it.
			 */
			if ( env.getProject().getCompilerManager().getCompilationStep().ordinal() > CompilationStep.step_3.ordinal() ) {
				apply_ati_ActionsToGenericPrototype(newEnv, compUnit);
				
				// *************************************************

				/*
	    		CompilationStep compStep = env.getProject().getCompilerManager().getCompilationStep();
	    		if ( compStep.ordinal() >= CompilationStep.step_6.ordinal() ) {
		    		HashSet<saci.CompilationInstruction> compInstSet = new HashSet<>();
		    		compInstSet.add(CompilationInstruction.dpa_actions);
					Compiler compiler = new Compiler(compUnit, compInstSet, compStep, env.getProject(), null);
					boolean allowCreationOfPrototypesInLastCompilerPhases = env.getAllowCreationOfPrototypesInLastCompilerPhases();;
					try {
						if ( allowCreationOfPrototypesInLastCompilerPhases ) {
							compiler.setAllowCreationOfPrototypesInLastCompilerPhases(true);
						}
						compiler.parse();
					}
					finally {
						compiler.setAllowCreationOfPrototypesInLastCompilerPhases(allowCreationOfPrototypesInLastCompilerPhases);
					}
					
									
	    			
	    		}
	    		*/
				
				// *************************************************
				if (  interfaceCompilationUnit != null )  {
					apply_ati_ActionsToGenericPrototype(env, interfaceCompilationUnit);
				}
			}
			if ( newEnv.isThereWasError() ) {
				env.setThereWasError(true);
			}
			return compUnit.getPublicPrototype();
			
		}
		
		
	}
	
	public static void copyErrorsFromTo(Env from, Env to) {
		Map<CompilationUnit, ArrayList<UnitError>> mapCompUnitErrorList = from.getMapCompUnitErrorList();
		if ( mapCompUnitErrorList != null ) {
			for ( CompilationUnit cunit : mapCompUnitErrorList.keySet() ) {
				ArrayList<UnitError> errorList = mapCompUnitErrorList.get(cunit);
				if ( errorList != null ) {
					for ( UnitError error : errorList ) {
						to.addError(error);
					}
				}
			}
		}
	}
	
	private static void apply_ati_ActionsToGenericPrototype(Env env, CompilationUnit compUnit) {
		ICompiler_ati compiler_ati = new Compiler_ati(env);
		CompilerManager_ati compilerManager = new CompilerManager_ati(env);
		
		
		compUnit.ati_actions(compiler_ati, compilerManager);
				
		/*
		 * all changes demanded by metaobject annotations collected above are made in the call
		 * to CompilerManager_ati#changeCheckProgram.
		 */
		compilerManager.changeCheckProgram();
				
	}
	
	/**
	 * Create an instantiation of a generic prototype given by parameter gt
	 * 
	   @param gt
	   @param env
	   @return
	 */
	private CompilationUnit createGenericPrototypeInstantiation(ExprGenericPrototypeInstantiation gt, Env env) {


		
		CompilationUnit genericProto = null;
		
		String genSourceFileName = gt.getGenericSourceFileName();
		
		
		String genSourceFileNameVaryingNumberOfParameters = gt.getGenericSourceFileNameWithVaryingNumberOfParameters();
		boolean isInterface = false;
		if ( NameServer.isPrototypeFromInterface(genSourceFileName) ) {
			isInterface = true;
			genSourceFileName = NameServer.prototypeFileNameFromInterfaceFileName(genSourceFileName);
			genSourceFileNameVaryingNumberOfParameters = NameServer.prototypeFileNameFromInterfaceFileName(
					genSourceFileNameVaryingNumberOfParameters);
			
		}
		// something like util.Stack if gt is "util.Stack<Int>" or
		// Stack if gt is Stack<Int>
		ExprIdentStar typeIdent = gt.getTypeIdent();
		if ( typeIdent.getIdentSymbolArray().size() == 1 ) {
			// no package preceding the generic prototype name as in "Stack<Int>"
			ProgramUnit pu = env.searchProgramUnitBySourceFileName(genSourceFileName, gt.getFirstSymbol(), false);
			if ( pu != null ) {
				genericProto = pu.getCompilationUnit();
				ProgramUnit pu2 = env.searchProgramUnitBySourceFileName(genSourceFileNameVaryingNumberOfParameters, gt.getFirstSymbol(), false);
				if ( pu2 != null ) 
					/* found both generic prototype and generic prototype with varying number of parameters
					 * Example: found both Tuple<T> and Tuple<T+>
					 */
					env.error(gt.getFirstSymbol(), "Ambiguity in creating a real prototype from a generic prototype. There is both "
					 + pu.getCompilationUnit().getPackageName() + "."	+ genSourceFileName + " and "  + 
					 pu2.getCompilationUnit().getPackageName() + "." + genSourceFileNameVaryingNumberOfParameters, true, true 
						);
					
			}
			if ( genericProto == null ) {
				pu = env.searchProgramUnitBySourceFileName(genSourceFileNameVaryingNumberOfParameters, gt.getFirstSymbol(), false);
				if ( pu != null ) 
					genericProto = pu.getCompilationUnit();
			}
		}
		else {
			// package preceding the generic prototype name as in "util.Stack<Int>"
			int i = 0;
			ArrayList<Symbol> symbolList = typeIdent.getIdentSymbolArray();
			int sizeLessOne = symbolList.size() - 1;
			String packageName = "";
			while ( i < sizeLessOne ) {
				packageName = packageName + symbolList.get(i).getSymbolString();
				++i;
				if ( i < sizeLessOne ) 
					packageName += ".";
			}
			CyanPackage cyanPackage = env.getProject().searchPackage(packageName);
			if ( cyanPackage == null ) {
				env.error(typeIdent.getFirstSymbol(), "Package '" + packageName + "' was not found", true, true);
				return null;
			}
			// first searches for something like "Stack(1)" in package 'util'
			for ( CompilationUnit cunit : cyanPackage.getCompilationUnitList() ) {
				if ( genSourceFileName.equals(cunit.getFileNameWithoutExtension()) ) {
					genericProto = cunit;
					break;
				}
					
			}
			CompilationUnit genericProto2 = null;
			// searches for a generic prototype with varying number of parameters
			// something like "Stack(1+)"
			for ( CompilationUnit cunit : cyanPackage.getCompilationUnitList() ) {
				if ( genSourceFileNameVaryingNumberOfParameters.equals(cunit.getFileNameWithoutExtension()) ) {
					genericProto2 = cunit;
					break;
				}
					
			}
			
			if ( genericProto != null && genericProto2 != null ) {
				env.error(gt.getFirstSymbol(), "Ambiguity in creating a real prototype from a generic prototype. There is both "
						 + genericProto.getPackageName() + "."	+ genSourceFileName + " and "  + 
						 genericProto2.getPackageName() + "." + genSourceFileNameVaryingNumberOfParameters, true, true 
							);
			}
			if ( genericProto == null ) 
				genericProto = genericProto2;
		}
		
		
		
		//genericProto = nameGenProtoUnitTable.get(genSourceFileName);
		
		if ( genericProto == null ) {
			if ( env.getProject().getCompilerManager().getCompilationStep() == CompilationStep.step_9 ) {
				env.error(true, gt.getFirstSymbol(), 
						"Prototype '" + gt.getName() + "' was not found. This prototype probably was " +
				        "instantiated by code generated during semantic analysis. You cannot instantiate new generic prototypes in this phase", 
						gt.getName(), ErrorKind.prototype_was_not_found_inside_method );
			}
			else {
				env.error(true, gt.getFirstSymbol(), 
						"Prototype '" + gt.getName() + "' was not found", gt.getName(), ErrorKind.prototype_was_not_found_inside_method );
			}
			return null;
		}
		else {
			/**
			 * if there was no compilation error in "Stack(1).cyan", then create an instance of
			 * the generic prototype
			 */
			
			if ( env.getProject().getCompilerManager().getCompilationStep().ordinal() >= CompilationStep.step_7.ordinal() ) {
				/*
				 * it is allowed to create instantiations of generic prototypes only for super-prototypes of inner prototypes.
				 * There is an inner prototype for each anonymous function of the prototype.
				 */
				if ( ! env.getAllowCreationOfPrototypesInLastCompilerPhases() && ! genericProto.getPackageName().equals(NameServer.cyanLanguagePackageName)  ) {
					env.error(gt.getFirstSymbol(), "Attempt to create a generic prototype, " + gt.asString() + " after step 6 of the compilation." +
					         "This is caused by code introduced in step 6, semantic analysis, that introduced a new generic prototype instantiation. " +
								"This could be a new type of variable or a new literal object such as an anonymous function or tuple. To solve that, " +
					         "create this generic prototype before this step. If necessary, use method 'createNewGenericPrototype' of interface ICompiler_dsa"
								);
						return null;
					
				}
			}
			
			if ( isInterface ) {
				gt.removeProtoPrefix();
			}
			
			
			CompilationUnit newCompilationUnit = genericProto.createInstanceGenericPrototype(gt, env);
			CompilationUnit interCompilationUnit = null;
			/**
			 * if the package for this generic prototype instantiation was not created 
			 * before, create it now.
			 */
			
			
			CyanPackage cyanPackage = genericProto.getCyanPackage();
			
			
			cyanPackage.addCompilationUnit(newCompilationUnit);
			newCompilationUnit.setCyanPackage(cyanPackage);
			nonGenericCompilationUnitList.add(newCompilationUnit);
			
			if ( nameSet.contains(newCompilationUnit.getFullFileNamePath()) ) {
				env.error(gt.getFirstSymbol(), "Internal error in CompilerManager");
				gt.ifPrototypeReturnsNameWithPackageAndType(env);
			}
			else
				nameSet.add(newCompilationUnit.getFullFileNamePath());
			
			
			
			program.addCompilationUnit(newCompilationUnit);

			newCompilationUnit.readSourceFile();
    		HashSet<saci.CompilationInstruction> compInstSet = new HashSet<>();
    		compInstSet.add(CompilationInstruction.dpa_actions);
    		compInstSet.add(CompilationInstruction.pp_addCode);
    		if ( compilationStep.compareTo(CompilationStep.step_5) >= 0 ) 
    			compInstSet.add(CompilationInstruction.new_addCode);
			Compiler compiler = new Compiler(newCompilationUnit, compInstSet, compilationStep, project, null);
			boolean allowCreationOfPrototypesInLastCompilerPhases = env.getAllowCreationOfPrototypesInLastCompilerPhases();;
			try {
				if ( allowCreationOfPrototypesInLastCompilerPhases ) {
					compiler.setAllowCreationOfPrototypesInLastCompilerPhases(true);
				}
				compiler.parse();
			}
			finally {
				compiler.setAllowCreationOfPrototypesInLastCompilerPhases(allowCreationOfPrototypesInLastCompilerPhases);
			}
			
			
			

			if ( newCompilationUnit.hasCompilationError() ) {
			    // newCompilationUnit.printErrorList(printWriter);
				env.setThereWasError(true);
				throw new CompileErrorException();
			}
			else if ( compInstSet.contains(CompilationInstruction.createPrototypesForInterfaces) &&
					  newCompilationUnit.getPrototypeIsNotGeneric()  &&
					  newCompilationUnit.getPublicPrototype() instanceof InterfaceDec ) {
				// if public program unit is an interface, create ProtoInterface
				
				interCompilationUnit = newCompilationUnit.createProtoInterface();
				if ( interCompilationUnit != null ) {
					
					interCompilationUnit.setCyanPackage(cyanPackage);
					cyanPackage.addCompilationUnit(interCompilationUnit);
					newCompilationUnit.setCyanPackage(cyanPackage);
					nonGenericCompilationUnitList.add(interCompilationUnit);
					
					String nameInter = interCompilationUnit.getFilename();
					if ( nameInter.endsWith(NameServer.dotCyanSourceFileExtension) )
						nameInter = nameInter.substring(0, nameInter.length() - NameServer.sizeCyanSourceFileExtensionPlusOne );
					
					//nameRealGenProtoUnitTable.put(nameInter, interCompilationUnit);
					program.addCompilationUnit(interCompilationUnit);
					
					interCompilationUnit.readSourceFile();
					compiler = new Compiler(interCompilationUnit, compInstSet, compilationStep, project, null);
					compiler.parse();
					if ( interCompilationUnit.getActionList().size() > 0 )
						interCompilationUnit.doActionList(printWriter);
					interCompilationUnit.clearErrorsActions();

				}
			}
			// newCompilationUnit.clearErrorsActions();
			if ( isInterface ) 
				return interCompilationUnit;
			else
				return newCompilationUnit;
		}
			
		
		
		
	}

	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	
	public static void collectSubDirectories( String path, String parent, ArrayList<String> dirPathList ) {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	dirPathList.add(parent + f.getName() + NameServer.fileSeparatorAsString );
                collectSubDirectories( f.getAbsolutePath(), parent + f.getName() + NameServer.fileSeparatorAsString, dirPathList );
            }
        }
    }

	public static void loadMetaobjectsFromPackage(String packageCanonicalPath, String packageName, ArrayList<CyanMetaobject> metaobjectList,
			Compiler compiler) {

		String slash = File.separator;
		String path;
		String name = "";
		
		
		
		path = packageCanonicalPath + (packageCanonicalPath.endsWith(slash) ? "" : slash ) + NameServer.metaobjectPackageName;
		//path = packageCanonicalPath + (packageCanonicalPath.endsWith(slash) ? "" : slash ) + "meta";
		if ( ! path.endsWith(slash) )
			path = path + slash;

		int dotClassLength = ".class".length();
		String metaobjectName;
		String packageNameMO;
		URLClassLoader urlcl = null;
		try {
			File dir = new File(path);
			if ( dir.exists() ) {
				/*
				urlcl = URLClassLoader.newInstance(new URL[] {
					       new URL("file:///" + path )
					    });
			    */
				
				ArrayList<String> dirPathList = new ArrayList<>();
				dirPathList.add("");
				collectSubDirectories(path, "", dirPathList);
				/*
				for ( String s : dirPathList ) {
					System.out.println("dir ::= " + s);
				}
				*/
				
				/*
				Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach( (Path p) -> { 
					System.out.println(p.getFileName());
				} 
				);
				*/
				
				URL singleURL = (new File(path)).toURI().toURL();
				/*
					urlcl = new URLClassLoader( new URL[] { new URL("file:" + slash + slash + path ) },
						                    Thread.currentThread().getContextClassLoader() ); 
								 * 
				 */
				//String strsss = singleURL.toString();
				urlcl = new URLClassLoader( new URL[] { singleURL },
						                    Thread.currentThread().getContextClassLoader() ); 
				// urlcl = URLClassLoader.newInstance(new URL[]{dir.toURI().toURL()});		
				
				//Class<?> dthis = urlcl.loadClass(NameServer.metaobjectPackageNameCyanCompilerDot + "CyanMetaobjectDeleteThis"); 
				//System.out.println(dthis.getName());

				Class<?> cyanMetaobjectClass = CyanMetaobject.class;
				for ( String classPath : dirPathList ) {
					File dirPathFile = new File(path + classPath);
					packageNameMO = classPath.replace(File.separatorChar,  '.');
					File []fileList = dirPathFile.listFiles();
					if ( fileList == null ) 
						continue;
					for ( File file : fileList ) {

				    	if ( ! file.isDirectory() ) {
				    		name = file.getName();
				    		if ( name.endsWith(".class") ) {
				    			metaobjectName = name.substring(0, name.length() - dotClassLength);
				    			Class<?> clazz = urlcl.loadClass(packageNameMO + metaobjectName);
				    			if ( ! metaobjectName.equals(NameServer.cyanMetaobjectClassName) ) {
				    			    // we are only interested in subclasses of CyanMetaobject or CyanMetaobject
				    				if ( ! cyanMetaobjectClass.isAssignableFrom(clazz) ) 
				    					continue;
				    				
				    			}
				    			try {
				    				int modifier = clazz.getModifiers();
				    				if ( ! Modifier.isAbstract(modifier) && ! Modifier.isInterface(modifier) &&
				    					 ! Modifier.isPrivate(modifier) &&
				    					 ! clazz.isEnum() ) {
				    					
				    					boolean foundConstructorWithoutParameters = false;
				    					Constructor<?>[] constructorList = clazz.getConstructors();
				    					for ( Constructor<?> constructor : constructorList ) {
				    						if ( constructor.getExceptionTypes().length == 0 &&
				    							 constructor.getGenericExceptionTypes().length == 0 &&
				    							 constructor.getParameterTypes().length == 0 &&
				    							 constructor.getGenericParameterTypes().length == 0
				    							 ) {
				    							foundConstructorWithoutParameters = true;
				    							break;
				    						}
				    					}
				    					if ( foundConstructorWithoutParameters ) {
							    			Object newObj = clazz.newInstance();
							    			if ( !(newObj instanceof CyanMetaobject) ) {
							    				compiler.error2(null, "Class of file '" + path + classPath + name + "' should inherit from '" +
							    			          CyanMetaobject.class.getName() + "'");
							    			}
							    			else {
							    				CyanMetaobject cyanMetaobject =  (CyanMetaobject ) newObj;
							    				checkMetaobject(cyanMetaobject, compiler, packageName);
							    				cyanMetaobject.setFileName(name);
							    				cyanMetaobject.setPackageName(packageName);
							    				metaobjectList.add(cyanMetaobject);
							    			}
				    					}
				    					else
						    				compiler.error2(null, "File '" + path + classPath + name + "' should contain a class with a constructor without parameters and that does not throw any exception");
				    				}
				    			}
				    			catch (IllegalAccessException | InstantiationException e) {
				    				compiler.error2(null, "Illegal metaobject in '" + path + classPath + name + "'");
				    			}
				    			
				    		}
				    	}
				    						
					}
				}

				urlcl.close();
							
			}
		}
		catch ( ClassNotFoundException | NoClassDefFoundError | IOException e) {
			compiler.error2(null, "Error when reading metaobjects of path " + path + 
					(name.length() != 0 ? ", probably in file " + name : "") + " The compiler expected this file to exist"
					);
			return;
		}
		catch ( Throwable e ) {
			compiler.error2(null, "Internal error when reading metaobjects of path " + path + 
					(name.length() != 0 ? ", probably in file " + name : "") + " The compiler expected this file to exist"
					);
			return;
		}
		finally {
			if ( urlcl != null ) {
				try {
					urlcl.close();
				}
				catch (IOException e1) {
				}
			}
		}
			
	}

	/**
	 * check inheritance and interfaces implemented by that metaobject <code>cyanMetaobject</code>. Some combinations are illegal
	 * such as a macro metaobject implement {@link meta#IParseWithoutCyanCompiler_dpa}. However, most illegal combinations are not 
	 * checked. This will be a future work.
	   @param cyanMetaobject
	 */
	private static void checkMetaobject(CyanMetaobject cyanMetaobject, Compiler compiler, String packageName) {
		
		
		if ( cyanMetaobject instanceof ICheck_cin ) { 
			if ( !(cyanMetaobject instanceof CyanMetaobjectWithAt) ) {
				compiler.error2(false, compiler.symbol, "Interface 'ICheck_cin' can only be implemented by metaobjects that start with '@'. But it is being"
						+ " implemented by metaobject '" + cyanMetaobject.getName() + "'", true
						); // + "' of package '" + cyanMetaobject.getPackageName() + "'"));
			}
			else {
				CyanMetaobjectWithAt metaobject = (CyanMetaobjectWithAt ) cyanMetaobject;
				if ( ! metaobject.attachedToSomething() ) {
					compiler.error2(false, compiler.symbol, "Metaobject Interface 'ICheck_cin' can only be implemented by metaobjects that are attached "
							+ " to a declaration. But this is not", true
							);
				}
			}
		}
		
		ArrayList<String> incompatibleInterfaceNameList = null;
		if ( cyanMetaobject instanceof meta.IParse_dpa && cyanMetaobject instanceof meta.IAction_dpa ) {
			incompatibleInterfaceNameList = new ArrayList<>();
			incompatibleInterfaceNameList.add(meta.IParse_dpa.class.getName());
			incompatibleInterfaceNameList.add(meta.IAction_dpa.class.getName());
		}
		if ( cyanMetaobject instanceof CyanMetaobjectWithAt ) {
			CyanMetaobjectWithAt withAt = (CyanMetaobjectWithAt ) cyanMetaobject;
			if ( withAt.shouldTakeText() && withAt instanceof IAction_dpa ) {
				compiler.error2(false, compiler.symbol, "Metaobject '" + cyanMetaobject.getName() + "' imported from package '"
						+ packageName + "' should take a text as in @concept{* ... *} and it implements interface '"
						+ meta.IAction_dpa.class.getName() + "'. This is illegal. Metaobjects that take text should implement one "
						+ "of the '" +IParse_dpa.class.getName() + "' sub-interfaces. To produce code, they should implement " + 
						"interface '" + IAction_dpa.class.getName() + "' to produce code in phase 'dsa'", true);
			}
		}
		
		if ( cyanMetaobject instanceof IActionPackage_ati || cyanMetaobject instanceof IActionProgram_ati ) {
			if ( cyanMetaobject instanceof IAction_dpa || cyanMetaobject instanceof IParse_dpa || 
				 cyanMetaobject instanceof IAction_dsa || cyanMetaobject instanceof IActionVariableDeclaration_dsa || 
				 cyanMetaobject instanceof IActionMessageSend_dsa || cyanMetaobject instanceof ICompileTimeDoesNotUnderstand_dsa ||
				 cyanMetaobject instanceof IAction_cge || cyanMetaobject instanceof IActionAssignment_cge ||
				 cyanMetaobject instanceof ICheckProgramUnit_ati_dsa ) {
				incompatibleInterfaceNameList = new ArrayList<>();
				compiler.error2(false, compiler.symbol, "Metaobject '" + cyanMetaobject.getName() + "' imported from package '"
						+ packageName + "' implements interface '" + IActionPackage_ati.class.getName() + "' or '" + 
						IActionProgram_ati.class.getName() + "' and at least one other interface that should be implemented only " +
						"by metaobjects that should not be attached to a package or the program such as '" + IAction_dpa.class.getName() + "'", true);
				
			}
		}
		if ( cyanMetaobject instanceof IParse_dpa && cyanMetaobject instanceof IAction_dpa ) {
			compiler.error2( false, compiler.symbol, "Metaobject '" + cyanMetaobject.getName() + "' imported from package '"
					+ packageName + "' implements interfaces '" + IParse_dpa.class.getName() + "' and '" + 
					IAction_dpa.class.getName() + "'. They are incompatible, only one of them should be implemented", true);
			
		}
		if ( cyanMetaobject instanceof IParse_dpa && cyanMetaobject instanceof CyanMetaobjectWithAt ) {
			if ( ! ((CyanMetaobjectWithAt ) cyanMetaobject).shouldTakeText() ) {
				compiler.error2( false, compiler.symbol, "Metaobject '" + cyanMetaobject.getName() + "' imported from package '"
						+ packageName + "' implements interface '" + IParse_dpa.class.getName() + "'. Therefore a text (DSL) should be " + 
						"attached to the metaobject annotation. However, this is not possible because method 'shouldTakeText' returns false",
						true);
			}
			
		}

		if ( cyanMetaobject instanceof CyanMetaobjectLiteralObjectSeq ) {
			String lcs = ((CyanMetaobjectLiteralObjectSeq) cyanMetaobject).leftCharSequence();
			if ( illegalLeftCharSeq.contains( lcs ) && !packageName.equals(NameServer.cyanLanguagePackageName) ) {
				compiler.error2( false, compiler.symbol, "Metaobject '" + cyanMetaobject.getName() + "' imported from package '"
						+ packageName + "' extends '" + CyanMetaobjectLiteralObjectSeq.class.getName() + "' and it defines "
								+ "'" + lcs + "' as "
								+ "left char sequence. This sequence is illegal because it is reserved for the package "
										+ "cyan.lang or it is '{*', the standard for delimiting DSL code that follows a metaobject annotation",
						true);
				
			}
		}
		
		if ( incompatibleInterfaceNameList != null ) {
			String all = "";
			for ( String s : incompatibleInterfaceNameList ) {
				all += s + " ";
			}
			compiler.error2( false, compiler.symbol, "Metaobject '" + cyanMetaobject.getName() + "' imported from package '"
					+ packageName + "' implements two incompatible interfaces: " + all, true);
		}
		//if ( cyanMetaobject instanceof IActionPackage_ati 
	}

	private static Set<String> illegalLeftCharSeq;

	static {
		illegalLeftCharSeq = new HashSet<String>();
		for ( String s : new String[] { "[!", "[@", "[&", "[=", "[:", "[|", "[*", "[+", "[?", "{*" } ) {
			illegalLeftCharSeq.add(s);
		}
	}
	private Project project;
	/**
	 * stream to where the errors should be sent
	 */
	private PrintWriter printWriter;
	/**
	 * compilerOptions[1] to compilerOptions[ compilerOptions.length - 1] contains the
	 * options passed to the Compiler in the command line. See the Cyan manual for
	 * the available options
	 */
	private HashMap<String, String> compilerOptions;

	
	/**
	 * table with all compilation units whose public prototype is an instantiated generic prototype
	 * like "Stack(Int)". That is, the public prototype is like
	 *        object Stack<Int> ... end
	 */
	//private Hashtable<String, CompilationUnit> nameRealGenProtoUnitTable;
	


	/*
	 * list of non-generic prototypes of the program
	 */
	private ArrayList<CompilationUnit> nonGenericCompilationUnitList;
	/**
	 * the program
	 */
	private Program program;
	
	HashSet<String> nameSet; 

	/**
	 * the compilation step according to the Figure of Chapter Metaobjects of the Cyan manual
	 */
	private CompilationStep compilationStep;

	public CompilationStep getCompilationStep() {
		return compilationStep;
	}

	public void setCompilationStep(CompilationStep compilationStep) {
		this.compilationStep = compilationStep;
	}

	/**
     * save to each package directory information saying the package was compiled 
     * Successfully by this compiler version
	 * 
	 */
	public void saveCompilationInfoPackages(Env env)  {
		ArrayList<CyanPackage> packageList = program.getPackageList();
		for ( CyanPackage p : packageList ) {
			String path = p.getPackageCanonicalPath();
			if ( ! path.endsWith( NameServer.fileSeparatorAsString) ) {
				path = path + NameServer.fileSeparatorAsString;
			}
			path = path + NameServer.directoryNameLinkPastFuture;

			appendToFile(path, NameServer.fileNameAfterSuccessfulCompilation, 
					"compiler saci version " + CompilerManager.CompilerVersonNumber + "\n", env, true);			
		}
	}

	/**
     * load information regarding when the package was last successfully compiled
	 * 
	 */
	public void loadCompilationInfoPackages(Env env)  {
		ArrayList<CyanPackage> packageList = program.getPackageList();
		for ( CyanPackage p : packageList ) {
			
			
			String path = p.getPackageCanonicalPath();
			if ( ! path.endsWith( NameServer.fileSeparatorAsString) ) {
				path = path + NameServer.fileSeparatorAsString;
			}
			path = path + NameServer.directoryNameLinkPastFuture;
			ArrayList<String> lineList = this.readTextLinesFromFile(path, 
					NameServer.fileNameAfterSuccessfulCompilation, env, "This file has information regarding the last successful compilation");
			int i = lineList.size() - 1;
			while ( i >= 0 ) {
				if ( lineList.get(i).trim().length() == 0 )
					--i;
			}
			if ( i >= 0 ) {
				// found a line with compilation info
				String line = lineList.get(i);
				String wordList[] = line.split(" ");
				if ( wordList.length > 0 ) {
					String last = wordList[wordList.length - 1];
					int n = -1;
					try {
					    n = Integer.parseInt(last);
					}
					catch (NumberFormatException e ) {
						env.error(null, "File '" + path + "' is damaged. The last field is not a compiler version number");
					}
					p.setCompilerVersionLastSuccessfulCompilation(n);
				}
			}
			
		}
	}
	
	
	
	/**
	 * append <code>strToAppend</code> to the end of file <code>pathDir/fileName</code>. Any errors are signaled using env.
	 * If the directory or the file do not exist, there are created. It is assumed that the file is a text file.
	 * <br> Be sure to add a {@literal \n} at the end of <code>strToAppend</code> if you want a new line after each 
	 * string written. 
	 * 
	 * If <code>appendOnlyIfDifferent</code> is true, the string <code>strToAppend</code> will only be appended if it is different from the 
	 * last line. 
	   @param pathDir
	   @param fileName
	   @param strToAppend
	   @param env
	 */
	public void appendToFile(String pathDir, String fileName, String strToAppend, Env env, boolean appendOnlyIfDifferent) {

		String path = pathDir;
		if ( ! path.endsWith( NameServer.fileSeparatorAsString) ) {
			path = path + NameServer.fileSeparatorAsString;
		}
		File f = new File(path);
		if ( !f.exists() ) {
			if ( ! f.mkdirs() ) {
				env.error(null, "Cannot create directory " + path);
				return ;
			}
		}
		path = path + NameServer.fileSeparatorAsString + fileName;
		f = new File(path);
		if ( !f.exists() ) {
			try {
				f.createNewFile();
			}
			catch (IOException e) {
				env.error(null, "Cannot create file " + path);
				return ;
			}
		}
		if ( appendOnlyIfDifferent ) {
			try ( BufferedReader br = new BufferedReader(new FileReader(path)) ) {
			    String line, lastLine = null;
			    while ( (line = br.readLine()) != null ) {
			    	lastLine = line;
			    }
			    if ( lastLine != null ) {
			    	if ( lastLine.endsWith("\n") )
			    		lastLine = lastLine.substring(0, lastLine.length()-1);
			    	String s = strToAppend;
			    	if ( s.endsWith("\n") )
			    		s = s.substring(0, s.length() - 1);
			    	if ( s.equals(lastLine) )
			    		return ;
			    }
			}
			catch (FileNotFoundException e1) {
				env.error(null, "File '" + path + "' was supposed to exist. But it does not");
				return ;
			}
			catch (IOException e1) {
				env.error(null, "Error reading file '" + path + "'");
				return ;
			}
		}
		try (   FileWriter fw = new FileWriter(path, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw) )   {
			out.print(strToAppend);
		} 
		catch (IOException e) {
			env.error(null, "Cannot write to file " + path);
			return ;
		}			
	
	}
	
	
	/**
	 * load text from file pathDir/fileName and appends the contents in <code>text</code>. Returns false in error.
	 * If there is any error, an error message is issued through <code>env</code> with message <code>whyMessage</code> appended.  
	 */
	public boolean readTextFromFile(String pathDir, String fileName, StringBuffer text, Env env, String whyMessage) {

		String path = pathDir;
		if ( ! path.endsWith( NameServer.fileSeparatorAsString) ) {
			path = path + NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.fileSeparatorAsString + fileName;
		MyFile myFile = new MyFile(path);
		char []charArray = myFile.readFile();
		if ( charArray != null ) {
			text.append(charArray);
			return true;
		}
		else {
			env.error(null,  "Cannot load file '" + path + "'. " + whyMessage);
			return false;
		}
	}
	

	/**
	 * load text from file pathDir/fileName and return the contents as an array of strings, one for each line.
	 * If there is any error, null is returned and an error message is issued through <code>env</code> 
	 * with message <code>whyMessage</code> appended.  
	 */
	public ArrayList<String> readTextLinesFromFile(String pathDir, String fileName, Env env, String whyMessage) {

		String path = pathDir;
		if ( ! path.endsWith( NameServer.fileSeparatorAsString) ) {
			path = path + NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.fileSeparatorAsString + fileName;
		MyFile myFile = new MyFile(path);
		ArrayList<String> lineList = myFile.readLinesFile();
		if ( lineList == null ) {
			env.error(null,  "Cannot load file '" + path + "'. " + whyMessage);
		}
		return lineList;
	}
	

	/**
	 * return a tuple consisting of: a) a file name b) a directory and c) a package. 
	 * 
	 * 
	 * Parameter fileName contains a file name possibly with a directory such as <code>python/script0.py</code>.
	 * There should be a file <code>python/script0.py</code> in the hidden directory hiddenDirectory of package packageName.
	 * 
	 * The file name returned does not contain the directory name. The directory returned is the full name of 
	 * the directory.
	 * 
	 * Return null if the file or directory does not exist
	   @param fileName
	 * @param packageName
	 * @param hiddenDirectory 
	   @return
	 */

	public Tuple3<String, String, CyanPackage> getAbsolutePathHiddenDirectoryFile(
			  String fileName, String packageName, DirectoryPackage hiddenDirectory) {

		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return null;
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + hiddenDirectory.toString() + NameServer.fileSeparatorAsString;
		
		
		int indexdot = fileName.lastIndexOf(NameServer.fileSeparatorAsString);
		if ( indexdot < 0 ) {
			// no '.' in the file name, use package of the metaobject
			return new Tuple3<String, String, CyanPackage>(fileName, path, cyanPackage);
		}
		else {
			String fn = fileName.substring(indexdot + 1);
			if ( indexdot + 1 > fileName.length() ) {
				return null;
			}
			String dir = fn.substring(0, indexdot);
			if ( ! dir.endsWith(NameServer.fileSeparatorAsString) ) {
				dir += NameServer.fileSeparatorAsString;
			}
			return new Tuple3<String, String, CyanPackage>(fn, path + dir, cyanPackage);
		}

	}
	

	
	/**
	   @param fileName
	   @param prototypeFileName
	   @param packageName
	   @param hiddenDirectory
	   @return  // C:\Dropbox\Cyan\cyanTests\simple\\main\Program\--tmpkeepValue_n\keepValue_n
	 */
	public String getPathFileHiddenDirectory(String prototypeFileName, String packageName,
			DirectoryPackage hiddenDirectory) {
		String path = this.project.getProjectDir();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		switch ( hiddenDirectory.getWhere() ) {
		case PROJECT:
			path = path + hiddenDirectory.toString() + NameServer.fileSeparatorAsString +  
			       packageName.replace(NameServer.fileSeparator,  '.')  + NameServer.fileSeparatorAsString +  
				   prototypeFileName + NameServer.fileSeparatorAsString;
			break;
		case PACKAGE:
			path = path + packageName.replace(NameServer.fileSeparator,  '.')  + NameServer.fileSeparatorAsString +  
			       hiddenDirectory.toString() + NameServer.fileSeparatorAsString;
			break;
		case PROTOTYPE:
			path = path +  packageName.replace(NameServer.fileSeparator,  '.')  + NameServer.fileSeparatorAsString +  
				prototypeFileName + NameServer.fileSeparatorAsString + hiddenDirectory.toString() + 
				NameServer.fileSeparatorAsString;
			break;
		default:
			break;
		
		}
		return path;
	}
		
	
	private static Tuple2<ArrayList<String>, String> getFormalParamList(String filename, int start) {
		 
		
		ArrayList<String> formalParamList = new ArrayList<>();
		String paramListStr = filename.substring(start);
		int indexRightPar = paramListStr.indexOf(')');
		if ( indexRightPar < 0 ) {
			return new Tuple2<ArrayList<String>, String>(null, "parameter list of file name is not well formed");
		}
		paramListStr = paramListStr.substring(0, indexRightPar);
		if ( paramListStr.indexOf(' ') >= 0 ) {
			return new Tuple2<ArrayList<String>, String>(null, "Parameter list cannot contains space");
		}
		// now paramListStr is a list like   "T,R,S"
		int i = 0;
		while ( i < paramListStr.length() ) {
			String s = "";
			while ( i < paramListStr.length() && Character.isAlphabetic(paramListStr.charAt(i)) ) {
				s += paramListStr.charAt(i);
				++i;
			}
			if ( s.length() == 0 ) {
				return new Tuple2<ArrayList<String>, String>(null, "parameter list of file name is not well formed");
			}
			formalParamList.add(s);
			if ( i >= paramListStr.length() )
				return new Tuple2<ArrayList<String>, String>(formalParamList, null);
			if ( paramListStr.charAt(i) != ',' )
				return new Tuple2<ArrayList<String>, String>(null, "',' expected in parameter list of file name");;
			++i;
			if ( i + 1 >= paramListStr.length() || ! Character.isAlphabetic(paramListStr.charAt(i)) ) {
				return new Tuple2<ArrayList<String>, String>(null, "parameter list of file name is not well formed");
			}
		}

		
		return new Tuple2<ArrayList<String>, String>(null, "parameter list of file name is not well formed");
	}


	
	/**
	 * load a text file from the hidden directory given by the enumerate value <code>hiddenDirectory</code>
	 * of package <code>packageName</code>. If numParameters is 0, the file read is fileName.extension. 
	 * If numParameters is, for example, 2, the file read may be something as <code>fileName(A,B).extension</code>. 
	 * realParamList is a list of strings that should replace, in the file read, its parameters. That is,
	 * if realParamList is, in Cyan syntax, <br>
	 * <code>
	 *     [ "Company", "Client" ]<br>
	 * </code><br>
	 * and the complete file name is <code>Relation(A,B).rel</code>, this method searches for words <code>A</code>
	 * and <code>B</code> in the text read from <code>Relation(A,B).rel</code> and replaces them for <code>Company</code>
	 * and <code>Client</code>. 
	 *  
	 * This method returns a 5-tuple. The first element is an error messages. null if none. The second is
	 * the text read (with the replacements if realParamList is not null). The third tuple element is the complete file name, with 
	 * the parameters. The fourth is the
	 * path of the directory and the fifth is the Cyan Package in which the file is.
	 * 
	 */
	
	public Tuple5<FileError, char[], String, String, CyanPackage> readTextFileFromPackage(
			String fileName,
			String extension,
			String packageName, 
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		
		
		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return new Tuple5<FileError, char[], String, String, CyanPackage>(FileError.package_not_found, 
					null, null, null, null);
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + hiddenDirectory.toString() + NameServer.fileSeparatorAsString;
		String filenameWithParameters;
		/**
		 * look for a file name with the appropriate number of parameters
		 */
		File dslDir = new File(path);
		if ( dslDir.list() == null ) {
			/*
			 * "File '" + fileName + (numParameters == 0 ? "" : "(...)") + 
					fileNameExtension + " of package " + packageName + "' was not found"
			 */
			return new Tuple5<FileError, char[], String, String, CyanPackage>(FileError.file_not_found,
					null, null, null, null);
		}
		String []formalParamList;
		Tuple2<String, String[]> fnWithParam = searchFileWithParameters(fileName, extension, dslDir, numParameters); 
		if ( fnWithParam == null ) {
			/*
			 * "File '" + fileName + (numParameters == 0 ? "" : "(...)") + 
					fileNameExtension + " of package " + packageName + "' was not found"
			 */
			return new Tuple5<FileError, char[], String, String, CyanPackage>( FileError.file_not_found, 
					null, null, null, null);
		}
		filenameWithParameters = fnWithParam.f1;
		formalParamList = fnWithParam.f2;

		String packagePath = path;
		path = path + filenameWithParameters;
		MyFile f = new MyFile(path); 
		char []charArray = f.readFile(false, false);
		if ( f.getError() != MyFile.ok_e ) {
			/*
			 * "Error in reading file '" + fileName + (numParameters == 0 ? "" : "(...)") + 
					fileNameExtension + " of package " + packageName + "'"
			 */
			return new Tuple5<FileError, char[], String, String, CyanPackage>(FileError.read_error_e, 
					null, null, null, null);
		}
		else {
			// int indexLeftPar = filenameWithParameters.indexOf('(');
			if ( realParamList != null && realParamList.size() > 0) {
				if ( numParameters == 0 ) {
					/*
					 * "Internal error in metaobject: file '" + fileName + (numParameters == 0 ? "" : "(...)") + 
							fileNameExtension + " of package " + packageName + 
							"' take parameters. It was expected that it does not take any"
					 */
					return new Tuple5<FileError, char[], String, String, CyanPackage>(
							FileError.file_should_not_take_parameters, null, null, null, null);
				}
			}
			else if ( numParameters > 0 ) {
				/*
				 * "Internal error in metaobject: file '" + fileName + (numParameters == 0 ? "" : "(...)") + 
						fileNameExtension + " of package " + packageName + 
						"' take parameters. It was expected that it does not take any",
				 */
				return new Tuple5<FileError, char[], String, String, CyanPackage>(
						FileError.file_should_take_parameters, null, null, null, null);
			}

			if ( numParameters > 0 && realParamList != null ) {
				/*
				 * replace the formal parameters by the real parameters in the text file
				 * /
				Tuple2<ArrayList<String>, String> messageFormalParamList = getFormalParamList(filenameWithParameters, indexLeftPar + 1); 
				if ( messageFormalParamList.f2 != null ) {
					/*
					 * "File '" + filenameWithParameters + "' of package " + packageName + 
							"' do not have the correct file name format. Use something like 'comparison(T).concept' or 'comparable(R,S).concept'",
					 * /
					return new Tuple5<FileError, char[], String, String, CyanPackage>(
							FileError.file_name_does_not_have_the_correct_name_format,
							null, null, null, null);
				}
				formalParamList = messageFormalParamList.f1;
				*/
				Hashtable<String, String> formalRealTable = new Hashtable<>();
				int k = 0;
				for ( String formalParam : formalParamList ) {
					if ( formalRealTable.put(formalParam, realParamList.get(k)) != null ) {
						/*
						 * "Error in file '" + fileName + NameServer.fileSeparatorAsString + packageName + 
								"'. Its parameters list has two parameters with the same name"
						 */
						return new Tuple5<FileError, char[], String, String, CyanPackage>(
								FileError.two_parameters_are_the_same, null, null, null, null);
					}
					++k;
				}
				charArray = CompilerManager.replaceOnly(charArray, formalRealTable, "", ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE);
			}
					
			return new Tuple5<FileError, char[], String, String, CyanPackage>(FileError.ok_e, charArray, 
					filenameWithParameters, packagePath, cyanPackage);
		}
	}
	
	/**
	 * load a text file from the hidden directory given by the enumerate value <code>hiddenDirectory</code>
	 * of the project.  If numParameters is 0, the file read is fileName.extension. 
	 * If numParameters is, for example, 2, the file read may be something as <code>fileName(A,B).extension</code>. 
	 * realParamList is a list of strings that should replace, in the file read, its parameters. That is,
	 * if realParamList is, in Cyan syntax, <br>
	 * <code>
	 *     [ "Company", "Client" ]<br>
	 * </code><br>
	 * and the complete file name is <code>Relation(A,B).rel</code>, this method searches for words <code>A</code>
	 * and <code>B</code> in the text read from <code>Relation(A,B).rel</code> and replaces them for <code>Company</code>
	 * and <code>Client</code>. 
	 *  
	 * This method returns a 4-tuple. The first element is an error messages. null if none. The second is
	 * the text read (with the replacements if realParamList is not null). The third tuple element is the complete file name, with 
	 * the parameters. The fourth is the
	 * path of the directory in which the file is.
	 * 
	 */

	public Tuple4<FileError, char[], String, String> readTextFileFromProject(
			String fileName,
			String extension,
			DirectoryPackage hiddenDirectory, 
			int numParameters, 
			ArrayList<String> realParamList) {
		
		/***
		 * WARNING: HIGHLY duplicated code
		 */

		String path = this.project.getProjectCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + hiddenDirectory.toString() + NameServer.fileSeparatorAsString;
		String filenameWithParameters;
		/**
		 * look for a file name with the appropriate number of parameters
		 */
		File dslDir = new File(path);
		if ( dslDir.list() == null ) {
			/*
			 * "File '" + fileName + (numParameters == 0 ? "" : "(...)") + 
					fileNameExtension + " of package " + packageName + "' was not found"
			 */
			return new Tuple4<FileError, char[], String, String>(FileError.file_not_found,
					null, null, null);
		}
		String []formalParamList;
		Tuple2<String, String[]> fnWithParam = searchFileWithParameters(fileName, extension, dslDir, numParameters); 
		if ( fnWithParam == null ) {
			/*
			 * "File '" + fileName + (numParameters == 0 ? "" : "(...)") + 
					fileNameExtension + " of package " + packageName + "' was not found"
			 */
			return new Tuple4<FileError, char[], String, String>( FileError.file_not_found, 
					null, null, null);
		}
		filenameWithParameters = fnWithParam.f1;
		formalParamList = fnWithParam.f2;

		String packagePath = path;
		path = path + filenameWithParameters;
		MyFile f = new MyFile(path); 
		char []charArray = f.readFile(false, false);
		if ( f.getError() != MyFile.ok_e ) {
			/*
			 * "Error in reading file '" + fileName + (numParameters == 0 ? "" : "(...)") + 
					fileNameExtension + " of package " + packageName + "'"
			 */
			return new Tuple4<FileError, char[], String, String>(FileError.read_error_e, 
					null, null, null);
		}
		else {
			// int indexLeftPar = filenameWithParameters.indexOf('(');
			if ( realParamList != null && realParamList.size() > 0) {
				if ( numParameters == 0 ) {
					/*
					 * "Internal error in metaobject: file '" + fileName + (numParameters == 0 ? "" : "(...)") + 
							fileNameExtension + " of package " + packageName + 
							"' take parameters. It was expected that it does not take any"
					 */
					return new Tuple4<FileError, char[], String, String>(
							FileError.file_should_not_take_parameters, null, null, null);
				}
			}
			else if ( numParameters > 0 ) {
				return new Tuple4<FileError, char[], String, String>(
						FileError.file_should_take_parameters, null, null, null);
			}

			if ( numParameters > 0 && realParamList != null ) {
				/*
				 * replace the formal parameters by the real parameters in the text file
				 * /
				*/
				Hashtable<String, String> formalRealTable = new Hashtable<>();
				int k = 0;
				for ( String formalParam : formalParamList ) {
					if ( formalRealTable.put(formalParam, realParamList.get(k)) != null ) {
						return new Tuple4<FileError, char[], String, String>(
								FileError.two_parameters_are_the_same, null, null, null);
					}
					++k;
				}
				charArray = CompilerManager.replaceOnly(charArray, formalRealTable, "", ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE);
			}
					
			return new Tuple4<FileError, char[], String, String>(FileError.ok_e, charArray, 
					filenameWithParameters, packagePath);
		}
	}
	

	/**
	 * read a binary file from the hidden directory given by the enumerate value <code>hiddenDirectory</code>
	 * of package <code>packageName</code>. The file read is fileName.extension.
	 *  
	 * This method returns a 5-tuple. The first element is an error messages. null if none. The second is
	 * the data read. The third tuple element is the complete file name. The fourth is the
	 * path of the directory and the fifth is the Cyan Package in which the file is.
	 * 
	 */
	
	public Tuple5<FileError, byte[], String, String, CyanPackage> readBinaryFileFromPackage(
			String fileName,
			String extension,
			String packageName, 
			DirectoryPackage hiddenDirectory 
			) {
		
		
		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return new Tuple5<FileError, byte[], String, String, CyanPackage>(FileError.package_not_found, 
					null, null, null, null);
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + hiddenDirectory.toString() + NameServer.fileSeparatorAsString;
		String filenameExtension = fileName + "." + extension;
		
		String packagePath = path;
		path = path + filenameExtension;
		
		Path aPath = Paths.get(path);
		byte[] byteArray = null;
	    try {
			 byteArray = Files.readAllBytes(aPath);
		}
		catch (IOException e) {
			return new Tuple5<FileError, byte[], String, String, CyanPackage>(FileError.cannot_be_read_e, 
					null, null, null, null);
		}		
		
		
		return new Tuple5<FileError, byte[], String, String, CyanPackage>(FileError.ok_e, byteArray, 
					filenameExtension, packagePath, cyanPackage);
	}
	

	/**
	 * read a binary file from the hidden directory given by the enumerate value <code>hiddenDirectory</code>
	 * of the project. The file read is fileName.extension.
	 *  
	 * This method returns a 4-tuple. The first element is an error messages. null if none. The second is
	 * the data read. The third tuple element is the complete file name. The fourth is the
	 * path of the directory in which the file is.
	 * 
	 */
	
	public Tuple4<FileError, byte[], String, String> readBinaryFileFromProject(
			String fileName,
			String extension,
			DirectoryPackage hiddenDirectory 
			) {
		

		/***
		 * WARNING: HIGHLY duplicated code
		 */

		String path = this.project.getProjectCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + hiddenDirectory.toString() + NameServer.fileSeparatorAsString;
		
		String filenameExtension = fileName + "." + extension;
		
		String packagePath = path;
		path = path + filenameExtension;
		
		Path aPath = Paths.get(path);
		byte[] byteArray = null;
	    try {
			 byteArray = Files.readAllBytes(aPath);
		}
		catch (IOException e) {
			return new Tuple4<FileError, byte[], String, String>(FileError.cannot_be_read_e, 
					null, null, null);
		}		
		
		
		return new Tuple4<FileError, byte[], String, String>(FileError.ok_e, byteArray, 
					filenameExtension, packagePath);
	}
	
	
	
	
	

	/**
	   @param numParameters
	   @param filenameWithParameters
	   @param dslDir
	   @return
	 */
	public Tuple2<String, String[]> searchFileWithParameters(String fileName, String extension, File dslDir, int numParameters) {
		if ( numParameters == 0 ) {
			fileName += extension;
			for ( String fname : dslDir.list() ) {
				if ( fname.equals(fileName) ) 
					return new Tuple2<String, String[]>(fname, null);
			}
			return null;
		}
		else {
			for ( String fname : dslDir.list() ) {

				int indexLeftPar = fname.indexOf('(');
				String s;
				if ( indexLeftPar > 0 ) {
					String name = fname.substring(0, indexLeftPar);
					if (  name.equals(fileName) ) {
						s = fname.substring(indexLeftPar + 1);
						int indexRightPar = s.indexOf(')');
						if ( indexRightPar > 0 ) {
							String ext = "";
							if ( indexRightPar < s.length() - 1  )
								ext = s.substring(indexRightPar + 2);
							s = s.substring(0, indexRightPar);
							if ( s.length() > 0 ) {
								String paramList[] = s.split(",");
								if ( paramList.length == numParameters && ext.equals(extension) ) {
									return new Tuple2<String, String[]>(fname, paramList);
								}
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * load text file <code>fileName</code> from the data directory of package <code>packageName</code>. Return a tuple
	 * with an error code and the read char array.
	 * 
	   @param fileName
	   @param packageName
	   @return
	 */
	
	public Tuple2<FileError, char[]> readTextDataFileFromPackage(String fileName, String packageName) {
		
		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return new Tuple2<FileError, char[]>(FileError.package_not_found, null);
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.directoryNamePackageData + NameServer.fileSeparatorAsString + fileName;
		MyFile f = new MyFile(path); 
		char []charArray = f.readFile(false, false);
		if ( f.getError() != MyFile.ok_e ) {
			return new Tuple2<FileError, char[]>(FileError.cannot_be_read_e, null);
		}
		else
			return new Tuple2<FileError, char[]>(FileError.ok_e, charArray);
	}
	

	/**
	 * load text file <code>fileName</code> from the data directory of package <code>packageName</code>.
	 * Issue <code>errorMessage</code> in error with symbol <code>sym</code>
	   @param fileName
	   @param packageName
	   @return
	 */
	
	public Tuple2<FileError, byte[]> readBinaryDataFileFromPackage(String fileName, String packageName) {
		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return new Tuple2<FileError, byte[]>(FileError.package_not_found, null);
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.directoryNamePackageData + NameServer.fileSeparatorAsString + fileName;
		
		
		Path aPath = Paths.get(path);
		byte[] byteArray = null;
	      try {
			 byteArray = Files.readAllBytes(aPath);
		}
		catch (IOException e) {
			return new Tuple2<FileError, byte[]>(FileError.cannot_be_read_e, null);
		}		
		return new Tuple2<FileError, byte[]>(FileError.ok_e, byteArray);
	}
	
	
	public FileError saveBinaryDataFileToPackage(byte[] data, String fileName, String packageName) {
		
		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return FileError.package_not_found;
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.directoryNamePackageData + NameServer.fileSeparatorAsString + fileName;
		
		Path aPath = Paths.get(path);
        try {
        	Files.write(aPath, data);
		}
		catch (IOException e) {
			return FileError.write_error_e;
		}
	    return FileError.ok_e;
	}

	/**
	 * delete all files of the directory dirName of the test directory of package packageName 
	 */
	public boolean deleteDirOfTestDir(String dirName, String packageName) {

		String path = this.project.getProjectDir();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.directoryNamePackageTests + NameServer.fileSeparatorAsString +
				packageName.replace(NameServer.fileSeparator,  '.') + NameServer.fileSeparatorAsString + 
				dirName;
		File fpath = new File(path);	
		return MyFile.deleteFileDirectory(fpath);
	}
	
	/**
	 * write 'data' to file 'fileName' that is created in the test directory of package packageName.
	 * Return an object of FileError indicating any errors.
	 */
	public FileError  writeTestFileTo(StringBuffer data, String fileName, String dirName, String packageName) {

		String path = this.project.getProjectDir();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.directoryNamePackageTests + NameServer.fileSeparatorAsString +
				packageName.replace(NameServer.fileSeparator,  '.')  + NameServer.fileSeparatorAsString +  
				dirName + NameServer.fileSeparatorAsString;
		File fpath = new File(path);
		fpath.mkdirs();
		
		path += fileName;
		
		
        try {
    		Path aPath = Paths.get(path);
        	Files.write(aPath, data.toString().getBytes());
		}
		catch (IOException e) {
			return FileError.write_error_e;
		}
	    return FileError.ok_e;
			
	}
	

	
	/**
	 * write 'str' to file 'fileName' of the directory 'hiddenDirectory' of the prototype of package 
	 * 'packageName' that is in file 'prototypeFileName'
	   @param charArray
	   @param fileName
	   @param prototypeFileName
	   @param packageName
	   @param hiddenDirectory
	   @return
	 */
	public FileError writeTextFile(
			String str,
			String fileName,
			String prototypeFileName,
			String packageName, 
			DirectoryPackage hiddenDirectory) {

		if ( hiddenDirectory.isReadOnly() ) {
			return FileError.write_error_e;
		}

		String path = getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		File fpath = new File(path);
		fpath.mkdirs();
		
		path += NameServer.fileSeparatorAsString + fileName;
		
		
        try (FileWriter f = new FileWriter(path) ) {
        	f.write(str);
		}
		catch (IOException e) {
			return FileError.write_error_e;
		}
	    return FileError.ok_e;
	}

	
	
	/**
	 * write 'charArray' to file 'fileName' of the directory 'hiddenDirectory' of the prototype of package 
	 * 'packageName' that is in file 'prototypeFileName'
	   @param charArray
	   @param fileName
	   @param prototypeFileName
	   @param packageName
	   @param hiddenDirectory
	   @return
	 */
	public FileError writeTextFile(
			char []charArray,
			String fileName,
			String prototypeFileName,
			String packageName, 
			DirectoryPackage hiddenDirectory) {

		if ( hiddenDirectory.isReadOnly() ) {
			return FileError.write_error_e;
		}

		String path = getPathFileHiddenDirectory(prototypeFileName, packageName, hiddenDirectory);
		File fpath = new File(path);
		fpath.mkdirs();
		
		path += NameServer.fileSeparatorAsString + fileName;
		
		
        try (FileWriter f = new FileWriter(path) ) {
        	f.write(charArray);
		}
		catch (IOException e) {
			return FileError.write_error_e;
		}
	    return FileError.ok_e;
	}


	/**
	 * return the absolute path in which the file <code>fileName</code> would be stored in the data directory of package <code>packageName</code>.
	 * If the package does not exist or the fileName is invalid, returns null
	   @param fileName
	   @param packageName
	   @return
	 */
	public String pathDataFilePackage(String fileName, String packageName) {
		CyanPackage cyanPackage = this.project.searchPackage(packageName);
		if ( cyanPackage == null ) {
			return null;
		}
		String path = cyanPackage.getPackageCanonicalPath();
		if ( ! path.endsWith(NameServer.fileSeparatorAsString) ) {
			path += NameServer.fileSeparatorAsString;
		}
		path = path + NameServer.directoryNamePackageData + NameServer.fileSeparatorAsString + fileName;
		return path;
	}
	
	
	
	/** replace all occurrences of keys of  formalRealTable by values of this table. Return the modified char array */
	
	public static char []replaceOnly(char []text,  Hashtable<String, String> formalRealTable, String currentPrototypeName,
			ReplacementPolicyInGenericInstantiation replacementPolicy) {
		final int StepSize = 200;
		int sizeText = text.length;
		int sizeNewText = sizeText;
		char []newText = new char[sizeNewText + 1];
		int indexText = 0;
		int indexNewText = 0;
		while ( indexText < sizeText ) {
			// look for an identifier
			if ( text[indexText] == '_' || Character.isLetter(text[indexText]) ) {
				String id = "";
				while ( indexText < sizeText && text[indexText] == '_' || Character.isLetterOrDigit(text[indexText]) ) {
					id = id + text[indexText];
					if ( indexNewText >= sizeNewText - 1 ) {
						char []newNewText = new char[sizeNewText + StepSize];
						CyanMetaobjectWithAtAnnotation.copyCharArray(newNewText, newText);
						sizeNewText = sizeNewText + StepSize;
						newText = newNewText;
					}
					newText[indexNewText++] = text[indexText];
					++indexText;
				}
				/* found an identifier (a sequence of letters, digits and _ ) */
				String value = formalRealTable.get(id); 
				if ( value == null && id.equals("Java_Current__Prototype___Name")) {
					value = currentPrototypeName;
				}
				
				
				if ( value != null ) {
					
					switch ( replacementPolicy ) {
					case NO_REPLACEMENT:
						// this should not occur because this method should not have been
						// called with replacementPolicy equal to NO_REPLACEMENT
						value = id;
						break;
					case REPLACE_BY_CYAN_VALUE:
						   // value is already the Cyan value
						break;
					case REPLACE_BY_JAVA_VALUE:
						value = NameServer.getJavaName(value);
					}
					
					indexNewText = indexNewText - id.length();
					for (int i = 0; i < value.length(); ++i) {
						if ( indexNewText >= sizeNewText - 1) {
							char []newNewText = new char[sizeNewText + StepSize];
							CyanMetaobjectWithAtAnnotation.copyCharArray(newNewText, newText);
							sizeNewText = sizeNewText + StepSize;
							newText = newNewText;
						}
						newText[indexNewText++] = value.charAt(i);						
					}
				}
			}
			else {
				if ( indexNewText >= sizeNewText - 1 ) {
					char []newNewText = new char[sizeNewText + StepSize];
					CyanMetaobjectWithAtAnnotation.copyCharArray(newNewText, newText);
					sizeNewText = sizeNewText + StepSize;
					newText = newNewText;
				}
				newText[indexNewText++] = text[indexText];
				++indexText;
			}
	
		}
		newText[indexNewText] = '\0';
		return newText;
	}

	public static Tuple2<String, String> separatePackagePrototype(String fullName) {
		String packageName = "";
		String prototypeName = "";
		String s = fullName;
		int indexLessThan = s.indexOf('<');
		if ( indexLessThan >= 0 ) { 
			  // eliminates the parameters to the generic prototype
			  // "Tuple<main.Person, Int>" becomes "Tuple" and
			  // "cyan.lang.tmp.Tuple<main.Person, Int>" becomes "cyan.lang.tmp.Tuple"
			prototypeName = s.substring(indexLessThan);
			s = s.substring(0, indexLessThan);
		}
		int i = s.lastIndexOf('.');
		if ( i >= 0 ) {
			// there is a package
			packageName = s.substring(0, i);
			prototypeName = s.substring(i+1) + prototypeName;
		}
		else {
			prototypeName = s + prototypeName;
		}
		
		
		return new Tuple2<String, String>(packageName, prototypeName);
	}
	

	

	public class VersionFeatures {
		public VersionFeatures(int version, String []featureList) { this.version = version; this.featureList = featureList; }
		public int version;
		public String []featureList;
	}

	
	
	
	public final static String CompilerVersionName = "photon 0.0001";
	  // it is in fact CompilerVersonNumber/10.000
	public final static int CompilerVersonNumber = 1; 
	/**
	 * for each version number, there is a list of features that it has or that were added 
	 * since the last version 
	 */
	public VersionFeatures[] versionFeaturesList = { 
			new VersionFeatures(1, new String [] { "fun", "multimethod" } ) 
	} ;

}
