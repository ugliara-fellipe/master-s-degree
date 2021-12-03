package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.ExprAnyLiteral;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.ICompilerAction_dpa;
import saci.NameServer;

public class CyanMetaobjectMOCParamAsArray extends CyanMetaobjectWithAt  implements meta.IAction_dpa {

	public CyanMetaobjectMOCParamAsArray() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}
	
	@Override
	public String getName() {
		return "moCallParamAsArray";
	}




	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		StringBuffer s = new StringBuffer();
		
		s.append("var moParamList = [ ");
		ArrayList<ExprAnyLiteral> parameterList =  annotation.getRealParameterList(); //annotation.getJavaParameterList();
		int size3 = parameterList.size();
		for ( Object obj : parameterList ) {
			String cyanStr = ((ExprAnyLiteral ) obj).asString();
			if ( cyanStr.charAt(0) == '#' )
				cyanStr = "\\" + cyanStr;
			cyanStr = NameServer.addQuotes(cyanStr);
			s.append( cyanStr );
			//s.append( ((ExprAnyLiteral ) obj).getStringJavaValue() );
			if ( --size3 > 0 ) 
				s.append(", ");
		}
		s.append(" ];\n");

		
		ArrayList<ArrayList<String>> strListList = compiler.getGenericPrototypeArgListList();
		s.append(" var genParamList = [ ");
		int size = strListList.size();
		for ( ArrayList<String> strList : strListList ) {
			s.append("[ ");
			int size2 = strList.size();
			for ( String elem : strList ) {
				s.append("\"" + elem + "\"");
				if ( --size2 > 0 )
					s.append(", ");
			}
			s.append(" ] ");
			if ( --size > 0 )
				s.append(", ");
		}
		s.append(" ];\n");
		
		return s;
	}


}
