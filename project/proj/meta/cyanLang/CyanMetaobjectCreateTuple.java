package meta.cyanLang;

import java.util.ArrayList;
import ast.MetaobjectArgumentKind;
import lexer.Lexer;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;
import saci.NameServer;

public class CyanMetaobjectCreateTuple extends CyanMetaobjectWithAt 
      implements IAction_dpa {

	public CyanMetaobjectCreateTuple() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}
	
	@Override
	public String getName() {
		return "createTuple";
	}




	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
		ArrayList<ArrayList<String>> strListList = compiler.getGenericPrototypeArgListList();
		String fullPrototypeName = compiler.getCurrentPrototypeName();
		int indexOfLessThan = fullPrototypeName.indexOf('<');
		if ( ! compiler.getCurrentPrototypeId().equals("Tuple") || strListList == null || indexOfLessThan < 0 ) {
			compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
					"This metaobject should only be used in generic prototype Tuple");
			return null;
		}
		if ( strListList.size() != 1 ) {
			compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
					"This generic prototype should be instantiated with just one pair of '<' and '>' as in 'Tuple<Int, String>'");
			return null;
		}
		ArrayList<String> strList = strListList.get(0);
		boolean isNTuple = false;
		for ( String str : strList ) {
			int indexOfDot = str.indexOf('.');
			if ( Character.isLowerCase(str.charAt(0)) && indexOfDot < 0 ) {
				   // found a symbol such as 'key' and 'value' in Tuple<key, Int, value, String>
				   // if indexOfDot >= 0 then 'str' would be a prototype preceded by a package 
				isNTuple = true;
			}
		}
		ArrayList<String> typeList;
		String []fieldList;
		int size;
		int i;
		
		if ( isNTuple ) {
			// NTuple
			if ( strList.size() %2 != 0 ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), "The number of generic parameters to this generic prototype should be even");
				return null;
			}
			typeList = new ArrayList<>();
			size = strList.size()/2;
			fieldList = new String[size];
			int k = 0;
			for ( String str : strList ) {
				if ( k%2 == 0 ) {
					if ( ! Character.isLowerCase(str.charAt(0)) || str.indexOf('.') >= 0 ) {
						compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
								"The " + k + "-th parameter to this generic prototype should start with a lower case letter");
						return null;
					}
					else
						fieldList[k/2] = str;
				}
				else {
					if ( ! Character.isUpperCase(str.charAt(0)) && str.indexOf('.') < 0 )  {
						compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
								"The " + k + "-th parameter to this generic prototype should start with an upper case letter");
						return null;
					}
					else
						typeList.add(str);
				}
				++k;
			}
		}
		else {
			// Tuple
			typeList = strListList.get(0);
			size = typeList.size();
			fieldList = new String[size];
			for (i = 0; i < size; ++i) { 
				fieldList[i] = "f" + (i+1);
			}
		}
		
		
		StringBuffer s = new StringBuffer();

		s.append("    func init: (");
		for (i = 0; i < size; ++i) {
			String tn = typeList.get(i);
			s.append(tn + " g" + (i+1));
			if ( i < size - 1 ) 
				s.append(", ");
		}
		s.append(") { \n");
		for (i = 0; i < size; ++i) {
			s.append("        _" + fieldList[i] + " = g" + (i+1) + ";\n");
		}
		s.append("    }\n");
		
		if ( size > 1 ) {
			s.append("    func ");
			for (i = 0; i < size; ++i) {
				String tn = typeList.get(i);
				s.append(fieldList[i] + ": " + tn + " g" + (i+1) + " ");
			}
			s.append("-> Tuple<");
			String s2 = "";
			for(i = 0; i < size; ++i) {
				s2 += fieldList[i] + ", " + typeList.get(i);
				if ( i < size - 1)
					s2 += ", ";
			}
			s.append(s2 + "> { \n" );
	
			s.append("        return Tuple<" + s2 + "> new: ");
			for (i = 0; i < size; ++i) {
				s.append(" g" + (i+1));
				if ( i < size - 1 )
					s.append(", ");
			}
			s.append(";\n");
			

			s.append("    }\n");
			
		}
		for (i = 0; i < size; ++i) {
			String tn = typeList.get(i);
					
			s.append("    @annot( #" + fieldList[i] + " ) var " + tn + " _" + fieldList[i] + "\n");
			s.append("    func " + fieldList[i] + " -> " + tn + " = _" + fieldList[i] + ";\n");
			s.append("    func " + fieldList[i] + ": " + tn + " other { _" + fieldList[i] + " = other }\n");
		}
		fullPrototypeName = Lexer.addSpaceAfterComma(fullPrototypeName);
		s.append("    override\n");
		s.append("    func == (Dyn other) -> Boolean {\n");
		s.append("        if other isA: " + fullPrototypeName + " {\n");
		String javaFPN = NameServer.getJavaName(fullPrototypeName);
		s.append("            var " + fullPrototypeName + " another;\n");
		s.append("            @javacode{*");
		s.append("             _another = (" + javaFPN + " ) _other;\n");
		s.append("            *}\n");
		
		//s.append("            let " + fullPrototypeName + " another = " + fullPrototypeName + " cast: other;\n");
		for (int k = 0; k < fieldList.length; ++k) {
			s.append("            if " + fieldList[k] + " != (another " + fieldList[k] + ") {  return false }" + "\n");
		}
		s.append("            return true\n");
		s.append("        }\n");
		s.append("        else {\n");
		s.append("            return false\n");
		s.append("        }\n");
		s.append("     }\n");
		
		/*
         return "[. key = " + key + ", value = " + value+ " .] "
        */
		s.append("    override");
		s.append("    func asString -> String {\n");
		s.append("         return \"[. ");
		for (i = 0; i < size; ++i) {
			String f = fieldList[i];
			if ( typeList.get(i).equals("Nil") || typeList.get(i).equals("cyan.lang.Nil") ) {
				s.append(f + " = Nil\"" );
			}
			/*
			else if ( typeList.get(i).equals("String") || typeList.get(i).equals("cyan.lang.String") ) {
				  //     return "[. key = \"" ++ key\" ++ ", value = " ++ value ++ " .]"
				  //     return "[. key = \"" ++ key ++ "\" ++ ", value = " ++ value ++ " .]"
				s.append(f + " = \\\"\" ++ " + f + " ++ \"\\\"\"");
			}
			*/
			else {
				s.append(f + " = \" ++ " + f + " asStringQuoteIfString");
			}
			if ( i < size - 1 ) 
				s.append(" ++ \", ");
		}
		s.append(" ++ \" .]\"\n");
		s.append("     }\n");

		
		
		s.append("    func copyTo: (Any other) { }\n");
		return s;
	}

	/*
	@Override
	public StringBuffer dsa_codeToAddAtMetaobjectAnnotation(ICompiler_dsa compiler) {
		
		ArrayList<ArrayList<String>> strListList = compiler.getGenericPrototypeArgListList();

		/*
		 * copied from the previous method
		 * /
		ArrayList<String> strList = strListList.get(0);
		boolean isNTuple = false;
		for ( String str : strList ) {
			int indexOfDot = str.indexOf('.');
			if ( Character.isLowerCase(str.charAt(0)) && indexOfDot < 0 ) {
				isNTuple = true;
			}
		}
		ArrayList<String> typeList;
		String []fieldList;
		int size;
		int i;
		
		if ( isNTuple ) {
			typeList = new ArrayList<>();
			size = strList.size()/2;
			fieldList = new String[size];
			int k = 0;
			for ( String str : strList ) {
				if ( k%2 == 0 ) {
					fieldList[k/2] = str;
				}
				else {
					typeList.add(str);
				}
				++k;
			}
		}
		else {
			// Tuple
			typeList = strListList.get(0);
			size = typeList.size();
			fieldList = new String[size];
			for (i = 0; i < size; ++i) { 
				fieldList[i] = "f" + (i+1);
			}
		}

		//NameServer.splitPackagePrototype(packageProtoName)
		compiler.getEnv().searchPackagePrototype(packageName, prototypeName)
		
		return null;
	}
	*/
}
