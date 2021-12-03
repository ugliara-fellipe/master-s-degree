package meta.cyanLang;

import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICommunicateInPackage_ati_dsa;
import meta.ICompiler_dsa;
import saci.Tuple6;

public class CyanMetaobjectPrintInfoPackage extends CyanMetaobjectWithAt implements IAction_dsa, ICommunicateInPackage_ati_dsa {

	public CyanMetaobjectPrintInfoPackage() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}


	@Override
	public String getName() {
		return "printInfoPackage";
	}
	
	@Override
	public void ati_dsa_receiveInfoPackage( HashSet<Tuple6<String, Integer, Integer, String, String, Object>> moInfoSet ) {
		/*
          Every tuple is composed by a 
               metaobject name, 
               the number of this metaobject considering all metaobjects in the prototype, 
               the number of this metaobject considering only the metaobjects with the same name of the same prototype, 
               the package name of the metaobject annotation, 
               the prototype name of the metaobject annotation, 
               the information this metaobject annotation wants to share with other metaobject annotations, 
                     which is the value returned by ati_dsa_shareInfoPackage().		 * 
		 */
		CyanMetaobjectWithAtAnnotation withAt = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		withAt.setInfo_dpa(moInfoSet);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		CyanMetaobjectWithAtAnnotation withAt = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;

		HashSet<Tuple6<String, Integer, Integer, String, String, Object>> moInfoSet = (HashSet<Tuple6<String, Integer, Integer, String, String, Object>> ) withAt.getInfo_dpa();
		StringBuffer s = new StringBuffer("\"\"\"");
		Formatter format = new Formatter(s, Locale.ENGLISH);
    	format.format("%-20s %-15s %-17s %-8s %-12s %s\n", "Metaobject name", "num all proto", "num same in proto", "package", "prototype", "info");		
		for ( Tuple6<String, Integer, Integer, String, String, Object> t : moInfoSet ) {
	    	format.format("%-20s %-15s %-17s %-8s %-12s '%s'\n", t.f1, t.f2, t.f3, t.f4, t.f5, t.f6);		
		}
		s.append("\"\"\" println;");
		format.close();
		return s;
	}
}
