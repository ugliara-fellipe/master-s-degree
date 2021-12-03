package meta.cyanLang;

import ast.MetaobjectArgumentKind;
import meta.CyanMetaobjectWithAt;
import meta.IStaticTyping;

/**
 * See comments on {@link meta#IStaticTyping}
   @author José
 */
public class CyanMetaobjectStaticTyping extends CyanMetaobjectWithAt implements IStaticTyping {

	public CyanMetaobjectStaticTyping() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "staticTyping";
	}

}
