package br.ufscar.dc.compiladores.alguma.sintatico;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import java.io.PrintWriter;

public class ErrorListener implements ANTLRErrorListener {

    // Declaração do PrintWriter - Escrever no Arquivo
    PrintWriter pw;

    // Garante que só imprima uma vez o erro
    static boolean errorDetected = false;

    public ErrorListener(PrintWriter pw) {
        this.pw = pw;
    }

    @Override
    public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3, boolean arg4, java.util.BitSet arg5, ATNConfigSet arg6) {}

    @Override
    public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2, int arg3, java.util.BitSet arg4, ATNConfigSet arg5) {}

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {}

    @Override
    public void	syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        Token t = (Token) offendingSymbol; // Token que gerou o erro
        String text = t.getText(); // Transforma o token em texto
        String tipo = AlgumaLexer.VOCABULARY.getDisplayName(t.getType()); // Pegar o tipo para categorizar os erros

        if(!errorDetected){
            switch (tipo) {
                case "ERRO": // Erro de simbolo não identificado
                    pw.println("Linha " + t.getLine() + ": " + text + " - simbolo nao identificado");
                    errorDetected = true;
                    break;
                case "CADEIA_NAO_FECHADA": // Error de cadeia não fechada
                    pw.println("Linha " + t.getLine() + ": " + "cadeia literal nao fechada");
                    errorDetected = true;
                    break;
                case "COMENTARIO_NAO_FECHADO": // Error de comentario não fechado
                    pw.println("Linha " + t.getLine() + ": " + "comentario nao fechado");
                    errorDetected = true;
                    break;

                // Checa por erros sintáticos
                default:
                    // Caso o texto seja <EOF>, transformar em EOF
                    if (text.equals("<EOF>")) text = "EOF";

                    // Imprime erro sintático
                    pw.println("Linha " + line + ": erro sintatico proximo a " + text);
                    errorDetected = true;
                    break;
            }

        }

    }

}