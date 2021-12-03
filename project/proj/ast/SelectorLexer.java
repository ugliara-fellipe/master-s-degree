package ast;

import java.util.ArrayList;

/**
 * this class is used for parsing a message send, that is, a sequence of selectors with their real parameters.
   @author jose
 */

public class SelectorLexer {

	public SelectorLexer(ArrayList<SelectorWithRealParameters> selList) {
		this.selList = selList;
		index = 0;
		if ( selList.size() > 0 ) {
			current = selList.get(0);
		}
		else
			current = null;
	}
	
	public SelectorWithRealParameters current() {
		return current;
	}
	
	public SelectorWithRealParameters next() {
		if ( index < selList.size() - 1 ) {
			++index;
			current = selList.get(index);
		}
		else {
			index = selList.size();
			current = null;
		}
		return current;
	}

	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		if ( index < selList.size() ) {
			this.index = index;
			current = selList.get(index);
		}
		else {
			this.index = selList.size();
			current = null;
		}
	}
	

	
	private ArrayList<SelectorWithRealParameters> selList;
	private SelectorWithRealParameters current;
	private int index;
}
