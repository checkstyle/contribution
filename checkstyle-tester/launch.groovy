import static java.lang.System.err

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

static void main(String[] args) {
    def cliOptions = getCliOptions(args)
    if (cliOptions != null && areValidCliOptions(cliOptions)) {
        generateCheckstyleReport(cliOptions)
    }
    else {
        throw new IllegalArgumentException('Error: invalid command line arguments!')
    }
}

def getCliOptions(args) {
    def optionsDescLineLength = 120
    def cli = new CliBuilder(usage:'groovy launch.groovy [options]', header: 'options:', width: optionsDescLineLength)
    cli.with {
        c(longOpt: 'config', args: 1, required: true, argName: 'path', 'Path to checkstyle config file (required)')
        l(longOpt: 'listOfProjects', args: 1, required: true, argName: 'path', 'Path to file which contains projects to test on (required)')
        i(longOpt: 'ignoreExceptions', required: false, 'Whether Maven Checkstyle Plugin should ignore exceptions (optional, default is false)')
        g(longOpt: 'ignoreExcludes', required: false, 'Whether to ignore excludes specified in the list of projects (optional, default is false)')
        cv(longOpt: 'checkstyleVersion', args: 1, required: false,
            'What version of Checkstyle to use (optional, default the latest snapshot)')
    }
    return cli.parse(args)
}

def areValidCliOptions(options) {
    def valid = true
    def listOfProjectsFile = new File(options.listOfProjects)
    def checkstyleCfgFile = new File(options.config)
    if (!listOfProjectsFile.exists()) {
        err.println "Error: file ${listOfProjectsFile.name} does not exist!"
        valid = false
    } else if (!checkstyleCfgFile.exists()) {
        err.println "Error: file ${checkstyleCfgFile.name} does not exist!"
        valid = false
    }
    return valid
}

def generateCheckstyleReport(cliOptions) {
    println 'Testing Checkstyle started'

    def targetDir = 'target'
    def srcDir = getOsSpecificPath("src", "main", "java")
    def reposDir = 'repositories'
    def reportsDir = 'reports'
    createWorkDirsIfNotExist(srcDir, reposDir, reportsDir)

    final REPO_NAME_PARAM_NO = 0
    final REPO_TYPE_PARAM_NO = 1
    final REPO_URL_PARAM_NO = 2
    final REPO_COMMIT_ID_PARAM_NO = 3
    final REPO_EXCLUDES_PARAM_NO = 4
    final FULL_PARAM_LIST_SIZE = 5

    def checkstyleCfg = cliOptions.config
    def checkstyleVersion = cliOptions.checkstyleVersion
    def ignoreExceptions = cliOptions.ignoreExceptions
    def listOfProjectsFile = new File(cliOptions.listOfProjects)
    def projects = listOfProjectsFile.readLines()

    projects.each {
        project ->
            if (!project.startsWith('#') && !project.isEmpty()) {
                def params = project.split('\\|', -1)
                if (params.length < FULL_PARAM_LIST_SIZE) {
                    throw new InvalidPropertiesFormatException("Error: line '$project' in file '$listOfProjectsFile.name' should have $FULL_PARAM_LIST_SIZE pipe-delimeted sections!")
                }

                def repoName = params[REPO_NAME_PARAM_NO]
                def repoType = params[REPO_TYPE_PARAM_NO]
                def repoUrl = params[REPO_URL_PARAM_NO]
                def commitId = params[REPO_COMMIT_ID_PARAM_NO]

                def excludes = ""
                if (!cliOptions.ignoreExcludes) {
                    excludes = params[REPO_EXCLUDES_PARAM_NO]
                }

                cloneRepository(repoName, repoType, repoUrl, commitId, reposDir)
                deleteDir(srcDir)
                copyDir(getOsSpecificPath("$reposDir", "$repoName"), getOsSpecificPath("$srcDir", "$repoName"))
                runMavenExecution(srcDir, excludes, checkstyleCfg, ignoreExceptions, checkstyleVersion)
                postProcessCheckstyleReport(targetDir)
                deleteDir(getOsSpecificPath("$srcDir", "$repoName"))
                moveDir(targetDir, getOsSpecificPath("$reportsDir", "$repoName"))
            }
    }

    // restore empty_file to make src directory tracked by git
    new File(getOsSpecificPath("$srcDir", "empty_file")).createNewFile()
}

