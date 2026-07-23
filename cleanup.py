import pymysql, requests

# 清理数据库
conn = pymysql.connect(host='127.0.0.1', user='root', password='050602zhy', database='ecommerce')
cursor = conn.cursor()
cursor.execute("DELETE FROM kb_document")
cursor.execute("DELETE FROM kb_knowledge_base")
conn.commit()
print("数据库已清理")
conn.close()

# 清理Chroma集合
try:
    r = requests.get('http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections')
    collections = r.json()
    for c in collections:
        name = c.get('name')
        if name and name.startswith('ecommerce_kb_'):
            requests.delete(f'http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections/{name}')
            print(f"删除Chroma集合: {name}")
except Exception as e:
    print("Chroma清理:", e)

print("清理完成")
