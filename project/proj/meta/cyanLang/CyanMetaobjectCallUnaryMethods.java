package meta.cyanLang;

import java.util.regex.Pattern;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import error.ErrorKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dsa;
import saci.NameServer;

public class CyanMetaobjectCallUnaryMethods extends CyanMetaobjectWithAt implements IAction_dsa {

	public CyanMetaobjectCallUnaryMethods() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public String getName() {
		return "callUnaryMethods";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Object obj = annotation.getJavaParameterList().get(0);
		if ( !(obj instanceof String) ) {
			compiler_dsa.error(this.metaobjectAnnotation.getFirstSymbol(), "A string, symbol, or identifier was expected as parameter");
			return null;
		}
			
		String code = (String ) obj;
		code = NameServer.removeQuotes(code);
		/*
		if ( code.charAt(0) == '"' )
			code = code.substring(1);
		if ( code.endsWith("\"") )
			code = code.substring(0, code.length()-1);
		*/
		try {
			java.util.regex.Pattern.compile(code);
		}
		catch (java.util.regex.PatternSyntaxException e) {
			compiler_dsa.error(this.metaobjectAnnotation.getFirstSymbol(), "Pattern is not well defined", 
					null, ErrorKind.metaobject_error);
			return null;
		}
		
		
		StringBuffer s = new StringBuffer();
		
		Pattern pattern = Pattern.compile(code);
		
		for ( String methodName : compiler_dsa.getUnaryMethodNameList() ) {
			if ( pattern.matcher(methodName).matches() ) {
				s.append("    " + methodName + ";\n");
			}
		}
		
		return s;
	}
	

}
