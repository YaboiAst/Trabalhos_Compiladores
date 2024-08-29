package br.ufscar.dc.compiladores.alguma.parser;

import br.ufscar.dc.compiladores.expr.parser.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.expr.parser.AlgumaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Arrays;

public class AlgumaToC extends AlgumaBaseVisitor<Void> {
    StringBuilder cOutput; // Escreve o código em C

    TabelaDeSimbolos tabela;

    public AlgumaToC() {
        cOutput = new StringBuilder();
        this.tabela = new TabelaDeSimbolos(null);
    }

    // Raiz do programa
    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        // Cabeçalhos
        cOutput.append("#include <stdio.h>\n");
        cOutput.append("#include <stdlib.h>\n");
        cOutput.append("#include <string.h>\n\n");

        // Visita as declaracoes globais
        ctx.declaracoes().declaracao_local().forEach(this::visitDeclaracao_local);
        ctx.declaracoes().declaracao_global().forEach(this::visitDeclaracao_global);

        cOutput.append("int main() {\n");

        // Escreve o programa
        visitCorpo(ctx.corpo());

        cOutput.append("return 0; \n");
        cOutput.append("}\n");

        return null;
    }

    @Override
    public Void visitCorpo(AlgumaParser.CorpoContext ctx) {
        for (AlgumaParser.Declaracao_localContext dec : ctx.declaracao_local())
        {
            visitDeclaracao_local(dec);
        }

        for (AlgumaParser.CmdContext com : ctx.cmd())
        {
            visitCmd(com);
        }

        return null;
    }


    // --------------------------- DECLARAÇÕES
    @Override
    public Void visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        if (ctx.getText().contains("procedimento")) // Função VOID
        {
            cOutput.append("void ")
                    .append(ctx.IDENT().getText())
                    .append("(");
        }
        else
        {
            visitTipo_estendido(ctx.tipo_estendido());

            String tipoC = AlgumaSemanticoUtils.getTipoToC(ctx.tipo_estendido().getText().replace("^", ""));
            if(tipoC.equals("char"))
            {
                cOutput.append("[5]");
            }

            cOutput.append(" ")
                    .append(ctx.IDENT().getText())
                    .append("(");

            TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo_estendido().getText());
            tabela.adicionar(ctx.IDENT().getText(), tipo, TabelaDeSimbolos.StructAlguma.FUNC);
        }

        ctx.parametros().parametro().forEach(this::visitParametro);

        cOutput.append("){\n");

        // Declaracoes da funcao
        ctx.corpo().declaracao_local().forEach(this::visitDeclaracao_local);
        cOutput.append("\n");

        // Comandos da funcao
        ctx.corpo().cmd().forEach(this::visitCmd);

        cOutput.append("}\n");

        return null;
    }

    @Override
    public Void visitParametro(AlgumaParser.ParametroContext ctx) {
        int i = 0;
        String tipoC = AlgumaSemanticoUtils.getTipoToC(ctx.tipo_estendido().getText().replace("^", ""));
        TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo_estendido().getText());

        for (AlgumaParser.IdentificadorContext id : ctx.identificador()) {
            if (i++ > 0)
                cOutput.append(",");

            visitTipo_estendido(ctx.tipo_estendido());
            visitIdentificador(id);

            if (tipoC.equals("char")) cOutput.append("[5]");
            tabela.adicionar(id.getText(), tipo, TabelaDeSimbolos.StructAlguma.VAR);
        }

        return null;
    }

    @Override
    public Void visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        if(ctx.declaracao_variavel() != null)
        {
            visitDeclaracao_variavel(ctx.declaracao_variavel());
        }
        if(ctx.declaracao_constante() != null)
        {
            visitDeclaracao_constante(ctx.declaracao_constante());
        }
        else if(ctx.declaracao_tipo() != null)
        {
            visitDeclaracao_tipo(ctx.declaracao_tipo());
        }

        return null;
    }

    @Override
    public Void visitDeclaracao_variavel(AlgumaParser.Declaracao_variavelContext ctx) {
        visitVariavel(ctx.variavel());
        return null;
    }
    @Override
    public Void visitDeclaracao_constante(AlgumaParser.Declaracao_constanteContext ctx) {
        TabelaDeSimbolos.TipoAlguma typeVar = AlgumaSemanticoUtils.getTipo(ctx.tipo_basico().getText());
        tabela.adicionar(ctx.IDENT().getText(),typeVar,TabelaDeSimbolos.StructAlguma.VAR);

        String tipoC = AlgumaSemanticoUtils.getTipoToC(ctx.tipo_basico().getText());
        cOutput.append("const ")
                .append(tipoC)
                .append(" ")
                .append(ctx.IDENT().getText())
                .append(" = ");
        visitValor_constante(ctx.valor_constante());
        cOutput.append(";\n");

        return null;
    }
    @Override
    public Void visitDeclaracao_tipo(AlgumaParser.Declaracao_tipoContext ctx) {
        cOutput.append("typedef ");

        if(ctx.tipo().getText().contains("registro")){
            for(AlgumaParser.VariavelContext var : ctx.tipo().registro().variavel()){
                for(AlgumaParser.IdentificadorContext id : var.identificador()){
                    TabelaDeSimbolos.TipoAlguma tReg = AlgumaSemanticoUtils.getTipo(var.tipo().getText());
                    tabela.adicionar(ctx.IDENT().getText() + "." + id.getText(), tReg, TabelaDeSimbolos.StructAlguma.VAR);
                    tabela.adicionar(ctx.IDENT().getText(), tabela.new EntradaTabelaDeSimbolos(id.getText(), tReg, TabelaDeSimbolos.StructAlguma.TIPO));
                }
            }
        }

        TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo().getText());
        tabela.adicionar(ctx.IDENT().getText(), tipo, TabelaDeSimbolos.StructAlguma.VAR);

        visitTipo(ctx.tipo());

        cOutput.append(ctx.IDENT())
                .append(";\n");

        return null;
    }


    // --------------------------- TIPOS
    @Override
    public Void visitTipo(AlgumaParser.TipoContext ctx) {
        String tipoC = AlgumaSemanticoUtils.getTipoToC(ctx.getText().replace("^", ""));

        if (tipoC != null)
        {
            cOutput.append(tipoC);
        }
        else if (ctx.registro() != null)
        {
            visitRegistro(ctx.registro());
        }
        else
        {
            visitTipo_estendido(ctx.tipo_estendido());
        }

        boolean pointer = ctx.getText().contains("^");
        if (pointer) cOutput.append("*");

        return null;
    }

    @Override
    public Void visitTipo_estendido(AlgumaParser.Tipo_estendidoContext ctx) {
        visitTipo_basico_ident(ctx.tipo_basico_ident());

        if (ctx.getText().contains("^")) cOutput.append("*");

        return null;
    }

    @Override
    public Void visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx) {
        if (ctx.IDENT() != null)
        {
            cOutput.append(ctx.IDENT().getText());
        }
        else
        {
            cOutput.append(AlgumaSemanticoUtils.getTipoToC(ctx.getText().replace("^", "")));
        }

        return null;
    }

    @Override
    public Void visitRegistro(AlgumaParser.RegistroContext ctx) {
        cOutput.append("struct {\n");
        ctx.variavel().forEach(this::visitVariavel);
        cOutput.append("} ");

        return null;
    }

    @Override
    public Void visitVariavel(AlgumaParser.VariavelContext ctx) {
        String tipoC = AlgumaSemanticoUtils.getTipoToC(ctx.tipo().getText().replace("^", ""));
        TabelaDeSimbolos.TipoAlguma tipo = AlgumaSemanticoUtils.getTipo(ctx.tipo().getText());

        for (AlgumaParser.IdentificadorContext id : ctx.identificador())
        {
            // É registro
            if (ctx.tipo().getText().contains("registro"))
            {
                for (AlgumaParser.VariavelContext var : ctx.tipo().registro().variavel())
                {
                    for (AlgumaParser.IdentificadorContext ident : var.identificador())
                    {
                        TabelaDeSimbolos.TipoAlguma regT = AlgumaSemanticoUtils.getTipo(var.tipo().getText());
                        tabela.adicionar(id.getText() + "." + ident.getText(), regT, TabelaDeSimbolos.StructAlguma.VAR);
                    }
                }
            }
            else if (tipoC == null && tipo == null)
            {
                ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> tab = tabela.getTipoTabela(ctx.tipo().getText());
                if (tab != null)
                {
                    for (TabelaDeSimbolos.EntradaTabelaDeSimbolos val : tab)
                    {
                        tabela.adicionar(id.getText() + "." + val.nome, val.tipo, TabelaDeSimbolos.StructAlguma.VAR);
                    }
                }
            }

            // Vetor
            if (id.getText().contains("["))
            {
                String nome = id.IDENT().get(0).getText();

                int posAbre = id.getText().indexOf("[");
                int posFecha = id.getText().indexOf("]");

                String t = id.getText().substring(posAbre + 1, posFecha);
                int tamanho = Integer.parseInt(t);

                for (int i = 0; i < tamanho; i++) {
                    tabela.adicionar(nome + "[" + i + "]", tipo, TabelaDeSimbolos.StructAlguma.VAR);
                }

            }
            else
            {
                tabela.adicionar(id.getText(), tipo, TabelaDeSimbolos.StructAlguma.VAR);
            }

            visitTipo(ctx.tipo());
            cOutput.append(" ");
            visitIdentificador(id);

            if (tipoC.equals("char")) cOutput.append("[10]");

            cOutput.append(";\n");
        }

        return null;
    }

    @Override
    public Void visitValor_constante(AlgumaParser.Valor_constanteContext ctx) {
        if (ctx.getText().equals("verdadeiro")) cOutput.append("true");
        else if (ctx.getText().equals("falso")) cOutput.append("false");
        else cOutput.append(ctx.getText());

        return null;
    }


    // ----------------------------- IDENTIFICADORES E TERMOS
    @Override
    public Void visitIdentificador(AlgumaParser.IdentificadorContext ctx) {
        int i = 0;
        for (TerminalNode id : ctx.IDENT())
        {
            if (i++ > 0) cOutput.append(".");
            cOutput.append(id.getText());
        }

        // Se o identificador tiver dimensão
        visitDimensao(ctx.dimensao());
        return null;
    }
    @Override
    public Void visitDimensao(AlgumaParser.DimensaoContext ctx) {
        for (AlgumaParser.Exp_aritmeticaContext expr : ctx.exp_aritmetica())
        {
            // Saida -- [expr]*
            cOutput.append("[");
            visitExp_aritmetica(expr);
            cOutput.append("]");
        }

        return null;
    }

    @Override
    public Void visitTermo_logico(AlgumaParser.Termo_logicoContext ctx) {
        visitFator_logico(ctx.fator_logico(0));
        for (int i = 1; i < ctx.fator_logico().size(); i++)
        {
            cOutput.append(" && ");
            visitFator_logico(ctx.fator_logico(i));
        }

        return null;
    }
    @Override
    public Void visitExpressao(AlgumaParser.ExpressaoContext ctx) {
        // Escreve primeiro termo
        visitTermo_logico(ctx.termo_logico(0));

        // Se não tiver mais
        if (ctx.termo_logico().size() <= 1) return null;

        for (int i = 1; i < ctx.termo_logico().size(); i++)
        {
            cOutput.append(" || ");
            visitTermo_logico(ctx.termo_logico(i));
        }

        return null;
    }

    @Override
    public Void visitExp_aritmetica(AlgumaParser.Exp_aritmeticaContext ctx) {
        visitTermo(ctx.termo(0));

        if (ctx.termo().size() <= 1) return null;

        for (int i = 1; i < ctx.termo().size(); i++)
        {
            cOutput.append(ctx.op1(i - 1).getText());
            visitTermo(ctx.termo(i));
        }

        return null;
    }

    @Override
    public Void visitExp_relacional(AlgumaParser.Exp_relacionalContext ctx) {
        visitExp_aritmetica(ctx.exp_aritmetica(0));
        for (int i = 1; i < ctx.exp_aritmetica().size(); i++) {
            if (ctx.op_relacional().getText().equals("="))
            {
                cOutput.append(" == ");
            }
            else
            {
                cOutput.append(ctx.op_relacional().getText());
            }

            visitExp_aritmetica(ctx.exp_aritmetica(i));
        }

        return null;
    }

    @Override
    public Void visitTermo(AlgumaParser.TermoContext ctx) {
        visitFator(ctx.fator(0));

        if (ctx.fator().size() <= 1) return null;

        for (int i = 1; i < ctx.fator().size(); i++) {
            cOutput.append(ctx.op2(i - 1).getText());
            visitFator(ctx.fator(i));
        }

        return null;
    }

    @Override
    public Void visitFator(AlgumaParser.FatorContext ctx) {
        visitParcela(ctx.parcela(0));

        if (ctx.parcela().size() <= 1) return null;

        for (int i = 1; i < ctx.parcela().size(); i++)
        {
            cOutput.append(ctx.op3(i - 1).getText());
            visitParcela(ctx.parcela(i));
        }

        return null;
    }

    @Override
    public Void visitFator_logico(AlgumaParser.Fator_logicoContext ctx) {
        if (ctx.getText().startsWith("nao")) cOutput.append("!");
        visitParcela_logica(ctx.parcela_logica());

        return null;
    }

    @Override
    public Void visitParcela_logica(AlgumaParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null)
        {
            visitExp_relacional(ctx.exp_relacional());
        }
        else if (ctx.getText().equals("verdadeiro")) cOutput.append("true");
        else if (ctx.getText().equals("falso")) cOutput.append("false");

        return null;
    }

    @Override
    public Void visitParcela(AlgumaParser.ParcelaContext ctx) {
        // Tem op negativo?
        if (ctx.op_unario() != null)
        {
            cOutput.append(ctx.op_unario().getText());
        }

        // É unário ou não?
        if (ctx.parcela_unario() != null)
        {
            visitParcela_unario(ctx.parcela_unario());
        }
        else
        {
            visitParcela_nao_unario(ctx.parcela_nao_unario());
        }

        return null;
    }
    @Override
    public Void visitParcela_unario(AlgumaParser.Parcela_unarioContext ctx) {
        if (ctx.IDENT() != null) // Função
        {
            cOutput.append(ctx.IDENT().getText());

            cOutput.append("(");
            for (int i = 0; i < ctx.expressao().size(); i++)
            {
                visitExpressao(ctx.expressao(i));
                if (i < ctx.expressao().size() - 1)
                {
                    cOutput.append(", ");
                }
            }
            cOutput.append(")");
        }
        else if (ctx.expressao() != null) // (Expressao)
        {
            cOutput.append("(");
            visitExpressao(ctx.expressao(0));
            cOutput.append(")");

        }
        else // Valor
        {
            cOutput.append(ctx.getText());
        }

        return null;
    }
    @Override
    public Void visitParcela_nao_unario(AlgumaParser.Parcela_nao_unarioContext ctx) {
        cOutput.append(ctx.getText());
        return null;
    }


    // --------------------------- COMANDOS
    @Override
    public Void visitCmd(AlgumaParser.CmdContext ctx) {
        if (ctx.cmdLeia() != null) visitCmdLeia(ctx.cmdLeia());                         // Read
        else if (ctx.cmdEscreva() != null) visitCmdEscreva(ctx.cmdEscreva());           // Write
        else if (ctx.cmdAtribuicao() != null) visitCmdAtribuicao(ctx.cmdAtribuicao());  // Atribuição
        else if (ctx.cmdSe() != null) visitCmdSe(ctx.cmdSe());                          // IF
        else if (ctx.cmdCaso() != null) visitCmdCaso(ctx.cmdCaso());                    // CASE
        else if (ctx.cmdPara() != null) visitCmdPara(ctx.cmdPara());                    // FOR
        else if (ctx.cmdEnquanto() != null) visitCmdEnquanto(ctx.cmdEnquanto());        // WHILE
        else if (ctx.cmdFaca() != null) visitCmdFaca(ctx.cmdFaca());                    // DO
        else if (ctx.cmdChamada() != null) visitCmdChamada(ctx.cmdChamada());           // f()
        else if (ctx.cmdRetorne() != null) visitCmdRetorne(ctx.cmdRetorne());           // return

        return null;
    }


    @Override
    public Void visitCmdRetorne(AlgumaParser.CmdRetorneContext ctx) {
        cOutput.append("return ");
        visitExpressao(ctx.expressao());
        cOutput.append(";\n");

        return null;
    }


    @Override
    public Void visitCmdChamada(AlgumaParser.CmdChamadaContext ctx) {
        cOutput.append(ctx.IDENT().getText())
                .append("(");

        int i = 0;
        for (AlgumaParser.ExpressaoContext exp : ctx.expressao()) {
            if (i++ > 0)
                cOutput.append(",");

            visitExpressao(exp);
        }

        cOutput.append(");\n");

        return null;
    }


    @Override
    public Void visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
        for (AlgumaParser.IdentificadorContext id : ctx.identificador())
        {
            TabelaDeSimbolos.TipoAlguma identTipo = tabela.verificar(id.getText());
            if (identTipo != TabelaDeSimbolos.TipoAlguma.CADEIA) {
                cOutput.append("scanf(\"%")
                        .append(AlgumaSemanticoUtils.getTipoAlgumToC(identTipo))
                        .append("\", &")
                        .append(id.getText())
                        .append(");\n");
            }
            else
            {
                cOutput.append("gets(");
                visitIdentificador(id);
                cOutput.append(");\n");
            }
        }

        return null;
    }
    @Override
    public Void visitCmdEscreva(AlgumaParser.CmdEscrevaContext ctx) {
        for (AlgumaParser.ExpressaoContext exp : ctx.expressao())
        {
            Escopo escopo = new Escopo(tabela);
            String tipoC = AlgumaSemanticoUtils.getTipoAlgumToC(AlgumaSemanticoUtils.verificarTipo(escopo, exp));

            if (tabela.existe(exp.getText())) {
                TabelaDeSimbolos.TipoAlguma tipo = tabela.verificar(exp.getText());
                tipoC = AlgumaSemanticoUtils.getTipoAlgumToC(tipo);
            }

            cOutput.append("printf(\"%")
                    .append(tipoC)
                    .append("\", ");

            cOutput.append(exp.getText())
                    .append(");\n");
        }

        return null;
    }


    @Override
    public Void visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        if (ctx.getText().contains("^")) cOutput.append("*");

        // ID = EXP
        visitIdentificador(ctx.identificador());

        cOutput.append(" = ")
                .append(ctx.expressao().getText())
                .append(";\n");

        return null;
    }


    @Override
    public Void visitCmdSe(AlgumaParser.CmdSeContext ctx) {
        cOutput.append("if(");
        visitExpressao(ctx.expressao());
        cOutput.append(") {\n");

        for (AlgumaParser.CmdContext cmd : ctx.cmd())
        {
            visitCmd(cmd);
        }

        cOutput.append("}\n");

        if (ctx.cmdSenao() != null)
        {
            cOutput.append("else {\n");

            for (AlgumaParser.CmdContext cmd : ctx.cmdSenao().cmd())
            {
                visitCmd(cmd);
            }

            cOutput.append("}\n");
        }

        return null;
    }

    @Override
    public Void visitCmdCaso(AlgumaParser.CmdCasoContext ctx) {
        cOutput.append("switch(");
        visit(ctx.exp_aritmetica());
        cOutput.append("){\n");

        visit(ctx.selecao());

        if (ctx.getText().contains("senao"))
        {
            cOutput.append("default:\n");
            ctx.cmd().forEach(this::visitCmd);
            cOutput.append("break;\n");
        }

        cOutput.append("}\n");

        return null;
    }
    @Override
    public Void visitSelecao(AlgumaParser.SelecaoContext ctx) {
        ctx.item_selecao().forEach(this::visitItem_selecao);
        return null;
    }
    @Override
    public Void visitItem_selecao(AlgumaParser.Item_selecaoContext ctx) {
        ArrayList<String> intervalo = new ArrayList<>(Arrays.asList(ctx.constantes().getText().split("\\.\\.")));

        String first = !intervalo.isEmpty() ? intervalo.get(0) : ctx.constantes().getText();
        String last = intervalo.size() > 1 ? intervalo.get(1) : intervalo.get(0);

        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++) {
            cOutput.append("case ")
                    .append(i)
                    .append(":\n");
            ctx.cmd().forEach(this::visitCmd);
            cOutput.append("break;\n");
        }

        return null;
    }


    @Override
    public Void visitCmdPara(AlgumaParser.CmdParaContext ctx) {
        String ident = ctx.IDENT().getText();

        cOutput.append("for(" + ident + " = ");
        visitExp_aritmetica(ctx.exp_aritmetica(0));
        cOutput.append("; " + ident + " <= ");
        visitExp_aritmetica(ctx.exp_aritmetica(1));
        cOutput.append("; " + ident + "++){\n");

        ctx.cmd().forEach(this::visitCmd);
        cOutput.append("}\n");

        return null;
    }

    @Override
    public Void visitCmdEnquanto(AlgumaParser.CmdEnquantoContext ctx) {
        cOutput.append("while(");
        visitExpressao(ctx.expressao());

        cOutput.append("){\n");
        ctx.cmd().forEach(this::visitCmd);
        cOutput.append("}\n");

        return null;
    }

    @Override
    public Void visitCmdFaca(AlgumaParser.CmdFacaContext ctx) {
        cOutput.append("do{\n");
        ctx.cmd().forEach(this::visitCmd);
        cOutput.append("} while(");
        visitExpressao(ctx.expressao());
        cOutput.append(");\n");

        return null;
    }
}
