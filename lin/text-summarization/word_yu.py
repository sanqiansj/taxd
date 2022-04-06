from mailmerge import MailMerge

# 打印模板
template = "/Volumes/TURE_UPAN/参考博客.docx"

# 创建邮件合并文档并查看所有字段

document_1 = MailMerge(template)
print("Fields included in {}: {}".format(template, document_1.get_merge_fields()))

document_1.merge(
    people_name=u'勒布朗',
    identity_card_id='123456789',
    begin_work_year='2018',
    begin_work_month='7',
    department_name=u'洛杉矶湖人',
    job_name=u'联盟第一人'
    )
document_1.write("/Volumes/TURE_UPAN/参考博客_2.docx")
