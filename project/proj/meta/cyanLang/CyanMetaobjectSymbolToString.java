package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;
import saci.NameServer;

public class CyanMetaobjectSymbolToString extends CyanMetaobjectWithAt implements IAction_dpa {

	
	public CyanMetaobjectSymbolToString() {
		super(MetaobjectArgumentKind.OneParameter);
	}
	
	@Override
	public String getName() {
		return "symbolToString";
	}
	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		if ( !(((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList().get(0) instanceof String) ) {
			return addError("Parameter to this metaobject " + getName() + " should be an identifier or a literal string");
		}
		else
			return null;
	}


	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
		String s = (String ) ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList().get(0);
		s = NameServer.removeQuotes(s);
		return new StringBuffer(NameServer.addQuotes(s));
	}


	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	@Override
	public String getPrototypeOfType() {
		return "String";
	}

	@Override
	public boolean isExpression() {
		return true;
	}
	
	
}
