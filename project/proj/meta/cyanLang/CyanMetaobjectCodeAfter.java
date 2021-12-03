package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.MetaobjectArgumentKind;
import ast.StatementLocalVariableDec;
import ast.StatementLocalVariableDecList;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionVariableDeclaration_dsa;

public class CyanMetaobjectCodeAfter extends CyanMetaobjectWithAt 		
     implements IActionVariableDeclaration_dsa {

	public CyanMetaobjectCodeAfter() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ast.CyanMetaobject#getName()
	 */
	@Override
	public String getName() {
		return "codeAfter";
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.LOCAL_VAR_DEC };

	@Override
	public StringBuffer dsa_codeToAddAfter() {
		CyanMetaobjectWithAtAnnotation withAt = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Declaration dec = withAt.getDeclaration();

		StatementLocalVariableDecList varList = (StatementLocalVariableDecList ) dec;
		StringBuffer code = new StringBuffer("    \"the following variables were declared: \\n         \" print; ");
		for ( StatementLocalVariableDec s : varList.getLocalVariableDecList() ) {
			code.append("\"" + s.getName() + " \" print; ");
		}
		code.append("\"\" println;");
		return code;
	}

	
}
