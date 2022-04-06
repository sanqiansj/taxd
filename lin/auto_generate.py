# -*- coding: utf-8 -*-
import pandas as pd
import xlrd
import os
import random
import numpy as np
import xlutils.copy
import xlwt

def file_name(file_dir):
    L=[]
    for root, dirs, files in os.walk(file_dir):

        for file in files:
            if '大理' in os.path.splitext(file)[0]:
                L.append(os.path.join(root, file))
    return L

def readfile(filename):
    res = []
    with open(filename,'r',encoding='utf-8') as file:
        lines = file.readlines()
        for line in lines:
            res.append(line.strip())
    return res
def contain(keywords,desc,abstract):
    temp=str(desc)+str(abstract)
    for i in keywords:
        if i in temp and '应收' not in temp and '税' not in temp and '办公' not in temp:
            return True
    return False

def get_salary(file_name,target_row_year1):
    salary_keywords = readfile(file_name)
    salary_row = []
    for i in target_row_year1:
        if contain(salary_keywords, i[3], i[4]) and '预付' not in i[3]+i[4] and '应付' not in i[3] + i[4] and '代理' not in i[3] + i[4] and '收' not in i[3] + i[4] and '退回' not in i[3] + i[4] :
            salary_row.append(i)
    df_salary_year1 = pd.DataFrame(salary_row)
    df_salary_year1.columns = ['date', 'n_voucher', 'n_items', 'name_items', 'abstract', 'jie', 'amount']
    df_salary_year1['type'] = ['人员人工'] * len(df_salary_year1)
    df_salary_year1 = df_salary_year1.sort_values(by=["date", "n_voucher"])
    df_salary_year1 = df_salary_year1.drop_duplicates(subset=None, keep='first', inplace=False)
    df_salary_year1.reset_index(drop=True, inplace=True)
    name_list=df_salary_year1['name_items'].tolist()
    abstract_list=df_salary_year1['abstract'].tolist()
    name_abstract_list=[]
    for i in range(len(name_list)):
        name_abstract_list.append(name_list[i]+';'+abstract_list[i])
    df_salary_year1['abstract']=name_abstract_list
    jie_list=df_salary_year1['jie'].tolist()
    if '借' in jie_list:
        df_salary_year1_res = df_salary_year1[['date', 'n_voucher', 'abstract', 'amount','type']]
    else:
        df_salary_year1_res = df_salary_year1[['date', 'n_voucher', 'abstract', 'jie','type']]
        df_salary_year1_res.columns=['date', 'n_voucher', 'abstract', 'amount','type']

    df_salary_year1_res=df_salary_year1_res[df_salary_year1_res['amount']>=10]
    return df_salary_year1_res

def get_travel(target_row_year1):
    travel_row = []
    for i in target_row_year1:
        if '差旅' in i[3] + i[4] or '住宿' in i[3] + i[4]:
            if i not in travel_row and '租赁' not in i[3] + i[4]:
                travel_row.append(i)
    amount_travael_list = []
    for i in travel_row:
        if i[-2] =='借':
            amount_travael_list.append(i[-1])
        else:
            amount_travael_list.append(i[-2])
    max_amount_travel = sorted(amount_travael_list)[-2:]

    travel_selected = []
    for i in travel_row:
        if i[-1] in max_amount_travel or i[-2] in max_amount_travel:
            travel_selected.append(i)
    if len(travel_selected)==0:
        return
    df_travel_year1 = pd.DataFrame(travel_selected,
                             columns=['date', 'n_voucher', 'n_items', 'name_items', 'abstract', 'jie', 'amount'])
    df_travel_year1['type'] = ['其他费用'] * len(df_travel_year1)
    df_travel_year1 = df_travel_year1.sort_values(by=["date", "n_voucher"])
    df_travel_year1 = df_travel_year1.drop_duplicates(subset=None, keep='first', inplace=False)
    df_travel_year1.reset_index(drop=True, inplace=True)
    name_list=df_travel_year1['name_items'].tolist()
    abstract_list=df_travel_year1['abstract'].tolist()
    name_abstract_list=[]
    for i in range(len(name_list)):
        name_abstract_list.append(name_list[i]+';'+abstract_list[i])
    df_travel_year1['abstract']=name_abstract_list
    jie_list=df_travel_year1['jie'].tolist()
    if '借' in jie_list:
        df_travel_year1_res = df_travel_year1[['date', 'n_voucher', 'abstract', 'amount','type']]
    else:
        df_travel_year1_res = df_travel_year1[['date', 'n_voucher', 'abstract', 'jie','type']]
        df_travel_year1_res.columns=['date', 'n_voucher', 'abstract', 'amount','type']
    df_travel_year1_res = df_travel_year1_res[df_travel_year1_res['amount'] >= 10]
    return df_travel_year1_res

