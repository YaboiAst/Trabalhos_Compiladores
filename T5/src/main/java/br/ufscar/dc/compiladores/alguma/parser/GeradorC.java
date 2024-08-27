package br.ufscar.dc.compiladores.alguma.parser;

import br.ufscar.dc.compiladores.expr.parser.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.expr.parser.AlgumaParser;

public class GeradorC extends AlgumaBaseVisitor<Void> {
    private StringBuilder output;
    TabelaDeSimbolos tabelaS;

    public GeradorC(){
        output = new StringBuilder();
        tabelaS = new TabelaDeSimbolos();
    }

    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        // Cabeçalho
        output.append("#include <stdio.h>");
        output.append("#include <stdlib.h>");
        output.append("\n");

        // Global

        ctx.declaracoes().declaracao_local()
                .forEach(this::visitDeclaracao_local);
        ctx.declaracoes().declaracao_global()
                .forEach(this::visitDeclaracao_global);

        // Função
        output.append("int main()\n{\n");

        visitCorpo(ctx.corpo());

        // Fecha função
        output.append("return 0;\n");
        output.append("}\n");

        return null;
    }

    @Override
    public Void visitCorpo(AlgumaParser.CorpoContext ctx) {
        for (AlgumaParser.Declaracao_localContext localCtx : ctx.declaracao_local()){
            visitDeclaracao_local(localCtx);
        }

        for (AlgumaParser.CmdContext cmdCtx : ctx.cmd()){
            visitCmd(cmdCtx);
        }

        return null;
    }

    // ----------------------------------------------------------------------------------------------

    @Override
    public Void visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        // Pode ser procedimento ou funçao

        if (ctx.getText().contains("funcao"))
        {
            visitTipo_estendido(ctx.tipo_estendido());
            output.append(" ")
                    .append(ctx.IDENT().getText())
                    .append("(");
        }
        else if (ctx.getText().contains("procedimento"))
        {
            output.append("void ")
                    .append(ctx.IDENT().getText())
                    .append("(");
        }

        ctx.parametros().parametro().forEach(this::visitParametro);
        output.append("\n{\n");

        visitCorpo(ctx.corpo());

        output.append("}\n");

        return null;
    }

    @Override
    public Void visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        if (ctx.getText().contains("declare")) {
            visitVariavel(ctx.variavel());
        }
        else if (ctx.getText().contains("constante")) {
            String tipo = AlgumaSemanticoUtils.convertToC(ctx.tipo_basico().getText());

            output.append("const ")
                    .append(tipo)
                    .append(" ")
                    .append(ctx.IDENT().getText())
                    .append(" = ");

            visitValor_constante(ctx.valor_constante());
        }
        else if (ctx.getText().contains("tipo")) {
            String tipo = AlgumaSemanticoUtils.convertToC(ctx.tipo().getText());

            output.append("typedef ");

            visitTipo(ctx.tipo());
            output.append(ctx.IDENT().getText())
                    .append(";\n");
        }

        return null;
    }

    // ----------------------------------------------------------------------------------------------

    @Override
    public Void visitVariavel(AlgumaParser.VariavelContext ctx) {
        var tipoC = AlgumaSemanticoUtils.convertToC(ctx.tipo().getText().replace("^", ""));
        output.append(tipoC)
                .append(" ");

        for (AlgumaParser.IdentificadorContext id : ctx.identificador()){
            output.append(id.getText());
            output.append(",");
        }
        output.deleteCharAt(output.lastIndexOf(","));


        output.append(";\n");
        return null;
    }

    // ----------------------------------------------------------------------------------------------

    @Override
    public Void visitTipo(AlgumaParser.TipoContext ctx) {
        if (ctx.registro() != null)
            visitRegistro(ctx.registro());
        else
            visitTipo_estendido(ctx.tipo_estendido());

        return null;
    }

    @Override
    public Void visitTipo_estendido(AlgumaParser.Tipo_estendidoContext ctx) {
        boolean isPointer = ctx.getText().contains("^");

        if (ctx.tipo_basico_ident().IDENT() != null){
            output.append(ctx.tipo_basico_ident().IDENT().getText());
        }
        else{
            var tipoC = AlgumaSemanticoUtils.convertToC(ctx.tipo_basico_ident().tipo_basico().getText().replace("^", ""));
            output.append(tipoC);
        }

        output.append(isPointer ? "*" : "");

        return null;
    }

    @Override
    public Void visitRegistro(AlgumaParser.RegistroContext ctx) {
        output.append("struct \n{\n");
        ctx.variavel().forEach(this::visitVariavel);
        output.append("}");

        return null;
    }

    // ----------------------------------------------------------------------------------------------


    @Override
    public Void visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
        for (AlgumaParser.IdentificadorContext idCtx : ctx.identificador()) {

        }

        return null;
    }

    @Override
    public Void visitCmdEscreva(AlgumaParser.CmdEscrevaContext ctx) {
        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Void visitCmdSe(AlgumaParser.CmdSeContext ctx) {
        return super.visitCmdSe(ctx);
    }

    @Override
    public Void visitCmdCaso(AlgumaParser.CmdCasoContext ctx) {
        return super.visitCmdCaso(ctx);
    }

    @Override
    public Void visitCmdPara(AlgumaParser.CmdParaContext ctx) {
        return super.visitCmdPara(ctx);
    }

    @Override
    public Void visitCmdEnquanto(AlgumaParser.CmdEnquantoContext ctx) {
        return super.visitCmdEnquanto(ctx);
    }

    @Override
    public Void visitCmdFaca(AlgumaParser.CmdFacaContext ctx) {
        return super.visitCmdFaca(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        return super.visitCmdAtribuicao(ctx);
    }

    @Override
    public Void visitCmdChamada(AlgumaParser.CmdChamadaContext ctx) {
        return super.visitCmdChamada(ctx);
    }

    @Override
    public Void visitCmdRetorne(AlgumaParser.CmdRetorneContext ctx) {
        return super.visitCmdRetorne(ctx);
    }
}
