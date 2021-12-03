package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.InstanceVariableDec;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompiler_dsa;

public class CyanMetaobjectPublic extends CyanMetaobjectWithAt implements ICheckProgramUnit_dsa2 {

	public CyanMetaobjectPublic() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "javaPublic";
	}


	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}
	
	@Override
	public void dsa2_checkProgramUnit(ICompiler_dsa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation  ) this.metaobjectAnnotation;
		InstanceVariableDec iv = (InstanceVariableDec ) annotation.getDeclaration();
		iv.setJavaPublic(true);
		if ( ! annotation.getPackageOfAnnotation().equals("cyan.lang") ) {
			addError("Metaobject '" + getName() + "' can only be used in package 'cyan.lang'");
		}
	}
	
	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.INSTANCE_VARIABLE_DEC };
	
	
}
