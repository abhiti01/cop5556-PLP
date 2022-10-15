package edu.ufl.cise.plpfa22;

import org.hamcrest.core.IsInstanceOf;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.ast.SymbolTable;

public class ASTVisitorNew implements edu.ufl.cise.plpfa22.ast.ASTVisitor {

    SymbolTable symboltable = new SymbolTable();

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (block != null) {
            System.out.println("IIN BLOCK");
            if (block.constDecs.size() != 0) {
                for (int i = 0; i < block.constDecs.size(); i++) {
                    visitConstDec(block.constDecs.get(i), null);
                }
            }
            if (block.varDecs.size() != 0) {
                for (int i = 0; i < block.varDecs.size(); i++) {
                    visitVarDec(block.varDecs.get(i), null);
                }
            }
            if (block.procedureDecs.size() != 0) {
                for (int i = 0; i < block.varDecs.size(); i++) {
                    visitProcedure(block.procedureDecs.get(i), null);
                }
            }
            System.out.println("hio" + block);
            if (block.statement instanceof StatementOutput) {
                System.out.println("block statement not null" + block.statement);
                // System.out.println(block);
                // if (block.statement instanceof StatementOutput) {
                // System.out.println("inside if statement of statement output");
                // visitStatementOutput((StatementOutput) block.statement, arg);
                // }
            }

        } else {
            System.out.println("can");
        }
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        symboltable.enterScope();
        if (program != null) {
            visitBlock(program.block, null);
            // return program here?
        }
        symboltable.exitScope();

        return program;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (varDec != null) {
            return visitIdent(new Ident(varDec.ident), varDec);
        }
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        System.out.println("Got inside visit statement output" + statementOutput);
        if (statementOutput.expression instanceof ExpressionIdent) {
            System.out.println("Got inside if  visit statement output" + statementOutput);
            visitExpressionIdent((ExpressionIdent) statementOutput.expression, arg);
        }
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        System.out.println(expressionIdent.getFirstToken().getText().toString());
        if (symboltable.existsInScope(expressionIdent.getFirstToken().getText().toString())) {
            System.out.println(expressionIdent.getFirstToken().getText().toString());
            expressionIdent.setDec(symboltable.lookup(expressionIdent.getFirstToken().getText().toString()));
        }
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (expressionNumLit != null && expressionNumLit.getFirstToken().getKind() == Kind.NUM_LIT) {
            return expressionNumLit.getFirstToken().getIntValue();
        }
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (expressionStringLit != null && expressionStringLit.getFirstToken().getKind() == Kind.STRING_LIT) {
            return expressionStringLit.getFirstToken().getStringValue();
        }
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (expressionBooleanLit != null && expressionBooleanLit.getFirstToken().getKind() == Kind.BOOLEAN_LIT) {
            return expressionBooleanLit.getFirstToken().getBooleanValue();
        }
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        symboltable.enterScope();
        visitIdent(new Ident(procDec.ident), procDec);
        // symboltable.pushDeclaration(procDec.ident.getText().toString(), procDec);
        visitBlock(procDec.block, null);
        symboltable.exitScope();
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (constDec != null) {
            System.out.println("CONSTDEC IS NULL");
            return visitIdent(new Ident(constDec.ident), constDec);
        }
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        // Declaration dec = look up declaration of name in symbol table.
        // if dec is null, this identifier has not been declared in scope so throw an
        // exception
        if (arg == null) {
            System.out.println("ARG INSIDE VISIT IDENT IS NULL");
        }
        ident.setNest(symboltable.getCurrentScope());
        ident.setDec((Declaration) arg);
        if ((Declaration) arg == null) {
            System.out.println(" DECLARATION ARG VISIT IDENT IS NULL");
        }
        symboltable.pushDeclaration(ident.getText().toString(), ident.getDec());

        return null;
    }

    public Object visit(ASTVisitor v, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return visitProgram(null, arg);
    }

}
