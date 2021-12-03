/**
  
 */
package meta;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.MetaobjectArgumentKind;


/**
 * Represent a metaobject annotationed using the syntax
 *        @meta(parameters)<++ ... ++>
 *        
   @author José
 */

abstract public class CyanMetaobjectWithAt extends CyanMetaobject {
	
	
	@Override
	public CyanMetaobjectWithAt clone() {
		try {
			return (CyanMetaobjectWithAt ) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}	
	
	public CyanMetaobjectWithAt(MetaobjectArgumentKind parameterKind) { 
		this.parameterKind = parameterKind;
	}
	
	

	public MetaobjectArgumentKind getParameterKind() {
		return parameterKind;
	}

	
	/**
	 * return true if this metaobject is attached to a 
	 * declaration (variable, method, prototype) or statement.
	 * As an example, metaobject javacode is not attached to
	 * anything.
	 * 
	   @return
	 */
	final public boolean attachedToSomething() {
		return mayBeAttachedList() != null;
	}

	/**
	 * return an array with all kinds of declaration to which this metaobject may be attached.
	 * The default is every declaration. However, this result is only valid if
	 * {@link #attachedToSomething()} returns true.
	 */
	public DeclarationKind []mayBeAttachedList() {
		return null;
	}
	
	/**
	 * return true if this metaobject may be attached to <code>decKind</code> 
	 */
	public boolean mayBeAttached(DeclarationKind decKind) {
		if ( mayBeAttachedList() == null ) 
			return false;
		else {
			for ( DeclarationKind d : mayBeAttachedList() ) {
				if ( d == decKind ) 
					return true;
			}
			return false;
		}
	}
	
	public String attachedListAsString() {
		String s = "";
		DeclarationKind []list = mayBeAttachedList();
		int size = list.length;
		for ( DeclarationKind d : list ) {
			s = s + d.toString();
			if ( --size > 0 ) 
				s = s + ", ";
		}
		return s;
	}
	
	/**
	 * returns the declaration to which this metaobject is attached to. It may
	 * be MethodDec, InstanceVariableDec etc.
	 */
	public Declaration getAttachedDeclaration() {
		return ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getDeclaration();
	}
	
	
	
	/**
	 * return true if this metaobject should necessarily be followed by a text
	 * between two sequences of symbols. For example, metaobject <code>javacode</code> 
	 * should always be followed by a Java code: <br>
	 * <code>
	 *          {@literal @}javacode<<*    return _index; *>>  <br>
	 * </code><br>
	 */
	public boolean shouldTakeText() { return false; }
	
	@Override
	public CyanMetaobjectWithAtAnnotation getMetaobjectAnnotation() {
		return ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation);
	}

	/**
	 * return true if this metaobject is an expression by itself. Then it can be used as in
	 * <code><br>
	 * var n = {@literal @}color(red);
	 * </code><br>
	 * 
	 * If this method returns true, 
	 * 
	 */
	@Override
	public boolean isExpression() {
		return false;
	}

	

	@Override
	public String getPackageOfType() { return null; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	@Override
	public String getPrototypeOfType() { return null; }	

	/**
	 * check if the metaobject annotation is semantically correct just after it is found
	 * during parsing. Returns <code>null</code> if it is. Otherwise returns a list of error messages
	 */
	public ArrayList<CyanMetaobjectError> check() {
		return null;
	}
	
	
	/**
	 * kind of parameter that this metaobject demands
	 */
	
	protected MetaobjectArgumentKind	parameterKind;


	

}
