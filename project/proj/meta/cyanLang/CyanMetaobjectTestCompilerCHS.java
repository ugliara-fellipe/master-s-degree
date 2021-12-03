package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IAction_cge;

public class CyanMetaobjectTestCompilerCHS extends CyanMetaobjectWithAt 
       implements IAction_cge {

	public CyanMetaobjectTestCompilerCHS() {
		super( MetaobjectArgumentKind.TwoParameters );
	}
	
	@Override
	public String getName() {
		return "chs";
	}
	
	@Override
	public boolean shouldTakeText() { return false; }
	

	/* @see ast.CyanMetaobject#dsa_javaCodeThatReplacesMetaobjectAnnotation()()
	 */
	@Override
	public StringBuffer cge_codeToAdd() {
		
		StringBuffer sb = new StringBuffer();
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		
		String s1 = (String ) paramList.get(0); 
		String s2 = (String ) paramList.get(1);
		
		sb.append( "    if ( ! _System.checkStack.peek().equals(\"" + s1 + "\") ) { System.out.println(\"" + s2 + "\"); System.exit(1); } \n");
		sb.append("     else _System.checkStack.pop();\n");
		return sb;
		
	}

	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		if ( paramList.size() != 2 || ! ( paramList.get(0) instanceof String ) || ! ( paramList.get(1) instanceof String )) { 
			return addError("This metaobject takes exactly two parameters of type String");
		}
		return null;
	}

}
