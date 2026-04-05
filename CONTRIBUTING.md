# Contributing

## 前提条件

- Windows 11
- Docker Desktop（WSL2 バックエンドモード）

## 環境構築手順

### 1. WSL インスタンス作成

コマンドプロンプトを管理者として実行し、以下を入力：

```cmd
wsl --install Ubuntu-24.04 --name dev
```

インストール完了後、ユーザー名とパスワードを設定してください。

### 2. Docker Desktop の WSL Integration 設定

1. Docker Desktop を起動
2. **Settings > Resources > WSL Integration** を開く
3. `dev` を有効化
4. **Apply & Restart** をクリック

### 3. WSL インスタンスへの接続

```cmd
wsl -d dev
```

### 4. 初期セットアップ

```bash
# 共通ワークスペース作成
sudo mkdir -p /home/projects
sudo chown -R $USER:$USER /home
sudo chmod -R 755 /home

# 基本パッケージインストール
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl git wget unzip jq vim tree gnupg2 software-properties-common

# Git基本設定（⚠️ 以下2行は自分の名前・メールアドレスに書き換えてから実行してください）
git config --global user.name "Your Name"
git config --global user.email "you@example.com"

git config --global core.autocrlf input
git config --global core.fileMode true
git config --global core.symlinks true
git config --global alias.logs "log --pretty='format:%C(yellow)%h %C(green)%cd %C(cyan)%an %C(reset)%s %C(magenta)%d' --date=format:'%Y-%m-%d %H:%M:%S' --graph"
git config --global credential.helper store
```

### 5. GitHub CLI のインストールと認証

[GitHub CLI クイックスタート](https://docs.github.com/ja/github-cli/github-cli/quickstart) に従ってインストールと認証を行います。

### 6. リポジトリのクローン

```bash
cd /home/projects
gh repo clone yokozi-jp/spring-modulith-ai-harness
cd spring-modulith-ai-harness
```

### 7. セットアップスクリプトの実行

```bash
./scripts/setup/01-setup-tools.sh
./scripts/setup/02-setup-java.sh
./scripts/setup/03-setup-node.sh
./scripts/setup/04-setup-shell.sh
```

### 8. シェル再起動

```bash
source ~/.bashrc
```

### 9. Kiro CLI のインストール

```bash
curl -fsSL https://cli.kiro.dev/install | bash
```

### 10. 動作確認

```bash
docker info
```

エラーなく Docker の情報が表示されれば環境構築完了です。
