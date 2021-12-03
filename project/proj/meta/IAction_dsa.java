package meta;

import java.util.ArrayList;
import saci.Tuple2;

/**
 * This interface should be implemented by all metaobjects that should add code at phase dsa.
   @author José
 */
public interface IAction_dsa {
	/**
	 * Return Cyan code to be added after the metaobject annotation during semantic analysis.
	 * Only metaobjects annotations inside methods are affected. That is, message {@link #dsa_codeToAdd(ICompiler_dsa)}
	 * is sent only to metaobject annotations that are inside methods. If
	 * metaobject <code>foo</code> produces <code>i print;</code> (this method returns
	 * <code>"i print;"</code> in the second tuple element)
	 * then the code
	   <code>
	   @foo
	   i = 1;
	   </code>
	   </p>
	   will be replaced by
	   </p>
	   <code>
	   @foo#dsa
	   i print;
	   i = 1;
	   </code>
	   </p>

	   @param compiler_dsa the compiler
	 */
	StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa);

	/**
	 * this method should return a list of tuples. Each tuple is composed by
	 * a prototype name and the code of the compilation unit in which the prototype is ---
	 * the full text of the file.
	 * The compiler will create this prototype in the current package.
	 * @param compiler
	 */
	default ArrayList<Tuple2<String, StringBuffer>> dsa_NewPrototypeList(ICompiler_dsa compiler) { return null; }

}
