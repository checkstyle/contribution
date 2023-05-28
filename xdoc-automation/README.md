# XDoc Automation

## `separate_page_per_section.py`

This script separates all `<section>` elements into their own files.

> Note: You have to install `lxml` via `pip install lxml`

1. Copy `separate_page_per_section.py` to `checkstyle/src/xdocs`
2. Cd to `checkstyle/src/xdocs`
3. Run `python3 separate_page_per_section.py <config_name.xml> <target_dir> <title>`
   
   Example: `python3 separate_page_per_section.py config_naming.xml checks/naming Naming`
