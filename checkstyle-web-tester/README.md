About
=====

CheckStyle Web Tester is a web utility for regression and testing simple scenarios using checkstyle utility.
You can share code, configurations, and output from any version of checkstyle, including releases and nightlies.
There is even an option to create a report for submitting issues and problems with checkstyle.

Requirements
============

* PHP runtime environment
* `shell_exec` must be enabled for PHP to call the checkstyle utility
* Linux for running automated terminal shell scripts
* `git` and `maven` are required for the nightly script

Configuration
=============

You only need to modify `checkstyle.php` for where you will store the user supplied code and create the `jars` and `files` folder in that location.

Scripts
=======

All scripts should be placed inside the `jars` folder.

download-release.sh
-------------------

Only requires the full version of the release as the first parameter. Will download the checkstyle all jar from `sourceforge.net`.

download-nightly.sh
-------------------

No parameters required. Downloads checkstyle's repository and packages the jar from the master branch.