package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.ICommunicateInPackage_ati_dsa;
import saci.NameServer;

public class CyanMetaobjectSendInfoPackage extends CyanMetaobjectWithAt implements ICommunicateInPackage_ati_dsa {

	public CyanMetaobjectSendInfoPackage() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> javaArgList = annotation.getJavaParameterList();
		if ( javaArgList.size() != 1 || !(javaArgList.get(0) instanceof String) ) {
			return addError("This metaobject takes one parameter of type string or identifier");
		}
		annotation.setInfo_dpa(NameServer.removeQuotes( (String ) javaArgList.get(0)));
		return null;
	}

	@Override
	public String getName() {
		return "sendInfoPackage";
	}
	
	@Override
	public Object ati_dsa_shareInfoPackage() {
		return ((CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation).getInfo_dpa();
	}

}
