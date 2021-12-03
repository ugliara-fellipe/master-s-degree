package meta;

import ast.Type;

public interface IExprFunction extends IExpr {
	/**
	 * add a variable to a list of variables whose type should be changed inside 
	 * this literal function
	   @param varName
	   @param newType
	   @param newCode
	 */
	void addNewVarInfo(String varName, Type newType, String newCode);
}
