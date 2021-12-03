package meta.cyanLang;


import java.util.ArrayList;
import java.util.HashSet;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;

/**
 * This metaobject create the methods for the CatchExit prototype
   @author Jos�
 */

public class CyanMetaobjectCreateCatchExit extends CyanMetaobjectWithAt 
	implements IAction_dpa {

		public CyanMetaobjectCreateCatchExit() { 
			super(MetaobjectArgumentKind.ZeroParameter);
		}

		@Override
		public String getName() {
			return "createCatchExit";
		}



		@Override
		public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
			
			StringBuffer s = new StringBuffer();
			
			if ( ! compiler.getCurrentPrototypeId().equals("CatchExit") ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Metaobject '" + getName() + 
						"' should only be used in prototype CatchExit");
				return null;
			}

			
			ArrayList<ArrayList<String>> strListList = compiler.getGenericPrototypeArgListList();
			/* if ( strListList == null || ! compiler.getCurrentPrototypeName().startsWith("Union") ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Metaobject '" + getName() + 
						"' should only be used in a generic prototype", 
						this.getMetaobjectAnnotation().getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error);
				return null;
			}
			*/
			
			int sizeListList = strListList.size();
			if ( sizeListList != 1 ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
						"Prototype 'CatchExit' should have just one pair of '<' and '>' with parameters (like 'CatchExit<Exception1, Exception2>')");
				return null;
			}
			ArrayList<String> strList = strListList.get(0);
			HashSet<String> set = new HashSet<>();
			
			s.append("    overload\n");
			for ( String protoName : strList ) {
				s.append("    func eval: " + protoName + " e { exit: e prototypeName }\n ");
				if ( set.contains(protoName) ) {
					compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
							"Prototype '" + protoName + "' has been used twice in the instantiation of 'CatchExit'");
				}
				set.add(protoName);
			}
			s.append("\n");
			s.append("    func exit: String protoName { \n");
			s.append("        (\"Fatal error: exception \" ++ protoName ++ \" was thrown\") println;\n" );
			s.append("        System exit\n");
			s.append("    }\n");
			s.append("\n");
			return s;
		}

	}