def createWorkDirsIfNotExist(srcDirPath, repoDirPath, reportsDirPath) {
    def srcDir = new File(srcDirPath)
    if (!srcDir.exists()) {
        srcDir.mkdirs()
    }
    def repoDir = new File(repoDirPath)
    if (!repoDir.exists()) {
        repoDir.mkdir()
    }
    def reportsDir = new File(reportsDirPath)
    if (!reportsDir.exists()) {
        reportsDir.mkdir()
    }
}

def cloneRepository(repoName, repoType, repoUrl, commitId, srcDir) {
    def srcDestinationDir = getOsSpecificPath("$srcDir", "$repoName")
    if (!Files.exists(Paths.get(srcDestinationDir))) {
        def cloneCmd = getCloneCmd(repoType, repoUrl, srcDestinationDir)
        println "Cloning $repoType repository '$repoName' to $srcDestinationDir folder ..."
        executeCmdWithRetry(cloneCmd)
        println "Cloning $repoType repository '$repoName' - completed\n"
    }

    if (commitId && commitId != '') {
        def lastCommitSha = getLastCommitSha(repoType, srcDestinationDir)
        def commitIdSha = getCommitSha(commitId, repoType, srcDestinationDir)
        if (lastCommitSha != commitIdSha) {
            def resetCmd = getResetCmd(repoType, commitId)
            println "Reseting $repoType sources to commit '$commitId'"
            executeCmd(resetCmd, new File("$srcDestinationDir"))
        }
    }
    println "$repoName is synchronized"
}

def getCloneCmd(repoType, repoUrl, srcDestinationDir) {
    def cloneCmd = ''
    switch (repoType) {
        case 'git':
            cloneCmd = "git clone $repoUrl $srcDestinationDir"
            break
        case 'hg':
            cloneCmd = "hg clone $repoUrl $srcDestinationDir"
            break
        default:
            throw new IllegalArgumentException("Error! Unknown $repoType repository.")
    }
}

def getLastCommitSha(repoType, srcDestinationDir) {
    def cmd = ''
    switch (repoType) {
        case 'git':
            cmd = "git rev-parse HEAD"
            break
        case 'hg':
            cmd = "hg id -i"
            break
        default:
            throw new IllegalArgumentException("Error! Unknown $repoType repository.")
    }
    def sha = cmd.execute(null, new File("$srcDestinationDir")).text
    // cmd output contains new line character which should be removed
    return sha.replace('\n', '')
}

def getCommitSha(commitId, repoType, srcDestinationDir) {
    def cmd = ''
    switch (repoType) {
        case 'git':
            cmd = "git rev-parse $commitId"
            break
        case 'hg':
            cmd = "hg identify --id $commitId"
            break
        default:
            throw new IllegalArgumentException("Error! Unknown $repoType repository.")
    }
    def sha = cmd.execute(null, new File("$srcDestinationDir")).text
    // cmd output contains new line character which should be removed
    return sha.replace('\n', '')
}

def getResetCmd(repoType, commitId) {
    def resetCmd = ''
    switch (repoType) {
        case 'git':
            resetCmd = "git reset --hard $commitId"
            break
        case 'hg':
            resetCmd = "hg up $commitId"
            break
        default:
            throw new IllegalArgumentException("Error! Unknown $repoType repository.")
    }
}

def copyDir(source, destination) {
    new AntBuilder().copy(todir: destination) {
        fileset(dir: source)
    }
}

def moveDir(source, destination) {
    new AntBuilder().move(todir: destination) {
        fileset(dir: source)
    }
}

def deleteDir(dir) {
    new AntBuilder().delete(dir: dir, failonerror: false)
}

