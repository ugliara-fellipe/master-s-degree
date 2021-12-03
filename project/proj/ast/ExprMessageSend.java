/**
 *
 */
package ast;

import lexer.Symbol;

/** Represents a message send. The receiver may be super or an expression.
 * The message may be a chain of unary message as in 
 *          name = club memberList first getName;
 * or a message with selectors as in
 *          circle f1: 10 y: 20  radius: 5;
 *          file read:  ;// without arguments 
 * The receiver may be super or an expression. Each of the four possibilities is represented by a subclass
 * of ExprMessageSend
 * @author José
 *
 */
abstract public class ExprMessageSend  extends Expr implements ASTNode, INextSymbol {

	public ExprMessageSend(Symbol nextSymbol) {
		this.nextSymbol = nextSymbol;
		wasReplacedByExpr = false;
		cyanMetaobjectAnnotationThatReplacedMSbyExpr = null;
	}

	public ExprMessageSend() {
		this.nextSymbol = null;
		wasReplacedByExpr = false;
		cyanMetaobjectAnnotationThatReplacedMSbyExpr = null;
	}
	
	
	@Override
	public Symbol getNextSymbol() {
		return nextSymbol;
	}

	@Override
	public void setNextSymbol(Symbol nextSymbol) {
		this.nextSymbol = nextSymbol;
	}
	
	public CyanMetaobjectAnnotation getCyanMetaobjectAnnotationThatReplacedMSbyExpr() {
		return cyanMetaobjectAnnotationThatReplacedMSbyExpr;
	}	

	public void setCyanMetaobjectAnnotationThatReplacedMSbyExpr(CyanMetaobjectAnnotation cyanMetaobjectAnnotationThatReplacedMSbyExpr) {
		this.cyanMetaobjectAnnotationThatReplacedMSbyExpr = cyanMetaobjectAnnotationThatReplacedMSbyExpr;
	}


	/**
	 * the symbols that follows the message send
	 */
	private Symbol nextSymbol;

	/**
	 * true if this message send was replaced by an expression in the first dpa phase.
	 * This happens, for example, when the method to be called is a grammar method
	 */
	protected boolean wasReplacedByExpr;
	/**
	 * this message send may have been replaced by an expression in phase dsa. This 
	 * variable keeps the metaobject annotation that asked for this replacement, if non-null.
	 * Then if this variable is not null, {@link wasReplacedByExpr} is true. But
	 * {@link wasReplacedByExpr} may be true and {@link cyanMetaobjectAnnotationThatReplacedMSbyExpr}
	 * null because the compiler itself may have replaced the message send by an expression.
	 * This does not happens currently.
	 */
	protected CyanMetaobjectAnnotation cyanMetaobjectAnnotationThatReplacedMSbyExpr;
	
}
