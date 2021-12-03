
package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IAction_cge;
import meta.ICompilerAction_dpa;
import meta.IParseWithoutCyanCompiler_dpa;
import meta.ReplacementPolicyInGenericInstantiation;

/**
 * metaobject for documenting Cyan code. The metaobject annotation
 * <code><br>
 * {@literal @}doc{* this is a comment *}
 * </code><br>
 * is translated into <br>
 * <code><br>
 * /** this is a comment *{@literal /}
 * </code>

   @author jose
 */

public class CyanMetaobjectDoc extends CyanMetaobjectWithAt
		implements IAction_cge, IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectDoc() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ast.CyanMetaobject#getName()
	 */
	@Override
	public String getName() {
		return "doc";
	}

	@Override
	public boolean shouldTakeText() {
		return true;
	}

	/*
	 * @see ast.CyanMetaobject#dsa_javaCodeThatReplacesMetaobjectAnnotation()()
	 */
	@Override
	public StringBuffer cge_codeToAdd() {
		StringBuffer sb = new StringBuffer();

		sb.append("    /**");
		sb.append(((CyanMetaobjectWithAtAnnotation) metaobjectAnnotation).getText());
		sb.append("    */");
		return sb;
	}

	@Override
	public ReplacementPolicyInGenericInstantiation getReplacementPolicy() {
		return ReplacementPolicyInGenericInstantiation.NO_REPLACEMENT;
	}


	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {

		CyanMetaobjectWithAtAnnotation metaobjectAnnotationWithAt = (CyanMetaobjectWithAtAnnotation) metaobjectAnnotation;
		if ( metaobjectAnnotationWithAt.getText() == null ) {
			addError( "Metaobject 'doc' should take text between two sequences of symbols" );
		}
	}



	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] {
		DeclarationKind.INSTANCE_VARIABLE_DEC,
		DeclarationKind.METHOD_DEC, DeclarationKind.PROTOTYPE_DEC,
		DeclarationKind.PACKAGE_DEC,
		DeclarationKind.PROGRAM_DEC };


}
