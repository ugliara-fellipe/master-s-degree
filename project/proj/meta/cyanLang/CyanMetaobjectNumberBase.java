package meta.cyanLang;

import meta.CyanMetaobjectNumber;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;

public class CyanMetaobjectNumberBase extends CyanMetaobjectNumber 
       implements meta.IParseWithoutCyanCompiler_dpa {


	public CyanMetaobjectNumberBase() {
		super();
	}
	@Override
	public String[] getSuffixNames() {
		return new String[] { "base", "BASE", "Base" };
	}


	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		
		int len = code.length();
		int i = len - 1;
		while ( Character.isDigit(code.charAt(i)) )
			--i;
		if ( i == len ) {
            addError("After 'base' there should appear the number of the base");
			return ;
		}
		int base = 10;
		try {
		    base = Integer.valueOf(code.substring(i+1));
		}
		catch (  NumberFormatException e ) {
			addError("Base was not recognized");
			return ;		
		}
		if ( base < 2 || base > 26 ) {
			addError("Base should be between 2 and 26 (included)");
			return ;
		}
		while ( Character.isLetter(code.charAt(i)) )
			--i;
		int n = 0;
		String numberStr = code.substring(0,  i + 1);
		String numberWithoutUnderscore = ""; 
		for (int k = 0; k < numberStr.length(); ++k) {
			char ch = numberStr.charAt(k);
			if ( ch != '_' )
			    numberWithoutUnderscore = numberWithoutUnderscore + numberStr.charAt(k);
		}
		try {
			n = Integer.valueOf(numberWithoutUnderscore, base);
		}
		catch (  NumberFormatException e ) {
			addError("Number is not in base'" + base + "'");
			return ;

		}
		setInfo(new StringBuffer("" + n));
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
