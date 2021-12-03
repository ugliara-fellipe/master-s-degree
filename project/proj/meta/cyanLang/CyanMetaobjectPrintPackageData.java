package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.CyanPackage;
import ast.MetaobjectArgumentKind;
import error.FileError;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;
import saci.DirectoryPackage;
import saci.NameServer;
import saci.Tuple5;

public class CyanMetaobjectPrintPackageData extends CyanMetaobjectWithAt implements IAction_dpa {

	public CyanMetaobjectPrintPackageData() {
		super(MetaobjectArgumentKind.TwoParameters);
	}
	
	@Override
	public ArrayList<CyanMetaobjectError> check() {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		ArrayList<Object> paramList = annotation.getJavaParameterList();
		if ( !(paramList.get(0) instanceof String) || !(paramList.get(1) instanceof String)) {
			return this.addError("A string or identifier was expected as the parameter to this metaobject");
		}
		return null;
	}
		
	
	@Override
	public String getName() {
		return "packageDataAsString";
	}



	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
		ArrayList<Object> paramList = this.getMetaobjectAnnotation().getJavaParameterList();
		String packageName1 = NameServer.removeQuotes((String ) paramList.get(0));
		String fileName1 = NameServer.removeQuotes((String ) paramList.get(1));
		
		Tuple5<FileError, char[], String, String, CyanPackage> t = compiler.readTextFileFromPackage(
				fileName1, packageName1, DirectoryPackage.DATA, 0, null );		
		
		/*Tuple2<FileError,char []> t = compiler.loadTextDataFileFromPackage(fileName1, packageName1);  
		 * 
		 */
		
		if ( t.f1 != FileError.ok_e || t.f2 == null ) {
			compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Cannot read data file '" + fileName1 + "' of package '" + packageName1 + "'");
			return null;
		}
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("\"\"\"");
			sb.append(t.f2);
			sb.append("\"\"\"");

			return sb;
		}
		
	}	
}
