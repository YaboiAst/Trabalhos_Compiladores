package br.ufscar.dc.compiladores.alguma.parser;

import java.util.ArrayList;
import java.util.List;

import br.ufscar.dc.compiladores.expr.parser.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.expr.parser.AlgumaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AlgumaSemantico extends AlgumaBaseVisitor {
    Escopo escopo = new Escopo(TabelaDeSimbolos.TipoAlguma.VOID);

    // Raiz do programa
    @Override
    public Object visitPrograma(AlgumaParser.ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

    @Override
    public Object visitDeclaracao_constante(AlgumaParser.Declaracao_constanteContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo();

        if(tabela.existe(ctx.IDENT().getText()))
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "constante" + ctx.IDENT().getText()+ " ja declarado anteriormente");
        }
        else
        {
            TabelaDeSimbolos.TipoAlguma tipo = TabelaDeSimbolos.TipoAlguma.INTEIRO;
            TabelaDeSimbolos.TipoAlguma aux = AlgumaSemanticoUtils.getTipo(ctx.tipo_basico().getText()) ;
            if(aux != null) tipo = aux;

            // Adicionar na tabela constante declarada
            tabela.adicionar(ctx.IDENT().getText(), tipo, TabelaDeSimbolos.StructAlguma.CONST);
        }

        return super.visitDeclaracao_constante(ctx);
    }

    @Override
    public Object visitDeclaracao_tipo(AlgumaParser.Declaracao_tipoContext ctx){
        TabelaDeSimbolos tabela = escopo.getEscopo();

        if(tabela.existe(ctx.IDENT().getText()))
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText()+ " declarado duas vezes num mesmo escopo");
        }
        else
        {
            TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo().getText()) ;

            if(tipo != null) // Adicionar na tabela tipo declarado
            {
                tabela.adicionar(ctx.IDENT().getText(), tipo, TabelaDeSimbolos.StructAlguma.TIPO);
            }
            else if(ctx.tipo().registro() != null)
            {
                ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> lista = new ArrayList<>();

                for(AlgumaParser.VariavelContext va : ctx.tipo().registro().variavel())
                {
                    TabelaDeSimbolos.TipoAlguma tipoReg = AlgumaSemanticoUtils.getTipo(va.tipo().getText()) ; // Pegar o tipo
                    for(AlgumaParser.IdentificadorContext id : va.identificador())
                    {
                        lista.add(tabela.new EntradaTabelaDeSimbolos(id.getText(), tipoReg, TabelaDeSimbolos.StructAlguma.TIPO));
                    }
                }

                if(tabela.existe(ctx.IDENT().getText()))
                {
                    AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + ctx.IDENT().getText() + " ja declarado anteriormente");
                }
                else
                {
                    tabela.adicionar(ctx.IDENT().getText(), TabelaDeSimbolos.TipoAlguma.REGISTRO, TabelaDeSimbolos.StructAlguma.TIPO);
                }

                for(TabelaDeSimbolos.EntradaTabelaDeSimbolos entrada : lista)
                {
                    String nomeVar = ctx.IDENT().getText() + '.' + entrada.nome;
                    if(tabela.existe(nomeVar))
                    {
                        AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + nomeVar + " ja declarado anteriormente");
                    }
                    else
                    {
                        tabela.adicionar(entrada);
                        tabela.adicionar(ctx.IDENT().getText(),entrada);
                    }
                }
            }

            tabela.adicionar(ctx.IDENT().getText(), AlgumaSemanticoUtils.getTipo(ctx.tipo().getText()), TabelaDeSimbolos.StructAlguma.TIPO);
        }

        return super.visitDeclaracao_tipo(ctx);
    }

    @Override
    public Object visitDeclaracao_variavel(AlgumaParser.Declaracao_variavelContext ctx) {
        TabelaDeSimbolos tabela = escopo.getEscopo();

        for (AlgumaParser.IdentificadorContext ident : ctx.variavel().identificador())
        {
            // Constroi o caminho da variável
            StringBuilder nomeId = new StringBuilder(); int i = 0;
            for(TerminalNode id : ident.IDENT())
            {
                if(i++ > 0)
                    nomeId.append(".");
                nomeId.append(id.getText());
            }

            // Já foi declarada
            if(tabela.existe(nomeId.toString()))
            {
                AlgumaSemanticoUtils.adicionarErroSemantico(ident.start, "identificador " + nomeId + " ja declarado anteriormente"); // Erro
            }
            else
            {
                TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.variavel().tipo().getText());

                if(tipo != null) // Tipo simples
                {
                    tabela.adicionar(nomeId.toString(), tipo, TabelaDeSimbolos.StructAlguma.VAR);
                }
                else
                {
                    var tipoVar = ctx.variavel().tipo().tipo_estendido();
                    TerminalNode identTipo = ctx.variavel().tipo() != null
                            && tipoVar != null
                            && tipoVar.tipo_basico_ident() != null
                            && tipoVar.tipo_basico_ident().IDENT() != null
                                ? tipoVar.tipo_basico_ident().IDENT()
                                : null;

                    if(identTipo != null) // Tipo declarado
                    {
                        ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> lista = null;
                        boolean achou = false;

                        for(TabelaDeSimbolos tab: escopo.getPilhaTabela())
                        {
                            if(!achou && tab.existe(identTipo.getText()))
                            {
                                lista = tab.getTipoTabela(identTipo.getText());
                                achou = true;
                            }
                        }

                        if(tabela.existe(nomeId.toString())) // Tipo declarado existe?
                        {
                            AlgumaSemanticoUtils.adicionarErroSemantico(ident.start, "tipo " + nomeId + " nao declarado");
                        }
                        else
                        {
                            tabela.adicionar(nomeId.toString(), TabelaDeSimbolos.TipoAlguma.REGISTRO, TabelaDeSimbolos.StructAlguma.VAR);
                            for(TabelaDeSimbolos.EntradaTabelaDeSimbolos entrada : lista)
                            {
                                tabela.adicionar(nomeId + "." + entrada.nome , entrada.tipo, TabelaDeSimbolos.StructAlguma.VAR);
                            }
                        }
                    }
                    else if(ctx.variavel().tipo().registro() != null) // Registro
                    {
                        ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> lista = new ArrayList<>();

                        for(AlgumaParser.VariavelContext va : ctx.variavel().tipo().registro().variavel())
                        {
                            TabelaDeSimbolos.TipoAlguma tipoReg = AlgumaSemanticoUtils.getTipo(va.tipo().getText());

                            for(AlgumaParser.IdentificadorContext id : va.identificador())
                            {
                                lista.add(tabela.new EntradaTabelaDeSimbolos(id.getText(), tipoReg, TabelaDeSimbolos.StructAlguma.VAR));
                            }
                        }
                        tabela.adicionar(nomeId.toString(), TabelaDeSimbolos.TipoAlguma.REGISTRO, TabelaDeSimbolos.StructAlguma.VAR);

                        for(TabelaDeSimbolos.EntradaTabelaDeSimbolos entrada_2 : lista)
                        {
                            String nameVar = nomeId.toString() + '.' + entrada_2.nome;
                            if(tabela.existe(nameVar))
                            {
                                AlgumaSemanticoUtils.adicionarErroSemantico(ident.start, "identificador " + nameVar + " ja declarado anteriormente");
                            }
                            else
                            {
                                tabela.adicionar(entrada_2);
                                tabela.adicionar(nameVar, entrada_2.tipo , TabelaDeSimbolos.StructAlguma.VAR);
                            }
                        }
                    }
                    else
                    {
                        tabela.adicionar(ident.getText(), TabelaDeSimbolos.TipoAlguma.INTEIRO, TabelaDeSimbolos.StructAlguma.VAR);
                    }
                }
            }
        }

        return super.visitDeclaracao_variavel(ctx);
    }

    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx){
        TabelaDeSimbolos tabela = escopo.getEscopo();

        // Função ou procedimento já foi declarado?
        if(tabela.existe(ctx.IDENT().getText()))
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, ctx.IDENT().getText()+ " ja declarado anteriormente");
            return super.visitDeclaracao_global(ctx);
        }
        else
        {
            TabelaDeSimbolos.TipoAlguma tipoRetorno = TabelaDeSimbolos.TipoAlguma.VOID;

            // É função ou procedimento?
            if(ctx.getText().startsWith("funcao"))
            {
                tipoRetorno = AlgumaSemanticoUtils.getTipo(ctx.tipo_estendido().getText());
                tabela.adicionar(ctx.IDENT().getText(), tipoRetorno, TabelaDeSimbolos.StructAlguma.FUNC);
            }
            else
            {
                tabela.adicionar(ctx.IDENT().getText(), tipoRetorno, TabelaDeSimbolos.StructAlguma.PROC);
            }

            // Criar o escopo interno dessa declaracao_global
            escopo.novoEscopo(tipoRetorno);
            TabelaDeSimbolos tabelaAux = tabela;
            tabela = escopo.getEscopo();

            if(ctx.parametros() != null) // Validar parametros
            {
                for(AlgumaParser.ParametroContext param : ctx.parametros().parametro())
                {
                    for(AlgumaParser.IdentificadorContext id : param.identificador())
                    {
                        StringBuilder nomeId = new StringBuilder();
                        int i = 0;
                        for(TerminalNode ident : id.IDENT()){
                            if(i++ > 0)
                                nomeId.append(".");
                            nomeId.append(ident.getText());
                        }

                        if(tabela.existe(nomeId.toString()))
                        {
                            AlgumaSemanticoUtils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
                        }
                        else
                        {
                            TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(param.tipo_estendido().getText());

                            if(tipo != null)
                            {
                                tabela.adicionar(tabela.new EntradaTabelaDeSimbolos(nomeId.toString(), tipo, TabelaDeSimbolos.StructAlguma.VAR));
                                tabelaAux.adicionar(ctx.IDENT().getText(), tabela.new EntradaTabelaDeSimbolos(nomeId.toString(), tipo, TabelaDeSimbolos.StructAlguma.VAR));
                            }
                            else
                            {
                                TerminalNode identTipo = param.tipo_estendido().tipo_basico_ident() != null
                                        && param.tipo_estendido().tipo_basico_ident().IDENT() != null
                                            ? param.tipo_estendido().tipo_basico_ident().IDENT()
                                            : null;

                                if(identTipo !=null)
                                {
                                    ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> lista = null;
                                    boolean achou = false;
                                    for(TabelaDeSimbolos tab: escopo.getPilhaTabela())
                                    {
                                        if(!achou && tab.existe(identTipo.getText()))
                                        {
                                            lista = tab.getTipoTabela(identTipo.getText());
                                            achou = true;
                                        }
                                    }

                                    if(tabela.existe(nomeId.toString()))
                                    {
                                        AlgumaSemanticoUtils.adicionarErroSemantico(id.start , "identificador " + nomeId+ " ja declarado anteriormente"); // Erro
                                    }
                                    else
                                    {
                                        tabela.adicionar(tabela.new EntradaTabelaDeSimbolos(nomeId.toString(), TabelaDeSimbolos.TipoAlguma.REGISTRO , TabelaDeSimbolos.StructAlguma.VAR)); // Colocar na tabela o tipo e a Struct VAR
                                        tabelaAux.adicionar(ctx.IDENT().getText(), tabela.new EntradaTabelaDeSimbolos(nomeId.toString(), TabelaDeSimbolos.TipoAlguma.REGISTRO, TabelaDeSimbolos.StructAlguma.VAR)); // Colocar na tabela o tipo e a Struct VAR

                                        for(TabelaDeSimbolos.EntradaTabelaDeSimbolos entrada : lista)
                                        {
                                            tabela.adicionar(tabela.new EntradaTabelaDeSimbolos(nomeId + "." + entrada.nome, entrada.tipo, TabelaDeSimbolos.StructAlguma.VAR)); // Colocar na tabela o tipo e a Struct VAR
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Passa o controle para a classe pai
            Object ret;
            ret = super.visitDeclaracao_global(ctx);

            // Limpa o escopo da declaracao_global
            escopo.delEscopo();

            return ret;
        }
    }

    @Override
    public Object visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx){
        if(ctx.IDENT() != null)
        {
            // Verifica se a declaracao já foi adicionada em alguma escopo válido
            boolean achou = false;
            for(TabelaDeSimbolos escopos : escopo.getPilhaTabela()){
                if(escopos.existe(ctx.IDENT().getText())){
                    achou = true;
                    break;
                }
            }

            if(!achou)
            {
                AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText() + " nao declarado");
            }
        }

        return super.visitTipo_basico_ident(ctx);
    }

    @Override
    public Object visitIdentificador(AlgumaParser.IdentificadorContext ctx) {
        // Constroi o caminho da variavel
        StringBuilder nomeVar = new StringBuilder(); int i = 0;
        for(TerminalNode id : ctx.IDENT())
        {
            if(i++ > 0)
                nomeVar.append(".");
            nomeVar.append(id.getText());
        }

        // Verifica se a declaracao já foi adicionada em alguma escopo válido
        boolean achou = true;
        for(TabelaDeSimbolos escopos : escopo.getPilhaTabela())
        {
            if(escopos.existe(nomeVar.toString()))
            {
                achou = false;
                break;
            }
        }

        if(achou)
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + nomeVar + " nao declarado");
        }
        return super.visitIdentificador(ctx);
    }

    @Override
    public Object visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx){
        TabelaDeSimbolos.TipoAlguma tipoExp = AlgumaSemanticoUtils.verificarTipo(escopo, ctx.expressao());

        // Constroi o caminho da variável
        StringBuilder var = new StringBuilder(); int i = 0;
        for(TerminalNode id : ctx.identificador().IDENT()){
            if(i++ > 0)
                var.append(".");
            var.append(id.getText());
        }
        String pointer = ctx.getText().charAt(0) ==  '^' ? "^" : "";

        boolean erro = false;
        // Retorno da expressao é valido?
        if(tipoExp != TabelaDeSimbolos.TipoAlguma.INVALIDO)
        {
            boolean achou = false;
            for(TabelaDeSimbolos escopos : escopo.getPilhaTabela())
            {
                if(escopos.existe(var.toString()) && !achou)
                {
                    achou = true;

                    // Retorno da expressão é compatível com o tipo da variável?
                    TabelaDeSimbolos.TipoAlguma tipoVar = AlgumaSemanticoUtils.verificarTipo(escopo, var.toString());
                    Boolean expNumeric = tipoExp == TabelaDeSimbolos.TipoAlguma.INTEIRO || tipoExp == TabelaDeSimbolos.TipoAlguma.REAL;
                    Boolean varNumeric = tipoVar == TabelaDeSimbolos.TipoAlguma.INTEIRO || tipoVar == TabelaDeSimbolos.TipoAlguma.REAL;

                    if(!(expNumeric && varNumeric) && tipoExp != tipoVar) erro = true;
                }
            }
        }
        else erro = true;

        if(erro)
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start,"atribuicao nao compativel para " + pointer +  ctx.identificador().getText());
        }


        return super.visitCmdAtribuicao(ctx);

    }

    @Override
    public Object visitCmdRetorne(AlgumaParser.CmdRetorneContext ctx){
        if(escopo.getEscopo().tipo == TabelaDeSimbolos.TipoAlguma.VOID)
        {
            AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "comando retorne nao permitido nesse escopo");
        }

        return super.visitCmdRetorne(ctx);
    }

    @Override
    public Object visitParcela_unario(AlgumaParser.Parcela_unarioContext ctx){
        TabelaDeSimbolos tabela = escopo.getEscopo();

        // Verifica se todos os parametros passados para uma função
        // são compatíveis com o que foi declarado
        if(ctx.IDENT() != null)
        {
            String namePar = ctx.IDENT().getText();
            if(tabela.existe(ctx.IDENT().getText()))
            {
                List<TabelaDeSimbolos.EntradaTabelaDeSimbolos> params = tabela.getTipoTabela(namePar);
                boolean achou = false;

                if(params.size()!= ctx.expressao().size()) achou = true;
                else
                {
                    for(int i = 0; i < params.size(); i++)
                    {
                        if(AlgumaSemanticoUtils.verificarTipo(escopo, ctx.expressao().get(i)) != params.get(i).tipo) achou = true;
                    }
                }

                if(achou)
                {
                    AlgumaSemanticoUtils.adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + namePar);
                }
            }
        }

        return super.visitParcela_unario(ctx);
    }
}