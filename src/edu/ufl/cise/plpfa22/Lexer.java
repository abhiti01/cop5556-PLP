package edu.ufl.cise.plpfa22;

import java.io.Reader;

public class Lexer implements ILexer {
	
	String text;
	Reader r;
	char ch;
	public Lexer(String text) {
		this.text = text;
	}
	

	@Override
	public IToken next() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}
