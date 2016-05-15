<?php
// Configuration

$basedir = "/tmp/checkstyle";
$jarDir = $basedir . "/jars/";
$saveDir = $basedir . "/files/";

?>

<html>
<head>
	<title>CheckStyle Web Tester</title>
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>
	<script src="jquery-linedtextarea.js"></script>
	<script src="checkstyle_report.js"></script>
	<link href="jquery-linedtextarea.css" type="text/css" rel="stylesheet" />
</head>
<center><h1>CheckStyle Web Tester</h1></center>

<?php

ini_set('display_errors', 1);
ini_set('log_errors', 1);
ini_set('error_log', dirname(__FILE__) . '/error_log.txt');
error_reporting(E_ALL);

date_default_timezone_set('US/Eastern');

$action = getParameter("action");
$checkstyle = getParameter("checkstyle");
$config = getParameter("config");
$code = getParameter("code");
$printTree = getParameter("printTree");

if (!isset($checkstyle)) {
	$checkstyle = "checkstyle-6.17-all.jar";
}

if (!isset($action)) {
	if (!isset($config)) {
		$config = "<?xml version=\"1.0\"?>\n<!DOCTYPE module PUBLIC\n          \"-//Puppy Crawl//DTD Check Configuration 1.3//EN\"\n          \"http://www.puppycrawl.com/dtds/configuration_1_3.dtd\">\n\n<module name=\"Checker\">\n    <property name=\"charset\" value=\"UTF-8\"/>\n\n    <module name=\"TreeWalker\">\n    </module>\n</module>";
	}
	if (!isset($code)) {
		$code = "public class TestClass {\n    void method() {\n    }\n}";
	}

	showForm($checkstyle, $config, $code);
} else if ($action == "save") {
	if (!isset($config) || !isset($code)) {
		echo "Some fields are missing for a save.";
	// Size Limitations
	} else if (strlen($config) > 5120) { // 5kb
		echo "Size Security: Configuration is larger than 5kb";
	} else if (strlen($code) > 5120) { // 5kb
		echo "Size Security: Code is larger than 5kb";
	// Vulnerabilities
	// http://stackoverflow.com/questions/1906927/xml-vulnerabilities/1907500#1907500
	} else if (stripos($config, "<!ENTITY") !== false) {
		echo "XML Security: '<!ENTITY' is not allowed";
	} else if (stripos($config, "<!ELEMENT") !== false) {
		echo "XML Security: '<!ELEMENT' is not allowed";
	//
	} else {
		$config = fix_post_text($config);
		$configMD = hash("md5", $config);
		$configFile = $saveDir . $configMD;
		$code = fix_post_text($code);
		$codeMD = hash("md5", $code);
		$codeFile = $saveDir . $codeMD . ".java";

		if (!file_exists($configFile)) {
			$fhandle = @fopen($configFile, "w");
			if ($fhandle != FALSE) {
				if (fwrite($fhandle, $config) == FALSE) {
					die("Failed to save configuration to " . $configMD);
				} else {
					echo "Configuration file saved.<br />";
				}
			} else {
				die("Failed to save configuration to " . $configMD);
			}
		}

		if (!file_exists($codeFile)) {
			$fhandle = @fopen($codeFile, "w");
			if ($fhandle != FALSE) {
				if (fwrite($fhandle, $code) == FALSE) {
					die("Failed to save code to " . $codeMD);
				} else {
					echo "Code file saved.<br />";
				}
			} else {
				die("Failed to save code to " . $codeMD);
			}
		}

		$action = "view";
		$config = $configMD;
		$code = $codeMD;

		echo "<br />";
	}
}

