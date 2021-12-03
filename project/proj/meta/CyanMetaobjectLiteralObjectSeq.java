package meta;

/**
 * represents a user-defined literal object delimited by a sequence of symbols such as
 *       [% (1, 2), (2, 3), (3, 1) %]
 *       
   @author José
 */
abstract public class CyanMetaobjectLiteralObjectSeq extends CyanMetaobjectLiteralObject {
	
	
	@Override
	public CyanMetaobjectLiteralObjectSeq clone() {
		try {
			return (CyanMetaobjectLiteralObjectSeq ) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	/**
	 * return the left sequence of characters that should start this literal object. In the
	 * example  [| "one" -> 1, "two" -> 2 |],  this method should return "[|". 
	   @return
	 */
	abstract public String leftCharSequence();

	/**
	 * the name is the left char sequence that start the metaobject, a literal object 
	 */
	@Override
	public String getName() {
		return this.leftCharSequence();
	}

}
