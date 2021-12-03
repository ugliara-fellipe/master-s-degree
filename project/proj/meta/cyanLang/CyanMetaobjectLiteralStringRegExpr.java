package meta.cyanLang;

import meta.CyanMetaobjectLiteralString;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectLiteralStringRegExpr extends CyanMetaobjectLiteralString 
       implements IParseWithoutCyanCompiler_dpa  {

	public CyanMetaobjectLiteralStringRegExpr() {
		super();
	}
	@Override
	public String[] getPrefixNames() {
		return new String[] { "r", "R" };
	}


	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		
		//String t = code.substring(2, code.length() - 1);
		try {
			java.util.regex.Pattern.compile(code);
		}
		catch (java.util.regex.PatternSyntaxException e) {
			addError("Pattern is not well defined");
		}
		setInfo( new StringBuffer( "RegExpr(\"" + code + "\")") );
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		return (StringBuffer ) getInfo();
	}
	
	@Override
	public String getPackageOfType() {
		return "cyan.lang";
	}
	@Override
	public String getPrototypeOfType() {
		return "RegExpr";
	}

}
