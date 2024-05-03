package br.ufscar.dc.compiladores.alguma.sintatico;

import java.io.IOException;

import br.ufscar.dc.compiladores.alguma.sintatico.ErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/*
 * Vitor Gabriel Orsin - 801575
 *
 * T2 - ANALISADOR SINTÁTICO
 * Implementar um analisador sintático que consegue identificar a linguagem LA
 *
 * Lê um arquivo de entrada que contenha o código em LA e verifica por erros de sintaxe
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
            // Inicializa alguma-parser
            CharStream cs = CharStreams.fromFileName(input); // Leitura do arquivo de entrada
            AlgumaLexer lex = new AlgumaLexer(cs); // instancia o analisador léxico
            CommonTokenStream tokens = new CommonTokenStream(lex); // Gera a sequência de tokens
            AlgumaParser par = new AlgumaParser(tokens); // instancia o parser

            // Inicializa a classe de erro customizada
            ErrorListener errListener = new ErrorListener(pw);
            par.removeErrorListeners();
            par.addErrorListener(errListener);

            // Analisa
            par.programa();

            pw.println("fim da compilação");
        }
        catch (IOException ex) {
            System.err.println("Arquivo não encontrado: "+ args[1]);
        }
    }
}