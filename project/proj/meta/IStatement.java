package meta;

import lexer.Symbol;

public interface IStatement {
	String asString();
	Symbol getFirstSymbol();
}
