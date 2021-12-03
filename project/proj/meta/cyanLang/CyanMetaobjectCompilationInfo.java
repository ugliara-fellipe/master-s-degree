package meta.cyanLang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import lexer.Lexer;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectLiteralObject;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.IAction_dsa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import saci.Env;
import saci.NameServer;

/**
 * A metaobject annotation of this metaobject supports the parameters filename, prototypename, packagename, linenumber,
 *  localvariablelist, instancevariablelist, and signatureallmethodslist. 
 * 
   @author José
 */
public class CyanMetaobjectCompilationInfo extends CyanMetaobjectWithAt 
       implements IAction_dpa, IAction_dsa {

	private static HashMap<String, Function<CyanMetaobjectWithAtAnnotation, String>> moToFuncMap_dpa = new HashMap<>();
	private static HashMap<String, Function<Env, String>> moToFuncMap_dsa = new HashMap<>();
	private static HashMap<String, String> moToTypeMap = new HashMap<>();
	static {
		moToFuncMap_dpa.put("filename", (CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation) -> { 
			return cyanMetaobjectAnnotation.getCompilationUnit().getFullFileNamePath(); });
		
		moToFuncMap_dpa.put("prototypename", (CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation) -> { 
			return cyanMetaobjectAnnotation.getPrototypeOfAnnotation(); });

		moToFuncMap_dpa.put("packagename", (CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation) -> { 
    		return cyanMetaobjectAnnotation.getPackageOfAnnotation(); });
		moToFuncMap_dpa.put("linenumber", (CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation) -> { 
			return "" + cyanMetaobjectAnnotation.getSymbolMetaobjectAnnotation().getLineNumber(); });
		moToFuncMap_dpa.put("columnnumber", (CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation) -> { 
			return "" + cyanMetaobjectAnnotation.getSymbolMetaobjectAnnotation().getColumnNumber(); });
		
		
		moToFuncMap_dsa.put("localvariablelist", (Env env) -> { 
			return env.getStringVisibleLocalVariableList(); });

		moToFuncMap_dsa.put("instancevariablelist", (Env env) -> { 
			return env.getStringInstanceVariableList(); });

		moToFuncMap_dsa.put("signatureallmethodslist", (Env env) -> { 
			return env.getStringSignatureAllMethods(); });

		moToFuncMap_dsa.put("currentmethodname", (Env env) -> { 
			return env.getCurrentMethod() != null ? env.getCurrentMethod().getName() : "no current method" ; });
		
		moToFuncMap_dsa.put("currentmethodfullname", (Env env) -> { 
			return env.getCurrentMethod() != null ? env.getCurrentMethod().getMethodSignature().getFullName(env) : "no current method" ; });
		moToFuncMap_dsa.put("currentmethodreturntypename", (Env env) -> { 
			return env.getCurrentMethod() != null ? env.getCurrentMethod().getMethodSignature().getReturnType(env).getFullName(env) : "no current method" ; });

		
		moToTypeMap.put("filename", "String");
		moToTypeMap.put("prototypename", "String");
		moToTypeMap.put("packagename", "String");
		moToTypeMap.put("linenumber", "Int");
		moToTypeMap.put("columnnumber", "Int");
		moToTypeMap.put("localvariablelist", "Array<String>");
		moToTypeMap.put("instancevariablelist", "Array<String>");
		moToTypeMap.put("signatureallmethodslist", "Array<String>");
		moToTypeMap.put("currentmethodname", "String");
		moToTypeMap.put("currentmethodfullname", "String");
		moToTypeMap.put("currentmethodreturntypename", "String");
		
		
	}
	
	public CyanMetaobjectCompilationInfo() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
		ArrayList<Object> javaObjectList = cyanMetaobjectAnnotation.getJavaParameterList();
		if ( javaObjectList.size() != 1 || !(javaObjectList.get(0) instanceof String)) {
			return addError("A single identifier or a single string was expected as parameter to this metaobject");
		}
		String param = (String ) javaObjectList.get(0);
		param = NameServer.removeQuotes(param).toLowerCase();
		if ( moToFuncMap_dpa.get(param) == null && moToFuncMap_dsa.get(param) == null) {
			String supportedOptions = "";
			for (String s : moToFuncMap_dpa.keySet() ) 
				supportedOptions += "'" + s + "' ";
			return addError("Only the parameters " + supportedOptions + " are supported.");
		}
		return null;
	}

	@Override
	public String getName() {
		return "compilationInfo";
	}


	@Override
	public StringBuffer dpa_codeToAdd( ICompilerAction_dpa compiler ) {
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
		
		data = null;
		String param = (String ) cyanMetaobjectAnnotation.getJavaParameterList().get(0);
		param = NameServer.removeQuotes(param).toLowerCase();
		Function<CyanMetaobjectWithAtAnnotation, String> f = moToFuncMap_dpa.get(param);
		if ( f == null ) {
			return null;
		}
		data = f.apply(cyanMetaobjectAnnotation);
		if ( data == null ) 
			return null;
		else {
			String value = moToTypeMap.get(param);
			if ( value.equals("Int") ) {
				return new StringBuffer(data);
			}
			else {
				data = Lexer.escapeJavaString(data);
				return new StringBuffer("\"" + data + "\"");
			}
			
		}
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		if ( data != null ) {
			return null; 
		}
		else {
			CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
			
			String param = (String ) cyanMetaobjectAnnotation.getJavaParameterList().get(0);
			param = NameServer.removeQuotes(param).toLowerCase();
			Function<Env, String> f = moToFuncMap_dsa.get(param);
			if ( f == null ) 
				return null;
			data = f.apply(compiler_dsa.getEnv());
			if (  data != null ) {
				
				String value = moToTypeMap.get(param);
				if ( value.equals("Int") ) {
					return new StringBuffer(data);
				}
				else {
					if ( value.startsWith("Array<") ) {
						return new StringBuffer(data);
					}
					else {
						data = Lexer.escapeJavaString(data);
						return new StringBuffer("\"" + data + "\"");
					}
				}
			}
			else
				return null;
		}
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
		CyanMetaobjectWithAtAnnotation cyanMetaobjectAnnotation = this.getMetaobjectAnnotation();
		ArrayList<Object> jpList = cyanMetaobjectAnnotation.getJavaParameterList();
		
		if ( jpList == null || jpList.size() == 0 )
			return "Nil";
		
		String param = (String ) cyanMetaobjectAnnotation.getJavaParameterList().get(0);
		param = NameServer.removeQuotes(param).toLowerCase();

		String type = moToTypeMap.get(param);
		return type == null ? "Nil" : type;
	}

	@Override
	public boolean isExpression() {
		return true;
	}
	
	

	private String data;
}
