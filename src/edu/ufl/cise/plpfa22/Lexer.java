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
		HAVE_MINUS, 
		IN_STRINGLIT,
		IN_COMMENT
	}

	void updateLocation() throws IOException {
		System.out.println("Char inside ul: "+(char)ch);
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
			System.out.println("State: "+state);
			System.out.println("Character "+(char)ch);
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
							case 42:{
								t = new Token(Kind.TIMES,"*",line,column);
								updateLocation();
							}
							break;

							case 34: {
								state = State.IN_STRINGLIT;
								st = new StringBuilder();
								// st.append((char) 34);
								updateLocation();
							}
							break;
							case 47: {
								state = State.IN_COMMENT;
								updateLocation();
							}
							break;
							default:
							{
								if (ch>=40 && ch<=90 || ch>=97 && ch<=122 || ch==95 || ch==36){
									state = State.IN_IDENT;
									st = new StringBuilder();
									st.append((char)ch);
									updateLocation();
								}
							}
						}
					
					}
					break;
					case IN_STRINGLIT: {
						System.out.println("Current char:"+(char)ch);
						System.out.println("In case string lit");
						if (ch<0 || ch>127) {
							throw new LexicalException("Invalid character found in string literal");

						}
						else{
							if(ch == 34){
								// st.append((char)34);
								// String str = st.toString();
								t = new Token(Kind.STRING_LIT, st.toString(),line,column);
								state = State.START;
								updateLocation();
							}
							else if(ch == 92){
								updateLocation();
								switch(ch) {
									case 'b':{
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
										st.append("\'");
										updateLocation();
										break;
									}


									default: {
										throw new LexicalException("Could not match escape character after backslash");
									}
									

								}
							}
							else{
								st.append((char)ch);
								updateLocation();
							}
						}
						
					}
					break;
					case IN_COMMENT:{
						if (ch=='\n'||ch=='\r'||ch==-1){
							state = State.START;
							updateLocation();
						}
						else{
							updateLocation();
						}
					}
					break;
					case IN_IDENT:{
						//To Do
						break;
					}
					default:
						throw new LexicalException("case not handled by lexer for " + (char)ch);
					
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
