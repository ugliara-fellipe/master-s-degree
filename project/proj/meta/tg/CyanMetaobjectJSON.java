package meta.tg;

import org.json.JSONObject;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;


public class CyanMetaobjectJSON extends CyanMetaobjectWithAt 
   implements  IParseWithoutCyanCompiler_dpa,IAction_dsa { // ,  IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectJSON() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "json";
	}


	@SuppressWarnings("unused")
	@Override
	public StringBuffer dsa_codeToAdd( ICompiler_dsa compiler_dsa) {
		
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		char []text = annotation.getText();
		if ( text[text.length-1] == '\0' ) text[text.length-1] = ' ';	
		
		String jsonCode = new String(text);
		
		try{
			new JSONObject(jsonCode);
		}catch(Exception e){
			this.addError("Error in JSON code: " + e.toString() + "\n");			
		}
		
		StringBuffer s = new StringBuffer("CyJSONObject("  +"\"\"\"" + new String(text));
		s.deleteCharAt(s.length()-1);
		s.append("\"\"\")");
		return s;	
		
	}
	
	@Override
	public boolean shouldTakeText() { return true; }

	
	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	
	@Override
	public String getPrototypeOfType() {
			return "CyJSONObject";
	}

	@Override
	public boolean isExpression() {	
		return true;
	}


	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
	}	
	
}
