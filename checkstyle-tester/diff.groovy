import static java.lang.System.err

import java.nio.file.Paths
import java.util.regex.Pattern

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

        def checkstyleBaseReportInfo = null
        if (cfg.isDiffMode()) {
            checkstyleBaseReportInfo = generateCheckstyleReport(cfg.checkstyleToolBaseConfig)
        }

        def checkstylePatchReportInfo = generateCheckstyleReport(cfg.checkstyleToolPatchConfig)
        deleteDir(cfg.reportsDir)
        moveDir(cfg.tmpReportsDir, cfg.reportsDir)

        generateDiffReport(cfg.diffToolConfig)
        generateSummaryIndexHtml(cfg.diffDir, checkstyleBaseReportInfo, checkstylePatchReportInfo, cfg.listOfProjects)
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
            err.println "Error: \'${gitRepoDir.path}\' is not a git repository!"
            valid = false
        }
    }
    else {
        err.println "Error: \'${gitRepoDir.path}\' does not exist or it is not a directory!"
        valid = false
    }
    return valid
}

def isExistingGitBranch(gitRepo, branchName) {
    def exist = true
    def gitRevParseCmd = "git rev-parse --verify $branchName".execute(null, gitRepo)
    gitRevParseCmd.waitFor()
    if (gitRevParseCmd.exitValue() != 0) {
        err.println "Error: git repository ${gitRepo.path} does not have a branch with name \'$branchName\'!"
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

def generateCheckstyleReport(cfg) {
    println "Installing Checkstyle artifact ($cfg.branch) into local Maven repository ..."
    executeCmd("git checkout $cfg.branch", cfg.localGitRepo)
    executeCmd("git log -1 --pretty=MSG:%s%nSHA-1:%H", cfg.localGitRepo)

    def checkstyleVersion = getCheckstyleVersionFromPomXml("$cfg.localGitRepo/pom.xml", 'version')

    executeCmd("mvn -e --batch-mode -Pno-validations clean install", cfg.localGitRepo)
    executeCmd("""groovy launch.groovy --listOfProjects $cfg.listOfProjects
            --config $cfg.checkstyleCfg --ignoreExceptions --ignoreExcludes --checkstyleVersion $checkstyleVersion""")
    println "Moving Checkstyle report into $cfg.destDir ..."
    moveDir("reports", cfg.destDir)

    return new CheckstyleReportInfo(
        cfg.branch,
        getLastCommitSha(cfg.localGitRepo, cfg.branch),
        getLastCommitMsg(cfg.localGitRepo, cfg.branch)
    )
}

def getLastCommitSha(gitRepo, branch) {
    executeCmd("git checkout $branch", gitRepo)
    return 'git rev-parse HEAD'.execute(null, gitRepo).text.trim()
}

def getLastCommitMsg(gitRepo, branch) {
    executeCmd("git checkout $branch", gitRepo)
    return 'git log -1 --pretty=%B'.execute(null, gitRepo).text.trim()
}

def generateDiffReport(cfg) {
    def diffToolDir = Paths.get("").toAbsolutePath()
        .parent
        .resolve("patch-diff-report-tool")
        .toFile()
    executeCmd("mvn -e --batch-mode clean package -DskipTests", diffToolDir)
    def diffToolJarPath = getPathToDiffToolJar(diffToolDir)

    println 'Starting diff report generation ...'
    Paths.get(cfg.patchReportsDir).toFile().eachFile {
        fileObj ->
            if (fileObj.isDirectory()) {
                def projectName = fileObj.name
                def patchReportDir = new File("$cfg.patchReportsDir/$projectName")
                if (patchReportDir.exists()) {
                    def patchReport = "$cfg.patchReportsDir/$projectName/checkstyle-result.xml"
                    def outputDir = "$cfg.reportsDir/diff/$projectName"
                    def diffCmd = """java -jar $diffToolJarPath --patchReport $patchReport
                        --output $outputDir --patchConfig $cfg.patchConfig"""
                    if ('diff'.equals(cfg.mode)) {
                        def baseReport = "$cfg.masterReportsDir/$projectName/checkstyle-result.xml"
                        diffCmd += " --baseReport $baseReport --baseConfig $cfg.baseConfig"
                    }
                    if (cfg.shortFilePaths) {
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
    def targetDir = diffToolDir.absolutePath + '/target/'
    def pathToDiffToolJar
    Paths.get(targetDir).toFile().eachFile {
        fileObj ->
            def jarPattern = "patch-diff-report-tool-.*.jar-with-dependencies.jar"
            def fileName = fileObj.name
            if (fileName.matches(jarPattern)) {
                pathToDiffToolJar = fileObj.absolutePath
                return true
            }
    }
    if (pathToDiffToolJar == null) {
        throw new FileNotFoundException("Error: difff tool jar file is not found!")
    }
    return pathToDiffToolJar
}

def getTextTransform() {
    def diffToolDir = Paths.get("").toAbsolutePath()
        .parent
        .resolve("patch-diff-report-tool")
        .toFile()
    def diffToolJarPath = getPathToDiffToolJar(diffToolDir)
    this.class.classLoader.rootLoader.addURL(new URL("file:$diffToolJarPath"))
    def textTransform = Class.forName("com.github.checkstyle.site.TextTransform").newInstance()

    return textTransform;
}

def generateSummaryIndexHtml(diffDir, checkstyleBaseReportInfo, checkstylePatchReportInfo, listOfProjects) {
    println 'Starting creating report summary page ...'
    def projectsStatistic = getProjectsStatistic(diffDir)
    def summaryIndexHtml = new File("$diffDir/index.html")

    summaryIndexHtml.text = ''
    summaryIndexHtml << ('<html><head>')
    summaryIndexHtml << ('<link rel="icon" href="https://checkstyle.org/images/favicon.png" type="image/x-icon" />')
    summaryIndexHtml << ('<title>Checkstyle Tester Report Diff Summary</title>')
    summaryIndexHtml << ('</head><body>')
    summaryIndexHtml << ('\n')
    summaryIndexHtml << ('<h3><span style="color: #ff0000;">')
    summaryIndexHtml << ('<strong>WARNING: Excludes are ignored by diff.groovy.</strong>')
    summaryIndexHtml << ('</span></h3>')
    printReportInfoSection(summaryIndexHtml, checkstyleBaseReportInfo, checkstylePatchReportInfo, projectsStatistic)

    def textTransform = getTextTransform();
    def listOfProjectsFile = new File(listOfProjects)
    generateAndPrintConfigHtmlFile(diffDir, listOfProjectsFile, textTransform, summaryIndexHtml)

    projectsStatistic.sort { it.key.toLowerCase() }.sort { it.value == 0 ? 1 : 0 }.each {
        project, diffCount ->
            summaryIndexHtml << ("<a href='$project/index.html'>$project</a>")
            if (diffCount[0] != 0) {
                summaryIndexHtml << (" (&#177;${diffCount[0]}, <span style=\"color: green;\">+${diffCount[1]}, </span><span style=\"color: red;\">-${diffCount[2]}</span>)")
            }
            summaryIndexHtml << ('<br />')
            summaryIndexHtml << ('\n')
    }
    summaryIndexHtml << ('</body></html>')

    println 'Creating report summary page finished...'
}

def generateAndPrintConfigHtmlFile(diffDir, configFile, textTransform, summaryIndexHtml) {
    def configfilenameWithoutExtension = getFilenameWithoutExtension(configFile.name)
    def configFileHtml = new File("$diffDir/${configfilenameWithoutExtension}.html")
    textTransform.transform(configFile.name, configFileHtml.toPath().toString(), Locale.ENGLISH,
        "UTF-8", "UTF-8")

    summaryIndexHtml << ('<h6>')
    summaryIndexHtml << ("<a href='${configFileHtml.name}'>${configFile.name} file</a>")
    summaryIndexHtml << ('</h6>')
}

def getFilenameWithoutExtension(filename) {
    def filenameWithoutExtension
    int pos = filename.lastIndexOf(".");
    if (pos > 0) {
        filenameWithoutExtension = filename.substring(0, pos);
    }
    return filenameWithoutExtension
}

def printReportInfoSection(summaryIndexHtml, checkstyleBaseReportInfo, checkstylePatchReportInfo, projectsStatistic) {
    def date = new Date();
    summaryIndexHtml << ('<h6>')
    if (checkstyleBaseReportInfo) {
        summaryIndexHtml << "Base branch: $checkstyleBaseReportInfo.branch"
        summaryIndexHtml << ('<br />')
        summaryIndexHtml << "Base branch last commit SHA: $checkstyleBaseReportInfo.commitSha"
        summaryIndexHtml << ('<br />')
        summaryIndexHtml << "Base branch last commit message: \"$checkstyleBaseReportInfo.commitMsg\""
        summaryIndexHtml << ('<br />')
        summaryIndexHtml << ('<br />')
    }
    summaryIndexHtml << "Patch branch: $checkstylePatchReportInfo.branch"
    summaryIndexHtml << ('<br />')
    summaryIndexHtml << "Patch branch last commit SHA: $checkstylePatchReportInfo.commitSha"
    summaryIndexHtml << ('<br />')
    summaryIndexHtml << "Patch branch last commit message: \"$checkstylePatchReportInfo.commitMsg\""
    summaryIndexHtml << ('<br />')
    summaryIndexHtml << ('<br />')
    summaryIndexHtml << "Tested projects: ${projectsStatistic.size()}"
    summaryIndexHtml << ('<br />')
    summaryIndexHtml << "&#177; differences found: ${projectsStatistic.values().sum()[0]}"
    summaryIndexHtml << ('<br />')
    summaryIndexHtml << "Time of report generation: $date"
    summaryIndexHtml << ('</h6>')
}

def getProjectsStatistic(diffDir) {
    def projectsStatistic = new HashMap<>()
    def totalDiff = 0
    def addedDiff = 0
    def removedDiff = 0
    Paths.get(diffDir).toFile().eachFile {
        fileObjf ->
            if (fileObjf.isDirectory()) {
                def projectName = fileObjf.name
                def indexHtmlFile = new File(fileObjf.absolutePath + '/index.html')
                indexHtmlFile.eachLine {
                    line ->
                        def addPatchLinePattern = Pattern.compile("totalPatch\">[0-9]++ .[0-9]++ removed, (?<totalAdd>[0-9]++)")
                        def addPatchLineMatcher = addPatchLinePattern.matcher(line)
                        if (addPatchLineMatcher.find()) {
                            addedDiff = Integer.valueOf(addPatchLineMatcher.group('totalAdd'))
                        }

                        def removedPatchLinePattern = Pattern.compile("totalPatch\">[0-9]++ .(?<totalRemoved>[0-9]++)")
                        def removedPatchLineMatcher = removedPatchLinePattern.matcher(line)
                        if (removedPatchLineMatcher.find()) {
                            removedDiff = Integer.valueOf(removedPatchLineMatcher.group('totalRemoved'))
                        }

                        def linePattern = Pattern.compile("totalDiff\">(?<totalDiff>[0-9]++)")
                        def lineMatcher = linePattern.matcher(line)
                        if (lineMatcher.find()) {
                            totalDiff = Integer.valueOf(lineMatcher.group('totalDiff'))
                            def diffSummary = [totalDiff, addedDiff, removedDiff]
                            projectsStatistic.put(projectName, diffSummary)
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

def executeCmd(cmd, dir = new File("").absoluteFile) {
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
    return osSpecificCmd
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

    def getCheckstyleToolBaseConfig() {
        return [
            localGitRepo: localGitRepo,
            branch: baseBranch,
            checkstyleCfg: baseConfig,
            listOfProjects: listOfProjects,
            destDir: tmpMasterReportsDir,
        ]
    }

    def getCheckstyleToolPatchConfig() {
        return [
            localGitRepo: localGitRepo,
            branch: patchBranch,
            checkstyleCfg: patchConfig,
            listOfProjects: listOfProjects,
            destDir: tmpPatchReportsDir,
        ]
    }

    def getDiffToolConfig() {
        return [
            reportsDir: reportsDir,
            masterReportsDir: masterReportsDir,
            patchReportsDir: patchReportsDir,
            baseConfig: baseConfig,
            patchConfig: patchConfig,
            shortFilePaths: shortFilePaths,
            mode: mode,
        ]
    }
}

class CheckstyleReportInfo {
    def branch
    def commitSha
    def commitMsg

    CheckstyleReportInfo(branch, commitSha, commitMsg) {
        this.branch = branch
        this.commitSha = commitSha
        this.commitMsg = commitMsg
    }
}
