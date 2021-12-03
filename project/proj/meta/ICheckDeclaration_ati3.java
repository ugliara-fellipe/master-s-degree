package meta;

/**
 * Define methods for making checks during the THIRD ati phase of the compiler.
 * Metaobject classes implementing this interface should be attached to 
 * a prototype, method, or instance variable. 
   @author José
 */
public interface ICheckDeclaration_ati3 extends ICheckProgramUnit_ati_dsa {
	
	void ati3_checkDeclaration(ICompiler_ati compiler_ati);

}
