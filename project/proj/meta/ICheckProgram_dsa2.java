package meta;


/**
 * This interface should be implemented by all metaobjects that are attached to a program (in the .pyan project file) 
 * and that need to do checking in the second dsa phase (semantic analysis).
 * Method {@link ICheckProgram_dsa2#dsa2_checkProgram(ICompilerProgramView_ati)} may use the visitor of its parameter
 * to collect information on the program. The methods of this interface cannot add code. They are called,
 * of course, in the second dsa phase of the compilation.
   @author jose
 */

public interface ICheckProgram_dsa2 extends ICheckProgram_ati_dsa {
	void dsa2_checkProgram(ICompilerProgramView_ati compilerProgramView_ati);

}
