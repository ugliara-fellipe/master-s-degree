package meta;

/**
 * This interface should be implemented by all metaobjects that are attached to a package (in the .pyan project file) 
 * and that need to do checking in the last dsa phase (semantic analysis).
 * Method {@link ICheckPackage_dsa2#dsa2_checkPackage(ICompilerPackageView_dsa)} may use the visitor of its parameter
 * to collect information on the package. The methods of this interface cannot add code. They are called,
 * of course, in the second dsa phase of the compilation.
   @author jose
 */
public interface ICheckPackage_dsa2 extends ICheckPackage_ati_dsa {
	void dsa2_checkPackage(ICompilerPackageView_ati compilerPackageView_dsa);

}
