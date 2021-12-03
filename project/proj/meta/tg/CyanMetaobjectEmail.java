package meta.tg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import meta.CyanMetaobjectLiteralString;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectEmail extends CyanMetaobjectLiteralString implements IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectEmail() {
		super();
	}
	@Override
	public String[] getPrefixNames() {
		return new String[] { "email", "EMAIL", "Email" };
	}
	

	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {

		if (code != null) {
            String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(code);
            if (!matcher.matches()) {
            	addError("Invalid email address");
            }
            setInfo(new StringBuffer("\"" + code + "\""));
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

