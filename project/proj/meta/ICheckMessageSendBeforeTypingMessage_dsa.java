package meta;

import ast.Expr;
import ast.MessageWithSelectors;
import ast.MethodSignature;
import ast.Type;
import saci.Env;

/**
 * interface with methods for checking message sends before the types of parameters is known. The me
   @author José
 */

public interface ICheckMessageSendBeforeTypingMessage_dsa extends ICheckProgramUnit_ati_dsa {

	@SuppressWarnings("unused")
	default void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms) { }
	
}
