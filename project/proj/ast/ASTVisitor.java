package ast;

@SuppressWarnings("unused")
public abstract class ASTVisitor {

	private void signalError(ASTNode node) {
		System.out.println("Class '" + node.getClass().getName() + "' is not recognized by class " + this.getClass().getName());
		throw new error.CompileErrorException();
	}
	
	public void visit(Program node) { }
	public void visit(CyanPackage node) { }
	public void visit(JVMPackage node) { }
	public void visit(CompilationUnit node) { }
	public void visit(ProgramUnit node) { 
		if ( node instanceof InterfaceDec ) {
			this.visit((InterfaceDec ) node);
		}
		else if ( node instanceof ObjectDec ) {
			this.visit((ObjectDec ) node);
		}
		else {
			signalError(node);
		}
	}
	public void visit(InterfaceDec node) { }
	public void visit(ObjectDec node) { }
	public void visit(MethodDec node) { }
	public void visit(MethodSignature node) {
		if ( node instanceof MethodSignatureWithSelectors ) {
			this.visit( (MethodSignatureWithSelectors ) node );
		}
		else if ( node instanceof MethodSignatureOperator ) {
			this.visit( (MethodSignatureOperator ) node );
		}
		else if ( node instanceof MethodSignatureUnary ) {
			this.visit( (MethodSignatureUnary ) node );
		}
		else {
			signalError(node);
		}
	}
	public void visit(MethodSignatureWithSelectors node) { }  
	public void visit(MethodSignatureOperator node) { }  
	public void visit(MethodSignatureUnary node) { }
	
	
	public void preVisit(Program node) { }
	public void preVisit(CyanPackage node) { }
	public void preVisit(JVMPackage node) { }
	public void preVisit(CompilationUnit node) { }
	public void preVisit(ProgramUnit node) { 
		if ( node instanceof InterfaceDec ) {
			this.preVisit((InterfaceDec ) node);
		}
		else if ( node instanceof ObjectDec ) {
			this.preVisit((ObjectDec ) node);
		}
		else {
			signalError(node);
		}
}
	
	public void preVisit(InterfaceDec node) { }
	public void preVisit(ObjectDec node) { }
	public void preVisit(MethodDec node) { }
	public void preVisit(MethodSignature node) {
		if ( node instanceof MethodSignatureWithSelectors ) {
			this.preVisit( (MethodSignatureWithSelectors ) node );
		}
		else if ( node instanceof MethodSignatureOperator ) {
			this.preVisit( (MethodSignatureOperator ) node );
		}
		else if ( node instanceof MethodSignatureUnary ) {
			this.preVisit( (MethodSignatureUnary ) node );
		}
		else {
			signalError(node);
		}
	}
	
	public void preVisit(MethodSignatureWithSelectors node) { }
	public void preVisit(MethodSignatureOperator node) { }
	public void preVisit(MethodSignatureUnary node) { }
	
	
	public void visit(SlotDec node) { 
		if ( node instanceof InstanceVariableDec ) {
			if ( node instanceof ContextParameter ) {
				this.visit( (ContextParameter ) node );
			}
			else {
				this.visit( (InstanceVariableDec ) node );
			}
		}
		else if ( node instanceof MethodDec ) {
			this.visit( (MethodDec ) node );
		}
		else {
			signalError(node);
		}
	}
	
