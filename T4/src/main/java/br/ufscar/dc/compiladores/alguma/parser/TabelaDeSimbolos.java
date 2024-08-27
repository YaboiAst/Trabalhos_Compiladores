package br.ufscar.dc.compiladores.alguma.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TabelaDeSimbolos {
    public enum TipoAlguma{
        INVALIDO,
        INTEIRO,
        REAL,

        CADEIA,
        LOGICO,

        REGISTRO,
        VOID
    }

    public enum StructAlguma{
        VAR,
        CONST,
        PROC,
        FUNC,
        TIPO
    }

    public class EntradaTabelaDeSimbolos{
        String nome;
        TipoAlguma tipo;
        StructAlguma struct;

        public EntradaTabelaDeSimbolos(String nome, TipoAlguma tipo, StructAlguma struct){
            this.nome = nome;
            this.tipo = tipo;
            this.struct = struct;
        }
    }

    public TipoAlguma tipo;
    private final Map<String, EntradaTabelaDeSimbolos> tabela;
    private final Map<String, ArrayList<EntradaTabelaDeSimbolos>> tabelaTipo;

    public TabelaDeSimbolos(TipoAlguma tipo) {
        this.tabela = new HashMap<>();
        this.tabelaTipo = new HashMap<>();
        this.tipo = tipo;
    }

    public ArrayList<EntradaTabelaDeSimbolos> getTipoTabela(String tipo){
        return tabelaTipo.get(tipo);
    }

    public void adicionar(EntradaTabelaDeSimbolos input){
        tabela.put(input.nome, input);

    }

    public void adicionar(String tipo, EntradaTabelaDeSimbolos input){
        if (tabelaTipo.containsKey(tipo)) tabelaTipo.get(tipo).add(input);
        else{
            ArrayList<EntradaTabelaDeSimbolos> novoTipo = new ArrayList<>();
            novoTipo.add(input);
            tabelaTipo.put(tipo, novoTipo);
        }
    }

    public void adicionar(String nome, TipoAlguma tipo, StructAlguma struct) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo, struct)); // Adicionado struct
    }

    public boolean existe(String nome){
        return  tabela.containsKey(nome);
    }

    public TipoAlguma verificar(String nome){
        return tabela.get(nome).tipo;
    }
}
