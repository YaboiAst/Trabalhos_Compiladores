package br.ufscar.dc.compiladores.alguma.parser;

import br.ufscar.dc.compiladores.expr.parser.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.expr.parser.AlgumaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AlgumaSemantico extends AlgumaBaseVisitor {

    Escopo escopo = new Escopo();

    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo();
        if(tabela.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, ctx.IDENT().getText()+ " ja declarado anteriormente");
        }
        else {
            tabela.adicionar(ctx.IDENT().getText(), TabelaDeSimbolos.TipoAlguma.TIPO);
        }

        return super.visitDeclaracao_global(ctx);
    }

    @Override
    public Object visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo();

        for (AlgumaParser.IdentificadorContext ident : ctx.variavel().identificador()){
            if (tabela.existe(ident.getText())) {
                AlgumaSemanticoUtils.adicionarErroSemantico(ident.start, "identificador " + ident.getText() + " já declarado anteriormente");
            }
            else {
                TabelaDeSimbolos.TipoAlguma tipo = TabelaDeSimbolos.TipoAlguma.INVALIDO;
                AlgumaParser.TipoContext tipoVariavel = ctx.variavel().tipo();

                if (tipoVariavel.registro() != null){
                    tipo = TabelaDeSimbolos.TipoAlguma.REGISTRO;
                    for(AlgumaParser.VariavelContext regVariavel : tipoVariavel.registro().variavel()){
                        for (AlgumaParser.IdentificadorContext regIdent : regVariavel.identificador()){
                            if (tabela.existe(regIdent.getText())){
                                AlgumaSemanticoUtils.adicionarErroSemantico(regIdent.start, "identificador " + regIdent.getText() + " já declarado anteriormente");
                            }
                            else {
                                TabelaDeSimbolos.TipoAlguma regTipo = TabelaDeSimbolos.TipoAlguma.INVALIDO;

                                switch (ctx.tipo_basico().getText())
                                {
                                    case "literal":
                                        tipo = TabelaDeSimbolos.TipoAlguma.CADEIA;
                                        break;
                                    case "real":
                                        tipo = TabelaDeSimbolos.TipoAlguma.REAL;
                                        break;
                                    case "inteiro":
                                        tipo = TabelaDeSimbolos.TipoAlguma.INTEIRO;
                                        break;
                                    case "logico":
                                        tipo = TabelaDeSimbolos.TipoAlguma.LOGICO;
                                        break;
                                }

                                tabela.adicionar(ident.getText() + "." + regIdent.getText(), tipo);
                            }
                        }
                    }
                }
                else if (tipoVariavel.getText().startsWith("^")){
                    tipo = TabelaDeSimbolos.TipoAlguma.PONTEIRO;
                }
                else{
                    switch (ctx.tipo_basico().getText())
                    {
                        case "literal":
                            tipo = TabelaDeSimbolos.TipoAlguma.CADEIA;
                            break;
                        case "real":
                            tipo = TabelaDeSimbolos.TipoAlguma.REAL;
                            break;
                        case "inteiro":
                            tipo = TabelaDeSimbolos.TipoAlguma.INTEIRO;
                            break;
                        case "logico":
                            tipo = TabelaDeSimbolos.TipoAlguma.LOGICO;
                            break;
                    }
                }

                tabela.adicionar(ident.getText(), tipo);
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Object visitIdentificador(AlgumaParser.IdentificadorContext ctx) {
        StringBuilder identificador = new StringBuilder();
        for (TerminalNode ident : ctx.IDENT()){
            identificador.append(ident.getText()).append(".");
        }
        identificador.deleteCharAt(identificador.length() - 1);

        boolean hit = false;

        for (TabelaDeSimbolos escopos : escopo.getPilhaTabela()){
            if(escopos.existe(identificador.toString())){
                hit = true;
                break;
            }
        }

        if (!hit){
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + ctx.IDENT(0).getText() + " nao declarado");
        }

        return super.visitIdentificador(ctx);
    }

    @Override
    public Object visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        boolean erro = false;
        StringBuilder nomeVar = new StringBuilder();
        boolean isPointer = ctx.getText().charAt(0) == '^';

        for (TerminalNode ident : ctx.identificador().IDENT()){
            nomeVar.append(ident.getText()).append(".");
        }
        nomeVar.deleteCharAt(nomeVar.length() - 1);

        TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.verificarTipo(escopo, ctx.expressao());
        if (tipo != TabelaDeSimbolos.TipoAlguma.INVALIDO){
            for (var escopos : escopo.getPilhaTabela()){
                if (escopos.existe(nomeVar.toString())){
                    TabelaDeSimbolos.TipoAlguma tipoVar = AlgumaSemanticoUtils.verificarTipo(escopo, nomeVar.toString());

                    boolean expNumerico = tipo == TabelaDeSimbolos.TipoAlguma.INTEIRO || tipo == TabelaDeSimbolos.TipoAlguma.REAL;
                    boolean varNumerico = tipoVar == TabelaDeSimbolos.TipoAlguma.INTEIRO || tipoVar == TabelaDeSimbolos.TipoAlguma.REAL;

                    if (!(expNumerico && varNumerico) && tipo != tipoVar && tipo != TabelaDeSimbolos.TipoAlguma.INVALIDO){
                        erro = true;
                    }
                }
            }
        }
        else erro = true;

        if (erro) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuição não compatível para " + (isPointer ? "^" : "" ) + nomeVar);
        }

        return super.visitCmdAtribuicao(ctx);
    }
}
