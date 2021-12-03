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
 * usage of this metaobject: use it with parameters that are the prototype, method name, and code that should be
 * added before the method. <br>
 * {@code
 *     @addBeforeMethod("Program", "run", "\"calling 'run'\" println;")
 * }
   @author jose
 */
public class CyanMetaobjectAddBeforeMethod extends CyanMetaobjectWithAt implements IActionProgramUnit_ati
{

	public CyanMetaobjectAddBeforeMethod() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}


	@Override
	public ArrayList<Tuple3<String, String, StringBuffer>> ati_beforeMethodCodeList(
			ICompiler_ati compiler) {
		ArrayList<Object> parameterList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList();
		String prototypeName = (String ) parameterList.get(0);
		ArrayList<Tuple3<String, String, StringBuffer>> tupleList = new ArrayList<>();
		tupleList.add( new Tuple3<String, String, StringBuffer>(prototypeName,
				(String ) parameterList.get(1), new StringBuffer( (String) parameterList.get(2))));
		return tupleList;
	}
	
	
	@Override
	public String getName() {
		
		return "addBeforeMethod";
	}


	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.PROTOTYPE_DEC, DeclarationKind.METHOD_DEC };
}
