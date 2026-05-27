# -*- coding: utf-8 -*-
import zipfile
import os
import re

SRC = r'c:\Users\Administrator\Desktop\bysj\归档\2_2200592086_蔡敏_附件材料\1封面及附件目录 .docx'
TMP = SRC + '.tmp'
TITLE_TEXT = '基于协同过滤的酒文化内容推荐系统设计与实现'

with zipfile.ZipFile(SRC, 'r') as zin:
    names = zin.namelist()
    blobs = {n: zin.read(n) for n in names}

doc = blobs['word/document.xml'].decode('utf-8')

paras = re.findall(r'<w:p\s+w14:paraId="[^"]+"[^>]*>.*?</w:p>', doc, re.S)
title_paras = [p for p in paras if TITLE_TEXT in p]
print('title_para_count=', len(title_paras))
assert len(title_paras) >= 2, '未找到第二个题目段落'

para = title_paras[1]
new_para = para
new_para = re.sub(r'<w:ind w:firstLine="1287" w:firstLineChars="640"/>', '<w:ind w:firstLine="1799" w:firstLineChars="640"/>', new_para)
new_para = new_para.replace('<w:spacing w:val="-40"/>', '')

rmatch = re.search(r'<w:r>(?:(?!<w:r>).)*?' + re.escape(TITLE_TEXT) + r'.*?</w:r>', new_para, re.S)
assert rmatch, '未找到第二个题目正文 run'
title_run = rmatch.group(0)
new_title_run = title_run.replace('<w:sz w:val="28"/>', '<w:sz w:val="24"/>')
new_title_run = new_title_run.replace('<w:szCs w:val="28"/>', '<w:szCs w:val="24"/>')
new_para = new_para.replace(title_run, new_title_run)

assert new_para != para, '第二个题目段落未发生变化，可能已经修改过'
doc = doc.replace(para, new_para, 1)
blobs['word/document.xml'] = doc.encode('utf-8')

with zipfile.ZipFile(TMP, 'w', zipfile.ZIP_DEFLATED) as zout:
    for n in names:
        zout.writestr(n, blobs[n])

os.replace(TMP, SRC)
print('DONE')
