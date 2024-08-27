package br.ufscar.dc.compiladores.alguma.sintatico;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import java.io.PrintWriter;

/*
*   Classe personalizada para tratamento de erros sintáticos
*/
public class ErrorListener implements ANTLRErrorListener {
    PrintWriter pw;
    static boolean errorDetected = false; // Para no primeiro erro

    // Construtor
    public ErrorListener(PrintWriter pw) {
        this.pw = pw;
    }

    // Não utilizados
    @Override
    public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3, boolean arg4, java.util.BitSet arg5, ATNConfigSet arg6) {}
    @Override
    public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2, int arg3, java.util.BitSet arg4, ATNConfigSet arg5) {}
    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {}

    @Override
    public void	syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // Token que gerou o erro
        Token t = (Token) offendingSymbol;
        String text = t.getText();

        // Qual o lexico do erro
        String tipo = AlgumaLexer.VOCABULARY.getDisplayName(t.getType());

        if(!errorDetected){
            switch (tipo) {
                case "ERRO": // Erro de simbolo não identificado
                    pw.println("Linha " + t.getLine() + ": " + text + " - simbolo nao identificado");
                    errorDetected = true;
                    break;
                case "CADEIA_ABERTA": // Error de cadeia aberta
                    pw.println("Linha " + t.getLine() + ": " + "cadeia literal nao fechada");
                    errorDetected = true;
                    break;
                case "COMENTARIO_ABERTO": // Error de comentario aberto
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