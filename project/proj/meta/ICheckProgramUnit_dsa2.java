package meta;

/**
 * Metaobject classes implementing this interface do checks in the source code during
 * the semantic analysis (calculate internal types) of step 9 of the compilation. 
   @author jose
 */
public interface ICheckProgramUnit_dsa2 extends ICheckProgramUnit_ati_dsa {
	void dsa2_checkProgramUnit(ICompiler_dsa compiler);
}
