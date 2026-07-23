#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
智能电商运营平台 - 全链路验收脚本

使用方法:
    python acceptance-test.py [--host localhost] [--port 8080]

验收场景:
    1. 用户登录链路
    2. 商品查询链路
    3. 订单创建链路
    4. AI智能客服链路
    5. 知识库管理链路
    6. 情感分析链路
    7. 服务健康检查
"""

import requests
import sys
import argparse
from datetime import datetime

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    END = '\033[0m'

class AcceptanceTest:
    def __init__(self, host="localhost", port=8080):
        self.base_url = f"http://{host}:{port}"
        self.gateway_url = f"http://{host}:{port}"
        self.biz_url = f"http://{host}:8082"
        self.ai_url = f"http://{host}:8081"
        self.passed = 0
        self.failed = 0
        self.token = None

    def log(self, message, color=Colors.BLUE):
        print(f"{color}[{datetime.now().strftime('%H:%M:%S')}] {message}{Colors.END}")

    def assert_test(self, name, condition, detail=""):
        if condition:
            self.log(f"  PASS: {name}", Colors.GREEN)
            self.passed += 1
        else:
            self.log(f"  FAIL: {name} {detail}", Colors.RED)
            self.failed += 1

    def run_all(self):
        print(f"\n{'='*60}")
        print(f"  智能电商运营平台 - 全链路验收测试")
        print(f"  网关地址: {self.gateway_url}")
        print(f"  测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"{'='*60}\n")

        self.test_health_check()
        self.test_user_login()
        self.test_product_flow()
        self.test_order_flow()
        self.test_ai_chat()
        self.test_sentiment()
        self.test_knowledge_base()

        print(f"\n{'='*60}")
        print(f"  验收结果汇总")
        print(f"{'='*60}")
        print(f"  通过: {self.passed}")
        print(f"  失败: {self.failed}")
        print(f"  总计: {self.passed + self.failed}")
        if self.failed == 0:
            print(f"  状态: {Colors.GREEN}全部通过{Colors.END}")
        else:
            print(f"  状态: {Colors.RED}存在失败用例{Colors.END}")
        print(f"{'='*60}\n")

        return self.failed == 0

    # ========== 1. 服务健康检查 ==========
    def test_health_check(self):
        self.log("\n【场景1】服务健康检查")

        services = [
            ("Gateway", f"{self.gateway_url}/biz/product/list"),
            ("Biz-Service", f"{self.biz_url}/biz/product/list"),
            ("AI-Service", f"{self.ai_url}/ai/sentiment/analyze"),
        ]

        for name, url in services:
            try:
                r = requests.get(url, timeout=5)
                self.assert_test(f"{name} 可访问", r.status_code in [200, 401, 403])
            except Exception as e:
                self.assert_test(f"{name} 可访问", False, str(e)[:50])

    # ========== 2. 用户登录链路 ==========
    def test_user_login(self):
        self.log("\n【场景2】用户登录链路")

        try:
            # 登录
            r = requests.post(
                f"{self.biz_url}/biz/user/login",
                json={"username": "admin", "password": "123456"},
                timeout=10
            )
            data = r.json()
            self.assert_test("登录接口返回200", r.status_code == 200)
            self.assert_test("登录成功code=200", data.get("code") == 200)

            if data.get("code") == 200 and data.get("data"):
                self.token = data["data"].get("token")
                self.assert_test("获取到token", self.token is not None)
            else:
                # 尝试其他账号
                r2 = requests.post(
                    f"{self.biz_url}/biz/user/login",
                    json={"username": "test", "password": "test"},
                    timeout=10
                )
                data2 = r2.json()
                if data2.get("code") == 200 and data2.get("data"):
                    self.token = data2["data"].get("token")
        except Exception as e:
            self.assert_test("用户登录", False, str(e)[:50])

    # ========== 3. 商品查询链路 ==========
    def test_product_flow(self):
        self.log("\n【场景3】商品查询链路")

        try:
            # 商品列表
            r = requests.get(f"{self.biz_url}/biz/product/list", timeout=10)
            data = r.json()
            self.assert_test("商品列表接口", r.status_code == 200 and data.get("code") == 200)

            products = data.get("data", [])
            self.assert_test("商品列表非空", len(products) > 0)

            if products:
                product_id = products[0].get("id")
                # 商品详情
                r2 = requests.get(f"{self.biz_url}/biz/product/detail/{product_id}", timeout=10)
                data2 = r2.json()
                self.assert_test("商品详情接口", r2.status_code == 200 and data2.get("code") == 200)

                if data2.get("data"):
                    self.assert_test("商品详情包含名称", "name" in data2["data"])
                    self.assert_test("商品详情包含价格", "price" in data2["data"])
                    self.assert_test("商品详情包含卖家", "sellerName" in data2["data"] or "seller" in data2["data"])
        except Exception as e:
            self.assert_test("商品查询链路", False, str(e)[:50])

    # ========== 4. 订单创建链路 ==========
    def test_order_flow(self):
        self.log("\n【场景4】订单创建链路")

        try:
            # 查询商品
            r = requests.get(f"{self.biz_url}/biz/product/list", timeout=10)
            products = r.json().get("data", [])

            if not products:
                self.assert_test("订单链路-商品存在", False, "无商品数据")
                return

            product_id = products[0].get("id")

            # 创建订单
            headers = {"Authorization": f"Bearer {self.token}"} if self.token else {}
            r2 = requests.post(
                f"{self.biz_url}/biz/order/create",
                json={
                    "userId": 1,
                    "productId": product_id,
                    "quantity": 1,
                    "address": "测试地址",
                    "phone": "13800138000"
                },
                headers=headers,
                timeout=10
            )
            self.assert_test("创建订单接口", r2.status_code == 200)

            if r2.status_code == 200:
                data = r2.json()
                self.assert_test("订单创建成功", data.get("code") == 200)
        except Exception as e:
            self.assert_test("订单创建链路", False, str(e)[:50])

    # ========== 5. AI智能客服链路 ==========
    def test_ai_chat(self):
        self.log("\n【场景5】AI智能客服链路")

        try:
            r = requests.post(
                f"{self.ai_url}/ai/chat",
                json={"message": "你好，请问有什么商品推荐？", "userId": "test_user"},
                timeout=15
            )
            self.assert_test("AI对话接口", r.status_code == 200)
            if r.status_code == 200:
                data = r.json()
                self.assert_test("AI返回回答", data.get("data") is not None or data.get("message") is not None)
        except Exception as e:
            self.assert_test("AI智能客服", False, str(e)[:50])

    # ========== 6. 情感分析链路 ==========
    def test_sentiment(self):
        self.log("\n【场景6】情感分析链路")

        test_cases = [
            ("非常满意！", 1, "正面"),
            ("太差了！", 3, "负面"),
            ("一般吧", 2, "中性"),
        ]

        for text, expected, label in test_cases:
            try:
                r = requests.post(
                    f"{self.ai_url}/ai/sentiment/analyze",
                    json={"text": text},
                    timeout=10
                )
                if r.status_code == 200:
                    data = r.json()
                    result = data.get("data")
                    self.assert_test(f"情感分析-{label}", result == expected, f"期望{expected}实际{result}")
                else:
                    self.assert_test(f"情感分析-{label}", False, f"HTTP {r.status_code}")
            except Exception as e:
                self.assert_test(f"情感分析-{label}", False, str(e)[:50])

    # ========== 7. 知识库管理链路 ==========
    def test_knowledge_base(self):
        self.log("\n【场景7】知识库管理链路")

        try:
            # 查询知识库列表
            r = requests.get(f"{self.ai_url}/ai/knowledge/list?creatorId=1", timeout=10)
            self.assert_test("知识库列表接口", r.status_code == 200)

            # 创建知识库
            r2 = requests.post(
                f"{self.ai_url}/ai/knowledge/create",
                params={
                    "name": "验收测试知识库",
                    "description": "全链路验收使用",
                    "type": 1,
                    "creatorId": 1
                },
                timeout=10
            )
            self.assert_test("创建知识库接口", r2.status_code == 200)

            if r2.status_code == 200:
                data = r2.json()
                kb_id = data.get("data", {}).get("id")
                self.assert_test("知识库创建成功", kb_id is not None)

                if kb_id:
                    # 删除测试知识库
                    r3 = requests.delete(f"{self.ai_url}/ai/knowledge/{kb_id}", timeout=10)
                    self.assert_test("删除知识库接口", r3.status_code == 200)
        except Exception as e:
            self.assert_test("知识库管理链路", False, str(e)[:50])


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="智能电商运营平台全链路验收脚本")
    parser.add_argument("--host", default="localhost", help="服务主机地址")
    parser.add_argument("--port", type=int, default=8080, help="网关端口")
    args = parser.parse_args()

    tester = AcceptanceTest(args.host, args.port)
    success = tester.run_all()
    sys.exit(0 if success else 1)
