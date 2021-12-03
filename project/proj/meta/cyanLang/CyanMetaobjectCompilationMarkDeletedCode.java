package meta.cyanLang;

import java.util.ArrayList;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import saci.NameServer;

/**
 * This metaobject 
   @author José
 */
public class CyanMetaobjectCompilationMarkDeletedCode extends meta.CyanMetaobjectWithAt {

	public CyanMetaobjectCompilationMarkDeletedCode() {
		super(MetaobjectArgumentKind.OneParameter);
	}


	@Override
	public ArrayList<CyanMetaobjectError> check() {
		ArrayList<Object> javaObjectList = this.getMetaobjectAnnotation().getJavaParameterList();
		if ( javaObjectList == null || ! ( javaObjectList.get(0) instanceof Integer) ) {
			return addError("Metaobject '" + getName() + "' should have exactly one Int parameter");
		}
		this.numLinesDeleted = (Integer ) javaObjectList.get(0);
		return null;
	}

	@Override
	public String getName() {
		return NameServer.markDeletedCodeName;
	}
	
	private int	numLinesDeleted;

	public int getNumLinesDeleted() {
		return numLinesDeleted;
	}


	
}
