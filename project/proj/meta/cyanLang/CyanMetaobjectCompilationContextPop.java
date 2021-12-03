package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import saci.NameServer;

public class CyanMetaobjectCompilationContextPop extends CyanMetaobjectWithAt {

	public CyanMetaobjectCompilationContextPop() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}

	@Override
	public String getName() {
		return NameServer.popCompilationContextName;
	}


	@Override
	public
	ArrayList<CyanMetaobjectError> check() {
		ArrayList<Object> javaObjectList = this.getMetaobjectAnnotation().getJavaParameterList();
		
		boolean ok = false;
		if ( javaObjectList != null ) {
			if ( javaObjectList.size() == 1 && javaObjectList.get(0) instanceof String ) {
				ok = true;
			}
			else if ( javaObjectList.size() == 3 && javaObjectList.get(0) instanceof String && javaObjectList.get(1) instanceof String 
					&& javaObjectList.get(2) instanceof String ) {
				ok = true;
			}
		}
		if ( ! ok ) {
			CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
			ArrayList<CyanMetaobjectError> errorList = new ArrayList<>();
			errorList.add(new CyanMetaobjectError("Metaobject '" + this.getName() + "' should have one or three parameters",
					cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber(), cyanMetaobjectAnnotation.getFirstSymbol().getColumnNumber(),
					cyanMetaobjectAnnotation.getFirstSymbol().getOffset()));
			return errorList;
		}
		
		return null;
	}

}
