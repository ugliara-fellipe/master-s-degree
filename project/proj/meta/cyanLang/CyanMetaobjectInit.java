package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IActionProgramUnit_ati;
import meta.ICompiler_ati;
import meta.IInstanceVariableDec_ati;
import saci.Tuple3;

public class CyanMetaobjectInit extends CyanMetaobjectWithAt 
       implements IActionProgramUnit_ati {

	public CyanMetaobjectInit() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}
	@Override
	public String getName() {
		return "init";
	}

	@Override
	public ArrayList<Tuple3<String, String, StringBuffer>> 
	       ati_methodCodeList(ICompiler_ati compiler_ati) { 

		StringBuffer methodCode = new StringBuffer();
		methodCode.append("    func init: ");
		ArrayList<IInstanceVariableDec_ati> varList = new ArrayList<>(); 
		
		ArrayList<Object> javaParameterList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList();
		int n = 1;
		for (Object p : javaParameterList) {
			if ( ! (p instanceof String) ) {
				addError("parameter number " + n + " to this metaobject annotation is not a string");
								
				return null;
			}
			String strParam = (String ) p;
			IInstanceVariableDec_ati instVarDec = compiler_ati.searchInstanceVariable(strParam);
			if ( instVarDec == null ) {
				addError(strParam + " is not an instance variable");
				return null;
			}
			varList.add(instVarDec);
			++n;
		}
		int size = varList.size();
		for (IInstanceVariableDec_ati varDec : varList ) {
			methodCode.append(" " + varDec.getIType().getName() + " " + varDec.getName());
			if ( --size > 0 ) 
				methodCode.append(", ");
		}
		methodCode.append(" {\n" );
		for (IInstanceVariableDec_ati varDec : varList ) {
			methodCode.append("        self." + varDec.getName() + " = " + varDec.getName() + ";\n");
		}
		methodCode.append("    }\n " );
		/*
		 * addMethod(CyanMetaobjectWithAt, String, String, String, StringBuffer) 
		 */
		ArrayList<Tuple3<String, String, StringBuffer>> tupleList = new ArrayList<>();
		tupleList.add(
				new Tuple3<String, String, StringBuffer>(
				    this.getMetaobjectAnnotation().getProgramUnit().getName(), "init:" + varList.size(), methodCode) );
		return tupleList;
	}

}
