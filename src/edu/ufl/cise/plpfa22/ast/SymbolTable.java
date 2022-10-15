package edu.ufl.cise.plpfa22.ast;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

public class SymbolTable {
    Stack<Integer> scopeStack = new Stack<Integer>();
    HashMap<String, ArrayList<SpecialStructureTBD>> hashing = new HashMap<String, ArrayList<SpecialStructureTBD>>();
    int currentScope;
    int nextScope;

    public SymbolTable() {
        this.scopeStack = new Stack<Integer>();
        this.hashing = new HashMap<String, ArrayList<SpecialStructureTBD>>();
        this.currentScope = 0;
        this.nextScope = 1;
        scopeStack.push(currentScope);
    }

    public int getCurrentScope() {
        return currentScope;
    }

    public void enterScope() {
        currentScope = nextScope++;
        scopeStack.push(currentScope);
    }

    public void exitScope() {
        scopeStack.pop();
        currentScope = scopeStack.peek();
    }

    public void pushDeclaration(String ident, Declaration dec) {
        if (hashing.containsKey(ident)) {
            ArrayList<SpecialStructureTBD> newDec = hashing.get(ident);
            newDec.add(new SpecialStructureTBD(currentScope, dec));
            hashing.put(ident, newDec);
        } else {
            ArrayList<SpecialStructureTBD> newDec = new ArrayList<>();
            newDec.add(new SpecialStructureTBD(currentScope, dec));
            hashing.put(ident, newDec);
        }
    }

    public Declaration lookup(String ident) {
        ArrayList<SpecialStructureTBD> l = hashing.get(ident);
        if (l == null) {
            return null;
        }

        Declaration dec = null;
        // int delta = Integer.MAX_VALUE;
        for (SpecialStructureTBD p : l) {
            if (p.getScope() <= currentScope && scopeStack.contains(p.getScope())) {
                // && currentScope - p.getDecVal() <= delta)
                dec = p.getDecVal();
                // delta = currentScope - p.getKey();
            }
        }
        return dec;
    }

    public boolean existsInScope(String ident) {
        ArrayList<SpecialStructureTBD> name = hashing.get(ident);
        if (name == null) {
            return false;
        }
        for (SpecialStructureTBD existing : name) {
            if (existing.getScope() == currentScope) {
                return true;
            }
        }
        return false;
    }

    class SpecialStructureTBD {
        int scope;
        Declaration dec;

        public SpecialStructureTBD(int scope, Declaration dec) {
            this.scope = scope;
            this.dec = dec;
        }

        public int getScope() {
            return scope;
        }

        public Declaration getDecVal() {
            return dec;
        }

    }
}
