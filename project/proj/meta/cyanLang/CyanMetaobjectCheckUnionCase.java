package meta.cyanLang;

import java.util.ArrayList;
import ast.Expr;
import ast.ExprIdentStar;
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
import saci.NameServer;
import saci.Tuple;

/**
 * This class is not used anymore. But it is kept as an example of metaobject class.
   @author jose
 */
public class CyanMetaobjectCheckUnionCase extends CyanMetaobjectWithAt implements ICheckMessageSendBeforeTypingMessage_dsa {

	public CyanMetaobjectCheckUnionCase() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "checkUnionCase";
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
		if ( selList.size()%2 != 0 ) {
			addError("Metaobject '" + getName() + "' should be used only with method unionCase:do: of a Union");
			return ;
		}
		
		Expr paramExpr;
		Type paramType = null;
		boolean isUnionCase = true;
		for ( SelectorWithRealParameters sel : selList ) {
			if ( isUnionCase ) {
				if ( !sel.getSelectorNameWithoutSpecialChars().equals("unionCase") ) {
					addError("Selector 'unionCase' was expected");
					return ;
				}
				paramExpr = sel.getExprList().get(0);
				Tuple<String, Type> t = paramExpr.ifPrototypeReturnsNameWithPackageAndType(env);
				if ( t == null || t.f2 == null ) {
					addError("The parameter to selector 'unionCase' should be a prototype");
					return ;
				}
				paramType = selParamList.get(i).getParameterList().get(0).getType();
				String paramTypeFullName = paramType.getFullName();
				paramTypeFullName = paramTypeFullName.replace(NameServer.cyanLanguagePackageName + ".", "");
				if ( ! paramTypeFullName.equals(t.f1) ) {
					addError("This parameter to selector 'unionCase' should be '" + paramTypeFullName + "'");
					return ;
				}
				isUnionCase = false;
			}
			else {
				if ( !sel.getSelectorNameWithoutSpecialChars().equals("do") ) {
					addError("Selector 'unionCase' was expected");
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
							 *   a) the parameter to 'do:' is a literal function
							 *   b) the receiver of the message is a variable
							 * Then the type of the variable should be changed to the type
							 * of the parameter of the previous unionCase: selector. This
							 * change is not made here. We just ask to the literal function
							 * to change the types during the semantic analysis.
							 * The new Java code for the variable inside the literal function 
							 * will be<br>
							 *     ( (Tjava ) _x.elem)
							 * if the variable is 'x' and the parameter to unionCase: is T. 
							 * Here Tjava is the type of T in Java. 
							 */
							@SuppressWarnings("null")
							String newCode = "((" + paramType.getJavaName() + ") " + vdi.javaNameWithRef() + "._elem)";

							// String newCode = "((" + paramType.getJavaName() + ") " + vdi.getJavaName() + ".elem)";
							((IExprFunction ) paramExpr).addNewVarInfo(id, paramType, newCode);
							
						}
					}
				}
				isUnionCase = true;
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
