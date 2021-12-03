package meta.cyanLang;

import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICompiler_dpa;
import meta.IParseWithCyanCompiler_dpa;

public class CyanMetaobjectMoTest extends CyanMetaobjectWithAt implements IParseWithCyanCompiler_dpa {

	public CyanMetaobjectMoTest() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "motest";
	}


	/*
	@Override
	public StringBuffer ati_codeToAdd(
			ICompiler_ati compiler_ati) { 
		return new StringBuffer("final"); 
	}		
	*/
	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return new DeclarationKind[] { DeclarationKind.PROTOTYPE_DEC };
	}

	@Override
	public void dpa_parse(ICompiler_dpa compiler_dpa) {
		compiler_dpa.next();
		System.out.println(compiler_dpa.getCompilationStep());
	}
	
	@Override
	public boolean shouldTakeText() { return true; }	
}
