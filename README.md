# `Java` 项目工程化开发指南

<!-- ![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/pyloong/pythonic-project-guidelines/gh-deploy/main?label=gh-page&logo=github&style=flat-square) -->

## 使用方式

1. 克隆项目

    ```bash
    git clone https://github.com/lwpk110/java-project-guidelines.git
    ```

2. 初始化环境

    项目预览需要安装 Python 环境来启动 server，强烈建议使用 Python 3.6+ 的版本。如果本地没有 Python 环境，也可以使用 [Docker预览服务器](https://squidfunk.github.io/mkdocs-material/creating-your-site/#creating-your-site) 来启动。

    1. 本地初始化
        安装依赖

        ```bash
        pip install -r requirements.txt
        ```

    2. 使用 Docker 初始化

        ```bash
        docker pull squidfunk/mkdocs-material
        ```

3. 预览
   1. 本地预览

        ```bash
        mkdocs serve
        ```

   2. 使用 Docker 预览

        **uinx**:

        ```bash
        docker run --rm -it -p 8000:8000 -v ${PWD}:/docs squidfunk/mkdocs-material
        ```

        **Windows**:

        ```bash
        docker run --rm -it -p 8000:8000 -v "%cd%":/docs squidfunk/mkdocs-material
        ```
