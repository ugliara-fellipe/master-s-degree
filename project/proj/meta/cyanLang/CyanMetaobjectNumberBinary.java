package meta.cyanLang;

import meta.CyanMetaobjectNumber;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;

public class CyanMetaobjectNumberBinary extends CyanMetaobjectNumber {

	public CyanMetaobjectNumberBinary() {
		super();
	}
	
	@Override
	public String[] getSuffixNames() {
		return new String[] { "bin", "Bin", "BIN" };
	}



	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		
		int n = 0;
		String numberStr = code.substring(0,  code.length() - 3);
		try {
			n = Integer.valueOf(numberStr, 2);
		}
		catch (  NumberFormatException e ) {
			addError("Number is not binary");
			return ;			
		}
		this.setInfo(new StringBuffer("" + n));
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

