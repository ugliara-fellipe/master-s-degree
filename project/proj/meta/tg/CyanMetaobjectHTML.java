package meta.tg;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.w3c.tidy.Tidy;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dsa;



public class CyanMetaobjectHTML extends CyanMetaobjectWithAt 
   implements  IAction_dsa { // ,  IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectHTML() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "html";
	}


	@Override
	public StringBuffer dsa_codeToAdd( ICompiler_dsa compiler_dsa) {
		
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		char []text = annotation.getText();
		if ( text[text.length-1] == '\0' ) text[text.length-1] = ' ';	
		
		String htmlcode = new String(text);
		InputStream is = new ByteArrayInputStream(htmlcode.getBytes());
		OutputStream os = new ByteArrayOutputStream();
		Tidy tidy = new Tidy();
		tidy.parse(is, os);		

		
		if(tidy.getParseErrors() > 0 || tidy.getParseWarnings() > 0){
			this.addError("Error in HTML code\n");
		}				
		
		StringBuffer s = new StringBuffer( "\"\"\"" + htmlcode);
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


}
