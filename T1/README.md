# **Compiladores - Analisador Léxico**
* Vitor Gabriel Orsin - 801575

## Pré-requisitos
Para conseguir executar corretamente o programa, verifique a versão do Java e do Maven na sua máquina:
```
java -version
mvn -v
```
**Versão necessária** <br>
Java : 17 <br>
Maven: 3.6.3 <br>

## Como compilar
Com esse repositório clonado em sua máquina, navegue até o diretório do programa e compile usando o Maven:
```
mvn clean
mvn package
```

## Como usar
Utilize o java para executar o programa, passando os argumentos:
* O caminho literal até o arquivo contendo o código em LA
* O caminho literal até o arquivo onde o programa escreverá a saída
```
java -jar target/alguma-lexico-1.0-SNAPSHOT-jar-with-dependencies.jar ~/path/to/input/file.txt ~/path/to/output/file.txt
```

## Rodando com o corretor
Para executar o corretor disponibilizado pelo professor (https://github.com/dlucredio/compiladores-corretor-automatico/), use o seguinte comando na pasta do programa corretor:
```
java -jar target/compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar ~/{path/to/lexer}/alguma-lexico/target/alguma-lexico-1.0-SNAPSHOT-jar-with-dependencies.jar" gcc ~/temp ~//TestFiles/casos-de-teste/casos-de-teste/ "RA" gabarito-t1

```
