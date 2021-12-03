package meta.treplica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Triple;

import ast.ASTVisitor;
import ast.CompilationUnit;
import ast.CyanPackage;
import ast.Declaration;
import ast.ExprIdentStar;
import ast.ExprMessageSend;
import ast.ExprMessageSendUnaryChainToExpr;
import ast.ExprMessageSendUnaryChainToSuper;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.ExprMessageSendWithSelectorsToSuper;
import ast.IdentStarKind;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.ObjectDec;
import ast.ParameterDec;
import ast.Program;
import ast.ProgramUnit;
import error.FileError;
import javafx.util.Pair;
import lexer.Lexer;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.IAction_dsa;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple3;

public class CyanMetaobjectTreplicaAction extends CyanMetaobjectWithAt implements IActionProgramUnit_ati, IAction_dsa {// ,
																														// ICheckProgramUnit_dsa2
																														// {
	ICompiler_dsa compiler;
	Vector<Pair<Triple<String, String, String>, Triple<String, String, String>>> methodsND;
	Program program;
	String fixName;
	Vector<MethodDec> visited;
	HashMap<String, Set<ProgramUnit>> mapProtoSubProto;

	Vector<String> loadedPack;

	public CyanMetaobjectTreplicaAction() {
		super(MetaobjectArgumentKind.ZeroParameter);
		visited = new Vector<MethodDec>();
		methodsND = new Vector<Pair<Triple<String, String, String>, Triple<String, String, String>>>();
		loadedPack = new Vector<String>();
	}

  public static String substringBeforeLast(String str, String separator) {
    int pos = str.lastIndexOf(separator);
    if (pos == -1) {
      return str;
    }
    return str.substring(0, pos);
  }
    
	public void actionNonDeterminism(ExprMessageSend node, MethodDec method, String pack, String func) {
		loadTreplicaNd(method.getDeclaringObject().getCompilationUnit().getPackageName());
		func = func.replaceAll(":", "");
		String parameters = "";
		if (node != null) {
			String [] splitParam = node.asString().split(":", 2);
			if(splitParam.length == 2) {
				parameters = ":" + splitParam[1];
			}
		}
		//System.out.println("parameters: " + parameters);
		//System.out.println("test pack: " + pack + " func: " + func);
		Pair<String, String> left = new Pair<String, String>(pack, func);
		Triple<String, String, String> right = null;
		for (Pair<Triple<String, String, String>, Triple<String, String, String>> pairFunc : methodsND) {
			if(!pairFunc.getKey().getLeft().equals(substringBeforeLast(method.getDeclaringObject().getFullName(), ".")) ) {
				break;
			}
			//System.out.println("loop: pairFunc: " + pairFunc.getKey().getKey() + " " + pairFunc.getKey().getValue());
			if (pairFunc.getKey().getMiddle().equals(left.getKey())
					&& pairFunc.getKey().getRight().equals(left.getValue())) {
				right = pairFunc.getValue();
				break;
			}
		}
		if (right != null) {

			//System.out.println("replace: " + right.getKey() + " " + right.getValue() + parameters);
			//System.out.println("![ " + right.getValue().trim() + " ]!");
			//System.out.println("![ " + parameters + " ]!");
			StringBuffer codeToAdd = new StringBuffer(right.getLeft() + "." + right.getMiddle() + " " + right.getRight().trim() + parameters );
			//System.out.println("![ " + codeToAdd + " ]!");
			if (node != null) {
				compiler.removeAddCodeExprMessageSend(node, this.getMetaobjectAnnotation(), codeToAdd, node.getType());
				//System.out.println("replace end");
			}
		}
	}

	public boolean visitedContain(MethodDec item) {
		String packItem = item.getDeclaringObject().getCompilationUnit().getNamePublicPrototype();
		String funcItem = item.getNameWithoutParamNumber();

		for (MethodDec method : visited) {
			String packTemp = method.getDeclaringObject().getCompilationUnit().getNamePublicPrototype();
			String funcTemp = method.getNameWithoutParamNumber();

			if (packTemp.equals(packItem) && funcTemp.equals(funcItem)) {
				return true;
			}
		}
		return false;
	}

	public void visitorWalk(ExprMessageSend node, String pack, String func) {
		Pair<String, String> methodPair = new Pair<String, String>(pack, func);
		for (MethodDec method : findMethod(methodPair)) {
			if (!visitedContain(method)) {
				actionNonDeterminism(node, method, pack, func);
				visitorFunctionsTree(method);
			}
		}
	}