if ($action == "view") {
	if (preg_match("/[^a-z0-9.-]/i", $checkstyle)) {
		die("Improper checkstyle '" . $checkstyle . "' was supplied.");
	}
	if (preg_match("/[^a-fA-F0-9]/i", $config)) {
		die("Improper configuration '" . $config . "' was supplied.");
	}
	if (preg_match("/[^a-fA-F0-9]/i", $code)) {
		die("Improper code '" . $code . "' was supplied.");
	}

	$checkstyleFile = $jarDir . $checkstyle;
	$configFile = $saveDir . $config;
	$codeFile = $saveDir . $code . ".java";

	if (!file_exists($checkstyleFile)) {
		die("Can't find checkstyle file '" . $checkstyle . "'");
	}
	if (!file_exists($configFile)) {
		die("Can't find configuration file '" . $config . "'");
	}
	if (!file_exists($codeFile)) {
		die("Can't find code file '" . $code . "'");
	}

	$configContents = @file_get_contents($configFile);
	$codeContents = @file_get_contents($codeFile);

	echo "Results:<br />";
	echo "<div style='border: 1px solid black;' id='cs_results'>";

	$output = shell_exec("java -jar " . $checkstyleFile . ($printTree == "true" ? " -T" : " -c " . $configFile) . " " . $codeFile . " 2>&1");

	// pretty display
	echo str_replace("\n", "<br />", str_replace("  ", "&nbsp; ", str_replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;", str_replace($saveDir, "", str_replace($code, "TestClass", str_replace($config, "TestConfig.xml", _sanitizeText($output)))))));

	echo "</div>";

	$link = $_SERVER['SCRIPT_NAME'] . "?action=view&config=" . $config . "&code=" . $code . "&checkstyle=" . $checkstyle . "&printTree=" . $printTree;
	$lpos = strrpos($link, "/");
	if ($lpos >= 0) {
		$link = substr($link, $lpos + 1);
	}

	echo "<br />Share Link: <a href='" . $link . "'>" . $link . "</a>";
	if ($printTree != "true") {
		echo "<br /><br /><input type=\"submit\" value=\"Create GitHub CLI Report\" id=\"reportCreate\" onclick=\"report();\">";
	}
	echo "<br /><br /><hr /><br />";

	showForm($checkstyle, $configContents, $codeContents);
}

function showForm($checkstyle, $config, $code) {
	global $jarDir;

	echo "<form action=\"" . $_SERVER['SCRIPT_NAME'] . "\" method=\"POST\">";
	echo "<input type='hidden' name='action' value='save'>";
	echo "Checkstyle:<br />";
	echo "<select name='checkstyle' id='cs_jar' onchange='report_on_change();'>";

	foreach (getCheckStyles() as $key => $value) {
		echo '<option value="' . $value . '" ' . ($value == $checkstyle ? "selected='selected'" : "") . '>' . $value . '</option>';
	}

	echo "</select>";
	echo "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"submit\" value=\"Submit\"><br /><br />";
	echo "Configuration:<br />";
	echo "<textarea name='config' rows='12' wrap='off' style='width: 100%' id='cs_config' onchange='report_on_change();' onkeyup='report_on_change();'>";
	echo _sanitizeText($config);
	echo "</textarea><br /><br />";
	echo "Java Code:<br />";
	echo "<textarea name='code' rows='12' wrap='off' style='width: 100%' id='cs_code' onchange='report_on_change();' onkeyup='report_on_change();'>";
	echo _sanitizeText($code);
	echo "</textarea><br /><br />";
	echo "<input type=\"checkbox\" name=\"printTree\" value=\"true\"> Print Tree Only<br /><br /><br />";
	echo "<input type=\"submit\" value=\"Submit\">";
	echo "</form>";
	echo "<script>$(function() { $(\"#cs_config\").linedtextarea(); $(\"#cs_code\").linedtextarea(); });</script>";
}

function getCheckStyles() {
	global $jarDir;
	$result = array();

	$dhandle = @opendir($jarDir);
	if ($dhandle != FALSE) {
		while (($name = @readdir($dhandle)) !== false) {
			if (($name == ".") || ($name == "..")) continue;

			if ((@filetype($jarDir . $name) != "dir") && (str_endswith($name, ".jar")))
				$result[] = $name;
		}

		closedir($dhandle);
	}

	sort($result);
	return $result;
}

function str_endswith($string, $test) {
	$strlen = strlen($string);
	$testlen = strlen($test);
	if ($testlen > $strlen) return false;
	return substr_compare($string, $test, $strlen - $testlen, $testlen) === 0;
}

function getParameter($param) {
	return (isset($_POST[$param]) ? $_POST[$param] : (isset($_GET[$param]) ? $_GET[$param] : null));
}

function fix_post_text($in, $r_sq = "'", $r_dq = "\\\"", $r_ds = "\\\\") {
	$in = str_replace(
		array("\r",	$r_sq),
		array("",	"'"),
		$in);

	if (!ini_get('safe_mode')) {
		$in = str_replace(
			array($r_dq,	$r_ds),
			array("\"",	"\\"),
			$in);
	}

	return $in;
}

function _sanitizeText($in) {
	$in = str_replace(
		array(chr(38) . "",	"<",		">",		"'"),
		array(chr(38) . "amp;",	chr(38) . "lt;",chr(38) . "gt;","'"),
		$in);

	if (ini_get('safe_mode')) {
		$in = str_replace(
			array("\\\"",	"\\\\"),
			array("\"",	"\\"),
			$in);
	}

	return $in;
}

?>
</body></html>