def runMavenExecution(srcDir, excludes, checkstyleConfig, ignoreExceptions, checkstyleVersion) {
    println "Running 'mvn clean' on $srcDir ..."
    def mvnClean = "mvn --batch-mode clean"
    executeCmd(mvnClean)
    println "Running Checkstyle on $srcDir ... with excludes $excludes"
    def mvnSite = "mvn -e --batch-mode site -Dcheckstyle.config.location=$checkstyleConfig -Dcheckstyle.excludes=$excludes"
    if (checkstyleVersion) {
        mvnSite = mvnSite + " -Dcheckstyle.version=$checkstyleVersion"
    }
    if (ignoreExceptions) {
        mvnSite = mvnSite + ' -Dcheckstyle.failsOnError=false'
    }
    executeCmd(mvnSite)
    println "Running Checkstyle on $srcDir - finished"
}

def postProcessCheckstyleReport(targetDir) {
    def siteDir = getOsSpecificPath("$targetDir", "site")
    println 'linking report to index.html'
    new File(getOsSpecificPath("$siteDir", "index.html")).renameTo  getOsSpecificPath("$siteDir", "_index.html")
    Files.createLink(Paths.get(getOsSpecificPath("$siteDir", "index.html")),
        Paths.get(getOsSpecificPath("$siteDir", "checkstyle.html")))

    removeNonReferencedXrefFiles(siteDir)
    removeEmptyDirectories(new File(getOsSpecificPath("$siteDir", "xref")))

    new AntBuilder().replace(
        file: getOsSpecificPath("$targetDir", "checkstyle-result.xml"),
        token: getOsSpecificPath("checkstyle-tester", "src", "main", "java"),
        value: getOsSpecificPath("checkstyle-tester", "repositories")
    )
}

def removeNonReferencedXrefFiles(siteDir) {
    println 'Removing non refernced xref files in report ...'

    def linesFromIndexHtml = Files.readAllLines(Paths.get("$siteDir/index.html"))
    def filesReferencedInReport = getFilesReferencedInReport(linesFromIndexHtml)

    Paths.get(getOsSpecificPath("$siteDir", "xref")).toFile().eachFileRecurse {
        fileObj ->
            def path = fileObj.path
            path = path.substring(path.indexOf("xref"))
            if (isWindows()) {
                path = path.replace("\\", "/")
            }
            def fileName = fileObj.name
            if (fileObj.isFile()
                    && !filesReferencedInReport.contains(path)
                    && 'stylesheet.css' != fileName
                    && 'allclasses-frame.html' != fileName
                    && 'index.html' != fileName
                    && 'overview-frame.html' != fileName
                    && 'overview-summary.html' != fileName) {
                fileObj.delete()
            }
    }
}

def getFilesReferencedInReport(linesFromIndexHtml) {
    def xrefStartIdx = 2
    def pattern = Pattern.compile('\\./xref/[^<>]+\\.html')
    def referencedFiles = new HashSet<String>()
    linesFromIndexHtml.each {
        line ->
            def matcher = pattern.matcher(line)
            if (matcher.find()) {
                referencedFiles.addAll(matcher.collect { it.substring(xrefStartIdx) })
            }
    }
    return referencedFiles
}

def removeEmptyDirectories(file) {
    def contents = file.listFiles()
    if (contents != null) {
        for (File f : contents) {
            removeEmptyDirectories(f)
        }
    }
    if (file.isDirectory() && file.listFiles().length == 0) {
        file.delete()
    }
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

def executeCmdWithRetry(cmd, dir =  new File("").getAbsoluteFile(), retry = 5) {
    def osSpecificCmd = getOsSpecificCmd(cmd)
    def left = retry
    while (true) {
        def proc = osSpecificCmd.execute(null, dir)
        proc.consumeProcessOutput(System.out, System.err)
        proc.waitFor()
        left--
        if (proc.exitValue() != 0) {
            if (left <= 0) {
                throw new GroovyRuntimeException("Error: ${proc.err.text}!")
            }
            else {
                Thread.sleep(15000)
            }
        }
        else {
            break
        }
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

def getOsSpecificPath(String ... name) {
    def slash = isWindows() ? "\\" : "/"
    def path = name.join(slash)
    return path
}

def isWindows() {
    return System.properties['os.name'].toLowerCase().contains('windows')
}