	public void visitorFunctionsTree(MethodDec method) {
		visited.addElement(method);
		method.accept(new ASTVisitor() {
			@Override
			public void visit(ExprMessageSendWithSelectorsToExpr node) {
				if(node.getReceiverExpr() != null){
					if(node.getReceiverExpr().getType() != null){
				String pack = node.getReceiverExpr().getType().getName();
				String func = node.getMessage().getMethodName();
				visitorWalk(node, pack, func);
					}
				}
			}

			@Override
			public void visit(ExprIdentStar node) {
				if (node.getIdentStarKind() == IdentStarKind.unaryMethod_t) {
					String pack = ((CompilationUnit) node.getFirstSymbol().getCompilationUnit())
							.getNamePublicPrototype();
					String func = node.getName();
					visitorWalk(null, pack, func);
				}
			}

			@Override
			public void visit(ExprMessageSendUnaryChainToExpr node) {
				if( node.getReceiver().getType() != null){
				String pack = node.getReceiver().getType().getName();
				String func = node.getMessageName();
				visitorWalk(node, pack, func);
				}
			}

			@Override
			public void visit(ExprMessageSendWithSelectorsToSuper node) {
				String pack = node.getSuperobject().getType().getName();
				String func = node.getMessage().getMethodName();
				visitorWalk(node, pack, func);
			}

			@Override
			public void visit(ExprMessageSendUnaryChainToSuper node) {
				String pack = node.getReceiver().getType().getName();
				String func = node.getMessageName();
				visitorWalk(node, pack, func);
			}
		});
	}

	public ArrayList<MethodDec> findMethod(Pair<String, String> name) {
		ArrayList<MethodDec> list = new ArrayList<MethodDec>();
		for (CyanPackage pack : program.getPackageList()) {
			if (!pack.getPackageName().equals("cyan.lang")) {
				for (CompilationUnit compilerUnit : pack.getCompilationUnitList()) {
					for (ProgramUnit programUnit : compilerUnit.getProgramUnitList()) {
						Set<ProgramUnit> subProtoList = this.mapProtoSubProto
								.get(pack.getPackageName() + " " + programUnit.getName());
						for (ProgramUnit subProt : subProtoList) {
							ObjectDec dec = (ObjectDec) subProt;
							for (MethodDec method : dec.getMethodDecList()) {
								if (method.getNameWithoutParamNumber().equals(name.getValue())) {
									list.add(method);
								}
							}
						}

						ObjectDec dec = (ObjectDec) programUnit;
						for (MethodDec method : dec.getMethodDecList()) {
							if (method.getNameWithoutParamNumber().equals(name.getValue())) {
								list.add(method);
							}
						}
					}
				}
			}
		}
		return list;
	}

	@Override
	public String getName() {
		return "treplicaAction";
	}

