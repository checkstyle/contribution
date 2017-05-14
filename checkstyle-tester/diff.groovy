import java.nio.file.Paths

import static java.lang.System.err

static void main(String[] args) {
    def cliOptions = getCliOptions(args)
    if (areValidCliOptions(cliOptions)) {
        def cfg = new Config(cliOptions)

        if (hasUnstagedChanges(cfg.localGitRepo)) {
            def exMsg = "Error: git repository ${cfg.localGitRepo.path} has unstaged changes!"
            throw new IllegalStateException(exMsg)
        }

        // Delete work directories to avoid conflicts with previous reports generation
        if (new File(cfg.reportsDir).exists()) {
            deleteDir(cfg.reportsDir)
        }
        if (new File(cfg.tmpReportsDir).exists()) {
            deleteDir(cfg.tmpReportsDir)
        }

        if (cfg.isDiffMode()) {
            generateCheckstyleReport(cfg.localGitRepo, cfg.baseBranch, cfg.baseConfig, cfg.listOfProjects, cfg.tmpMasterReportsDir)
        }
        generateCheckstyleReport(cfg.localGitRepo, cfg.patchBranch, cfg.patchConfig, cfg.listOfProjects, cfg.tmpPatchReportsDir)
        deleteDir(cfg.reportsDir)
        moveDir(cfg.tmpReportsDir, cfg.reportsDir)
        generateDiffReport(cfg.reportsDir, cfg.masterReportsDir, cfg.patchReportsDir, cfg.baseConfig, cfg.patchConfig, cfg.shortFilePaths, cfg.mode)
        generateSummaryIndexHtml(cfg.diffDir)
    }
    else {
        throw new IllegalArgumentException('Error: invalid command line arguments!')
    }
}

def getCliOptions(args) {
    def cliOptionsDescLineLength = 120
    def cli = new CliBuilder(usage:'groovy diff.groovy [options]', header: 'options:', width: cliOptionsDescLineLength)
    cli.with {
        r(longOpt: 'localGitRepo', args: 1, required: true, argName: 'path',
            'Path to local git repository (required)')
        b(longOpt: 'baseBranch', args: 1, required: false, argName: 'branch_name',
            'Base branch name. Default is master (optional, default is master)')
        p(longOpt: 'patchBranch', args: 1, required: true, argName: 'branch_name',
            'Name of the patch branch in local git repository (required)')
        bc(longOpt: 'baseConfig', args: 1, required: false, argName: 'path', 'Path to the base ' \
            + 'checkstyle config file (optional, if absent then the tool will use only ' \
            + 'patchBranch in case the tool mode is \'single\', otherwise baseBranch ' \
            + 'will be set to \'master\')')
        pc(longOpt: 'patchConfig', args: 1, required: false, argName: 'path',
            'Path to the patch checkstyle config file (required if baseConfig is specified)')
        c(longOpt: 'config', args: 1, required: false, argName: 'path', 'Path to the checkstyle ' \
            + 'config file (required if baseConfig and patchConfig are not secified)')
        l(longOpt: 'listOfProjects', args: 1, required: true, argName: 'path',
            'Path to file which contains projects to test on (required)')
        s(longOpt: 'shortFilePaths', required: false, 'Whether to save report file paths' \
            + ' as a shorter version to prevent long paths. (optional, default is false)')
        m(longOpt: 'mode', args: 1, required: false, argName: 'mode', 'The mode of the tool:' \
            + ' \'diff\' or \'single\'. (optional, default is \'diff\')')
    }
    return cli.parse(args)
}

