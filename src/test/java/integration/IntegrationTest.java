package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.ArgumentsBuilder;

public class IntegrationTest {
    @Test
    void Duplicate_Alias_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("duplicate_alias")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("is already defined"));
    }

    @Test
    void Alias_Private_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("alias_private")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("reserved keyword"));
    }

    @Test
    void Alias_Public_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("alias_public")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("reserved keyword"));
    }

    @Test
    void Validation_Rule_Default_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withInstance("pass", "passport.metadata")
                    .withSchema("passport_ch", "default_validation_rule")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void Validation_Rule_Missing_Instance_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("equality")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("missing instance data"));
    }

    @Test
    void Validation_Rule_Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "statement_default_contradiction")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Witness_Exposure_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("witness_exposure")
                    .withWitness("pass_w", "passport")
                    .withInstance("pass_w", "passport.metadata")
                    .withInstance("pass_i", "passport")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("simultaneously"));
    }

    @Test
    void Instance_Data_Referenced_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("instance_data_referenced")
                    .withWitness("pass_w", "passport")
                    .withInstance("pass_w", "passport.metadata")
                    .withInstance("pass_i", "passport_instance")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void Verbose_Subject_Definition_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("verbose_default")
                    .withWitness("pass_1", "passport")
                    .withWitness("pass_2", "passport")
                    .withInstance("pass_1", "passport.metadata")
                    .withInstance("pass_2", "passport.metadata")
                    .withInstance("pass_3", "passport_instance")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void Validation_Rules_Test_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "validation_rules")
                    .withWitness("pass", "passport")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void Undefined_Schema_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("undefined_schema")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("undefined schema"));
    }

    @Test
    void Missing_Witness_Data_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("missing_witness_data")
                    .withWitness("pass1", "passport")
                    .withInstance("pass1", "passport.metadata")
                    .withInstance("pass2", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("missing witness data"));
    }

    @Test
    void Duplicate_Set_Entries_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("duplicate_set_entries")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("duplicate element"));
    }

    @Test
    void Undeclared_Alias_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("undeclared_alias")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("undeclared alias"));
    }

    @Test
    void Missing_Entry_Should_Throw() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withWitness("pass", "passport_missing_entry")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("missing entry"));
    }

    @Test
    void Witness_Type_Mismatch_Should_Throw() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withWitness("pass", "passport_invalid_entry")
                    .build();
            Compiler.run(args);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
