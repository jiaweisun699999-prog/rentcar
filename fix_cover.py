# -*- coding: utf-8 -*-
"""
修复 1封面及附件目录.docx 中题目行字距过密、与下方字段不对齐的问题。
- 把题目段落首行缩进从 1287 改为 1799（与姓名/学号等对齐）
- 去掉负字距 spacing="-40"
- 题目正文字号从 28 改为 24，保证一行能放下
"""
import zipfile
import shutil
import os
import re

SRC = r'c:\Users\Administrator\Desktop\bysj\归档\2_2200592086_蔡敏_附件材料\1封面及附件目录 .docx'
BAK = SRC + '.bak'
TMP = SRC + '.tmp'

# 备份
if not os.path.exists(BAK):
    shutil.copy2(SRC, BAK)
    print(f'BACKUP -> {BAK}')

# 读取
with zipfile.ZipFile(SRC, 'r') as zin:
    names = zin.namelist()
    blobs = {n: zin.read(n) for n in names}

doc = blobs['word/document.xml'].decode('utf-8')

# 我们只想改"题目"那一段，它的特征是包含 "基于协同过滤的酒文化内容推荐系统设计与实现"
# 该段开头是 <w:p w14:paraId="6FB37548">，结束是下一个 </w:p>
TITLE_TEXT = '基于协同过滤的酒文化内容推荐系统设计与实现'
assert TITLE_TEXT in doc, '没找到题目正文'

# 用正则找到该 <w:p ...>...</w:p> 段落
m = re.search(r'<w:p\s+w14:paraId="6FB37548"[^>]*>.*?</w:p>', doc, re.DOTALL)
assert m, '没找到 6FB37548 段落'
para = m.group(0)
print('原段落:')
print(para)
print('---')

new_para = para

# 1) 首行缩进 1287 -> 1799（标签与下方姓名对齐）
new_para = new_para.replace(
    '<w:ind w:firstLine="1287" w:firstLineChars="640"/>',
    '<w:ind w:firstLine="1799" w:firstLineChars="640"/>',
)

# 2) 去掉两个 run 内的 <w:spacing w:val="-40"/>
new_para = new_para.replace('<w:spacing w:val="-40"/>', '')

# 3) 把题目正文 run 的字号 28 -> 24
# 题目正文 run 是带 <w:u w:val="single"/> 且文本是 TITLE_TEXT 的那个 run
# 简单做法：先定位包含 TITLE_TEXT 的 <w:r>...</w:r>，再把其中 sz/szCs 28 -> 24
rmatch = re.search(r'<w:r>(?:(?!<w:r>).)*?' + re.escape(TITLE_TEXT) + r'.*?</w:r>', new_para, re.DOTALL)
assert rmatch, '没找到题目正文 run'
title_run = rmatch.group(0)
new_title_run = title_run.replace('<w:sz w:val="28"/>', '<w:sz w:val="24"/>')
new_title_run = new_title_run.replace('<w:szCs w:val="28"/>', '<w:szCs w:val="24"/>')
new_para = new_para.replace(title_run, new_title_run)

print('新段落:')
print(new_para)
print('---')

assert new_para != para, '未发生变化'

new_doc = doc.replace(para, new_para)
blobs['word/document.xml'] = new_doc.encode('utf-8')

# 写入
with zipfile.ZipFile(TMP, 'w', zipfile.ZIP_DEFLATED) as zout:
    for n in names:
        zout.writestr(n, blobs[n])

# 替换原文件
os.replace(TMP, SRC)
print('DONE')
