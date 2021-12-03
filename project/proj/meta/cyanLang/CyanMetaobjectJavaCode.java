/**
  
 */
package meta.cyanLang;


import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_cge;
import meta.ICompilerAction_dpa;
import meta.IParseWithoutCyanCompiler_dpa;
import meta.ReplacementPolicyInGenericInstantiation;

/**
 *  This is the class of metaobject "javacode" that is used to generate code
 *  in Java. When the compiler finds
 *       @javacode<<*    return _n; *>>
 *  It should replace this annotation by "    return _n; "  when generating Java 
 *  code.
   @author José
   
 */
public class CyanMetaobjectJavaCode extends CyanMetaobjectWithAt 
             implements IAction_cge, IParseWithoutCyanCompiler_dpa {

	public CyanMetaobjectJavaCode() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}
	/* (non-Javadoc)
	   @see ast.CyanMetaobject#getName()
	 */
	@Override
	public String getName() {
		return "javacode";
	}
	
	@Override
	public boolean shouldTakeText() { return true; }
	

	/* @see ast.CyanMetaobject#dsa_javaCodeThatReplacesMetaobjectAnnotation()()
	 */
	@Override
	public StringBuffer cge_codeToAdd() {
		StringBuffer sb = new StringBuffer(); 

		
		sb.append(((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getText());
		return sb;
	}

	@Override
	public ReplacementPolicyInGenericInstantiation getReplacementPolicy() {
		return ReplacementPolicyInGenericInstantiation.REPLACE_BY_JAVA_VALUE;
	}
	

	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {
		
		CyanMetaobjectWithAtAnnotation metaobjectAnnotationWithAt = (CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation;
		if ( metaobjectAnnotationWithAt.getText() == null ) {
			addError("Metaobject 'javacode' should take Java code between two sequences of symbols");
		}
	}
	
	@Override
	public String getPackageOfType() { return null; }
	
	@Override
	public String getPrototypeOfType() { return null; }
		
}
