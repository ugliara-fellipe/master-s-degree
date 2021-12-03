package ast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import error.ErrorKind;
import error.UnitError;
import lexer.Symbol;
import lexer.Token;
import meta.CompilerManager_ati;
import meta.IAction_cge;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import saci.CompilerOptions;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Project;
import saci.Tuple3;


/**
 * This class represents a source file in Cyan, a unit of compilation.
 * Each source file corresponds to at least an object or interface. It may be
 * more than one because there may be one public unit and several private ones.
 */
public class CompilationUnit extends CompilationUnitSuper implements ASTNode, Cloneable {


	public CompilationUnit(String filename, String packageCanonicalPath,
			               CompilerOptions compilerOptions,
			               CyanPackage cyanPackage) {
		
		super(filename, packageCanonicalPath);
		
		this.filename = filename;
		this.packageCanonicalPath = packageCanonicalPath;
		this.compilerOptions = compilerOptions;
		this.packageSymbol = null;
		/*
		errorList = new ArrayList<UnitError>();
		actionList = new ArrayList<Action>(); */
		this.cyanPackage = cyanPackage;
		
		// this.fullFileNamePath = this.packageCanonicalPath + this.filename;
		programUnitList = new ArrayList<ProgramUnit>();

		importPackageList = null;
		importedPackageNameList = null;
		conflictProgramUnitTable = null;
		importedCyanPackageTable = null;
		
		hasGenericPrototype = false;
		publicPrototype = null;
		isPrototypeInterface = false;
		alreadyCalcInterfaceTypes = false;
		alreadyCalcInternalTypes = false;
		interfaceCompilationUnit = null;
		lineMessageList = new ArrayList<>();
		isInterfaceAsObject = false;
		nonAttachedMetaobjectAnnotationListBeforePackage = null;
		prototypeIsNotGeneric = true;
	}
	
	@Override
	public CompilationUnit clone() {
		CompilationUnit clone = (CompilationUnit ) super.clone();
		return clone;
	}
		

	@Override
	public void accept(ASTVisitor visitor) {
		
		visitor.preVisit(this);
		
		for ( ProgramUnit pu : this.programUnitList ) {
			pu.accept(visitor);
		}
		visitor.visit(this);
	}
	

	@Override
	public void reset() {
		super.reset();
		programUnitList.clear();

		importPackageList = null;
		importedPackageNameList = null;
		conflictProgramUnitTable = null;
		importedCyanPackageTable = null;
		
		hasGenericPrototype = false;
		publicPrototype = null;
		isPrototypeInterface = false;
		alreadyCalcInterfaceTypes = false;
		alreadyCalcInternalTypes = false;
		interfaceCompilationUnit = null;
		lineMessageList.clear();		
	}

	public void setImportPackageList(ArrayList<ExprIdentStar> importPackageList) {
		this.importPackageList = importPackageList;
		this.importedPackageNameList = new HashSet<String>();
		for ( ExprIdentStar e : importPackageList ) 
			importedPackageNameList.add(e.getName());
		
		// no conflicts yet
		conflictProgramUnitTable = new HashMap<>();
	}

	public ArrayList<ExprIdentStar> getImportPackageList() {
		return importPackageList;
	}


	@Override
	public String getEntityName() {
		if ( publicObjectInterfaceName == null ) {
			String s = this.getFileNameWithoutExtension();

			int lastIndexBar = s.lastIndexOf(NameServer.fileSeparatorAsString);
			if ( lastIndexBar < 0 )
				lastIndexBar = -1;
			s = s.substring(lastIndexBar+1);
			

			String packageName = this.getPackageName();
			if ( packageName.length() > 0 ) 
				packageName += ".";
			publicObjectInterfaceName = packageName + NameServer.fileNameToPrototypeName(s);
		}
		return publicObjectInterfaceName;
	}


	public void setCompilerOptions(CompilerOptions compilerOptions) {
		this.compilerOptions = compilerOptions;
	}

	public CompilerOptions getCompilerOptions() {
		return compilerOptions;
	}

	public void setCyanPackage(CyanPackage cyanPackage) {
		this.cyanPackage = cyanPackage;
	}

	public CyanPackage getCyanPackage() {
		return cyanPackage;
	}

	/**
	 * if cyanEnv.getCreatingInstanceGenericPrototype(), this is the instantiation of a generic prototype such as Stack<Int> from Stack<:T>
	 * In this case, the compilation unit is put in a package ended with ".tmp" such as "cyan.lang.tmp" 
	 * @param pw
	 * @param cyanEnv
	 */

	public void genCyan(PWInterface pw, CyanEnv cyanEnv) {
		
		if ( nonAttachedMetaobjectAnnotationListBeforePackage != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationListBeforePackage ) {
				annotation.genCyan(pw, false, cyanEnv, true);
			}
		}
		pw.print("package ");
		pw.print(this.getPackageIdent().getName());


