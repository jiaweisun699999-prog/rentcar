# 悟空租车 (RentCar) 项目部署与运行指南

本项目为前后端分离架构的租车平台。前端使用 Vue 3 + Element Plus，后端使用 Spring Boot + MyBatis-Plus。

## 一、 环境依赖要求

为了保证项目能够顺利运行，请组员们在本地安装以下基础环境：
1. **Node.js**: v16.0 或以上版本（用于运行前端）
2. **Java (JDK)**: v1.8 或 v17（建议使用 JDK 17，根据具体项目配置）
3. **Maven**: v3.6 或以上（用于后端依赖管理）
4. **MySQL**: v8.0 或以上（用于数据存储）

---

## 二、 数据库初始化 (后端准备工作)

1. 在本地 MySQL 数据库中创建一个名为 `wukong_rental` 的数据库：
   ```sql
   CREATE DATABASE IF NOT EXISTS wukong_rental DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. *(注：如项目中有 SQL 脚本文件，请先执行 SQL 脚本初始化表结构)*
3. 打开后端项目 `rentcar-hou`，找到 `src/main/resources/application.yml`（或 `application.properties`）文件。
4. 修改数据库连接配置（用户名和密码）：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/rentcar?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
       username: root
       password: your_password # 修改为你的本地数据库密码
   ```

---

## 三、 后端项目启动 (rentcar-hou)

1. 使用 IntelliJ IDEA 打开 `rentcar-hou` 文件夹。
2. IDEA 会自动识别为 Maven 项目并下载依赖（如果没有自动下载，请点击 Maven 面板的刷新按钮，或在终端执行 `mvn clean install`）。
3. 找到项目启动类 `src/main/java/com/msb/rentcarhou/RentcarHouApplication.java`。
4. 右键点击 `Run 'RentcarHouApplication'` 启动后端服务。
5. 后端默认运行端口通常为 `8080`。

---

## 四、 前端项目启动 (rentcar-qian)

1. 打开终端（命令行工具），进入前端项目目录：
   ```bash
   cd rentcar-qian
   ```
2. 安装项目依赖：
   ```bash
   npm install
   ```
3. 启动本地开发服务器：
   ```bash
   npm run dev
   # 或者是 npm run serve (取决于 package.json 中的 scripts 配置)
   ```
4. 终端会输出本地访问地址，如：`http://localhost:5173` 或是 `http://localhost:8080`，在浏览器中打开该地址即可访问前端页面。

---

## 五、 团队协作规范 (Git 提交)

1. 拉取最新代码：`git pull origin master`
2. 提交代码：
   ```bash
   git add .
   git commit -m "feat: 添加了门店列表查询功能"
   git push origin master
   ```
3. **Commit 规范推荐**：
   - `feat`: 新增功能
   - `fix`: 修复 bug
   - `docs`: 文档更新
   - `style`: 代码格式调整（不影响逻辑）
   - `refactor`: 代码重构
