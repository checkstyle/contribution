import java.nio.file.Paths

import static java.lang.System.err

static void main(String[] args) {
    def cli = new CliBuilder(usage:'groovy diff.groovy [options]')
    cli.with {
        r(longOpt: 'localGitRepo', args: 1, argName: 'localGitRepo', 'Path to local git repository')
        b(longOpt: 'baseBranch', args: 1, argName: 'baseBranch', 'Base branch name. Default is master')
        p(longOpt: 'patchBranch', args: 1, argName: 'patchBranch', 'Name of the patch branch in local git repository')
        c(longOpt: 'checkstyleCfg', args: 1, argName: 'checkstyleCfg', 'Path to checkstyle config file')
        l(longOpt: 'projectsToTestOn', args: 1, argName: 'projectsToTestOn', 'Path to file which contains projects to test on')
    }
    def options = cli.parse(args)

    if (areValidCliArgs(options)) {
        def localGitRepo = new File(options.localGitRepo)
        if (hasUnstagedChanges(localGitRepo)) {
            throw new IllegalStateException("Error: git repository ${localGitRepo.getPath()} has unstaged changes!")
        }

        def baseBranch = options.baseBranch
        if (!baseBranch) {
            baseBranch = 'master'
        }

        def patchBranch = options.patchBranch
        def projectsToTestOn = options.projectsToTestOn
        def checkstyleCfg = options.checkstyleCfg

        def reportsDir = 'reports'
        def masterReportsDir = "$reportsDir/$baseBranch"
        def patchReportsDir = "$reportsDir/$patchBranch"

        def tmpReportsDir = 'tmp_reports'
        def tmpMasterReportsDir = "$tmpReportsDir/$baseBranch"
        def tmpPatchReportsDir = "$tmpReportsDir/$patchBranch"

        def diffDir = "$reportsDir/diff"

        // Delete work directories to avoid conflicts with previous reports generation
        if (new File(reportsDir).exists()) {
            deleteDir(reportsDir)
        }
        if (new File(tmpReportsDir).exists()) {
            deleteDir(tmpReportsDir)
        }

        generateCheckstyleReport(localGitRepo, baseBranch, checkstyleCfg, projectsToTestOn, tmpMasterReportsDir)
        generateCheckstyleReport(localGitRepo, patchBranch, checkstyleCfg, projectsToTestOn, tmpPatchReportsDir)
        deleteDir(reportsDir)
        moveDir(tmpReportsDir, reportsDir)
        generateDiffReport(reportsDir, masterReportsDir, patchReportsDir, checkstyleCfg)
        generateSummaryIndexHtml(diffDir)
    }
    else {
        throw new IllegalArgumentException('Error: invalid command line arguments!')
    }
}

def areValidCliArgs(options) {
    def valid = true
    if (options == null) {
        valid = false
    }
    else {
        if (options.checkstyleCfg
                && options.localGitRepo
                && options.patchBranch
                && options.projectsToTestOn) {
            def localGitRepo = new File(options.localGitRepo)
            def patchBranch = options.patchBranch
            def baseBranch = options.baseBranch
            if (!isValidGitRepo(localGitRepo)
                    || !isExistingGitBranch(localGitRepo, patchBranch)
                    || (baseBranch && !isExistingGitBranch(localGitRepo, baseBranch))) {
                valid = false
            }
        }
        else {
            err.println 'Error: wrong number of command line arguments!'
            valid = false
        }
    }
    return valid
}

def isValidGitRepo(gitRepoDir) {
    def valid = true
    if (gitRepoDir.exists() && gitRepoDir.isDirectory()) {
        def gitStatusCmd = "git status".execute(null, gitRepoDir)
        gitStatusCmd.waitFor()
        if (gitStatusCmd.exitValue() != 0) {
            err.println "Error: \'${gitRepoDir.getPath()}\' is not a git repository!"
            valid = false
        }
    }
    else {
        err.println "Error: \'${gitRepoDir.getPath()}\' does not exist or it is not a directory!"
        valid = false
    }
    return valid
}

def isExistingGitBranch(gitRepo, branchName) {
    def exist = true
    def gitRevParseCmd = "git rev-parse --verify $branchName".execute(null, gitRepo)
    gitRevParseCmd.waitFor()
    if (gitRevParseCmd.exitValue() != 0) {
        err.println "Error: git repository ${gitRepo.getPath()} does not have a branch with name \'$branchName\'!"
        exist = false
    }
    return exist
}

def hasUnstagedChanges(gitRepo) {
    def hasUnstagedChanges = true
    def gitStatusCmd = "git status".execute(null, gitRepo)
    gitStatusCmd.waitFor()
    def gitStatusOutput = gitStatusCmd.text
    if (gitStatusOutput.contains("nothing to commit, working directory clean")) {
        hasUnstagedChanges = false
    }
    return hasUnstagedChanges
}

def getCheckstyleVersionFromPomXml(pathToPomXml, xmlTagName) {
    def pomXmlFile = new File(pathToPomXml)
    def checkstyleVersion
    pomXmlFile.eachLine {
        line ->
            if (line.matches("^.*<$xmlTagName>.*-SNAPSHOT</$xmlTagName>.*")) {
                checkstyleVersion = line.substring(line.indexOf('>') + 1, line.lastIndexOf('<'))
                return true
            }
    }
    if (checkstyleVersion == null) {
        throw GroovyRuntimeException("Error: cannot get Checkstyle version from $pathToPomXml!")
    }
    return checkstyleVersion
}

