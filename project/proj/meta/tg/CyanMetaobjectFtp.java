package meta.tg;

import meta.CyanMetaobjectLiteralString;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectFtp extends CyanMetaobjectLiteralString implements IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectFtp() {
		super();
	}
	@Override
	public String[] getPrefixNames() {
		return new String[] { "ftp", "FTP" };
	}
	

	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {

		String urlPattern = "[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
		String ipPattern = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	    if (code.matches(urlPattern)){	  	
		
	    	setInfo(new StringBuffer("\"" + code + "\""));
	    }
	    else if(code.matches(ipPattern)){
	    	setInfo(new StringBuffer("\"" + code + "\""));
	    } 
	    else{
	    	addError("Invalid ftp address, must be a valid URL or IP");
	    }
	}
	
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		return (StringBuffer ) getInfo();
	}
	
	
	@Override
	public String getPackageOfType() { return "cyan.lang"; }

	@Override
	public String getPrototypeOfType() {
		return "String";
	}
	@Override
	public boolean isExpression() {
		return true;
	}
	
}
