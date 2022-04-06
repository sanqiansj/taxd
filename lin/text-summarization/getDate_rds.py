# -*- coding:utf-8 -*-
import re
import os
# import docx
from flask import Flask
from flask import request
import json
from auto_rd import generate_rd

app = Flask(__name__)
app.debug=True

'''
def readdocx2(filename):
    try:
        file=docx.Document(filename)
        N = len(file.paragraphs)
        content = file.paragraphs[0].text.replace('\n','').replace('\r','').replace('\t','')+'。'
        for i in range(N):
            content += file.paragraphs[i].text.replace('\n','').replace('\r','').replace('\t','')
        return content
    except:
        return 
'''

@app.route('/Date', methods=["GET"])

def getDate():
    myfile = request.args.get('file_name')
    #print('input_data:', myfile)
    #company_name = myfile.split('#')[0]
    #input_file_dir = myfile.split('#')[1]
    #input_file_list = input_file_dir.split(',')
    res = generate_rd(myfile)
    #print('res',res)
    res_list=[]
    for i in res:
        res1 = {}
        res1['saved_path'] = i[0]
        res1['result_path']=i[1]
        res_list.append(res1)
    res2 = {
        "status": 200,
        "msg": "completed",
    }
    res2['result'] = res_list
    print(res2)
    return res2


if __name__ == '__main__':
    app.run(host='0.0.0.0', port='5001', debug=True)