	@Override
	public DeclarationKind[] mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind[] decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };

	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(ICompiler_ati compiler) {
		// Method Name
		MethodDec md = (MethodDec) this.getAttachedDeclaration();
		String methodName = md.getNameWithoutParamNumber();

		methodName = methodName.replaceAll(":", "");
		
		String funcPar = "";
		String hasFunPar = " ";
		String callPar = "";
		String hasCallPar = "";
		String funcRet = "";
		String hasCallRet = "";
		String retStatement = "";
		for (ParameterDec par : md.getMethodSignature().getParameterList()) {
			funcPar += " " + par.getType().getName() + " " + par.getName() + ",";
			callPar += " " + par.getName() + ",";
		}

		if (md.getMethodSignature().getReturnTypeExpr() != null) {
			hasCallRet = " -> ";
			funcRet = md.getMethodSignature().getReturnTypeExpr().getType().getName();
			retStatement = "@javacode<<<\n" + "return ("
					+ md.getMethodSignature().getReturnTypeExpr().getType().getJavaName() + ")_ret;\n" + ">>>\n";
		}

		if (funcPar.length() > 0) {
			funcPar = funcPar.substring(0, funcPar.length() - 1);
			hasFunPar = ": ";
		}
		if (callPar.length() > 0) {
			callPar = callPar.substring(0, callPar.length() - 1);
			hasCallPar = ": ";
		}

		// Object Name
		String objectName = compiler.getCurrentPrototypeName();
		String prototypeName = NameServer.removeQuotes(objectName + methodName);

		StringBuffer code = new StringBuffer("\n\n" + "func " + methodName + hasFunPar + funcPar + hasCallRet + funcRet
				+ " {\n" + "var action = " + prototypeName + " new" + hasCallPar + callPar + ";\n"
				+ "var Dyn ret = getTreplica execute: action;\n" + retStatement + "}\n");
		ArrayList<Tuple2<String, StringBuffer>> result = new ArrayList<>();
		result.add(new Tuple2<String, StringBuffer>(objectName, code));
		return result;
	}

	public ArrayList<Tuple3<String, String, String[]>> ati_renameMethod(ICompiler_ati compiler_ati) {
		String prototypeName = compiler_ati.getCurrentPrototypeName();
		MethodDec md = (MethodDec) this.getAttachedDeclaration();
		String oldMethodName = md.getName();

		String newMethodNameWithPar[] = new String[1 + md.getMethodSignature().getParameterList().size()];
		String auxName = md.getNameWithoutParamNumber();
		auxName = auxName.replaceAll(":", "");
		newMethodNameWithPar[0] = auxName + "TreplicaAction" + this.fixName;
		
		int indexArrayName = 1;
		for (ParameterDec par : md.getMethodSignature().getParameterList()) {
			newMethodNameWithPar[indexArrayName] = par.getName();
			indexArrayName++;
		}
  
		ArrayList<Tuple3<String, String, String[]>> tupleList = new ArrayList<>();
		tupleList.add(new Tuple3<String, String, String[]>(prototypeName, oldMethodName, newMethodNameWithPar));
		return tupleList;
	}

	public ArrayList<Tuple2<String, StringBuffer>> ati_NewPrototypeList(ICompiler_ati compiler_ati) {
		// Method Name
		MethodDec md = (MethodDec) this.getAttachedDeclaration();
		String methodName = md.getNameWithoutParamNumber();
		methodName = methodName.replaceAll(":", "");
		
		String varDef = "";
		String funcPar = "";
		String hasFuncPar = " ";
		String funcAssign = "";
		String callPar = "";
		String hasCallPar = "";
		String retStatement = "";
		String retNullState = "";
		for (ParameterDec par : md.getMethodSignature().getParameterList()) {
			varDef += "var " + par.getType().getName() + " " + par.getName() + "Var\n";
			funcPar += " " + par.getType().getName() + " " + par.getName() + ",";
			funcAssign += par.getName() + "Var = " + par.getName() + ";\n";
			callPar += " " + par.getName() + "Var,";
		}

		if (md.getMethodSignature().getReturnTypeExpr() != null) {
			retStatement = "return ";
		} else {
			retNullState = "return Nil;\n";
		}

		if (funcPar.length() > 0) {
			funcPar = funcPar.substring(0, funcPar.length() - 1);
			hasFuncPar = ": ";
		}
		if (callPar.length() > 0) {
			callPar = callPar.substring(0, callPar.length() - 1);
			hasCallPar = ": ";
		}

		// Object Name
		String objectName = compiler_ati.getCurrentPrototypeName();

		ArrayList<Tuple2<String, StringBuffer>> protoCodeList = new ArrayList<>();
		String prototypeName = NameServer.removeQuotes(objectName + methodName);
    
    this.fixName = NameServer.nextLocalVariableName();
		StringBuffer code = new StringBuffer(

				"package main\n" + "\n" + "import treplica\n" + "\n" + "object " + prototypeName + " extends Action\n"
						+ "\n" + varDef + "\n" + "func init" + hasFuncPar + funcPar + " {\n" + funcAssign + "}\n" + "\n"
						+ "override\n" + "func executeOn: Context context -> Dyn {\n" + "var obj = Cast<" + objectName
						+ "> asReceiver: context;\n" + retStatement + "obj " + methodName + "TreplicaAction" + this.fixName 
            + hasCallPar + callPar + ";\n" + retNullState + "}\n" + "end\n"

		);
		String unescape = Lexer.unescapeJavaString(code.toString());
		protoCodeList.add(new Tuple2<String, StringBuffer>(prototypeName, new StringBuffer(unescape)));
		return protoCodeList;
	}

	private void loadTreplicaNd(String packName) {
		//System.out.println("load init: " + packName);
		if (!loadedPack.contains(packName)) {
			//System.out.println("load set: " + packName);
			loadedPack.add(packName);
			Tuple2<FileError, char[]> fileList = this.compiler.getEnv().getProject().getCompilerManager()
					.readTextDataFileFromPackage("deterministic", packName);
			if (fileList.f2 != null) {
				String rawText = new String(fileList.f2);
				String lines[] = rawText.split("\n");
				for (String line : lines) {
					String parts[] = line.split("-");
					String left[] = parts[0].split(",");
					String right[] = parts[1].split(",");
					//System.out.println(left[0] + " : " + left[1]);
					//System.out.println(right[0] + " : " + right[1]);
					Triple<String, String, String> pLeft = Triple.of(left[0], left[1], left[2]);
					Triple<String, String, String> pRight = Triple.of(right[0], right[1], right[2]);
					methodsND.add(0, new Pair<Triple<String, String, String>, Triple<String, String, String>>(pLeft, pRight));
				}
			}
		}
	}
	
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		this.compiler = compiler_dsa;
		this.loadTreplicaNd("treplica");
		program = compiler.getEnv().getProject().getProgram();
		Declaration dec = this.getMetaobjectAnnotation().getDeclaration();
		if (!(dec instanceof MethodDec)) {
			this.addError("Internal error: metaobject '" + getName() + "' should be attached to a method");
			return null;
		}
		mapProtoSubProto = compiler.getMapPrototypeSubtypeList();
		MethodDec method = (MethodDec) dec;
		visitorFunctionsTree(method);
		return null;
	}
}
