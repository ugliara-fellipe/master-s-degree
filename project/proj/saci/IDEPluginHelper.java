package saci;

import java.util.ArrayList;
import java.util.HashSet;
import ast.CompilationUnit;
import ast.CyanMetaobjectAnnotation;
import ast.InstanceVariableDec;
import ast.ObjectDec;
import ast.ProgramUnit;
import lexer.HighlightColor;
import lexer.Symbol;
import lexer.SymbolIdent;
import meta.CyanMetaobject;

/**
 * class with static methods to link the compiler to the IDE
   @author jose
 */
public class IDEPluginHelper {
	
	/**
	 * Return a list of tuples. Each tuple (color number, line number, column number, size). <br>
	 * The characters starting at line number, column number till column number + size - 1
	 * should be highlighted in color "color number". 
	   @param saci
	   @return
	 */
	static public ArrayList<Tuple4<Integer, Integer, Integer, Integer>>  getColorList(Saci saci) {
		ArrayList<Tuple4<Integer, Integer, Integer, Integer>> colorList = new ArrayList<>();
		
		HashSet<String> ivSet = new HashSet<>();
		CompilationUnit compUnit = saci.getLastCompilationUnitParsed();
		ObjectDec proto = null;
		if ( compUnit != null ) {
			ProgramUnit pu = compUnit.getPublicPrototype();
			if ( pu != null && pu instanceof ObjectDec ) {
				proto = (ObjectDec ) pu;
				for ( InstanceVariableDec iv : proto.getInstanceVariableList() ) {
					ivSet.add(iv.getName());
				}
			}
			
		}
		
		
		for (int i = 0; i < saci.getSizeSymbolList(); ++i) {
			Symbol sym = saci.getSymbolList()[i];
			
			CyanMetaobjectAnnotation annotation = sym.getCyanMetaobjectAnnotation();
			if ( annotation != null ) {
				/*
				 * symbol represents a metaobject annotation
				 */
				CyanMetaobject cyanMetaobject = annotation.getCyanMetaobject();
				ArrayList<Tuple4<Integer, Integer, Integer, Integer>> moColorList = cyanMetaobject.getColorList(annotation);
				if ( moColorList != null && moColorList.size() > 0 ) {
					int baseLine = annotation.getFirstSymbol().getLineNumber() - 1;
					for ( Tuple4<Integer, Integer, Integer, Integer> t : moColorList ) {
						/*
						 * baseLine must be added because the metaobject uses line numbers relative to the start of
						 * the metaobject annotation.
						 */
						colorList.add(new Tuple4<Integer, Integer, Integer, Integer>(
								t.f1, t.f2 + baseLine, t.f3, t.f4));
					}
				}
			}
			else if ( sym instanceof SymbolIdent ) { 
				SymbolIdent si = (SymbolIdent ) sym;
				if ( ivSet.contains(si.getSymbolString()) ) {
					// identifier is an instance variable. Use instance variable color
					colorList.add(new Tuple4<Integer, Integer, Integer, Integer>(
							HighlightColor.instanceVariable, sym.getLineNumber(), sym.getColumnNumber(), sym.getSymbolString().length()));
				}
			} 
			else {
				// not a metaobject annotation
				colorList.add(new Tuple4<Integer, Integer, Integer, Integer>(
						sym.getColor(), sym.getLineNumber(), sym.getColumnNumber(), sym.getSymbolString().length()));
			}
		}
		
		return colorList;
	}

}
