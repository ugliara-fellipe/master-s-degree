/**
 *
 */
package ast;

import java.util.ArrayList;
import saci.CyanEnv;
import saci.Env;

/**
 *  Represents either a single or several types separated by |
 *  In
 *      add: (int)*
 *   an object of TypeOneMany represents "int". In
 *      add: (int | String | Person)+
 *   an object of TypeOneMany represents
 *        int | String | Person
 *   each type is represented by one array element of typeArray.

 * @author José
 *
 */
public class TypeOneMany {

	public TypeOneMany(ArrayList<Expr> typeArray) {
		this.typeArray = typeArray;
	}

	public void setTypeArray(ArrayList<Expr> typeArray) {
		this.typeArray = typeArray;
	}

	public ArrayList<Expr> getTypeArray() {
		return typeArray;
	}



	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		int size = typeArray.size();
		for ( Expr e : typeArray ) {
			e.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			if ( --size > 0 ) {
				if ( printInMoreThanOneLine ) {
					pw.println(" | ");
					pw.printIdent("");
				}
				else
					pw.print(" | ");
			}
		}

	}

	/**
	 * part of the Java name corresponding to this alternative types. If
	 * this object represents
	 *      int | String | Is_a_boolean
	 * the result of this method will be
	 *      left_int_CyString_Is__a__boolean_right_or
	 *
	 * @return
	 */
	public String getJavaName() {
		if ( typeArray.size() == 1 )
			return typeArray.get(0).getJavaName();
		else {
			String s = "left";
			for ( Expr e : typeArray )
				s = "_" + e.getJavaName();
			return  s + "_right_or";
		}
	}


	public String getType() {
		String s = "";
		int size = typeArray.size();
		for ( Expr e : typeArray ) {
			s = s + PWCounter.toStringBuffer(e).toString();
			if ( --size > 0 )
				s = s + ", ";
		}
		return (typeArray.size() > 1) ? "UUnion<" + s + ">" : s;
	}

	public void calcInterfaceTypes(Env env) {
		for ( Expr expr : typeArray ) 
			expr.calcInternalTypes(env);
	}



	private ArrayList<Expr> typeArray;






}
