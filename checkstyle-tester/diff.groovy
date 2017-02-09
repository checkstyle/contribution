import java.nio.file.Paths

import static java.lang.System.err

static void main(String[] args) {
    def cliOptions = getCliOptions(args)
    if (areValidCliOptions(cliOptions)) {
        def localGitRepo = new File(cliOptions.localGitRepo)
        if (hasUnstagedChanges(localGitRepo)) {
            throw new IllegalStateException("Error: git repository ${localGitRepo.getPath()} has unstaged changes!")
        }

        def baseBranch = cliOptions.baseBranch
        if (!baseBranch) {
            baseBranch = 'master'
        }

        def baseConfig = cliOptions.baseConfig
        def patchConfig = cliOptions.patchConfig
        def config = cliOptions.config
        if (config) {
            baseConfig = config
            patchConfig = config
        }

        def patchBranch = cliOptions.patchBranch
        def listOfProjects = cliOptions.listOfProjects
        def checkstyleCfg = cliOptions.checkstyleCfg

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

        generateCheckstyleReport(localGitRepo, baseBranch, baseConfig, listOfProjects, tmpMasterReportsDir)
        generateCheckstyleReport(localGitRepo, patchBranch, patchConfig, listOfProjects, tmpPatchReportsDir)
        deleteDir(reportsDir)
        moveDir(tmpReportsDir, reportsDir)
        generateDiffReport(reportsDir, masterReportsDir, patchReportsDir, baseConfig, patchConfig)
        generateSummaryIndexHtml(diffDir)
    }
    else {
        throw new IllegalArgumentException('Error: invalid command line arguments!')
    }
}

def getCliOptions(args) {
    def cliOptionsDescLineLength = 120
    def cli = new CliBuilder(usage:'groovy diff.groovy [options]', header: 'options:', width: cliOptionsDescLineLength)
    cli.with {
        r(longOpt: 'localGitRepo', args: 1, required: true, argName: 'path', 'Path to local git repository (required)')
        b(longOpt: 'baseBranch', args: 1, required: false, argName: 'branch_name', 'Base branch name. Default is master (optional, default is master)')
        p(longOpt: 'patchBranch', args: 1, required: true, argName: 'branch_name', 'Name of the patch branch in local git repository (required)')
        bc(longOpt: 'baseConfig', args: 1, required: false, argName: 'path', 'Path to the base checkstyle config file (required if patchConfig is specified)')
        pc(longOpt: 'patchConfig', args: 1, required: false, argName: 'path', 'Path to the patch checkstyle config file (required if baseConfig is specified)')
        c(longOpt: 'config', args: 1, required: false, argName: 'path', 'Path to the checkstyle config file (required if baseConfig and patchConfig are not secified)')
        l(longOpt: 'listOfProjects', args: 1, required: true, argName: 'path', 'Path to file which contains projects to test on (required)')
    }
    return cli.parse(args)
}

def areValidCliOptions(cliOptions) {
    def valid = true
    def baseConfig = cliOptions.baseConfig
    def patchConfig = cliOptions.patchConfig
    def config = cliOptions.config
    def localGitRepo = new File(cliOptions.localGitRepo)
    def patchBranch = cliOptions.patchBranch
    def baseBranch = cliOptions.baseBranch

    if (!isValidCheckstyleConfigsCombination(config, baseConfig, patchConfig)) {
        valid = false
    }
    else if (!isValidGitRepo(localGitRepo)) {
        err.println "Error: $localGitRepo is not a valid git repository!"
        valid = false
    }
    else if (!isExistingGitBranch(localGitRepo, patchBranch)) {
        err.println "Error: $patchBranch is not an exiting git branch!"
        valid = false
    }
    else if (baseBranch && !isExistingGitBranch(localGitRepo, baseBranch)) {
        err.println "Error: $baseBranch is not an existing git branch!"
        valid = false
    }

    return valid
}

def isValidCheckstyleConfigsCombination(config, baseConfig, patchConfig) {
    def valid = true
    if (config && patchConfig
            || config && baseConfig
            || config && patchConfig && baseConfig) {
        err.println "Error: you should specify either \'config\' or \'baseConfig\' and \'patchConfig\'!"
        valid = false
    }
    else if (baseConfig && !patchConfig) {
        err.println "Error: \'patchConfig\' should be specified!"
        valid = false
    }
    else if (patchConfig && !baseConfig) {
        err.println "Error: \'baseConfig\' should be specified!"
        valid = false
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
    if (gitStatusOutput.contains("nothing to commit")) {
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

def generateCheckstyleReport(localGitRepo, branch, checkstyleCfg, listOfProjects, destDir) {
    println "Installing Checkstyle artifact ($branch) into local Maven repository ..."
    executeCmd("git checkout $branch", localGitRepo)

    def testerCheckstyleVersion = getCheckstyleVersionFromPomXml('./pom.xml', 'checkstyle.version')
    def checkstyleVersionInLocalRepo = getCheckstyleVersionFromPomXml("$localGitRepo/pom.xml", 'version')
    if (testerCheckstyleVersion != checkstyleVersionInLocalRepo) {
        throw new GroovyRuntimeException("Error: config version mis-match!\nCheckstyle version in tester's pom.xml is $testerCheckstyleVersion\nCheckstyle version in local repo is $checkstyleVersionInLocalRepo")
    }

    executeCmd("mvn -Pno-validations clean install", localGitRepo)
    executeCmd("groovy launch.groovy --listOfProjects $listOfProjects --config $checkstyleCfg --ignoreExceptions")
    println "Moving Checkstyle report into $destDir ..."
    moveDir("reports", destDir)
}

def generateDiffReport(reportsDir, masterReportsDir, patchReportsDir, baseConfig, patchConfig) {
    def diffToolDir = Paths.get("").toAbsolutePath()
        .getParent()
        .resolve("patch-diff-report-tool")
        .toFile()
    executeCmd("mvn clean package -DskipTests", diffToolDir)
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
