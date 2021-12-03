package ast;

/**
 * represents a package. 
 */
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import error.CompileErrorException;
import meta.CompilerManager_ati;
import meta.CompilerPackageView_ati;
import meta.CyanMetaobject;
import meta.CyanMetaobjectWithAt;
import meta.IActionPackage_ati;
import meta.IActionProgramUnit_ati;
import meta.IAction_cge;
import meta.ICheckPackage_ati3;
import meta.ICheckPackage_dsa2;
import meta.ICompilerPackageView_ati;
import saci.CompilerOptions;
import saci.Env;
import saci.NameServer;
import saci.Project;
import saci.Tuple2;
import saci.Tuple3;
import saci.Tuple6;

public class CyanPackage implements ASTNode {

	public CyanPackage(Program program, String packageName, Project project, String packageCanonicalPath, 
	        ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList,
	        ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList,
	        ArrayList<CyanMetaobject> metaobjectList
			) {
		this.program = program;
		this.packageName = packageName;
		compilationUnitList = new ArrayList<CompilationUnit>();
		this.project = project;
		this.setPackageCanonicalPath(packageCanonicalPath);
		this.nonAttachedMetaobjectAnnotationList = nonAttachedMetaobjectAnnotationList;
		this.attachedMetaobjectAnnotationList = attachedMetaobjectAnnotationList;
		hasGenericPrototype = false;
		compilerVersionLastSuccessfulCompilation = -1;
		this.metaobjectList = metaobjectList;
		this.compilationUnitDSLList= new ArrayList<CompilationUnitDSL>();
		// loadPackageMetaobjects();
	}

