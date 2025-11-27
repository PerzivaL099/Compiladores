package compilador.parser; // Línea 1

// Utilidades de Java
import java.util.ArrayList;
import java.util.List;

// Importar el Lexer y Token
import compilador.lexer.Lexer;
import compilador.lexer.Token;
import compilador.lexer.Token.TokenType;

// Importar TODOS los nodos del AST usando el wildcard (*)
import compilador.parser.declarations.*; 
import compilador.parser.expressions.*; 
import compilador.parser.statements.*; 
import compilador.parser.ASTNode; // Importar la clase base
import compilador.parser.ASTVisitor; // Importar la interfaz

public class Parser {

    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken(); // Cargar el primer token
    }

    // *** MÉTODOS DE UTILIDAD DEL PARSER ***

    private void consume(TokenType expectedType) {
        if (currentToken.getType() == expectedType) {
            currentToken = lexer.nextToken();
        } else {
            throw new RuntimeException("Error Sintáctico en línea " + currentToken.getLine() 
                + ": Se esperaba " + expectedType + " pero se encontró " + currentToken.getType());
        }
    }

    private boolean check(TokenType... types) {
        for (TokenType type : types) {
            if (currentToken.getType() == type) return true;
        }
        return false;
    }
    
    // *** MÉTODOS DE DECLARACIÓN Y PROGRAMA ***

    public compilador.parser.declarations.Program parseProgram() {
        int line = currentToken.getLine();
        // Nota: Asume que Program tiene un método addFunction
        Program program = new Program(line); 

        while (currentToken.getType() != TokenType.EOF) {
            program.addFunction(parseFunctionDeclaration());
        }
        return program;
    }

    private FunctionDeclaration parseFunctionDeclaration() {
        // ... (Implementación ya proporcionada)
        int line = currentToken.getLine();
        
        String returnType = parseType();
        Token idToken = currentToken;
        consume(TokenType.ID);
        String functionId = idToken.getLexeme();
        
        consume(TokenType.LPAREN);
        List<Parameter> parameters = parseParameterList();
        consume(TokenType.RPAREN);

        BlockStatement body = parseBlock(); 

        return new FunctionDeclaration(line, returnType, functionId, parameters, body);
    }

    private String parseType() {
        if (check(TokenType.INT)) {
            consume(TokenType.INT);
            return "int";
        }
        if (check(TokenType.BOOLEAN)) {
            consume(TokenType.BOOLEAN);
            return "boolean";
        }
        throw new RuntimeException("Error Sintáctico en línea " + currentToken.getLine() + ": Se esperaba un tipo (int/boolean)");
    }
    
    // Utiliza el nodo Parameter de tu AST
    private List<Parameter> parseParameterList() {
        List<Parameter> parameters = new ArrayList<>();
        
        if (check(TokenType.INT, TokenType.BOOLEAN)) {
            parameters.add(parseParameter());
            
            while (check(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                parameters.add(parseParameter());
            }
        }
        return parameters;
    }
    
    // Utiliza el nodo Parameter de tu AST
    private Parameter parseParameter() {
        int line = currentToken.getLine();
        String type = parseType();
        Token idToken = currentToken;
        consume(TokenType.ID);
        String id = idToken.getLexeme();
        return new Parameter(line, type, id);
    }


    // *** MÉTODOS DE SENTENCIAS Y BLOQUES ***
    
    public BlockStatement parseBlock() {
        int line = currentToken.getLine();
        consume(TokenType.LBRACE);
        
        List<ASTNode> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        
        consume(TokenType.RBRACE);
        return new BlockStatement(line, statements);
    }

    private ASTNode parseStatement() {
        
        if (check(TokenType.LBRACE)) {
            return parseBlock();
        } 
        
        if (check(TokenType.INT, TokenType.BOOLEAN)) {
            return parseDeclaration(); // Nuevo
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
        
        return parseAssignmentOrCall(); 
    }
    
    // Nuevo: <Decl> -> <Type> ID (ASSIGN <Expression>)? SEMICOLON
    private DeclarationStatement parseDeclaration() {
        int line = currentToken.getLine();
        String type = parseType();
        Token idToken = currentToken;
        consume(TokenType.ID);
        String id = idToken.getLexeme();
        
        Expression initialValue = null;
        if (check(TokenType.ASSIGN)) {
            consume(TokenType.ASSIGN);
            initialValue = parseExpression();
        }
        
        consume(TokenType.SEMICOLON);
        return new DeclarationStatement(line, type, id, initialValue);
    }

    // Nuevo: <ReturnStmt> -> RETURN <Expression> SEMICOLON
    private ReturnStatement parseReturnStatement() {
        int line = currentToken.getLine();
        consume(TokenType.RETURN);
        Expression value = parseExpression();
        consume(TokenType.SEMICOLON);
        return new ReturnStatement(line, value);
    }
    
    // Nuevo: Maneja Asignación o Llamada a Función al inicio de una línea
    private ASTNode parseAssignmentOrCall() {
        int line = currentToken.getLine();
        Token idToken = currentToken;
        
        // Debe empezar con ID
        if (!check(TokenType.ID)) {
             throw new RuntimeException("Error Sintáctico en línea " + line + ": Se esperaba ID, { o palabra clave de sentencia.");
        }
        consume(TokenType.ID);
        
        if (check(TokenType.LPAREN)) {
            // Es una Llamada a Función: ID LPAREN <ArgumentList> RPAREN SEMICOLON
            FunctionCall call = parseFunctionCall(idToken.getLexeme(), line);
            consume(TokenType.SEMICOLON);
            return call;
        } else if (check(TokenType.ASSIGN)) {
            // Es una Asignación: ID ASSIGN <Expression> SEMICOLON
            consume(TokenType.ASSIGN);
            Expression value = parseExpression();
            consume(TokenType.SEMICOLON);
            return new AssignmentStatement(line, idToken.getLexeme(), value);
        }
        
        throw new RuntimeException("Error Sintáctico en línea " + line + ": Asignación o llamada a función mal formada.");
    }

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
    
    private WhileStatement parseWhileStatement() {
        
        int line = currentToken.getLine();
        consume(TokenType.WHILE);
        
        consume(TokenType.LPAREN);
        Expression condition = parseExpression();
        consume(TokenType.RPAREN);

        ASTNode body = parseStatement();

        return new WhileStatement(line, condition, body);
    }

    // *** MÉTODOS DE EXPRESIONES (Jerarquía de Precedencia) ***
    
    public Expression parseExpression() {
        return parseLogicOr();
    }
    
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

    private Expression parseLogicAnd() {
        Expression expr = parseEquality(); // Llama al siguiente nivel de precedencia
        
        while (check(TokenType.AND)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseEquality();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }
    
    
    private Expression parseEquality() {
        Expression expr = parseRelational(); // Llama al siguiente nivel de precedencia

        while (check(TokenType.EQ, TokenType.NEQ)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseRelational();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }

    
    // <Relational> -> <Additive> ( (LT | GT | LTE | GTE) <Additive> )*
    private Expression parseRelational() {
        Expression expr = parseAdditive(); // Llama al siguiente nivel de precedencia

        while (check(TokenType.LT, TokenType.GT, TokenType.LTE, TokenType.GTE)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseAdditive();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }
    
    
    // <Additive> -> <Multiplicative> ( (PLUS | MINUS) <Multiplicative> )*
    private Expression parseAdditive() {
        Expression expr = parseMultiplicative(); // Llama al siguiente nivel de precedencia

        while (check(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseMultiplicative();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }

    
    // <Multiplicative> -> <Unary> ( (MULT | DIV) <Unary> )*
    private Expression parseMultiplicative() {
        Expression expr = parseUnary(); // Llama al siguiente nivel de precedencia

        while (check(TokenType.MULT, TokenType.DIV)) {
            Token operator = currentToken;
            consume(operator.getType());
            Expression right = parseUnary();
            expr = new BinaryExpression(expr.getLine(), operator, expr, right);
        }
        return expr;
    }

    
    // <Unary> -> (NOT | MINUS) <Unary> | <Primary>
    private Expression parseUnary() {
        if (check(TokenType.NOT, TokenType.MINUS)) {
            Token operator = currentToken;
            consume(operator.getType());
            // Llamada recursiva para manejar operadores unarios múltiples (ej. !!x)
            Expression operand = parseUnary(); 
            return new UnaryExpression(operator.getLine(), operator, operand);
        }
        
        return parsePrimary();
    }
    
    
    // <Primary> -> INTEGER_LITERAL | TRUE | FALSE | ID | <FunCall> | ( <Expression> )
    private Expression parsePrimary() {
        int line = currentToken.getLine();
        
        if (check(TokenType.INTEGER_LITERAL)) {
            Token literalToken = currentToken;
            consume(TokenType.INTEGER_LITERAL);
            return new LiteralExpression(line, literalToken.getLiteralValue());
        }
        
        if (check(TokenType.TRUE, TokenType.FALSE)) {
            Token literalToken = currentToken;
            consume(literalToken.getType());
            boolean value = literalToken.getType() == TokenType.TRUE;
            return new LiteralExpression(line, value);
        }
        
        if (check(TokenType.LPAREN)) {
            consume(TokenType.LPAREN);
            Expression expr = parseExpression(); // Recurrencia
            consume(TokenType.RPAREN);
            return expr;
        }

        if (check(TokenType.ID)) {
            Token idToken = currentToken;
            consume(TokenType.ID);
            
            
            if (check(TokenType.LPAREN)) {
                return parseFunctionCall(idToken.getLexeme(), line);
            }
            
            // Es solo acceso a una variable
            return new VariableAccess(line, idToken.getLexeme());
        }
        
        throw new RuntimeException("Error Sintáctico en línea " + line 
            + ": Se esperaba una expresión primaria, pero se encontró " + currentToken.getType());
    }

    
    private FunctionCall parseFunctionCall(String id, int line) {
        // Asume que el token ID ya fue consumido en parsePrimary() o parseAssignmentOrCall()
        consume(TokenType.LPAREN);
        
        List<Expression> arguments = new ArrayList<>();
        if (!check(TokenType.RPAREN)) { 
            arguments.add(parseExpression()); 
            while (check(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                arguments.add(parseExpression());
            }
        }
        
        consume(TokenType.RPAREN);
        return new FunctionCall(line, id, arguments);
    }
}