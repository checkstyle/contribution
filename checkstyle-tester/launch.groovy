import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

import static java.lang.System.err

if (areValidCliArgs(args)) {
    def projectsToTestOn = args[0]
    def checkstyleCfg = args[1]
    generateCheckstyleReport(projectsToTestOn, checkstyleCfg)
}
else {
    throw new IllegalArgumentException('Error: invalid command line arguments!')
}

def areValidCliArgs(args) {
    def valid = true
    if (args == null || args.length == 0) {
        valid = false
    }
    else {
        def validArgsCount = 2
        if (args.length == validArgsCount) {
            def projectsToTestOnFile = new File(args[0])
            def checkstyleCfgFile = new File(args[1])
            if (!projectsToTestOnFile.exists()) {
                err.println "Error: file ${projectsToTestOnFile.getName()} does not exist!"
                valid = false
            }
            else if (!checkstyleCfgFile.exists()) {
                err.println "Error: file ${checkstyleCfgFile.getName()} does not exist!"
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

def generateCheckstyleReport(projectsToTestOn, checkstyleConfig) {
    println 'Testing Checkstyle started'
    def projectsToTestOnFile = new File(projectsToTestOn)
    def projects = projectsToTestOnFile.readLines()

    def REPO_NAME_PARAM_NO = 0
    def REPO_TYPE_PARAM_NO = 1
    def REPO_URL_PARAM_NO = 2
    def REPO_COMMIT_ID_PARAM_NO = 3
    def REPO_EXCLUDES_PARAM_NO = 4
    def FULL_PARAM_LIST_SIZE = 5

    def srcDir = "src/main/java"
    def reposDir = 'repositories'
    def reportsDir = 'reports'
    createWorkDirsIfNotExist(srcDir, reposDir, reportsDir)

    def targetDir = 'target'

    projects.each {
        project ->
            if (!project.startsWith('#') && !project.isEmpty()) {
                def params = project.split('\\|', -1)
                if (params.length < FULL_PARAM_LIST_SIZE) {
                    throw new InvalidPropertiesFormatException("Error: line '$project' in file '$projectsToTestOnFile.name' should have $FULL_PARAM_LIST_SIZE pipe-delimeted sections!")
                }

                def repoName = params[REPO_NAME_PARAM_NO]
                def repoType = params[REPO_TYPE_PARAM_NO]
                def repoUrl = params[REPO_URL_PARAM_NO]
                def commitId = params[REPO_COMMIT_ID_PARAM_NO]
                def excludes = params[REPO_EXCLUDES_PARAM_NO]

                cloneRepository(repoName, repoType, repoUrl, commitId, reposDir)
                deleteDir(srcDir)
                copyDir("$reposDir/$repoName", "$srcDir/$repoName")
                runMavenExecution(srcDir, excludes, checkstyleConfig)
                postProcessCheckstyleReport(targetDir)
                deleteDir("$srcDir/$repoName")
                moveDir(targetDir, "$reportsDir/$repoName")
            }
    }

    // restore empty_file to make src directory tracked by git
    Files.createFile(Paths.get("$srcDir/empty_file"))
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

def runMavenExecution(srcDir, excludes, checkstyleConfig) {
    println "Running 'mvn clean' on $srcDir ..."
    def mvnClean = "mvn --batch-mode clean"
    executeCmd(mvnClean)
    println "Running Checkstyle on $srcDir ... with excludes $excludes"
    def mvnSite = "mvn -e --batch-mode site -Dcheckstyle.config.location=$checkstyleConfig -Dcheckstyle.excludes=$excludes -DMAVEN_OPTS=-Xmx3024m"
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
    def pattern = Pattern.compile('.*<td><a href="./xref.*\\.html#L\\d+">.*')
    def referencedFiles = new ArrayList<String>()
    linesFromIndexHtml.each {
        line ->
            def matcher = pattern.matcher(line)
            if (matcher.matches()) {
                def filePath = line.substring(line.indexOf("/xref") + 1, line.lastIndexOf('#L'))
                referencedFiles.add(filePath)
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

def executeCmd(cmd, dir = new File("./")) {
    def proc = cmd.execute(null, dir)
    proc.consumeProcessOutput(System.out, System.err)
    proc.waitFor()
    if (proc.exitValue() != 0) {
        throw new GroovyRuntimeException("Error: ${proc.err.text}!")
    }
}