def generateCheckstyleReport(localGitRepo, branch, checkstyleCfg, projectsToTestOn, destDir) {
    println "Installing Checkstyle artifact ($branch) into local Maven repository ..."
    executeCmd("git checkout $branch", localGitRepo)

    def testerCheckstyleVersion = getCheckstyleVersionFromPomXml('./pom.xml', 'checkstyle.version')
    def checkstyleVersionInLocalRepo = getCheckstyleVersionFromPomXml("$localGitRepo/pom.xml", 'version')
    if (testerCheckstyleVersion != checkstyleVersionInLocalRepo) {
        throw new GroovyRuntimeException("Error: config version mis-match!\nCheckstyle version in tester's pom.xml is $testerCheckstyleVersion\nCheckstyle version in local repo is $checkstyleVersionInLocalRepo")
    }

    executeCmd("mvn -Pno-validations clean install", localGitRepo)
    executeCmd("groovy launch.groovy $projectsToTestOn $checkstyleCfg")
    println "Moving Checkstyle report into $destDir ..."
    moveDir("reports", destDir)
}

def generateDiffReport(reportsDir, masterReportsDir, patchReportsDir, checkstyleCfg) {
    def diffToolDir = Paths.get("").toAbsolutePath()
        .getParent()
        .resolve("patch-diff-report-tool")
        .toFile()
    executeCmd("mvn clean package", diffToolDir)
    def diffToolJarPath = getPathToDiffToolJar(diffToolDir)

    println 'Starting diff report generation ...'
    Paths.get(masterReportsDir).toFile().eachFile {
        fileObj ->
            if (fileObj.isDirectory()) {
                def projectName = fileObj.getName()
                def patchReportDir = new File("$patchReportsDir/$projectName")
                if (patchReportDir.exists()) {
                    def baseReport = "$masterReportsDir/$projectName/checkstyle-result.xml"
                    def patchReport = "$patchReportsDir/$projectName/checkstyle-result.xml"
                    def outputDir = "$reportsDir/diff/$projectName"
                    def baseConfig = checkstyleCfg
                    def patchConfig = checkstyleCfg
                    def diffCmd = "java -jar $diffToolJarPath --baseReport $baseReport --patchReport $patchReport --output $outputDir --baseConfig $baseConfig --patchConfig $patchConfig"
                    executeCmd(diffCmd)
                } else {
                    throw new FileNotFoundException("Error: patch report for project $projectName is not found!")
                }
            }
    }
    println 'Diff report generation finished ...'
}

def getPathToDiffToolJar(diffToolDir) {
    def targetDir = diffToolDir.getAbsolutePath() + '/target/'
    def pathToDiffToolJar
    Paths.get(targetDir).toFile().eachFile {
        fileObj ->
            def jarPattern = "patch-diff-report-tool-.*.jar-with-dependencies.jar"
            def fileName = fileObj.getName()
            if (fileName.matches(jarPattern)) {
                pathToDiffToolJar = fileObj.getAbsolutePath()
                return true
            }
    }
    if (pathToDiffToolJar == null) {
        throw new FileNotFoundException("Error: difff tool jar file is not found!")
    }
    return pathToDiffToolJar
}

def generateSummaryIndexHtml(diffDir) {
    println 'Starting creating report summary page ...'
    def projectsStatistic = getProjectsStatistic(diffDir)
    def summaryIndexHtml = new File("$diffDir/index.html")

    summaryIndexHtml << ('<html><body>')
    summaryIndexHtml << ('\n')
    projectsStatistic.each {
        project, diffCount ->
            summaryIndexHtml << ("<a href='$project/index.html'>$project</a>")
            if (diffCount.compareTo(0) != 0) {
                summaryIndexHtml << (" ($diffCount)")
            }
            summaryIndexHtml << ('<br />')
            summaryIndexHtml << ('\n')
    }
    summaryIndexHtml << ('</body></html>')

    println 'Creating report summary page finished...'
}

def getProjectsStatistic(diffDir) {
    def projectsStatistic = new HashMap<>()
    Paths.get(diffDir).toFile().eachFile {
        fileObjf ->
            if (fileObjf.isDirectory()) {
                def projectName = fileObjf.getName()
                def indexHtmlFile = new File(fileObjf.getAbsolutePath() + '/index.html')
                indexHtmlFile.eachLine {
                    line ->
                        if (line.matches(".*totalDiff\">[0-9]+.*")) {
                            def totalDiff = Integer.valueOf(line.substring(line.indexOf('>') + 1, line.lastIndexOf('<')))
                            projectsStatistic.put(projectName, totalDiff)
                        }
                }
            }
    }
    return projectsStatistic
}

def moveDir(source, destination) {
    new AntBuilder().move(todir: destination) {
        fileset(dir: source)
    }
}

def deleteDir(dir) {
    new AntBuilder().delete(dir: dir, failonerror: false)
}

def executeCmd(cmd, dir = new File("./")) {
    def proc = cmd.execute(null, dir)
    proc.consumeProcessOutput(System.out, System.err)
    proc.waitFor()
    if (proc.exitValue() != 0) {
        throw new GroovyRuntimeException("Error: ${proc.err.text}!")
    }
}
