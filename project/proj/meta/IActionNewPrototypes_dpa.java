package meta;

import java.util.ArrayList;
import saci.Tuple2;

public interface IActionNewPrototypes_dpa {
	/**
	 * this method should return a list of tuples. Each tuple is composed by  
	 * a prototype name and the code of the compilation unit in which the prototype is ---
	 * the full text of the file. 
	 * The compiler will create this prototype in the current package. 
	 * @param compiler 
	 */
	default ArrayList<Tuple2<String, StringBuffer>> dpa_NewPrototypeList(ICompilerAction_dpa compiler) { return null; }	
}
