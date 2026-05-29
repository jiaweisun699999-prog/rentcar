import zipfile
import re

p = r'c:\Users\Administrator\Desktop\bysj\归档\2_2200592086_蔡敏_附件材料\1封面及附件目录 .docx'
text = '基于协同过滤的酒文化内容推荐系统设计与实现'
with zipfile.ZipFile(p) as z:
    doc = z.read('word/document.xml').decode('utf-8')

paras = re.findall(r'<w:p\s+w14:paraId="[^"]+"[^>]*>.*?</w:p>', doc, re.S)
titles = [x for x in paras if text in x]
print('title_count=', len(titles))
for i, para in enumerate(titles, 1):
    print(f'title_{i}_indent_1799=', 'w:firstLine="1799"' in para)
    print(f'title_{i}_negative_spacing=', 'w:spacing w:val="-40"' in para)
    print(f'title_{i}_font_24=', '<w:sz w:val="24"/>' in para and '<w:szCs w:val="24"/>' in para)