		pw.println("");
		
				
		
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			/** This is an instantiation of a generic prototype. That is, a real prototype is being created
			     from a generic prototype by replacing its formal parameters by real parameters. If a real
			     parameter is a prototype, its package should be imported inside the instantiation. 
			     For example, suppose prototype A in a source file A.cyan uses "Stack<Tuple<Bank, RegExpr>>".
			     This source file imports Bank from package "bank" and RegExpr from package "util".
			     The creation of a real prototype for "Stack<Tuple<Bank, RegExpr>>" is made in this
			     genCyan method of class CompilationUnit of the Cyan Compiler. This real prototype is
			     put in a source file "Stack(Tuple(Bank,RegExpr)).cyan". This file imports packages
			     "bank" and "util" because "Bank" and "RegExpr" are used in the instantiation. Tuple
			     is always imported by any Cyan source file.
			     
			
			for ( String usedGenericPrototype : cyanEnv.getUsedGenericPrototypeSet() ) {
				CyanPackage aPackage = getCyanPackage().getProject().searchPackageOfCompilationUnit(usedGenericPrototype);
				 
				 // if aPackage == null, it should be a non-type such as "f1" in the generic prototype instantiation "Tuple<f1, Int>"
				 
				if ( aPackage != null ) { 
					String packageName = aPackage.getPackageName();
					if ( ! packageName.equals(NameServer.cyanLanguagePackageName) && ! packageName.equals(this.getPackageIdent().getName()) )
					    importPackageNameList.add(aPackage.getPackageName());
					
				} 
				
			} */
		
