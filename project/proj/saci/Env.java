/**
 *
 */

package saci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import ast.CodeWithError;
import ast.CompilationUnit;
import ast.CompilationUnitSuper;
import ast.ContextParameter;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectMacroCall;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.Expr;
import ast.ExprFunction;
import ast.ExprIdentStar;
import ast.ExprMessageSend;
import ast.GenericParameter;
import ast.INextSymbol;
import ast.InstanceVariableDec;
import ast.InterfaceDec;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureOperator;
import ast.MethodSignatureUnary;
import ast.MethodSignatureWithSelectors;
import ast.ObjectDec;
import ast.PWCharArray;
import ast.ParameterDec;
import ast.ProgramUnit;
import ast.SelectorWithParameters;
import ast.SlotDec;
import ast.Statement;
import ast.Type;
import ast.VariableDecInterface;
import error.CompileErrorException;
import error.ErrorKind;
import error.UnitError;
import lexer.CompilerPhase;
import lexer.Lexer;
import lexer.Symbol;
import lexer.Token;
import meta.CyanMetaobject;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectLiteralString;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.IEnv_ati;
import meta.SourceCodeChangeAddText;
import meta.SourceCodeChangeByMetaobjectAnnotation;
import meta.SourceCodeChangeDeleteText;
import meta.SourceCodeChangeShiftPhase;

/**
 * This class provides the environment in which a semantic analysis is made.
 * That is, is provides the symbol table and an error list.
 *
 * An object of this class is passed as a parameter to every method that does
 * semantic analysis. Every object of the ast passes it way down in the
 * hierarchy, updating the symbol table appropriately. For example, if a method
 * public void check(Env env) of class MethodDec does semantic analysis, it adds
 * to the symbol table the parameters of the method (and at the end it removes
 * them). Parameter env is passed to "check" methods of objects referenced by
 * "this" that do semantic analysis.
 *
 * It is also used for collecting all instantiations of generic objects.
 *
 * @author José
 *
 */
public class Env implements Cloneable, IEnv_ati {

	public Env(Project project) {
		this.project = project;
		publicSourceFileNameTable = new Hashtable<String, ProgramUnit>();
		publicProgramUnitTable = new Hashtable<String, ProgramUnit>();
		conflictProgramUnitTable = new Hashtable<String, CyanPackage>();
		privateProgramUnitTable = new Hashtable<String, ProgramUnit>();
		slotDecTable = new Hashtable<String, SlotDec>();
		variableDecTable = new Hashtable<String, VariableDecInterface>();
		functionList = new ArrayList<ExprFunction>();
		variableDecStack = new Stack<VariableDecInterface>();
		functionStack = new Stack<ExprFunction>();
		statementStack = new Stack<CodeWithError>();
		enclosingObjectDec = null;
		thereWasError.elem = false;
		compInstSet = new HashSet<>();
		this.setOfChanges = new HashMap<>();
		cyanMetaobjectCompilationContextStack = new Stack<>();
		lineMessageList = null;
		metaobjectAnnotationParseWithCompilerStack = new Stack<>();
		compilationUnitToWriteList = null;
		stackVariableLevel = new Stack<>();
		creatingInnerPrototypesInsideEval = false;
		offsetPushCompilationContextStack = new Stack<>();
		lineShift = 0;
		mapPackageSpacePrototypeNameToSubprototypeList = null;
		mapPackageSpaceInterfaceNameToSubinterfaceList = null;
		mapPackageSpaceInterfaceNameToImplementedList = null;
		mapCompUnitErrorList = null;
		/*
																 * if an
																 * instance
																 * variable is
																 * added here,
																 * maybe it
																 * should be
																 * added to the
																 * clone method
																 * too.
																 */
	}

	@SuppressWarnings("unchecked")
	@Override
	public Env clone() {

		try {
			Env newObj = (Env) super.clone();

			newObj.stackVariableLevel = (Stack<Tuple3<VariableDecInterface, String, Integer>>) newObj.stackVariableLevel
					.clone();
			if ( newObj.cyException != null ) {
				newObj.cyException = newObj.cyException.clone();
			}
			newObj.variableDecTable = (Hashtable<String, VariableDecInterface>) newObj.variableDecTable.clone();
			newObj.variableDecStack = (Stack<VariableDecInterface>) newObj.variableDecStack.clone();
			newObj.slotDecTable = (Hashtable<String, SlotDec>) newObj.slotDecTable.clone();
			newObj.privateProgramUnitTable = (Hashtable<String, ProgramUnit>) newObj.privateProgramUnitTable.clone();
			newObj.publicProgramUnitTable = (Hashtable<String, ProgramUnit>) newObj.publicProgramUnitTable.clone();
			newObj.conflictProgramUnitTable = (Hashtable<String, CyanPackage>) newObj.conflictProgramUnitTable.clone();
			newObj.publicSourceFileNameTable = (Hashtable<String, ProgramUnit>) newObj.publicSourceFileNameTable
					.clone();
			if ( newObj.genericPrototypeFormalParameterTable != null ) {
				newObj.genericPrototypeFormalParameterTable = (Hashtable<String, GenericParameter>) newObj.genericPrototypeFormalParameterTable
						.clone();
			}
			// do not clone currentCompilationUnit
			/*
			 * if ( newObj.currentCompilationUnit != null ) {
			 * newObj.currentCompilationUnit =
			 * newObj.currentCompilationUnit.clone(); }
			 */
			if ( newObj.currentProgramUnit != null ) {
				newObj.currentProgramUnit = newObj.currentProgramUnit.clone();
			}
			newObj.functionList = (ArrayList<ExprFunction>) newObj.functionList.clone();
			newObj.statementStack = (Stack<CodeWithError>) newObj.statementStack.clone();
			newObj.functionStack = (Stack<ExprFunction>) newObj.functionStack.clone();
			newObj.cyanMetaobjectCompilationContextStack = (Stack<Tuple6<String, String, String, String, Integer, Integer>>) newObj.cyanMetaobjectCompilationContextStack
					.clone();
			newObj.metaobjectAnnotationParseWithCompilerStack = (Stack<CyanMetaobjectAnnotation>) newObj.metaobjectAnnotationParseWithCompilerStack
					.clone();
			if ( newObj.compilationUnitToWriteList != null ) {
				newObj.compilationUnitToWriteList = (HashSet<CompilationUnit>) newObj.compilationUnitToWriteList
						.clone();
			}
			newObj.offsetPushCompilationContextStack = (Stack<Integer>) newObj.offsetPushCompilationContextStack
					.clone();
			if ( newObj.programUnitForGenericPrototypeList != null ) {
				newObj.programUnitForGenericPrototypeList = (ArrayList<ProgramUnit>) newObj.programUnitForGenericPrototypeList
						.clone();
			}

			return newObj;
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * number used to generate unique identifiers in Cyan programs
	 */
	private static int magicIdNumber = 1;

	/**
	 * unique name for local variable, shared variable or instance variable of
	 * the current prototype
	 *
	 * @return
	 */
	public String getNewUniqueVariableName() {
		if ( this.currentCompilationUnit == null || this.currentProgramUnit == null ) {
			return null;
		}
		else {
			String name;
			while (true) {
				name = "var" + magicIdNumber++;
				if ( this.currentMethod == null ) {
					if ( this.searchInstanceVariable(name) == null ) break;
				}
				else {
					if ( this.searchLocalVariableParameter(name) == null && this.searchInstanceVariable(name) == null )
						break;
				}
			}
			return name;
		}
	}

	/**
	 * execute the dsa actions asked through the call to methods
	 * {@link #addCodeAtMetaobjectAnnotation(CyanMetaobject, StringBuffer, boolean)},
	 * {@link #removeCodeMetaobjectAnnotation(CyanMetaobject)}, and
	 * {@link #addSuffixToChange(CyanMetaobject)}.
	 */
	public void dsa_actions() {
		Saci.makeChanges(setOfChanges, this);
	}

	/**
	 * add code produced by <code>cyanMetaobject</code> after the call of one of the metaobject methods.
	 * It is assumed that the annotation of the metaobject is in the current compilation unit.
	 * The code <code>codeToAdd</code> is added at offset
	 * <code>offsetToAdd</code> in the compilation unit of the metaobject annotation.
	 *
	 * @param cyanMetaobject
	 * @param codeToAdd
	 * @return
	 */
	public boolean addCodeAtMetaobjectAnnotation(CyanMetaobject cyanMetaobject, StringBuffer codeToAdd, int offsetToAdd) {

		CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
		CompilationUnitSuper compilationUnitMetaobjectAnnotation = cyanMetaobjectAnnotation.getCompilationUnit();
		/*
		 * add change to the list of changes
		 */
		ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compilationUnitMetaobjectAnnotation);
		if ( changeList == null ) {
			changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
			setOfChanges.put(compilationUnitMetaobjectAnnotation, changeList);
		}
		String code = " " + Env.getCodeToAddWithContext(cyanMetaobject, codeToAdd.toString(), null) + " ";
		/*
		if ( cyanMetaobjectAnnotation.getInExpr() ) {
			code = " ( " + code + " ) ";
		}
		*/

		if ( offsetToAdd < 0 ) offsetToAdd = cyanMetaobjectAnnotation.getNextSymbol().getOffset();
		changeList.add(new SourceCodeChangeAddText(offsetToAdd, new StringBuffer(code), cyanMetaobjectAnnotation));

		return true;
	}

	/**
	 * remove the code of a metaobject annotation
	 *
	 * @return
	 */
	public boolean removeCodeMetaobjectAnnotation(CyanMetaobject cyanMetaobject) {

		if ( cyanMetaobject instanceof IAction_dsa ) {
			// IAction_dsa cyanMetaobjectCodeGen = (IAction_dsa )
			// cyanMetaobject;

			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
			CompilationUnitSuper compilationUnitMetaobjectAnnotation = cyanMetaobjectAnnotation.getCompilationUnit();
			/*
			 * add change to the list of changes
			 */
			ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compilationUnitMetaobjectAnnotation);
			if ( changeList == null ) {
				changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
				setOfChanges.put(compilationUnitMetaobjectAnnotation, changeList);
			}

			int offsetStart = cyanMetaobject.getMetaobjectAnnotation().getFirstSymbol().getOffset();

			CyanMetaobjectAnnotation annotation = cyanMetaobject.getMetaobjectAnnotation();
			if ( annotation instanceof CyanMetaobjectMacroCall ) {
				/*
				 * this instanceof should not be here. I know that.
				 */
				CyanMetaobjectMacroCall macroCall = (CyanMetaobjectMacroCall) annotation;

				Symbol lastSymbol = macroCall.getLastSymbolMacroCall();
				int offsetEndLastMacroSymbol = lastSymbol.getOffset() + lastSymbol.getSymbolString().length();

				SourceCodeChangeByMetaobjectAnnotation thisChange = new SourceCodeChangeDeleteText(offsetStart,
						offsetEndLastMacroSymbol - offsetStart, annotation);

				int numSourceCodeChanges = 1;
				changeList.add(thisChange);
				int numLinesToDelete = this.numLinesBetween(compilationUnitMetaobjectAnnotation.getText(), offsetStart,
						offsetEndLastMacroSymbol);

				if ( numLinesToDelete > 0 ) {
					changeList.add(new meta.SourceCodeChangeAddText(offsetStart,
							new StringBuffer(" @markDeletedCodeName(" + numLinesToDelete + ") "), annotation));
					++numSourceCodeChanges;
				}
				/**
				 * remove all source code changes asked by metaobjects, macros,
				 * or message sends (compile-time does not understand).
				 */
				// int sizeChangeList = changeList.size();
				removeChangesFromTo(changeList, offsetStart, offsetEndLastMacroSymbol, numSourceCodeChanges);

			}
			else {
				int offsetNext = cyanMetaobject.getMetaobjectAnnotation().getNextSymbol().getOffset();
				/*
				 * keep the spaces before the next token till the start of the
				 * line
				 */
				char[] text = compilationUnitMetaobjectAnnotation.getText();
				int i = offsetNext - 1;
				while (i > 0 && Character.isWhitespace(text[i]) && text[i] != '\n') {
					--i;
				}
				if ( (i >= 0 && text[i] == '\n') || (i > 0 && !Character.isWhitespace(text[i])) ) offsetNext = i + 1;

				SourceCodeChangeByMetaobjectAnnotation thisChange = new SourceCodeChangeDeleteText(offsetStart,
						offsetNext - offsetStart, annotation);
				changeList.add(thisChange);
				int numSourceCodeChanges = 1;
				int numLinesToDelete = this.numLinesBetween(compilationUnitMetaobjectAnnotation.getText(), offsetStart,
						offsetNext);

				if ( numLinesToDelete > 0 ) {
					changeList.add(new meta.SourceCodeChangeAddText(offsetStart,
							new StringBuffer(" @markDeletedCodeName(" + numLinesToDelete + ") "), annotation));
					++numSourceCodeChanges;
				}
				/**
				 * remove all source code changes asked by metaobjects, macros,
				 * or message sends (compile-time does not understand).
				 */
				removeChangesFromTo(changeList, offsetStart, offsetNext, numSourceCodeChanges);

			}

		}
		return true;
	}

