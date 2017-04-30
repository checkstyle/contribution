import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

import static java.lang.System.err

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
    }
    return cli.parse(args)
}

def areValidCliOptions(options) {
    def valid = true
    def listOfProjectsFile = new File(options.listOfProjects)
    def checkstyleCfgFile = new File(options.config)
    if (!listOfProjectsFile.exists()) {
        err.println "Error: file ${listOfProjectsFile.getName()} does not exist!"
        valid = false
    } else if (!checkstyleCfgFile.exists()) {
        err.println "Error: file ${checkstyleCfgFile.getName()} does not exist!"
        valid = false
    }
    return valid
}

def generateCheckstyleReport(cliOptions) {
    println 'Testing Checkstyle started'

    def targetDir = 'target'
    def srcDir = "src/main/java"
    def reposDir = 'repositories'
    def reportsDir = 'reports'
    createWorkDirsIfNotExist(srcDir, reposDir, reportsDir)

    def REPO_NAME_PARAM_NO = 0
    def REPO_TYPE_PARAM_NO = 1
    def REPO_URL_PARAM_NO = 2
    def REPO_COMMIT_ID_PARAM_NO = 3
    def REPO_EXCLUDES_PARAM_NO = 4
    def FULL_PARAM_LIST_SIZE = 5

    def checkstyleCfg = cliOptions.config
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
                copyDir("$reposDir/$repoName", "$srcDir/$repoName")
                runMavenExecution(srcDir, excludes, checkstyleCfg, ignoreExceptions)
                postProcessCheckstyleReport(targetDir)
                deleteDir("$srcDir/$repoName")
                moveDir(targetDir, "$reportsDir/$repoName")
            }
    }

    // restore empty_file to make src directory tracked by git
    new File("$srcDir/empty_file").createNewFile()
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
    def srcDestinationDir = "$srcDir/$repoName"
    if (!Files.exists(Paths.get(srcDestinationDir))) {
        def cloneCmd = getCloneCmd(repoType, repoUrl, srcDestinationDir)
        println "Cloning $repoType repository '$repoName' to $srcDestinationDir folder ..."
        executeCmd(cloneCmd)
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

def runMavenExecution(srcDir, excludes, checkstyleConfig, ignoreExceptions) {
    println "Running 'mvn clean' on $srcDir ..."
    def mvnClean = "mvn --batch-mode clean"
    executeCmd(mvnClean)
    println "Running Checkstyle on $srcDir ... with excludes $excludes"
    def mvnSite = "mvn -e --batch-mode site -Dcheckstyle.config.location=$checkstyleConfig -Dcheckstyle.excludes=$excludes -DMAVEN_OPTS=-Xmx3024m"
    if (ignoreExceptions) {
        mvnSite = mvnSite + ' -Dcheckstyle.failsOnError=false'
    }
    executeCmd(mvnSite)
    println "Running Checkstyle on $srcDir - finished"
}

def postProcessCheckstyleReport(targetDir) {
    def siteDir = "$targetDir/site"
    println 'linking report to index.html'
    new File("$siteDir/index.html").renameTo "$siteDir/_index.html"
    Files.createLink(Paths.get("$siteDir/index.html"), Paths.get("$siteDir/checkstyle.html"))

    removeNonReferencedXrefFiles(siteDir)
    removeEmptyDirectories(new File("$siteDir/xref"))

    new AntBuilder().replace(
        file: "$targetDir/checkstyle-result.xml",
        token: "checkstyle-tester/src/main/java",
        value: "checkstyle-tester/repositories"
    )
}

def removeNonReferencedXrefFiles(siteDir) {
    println 'Removing non refernced xref files in report ...'

    def linesFromIndexHtml = Files.readAllLines(Paths.get("$siteDir/index.html"))
    def filesReferencedInReport = getFilesReferencedInReport(linesFromIndexHtml)

    Paths.get("$siteDir/xref").toFile().eachFileRecurse {
        fileObj ->
            def path = fileObj.getPath()
            path = path.substring(path.indexOf("xref"))
            def fileName = fileObj.getName()
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

def getOsSpecificCmd(cmd) {
    def osSpecificCmd
    if (System.properties['os.name'].toLowerCase().contains('windows')) {
        osSpecificCmd = "cmd /c $cmd"
    }
    else {
        osSpecificCmd = cmd
    }
}
