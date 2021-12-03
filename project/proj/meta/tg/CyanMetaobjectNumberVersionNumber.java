package meta.tg;

import meta.CyanMetaobjectNumber;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;

public class CyanMetaobjectNumberVersionNumber extends CyanMetaobjectNumber implements meta.IParseWithoutCyanCompiler_dpa {
	public CyanMetaobjectNumberVersionNumber(){
		super();
	}

	@Override
	public String[] getSuffixNames() {
		return new String[] { "version", "VERSION", "Version" };
	}



	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		
		int n = code.length();
		String versionNumber = new String();
		String auxNum = new String();
		
		for(int i = 0; i < n; i++){
			char c = code.charAt(i);
			if(Character.isDigit(c)){
				auxNum += c;
				if(i == n - 1) versionNumber += auxNum;
			}else{
				if(c != '_' ){
					
					if ( code.substring(i).equalsIgnoreCase(this.getSuffixNames()[0]) ) {
						versionNumber += auxNum;
						break;
					}
					addError("Malformed version metaobject");
					return ;
				}else{
					versionNumber += auxNum;
					versionNumber += ".";
					auxNum = "";
				}
			}
		}

		this.setInfo(new StringBuffer("\"" + versionNumber + "\""));

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