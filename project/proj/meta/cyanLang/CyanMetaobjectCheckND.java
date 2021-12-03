package meta.cyanLang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ast.ASTVisitor;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.ExprMessageSendUnaryChainToExpr;
import ast.ExprMessageSendUnaryChainToSuper;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.ExprMessageSendWithSelectorsToSuper;
import ast.ExprObjectCreation;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureWithSelectors;
import ast.ProgramUnit;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompiler_dsa;
import saci.Env;

public class CyanMetaobjectCheckND extends CyanMetaobjectWithAt implements ICheckProgramUnit_dsa2 {

	public CyanMetaobjectCheckND() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	int numObjectsCreated = 0;
	ProgramUnit currentPrototype;
	HashMap<String, HashSet<String>> nonDeterministicSet;
	
	@Override
	public void dsa2_checkProgramUnit(ICompiler_dsa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		Declaration dec = annotation.getDeclaration();
		if ( ! (dec instanceof MethodDec) ) {
			this.addError("Internal error: metaobject '" + getName() + "' should be attached to a method");
			return ;
		}
		MethodDec method = (MethodDec ) dec;
		Env env = compiler.getEnv();
		StringBuffer msg = new StringBuffer("");
		
		/*
		 *  nonDeterministicSet keeps a set of non-deterministic methods. The key is "packageName prototypeName" and
		 *  the value is a method signature. The types of the parameters are not considered. 
		 */
		nonDeterministicSet = new HashMap<>();
		HashSet<String> signSet = new HashSet<>();
		signSet.add("aaa:1 bbb:2");
		nonDeterministicSet.put("other A", signSet);
		signSet = new HashSet<>();
		signSet.add("currentTime");
		nonDeterministicSet.put("cyan.lang System", signSet);
		signSet = new HashSet<>();
		signSet.add("time");
		nonDeterministicSet.put("main NDTest", signSet);

		/*
		 * a map of all subtypes of each prototype. See the documentation of this method.
		 */
		
		HashMap<String, Set<ProgramUnit>> mapPrototypeSubtypeList = compiler.getMapPrototypeSubtypeList();
		
		currentPrototype = method.getDeclaringObject();
		
		/**
		 * visit the method that is annotated with @checkND
		 * 
		 */
		method.accept(  new ASTVisitor() {
			   /*
			    * visit the method signature. Unnecessary because we know that. It is
			    * method.getMethodSignature()
			    * 
			    * adds the method signature to msg(non-Javadoc)
			      @see ast.ASTVisitor#visit(ast.MethodSignatureWithSelectors)
			    */
			@Override
			public void visit(MethodSignatureWithSelectors node) {
				msg.append("signature: " + node.getFullName(env) + "\n");
			}
			
			/*
			 * visit all unary message sends to expressions. That encompasses all unary message sends but 
			 * those to 'super'
			   @see ast.ASTVisitor#visit(ast.ExprMessageSendUnaryChainToExpr)
			 */
			
			@Override
			public void visit(ExprMessageSendUnaryChainToExpr node) { 
				ProgramUnit pu;
				String receiver = "";
				if ( node.getReceiver() == null ) { 
					  // if the receiver is null, it is a message send to self without the word 'self'
					pu = currentPrototype;
					receiver = "self";
				}
				else {
					pu = (ProgramUnit ) node.getReceiver().getType();
					receiver = node.getReceiver().asString();
				}
				
				HashSet<String> ss = nonDeterministicSet.get(pu.getCompilationUnit().getPackageName() + " " + pu.getName());
				/*
				 * the name of the message is node.getMessageName()
				 */
				
				if ( ss != null && ss.contains(node.getMessageName()) ) {
					msg.append("found non-deterministic call: " + pu.getCompilationUnit().getPackageName() + "." + pu.getName()
					+ "::" + node.getMessageName()  + " (receiver: '" + receiver + "')\n");
				}
				
			}
			
			   // maybe it will be necessary to implement this method
			@Override
			public void visit(ExprMessageSendUnaryChainToSuper node) { }
			
			   // just count
			@Override
			public void visit(ExprObjectCreation node) { ++numObjectsCreated; }
			
			@Override
			public void visit(ExprMessageSendWithSelectorsToExpr node) { 
				
				/*
				 * just to show how to use. Completely unuseful. 
				 */
				msg.append("receiver: " + node.getReceiverExpr().asString() + "\n");
				msg.append("method name to be called: " + node.getMessage().getMethodNameWithParamNumber() + "\n");
				msg.append("Type first parameter: " + 
				node.getMessage().getSelectorParameterList()   // selector list with the real parameters
				    .get(0).getExprList()                      // real parameters of the first selector
				    .get(0).getType().getFullName()                          // type of the first parameter of the first selector
				   + "\n");
				ProgramUnit pu;
				if ( node.getReceiverExpr() == null ) 
					    // in message sends to self without 'self', the receiver expression is null
					pu = currentPrototype;
				else 
					pu = (ProgramUnit ) node.getReceiverExpr().getType();
				   /*
				    * always search for a method using node.getMessage().getMethodNameWithParamNumber().
				    * If the method has not been overridden in sub-prototypes,  this 
				    * list will always have ONE element. If the method is overridden, the list may have
				    * several elements. And this list has several elements in the case the method is overloaded
				    * (see the Cyan manual for keyword 'overload'). 
				    * 
				    * If it was necessary to check if the real parameters are subtypes of the formal parameters,
				    * you can use method<br>
				    * <code>
				    * ExprMessageSendWithSelectors::MethodSignature checkMessageSend(ArrayList<MethodSignature> methodSignatureList, Env env) 
				    * </code><br>
				    * 
				    */
				ArrayList<MethodSignature> msList = pu.searchMethodPublicSuperPublic(node.getMessage().getMethodNameWithParamNumber(), env);
				for ( MethodSignature ms : msList ) {
					String aux = "";
					if ( ms.getDeclaringInterface() != null ) {
						aux = ms.getDeclaringInterface().getFullName();
					}
					else if ( ms.getMethod() != null && ms.getMethod().getDeclaringObject() != null ) {
						aux = ms.getMethod().getDeclaringObject().getFullName();
					}
					msg.append("method signature, prototype of the method that can be called: " + ms.getFullName(env) + " " + aux + 
					  "\n");
				}
				String key = pu.getCompilationUnit().getPackageName() + " " + pu.getName();
				HashSet<String> ss = nonDeterministicSet.get(key);
				String methodNameWPN = node.getMessage().getMethodNameWithParamNumber();
				if ( ss != null && ss.contains(methodNameWPN) ) {
					msg.append("found non-deterministic call: " + pu.getCompilationUnit().getPackageName() + "." + pu.getName()
					+ "::" + node.getMessage().getMethodNameWithParamNumber() + "\n");
					Set<ProgramUnit> set = mapPrototypeSubtypeList.get(key);
					msg.append("Subtypes of '" + pu.getFullName() + "' : ");
					  // print only the direct subtypes
					for ( ProgramUnit sub : set ) {
						msg.append(sub.getFullName() + " ");
					}
					msg.append("\n");
				}
			}
			@Override
			public void visit(ExprMessageSendWithSelectorsToSuper node) { 
				
			}

			
		  } 
		  );
		System.out.println(msg + "\n" + "num objets created: " + numObjectsCreated);
	}

	@Override
	public String getName() {
		return "checkND";
	}

	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };

	
	
}
