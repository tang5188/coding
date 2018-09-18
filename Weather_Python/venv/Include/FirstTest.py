import requests
import time
import random
import socket
import http.client
import pymysql
from bs4 import BeautifulSoup
import csv


def getContent(url, data=None):
    header = {
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Encoding': 'gzip, deflate, sdch',
        'Accept-Language': 'zh-CN,zh;q=0.8',
        'Connection': 'keep-alive',
        'User-Agent': 'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.235'
    }  # rquest请求头
    timeout = random.choice(range(80, 180))
    while True:
        try:
            req = requests.get(url, headers=header, timeout=timeout)
            req.encoding = 'utf-8'
            break
        except socket.timeout as e:  # 以下都是异常处理
            print('3:', e)
            time.sleep(random.choice(range(8, 15)))
        except socket.error as e:
            print('4:', e)
            time.sleep(random.choice(range(20, 60)))
        except http.client.BadStatusLine as e:
            print('5:', e)
            time.sleep(random.choice(range(30, 80)))
        except http.client.IncompleteRead as e:
            print('6:', e)
            time.sleep(random.choice(range(5, 15)))
    print('request success')
    return req.text  # 返回html全文


def getData(html_text):
    ret = []
    bs = BeautifulSoup(html_text, 'html.parser')  # 创建BeautifulSoup对象
    body = bs.body  # 获取body
    data = body.find('div', {'id': '7d'})
    ul = data.find('ul')
    li = ul.find_all('li')

    for day in li:
        temp = []
        date = day.find('h1').string
        temp.append(date)
        inf = day.find_all('p')
        weather = inf[0].string
        temp.append(weather)

        temperature_high = "-1"
        if inf[1].find('span') is not None:  # 最高温度有可能不表示
            temperature_high = inf[1].find('span').string
        temperature_low = inf[1].find('i').string
        temp.append(temperature_low)
        temp.append(temperature_high)
        ret.append(temp)
    print('get data success')
    return ret


def writeData(data, name):
    with open(name, 'a', errors='ignore', newline='') as f:
        f_csv = csv.writer(f)
        f_csv.writerows(data)
    print('write csv success')


def createTable():
    # 打开数据库连接
    db = pymysql.connect('localhost', 'root', 'rf123456', 'demo_idea')
    # 使用cursor()方法创建一个游标对象 cursor
    cursor = db.cursor()
    # 使用execute()方法执行sql查询
    cursor.execute("select version()")
    # 使用feachone()方法获取单条数据
    data = cursor.fetchone()
    # 显示数据库版本（仅作为python连接数据库的例子）
    print('Database version: %s' % data)
    # 如果数据库中已经含有weather表，则不需要继续创建表
    cursor.execute("show tables like 'weather'")
    data = cursor.fetchone()
    if len(data) == 1 and data[0] == 'weather':
        return
    # 创建表
    cursor.execute("drop table if exists weather")
    sql = """create table weather(
                w_id int(8) not null primary key auto_increment,
                w_date varchar(20) not null,
                w_detail varchar(30),
                w_temperature_low varchar(10),
                w_temperature_high varchar(10)
              ) default charset=utf8"""
    cursor.execute(sql)
    # 关闭数据库连接
    db.close()
    print('create table success')


def insert_data(datas):
    db = pymysql.connect('localhost', 'root', 'rf123456', 'demo_idea')
    cursor = db.cursor()
    try:
        cursor.executemany(
            'insert into weather(w_date, w_detail, w_temperature_low, w_temperature_high) value(%s, %s, %s, %s)',
            datas)
        db.commit()
    except Exception as e:
        print('插入时发生异常' + e)
        db.rollback()
    db.close()


# 关联网站页面：https://www.cnblogs.com/zhaww/p/9517514.html
if __name__ == '__main__':
    url = 'http://www.weather.com.cn/weather/101210101.shtml'
    html = getContent(url)  # 调用获取网页信息
    result = getData(html)  # 解析网页信息，拿到需要的数据
    writeData(result, 'D:\Temp\weather.csv')  # 数据写到csv文件中
    createTable()  # 创建数据库表
    insert_data(result)  # 批量写入数据
    print(result)