def get_old(target_row_year1):
    old_row = []
    for i in target_row_year1:
        if '旧' in i[3] + i[4] and '更正' not in i[3] + i[4]:
            old_row.append(i)
    if len(old_row)==0:
        return
    df_old_year1 = pd.DataFrame(old_row,
                          columns=['date', 'n_voucher', 'n_items', 'name_items', 'abstract', 'jie',
                                   'amount'])
    df_old_year1['type'] = ['折旧与长期待摊'] * len(df_old_year1)
    df_old_year1 = df_old_year1.sort_values(by=["date", "n_voucher"])
    df_old_year1 = df_old_year1.drop_duplicates(subset=None, keep='first', inplace=False)
    df_old_year1.reset_index(drop=True, inplace=True)
    name_list=df_old_year1['name_items'].tolist()
    abstract_list=df_old_year1['abstract'].tolist()
    name_abstract_list=[]
    for i in range(len(name_list)):
        name_abstract_list.append(name_list[i]+';'+abstract_list[i])
    df_old_year1['abstract']=name_abstract_list
    jie_list=df_old_year1['jie'].tolist()
    if '借' in jie_list:
        df_old_year1_res = df_old_year1[['date', 'n_voucher', 'abstract', 'amount','type']]
    else:
        df_old_year1_res = df_old_year1[['date', 'n_voucher', 'abstract', 'jie','type']]
        df_old_year1_res.columns=['date', 'n_voucher', 'abstract', 'amount','type']
    df_old_year1_res = df_old_year1_res[df_old_year1_res['amount'] >= 10]

    return df_old_year1_res

def get_service(target_row_year1):
    service_row = []
    for i in target_row_year1:
        if '开发' in i[3] + i[4] or '服务' in i[3] + i[4] or '软件费' in i[3]+i[4] or ('软件' in i[3]+i[4] and ('购入' in i[3]+i[4] or '摊销' in i[3]+i[4])):
            if '银行存款' not in i[3] + i[4] and '收入' not in i[3] + i[4] and '顺丰' not in i[3] + i[4] and '商标' not in i[3] + i[4] and '产权' not in i[3] + i[4] and '版权' not in i[3] + i[4] :
                service_row.append(i)
    if len(service_row)==0:
        return
    df_service_year1 = pd.DataFrame(service_row,
                              columns=['date', 'n_voucher', 'n_items', 'name_items', 'abstract', 'jie',
                                       'amount'])
    df_service_year1['type'] = ['委托外部研发'] * len(df_service_year1)

    df_service_year1 = df_service_year1.sort_values(by=["date", "n_voucher"])
    df_service_year1 = df_service_year1.drop_duplicates(subset=None, keep='first', inplace=False)

    df_service_year1.reset_index(drop=True, inplace=True)
    name_list=df_service_year1['name_items'].tolist()
    abstract_list=df_service_year1['abstract'].tolist()
    name_abstract_list=[]
    for i in range(len(name_list)):
        name_abstract_list.append(name_list[i]+';'+abstract_list[i])
    df_service_year1['abstract']=name_abstract_list
    jie_list=df_service_year1['jie'].tolist()
    if '借' in jie_list:
        df_service_year1_res = df_service_year1[['date', 'n_voucher', 'abstract', 'amount','type']]
    else:
        df_service_year1_res = df_service_year1[['date', 'n_voucher', 'abstract', 'jie','type']]
        df_service_year1_res.columns=['date', 'n_voucher', 'abstract', 'amount','type']
    df_service_year1_res = df_service_year1_res[df_service_year1_res['amount'] >= 10]
    return df_service_year1_res

