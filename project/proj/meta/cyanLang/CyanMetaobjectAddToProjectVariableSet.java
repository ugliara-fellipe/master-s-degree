package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpp;
import meta.ICompiler_dpp;

public class CyanMetaobjectAddToProjectVariableSet extends CyanMetaobjectWithAt 
		implements IAction_dpp {


	public CyanMetaobjectAddToProjectVariableSet() { 
		super(MetaobjectArgumentKind.TwoParameters);
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		if ( ! (paramList.get(0) instanceof String) ) {
			return this.addError("A variable name was expected as the first parameter to this metaobject annotation");
		}
		else {
			String varName = (String ) paramList.get(0);
			int size = varName.length();
			for (int i = 0; i < size; ++i) {
				char ch = varName.charAt(i);
				if ( !Character.isAlphabetic(ch) || !Character.isDigit(ch) || ch != '_' ) {
					return this.addError("Character '" + ch + "' is not allowed in a variable name. An identifier was expected as the first parameter to this metaobject annotation");
				}
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "addToSet";
	}


	@Override
	public void dpp_action(ICompiler_dpp project) {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		String varName = (String ) paramList.get(0);
		String value = (String ) paramList.get(0);
		project.addToProjectSet(varName, value);
	}

}
