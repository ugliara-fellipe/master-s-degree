package meta;

/**
 * Represents a Cyan  expression 
 */
public interface IExpr extends IStatement {
	IType getIType();
	String ifPrototypeReturnsItsName();	
}
