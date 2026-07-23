import pymysql

try:
    conn = pymysql.connect(host='127.0.0.1', user='root', password='050602zhy', database='ecommerce')
    cursor = conn.cursor()
    
    cursor.execute("SHOW COLUMNS FROM kb_knowledge_base LIKE 'collection_name'")
    if not cursor.fetchone():
        cursor.execute("ALTER TABLE kb_knowledge_base ADD COLUMN collection_name VARCHAR(100)")
        print("添加 collection_name 成功")
    else:
        print("collection_name 已存在")
    
    cursor.execute("SHOW COLUMNS FROM kb_document LIKE 'error_msg'")
    if not cursor.fetchone():
        cursor.execute("ALTER TABLE kb_document ADD COLUMN error_msg VARCHAR(500)")
        print("添加 error_msg 成功")
    else:
        print("error_msg 已存在")
    
    conn.commit()
    print("数据库字段更新完成")
except Exception as e:
    print("错误:", e)
finally:
    if 'conn' in locals():
        conn.close()
