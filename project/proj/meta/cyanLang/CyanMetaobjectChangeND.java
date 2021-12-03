package meta.cyanLang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ast.ASTVisitor;
import ast.CompilationUnit;
import ast.CompilationUnitSuper;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.Expr;
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
import ast.SelectorWithRealParameters;
import lexer.Symbol;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IAction_dsa;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompiler_dsa;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

public class CyanMetaobjectChangeND extends CyanMetaobjectWithAt implements IAction_dsa, ICheckProgramUnit_dsa2 {

	public CyanMetaobjectChangeND() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "changeND";
	}

	
	
	int numObjectsCreated = 0;
	ProgramUnit currentPrototype;
	HashMap<String, HashSet<Tuple2<String, String>>> nonDetermSet;

	HashMap<String, HashSet<String>> nonDeterministicSet;
	
	
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler) {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		Declaration dec = annotation.getDeclaration();
		if ( ! (dec instanceof MethodDec) ) {
			this.addError("Internal error: metaobject '" + getName() + "' should be attached to a method");
			return null;
		}
		MethodDec method = (MethodDec ) dec;
		Env env = compiler.getEnv();
		StringBuffer msg = new StringBuffer("");
		
		final Symbol moCallSymbol = this.metaobjectAnnotation.getFirstSymbol();
		final CompilationUnitSuper currentCompilationUnit = this.metaobjectAnnotation.getCompilationUnit();
		final ProgramUnit currentProgramUnit = this.metaobjectAnnotation.getProgramUnit();
		/*
		 *  nonDeterministicSet keeps a set of non-deterministic methods. The key is "packageName prototypeName" and
		 *  the value is a method signature. The types of the parameters are not considered. 
		 */
		nonDetermSet = new HashMap<>();
		HashSet<Tuple2<String, String>> signSet = new HashSet<>();
		/*
		 * the method aaa:1 bbb:2 of other.A should be replaced by a call to aa:1 bb:2 of the same
		 * prototype. This is what is made in this metaobject, which is just for fun.
		 *  
		 * 
		 * Ideally there should be created a DSL for specifying how to do the map. An example
		 * of the code of this DSL is below. 
		 * 		 
		 *       let Proto exprReceiver;
		 *       exprReceiver aaa: T1 p1 bbb: T2 p2, T3 p3 ===>
		 *           MyDeterministicPrototype receiver: exprReceiver aaa: p1 bbb: p2, p3
		 *
		 * This code says that, when the receiver of message 'aaa: T1 bbb: T2, T3' is an expression
		 * of prototype 'Proto', this message send should be replaced by
		 *          MyDeterministicPrototype receiver: exprReceiver aaa: p1 bbb: p2, p3
		 *          
		 * T1, T2 and T3 are the types of the real parameters of the original message.
		 * 
		 * What this metaobject does is given by the code
		 *        let other.A expr;
		 *        expr aaa: Int p1 bbb: Char p2, Char p3  ===>
		 *            self aa: p1 bb: p2, p3
		 *        let cyan.lang.System expr;
		 *        expr currentTime ===> self deterministicCurrentTime
		 *        let main.NDTest expr;
		 *        expr time ===> self deterministicTime
		 */
		signSet.add( new Tuple2<String, String>("aaa:1 bbb:2", "aa:bb:") );
		nonDetermSet.put("other A", signSet);
		signSet = new HashSet<>();
		signSet.add( new Tuple2<String, String>("currentTime", "deterministicCurrentTime")  );
		nonDetermSet.put("cyan.lang System", signSet);
		
		signSet = new HashSet<>();
		signSet.add( new Tuple2<String, String>("time", "deterministicTime")  );
		nonDetermSet.put("main NDTest", signSet);

		/*
		 * a map of all subtypes of each prototype. See the documentation of this method.
		 */
		
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
				String receiver; 
				if ( node.getReceiver() == null ) { 
					  // if the receiver is null, it is a message send to self without the word 'self'
					pu = currentPrototype;
					receiver = "self";
				}
				else {
					pu = (ProgramUnit ) node.getReceiver().getType();
					receiver = node.getReceiver().asString();
				}				
				String key = pu.getCompilationUnit().getPackageName() + " " + pu.getName();
				HashSet<Tuple2<String, String>> ss = nonDetermSet.get(key);
				String methodNameWPN = node.getMessageName();
				if ( ss != null  ) {
					for ( Tuple2<String, String> t : ss ) {
						if ( t.f1.equals(methodNameWPN) ) {
							StringBuffer codeToAdd = new StringBuffer(" { let " + compiler.nextLocalVariableName() + " = " + 
						         receiver + "; ^self } eval " + t.f2) ;
							compiler.removeAddCodeExprMessageSend(node, annotation, codeToAdd, node.getType());
							
							compiler.createNewGenericPrototype(moCallSymbol, currentCompilationUnit, currentProgramUnit, 
									NameServer.cyanLanguagePackageName + ".Function<" + 
							            ((CompilationUnit ) node.getFirstSymbol().getCompilationUnit()).getPublicPrototype().getName() + ">",
							            "Error caused by method dsa_codeToAddAtMetaobjectAnnotation of metaobject '" +
							            annotation.getCyanMetaobject().getName() + "'. "
							            );
							
							/*
							compiler.createNewGenericPrototype(moCallSymbol, currentCompilationUnit, currentProgramUnit, 
									NameServer.cyanLanguagePackageName + ".Function<DoNotExist",
						            "Error caused by method dsa_codeToAddAtMetaobjectAnnotation of metaobject '" +
						            annotation.getCyanMetaobject().getName() + "' of line " + moCallSymbol.getLineNumber() +
						            " of file '" + moCallSymbol.getCompilationUnit().getFullFileNamePath() + "'" +
						            ". "
									);
							*/
							
							
						}
					}
				}

				
			}
			
			   // maybe it will be necessary to implement this method
			@Override
			public void visit(ExprMessageSendUnaryChainToSuper node) { }
			
			@Override
			public void visit(ExprMessageSendWithSelectorsToExpr node) { 
				
				ProgramUnit pu;
				String receiver = null;
				if ( node.getReceiverExpr() == null ) { 
					    // in message sends to self without 'self', the receiver expression is null
					pu = currentPrototype;
				}
				else { 
					pu = (ProgramUnit ) node.getReceiverExpr().getType();
					receiver = node.getReceiverExpr().asString();
				}
				String key = pu.getCompilationUnit().getPackageName() + " " + pu.getName();
				HashSet<Tuple2<String, String>> ss = nonDetermSet.get(key);
				String methodNameWPN = node.getMessage().getMethodNameWithParamNumber();
				if ( ss != null  ) {
					for ( Tuple2<String, String> t : ss ) {
						if ( t.f1.equals(methodNameWPN) ) {
							/*
							 * para uma mensagem
							 *      rec s1: expr1, expr2  s2: expr3
							 * que deve ser substituída por
							 *      another s5: expr1, expr2  s6: expr3
							 *      
							 * deve-se gerar
							 *     { let p1__ = expr1; let p2__ = expr2; let p3__ = expr3; 
							 *        ^another s5: p1__, p2__ s6: p3__ } eval
							 */
							StringBuffer codeToAdd = new StringBuffer(" { ") ;
							if ( receiver != null ) {
								codeToAdd.append("let " + compiler.nextLocalVariableName() + " = " + receiver + "; ");
							}
							String[] selList = t.f2.split(":"); 
							if ( node.getMessage().getSelectorParameterList().size() != selList.length ) {
								compiler.error(node.getFirstSymbol(), "Metaobject 'changeND' is trying to replace a method call with another" +
							      " that has a different number of selectors");
							}
							ArrayList<String> localVarName = new ArrayList<>();
							for ( SelectorWithRealParameters sel : node.getMessage().getSelectorParameterList() ) {
								if ( sel.getExprList() != null ) {
									for ( Expr realParam : sel.getExprList() ) {
										String varName = compiler.nextLocalVariableName();
										localVarName.add(varName);
										codeToAdd.append("let " + varName + " = ");
										codeToAdd.append(realParam.asString() + "; ");
									}
								}
							}

							int i = 0;
							codeToAdd.append(" ^self");
							for ( SelectorWithRealParameters sel : node.getMessage().getSelectorParameterList() ) {
								codeToAdd.append(" " + selList[i] + ": ");
								int size = sel.getExprList().size();
								if ( sel.getExprList() != null ) {
									for ( int k = 0; k < sel.getExprList().size(); ++k ) {
										codeToAdd.append(localVarName.get(i));
										if ( --size > 0 ) 
											codeToAdd.append(", ");
										++i;
										
									}
								}
							}
							codeToAdd.append(" } eval ");
							compiler.removeAddCodeExprMessageSend(node, annotation, codeToAdd, node.getType());
							compiler.createNewGenericPrototype(moCallSymbol, currentCompilationUnit, currentProgramUnit, 
									NameServer.cyanLanguagePackageName + ".Function<" + node.getType().getFullName()  + ">", 
						            "Error caused by method dsa_codeToAddAtMetaobjectAnnotation of metaobject '" +
						            annotation.getCyanMetaobject().getName() + "' of line " + moCallSymbol.getLineNumber() +
						            " of file '" + moCallSymbol.getCompilationUnit().getFullFileNamePath() + "'" +
						            ". "
									);
						}
					}
				}
			}
			@Override
			public void visit(ExprMessageSendWithSelectorsToSuper node) { 
				
			}

			
		  } 
		  );
		return null;
	}

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
				    * If this is not a method that overrides a super-prototype method, this 
				    * list will always have ONE element. If the current method is overridden, the list may have
				    * several elements. And this list may have several elements in the case the method is overloaded
				    * (search the Cyan manual for keyword 'overload'). 
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
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };


	
	
}
