package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
 * represents an expression surrounded by metaobject annotations
   such as <code>i+j</code> in
 * <code> <br>
 * {@literal @}markDeletedCode(1)
 * {@literal @}pushCompilationContext(ati_id_2, "number(bin, Bin, BIN)", main, Program, 6) i + j {@literal @}popCompilationContext(ati_id_2)<br><br>
 *
 * The metaobject annotations that precede the expression should be a regular metaobject annotation, optionally markDeletedCode, and necessarily
 * pushCompilationContext. The metaobject annotation that follows the expression should be popCompilationContext.
 * </code>
   @author José
 */

public class ExprSurroundedByContext extends Expr {

	public ExprSurroundedByContext(CyanMetaobjectWithAtAnnotation regularMetaobjectAnnotation, CyanMetaobjectWithAtAnnotation markDeletedCodeMetaobjectAnnotation,
			CyanMetaobjectWithAtAnnotation pushCompilationContextMetaobjectAnnotation,
			Expr expr, CyanMetaobjectWithAtAnnotation popCompilationContextMetaobjectAnnotation) {
		super();
		this.regularMetaobjectAnnotation = regularMetaobjectAnnotation;
		this.markDeletedCodeMetaobjectAnnotation = markDeletedCodeMetaobjectAnnotation;
		this.pushCompilationContextMetaobjectAnnotation = pushCompilationContextMetaobjectAnnotation;
		this.expr = expr;
		this.popCompilationContextMetaobjectAnnotation = popCompilationContextMetaobjectAnnotation;
	}


	@Override
	public void accept(ASTVisitor visitor) {
		expr.accept(visitor);
		visitor.visit(this);
	}


	@Override
	public boolean mayBeStatement() {
		return expr.mayBeStatement();
	}



	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine,
			CyanEnv cyanEnv, boolean genFunctions) {
		pw.print(" ( ");
		if ( regularMetaobjectAnnotation != null ) {
			regularMetaobjectAnnotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		}
		pw.print(" ");
		if ( this.markDeletedCodeMetaobjectAnnotation != null ) {
			this.markDeletedCodeMetaobjectAnnotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			pw.print(" ");
		}
		pushCompilationContextMetaobjectAnnotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.print(" ");
		expr.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.print(" ");
		popCompilationContextMetaobjectAnnotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.print(" ) ");
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		return expr.genJavaExpr(pw, env);
	}

	@Override
	public void calcInternalTypes(Env env) {

		if ( regularMetaobjectAnnotation != null ) {
			regularMetaobjectAnnotation.calcInternalTypes(env);
		}
		if ( this.markDeletedCodeMetaobjectAnnotation != null ) {
			this.markDeletedCodeMetaobjectAnnotation.calcInternalTypes(env);
		}
		pushCompilationContextMetaobjectAnnotation.calcInternalTypes(env);
		expr.calcInternalTypes(env);
		type = expr.getType(env);
		popCompilationContextMetaobjectAnnotation.calcInternalTypes(env);
		ArrayList<Object> javaParamList = popCompilationContextMetaobjectAnnotation.getJavaParameterList();
		if ( javaParamList.size() != 3 ) {
			env.error(this.popCompilationContextMetaobjectAnnotation.getFirstSymbol(), "It was expected that '" + NameServer.popCompilationContextName +
					"' had three parameters, the last two with the type of the expression");
		}
		else {
			String packageName = NameServer.removeQuotes((String ) popCompilationContextMetaobjectAnnotation.getJavaParameterList().get(1));
			String prototypeName = NameServer.removeQuotes((String ) popCompilationContextMetaobjectAnnotation.getJavaParameterList().get(2));
			ProgramUnit pu = env.searchPackagePrototype(
					packageName,
					prototypeName);
			if ( pu == null ) {
				env.error(this.popCompilationContextMetaobjectAnnotation.getFirstSymbol(), "Metaobject'" + NameServer.popCompilationContextName +
						"' says that the metaobject '" +
						NameServer.removeQuotes( (String  ) this.pushCompilationContextMetaobjectAnnotation.getJavaParameterList().get(1))
						+ "' should produce an expression of type '" +
						packageName + "." + prototypeName + "' but this type does not exist"
						);
			}
			else {
				if ( !(pu.isSupertypeOf(type, env)) ) {
					env.error(this.popCompilationContextMetaobjectAnnotation.getFirstSymbol(), "Metaobject '" + NameServer.popCompilationContextName +
							"' says that the metaobject '" +
							NameServer.removeQuotes( (String  ) this.pushCompilationContextMetaobjectAnnotation.getJavaParameterList().get(1))
							+ "' should produce an expression of type '" +
							packageName + "." + prototypeName + "'. But the expression produced has type '" +
							type.getFullName() + "' which is not subtype of that type"
							);
				}
			}
		}


		super.calcInternalTypes(env);
	}

	@Override
	public Symbol getFirstSymbol() {
		return regularMetaobjectAnnotation.getFirstSymbol();
	}

	public Expr getExpr() {
		return expr;
	}



	private CyanMetaobjectWithAtAnnotation regularMetaobjectAnnotation, markDeletedCodeMetaobjectAnnotation, pushCompilationContextMetaobjectAnnotation, popCompilationContextMetaobjectAnnotation;
	private Expr expr;
}
