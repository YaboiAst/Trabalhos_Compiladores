package br.ufscar.dc.compiladores.alguma.parser;

import java.util.LinkedList;
import java.util.List;

public class Escopo {
    private LinkedList<TabelaDeSimbolos> pilhaTabela;

    public Escopo(TabelaDeSimbolos.TipoAlguma tipo){
        pilhaTabela = new LinkedList<>();
        novoEscopo(tipo);
    }

    public Escopo(TabelaDeSimbolos simbolos)
    {
        pilhaTabela = new LinkedList<>();
        pilhaTabela.push(simbolos);
    }

    public void novoEscopo(TabelaDeSimbolos.TipoAlguma tipo) {
        pilhaTabela.push(new TabelaDeSimbolos(tipo));
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
