import pymysql
conn = pymysql.connect(host='127.0.0.1', user='root', password='050602zhy', database='ecommerce')
cursor = conn.cursor()

# 查看所有表
cursor.execute("SHOW TABLES")
tables = [t[0] for t in cursor.fetchall()]
print("所有表:", tables)

# 找用户表
user_tables = [t for t in tables if 'user' in t.lower()]
print("用户相关表:", user_tables)

for t in user_tables:
    cursor.execute(f"SHOW COLUMNS FROM `{t}`")
    cols = [c[0] for c in cursor.fetchall()]
    print(f"\n{t} 字段: {cols}")
    cursor.execute(f"SELECT * FROM `{t}` LIMIT 5")
    for row in cursor.fetchall():
        print(f"  {row}")

conn.close()