	public boolean replaceMessageSendByExpression() {
		return true;
	}

	/**
	 * remove the code of unary message send unaryMessageSend that is in offset
	 * offsetToAdd of the text of compilationUnit. This is being asked by
	 * metaobject annotation annotation. The message send is replaced by codeToAdd whose
	 * type is codeType
	 *
	 */
	public boolean removeAddCodeExprIdentStar(ExprIdentStar unaryMessageSend, CompilationUnitSuper compilationUnit,
			CyanMetaobjectWithAtAnnotation annotation, StringBuffer codeToAdd, Type codeType, int offsetToAdd) {

		if ( !removeCodeExprMessageSend(unaryMessageSend, compilationUnit, annotation) ) {
			return false;
		}
		unaryMessageSend.setCodeThatReplacesThisExpr(codeToAdd);

		return addCodeAtMessageSend(annotation, compilationUnit, codeToAdd, codeType, offsetToAdd);
	}


	/**
	 * remove the code of message send exprMessageSend that is in offset
	 * offsetToAdd of the text of compilationUnit. This is being asked by
	 * metaobject annotation annotation. The message send is replaced by codeToAdd whose
	 * type is codeType
	 *
	 * @param exprMessageSend
	 * @param compilationUnit
	 * @param annotation
	 * @param codeToAdd
	 * @param offsetToAdd
	 * @return
	 */
	public boolean removeAddCodeExprMessageSend(ExprMessageSend exprMessageSend, CompilationUnitSuper compilationUnit,
			CyanMetaobjectWithAtAnnotation annotation, StringBuffer codeToAdd, Type codeType, int offsetToAdd) {

		if ( !removeCodeExprMessageSend(exprMessageSend, compilationUnit, annotation) ) {
			return false;
		}
		exprMessageSend.setCodeThatReplacesThisExpr(codeToAdd);

		return addCodeAtMessageSend(annotation, compilationUnit, codeToAdd, codeType, offsetToAdd);
	}

	/**
	 * remove the code of expression
	 *
	 * @return
	 */
	private boolean removeCodeExprMessageSend(INextSymbol exprMessageSend, CompilationUnitSuper compilationUnit,
			CyanMetaobjectWithAtAnnotation annotation) {

		/*
		 * add change to the list of changes
		 */
		ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compilationUnit);
		if ( changeList == null ) {
			changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
			setOfChanges.put(compilationUnit, changeList);
		}

		int offsetStart = exprMessageSend.getFirstSymbol().getOffset();

		int offsetNext = exprMessageSend.getNextSymbol().getOffset();
		/*
		 *
		 */
		char[] text = compilationUnit.getText();
		int i = offsetNext - 1;
		while (i > 0 && Character.isWhitespace(text[i])) {
			--i;
		}
		/*
		 * since this method is removing a message send, necessarily i > 0 and
		 * text[i] is not a white space
		 *
		 */
		if ( i == 0 ) return false;
		offsetNext = i + 1;

		SourceCodeChangeByMetaobjectAnnotation thisChange = new SourceCodeChangeDeleteText(offsetStart, offsetNext - offsetStart,
				annotation);
		changeList.add(thisChange);
		int numSourceCodeChanges = 1;
		int numLinesToDelete = this.numLinesBetween(text, offsetStart, offsetNext);

		if ( numLinesToDelete > 0 ) {
			changeList.add(new meta.SourceCodeChangeAddText(offsetStart,
					new StringBuffer(" @markDeletedCodeName(" + numLinesToDelete + ") "), annotation));
			++numSourceCodeChanges;
		}

		/**
		 * remove all source code changes asked by metaobjects or message sends
		 * (compile-time does not understand).
		 */
		removeChangesFromTo(changeList, offsetStart, offsetNext, numSourceCodeChanges);