	@Override
	public void accept(ASTVisitor visitor) {

		visitor.preVisit(this);
		for ( CompilationUnit cunit : this.compilationUnitList ) {
			cunit.accept(visitor);
		}
		visitor.visit(this);
	}	
	
	
	private void loadPackageMetaobjects() {
		String slash = File.separator;
		String path;
		String name = "";
		HashMap<String, Class<?>> cyanMetaobjectTable = program.getCyanMetaobjectTable();
		
		metaobjectList = new ArrayList<CyanMetaobject>();
		path = this.packageCanonicalPath + (this.packageCanonicalPath.endsWith(slash) ? "" : slash ) + NameServer.metaobjectPackageName;
		if ( ! path.endsWith(slash) )
			path = path + slash;
		File dir = new File(path);
		
		if ( dir.exists() ) {
			
			
		    for( File file: dir.listFiles() ) {
		    	if ( ! file.isDirectory() ) {
		    		name = file.getName();
		    		if ( name.endsWith(".class") ) {
		    			String metaobjectName = name.substring(0, name.length() - NameServer.dotClassLength);
		    			Class<?> clazz = cyanMetaobjectTable.get(metaobjectName);
		    			
			    		// System.out.println("moName : " + name + "clazz = " + (clazz == null ? "null" : "not null")); 

		    			if ( clazz != null ) {
			    			//Class<?> clazz = urlcl.loadClass(NameServer.metaobjectPackageName + metaobjectName);
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
			    					// System.out.println("metaobject: " + name);
			    					if ( foundConstructorWithoutParameters ) {
						    			Object newObj = clazz.newInstance();
						    			if ( !(newObj instanceof CyanMetaobject) )
						    				project.error("File '" + path + name + "' should inherit from 'CyanMetaobject'");
						    			else {
						    				CyanMetaobject cyanMetaobject =  (CyanMetaobject ) newObj;
						    				cyanMetaobject.setFileName(name);
						    				cyanMetaobject.setPackageName(this.packageName);
						    				if ( checkMetaobject(cyanMetaobject) ) {
							    				metaobjectList.add(cyanMetaobject);
						    				}
						    			}
			    					}
			    					else
					    				project.error("File '" + path + name + "' should contain a class with a constructor without parameters and that does not throw any exception."
					    						+ " It seems that someone defined a metaobject class, in Java, that do not have a constructor that does not take parameters.");
			    				}
			    			}
			    			catch (IllegalAccessException | InstantiationException e) {
			    				project.error("Illegal metaobject in '" + path + name + "'");
			    			}
		    			}
		    			
		    		}
		    	}
		    }
		}
		
	}
	
	
	/**
	 * check if <code>cyanMetaobject</code> was defined correctly. It may, for example, implement two incompatible 
	 * interfaces.
	   @param cyanMetaobject
	 */
	public boolean checkMetaobject(CyanMetaobject cyanMetaobject) {
		if ( cyanMetaobject instanceof IAction_cge && !packageName.equals(NameServer.cyanLanguagePackageName) ) {
			project.error("Interface 'IAction_cge' can only be implemented by metaobjects of package cyan.lang. But it is being " 
					+ " implemented by metaobject '" + cyanMetaobject.getName() + "'"); //+ "' of package '" + cyanMetaobject.getPackageName() + "'"));
			return false;
		}
		return true;
	}
	
	
	/** prints all package information
	 * 
	 */
	public void print() {
		System.out.println("    package name: " + packageName+ "\n" +  // ok
				  "    Compiler Options: " + compilerOptions + "\n" +
				  "    source files: "
				);
		for (int i = 0; i < compilationUnitList.size(); i++) {
			System.out.println("        source: " + compilationUnitList.get(i).getFilename()  + "\n");  // ok
			compilationUnitList.get(i).getCompilerOptions().print();
		}
		
	}
	
	public void addCompilationUnit(CompilationUnit compilationUnit) {
		compilationUnitList.add(compilationUnit);
	}

	public ArrayList<CompilationUnit> getCompilationUnitList() {
		return compilationUnitList;
	}


	public void addCompilationUnitDSL(CompilationUnitDSL compilationUnitDSL) {
		compilationUnitDSLList.add(compilationUnitDSL);
	}

	public ArrayList<CompilationUnitDSL> getCompilationUnitDSLList() {
		return compilationUnitDSLList;
	}

	
	
	public String getPackageName() {
		return packageName;
	}

	public void setCompilerOptions(CompilerOptions compilerOptions) {
		this.compilerOptions = compilerOptions;
	}


	public CompilerOptions getCompilerOptions() {
		return compilerOptions;
	}


	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}

	public Program getProgram() {
		return program;
	}
	
	/**
	 * search and returns a public program unit whose name is <code>"name"</code> in this package.
	 * It includes program units like <code>"Stack< main.Person >"</code> but not <code>Stack<T></code>. In 
	 * the last case, <code>T</code> is a generic parameter
	 */
	public ProgramUnit searchPublicNonGenericProgramUnit(String name) {
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			ProgramUnit pu = compilationUnit.getPublicPrototype();
			
			/*
			if ( name.equals("P<main.B,name>") ) {
				// System.out.println(pu.getName());
			}
			*/
				
			if ( pu != null && pu.getName().equals(name)  && ! pu.getGenericPrototype() )
				return compilationUnit.getPublicPrototype();
		}
		return null;
	}
	
	/**
	 * return prototype whose source file name is sourceFileName and that was declared in this package 
	 */
	
	public ProgramUnit searchProgramUnitBySourceFileName(String sourceFileName) {
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			if ( compilationUnit.getFileNameWithoutExtension().equals(sourceFileName) )
				return compilationUnit.getPublicPrototype();
			/*ProgramUnit publicPrototype = compilationUnit.getPublicPrototype();
			if ( publicPrototype.getNameSourceFile().equals(sourceFileName)  );
				return publicPrototype;  */
		}
		return null;
	}
	
	/**
	 * return the compilation unit that has name 'fileName.cyan' of this package. null if none.
	   @return
	 */
	public CompilationUnit searchCompilationUnit(String fileName) {
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			if ( compilationUnit.getFileNameWithoutExtension().equals(fileName) ) 
				return compilationUnit;
		}
		return null;
	}

	public String getPackageCanonicalPath() {
		return packageCanonicalPath;
	}


	public void setPackageCanonicalPath(String packageCanonicalPath) {
		this.packageCanonicalPath = packageCanonicalPath;
	}

	public ArrayList<CyanMetaobject> getMetaobjectList() {
		return metaobjectList;
	}
	
	public boolean getHasGenericPrototype() {
		return hasGenericPrototype;
	}


	public void setHasGenericPrototype(boolean hasGenericPrototype) {
		this.hasGenericPrototype = hasGenericPrototype;
	}
	
	
	public ArrayList<CyanMetaobjectWithAtAnnotation> getNonAttachedMetaobjectAnnotationList() {
		return nonAttachedMetaobjectAnnotationList;
	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getAttachedMetaobjectAnnotationList() {
		return attachedMetaobjectAnnotationList;
	}


	public ExprAnyLiteral searchFeature(String featureName) {
		if ( featureList != null ) {
			for ( Tuple2<String, ExprAnyLiteral> t : this.featureList ) {
				if ( t.f1.equals(featureName) ) {
					return t.f2;
				}
			}
		}
		return null;
	}
	
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		return featureList;
	}

	public void addFeature(Tuple2<String, ExprAnyLiteral> feature) {
		if ( featureList == null ) 
			featureList = new ArrayList<>();
		else {
			int size = featureList.size();
			for ( int i = 0; i < size; ++i) {
				if ( featureList.get(i).f1.equals(feature.f1) ) {
					// replace
					featureList.set(i, feature);
					return;
				}
			}
		}
		featureList.add(feature);
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void dsa2_check(Env env, CompilerPackageView_ati compilerPackageView_ati) {
		
		ArrayList<CyanMetaobjectWithAtAnnotation> packageMetaobjectAnnotationList = new ArrayList<>();
		if ( attachedMetaobjectAnnotationList != null )
			packageMetaobjectAnnotationList.addAll(attachedMetaobjectAnnotationList);
		if ( nonAttachedMetaobjectAnnotationList != null )
			packageMetaobjectAnnotationList.addAll(nonAttachedMetaobjectAnnotationList);

		/**
		 * for each metaobject attached and non-attached to this package
		 * in the .pyan file, calls method {@link ICheckPackage_ati3#ati_check}
		 */
		for ( CyanMetaobjectWithAtAnnotation annotation : packageMetaobjectAnnotationList ) {
			CyanMetaobjectWithAt metaobject = annotation.getCyanMetaobject();
			metaobject.setMetaobjectAnnotation(annotation);
			if ( metaobject instanceof ICheckPackage_dsa2 )  {
				ICheckPackage_dsa2 fp = (ICheckPackage_dsa2 ) metaobject;
				
				try {
					fp.dsa2_checkPackage(compilerPackageView_ati);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(annotation, annotation.getFirstSymbol(), e);
				}					
				finally {
					env.errorInMetaobjectCatchExceptions(metaobject, annotation);
				}
			}
		}
	}
	
	
	public void ati3_check(Env env, ICompilerPackageView_ati compilerPackageView) {
		
		
		ArrayList<CyanMetaobjectWithAtAnnotation> packageMetaobjectAnnotationList = new ArrayList<>();
		if ( attachedMetaobjectAnnotationList != null )
			packageMetaobjectAnnotationList.addAll(attachedMetaobjectAnnotationList);
		if ( nonAttachedMetaobjectAnnotationList != null )
			packageMetaobjectAnnotationList.addAll(nonAttachedMetaobjectAnnotationList);

		/**
		 * for each metaobject attached and non-attached to this package
		 * in the .pyan file, calls method {@link ICheckPackage_ati3#ati_check}
		 */
		for ( CyanMetaobjectWithAtAnnotation annotation : packageMetaobjectAnnotationList ) {
			CyanMetaobjectWithAt metaobject = annotation.getCyanMetaobject();
			metaobject.setMetaobjectAnnotation(annotation);
			if ( metaobject instanceof ICheckPackage_ati3 )  {
				ICheckPackage_ati3 fp = (ICheckPackage_ati3 ) metaobject;
				
				try {
					fp.ati3_checkPackage(compilerPackageView);
				}
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(annotation, annotation.getFirstSymbol(), e);
				}					
				finally {
					env.errorInMetaobjectCatchExceptions(metaobject, annotation);
				}
				
				
				
			}
		}

		
	}

	public void ati_actions(Env env, ICompilerPackageView_ati compilerPackageView_ati, CompilerManager_ati compilerManager) {
		
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation : nonAttachedMetaobjectAnnotationList ) {
				if ( cyanMetaobjectAnnotation instanceof IActionProgramUnit_ati || cyanMetaobjectAnnotation instanceof IActionPackage_ati ) {
					env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "This metaobject is not attached to a package. It cannot implement "
							+ "interfaces ICodegGen_ati or IActionPackage_ati" );
				}
			}
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation : attachedMetaobjectAnnotationList ) {
				
				
				CyanMetaobject cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();
				if ( cyanMetaobject == null ) continue;
		
				cyanMetaobject.setMetaobjectAnnotation(cyanMetaobjectAnnotation);
				
				if ( cyanMetaobject instanceof IActionProgramUnit_ati ) {
					env.error(cyanMetaobjectAnnotation.getFirstSymbol(), "Metaobject '" + cyanMetaobject.getName() + "' attached to Cyan package '"
							+ packageName + "' implements interface '" + IActionProgramUnit_ati.class.getName() + "'. "
						 + "It should implement interface '" + IActionPackage_ati.class.getName() + "' instead.");
					return ;
				}
				
				if ( cyanMetaobject instanceof IActionPackage_ati ) {
					
					CyanMetaobjectWithAt cyanMetaobjectWithAt = (CyanMetaobjectWithAt ) cyanMetaobject;
					
					/**
					 * create new prototypes
					 */
					try {
						IActionPackage_ati metaobject = (IActionPackage_ati ) cyanMetaobject;
						ArrayList<Tuple2<String, StringBuffer>> prototypeNameCodeList = null;
						
						try {
							prototypeNameCodeList = metaobject.ati_NewPrototypeList(compilerPackageView_ati);
						} 
						catch ( error.CompileErrorException e ) {
						}
						catch ( RuntimeException e ) {
							env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
						}
						finally {
							env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
						}
						
						
						
						if ( prototypeNameCodeList != null ) {
							for ( Tuple2<String, StringBuffer> prototypeNameCode : prototypeNameCodeList ) {
								Tuple2<CompilationUnit, String> t = env.getProject().getCompilerManager().createNewPrototype(
										prototypeNameCode.f1, prototypeNameCode.f2, 
										this.getCompilerOptions(), this);
								if ( t != null && t.f2 != null ) {
									env.error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), t.f2);
								}
							}
						}
					}
					catch ( CompileErrorException e ) {  
					
					}
					
					
					/**
					 * add code to prototypes
					 */
					ArrayList<Tuple2<String, StringBuffer>> codeToAddList = null;
					
					
					
					try {
						codeToAddList = 
								((IActionPackage_ati ) cyanMetaobjectWithAt).ati_CodeToAdd(compilerPackageView_ati);						
						
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
					}
					
					if ( codeToAddList != null ) {
						for ( Tuple2<String, StringBuffer> codeToAdd : codeToAddList ) {
							String prototypeName = codeToAdd.f1;				
							compilerManager.addCode(cyanMetaobjectWithAt, packageName, prototypeName, codeToAdd.f2);
						}
					}
					
					
					/**
					 * add method to prototypes
					 */
					ArrayList<Tuple3<String, String, StringBuffer>> methodList = null;
					
					try {
						methodList = ((IActionPackage_ati ) cyanMetaobjectWithAt).ati_methodCodeList(compilerPackageView_ati);
						
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
					}
					
					if ( methodList != null ) {
						for ( Tuple3<String, String, StringBuffer> t : methodList ) {
							String prototypeName = t.f1;				
							compilerManager.addMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f2, t.f3);
						}
					}
					
					/**
					 * add statements to methods
					 */
					ArrayList<Tuple3<String, String, StringBuffer>> statsList = null;
					
					try {
						statsList = ((IActionPackage_ati ) cyanMetaobjectWithAt).ati_beforeMethodCodeList(compilerPackageView_ati);
						
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
					}
					
					
					if ( statsList != null ) {
						for ( Tuple3<String, String, StringBuffer> t : statsList ) {
							String prototypeName = t.f1;
							
							compilerManager.addBeforeMethod(cyanMetaobjectWithAt, packageName, prototypeName, t.f2, t.f3);
						}
					}
					
					/**
					 * add instance variables to prototypes
					 */
					ArrayList<Tuple6<String, Boolean, Boolean, Boolean, String, String>> instanceVariableList = null;
					
					try {
						instanceVariableList = ((IActionPackage_ati ) cyanMetaobjectWithAt).ati_instanceVariableList(compilerPackageView_ati);
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						env.thrownException(cyanMetaobjectAnnotation, cyanMetaobjectAnnotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(cyanMetaobject, cyanMetaobjectAnnotation);
					}
										
					
					if ( instanceVariableList != null ) {
						for ( Tuple6<String, Boolean, Boolean, Boolean, String, String> t : instanceVariableList ) {
							String prototypeName = t.f1;
							compilerManager.addInstanceVariable(cyanMetaobjectWithAt, 
										packageName, prototypeName, t.f2, t.f3, t.f4, t.f5, t.f6);
						}
					}
		
					
				}
						
				
			}
				
			
		}
		
		
		// CyanPackage.addCodeAndSlotsTo(attachedMetaobjectAnnotationList, env, compilerPackageView_ati, compilerManager, this.getPackageName());					
		
	}


	
	public int getCompilerVersionLastSuccessfulCompilation() {
		return compilerVersionLastSuccessfulCompilation;
	}

	public void setCompilerVersionLastSuccessfulCompilation(int compilerVersionLastSuccessfulCompilation) {
		this.compilerVersionLastSuccessfulCompilation = compilerVersionLastSuccessfulCompilation;
	}


	public void checkPublicNonGenericProgramUnit() {
		HashSet<String> unitNameSet = new HashSet<>();
		int numRepeated = 0;
		for ( CompilationUnit compilationUnit : compilationUnitList ) {
			ProgramUnit pu = compilationUnit.getPublicPrototype();
			String name = pu.getName();
			if ( unitNameSet.contains(name) ) {
				System.out.println("already in set: " + unitNameSet);   // ok
				++numRepeated;
			}
			else {
				unitNameSet.add(name);
			}
		}
		System.out.println("cunitList size = " + compilationUnitList.size() + " repeated = " + numRepeated);  // ok
	}


	/**
	 * the directory in which will be put the generated Java code for this package
	 */
	private String outputDirectory;
	
	/**
	 * the list of features associated to this package
	 */
	private ArrayList<Tuple2<String, ExprAnyLiteral>> featureList;	
	
	
	private String packageName;
	/**
	 * Compiler options of this package.
	 */
	private CompilerOptions compilerOptions;
	/**
	 * list of compilation units of this package
	 */
	private ArrayList<CompilationUnit> compilationUnitList;
	

	/**
	 * list of compilation units for DSLs of this package
	 */
	private ArrayList<CompilationUnitDSL> compilationUnitDSLList;
	
	
	/**
	 * the project of the whole program
	 */
	private Project project;


	private String packageCanonicalPath;
	
	/**
	 * A list with the metaobjects defined in this package
	 */
	private ArrayList<CyanMetaobject> metaobjectList;


	/**
	 * list of metaobject annotations that precede this package in project .pyan.
	 * These metaobject annotations are from metaobjects that are not attached to 
	 * a declaration.
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList;
	/**
	 * list of metaobject annotations that precede this package in project .pyan
	 * and that are attached to the package.
	 */
	private ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList;
	
	/**
	 * true if this package has any generic prototype. If it has, then
	 * there is a directory "--tmp" inside it with the generic prototype
	 * instantiations
	 */
	private boolean hasGenericPrototype;
	
	/**
	 * Program to which this Cyan package belongs to
	 */
	private Program program;

	/**
	 * compiler version of the last successful compilation of this package
	 */
	private int compilerVersionLastSuccessfulCompilation;

	/**
	 * true if the metaobjects of different prototypes should communicate with each other
	 */
	private Boolean communicateInPackage = null;

	public boolean getCommunicateInPackage() {
		if ( this.communicateInPackage == null ) {
			ExprAnyLiteral any = searchFeature(NameServer.COMMUNICATE_IN_PACKAGE);
			if ( any == null ) {
				communicateInPackage = false;
			}
			else {
				communicateInPackage = any.getJavaValue().equals(NameServer.ON); 	
			}
		}
		return communicateInPackage;
	}

}
