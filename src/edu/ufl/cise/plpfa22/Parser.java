package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
public class Parser implements IParser {

    ILexer lexer;
    IToken token;

    public Parser (ILexer lexer) throws LexicalException{
        this.lexer = lexer;
        token = lexer.next();
    }

    @Override
    public ASTNode parse() throws PLPException {
        // TODO Auto-generated method stub
        Program program = program();
        //match end of file
        //return program
        return null;
    }

    private Program program(){
        Block block;
        List <ConstDec> constDList = new ArrayList<ConstDec>();
        List <VarDec> varDlList = new ArrayList<VarDec>();
        List <ProcDec> procDecs = new ArrayList<ProcDec>();
        IToken firstToken = token;
        while (isKind(Kind.KW_CONST) || isKind(Kind.KW_VAR) || isKind(Kind.KW_PROCEDURE)){
            switch (token.getKind()) {
				case KW_CONST: {
					constDList.add((placeholderMethod()));
				}break;
				
				case IDENT : {
					varDlList.add(varDeclaration());
				}break;
                case KW_PROCEDURE:{
                    ProcDec.add(procDeclaration());
                }
				//Your finished parser should NEVER throw UnsupportedOperationException, but it is convenient as a placeholder for unimplemented features.
				default:{throw new UnsupportedOperationException("unimplemented feature in program"); } 
			}
            //match semicolon here
        }
        return new Program(firstToken, declarationsAndStatements);
    }
    private boolean isKind(IToken token, Kind kind){
        return token.getKind() == kind;
    }

    
    private IToken consume() throws LexicalException{
        token = lexer.next();
        return token;
    }

    private IToken match (Kind kind) throws SyntaxException, LexicalException{
        if(isKind(token,kind)){
            return consume();
        }

        else{
            throw new SyntaxException(); 
        }
    }

    
}
