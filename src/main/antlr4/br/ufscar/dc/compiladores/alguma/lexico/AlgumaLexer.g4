lexer grammar AlgumaLexer;

PALAVRA_CHAVE
    :	'declare' | 'algoritmo' | 'inteiro' | 'literal'  | 'leia' | 'se' | 'entao' | 'senao' | 'fim_se'
    | 'escreva' | 'fim_algoritmo' | 'e' | 'ou' | 'nao' | 'real' | 'logico' | 'caso' | 'seja' | 'fim_caso' | 'para' | 'fim_para'
    | 'ate' | 'faca' | 'enquanto' | 'fim_enquanto' | 'registro' | 'fim_registro' | 'tipo' | 'procedimento' | 'fim_procedimento'
    | 'var' | 'funcao' | 'fim_funcao' | 'retorne' | 'constante' | 'falso' | 'verdadeiro'
    ;

NUMINT	: ('+'|'-')?('0'..'9')+
	;
NUMREAL	: ('+'|'-')?('0'..'9')+ ('.' ('0'..'9')+)?
	;

IDENT : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
	;
CADEIA 	: '"' (~( '"'|'\\' |'\n'|'\r')| ESC_SEQ)* '"'
	;

fragment
ESC_SEQ	: '\\\'';

COMENTARIO
    : '{' ~('\n'|'\r'|'{'|'}' )* '}' '\r'? '\n'? {skip();}
    ;
WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {skip();}
    ;

BRECOL : '['
    ;
FECHACOL : ']'
    ;

VIRGULA	:	','
    ;

DELIM:	':'
    ;

DOIS_PONTOS: '..'
    ;

OP_REL	:	'>' | '>=' | '<' | '<=' | '<>' | '='
    ;

OP_ARIT	:	'+' | '-' | '*' | '/'
    ;

ABREPAR :	'('
	;
FECHAPAR:	')'
	;

ERRO    :   .
    ;