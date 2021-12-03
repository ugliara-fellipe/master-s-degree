package meta.cyanLang;

import java.util.ArrayList;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.ProgramUnit;
import error.ErrorKind;
import meta.CyanMetaobjectWithAt;
import meta.ICompiler_ati;
import meta.IEnv_ati;
import saci.NameServer;

public class CyanMetaobjectCreateArrayMethods  extends CyanMetaobjectWithAt 
	implements meta.IActionProgramUnit_ati {

	public CyanMetaobjectCreateArrayMethods() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}
	@Override
	public String getName() {
		return "createArrayMethods";
	}

	@Override
	public StringBuffer ati_codeToAdd(
			ICompiler_ati compiler_ati) {
		
		
		
		
		ArrayList<ArrayList<String>> strListList = compiler_ati.getGenericPrototypeArgListList();
		if ( strListList == null || strListList.get(0) == null || strListList.get(0).size() != 1 ) {
			compiler_ati.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Metaobject '" + getName() + "' should only be used in a generic prototype with just one parameter", 
					this.getMetaobjectAnnotation().getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error);
			return null;
		}
		String paramTypeName = strListList.get(0).get(0);


		IEnv_ati env = compiler_ati.getEnv();
		
		String javaParamTypeName = NameServer.getJavaName(paramTypeName);
		
		if ( paramTypeName.equals(NameServer.dynName) )
			return null;
		
		ProgramUnit pu = env.searchPackagePrototype(paramTypeName, env.getCurrentProgramUnit().getFirstSymbol());
		if ( pu != null ) {
			ArrayList<MethodSignature> methodSignatureList = env.searchMethodPublicSuperPublic(pu, "<=>1");
			if ( methodSignatureList != null && methodSignatureList.size() > 0 ) {
				/**
				 * add method 'sort'
				 */
				StringBuffer s = new StringBuffer();
				
				s.append("    @javacode{*+\n");
				s.append("    public static java.util.Comparator<" + javaParamTypeName + "> lowToHighComparator\n");
				s.append("                              = new java.util.Comparator<" + javaParamTypeName + ">() {\n");
				s.append("\n");
				s.append("	        public int compare(" + javaParamTypeName + " elem1, " + javaParamTypeName + " elem2) {\n");
				s.append("\n");
				s.append("    	        return elem1._lessThan_equal_greaterThan(elem2).n;\n");
				s.append("\n");
				s.append("	        }\n");
				s.append("\n");
				s.append("	    };\n");

				s.append("    public static java.util.Comparator<" + javaParamTypeName + "> highToLowComparator\n");
				s.append("                              = new java.util.Comparator<" + javaParamTypeName + ">() {\n");
				s.append("\n");
				s.append("	        public int compare(" + javaParamTypeName + " elem1, " + javaParamTypeName + " elem2) {\n");
				s.append("\n");
				s.append("    	        return elem2._lessThan_equal_greaterThan(elem1).n;\n");
				s.append("\n");
				s.append("	        }\n");
				s.append("\n");
				s.append("	    };\n");
				s.append("    +*}\n");


				s.append("    func sort -> Array<" + paramTypeName + "> {\n");
				s.append("        @javacode{*\n");
				s.append("            Collections.sort( array, lowToHighComparator );\n");
				s.append("        *}\n");
				s.append("        return self\n");
				s.append("    }\n");


				s.append("    func sortDescending -> Array<" + paramTypeName + "> {\n");
				s.append("        @javacode{*\n");
				s.append("            Collections.sort( array, highToLowComparator );\n");
				s.append("        *}\n");
				s.append("        return self\n");
				s.append("    }\n"); 				
				s.append("");
				return s;
			}
				// _lessThan_equal_greaterThan
		}
		return null;
	}

}
