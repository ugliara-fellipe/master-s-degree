package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IAction_cge;

public class CyanMetaobjectTestCompilerWR extends CyanMetaobjectWithAt 
implements IAction_cge {

	public CyanMetaobjectTestCompilerWR() {
		super( MetaobjectArgumentKind.OneParameter );
	}
	@Override
	public String getName() {
		return "wr";
	}
	
	@Override
	public boolean shouldTakeText() { return false; }
	

	/* @see ast.CyanMetaobject#dsa_javaCodeThatReplacesMetaobjectAnnotation()()
	 */
	@Override
	public StringBuffer cge_codeToAdd() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		
		StringBuffer sb = new StringBuffer(); 
		sb.append( "_System.checkStack.push(\"" + ((String ) annotation.getInfo_dpa()) + "\");\n");
		return sb;
		
	}

	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		if ( paramList.size() != 1 || ! ( paramList.get(0) instanceof String ) ) { 
			return addError("This metaobject takes exactly one parameter of type String");
		}
		annotation.setInfo_dpa( paramList.get(0) );
		return null;
	}

}