def get_ip(target_row_year1):
    ip_row = []
    for i in target_row_year1:
        str1 = i[3] + i[4]
        if  '专利' in str1 or '商标' in str1 or '产权' in str1 or '软著' in str1 or '软件' in str1 or '版权' in str1:
            if '开发' not in str1 and '服务' not in str1 and '调账' not in str1 and '软件费' not in str1 :
                if not ('软件' in str1 and '摊销' in str1):
                    if not ('软件' in str1 and '购入' in str1):
                        if not ('软件' in str1 and '安装' in str1):
                            if not ('软件' in str1 and '系统' in str1):
                                ip_row.append(i)
    if len(ip_row)==0:
        return
    df_ip_year1 = pd.DataFrame(ip_row,
                          columns=['date', 'n_voucher', 'n_items', 'name_items', 'abstract', 'jie',
                                   'amount'])
    df_ip_year1['type'] = ['无形资产摊销'] * len(df_ip_year1)
    df_ip_year1 = df_ip_year1.sort_values(by=["date", "n_voucher"])
    df_ip_year1 = df_ip_year1.drop_duplicates(subset=None, keep='first', inplace=False)

    df_ip_year1.reset_index(drop=True, inplace=True)
    name_list=df_ip_year1['name_items'].tolist()
    abstract_list=df_ip_year1['abstract'].tolist()
    name_abstract_list=[]
    for i in range(len(name_list)):
        name_abstract_list.append(name_list[i]+';'+abstract_list[i])
    df_ip_year1['abstract']=name_abstract_list
    jie_list=df_ip_year1['jie'].tolist()
    if '借' in jie_list:
        df_ip_year1_res = df_ip_year1[['date', 'n_voucher', 'abstract', 'amount','type']]
    else:
        df_ip_year1_res = df_ip_year1[['date', 'n_voucher', 'abstract', 'jie','type']]
        df_ip_year1_res.columns=['date', 'n_voucher', 'abstract', 'amount','type']
    df_ip_year1_res = df_ip_year1_res[df_ip_year1_res['amount'] >= 10]
    return df_ip_year1_res

def get_material(target_row_year1, current_ratio, All_income_year1):
    material_row = []
    if current_ratio >= 0.15:
        material_ratio = 0.05
    else:
        material_ratio = min(0.05, (0.15 - current_ratio))
    material_target = material_ratio * All_income_year1

    for i in target_row_year1:
        str1 = i[3] + i[4]
        #print('str1',str1)
        if '材料' in str1 or ('购进' in str1 and '贸易' not in str1 and '手机' not in str1 and '办公' not in str1)\
                or ('应付账款' in str1 and '贸易' not in str1 and '手机' not in str1 and '办公' not in str1)\
                or ('支付' in str1 and '货款' in str1 and '贸易' not in str1 and '手机' not in str1 and '办公' not in str1):
            if i not in material_row:
                material_row.append(i)
    print('material_row',material_row)
    amount_material_list = []
    amount_material_list_sorted = []
    for i in material_row:
        if type(i[-2]) == float:
            amount_material_list.append(abs(i[-2] - material_target))
        else:
            amount_material_list.append(abs(i[-1] - material_target))
        amount_material_list_sorted = sorted(amount_material_list)
    if len(amount_material_list_sorted)==0:
        return pd.DataFrame()
    min_amount_material = amount_material_list_sorted[0]
    #print('amount_material_list_sorted',amount_material_list_sorted)
    #print('min_amount_material',min_amount_material)

    material_selected = []
    material_row_sorted = []
    #    print('amount_material_list_sorted',amount_material_list_sorted)
    for i in amount_material_list_sorted:
        for j in material_row:
            if type(j[-2]) == float:
                temp = j[-2]
            else:
                temp = j[-1]
            if abs(temp - material_target) == i:
                material_row_sorted.append(j)
                break
    if current_ratio > 0.05:
        for i in material_row:
            if type(i[-2]) == float:
                temp2 = i[-2]
            else:
                temp2 = i[-1]
            if abs(temp2 - material_target) == min_amount_material:
                material_selected.append(i)
    else:
        for i in material_row_sorted:
            # print('cr_change',current_ratio)
            if current_ratio <= 0.075:
                material_selected.append(i)
                if type(i[-2]) == float:
                    temp3 = i[-2]
                else:
                    temp3 = i[-1]
                current_ratio += temp3 / All_income_year1
            else:
                break
    #print(material_selected)
    if material_selected != []:
        df_material = pd.DataFrame(material_selected,
                                   columns=['date', 'n_voucher', 'n_items', 'name_items', 'abstract', 'jie',
                                            'amount'])

        df_material['type'] = ['直接投入'] * len(df_material)
        df_material['abstract'] = df_material.apply(lambda x: str(x['name_items']) + x['abstract'], axis=1)
        jie_list = df_material['jie'].tolist()
        if '借' in jie_list:
            df_rd_material = df_material[['date', 'n_voucher', 'abstract', 'amount', 'type']]
        else:
            df_rd_material = df_material[['date', 'n_voucher', 'abstract', 'jie', 'type']]
            df_rd_material.columns = ['date', 'n_voucher', 'abstract', 'amount', 'type']
          # print(df_rd_material)
        df_rd_material = df_rd_material[df_rd_material['amount'] >= 10]
        return df_rd_material
    else:
        return pd.DataFrame()

