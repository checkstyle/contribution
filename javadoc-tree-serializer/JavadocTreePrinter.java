package com.puppycrawl.tools.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;

public class JavadocTreePrinter
{

    private static final String PREFIX = "|--";

    public static void printTree(DetailNode aRoot)
    {

        if (aRoot.getChildren().length != 0) {
            for (DetailNode node : aRoot.getChildren()) {
                System.out.println(getLevel(node) + node.toString()
                        + " : ["
                        + node.getText().replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r")
                        .replaceAll("\t", "\\\\t")
                        + "]");

                if (node.getType() == JavadocTokenTypes.TEXT) {
                    continue;
                }

                printTree(node);
            }
        }
    }

    private static String getLevel(DetailNode aNode)
    {
        DetailNode parent = aNode;
        String level = "";
        while (parent.getParent() != null) {
            parent = parent.getParent();
            if (parent.getParent() != null) {
                level = level + "    ";
            }
            else {
                level = level + PREFIX;
            }
        }
        return level;
    }
}
