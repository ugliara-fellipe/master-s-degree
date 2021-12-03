package meta.util;

import meta.CyanMetaobjectNumber;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;

public class CyanMetaobjectKg  extends CyanMetaobjectNumber 
    implements meta.IParseWithoutCyanCompiler_dpa {


	public CyanMetaobjectKg() {
		super();
	}
	@Override
	public String[] getSuffixNames() {
		return new String[] { "kg", "KG", "Kg" };
	}


	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {

		String s = "";
		int size = code.length();
		for (int i = 0; i < size; ++i) {
			char ch = code.charAt(i);
			if ( Character.isDigit(ch) )
				s += ch;
			else if ( ch != '_' ) {
				if ( !code.substring(i).equalsIgnoreCase("kg") ) {
					addError("Wrong format for 'kg'");
					return ;
				}
				break;
			}
		}
		setInfo(new StringBuffer(s));
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
		return "Int";
	}


}
