package edu.ufl.cise.plpfa22;

import java.io.IOException;
import java.io.StringReader;
import edu.ufl.cise.plpfa22.Token;
import static edu.ufl.cise.plpfa22.Token.Kind;

public class Lexer implements ILexer {

	int currLine = 1;
	int currColumn = 0;
	String text;
	StringReader r;
	int ch;

	public Lexer(String text) {
		this.text = text;
		this.r = new StringReader(text);
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

	void updateLocation() throws IOException {
		if (ch == '\n') {
			currLine = currLine + 1;
			ch = r.read();
			currColumn = 1;

		} else if (ch == '\r') {
			currLine++;
			ch = r.read();
			if (ch == '\n') {
				ch = r.read();
			}
			currColumn = 1;
		} else {
			ch = r.read();
			currColumn++;
		}
	}

	@Override
	public IToken next() throws LexicalException {
		int line = 0;
		int column = 0;
		Token t = null;
		StringBuilder st = new StringBuilder();
		State state = State.START;
		while (t == null) {
			try {
				switch (state) {
					case START: {
						while (Character.isWhitespace(ch)) {
							updateLocation();
						}
						column = currColumn;
						line = currLine;
						switch (ch) {
							case 0: {
								updateLocation();
							}
								break;
							case -1: {
								t = new Token(Kind.EOF, "EOF", line, column);
							}
								break;
							case 43: {

								t = new Token(Kind.PLUS, "+", line, column);
								updateLocation();

							}
								break;
							case 45: {
								t = new Token(Kind.MINUS, "-", line, column);
								updateLocation();
							}
								break;
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
