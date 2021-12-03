/**
  
 */
package meta.cyanLang;

import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;

/**
 * Checks whether the method t:f: of Boolean has the correct parameters. Not implemented yet.
   @author José
   
 */
public class CyanMetaobjectCheckTF extends CyanMetaobjectWithAt {

	public CyanMetaobjectCheckTF() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "checkTF";
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };


}
