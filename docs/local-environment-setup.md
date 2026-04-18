# 開発環境構築

## 前提条件

- Windows 11
- Docker Desktop（WSL2 バックエンドモード）
- Visual Studio Code

## 手順

### 1. WSL インスタンス作成

コマンドプロンプトを実行し、以下を入力：

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
sudo chown -R $USER:$USER /home/projects
sudo chmod -R 755 /home/projects

# 基本パッケージインストール
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl git wget unzip jq vim tree gnupg2 software-properties-common

# Git基本設定（⚠️ 以下2行は自分の名前・メールアドレスに書き換えてから実行してください）
# git config --global user.name "Your Name"
# git config --global user.email "you@example.com"

git config --global core.autocrlf input
git config --global core.fileMode true
git config --global core.symlinks true
git config --global alias.logs "log --pretty='format:%C(yellow)%h %C(green)%cd %C(cyan)%an %C(reset)%s %C(magenta)%d' --date=format:'%Y-%m-%d %H:%M:%S' --graph"
```

### 5. GitHub CLI のインストールと認証

[GitHub CLI クイックスタート](https://docs.github.com/ja/github-cli/github-cli/quickstart) に従ってインストールと認証を行います。

認証完了後、Git の認証を GitHub CLI に委任します：

```bash
gh auth setup-git
```

### 6. リポジトリのクローン

```bash
cd /home/projects
gh repo clone yokozi-jp/spring-modulith-ai-harness
cd spring-modulith-ai-harness
```

### 7. セットアップスクリプトの実行

```bash
cd scripts/local-environment-setup
./01-setup-java.sh
./02-setup-viteplus.sh
source ~/.bashrc
./03-setup-pnpm.sh
./04-setup-kiro.sh
./05-setup-shell.sh
```

> ⚠️ `02-setup-viteplus.sh` 実行時にプロンプトが表示されたら `Y` を入力して、VITE+ に Node.js バージョン管理を任せてください。

### 8. VSCode 拡張機能のインストール

`.vscode/extensions.json` に記載されている推奨拡張機能をインストールしてください。

### 9. 動作確認

```bash
docker info
java -version
node -v
gh auth status
```

エラーなく各ツールの情報が表示されれば環境構築完了です。
