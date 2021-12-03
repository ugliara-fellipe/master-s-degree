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
import saci.NameServer;
import saci.Tuple2;

/**
 * This class represents metaobject 'feature'
 * 
   @author José
 */
public class CyanMetaobjectFeature extends CyanMetaobjectWithAt 
    implements ICompilerInfo_dpa {

	public CyanMetaobjectFeature() { 
		super(MetaobjectArgumentKind.TwoParameters);
	}

	@Override
	public String getName() {
		return "feature";
	} 
	
	@Override
	public
	ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
		if ( ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList() == null || 
				((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList().size() != 2 )  {
			return addError("This metaobject annotation should have exactly two parameters");
		}
		Object featureName = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).javaParameterAt(0);
		Object featureValue = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList().get(1);
		if ( !(featureName instanceof String) ) {
			return addError("This metaobject annotation should have the first parameter of type 'String'");
		}
		if ( featureValue instanceof ExprLiteralNil ) {
			return addError("'Nil' is not allowed as a feature value");
		}
		if ( ! (featureValue instanceof ExprAnyLiteral) ) {
			return addError("Internal error: the parameter to metaobject '" + getName() + "' should be subtype of ExprAnyLiteral");
		}
		ExprAnyLiteral valueExpr = (ExprAnyLiteral ) featureValue;
		if ( ! valueExpr.isValidMetaobjectFeatureParameter() ) {
			return addError("The value of this feature is not a valid expression. Probably it "
					+ "has an array with elements of more than one type or a map with keys or "
					+ "values of more than one type. For example,\n    @feature(list, [ 0.0, 0, \"abc\"])");
		}
		
		cyanMetaobjectAnnotation.setInfo_dpa( new Tuple2<String, ExprAnyLiteral>( NameServer.removeQuotes((String ) featureName), 
				(ExprAnyLiteral ) featureValue));
		return null;
	}

	/**
	 * @feature(name, [ "o", "a" ] )   Object []array
	 */
	@Override
	public Tuple2<String, ExprAnyLiteral> infoToAddProgramUnit() {
		// ArrayList<Expr> exprList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getRealParameterList();
		@SuppressWarnings("unchecked")
		Tuple2<String, ExprAnyLiteral> t = (Tuple2<String, ExprAnyLiteral> ) getMetaobjectAnnotation().getInfo_dpa();
		return t;
	}
	
	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.INSTANCE_VARIABLE_DEC,
		DeclarationKind.METHOD_DEC, DeclarationKind.PROTOTYPE_DEC, DeclarationKind.METHOD_SIGNATURE_DEC,
		DeclarationKind.PACKAGE_DEC, DeclarationKind.PROGRAM_DEC };


}
