package meta.tg;

import meta.CyanMetaobjectLiteralString;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectLiteralStringWeb extends CyanMetaobjectLiteralString implements IParseWithoutCyanCompiler_dpa{
	public CyanMetaobjectLiteralStringWeb(){
		super();
	}

	@Override
	public String[] getPrefixNames() {
		return new String[] { "web", "WEB", "Web" };
	}



	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		String regexURL = "^(?!mailto:)(?:(?:http|https|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$";
		
		if(!code.matches(regexURL)){
			addError("The given URL is not valid.");
		}

		setInfo(new StringBuffer("\"" + code + "\""));
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
		return "String";
	}
	
	@Override
	public boolean isExpression(){
		return true;
	}
}