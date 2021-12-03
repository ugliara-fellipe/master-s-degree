package meta.cyanLang;

import java.util.ArrayList;
import java.util.HashSet;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import error.ErrorKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICommunicateInPrototype_ati_dsa;
import meta.ICompiler_ati;
import saci.NameServer;
import saci.Tuple3;
import saci.Tuple4;


/**
 *   {@literal @}addMethodTo("prototypeToWhereAddMethod", "methodName", "methodToAdd")
 *   
   @author jose
 */
public class CyanMetaobjectAddMethodTo extends CyanMetaobjectWithAt implements IActionProgramUnit_ati, ICommunicateInPrototype_ati_dsa
{

	public CyanMetaobjectAddMethodTo() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
		commentBeforeMethod = null;
	}


	@Override
	public ArrayList<Tuple3<String, String, StringBuffer>> ati_methodCodeList(ICompiler_ati compiler) {
		ArrayList<Object> parameterList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList();
		if ( parameterList.size() != 3 ) {
			compiler.error(metaobjectAnnotation.getFirstSymbol(), "This metaobject annotation should have exactly three string parameters", 
					metaobjectAnnotation.getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error);
		}
		/*
		 * check if the parameters are strings.
		 */
		for ( Object obj : parameterList ) 
			if ( ! (obj instanceof String) ) {
				compiler.error(metaobjectAnnotation.getFirstSymbol(), "The parameters to this metaobject annotation should all be strings", 
						metaobjectAnnotation.getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error);
			}
		String prototypeName = (String ) parameterList.get(0);
		prototypeName = NameServer.removeQuotes(prototypeName);
		String methodName = (String ) parameterList.get(1);
		methodName = NameServer.removeQuotes(methodName);
		String code =  (String) parameterList.get(2);
		code = NameServer.removeQuotes(code);
		
	
		ArrayList<Tuple3<String, String, StringBuffer>> tupleList = new ArrayList<>();
		tupleList.add( new Tuple3<String, String, StringBuffer>(prototypeName,
				methodName, new StringBuffer( commentBeforeMethod +code )));
		return tupleList;
	}
	
	
	@Override
	public String getName() {
		return "addMethodTo";
	}



	@Override
	public
	Object ati_dsa_shareInfoPrototype() {
		return "addMethodTo of line " + metaobjectAnnotation.getMetaobjectAnnotationNumber();
	}	

	@Override
	public
	void ati_dsa_receiveInfoPrototype(HashSet<Tuple4<String, Integer, Integer, Object>> moInfoSet) {
		commentBeforeMethod = "/*\n";
		for ( Tuple4<String, Integer, Integer, Object> t : moInfoSet ) {
			if ( t.f4 instanceof String ) 
				commentBeforeMethod += "{ name:'" + t.f1 + "', #proto = '" + t.f2 + "', #kind = '" + t.f3 + "', info = '" + t.f4 + "'" + "} \n";
		}
		commentBeforeMethod += "*/\n";
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.PROGRAM_DEC, DeclarationKind.PROTOTYPE_DEC };
	
	
	private String commentBeforeMethod;
}
