/*
 * Eduardo Romao da Rocha
 * */

package meta.tg;

import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import cyan_plugin.gui.CodegRegExprGUI;
import lexer.Lexer;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICodeg;
import meta.ICompiler_ded;
import meta.ICompiler_dsa;

public class CyanMetaobjectCodegRegExpr extends CyanMetaobjectWithAt 
implements ICodeg, IAction_dsa{
	public CyanMetaobjectCodegRegExpr(){
		super(MetaobjectArgumentKind.TwoParameters);
	}
	
	@Override
	public String getName() {
		return "re";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		ArrayList<Object> list = annotation.getJavaParameterList();
		String regEx = Lexer.escapeJavaString(list.get(1).toString());
		
		return new StringBuffer("RegExpr(" + "\"" +regEx + "\"" + ");");
		/*return new StringBuffer("RegExpr(" + "\""
	            + this.getMetaobjectAnnotation().getCodegInfo().toString() + "\""); */
	}

	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	
	@Override
	public String getPrototypeOfType() { return "RegExpr"; }

	
	@Override
	public byte []getUserInput(ICompiler_ded compiler_ded, byte[] previousUserInput) {
		/*
		 * inside a Codeg one can access the current prototype by calling
		 *    this.getMetaobjectAnnotation().getProgramUnit()
		 * 
		 * the list of local variables visible at the point of declaration is given by
		 *      this.getMetaobjectAnnotation().getLocalVariableNameList()
		 */
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		ArrayList<Object> javaParamList = annotation.getJavaParameterList();
		String regExFromEditor = (String) annotation.getJavaParameterList().get(1);
		regExFromEditor = Lexer.unescapeJavaString(regExFromEditor);
		
		ArrayList<String> list = new ArrayList<>();
		
		CodegRegExprGUI codegGUI = new CodegRegExprGUI(new Frame(), previousUserInput, regExFromEditor, list);
		byte[] userInput = null;
		//list = codegGUI.getStringsFromTests();
		if(javaParamList.size() == 0){
			//TODO => msg de erro !!
			return null;
		}
		String []strList = codegGUI.getStringsFromTests();
		ArrayList<String> sl = new ArrayList<>();
		
		//Converting a String[] ("strList") to byte[]
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		for(String s: strList){
			try{
				dos.writeUTF(s);
			}catch(IOException e){
				
			}
		}
		
		userInput = baos.toByteArray();
		try{
			baos.flush();
			baos.close();
			dos.flush();
			dos.close();
		}catch(IOException e){
			
		}
		
		return userInput;
		//return "".getBytes();
		/* return ("RegExpr(\"" + regExFromEditor 
				+ "\")").getBytes(); */
	}
	
		
	@Override
	public boolean isExpression() {
		return true;
	}
}

