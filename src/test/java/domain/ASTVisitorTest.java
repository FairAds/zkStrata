package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zkstrata.compiler.Arguments;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Node;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.parser.ast.types.Identifier;
import zkstrata.parser.ast.types.IntegerLiteral;
import zkstrata.utils.ArgumentsBuilder;
import zkstrata.utils.Constants;

import java.util.Collections;
import java.util.List;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class ASTVisitorTest {
    private static final String SOURCE = "test";
    private static final String STATEMENT = "statement";
    private static final List<Subject> NO_SUBJECTS = Collections.emptyList();

    private static final Subject SUBJECT = createSubject(true, "");

    private static final Identifier IDENTIFIER = createIdentifier("", "String");
    private static final IntegerLiteral INT_NEG = createIntegerLiteral(-10);
    private static final IntegerLiteral INT_13 = createIntegerLiteral(13);
    private static final IntegerLiteral INT_LARGE = createIntegerLiteral(Constants.UNSIGNED_65BIT_MIN);

    // TODO: either here or integration test: negative number, number that is out of range

    // TODO: undefined schema
    // TODO: reserved keyword

    private ASTVisitor visitor;

    @BeforeEach
    void setup() {
        Arguments args = new ArgumentsBuilder(ASTVisitorTest.class)
                .withSchema("schema", "schema")
                .build();
        this.visitor = new ASTVisitor(args, "test");
    }

    @Test
    void Missing_Clause_Visitor_Should_Throw() {
        Node unknownClause = Mockito.mock(Node.class);
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, NO_SUBJECTS, unknownClause);

        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("missing visitor for ast node"));
    }

    @Test
    void Missing_Gadget_Implementation_Should_Throw() {
        Predicate predicateClause = Mockito.mock(Predicate.class);
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, NO_SUBJECTS, predicateClause);

        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("missing gadget implementation"));
    }

    @Test
    void Number_Negative_Should_Throw() {
        BoundsCheck boundsCheckGadget = new BoundsCheck(IDENTIFIER, INT_NEG, INT_13, getAbsPosition());
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), boundsCheckGadget);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("negative number"));
    }


    @Test
    void Number_Too_Large_Should_Throw() {
        BoundsCheck boundsCheckGadget = new BoundsCheck(IDENTIFIER, INT_13, INT_LARGE, getAbsPosition());
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), boundsCheckGadget);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("number too large"));
    }
}
