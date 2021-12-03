package ast;

/**
 * represents each variable declaration in a notNil statement. For example,
 * <code>
 *     notNil T v = e { ... }
 * </code>
 * T is typeInDec, v is localVar, and e is expr
   @author jose
 */
public class NotNilRecord {
	public NotNilRecord(Expr varExprType, StatementLocalVariableDec localVar,
			Expr expr) {
		super();
		this.typeInDec = varExprType;
		this.localVar = localVar;
		this.expr = expr;
	}

	/**
	 * expression of the kind T|Nil or Nil|T such as e1 and e2 in<br>
	 * <code>
	 * notNil b = e1, c = e2 { ... } <br>
	 * </code> 
	 */
	public Expr typeInDec; 
	public StatementLocalVariableDec localVar; 
	
	/**
	 * the variable b, c in the example above.
	 */
	public Expr expr;
	
}
