lexer grammar AlgumaLexer;

fragment
ESC_SEQ	: '\\\'';

// Palavras reservadas de LA
PALAVRA_CHAVE
    :	'declare' | 'algoritmo' | 'inteiro' | 'literal'  | 'leia' | 'se' | 'entao' | 'senao' | 'fim_se'
    | 'escreva' | 'fim_algoritmo' | 'e' | 'ou' | 'nao' | 'real' | 'logico' | 'caso' | 'seja' | 'fim_caso' | 'para' | 'fim_para'
    | 'ate' | 'faca' | 'enquanto' | 'fim_enquanto' | 'registro' | 'fim_registro' | 'tipo' | 'procedimento' | 'fim_procedimento'
    | 'var' | 'funcao' | 'fim_funcao' | 'retorne' | 'constante' | 'falso' | 'verdadeiro'
    ;

// Números
NUM_INT	: ('0'..'9')+
	;
NUM_REAL: ('0'..'9')+ ('.' ('0'..'9')+)?
	;

// Strings
CADEIA 	: '"' (~( '"'|'\\' |'\n'|'\r')| ESC_SEQ)* '"'
	;

// Variáveis
IDENT : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
	;

// Comentários (São definidos dentro de {})
COMENTARIO : '{' ~('\n'|'\r'|'{'|'}' )* '}' '\r'? '\n'? {skip();}
    ;

// WS
WS  :   ( ' ' | '\t' | '\r' | '\n' ) {skip();}
    ;

// Separadores
ABRE_COL : '[' ;
FECHA_COL : ']' ;

ABRE_PAR :	'(' ;
FECHA_PAR:	')' ;

VIRGULA	:	',' ;

DELIM:	':' ;

DOIS_PONTOS: '..' ;

// Operações
OP_ARIT	:	'+' | '-' | '*' | '/' | '&' | '%' | '^' ;
OP_REL	:	'>' | '>=' | '<' | '<=' | '<>' | '='    ;
OP_ATR  :   '<-' ;
OP_ACESSO : '.'  ;


// Erros do código
ERRO                :   .
        ;
CADEIA_ABERTA       : '"' (~( '"'|'\\' |'\n'|'\r')| ESC_SEQ)* '\r'? '\n'?
        ;
COMENTARIO_ABERTO   : '{' ~('\n'|'\r'|'{'|'}' )* '\r'? '\n'?
        ;