package meta;

import ast.Type;

/**
 * this interface is used when assignments  should be changed in the generated Java code. The right-hand side
 * of the assignment, in Java, should be changed to the value returned by this method. The code
 * should be  changed not only in assignments but also in parameter passing and return value of 
 * methods. 
 * <br>
 * As an example, consider an Union {@code Union<Int, String>} and a variable {@code intStr} of this type.
 * an assignment <br>
 * {@code     intStr = 0}<br>
 * should be changed in the Java code to<br>
 * {@code     _intStr = _UnionJavaName._assign( new CyInt(0) )}  
 * 
   @author Jos?
 */
public interface IActionAssignment_cge {

	String cge_changeRightHandSideTo(Type leftType, String rightHandSideExprInJava, Type rightType);

}
