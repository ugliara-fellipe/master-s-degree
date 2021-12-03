package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.ObjectDec;
import meta.CyanMetaobject;
import meta.CyanMetaobjectWithAt;
import meta.IActionProgramUnit_ati;
import meta.IAction_dpa;
import meta.IAction_dsa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import saci.Tuple2;

public class CyanMetaobjectPrintAll extends CyanMetaobjectWithAt implements IAction_dpa, IActionProgramUnit_ati, IAction_dsa {

	public CyanMetaobjectPrintAll() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}

	@Override
	public String getName() {
		return "printAll";
	}
	

	@Override
	public StringBuffer dpa_codeToAdd(
                    ICompilerAction_dpa compiler ) {
    	
    	StringBuffer s = new StringBuffer("\"");
    	CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
    	for ( Object param : annotation.getJavaParameterList() ) {
    		if ( param instanceof String ) {
    			param = CyanMetaobject.removeQuotes( (String ) param);
    		}
    		s.append(param + " ");
    	}
		if ( compiler.getCurrentMethod() == null ) {
			this.addError("Metaobject '" + this.getName() + 
					"' should only be used inside a method of a prototype");
		}
    	s.append("\" println;");
        return s;
    }

	@Override
	public StringBuffer ati_codeToAdd(
			ICompiler_ati compiler_ati) { 
		return new StringBuffer("Out println: \"method ten returns \" ++ ten;"); 
	}	
	
	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
            ICompiler_ati compiler)	{
		
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		if ( annotation.getMetaobjectAnnotationNumberByKind() == 1 ) {
			ObjectDec currentObj = compiler.getEnv().getCurrentObjectDec(); 
			ArrayList<MethodSignature> msList = compiler.getEnv().
					   searchMethodProtectedPublicSuperProtectedPublic(currentObj, "ten"); 
			if ( (msList != null && msList.size() > 0) ||
				 currentObj.searchMethodPrivate("ten") != null ||
				 currentObj.searchInstanceVariable("ten") != null
		      ) {
				return null;
			}
					
			ArrayList<Tuple2<String, StringBuffer>> tupleList = new ArrayList<>();
			tupleList.add( new Tuple2<String, StringBuffer>(
					"Program", new StringBuffer("func ten -> Int = 10;"))); 
			return tupleList;
		}
		else {
			return null;
		}
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		if ( compiler_dsa.searchLocalVariableParameter("ten") != null ) {
			addError("There is a local variable called 'ten'. "
					+ "This is illegal when using this metaobject");
		}
		return new StringBuffer("\"produced by dsa_codeToAdd\" println;");
	}
	
}

/*
 * 				 compiler.getEnv().searchLocalVariableParameter("ten") != null ||
				 compiler.getEnv().searchInstanceVariable("ten") != null

*/