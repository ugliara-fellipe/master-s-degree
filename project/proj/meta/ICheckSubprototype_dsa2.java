package meta;

import ast.Type;

/**
 * This interface should be implemented by any metaobject that needs to check
 * sub-prototypes. That is, if a metaobject attached to a prototype implements
 * this interface, method {@link ICheckSubprototype_dsa2#dsa2_checkSubprototype(ICompiler_dsa, Type)}
 * of it will be called whenever the prototype is inherited directly or indirectly. This call is made
 * in the second phase dsa (semantic analysis), step 9 of the compilation.
 *
 * This method will be called for any descendants of the prototype to which the
 * metaobject is attached.
 *
   @author jose
 */
public interface ICheckSubprototype_dsa2 extends ICheckProgramUnit_ati_dsa {
	/**
	 * Suppose the Java class W implements this interface and is put
	 * in the metaobject folder which is included in the compilation.
	 * Suppose method W::getName() returns "subprototypeList" and
	 * prototype Proto is declared as
	 *        {@literal @} subprototypeList(Sub) object Proto ... end
	 * When Proto is inherited, the compiler calls method
	 * {@link ICheckSubprototype_ati3#ati_checkSubprototype(Type)}  with the sub-prototype as parameter.
	 * That is, if we have
	 *            object Sub extends Proto ... end
	 *  the compiler calls {@link ICheckSubprototype_ati3#ati_checkSubprototype(Type)} passing
	 *  Sub as parameter.
	 */
	void dsa2_checkSubprototype(ICompiler_dsa compiler_dsa, Type t);


}
