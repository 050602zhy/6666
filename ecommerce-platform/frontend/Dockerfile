# frontend Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

# 复制依赖文件并安装
COPY package*.json ./
RUN npm install

# 复制源码并构建
COPY . .
RUN npm run build

# 生产环境使用nginx
FROM nginx:alpine

LABEL maintainer="ecommerce-platform"
LABEL description="智能电商运营平台 - 前端"

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