			/*
			// genCyan is being called to create an instance of a generic prototype 
			// currently only one program unit can be generic in a compilation unit.
			// So the next "for" is unnecessary
			for ( ProgramUnit pu : programUnitList )
				if ( pu.isGeneric() ) {
					   // getGenericParameterListList() returns something like <Int, Int><Boolean>
					for ( ArrayList<GenericParameter> genParamList : pu.getGenericParameterListList() ) 
						   // genParamList is just one list of parameters like <Int, Int>
						for ( GenericParameter genParam : genParamList )  {
							// genParam is just like Int in <Int, Int>
							
							// all packages of all real parameters should be imported. That is,
							// if this compilation unit defines "object Stack<Program> ... end" then 
							// it should import package "main" in which "Program" is declared 
							 
							//  realPrototypeName is something like  "Stack(1)" 
							 
							String realPrototypeName; // = cyanEnv.getFormalRealLimitedTable().get( genParam.getName() );
							realPrototypeName = NameServer.prototypeNameToFileName(cyanEnv.formalGenericParamToRealParam(genParam.getName()));
							
							 // if the real parameter starts with an upper case letter, it should be a real prototype. 
							 
							if ( Character.isUpperCase(realPrototypeName.charAt(0)) ) {
								   // get 
								CyanPackage aPackage = getCyanPackage().getProject().searchPackageOfCompilationUnit(realPrototypeName);
								if ( aPackage == null ) {
									String s = NameServer.prototypeNameToFileName(cyanEnv.getFormalParamToRealParamFileNameTable().get(genParam.getName()));
									aPackage = getCyanPackage().getProject().searchPackageOfCompilationUnit(s);
								}
								 
								 // if aPackage == null, it should be a non-type such as "f1" in the generic prototype instantiation "Tuple<f1, Int>"
								
								if ( aPackage == null ) {
									pu.getCompilationUnit().error(null,  "Prototype '" + realPrototypeName + "' was not found");
								}
								else {
									String packageName1 = aPackage.getPackageName();
									if ( ! packageName1.equals(NameServer.cyanLanguagePackageName) && ! packageName1.equals(this.getPackageIdent().getName()) )
									    importPackageNameList.add(aPackage.getPackageName());
								}
								
							}
						}
				}
			*/
			
		}
		//String currentPackageNameWithTMP = packageIdent.getName() + NameServer.dotTemporaryDirName;

		for ( String packageName : importedPackageNameList ) {
				pw.println("import " + packageName);
		}
		pw.println("");

		for ( ProgramUnit programUnit : programUnitList )
			programUnit.genCyan(pw, cyanEnv, true);
	}
	

	public void genJava(PWInterface pw, Env env) {
		
		env.setCurrentCompilationUnit(this);
		
		if ( nonAttachedMetaobjectAnnotationListBeforePackage != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationListBeforePackage ) {
				if ( annotation.getCyanMetaobject() instanceof IAction_cge ) {
					env.error(annotation.getFirstSymbol(), "Metaobject '" + annotation.getCyanMetaobject().getName() + "' implements interface "
							+ " IAction_cge. It cannot be called before 'package'");
				}
			}
		}
			
		String thisPackageName = getPackageIdent().getName();
		pw.print("package " + thisPackageName + ";");


		Set<CyanPackage> importedPackageSet = this.getImportedCyanPackageTable();
		
		env.atBeginningOfCurrentCompilationUnit(this);
		
		
		pw.println("");
		pw.println("import cyanruntime.*;\n");
		/*
		if ( ! thisPackageName.equals(NameServer.cyanLanguagePackageName) ) {
			pw.println("import " + NameServer.cyanLanguagePackageName + ".*;\n");
		}
		*/
		for ( CyanPackage aPackage : importedPackageSet ) {
			if ( aPackage != cyanPackage && 
				 ( aPackage.getCompilationUnitList().size() > 0 || 
				   (aPackage.getCompilationUnitDSLList() != null && aPackage.getCompilationUnitDSLList().size() > 0) ) ) {
				pw.println("import " + aPackage.getPackageName() + ".*;");  // caveat: aPackage name should already be in Java
			}
		}

		for ( ProgramUnit programUnit : programUnitList ) {
			pw.println("");
			programUnit.genJava(pw, env);
		}
		env.atEndOfCurrentCompilationUnit();
	}



	public void addProgramUnit(ProgramUnit programUnit) {
		programUnitList.add(programUnit);
	}

	public ArrayList<ProgramUnit> getProgramUnitList() {
		return programUnitList;
	}
	
	/**
	 * return the name of the sole public prototype declared in this compilation Unit.
	 * This name should be equal to the file name of the file. 
	 * @param data.env
	 */
	public String getNamePublicPrototype() {
		return this.getPublicPrototype().getName();
	}
	
	public ProgramUnit getPublicPrototype() {
		if ( publicPrototype == null ) {
			for ( ProgramUnit p : programUnitList ) 
				if ( p.getVisibility() == Token.PUBLIC ) {
					publicPrototype = p;
					return p;
				}
		}
		return publicPrototype;
	}


	/**
	 * this method is only called when the compilation unit has a single prototype which is generic.
	 * This method creates an instance of the prototype with the data of parameter "e". That is,
	 * if "this" refer to a compilation unit which has prototype "Stack<T>" and "e" 
	 * references "Stack<Int>", this method creates a prototype whose name is given by
	 *      NameServer.getJavaNameInterfaceObject(e)
	 * In this case, it will be created a prototype
	 *      Stack_left_gp_Int_right
	 * @param e
	 */
	@SuppressWarnings("resource")
	public CompilationUnit createInstanceGenericPrototype(ExprGenericPrototypeInstantiation e, Env env) {
		String fileName = null;
		String newFileName = null;
		String packageCanonicalPath1 = null;
		
		
		/*#
		make a new method for searching for I1(1), getting its package, "ga". Than add ".tmp". This should
		be recursive: the generic instantiations of I1 should suffer the same process.  */
		   // name of the file with the source code of the generic prototype instantiation
		String cyanSourceFileName = e.getSpecificSourceFileName(env) + NameServer.dotCyanSourceFileExtension;
		
		
		FileOutputStream fos = null;
		
		ProgramUnit programUnit = null;
		for ( ProgramUnit pu : this.getProgramUnitList() )
			if ( pu.getVisibility() == Token.PUBLIC ) {
				programUnit = pu;
				break;
			}
		if ( programUnit == null ) return null;

		PrintWriter printWriter = null;
		newFileName = fileName = getFullFileNamePath();

		int indexOfStartFileName = fileName.lastIndexOf(File.separator);
		if ( indexOfStartFileName > 0 ) {
			newFileName = fileName.substring(0, indexOfStartFileName);
			String dirName = newFileName + File.separator + NameServer.temporaryDirName;
			File dir = new File(dirName);
			if ( ! dir.exists() ) {
				dir.mkdirs();
			}
			packageCanonicalPath1 = dirName + File.separator;
			newFileName = packageCanonicalPath1 + cyanSourceFileName;
		}
		
		try {

			fos = new FileOutputStream(newFileName);
			printWriter = new PrintWriter(fos, true);
			PW pw = new PW();
			pw.set(printWriter);
			//if ( newFileName.contains("G1") )
				//System.gc();
			
			ProgramUnit currentProgramUnit = env.getCurrentProgramUnit();
			String prototypeNameInstantiation1;
			String packageNameInstantiation1;

			if ( currentProgramUnit == null ) {
				// env.error(e.getFirstSymbol(),  "Prototype instantiation outside a prototype");
				// return null;
				prototypeNameInstantiation1 = env.getProject().getProjectName();
				packageNameInstantiation1 = "";
			}
			else {
				prototypeNameInstantiation1 = currentProgramUnit.getNameWithOuter();
				packageNameInstantiation1 = currentProgramUnit.getCompilationUnit().getPackageName();
			}
			
			
			CyanEnv cyanEnv = new CyanEnv(programUnit, e, env, packageNameInstantiation1, prototypeNameInstantiation1);

			genCyan(pw, cyanEnv);
			
			// System.out.println("Creating: " + newFileName + " ");
		}
		catch ( FileNotFoundException e1 ) {
			this.error(null, 0, "Cannot create file " + newFileName, null, env);
			return null;
		}
		catch ( NullPointerException e3 ) {
			// e3.printStackTrace();
			this.error(null, 0, "Internal error in CompilationUnit when writing to file " + newFileName, null, env);
		}
		catch (Exception e2 ) {
			e2.printStackTrace();
			this.error(null, 0, "error in writing to file " + newFileName, null, env);
			return null;
		}
		finally {
			if ( fos != null ) {
				try {
					fos.close();
				}
				catch (IOException e1) {
					this.error(null, 0, "error in writing to file " + newFileName, null, env);
					return null;
				}
				finally {
					if ( printWriter != null ) {
						printWriter.flush();
						printWriter.close();
					}
				}
			}
		}
	
		
		CompilationUnit compilationUnit = new CompilationUnit(
				cyanSourceFileName,
				packageCanonicalPath1,
				getCompilerOptions(),
				null
				);
		
		return compilationUnit;
		
	}

	/**
	 * If the public program unit of this compilation unit is an interface, this method
	 * creates a Proto-interface for it. This object is used when the interface appears
	 * in an expression. So, if Shape is an interface, this method creates a Cyan 
	 * object named Proto_Shape. In the code
	 *       var Shape sh;
	 * The Java interface created for Shape, which is _Shape, is used as the
	 * type of sh in the Java code. However, in
	 *       sh = Shape;
	 * Shape is used in an expression. Then this code is equivalent to
	 *       sh = Proto_Shape;
	 * Proto_Shape is a Cyan prototype that implements Shape and whose methods
	 * throw exception ExceptionCannotCallInterfaceMethod. 
	 * @throws IOException 
	 */
	public CompilationUnit createProtoInterface() {
		String newFileName = null;
		
		   // name of the file with the source code of the generic prototype instantiation
		String cyanSourceFileName = NameServer.prototypeFileNameFromInterfaceFileName(this.getFileNameWithoutExtension())
				+ "." + NameServer.cyanSourceFileExtension;
		
		FileOutputStream fos = null;
		String dirName;
		try {
			newFileName = getFullFileNamePath();
			int indexOfSlash = newFileName.lastIndexOf(File.separatorChar);
			if ( indexOfSlash < 0 ) indexOfSlash = 0;
			

			dirName = newFileName.substring(0, indexOfSlash);
			if ( dirName.endsWith(NameServer.temporaryDirName) ) {
				dirName += File.separatorChar;
			}
			else {
				dirName += File.separatorChar +
						NameServer.temporaryDirName + File.separatorChar;
			}
			File tmpDir = new File(dirName);
			if ( ! (tmpDir.exists() && tmpDir.isDirectory()) ) {
				tmpDir.mkdirs();
			}
			newFileName = dirName  +
					cyanSourceFileName;
			
			fos = new FileOutputStream(newFileName);
			PrintWriter printWriter = new PrintWriter(fos, true);
			PW pw = new PW();
			pw.set(printWriter);
			genCyanProtoInterface(pw);
			printWriter.close();
		}
		catch ( FileNotFoundException e ) {
			Env env = null;
			this.error(null, 0, "Cannot create file " + newFileName, null, env);
			return null;
		}
		catch (Exception e ) {
			Env env = null;
			this.error(null, 0, "error in writing to file " + newFileName, null, env);
			try {
				if ( fos != null ) 
					fos.close();
			}
			catch (IOException e1) {
			}
			return null;
		}
		CompilationUnit newCompilationUnit = new CompilationUnit(
				cyanSourceFileName,
				dirName,
				getCompilerOptions(),
				cyanPackage
				);
		newCompilationUnit.readSourceFile();
		newCompilationUnit.setIsPrototypeInterface(true);
		
		this.interfaceCompilationUnit = newCompilationUnit;
		return newCompilationUnit;
		
	}
	
	
	
	
	private void genCyanProtoInterface(PW pw) {
		pw.println("package " + this.getPackageIdent().getName());

		pw.println("");
		for ( ExprIdentStar expr : this.getImportPackageList() ) {
			pw.println("import " + expr.getName());  
		}
		pw.println("");
		for ( ProgramUnit programUnit : this.getProgramUnitList() )
			if ( programUnit instanceof InterfaceDec )
				((InterfaceDec) programUnit).genCyanProtoInterface(pw);
	}


	public String getPackageCanonicalPath() {
		return packageCanonicalPath;
	}

	
	public void setHasGenericPrototype(boolean hasGenericPrototype) {
		this.hasGenericPrototype = hasGenericPrototype; 
	}
	public boolean getHasGenericPrototype() {
		return hasGenericPrototype;
	}
	
	/**
	 * true if this compilation unit has a generic prototype. The same
	 * as {@link #getHasGenericPrototype()}. However, this method relays
	 * on the file name of the compilation unit. It it has a '(' in the
	 * file name then it has a generic prototype
	   @return
	 */
	public boolean hasGenericPrototype() {
		int i = filename.indexOf('(');
		return i > 0 && Character.isDigit(filename.charAt(i+1));
	}

	public boolean getPrototypeIsNotGeneric() {
		return prototypeIsNotGeneric;
	}

	public void setPrototypeIsNotGeneric(boolean hasPrototypeInstantiation) {
		this.prototypeIsNotGeneric = hasPrototypeInstantiation;
		
	}


	public void calcInternalTypes(ICompiler_dsa compiler_dsa, Env env) {
		
		if ( ! alreadyCalcInternalTypes ) {
			alreadyCalcInternalTypes = true;
			env.atBeginningOfCurrentCompilationUnit(this);
			if ( nonAttachedMetaobjectAnnotationListBeforePackage != null ) {
				for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationListBeforePackage ) {
					annotation.calcInternalTypes(env);
				}
			}
			
			compiler_dsa.getEnv().atBeginningOfCurrentCompilationUnit(this);
			
			for ( ProgramUnit programUnit : programUnitList ) 
				programUnit.calcInternalTypes(compiler_dsa, env);
			
			
			
			checkErrorMessages(env);			
			compiler_dsa.getEnv().atEndOfCurrentCompilationUnit();

			env.atEndOfCurrentCompilationUnit();
		}
	}
	
	public void prepareGenericCompilationUnit(Env env) {
		this.importedPackageNameList = new HashSet<String>();
		for ( ExprIdentStar e : importPackageList ) 
			importedPackageNameList.add(e.getName());
		
		// no conflicts yet
		conflictProgramUnitTable = new HashMap<>();
		prepareCompilationUnit(env);
		/*
		 * inserts all generic parameters as prototypes in the package of this compilation unit
		 */
		ProgramUnit publicPU = this.getPublicPrototype();
		ArrayList<ProgramUnit> puList = new ArrayList<>();
		for ( ArrayList<GenericParameter> gpList : this.getPublicPrototype().getGenericParameterListList() ) {
			for ( GenericParameter gp : gpList ) {
				ProgramUnit obj = publicPU.clone(); // new ObjectDec(null, Token.PUBLIC, null, null,);
				if ( obj == null ) {
					env.error(publicPU.getFirstSymbol(), "Internal error: cannot clone this object");
					return ;
				}
				obj.setRealName(gp.getName());
				puList.add( obj );
			}
		}
		env.setProgramUnitForGenericPrototypeList(puList);
	}

	public void prepareCompilationUnit(Env env) {

		
		if ( alreadyCalcInterfaceTypes )  return;
		alreadyCalcInterfaceTypes = true;
		
		Project project = cyanPackage.getProject();
		String packageNameOfThisCompilationUnit = cyanPackage.getPackageName();
		importedCyanPackageTable = new HashSet<CyanPackage>();
		
		int i = 0;
		for ( String packageName : this.importedPackageNameList ) {
			CyanPackage p = project.searchPackage(packageName);
			if ( p == null ) {
				ExprIdentStar importedPackage = this.importPackageList.get(i);
				env.error( true, importedPackage.getFirstSymbol(), 
						   "Package " + packageName + " was not found", packageName, ErrorKind.package_was_not_found_outside_prototype);
			}
			else {
				if ( packageNameOfThisCompilationUnit.compareTo(packageName) == 0 ) {
				ExprIdentStar importedPackage = this.importPackageList.get(i);
					env.error(true, importedPackage.getFirstSymbol(), "Package cannot import itself",
							packageName, ErrorKind.package_is_importing_itself);
				}
				
				this.importedCyanPackageTable.add(p);
			}
		}
		
		if ( ! this.importPackageSet.equals(this.importedCyanPackageTable) ) {
			System.out.println("ips != icpt");
		}
		String publicProgramUnitNameThisCompilationUnit = this.getPublicPrototype().getSymbol().getSymbolString();
		
		  // import package of this compilation unit
		importedCyanPackageTable.add(this.cyanPackage);
		
		if ( ! cyanPackage.getPackageName().equals(NameServer.cyanLanguagePackageName) ) {
			  // import package cyan.lang
			CyanPackage cyanLangPackage = project.searchPackage(NameServer.cyanLanguagePackageName);
			if ( cyanLangPackage == null ) {
				env.error( true, null, 
						   "Package 'cyan.lang' was not found", "cyan.lang", ErrorKind.package_was_not_found_outside_prototype);
			}
			importedCyanPackageTable.add(cyanLangPackage);
		}
		
		
		HashMap<String, String> importedProgramUnitSet = new HashMap<String, String>();
		for ( CyanPackage cp : importedCyanPackageTable ) {
			Set<String> onePackageRawPrototypeNameSet = new HashSet<String>();
			/*
			 * first collect all raw prototype names of package 'cp' in set onePackageRawPrototypeNameSet.
			 * Currently every compilation unit has just one program unit 
			 */
			for ( CompilationUnit compUnit : cp.getCompilationUnitList() ) {
				for ( ProgramUnit programUnit : compUnit.getProgramUnitList() ) {
					if ( programUnit.getVisibility() == Token.PUBLIC ) {
						String puName = programUnit.getSymbol().getSymbolString();
						onePackageRawPrototypeNameSet.add(puName);
					}
				}
			}
			for ( String rawPrototypeName : onePackageRawPrototypeNameSet ) {
				String oldPackageName = importedProgramUnitSet.put(rawPrototypeName, cp.getPackageName() );
				if ( oldPackageName != null ) {
					/* importedProgramUnitSet already contains a program unit with this name
					 * That is ok if this program unit is not the public program unit of 
					 * this compilation unit. 
					 * */
					if ( ! rawPrototypeName.equals(publicProgramUnitNameThisCompilationUnit) ) {
						this.conflictProgramUnitTable.put(rawPrototypeName, oldPackageName + ", " + cp.getPackageName());
					}
				}
				
			}
		}
	}
	
	

	/**
	 * Do the lexical analysis of compilationUnit. The result is an array of
	 * Symbols that is put in the object compilationUnit.
	 * 
	 * @param compilationUnit
	 *            , the unit to be compiled, an object or interface
	 * @return true if there was no fatal error.
	 */
	

	public void calcInterfaceTypes(Env env) {
		

		if ( ! alreadyCalcInterfaceTypes ) {
			env.atBeginningOfCurrentCompilationUnit(this);
			this.prepareCompilationUnit(env);
			alreadyCalcInterfaceTypes = true;

			boolean allowCreationOfPrototypesInLastCompilerPhases = env.getAllowCreationOfPrototypesInLastCompilerPhases();;
			try {
				
				for ( ProgramUnit programUnit : programUnitList ) {
					if ( this.cyanPackage.getPackageName().equals(NameServer.cyanLanguagePackageName) ) {
						env.setAllowCreationOfPrototypesInLastCompilerPhases(true);
					}
					if ( programUnit.getPrototypeIsNotGeneric() )
					    programUnit.calcInterfaceTypes(env);
				}
			}
			finally {
				env.setAllowCreationOfPrototypesInLastCompilerPhases(allowCreationOfPrototypesInLastCompilerPhases);
			}
			
			
			
			
			checkErrorMessages(env);
			env.atEndOfCurrentCompilationUnit();
		}
	}


	/**   
	 * 
	 * Check whether the error messages appointed by metaobject annotations to compilationError were
	 * signaled by the compiler. The messages checked are only those with the same line number
	 * in compilationError and in the compiler
	 *  
	   @param env
	 */
	private void checkErrorMessages(Env env) {
		
		/*
		 * checking should be done if errorList.size() > 0 
		 * At the end of compilation, before step 10, there should be
		 * 
		 */
		if ( lineMessageList != null && lineMessageList.size() > 0 &&
				errorList != null && errorList.size() > 0  ) {
			/*
			 * metaobject <code>compilationErrro</code> was called in the current 
			 * compilation unit. The we need to check whether the compiler really 
			 * Signaled the errors appointed by the calls to this metaobject
			 */
			int ii = 0;
			/*
			 * metaobject compilationError has been called in this compilation unit
			 */
			ArrayList<Tuple3<Symbol, Integer, String>> moreErrorList = null;
			String expectedErrorMessage = null;
			for (UnitError ue : errorList ) {
				for ( Tuple3<Integer, String, Boolean> t: lineMessageList ) {
					if ( !t.f3 && t.f1 == ue.getLineNumber() ) {
						// found the correct line number  and the error has not been signaled
						expectedErrorMessage = t.f2;

						if ( moreErrorList == null ) 
							moreErrorList = new ArrayList<>();
						moreErrorList.add( new Tuple3<Symbol, Integer, String>(ue.getSymbol(), ue.getSymbol().getLineNumber(), 
								"The expected error message at line " + ue.getLineNumber() + " was '" + expectedErrorMessage + "'. The message given by the compiler was '" + 
						   ue.getMessage() + "'"));
	               /* 					this.warning(ue.getSymbol(), ue.getSymbol().getLineNumber(), 
								"The expected error message at line " + ue.getLineNumber() + " was '" + expectedErrorMessage + "'. The message given by the compiler was '" + 
						   ue.getMessage() + "'");  */
						
						lineMessageList.get(ii).f3 = true;
						
						expectedErrorMessage = null;
											
						break;
					}
					++ii;
				}
			}
			if ( moreErrorList != null ) {
				for ( Tuple3<Symbol, Integer, String> error : moreErrorList ) {
					this.warning(error.f1,  error.f2,  error.f3, null, env);
				}
			}
		}
	}
	

	public void ati_actions(ICompiler_ati compiler_ati, CompilerManager_ati compilerManager) {
		Env env = (Env ) compiler_ati.getEnv();
		env.atBeginningOfCurrentCompilationUnit(this);


		for ( ProgramUnit programUnit : programUnitList ) {
			if ( programUnit.getPrototypeIsNotGeneric() )
			    programUnit.ati_actions(compiler_ati, compilerManager);
		}
		env.atEndOfCurrentCompilationUnit();
	}


	public void ati3_check(ICompiler_ati compiler_ati) {
		Env env = (Env ) compiler_ati.getEnv();
		env.atBeginningOfCurrentCompilationUnit(this);


		for ( ProgramUnit programUnit : programUnitList ) {
			if ( programUnit.getPrototypeIsNotGeneric() )
			    programUnit.ati3_check(compiler_ati, env);
		}
		env.atEndOfCurrentCompilationUnit();
		
	}
	
	
	public void dsa2_check(ICompiler_dsa compiler_dsa, Env env) {
		compiler_dsa.getEnv().atBeginningOfCurrentCompilationUnit(this);


		for ( ProgramUnit programUnit : programUnitList ) {
			if ( programUnit.getPrototypeIsNotGeneric() )
			    programUnit.dsa2_check(compiler_dsa, env);
		}
		compiler_dsa.getEnv().atEndOfCurrentCompilationUnit();
		
		
	}

	

	
	public boolean getIsPrototypeInterface() {
		return isPrototypeInterface;
	}


	public void setIsPrototypeInterface(boolean isPrototypeInterface) {
		this.isPrototypeInterface = isPrototypeInterface;
	}
	
	


	/*public HashMap<String, CyanMetaobjectLiteralObjectIdentSeq> getMetaObjectLiteralObjectIdentSeqTable() {
		return metaObjectLiteralObjectIdentSeqTable;
	} */


	public Program getProgram() {
		return program;
	}


	public void setProgram(Program program) {
		this.program = program;
	}


	public CompilationUnit getInterfaceCompilationUnit() {
		return interfaceCompilationUnit;
	}


	public void setInterfaceCompilationUnit(CompilationUnit interfaceCompilationUnit) {
		this.interfaceCompilationUnit = interfaceCompilationUnit;
	}

	public HashMap<String, String> getConflictProgramUnitTable() {
		return conflictProgramUnitTable;
	}

	public Set<CyanPackage> getImportedCyanPackageTable() {
		return importedCyanPackageTable;
	}

	/**
	 * return the original source code of this compilation unit, without any additions or
	 * changes made by the compiler during the compilation
	   @return
	 */
	public char[] getOriginalText() {
		return originalText;
	}
	
	public ArrayList<Tuple3<Integer, String, Boolean>> getLineMessageList() {
		return lineMessageList;
	}


	public void addLineMessageList(
			Tuple3<Integer, String, Boolean> lineMessage) {
		lineMessageList.add(lineMessage);
	}

	
	public boolean getIsInterfaceAsObject() {
		return isInterfaceAsObject;
	}

	public void setInterfaceAsObject(boolean isInterfaceAsObject) {
		this.isInterfaceAsObject = isInterfaceAsObject;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getNonAttachedMetaobjectAnnotationListBeforePackage() {
		return nonAttachedMetaobjectAnnotationListBeforePackage;
	}

	public void setNonAttachedMetaobjectAnnotationListBeforePackage(
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationListBeforePackage) {
		this.nonAttachedMetaobjectAnnotationListBeforePackage = nonAttachedMetaobjectAnnotationListBeforePackage;
	}

	
	public Set<String> getImportedPackageNameList() {
		return importedPackageNameList;
	}

	
	/**
	 * if this program unit was created from a generic prototype instantiation, 
	 * the instantiation is in package packageNameInstantiation, prototype prototypeNameInstantiation,
	 * line number lineNumberInstantiation, and column number columnNumberInstantiation.
	 */
	private String packageNameInstantiation, prototypeNameInstantiation;
	private int lineNumberInstantiation, columnNumberInstantiation;


	public String getPackageNameInstantiation() {
		return packageNameInstantiation;
	}


	public void setPackageNameInstantiation(String packageNameInstantiation) {
		this.packageNameInstantiation = packageNameInstantiation;
	}


	public String getPrototypeNameInstantiation() {
		return prototypeNameInstantiation;
	}


	public void setPrototypeNameInstantiation(String prototypeNameInstantiation) {
		this.prototypeNameInstantiation = prototypeNameInstantiation;
	}


	public int getLineNumberInstantiation() {
		return lineNumberInstantiation;
	}


	public void setLineNumberInstantiation(int lineNumberInstantiation) {
		this.lineNumberInstantiation = lineNumberInstantiation;
	}


	public int getColumnNumberInstantiation() {
		return columnNumberInstantiation;
	}


	public void setColumnNumberInstantiation(int columnNumberInstantiation) {
		this.columnNumberInstantiation = columnNumberInstantiation;
	}


	public Set<JVMPackage> getImportJVMPackageSet() {
		return importJVMPackageSet;
	}

	public void setImportJVMPackageSet(Set<JVMPackage> importJVMPackageSet) {
		this.importJVMPackageSet = importJVMPackageSet;
	}

	public Set<TypeJavaRef> getImportJVMJavaRefSet() {
		return importJVMJavaRefSet;
	}

	public void setImportJVMJavaRefSet(Set<TypeJavaRef> importJVMJavaRefSet) {
		this.importJVMJavaRefSet = importJVMJavaRefSet;
	}


	
	/*
	 * true if there is a declaration of a generic prototype in this program unit
	 */
	private boolean hasGenericPrototype;

	/**
	 * Compiler options of this class or interface
	 */
	private CompilerOptions compilerOptions;
	/**
	 * the package to which this compilation unit belong to
	 */
	private CyanPackage cyanPackage;

	/**
	 * Canonical path of the source file. This path is something like
	 *      D:\My Dropbox\art\programming languages\Cyan\
	 * the last character is always \ in Windows
	 */
	private String packageCanonicalPath;

	/**
	 * list of the program units of this compilation unit: all objects or
	 * interfaces declared in file "filename".
	 */
	private ArrayList<ProgramUnit> programUnitList;

	/* Map with conflicts of program units. It consists of the name of the prototype
	 * and a string. This string contains the names, separated by spaces, of the
	 * packages that define the prototype and that are imported by this compilation unit.
	 */
	private HashMap<String, String> conflictProgramUnitTable;

	/*
	 * true if there is a declaration of an instantiation of  generic prototype in this compilation unit
	 * Something like "object Stack<Int> ... end". 
	 */
	private boolean prototypeIsNotGeneric;
	
	
	
	/**
	 * the public prototype of this compilation unit
	 */
	public ProgramUnit publicPrototype;

	/**
	 * true if this compilation unit was created by the compiler from an interface. 
	 * For each interface named "Inter" the compiler creates a prototype
	 * "Proto_Inter" which is used whenever the interface appears inside
	 * an expression in a Cyan program. Only these "Proto_Inter" prototypes
	 * are in compilation units that have isPrototypeInterface equal to true
	 */
	private boolean isPrototypeInterface;

	/**
	 * the program in which this compilation unit is
	 */
	private Program program;
	

	/**
	 * true if method calcInterfaceTypes has already been called.
	 */
	private boolean	alreadyCalcInterfaceTypes;

	/**
	 * true if method calcInterfaceTypes has already been called.
	 */
	private boolean	alreadyCalcInternalTypes;

	/**
	 * For each interface I the compiler creates another prototype <code>Proto_I</code> that represents the interface 
	 * in expressions. That is, when an interface I is used as in <br>
	 * <code>             s = I prototypeName;</code> <br>
	 * the compiler in fact uses  Proto_I:<br>
	 * <code>             s = Proto_I prototypeName</code><br>
	 *              
	 * If this compilation unit contains the public interface I, interfaceCompilationUnit refers to
	 * a compilation unit with the public prototype <code>Proto_I</code>               
	 */
	private CompilationUnit interfaceCompilationUnit;
	/**
	 * true if this compilation unit represents an interface when considered as an object. See {@link #interfaceCompilationUnit}
	 */
	private boolean isInterfaceAsObject;
	/**
	 * list of pairs <code>(lineNumber, errorMessage, used)</code>. A metaobject that is an instance of {@link meta#IInformCompilationError} 
	 * signaled that there should be an error in this source file (being compiled) at line <code>lineNumber</code>. The possible
	 * error message is <code>errorMessage</code>. 'used' is true if this error has been signaled by the compiler 
	 */
	private ArrayList<Tuple3<Integer, String, Boolean>> lineMessageList;

	
	/**
	 * symbols of the names of the imported packages
	 */
	protected ArrayList<ExprIdentStar>	importPackageList;
	protected Set<CyanPackage>	importedCyanPackageTable;
	/**
	 * names of the imported packages. 
	 */
	protected Set<String>	importedPackageNameList;

	/**
	 * symbol of keyword 'package' that starts the compilation unit
	 */
	Symbol packageSymbol;

	/**
	 * list of metaobject annotations before keyword 'package'
	 */
	ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationListBeforePackage;

	/**
	 * set of JVM packages imported by this compilation unit
	 */
	private Set<JVMPackage>  importJVMPackageSet;
	/**
	 * set of JVM classes or interfaces imported by this compilation unit
	 */
	private Set<TypeJavaRef>  importJVMJavaRefSet;

	
}