	public void visit(ContextParameter node) { }
	public void visit(InstanceVariableDec node) { }
	public void visit(CyanMetaobjectAnnotation node) { 
		if ( node instanceof CyanMetaobjectLiteralObjectAnnotation ) {
			this.visit( (CyanMetaobjectLiteralObjectAnnotation ) node);
		}
		else if ( node instanceof CyanMetaobjectMacroCall ) {
			this.visit( (CyanMetaobjectMacroCall )  node );
		}
		else if ( node instanceof CyanMetaobjectWithAtAnnotation ) {
			this.visit( (CyanMetaobjectWithAtAnnotation ) node );
		}
		else {
			signalError(node);
		}
	}
	public void visit(CyanMetaobjectLiteralObjectAnnotation node) { }
	public void visit(CyanMetaobjectMacroCall node) { }
	public void visit(CyanMetaobjectWithAtAnnotation node) { }

	
	public void visit(Expr node) { 

		if ( node instanceof CyanMetaobjectLiteralObjectAnnotation ) {
			this.visit( (CyanMetaobjectLiteralObjectAnnotation ) node);
		}
		else if ( node instanceof CyanMetaobjectMacroCall ) {
			this.visit( (CyanMetaobjectMacroCall )  node );
		}
		else if ( node instanceof CyanMetaobjectWithAtAnnotation ) {
			this.visit( (CyanMetaobjectWithAtAnnotation ) node );
		}
		else if ( node instanceof ExprAnyLiteral ) {
			this.visit( (ExprAnyLiteral ) node);
		}
		else if ( node instanceof ExprBooleanAnd ) {
			this.visit( (ExprBooleanAnd )  node );
		}
		else if ( node instanceof ExprBooleanOr ) {
			this.visit( (ExprBooleanOr ) node );
		}
		else if ( node instanceof ExprFunctionRegular ) {
			this.visit( (ExprFunctionRegular ) node);
		}
		else if ( node instanceof ExprFunctionWithSelectors ) {
			this.visit( (ExprFunctionWithSelectors )  node );
		}
		else if ( node instanceof  ExprGenericPrototypeInstantiation ) {
			this.visit( (ExprGenericPrototypeInstantiation ) node );
		}
		else if ( node instanceof ExprIdentStar ) {
			this.visit( (ExprIdentStar ) node);
		}
		else if ( node instanceof ExprIndexed ) {
			this.visit( (ExprIndexed )  node );
		}
		else if ( node instanceof ExprMessageSendUnaryChainToExpr ) {
			this.visit( (ExprMessageSendUnaryChainToExpr ) node );
		}
		else if ( node instanceof ExprMessageSendUnaryChainToSuper ) {
			this.visit( (ExprMessageSendUnaryChainToSuper ) node );
		}
		else if ( node instanceof ExprMessageSendWithSelectorsToExpr ) {
			this.visit( (ExprMessageSendWithSelectorsToExpr ) node );
		}
		else if ( node instanceof ExprMessageSendWithSelectorsToSuper ) {
			this.visit( (ExprMessageSendWithSelectorsToSuper ) node );
		}
		else if ( node instanceof ExprObjectCreation ) {
			this.visit( (ExprObjectCreation ) node );
		}
		else if ( node instanceof ExprSelf ) {
			this.visit( (ExprSelf ) node);
		}
		else if ( node instanceof ExprSurroundedByContext ) {
			this.visit( (ExprSurroundedByContext )  node );
		}
		else if ( node instanceof ExprTypeof ) {
			this.visit( (ExprTypeof ) node );
		}
		else if ( node instanceof ExprUnary ) {
			this.visit( (ExprUnary ) node);
		}
		else if ( node instanceof ExprWithParenthesis ) {
			this.visit( (ExprWithParenthesis )  node );
		}
		else {
			signalError(node);
		}
	}
	
	public void visit(ExprAnyLiteral node) { 
		if ( node instanceof ExprAnyLiteralIdent ) {
			this.visit( (ExprAnyLiteralIdent ) node);
		}
		else if ( node instanceof ExprLiteral ) {
			this.visit( (ExprLiteral )  node );
		}
		else if ( node instanceof ExprLiteralArray ) {
			this.visit( (ExprLiteralArray ) node );
		}
		else if ( node instanceof ExprLiteralTuple ) {
			this.visit( (ExprLiteralTuple ) node );
		}
		else if ( node instanceof ExprLiteralMap ) {
			this.visit( (ExprLiteralMap ) node );
		}
		else {
			signalError(node);
		}
		
	}
	public void visit(ExprAnyLiteralIdent node) { }
	public void visit(ExprLiteral node) { 
		if ( node instanceof ExprLiteralBoolean ) {
			this.visit( (ExprLiteralBoolean ) node);
		}
		else if ( node instanceof ExprLiteralChar ) {
			this.visit( (ExprLiteralChar )  node );
		}
		else if ( node instanceof ExprLiteralCyanSymbol ) {
			this.visit( (ExprLiteralCyanSymbol ) node );
		}
		else if ( node instanceof ExprLiteralNil ) {
			this.visit( (ExprLiteralNil ) node );
		}
		else if ( node instanceof ExprLiteralNumber ) {
			this.visit( (ExprLiteralNumber ) node );
		}
		else if ( node instanceof ExprLiteralString ) {
			this.visit( (ExprLiteralString ) node );
		}
		else {
			signalError(node);
		}

	}

