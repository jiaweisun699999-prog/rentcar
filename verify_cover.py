import zipfile
import re

p = r'c:\Users\Administrator\Desktop\bysj\归档\2_2200592086_蔡敏_附件材料\1封面及附件目录 .docx'
with zipfile.ZipFile(p) as z:
    doc = z.read('word/document.xml').decode('utf-8')

para = re.search(r'<w:p\s+w14:paraId="6FB37548"[^>]*>.*?</w:p>', doc, re.S).group(0)
print('zip_ok')
print('indent_1799=', 'w:firstLine="1799"' in para)
print('negative_spacing=', 'w:spacing w:val="-40"' in para)
print('title_text=', '基于协同过滤的酒文化内容推荐系统设计与实现' in para)
print('font_24_count=', para.count('w:val="24"'))
