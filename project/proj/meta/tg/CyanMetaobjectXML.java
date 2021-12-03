package meta.tg;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;


public class CyanMetaobjectXML extends CyanMetaobjectWithAt 
   implements  IParseWithoutCyanCompiler_dpa,IAction_dsa { // ,  IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectXML() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "xml";
	}


	@Override
	public StringBuffer dsa_codeToAdd( ICompiler_dsa compiler_dsa) {
		
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		char []text = annotation.getText();
		if ( text[text.length-1] == '\0' ) text[text.length-1] = ' ';	
		
		String xmlcode = new String(text);
		InputStream is = new ByteArrayInputStream(xmlcode.getBytes());
		
		try {
			DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (Exception e) {
			this.addError("Error in XML code" + e.toString() + "\n");
			this.addError(e.toString());
		}		
		
		StringBuffer s = new StringBuffer( "\"\"\"" + new String(text));
		s.deleteCharAt(s.length()-1);
		s.append("\"\"\"");
		return s;	
		
	}
	
	@Override
	public boolean shouldTakeText() { return true; }

	
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
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
	}	
	
}
