//package parser;

package compilador.parser;

// Importar todas las categorías de nodos
import compilador.parser.expressions.*;
import compilador.parser.statements.*;
import compilador.parser.declarations.*;

/**
 * Interfaz que define el Patrón Visitor para recorrer el AST.
 * Cada fase del compilador (Semántico, Cuádruplas, Diagramas)
 * implementará esta interfaz.
 */
public interface ASTVisitor {
    
    // Nodos de Declaración
    Object visit(Program node);
    Object visit(FunctionDeclaration node);
    Object visit(Parameter node);

    // Nodos de Sentencias (Statements)
    Object visit(BlockStatement node);
    Object visit(DeclarationStatement node);
    Object visit(AssignmentStatement node);
    Object visit(IfStatement node);
    Object visit(WhileStatement node);
    Object visit(ReturnStatement node);

    // Nodos de Expresiones (Expressions)
    Object visit(BinaryExpression node);
    Object visit(UnaryExpression node);
    Object visit(FunctionCall node);
    Object visit(VariableAccess node);
    Object visit(LiteralExpression node);
    
    // Nota: La clase abstracta base 'Expression' no necesita un método 'visit' propio,
    
}
