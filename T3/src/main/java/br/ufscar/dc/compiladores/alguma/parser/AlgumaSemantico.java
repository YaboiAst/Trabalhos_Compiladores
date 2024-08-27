package br.ufscar.dc.compiladores.alguma.parser;

import br.ufscar.dc.compiladores.expr.parser.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.expr.parser.AlgumaParser;

public class AlgumaSemantico extends AlgumaBaseVisitor {

    Escopo escopo = new Escopo();

    // Raiz do programa
    @Override
    public Object visitPrograma(AlgumaParser.ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo();
        // IDENT já existe na tabela? Caso não, adicionar
        if(tabela.existe(ctx.IDENT().getText()))
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, ctx.IDENT().getText()+ " ja declarado anteriormente");
        }
        else
        {
            tabela.adicionar(ctx.IDENT().getText(), TabelaDeSimbolos.TipoAlguma.TIPO);
        }

        return super.visitDeclaracao_global(ctx);
    }

    @Override
    public Object visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo();

        if (ctx.getText().contains("declare")) // É variável
        {
            for (AlgumaParser.IdentificadorContext ident : ctx.variavel().identificador())
            {
                // A variável já existe? Se não, descobrir seu tipo e adiconar na tabela
                if(tabela.existe(ident.getText()))
                {
                    AlgumaSemanticoUtils.adicionarErroSemantico(ident.start, "identificador " + ident.getText()+ " ja declarado anteriormente");
                }
                else
                {
                    TabelaDeSimbolos.TipoAlguma tipo = TabelaDeSimbolos.TipoAlguma.INTEIRO;

                    switch (ctx.variavel().tipo().getText())
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

                        default:
                            break;
                    }

                    tabela.adicionar(ident.getText(), tipo); // Colocar na tabela o tipo
                }
            }
        }
        else if(ctx.getText().contains("constante")) // Constante
        {
            // Constante já existe? Se não, descobrir seu tipo adicionar na tabela
            if(tabela.existe(ctx.IDENT().getText()))
            {
                AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "constante" + ctx.IDENT().getText()+ " ja declarado anteriormente");
            }
            else {
                TabelaDeSimbolos.TipoAlguma tipo = TabelaDeSimbolos.TipoAlguma.INTEIRO;
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
                tabela.adicionar(ctx.IDENT().getText(), tipo);
            }
        }
        else // Tipo
        {
            // Tipo já existe? Caso não, adicionar na tabela
            if(tabela.existe(ctx.IDENT().getText()))
            {
                AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText()+ " declarado duas vezes num mesmo escopo");
            }
            else
            {
                tabela.adicionar(ctx.IDENT().getText(), TabelaDeSimbolos.TipoAlguma.TIPO);
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Object visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx){
        if(ctx.IDENT() != null)
        {
            // O tipo foi declarado em algum escopo válido?
            for(TabelaDeSimbolos escopos : escopo.getPilhaTabela()){
                if(!escopos.existe(ctx.IDENT().getText()))
                {
                    AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText()+ " nao declarado");
                }
            }
        }

        return super.visitTipo_basico_ident(ctx);
    }

    @Override
    public Object visitIdentificador(AlgumaParser.IdentificadorContext ctx) {
        for (TabelaDeSimbolos escopos : escopo.getPilhaTabela())
        {
            // O identificador foi declarado em algum escopo valido?
            if(!escopos.existe(ctx.IDENT(0).getText()))
            {
                AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + ctx.IDENT(0).getText() + " nao declarado");
            }
        }

        return super.visitIdentificador(ctx);
    }

    @Override
    public Object visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        String nomeVar = ctx.identificador().getText();
        TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.verificarTipo(escopo, ctx.expressao());

        boolean erro = false;
        if (tipo != TabelaDeSimbolos.TipoAlguma.INVALIDO) // Tipo da expressao existe?
        {
            for (var escopos : escopo.getPilhaTabela())
            {
                if (escopos.existe(nomeVar)) // Achou a variavel
                {
                    TabelaDeSimbolos.TipoAlguma tipoVar = AlgumaSemanticoUtils.verificarTipo(escopo, nomeVar);

                    boolean expNumerico = tipo == TabelaDeSimbolos.TipoAlguma.INTEIRO || tipo == TabelaDeSimbolos.TipoAlguma.REAL;
                    boolean varNumerico = tipoVar == TabelaDeSimbolos.TipoAlguma.INTEIRO || tipoVar == TabelaDeSimbolos.TipoAlguma.REAL;

                    if (!(expNumerico && varNumerico) && tipo != tipoVar) // Atribuição é inválida?
                    {
                        erro = true;
                    }
                }
            }
        }
        else erro = true;

        if (erro)
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + nomeVar);
        }

        return super.visitCmdAtribuicao(ctx);
    }
}
