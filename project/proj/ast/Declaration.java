/**
  
 */
package ast;

import java.util.ArrayList;
import meta.DeclarationKind;
import saci.Tuple2;

/** represents a declaration which may be a prototype declaration, a method, or an instance variable.
 * 
   @author José
   
 */
public interface Declaration {
	String getName();
	DeclarationKind getKind();
	
	ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList();
}
