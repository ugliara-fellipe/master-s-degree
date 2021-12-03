package meta.tg;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import meta.CyanMetaobjectLiteralString;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectLiteralStringFilename extends CyanMetaobjectLiteralString implements IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectLiteralStringFilename(){
		super();
	}

	@Override
	public String[] getPrefixNames(){
		return new String[] { "FILENAME", "Filename", "filename" };
	}



	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		try{
			Paths.get(code);
		} 
		catch(InvalidPathException e){
			addError("Invalid filename format.");
		}

		setInfo(new StringBuffer("\"" + code + "\""));
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

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		return (StringBuffer ) getInfo();
	}
}



