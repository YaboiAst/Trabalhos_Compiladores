package br.ufscar.dc.compiladores.alguma.parser;

import br.ufscar.dc.compiladores.expr.parser.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.expr.parser.AlgumaParser;

public class AlgumaSemantico extends AlgumaBaseVisitor {

    Escopo escopo = new Escopo();

    @Override
    public Object visitPrograma(AlgumaParser.ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo(); // Pegar o peek
        if(tabela.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, ctx.IDENT().getText()+ " ja declarado anteriormente"); // Erro
        }
        else {
            tabela.adicionar(ctx.IDENT().getText(), TabelaDeSimbolos.TipoAlguma.TIPO); // Colocar na tabela o tipo
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

                switch (ctx.variavel().tipo().getText()){
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
                    default:
                        break;
                }

                tabela.adicionar(ident.getText(), tipo);
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Object visitIdentificador(AlgumaParser.IdentificadorContext ctx) {
        for (TabelaDeSimbolos escopos : escopo.getPilhaTabela()){
            if(!escopos.existe(ctx.IDENT(0).getText())){
                AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + ctx.IDENT(0).getText() + " nao declarado");
            }
        }

        return super.visitIdentificador(ctx);
    }

    @Override
    public Object visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.verificarTipo(escopo, ctx.expressao());

        boolean erro = false;
        String nomeVar = ctx.identificador().getText();

        if (tipo != TabelaDeSimbolos.TipoAlguma.INVALIDO){
            for (var escopos : escopo.getPilhaTabela()){
                if (escopos.existe(nomeVar)){
                    TabelaDeSimbolos.TipoAlguma tipoVar = AlgumaSemanticoUtils.verificarTipo(escopo, nomeVar);

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
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuição não compatível para " + nomeVar);
        }

        return super.visitCmdAtribuicao(ctx);
    }
}
