package zkstrata.api.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static zkstrata.compiler.Arguments.Statement;

public class CommandLineInterface {
    private Options options;

    public CommandLineInterface() {
        this.options = new OptionBuilder().withLongOpts().withFlags().build();
    }

    /**
     * Parses the supplied arguments using Apache Commons CLI.
     *
     * @param args Command line arguments.
     * @return Parsed arguments for the zkStrata compiler to use.
     */
    public Arguments parse(String[] args) {
        try {
            return parseArguments(args);
        } catch (ParseException e) {
            printHelp();
        }

        throw new IllegalStateException();
    }

    private Arguments parseArguments(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        checkFlags(parser.parse(new OptionBuilder().withFlags().build(), args, true));
        CommandLine cmd = parser.parse(options, args);
        checkFlags(cmd);

        String file = getStatementFile(cmd);
        String name = getStatementName(file);
        Statement statement = new Statement(file, getStatement(file));
        List<Statement> premises = getPremises(cmd);

        HashMap<String, ValueAccessor> witnessFiles = getWitnessData(cmd);
        HashMap<String, Schema> schemaFiles = getSchemas(cmd);
        HashMap<String, ValueAccessor> instanceFiles = getInstanceData(cmd);

        return new Arguments(name, statement, premises, witnessFiles, instanceFiles, schemaFiles);
    }

    /**
     * Checks whether special option flags were used.
     *
     * @param cmd {@link CommandLine} object that represents a list of arguments
     */
    private void checkFlags(CommandLine cmd) {
        if (cmd.hasOption("help"))
            printHelp();

        if (cmd.hasOption("version"))
            printVersion();

        if (cmd.hasOption("verbose"))
            setVerbose();
    }

    /**
     * Terminates the execution with the help information.
     */
    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        formatter.setOptionComparator(null); // print options in the order they were added
        String name = Compiler.class.getPackage().getImplementationTitle();
        String header = "Compiles the given zkStrata statement into an intermediate representation of gadgets.";
        formatter.printHelp(name, header, options, null, true);
        System.exit(1);
    }

    /**
     * Terminates the execution with the version information.
     */
    private void printVersion() {
        String name = Compiler.class.getPackage().getImplementationTitle();
        String version = Compiler.class.getPackage().getImplementationVersion();
        System.out.println(String.format("%s %s", name, version));
        System.exit(1);
    }

    private void setVerbose() {
        Configurator.setRootLevel(Level.DEBUG);
    }

    private String getStatementFile(CommandLine cmd) {
        return cmd.getOptionValue("statement");
    }

    private String getStatementName(String file) {
        return FilenameUtils.getBaseName(file);
    }

    private String getStatement(String file) {
        try {
            return Files.readString(Path.of(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to read file: %s", file));
        }
    }

    private HashMap<String, Schema> getSchemas(CommandLine cmd) {
        HashMap<String, Schema> schemas = new HashMap<>();
        if (cmd.hasOption("schemas")) {
            for (String schema : cmd.getOptionValues("schemas")) {
                String[] parts = schema.split("=");
                if (parts.length != 2) {
                    String msg = String.format("Malformed schema provided as argument: %s", schema);
                    throw new IllegalArgumentException(msg);
                }
                schemas.put(parts[0], new JsonSchema(parts[1], parts[0]));
            }
        }
        return schemas;
    }

    private HashMap<String, ValueAccessor> getWitnessData(CommandLine cmd) {
        HashMap<String, ValueAccessor> witnessData = new HashMap<>();
        if (cmd.hasOption("witness-data")) {
            for (String witness : cmd.getOptionValues("witness-data")) {
                String[] parts = witness.split("=");
                if (parts.length != 2) {
                    String msg = String.format("Malformed witness data provided as argument: %s", witness);
                    throw new IllegalArgumentException(msg);
                }
                witnessData.put(parts[0], new JsonAccessor(parts[1]));
            }
        }
        return witnessData;
    }

    private HashMap<String, ValueAccessor> getInstanceData(CommandLine cmd) {
        HashMap<String, ValueAccessor> instanceData = new HashMap<>();
        if (cmd.hasOption("instance-data")) {
            for (String instance : cmd.getOptionValues("instance-data")) {
                String[] parts = instance.split("=");
                if (parts.length != 2) {
                    String msg = String.format("Malformed instance data provided as argument: %s", instance);
                    throw new IllegalArgumentException(msg);
                }
                instanceData.put(parts[0], new JsonAccessor(parts[1]));
            }
        }
        return instanceData;
    }

    private List<Statement> getPremises(CommandLine cmd) {
        List<Statement> premises = new ArrayList<>();
        if (cmd.hasOption("premises"))
            for (String file : cmd.getOptionValues("premises"))
                premises.add(new Statement(file, getStatement(file)));

        return premises;
    }
}
