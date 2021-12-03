/**
  
 */
package meta.cyanLang;

import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckDeclaration_ati3;
import meta.ICompiler_ati;

/**
 * This metaobject checks the style of a prototype or a method
   @author José
   
 */
public class CyanMetaobjectCheckStyle extends CyanMetaobjectWithAt implements ICheckDeclaration_ati3 {

	public CyanMetaobjectCheckStyle() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "checkStyle";
	}
	
	
	@Override
	public void ati3_checkDeclaration(ICompiler_ati compiler_ati) {
		checkName(this.getAttachedDeclaration().getName());
		// other checkings will be added in future
		/*
		if ( this.getAttachedDeclaration() instanceof ProgramUnit ) {
			checkName(this.getAttachedDeclaration().getName());
		}
		else  {
		}
		*/
	}
	
	private boolean checkName(String name) {
		if ( name.contains("_") ) {
			addError("Illegal '_' in identifier name");
			return false;
		}
		else
			return true;
		
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKind;
	}


	private DeclarationKind[] decKind = new DeclarationKind[]{ DeclarationKind.METHOD_DEC, DeclarationKind.PROTOTYPE_DEC, 
			DeclarationKind.INSTANCE_VARIABLE_DEC
			};
}
