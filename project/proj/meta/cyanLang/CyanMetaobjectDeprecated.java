package meta.cyanLang;

import java.util.ArrayList;
import ast.CompilationUnit;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Expr;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.Type;
import lexer.Symbol;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ExprReceiverKind;
import meta.ICheckMessageSend_dsa;
import saci.Env;

public class CyanMetaobjectDeprecated extends CyanMetaobjectWithAt 
    implements ICheckMessageSend_dsa {

	public CyanMetaobjectDeprecated() {
		super(MetaobjectArgumentKind.ZeroOrMoreParameters);
	}

	@Override
	public String getName() {
		return "deprecated";
	}
	
	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation withAt = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		
		ArrayList<Object> javaParamList = withAt.getJavaParameterList();
		if ( javaParamList.size() == 0 ) {
			withAt.setInfo_dpa("This method is deprecated, you should not use it");
			return null;
		}
		else if ( javaParamList.size() == 1 ) {
			if ( !(javaParamList.get(0) instanceof String) ) {
				return addError("This metaobject annotation should take one string parameter");
			}
			else {
				withAt.setInfo_dpa( javaParamList.get(0) );
				return null;
			}
		}
		else {
			return this.addError("This metaobject annotation should take just one string parameter");
		}
	}
	
	
	@Override
	public void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms
			) { 
		CyanMetaobjectWithAtAnnotation withAt = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Symbol first = receiverExpr.getFirstSymbol();
		CompilationUnit cunit = (CompilationUnit ) first.getCompilationUnit();
		addError( "In " + cunit.getPublicPrototype().getFullName() + " line " + first.getLineNumber() + " column " + first.getColumnNumber() + " " +  
				((String ) withAt.getInfo_dpa()) );
	}	

	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };

	
}
