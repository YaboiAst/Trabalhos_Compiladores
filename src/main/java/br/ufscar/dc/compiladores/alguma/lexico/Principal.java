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
            System.out.println("Argumento inválidos, use: " +
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
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                String tokenId = AlgumaLexer.VOCABULARY.getDisplayName(t.getType()); // Token a ser analisada

                // ANÁLISE LÉXICA --------------------------------------------------------------
                // Erros da linguagem (Léxico não reconhecida, cadeias ou comentários abertos)
                if(tokenId.equals("ERRO")) {
                    pw.println("Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado"); // Erro de Simbolo
                    break;
                }
                else if(tokenId.equals("CADEIA_NAO_FECHADA")) {
                    pw.println("Linha " + t.getLine() + ": cadeia literal nao fechada"); // Cadeia não fechada
                    break;
                }
                else if(tokenId.equals("COMENTARIO_NAO_FECHADO")) {
                    pw.println("Linha " + t.getLine() + ": comentario nao fechado"); // Comentário não fechado
                    break;
                }

                // Se o token não for nenhum erro
                else {
                    // Palavras-chave e operações são mostradas como tokens do próprio nome
                    if(tokenId.equals("PALAVRA_CHAVE") || tokenId.equals("OP_ARIT") || tokenId.equals("OP_REL"))
                        pw.println("<'" + t.getText() + "','" + t.getText() + "'>");
                    // Outros tokens são mostradas com o identificador do léxico
                    else
                        pw.println("<'" + t.getText() + "'," +  tokenId + ">"); //  Imprime o texto e o nome
                }
            }
        } catch (IOException ex) {
            System.err.println("Arquivo não encontrado: "+ args[1]);
        }
    }
}