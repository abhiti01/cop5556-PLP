/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plpfa22.CompilerComponentFactory;
import edu.ufl.cise.plpfa22.ILexer;
import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.IToken.SourceLocation;
import edu.ufl.cise.plpfa22.LexicalException;

class LexerTest {

	/*** Useful functions ***/
	ILexer getLexer(String input) {
		return CompilerComponentFactory.getLexer(input);
	}

	// makes it easy to turn output on and off (and less typing than
	// System.out.println)
	static final boolean VERBOSE = true;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	// check that this token has the expected kind
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}

	// check that the token has the expected kind and position
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn) {
		assertEquals(expectedKind, t.getKind());
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token is an IDENT and has the expected name
	void checkIdent(IToken t, String expectedName) {
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, String.valueOf(t.getText()));
	}

	// check that this token is an IDENT, has the expected name, and has the
	// expected position
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn) {
		checkIdent(t, expectedName);
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token is an NUM_LIT with expected int value
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.NUM_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());
	}

	// check that this token is an NUM_LIT with expected int value and position
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t, expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}

	/*** Tests ****/

	// The lexer should add an EOF token to the end.
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		show(lexer);
		checkEOF(lexer.next());
	}

	// A couple of single character tokens
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+
				-
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1, 1);
		checkToken(lexer.next(), Kind.MINUS, 2, 1);
		checkEOF(lexer.next());
	}

	// comments should be skipped
	@Test
	void testComment0() throws LexicalException {
		// Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				// this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 1, 1);
		checkToken(lexer.next(), Kind.TIMES, 3, 1);
		checkEOF(lexer.next());
	}

	// Example for testing input with an illegal character
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		// this check should succeed
		checkIdent(lexer.next(), "abc");
		// this is expected to throw an exception since @ is not a legal
		// character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	// Several identifiers to test positions
	@Test
	public void testIdent0() throws LexicalException {
		String input = """
				abc
				  def
				     ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 1, 1);
		checkIdent(lexer.next(), "def", 2, 3);
		checkIdent(lexer.next(), "ghi", 3, 6);
		checkEOF(lexer.next());
	}

	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 1, 1);
		checkInt(lexer.next(), 456, 1, 6);
		checkIdent(lexer.next(), "b", 1, 9);
		checkEOF(lexer.next());
	}

	// Example showing how to handle number that are too big.
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "\"\\b \\t \\n \\f \\r \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = "\b \t \n \f \r ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\"\\b \\t \\n \\f \\r \"";
		assertEquals(expectedText, text);
	}

	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = " ...  \"  \'  \\  ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; // almost the same as input, but white space is omitted
		assertEquals(expectedText, text);
	}

	@Test
	public void ptest1() throws LexicalException {
		String input = """

				""";
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(kind, Kind.EOF);
		}
	}
	@Test
	public void test1() throws LexicalException {
		String input = """
				abc ()
				""";
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(kind, Kind.IDENT);
			SourceLocation sl= token.getSourceLocation();
			int line = sl.line();
			System.err.println("Line"+line);

			assertEquals(sl.line(), 1);
			int charPositionInLine = sl.column();
			System.err.println("Pos"+charPositionInLine);

			assertEquals(charPositionInLine, 1);
			char [] text = token.getText();
			String text2 = String.valueOf(text);
			System.out.println("text" +text2);
			assertEquals(text2, "abc");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(kind, Kind.IDENT);
			SourceLocation sl= token.getSourceLocation();
			int line = sl.line();
			assertEquals(line, 2);
			int charPositionInLine = sl.column();
			assertEquals(charPositionInLine, 2);
			char[] text = token.getText();
			assertEquals(text, "def");
			
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(kind, Kind.IDENT);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
			assertEquals(line, 3);
			int charPositionInLine = sl.column();
			assertEquals(charPositionInLine, 5);
			char[] text = token.getText();
			assertEquals(text, "ghi");
			
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(kind, Kind.EOF);
		}
	}

	@Test
	public void test2() throws LexicalException {
		String input = """
				a123 123a
				""";
//		System.out.println("New test");
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);

			assertEquals(kind, Kind.IDENT);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);

			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);

			assertEquals(charPositionInLine, 1);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals(text2, "a123");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("mixed1Kind"+kind);

			assertEquals(kind, Kind.NUM_LIT);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);

			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);

			assertEquals(charPositionInLine, 6);
			char [] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals(text2, "123");
			int val = token.getIntValue();
			assertEquals(val, 123);
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("mixed2Kind"+kind);

			assertEquals(kind, Kind.IDENT);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);

			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);

			assertEquals(charPositionInLine, 9);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals(text2, "a");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			System.err.println("Kind"+kind);

			assertEquals(kind, Kind.EOF);
		}
	}

	@Test
	public void test3() throws LexicalException {
		String input = """
				= := ===
				""";
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);
//			
			assertEquals(kind, Kind.ASSIGN);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);
//			
			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);
			
			assertEquals(charPositionInLine, 0);
			char[] text = token.getText();
			assertEquals(text, "=");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);
			
			assertEquals(kind, Kind.EQ);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);
			
			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);
			
			assertEquals(charPositionInLine, 2);
			char[] text = token.getText();			
			assertEquals(text, "==");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);
			
			assertEquals(kind, Kind.EQ);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);
			
			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);
			
			assertEquals(charPositionInLine, 5);
			char[] text = token.getText();
			assertEquals(text, "==");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);
			SourceLocation sl = token.getSourceLocation();
			assertEquals(kind, Kind.ASSIGN);
			int line = sl.line();
//			System.err.println("Line"+line);
			
			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);
			
			assertEquals(charPositionInLine, 7);
			char[] text = token.getText();			
			assertEquals(text, "=");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);
			
			assertEquals(kind, Kind.EOF);
		}
	}

	@Test
	public void test4() throws LexicalException {
		String input = """
				a %
				""";
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
//			System.err.println("Kind"+kind);
			
			assertEquals(kind, Kind.IDENT);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
//			System.err.println("Line"+line);
			
			assertEquals(line, 1);
			int charPositionInLine = sl.column();
//			System.err.println("Pos"+charPositionInLine);
			
			assertEquals(charPositionInLine, 1);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals(text2, "a");
		}
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	@Test
	public void test5() throws LexicalException {
		String input = """
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
		}
}
