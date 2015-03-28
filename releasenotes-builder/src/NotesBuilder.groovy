
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

                        
            def index = line.indexOf(". Author")
            def author = line.substring(index + 8 + 1, line.size())
          
            def message = line.substring(0, index)
            def issueIndex = message.indexOf(", issue")
            def messageContent = message
            if (issueIndex != -1) {
                messageContent = message.substring(0, issueIndex)
            }
            
            def issue = ""
            if (issueIndex != -1) {
               issue = message.substring(message.lastIndexOf(',')+2, message.size())
               issue = issue.split("#")[1]
            }
            
//            if (issue == "") {
//                println "PARSE ERROR: (no issue), LINE:" + line;
//                System.exit(1)
//            }
            
            if (issue != "") {
            print """
        <li>
          $messageContent. Author: $author <a href="https://github.com/checkstyle/checkstyle/issues/$issue">#$issue</a>
        </li>""" 
            } else {
                print """
        <li>
          $messageContent. Author: $author
        </li>""" 
            
            }
        
        }
    }
}
