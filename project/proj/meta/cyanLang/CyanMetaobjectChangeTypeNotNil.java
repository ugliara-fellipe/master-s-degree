package meta.cyanLang;

import java.util.ArrayList;
import ast.Expr;
import ast.ExprIdentStar;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.ObjectDec;
import ast.SelectorWithRealParameters;
import ast.Type;
import ast.VariableDecInterface;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ExprReceiverKind;
import meta.ICheckMessageSendBeforeTypingMessage_dsa;
import meta.IExprFunction;
import saci.Env;

public class CyanMetaobjectChangeTypeNotNil extends CyanMetaobjectWithAt  implements ICheckMessageSendBeforeTypingMessage_dsa  {
	
	public CyanMetaobjectChangeTypeNotNil() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public String getName() {
		return "changeTypeNotNil";
	}
	
	@Override
	public void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms) {
		
		if ( ! (receiverType instanceof ObjectDec) ) {
			addError("A prototype was expected as the type of the receiver of this message");
			return ;			
		}
		if ( !(ms instanceof ast.MethodSignatureWithSelectors) ) {
			addError("Internal error in '" + getName() + "'");
			return ;			
		}
		
		ArrayList<SelectorWithRealParameters> selList = message.getSelectorParameterList();
		
		if ( selList.size() != 1 ) {
			addError("Metaobject '" + getName() + "' should be used only with method 'notNil:' of a Union");
			return ;
		}
		
		SelectorWithRealParameters sel = selList.get(0);
		if ( !sel.getSelectorNameWithoutSpecialChars().equals("notNil") ) {
			addError("Selector 'notNil' was expected");
			return ;
		}
		/*
		 * get the type that is not Nil
		 */
		String paramTypeStr = (String ) this.getMetaobjectAnnotation().getJavaParameterList().get(0);
		
		if ( paramTypeStr.charAt(0) == '"' && paramTypeStr.charAt(paramTypeStr.length() - 1) == '"' )
			paramTypeStr = paramTypeStr.substring(1,  paramTypeStr.length()-1);
				

		Type paramType = env.searchPackagePrototype(paramTypeStr, this.getMetaobjectAnnotation().getFirstSymbol());
		Expr paramExpr = sel.getExprList().get(0);
		
		if ( (paramExpr instanceof IExprFunction) ) {
			if ( receiverExpr instanceof ExprIdentStar)  {
				String id = ((ExprIdentStar) receiverExpr).getName();
				VariableDecInterface vdi = env.searchVariable(id); 
				if ( vdi != null ) {
					/*
					 * at this point: 
					 *   a) the parameter to 'notNil:' is a literal function
					 *   b) the receiver of the message is a variable
					 * Then the type of the variable should be changed to the type
					 * of the non-Nil prototype of the type of the variable. This
					 * change is not made here. We just ask to the literal function
					 * to change the types during the semantic analysis.
					 * The new Java code for the variable inside the literal function 
					 * will be<br>
					 *     ( (Tjava ) _x.elem)
					 * if the variable is 'x' and the parameter to unionCase: is T. 
					 * Here Tjava is the type of T in Java. 
					 */
					String newCode = "((" + paramType.getJavaName() + ") " + vdi.javaNameWithRef() + "._elem)";

					// String newCode = "((" + paramType.getJavaName() + ") " + vdi.getJavaName() + ".elem)";
					((IExprFunction ) paramExpr).addNewVarInfo(id, paramType, newCode);
					
				}
			}
		}
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };
	
	
}
