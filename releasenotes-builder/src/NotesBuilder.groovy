
class NotesBuilder {

    static main(args) {
       def path = "input.txt"
       if (args.length != 0) {
           path = args[0]
       }

       doNotes(path)
    }

    static void doNotes(String filePath) {
        def f = new File(filePath)
        f.eachLine() { line ->
            //println "LINE:"+line

            if (line == "") {
              // keep separators from source
              print "\n"
              return // continue
            }

            def indexIssue = line.indexOf("Issue #")
            def indexPull = line.indexOf("Pull #")
            def indexPrefix = line.indexOf(":")
            if (indexIssue != 0 && indexPull != 0 && indexPrefix > 5) {
              throw new RuntimeException("strange format was detected at line: $line");
            }

            def issue = ""
            def indexMessage = line.indexOf(": ");
            if (indexIssue == 0) {
              // this is Issue
              issue = line.substring(7, indexMessage)
            } else if (indexPull == 0) {
              // this is Pull
              issue = line.substring(6, indexMessage)
            } else {
              // this is some minor
              issue = ""
            }

            def message = line.substring(indexMessage + 2, line.size())

            if (issue != "" && indexIssue == 0) {
              print """
        <li>
          $message <a href="https://github.com/checkstyle/checkstyle/issues/$issue">#$issue</a>
        </li>"""
            } else if (issue != "" && indexPull == 0) {
              print """
        <li>
          $message <a href="https://github.com/checkstyle/checkstyle/pull/$issue">#$issue</a>
        </li>"""
            }else {
                print """
        <li>
          $message
        </li>"""

            }

        }
        print """\n"""
    }
}
