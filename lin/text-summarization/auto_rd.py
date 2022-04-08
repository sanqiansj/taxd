# coding:utf-8
import jieba
import numpy as np
import collections
from sklearn import feature_extraction
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import CountVectorizer
from summary import summary
from summary import split_sentence
import xlrd
from mailmerge import MailMerge
import json

def read_rd_excel_sheet(filename,keywords):
    # 读取RD规划表和公司年收入
    #print('keywords',keywords)
    workbook_RD = xlrd.open_workbook(filename) #rd_list[-1]
    worksheets_RD = workbook_RD.sheet_names()
    for i in worksheets_RD:
        if keywords in i:
            RD_sheet = i
    worksheet_RD = workbook_RD.sheet_by_name(RD_sheet)
    num_rows = worksheet_RD.nrows
    rows_list = []
    for curr_row in range(num_rows):
        row = worksheet_RD.row_values(curr_row)
        rows_list.append(row)
    rows_list = rows_list[1:]
    return rows_list

def get_rd_dict(filename,keywords):
    rows_list=read_rd_excel_sheet(filename,keywords)

    res={}
    id_list=[]
    name_list=[]
    time_list=[]
    ip_list=[]
    for i in rows_list:
        id_list.append(i[0])
        name_list.append(i[1])
        time_list.append(i[3])
        ip_list.append(i[-1])
    res['dev_id']=id_list
    res['dev_name']=name_list
    res['dev_time']=time_list
    res['ip_name']=ip_list
    return res

def get_name(filename,keywords):
    rows_list = read_rd_excel_sheet(filename, keywords)
    name='XX公司'
    for i in rows_list:
        if '公司成立' in i[2]:
            name=i[2][:i[2].find('公司成立')+2]
    return name

def read_keywords(filename):
    word_list = []
    with open(filename, 'r', encoding='utf8') as file:
        lines = file.readlines()
    for i in lines:
        word_list.append(i.strip())
    return word_list
def readfile(filename):
    words = []
    with open(filename, 'r', encoding='utf8') as file:
        lines = file.readlines()
        for line in lines:
            words.append(line.strip().split('/'))
    return words

def get_conclusion_bg(text, words_list):
    sentence_set, sentence_with_index=split_sentence(text, punctuation_list='!?。,，！？')
    sentence_set_last_5=sentence_set[-6:]
    message=''.join(sentence_set_last_5)

    #print(sentence_set_last_5)
   # print(sentence_with_index)
    for words in words_list:
        num = 0
        count_star = 0
        for w in words:
            if w[0] == '*':
                w = w[1:]
                if w in message:
                    num += 2
                    count_star += 1
                    for i in range(len(sentence_set_last_5)):
                        if w in sentence_set_last_5[i]:
                            res=sentence_set_last_5[:i]
            elif w in message:
                num += 1
        if num >= 3 and count_star > 0:
            res_text=''.join(sentence_set[:-6]+res)[:-1]+'。'
            return res_text
    return ''.join(sentence_set)
def generate_background(text,file_name):
    stop_word = read_keywords('stopWordList.txt')
    file_name = file_name.replace('的研发', '')
    if len(text)<=220:
        res_text=text+'因此，我公司决定研发'+file_name+'。'
    elif 220<len(text)<=300:
        res_text=summary(text,stop_word,topK_ratio=0.75)+'因此，我公司决定研发'+file_name+'。'
    else:
        res_text = summary(text, stop_word, topK_ratio=0.6) + '因此，我公司决定研发' + file_name + '。'
    return res_text

def generate_index(res):
    counter=0
    index_set=[]
    res1=[]
    if len(res)>0:
        for i in range(1,len(res)+1):
            index_set.append(str(i)+'.')
        for i in range(len(res)):
                temp=index_set[i]+res[i]
                res1.append(temp)
                counter+=1
    return [res1,counter]