	public void visit(ExprBooleanAnd node) { }
	public void visit(ExprBooleanOr node) { }
	public void visit(ExprLiteralBoolean node) { }
	public void visit(ExprLiteralChar node) { }
	public void visit(ExprLiteralCyanSymbol node) { }
	public void visit(ExprLiteralNil node) { }
	public void visit(ExprLiteralNumber node) { }
	public void visit(ExprLiteralString node) { }
	public void visit(ExprLiteralArray node) { }
	public void visit(ExprLiteralTuple node) { }
	public void visit(ExprLiteralMap node) { }
	
	
	public void visit(ExprFunction node) { 
		if ( node instanceof ExprFunctionRegular ) {
			this.visit( (ExprFunctionRegular ) node );
		}
		else if ( node instanceof ExprFunctionWithSelectors ) {
			this.visit( (ExprFunctionWithSelectors ) node );
		}
		else {
			signalError(node);
		}		
	}
	public void visit(ExprFunctionRegular node) { }
	public void visit(ExprFunctionWithSelectors node) { }

	public void visit(ExprGenericPrototypeInstantiation node) { }
	public void visit(ExprIdentStar node) { }
	public void visit(ExprIndexed node) { }

	public void visit(ExprMessageSend node) { 
		if ( node instanceof ExprMessageSendUnaryChainToExpr ) {
			this.visit( (ExprMessageSendUnaryChainToExpr ) node );
		}
		else if ( node instanceof ExprMessageSendUnaryChainToSuper ) {
			this.visit( (ExprMessageSendUnaryChainToSuper ) node );
		}
		else if ( node instanceof ExprMessageSendWithSelectorsToExpr ) {
			this.visit( (ExprMessageSendWithSelectorsToExpr ) node );
		}
		else if ( node instanceof ExprMessageSendWithSelectorsToSuper ) {
			this.visit( (ExprMessageSendWithSelectorsToSuper ) node );
		}
		else {
			signalError(node);
		}		
		
	}
	public void visit(ExprMessageSendUnaryChainToExpr node) { }
	public void visit(ExprMessageSendUnaryChainToSuper node) { }
	public void visit(ExprMessageSendWithSelectorsToExpr node) { }
	public void visit(ExprMessageSendWithSelectorsToSuper node) { }
	
	public void visit(ExprObjectCreation node) { }
	public void visit(ExprSelf node) { }
	public void visit(ExprSelfPeriodIdent node) { }
	public void visit(ExprSurroundedByContext node) { }
	public void visit(ExprTypeof node) { }
	public void visit(ExprUnary node) { }
	public void visit(ExprWithParenthesis node) { 
		this.visit(node.getExpr());
	}
	
	public void visit(GenericParameter node) { }

	public void visit(MessageBinaryOperator node) { }
	public void visit(MessageWithSelectors node) { 
		if ( node instanceof MessageBinaryOperator ) {
			this.visit( (MessageBinaryOperator ) node );
		}
	}

