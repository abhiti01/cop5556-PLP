package edu.ufl.cise.plpfa22;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.util.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
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
	int visitingProcedure;
	List<GenClass> genClassInstances = new ArrayList<GenClass>();
	String currentProcLocation = "";
	ClassWriter classWriter;

	public String getParent(String currentProcLocString) {
		// substring currentProcLocation till before last '$' and return
		String parent = currentProcLocString.substring(0, currentProcLocString.lastIndexOf('$'));
		// if length of string parent is 0, then return empty string
		if (parent.length() == 0) {
			return "";
		}
		return currentProcLocation.substring(0, currentProcLocation.lastIndexOf("$"));
	}

	public CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc = "L" + this.fullyQualifiedClassName + ';';
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
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		classWriter.visitSource(className + ".java", null);
		// Proc Visit
		visitingProcedure = 1;
		// visit nest member, visit innerclass
		// classWriter.visitNestMember(fullyQualifiedClassName+"$p1");
		// classWriter.visitInnerClass(fullyQualifiedClassName+"$p1",
		// fullyQualifiedClassName, "p1", 0);
		program.block.visit(this, classWriter);

		visitingProcedure = 0;
		// creating method visitor
		MethodVisitor methodVisitor;
		methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(1, 1);
		methodVisitor.visitEnd();
		// get a method visitor for the main method.
		methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		methodVisitor.visitCode();
		methodVisitor.visitTypeInsn(NEW, fullyQualifiedClassName);
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, fullyQualifiedClassName, "<init>", "()V", false);
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName, "run", "()V", false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(2, 1);
		methodVisitor.visitEnd();
		program.block.visit(this, classWriter);
		classWriter.visitEnd();

		genClassInstances.add(0, new CodeGenUtils.GenClass(fullyQualifiedClassName, classWriter.toByteArray()));

		return genClassInstances;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {

		if (visitingProcedure == 1) {
			ClassWriter classWriter = (ClassWriter) arg;
			for (ProcDec procDec : block.procedureDecs) {
				procDec.visit(this, arg);
			}
		} else {

			ClassWriter classWriter = (ClassWriter) arg;
			for (VarDec varDec : block.varDecs) {
				varDec.visit(this, classWriter);
			}
			for (ProcDec procDec : block.procedureDecs) {
				procDec.visit(this, classWriter);
			}
			MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
			methodVisitor.visitCode();
			// add instructions from statement to method
			block.statement.visit(this, methodVisitor);
			methodVisitor.visitInsn(RETURN);
			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}

		return null;

	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		if (visitingProcedure == 1) {
			ClassWriter classWriter = (ClassWriter) arg;
			classWriter.visitNestMember(fullyQualifiedClassName + procDec.getProcLocation());
			classWriter.visitInnerClass(fullyQualifiedClassName + procDec.getProcLocation(), fullyQualifiedClassName,
					String.valueOf(procDec.ident.getKind()), 0);
			procDec.block.visit(this, arg);
		} else {
			ClassWriter classWriterProc = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			classWriterProc.visit(V18, ACC_PUBLIC | ACC_SUPER,
					fullyQualifiedClassName + procDec.getProcLocation(), null,
					"java/lang/Object",
					new String[] { "java/lang/Runnable" });
			classWriterProc.visitSource(className + ".java", null);
			classWriterProc.visitNestHost(fullyQualifiedClassName);
			classWriterProc.visitInnerClass(
					fullyQualifiedClassName + procDec.getProcLocation(),
					fullyQualifiedClassName + procDec.getParentProcLocation(),
					String.valueOf(procDec.ident.getText()), 0);
			FieldVisitor fieldVisitorProc = classWriterProc.visitField(ACC_FINAL |
					ACC_SYNTHETIC,
					"this$" + procDec.getNest(),
					"L" + fullyQualifiedClassName + procDec.getParentProcLocation() + ";", null,
					null);
			fieldVisitorProc.visitEnd();
			MethodVisitor methodVisitorProc = classWriterProc.visitMethod(0, "<init>",
					"(L" + fullyQualifiedClassName + procDec.getParentProcLocation() + ";)V",
					null, null);
			methodVisitorProc.visitCode();
			methodVisitorProc.visitVarInsn(ALOAD, 0);
			methodVisitorProc.visitVarInsn(ALOAD, 1);
			methodVisitorProc.visitFieldInsn(PUTFIELD,
					fullyQualifiedClassName + procDec.getProcLocation(), "this$" +
							procDec.getNest(),
					"L" + fullyQualifiedClassName + procDec.getParentProcLocation() + ";");

			methodVisitorProc.visitVarInsn(ALOAD, 0);
			methodVisitorProc.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
					"<init>", "()V", false);
			methodVisitorProc.visitInsn(RETURN);
			methodVisitorProc.visitMaxs(2, 2);
			methodVisitorProc.visitEnd();

			String tempCurrentProcLocation = currentProcLocation;
			currentProcLocation = procDec.getProcLocation();
			procDec.block.visit(this, classWriterProc);
			currentProcLocation = tempCurrentProcLocation;
			classWriterProc.visitEnd();
			genClassInstances
					.add(new CodeGenUtils.GenClass(fullyQualifiedClassName +
							procDec.getProcLocation(),
							classWriterProc.toByteArray()));
		}
		return null;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		String ident = String.valueOf(constDec.ident.getText());
		String descriptor = (constDec.getType().equals(Type.NUMBER) ? "I"
				: (constDec.getType().equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		FieldVisitor fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_STATIC |
				ACC_FINAL, ident, descriptor, null, null);
		fieldVisitor.visitEnd();

		if (descriptor.equals("Z") || descriptor.equals("I")) {
			mv.visitInsn(ICONST_0);
		} else if (descriptor.equals("Ljava/lang/String;")) {
			mv.visitLdcInsn((String) constDec.val);
		}
		mv.visitFieldInsn(PUTSTATIC, className, ident, descriptor);
		mv.visitEnd();
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		// throw new UnsupportedOperationException();
		statementAssign.expression.visit(this, arg);
		statementAssign.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		// throw new UnsupportedOperationException();
		ClassWriter classWriter = (ClassWriter) arg;
		Type varDecType = varDec.getType();
		if (varDecType != null) {
			String ident = String.valueOf(varDec.ident.getText());
			String descriptor = (varDec.getType().equals(Type.NUMBER) ? "I"
					: (varDec.getType().equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
			FieldVisitor fieldVisitor = classWriter.visitField(0, ident, descriptor, null, null);
			fieldVisitor.visitEnd();
		}
		return null;
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		if ((fullyQualifiedClassName + currentProcLocation)
				.equals(fullyQualifiedClassName + statementCall.ident.getDec().getProcLocation())) {
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName + currentProcLocation,
					"run", "()V", false);
		} else {
			methodVisitor.visitTypeInsn(NEW, fullyQualifiedClassName + statementCall.ident.getDec().getProcLocation());
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ALOAD, 0);
			String currentPath = currentProcLocation;
			for (int i = statementCall.ident.getNest(); i > statementCall.ident.getDec().getNest(); i--) {
				String parentPath = getParent(currentPath);
				methodVisitor.visitFieldInsn(GETFIELD, fullyQualifiedClassName + currentPath,
						"this$" + String.valueOf(i - 1),
						"L" + fullyQualifiedClassName + parentPath + ";");
				currentPath = parentPath;
			}

			methodVisitor.visitMethodInsn(INVOKESPECIAL,
					fullyQualifiedClassName + statementCall.ident.getDec().getProcLocation(),
					"<init>", "(L" + fullyQualifiedClassName + currentPath + ";)V", false);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
					fullyQualifiedClassName + statementCall.ident.getDec().getProcLocation(),
					"run", "()V", false);

		}
		return null;
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
		MethodVisitor mv = (MethodVisitor) arg;
		Label expressionTrueBranch = new Label();
		Label expressionFalseBranch = new Label();
		Expression guardExp = statementWhile.expression;

		mv.visitLabel(expressionTrueBranch);
		guardExp.visit(this, arg);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(IF_ICMPLT, expressionFalseBranch);
		statementWhile.statement.visit(this, arg);
		mv.visitJumpInsn(GOTO, expressionTrueBranch);
		mv.visitLabel(expressionFalseBranch);
		return null;

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
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> mv.visitInsn(IADD);
					case TIMES -> mv.visitInsn(IMUL);
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
		// throw new UnsupportedOperationException();
		MethodVisitor mv = (MethodVisitor) arg;
		if (expressionIdent.getDec() instanceof ConstDec) {
			mv.visitLdcInsn(((ConstDec) expressionIdent.getDec()).val);
		} else {
			// add .this$n to descriptorfor non local variables
			mv.visitVarInsn(ALOAD, 0);
			String descriptor = (expressionIdent.getDec().getType().equals(Type.NUMBER) ? "I"
					: (expressionIdent.getDec().getType().equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
			// use nest level to go up the chain of this$n variables for non local variables
			// and getfield
			if (expressionIdent.getNest() != expressionIdent.getDec().getNest()) {
				String currentPath = currentProcLocation;
				for (int i = expressionIdent.getNest(); i > expressionIdent.getDec().getNest(); i--) {
					String parentPath = getParent(currentPath);
					mv.visitFieldInsn(GETFIELD, fullyQualifiedClassName + currentPath, "this$" + String.valueOf(i - 1),
							"L" + fullyQualifiedClassName + parentPath + ";");
					currentPath = parentPath;
				}
				mv.visitFieldInsn(GETFIELD, fullyQualifiedClassName + currentPath,
						String.valueOf(expressionIdent.firstToken.getText()),
						descriptor);
			} else {
				mv.visitFieldInsn(GETFIELD, fullyQualifiedClassName + currentProcLocation,
						String.valueOf(expressionIdent.firstToken.getText()),
						descriptor);
			}

		}
		return null;
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
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitVarInsn(ALOAD, 0);

		String descriptor = (ident.getDec().getType().equals(Type.NUMBER) ? "I"
				: (ident.getDec().getType().equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));

		// use nest level to go up the chain of this$n vars for non local variables and
		// putfield
		if (ident.getNest() != ident.getDec().getNest()) {
			String currentPath = currentProcLocation;
			String parentPath = "";
			for (int i = ident.getNest(); i > ident.getDec().getNest(); i--) {
				parentPath = getParent(currentPath);
				mv.visitFieldInsn(GETFIELD, fullyQualifiedClassName + currentPath, "this$" + String.valueOf(i - 1),
						"L" + fullyQualifiedClassName + parentPath + ";");
				currentPath = parentPath;
			}
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, fullyQualifiedClassName + parentPath,
					String.valueOf(ident.firstToken.getText()),
					descriptor);
		} else {
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, fullyQualifiedClassName + currentProcLocation,
					String.valueOf(ident.firstToken.getText()),
					descriptor);
		}

		return null;
	}

}