def get_key_tech(text):
    stop_word = read_keywords('stopWordList.txt')
    sentence_set, sentence_with_index = split_sentence(text, punctuation_list=';；!?。！？')
    tech_content_set=[]
    for i in sentence_set:
        for word in ['优选的','其中','进一步的','所述']:
            if word in i:
                res_str=i[i.find(word)+len(word):]
                res_str=res_str.replace('：','').replace('所述','').replace('\n','')
                if res_str.startswith('，'):
                    res_str = res_str[1:]
                if res_str.startswith('；'):
                    res_str = res_str[1:]
                if res_str.startswith(' '):
                    res_str = res_str[1:]
                for d in "''()（）[]【】{}「」:：0123456789.、：  ":
                    res_str = res_str.replace(d, '')
                tech_content_set.append(res_str)
    res_tech=[]
    res = []
    for i in tech_content_set:
        if len(i)<=90:
            res_tech.append(i)
        elif 90<len(i)<=180:
            temp_str=summary(i, stop_word, topK_ratio=0.7)
            res_tech.append(temp_str)
        else:
            temp_str = summary(i, stop_word, topK_ratio=0.5)
            res_tech.append(temp_str)
        res = []
        for i in res_tech:
            if i[-1] !='。':
                res.append(i[:-1]+'。')
            else:
                res.append(i)
    res_temp=list(set(res))
    #res_remain=[]
    if len(res_temp)>=10:
        res_temp_2=res_temp[:9]
        res1 = generate_index(res_temp_2)
        #res_remain=res_temp[9:]
    else:
        res1=generate_index(res_temp)
    #res1_remain=generate_index(res_remain)
    return ['\n'.join(res1[0]),res1[1],res1[0],res_temp]
def checkChinese(mychar):
    return '\u4e00' <= mychar <= '\u9fa5'
