package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICompiler_ati;
import saci.Tuple3;

/**
 * rename a method. Usage:
 * <code> <br>
 * {@literal @}rename("PrototypeName", "at:1 with:2", "myAt:", "myWith:")
 * </code>
   @author jose
 */
public class CyanMetaobjectRenameMethod extends CyanMetaobjectWithAt implements IActionProgramUnit_ati
{

	public CyanMetaobjectRenameMethod() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}


	@Override
	public ArrayList<Tuple3<String, String, String []>> ati_renameMethod(
			ICompiler_ati compiler_ati) {
		ArrayList<Object> parameterList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList();
		if ( parameterList.size() < 3 ) {
			compiler_ati.error(((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList().get(0).getFirstSymbol(), 
					"This metaobject takes at least three string parameters");
		}
		String []strList = new String[parameterList.size()];
		int i = 0;
		for ( Object elem : parameterList ) {
			if ( !(elem instanceof String) ) {
				compiler_ati.error(((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList().get(i).getFirstSymbol(), 
						"All parameter to this metaobject should be strings");
			}
			else {
				strList[i] = (String ) elem;
			}
			++i;
		}
		String prototypeName = strList[0];
		String oldMethodName = strList[1];
		String newMethodName[] = new String[strList.length-2];
		int j = 0;
		for (i = 2; i < strList.length; ++i) {
			newMethodName[j] = strList[i];
			++j;
		}
		ArrayList<Tuple3<String, String, String []>> tupleList = new ArrayList<>();
		tupleList.add( new Tuple3<String, String, String []>(prototypeName, oldMethodName, newMethodName));
		return tupleList;
	}
	
	
	@Override
	public String getName() {
		return "renameMethod";
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return null;
	}

}
