package ast;

import java.util.ArrayList;
import error.CompileErrorException;
import lexer.Symbol;
import lexer.Token;
import saci.CyanEnv;
import saci.Env;
import saci.Tuple2;

/**
 * This class is superclass of all classes describing slots: InstanceVariableDec and MethodDec.
 * @author José
 *
 */
abstract public class SlotDec implements Declaration, ASTNode {

	public SlotDec(Token visibility, ArrayList<CyanMetaobjectWithAtAnnotation> attachedSlotMetaobjectAnnotationList,
			ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedSlotMetaobjectAnnotationList) {
		this.visibility = visibility;
		this.attachedMetaobjectAnnotationList = attachedSlotMetaobjectAnnotationList;
		this.nonAttachedMetaobjectAnnotationList = nonAttachedSlotMetaobjectAnnotationList;
	}
	public void setVisibility(Token visibility) {
		this.visibility = visibility;
	}
	public Token getVisibility() {
		return visibility;
	}

	@Override
	abstract public String getName();
	abstract public Symbol getFirstSymbol();

	public void calcInterfaceTypes(Env env) {
		try {
			this.calcInternalTypes(env);
		}
		catch ( CompileErrorException e ) {
		}

	}

	public void calcInternalTypes(Env env) {
		calcInternalTypesMetaobjectAnnotationsPreced(env);
	}

	/**
	 * calculates internal types of annotations that precede the slot
	   @param env
	 */
	public void calcInternalTypesMetaobjectAnnotationsPreced(Env env) {
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList )
				annotation.calcInternalTypes(env);
		}
	}

	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		if ( nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList )
				annotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		}
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList )
				annotation.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		}

	}

	public void genJava(PWInterface pw, Env env) {

		if ( this.attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				annotation.genJava(pw, env);
				/* if ( annotation instanceof meta.IAction_cge )
					pw.print( ((IAction_cge) annotation).cge_codeToAddAtMetaobjectAnnotation() ); */
			}
		}
		if ( this.nonAttachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : nonAttachedMetaobjectAnnotationList ) {
				annotation.genJava(pw, env);
				/* if ( annotation instanceof meta.IAction_cge )
					pw.print( ((IAction_cge) annotation).cge_codeToAddAtMetaobjectAnnotation() ); */
			}
		}

	}

	public ArrayList<CyanMetaobjectWithAtAnnotation> getAttachedMetaobjectAnnotationList() {
		return attachedMetaobjectAnnotationList;
	}
	public void setMetaobjectAnnotationList(ArrayList<CyanMetaobjectWithAtAnnotation> ctmetaobjectAnnotationList) {
		this.attachedMetaobjectAnnotationList = ctmetaobjectAnnotationList;
	}


	@Override
	public ArrayList<Tuple2<String, ExprAnyLiteral>> getFeatureList() {
		return featureList;
	}

	public void addFeature(Tuple2<String, ExprAnyLiteral> feature) {
		if ( featureList == null )
			featureList = new ArrayList<>();
		else {
			int size = featureList.size();
			for ( int i = 0; i < size; ++i) {
				if ( featureList.get(i).f1.equals(feature.f1) ) {
					// replace
					featureList.set(i, feature);
					return;
				}
			}
		}
		featureList.add(feature);
	}

	public void check_cin(Env env) {
		if ( attachedMetaobjectAnnotationList != null ) {
			for ( CyanMetaobjectWithAtAnnotation annotation : attachedMetaobjectAnnotationList ) {
				if ( annotation instanceof ICheck_cin ) {

					try {
						((ICheck_cin) annotation).check(env);
					}
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						// e.printStackTrace();
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobjectCatchExceptions(annotation.getCyanMetaobject(), annotation);
					}

				}
			}
		}
	}


	/**
	 * the list of features associated to this slot
	 */
	private ArrayList<Tuple2<String, ExprAnyLiteral>> featureList;


	/**
	 * metaobject annotations before this slot declaration such as in <br>
	 * <code>
	 *     {@literal @}getset var Int name<br>
	 * </code><br>
	 * These annotations are attached to the slot declaration.
	 */
	protected ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList;

	/**
	 * metaobject annotations before this program unit such as <br>
	 * <code>
	 *     {@literal @}javacode{*  void asString() { ... } *}
	 *     var Int name<br>
	 * </code><br>
	 * These annotations are NOT attached to the slot declaration.
	 */
	protected ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList;

	protected Token visibility;


}
