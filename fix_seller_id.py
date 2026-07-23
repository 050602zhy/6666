import pymysql
conn = pymysql.connect(host='127.0.0.1', user='root', password='050602zhy', database='ecommerce')
cursor = conn.cursor()

# 添加seller_id列
cursor.execute("SHOW COLUMNS FROM product LIKE 'seller_id'")
if not cursor.fetchone():
    cursor.execute("ALTER TABLE product ADD COLUMN seller_id BIGINT COMMENT '卖家用户ID'")
    print("已添加 seller_id 列")
else:
    print("seller_id 已存在")

# 查看用户
cursor.execute("SELECT id, username, nickname, role FROM user")
print("\n用户列表:")
for row in cursor.fetchall():
    print(f"  id={row[0]} username={row[1]} nickname={row[2]} role={row[3]}")

# 将商品的seller_id设为第一个seller角色的用户
cursor.execute("SELECT id, nickname FROM user WHERE role='seller' LIMIT 1")
seller = cursor.fetchone()
if seller:
    sid = seller[0]
    sname = seller[1]
    cursor.execute(f"UPDATE product SET seller_id = {sid}")
    print(f"\n已将所有商品的seller_id设为 {sid} ({sname})")
else:
    cursor.execute("UPDATE product SET seller_id = 2")
    print("\n未找到seller角色用户，已设seller_id=2")

conn.commit()
conn.close()
print("完成")