package meta;

/**
 * This interface should be implemented by all metaobjects that are attached to a program unit and that need to do checking.
 * Method {@link ICheckProgramUnit_ati3#ati3_checkProgramUnit(ICompiler_ati)} may use the visitor of its parameter
 * to collect information on the program. The methods of this interface cannot add code. They are called,
 * of course, in the third ati phase of the compilation.
   @author jose
 */

public interface ICheckProgramUnit_ati3 extends ICheckProgramUnit_ati_dsa {
	void ati3_checkProgramUnit(ICompiler_ati compiler_ati );

}
