package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.ExprAnyLiteral;
import ast.ExprLiteralNil;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICompilerInfo_dpa;
import saci.Tuple2;

/**
 * This class represents metaobject 'feature'
 * 
   @author José
 */
public class CyanMetaobjectAnnot extends CyanMetaobjectWithAt  implements ICompilerInfo_dpa  {

	public CyanMetaobjectAnnot() { 
		super(MetaobjectArgumentKind.OneParameter);
	}
	
	@Override
	public String getName() {
		return "annot";
	}
	

	@Override
	public
	ArrayList<CyanMetaobjectError> check() {
		ArrayList<ExprAnyLiteral> exprList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList();
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();

		ExprAnyLiteral featureValue = exprList.get(0);
		if ( featureValue instanceof ExprLiteralNil ) {
			return addError("'Nil' is not allowed as a feature value");
		}
		
		
		if ( featureValue instanceof ExprLiteralNil ) {
			return addError("'Nil' is not allowed as a feature value");
		}
		if ( ! featureValue.isValidMetaobjectFeatureParameter() ) {
			return addError("The value of this annotation is not a valid expression. Probably it "
					+ "has an array with elements of more than one type or a map with keys or "
					+ "values of more than one type. For example,\n    @annot([ 0.0, 0, \"abc\"])");
		}
		
		
		cyanMetaobjectAnnotation.setInfo_dpa( new Tuple2<String, ExprAnyLiteral>( "annot", 
				featureValue));
		return null;
			
		
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	@Override
	public Tuple2<String, ExprAnyLiteral> infoToAddProgramUnit() {
		// ArrayList<Expr> exprList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList();
		@SuppressWarnings("unchecked")
		Tuple2<String, ExprAnyLiteral> t = (Tuple2<String, ExprAnyLiteral> ) getMetaobjectAnnotation().getInfo_dpa();
		return t;
	}
	

	
	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.INSTANCE_VARIABLE_DEC,
		DeclarationKind.METHOD_DEC, DeclarationKind.PROTOTYPE_DEC,
		DeclarationKind.PACKAGE_DEC, DeclarationKind.PROGRAM_DEC };

}

