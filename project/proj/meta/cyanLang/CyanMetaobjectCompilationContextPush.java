package meta.cyanLang;

import java.util.ArrayList;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import saci.NameServer;

/**
 * when the compiler generates code, it prefix it by
 * <ul>
  <li> <code>{@literal @}pushCompilationContext(id, moName, packageName, sourceFilename, lineNumber)</code>. This means the code was
  generate by the metaobject annotation of line <code>lineNumber</code> of source code of file <code>sourceFilename</code> of package <code>packageName</code>.
  The metaobject name is  <code>moName</code> . 

  </li>
  <li>  <code>{@literal @}pushCompilationContext(id, pp)</code> means that
  the code was generated in phase dpa (parsing) with the addition of methods <code>prototype</code>
  and <code>prototypeNew</code>.
  </li> 
  <li> <code>{@literal @}pushCompilationContext(id, new)</code> means that
  the code was generated in phase dpa (parsing) with the addition of methods <code>new</code>, <code>new:</code>,
  <code>defaultValue</code>, etc. These are the methods that the compiler adds to every prototype if necessary
  (there are <code>init</code> or <code>init:</code> methods and the source code does not define some methods such
  as <code>defaultValue</code>). 
  </li>
  <li> <code>{@literal @}pushCompilationContext(id, inner)</code> means that
  the code was generated in phase dpa (parsing) with the addition of inner prototypes. For each 
  method and anonymous function the compiler creates an inner prototype that is added to the code
  of the original prototype.
  </li>
</ul>

  
   This prefix with <code>pushCompilationContext</code> will give to the next
 * compilation phase information on who inserted this generated code. Then the error messages will be more precise. 
 * If there is any error in the generated code, the compiler will inform to the user who
 * inserted the code with the error. </p> 
 * 
 *  At the end of the generated code, the compiler adds <code>{@literal @}popCompilationContext(id)</code>. 
 *  
   @author José
 */public class CyanMetaobjectCompilationContextPush extends CyanMetaobjectWithAt  {
	
	public CyanMetaobjectCompilationContextPush() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}

	@Override
	public String getName() {
		return NameServer.pushCompilationContextName;
	}

	@Override
	public
	ArrayList<CyanMetaobjectError> check() {
		ArrayList<Object> javaObjectList = this.getMetaobjectAnnotation().getJavaParameterList();
		if ( javaObjectList == null || (javaObjectList.size() != 5 && javaObjectList.size() != 2) ) {
			return addError("An annotation of a metaobject that implements ICompilationContextInfoPush should have exactly two or five parameters");
		}
		else {
			/**  
			 * when the compiler generates code, it prefix it by 
			 * @pushCompilationContext(id, moName, packageName, sourceFileName, lineNumber).
			 * @pushCompilationContext(id, pp) 
			 * @pushCompilationContext(id, new) 
			 * @pushCompilationContext(id, inner)

			 */
			Object id = javaObjectList.get(0);
			if ( javaObjectList.size() == 2 ) {
				Object actionObj = javaObjectList.get(1);
				if ( ! (actionObj instanceof String) ) {
					return addError("An annotation of a metaobject that implements ICompilationContextInfoPush should have the two parameters of type 'String'");
				}
				String action = (String ) actionObj;
				if ( ! action.equals("pp") && ! action.equals("new") && ! action.equals("inner") ) {
					return addError("An annotation of a metaobject that implements ICompilationContextInfoPush should have the second parameter " +
				             "equal to 'pp', 'new', or 'inner'");
				}

			}
			else if ( javaObjectList.size() == 5 ) {
				Object cyanMetaobjectName = javaObjectList.get(1);
				Object packageNameContext = javaObjectList.get(2); 
				Object sourceFileNameContext = javaObjectList.get(3);
				Object lineNumber = javaObjectList.get(4);
				if ( !(id instanceof String) && !(cyanMetaobjectName instanceof String) && !(packageNameContext instanceof String) 
						&& !(sourceFileNameContext instanceof String) || !(lineNumber instanceof Integer) ) {
					return addError("An annotation of a metaobject that implements ICompilationContextInfoPush should have two parameters or five parameters " +
				             "of types  String, String, String, String, and int");
				}
				
				
			}
		}
		return null;
	}
	
}
