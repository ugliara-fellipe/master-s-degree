package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectAnnotation;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICodeg;
import meta.ICompiler_ded;
import meta.ICompiler_dsa;
import saci.Tuple2;
import saci.Tuple4;

public class CyanMetaobjectCodegColorColor extends CyanMetaobjectWithAt 
             implements ICodeg, IAction_dsa {

	public CyanMetaobjectCodegColorColor() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}


	@Override
	public String getName() {
		return "colorColor";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		System.out.println( this.getMetaobjectAnnotation().getJavaParameterList().get(1));
		System.out.println( this.getMetaobjectAnnotation().getJavaParameterList().get(2));
		byte []info = this.getMetaobjectAnnotation().getCodegInfo();
		if ( info == null ) {
			return new StringBuffer("12");
		}
		else {
			StringBuffer s = new StringBuffer();
			for ( byte b : info ) {
				s.append( (char ) b);
			}
			return s;
		}
		/*StringBuffer s = new StringBuffer(info.toString());
		return s; */
	}

	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	
	@Override
	public String getPrototypeOfType() { return "Int"; }

	
	@Override
	public byte []getUserInput(ICompiler_ded compiler_ded, byte []previousUserInput) {
		/*
		 * inside a Codeg one can access the current prototype by calling
		 *    this.getMetaobjectAnnotation().getProgramUnit()
		 * 
		 * the list of local variables visible at the point of declaration is given by
		 *      this.getMetaobjectAnnotation().getLocalVariableNameList()
		 */
		System.out.println("Local variable list: ");
		for ( Tuple2<String, String> t : compiler_ded.getLocalVariableList() ) {
			System.out.println( "name = " + t.f1 + " type = " + (t.f2 == null ? "implicit" : t.f2) ); 
		}
		System.out.println("Instance variable list: ");
		for ( Tuple2<String, String> t : compiler_ded.getInstanceVariableList() ) {
			System.out.println( "name = " + t.f1 + " type = " + (t.f2 == null ? "implicit" : t.f2) ); 
		}
		
		return "32".getBytes();
	}

	@Override
	public ArrayList<Tuple4<Integer, Integer, Integer, Integer>>  getColorList(CyanMetaobjectAnnotation metaobjectAnnotation1) {
		String strColor = this.getMetaobjectAnnotation().getCodegInfo().toString();
		/*
		 * convert strColor to int color, put it in colorNumber
		 * 
		 */
		int colorNumber = 0;
		try {
			colorNumber = Integer.parseInt(strColor);
		}
		catch ( NumberFormatException e ) {
			return null;
		}
		int column = metaobjectAnnotation1.getFirstSymbol().getColumnNumber();
		int columnLeftPar = column + 1 + this.getName().length() + 1;
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation1;
		String red = (String ) annotation.getJavaParameterList().get(0);
		
		ArrayList<Tuple4<Integer, Integer, Integer, Integer>> array = new ArrayList<>();
		Tuple4<Integer, Integer, Integer, Integer> t = new Tuple4<>(colorNumber, 1, columnLeftPar, red.length());
		array.add(t);
		
		return array;
		
		
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}
	
		
	
}
