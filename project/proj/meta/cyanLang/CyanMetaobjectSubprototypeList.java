package meta.cyanLang;


import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.InterfaceDec;
import ast.MetaobjectArgumentKind;
import ast.ObjectDec;
import ast.ProgramUnit;
import ast.Type;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckSubprototype_dsa2;
import meta.ICompiler_dsa;
import saci.NameServer;

/**
 * The metaobject this class represents limits the subprototypes a prototype can have. 
 * When used as 
 *      @subprototypeList(AddExpr, MultExpr, LiteralExpr) object Expr ... end
 *      
 * the compiler will sign an error a prototype not listed in the metaobject arguments tries to
 * inherit from Expr. Generic prototypes should be put among quotes:
 * 
 *      @subprototypeList("MyStack<T>") object Stack<T> ... end
 *  
   @author José
 */

public class CyanMetaobjectSubprototypeList extends CyanMetaobjectWithAt 
    implements ICheckSubprototype_dsa2 {
	
	public CyanMetaobjectSubprototypeList() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}
	
	@Override
	public String getName() {
		return "subprototypeList";
	}
	


	@Override
	public
	ArrayList<CyanMetaobjectError> check() {
		ArrayList<Object> parameterList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList();
		for ( Object obj : parameterList ) 
			if ( ! (obj instanceof String) ) {
				return addError("All parameters should be of type String");
			}
		return null;
	}


	@Override
	public void dsa2_checkSubprototype(ICompiler_dsa compiler_dsa, Type t) {
		ProgramUnit attachedProgramUnit =  (ProgramUnit ) this.getAttachedDeclaration();
		if ( t instanceof ObjectDec ) {
			ObjectDec subProto = (ObjectDec ) t;
			if ( subProto.getSuperobject() != attachedProgramUnit ) {
				/*
				 * only direct subprototypes are considered
				 */
				return ;
			}
		}
		else if ( t instanceof InterfaceDec ) {
			InterfaceDec subInter = (InterfaceDec ) t;
			boolean found = false;
			for ( InterfaceDec superInter : subInter.getSuperInterfaceList() ) {
				if ( superInter == attachedProgramUnit ) {
					found = true;
					break;
				}
			}
			if ( ! found ) {
				/*
				 * only direct sub-interfaces are considered
				 */
				return ;
			}
		}
		
		ArrayList<Object> parameterList = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getJavaParameterList();
		String typeName = t.getFullName();
		for ( Object obj : parameterList ) {
			String s = NameServer.removeQuotes((String ) obj);
			/* if ( s.contains(" ") )
				S ystem.out.p rintln("s");  
			
			s = s.replaceAll("\\s", "");
			if ( s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"' )
				s = s.substring(1, s.length()-1);
			*/
			if ( s.equals(typeName) )
				return ;
		}
		Declaration dec = ((CyanMetaobjectWithAtAnnotation ) metaobjectAnnotation).getDeclaration();
		ProgramUnit pu = (ProgramUnit ) dec;
		
		this.addError("Prototype '" + pu.getFullName() + "' cannot be inherited by '" + typeName + "'");
	}	


	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.PROTOTYPE_DEC };
	
}
