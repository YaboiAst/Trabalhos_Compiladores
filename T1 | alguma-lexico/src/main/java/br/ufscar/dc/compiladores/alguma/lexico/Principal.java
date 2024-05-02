package br.ufscar.dc.compiladores.alguma.lexico;

import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/*
    * Vitor Gabriel Orsin - 801575
    *
    * T1 - ANALISADOR LÉXICO
    * Implementar um analisador léxico que consegue identificar a linguagem LA
    *
    * Lê um arquivo de entrada que contenha o código em LA e escreve todos os tokens encontradas pelo Lexer
    * em um arquivo de saída especificados nos argumentos
* */

public class Principal {

    public static void main(String[] args) {
        // Terminar execução se os argumentos estiverem incorretos
        if(args.length != 2){
            System.out.println("Argumentos inválidos, use: " +
                    "java -jar target/alguma-lexico-1.0-SNAPSHOT-jar-with-dependencies.jar"
                    + "<Caminho do arquivo de entrada>"
                    + "<Caminho do arquivo de saída>");
            return;
        }

        String input = args[0];     // Caminho com o arquivo contendo o código em LA
        String output = args[1];    // Caminho com o arquivo que armazenará os tokens

        try (PrintWriter pw = new PrintWriter(output)) {
            CharStream cs = CharStreams.fromFileName(input); // Leitura do arquivo de entrada
            AlgumaLexer lex = new AlgumaLexer(cs); // instancia o analisador léxico

            Token t = null;

            // Continua pegando tokens até achar o fim do arquivo
            label:
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                String tokenId = AlgumaLexer.VOCABULARY.getDisplayName(t.getType()); // Token a ser analisada

                // ANÁLISE LÉXICA --------------------------------------------------------------
                switch (tokenId) {
                    // Erros da linguagem (Léxico não reconhecida, cadeias ou comentários abertos)
                    case "ERRO":
                        pw.println("Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado"); // Erro de Simbolo
                        break label;
                    case "CADEIA_ABERTA":
                        pw.println("Linha " + t.getLine() + ": cadeia literal nao fechada"); // Cadeia não fechada
                        break label;
                    case "COMENTARIO_ABERTO":
                        pw.println("Linha " + t.getLine() + ": comentario nao fechado"); // Comentário não fechado
                        break label;

                    // Se o token não for nenhum erro
                    case "PALAVRA_CHAVE":
                    case "OP_ARIT":
                    case "OP_REL":
                    case "OP_ATR":
                    case "OP_ACESSO":
                        pw.println("<'" + t.getText() + "','" + t.getText() + "'>");
                        break;
                    default:
                        pw.println("<'" + t.getText() + "'," + tokenId + ">"); //  Imprime o texto e o nome
                        break;
                }
            }
        } catch (IOException ex) {
            System.err.println("Arquivo não encontrado: "+ args[1]);
        }
    }
}