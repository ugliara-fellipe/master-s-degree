package meta.designPattern;

import java.util.ArrayList;
import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICompiler_ati;
import meta.IInstanceVariableDec_ati;
import saci.Tuple2;

public class CyanMetaobjectObservable extends CyanMetaobjectWithAt implements IActionProgramUnit_ati  {

	public CyanMetaobjectObservable() {
		super(MetaobjectArgumentKind.ZeroParameter);

	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {     
		if ( this.getMetaobjectAnnotation().getExprStatList() != null && this.getMetaobjectAnnotation().getExprStatList().size() == 0 ) {
			return addError("This metaobject takes zero parameters");
		}
		return null;
	}

	@Override
	public String getName() {
		return "observable";
	}
	
	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_methodCodeListThisPrototype(
			ICompiler_ati compiler) {
		
		ArrayList<Tuple2<String, StringBuffer>> tupleList = new ArrayList<>();
		
		String code = "    Function<String, Nil> notify;\n";

		code     += "    func init: Function<String, Nil> f { \n";
		for ( IInstanceVariableDec_ati iv : compiler.getInstanceVariableList() ) 
			code += "        self." + iv.getName() + " = \"nil\";\n";
		code     += "        self.notify = f;\n";
		code     += "    }\n";

		for ( IInstanceVariableDec_ati iv : compiler.getInstanceVariableList() ) {

			String name = iv.getName().toLowerCase();
			name = Character.toString(name.charAt(0)).toUpperCase()+name.substring(1);

			code += "    func set" + name  + ": String s {\n";
			code += "        self." + iv.getName() + " = s;\n";
			code += "        notify eval: s;\n";
			code += "    }\n";
		}
		tupleList.add( new Tuple2<String, StringBuffer>("init:", new StringBuffer(code)));
		return tupleList;
	}

	/*
	@Override
	public StringBuffer ati_codeToAddAtMetaobjectAnnotation(
			ICompiler_ati compiler_ati) { 

		String code = "Function<String, Nil> notify;\n";

		code += "func init: Function<String, Nil> f { \n";
		for ( IInstanceVariableDec_ati iv : compiler_ati.getInstanceVariableList() ) 
			code += "self." + iv.getName() + " = \"nil\";\n";
		code += "self.notify = f;\n";
		code += "}\n";

		for ( IInstanceVariableDec_ati iv : compiler_ati.getInstanceVariableList() ) {

			String name = iv.getName().toLowerCase();
			name = Character.toString(name.charAt(0)).toUpperCase()+name.substring(1);

			code += "func set" + name  + ": String n {\n";
			code += "self." + iv.getName() + " = n;\n";
			code += "notify eval: \"update\";\n";
			code += "}\n";
		}
		return new StringBuffer(code);
	}
	*/
	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.PROTOTYPE_DEC };


	
}
