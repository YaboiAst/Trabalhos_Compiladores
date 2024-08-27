package br.ufscar.dc.compiladores.alguma.parser;

import java.util.LinkedList;
import java.util.List;

public class Escopo {
    private LinkedList<TabelaDeSimbolos> pilhaTabela;

    public Escopo(){
        pilhaTabela = new LinkedList<>();
        novoEscopo();
    }

    public void novoEscopo() {
        pilhaTabela.push(new TabelaDeSimbolos());
    }

    public TabelaDeSimbolos getEscopo(){
            return  pilhaTabela.peek();
    }

    public void delEscopo(){
        pilhaTabela.pop();
    }

    public List<TabelaDeSimbolos> getPilhaTabela(){
        return pilhaTabela;
    }
}
