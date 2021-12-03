package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.ExprAnyLiteral;
import ast.MetaobjectArgumentKind;
import lexer.Lexer;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dsa;
import saci.NameServer;
import saci.Tuple2;

public class CyanMetaobjectPackageFeature extends CyanMetaobjectWithAt implements IAction_dsa {

	
	public CyanMetaobjectPackageFeature() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}
	
	@Override
	public String getName() {
		return "packageFeature";
	}
	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation;
		ArrayList<Object> jpList = annotation.getJavaParameterList();
		if ( !(jpList.get(0) instanceof String) ) {
			return addError("The first parameter to this metaobject " + getName() + " should be an identifier or a literal string");
		}
		if ( jpList.size() > 2 ) {
			return addError("This metaobject should take one or two string parameters");
		}
		if ( jpList.size() == 2 && 	!(jpList.get(1) instanceof String) ) {
			return addError("The second parameter to this metaobject " + getName() + " should be an identifier or a literal string");
		}

		return null;
	}
	
	
	

	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	@Override
	public String getPrototypeOfType() {
		return "Array<Tuple<key,String,value,Any>>";
	}

	@Override
	public boolean isExpression() {
		return true;
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation;
		ArrayList<Object> jpList = annotation.getJavaParameterList();
		String packageName1 = NameServer.removeQuotes((String ) jpList.get(0));
		CyanPackage pack = compiler_dsa.searchPackage(packageName1);
		
		StringBuffer s = new StringBuffer();
		if ( pack.getFeatureList() == null || pack.getFeatureList().size() == 0 ) {
			s.append("Array<Tuple<key, String, value, Any>>()");
		}
		else {
			if ( jpList.size() == 2 ) {
				String featureName = NameServer.removeQuotes((String ) jpList.get(1));
				ArrayList<String> arrayElemList = new ArrayList<>();
				for ( Tuple2<String, ExprAnyLiteral> t : pack.getFeatureList() ) {
					
					String strValue;
					//strValue = Lexer.valueToFeatureString(t);
					strValue = t.f2.metaobjectParameterAsString(
							() -> { compiler_dsa.error(t.f2.getFirstSymbol(), "This expression cannot be used in a parameter in a metaobject annotation"); }
							); 
					
					String key = NameServer.removeQuotes(t.f1);
					if ( key.equals(featureName) ) {
						// arrayElemList.add("[. key = \"" + key + "\", value = Any cast: " + strValue + " .]");
						arrayElemList.add("[. key = \"" + key + "\", value = Any toAny: " + strValue + " .]");
					}
				}
				if ( arrayElemList.size() == 0 ) {
					s.append("Array<Tuple<key, String, value, Any>>()");
				}
				else {
					s.append(" [ ");
					int size = arrayElemList.size();
					for ( String elem : arrayElemList ) {
						s.append(elem);
						if ( --size > 0 ) 
							s.append(", ");
					}
					s.append(" ] ");
				}
			}
			else {
				s.append(" [ ");
				int size = pack.getFeatureList().size();
				for ( Tuple2<String, ExprAnyLiteral> t : pack.getFeatureList() ) {
					
					String strValue;
					strValue = Lexer.valueToFeatureString(t);
					
					String key = NameServer.removeQuotes(t.f1);
					//s.append("[. key = \"" + key + "\", value = Any cast: " + strValue + " .]");
					s.append("[. key = \"" + key + "\", value = Any toAny: " + strValue + " .]");
					if ( --size > 0 ) 
						s.append(", ");
				}
				s.append(" ] ");
				
			}
			
		}
		
		
		return s;
	}
	
	
}
