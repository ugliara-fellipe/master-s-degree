package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.ICommunicateInPackage_ati_dsa;
import saci.Tuple2;

public class CyanMetaobjectTestCompilerCELABEL extends CyanMetaobjectWithAt
   implements ICommunicateInPackage_ati_dsa
   {

	
	public CyanMetaobjectTestCompilerCELABEL() {
		super( MetaobjectArgumentKind.TwoParameters );
	}
	
	
	@Override
	public String getName() {
		return "celabel";
	}
	
	
	@Override
	public Object ati_dsa_shareInfoPackage() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		return new Tuple2<String, String>( (String ) paramList.get(0), (String ) paramList.get(1));
	}	
	
	@Override
	public boolean shouldTakeText() { return false; }
	

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		if ( paramList.size() != 2 || ! ( paramList.get(0) instanceof String ) || ! ( paramList.get(1) instanceof String )) { 
			return addError("This metaobject takes exactly two parameters. The first is an identifier and the other is a literal String");
		}
		return null;
	}


}
