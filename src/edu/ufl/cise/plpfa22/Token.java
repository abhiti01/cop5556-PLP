package edu.ufl.cise.plpfa22;

public class Token implements IToken {

	public final Kind kind;
	public final String text;
	public final int line;
	public final int column;

	public Token(Kind kind, String text, int line, int column) {
		super();
		this.text = text;
		this.kind = kind;
		this.line = line;
		this.column = column;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public char[] getText() {
		String results = unEscapeString(text);
		return results.toCharArray();

	}

	public static String unEscapeString(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			switch (s.charAt(i)) {
				case '\n':
					sb.append("\\n");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\"': {
					if (i == 0 || i == s.length() - 1) {
						sb.append("\"");
					} else {
						sb.append("\\\"");
					}
				}
					break;
				case '\'': {
					if (i == 0 || i == s.length() - 1) {
						sb.append("\'");
					} else {
						sb.append("\\\'");
					}
				}
					break;
				case '\\':
					sb.append("\\\\");
					break;

				default:
					sb.append(s.charAt(i));
			}
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		SourceLocation sl = new SourceLocation(line, column);
		return sl;
	}

	@Override
	public int getIntValue() {
		if (kind == Kind.NUM_LIT) {
			return Integer.parseInt(text);
		}
		return 0;
	}

	@Override
	public boolean getBooleanValue() {
		if (kind == Kind.BOOLEAN_LIT) {
			return Boolean.parseBoolean(text);
		}
		return false;

	}

	@Override
	public String getStringValue() {
		if (kind == Kind.STRING_LIT) {
			return text.substring(1, text.length() - 1);
		}
		return "";
	}

}
