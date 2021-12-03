package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import lexer.Symbol;
import meta.Compiler_dpa;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IAction_cge;
import meta.IAction_dpa;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import saci.Env;
import saci.NameServer;

public class CyanMetaobjectGenericPrototypeInstantiationInfo extends CyanMetaobjectWithAt 
          implements IAction_dpa, ICheckProgramUnit_dsa2, IAction_cge {

	public CyanMetaobjectGenericPrototypeInstantiationInfo() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		if ( annotation.getJavaParameterList().size() != 4 ) {
			return addError("four parameters was expected for metaobject '" + this.getName() + "'");
		}
		return null;
	}

	@Override
	public String getName() {
		return "genericPrototypeInstantiationInfo";
	}

	@Override
	public StringBuffer cge_codeToAdd() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();
		String packageNameInstantiation = NameServer.removeQuotes((String ) javaParamList.get(0));
		String prototypeNameInstantiation = NameServer.removeQuotes( (String ) javaParamList.get(1));
		int lineNumber = (Integer ) javaParamList.get(2);
		int columnNumber = (Integer ) javaParamList.get(3);
		
		return new StringBuffer("/* this generic prototype was created because of a type that is in \n    "
				+ packageNameInstantiation + "." + prototypeNameInstantiation + " at line " + lineNumber + " column "
				+ columnNumber + " */");
	}



	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler_dpa) {
		
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Symbol sym = annotation.getFirstSymbol();
		int errorLine = sym.getLineNumber();
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();
		if ( ! (javaParamList.get(0) instanceof String) ) {
			compiler_dpa.error(errorLine, "A package name, as String, was expected as first parameter to this metaobject annotation");
		}
		if ( ! (javaParamList.get(1) instanceof String) ) {
			compiler_dpa.error(errorLine, "A prototype name, as String, was expected as second parameter to this metaobject annotation");
		}
		if ( ! (javaParamList.get(2) instanceof Integer) ) {
			compiler_dpa.error(errorLine, "A line number was expected as third parameter to this metaobject annotation");
		}
		if ( ! (javaParamList.get(3) instanceof Integer) ) {
			compiler_dpa.error(errorLine, "A column number was expected as fourth parameter to this metaobject annotation");
		}
		String packageNameInstantiation = NameServer.removeQuotes((String ) javaParamList.get(0));
		String prototypeNameInstantiation = NameServer.removeQuotes( (String ) javaParamList.get(1));
		int lineNumber = (Integer ) javaParamList.get(2);
		int columnNumber = (Integer ) javaParamList.get(3);
		/*
		 * this cast is to hide these methods from the regular programmer. This cast
		 * can be made illegal at any time by the compiler designer. 
		 */
		Compiler_dpa compiler = (Compiler_dpa ) compiler_dpa;
		compiler.setPackageNameInstantiation(packageNameInstantiation);
		compiler.setPrototypeNameInstantiation(prototypeNameInstantiation);
		compiler.setLineNumberInstantiation(lineNumber);
		compiler.setColumnNumberInstantiation(columnNumber);
		return null;
	}

	@Override
	public void dsa2_checkProgramUnit(ICompiler_dsa compiler_dsa) {
		/*
		 * this cast is to hide these methods from the regular programmer. This cast
		 * can be made illegal at any time by the compiler designer. 
		 */

		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();
		String packageNameInstantiation = NameServer.removeQuotes((String ) javaParamList.get(0));
		String prototypeNameInstantiation = NameServer.removeQuotes( (String ) javaParamList.get(1));
		int lineNumber = (Integer ) javaParamList.get(2);
		int columnNumber = (Integer ) javaParamList.get(3);

		Env env = compiler_dsa.getEnv();
		
		/*
		if ( env.getCurrentProgramUnit() != null  ) {
			packageNameInstantiation += " / " + env.getCurrentProgramUnit().getCompilationUnit().getPackageName();
			prototypeNameInstantiation += " / " + env.getCurrentProgramUnit().getName();
		}
		*/
		env.setPackageNameInstantiation(packageNameInstantiation  );
		env.setPrototypeNameInstantiation(prototypeNameInstantiation );
		env.setLineNumberInstantiation(lineNumber);
		env.setColumnNumberInstantiation(columnNumber);
		
	}
	
}
