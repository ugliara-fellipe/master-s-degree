package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import lexer.Lexer;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dsa;
import saci.NameServer;

public class CyanMetaobjectParametersToString extends CyanMetaobjectWithAt implements IAction_dsa {

	public CyanMetaobjectParametersToString() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}

	@Override
	public String getName() {
		return "parametersToString";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa)  {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		StringBuffer s = new StringBuffer("\"");
		for ( Object obj : annotation.getJavaParameterList() ) {
			s.append(Lexer.escapeJavaString(convert(obj)));
			s.append("\\n");
		}
		s.append("\\n" + Lexer.escapeJavaString(new String(annotation.getText())) + "\\n");
		s.append("\"");
		return s;
	}
	
	private String convert(Object top) {
		if ( top instanceof Object [] ) {
			String s = "[ ";
			Object []objArray = (Object []) top;
			int size = objArray.length;
			for ( Object obj : objArray ) {
				s += convert(obj);
				if ( --size > 0 ) {
					s += ", ";
				}
			}
			return s + " ]";
		}
		else 
			return "" + top;
	}
	
	@Override
	public boolean shouldTakeText() { return true; }	

	@Override
	public boolean isExpression() {
		return true;
	}

	

	@Override
	public String getPackageOfType() { return NameServer.cyanLanguagePackageName; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	@Override
	public String getPrototypeOfType() { return "String"; }		
}