package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import lexer.Lexer;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IActionProgramUnit_ati;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_ati;
import saci.NameServer;
import saci.Tuple2;

public class CyanMetaobjectCreatePrototype extends CyanMetaobjectWithAt implements IAction_dpa, IActionProgramUnit_ati {

	public CyanMetaobjectCreatePrototype() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}
	
	@Override
	public String getName() {
		return "createPrototype";
	}


	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation  ) this.metaobjectAnnotation;
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();
		if ( javaParamList.size() !=  4 && javaParamList.size() != 2 ) {
			return addError("This metaobject annotation should have two or four parameters");
		}
		if ( !(javaParamList.get(0) instanceof String) ||  !(javaParamList.get(1) instanceof String) 
				|| !(javaParamList.get(2) instanceof String) || !(javaParamList.get(3) instanceof String) ) {
			return addError("All parameters to this metaobject annotation should be strings");
		}
		
		return null;
	}

	@Override
	public ArrayList<Tuple2<String, StringBuffer>> dpa_NewPrototypeList(ICompilerAction_dpa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation  ) this.metaobjectAnnotation;
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();
		
		ArrayList<Tuple2<String, StringBuffer>> protoCodeList = new ArrayList<>();
		String prototypeName = NameServer.removeQuotes((String ) javaParamList.get(0));
		StringBuffer code = new StringBuffer( NameServer.removeQuotes((String ) javaParamList.get(1)) );
		//String escape = Lexer.escapeJavaString(code.toString());
		String unescape = Lexer.unescapeJavaString(code.toString());
		protoCodeList.add( new Tuple2<String, StringBuffer>( prototypeName, new StringBuffer(unescape)));
		return protoCodeList; 
	}	
	
	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_NewPrototypeList(
			ICompiler_ati compiler_ati) {

		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation  ) this.metaobjectAnnotation;
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();

		if ( javaParamList.size() ==  4 ) {
			ArrayList<Tuple2<String, StringBuffer>> protoCodeList = new ArrayList<>();
			String prototypeName = NameServer.removeQuotes((String ) javaParamList.get(2));
			StringBuffer code = new StringBuffer( NameServer.removeQuotes((String ) javaParamList.get(3)) );
			//String escape = Lexer.escapeJavaString(code.toString());
			String unescape = Lexer.unescapeJavaString(code.toString());
			protoCodeList.add( new Tuple2<String, StringBuffer>( prototypeName, new StringBuffer(unescape)));
			return protoCodeList; 
		}
		else
			return null;
		
		
	}
}
