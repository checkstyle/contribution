# 1. Copy this script to src/xdocs
# 2. Cd to src/xdocs
# 3. Run python3 separate_page_per_section.py <file_name.xml> <target_dir> <title>

import sys
import os
import re
from lxml import etree
from xml.sax.saxutils import escape

if len(sys.argv) < 3:
    print("Usage: python3 separate_page_per_section.py <file_name.xml> <target_dir> <title>")
    print("Example: python3 separate_page_per_section.py config_naming.xml checks/naming Naming")
    sys.exit(1)

file_name = sys.argv[1]
dir = sys.argv[2]
title = sys.argv[3]
print(f"file_name: {file_name}")
print(f"dir: {dir}")
print(f"title: {title}")

if not os.path.exists(file_name):
    print(f"File '{file_name}' does not exist")
    sys.exit(1)

file_tree = etree.parse(file_name)
sections = file_tree.xpath('//*[local-name()="section"][not(@name="Content")]')
if len(sections) == 0:
    print(f"No sections found")
    sys.exit(1)
print(f"Found {len(sections)} sections")

if not os.path.exists(dir):
    os.makedirs(dir, exist_ok=True)
    print(f"Directory {dir} created")
else:
    print(f"Directory {dir} already exists")

def wrap_in_document(text, title):
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<document xmlns="http://maven.apache.org/XDOC/2.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/XDOC/2.0 https://maven.apache.org/xsd/xdoc-2.0.xsd">
  <head>
    <title>{title}</title>
  </head>
  <body>
    {text}
  </body>
</document>"""

# Create file per section
for section in sections:
    module_name = section.get('name')
    new_file_name = f'{module_name.lower()}.xml'
    new_file_path = os.path.join(dir, new_file_name)
    anchors = section.xpath('.//*[local-name()="a"]')

    # Convert relative links accordingly
    for anchor in anchors:
        anchor_link = anchor.get('href')
        if anchor_link.startswith('http')\
            or anchor_link.startswith('apidocs')\
            or anchor_link.startswith('#'):
            continue
        another_xdoc_name = f"{anchor_link.split('.html')[0]}.xml"
        another_xdoc_path = None
        for root, dirs, files in os.walk(os.getcwd()):
            if another_xdoc_name in files:
                another_xdoc_path = os.path.join(root, another_xdoc_name)
                break
        if another_xdoc_path is None:
            print(f"{another_xdoc_name} not found")
            sys.exit(1)
        relative_path = os.path.relpath(another_xdoc_path, os.path.dirname(new_file_path))
        new_anchor_value = relative_path.replace(".xml", ".html") + anchor_link.split('.html')[1]
        anchor.set('href', new_anchor_value)
    # Write new file
    section_text = etree.tostring(section, encoding='unicode')\
        .replace(' xmlns="http://maven.apache.org/XDOC/2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')\
        .strip()
    with open(new_file_path, 'w') as f:
        f.write(wrap_in_document(section_text, title=module_name))
    print(f"{new_file_path} created")

# Create table for index.xml
checks_tree = etree.parse('checks.xml')
table_rows = ""
for section in sections:
    module_name = section.get('name')
    tr = checks_tree.xpath(f'//*[local-name()="tr"][descendant::*[local-name()="a"][contains(text(), "{module_name}")]]')[0]
    second_td_text = tr[1].text.strip().replace(" " * 14, " " * 12)
    table_rows += f"""        <tr>
          <td>
            <a href="{module_name.lower()}.html#{module_name}">
              {module_name}
            </a>
          </td>
          <td>
            {escape(second_td_text)}
          </td>
        </tr>
"""

# Create index.xml
index_path = os.path.join(dir, 'index.xml')
with open(index_path, 'w') as f:
    index_content = f"""<section name="{title} Checks">
      <div class="wrapper">
        <table>
{table_rows.rstrip()}
        </table>
      </div>
    </section>"""
    f.write(wrap_in_document(index_content, title=title))
    print(f"{index_path} created")

# Generate items for site.xml
items = ""
for section in sections:
    module_name = section.get('name')
    current_item =  f"""          <item name="{module_name}" href="{os.path.join(dir, module_name.lower())}.html"/>
"""
    if len(current_item) > 100:
            current_item =  f"""          <item name="{module_name}"
                href="{os.path.join(dir, module_name.lower())}.html"/>
"""
    items += current_item

items = f"""<item name="{title}" href="{os.path.join(dir, 'index')}.html" collapse="true">
{items.rstrip()}
        </item>"""

# Update site.xml
filename = os.path.join("..", "site", "site.xml")
search_string = rf'<item name="{title}" .+"/>'
with open(filename, 'r') as f:
    content = f.read()
    modified_content = re.sub(search_string, items, content)

with open(filename, 'w') as f:
    f.write(modified_content)

os.remove(file_name)
print(f"{file_name} removed")

print(f"Dont forget to git grep for {file_name.split('.xml')[0]}")
