package com.puppycrawl.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for command line managing.
 * @author Vladislav Lisetskii
 */
public final class Main {

    /** Name for the option 'r'. */
    private static final String OPTION_R_NAME = "r";

    /** Name for the option 'o'. */
    private static final String OPTION_O_NAME = "o";

    /** Prevent instantiating. */
    private Main() {
    }

    /**
     * Enter point.
     * @param args the command line arguments.
     * @throws IOException if there is a problem with files access.
     * @throws JDOMException if there is a problem with xml structure.
     */
    public static void main(String... args) throws IOException, JDOMException {
        int exitStatus = 0;
        final int exitWithCliViolation = -1;

        try {
            final CommandLine commandLine = parseCli(args);
            final List<String> messages = validateCli(commandLine);
            if (messages.isEmpty()) {
                runCli(commandLine);
            }
            else {
                exitStatus = exitWithCliViolation;
                messages.forEach(System.out::println);
            }
        }
        catch (ParseException pex) {
            exitStatus = exitWithCliViolation;
            System.out.println(pex.getMessage());
        }
        finally {
            if (exitStatus != 0) {
                System.exit(exitStatus);
            }
        }
    }

    /**
     * Parses command line parameters.
     * @param args command line parameters.
     * @return parsed information about passed parameters.
     * @throws ParseException when parsed arguments are not valid.
     */
    private static CommandLine parseCli(String... args) throws ParseException {
        final CommandLineParser clp = new DefaultParser();
        return clp.parse(buildOptions(), args);
    }

    /**
     * Do validation of Command line options.
     * @param commandLine command line object
     * @return list of violations
     */
    private static List<String> validateCli(CommandLine commandLine) {
        final List<String> result = new ArrayList<>();
        if (!commandLine.hasOption(OPTION_R_NAME)) {
            result.add("A release notes input file must be specified.");
        }
        if (!commandLine.hasOption(OPTION_O_NAME)) {
            result.add("An output folder location must be specified.");
        }
        return result;
    }

    /**
     * Do execution of PostsBuilder based on Command line options.
     * @param commandLine command line object
     * @throws IOException if a file could not be read.
     * @throws JDOMException if there is a problem with xml structure.
     */
    private static void runCli(CommandLine commandLine) throws JDOMException, IOException {
        final CliOptions config = convertCliToPojo(commandLine);

        final File input = new File(config.getReleaseNotes());
        final SAXBuilder builder = new SAXBuilder();

        final Document document = builder.build(input);
        final Element rootNode = document.getRootElement();

        final PostBuilder postBuilder = new PostBuilder(rootNode, config);
        postBuilder.writeFullPost();
        postBuilder.writeShortPost();
    }

    /**
     * Util method to convert CommandLine type to POJO object.
     * @param commandLine command line object.
     * @return command line option as a POJO object.
     */
    private static CliOptions convertCliToPojo(CommandLine commandLine) {
        final CliOptions conf = new CliOptions();
        conf.setReleaseNotes(commandLine.getOptionValue(OPTION_R_NAME));
        conf.setOutputLocation(commandLine.getOptionValue(OPTION_O_NAME));
        return conf;
    }

    /**
     * Builds and returns list of parameters supported by cli.
     * @return available options.
     */
    private static Options buildOptions() {
        final Options options = new Options();
        options.addOption(OPTION_R_NAME, true, "Sets the path to the input release notes file.");
        options.addOption(OPTION_O_NAME, true, "Sets the result destination.");
        return options;
    }
}
