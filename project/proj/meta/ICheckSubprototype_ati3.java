package meta;

import ast.Type;

/**
 * This interface should be implemented by any metaobject that needs to check
 * sub-prototypes. That is, if a metaobject attached to a prototype implements
 * this interface, method {@link ICheckSubprototype_ati3#ati_checkSubprototype(Type)}
 * of it will be called whenever the prototype is inherited directly or indirectly.
 * That is, this method will be called for any descendants of the prototype to which the
 * metaobject annotation is attached. This method is called in the third ati phase of the compilation.
   @author jose
 */
public interface ICheckSubprototype_ati3 extends ICheckProgramUnit_ati_dsa {
	/**
	 * Suppose a prototype Proto has an attached metaobject that implements this interface.
	 * When Proto is inherited, the compiler calls method
	 * {@link ICheckSubprototype_ati3#ati_checkSubprototype(Compiler_ati, Type)}  with the sub-prototype as parameter.
	 * That is, if we have
	 *            object Sub extends Proto ... end
	 *  the compiler calls {@link ICheckSubprototype_ati3#ati_checkSubprototype(compiler_ati, Type)} passing
	 *  a compiler and Sub as parameters.
	 *
	   @param t
	   @return a list of error messages or null if there is no error.
	 */
	void ati3_checkSubprototype(Compiler_ati compiler_ati, Type t);

}