	public void visit(Selector node) { 
		if ( node instanceof SelectorGrammar ) {
			this.visit( (SelectorGrammar ) node );
		}
		else if ( node instanceof SelectorWithMany ) {
			this.visit( (SelectorWithMany ) node );
		}
		else if ( node instanceof SelectorWithParameters ) {
			this.visit( (SelectorWithParameters ) node );
		}
		else if ( node instanceof SelectorWithTypes ) {
			this.visit( (SelectorWithTypes ) node );
		}
		else {
			signalError(node);
		}		
		
	}
	public void visit(SelectorGrammar node) { }
	public void visit(SelectorWithMany node) { }
	public void visit(SelectorWithParameters node) { }
	public void visit(SelectorWithTypes node) { }

	public void visit(VariableDecInterface node) {
		if ( node instanceof InstanceVariableDec ) {
			if ( node instanceof ContextParameter ) {
				this.visit( (ContextParameter ) node );
			}
			else {
				this.visit( (InstanceVariableDec ) node );
			}
		}
		else if ( node instanceof ParameterDec ) {
			this.visit( (ParameterDec ) node );
		}
		else if ( node instanceof StatementLocalVariableDec ) {
			this.visit( (StatementLocalVariableDec ) node );
		}
		else {
			signalError(node);
		}
	}
	public void visit(ParameterDec node) { }

	public void visit(Statement node) { 
		if ( node instanceof Expr ) {
			this.visit( (Expr ) node );
		}
		else if ( node instanceof StatementAssignmentList ) {
			this.visit( (StatementAssignmentList ) node );
		}
		else if ( node instanceof StatementBreak ) {
			this.visit( (StatementBreak ) node );
		}
		else if ( node instanceof StatementMetaobjectAnnotation ) {
			this.visit( (StatementMetaobjectAnnotation ) node );
		}
		else if ( node instanceof StatementFor ) {
			this.visit( (StatementFor ) node );
		}
		else if ( node instanceof StatementType ) {
			this.visit( (StatementType) node );
		}
		else if ( node instanceof StatementIf ) {
			this.visit( (StatementIf ) node );
		}
		else if ( node instanceof StatementLocalVariableDec ) {
			this.visit( (StatementLocalVariableDec ) node );
		}
		else if ( node instanceof StatementLocalVariableDecList ) {
			this.visit( (StatementLocalVariableDecList ) node );
		}
		else if ( node instanceof StatementMinusMinusIdent ) {
			this.visit( (StatementMinusMinusIdent ) node );
		}
		else if ( node instanceof StatementNull ) {
			this.visit( (StatementNull ) node );
		}
		else if ( node instanceof StatementPlusPlusIdent ) {
			this.visit( (StatementPlusPlusIdent ) node );
		}
		else if ( node instanceof StatementReturn ) {
			this.visit( (StatementReturn ) node );
		}
		else if ( node instanceof StatementReturnFunction ) {
			this.visit( (StatementReturnFunction ) node );
		}
		else if ( node instanceof StatementReturnMethod ) {
			this.visit( (StatementReturnMethod ) node );
		}
		else if ( node instanceof StatementWhile ) {
			this.visit( (StatementWhile ) node );
		}
		else if ( node instanceof StatementNotNil ) {
			this.visit( (StatementNotNil ) node );
		}
		else {
			signalError(node);
		}
		
	}
	
	
	public void visit(StatementList node) { }
	public void visit(StatementAssignmentList node) { }
	public void visit(StatementBreak node) { }
	public void visit(StatementMetaobjectAnnotation node) { }

	public void visit(StatementFor node) { }
	public void visit(StatementIf node) { }
    public void visit(StatementLocalVariableDec node) { }
    public void visit(StatementLocalVariableDecList node) { }
	public void visit(StatementMinusMinusIdent node) { }
	public void visit(StatementNull node) { }
	public void visit(StatementPlusPlusIdent node) { }
    
	public void visit(StatementReturn node) { }
	public void visit(StatementReturnFunction node) { }
	public void visit(StatementReturnMethod node) { }
	public void visit(StatementWhile node) { }
	public void visit(StatementType node) { }
	public void visit(CaseRecord node) { }
	public void visit(StatementNotNil node) { }   
	

	public void visit(SelectorWithRealParameters node) { }
	
	
}