		return true;
	}

	private boolean addCodeAtMessageSend(CyanMetaobjectAnnotation cyanMetaobjectAnnotation, CompilationUnitSuper compilationUnit,
			StringBuffer codeToAdd, Type codeType, int offsetToAdd) {

		CyanMetaobject cyanMetaobject = cyanMetaobjectAnnotation.getCyanMetaobject();
		/*
		 * add change to the list of changes
		 */
		ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compilationUnit);
		if ( changeList == null ) {
			changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
			setOfChanges.put(compilationUnit, changeList);
		}
		String code = " " + Env.getCodeToAddWithContext(cyanMetaobject, codeToAdd.toString(), codeType);
		if ( cyanMetaobjectAnnotation.getInExpr() ) {
			code = " ( " + code + " ) ";
		}

		changeList.add(new SourceCodeChangeAddText(offsetToAdd, new StringBuffer(code), cyanMetaobjectAnnotation));

		return true;
	}

	private static void removeChangesFromTo(ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList, int offsetStart,
			int offsetNext, int numSourceCodeChanges) {
		ArrayList<Integer> indexToDelete = new ArrayList<>();
		for (int i = 0; i < changeList.size() - numSourceCodeChanges; ++i) {
			SourceCodeChangeByMetaobjectAnnotation change = changeList.get(i);
			if ( change.offset >= offsetStart && change.offset < offsetNext ) {
				indexToDelete.add(i);
			}
		}
		for (int i = indexToDelete.size() - 1; i >= 0; --i) {
			changeList.remove((int) indexToDelete.get(i));
		}
	}

	/**
	 * add a metaobject annotation that should change the suffix. That is, something
	 * like <code>{@literal @}myMO(10)</code> should be changed to
	 * <code>{@literal @}myMO#dsa(10)</code>.
	 */
	public boolean addSuffixToChange(CyanMetaobject cyanMetaobject) {

		if ( cyanMetaobject instanceof IAction_dsa ) {
			CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
			CompilationUnitSuper compilationUnitMetaobjectAnnotation = cyanMetaobjectAnnotation.getCompilationUnit();
			/*
			 * add change to the list of changes
			 */
			ArrayList<SourceCodeChangeByMetaobjectAnnotation> changeList = setOfChanges.get(compilationUnitMetaobjectAnnotation);
			if ( changeList == null ) {
				changeList = new ArrayList<SourceCodeChangeByMetaobjectAnnotation>();
				setOfChanges.put(compilationUnitMetaobjectAnnotation, changeList);
			}
			changeList.add(new SourceCodeChangeShiftPhase(compilationUnitMetaobjectAnnotation.getText(), this,
					cyanMetaobjectAnnotation.getFirstSymbol().getOffset(), CompilerPhase.DSA, compilationUnitMetaobjectAnnotation,
					cyanMetaobjectAnnotation));
		}
		return true;
	}

	/**
	 * for each compilation unit that should be changed by metaobjects or by
	 * adding "#dsa" to metaobject annotations, there is associated list of code
	 * changes
	 */

	private HashMap<CompilationUnitSuper, ArrayList<SourceCodeChangeByMetaobjectAnnotation>> setOfChanges = new HashMap<>();

	public GenericParameter getGenericPrototypeFormalParameter(String key) {
		return genericPrototypeFormalParameterTable.get(key);
	}

	public GenericParameter addGenericPrototypeFormalParameter(String key, GenericParameter genericParameter) {
		if ( genericParameter.isRealPrototype() )
			return null;
		else
			return genericPrototypeFormalParameterTable.put(key, genericParameter);

	}

	/**
	 * searches for <code>methodSignature</code> in
	 * <code>methodSignatureList</code>, a list of method signatures that have
	 * the same name as <code>methodSignature</code>. The signature found in
	 * <code>methodSignatureList</code> is returned. A method signature is
	 * considered equal to other if the selectors are the same and the parameter
	 * types too.
	 *
	 * @param methodSignatureList
	 * @param env
	 * @return
	 */
	public MethodSignature searchMethodSignature(MethodSignature methodSignature,
			ArrayList<MethodSignature> methodSignatureList) {

		if ( methodSignatureList == null || methodSignatureList.size() == 0 ) return null;
		if ( methodSignature instanceof MethodSignatureUnary ) {
			if ( methodSignatureList.get(0).getReturnType(this) == methodSignature.getReturnType(this) ) {
				return methodSignatureList.get(0);
			}
			else {
				return null;
			}
		}
		else if ( methodSignature instanceof MethodSignatureOperator ) {
			MethodSignatureOperator methodSignatureOperator = (MethodSignatureOperator) methodSignature;
			if ( methodSignatureOperator.getOptionalParameter() != null ) {
				// binary
				removeAllLocalVariableDec();
				methodSignatureOperator.calcInterfaceTypes(this);

				Type paramType = methodSignatureOperator.getOptionalParameter().getType(this);
				Type returnType = methodSignatureOperator.getReturnType(this);
				for (MethodSignature ms : methodSignatureList) {
					if ( ms instanceof MethodSignatureOperator ) {
						// this should always be true
						MethodSignatureOperator msOther = (MethodSignatureOperator) ms;
						if ( msOther.getOptionalParameter() != null
								&& msOther.getOptionalParameter().getType(this) == paramType
								&& msOther.getReturnType(this) == returnType ) {
							return ms;
						}
					}
				}
				return null;
			}
			else {
				// unary operator
				for (MethodSignature ms : methodSignatureList) {

					if ( ms instanceof MethodSignatureOperator
							&& ((MethodSignatureOperator) ms).getOptionalParameter() == null ) {
						if ( ms.getReturnType(this) == methodSignature.getReturnType(this) ) {
							return ms;
						}
						else {
							return null;
						}
					}
				}
				return null;
			}
		}
		else if ( methodSignature instanceof MethodSignatureWithSelectors ) {
			MethodSignatureWithSelectors msng = (MethodSignatureWithSelectors) methodSignature;
			ArrayList<SelectorWithParameters> selectorList = msng.getSelectorArray();
			for (MethodSignature ms : methodSignatureList) {
				removeAllLocalVariableDec();
				ms.calcInterfaceTypes(this);
				if ( ms instanceof MethodSignatureWithSelectors ) {
					MethodSignatureWithSelectors msother = (MethodSignatureWithSelectors) ms;
					boolean allTypesEqual = true;
					if ( selectorList.size() != msother.getSelectorArray().size() ) {
						allTypesEqual = false;
					}
					else {
						int n = 0;
						ArrayList<SelectorWithParameters> selectorOtherList = msother.getSelectorArray();
						for (SelectorWithParameters sel : selectorList) {
							// for each selector of methodSignature, try to
							// match with selectorOtherList
							SelectorWithParameters selOther = selectorOtherList.get(n);
							if ( sel.getParameterList() != null && selOther.getParameterList() != null ) {
								if ( sel.getParameterList().size() == selOther.getParameterList().size() ) {
									int i = 0;
									for (ParameterDec param : sel.getParameterList()) {
										ParameterDec paramOther = selOther.getParameterList().get(i);
										if ( param.getType(this) != paramOther.getType(this) ) {
											allTypesEqual = false;
											break;
										}
										++i;
									}
								}
								else {
									allTypesEqual = false;
								}
							}
							else if ( sel.getParameterList() == null && selOther.getParameterList() != null )
								allTypesEqual = false;
							else if ( sel.getParameterList() != null && selOther.getParameterList() == null )
								allTypesEqual = false;

							++n;
						}
					}
					if ( allTypesEqual ) {
						if ( ms.getReturnType(this) == methodSignature.getReturnType(this) ) {
							return ms;
						}
					}
				}
			}
			return null;
		}
		else {
			return null;
		}
	}

	/**
	 * return the prototype whose name is prototypeName and that was imported by
	 * the current compilation unit or was declared as 'public' in the current
	 * compilation uniot. The prototype name is "Person" for prototype "object
	 * Person ... end" and "Stack(Int)" for the instantiation Stack<Int>. For
	 * short, the prototype name is the name of the file in which the public
	 * prototype is.
	 *
	 *
	 *
	 * public ProgramUnit searchPublicProgramUnit(String prototypeName) { return
	 * publicProgramUnitTable.get(prototypeName); }
	 */

	// public ArrayList<CompilationUnit> searchCompilationUnitByFil

	/*
	 * add a program unit to the visible program units inside the current
	 * compilation unit. Returns false if another prototype with this same name
	 * was imported. In this case, no program unit is considered visible
	 */
	public boolean addPublicProgramUnit(String key, ProgramUnit publicProgramUnit) {

		ProgramUnit previousProgramUnit = publicProgramUnitTable.put(key, publicProgramUnit);
		if ( previousProgramUnit != null ) {
			publicProgramUnitTable.remove(key);
			conflictProgramUnitTable.put(key, publicProgramUnit.getCompilationUnit().getCyanPackage());
			return false;
		}
		else if ( conflictProgramUnitTable.get(key) != null ) {
			publicProgramUnitTable.remove(key);
			return false;
		}
		else {
			return true;
		}
	}

	public Object addSourceFileName(String key, ProgramUnit value) {
		return publicSourceFileNameTable.put(key, value);
	}

	public ProgramUnit searchPrivateProgramUnit(String key) {
		return privateProgramUnitTable.get(key);
	}

	public Object addPrivateProgramUnit(String key, ProgramUnit value) {
		return privateProgramUnitTable.put(key, value);
	}

	public VariableDecInterface getLocalVariableDec(String key) {
		return variableDecTable.get(key);
	}

	/*
	 * private StatementLocalVariableDec addLocalVariableDec(String key,
	 * StatementLocalVariableDec value) { return variableDecTable.put(key,
	 * value); }
	 */

	public VariableDecInterface removeLocalVariableDec(String key) {
		return variableDecTable.remove(key);
	}

	public void removeAllLocalVariableDec() {
		variableDecTable.clear();
		;
		variableDecStack.clear();
	}

	public SlotDec getSlotDec(String key) {
		return slotDecTable.get(key);
	}

	public SlotDec addSlotDec(String key, SlotDec value) {
		return slotDecTable.put(key, value);
	}

	public void atBeginningOfCurrentMethod(MethodDec currentMethod) {
		this.currentMethod = currentMethod;
		variableDecStack.clear();
		variableDecTable.clear();
	}

	public void atBeginningOfCurrentCompilationUnit(CompilationUnit currentCompilationUnit1) {

		this.currentCompilationUnit = currentCompilationUnit1;
		enclosingObjectDec = null;
		lineMessageList = currentCompilationUnit1.getLineMessageList();
		lineShift = 0;

		/**
		 * add all private program units to table privateProgramUnitTable
		 */
		if ( currentCompilationUnit1.getProgramUnitList() != null ) {
			// it is null in a .pyan file
			for (ProgramUnit programUnit : currentCompilationUnit1.getProgramUnitList()) {
				if ( !programUnit.isGeneric() && programUnit.getVisibility() == Token.PRIVATE )
					this.addPrivateProgramUnit(programUnit.getName(), programUnit);
			}

		}

	}

	public void atEndOfCurrentCompilationUnit() {

		currentCompilationUnit = null;
		publicProgramUnitTable.clear();
		privateProgramUnitTable.clear();
		functionList.clear();

	}

	@Override
	public CompilationUnit getCurrentCompilationUnit() {
		return currentCompilationUnit;
	}

	public void addError(UnitError error) {
		thereWasError.elem = true;
		currentCompilationUnit.addError(error);

		ArrayList<UnitError> errorList;
		if ( mapCompUnitErrorList == null ) {
			mapCompUnitErrorList = new HashMap<>();
			errorList = new ArrayList<>();
			errorList.add(error);
		}
		else {
			errorList = mapCompUnitErrorList.get(this.currentCompilationUnit);
			if ( errorList == null ) {
				errorList = new ArrayList<>();
			}
			errorList.add(error);
		}
		mapCompUnitErrorList.put(this.currentCompilationUnit,  errorList);

	}

	/**
	 * If necessary, add a context message to the error message. This context
	 * informs that the error was caused by code introduced by such and such
	 * metaobject annotations OR by code introduced by the compiler.
	 *
	 * @param msg
	 * @return
	 */
	public static String addContextMessage(Stack<Tuple6<String, String, String, String, Integer, Integer>> contextStack,
			String msg) {

		if ( !contextStack.isEmpty() ) {
			/*
			 * there is a context. Then the code that caused this compilation
			 * error was introduced by some metaobject annotation or by the compiler
			 */
			Tuple6<String, String, String, String, Integer, Integer> t = contextStack.peek();

			if ( t.f3 == null || t.f4 == null || t.f5 == null ) {
				msg = msg + ". This internal error was caused by code introduced by the compiler in step '" + t.f2
						+ "'. Check the documentation of " + meta.cyanLang.CyanMetaobjectCompilationContextPush.class.getName();
			}
			else {
				String cyanMetaobjectName = t.f2;
				String packageName = t.f3;
				String prototypeName = t.f4;
				int lineNumber = t.f5;
				msg = msg + ". This error was caused by code introduced initially by metaobject annotation '" + cyanMetaobjectName
						+ "' at line " + lineNumber + " of " + packageName + "." + prototypeName;
				if ( contextStack.size() > 1 ) {
					String s = ". The complete stack of "
							+ "context (metaobject name, package.prototype, line number) is: ";
					for (int kk = 1; kk < contextStack.size(); ++kk) {
						t = contextStack.get(kk);
						cyanMetaobjectName = t.f2;
						packageName = t.f3;
						prototypeName = t.f4;
						lineNumber = t.f5;
						s += "(" + cyanMetaobjectName + ", " + packageName + "." + prototypeName + ", " + lineNumber
								+ ") ";
					}
					msg = msg + s;
				}
			}

		}
		return msg;
	}

	/**
	 * If there was any previous call to a method of a metaobject implementing
	 * {@link meta#IInformCompilationError} this method checks whether this
	 * error message was foreseen. If it was not, a warning is signaled.
	 *
	 * @param sym
	 * @param lineNumber
	 * @param specificMessage
	 * @return true if there was a previous call to a method of {@link meta#IInformCompilationError}
	 */
	private boolean checkErrorMessage(Symbol sym, int lineNumber, String specificMessage) {

		if ( lineMessageList == null || lineMessageList.size() == 0 )
			return false;
		else {
			/*
			 * a method of  {@link meta#IInformCompilationError} has been called in this compilation
			 * unit
			 */
			int i = 0;
			boolean found = false;
			boolean throwCEE = false;
			for (Tuple3<Integer, String, Boolean> t : lineMessageList) {

				if ( (lineNumber < 0 && t.f1 < 0) || (t.f1 == lineNumber) ) {
					try {
						// found the correct line number
						this.warning(sym, "The expected error message at line " + lineNumber + " was '" + t.f2
								+ "'. The message given by the compiler was '" + specificMessage + "'");
					}
					catch (CompileErrorException e) {
						throwCEE = true;
					}
					lineMessageList.get(i).f3 = true;
					found = true;
					break;
				}
				++i;
			}
			if ( !found ) {
				this.errorWithoutCheckingMetaobjectCompilationError(sym,
						"The compiler issued the error message '" + specificMessage
								+ "'. However, no metaobject implementing 'IInformCompilationError' has foreseen this error",
						true);
			}
			if ( throwCEE ) throw new CompileErrorException();
			return true;
		}
	}

	public void warning(Symbol sym, String msg) {
		error(sym, msg, false, false);
	}

	public void errorInMetaobject(CyanMetaobject cyanMetaobject, CyanMetaobjectAnnotation annotation) {
		ArrayList<CyanMetaobjectError> errorList = cyanMetaobject.getErrorMessageList();
		if ( errorList != null ) {
			for (CyanMetaobjectError moError : errorList) {
				error(annotation.getFirstSymbol(), moError.getMessage());
			}
		}

	}

	public void errorInMetaobjectCatchExceptions(CyanMetaobject cyanMetaobject, CyanMetaobjectAnnotation annotation) {
		ArrayList<CyanMetaobjectError> errorList = cyanMetaobject.getErrorMessageList();
		if ( errorList != null ) {
			for (CyanMetaobjectError moError : errorList) {
				try {
					error(annotation.getFirstSymbol(), moError.getMessage());
				}
				catch (error.CompileErrorException e) {

				}
			}
		}
	}

	public void errorInMetaobject(CyanMetaobject cyanMetaobject, Symbol symbolWithError) {
		ArrayList<CyanMetaobjectError> errorList = cyanMetaobject.getErrorMessageList();
		if ( errorList != null ) {
			for (CyanMetaobjectError moError : errorList) {
				error(symbolWithError, moError.getMessage());
			}
		}
	}

	public void thrownException(CyanMetaobjectAnnotation annotation, Symbol firstSymbol, RuntimeException e) {
		String prototypeName = annotation.getPrototypeOfAnnotation();
		String packageName = annotation.getPackageOfAnnotation();
		int lineNumber = annotation.getFirstSymbol().getLineNumber();
		error(firstSymbol,
				"Metaobject annotation '" + annotation.getCyanMetaobject().getName() + "' at line number " + lineNumber
						+ " in " + packageName + "." + prototypeName + " has thrown exception '"
						+ e.getClass().getName() + "'");

	}

	public void error(int lineNumber, int columnNumber, String message) {
		thereWasError.elem = true;

		error(null, lineNumber, columnNumber, message, true, true);
	}


	public void error(Symbol symbol, String message) {
		thereWasError.elem = true;

		error(symbol, message, true, true);
	}

	public void error(Symbol symbol, String message, boolean checkMessage) {
		error(symbol, message, checkMessage, true);
	}

	// error(lineNumber, columnNumber, message, true, true);


	public void error(Symbol symbol, String message, boolean checkMessage, boolean throwException) {
		error(symbol, message, checkMessage, throwException, true);
	}

	public void error(Symbol symbol, String message, boolean checkMessage, boolean throwException, boolean shiftLineNumber) {
		thereWasError.elem = true;

		if ( symbol != null ) {
			if ( this.cyanMetaobjectCompilationContextStack.isEmpty() ) {
				if ( shiftLineNumber ) {
					symbol.setLineNumber(symbol.getLineNumber() - this.lineShift);
				}
			}
			else {
				/*
				 * error was in code produced by the compiler, maybe with the
				 * help of metaobjects. Give to the user the line number of the
				 * file with the expanded source code
				 */
				message = "Look for the error in the expanded source code, not in the original one. If the source code is 'Program.cyan', the "
						+ "expanded source code should be in 'full-Program.cyan' in the directory of the project. "
						+ message;
			}
		}

		message = addContextMessage(this.cyanMetaobjectCompilationContextStack, message);

		if ( checkMessage )
			if ( checkErrorMessage(symbol, symbol == null ? -1 : symbol.getLineNumber(), message) ) return;

		errorWithoutCheckingMetaobjectCompilationError(symbol, message, throwException);

	}


	public void errorInsideMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation, int lineNumber, int columnNumber, String message) {
		CyanMetaobject metaobject = metaobjectAnnotation.getCyanMetaobject();
		if ( metaobject instanceof CyanMetaobjectLiteralString || metaobject instanceof CyanMetaobjectWithAt ) {
			error(null, metaobjectAnnotation.getFirstSymbol().getLineNumber() + lineNumber - 1, columnNumber, message, true, true);
		}
	}




	public void error(Symbol symbol, int lineNumber, int columnNumber, String message, boolean checkMessage, boolean throwException) {
		thereWasError.elem = true;

		if ( symbol != null ) {
			if ( this.cyanMetaobjectCompilationContextStack.isEmpty() )
				symbol.setLineNumber(symbol.getLineNumber() - this.lineShift);
			else {
				/*
				 * error was in code produced by the compiler, maybe with the
				 * help of metaobjects. Give to the user the line number of the
				 * file with the expanded source code
				 */
				message = "Look for the error in the expanded source code, not in the original one. If the source code is 'Program.cyan', the "
						+ "expanded source code should be in 'full-Program.cyan' in the directory of the project. "
						+ message;
			}
		}

		message = addContextMessage(this.cyanMetaobjectCompilationContextStack, message);

		if ( checkMessage )
			if ( checkErrorMessage(symbol, lineNumber, message) ) return;

		errorWithoutCheckingMetaobjectCompilationError(symbol, lineNumber, columnNumber, message, throwException);

	}


	public void error(CompilationUnit cunitError, Symbol symbol, String message) {
		thereWasError.elem = true;

		cunitError.error(symbol, (symbol == null ? -1 : symbol.getLineNumber()), message, null, this);
	}

	/**
	 * @param symbol
	 * @param message
	 * @param throwException
	 *            TODO
	 */
	private void errorWithoutCheckingMetaobjectCompilationError(Symbol symbol, String message, boolean throwException) {
		errorWithoutCheckingMetaobjectCompilationError(symbol, -1, 0, message, throwException);
		/*
		thereWasError.elem = true;
		if ( prefixErrorMessage != null ) {
			message = prefixErrorMessage + message;
			prefixErrorMessage = null;
		}

		if ( symbol == null ) {
			if ( currentCompilationUnit != null ) {
				currentCompilationUnit.error(symbol, -1, message, null, this);
			}
			else {
				this.project.error(message);
			}
		}
		else {
			if ( symbol.getCompilationUnit() != null ) {
				// error(true, symbol, lineNumber, msg);
				CompilationUnitSuper cunit = symbol.getCompilationUnit();
				try {
					cunit.error(throwException, symbol, symbol.getLineNumber(), message, null, this);
				}
				finally {
					UnitError lastError = cunit.getErrorList().get(cunit.getErrorList().size()-1);
					ArrayList<UnitError> errorList;
					if ( mapCompUnitErrorList == null ) {
						mapCompUnitErrorList = new HashMap<>();
						errorList = new ArrayList<>();
						errorList.add(lastError);
					}
					else {
						errorList = mapCompUnitErrorList.get(this.currentCompilationUnit);
						if ( errorList == null ) {
							errorList = new ArrayList<>();
						}
						errorList.add(lastError);
					}
					mapCompUnitErrorList.put(this.currentCompilationUnit,  errorList);
				}



			}
			else {
				this.project.error(message);
			}
		}
		*/
	}


	private void errorWithoutCheckingMetaobjectCompilationError(Symbol symbol, int lineNumber, int columnNumber,
			String message, boolean throwException) {
		thereWasError.elem = true;
		if ( prefixErrorMessage != null ) {
			message = prefixErrorMessage + message;
			prefixErrorMessage = null;
		}

		if ( symbol == null ) {
			if ( currentCompilationUnit != null ) {
				currentCompilationUnit.error(symbol, lineNumber, columnNumber, message, null, this);
			}
			else {
				this.project.error(message);
			}
		}
		else {
			if ( symbol.getCompilationUnit() != null ) {
				// error(true, symbol, lineNumber, msg);
				CompilationUnitSuper cunit = symbol.getCompilationUnit();
				try {
					cunit.error(throwException, symbol, symbol.getLineNumber(), message, null, this);
				}
				finally {
					UnitError lastError = cunit.getErrorList().get(cunit.getErrorList().size()-1);
					ArrayList<UnitError> errorList;
					if ( mapCompUnitErrorList == null ) {
						mapCompUnitErrorList = new HashMap<>();
						errorList = new ArrayList<>();
						errorList.add(lastError);
					}
					else {
						errorList = mapCompUnitErrorList.get(this.currentCompilationUnit);
						if ( errorList == null ) {
							errorList = new ArrayList<>();
						}
						errorList.add(lastError);
					}
					mapCompUnitErrorList.put(this.currentCompilationUnit,  errorList);
				}



			}
			else {
				this.project.error(message);
			}
		}
	}




	public void error(boolean checkMessage, Symbol symbol, String specificMessage, String identifier,
			ErrorKind errorKind, String... furtherArgs) {

		if ( symbol != null ) {
			if ( this.cyanMetaobjectCompilationContextStack.isEmpty() )
				symbol.setLineNumber(symbol.getLineNumber() - this.lineShift);
			/*
			 * else { / * error was in code produced by the compiler, maybe with
			 * the help of metaobjects. Give to the user the line number of the
			 * file with the expanded source code / specificMessage =
			 * "Look for the error in the expanded source code, not in the original one. If the source code is 'Program.cyan', the "
			 * +
			 * "expanded source code should be in 'full-Program.cyan' in the directory of the project."
			 * + specificMessage; }
			 */
		}
		if ( checkMessage )
			if ( checkErrorMessage(symbol, symbol == null ? -1 : symbol.getLineNumber(), specificMessage) ) return;

		/*
		 * do not delete this call before copying the code of error(...) to
		 * here. The line number must be shift
		 */
		error(symbol, specificMessage, true, true);

		thereWasError.elem = true;

		ArrayList<String> sielCode = new ArrayList<String>();
		sielCode.add("error = \"" + specificMessage + "\"");
		for (String field : errorKind.getFieldList()) {
			switch (field) {
			case "methodName":
				sielCode.add("methodName = \"" + this.currentMethod.getName() + "\"");
				break;
			case "identifier":
				sielCode.add("identifier = \"" + identifier + "\"");
				break;
			case "statementText":
				if ( !(getCurrentProgramUnit() instanceof InterfaceDec) ) {
					sielCode.add("statementText = \"" + stringCurrentStatement() + "\"");
				}
				break;
			case "methodSignature":
				if ( getCurrentMethod() != null ) {
					sielCode.add("methodSignature = \"" + stringSignatureCurrentMethod() + "\"");
				}
				break;
			case "prototypeName":
				sielCode.add("prototypeName = \"" + getCurrentObjectDec().getName() + "\"");
				break;
			case "interfaceName":
				sielCode.add("interfaceName = \"" + identifier + "\"");
				break;
			case "visibleLocalVariableList":
				sielCode.add("visibleLocalVariableList = \"" + getStringVisibleLocalVariableList() + "\"");
				break;
			case "instanceVariableList":
				sielCode.add("instanceVariableList = \"" + getStringVisibleLocalVariableList() + "\"");
				break;
			case "methodList":
				sielCode.add("methodList = \"" + getStringSignatureAllMethods() + "\"");
				break;
			case "packageName":
				sielCode.add("packageName = \"" + this.getCurrentCompilationUnit().getPackageName());
				break;
			case "importList":
				String strImportList = "";
				for (ExprIdentStar e : getCurrentCompilationUnit().getImportPackageList())
					strImportList = strImportList + " " + e.getName();
				sielCode.add("importList = \"" + strImportList + "\"");
				break;
			case "receiver":
				sielCode.add("receiver = \"" + furtherArgs[0]);
				break;
			case "supertype":
				String supertypeName = "";
				if ( currentProgramUnit instanceof ObjectDec ) {
					ObjectDec superProto = ((ObjectDec) currentProgramUnit).getSuperobject();
					if ( superProto != null ) supertypeName = superProto.getFullName();
				}
				else if ( currentProgramUnit instanceof InterfaceDec ) {
					ArrayList<Expr> superInterfaceList = ((InterfaceDec) currentProgramUnit)
							.getSuperInterfaceExprList();
					if ( superInterfaceList != null && superInterfaceList.size() > 0 ) {
						supertypeName = "";
						int size = superInterfaceList.size();
						for (Expr superInterface : superInterfaceList) {
							supertypeName += superInterface.asString();
							if ( --size > 0 ) supertypeName += ", ";
						}
					}
				}
				sielCode.add("supertype = \"" + supertypeName + "\"");
				break;
			case "implementedInterfaces":
				String implInterfacesStr = "";
				if ( currentProgramUnit instanceof ObjectDec ) {
					ArrayList<Expr> superInterfaceList = ((ObjectDec) currentProgramUnit).getInterfaceList();
					if ( superInterfaceList != null && superInterfaceList.size() > 0 ) {
						implInterfacesStr = "";
						int size = superInterfaceList.size();
						for (Expr superInterface : superInterfaceList) {
							implInterfacesStr += superInterface.asString();
							if ( --size > 0 ) implInterfacesStr += ", ";
						}
					}

				}
				sielCode.add("implementedInterfaces = \"" + implInterfacesStr + "\"");
				break;
			default:
				String keyValue = null;
				String fieldName;
				for (String other : furtherArgs) {
					int i = other.indexOf("=");
					if ( i > 0 ) {
						while (i > 0 && other.charAt(i - 1) == ' ')
							--i;
						if ( i > 0 ) {
							fieldName = other.substring(0, i);
							if ( fieldName.equals(field) ) {
								keyValue = other;
								break;
							}
						}

					}
					else
						error(null, "Internal error in Env::error: error called without a key/value pair", true, true);

				}
				if ( keyValue != null )
					sielCode.add(keyValue);
				else
					error(null, "Internal error in Env::error: field " + field + " of Siel was not recognized", true,
							true);
				return;
			}
		}

		for (String other : furtherArgs) {
			sielCode.add(other);
		}
		char[] compUnitText = null;
		String filename = null;
		if ( currentCompilationUnit != null ) {
			compUnitText = currentCompilationUnit.getText();
			filename = currentCompilationUnit.getFilename();
		}
		/*
		 * SignalCompilerError.signalCompilerError( compUnitText, filename,
		 * symbol == null ? -1 : symbol.getLineNumber(), symbol == null ? -1 :
		 * symbol.getColumnNumber(), sielCode );
		 */
		// throw new CompileErrorException();
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	public void pushVariableDec(VariableDecInterface localVariableDec) {
		/*
		 * context function have a first parameter called "self". This should
		 * not be inserted or popped from the stack
		 */
		String namevar = localVariableDec.getName();
		if ( namevar != null && !namevar.equals("self") ) {
			variableDecStack.push(localVariableDec);
			variableDecTable.put(namevar, localVariableDec);
		}
	}

	/**
	 * pop num variable declarations from the stack of local variables
	 *
	 * @param num
	 */
	public void popNumLocalVariableDec(int num) {
		while (num-- > 0) {
			/*
			 * context function have a first parameter called "self". This
			 * should not be inserted or popped from the stack
			 */
			String name = variableDecStack.pop().getName();
			if ( !name.equals("self") ) variableDecTable.remove(name);
		}
	}

	/**
	 * return the number of local variables declared till this point
	 */
	public int numberOfLocalVariables() {
		return variableDecStack.size();
	}

	/**
	 * return a string containing the local variables visible in this point,
	 * including the parameters of the method and functions. The variables are
	 * separated by commas like "index, myStack, i"
	 */
	public String getStringVisibleLocalVariableList() {
		String stringVisibleLocalVariableList;
		int size = variableDecStack.size();
		int count = size;
		if ( size == 0 ) {
			return "(Array<String> new)";
		}
		else {
			stringVisibleLocalVariableList = " [ ";
			for (VariableDecInterface variableDec : variableDecStack) {
				stringVisibleLocalVariableList = stringVisibleLocalVariableList + "\"" + variableDec.getName() + "\"";
				if ( --count > 0 ) stringVisibleLocalVariableList = stringVisibleLocalVariableList + ", ";
			}
			stringVisibleLocalVariableList += " ] ";
			return stringVisibleLocalVariableList;
		}
	}

	/**
	 * return a string with the list of all instance variables of the current
	 * prototype
	 */
	public String getStringInstanceVariableList() {

		if ( currentProgramUnit != null && currentProgramUnit instanceof ObjectDec ) {
			ObjectDec prototype = (ObjectDec) currentProgramUnit;
			ArrayList<InstanceVariableDec> instanceVariableDecList = prototype.getInstanceVariableList();
			int count = instanceVariableDecList.size();
			if ( count > 0 ) {
				String stringInstanceVariableList = " [ ";
				for (InstanceVariableDec instanceVariableDec : instanceVariableDecList) {
					stringInstanceVariableList = stringInstanceVariableList + "\"" + instanceVariableDec.getName()
							+ "\"";
					if ( --count > 0 ) stringInstanceVariableList = stringInstanceVariableList + ", ";
				}
				stringInstanceVariableList += " ] ";
				return stringInstanceVariableList;
			}
		}
		return "(Array<String> new)";
	}

	/**
	 * to be called at the beginning of a function
	 */
	public void atBeginningFunctionDec() {

	}

	/**
	 * at the end of a function, all local variables of the functions should be
	 * removed from the local table
	 */
	public void atEndFunctionDec() {

	}

	/**
	 * at the end of a method declaration, all parameters and instance variables
	 * should be eliminated from the environment
	 */
	public void atEndMethodDec() {
		currentMethod = null;
		variableDecTable.clear();
		variableDecStack.clear();
	}

	/**
	 * at the beginning of an object or interface declaration
	 */
	public void atBeginningOfObjectDec(ProgramUnit currentObjectDec) {

		this.currentProgramUnit = currentObjectDec;
		if ( currentProgramUnit.getVisibility() == Token.PRIVATE )
			this.privateProgramUnitTable.put(currentProgramUnit.getName(), currentProgramUnit);

		if ( currentObjectDec instanceof ObjectDec )
			this.enclosingObjectDec = ((ObjectDec) currentObjectDec).getOuterObject();

		genericPrototypeFormalParameterTable = new Hashtable<String, GenericParameter>();
		if ( currentObjectDec.isGeneric() ) {
			for (ArrayList<GenericParameter> genericList : currentObjectDec.getGenericParameterListList()) {
				for (GenericParameter g : genericList) {
					// only add generic parameters that are not real prototypes.
					// That is,
					// "T" in "object Stack<:T> ... end" is added. But "Int" in
					// "object Stack<Int> ... end" would not.
					if ( !g.isRealPrototype() ) addGenericPrototypeFormalParameter(g.getName(), g);
				}
			}
		}
	}

	/**
	 * at the end of an object declaration, all instance variables and methods
	 * should be eliminated from the environment
	 */
	public void atEndOfObjectDec() {
		this.currentProgramUnit = null;
		slotDecTable.clear();
		genericPrototypeFormalParameterTable.clear();
		this.enclosingObjectDec = null;
	}

	@Override
	public ObjectDec getCurrentObjectDec() {
		if ( currentProgramUnit instanceof ObjectDec )
			return (ObjectDec) currentProgramUnit;
		else
			return null;
	}

	public MethodDec getCurrentMethod() {
		return currentMethod;
	}

	public void setCurrentMethod(MethodDec currentMethod) {
		this.currentMethod = currentMethod;
	}

	public ArrayList<ExprFunction> getFunctionList() {
		return functionList;
	}

	public void setFunctionList(ArrayList<ExprFunction> functionList) {
		this.functionList = functionList;
	}

	@Override
	public Set<CyanPackage> getImportedPackageSet() {
		if ( this.currentCompilationUnit == null ) {
			return null;
		}
		else {
			return this.currentCompilationUnit.getImportPackageSet();
		}
	}

	/**
	 * returns a string containing the current statement. If there is no current
	 * statement, returns null
	 *
	 * @return
	 */
	public String stringCurrentStatement() {
		char[] currentCompilationUnitText = getCurrentCompilationUnit().getText();
		int start = peekCode().getFirstSymbol().getOffset();
		Symbol lastSymbol = peekCode().getLastSymbol();
		int theEnd = lastSymbol.getOffset() + lastSymbol.symbolString.length();
		int size = theEnd - start;
		char[] statementText = new char[size];
		/*
		 * for (int i = 0; i < size; ++i) { statementText[i] =
		 * currentCompilationUnitText[start + i]; }
		 */
		System.arraycopy(currentCompilationUnitText, start, statementText, 0, size);

		return new String(statementText);
	}

	public void pushCode(Statement statement) {
		statementStack.push(statement);
	}

	public CodeWithError peekCode() {
		return statementStack.peek();
	}

	public CodeWithError popCode() {
		return statementStack.pop();
	}

	/**
	 * returns the signature of the current method
	 *
	 * @return
	 */
	public String stringSignatureCurrentMethod() {
		PWCharArray pwChar = new PWCharArray();
		if ( currentMethod.getHasOverride() ) pwChar.print("override ");
		if ( currentMethod.getVisibility() == Token.PUBLIC )
			pwChar.print("public ");
		else if ( currentMethod.getVisibility() == Token.PRIVATE )
			pwChar.print("private ");
		else if ( currentMethod.getVisibility() == Token.PROTECTED ) pwChar.print("protected ");
		if ( currentMethod.isAbstract() ) pwChar.print("abstract ");
		pwChar.print("func ");
		currentMethod.getMethodSignature().genCyan(pwChar, false, NameServer.cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}

	/**
	 * returns a string with a literal array with the signatures of all methods
	 * of the current program unit (prototype or interface)
	 *
	 * @return
	 */
	public String getStringSignatureAllMethods() {
		String s;
		if ( currentProgramUnit instanceof ObjectDec ) {
			ObjectDec currentObjectDec = (ObjectDec) currentProgramUnit;
			ArrayList<MethodDec> methodDecList = currentObjectDec.getMethodDecList();
			int count = methodDecList.size();
			if ( count > 0 ) {
				s = " [ ";
				for (MethodDec methodDec : methodDecList) {
					s += "\"";
					s += methodDec.getMethodSignatureAsString().trim();
					s += "\"";
					if ( --count > 0 ) s += ", ";
				}
				s += " ] ";
			}
			else {
				s = "(Array<String> new)";
			}
		}
		else if ( currentProgramUnit instanceof InterfaceDec ) {
			InterfaceDec currentInterfaceDec = (InterfaceDec) currentProgramUnit;
			ArrayList<MethodSignature> methodSignatureList = currentInterfaceDec.getMethodSignatureList();
			int count = methodSignatureList.size();
			if ( count > 0 ) {
				s = " [ ";
				for (MethodSignature methodSignature : methodSignatureList) {
					s += methodSignature.getNameWithoutParamNumber().trim();
					// methodSignature.genCyan(pwChar, false,
					// NameServer.cyanEnv, true);
					if ( --count > 0 ) s += ", ";
				}
				s += " ] ";
			}
			else {
				s = "(Array<String> new)";
			}
		}
		else {
			s = "(Array<String> new)";
		}
		return s;

	}

	/**
	 * returns a string with a literal array with the signatures of all methods
	 * of the current program unit (prototype or interface)
	 *
	 * @return
	 */
	public String getStringSignatureAllMethods2() {
		PWCharArray pwChar = new PWCharArray();
		if ( currentProgramUnit instanceof ObjectDec ) {
			ObjectDec currentObjectDec = (ObjectDec) currentProgramUnit;
			ArrayList<MethodDec> methodDecList = currentObjectDec.getMethodDecList();
			int count = methodDecList.size();
			if ( count > 0 ) {
				pwChar.print(" [ ");
				for (MethodDec methodDec : methodDecList) {
					pwChar.print("\"");
					// methodDec.getMethodSignatureAsString()
					methodDec.getMethodSignature().genCyan(pwChar, false, NameServer.cyanEnv, true);
					pwChar.print("\"");
					if ( --count > 0 ) pwChar.print(", ");
				}
				pwChar.print(" ] ");
			}
			else {
				pwChar.println("(Array<String> new)");
			}
		}
		else if ( currentProgramUnit instanceof InterfaceDec ) {
			InterfaceDec currentInterfaceDec = (InterfaceDec) currentProgramUnit;
			ArrayList<MethodSignature> methodSignatureList = currentInterfaceDec.getMethodSignatureList();
			int count = methodSignatureList.size();
			if ( count > 0 ) {
				pwChar.print(" [ ");
				for (MethodSignature methodSignature : methodSignatureList) {
					methodSignature.genCyan(pwChar, false, NameServer.cyanEnv, true);
					if ( --count > 0 ) pwChar.print(", ");
				}
				pwChar.print(" ] ");
			}
			else {
				pwChar.println("(Array<String> new)");
			}
		}
		else {
			pwChar.println("(Array<String> new)");
		}
		return pwChar.getGeneratedString().toString();

	}

	@Override
	public ProgramUnit getCurrentProgramUnit() {
		return currentProgramUnit;
	}

	public void setCurrentProgramUnit(ProgramUnit currentProgramUnit) {
		this.currentProgramUnit = currentProgramUnit;
	}

	/**
	 * return prototype whose source file name is sourceFileName and that was
	 * imported by the current compilation unit.
	 */

	public ProgramUnit searchProgramUnitBySourceFileName(String sourceFileName, Symbol firstSymbol,
			boolean insideMethod) {
		// return this.publicSourceFileNameTable.get(sourceFileName);

		// prototype name without the generic parameters
		String rawPrototypeName = sourceFileName;
		int lessIndex = sourceFileName.indexOf('(');
		if ( lessIndex >= 0 ) rawPrototypeName = sourceFileName.substring(0, lessIndex);
		int lastDot = rawPrototypeName.lastIndexOf('.');
		if ( lastDot >= 0 ) {
			/*
			 * there is a package preceding the prototype name
			 */
			String packageName = sourceFileName.substring(0, lastDot);
			String realPrototypeName = sourceFileName.substring(lastDot + 1);
			CyanPackage aPackage = getProject().searchPackage(packageName);
			if ( aPackage == null )
				return null;
			else {
				return aPackage.searchProgramUnitBySourceFileName(realPrototypeName);
			}
		}
		else {
			/*
			 * no package preceding the prototype name
			 */

			/*
			 * program units defined in the current compilation unit have
			 * precedence over the imported ones.
			 */
			if ( sourceFileName.equals(this.currentCompilationUnit.getFileNameWithoutExtension()) ) {
				return this.currentCompilationUnit.getPublicPrototype();
			}

			if ( this.currentCompilationUnit.getConflictProgramUnitTable().get(rawPrototypeName) != null ) {
				// prototypeName is imported from two or more packages
				String packageNameList = "";
				for (CyanPackage cp : this.currentCompilationUnit.getImportedCyanPackageTable()) {
					if ( cp.searchProgramUnitBySourceFileName(rawPrototypeName) != null )
						packageNameList += cp.getPackageName() + " ";
				}
				if ( insideMethod )
					this.error(true, firstSymbol,
							"Prototype '" + rawPrototypeName + "' is imported from two or more packages: "
									+ packageNameList,
							rawPrototypeName, ErrorKind.prototype_imported_from_two_or_more_packages_inside_method);
				else
					this.error(true, firstSymbol,
							"Prototype '" + rawPrototypeName + "' is imported from two or more packages: "
									+ packageNameList,
							rawPrototypeName, ErrorKind.prototype_imported_from_two_or_more_packages_outside_method);
			}
			else {
				for (CyanPackage cp : this.currentCompilationUnit.getImportedCyanPackageTable()) {
					ProgramUnit pu;
					if ( (pu = cp.searchProgramUnitBySourceFileName(sourceFileName)) != null ) {
						return pu;
					}

				}
			}
		}

		return null;
	}

	/**
	 * return the program unit that has name prototypeName and that was imported
	 * by the current compilation unit or that was declared in the current
	 * source file.
	 *
	 * If prototypeName can be imported from two or more packages, an error is
	 * signaled. If prototypeName is not found, returns null.
	 *
	 * firstSymbol is the first symbol of 'prototypeName'. It is used to sign
	 * errors
	 *
	 * insideMethod is true if this method is called to search for a prototype
	 * that is used inside a method.
	 *
	 * @param prototypeName
	 * @param firstSymbol
	 * @return
	 */
	public ProgramUnit searchVisibleProgramUnit(String prototypeName, Symbol firstSymbol, boolean insideMethod) {

		// prototype name without the generic parameters
		String rawPrototypeName = prototypeName;
		int lessIndex = prototypeName.indexOf('<');
		if ( lessIndex >= 0 ) rawPrototypeName = prototypeName.substring(0, lessIndex);
		int lastDot = rawPrototypeName.lastIndexOf('.');
		if ( lastDot >= 0 ) {
			/*
			 * there is a package preceding the prototype name
			 */
			String packageName = prototypeName.substring(0, lastDot);
			String realPrototypeName = prototypeName.substring(lastDot + 1);
			CyanPackage aPackage = getProject().searchPackage(packageName);
			if ( aPackage == null )
				return null;
			else {
				return aPackage.searchPublicNonGenericProgramUnit(realPrototypeName);
			}
		}
		else {
			/*
			 * no package preceding the prototype name
			 */

			/**
			 * search in inner prototypes first
			 */
			if ( currentProgramUnit != null ) {
				ArrayList<ObjectDec> innerPrototypeList = null;
				if ( currentProgramUnit.getOuterObject() != null ) {
					/*
					 * if the current prototype is an inner prototype, search in
					 * the inner prototypes of the outer prototype. The test
					 * "currentProgramUnit.getOuterObject().getInnerPrototypeList()"
					 * should always return a value != null, so nowadays this
					 * test is unnecessary
					 */
					if ( currentProgramUnit.getOuterObject().getInnerPrototypeList() != null )
						innerPrototypeList = currentProgramUnit.getOuterObject().getInnerPrototypeList();
				}
				else {
					// current prototype is NOT an inner prototype. Search in
					// its inner prototypes
					innerPrototypeList = currentProgramUnit.getInnerPrototypeList();
				}
				if ( innerPrototypeList != null ) {
					for (ObjectDec innerObj : innerPrototypeList) {
						if ( prototypeName.compareTo(innerObj.getName()) == 0 ) {
							return innerObj;
						}
					}
				}
			}
			/*
			 * program units defined in the current compilation unit have
			 * precedence over the imported ones.
			 */
			if ( currentCompilationUnit.getProgramUnitList() != null ) {
				/*
				 * it is null if the compilation unit is the project file, the
				 * .pyan file
				 */
				for (ProgramUnit programUnit : this.currentCompilationUnit.getProgramUnitList()) {
					if ( prototypeName.compareTo(programUnit.getName()) == 0 ) {
						return programUnit;
					}
				}

			}

			if ( this.currentCompilationUnit.getConflictProgramUnitTable().get(rawPrototypeName) != null ) {
				// prototypeName is imported from two or more packages
				String packageNameList = this.currentCompilationUnit.getConflictProgramUnitTable()
						.get(rawPrototypeName);

				/*
				 * for ( CyanPackage cp :
				 * this.currentCompilationUnit.getImportedCyanPackageTable() ) {
				 * if ( cp.searchPublicNonGenericProgramUnit(prototypeName) !=
				 * null ) packageNameList += cp.getPackageName() + " "; }
				 */
				if ( insideMethod )
					this.error(true, firstSymbol,
							"Prototype '" + prototypeName + "' is imported from two or more packages: "
									+ packageNameList,
							prototypeName, ErrorKind.prototype_imported_from_two_or_more_packages_inside_method);
				else
					this.error(true, firstSymbol,
							"Prototype '" + prototypeName + "' is imported from two or more packages: "
									+ packageNameList,
							prototypeName, ErrorKind.prototype_imported_from_two_or_more_packages_outside_method);
			}
			else {
				for (CyanPackage cp : this.currentCompilationUnit.getImportedCyanPackageTable()) {
					ProgramUnit pu;
					if ( (pu = cp.searchPublicNonGenericProgramUnit(prototypeName)) != null ) {
						return pu;
					}

				}
				/**
				 * if programUnitForGenericPrototypeList is not null, then we
				 * are doing semantic analysis in a generic prototype. This is
				 * only done by metaobject 'concept'.
				 */
				if ( programUnitForGenericPrototypeList != null ) {
					for (ProgramUnit pu : programUnitForGenericPrototypeList) {
						if ( pu.getName().equals(prototypeName) ) {
							return pu;
						}
					}
				}
			}

			return null;
		}
	}

	public void setCurrentCompilationUnit(CompilationUnit currentCompilationUnit) {
		this.currentCompilationUnit = currentCompilationUnit;
	}

	public void pushFunction(ExprFunction function) {
		functionStack.push(function);
	}

	public ExprFunction popFunction() {
		return functionStack.pop();
	}

	public ExprFunction peekFunction() {
		return functionStack.peek();
	}

	public Stack<ExprFunction> getFunctionStack() {
		return functionStack;
	}

	/**
	 * searches for a variable "name" in the local variables and parameters.
	 *
	 * @param name
	 * @return
	 */
	public VariableDecInterface searchLocalVariableParameter(String name) {
		return this.variableDecTable.get(name);
	}

	/**
	 * searches for a variable "name" in the lists of local variables,
	 * parameters, and instance variables. The search is
	 * made in the list of instance variables only if no local
	 * variable or parameter is found. This method should not be used in inner
	 * prototypes.
	 *
	 * @param name
	 * @return an object of VariableDecInterface (local variable, parameter) or
	 *         an object of InstanceVariableDec
	 */
	public VariableDecInterface searchVariable(String name) {

		VariableDecInterface ret = this.searchLocalVariableParameter(name);

		if ( ret == null ) {
			ret = currentProgramUnit.searchInstanceVariablePrivateProtectedSuperProtected(name);
		}
		return ret;
	}

	/**
	 * search for a variable <code>name</code> inside an 'eval' or 'eval:eval:
	 * ...' method of an inner prototype created for a function or a method.
	 *
	 * @param name
	 * @return
	 */
	public VariableDecInterface searchVariableInEvalOfInnerPrototypes(String name) {

		/*
		 * the search order is: local variables, parameters, instance variables
		 * that are context parameters, and instance variables of the
		 * outer object (enclosingObjectDec)
		 */
		VariableDecInterface ret = this.searchLocalVariableParameter(name);

		if ( ret == null ) {
			ret = currentProgramUnit.searchInstanceVariableDec(name);
			if ( ret == null || !(ret instanceof ContextParameter) ) {
				/*
				 * found an instance variable of the inner prototype that is not
				 * a context parameter or did not find anything.
				 */
				ret = enclosingObjectDec.searchInstanceVariableDec(name);
			}
		}
		return ret;
	}

	/**
	 * search for a variable <code>name</code> inside an 'bindToFunction' method
	 * of a prototype representing a context function
	 *
	 * @param name
	 * @return
	 */
	public VariableDecInterface searchVariableInBindToFunction(String name) {

		/*
		 * the search order is: local variables, parameters, instance variables
		 * that are context parameters, and instance variables of the
		 * outer object (enclosingObjectDec)
		 */
		VariableDecInterface ret = this.searchLocalVariableParameter(name);

		if ( ret == null ) {
			ret = currentProgramUnit.searchInstanceVariableDec(name);
			if ( ret != null && ret instanceof ContextParameter ) ret = null;
		}
		return ret;
	}

	/**
	 * search for a variable <code>name</code> inside a method that is not an
	 * 'eval' or 'eval:eval: ...' method of an inner prototype created for a
	 * function or a method.
	 *
	 * @param name
	 * @return
	 */
	public VariableDecInterface searchVariableIn_NOT_EvalOfInnerPrototypes(String name) {
		/*
		 * the search order is: local variables, parameters, instance variables
		 * that are NOT context parameters.
		 */
		VariableDecInterface ret = this.searchLocalVariableParameter(name);

		if ( ret == null ) {
			ret = currentProgramUnit.searchInstanceVariableDec(name);
			if ( ret != null && (ret instanceof ContextParameter) ) {
				ret = null;
				/*
				 * found an instance variable of the inner prototype that is a
				 * context parameter
				 */
			}
		}
		return ret;
	}

	public InstanceVariableDec searchInstanceVariable(String name) {
		/*
		 * if ( enclosingObjectDec != null ) return
		 * enclosingObjectDec.searchInstanceVariableDec(name); else
		 */
		InstanceVariableDec iv = currentProgramUnit.searchInstanceVariableDec(name);
		if ( iv != null ) {
			return iv;
		}
		else {
			if ( currentProgramUnit instanceof ObjectDec ) {
				ObjectDec proto = ((ObjectDec) currentProgramUnit).getSuperobject();
				while (proto != null && proto != Type.Any) {
					iv = proto.searchInstanceVariable(name);
					if ( iv != null && iv.getVisibility() == Token.PROTECTED )
						return iv;
					else
						proto = proto.getSuperobject();
				}
			}
			return null;
		}
	}


	public Type createNewGenericPrototype(Symbol symUsedInError, CompilationUnitSuper compUnit, ProgramUnit currentPU,
			String fullPrototypeName, String errorMessage) {
		try {
			this.setPrefixErrorMessage(errorMessage);
			Expr newProgramUnit = saci.Compiler.parseSingleTypeFromString(fullPrototypeName,
					symUsedInError, errorMessage, compUnit, currentPU);
			newProgramUnit.calcInternalTypes(this);
			return newProgramUnit.getType();
		}
		catch ( CompileErrorException cee ) {
			this.setThereWasError(true);
		}
		finally {
			this.setPrefixErrorMessage(null);
		}
		return null;
	}


	/*
	 * public Project getCurrentProject() { return
	 * currentCompilationUnit.getCyanPackage().getProject(); }
	 */

	public ObjectDec getEnclosingObjectDec() {
		return enclosingObjectDec;
	}

	public void setEnclosingObjectDec(ObjectDec enclosingObjectDec) {
		this.enclosingObjectDec = enclosingObjectDec;
	}

	public Hashtable<String, ProgramUnit> getPublicProgramUnitTable() {
		return publicProgramUnitTable;
	}

	public Hashtable<String, CyanPackage> getConflictProgramUnitTable() {
		return conflictProgramUnitTable;
	}

	public CyanPackage searchPackage(String packageName) {
		return project.searchPackage(packageName);
	}

	public ProgramUnit searchPackagePrototype(String packageName, String prototypeName) {
		CyanPackage pack = project.searchPackage(packageName);
		if ( pack == null )
			return null;
		else
			return pack.searchPublicNonGenericProgramUnit(prototypeName);
	}

	@Override
	public ProgramUnit searchPackagePrototype(String packagePrototypeName, Symbol symUsedInError) {

		/*
		 * ProgramUnit singleTypeFromString(String typeAsString, Symbol
		 * symUsedInError, String message, CompilationUnit compUnit, ProgramUnit
		 * currentPU, Env env)
		 */

		return Compiler.singleTypeFromString(packagePrototypeName, symUsedInError,
				"Prototype '" + packagePrototypeName + "' was not found", this.currentCompilationUnit,
				this.currentProgramUnit, this);
	}

	public boolean isThereWasError() {
		return thereWasError.elem;
	}

	public HashSet<saci.CompilationInstruction> getCompInstSet() {
		return compInstSet;
	}

	public void setCompInstSet(HashSet<saci.CompilationInstruction> compInstSet) {
		this.compInstSet = compInstSet;
	}

	/**
	 * add context to the code codeToAdd. This context is composed by a push
	 * compilation context and a pop compilation context. It is considered that
	 * this code has type codeType if this parameter is not-null. Otherwise, if
	 * cyanMetaobject is an expression, it is considered that codeToAdd has type
	 * given by cyanMetaobject.getMetaobjectAnnotation().
	 *
	 * @param cyanMetaobject
	 * @param codeToAdd
	 * @param cyanMetaobjectAnnotation
	 * @return
	 */
	public static String getCodeToAddWithContext(CyanMetaobject cyanMetaobject, String codeToAdd, Type codeType) {
		CyanMetaobjectAnnotation cyanMetaobjectAnnotation = cyanMetaobject.getMetaobjectAnnotation();
		String name = cyanMetaobject.getName();
		String str;
		if ( cyanMetaobjectAnnotation.getInsideProjectFile() ) {
			str = "\"the project file \"";
		}
		else {
			str = cyanMetaobjectAnnotation.getPackageOfAnnotation();
		}

		String codeToAddWithContext;
		if ( codeType != null ) {
			Tuple2<String, String> packagePrototype = CompilerManager.separatePackagePrototype(codeType.getFullName());

			codeToAddWithContext = " @" + NameServer.pushCompilationContextName + "(" + "ati_id_" + Env.atiIdNumber
					+ ", \"" + name + "\", " + str + ", \""
					+ cyanMetaobjectAnnotation.getCompilationUnit().getFullFileNamePath() + "\", "
					+ cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + ") " + codeToAdd
					+ " @popCompilationContext(" + "ati_id_" + Env.atiIdNumber + ", \"" + packagePrototype.f1 + "\", \""
					+ packagePrototype.f2 + "\") \n";
		}
		else if ( cyanMetaobject.isExpression() ) {
			codeToAddWithContext = " @" + NameServer.pushCompilationContextName + "(" + "ati_id_" + Env.atiIdNumber
					+ ", \"" + name + "\", " + str + ", \""
					+ cyanMetaobjectAnnotation.getCompilationUnit().getFullFileNamePath() + "\", "
					+ cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + ") " + codeToAdd
					+ " @popCompilationContext(" + "ati_id_" + Env.atiIdNumber + ", \""
					+ cyanMetaobject.getPackageOfType() + "\", \"" + cyanMetaobject.getPrototypeOfType() + "\") \n";
		}
		else {
			codeToAddWithContext = " @" + NameServer.pushCompilationContextStatementName + "(" + "ati_id_"
					+ Env.atiIdNumber + ", \"" + name + "\", " + str + ", \""
					+ cyanMetaobjectAnnotation.getCompilationUnit().getFullFileNamePath() + "\", "
					+ cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber() + ") " + codeToAdd
					+ " @popCompilationContext(" + "ati_id_" + Env.atiIdNumber + ") \n";

		}
		++Env.atiIdNumber;

		/*
		 * msg = msg +
		 * " This error was caused by code introduced initially by metaobject '"
		 * + cyanMetaobjectName + "' called at line " + lineNumber + " of " +
		 * packageName + " of file " + sourceFileName;
		 *
		 */
		return codeToAddWithContext;
	}

	/**
	 * push a context used to issue errors in code generated by metaobjects
	 *
	 * @param id
	 * @param cyanMetaobjectName
	 * @param packageName
	 * @param prototypeName
	 * @param lineNumber
	 * @param lineNumberPushContext
	 *            line number of the annotation of metaobject
	 *            {@link meta#CyanMetaobjectCompilationContextPush}
	 */
	public void pushCompilationContext(String id, String cyanMetaobjectName, String packageName, String prototypeName,
			int lineNumber, int offsetPushContext) {
		cyanMetaobjectCompilationContextStack.push(new Tuple6<String, String, String, String, Integer, Integer>(id,
				cyanMetaobjectName, packageName, prototypeName, lineNumber, offsetPushContext));
		offsetPushCompilationContextStack.push(offsetPushContext);

	}

	/**
	 * pop a context used to issue errors in code generated by metaobjects
	 *
	 * @param id
	 * @param metaobjectSymbol
	 * @param offsetPopContext,
	 *            the offset inside the compilation unit of the annotation
	 *            {@literal @}popCompilationContext
	 * @return
	 */
	public boolean popCompilationContext(String id, Symbol metaobjectSymbol) {

		if ( cyanMetaobjectCompilationContextStack.isEmpty() ) {
			this.error(true, metaobjectSymbol,
					"Attempt to pop a context through @popCompilationContext in an empty stack", null,
					ErrorKind.metaobject_error);
			return false;
		}
		else {
			if ( !id.equals(cyanMetaobjectCompilationContextStack.peek().f1) ) {
				this.error(true, metaobjectSymbol,
						"Attempt to pop a context through @popCompilationContext with a wrong id. It should be '"
								+ cyanMetaobjectCompilationContextStack.peek().f1 + "'",
						null, ErrorKind.metaobject_error);
				return false;
			}
			else {
				/*
				 * the "+ 1" in the line below refer to the line of
				 *
				 * @popCompilationContext(ati_id_0) which just occupy just one
				 * line
				 */
				lineShift = lineShift + this.numLinesBetween(this.currentCompilationUnit.getText(),
						offsetPushCompilationContextStack.pop(), metaobjectSymbol.getOffset()) + 1;
				cyanMetaobjectCompilationContextStack.pop();
				return true;
			}
		}
	}

	/**
	 * return the number of lines between offsetPushContext and offsetPopContext
	 * of the current compilation unit
	 *
	 * @param f6
	 * @param offsetPopContext
	 * @return
	 */
	public int numLinesBetween(char[] myText, int offsetPushContext, int offsetPopContext) {
		int n = 0;
		int i = offsetPushContext;
		if ( offsetPopContext > myText.length ) return 0;
		while (i < offsetPopContext) {
			if ( myText[i] == '\n' ) ++n;
			++i;
		}
		return n;
	}

	/**
	 * return the number of lines of <code>s</code>
	 *
	 * @param s
	 * @return
	 */
	static public int numLinesOf(StringBuffer s) {
		int n = 0;
		int i = 0;
		while (i < s.length()) {
			if ( s.charAt(i) == '\n' ) ++n;
			++i;
		}
		return n;
	}

	public void writePrototypesToFile() {
		MyFile.writePrototypesToFile(compilationUnitToWriteList, "-full");
	}

	public void addCompilationUnitToWrite(CompilationUnit compilationUnit) {
		if ( compilationUnitToWriteList == null ) compilationUnitToWriteList = new HashSet<>();
		compilationUnitToWriteList.add(compilationUnit);
	}

	/**
	 * return a list of pairs (lineNumber, expectedMessage). Each pair gives the
	 * line number and an error message that the compiler should issue at that
	 * line of the current compilation unit.
	 *
	 * @return
	 */
	public ArrayList<Tuple3<Integer, String, Boolean>> getLineMessageList() {
		return lineMessageList;
	}

	/**
	 * push a metaobject annotation that implements IParseWithCyanCompiler_dpa or
	 * IParseMacro_dpa
	 */
	public void pushMetaobjectAnnotationParseWithCompiler(CyanMetaobjectAnnotation cyanMetaobjectAnnotation) {
		metaobjectAnnotationParseWithCompilerStack.push(cyanMetaobjectAnnotation);
	}

	/**
	 * pop a metaobject annotation that implements IParseWithCyanCompiler_dpa or
	 * IParseMacro_dpa
	 */
	public CyanMetaobjectAnnotation popMetaobjectAnnotationParseWithCompiler() {
		return metaobjectAnnotationParseWithCompilerStack.pop();
	}

	public int sizeStackMetaobjectAnnotationParseWithCompiler() {
		return metaobjectAnnotationParseWithCompilerStack.size();
	}

	public ProgramUnit getCyException() {
		if ( cyException == null ) {
			cyException = this.searchPackagePrototype(NameServer.cyanLanguagePackageName,
					NameServer.cyExceptionPrototype);
			if ( cyException == null ) this.error(null, "Prototype '" + NameServer.cyanLanguagePackageName + "."
					+ NameServer.cyExceptionPrototype + "' was not found", true, true);
		}
		return cyException;
	}

	public boolean getHasJavaCode() {
		return hasJavaCode;
	}

	public void setHasJavaCode(boolean hasJavaCode) {
		this.hasJavaCode = hasJavaCode;
	}

	public int getLexicalLevel() {
		return lexicalLevel;
	}

	public void setLexicalLevel(int lexicalLevel) {
		this.lexicalLevel = lexicalLevel;
	}

	public void addLexicalLevel() {
		++this.lexicalLevel;
	}

	public void subLexicalLevel() {
		--this.lexicalLevel;
	}

	public void pushVariableAndLevel(VariableDecInterface theVar, String varName) {
		stackVariableLevel.push(new Tuple3<VariableDecInterface, String, Integer>(theVar, varName, lexicalLevel));
	}

	public void removeVariablesLastLevel() {
		while (!stackVariableLevel.empty() && this.stackVariableLevel.peek().f3 == this.lexicalLevel) {
			this.stackVariableLevel.pop();
		}
	}

	public void clearStackVariableLevel() {
		this.stackVariableLevel.clear();
	}

	public String getCurrentClassNameWithOuter() {
		return currentClassNameWithOuter;
	}

	public void setCurrentClassNameWithOuter(String currentClassNameWithOuter) {
		this.currentClassNameWithOuter = currentClassNameWithOuter;
	}

	public boolean getCreatingInnerPrototypesInsideEval() {
		return creatingInnerPrototypesInsideEval;
	}

	public void setCreatingInnerPrototypesInsideEval(boolean creatingInnerPrototypesInsideEval) {
		this.creatingInnerPrototypesInsideEval = creatingInnerPrototypesInsideEval;
	}

	/**
	 * sets the string with code to initialize reference variables, which are
	 * variables referred by anonymous functions
	 *
	 * @param refVarInitStr
	 */
	public void setStrInitRefVariables(String strInitRefVariables) {
		this.strInitRefVariables = strInitRefVariables;
	}

	public String getStrInitRefVariables() {
		return strInitRefVariables;
	}

	public void setIsInsideInitMethod(boolean isInsideInitMethod) {
		this.isInsideInitMethod = isInsideInitMethod;
	}

	public boolean getIsInsideInitMethod() {
		return isInsideInitMethod;
	}

	public Stack<Tuple3<VariableDecInterface, String, Integer>> getStackVariableLevel() {
		return stackVariableLevel;
	}

	public Stack<VariableDecInterface> getVariableDecStack() {
		return variableDecStack;
	}

	public void addToLineShift(int toAdd) {
		lineShift += toAdd;
	}

	public void setThereWasError(boolean thereWasError) {
		this.thereWasError.elem = thereWasError;
	}

	public boolean getDuring_dsa_actions() {
		return during_dsa_actions;
	}

	public ArrayList<ProgramUnit> getProgramUnitForGenericPrototypeList() {
		return programUnitForGenericPrototypeList;
	}

	public void setProgramUnitForGenericPrototypeList(ArrayList<ProgramUnit> programUnitForGenericPrototypeList) {
		this.programUnitForGenericPrototypeList = programUnitForGenericPrototypeList;
	}

	/**
	 * if the current program unit was created from a generic prototype
	 * instantiation, the instantiation is in package packageNameInstantiation,
	 * prototype prototypeNameInstantiation, line number
	 * lineNumberInstantiation, and column number columnNumberInstantiation. The
	 * methods below are the getters and setters for these variables. In regular
	 * prototypes packageNameInstantiation and prototypeNameInstantiation are
	 * null.
	 */

	public String getPackageNameInstantiation() {
		if ( currentCompilationUnit == null ) {
			return null;
		}
		return currentCompilationUnit.getPackageNameInstantiation();
	}

	public void setPackageNameInstantiation(String packageNameInstantiation) {
		if ( currentCompilationUnit == null ) {
			error(null, "Attempt to set package name of a prototype instantiation outside a compilation unit");
			return;
		}
		currentCompilationUnit.setPackageNameInstantiation(packageNameInstantiation);
	}

	public String getPrototypeNameInstantiation() {
		if ( currentCompilationUnit == null ) {
			return null;
		}
		return currentCompilationUnit.getPrototypeNameInstantiation();
	}

	public void setPrototypeNameInstantiation(String prototypeNameInstantiation) {
		if ( currentCompilationUnit == null ) {
			error(null, "Attempt to set prototype name of a prototype instantiation outside a compilation unit");
			return;
		}
		currentCompilationUnit.setPrototypeNameInstantiation(prototypeNameInstantiation);
	}

	public int getLineNumberInstantiation() {
		if ( currentCompilationUnit == null ) {
			return -1;
		}
		return currentCompilationUnit.getLineNumberInstantiation();
	}

	public void setLineNumberInstantiation(int lineNumberInstantiation) {
		if ( currentCompilationUnit == null ) {
			error(null, "Attempt to set the line number of a prototype instantiation outside a compilation unit");
			return;
		}
		currentCompilationUnit.setLineNumberInstantiation(lineNumberInstantiation);
	}

	public int getColumnNumberInstantiation() {
		if ( currentCompilationUnit == null ) {
			return -1;
		}
		return currentCompilationUnit.getColumnNumberInstantiation();
	}

	public void setColumnNumberInstantiation(int columnNumberInstantiation) {
		if ( currentCompilationUnit == null ) {
			error(null, "Attempt to set the column number of a prototype instantiation outside a compilation unit");
			return;
		}
		currentCompilationUnit.setColumnNumberInstantiation(columnNumberInstantiation);
	}

	/**
	 * stack with the variables initialize in each lexical level. It is
	 * something like<br>
	 * <code>
	 * [ [. objectFor_i, "i", 0 .], [. objectFor_j, "j", 0 .], [. objectFor_k, "k", 1 .], [. objectFor_name, "name", 2 .] ]
	 * </code><br>
	 * using the Cyan syntax for arrays and tuples
	 */
	private Stack<Tuple3<VariableDecInterface, String, Integer>> stackVariableLevel;

	/**
	 * used to discover if some variable was not initialized
	 */
	private int lexicalLevel;

	/**
	 * points to
	 */
	private ProgramUnit cyException;

	/**
	 * a table of local variables, which includes method parameters, function
	 * parameters, and function local variables.
	 */
	private Hashtable<String, VariableDecInterface>	variableDecTable;

	/**
	 * a stack of local variables, which includes method parameters, function
	 * parameters, and function local variables.
	 */
	private Stack<VariableDecInterface> variableDecStack;

	/**
	 * contains instance variables declared in the current prototype
	 * (that being analyzed at this moment)
	 */
	private Hashtable<String, SlotDec> slotDecTable;

	/**
	 * contains all private ProgramUnits of the current compilation Unit. That
	 * is, of the current file being compiled.
	 */
	private Hashtable<String, ProgramUnit> privateProgramUnitTable;

	/**
	 * contains all public Program Units (objects and interfaces) imported by
	 * the compilation Unit (a file) being analyzed. The importation is made by
	 * method loadPackage
	 */
	private Hashtable<String, ProgramUnit> publicProgramUnitTable;

	/**
	 * contains all public program units (prototypes) imported by the current
	 * compilation unit from two or more packages.
	 */
	private Hashtable<String, CyanPackage> conflictProgramUnitTable;

	/**
	 * contains all source files names of packages imported by the compilation
	 * Unit (a file) being analyzed. The importation is made by method
	 * loadPackage
	 */
	private Hashtable<String, ProgramUnit> publicSourceFileNameTable;

	/**
	 * if this object or interface is generic, genericObjectInterfaceTable is
	 * the list of the generic parameters. It would contain T and R in the
	 * object object Table<:T, :R> ... end
	 */
	private Hashtable<String, GenericParameter>	genericPrototypeFormalParameterTable;

	/**
	 * current compilation unit.
	 *
	 */
	private CompilationUnit	currentCompilationUnit;

	/**
	 * the list of compilation units with the errors in each of them signalled using Env
	 */
	private Map<CompilationUnit, ArrayList<UnitError>> mapCompUnitErrorList;
	/**
	 * the project. Used to find the imported packages of an interface or object
	 * declaration.
	 */
	private Project	project;

	/**
	 * current object or interface. That is, the object that is being checked at
	 * this moment, if there is one. null if what is being checked is not an
	 * object.
	 */
	private ProgramUnit	currentProgramUnit;

	/**
	 * current method being compiled, if the checking is made inside a method.
	 * null otherwise.
	 */
	private MethodDec currentMethod;


	/**
	 * list of functions declared in the current compilation unit
	 */
	private ArrayList<ExprFunction>	functionList;
	/**
	 * stack of statements. The current statement should be passed as parameter
	 * to method signalCompilerError of class SignalCompilerError.
	 */
	private Stack<CodeWithError> statementStack;
	/**
	 * stack of functions. At the end of the code generation/checking of a
	 * function, it is removed from this stack.
	 *
	 */
	private Stack<ExprFunction> functionStack;

	/**
	 * For each Function the compiler creates a brand new prototype that
	 * inherits something as Function<R, T>. However, inside this prototype it
	 * is legal to access the instance variables and methods of the enclosing
	 * prototype of the function (including the inherited ones). This variable
	 * is a link to this enclosing prototype. Methods and instance variables are
	 * searched for first in this link if it is non-null
	 */
	private ObjectDec enclosingObjectDec;

	/**
	 * true if there was an error during the compilation
	 */
	private Ref<Boolean> thereWasError = new Ref<>();

	/**
	 * the instruction set to the compilation
	 */
	private HashSet<saci.CompilationInstruction> compInstSet;

	/**
	 * the context of the code generated by a metaobject annotation. The elements of
	 * each tuple are: an identifier, the metaobject name, the package name of
	 * the metaobject annotation, the prototype name of the metaobject annotation, and the
	 * line number of the call. If this stack is not empty and there is a
	 * compilation error, then the code that caused the error was introduced by
	 * a metaobject annotation. This code was generated by a metaobject annotation in the
	 * prototype specified in the tuple. That is, the statement
	 * </p>
	 * <code>cyanMetaobjectCompilationContextStack.push(new Tuple5<...>(...))</code>
	 * </p>
	 * is called before the compilation of the code generated by the metaobject
	 * and
	 * </p>
	 * <code>cyanMetaobjectCompilationContextStack.pop()</code>
	 * </p>
	 * is called after the compilation of the code generated by the metaobject.
	 * The compiler itself, when generating code for a metaobject, inserts annotations
	 *  <code>{@literal @}pushCompilationContext</code> and
	 * <code>{@literal @}popCompilationContext</code>.
	 *
	 * The last number in each tuple is the offset in the source code of the
	 * first character of the annotation
	 * <code>{@literal @}pushCompilationContext</code>
	 */
	private Stack<Tuple6<String, String, String, String, Integer, Integer>>	cyanMetaobjectCompilationContextStack;

	/**
	 * number for the next identifier of pushCompilationContext. The identifiers
	 * will be "ati_id_" + atiIdNumber
	 */
	public static int														atiIdNumber	= 0;

	/**
	 * Code may be inserted into a compilation unit by the compiler itself (see
	 * Figure of chapter Metaobjects of the Cyan manual) or by metaobject annotations.
	 * Code may be deleted from a compilation unit by metaobject annotations.
	 * Therefore when the compiler finds an error it may point to the wrong
	 * line. If code was inserted before the error the message would point to a
	 * line number greater than it is. The opposite happens when code was
	 * deleted. Variable lineShift keeps how many lines were inserted in the
	 * code. If negative, -lineShift is how many lines were deleted.
	 */
	private int																lineShift;
	/**
	 * list of pairs <code>(lineNumber, errorMessage, used)</code>. A metaobject
	 * that is an instance of {@link meta#IInformCompilationError} signaled that
	 * there should be an error in this source file (being compiled) at line
	 * <code>lineNumber</code>. The possible error message is
	 * <code>errorMessage</code>. 'used' is true if this error has been signaled
	 * by the compiler
	 */
	private ArrayList<Tuple3<Integer, String, Boolean>>						lineMessageList;

	/**
	 * a stack of literal objects. In <br>
	 * <code>
	 *    [* [* 1, 2 *] [* 3 *] *] <br>
	 * </code> the stack will have two elements when calculating the internal
	 * types of '2'. These literal objects include all metaobject annotations of
	 * metaobjects that implement IParseWithCyanCompiler_dpa or IParseMacro_dpa.
	 */
	private Stack<CyanMetaobjectAnnotation>										metaobjectAnnotationParseWithCompilerStack;

	/**
	 * A metaobject annotation such as<br>
	 * <code>
	 * Function<String>.#writeCode
	 * </code> demand that the source code of Function<String> be written in the
	 * directory in which this prototype is. The set below keeps all compilation
	 * units prototypes for which the source code should be written to a file.
	 * Of course, this only makes sense because metaobjects and the Cyan
	 * compiler adds code to prototypes.
	 */
	private HashSet<CompilationUnit>										compilationUnitToWriteList;

	/**
	 * true if a method being analyzed has inside it a metaobject javacode. It
	 * it has, the compiler cannot deduce if the method will ever return a value
	 * or not
	 */
	private boolean															hasJavaCode;

	/**
	 * the name of a inner class representing a function or a method with the
	 * outer class name. For example, it may be "_Program._Fun_0__"
	 */
	private String															currentClassNameWithOuter;

	/**
	 * true if Env is being used for generating Java code for method 'eval' or
	 * 'eval:' of an inner prototype
	 */
	private boolean															creatingInnerPrototypesInsideEval;

	private String															strInitRefVariables;

	/**
	 * true if the compiler is inside an 'init' or 'init:' method during code
	 * generation
	 */
	private boolean															isInsideInitMethod;

	public void setFirstMethodStatement(boolean b) {
		firstMethodStatement = b;
	}

	public boolean getFirstMethodStatement() {
		return this.firstMethodStatement;
	}
	/*
	 * inside an 'init' or 'init:' method, only the first statement can be a
	 * call 'super init' or 'super init: a, b, c'. The flag firstMethodStatment
	 * is used to check this.
	 *
	 */

	private boolean firstMethodStatement = false;

	public void begin_dsa_actions() {
		during_dsa_actions = true;
	}

	public void end_dsa_actions() {
		during_dsa_actions = false;

	}

	public boolean isInPackageCyanLang(String name) {
		return this.project.getProgram().isInPackageCyanLang(name);
	}

	/**
	 * true if the compiler is during some dsa action. That is, during semantic
	 * analysis in the statements of methods the compiler is adding or removing
	 * code. This is used to prevent, for example, a macro calling inside it
	 * another macro or a literal expression
	 */
	private boolean					during_dsa_actions;

	/**
	 * stack with the line numbers that start a pushCompilationContext
	 */
	private Stack<Integer>			offsetPushCompilationContextStack;

	/**
	 * a list of non-existing prototypes, one for each generic parameter. This
	 * is necessary for metaobject concept
	 */
	private ArrayList<ProgramUnit>	programUnitForGenericPrototypeList;

	public void calculateSubtypes() {
		HashMap<String, Set<ProgramUnit>> m;
		m = mapPackageSpaceCyanTypeNameToSubtypeList = new HashMap<String, Set<ProgramUnit>>();
		for (CyanPackage p : this.getProject().getPackageList()) {
			String packageName = p.getPackageName() + " ";
			for (CompilationUnit cunit : p.getCompilationUnitList()) {
				ProgramUnit pu = cunit.getPublicPrototype();
				if ( !pu.isGeneric() ) {
					m.put(packageName + pu.getName(), new HashSet<ProgramUnit>());
				}
			}
		}

		for (CyanPackage p : this.getProject().getPackageList()) {
			for (CompilationUnit cunit : p.getCompilationUnitList()) {
				ProgramUnit pu = cunit.getPublicPrototype();
				if ( !pu.isGeneric() ) {
					if ( pu instanceof ObjectDec ) {
						ObjectDec obj = (ObjectDec) pu;
						if ( obj.getSuperobject() != null ) {
							ObjectDec superObj = obj.getSuperobject();
							Set<ProgramUnit> set = m
									.get(superObj.getCompilationUnit().getPackageName() + " " + superObj.getName());
							if ( set == null ) {
								this.error(null, "Internal error: prototype '" + superObj.getFullName()
										+ "' was not found in calculateSubtypes()");
							}
							else {
								// set is the set of subtypes of 'obj'
								// super-type
								set.add(pu);
							}
						}
						ArrayList<Expr> exprInterList = obj.getInterfaceList();
						if ( exprInterList != null ) {
							for (Expr exprInter : exprInterList) {
								InterfaceDec superInter = (InterfaceDec) exprInter.getType();
								if ( superInter == null ) {
									this.error(null, "Internal error: superinterface of prototype '" + pu.getFullName()
											+ "' was not found in calculateSubtypes()");
								}
								else {
									Set<ProgramUnit> set = m.get(superInter.getCompilationUnit().getPackageName() + " "
											+ superInter.getName());
									if ( set == null ) {
										this.error(null, "Internal error: prototype '" + superInter.getFullName()
												+ "' was not found in calculateSubtypes()");
									}
									else {
										// set is the set of subtypes of 'obj'
										// super-type
										set.add(pu);
									}
								}

							}
						}
					}
					else if ( pu instanceof InterfaceDec ) {
						InterfaceDec inter = (InterfaceDec) pu;
						if ( inter.getSuperInterfaceList() != null ) {
							for (InterfaceDec superInter : inter.getSuperInterfaceList()) {
								Set<ProgramUnit> set = m.get(
										superInter.getCompilationUnit().getPackageName() + " " + superInter.getName());
								if ( set == null ) {
									this.error(null, "Internal error: prototype '" + superInter.getFullName()
											+ "' was not found in calculateSubtypes()");
								}
								else {
									// set is the set of subtypes of 'obj'
									// super-type
									set.add(pu);
								}

							}
						}
					}
				}
			}
		}

	}

	public int getLineShift() {
		return lineShift;
	}

	public void setLineShift(int lineShift) {
		this.lineShift = lineShift;
	}


	/**
	 * return a map with a key for each prototype or interface. The value for
	 * the key is a set with all direct subtypes of the prototype or interface.
	 * This map is only created on demand. The key has the format: the package
	 * name, a single space, prototype name. It can be, for example,<br>
	 * <code>
	 * "br.main Program"
	 * </code><br>
	 * The package name is "br.main" and the prototype name is "Program".
	 *
	 */

	public HashMap<String, Set<ProgramUnit>> getMapPrototypeSubtypeList() {
		if ( mapPackageSpaceCyanTypeNameToSubtypeList == null ) {
			calculateSubtypes();
			/*
			 * try { } catch ( RuntimeException e ) { e.printStackTrace(); }
			 */
		}
		return mapPackageSpaceCyanTypeNameToSubtypeList;
	}

	// throw new ExceptionContainer__(new _ExceptionCast(
	// "Cannot cast expression '(`sprint: \"aa\", 'A' `swith: 0, 3.14)' of line '62' of file 'Program.cyan' to cyan.lang.Any") );
	public String javaCodeForCastException(Expr exprToCast, Type leftType) {
		return  "new _ExceptionCast( new CyString(\"Cannot cast expression '" + Lexer.escapeJavaString(exprToCast.asString()) +
				"' to '" + leftType.getFullName() +
				"' in line " +
	            exprToCast.getFirstSymbol().getLineNumber() + " of file "
				+ getCurrentCompilationUnit().getFilename() +  "\") )";
	}

	public String javaCodeForCastException(VariableDecInterface varToCast, Type leftType) {
		return  "new _ExceptionCast( new CyString(\"Cannot cast variable '" + varToCast.getName() + " to '" + leftType.getFullName() + "' in line " +
	            varToCast.getFirstSymbol().getLineNumber() + "' of file '"
				+ getCurrentCompilationUnit().getFilename() +  "\") )";
	}


	/**
	 * a set of all subtype of each program prototype or interface. This list is
	 * only created on demand. The key to this map is the package name, space,
	 * prototype name. It can be, for example,<br>
	 * <code>
	 * "br.main Program"
	 * </code><br>
	 * The package name is "br.main" and the prototype name is "Program".
	 *
	 */
	HashMap<String, Set<ProgramUnit>>	mapPackageSpaceCyanTypeNameToSubtypeList;

	/**
	 * get a set of all sub prototypes of each program prototype (interfaces
	 * excluded). This list is only created on demand. The key to this map is
	 * the package name, space, prototype name. It can be, for example,<br>
	 * <code>
	 * "br.main Program"
	 * </code><br>
	 * The package name is "br.main" and the prototype name is "Program".
	 *
	 */
	HashMap<String, Set<ObjectDec>>		mapPackageSpacePrototypeNameToSubprototypeList;

	/**
	 * get a set of all sub interfaces of each program interface. This list is
	 * only created on demand. The key to this map is the package name, space,
	 * interface name. It can be, for example,<br>
	 * <code>
	 * "br.main IType"
	 * </code><br>
	 * The package name is "br.main" and the interface name is "IType".
	 *
	 */
	HashMap<String, Set<ObjectDec>>		mapPackageSpaceInterfaceNameToSubinterfaceList;

	/**
	 * get a set of all prototypes that implement a given interface. This list
	 * is only created on demand. The key to this map is the package name,
	 * space, interface name. It can be, for example,<br>
	 * <code>
	 * "br.main IType"
	 * </code><br>
	 * The package name is "br.main" and the interface name is "IType".
	 *
	 */
	HashMap<String, Set<ObjectDec>>		mapPackageSpaceInterfaceNameToImplementedList;

	public void setPrefixErrorMessage(String errorMessage) {
		prefixErrorMessage = errorMessage;
	}

	/**
	 * message to be added in front of the first error message the Env issue
	 */
	private String prefixErrorMessage;

	public Map<CompilationUnit, ArrayList<UnitError>> getMapCompUnitErrorList() {
		return mapCompUnitErrorList;
	}

	public boolean getAllowCreationOfPrototypesInLastCompilerPhases() {
		return allowCreationOfPrototypesInLastCompilerPhases;
	}

	public void setAllowCreationOfPrototypesInLastCompilerPhases(boolean allowCreationOfPrototypesInLastCompilerPhases) {
		this.allowCreationOfPrototypesInLastCompilerPhases = allowCreationOfPrototypesInLastCompilerPhases;
	}

	public boolean getTopLevelStatements() {
		return topLevelStatements;
	}

	public void setTopLevelStatements(boolean topLevelStatements) {
		this.topLevelStatements = topLevelStatements;
	}

	/**
	 * true if the compiler is allowed to create instantiations of generic prototypes in
	 * compilation phases >= 7.
	 */
	private boolean allowCreationOfPrototypesInLastCompilerPhases;


	/**
	 * true if the statements currently being analyzed are the top level statements of a method
	 */
	private boolean topLevelStatements = true;

}
