/**
  
 */
package meta;

import ast.ExprAnyLiteral;
import saci.Tuple2;

/**
 * interface with methods to allow information to flow from the metaobjects to the compiler 
 * The information is composed by a string and an object which may be anything that can be
 * parameter to a metaobject  
   @author jose
   
 */
public interface ICompilerInfo_dpa {
	default Tuple2<String, ExprAnyLiteral> infoToAddProgramUnit() { return null; }
}
