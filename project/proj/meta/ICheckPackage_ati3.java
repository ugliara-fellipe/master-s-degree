package meta;

/**
 * This interface should be implemented by all metaobjects that are attached to a package (in the .pyan project file) and that need to do checking.
 * Method {@link ICheckPackage_ati3#ati3_checkPackage(ICompilerPackageView_ati)} may use the visitor of its parameter
 * to collect information on the package. The methods of this interface cannot add code. They are called,
 * of course, in the third ati phase of the compilation.
   @author jose
 */
public interface ICheckPackage_ati3 extends ICheckPackage_ati_dsa {
	void ati3_checkPackage(ICompilerPackageView_ati compilerPackageView_ati);

}
