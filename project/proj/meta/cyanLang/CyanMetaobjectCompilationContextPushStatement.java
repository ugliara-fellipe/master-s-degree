package meta.cyanLang;

import saci.NameServer;

/**
 * See documentation on {@link meta#CyanMetaobjectCompilationContextPush}. 
 * When the code produced is a statement, use this class instead of  {@link meta#CyanMetaobjectCompilationContextPush}.
   @author jose
 */
public class CyanMetaobjectCompilationContextPushStatement extends CyanMetaobjectCompilationContextPush {

	@Override
	public String getName() {
		return NameServer.pushCompilationContextStatementName;
	}

}
