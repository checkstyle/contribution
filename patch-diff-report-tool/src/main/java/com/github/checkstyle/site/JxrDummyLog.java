package com.github.checkstyle.site;

import org.apache.maven.jxr.log.Log;

/**
 * Dummy log used by maven-jxr PackageManager.
 *
 * @author atta_troll
 *
 */
public class JxrDummyLog implements Log {

    @Override
    public void debug(String arg0) {

    }

    @Override
    public void error(String arg0) {
        System.out.println(arg0);

    }

    @Override
    public void info(String arg0) {

    }

    @Override
    public void warn(String arg0) {
        System.out.println(arg0);

    }

}
