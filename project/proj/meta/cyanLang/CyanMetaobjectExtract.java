package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dsa;
import saci.NameServer;

/**
 * this metaobject should be used as<br>
 * <code>
 *      {@literal @}extract(int100) <br>
 *      {@literal @}extract(int_500) <br>
 * </code>
 * These values are transformed in 100 and 500 by extract.
 * 
   @author jose
 */
public class CyanMetaobjectExtract extends CyanMetaobjectWithAt implements IAction_dsa {

	public CyanMetaobjectExtract() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public String getName() {
		return "extract";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa)  {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		Object first = annotation.getJavaParameterList().get(0);
		boolean error = false;
		if ( ! (first instanceof String) ) {
			error = true;
		}
		Integer n = -1;
		String s = NameServer.removeQuotes((String ) first);
		if ( s.charAt(0) == '\"' || s.charAt(s.length()-1) == '\"' || ! s.startsWith(strInt) || s.length() < sizeInt + 1 ) {
			error = true;
		}
		else {
			String strNum;
			if ( s.charAt(sizeInt) == '_' ) {
				strNum = s.substring(sizeInt + 1);
			}
			else {
				strNum = s.substring(sizeInt);
			}
			try {
				n = Integer.valueOf(strNum);
			}
			catch ( NumberFormatException e ) {
				error = true;
			}
		}
		if ( error ) {
			this.addError("The first parameter to this metaobject annotation should be an identifier in the format '" + strInt + 
					"x' or '" + strInt + "_x' in which 'x' is a number");
			return null;
		}
		else {
			return new StringBuffer("" + n);
		}
	}

	private final static String strInt = "int";
	private final static int sizeInt = strInt.length();
	@Override
	public boolean isExpression() {
		return true;
	}

	

	@Override
	public String getPackageOfType() { return NameServer.cyanLanguagePackageName; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	@Override
	public String getPrototypeOfType() { return "Int"; }		
}
