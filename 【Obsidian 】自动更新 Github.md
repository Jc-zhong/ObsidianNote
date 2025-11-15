## 1、下载 Obsidian 软件，并安装 Git 插件

## 1.1📥 Git 插件安装步骤

### 步骤 一：启用社区插件

1. 打开 Obsidian → 点击左下角 **设置（Settings）**
2. 在左侧菜单中找到 **社区插件（Community Plugins）**
3. 点击 **管理（Manage）** 或 **浏览社区插件（Browse）**
4. **关闭安全模式（Restricted Mode）** - 这是安装第三方插件的前提
### 步骤 二：搜索并安装 Git

1. 在社区插件页面顶部的搜索框中输入 Git
2. 在搜索结果中找到该插件
3. 点击 **安装（Install）** 按钮
4. 等待安装完成
### 步骤 三：启用插件

1. 安装完成后，回到 **社区插件** 主页面
2. 在 **已安装插件** 列表中找到 Git
3. 将旁边的开关 **打开（Enable）**

## 2、 创建远程仓库

### 步骤 一：创建 GitHub 远程仓库
**以 GitHub 为例：**

1. 登录 GitHub，点击右上角 **+** → **New repository**
2. 填写仓库信息：
    - **Repository name**: `my-obsidian-notes` (建议用英文)
    - **Description**: "My personal knowledge base" (可选)
    - **Visibility**: **Private** (重要！确保笔记隐私)
    - **不要勾选** "Initialize with README" (我们要推送已有内容)
3. 点击 **Create repository**
4. 创建成功后，复制仓库的 HTTPS/SSH URL（后面会用到）

### 步骤 二：初始化本地 Obsidian 仓库

1. **在 Obsidian 中创建或打开现有仓库**
    - 如果是新仓库：`Create new vault` → `Open folder as vault`
    - 如果已有仓库：直接打开
2. **在仓库根目录初始化 Git**
    // 打开终端，进入你的 Obsidian 仓库目录
    cd /path/to/your/obsidian-vault
    // 初始化 Git 仓库
    git init
    // 查看当前状态
    git status
3. **创建 .gitignore 文件**（重要！）  
    在仓库根目录创建 `.gitignore` 文件，内容如下：
    // Obsidian 缓存和系统文件
    .obsidian/
    .trash/
    *.tmp
    
    // 操作系统文件
    .DS_Store
    Thumbs.db
    
    // 临时文件
    *.log
    
4. **首次提交**
    // 添加所有文件到暂存区
    git add .
    
    // 提交初始版本
    git commit -m "feat: initial commit - Obsidian vault setup"
    

### 步骤 三：关联远程仓库并推送

// 添加远程仓库地址（替换成你自己的URL）
git remote add origin https://github.com/your-username/my-obsidian-notes.git

// 推送到远程主分支
git push -u origin main

// 如果遇到错误，可能是分支名不同，尝试：
git push -u origin master
// 或者
git branch -M main
git push -u origin main

**成功提示**：你会看到类似 `* [new branch] main -> main` 的信息。

## 3、⚡ 自动化同步配置

### 安装并配置 Obsidian Git 插件

1. **安装插件**（如之前所述）
    - 关闭安全模式 → 社区插件市场搜索 "Obsidian Git" → 安装并启用
        
2. **关键配置**（在插件设置中）：
    - **✅ Auto pull on startup**: 启动时自动拉取
    - **✅ Auto pull interval**: `10` (分钟)
    - **✅ Auto commit on file change**: 文件变化后自动提交
    - **✅ Auto push on file change**: 文件变化后自动推送
    - **Auto commit interval**: `30` (分钟)
    - **Auto push interval**: `30` (分钟)
    - **Commit message**: `Auto: {{date}}` (自动生成提交信息)
        
3. **测试自动化**：
    - 在 Obsidian 中新建一个笔记并保存
    - 等待几十秒，观察底部状态栏是否显示 Git 操作
    - 去 GitHub 网页查看提交记录

## 4、👥 多人协作配置

### 方案 A：直接协作（小团队）

1. **添加协作者**：
    - 在 GitHub 仓库页面 → **Settings** → **Collaborators**
    - 点击 **Add people**，输入队友的用户名
    - 队友会收到邀请邮件，接受后即可访问
        
2. **队友初始化**：
    // 队友在自己的电脑上执行
    git clone https://github.com/your-username/my-obsidian-notes.git
    cd my-obsidian-notes
    // 用 Obsidian 打开这个文件夹
## 5、常见问题

如过遇到配置完上面后仍无法自动同步，则需要补充进行如下操作
**步骤 一：生成 SSH 密钥**
ssh-keygen -t ed25519 -C "your_email@example.com"
ssh-keygen -t ed25519 -f "$HOME/.ssh/id_ed25519_personal" -C "your_email@example.com"
ssh-keygen -t ed25519 -f "$HOME/.ssh/id_ed25519_work" -C "your_email@example.com"
// 一直按回车使用默认路径

**步骤 二：将公钥添加到 GitHub**
// 复制公钥内容
cat ~/.ssh/id_ed25519.pub

1. 登录 GitHub → Settings → SSH and GPG keys
2. 点击 "New SSH key"
3. 粘贴公钥内容
    
**步骤 三：修改远程仓库地址为 SSH**
// 进入你的 Obsidian 仓库目录
cd /path/to/your/obsidian-vault

// 添加 or 修改远程仓库 URL
git remote add origin git@github.com:hzuzkt/ObsidianNote.git
git remote set-url origin git@github.com:hzuzkt/ObsidianNote.git

// 验证连接
ssh -T git@github.com
ssh -T -i ~/.ssh/id_ed25519_personal git@github.com

