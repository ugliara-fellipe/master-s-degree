package meta.cyanLang;

import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dsa;

public class CyanMetaobjectCallTestMethods extends CyanMetaobjectWithAt implements IAction_dsa {

	public CyanMetaobjectCallTestMethods() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "callTestMethods";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		StringBuffer s = new StringBuffer();
		
		for ( String methodName : compiler_dsa.getUnaryMethodNameList() ) {
			if ( methodName.endsWith("Test") ) {
				s.append("    " + methodName + ";\n");
			}
		}
		
		return s;
	}

}
