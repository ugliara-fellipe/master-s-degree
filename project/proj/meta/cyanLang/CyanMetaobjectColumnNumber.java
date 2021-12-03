package meta.cyanLang;


import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
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
public class CyanMetaobjectColumnNumber extends CyanMetaobjectWithAt 
       implements IAction_dpa {
	
	public CyanMetaobjectColumnNumber() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}


	@Override
	public String getName() {
		return "columnNumber";
	}


	@Override
	public StringBuffer dpa_codeToAdd( ICompilerAction_dpa compiler ) {
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();

		return new StringBuffer("\"" +  "" + cyanMetaobjectAnnotation.getSymbolMetaobjectAnnotation().getColumnNumber() + "\"");

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

