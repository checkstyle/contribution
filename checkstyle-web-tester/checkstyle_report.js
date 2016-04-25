function report() {
	var report = "";

	report += "````<br />";
	report += "$ cat TestClass.java<br />";
	report += sanitize_text(document.getElementById("cs_code").innerHTML);
	report += "<br /><br />";


	report += "$ cat TestConfig.xml<br />";
	report += sanitize_text(document.getElementById("cs_config").innerHTML);
	report += "<br /><br />";


	var select = document.getElementById("cs_jar");
	report += "$ java -jar " + sanitize_text(select.options[select.selectedIndex].text) + " -c TestConfig.xml TestClass.java<br />";
	report += document.getElementById("cs_results").innerHTML;
	report += "````<br />";

	var win = window.open("", "_blank", "toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=500,height=400");
	win.document.title = "CheckStyle Report";
	win.document.write(report);
}

function sanitize_text(s) {
	return replace_all(replace_all(s, " ", "&nbsp;"), "\n", "<br />");
}

function replace_all(s, f, r) {
	return s.replace(new RegExp(f, 'g'), r);
}

function report_on_change() {
	var element = document.getElementById("reportCreate");

	if (element != null)
		element.style.display = 'none';
}