package meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ast.CompilationUnit;
import ast.CompilationUnitSuper;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureOperator;
import ast.MethodSignatureUnary;
import ast.MethodSignatureWithSelectors;
import ast.ObjectDec;
import ast.ProgramUnit;
import ast.SelectorWithParameters;
import error.ErrorKind;
//import meta.*;
import lexer.CompilerPhase;
import lexer.Lexer;
import lexer.Symbol;
import saci.Env;
import saci.NameServer;
import saci.Saci;
import saci.Tuple4;
import saci.Tuple5;
import saci.Tuple6;

public class CompilerManager_ati  {

	public CompilerManager_ati(Env env) {
		this.env = env;
		packagePrototypeMethodMap = new HashMap<String, CyanMetaobjectWithAt>();
		methodToAddList = new ArrayList<Tuple5<CyanMetaobjectWithAt, String, String, String, StringBuffer>>();
		codeToAddList = new ArrayList<>();
		codeToAddAtMetaobjectAnnotationList = new ArrayList<Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer>>();
		packagePrototypeInstanceVariableMap = new HashMap<String, CyanMetaobjectWithAt>();
		instanceVariableToAddList = new ArrayList<Tuple6<CyanMetaobjectWithAt, String, String, String, String, String>>();
		renameMethodList = new ArrayList<>();
		packagePrototypeMethodRenameMap = new HashMap<>();
		beforeMethodToAddList = new ArrayList<>();
	}

	public ArrayList<IPackage_ati> getPackageList() {
		ArrayList<IPackage_ati> ipackage_atiList = new ArrayList<IPackage_ati>();
		for ( CyanPackage cyanPackage : env.getProject().getPackageList() ) {
			IPackage_ati ipackage = new Package_ati(cyanPackage);
			ipackage_atiList.add(ipackage);
		}
		return ipackage_atiList;
	}



	/**
	 * add codeToAdd to prototype prototypeName of package packageName. Metaobject cyanMetaobject asked for that.
	   @param cyanMetaobject
	   @param packageName
	   @param prototypeName
	   @param codeToAdd  code to be added
	   @return <code>true</code> if the method can be added, <code>false</code> otherwise.
	 */
	public boolean addCode( CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName,
			StringBuffer codeToAdd ) {

		packageName = NameServer.removeQuotes(packageName);
		prototypeName = NameServer.removeQuotes(prototypeName);


		codeToAddList.add( new Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer>(
				cyanMetaobject, packageName, prototypeName, codeToAdd) );
		return true;

	}


