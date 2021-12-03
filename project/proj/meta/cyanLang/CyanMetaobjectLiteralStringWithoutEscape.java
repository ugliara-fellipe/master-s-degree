package meta.cyanLang;

import lexer.Lexer;
import meta.CyanMetaobjectLiteralString;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;
import meta.IParseWithoutCyanCompiler_dpa;

public class CyanMetaobjectLiteralStringWithoutEscape extends CyanMetaobjectLiteralString 
       implements IParseWithoutCyanCompiler_dpa {

		public CyanMetaobjectLiteralStringWithoutEscape() {
			super();
		}
		
		@Override
		public String[] getPrefixNames() {
			return new String[] { "n", "N" };
		}


		@Override
		public void dpa_parse(ICompilerAction_dpa compilerAction, String code) {

			setInfo( new StringBuffer("\"" + Lexer.escapeJavaString(code) + "\"") );
		}

		@Override
		public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
			return (StringBuffer ) getInfo();
		}
		
		@Override
		public String getPackageOfType() {
			return "cyan.lang";
		}

		@Override
		public String getPrototypeOfType() {
			return "String";
		}
				
		
	}