def areValidCliOptions(cliOptions) {
    def valid = true
    def baseConfig = cliOptions.baseConfig
    def patchConfig = cliOptions.patchConfig
    def config = cliOptions.config
    def toolMode = cliOptions.mode
    def localGitRepo = new File(cliOptions.localGitRepo)
    def patchBranch = cliOptions.patchBranch
    def baseBranch = cliOptions.baseBranch

    if (toolMode && !('diff'.equals(toolMode) || 'single'.equals(toolMode))) {
        err.println "Error: Invalid mode: \'$toolMode\'. The mode should be \'single\' or \'diff\'!"
        valid = false
    }
    else if (!isValidCheckstyleConfigsCombination(config, baseConfig, patchConfig, toolMode)) {
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

def isValidCheckstyleConfigsCombination(config, baseConfig, patchConfig, toolMode) {
    def valid = true
    if (config && (patchConfig || baseConfig)) {
        err.println "Error: you should specify either \'config\'," \
            + " or \'baseConfig\' and \'patchConfig\', or \'patchConfig\' only!"
        valid = false
    }
    else if ('diff'.equals(toolMode) && baseConfig && !patchConfig) {
        err.println "Error: \'patchConfig\' should be specified!"
        valid = false
    }
    else if ('diff'.equals(toolMode) && patchConfig && !baseConfig) {
        err.println "Error: \'baseConfig\' should be specified!"
        valid = false
    }
    else if ('single'.equals(toolMode) && (baseConfig || config)) {
        err.println "Error: \'baseConfig\' and/or \'config\' should not be used in \'single\' mode!"
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
        throw new GroovyRuntimeException("Error: cannot get Checkstyle version from $pathToPomXml!")
    }
    return checkstyleVersion
}

def generateCheckstyleReport(localGitRepo, branch, checkstyleCfg, listOfProjects, destDir) {
    println "Installing Checkstyle artifact ($branch) into local Maven repository ..."
    executeCmd("git checkout $branch", localGitRepo)
    executeCmd("git log -1 --pretty=MSG:%s%nSHA-1:%H", localGitRepo)

    def testerCheckstyleVersion = getCheckstyleVersionFromPomXml('./pom.xml', 'checkstyle.version')
    def checkstyleVersionInLocalRepo = getCheckstyleVersionFromPomXml("$localGitRepo/pom.xml", 'version')
    if (testerCheckstyleVersion != checkstyleVersionInLocalRepo) {
        throw new GroovyRuntimeException("Error: config version mis-match!\nCheckstyle version in tester's pom.xml is $testerCheckstyleVersion\nCheckstyle version in local repo is $checkstyleVersionInLocalRepo")
    }

    executeCmd("mvn -Pno-validations clean install", localGitRepo)
    executeCmd("groovy launch.groovy --listOfProjects $listOfProjects --config $checkstyleCfg --ignoreExceptions --ignoreExcludes")
    println "Moving Checkstyle report into $destDir ..."
    moveDir("reports", destDir)
}

def generateDiffReport(reportsDir, masterReportsDir, patchReportsDir, baseConfig, patchConfig, shortFilePaths, toolMode) {
    def diffToolDir = Paths.get("").toAbsolutePath()
        .getParent()
        .resolve("patch-diff-report-tool")
        .toFile()
    executeCmd("mvn clean package -DskipTests", diffToolDir)
    def diffToolJarPath = getPathToDiffToolJar(diffToolDir)

    println 'Starting diff report generation ...'
    Paths.get(patchReportsDir).toFile().eachFile {
        fileObj ->
            if (fileObj.isDirectory()) {
                def projectName = fileObj.getName()
                def patchReportDir = new File("$patchReportsDir/$projectName")
                if (patchReportDir.exists()) {
                    def patchReport = "$patchReportsDir/$projectName/checkstyle-result.xml"
                    def outputDir = "$reportsDir/diff/$projectName"
                    def diffCmd = "java -jar $diffToolJarPath --patchReport $patchReport " \
                        + "--output $outputDir --patchConfig $patchConfig"
                    if ('diff'.equals(toolMode)) {
                        def baseReport = "$masterReportsDir/$projectName/checkstyle-result.xml"
                        diffCmd += " --baseReport $baseReport --baseConfig $baseConfig"
                    }
                    if (shortFilePaths) {
                        diffCmd += ' --shortFilePaths'
                    }
                    executeCmd(diffCmd)
                } else {
                    def exMsg = "Error: patch report for project $projectName is not found!"
                    throw new FileNotFoundException(exMsg)
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
    summaryIndexHtml << ('<h3><span style="color: #ff0000;">')
    summaryIndexHtml << ('<strong>WARNING: Excludes are ignored by diff.groovy.</strong>')
    summaryIndexHtml << ('</span></h3>')
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

def executeCmd(cmd, dir =  new File("").getAbsoluteFile()) {
    def osSpecificCmd = getOsSpecificCmd(cmd)
    def proc = osSpecificCmd.execute(null, dir)
    proc.consumeProcessOutput(System.out, System.err)
    proc.waitFor()
    if (proc.exitValue() != 0) {
        throw new GroovyRuntimeException("Error: ${proc.err.text}!")
    }
}

def getOsSpecificCmd(cmd) {
    def osSpecificCmd
    if (System.properties['os.name'].toLowerCase().contains('windows')) {
        osSpecificCmd = "cmd /c $cmd"
    }
    else {
        osSpecificCmd = cmd
    }
}

class Config {
    def localGitRepo
    def shortFilePaths
    def listOfProjects
    def mode

    def baseBranch
    def patchBranch

    def baseConfig
    def patchConfig
    def config

    def reportsDir
    def masterReportsDir
    def patchReportsDir
    def tmpReportsDir
    def tmpMasterReportsDir
    def tmpPatchReportsDir
    def diffDir

    Config(cliOptions) {
        localGitRepo = new File(cliOptions.localGitRepo)
        shortFilePaths = cliOptions.shortFilePaths
        listOfProjects = cliOptions.listOfProjects

        mode = cliOptions.mode
        if (!mode) {
            mode = 'diff'
        }

        baseBranch = cliOptions.baseBranch
        if (!baseBranch) {
            baseBranch = 'master'
        }
        patchBranch = cliOptions.patchBranch

        baseConfig = cliOptions.baseConfig
        patchConfig = cliOptions.patchConfig
        config = cliOptions.config
        if (config) {
            baseConfig = config
            patchConfig = config
        }

        reportsDir = 'reports'
        masterReportsDir = "$reportsDir/$baseBranch"
        patchReportsDir = "$reportsDir/$patchBranch"

        tmpReportsDir = 'tmp_reports'
        tmpMasterReportsDir = "$tmpReportsDir/$baseBranch"
        tmpPatchReportsDir = "$tmpReportsDir/$patchBranch"

        diffDir = "$reportsDir/diff"
    }

    def isDiffMode() {
        return 'diff'.equals(mode)
    }

    def isSingleMode() {
        return 'single'.equals(mode)
    }
}
