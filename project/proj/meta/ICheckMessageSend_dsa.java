package meta;

import ast.Expr;
import ast.MessageWithSelectors;
import ast.MethodSignature;
import ast.Type;
import saci.Env;

/**
 * interface with methods for checking message sends
 * inheritance etc.
   @author José
 */
public interface ICheckMessageSend_dsa extends ICheckProgramUnit_ati_dsa {


	/**
	 * check unary message send. The receiver expression of the message is receiverExpr.
	 * The type of the receiver is receiverType. The metaobject that implements this
	 * interface is always attached to a method. Then it knows which message was sent
	 * to receiverExpr.
	 */
	@SuppressWarnings("unused")
	default void dsa_checkUnaryMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind) { }
	/**
	 * check unary message send just like the above method. However,
	 * this method is called only when the metaobject annotation is attached to the most specific method
	 * in the hierarchy. That is, suppose method 'm -> Int' of prototype A is overridden in sub-prototype B
	 * of A. In both methods 'm -> Int', there is an attached metaobject that implements interface
	 * ICheckMessageSend_dsa. In a message send <br>
	 * <code>
	 *     var b = B new; <b>
	 *     b m println;<br>
	 * </code>
	 * the compiler calls just method <code>dsa_checkSelectorMessageSendMostSpecific</code> of B.
	 *
	 */
	@SuppressWarnings("unused")
	default void dsa_checkUnaryMessageSendMostSpecific(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, Type mostSpecificReceiver) { }
	/**
	 * check message send with selectors (not an unary message send). The receiver expression of the message is receiverExpr.
	 * The type of the receiver is receiverType. The metaobject that implements this
	 * interface is always attached to a method. Then it knows which message was sent
	 * to receiverExpr.
	 */
	@SuppressWarnings("unused")
	default void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms
			) {

	}
	/**
	 * check message send with selectors (not an unary message send) just like the above method. However,
	 * this method is called only when the metaobject annotation is attached to the most specific method
	 * in the hierarchy. That is, suppose method 'm: Int' of prototype A is overridden in sub-prototype B
	 * of A. In both methods 'm: Int', there is an attached metaobject that implements interface
	 * ICheckMessageSend_dsa. In a message send <br>
	 * <code>
	 *     var b = B new; <b>
	 *     b m: 0;<br>
	 * </code>
	 * the compiler calls just method <code>dsa_checkSelectorMessageSendMostSpecific</code> of B.
	 *
	 */
	@SuppressWarnings("unused")
	default void dsa_checkSelectorMessageSendMostSpecific(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms,
			Type mostSpecificReceiver) {

	}

}
