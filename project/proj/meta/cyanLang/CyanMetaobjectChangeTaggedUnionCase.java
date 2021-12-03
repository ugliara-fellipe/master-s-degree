package meta.cyanLang;

import java.util.ArrayList;
import ast.Expr;
import ast.ExprIdentStar;
import ast.GenericParameter;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.MethodSignatureWithSelectors;
import ast.ObjectDec;
import ast.SelectorWithParameters;
import ast.SelectorWithRealParameters;
import ast.Type;
import ast.VariableDecInterface;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ExprReceiverKind;
import meta.ICheckMessageSendBeforeTypingMessage_dsa;
import meta.IExprFunction;
import saci.Env;

public class CyanMetaobjectChangeTaggedUnionCase extends CyanMetaobjectWithAt implements ICheckMessageSendBeforeTypingMessage_dsa {

	public CyanMetaobjectChangeTaggedUnionCase() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "changeTaggedUnionCase";
	}
	
	@Override
	public void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms) {
		
		if ( ! (receiverType instanceof ObjectDec) ) {
			addError("A prototype was expected as the type of the receiver of this message");
			return ;
		}
		if ( !(ms instanceof ast.MethodSignatureWithSelectors) ) {
			addError("Internal error in checkunioncase");
			return ;
		}
		MethodSignatureWithSelectors msng = (MethodSignatureWithSelectors ) ms;
		
		ArrayList<SelectorWithParameters> selParamList = msng.getSelectorArray();
		ArrayList<SelectorWithRealParameters> selList = message.getSelectorParameterList();
		int i = 0;
		
		ObjectDec receiverProto = (ObjectDec ) receiverType;
		if ( receiverProto.getGenericParameterListList().size() != 1 ) {
			addError("The type of the receiver of this message should be a generic prototype "
					+ "with just one list of generic parameters");
			return ;
		}
		ArrayList<GenericParameter> gpList = receiverProto.getGenericParameterListList().get(0);
			
		Expr paramExpr;
		for ( SelectorWithRealParameters sel : selList ) {

			
			if ( !sel.getSelectorNameWithoutSpecialChars().equals(selParamList.get(i).getSelectorNameWithoutSpecialChars()) ) {
				addError("Selector '" + selParamList.get(i).getName() + "' was expected");
				return ;
			}
			paramExpr = sel.getExprList().get(0);		
			if ( (paramExpr instanceof IExprFunction) ) {
				if ( receiverExpr instanceof ExprIdentStar)  {
					String id = ((ExprIdentStar) receiverExpr).getName();
					VariableDecInterface vdi = env.searchVariable(id); 
					if ( vdi != null ) {
						/*
						 * at this point: 
						 *   a) the parameter to the selector is a literal function
						 *   b) the receiver of the message is a variable
						 * Then the type of the variable should be changed to the type
						 * associated to the selector. In <br>
						 * {@code          var mix = Union<calorie, Float, name, String, age, Int, bank, Bank, client, bank.Client> new;}<br>
						 * the type associated to 'calorie' is Float. In this case, the type of the variable should
						 * be changed to 'Float' inside the literal function. This
						 * change is not made here. We just ask to the literal function
						 * to change the types during the semantic analysis.
						 * The new Java code for the variable inside the literal function 
						 * will be<br>
						 *     ( (Tjava ) _x.elem)
						 * if the variable is 'x' and the type associated to the selector is T. 
						 * Here Tjava is the type of T in Java. 
						 */
						Type paramType = gpList.get(2*i+1).getParameter().getType(); //selParamList.get(i).getParameterList().get(0).getType();

						/*
						 * we use ".elem" because if the variable is used inside the literal function, 
						 * then it is a ref type.
						 */
						String newCode = "((" + paramType.getJavaName() + ") " + vdi.javaNameWithRef() + "._elem)";
						vdi.setTypeWasChanged(true);
						((IExprFunction ) paramExpr).addNewVarInfo(id, paramType, newCode);
						
					}
				}
			}
			++i;
		}
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };
	
	
}
