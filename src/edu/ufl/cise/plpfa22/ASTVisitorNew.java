package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;
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
import edu.ufl.cise.plpfa22.ast.Statement;
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
    int nestLevel = 0;

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        if (block != null) {
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
                for (int i = 0; i < block.procedureDecs.size(); i++) {
                    visitProcedure(block.procedureDecs.get(i), "one");
                }
                for (int i = 0; i < block.procedureDecs.size(); i++) {
                    visitProcedure(block.procedureDecs.get(i), "two");

                }
            }
            visitStatement(block.statement, null);

        } else {
            throw new ScopeException("block doesn't exist");
        }
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        symboltable.enterScope();
        if (program != null) {
            // visitBlock(program.block, "prog");
            visitBlock(program.block, arg);
        }
        symboltable.exitScope();

        return program;
    }

    public Object visitStatement(Statement s, Object arg) throws PLPException {
        if (s != null) {
            if (s instanceof StatementAssign) {
                visitStatementAssign((StatementAssign) s, arg);
            } else if (s instanceof StatementInput) {
                visitStatementInput((StatementInput) s, arg);
            } else if (s instanceof StatementOutput) {
                visitStatementOutput((StatementOutput) s, arg);
            } else if (s instanceof StatementBlock) {
                visitStatementBlock((StatementBlock) s, arg);
            } else if (s instanceof StatementIf) {
                visitStatementIf((StatementIf) s, arg);
            } else if (s instanceof StatementCall) {
                visitStatementCall((StatementCall) s, arg);
            } else if (s instanceof StatementWhile) {
                visitStatementWhile((StatementWhile) s, arg);
            } else {
                visitStatementEmpty((StatementEmpty) s, null);
            }
        }
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        visitIdent((Ident) statementAssign.ident, null);
        if (statementAssign.expression instanceof ExpressionIdent) {
            visitExpressionIdent((ExpressionIdent) statementAssign.expression, null);
        } else if (statementAssign.expression instanceof ExpressionBinary) {
            visitExpressionBinary((ExpressionBinary) statementAssign.expression, null);
        }
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        if (varDec != null) {
            varDec.setNest(nestLevel);
            return visitIdent(new Ident(varDec.ident), varDec);
        }
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        visitIdent((Ident) statementCall.ident, null);
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        visitIdent((Ident) statementInput.ident, null);
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        if (statementOutput.expression instanceof ExpressionIdent) {
            visitExpressionIdent((ExpressionIdent) statementOutput.expression, null);
        }
        if (statementOutput.expression instanceof ExpressionBinary) {
            visitExpressionBinary((ExpressionBinary) statementOutput.expression, arg);
        }
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        if (statementBlock.statements.size() != 0) {
            for (int i = 0; i < statementBlock.statements.size(); i++) {
                visitStatement((Statement) statementBlock.statements.get(i), null);
            }
        }
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        if (statementIf.expression instanceof ExpressionIdent) {
            visitExpressionIdent((ExpressionIdent) statementIf.expression, null);
        } else if (statementIf.expression instanceof ExpressionBinary) {
            visitExpressionBinary((ExpressionBinary) statementIf.expression, arg);
        }
        visitStatement((Statement) statementIf.statement, null);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        if (statementWhile.expression instanceof ExpressionIdent) {
            visitExpressionIdent((ExpressionIdent) statementWhile.expression, null);
        } else if (statementWhile.expression instanceof ExpressionBinary) {
            visitExpressionBinary((ExpressionBinary) statementWhile.expression, arg);
        }
        visitStatement((Statement) statementWhile.statement, null);
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        if (expressionBinary.e0 instanceof ExpressionIdent) {
            visitExpressionIdent((ExpressionIdent) expressionBinary.e0, null);
        } else if (expressionBinary.e0 instanceof ExpressionBinary) {
            visitExpressionBinary((ExpressionBinary) expressionBinary.e0, null);
        }
        if (expressionBinary.e1 instanceof ExpressionBinary) {
            visitExpressionBinary((ExpressionBinary) expressionBinary.e1, null);
        } else if (expressionBinary.e1 instanceof ExpressionIdent) {
            visitExpressionIdent((ExpressionIdent) expressionBinary.e1, null);
        }

        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        Token tempToken = (Token) expressionIdent.getFirstToken();
        if (symboltable.lookup(tempToken.getString()) != null) {
            expressionIdent.setDec(symboltable.lookup(tempToken.getString()));
            expressionIdent.setNest(nestLevel);
        } else {
            throw new ScopeException("Ident used before declaration" + tempToken.getString());
        }
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        if (expressionNumLit != null && expressionNumLit.getFirstToken().getKind() == Kind.NUM_LIT) {
            return expressionNumLit.getFirstToken().getIntValue();
        }
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        if (expressionStringLit != null && expressionStringLit.getFirstToken().getKind() == Kind.STRING_LIT) {
            return expressionStringLit.getFirstToken().getStringValue();
        }
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        if (expressionBooleanLit != null && expressionBooleanLit.getFirstToken().getKind() == Kind.BOOLEAN_LIT) {
            return expressionBooleanLit.getFirstToken().getBooleanValue();
        }
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        if (arg == "one") {
            visitIdent(new Ident(procDec.ident), procDec);
            procDec.setNest(nestLevel);
        } else {
            symboltable.enterScope();
            nestLevel++;
            visitBlock(procDec.block, null);
            symboltable.exitScope();
            nestLevel--;
        }
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        if (constDec != null) {
            constDec.setNest(nestLevel);
            return visitIdent(new Ident(constDec.ident), constDec);
        }
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        Token tempToken = (Token) ident.getFirstToken();

        if (arg == null) {
            if (symboltable.lookup(tempToken.getString()) == null) {
                throw new ScopeException("Ident used before declaration" + tempToken.getString());
            } else {
                Declaration gotDec = symboltable.lookup(tempToken.getString());
                ident.setDec(gotDec);
                ident.setNest(nestLevel);
            }
        } else {
            if (symboltable.existsInScope(tempToken.getString())) {
                throw new ScopeException("Already exists in scope");
            }
            ident.setNest(nestLevel);
            ident.setDec((Declaration) arg);
            symboltable.pushDeclaration(tempToken.getString(), ident.getDec());
            // symboltable.pushDeclaration(tempToken.getString(),
            // ident.getDec(),symboltable.getScopeId());
        }

        return null;
    }
}
