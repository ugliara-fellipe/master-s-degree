/**
  
 */
package meta.cyanLang;

import ast.Expr;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.Type;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ExprReceiverKind;
import meta.ICheckMessageSend_dsa;
import saci.Env;
import saci.Tuple;

/** checks whether the parameters to the method "isA: Any -> Boolean" of  Any
 * are correct
 * 
   @author José
 */
public class CyanMetaobjectCheckIsA extends CyanMetaobjectWithAt implements ICheckMessageSend_dsa {

	public CyanMetaobjectCheckIsA() { 
		super(MetaobjectArgumentKind.ZeroParameter);
	}
	
	@Override
	public String getName() {
		return "checkIsA";
	}
	

	@Override
	public void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms) {
		
		Expr paramExpr = message.getSelectorParameterList().get(0).getExprList().get(0);
		Tuple<String, Type> t = paramExpr.ifPrototypeReturnsNameWithPackageAndType(env);
		if ( t == null || t.f2 == null ) {
			addError("The parameter to this message send should be a prototype");
		}
	}
	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };

}
