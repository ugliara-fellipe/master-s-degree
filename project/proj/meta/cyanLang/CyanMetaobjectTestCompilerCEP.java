package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IInformCompilationError;
import saci.Tuple3;

/**
 * This is just like metaobject 'ce' but the line number of the error is the current line plus the
 * number that is parameter. That is, in <br>
 * {@literal @}cep(3, "syntax error") <br>
 * the line with the error is 10 if this metaobject is in line 7.
   @author jose
 */
public class CyanMetaobjectTestCompilerCEP extends CyanMetaobjectWithAt implements IInformCompilationError {

	public CyanMetaobjectTestCompilerCEP() {
		super( MetaobjectArgumentKind.TwoParameters);
		
	}
	
	@Override
	public String getName() {
		return "cep";
	}

	@Override
	public boolean shouldTakeText() { return false; }

	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		
		/** 
		 * the compiler should point an error at line number lineNumber 
		 */
		int lineNumber;
		/**
		 * the id of the message is <code>id</code>
		 */
		String id;
		/**
		 * the message that the compiler should issue (or similar to this)
		 */
		String errorMessage;
		id = null;
		errorMessage = null;
		if ( !(paramList.get(0) instanceof Integer) || !(paramList.get(1) instanceof String)  ) {
			return addError("The first parameter to this metaobject should be an Int and the second parameter should be an identifier or a literal string");
		}
		lineNumber = (Integer ) paramList.get(0) + this.metaobjectAnnotation.getFirstSymbol().getLineNumber();
		String third = (String ) paramList.get(1); 
		if ( third.contains(" ") )
			errorMessage = third;
		else
			id = third;

		this.metaobjectAnnotation.setInfo_dpa(new Tuple3<Integer, String, String>(lineNumber, id, errorMessage));
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int getLineNumber() {
		return ((Tuple3<Integer, String, String>) this.metaobjectAnnotation.getInfo_dpa()).f1;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getId() {
		return ((Tuple3<Integer, String, String> ) this.metaobjectAnnotation.getInfo_dpa()).f2;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getErrorMessage() {
		return ((Tuple3<Integer, String, String> ) this.metaobjectAnnotation.getInfo_dpa()).f3;
	}
	
}

