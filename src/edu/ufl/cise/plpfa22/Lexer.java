package edu.ufl.cise.plpfa22;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import edu.ufl.cise.plpfa22.Token;
import static edu.ufl.cise.plpfa22.Token.Kind;

public class Lexer implements ILexer {

	int currLine = 1;
	int currColumn = 0;
	String text;
	StringReader r;
	int ch;
	int c;
	List<String> keyWords = Arrays.asList("CONST", "VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE",
			"DO");
	List<String> Bools = Arrays.asList("TRUE", "FALSE");

	public Lexer(String text) {
		this.text = text;
		this.r = new StringReader(text);
	}

	public enum State {
		START,
		IN_IDENT,
		IN_NUM,
		IN_STRINGLIT,
		IN_COMMENT,
		HAVE_COLON,
		HAVE_LT,
		HAVE_GT
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
		StringBuilder st = null;
		State state = State.START;
		while (t == null) {
			System.out.println("State: " + state);
			System.out.println("Character " + (char) ch);
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
							case 42: {
								t = new Token(Kind.TIMES, "*", line, column);
								updateLocation();
							}
								break;
							case 40: {
								t = new Token(Kind.LPAREN, "(", line, column);
								updateLocation();
							}
								break;
							case 41: {
								t = new Token(Kind.RPAREN, ")", line, column);
								updateLocation();
							}
								break;
							case 46: {
								t = new Token(Kind.DOT, ".", line, column);
								updateLocation();
							}
								break;
							case 59: {
								t = new Token(Kind.SEMI, ";", line, column);
								updateLocation();
							}
								break;
							case 37: {
								t = new Token(Kind.MOD, "%", line, column);
								updateLocation();
							}
								break;
							case 63: {
								t = new Token(Kind.QUESTION, "?", line, column);
								updateLocation();
							}
								break;
							case 33: {
								t = new Token(Kind.BANG, "!", line, column);
								updateLocation();
							}
								break;
							case 35: {
								t = new Token(Kind.NEQ, "#", line, column);
								updateLocation();
							}
								break;
							case 61: {
								t = new Token(Kind.EQ, "=", line, column);
								updateLocation();
							}
								break;
							case 58: {
								state = State.HAVE_COLON;
								updateLocation();
							}
								break;
							case 60: {
								state = State.HAVE_LT;
								updateLocation();
							}
								break;
							case 62: {
								state = State.HAVE_GT;
								updateLocation();
							}
								break;
							case 34: {
								state = State.IN_STRINGLIT;
								st = new StringBuilder();
								st.append((char) 34);
								updateLocation();
							}
								break;
							case 47: {
								state = State.IN_COMMENT;
								updateLocation();
							}
								break;
							default: {
								if (Character.isJavaIdentifierStart(ch)) {
									System.out.println((char) ch);
									state = State.IN_IDENT;
									st = new StringBuilder();
									st.append((char) ch);
									updateLocation();
								} else if (Character.isDigit(ch)) {
									state = State.IN_NUM;
									st = new StringBuilder();
									st.append((char) ch);
									updateLocation();
								} else {
									throw new LexicalException(
											"Error while getting next token, state: " + state + ", char: " + (char) ch);
								}
							}
								break;
						}

					}
						break;
					case HAVE_LT: {
						switch (ch) {
							case '=': {
								t = new Token(Kind.LE, "<=", line, column);
								state = State.START;
								updateLocation();
							}
								break;
							default: {
								t = new Token(Kind.LT, "<", line, column);
								state = State.START;
							}
								break;
						}
					}
						break;
					case HAVE_GT: {
						switch (ch) {
							case '=': {
								t = new Token(Kind.GE, ">=", line, column);
								state = State.START;
								updateLocation();
							}
								break;
							default: {
								t = new Token(Kind.GT, ">", line, column);
								state = State.START;
							}
								break;
						}
					}
						break;
					case HAVE_COLON: {
						switch (ch) {
							case '=': {
								t = new Token(Kind.ASSIGN, ":=", line, column);
								state = State.START;
								updateLocation();
							}
								break;
							default: {
								throw new LexicalException("Expected \" = \" after \" : \"");
							}
						}
					}
						break;
					case IN_STRINGLIT: {
						if (ch < 0 || ch > 127) {
							throw new LexicalException("Invalid character found in string literal");

						} else {
							if (ch == 34) {
								st.append((char) ch);
								t = new Token(Kind.STRING_LIT, st.toString(), line, column);
								state = State.START;
								updateLocation();
							} else if (ch == 92) {
								updateLocation();
								switch (ch) {
									case 'b': {
										st.append("\b");
										updateLocation();
										break;
									}
									case 't': {
										st.append("\t");
										updateLocation();
										break;
									}
									case 'n': {
										st.append("\n");
										updateLocation();
										break;
									}
									case 'f': {
										st.append("\f");
										updateLocation();
										break;
									}
									case 'r': {
										st.append("\r");
										updateLocation();
										break;
									}
									case 34: {
										st.append("\"");
										updateLocation();
										break;
									}
									case 39: {
										st.append("\'");
										updateLocation();
										break;
									}
									case 92: {
										st.append("\\");
										updateLocation();
										break;
									}

									default: {
										throw new LexicalException("Could not match escape character after backslash");
									}

								}
							} else {
								st.append((char) ch);
								updateLocation();
							}
						}

					}
						break;
					case IN_COMMENT: {
						if (ch == '\n' || ch == '\r' || ch == -1) {
							state = State.START;
							updateLocation();
						} else {
							updateLocation();
						}
					}
						break;
					case IN_IDENT: {

						if (Character.isJavaIdentifierPart(ch)) {
							st.append((char) ch);
							updateLocation();
						} else if (ch == '\n') {
							t = new Token(Kind.IDENT, st.toString(), line, column);
							state = State.START;
							updateLocation();
						} else if (ch == 32) {
							String txt;
							txt = st.toString();
							t = new Token(Kind.IDENT, txt, line, column);
							state = State.START;
							updateLocation();
						} else {
							String txt;
							txt = st.toString();
							if (keyWords.contains(txt) || Bools.contains(txt)) {
								switch (txt) {
									case "CONST": {
										t = new Token(Kind.KW_CONST, txt, line, column);
										state = State.START;
									}
										break;
									case "VAR": {
										t = new Token(Kind.KW_VAR, txt, line, column);
										state = State.START;
									}
										break;
									case "PROCEDURE": {
										t = new Token(Kind.KW_PROCEDURE, txt, line, column);
										state = State.START;
									}
										break;
									case "CALL": {
										t = new Token(Kind.KW_CALL, txt, line, column);
										state = State.START;
									}
										break;
									case "BEGIN": {
										t = new Token(Kind.KW_BEGIN, txt, line, column);
										state = State.START;
									}
										break;
									case "END": {
										t = new Token(Kind.KW_END, txt, line, column);
										state = State.START;
									}
										break;
									case "IF": {
										t = new Token(Kind.KW_IF, txt, line, column);
										state = State.START;
									}
										break;
									case "THEN": {
										t = new Token(Kind.KW_THEN, txt, line, column);
										state = State.START;
									}
										break;
									case "WHILE": {
										t = new Token(Kind.KW_WHILE, txt, line, column);
										state = State.START;
									}
										break;
									case "DO": {
										t = new Token(Kind.KW_DO, txt, line, column);
										state = State.START;
									}
										break;
									case "TRUE": {
										t = new Token(Kind.BOOLEAN_LIT, txt, line, column);
										state = State.START;
									}
										break;
									case "FALSE": {
										t = new Token(Kind.BOOLEAN_LIT, txt, line, column);
										state = State.START;
									}
										break;
									default: {
										t = new Token(Kind.IDENT, txt, line, column);
										state = State.START;
									}
										break;
								}
							} else {
								throw new LexicalException("Invalid character for Identifier");
							}
						}
					}
						break;
					case IN_NUM: {
						if (Character.isDigit(ch)) {
							st.append((char) ch);
							updateLocation();
						} else {
							String txt;
							txt = st.toString();
							try {
								Integer.parseInt(txt);
								t = new Token(Kind.NUM_LIT, txt, line, column);
								state = State.START;
							} catch (NumberFormatException ex) {
								throw new LexicalException("The number is too big for integer literal");
							}
						}
					}
						break;
					default:
						throw new LexicalException("case not handled by lexer for " + (char) ch);

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return t;
	}

	@Override
	public IToken peek() throws LexicalException {
		Token t = null;
		StringBuilder s = null;
		int line = 0;
		int column = 0;
		try {
			r.mark(Integer.MAX_VALUE);
			c = r.read();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		State state = State.START;
		while (t == null) {
			try {
				switch (state) {
					case START: {
						line = currLine;
						column = currColumn;
						switch (c) {
							case 0: {
								c = r.read();
							}
								break;
							case -1: {
								t = new Token(Kind.EOF, "EOF", line, column);
							}
								break;
							case 43: {
								t = new Token(Kind.PLUS, "+", line, column);
							}
								break;
							case 45: {
								t = new Token(Kind.MINUS, "-", line, column);
							}
								break;
							case 42: {
								t = new Token(Kind.TIMES, "*", line, column);
							}
								break;
							case 40: {
								t = new Token(Kind.LPAREN, "(", line, column);
							}
								break;
							case 41: {
								t = new Token(Kind.RPAREN, ")", line, column);
							}
								break;
							case 46: {
								t = new Token(Kind.DOT, ".", line, column);
							}
								break;
							case 59: {
								t = new Token(Kind.SEMI, ";", line, column);
							}
								break;
							case 37: {
								t = new Token(Kind.MOD, "%", line, column);
							}
								break;
							case 63: {
								t = new Token(Kind.QUESTION, "?", line, column);
							}
								break;
							case 33: {
								t = new Token(Kind.BANG, "!", line, column);
							}
								break;
							case 35: {
								t = new Token(Kind.NEQ, "#", line, column);
							}
								break;
							case 61: {
								t = new Token(Kind.EQ, "=", line, column);
							}
								break;
							case 58: {
								state = State.HAVE_COLON;
							}
								break;
							case 60: {
								state = State.HAVE_LT;
							}
								break;
							case 62: {
								state = State.HAVE_GT;
							}
								break;
							case 34: {
								state = State.IN_STRINGLIT;
								s = new StringBuilder();
								s.append((char) 34);
							}
								break;
							case 47: {
								state = State.IN_COMMENT;
							}
								break;
							default: {
								if (Character.isJavaIdentifierStart(c)) {
									state = State.IN_IDENT;
									s = new StringBuilder();
									s.append((char) c);
								} else if (Character.isDigit(c)) {
									state = State.IN_NUM;
									s = new StringBuilder();

								} else {
									throw new LexicalException(
											"Error while peeking, state: " + state + ", char: " + (char) c);
								}
							}
						}
					}
						break;
					case HAVE_LT: {
						c = r.read();
						switch (c) {
							case '=': {
								t = new Token(Kind.LE, "<=", line, column);
								state = State.START;
							}
								break;
							default: {
								t = new Token(Kind.LT, "<", line, column);
								state = State.START;
							}
								break;
						}
					}
						break;
					case HAVE_GT: {
						c = r.read();
						switch (ch) {
							case '=': {
								t = new Token(Kind.GE, ">=", line, column);
								state = State.START;
							}
								break;
							default: {
								t = new Token(Kind.GT, ">", line, column);
								state = State.START;
							}
								break;
						}
					}
						break;
					case HAVE_COLON: {
						c = r.read();
						switch (c) {
							case '=': {
								t = new Token(Kind.ASSIGN, ":=", line, column);
								state = State.START;
							}
								break;
							default: {
								throw new LexicalException("Expected \" = \" after \" : \"");
							}
						}
					}
						break;
					case IN_STRINGLIT: {
						c = r.read();
						if (c < 0 || c > 127) {
							throw new LexicalException("Invalid character found in string literal");

						} else {
							if (c == 34) {
								s.append((char) c);
								t = new Token(Kind.STRING_LIT, s.toString(), line, column);
								state = State.START;
							} else if (c == 92) {
								c = r.read();
								switch (c) {
									case 'b': {
										s.append("\b");
										c = r.read();
										break;
									}
									case 't': {
										s.append("\t");
										c = r.read();
										break;
									}
									case 'n': {
										s.append("\n");
										c = r.read();
										break;
									}
									case 'f': {
										s.append("\f");
										c = r.read();
										break;
									}
									case 'r': {
										s.append("\r");
										c = r.read();
										break;
									}
									case 34: {
										s.append("\"");
										c = r.read();
										break;
									}
									case 39: {
										s.append("\'");
										c = r.read();
										break;
									}
									case 92: {
										s.append("\\");
										c = r.read();
										break;
									}

									default: {
										throw new LexicalException("Could not match escape character after backslash");
									}

								}
							} else {
								s.append((char) c);
								c = r.read();
							}
						}

					}
						break;
					case IN_COMMENT: {
						c = r.read();
						if (c == '\n' || c == '\r' || c == -1) {
							state = State.START;
						} else {
							c = r.read();
						}
					}
						break;
					case IN_IDENT: {
						c = r.read();
						if (Character.isJavaIdentifierPart(c)) {
							s.append((char) c);
							c = r.read();
						} else if (c == '\n') {
							t = new Token(Kind.IDENT, s.toString(), line, column);
							state = State.START;
						} else if (c == 32) {
							String txt;
							txt = s.toString();
							t = new Token(Kind.IDENT, txt, line, column);
							state = State.START;
						} else {
							String txt;
							txt = s.toString();
							if (keyWords.contains(txt) || Bools.contains(txt)) {
								switch (txt) {
									case "CONST": {
										t = new Token(Kind.KW_CONST, txt, line, column);
										state = State.START;
									}
										break;
									case "VAR": {
										t = new Token(Kind.KW_VAR, txt, line, column);
										state = State.START;
									}
										break;
									case "PROCEDURE": {
										t = new Token(Kind.KW_PROCEDURE, txt, line, column);
										state = State.START;
									}
										break;
									case "CALL": {
										t = new Token(Kind.KW_CALL, txt, line, column);
										state = State.START;
									}
										break;
									case "BEGIN": {
										t = new Token(Kind.KW_BEGIN, txt, line, column);
										state = State.START;
									}
										break;
									case "END": {
										t = new Token(Kind.KW_END, txt, line, column);
										state = State.START;
									}
										break;
									case "IF": {
										t = new Token(Kind.KW_IF, txt, line, column);
										state = State.START;
									}
										break;
									case "THEN": {
										t = new Token(Kind.KW_THEN, txt, line, column);
										state = State.START;
									}
										break;
									case "WHILE": {
										t = new Token(Kind.KW_WHILE, txt, line, column);
										state = State.START;
									}
										break;
									case "DO": {
										t = new Token(Kind.KW_DO, txt, line, column);
										state = State.START;
									}
										break;
									case "TRUE": {
										t = new Token(Kind.BOOLEAN_LIT, txt, line, column);
										state = State.START;
									}
										break;
									case "FALSE": {
										t = new Token(Kind.BOOLEAN_LIT, txt, line, column);
										state = State.START;
									}
										break;
									default: {
										t = new Token(Kind.IDENT, txt, line, column);
										state = State.START;
									}
										break;
								}
							} else {
								throw new LexicalException("Invalid character for Identifier");
							}
						}
					}
						break;
					case IN_NUM: {
						c = r.read();
						if (Character.isDigit(c)) {
							s.append((char) c);
						} else {
							String txt;
							txt = s.toString();
							try {
								Integer.parseInt(txt);
								t = new Token(Kind.NUM_LIT, txt, line, column);
								state = State.START;
							} catch (NumberFormatException ex) {
								throw new LexicalException("The number is too big for integer literal");
							}
						}
					}
						break;
					default: {
						if (Character.isJavaIdentifierStart(c)) {
							state = State.IN_IDENT;
							s = new StringBuilder();
							s.append((char) c);
						} else if (Character.isDigit(c)) {
							state = State.IN_NUM;
							s = new StringBuilder();

						} else {
							throw new LexicalException("Error while peeking, state: " + state + ", char: " + (char) c);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		try {
			r.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t;

	}

}