def get_innovation(text,file_name,res_temp):
    stop_word = read_keywords('stopWordList.txt')
    sentence_set, sentence_with_index = split_sentence(text, punctuation_list='!?。:：！？；\n')
    inv_content_set = []
    N=len(sentence_set)
    pointer=N
    for i in range(N):
        t=sentence_set[i]
        if '有益' in t or '设计合理' in t or '全新' in t or ('现有' in t and '相比' in t) :
            if  '上述技术方案' in t or '本' in t:
                pointer=i
                #print(N,pointer)
    if pointer!=N:
        for i in range(pointer+1,N):
            res_str = sentence_set[i]
           # print('000',sentence_set)
            if '实用新型' in sentence_set[i]:
                res_str = res_str.replace('实用新型', file_name)
            if '上述方案' in sentence_set[i]:
                res_str = res_str.replace('上述方案', '本研发')
            if '发明' in sentence_set[i]:
                res_str = res_str.replace('发明', file_name)
            res_str = res_str.replace(':', '').replace('所述', '').replace('\n', '').replace('\r', '').replace('\t', '').replace(' ','')
            for d in "''()（）[]【】{}「」:：0123456789.、： " :
                res_str = res_str.replace(d, '')
            if res_str.startswith('，'):
                res_str = res_str[1:]
            if res_str.startswith('；'):
                res_str = res_str[1:]
            if len(res_str) <= 70:
                res_str=res_str
            if len(res_str)==0:
                continue
            elif 70 < len(res_str) <= 140:
                temp_str = summary(res_str, stop_word, topK_ratio=0.7)
                res_str=temp_str
            elif 140<len(res_str)<=300:
                temp_str = summary(res_str, stop_word, topK_ratio=0.5)
                res_str=temp_str
            else:
                temp_str = summary(res_str, stop_word, topK_ratio=0.4)
                res_str=temp_str
           # print('aaaaa',res_str)
            #print('dddddddddddddddddddddddddddd',res_str,len(res_str),type(res_str),res_str[-1],res_str[:-1])
            while not checkChinese(res_str[-1]):
                print('res_str1',res_str[-1])
                res_str=res_str[:-1]
                print('res_str2', res_str)
            if res_str[-1] != '。':
                res_str+= '。'
            inv_content_set.append(res_str)

    if len(inv_content_set)==0:
        #print(res_temp)
        res_temp_result=res_temp[len(res_temp)//2:]
        #print(res_temp_result)
        for i in res_temp_result:
            temp_res_str=summary(i, stop_word, topK_ratio=0.68)
            temp_res_str = temp_res_str.replace(':', '').replace('所述', '').replace('\n', '')
            for d in "''()（）[]【】{}「」:：0123456789.、：  ":
                temp_res_str = temp_res_str.replace(d, '')
            if temp_res_str.startswith('，'):
                temp_res_str = temp_res_str[1:]
            if temp_res_str.startswith('；'):
                temp_res_str = temp_res_str[1:]
            inv_content_set.append(temp_res_str)
            inv_content_set_temp=[]
            for i in inv_content_set:
                if len(i)>0:
                    inv_content_set_temp.append(i)
            res1 = generate_index(inv_content_set_temp)[0]
    else:
        res1 = generate_index(inv_content_set)[0]
    return '\n'.join(res1)

def get_intro(text):
    stop_word = read_keywords('stopWordList.txt')
    sentence_set, sentence_with_index = split_sentence(text, punctuation_list='!?。！？')
    tech_content_set=[]
    for i in sentence_set:
        for word in ['优选的','其中']:
            if word in i:
                res_str=i[i.find(word)+len(word):]
                res_str=res_str.replace('：','').replace('所述','').replace('\n','')
                if res_str.startswith('，'):
                    res_str = res_str[1:]
                if res_str.startswith('；'):
                    res_str = res_str[1:]
                tech_content_set.append(res_str)
    res_tech=[]
    res = []
    for i in tech_content_set:
        if len(i)<=90:
            res_tech.append(i)
        elif 90<len(i)<=180:
            temp_str=summary(i, stop_word, topK_ratio=0.7)
            res_tech.append(temp_str)
        else:
            temp_str = summary(i, stop_word, topK_ratio=0.5)
            res_tech.append(temp_str)
        res = []
        for i in res_tech:
            if i[-1] !='。':
                res.append(i[:-1]+'。')
            else:
                res.append(i)
    res1=generate_index(res)
    return '\n'.join(res1)

def get_time_period(time_list):
    res={}
    start_time_list=[]
    end_time_list=[]
    time_period_list=[]
    time_length_list=[]
    time_dev_peropd_list=[]
    for i in time_list:
        time_dev_peropd_list_1=[]
        temp1=i.split('-')[0].split('.')
        temp2=i.split('-')[1].split('.')
        start_time_list.append(temp1[0]+'年'+temp1[1]+'月'+temp1[2]+'日')
        end_time_list.append(temp2[0]+'年'+temp2[1]+'月'+temp2[2]+'日')
        time_period_list.append('-'.join([temp1[0]+'年'+temp1[1]+'月'+temp1[2]+'日',temp2[0]+'年'+temp2[1]+'月'+temp2[2]+'日']))
        time_length_list.append(str(int(temp2[1])-int(temp1[1])+1))
        if temp1[1]=='1':
            time_dev_peropd_list_1.append([temp1[0]+'年1月-'+temp1[0]+'年3月',temp1[0]+'年4月-'+temp1[0]+'年10月',temp1[0]+'年11月-'+temp1[0]+'年12月'])
        else:
            print(temp1[0],temp1[1],type(temp1[0]),type(temp1[1]))
            #time_dev_peropd_list_1.append([temp1[0]+'年1月-'+temp1[0]+'年3月',temp1[0]+'年4月-'+temp1[0]+'年10月',temp1[0]+'年11月-'+temp1[0]+'年12月'])
            time_dev_peropd_list_1.append([temp1[0]+'年'+temp1[1]+'月-'+temp1[0]+'年'+str(min(12,int(int(temp1[1])+1)))+'月',temp1[0]+'年'+str(min(12,int(int(temp1[1])+1)))+'月-'+temp1[0]+'年'+str(max(int(12-int(temp1[1])),min(12,int(int(temp1[1])+1))))+'月',temp1[0]+'年11月-'+temp1[0]+'年12月'])
        time_dev_peropd_list.append(time_dev_peropd_list_1)
    res['start_time']=start_time_list
    res['end_time']=end_time_list
    res['time_period']= time_period_list
    res['time_length']=time_length_list
    res['dev_period']=time_dev_peropd_list
    return res

def generate_rd(file_name):
    f = open(file_name,encoding='utf-8')
    data = json.load(f)
    data_list = []
    #data=eval(file_name)
    #data = eval(json.loads(file_name))
    #print('data',data,type(data))

    for i in data:
        if i.get('规划表', 0) == 0:
            data_list.append(i)
        else:
            rd_list = i.get('规划表', 0)

    RD_keywords = 'RD'
    income_keywords = '简介'
    company_name = get_name(rd_list, income_keywords)
    dict_rd = get_rd_dict(rd_list, RD_keywords)
   # print("dict_rd['dev_time']", dict_rd['dev_time'])
    time = get_time_period(dict_rd['dev_time'])

    #temp = data_list[0]
    res_list=[]
    for temp in range(len(data_list)):
        if data_list[temp]['发明名称'] is not None:
            IP_name = data_list[temp]['发明名称']
        elif data_list[temp]['实用新型名称'] is not None:
            IP_name = data_list[temp]['实用新型名称']
        else:
            IP_name=dict_rd['ip_name'][temp]
        for d in "''()（）[]【】{}「」:：0123456789.、： " :
            IP_name = IP_name.replace(d, '')
        text = data_list[temp]['背景技术']
        if data_list[temp]['发明内容'] is not None:
            text2 = data_list[temp]['发明内容']
        elif  data_list[temp]['实用新型内容'] is not None:
            text2 = data_list[temp]['实用新型内容']
        else:
            text2 = data_list[temp]['具体实施方式']
        #print(len(text2))
        file_name = dict_rd['dev_name'][temp]
        #IP_name_from_rd=dict_rd['ip_name'][temp]
        print('IP_name',IP_name)
        print('file_name',file_name)
        text = text.replace(IP_name, file_name)
        text2 = text2.replace(IP_name, file_name)

        background_keywords = readfile('background_keywords.txt')
        bg_conclusion = get_conclusion_bg(text, background_keywords)
        aim_text = generate_background(bg_conclusion, file_name)
        print('背景技术：', len(aim_text), aim_text)
        key_tech = get_key_tech(text2)[0]
        key_tech_num = str(get_key_tech(text2)[1])
        key_tech_temp = get_key_tech(text2)[2]
        res_temp=get_key_tech(text2)[-1]
        if int(key_tech_num) >= 3:
            key_tech_example = ' '.join(key_tech_temp[:3])
        else:
            key_tech_example = str(key_tech_temp[0])
        innovation = get_innovation(text2,file_name,res_temp)
        print('核心技术：', key_tech)
        print('创新点：', innovation)

    # 打印模板
        template = "F:/flask学习资料/Python Flask全程实战-多功能博客系统开发/FlaskDemo-前端页面/lin/3-31/test_new.docx"

    # 创建邮件合并文档并查看所有字段
        document_1 = MailMerge(template)
        print("Fields included in {}: {}".format(template, document_1.get_merge_fields()))
        print("key_tech_example", key_tech_example, type(key_tech_example))
        print('key_tech_num',key_tech_num,type(key_tech_num))
        document_1.merge(
            company_name=company_name,
            dev_name=dict_rd['dev_name'][temp],
            start_time=time['start_time'][temp],
            end_time=time['end_time'][temp],
            dev_id=dict_rd['dev_id'][temp],
            back_ground=aim_text,
            key_tech=key_tech,
            innovation=innovation,
            time_length=str(time['time_length'][temp]),
            dev_period_1=str(time['dev_period'][temp][0][0]),
            dev_period_2=str(time['dev_period'][temp][0][1]),
            dev_period_3=str(time['dev_period'][temp][0][2]),
            key_tech_num=str(key_tech_num),
            key_tech_example=str(key_tech_example)
        )
        output_path="F:/flask学习资料/Python Flask全程实战-多功能博客系统开发/FlaskDemo-前端页面/lin/3-31/"+str(temp)+".docx"
        document_1.write(output_path)
        print('success_write')
        result_path=data_list[temp]['原始文件路径']
        res_list.append([output_path,result_path])
    return res_list
if __name__ == '__main__':

    input_file = '/Users/ture/BU/work/专利/3-31/httpParam.txt'
    #print(generate_rd(input_file))
