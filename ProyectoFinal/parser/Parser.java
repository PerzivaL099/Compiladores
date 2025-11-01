package parser;

package compilador.parser;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import compilador.lexer.Lexer;
import compilador.lexer.Token;
import compilador.lexer.Token.TokenType;
import compilador.parser.declarations.*;
import compilador.parser.expressions.*;
import compilador.parser.statements.*;

public class Parser {

    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken(); // Cargar el primer token
    }

    // *** MÉTODOS DE UTILIDAD DEL PARSER ***

    // Método para consumir el token actual y avanzar al siguiente.
    private void consume(TokenType expectedType) {
        if (currentToken.getType() == expectedType) {
            currentToken = lexer.nextToken();
        } else {
            // Manejo de error de sintaxis
            throw new RuntimeException("Error Sintáctico en línea " + currentToken.getLine() 
                + ": Se esperaba " + expectedType + " pero se encontró " + currentToken.getType());
        }
    }

    // Método de utilidad para verificar si el token actual coincide con alguno de la lista.
    private boolean check(TokenType... types) {
        for (TokenType type : types) {
            if (currentToken.getType() == type) return true;
        }
        return false;
    }
    
    // *** MÉTODO PRINCIPAL ***

    // Corresponde a la regla <Program> -> <FunDecl>*
    public Program parseProgram() {
        int line = currentToken.getLine();
        Program program = new Program(line);

        while (currentToken.getType() != TokenType.EOF) {
            // Asumimos que todo en el nivel superior es una declaración de función (por ahora)
            program.addFunction(parseFunctionDeclaration());
        }
        
        // No necesitamos consumir EOF, el bucle while lo gestiona.
        return program;
    }

    // Corresponde a la regla <FunDecl>
    // <FunDecl> -> <Type> ID LPAREN <ParameterList> RPAREN <Block>
    private FunctionDeclaration parseFunctionDeclaration() {
        int line = currentToken.getLine();
        
        String returnType = parseType(); // INT o BOOLEAN
        consume(TokenType.ID);
        String functionId = currentToken.getLexeme();
        
        consume(TokenType.LPAREN);
        List<Parameter> parameters = parseParameterList();
        consume(TokenType.RPAREN);

        // El cuerpo de la función es un bloque
        BlockStatement body = parseBlock(); 

        return new FunctionDeclaration(line, returnType, functionId, parameters, body);
    }

    // <Type> -> INT | BOOLEAN
    private String parseType() {
        if (check(TokenType.INT)) {
            consume(TokenType.INT);
            return "int";
        }
        if (check(TokenType.BOOLEAN)) {
            consume(TokenType.BOOLEAN);
            return "boolean";
        }
        throw new RuntimeException("Error Sintáctico: Se esperaba un tipo (int/boolean)");
    }
    
    // <ParameterList> -> <Parameter> (, <Parameter>)* | ε
    private List<Parameter> parseParameterList() {
        List<Parameter> parameters = new ArrayList<>();
        
        if (check(TokenType.INT, TokenType.BOOLEAN)) {
            // Primer parámetro
            parameters.add(parseParameter());
            
            // Resto de parámetros
            while (check(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                parameters.add(parseParameter());
            }
        }
        // Si no hay tipo, la lista está vacía (ε), se retorna lista vacía.
        return parameters;
    }
    
    // <Parameter> -> <Type> ID
    private Parameter parseParameter() {
        int line = currentToken.getLine();
        String type = parseType();
        consume(TokenType.ID);
        String id = currentToken.getLexeme();
        return new Parameter(line, type, id);
    }

    // <Block> -> LBRACE <Statement>* RBRACE
    public BlockStatement parseBlock() {
        int line = currentToken.getLine();
        consume(TokenType.LBRACE);
        
        List<ASTNode> statements = new ArrayList<>();
        // Seguimos parseando mientras no lleguemos al final del bloque o del archivo
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        
        consume(TokenType.RBRACE);
        return new BlockStatement(line, statements);
    }

    // <Statement> -> <Block> | <Decl> | <IfStmt> | ...
    private ASTNode parseStatement() {
        int line = currentToken.getLine();
        
        if (check(TokenType.LBRACE)) {
            return parseBlock();
        } 
        
        if (check(TokenType.INT, TokenType.BOOLEAN)) {
            // Es una declaración
            return parseDeclaration();
        }
        
        if (check(TokenType.IF)) {
            return parseIfStatement();
        }
        
        if (check(TokenType.WHILE)) {
            return parseWhileStatement();
        }
        
        if (check(TokenType.RETURN)) {
            return parseReturnStatement();
        }
        
        // Si no es ninguna de las anteriores, debe ser una Asignación o Llamada a Función
        return parseAssignmentOrCall();
    }
    
    // *** Ejemplos de Flujo de Control ***

    // <IfStmt> -> IF LPAREN <Expression> RPAREN <Statement> (ELSE <Statement>)?
    private IfStatement parseIfStatement() {
        int line = currentToken.getLine();
        consume(TokenType.IF);
        
        consume(TokenType.LPAREN);
        Expression condition = parseExpression();
        consume(TokenType.RPAREN);

        ASTNode thenBranch = parseStatement();
        ASTNode elseBranch = null;

        if (check(TokenType.ELSE)) {
            consume(TokenType.ELSE);
            elseBranch = parseStatement();
        }

        return new IfStatement(line, condition, thenBranch, elseBranch);
    }
    
    // <WhileStmt> -> WHILE LPAREN <Expression> RPAREN <Statement>
    private WhileStatement parseWhileStatement() {
        int line = currentToken.getLine();
        consume(TokenType.WHILE);
        
        consume(TokenType.LPAREN);
        Expression condition = parseExpression();
        consume(TokenType.RPAREN);

        ASTNode body = parseStatement();

        return new WhileStatement(line, condition, body);
    }

    // ... (Faltan métodos para Declaración, Retorno, Asignación/Llamada)

    // *** MÉTODOS DE EXPRESIONES (Basados en Precedencia de la GLC) ***
    
    // <Expression> (Nivel de menor precedencia: OR)
    public Expression parseExpression() {
        return parseLogicOr();
    }
    
    // <Expression> -> <LogicAnd> (OR <LogicAnd>)*
    private Expression parseLogicOr() {
        Expression expr = parseLogicAnd();
        
        while (check(TokenType.OR)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseLogicAnd();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }

    // <LogicAnd> -> <Equality> (AND <Equality>)*
    private Expression parseLogicAnd() {
        Expression expr = parseEquality();
        
        while (check(TokenType.AND)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseEquality();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }
    
}
