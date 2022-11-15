package edu.ufl.cise.plpfa22;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName;
	final String classDesc;

	ClassWriter classWriter;
	// public static final String stringUtilClass =
	// "edu/ufl/cise/plpfa22/StringUtil";

	public CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc = "L" + this.fullyQualifiedClassName + ';';
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		methodVisitor.visitCode();
		for (ConstDec constDec : block.constDecs) {
			constDec.visit(this, null);
		}
		for (VarDec varDec : block.varDecs) {
			varDec.visit(this, methodVisitor);
		}
		for (ProcDec procDec : block.procedureDecs) {
			procDec.visit(this, null);
		}
		// add instructions from statement to method
		block.statement.visit(this, arg);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();
		return null;

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		// create a classWriter and visit it
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// Hint: if you get failures in the visitMaxs, try creating a ClassWriter with 0
		// instead of ClassWriter.COMPUTE_FRAMES. The result will not be a valid
		// classfile,
		// but you will be able to print it so you can see the instructions. After
		// fixing,
		// restore ClassWriter.COMPUTE_FRAMES
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", null);

		// get a method visitor for the main method.
		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		// visit the block, passing it the methodVisitor
		program.block.visit(this, methodVisitor);
		// finish up the class
		classWriter.visitEnd();
		// return the bytes making up the classfile
		return classWriter.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		statementOutput.expression.visit(this, arg);
		Type etype = statementOutput.expression.getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		String printlnSig = "(" + JVMType + ")V";
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSig, false);
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		for (Statement statement : statementBlock.statements) {
			statement.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		statementIf.expression.visit(this, arg);
		Label expressionFalseBranch = new Label();
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(IF_ICMPNE, expressionFalseBranch);
		statementIf.statement.visit(this, arg);
		mv.visitLabel(expressionFalseBranch);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Type argType = expressionBinary.e0.getType();
		Kind op = expressionBinary.op.getKind();
		switch (argType) {
			case NUMBER -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> mv.visitInsn(IADD);
					case MINUS -> mv.visitInsn(ISUB);
					case TIMES -> mv.visitInsn(IMUL);
					case DIV -> mv.visitInsn(IDIV);
					case MOD -> mv.visitInsn(IREM);
					case EQ -> {
						Label labelNumEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumEq);
						mv.visitLabel(labelNumEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumEq);
					}
					case NEQ -> {
						// throw new UnsupportedOperationException();
						Label labelNumEqTrueBr = new Label();
						mv.visitJumpInsn(IF_ICMPEQ, labelNumEqTrueBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumNeq = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumNeq);
						mv.visitLabel(labelNumEqTrueBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumNeq);
					}
					case LT -> {
						// throw new UnsupportedOperationException();
						Label labelNumLTFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGE, labelNumLTFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumLT = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumLT);
						mv.visitLabel(labelNumLTFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumLT);
					}
					case LE -> {
						// throw new UnsupportedOperationException();
						Label labelNumLEFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGT, labelNumLEFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumLE = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumLE);
						mv.visitLabel(labelNumLEFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumLE);
					}
					case GT -> {
						// throw new UnsupportedOperationException();
						Label labelNumGTFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLE, labelNumGTFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumGT = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumGT);
						mv.visitLabel(labelNumGTFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumGT);
					}
					case GE -> {
						// throw new UnsupportedOperationException();
						Label labelNumGEFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLT, labelNumGEFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostNumGE = new Label();
						mv.visitJumpInsn(GOTO, labelPostNumGE);
						mv.visitLabel(labelNumGEFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostNumGE);
					}
					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
					}
				}
				;
			}
			case BOOLEAN -> {
				// throw new UnsupportedOperationException();
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case EQ -> {
						Label labelBoolEqFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPNE, labelBoolEqFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostBoolEq = new Label();
						mv.visitJumpInsn(GOTO, labelPostBoolEq);
						mv.visitLabel(labelBoolEqFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostBoolEq);
					}
					case NEQ -> {
						Label labelBoolEqTrueBr = new Label();
						mv.visitJumpInsn(IF_ICMPEQ, labelBoolEqTrueBr);
						mv.visitInsn(ICONST_1);
						Label labelPostBoolNeq = new Label();
						mv.visitJumpInsn(GOTO, labelPostBoolNeq);
						mv.visitLabel(labelBoolEqTrueBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostBoolNeq);
					}
					case LT -> {
						// throw new UnsupportedOperationException();
						Label labelBoolLTFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGE, labelBoolLTFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostBoolLT = new Label();
						mv.visitJumpInsn(GOTO, labelPostBoolLT);
						mv.visitLabel(labelBoolLTFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostBoolLT);
					}
					case LE -> {
						// throw new UnsupportedOperationException();
						Label labelBoolLEFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPGT, labelBoolLEFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostBoolLE = new Label();
						mv.visitJumpInsn(GOTO, labelPostBoolLE);
						mv.visitLabel(labelBoolLEFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostBoolLE);
					}
					case GT -> {
						// throw new UnsupportedOperationException();
						Label labelBoolGTFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLE, labelBoolGTFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostBoolGT = new Label();
						mv.visitJumpInsn(GOTO, labelPostBoolGT);
						mv.visitLabel(labelBoolGTFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostBoolGT);
					}
					case GE -> {
						// throw new UnsupportedOperationException();
						Label labelBoolGEFalseBr = new Label();
						mv.visitJumpInsn(IF_ICMPLT, labelBoolGEFalseBr);
						mv.visitInsn(ICONST_1);
						Label labelPostBoolGE = new Label();
						mv.visitJumpInsn(GOTO, labelPostBoolGE);
						mv.visitLabel(labelBoolGEFalseBr);
						mv.visitInsn(ICONST_0);
						mv.visitLabel(labelPostBoolGE);
					}
					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary BOOLEAN");
					}
				}
			}
			case STRING -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
								"(Ljava/lang/String;)Ljava/lang/String;", false);
					}
					case EQ -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
					}
					case NEQ -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
						mv.visitMethodInsn(INVOKESTATIC, "edu/ufl/cise/plpfa22/StringUtil", "not", "(Z)Z", false);
					}
					case LT -> {
						mv.visitMethodInsn(INVOKESTATIC, "edu/ufl/cise/plpfa22/StringUtil", "lessThanString",
								"(Ljava/lang/String;Ljava/lang/String;)Z", false);
					}
					case LE -> {
						mv.visitMethodInsn(INVOKESTATIC, "edu/ufl/cise/plpfa22/StringUtil", "lesserAndEqualString",
								"(Ljava/lang/String;Ljava/lang/String;)Z", false);
					}
					case GT -> {
						mv.visitMethodInsn(INVOKESTATIC, "edu/ufl/cise/plpfa22/StringUtil", "greaterThanString",
								"(Ljava/lang/String;Ljava/lang/String;)Z", false);
					}
					case GE -> {
						mv.visitMethodInsn(INVOKESTATIC, "edu/ufl/cise/plpfa22/StringUtil", "greaterAndEqualString",
								"(Ljava/lang/String;Ljava/lang/String;)Z", false);
					}
					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary STRING");
					}
				}

			}
			default -> {
				throw new IllegalStateException("code gen bug in visitExpressionBinary");
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

}