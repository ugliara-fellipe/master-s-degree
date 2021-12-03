package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.ICompilerAction_dpa;

public class CyanMetaobjectIssueCompilerError extends CyanMetaobjectWithAt 
   implements meta.IAction_dpa {

	public CyanMetaobjectIssueCompilerError() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		if ( !(annotation.getJavaParameterList().get(0) instanceof String) ) {
			return addError("A string is expected as the parameter of this metaobject annotation");
		}
		return null;
	}

	@Override
	public String getName() {
		return "error";
	}



	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		String msg = (String ) annotation.getJavaParameterList().get(0);
		compiler.error(this.metaobjectAnnotation.getFirstSymbol().getLineNumber(), msg);
		return null;
	}

}
