package meta.tg;

import meta.CyanMetaobjectNumber;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectNumberColor extends CyanMetaobjectNumber implements IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectNumberColor(){
		super();
	}
	
	@Override
	public String[] getSuffixNames() {
		return new String[] { "Color", "COLOR", "color" };
	}
	


	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		String colorPattern = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\_){2}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
				
		if(code.matches(colorPattern)){
			setInfo(new StringBuffer("\"" + code + "\""));
		}
		else{
			addError("Invalid color format, should be red_green_blue");
		}
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
	
}