def read_rd_excel_sheet(filename,keywords):

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
    rd_year1 = []
    rd_year2 = []
    rd_year3 = []
    for i in rows_list:
        if '2019' in i[3]:
            rd_year1.append((i[0], i[1],i[3]))
        elif '2020' in i[3]:
            rd_year2.append((i[0], i[1],i[3]))
        else:
            rd_year3.append((i[0], i[1],i[3]))
    dict_rd = {'year1': rd_year1, 'year2': rd_year2, 'year3': rd_year3}
    return dict_rd

def get_income(filename,keywords):
    rows_list = read_rd_excel_sheet(filename, keywords)
    year1_income=0
    year2_income = 0
    year3_income = 0
    for i in rows_list:
        if '2019年销售收入' in i[2]:
            year1_income=float(i[2][i[2].find('2019年销售收入')+9:i[2].find('万元')])*10000
        if '2020年销售收入' in i[2]:
            i[2]=i[2][i[2].find('2020年销售收入'):]
            year2_income=float(i[2][i[2].find('2020年销售收入')+9:i[2].find('万元')])*10000

        if '2021年销售收入' in i[2]:
            i[2] = i[2][i[2].find('2021年销售收入'):]
            year3_income=float(i[2][i[2].find('2021年销售收入')+9:i[2].find('万元')])*10000

    dict_income={'year1':year1_income,'year2':year2_income,'year3':year3_income}
    return dict_income

def get_name(filename,keywords):
    rows_list = read_rd_excel_sheet(filename, keywords)
    name='XX公司'
    for i in rows_list:
        if '公司成立' in i[2]:
            name=i[2][:i[2].find('公司成立')+2]
    return name

def get_rd_ratio(num_rd):
    dev_ratio = round(random.uniform(0.81, 0.92), 2)
    n = num_rd
    rd_rate = []
    for i in range(n):
        rd_rate.append(round(random.uniform(0.14, dev_ratio / n), 2))
    return rd_rate

def get_rd_dataframe(df_rd,rd_ratio):
    if df_rd is not None and 'amount' in df_rd.columns:
        #print(df_rd)
        df_rd_n=df_rd
        df_rd_n['amount']=df_rd.apply(lambda col: round(col['amount']*rd_ratio,2), axis=1)
        return df_rd_n
    else: return

def read_detailed_bills(filename,keywords):
    workbook = xlrd.open_workbook(filename)
    worksheets=workbook.sheet_names()
    worksheet1=workbook.sheets()[0]
    num_rows = worksheet1.nrows
    rows_list=[]
    for curr_row in range(num_rows):
        row = worksheet1.row_values(curr_row)
        rows_list.append(row)

    rows_list=rows_list[1:]

    target_row_year1=[]
    for i in rows_list:
        if '借' in str(i[5]) or '贷' in str(i[5]):
            if '借' in str(i[5]) and str(i[-1])!='' and '-' not in str(i[-1]):
                if contain(keywords,i[3],i[4]):
                    target_row_year1.append(i)
        else:
            if str(i[5])!='' and '-' not in str(i[5]) and i[5]!=0:
                if contain(keywords,i[3],i[4]):
                    target_row_year1.append(i)
    return target_row_year1

