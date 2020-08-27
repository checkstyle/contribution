////////////////////////////////////////////////////////////////////////////////
// How to use this script:
//
// options:
//  -c , --checkstyleDir<path> Location of local Checkstyle repo(required).
//  -o , --outputDir<path> Location where tool will populate finished reports(required).
//  -p , --patchBranch<branch_name> Name of the branch run check regression reports
//      for (required).
//  -t , --configDir<path> Location where configurations reside(required).
//
// Examples (note that we have used '\' as a line continuation, as in BASH):
//
//  - With short names:
//
//    groovy multi_config_check_regression.groovy - c ~/IdeaProjects/\
//    checkstyle -o output/ -t configs/ -p your-branch-here
//
//  -With long names:
//
//    groovy multi_config_check_regression.groovy -checkstyleDir \
//    ~/IdeaProjects/checkstyle - outputDir output/ -configDir configs/ \
//    -patchBranch your-branch-here
//
// This script is used to run check regression with many configuration files, such as when
// making ANTLR grammar changes.
//
// The -configDir option should point to the directory with all
// configuration files that you will target for check regression. See
// https://github.com/checkstyle/contribution/tree/master/checkstyle-tester#check-regression-report
// for more information about best practices for separating checks.
////////////////////////////////////////////////////////////////////////////////

static void main(String[] args) {
    def cliOptions = getCliOptions(args)
        def checkstyleRepoDir = cliOptions.checkstyleDir
        def configFilesDir = cliOptions.configDir
        def outputDirectory = cliOptions.outputDir
        def patchBranch = cliOptions.patchBranch
        def projectsToTestOn = new File("projects-to-test-on.properties")

        println "Running check regression reports for patch branch: ${patchBranch}."
        println "Make sure that you have selected (uncommented)" +
            " correct projects from projects-to-test-on.properties!"

        def reportDirectoryName =
            java.time.LocalDate.now().toString() + "_" + patchBranch + "_reports";

        final File reportDirectoryPath = new File("${outputDirectory}" + reportDirectoryName)
        reportDirectoryPath.mkdirs()

        println "Report files will populate in ${reportDirectoryPath}"

        File folder = new File(configFilesDir)
        folder.eachFile { file ->
            if (file.name.endsWith(".xml")) {

                println "Running check regression report using config file ${file.toString()}"
                String command = "groovy diff.groovy -r ${checkstyleRepoDir} -b" +
                    " master -p ${patchBranch} -c ${file.toString()}" +
                    " -l ${projectsToTestOn.toString()}"

                try {
                   println command.execute().text;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String fileWithoutExt = file.name.take(file.name.lastIndexOf('.'))
                String reportName = "diff_report_" + fileWithoutExt
                println "Moving report to ${reportDirectoryPath}/${reportName}/..."
                String source = "reports/diff"
                String target = reportDirectoryPath.toString() + "/" + reportName

                try {
                    "cp -r ${source} ${target}".execute()
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
}

def getCliOptions(args) {
    def cliOptionsDescLineLength = 120
    def cli = new CliBuilder(usage: 'groovy multi_config_check_regression.groovy [options]',
        header: 'options:', width: cliOptionsDescLineLength)
    cli.with {
        c(longOpt: 'checkstyleDir', args: 1, required: true, argName: 'path',
            'Location of local Checkstyle repo (required).')
        o(longOpt: 'outputDir', args: 1, required: true, argName: 'path',
            'Location where tool will populate finished reports (required).')
        t(longOpt: 'configDir', args: 1, required: true, argName: 'path',
            'Location where configurations reside (required).')
        p(longOpt: 'patchBranch', args: 1, required: true, argName: 'branch_name', 'Name of ' +
            'the branch run check regression reports for (required).')
    }
    return cli.parse(args)
}
