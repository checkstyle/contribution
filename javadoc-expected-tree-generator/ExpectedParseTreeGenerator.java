////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.javadoc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.puppycrawl.tools.checkstyle.grammars.javadoc.JavadocLexer;
import com.puppycrawl.tools.checkstyle.grammars.javadoc.JavadocParser;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtils;

/**
 * Generates source code of methods that build ParseTree of given Javadoc comments.
 * List text files in 'inputNames' array. Only one Javadoc comment should be in each file.
 * A text file should not contain '/*' at the beginning and '*&#47;' at the end.
 *
 * This class is not stand-alone, it should be used in scope of Checkstyle project,
 * because it uses Javadoc parser, ANTLR classes, etc. Just copy this file to Checkstyle repo,
 * specify input file you are working on and execute it through 'main' method to generate
 * appropriate unit-test source code.
 */
public final class ExpectedParseTreeGenerator {
    private static String pathToInputs =
        "src/test/resources/com/puppycrawl/tools/checkstyle/grammars/javadoc/";

    /**
     * Input files from main Checkstyle repo
     */
    private static String[] inputNames = {
        "InputLeadingAsterisks.txt",
    };

    private Map<String, Integer> variableCounters = new HashMap<>();

    private ExpectedParseTreeGenerator() {
    }

    public static void main(String[] args) throws Exception {
        final ExpectedParseTreeGenerator generator = new ExpectedParseTreeGenerator();
        for (String inputName: inputNames) {
            final File file = new File(pathToInputs + inputName);
            final ParseTree generatedTree = parseJavadocFromFile(file);

            final String filename = file.getName();
            String treeName = filename;
            final String inputWord = "Input";
            if (filename.indexOf(inputWord) >= 0) {
                treeName = filename.substring(filename.indexOf(inputWord) + inputWord.length());
                if (treeName.lastIndexOf('.') >= 0) {
                    treeName = treeName.substring(0, treeName.lastIndexOf('.'));
                }
            }

            System.out.println("public static ParseTree tree" + treeName
                    + "()\n{");
            final String id = generator.walk(generatedTree, "null");
            generator.resetCounter();
            System.out.println("    return " + id + ";\n}\n");
        }
    }

    private static ParseTree parseJavadocFromFile(File file)
        throws IOException {
        final String content = Files.toString(file, Charsets.UTF_8);
        final InputStream in = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));

        final ANTLRInputStream input = new ANTLRInputStream(in);
        final JavadocLexer lexer = new JavadocLexer(input);
        lexer.removeErrorListeners();

        final BaseErrorListener errorListener = new FailOnErrorListener();
        lexer.addErrorListener(errorListener);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final JavadocParser parser = new JavadocParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return parser.javadoc();
    }

    private void resetCounter() {
        variableCounters.clear();
    }

    public String walk(ParseTree t, String parentObjectName) {
        final String className = t.getClass().getSimpleName();
        String id = null;

        if (t instanceof TerminalNode) {
            final TerminalNodeImpl terminal = (TerminalNodeImpl) t;
            final int type = terminal.symbol.getType();
            String tokenType = "";
            if (type == -1) {
                tokenType = "EOF";
            }
            else {
                tokenType = JavadocUtils.getTokenName(type);
            }
            String text = terminal.getText();
            if ("\n".equals(text)) {
                text = "\\n";
            }
            else if ("\t".equals(text)) {
                text = "\\t";
            }
            else {
                text = text.replace("\"", "\\\"");
            }

            final int number = getVariableCounter(tokenType);

            id = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tokenType.toLowerCase())
                    + number;

            System.out.println("    CommonToken " + id
                    + " = new CommonToken(JavadocTokenTypes." + tokenType
                    + ", \"" + text + "\");");
        }
        else {
            int number = getVariableCounter(className);

            id = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, className) + number++;

            System.out.println("    " + className + " " + id + " = new "
                    + className + "(" + parentObjectName + ", 0);");

            final int n = t.getChildCount();
            for (int i = 0; i < n; i++) {
                final String childId = walk(t.getChild(i), id);
                System.out.println("    " + id + ".addChild(" + childId + ");");
            }
        }
        return id;
    }

    private int getVariableCounter(String className) {
        int  number = 0;
        if (variableCounters.containsKey(className)) {
            number = variableCounters.get(className) + 1;
        }

        variableCounters.put(className, number);

        return number;
    }

    private static class FailOnErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer, Object offendingSymbol,
                int line, int charPositionInLine,
                String msg, RecognitionException e) {
            throw new RuntimeException("[" + line + ", " + charPositionInLine + "] " + msg);
        }
    }

}
