package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;

/**
 * A metaobject annotation of this metaobject supports the parameters filename, prototypename, packagename, linenumber,
 *  localvariablelist, instancevariablelist, and signatureallmethodslist.
 *
   @author José
 */
public class CyanMetaobjectLineNumber extends CyanMetaobjectWithAt
       implements IAction_dpa {

	public CyanMetaobjectLineNumber() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		return null;
	}

	@Override
	public String getName() {
		return "lineNumber";
	}



	@Override
	public StringBuffer dpa_codeToAdd( ICompilerAction_dpa compiler ) {
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
		return new StringBuffer("" +  cyanMetaobjectAnnotation.getSymbolMetaobjectAnnotation().getLineNumber() );
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
			return "Int";
	}

	@Override
	public boolean isExpression() {
		return true;
	}
}

