package meta;

import java.util.ArrayList;
import error.FileError;
import saci.Tuple2;

/**
 * compiler as seen by Codegs. "ded" is "during editing". This interface brings the view of the compiler after just
 * the parsing of the source code being edited. 
   @author jose
 */
public interface ICompiler_ded extends IAbstractCyanCompiler {

	/**
	 * write <code>data</code> to file <code>fileName</code> of the data directory of package <code>packageName</code>. 
	 * If there is any error, this is signaled in symbol <code>sym</code> with message <code>errorMessage</code>. 
	 * <br>
	 * 
	 * Return an object of type {@link error.FileError} which may be checked to discover if there was any errors in the process. 
	 * 
	 * This method should check if the current compilation unit imported package <code>packageName</code>.
	 * Currently that is not checked. 
	   @param fileName
	   @param packageName
	   @return
	 */
	FileError saveBinaryDataFileToPackage(byte[] data, String fileName, String packageName);	
	
	/**
	 * return the absolute path in which the file <code>fileName</code> would be stored in the data directory of package <code>packageName</code>.
	 * If the package does not exist or the fileName is invalid, returns null
	   @param fileName
	   @param packageName
	   @return
	 */
	String pathDataFilePackage(String fileName, String packageName);
	
	/**
	 * return a list of pairs (name, type) of local variables visible where the metaobject annotation is. If the type is not explicitly given,
	 * the 'type' is null
	   @return
	 */
	ArrayList<Tuple2<String, String>> getLocalVariableList(); 
	/**
	 * return a list of pairs (name, type) of instance variables of the current prototype
	   @return
	 */
	ArrayList<Tuple2<String, String>> getInstanceVariableList(); 
}