	/**
	 * add method methodName to prototype prototypeName of package packageName. Metaobject cyanMetaobject asked for that.
	   @param cyanMetaobject
	   @param packageName
	   @param prototypeName
	   @param methodName the name of the method
	   @param methodCode  code of the method to be added
	   @return <code>true</code> if the method can be added, <code>false</code> otherwise.
	 */
	public boolean addMethod( CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName,
			String methodName, StringBuffer methodCode ) {

		packageName = NameServer.removeQuotes(packageName);
		prototypeName = NameServer.removeQuotes(prototypeName);
		methodName = NameServer.removeQuotes(methodName);

		// checkMetaobjectAnnotation(cyanMetaobject, packageName, prototypeName);

		CyanMetaobject previousCyanMetaobject =
				packagePrototypeMethodMap.put(packageName + " " + prototypeName + " " + methodName, cyanMetaobject);
		if ( previousCyanMetaobject == null ) {
			methodToAddList.add( new Tuple5<CyanMetaobjectWithAt, String, String, String, StringBuffer>(
					cyanMetaobject, packageName, prototypeName, methodName, methodCode) );
			return true;
		}
		else {
			/*
			 * previousCyanMetaobject already asked to add a method methodName to packageName.prototypeName.
			 */
			String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
			String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();

			String packageName2 = previousCyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
			String prototypeName2 = previousCyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();

			error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
					cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
					prototypeName1 + " of package " + packageName1 + " is trying to add method " + methodName + " to prototype " +
					prototypeName + " of package " + packageName +
					". However, another metaobject annotation has added the same method to the same prototype. This annotation is in" +
					" line " + previousCyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() +
					" of prototype " +
					prototypeName2 + " of package " + packageName2, methodName,
					ErrorKind.metaobject_attempt_to_add_two_methods_with_the_same_name_to_a_prototype);
			return false;
		}
	}


	/**
	 * add the statements statementCode before the first statement of method methodName of
	 * prototype prototypeName of package packageName. statementCode is added to all methods with name
	 * methodName of the given prototype.  Metaobject cyanMetaobject asked for that.
	   @param cyanMetaobject
	   @param packageName
	   @param prototypeName
	   @param methodName
	   @param statementCode code to be added before the first statement of the method
	   @return <code>true</code> if statementCode can be added, <code>false</code> otherwise.
	*/

	public boolean addBeforeMethod(CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName,
			String methodSignature, StringBuffer statementCode) {

		packageName = NameServer.removeQuotes(packageName);
		prototypeName = NameServer.removeQuotes(prototypeName);
		methodSignature = NameServer.removeQuotes(methodSignature);
		statementCode = new StringBuffer(Lexer.unescapeJavaString(NameServer.removeQuotes(statementCode.toString())));


		// checkMetaobjectAnnotation(cyanMetaobject, packageName, prototypeName);
		beforeMethodToAddList.add(new Tuple5<>(cyanMetaobject, packageName, prototypeName, methodSignature, statementCode));
		return true;
	}


	/*
	@Override
	public boolean addBeforeMethod(CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName,
			String methodName, StringBuffer statementCode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addEndMethod(CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName,
			String methodName, StringBuffer statementCode) {
		// TODO Auto-generated method stub
		return false;
	}
	*/

	public boolean renameMethods(CyanMetaobjectWithAt cyanMetaobjectWithAt, String packageName,
			String prototypeName, String methodName, String[] newMethodSelectors) {

		packageName = NameServer.removeQuotes(packageName);
		prototypeName = NameServer.removeQuotes(prototypeName);
		methodName = NameServer.removeQuotes(methodName);
		for ( int i = 0; i  < newMethodSelectors.length; ++i ) {
			newMethodSelectors[i] = NameServer.removeQuotes(newMethodSelectors[i]);
		}


		CyanMetaobject previousCyanMetaobject = this.packagePrototypeMethodRenameMap.put(
				packageName + " " + prototypeName + " " + methodName, cyanMetaobjectWithAt);
		if ( previousCyanMetaobject == null ) {
			renameMethodList.add( new Tuple5<CyanMetaobjectWithAt, String, String, String, String[]>(
					cyanMetaobjectWithAt, packageName, prototypeName, methodName, newMethodSelectors));
			return true;
		}
		else {
			/*
			 * previousCyanMetaobject already asked to rename the same method
			 */

			String packageName1 = cyanMetaobjectWithAt.getMetaobjectAnnotation().getPackageOfAnnotation();
			String prototypeName1 = cyanMetaobjectWithAt.getMetaobjectAnnotation().getPrototypeOfAnnotation();

			String packageName2 = previousCyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
			String prototypeName2 = previousCyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();

			error(cyanMetaobjectWithAt.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
					cyanMetaobjectWithAt.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
					prototypeName1 + " of package " + packageName1 + " is trying to rename method " + methodName
					+ " of prototype " +
					prototypeName + " of package " + packageName +
					". However, another metaobject annotation has tried to rename the same method of the same prototype. This annotation is in" +
					" line " + previousCyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() +
					" of prototype " +
					prototypeName2 + " of package " + packageName2, methodName,
					ErrorKind.metaobject_error);
			return false;
		}


	}

	/** add instance variable variableName of type variableType to prototype prototypeName of package packageName.
	 *
	   @param cyanMetaobject
	   @param packageName
	   @param prototypeName
	   @param variableType
	   @param variableName
	   @return <code>true</code> if the instance variable can be added, <code>false</code> otherwise.
	 */
	public boolean addInstanceVariable(CyanMetaobjectWithAt cyanMetaobject, String packageName,
			String prototypeName, boolean isPublic, boolean isShared, boolean isReadonly, String variableType, String variableName) {

		packageName = NameServer.removeQuotes(packageName);
		prototypeName = NameServer.removeQuotes(prototypeName);
		variableType = NameServer.removeQuotes(variableType);
		variableName = NameServer.removeQuotes(variableName);
		String qualifiers = "";
		if ( isPublic ) { qualifiers = "public "; }
		if ( isShared ) { qualifiers = qualifiers + "shared "; }
		if ( ! isReadonly ) { qualifiers = qualifiers + "var "; }



		// checkMetaobjectAnnotation(cyanMetaobject, packageName, prototypeName);

		CyanMetaobject previousCyanMetaobject = packagePrototypeInstanceVariableMap.put(packageName + " " + prototypeName + " " + variableName, cyanMetaobject);
		if ( previousCyanMetaobject == null ) {
			instanceVariableToAddList.add( new Tuple6<CyanMetaobjectWithAt, String, String, String, String, String>(
					cyanMetaobject, packageName, prototypeName, qualifiers, variableType, variableName));
			return true;
		}
		else {
			/*
			 * previousCyanMetaobject already asked to add instance variable variableName to packageName.prototypeName.
			 */

			String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
			String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();

			String packageName2 = previousCyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
			String prototypeName2 = previousCyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();

			error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
					cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
					prototypeName1 + " of package " + packageName1 + " is trying to add the instance variable " + variableName
					+ " to prototype " +
					prototypeName + " of package " + packageName +
					". However, another metaobject annotation has added the same instance variable to the same prototype. This annotation is in" +
					" line " + previousCyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() +
					" of prototype " +
					prototypeName2 + " of package " + packageName2, variableName,
					ErrorKind.metaobject_attempt_to_add_two_instance_variables_with_the_same_name_to_a_prototype);
			return false;
		}
	}



	/**
	 * Code <code>codeToAdd</code> should be added after the metaobject annotation <code>cyanMetaobject.getMetaobjectAnnotation()</code>
	   @param cyanMetaobject
	   @param codeToAdd
	   @return <code>false</code> if there was an error
	 */
	public boolean addCodeAtMetaobjectAnnotation(CyanMetaobjectWithAt cyanMetaobject, StringBuffer codeToAdd) {
		/*
		 * 			String cyanMetaobjectName = t.f2;
			String packageName = t.f3;
			String prototypeName = t.f4;
			int lineNumber = t.f5;
		 */
		CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
		codeToAddAtMetaobjectAnnotationList.add(new Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer>(
				cyanMetaobject, cyanMetaobjectAnnotation.getPackageOfAnnotation(),
				cyanMetaobjectAnnotation.getPrototypeOfAnnotation(), codeToAdd));
		return true;
	}






	/**
	 * add to the prototypes of the program all changes demanded by calls to methods #addMethod, #addInstanceVariable, etc.
	 * And do all checks demanded by the metaobjects of the program.
	 *
	   @return <code>false</code> in error, <code>true</code> otherwise
	 */
	public boolean changeCheckProgram() {

		/**
		 * for each compilation unit that should be changed by metaobjects or by adding "#ati" to metaobject annotations,
		 * there is associated list of code changes
		 */
		HashMap<CompilationUnitSuper, ArrayList<SourceCodeChangeByMetaobjectAnnotation>> setOfChanges = new HashMap<>();
		/**
		 * set of prefixes of metaobject annotations to change. This variable is necessary because a
		 * metaobject annotation may insert, for example, a method and an instance variable. So the change of the prefix
		 * could be inserted twice in the set <code>setOfChanges</code>.
		 */
		HashMap<CompilationUnitSuper, ArrayList<Integer>> setOfSuffixToChange = new HashMap<>();
		  /*
		   * set with elements "packageName prototypeName" such that the prototype of the package
		   * has been changed during the compiler phase "ati". Then these prototypes should be
		   * compiled again.
		   */
		// compilationUnitChangedSet = new HashSet<CompilationUnit>();
		ProgramUnit pu;
		CompilationUnit compUnit;
		String packageName, prototypeName;



		for ( Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer> t : codeToAddList ) {
			CyanMetaobjectWithAt cyanMetaobject = t.f1;
			packageName = t.f2;
			prototypeName = t.f3;
			StringBuffer codeToAdd = t.f4;
			pu = env.searchPackagePrototype(packageName, prototypeName);
			if ( pu == null ) {
				String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
				String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
				error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
						cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
						prototypeName1 + " of package " + packageName1 + " is trying to add code to prototype " +
						prototypeName + " of package " + packageName +
						". However, there is no package " + packageName + " or no prototype " +
						prototypeName + " in this package",  null,
						ErrorKind.metaobject_error);
			}
			else {
				compUnit = pu.getCompilationUnit();
				/**
				 * it is necessary to find where the add the code. If there is no method with the same name,
				 * the method can be added at the 'end' symbol of the prototype. Otherwise the method
				 * should be added before the first method with the same name.
				 */

				ObjectDec objDec = (ObjectDec ) pu;
				int smallerOffset = objDec.getEndSymbol().getOffset();


				/*
				 * add change to the list of changes
				 */
				String codeToAddWithContext = Env.getCodeToAddWithContext(cyanMetaobject,
						"\n" + codeToAdd + "\n", null);
				addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(smallerOffset, new StringBuffer(codeToAddWithContext),
						cyanMetaobject.getMetaobjectAnnotation()));

				CyanMetaobjectWithAtAnnotation annotation = cyanMetaobject.getMetaobjectAnnotation();
				if ( ! annotation.getInsideProjectFile() ) {
					/*
					 * metaobject annotation is inside a regular .cyan file
					 */
					ProgramUnit puMetaobjectAnnotation = env.searchPackagePrototype(annotation.getPackageOfAnnotation(),
							cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation());
					if ( puMetaobjectAnnotation == null )
						error(null, "Internal error: I cannot find a metaobject annotation of package " +
					            cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation() + " and prototype" +
								cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation(),
								cyanMetaobject.getMetaobjectAnnotation().toString(), ErrorKind.metaobject_error);
					else {
						/*
						 * the metaobject annotation will be changed to use the suffix "ati". Then all
						 * metaobject annotations should be collected
						 */
						addSuffixToChange(setOfSuffixToChange, puMetaobjectAnnotation.getCompilationUnit(), cyanMetaobject);
					}

				}

			}
		}



		for ( Tuple6<CyanMetaobjectWithAt, String, String, String, String, String> t : instanceVariableToAddList ) {
			CyanMetaobjectWithAt cyanMetaobject = t.f1;
			packageName = t.f2;
			prototypeName = t.f3;
			// String qualifiers = t.f4;
			String instanceVariableType = t.f5;
			String instanceVariableName = t.f6;
			pu = env.searchPackagePrototype(packageName, prototypeName);

			/*
			 * the instance variable is to be added to <code>pu</code> before the 'end' symbol.
			 * The metaobject annotation that asked for this inclusion is in package
			 *     			cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation()
			 *  and prototype
			 *              cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation()

			 */
			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
			if ( pu == null ) {
				if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
							env.getProject().getProjectName() + " is trying to add instance variable '"
							+ instanceVariableName + "' to prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  instanceVariableName,
							ErrorKind.metaobject_error);

				}
				else {
					String packageName1 = cyanMetaobjectAnnotation.getPackageOfAnnotation();
					String prototypeName1 = cyanMetaobjectAnnotation.getPrototypeOfAnnotation();
					error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of prototype " +
							prototypeName1 + " of package " + packageName1 + " is trying to add instance variable '"
							+ instanceVariableName + "' to prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  instanceVariableName,
							ErrorKind.metaobject_error);

				}
			}
			else {
				compUnit = pu.getCompilationUnit();
				/*
				 * add change to the list of changes
				 */
				String codeToAddWithContext = Env.getCodeToAddWithContext(cyanMetaobject,
						"\n    " + instanceVariableType + " " + instanceVariableName + "\n", null);
				addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(pu.getEndSymbol().getOffset(),
						new StringBuffer(codeToAddWithContext), cyanMetaobjectAnnotation)
				);

				if ( ! cyanMetaobjectAnnotation.getInsideProjectFile() )  {
					ProgramUnit puMetaobjectAnnotation = env.searchPackagePrototype(cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation(),
							cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation());
					if ( puMetaobjectAnnotation == null )
						error(null, "Internal error: I cannot find a metaobject annotation of package " +
					            cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation() + " and prototype" +
								cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation(),
								cyanMetaobject.getMetaobjectAnnotation().toString(), ErrorKind.metaobject_error);
					else {
						/*
						 * the metaobject annotation will be changed to use the suffix "ati". Then all
						 * metaobject annotations should be collected
						 */
						addSuffixToChange(setOfSuffixToChange, puMetaobjectAnnotation.getCompilationUnit(), cyanMetaobject);
					}
				}
				// compilationUnitChangedSet.add(compUnit);

			}
		}
		for ( Tuple5<CyanMetaobjectWithAt, String, String, String, StringBuffer> t : methodToAddList ) {
			CyanMetaobjectWithAt cyanMetaobject = t.f1;
			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
			packageName = t.f2;
			prototypeName = t.f3;
			String methodName = t.f4;
			StringBuffer methodCode = t.f5;
			pu = env.searchPackagePrototype(packageName, prototypeName);
			if ( pu == null ) {

				if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
							env.getProject().getProjectName() + " is trying to add method " + methodName
							+ " to prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  methodName,
							ErrorKind.metaobject_error);
				}
				else {
					String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
					String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
					error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
							prototypeName1 + " of package " + packageName1 + " is trying to add method " + methodName
							+ " to prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  methodName,
							ErrorKind.metaobject_error);

				}
			}
			else {
				compUnit = pu.getCompilationUnit();
				/**
				 * it is necessary to find where the add the method. If there is no method with the same name,
				 * the method can be added at the 'end' symbol of the prototype. Otherwise the method
				 * should be added before the first method with the same name.
				 */

				ObjectDec objDec = (ObjectDec ) pu;
				ArrayList<MethodSignature> sameNameMethodList = objDec.searchMethodPrivateProtectedPublic(methodName);
				int smallerOffset;
				if ( sameNameMethodList == null || sameNameMethodList.size() == 0 ) {
					smallerOffset = objDec.getEndSymbol().getOffset();
				}
				else {
					// method 'methodName' should be added before the first method of this list
					smallerOffset = sameNameMethodList.get(0).getMethod().getFirstSymbol().getOffset();
					for ( int i = 1; i < sameNameMethodList.size(); ++i ) {
						if ( sameNameMethodList.get(i).getMethod().getFirstSymbol().getOffset() < smallerOffset )
							smallerOffset = sameNameMethodList.get(i).getFirstSymbol().getOffset();
					}
				}



				/*
				 * add change to the list of changes
				 */
				// CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
				String codeToAddWithContext = Env.getCodeToAddWithContext(cyanMetaobject,
						"\n" + methodCode + "\n", null);
				addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(smallerOffset, new StringBuffer(codeToAddWithContext),
						cyanMetaobject.getMetaobjectAnnotation()));


				if ( ! cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					ProgramUnit puMetaobjectAnnotation = env.searchPackagePrototype(cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation(),
							cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation());
					if ( puMetaobjectAnnotation == null )
						error(null, "Internal error: I cannot find a metaobject annotation of package " +
					            cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation() + " and prototype" +
								cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation(),
								cyanMetaobject.getMetaobjectAnnotation().toString(), ErrorKind.metaobject_error);
					else {
						/*
						 * the metaobject annotation will be changed to use the suffix "ati". Then all
						 * metaobject annotations should be collected
						 */
						addSuffixToChange(setOfSuffixToChange, puMetaobjectAnnotation.getCompilationUnit(), cyanMetaobject);
					}

				}


				// compilationUnitChangedSet.add(pu.getCompilationUnit());
			}
		}

		for ( Tuple5<CyanMetaobjectWithAt, String, String, String, StringBuffer> t : beforeMethodToAddList) {
			CyanMetaobjectWithAt cyanMetaobject = t.f1;
			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
			packageName = t.f2;
			prototypeName = t.f3;
			String methodName = t.f4;
			StringBuffer statementsCode = t.f5;
			pu = env.searchPackagePrototype(packageName, prototypeName);
			if ( pu == null ) {

				if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
							env.getProject().getProjectName() + " is trying to add statements to method " + methodName
							+ " of prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  methodName,
							ErrorKind.metaobject_error);

				}
				else {


					String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
					String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
					error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
							prototypeName1 + " of package " + packageName1 + " is trying to add statements to method " + methodName
							+ " of prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  methodName,
							ErrorKind.metaobject_error);
				}
			}
			else {
				compUnit = pu.getCompilationUnit();

				ObjectDec objDec = (ObjectDec ) pu;
				ArrayList<MethodSignature> sameNameMethodList = objDec.searchMethodPrivateProtectedPublic(methodName);
				if ( sameNameMethodList == null || sameNameMethodList.size() == 0 ) {

					if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
						error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
								cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
								env.getProject().getProjectName() + " is trying to add statements to method " + methodName
								+ " of prototype " +
								prototypeName + " of package " + packageName +
								". However, there is no such method", methodName,
								ErrorKind.metaobject_error);
					}
					else {
						String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
						String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
						error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
								cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
								prototypeName1 + " of package " + packageName1 + " is trying to add statements to method " + methodName
								+ " of prototype " +
								prototypeName + " of package " + packageName +
								". However, there is no such method", methodName,
								ErrorKind.metaobject_error);

					}
				}
				else {
					/*
					 * add the statements to all methods with name 'methodName'
					 */
					for ( MethodSignature ms : sameNameMethodList ) {
						MethodDec aMethod = ms.getMethod();
						if ( aMethod.getLeftCBsymbol() != null ) {
							/*
							 * method has a '{' after its signature, possibly followed by statements till '}'.
							 * Some methods are different, they have a '=' followed by an expression.
							 * For example, <br>
							 * <code>
							 * func zero -> Int = 0 <br>
							 * </code>
							 * These methods are not changed
							 */
							int offsetFirstStat = aMethod.getLeftCBsymbol().getOffset() + 1;
							/*
							 * add change to the list of changes
							 */
							String codeToAddWithContext = Env.getCodeToAddWithContext(cyanMetaobject,
									"\n" + statementsCode + "\n", null);
							addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(offsetFirstStat, new StringBuffer(codeToAddWithContext),
									cyanMetaobject.getMetaobjectAnnotation()));

							if ( ! cyanMetaobjectAnnotation.getInsideProjectFile() ) {

								ProgramUnit puMetaobjectAnnotation = env.searchPackagePrototype(cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation(),
										cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation());
								if ( puMetaobjectAnnotation == null )
									error(null, "Internal error: I cannot find a metaobject annotation of package " +
								            cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation() + " and prototype" +
											cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation(),
											cyanMetaobject.getMetaobjectAnnotation().toString(), ErrorKind.metaobject_error);
								else {
									/*
									 * the metaobject annotation will be changed to use the suffix "ati". Then all
									 * metaobject annotations should be collected
									 */
									addSuffixToChange(setOfSuffixToChange, puMetaobjectAnnotation.getCompilationUnit(), cyanMetaobject);
								}

							}
						}
					}

					// compilationUnitChangedSet.add(pu.getCompilationUnit());
				}
			}
		}

		for ( Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer> t : codeToAddAtMetaobjectAnnotationList ) {
			CyanMetaobjectWithAt cyanMetaobject = t.f1;
			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();

			packageName = t.f2;
			prototypeName = t.f3;
			StringBuffer codeToAdd = t.f4;

			pu = env.searchPackagePrototype(packageName, prototypeName);

			if ( pu == null ) {
				if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					error(  cyanMetaobjectAnnotation.getFirstSymbol(), "Internal error: metaobject annotation of line " +
							cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
							env.getProject().getProjectName() +  " does not exist anymore", null,
							ErrorKind.metaobject_error );
				}
				else {
					String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
					String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
					error( cyanMetaobjectAnnotation.getFirstSymbol(), "Internal error: metaobject annotation of line " +
							cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
							prototypeName1 + " of package " + packageName1 + " does not exist anymore", null,
							ErrorKind.metaobject_error );
				}
			}
			else {
				compUnit = pu.getCompilationUnit();
				/*
				 * add change to the list of changes
				 */
				String codeToAddWithContext = Env.getCodeToAddWithContext(cyanMetaobject,
						codeToAdd + " ", null);
				addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(
						cyanMetaobjectAnnotation.getNextSymbol().getOffset(),
						new StringBuffer(codeToAddWithContext), cyanMetaobject.getMetaobjectAnnotation()));

				if ( ! cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					/*
					 * the metaobject annotation will be changed to use the suffix "ati". Then all
					 * metaobject annotations should be collected
					 */
					addSuffixToChange(setOfSuffixToChange, pu.getCompilationUnit(), cyanMetaobject);

				}
				// compilationUnitChangedSet.add(pu.getCompilationUnit());
			}


		}

		for ( Tuple5<CyanMetaobjectWithAt, String, String, String, String[]> t : renameMethodList ) {



			CyanMetaobjectWithAt cyanMetaobject = t.f1;
			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
			packageName = t.f2;
			prototypeName = t.f3;
			String oldMethodName = t.f4;
			String[] selectorNames = t.f5;
			pu = env.searchPackagePrototype(packageName, prototypeName);
			if ( pu == null ) {

				if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
					error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
							env.getProject().getProjectName() + " is trying to rename " + oldMethodName
							+ " of prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  oldMethodName,
							ErrorKind.metaobject_error);

				}
				else {


					String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
					String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
					error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
							cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
							prototypeName1 + " of package " + packageName1 + " is trying rename method " + oldMethodName
							+ " of prototype " +
							prototypeName + " of package " + packageName +
							". However, there is no package " + packageName + " or no prototype " +
							prototypeName + " in this package",  oldMethodName,
							ErrorKind.metaobject_error);
				}
			}
			else {
				compUnit = pu.getCompilationUnit();

				ObjectDec objDec = (ObjectDec ) pu;
				ArrayList<MethodSignature> sameNameMethodList = objDec.searchMethodPrivateProtectedPublic(oldMethodName);
				if ( sameNameMethodList == null || sameNameMethodList.size() == 0 ) {

					if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
						error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
								cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
								env.getProject().getProjectName() + " is trying rename method " + oldMethodName
								+ " of prototype " +
								prototypeName + " of package " + packageName +
								". However, there is no such method", oldMethodName,
								ErrorKind.metaobject_error);
					}
					else {
						String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
						String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
						error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
								cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
								prototypeName1 + " of package " + packageName1 + " is trying to rename method " + oldMethodName
								+ " of prototype " +
								prototypeName + " of package " + packageName +
								". However, there is no such method", oldMethodName,
								ErrorKind.metaobject_error);

					}
				}
				else {
					/*
					 * rename all methods with name oldMethodName
					 */
					for ( MethodSignature ms : sameNameMethodList ) {
						if ( ms instanceof MethodSignatureWithSelectors ) {
							MethodSignatureWithSelectors realMS = (MethodSignatureWithSelectors ) ms;
							ArrayList<SelectorWithParameters> selecWithParametersList = realMS.getSelectorArray();
							// nao era feito na versao anterior
							//System.out.println("fau: " + selectorNames.length + " " + selecWithParametersList.size() );
							/*if ( selectorNames.length != selecWithParametersList.size() ) {
								String newMethodName = "";
								for ( String s : selectorNames ) {
									newMethodName += s;
								}
								if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
									error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
											cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
											env.getProject().getProjectName() + " is trying to rename " + oldMethodName
											+ " of prototype " +
											prototypeName + " of package " + packageName +
											". However, the number of selectors of the old and new methods are different: " +
											oldMethodName + " (old, " + selecWithParametersList.size() + " selectors) " +
											newMethodName + " (new, " + selectorNames.length + " selectors)",  oldMethodName,
											ErrorKind.metaobject_error);

								}
								else {
									String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
									String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
									error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
											cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
											prototypeName1 + " of package " + packageName1 + " is trying rename method " + oldMethodName
											+ " of prototype " +
											prototypeName + " of package " + packageName +
											". However, the number of selectors of the old and new methods are different: " +
											oldMethodName + " (old, " + selecWithParametersList.size() + " selectors) " +
											newMethodName + " (new, " + selectorNames.length + " selectors)",  oldMethodName,
											ErrorKind.metaobject_error);
								}

							}*/
							int i = 0;
							for ( SelectorWithParameters selec : selecWithParametersList ) {
								int offsetFirstStat = selec.getSelector().getOffset();
								addChange(setOfChanges, compUnit, new SourceCodeChangeDeleteText(
										offsetFirstStat, selec.getSelector().getSymbolString().length(),
										cyanMetaobject.getMetaobjectAnnotation()));
								String selector = selectorNames[i];
								if ( ! selector.endsWith(":") ) {
									selector += ":";
								}
								addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(offsetFirstStat,
										new StringBuffer(selector),
										cyanMetaobject.getMetaobjectAnnotation()));
								++i;
							}
						}
						else if ( ms instanceof MethodSignatureOperator ) {
							MethodSignatureOperator realMS = (MethodSignatureOperator ) ms;
							boolean signError = selectorNames.length != 1;
							signError = signError || (selectorNames.length > 1);
							/*
							 * many more checkings should be made.
							 */

							if ( signError ) {
								String newMethodName = "";
								for ( String s : selectorNames ) {
									newMethodName += s;
								}
								if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
									error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
											cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
											env.getProject().getProjectName() + " is trying to rename " + oldMethodName
											+ " of prototype " +
											prototypeName + " of package " + packageName +
											". However, the old method name and the new name are incompatible: " +
											oldMethodName + " (old) " +
											newMethodName + " (new)",  oldMethodName,
											ErrorKind.metaobject_error);

								}
								else {
									String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
									String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
									error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
											cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
											prototypeName1 + " of package " + packageName1 + " is trying rename method " + oldMethodName
											+ " of prototype " +
											prototypeName + " of package " + packageName +
											". However, the old method name and the new name are incompatible: " +
											oldMethodName + " (old) " +
											newMethodName + " (new)",  oldMethodName,
											ErrorKind.metaobject_error);
								}

							}
							Symbol unarySymbol = realMS.getFirstSymbol();
							int offsetFirstStat = unarySymbol.getOffset();
							addChange(setOfChanges, compUnit, new SourceCodeChangeDeleteText(
									offsetFirstStat, unarySymbol.getSymbolString().length(),
									cyanMetaobject.getMetaobjectAnnotation()));
							addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(offsetFirstStat,
									new StringBuffer(selectorNames[0]),
									cyanMetaobject.getMetaobjectAnnotation()));




						}
						else if ( ms instanceof MethodSignatureUnary ) {
							MethodSignatureUnary realMS = (MethodSignatureUnary )  ms;
							if ( selectorNames.length != 1 || (selectorNames.length > 0 && selectorNames[0].endsWith(":")) ) {
								String newMethodName = "";
								for ( String s : selectorNames ) {
									newMethodName += s;
								}
								if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
									error(  cyanMetaobjectAnnotation.getFirstSymbol(), "metaobject annotation of line " +
											cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + " of the project file " +
											env.getProject().getProjectName() + " is trying to rename " + oldMethodName
											+ " of prototype " +
											prototypeName + " of package " + packageName +
											". However, the old method is unary and the new name is from a non-unary method: " +
											oldMethodName + " (old) " +
											newMethodName + " (new)",  oldMethodName,
											ErrorKind.metaobject_error);

								}
								else {
									String packageName1 = cyanMetaobject.getMetaobjectAnnotation().getPackageOfAnnotation();
									String prototypeName1 = cyanMetaobject.getMetaobjectAnnotation().getPrototypeOfAnnotation();
									error(cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol(), "metaobject annotation of line " +
											cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getLineNumber() + " of prototype " +
											prototypeName1 + " of package " + packageName1 + " is trying rename method " + oldMethodName
											+ " of prototype " +
											prototypeName + " of package " + packageName +
											". However, the old method is unary and the new name is from a non-unary method: " +
											oldMethodName + " (old) " +
											newMethodName + " (new)",  oldMethodName,
											ErrorKind.metaobject_error);
								}

							}
							Symbol unarySymbol = realMS.getFirstSymbol();
							int offsetFirstStat = unarySymbol.getOffset();
							addChange(setOfChanges, compUnit, new SourceCodeChangeDeleteText(
									offsetFirstStat, unarySymbol.getSymbolString().length(),
									cyanMetaobject.getMetaobjectAnnotation()));
							addChange(setOfChanges, compUnit, new SourceCodeChangeAddText(offsetFirstStat,
									new StringBuffer(selectorNames[0]),
									cyanMetaobject.getMetaobjectAnnotation()));


						}
					}

					// compilationUnitChangedSet.add(pu.getCompilationUnit());
				}
			}



		}
		/**
		 * changeList has already all changes demanded by the metaobjects such as add methods and
		 * instance variables to prototypes.
		 * The code below adds the phase changes to the metaobject annotations. That is, a metaobject annotation
		 *           @init(name)
		 *  should be changed to
		 *           @init#ati(name)
		 *  The phase changes are in map setOfPrefixToChange.
		 *  The code below just add these changes to the list setOfChanges.
		 */
		for ( Map.Entry<CompilationUnitSuper, ArrayList<Integer>> entry: setOfSuffixToChange.entrySet() )  {
			  // the compilation unit in which the metaobject annotation is
			CompilationUnitSuper compUnitEntry = entry.getKey();
			  // the list of offsets of the metaobject annotations inside the compilation unit
			ArrayList<Integer> offsetList = entry.getValue();
			   // the changes already collected for this compilation unit
			ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compUnitEntry);
			if ( changeList == null ) {
				  // there were no changes for this compilation unit. Add one list of changes, empty
				changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
				setOfChanges.put(compUnitEntry, changeList);
			}
			   // for each metaobject annotation of this compilation unit, add a change to changeList
			for ( Integer offset : offsetList ) {
				changeList.add(new SourceCodeChangeShiftPhase(compUnitEntry.getText(), env, offset, CompilerPhase.ATI, compUnitEntry,
						null));
			}
		}



		Saci.makeChanges(setOfChanges, env);


		return true;
	}


	static private void addChange(HashMap<CompilationUnitSuper, ArrayList<SourceCodeChangeByMetaobjectAnnotation>> setOfChanges,
			CompilationUnit compUnit, SourceCodeChangeByMetaobjectAnnotation change) {

		ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compUnit);
		if ( changeList == null ) {
			changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
			setOfChanges.put(compUnit, changeList);
		}
		changeList.add(change);
	}

	private static void addSuffixToChange(
			HashMap<CompilationUnitSuper, ArrayList<Integer>> setOfPrefixToChange,
			CompilationUnit compUnit,
			CyanMetaobject cyanMetaobject) {

		int newOffset = cyanMetaobject.metaobjectAnnotation.getFirstSymbol().getOffset();
		ArrayList<Integer> offsetList = setOfPrefixToChange.get(compUnit);
		if ( offsetList == null ) {
			offsetList = new ArrayList<>();
			offsetList.add(newOffset);
			setOfPrefixToChange.put(compUnit, offsetList);
		}
		else {
			/**
			 * two Integers with the same value are
			 * considered different. Then we have to search in offsetList for
			 * an integer with value equal to newOffset
			 */
			boolean found = false;
			for (Integer offset : offsetList ) {
				if ( offset == newOffset ) {
					found = true;
					break;
				}
			}
			if ( ! found )
				offsetList.add(newOffset);
		}
	}


	/**
	 * add the statements statementCode after the last statement of method methodName of
	 * prototype prototypeName of package packageName. statementCode is added to all methods with name
	 * methodName of the given prototype.  Metaobject cyanMetaobject asked for that.
	   @param cyanMetaobject
	   @param packageName
	   @param prototypeName
	   @param methodName
	   @param statementCode code to be added after the last statement of the method
	   @return <code>true</code> if statementCode can be added, <code>false</code> otherwise.
	 *
	boolean addEndMethod(       CyanMetaobjectWithAt cyanMetaobject, String packageName, String prototypeName, String methodName, StringBuffer statementCode);
	*/


	/*
	public HashSet<CompilationUnit> getCompilationUnitChangedSet() {
		return compilationUnitChangedSet;
	} */

	public void error(Symbol sym, String specificMessage, String identifier,
			ErrorKind errorKind, String... furtherArgs) {
		env.error(true, sym, specificMessage, identifier, errorKind, furtherArgs);
	}


	/**
	 * map containing pairs <code>(s, m)</code> in which <code>s</code> is a string of the form
	 * <code>"packageName prototypeName methodName"</code> and <code>m</code> is a metaobject.
	 * Metaobject <code>m</code> asked to add method <code>methodName</code> to prototype
	 * <code>prototypeName</code> of package <code>packageName</code>. This map is used to check
	 * whether two different metaobjects try to add the same method to a prototype. This is illegal.
	 * A method name is just a concatenation of the method selectors such as "at:put:".
	 */
	private HashMap<String, CyanMetaobjectWithAt> packagePrototypeMethodMap;


	/**
	 * list of tuples containing a metaobject, package name, a prototype name, and a source code
	 * that should be inserted in the prototype
	*/
	private ArrayList<Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer>> codeToAddList;

	/**
	 * list of tuples containing a metaobject, package name, a prototype name, method name, and the source code of a method
	 * that should be inserted in the prototype
	*/
	private ArrayList<Tuple5<CyanMetaobjectWithAt, String, String, String, StringBuffer>> methodToAddList;

	/**
	 * list of tuples containing a metaobject, package name, a prototype name, a method signature (with
	 * full name of the parameters), and the source code of a method
	 * that should be inserted before the first statement of the method.
	*/
	private ArrayList<Tuple5<CyanMetaobjectWithAt, String, String, String, StringBuffer>> beforeMethodToAddList;

	/**
	 * list of tuples containing a metaobject, a package name, a prototype name, and a source code to be
	 * added after the metaobject annotation.
	 */
	private ArrayList<Tuple4<CyanMetaobjectWithAt, String, String, StringBuffer>> codeToAddAtMetaobjectAnnotationList;

	/**
	 * map containing pairs <code>(s, m)</code> in which <code>s</code> is a string of the form
	 * <code>packageName prototypeName instanceVariableName</code> and <code>m</code> is a metaobject.
	 * Metaobject <code>m</code> asked to add instance variable <code>instanceVariableName</code> to prototype
	 * <code>prototypeName</code> of package <code>packageName</code>. This map is used to check
	 * whether two different metaobjects try to add the same instance variable to a prototype. This is illegal.
	 */
	private HashMap<String, CyanMetaobjectWithAt> packagePrototypeInstanceVariableMap;

	/**
	 * map containing pairs <code>(s, m)</code> in which <code>s</code> is a string of the form
	 * <code>packageName prototypeName methodName</code> and <code>m</code> is a metaobject.
	 * Metaobject <code>m</code> asked to rename method methodname of prototype
	 * <code>prototypeName</code> of package <code>packageName</code>. This map is used to check
	 * whether two different metaobjects try to rename the same method of a prototype. This is illegal.
	 */
	private HashMap<String, CyanMetaobjectWithAt> packagePrototypeMethodRenameMap;
	/**
	 * list of tuples containing a metaobject, package name, a prototype name, qualifiers (public, private, shared, let, var), a type name, and an instance variable name
	 * that should be inserted in this prototype
	*/
	private ArrayList<Tuple6<CyanMetaobjectWithAt, String, String, String, String, String>> instanceVariableToAddList;

	/**
	 * list of tuples containing a metaobject, package name, a prototype name,
	 * old method name (with the parameters like 'at:1 with:2'), and an array with the new selector names
	 *
	*/

	private ArrayList<Tuple5<CyanMetaobjectWithAt, String, String, String, String[]>> renameMethodList;
	  /*
	   * set with the compilation units that have been changed during the compiler phase "ati".
	   * These compilation units should be compiled again.
	   * /
	private HashSet<CompilationUnit> compilationUnitChangedSet; */



	private Env env;

}
