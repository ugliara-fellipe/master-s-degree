package meta.cyanLang;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.Expr;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureGrammar;
import ast.MethodSignatureWithSelectors;
import ast.ObjectDec;
import ast.SelectorLexer;
import ast.Type;
import lexer.Token;
import meta.Compiler_dpa;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckDeclaration_ati2;
import meta.ICheckDeclaration_ati3;
import meta.ICompileTimeDoesNotUnderstand_dsa;
import meta.ICompiler_ati;
import meta.ICompiler_dpa;
import meta.IParseWithCyanCompiler_dpa;
import saci.Compiler;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

public class CyanMetaobjectGrammarMethod extends CyanMetaobjectWithAt 
       implements IParseWithCyanCompiler_dpa, ICheckDeclaration_ati2, ICheckDeclaration_ati3, 
       ICompileTimeDoesNotUnderstand_dsa {

	
	public CyanMetaobjectGrammarMethod() { 
			super(MetaobjectArgumentKind.ZeroOrMoreParameters);
	}

	@Override
	public boolean shouldTakeText() { return true; }
	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };

	@Override
	public String getName() {
		return "grammarMethod";
	}

	/*
	public String getPackageOfType() { 
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Declaration dec = annotation.getDeclaration();
		MethodDec method = (MethodDec ) dec;
		if ( method == null ) {
			return NameServer.cyanLanguagePackageName;
		}
		MethodSignature mss = method.getMethodSignature();
		if ( mss.getReturnTypeExpr() == null ) 
			return NameServer.cyanLanguagePackageName;
		else {
			ProgramUnit pu = (ProgramUnit ) mss.getReturnTypeExpr().getType();
			return pu.getCompilationUnit().getPackageName();
		}
	}
	
	public String getPrototypeOfType() { 
		
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Declaration dec = annotation.getDeclaration();
		MethodDec method = (MethodDec ) dec;
		if ( method == null ) {
			return NameServer.cyanLanguagePackageName;
		}
		
		MethodSignature mss = method.getMethodSignature();
		if ( mss.getReturnTypeExpr() == null ) 
			return "Nil";
		else {
			ProgramUnit pu = (ProgramUnit ) mss.getReturnTypeExpr().getType(); 
			return pu.getName();
		}
	}
	*/
	
	
	@Override
	public void dpa_parse(ICompiler_dpa icompiler_dpa) {
		
		Compiler_dpa compiler_dpa = (Compiler_dpa ) icompiler_dpa;
		saci.Compiler compiler = compiler_dpa.getCompiler();
		
		compiler.next();
		MethodSignatureGrammar msg = compiler.methodSignatureGrammarForMetaobject();

		if ( compiler_dpa.getSymbol().token != Token.EOLO ) {
			compiler_dpa.error(compiler_dpa.getSymbol(), "Unexpected symbol: '" + compiler_dpa.getSymbol().getSymbolString() + "'");
		}
		
		

		/*
		 * the type that the grammar method should have is in the string 
		 *     msg.getSelectorGrammar().getStringType().
		 * Method Compiler.parseSingleTypeFromString converts that to an object of the AST. 
		 * This object is added to the a list of statements and expressions of the compiler_dpa
		 * by statement
		 *      		compiler_dpa.addExprStat(type);
		 * During semantic analysis, the compiler will find the type for this expression and it will
		 * create all instantiations of generic prototypes that  it needs.
  
		 */
		Expr type = Compiler.parseSingleTypeFromString(msg.getSelectorGrammar().getStringType(), 
				this.metaobjectAnnotation.getFirstSymbol(), "Internal error: ",  
				compiler.getCurrentProgramUnit().getCompilationUnit(), compiler.getCurrentProgramUnit());
		
		compiler_dpa.addExprStat(type);

		Expr astRootTypeExpr = null;
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		if ( annotation.getJavaParameterList() != null && annotation.getJavaParameterList().size() == 1 ) {
			if ( annotation.getJavaParameterList().get(0) instanceof String ) {
				// there is a prototype name as parameter
				String prototypeName = NameServer.removeQuotes( (String ) annotation.getJavaParameterList().get(0) );
				astRootTypeExpr = Compiler.parseSingleTypeFromString(prototypeName, 
						this.metaobjectAnnotation.getFirstSymbol(), "'" + prototypeName + "' is not a valid prototype name",  
						compiler.getCurrentProgramUnit().getCompilationUnit(), compiler.getCurrentProgramUnit());
				
				compiler_dpa.addExprStat(astRootTypeExpr);				
			}
			else {
				this.addError("Wrong argument type for this metaobject annotation. It may take a prototype name as parameter as in '"
					+ this.getName() + "(Graph)' or in '" + this.getName() + "(myutil.Graph)'"	);
			}
		}
		
		
		
		this.getMetaobjectAnnotation().setInfo_dpa( new Tuple2<Expr, MethodSignatureGrammar>(astRootTypeExpr, msg) );
		
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void ati2_checkDeclaration(ICompiler_ati compiler_ati) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Declaration dec = annotation.getDeclaration();
		if ( ! (dec instanceof MethodDec) ) {
			this.addError("This metaobject should be attached to a method");
		}
		else {
			MethodDec method = (MethodDec ) dec;
			MethodSignature mss = method.getMethodSignature();
			if ( !(mss instanceof ast.MethodSignatureWithSelectors) ) {
				compiler_ati.error(method.getFirstSymbol(), "Metaobject '" + this.getName() + "' is attached to this method. Then it "
						+ "should be a method with one selector that is not an operator. Like 'add: Array<Int>'");
			}
			MethodSignatureWithSelectors msws = (MethodSignatureWithSelectors ) mss;
			if ( msws.getNameWithoutParamNumber().equals("init:") ) {
				compiler_ati.error(method.getFirstSymbol(), "Metaobject '" + this.getName() + "' is attached to an 'init:' method. This is illegal");
			}
			
			Expr astRootTypeExpr = ((Tuple2<Expr, MethodSignatureGrammar> ) annotation.getInfo_dpa()).f1;
			Type astRootType = null;
			if ( astRootTypeExpr != null ) {
				astRootType = astRootTypeExpr.getType( (Env ) compiler_ati.getEnv());
				if ( !(astRootType instanceof ObjectDec) ) {
					compiler_ati.error(annotation.getFirstSymbol(), "The parameter to this metaobject annotation should be a prototype. It cannot be Dyn or an interface");
				}
			}
			
			MethodSignatureGrammar msg = ((Tuple2<Expr, MethodSignatureGrammar> ) this.getMetaobjectAnnotation().getInfo_dpa()).f2;
			if ( astRootType != null ) {
				msg.setAstRootType( (ObjectDec ) astRootType, (Env ) compiler_ati.getEnv(), 
						annotation.getRealParameterList().get(0).getFirstSymbol() );
			}
			/*
			if ( method.getMethodSignature().getParameterList() == null 
					|| method.getMethodSignature().getParameterList().size() == 0 || 
					method.getMethodSignature().getParameterList().get(0).getType() == null ) {
				compiler_ati.error(method.getMethodSignature().getFirstSymbol(), 
						"This method should have one parameter of type '" + ms.getParameterDec().getType().getFullName() );
			}
			if ( ms.getParameterDec().getType() != method.getMethodSignature().getParameterList().get(0).getType() ) {
				compiler_ati.error(method.getMethodSignature().getParameterList().get(0).getFirstSymbol(), 
						"The parameter of this method should have type '" + ms.getParameterDec().getType().getFullName() );
			}
			*/
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void ati3_checkDeclaration(ICompiler_ati compiler_ati) {
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Declaration dec = annotation.getDeclaration();
		MethodDec method = (MethodDec ) dec;
		MethodSignature mss = method.getMethodSignature();
		MethodSignatureWithSelectors msNonGrammar = (MethodSignatureWithSelectors ) mss;
		MethodSignatureGrammar msg = ((Tuple2<Expr, MethodSignatureGrammar> ) this.getMetaobjectAnnotation().getInfo_dpa()).f2;
		if ( msNonGrammar.getSelectorArray() == null || msNonGrammar.getSelectorArray().size() != 1 ||
			 msNonGrammar.getSelectorArray().get(0).getParameterList() == null ||
					 msNonGrammar.getSelectorArray().get(0).getParameterList().size() != 1 ) {
			compiler_ati.error(method.getFirstSymbol(), "Metaobject '" + this.getName() + "' is attached to this method. Then it "
					+ "should be a method with one selector that is not an operator. Like 'add: Array<Int>'");
		}
		Type type = Compiler.singleTypeFromString(msg.getSelectorGrammar().getStringType(), 
				msNonGrammar.getFirstSymbol(), "Internal error: ", compiler_ati.getEnv().getCurrentCompilationUnit(), 
				compiler_ati.getEnv().getCurrentProgramUnit(), (Env ) compiler_ati.getEnv());

		
		Type methodParameterType = msNonGrammar.getSelectorArray().get(0).getParameterList().get(0).getType();
		if ( type != methodParameterType ) {
			compiler_ati.error(method.getFirstSymbol(),  "The parameter of this method has type '" +
					methodParameterType.getFullName() + "' but according to metaobject '" + this.getName()+ "' attached to it "	
					+ "this method should have type '" + type.getFullName() + "'");
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Tuple2<StringBuffer, Type> dsa_analyzeReplaceMessageWithSelectors(Expr receiver, MessageWithSelectors message, Env env) {


		SelectorLexer lexer = new SelectorLexer( message.getSelectorParameterList() );

		MethodSignatureGrammar gmSignature = ((Tuple2<Expr, MethodSignatureGrammar> ) this.getMetaobjectAnnotation().getInfo_dpa()).f2;
		
		CyanMetaobjectWithAtAnnotation annotation = (CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		Declaration dec = annotation.getDeclaration();
		MethodDec method = (MethodDec ) dec;
		
		
		Tuple2<String, String> t = gmSignature.getSelectorGrammar().parse(lexer, env);
		
		
		if ( t != null && lexer.current() == null ) {
			return new Tuple2<StringBuffer, Type>(new StringBuffer ( ( receiver != null ? receiver.asString() : "" ) + " " + 
					method.getNameWithoutParamNumber() + " " + t.f1 ), method.getMethodSignature().getReturnType(env));
			
		}
		else {
			return null;
		}
	}
	
}