def auto_sup_bills(filedir,company_name):
    company_name = company_name
    curpath = os.path.realpath(__file__)  # 获取当前文件绝对路径
    dirpath = os.path.dirname(curpath)  # 获取当前文件的文件夹路径
    #filedir = os.path.join(dirpath, '大理')
    #file_name_list = file_name(filedir)
    file_name_list = filedir
    print(file_name_list)
    rd_list = []
    bill_list = []
    for i in file_name_list:
        #print("i.split('/')[-1]",i.split("/")[-1])
        if '账' in i.split("/")[-1] and '规划表' not in i.split("/")[-1]:
            bill_list.append(i.strip())
        else:
            if '~$' not in i:
                rd_list.append(i.strip())
    keywords = readfile('dev_keywords.txt')

    bill_list = sorted(bill_list)
    print('bill:',bill_list)
    print('rd',rd_list)
    target_row_year1 = read_detailed_bills(bill_list[0], keywords)
    target_row_year2 = read_detailed_bills(bill_list[1], keywords)
    target_row_year3 = read_detailed_bills(bill_list[2], keywords)

    # 人员人工
    salary_keyword = 'salary_keywords.txt'
    #df_salary_year1 = get_salary(salary_keyword, target_row_year1)  # pandas dataframe
    #df_salary_year2 = get_salary(salary_keyword, target_row_year2)  # pandas dataframe
    #df_salary_year3 = get_salary(salary_keyword, target_row_year3)  # pandas dataframe

    # 差旅费
    #df_travel_year1 = get_travel(target_row_year1)  # pandas dataframe
   # df_travel_year2 = get_travel(target_row_year2)  # pandas dataframe
   # df_travel_year3 = get_travel(target_row_year3)  # pandas dataframe

    # 折旧
    #df_old_year1 = get_old(target_row_year1)  # pandas dataframe
    #df_old_year2 = get_old(target_row_year2)  # pandas dataframe
    #df_old_year3 = get_old(target_row_year3)  # pandas dataframe

    # 委托外部服务费
    #df_service_year1 = get_service(target_row_year1)  # pandas dataframe
    #df_service_year2 = get_service(target_row_year2)  # pandas dataframe
    #df_service_year3 = get_service(target_row_year3)  # pandas dataframe

    # 知识产权的
    #df_ip_year1 = get_ip(target_row_year1)  # pandas dataframe
    #df_ip_year2 = get_ip(target_row_year2)  # pandas dataframe
    #df_ip_year3 = get_ip(target_row_year3)  # pandas dataframe
    # print('dfip3:',df_ip_year1)
    # 读取RD规划表和公司年收入
    RD_keywords = 'RD'
    dict_rd = get_rd_dict(rd_list[0], RD_keywords)
    num_rd_year1 = len(dict_rd['year1'])
    num_rd_year2 = len(dict_rd['year2'])
    num_rd_year3 = len(dict_rd['year3'])
    num_rd = num_rd_year1 + num_rd_year2 + num_rd_year3
    income_keywords = '简介'
    All_income_year1 = get_income(rd_list[0], income_keywords)['year1']
    All_income_year2 = get_income(rd_list[0], income_keywords)['year2']
    All_income_year3 = get_income(rd_list[0], income_keywords)['year3']
    #company_name = get_name(rd_list[0], income_keywords)
    dict_rd_ritio = {'year1': get_rd_ratio(num_rd_year1),
                     'year2': get_rd_ratio(num_rd_year2),
                     'year3': get_rd_ratio(num_rd_year3)}
    rd_ritio_list = []
    for i in dict_rd_ritio:
        for j in dict_rd_ritio[i]:
            rd_ritio_list.append(j)
    df_rd_salary_list = []
    df_rd_travel_list = []
    df_rd_old_list = []
    df_rd_service_list = []
    df_rd_ip_list = []
    count_rd_generate = 0
    for i in rd_ritio_list:
        count_rd_generate += 1
        if count_rd_generate > num_rd_year1 + num_rd_year2:
            target_row_year1_temp = target_row_year3
        elif count_rd_generate > num_rd_year1:
            target_row_year1_temp = target_row_year2
        else:
            target_row_year1_temp = target_row_year1
        df_rd_salary_list.append(get_rd_dataframe(get_salary(salary_keyword, target_row_year1_temp), i))
        df_rd_travel_list.append(get_rd_dataframe(get_travel(target_row_year1_temp), i * 0.7))
        df_rd_old_list.append(get_rd_dataframe(get_old(target_row_year1_temp), i / 3))
        df_rd_service_list.append(get_rd_dataframe(get_service(target_row_year1_temp), i * 0.8))
        df_rd_ip_list.append(get_rd_dataframe(get_ip(target_row_year1_temp), i))

    # 第一年人员工资
    df_rd_year1_list = []
    for i in range(num_rd_year1):
        df_rd_01 = pd.concat(
            [df_rd_salary_list[i], df_rd_travel_list[i], df_rd_old_list[i], df_rd_service_list[i], df_rd_ip_list[i]],
            ignore_index=True).sort_values(by=["date", "n_voucher"])
        df_rd_01.reset_index(drop=True, inplace=True)
        df_rd_year1_list.append(df_rd_01)
    # df_rd_02=pd.concat([df_rd_02_salary,df_rd_02_travel,df_rd_02_old,df_rd_02_service,df_rd_02_ip],ignore_index=True).sort_values(by=["date", "n_voucher"])
    # df_rd_02.reset_index(drop=True, inplace=True)
    if len(df_rd_year1_list)!=0:
        df_all_final = pd.concat(df_rd_year1_list)
        sum_amount = np.sum(df_all_final['amount'].tolist())
        current_ratio = sum_amount / All_income_year1
    else:
        df_all_final=pd.DataFrame()
        sum_amount=0
        current_ratio=0
    # 材料费
    df_material = get_material(target_row_year1, current_ratio, All_income_year1)
    df_rd_year1_material_list = []
    df_rd_year1_final_list = []
    for i in range(num_rd_year1):
        if len(df_material) != 0:

            df_rd_01_material = get_rd_dataframe(get_material(target_row_year1, current_ratio, All_income_year1),
                                                 rd_ritio_list[i])
            df_rd_year1_material_list.append(df_rd_01_material)

            df_rd_01_final = pd.concat(
                [df_rd_salary_list[i], df_rd_travel_list[i], df_rd_old_list[i], df_rd_service_list[i], df_rd_ip_list[i],
                 df_rd_year1_material_list[i]],
                ignore_index=True).sort_values(by=["date", "n_voucher"])
            df_rd_01_final.reset_index(drop=True, inplace=True)
            df_rd_year1_final_list.append(df_rd_01_final)
        else:
            df_rd_01_final = pd.concat(
                [df_rd_salary_list[i], df_rd_travel_list[i], df_rd_old_list[i], df_rd_service_list[i],
                 df_rd_ip_list[i]],
                ignore_index=True).sort_values(by=["date", "n_voucher"])
            df_rd_01_final.reset_index(drop=True, inplace=True)
            df_rd_year1_final_list.append(df_rd_01_final)

    # 第二年人员工资
    df_rd_year2_list = []
    for i in range(num_rd_year2):
        df_rd_01 = pd.concat(
            [df_rd_salary_list[num_rd_year1 + i], df_rd_travel_list[num_rd_year1 + i], df_rd_old_list[num_rd_year1 + i],
             df_rd_service_list[num_rd_year1 + i], df_rd_ip_list[num_rd_year1 + i]],
            ignore_index=True).sort_values(by=["date", "n_voucher"])
        df_rd_01.reset_index(drop=True, inplace=True)
        df_rd_year2_list.append(df_rd_01)

    if len(df_rd_year2_list)!=0:
        df_all_final_19 = pd.concat(df_rd_year2_list)
        sum_amount_19 = np.sum(df_all_final_19['amount'].tolist())
        current_ratio_19 = sum_amount_19 / All_income_year2
    else:
        df_all_final_19=pd.DataFrame()
        sum_amount_19=0
        current_ratio_19=0

    # 材料费19年
    df_materia_19 = get_material(target_row_year2, current_ratio_19, All_income_year2)
    df_rd_year2_material_list = []
    df_rd_year2_final_list = []
    for i in range(num_rd_year2):
        if len(df_materia_19) != 0:

            df_rd_01_material = get_rd_dataframe(get_material(target_row_year2, current_ratio_19, All_income_year2),
                                                 rd_ritio_list[num_rd_year1 + i])
            df_rd_year2_material_list.append(df_rd_01_material)

            df_rd_temp_final = pd.concat([df_rd_salary_list[num_rd_year1 + i], df_rd_travel_list[num_rd_year1 + i],
                                          df_rd_old_list[num_rd_year1 + i], df_rd_service_list[num_rd_year1 + i],
                                          df_rd_ip_list[num_rd_year1 + i], df_rd_year2_material_list[i]],
                                         ignore_index=True).sort_values(by=["date", "n_voucher"])
            df_rd_temp_final.reset_index(drop=True, inplace=True)
            df_rd_year2_final_list.append(df_rd_temp_final)
        else:
            df_rd_temp_final = pd.concat(
                [df_rd_salary_list[num_rd_year1 + i], df_rd_travel_list[num_rd_year1 + i],
                 df_rd_old_list[num_rd_year1 + i],
                 df_rd_service_list[num_rd_year1 + i], df_rd_ip_list[num_rd_year1 + i]],
                ignore_index=True).sort_values(by=["date", "n_voucher"])
            df_rd_temp_final.reset_index(drop=True, inplace=True)
            df_rd_year2_final_list.append(df_rd_temp_final)

    # 第三年人员工资
    df_rd_year3_list = []
    for i in range(num_rd_year3):
        df_rd_01 = pd.concat(
            [df_rd_salary_list[num_rd_year1 + num_rd_year2 + i], df_rd_travel_list[num_rd_year1 + num_rd_year2 + i],
             df_rd_old_list[num_rd_year1 + num_rd_year2 + i],
             df_rd_service_list[num_rd_year1 + num_rd_year2 + i], df_rd_ip_list[num_rd_year1 + num_rd_year2 + i]],
            ignore_index=True).sort_values(by=["date", "n_voucher"])
        df_rd_01.reset_index(drop=True, inplace=True)
        df_rd_year3_list.append(df_rd_01)

    df_all_final_20 = pd.concat(df_rd_year3_list)
    sum_amount_20 = np.sum(df_all_final_20['amount'].tolist())
    current_ratio_20 = sum_amount_20 / All_income_year3

    # 材料费20年
    df_materia_20 = get_material(target_row_year3, current_ratio_20, All_income_year3)
    df_rd_year3_material_list = []
    df_rd_year3_final_list = []
    for i in range(num_rd_year3):
        if len(df_materia_20) != 0:

            df_rd_01_material = get_rd_dataframe(get_material(target_row_year3, current_ratio_20, All_income_year3),
                                                 rd_ritio_list[num_rd_year1 + num_rd_year2 + i])
            df_rd_year3_material_list.append(df_rd_01_material)

            df_rd_03_final = pd.concat(
                [df_rd_salary_list[num_rd_year1 + num_rd_year2 + i], df_rd_travel_list[num_rd_year1 + num_rd_year2 + i],
                 df_rd_old_list[num_rd_year1 + num_rd_year2 + i],
                 df_rd_service_list[num_rd_year1 + num_rd_year2 + i], df_rd_ip_list[num_rd_year1 + num_rd_year2 + i],
                 df_rd_year3_material_list[i]],
                ignore_index=True).sort_values(by=["date", "n_voucher"])
            df_rd_03_final.reset_index(drop=True, inplace=True)
            df_rd_year3_final_list.append(df_rd_03_final)
        else:
            df_rd_03_final = pd.concat(
                [df_rd_salary_list[num_rd_year1 + num_rd_year2 + i], df_rd_travel_list[num_rd_year1 + num_rd_year2 + i],
                 df_rd_old_list[num_rd_year1 + num_rd_year2 + i],
                 df_rd_service_list[num_rd_year1 + num_rd_year2 + i], df_rd_ip_list[num_rd_year1 + num_rd_year2 + i]],
                ignore_index=True).sort_values(by=["date", "n_voucher"])
            df_rd_03_final.reset_index(drop=True, inplace=True)
            df_rd_year3_final_list.append(df_rd_03_final)

    resdir = os.path.join(dirpath, 'dev_template.xlsx')

    rd_list_res = dict_rd['year1'] + dict_rd['year2'] + dict_rd['year3']

    rb = xlrd.open_workbook(resdir)
    wb = xlutils.copy.copy(rb)
    # 获取sheet对象，通过sheet_by_index()获取的sheet对象没有write()方法

    ws_rd = wb.get_sheet(0)
    ws0 = wb.get_sheet(2)
    ws_list = []
    for i in range(1, num_rd + 1):
        ws_list.append(wb.get_sheet(i + 2))

    # 写入rdps表
    for i in range(len(rd_list_res)):
        ws_rd.write(i + 2, 0, rd_list_res[i][0])
        ws_rd.write(i + 2, 1, rd_list_res[i][1])
        ws_rd.write(i + 2, 2, rd_list_res[i][2])
    num_rows_list = []
    for i in range(len(ws_list)):
        if i < len(df_rd_year1_final_list):
            df_rd_temp_final = df_rd_year1_final_list[i]
        elif len(df_rd_year1_final_list) <= i < len(df_rd_year1_final_list) + len(df_rd_year2_final_list):
            df_rd_temp_final = df_rd_year2_final_list[i - len(df_rd_year1_final_list)]
        else:
            df_rd_temp_final = df_rd_year3_final_list[i - len(df_rd_year2_final_list) - len(df_rd_year1_final_list)]
        ws_list[i].write(2, 5, xlwt.Formula("'RDPS'!B" + str(i + 3)))
        index_list = df_rd_temp_final.index.tolist()
        if type(df_rd_temp_final['date'].tolist()[0])==float:
            year_dict={'2019':range(43466,43831),'2020':range(43831,44197),'2021':range(44197,44562)}
            dates_float=df_rd_temp_final['date'].tolist()[0]
            if int(dates_float) in year_dict['2019']:
                year='2019'
            elif int(dates_float) in year_dict['2020']:
                year='2020'
            else:
                year = '2021'
        else:
            year = df_rd_temp_final['date'].tolist()[0][:4]
        ws_list[i].write_merge(4, 5, 1, 1, year)
        for j in index_list:
            ws_list[i].write(j + 6, 1, df_rd_temp_final['date'].tolist()[j])
            ws_list[i].write(j + 6, 3, df_rd_temp_final['n_voucher'].tolist()[j])
            ws_list[i].write(j + 6, 4, df_rd_temp_final['abstract'].tolist()[j])
            ws_list[i].write(j + 6, 5, df_rd_temp_final['amount'].tolist()[j])
            if df_rd_temp_final['type'].tolist()[j] == '人员人工':
                ws_list[i].write(j + 6, 6, df_rd_temp_final['amount'].tolist()[j])
            elif df_rd_temp_final['type'].tolist()[j] == '直接投入':
                ws_list[i].write(j + 6, 7, df_rd_temp_final['amount'].tolist()[j])
            elif df_rd_temp_final['type'].tolist()[j] == '折旧与长期待摊':
                ws_list[i].write(j + 6, 8, df_rd_temp_final['amount'].tolist()[j])
            elif df_rd_temp_final['type'].tolist()[j] == '无形资产摊销':
                ws_list[i].write(j + 6, 10, df_rd_temp_final['amount'].tolist()[j])
            elif df_rd_temp_final['type'].tolist()[j] == '委托外部研发':
                ws_list[i].write(j + 6, 12, df_rd_temp_final['amount'].tolist()[j])
            elif df_rd_temp_final['type'].tolist()[j] == '其他费用':
                ws_list[i].write(j + 6, 13, df_rd_temp_final['amount'].tolist()[j])
        num_rows_2 = index_list[-1] + 7
        num_rows_list.append(num_rows_2)
        ws_list[i].write_merge(num_rows_2, num_rows_2, 1, 4, '本年累计')

        ws_list[i].write(num_rows_2, 5, xlwt.Formula("SUM(F7:F" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 6, xlwt.Formula("SUM(G7:G" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 7, xlwt.Formula("SUM(H7:H" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 8, xlwt.Formula("SUM(I7:I" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 9, xlwt.Formula("SUM(J7:J" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 10, xlwt.Formula("SUM(K7:K" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 11, xlwt.Formula("SUM(L7:L" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 12, xlwt.Formula("SUM(M7:M" + str(num_rows_2) + ")"))
        ws_list[i].write(num_rows_2, 13, xlwt.Formula("SUM(N7:N" + str(num_rows_2) + ")"))

    for i in range(1, len(rd_list_res) + 1):
        ws0.write(i + 1, 0, xlwt.Formula("'RDPS'!A" + str(i + 2)))
        ws0.write(i + 1, 1, xlwt.Formula("'RDPS'!B" + str(i + 2)))
        ws0.write(i + 1, 2, xlwt.Formula("'RD" + str(i) + "'!" + "G" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 3, xlwt.Formula("'RD" + str(i) + "'!" + "H" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 4, xlwt.Formula("'RD" + str(i) + "'!" + "I" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 5, xlwt.Formula("'RD" + str(i) + "'!" + "J" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 6, xlwt.Formula("'RD" + str(i) + "'!" + "K" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 7, xlwt.Formula("'RD" + str(i) + "'!" + "L" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 8, xlwt.Formula("'RD" + str(i) + "'!" + "M" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 9, xlwt.Formula("'RD" + str(i) + "'!" + "N" + str(eval(str(num_rows_list[i - 1])) + 1)))
        ws0.write(i + 1, 10, xlwt.Formula("SUM(C" + str(i + 2) + ":J" + str(i + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 0, '合计')
    ws0.write(len(rd_list_res) + 2, 1, '合计')
    ws0.write(len(rd_list_res) + 2, 2, xlwt.Formula("SUM(C3:C" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 3, xlwt.Formula("SUM(D3:D" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 4, xlwt.Formula("SUM(E3:E" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 5, xlwt.Formula("SUM(F3:F" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 6, xlwt.Formula("SUM(G3:G" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 7, xlwt.Formula("SUM(H3:H" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 8, xlwt.Formula("SUM(I3:I" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 9, xlwt.Formula("SUM(J3:J" + str(len(rd_list_res) + 2) + ")"))
    ws0.write(len(rd_list_res) + 2, 10, xlwt.Formula("SUM(K3:K" + str(len(rd_list_res) + 2) + ")"))

    company_name_path = company_name + '研发费用明细账.xls'
    output_path = os.path.join(dirpath, company_name_path)
    wb.save(output_path)
    return output_path


if __name__ == '__main__':
    file_dir='/Users/ture/BU/work/专利/大理'
    company_name='大理欧普智能科技有限公司公司公司'
    rd_list='/Users/ture/BU/work/专利/大理/大理欧普智能科技有限公司规划表.xls'
    RD_keywords = 'RD'
    dict_rd = get_rd_dict(rd_list, RD_keywords)
    print(dict_rd)

