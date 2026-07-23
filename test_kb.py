import requests, time

BASE = "http://localhost:8081"

# 1. 创建知识库
print("=== 1. 创建知识库 ===")
r = requests.post(f"{BASE}/ai/knowledge/create", params={
    "name": "测试知识库",
    "description": "用于测试RAG功能",
    "type": 1,
    "creatorId": 1
})
d = r.json()
print(f"状态: {d.get('code')}, 数据: {d.get('data')}")
kb_id = d.get('data', {}).get('id')
if not kb_id:
    print("创建知识库失败")
    exit(1)
print(f"知识库ID: {kb_id}")

# 2. 上传文档
print("\n=== 2. 上传文档 ===")
with open("c:/Users/周浩源/Downloads/test.txt", "rb") as f:
    files = {"file": ("test.txt", f, "text/plain")}
    r = requests.post(f"{BASE}/ai/knowledge/{kb_id}/upload", files=files)
    d = r.json()
    print(f"状态: {d.get('code')}, 消息: {d.get('message')}")
    doc = d.get('data')
    if doc:
        print(f"文档ID: {doc.get('id')}, 名称: {doc.get('name')}, 状态: {doc.get('status')}")

# 3. 等待解析完成
print("\n=== 3. 等待解析完成 (最多60秒) ===")
for i in range(12):
    time.sleep(5)
    r = requests.get(f"{BASE}/ai/knowledge/{kb_id}/documents")
    docs = r.json().get("data", [])
    if docs:
        status = docs[0].get("status")
        chunk_count = docs[0].get("chunkCount")
        error_msg = docs[0].get("errorMsg")
        print(f"  第{i+1}次检查: status={status}, chunks={chunk_count}, error={error_msg}")
        if status == 1:
            print("文档解析完成!")
            break
        elif status == 2:
            print(f"文档解析失败: {error_msg}")
            break
    else:
        print(f"  第{i+1}次检查: 无文档")

# 4. 测试问答
print("\n=== 4. 测试问答 ===")
r = requests.post(f"{BASE}/ai/knowledge/{kb_id}/chat", params={"question": "测试文档内容是什么"})
d = r.json()
print(f"状态: {d.get('code')}")
print(f"回答: {d.get('data', {}).get('content', '无内容')[:100]}...")

# 5. 测试删除文档
print("\n=== 5. 测试删除文档 ===")
doc_id = doc.get("id") if doc else None
if doc_id:
    r = requests.delete(f"{BASE}/ai/knowledge/doc/{doc_id}")
    print(f"删除文档: {r.json().get('code')}")

# 6. 测试删除知识库
print("\n=== 6. 测试删除知识库 ===")
r = requests.delete(f"{BASE}/ai/knowledge/{kb_id}")
print(f"删除知识库: {r.json().get('code')}")

print("\n=== 全部测试完成 ===")
