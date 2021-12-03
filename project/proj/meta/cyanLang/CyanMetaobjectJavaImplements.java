package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import ast.ObjectDec;
import ast.ProgramUnit;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompiler_dsa;
import saci.NameServer;

public class CyanMetaobjectJavaImplements extends CyanMetaobjectWithAt implements ICheckProgramUnit_dsa2 {

	public CyanMetaobjectJavaImplements() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public String getName() {
		return "javaImplements";
	}


	@Override
	public DeclarationKind []mayBeAttachedList() {
		return null;
	}
	
	@Override
	public void dsa2_checkProgramUnit(ICompiler_dsa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation  ) this.metaobjectAnnotation;
		ProgramUnit pu = compiler.getEnv().getCurrentProgramUnit(); // (ProgramUnit ) annotation.getDeclaration();
		if ( pu == null ) {
			addError("This metaobject should be used inside a prototype");
			return ;
		}
		if ( ! (pu instanceof ObjectDec) ) {
			addError("This metaobject cannot be used with interfaces");
			return ;
		}
		Object objParam = annotation.getJavaParameterList().get(0);
		if ( !(objParam instanceof String) ) {
			addError("This metaobject should take a literal string or identifier as parameter");
			return ;
		}
		String param = (String ) objParam;
		param = NameServer.removeQuotes(param);
		
		ObjectDec proto = (ObjectDec ) pu;
		if ( ! proto.addJavaInterface(param) ) {
			compiler.error(annotation.getFirstSymbol(), "The interface '" + param + "' is already in the list of Java interfaces "
					+ "implemented by this prototype");
		}
	}
	

}
