package meta.cyanLang;


import java.util.ArrayList;
import java.util.HashSet;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;

/**
 * This metaobject create the  methods for the CatchIgnore prototype
   @author José
 */

public class CyanMetaobjectCreateCatchIgnore extends CyanMetaobjectWithAt 
	implements IAction_dpa {

		public CyanMetaobjectCreateCatchIgnore() { 
			super(MetaobjectArgumentKind.ZeroParameter);
		}

		@Override
		public String getName() {
			return "createCatchIgnore";
		}



		@Override
		public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
			
			StringBuffer s = new StringBuffer();
			String currentProtoName = compiler.getCurrentPrototypeId();
			
			if ( ! currentProtoName.equals("CatchIgnore") ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Metaobject '" + getName() + 
						"' should only be used in prototype CatchIgnore");
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
						"Prototype 'CatchIgnore' should have just one pair of '<' and '>' with parameters (like 'CatchIgnore<Exception1, Exception2>')");
				return null;
			}
			ArrayList<String> strList = strListList.get(0);
			HashSet<String> set = new HashSet<>();
		
			s.append("    overload\n");
			for ( String protoName : strList ) {
				s.append("    func eval: " + protoName + " e { }\n ");
				if ( set.contains(protoName) ) {
					compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
							"Prototype '" + protoName + "' has been used twice in the instantiation of 'CatchIgnore'");
				}
				set.add(protoName);
			}
			return s;
		}

	}
