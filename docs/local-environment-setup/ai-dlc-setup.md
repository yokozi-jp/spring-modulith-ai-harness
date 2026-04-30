# AI-DLC セットアップ手順

## 前提条件

- GitHub CLI (`gh`) がインストールされていること

## 手順

### 1. aidlc-workflows リポジトリをクローン

```bash
cd /home/projects
gh repo clone awslabs/aidlc-workflows
```

### 2. ルールファイルをプロジェクトにコピー

```bash
cp -R /home/projects/aidlc-workflows/aidlc-rules/aws-aidlc-rules /home/projects/spring-modulith-ai-harness/.kiro/steering/
cp -R /home/projects/aidlc-workflows/aidlc-rules/aws-aidlc-rule-details /home/projects/spring-modulith-ai-harness/.kiro/
```
