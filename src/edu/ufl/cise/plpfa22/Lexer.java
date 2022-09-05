package edu.ufl.cise.plpfa22;

import java.io.IOException;
import java.io.StringReader;
import edu.ufl.cise.plpfa22.Token;
import static edu.ufl.cise.plpfa22.Token.Kind;

public class Lexer implements ILexer {

	String text;
	StringReader r;
	int ch;

	public Lexer(String text) {
		this.text = text;
	}

	public enum State {
		START,
		IN_IDENT,
		HAVE_ZERO,
		HAVE_DOT,
		IN_FLOAT,
		IN_NUM,
		HAVE_EQ,
		HAVE_MINUS
	}

	@Override
	public IToken next() throws LexicalException {
		Token t = null;
		StringBuilder st = new StringBuilder();
		State state = State.START;
		r = new StringReader(text);
		while (t == null) {
			try {
				ch = r.read();
				switch (state) {
					case START: {
						switch (ch) {
							case -1: {
								t = new Token(Kind.EOF, 1, 1);
							}
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return t;
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}
