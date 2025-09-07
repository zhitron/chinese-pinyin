# Chinese Pinyin

## 📄 项目简介

`Chinese Pinyin` 是一个用于将中文字符转换为拼音的Java库。它支持多种拼音格式，包括带声调和不带声调的拼音，并且可以处理多音字的情况。

---

## 🚀 快速开始

### 构建要求

- JDK 8 或以上（推荐使用 JDK 8）
- Maven 3.x

### 添加依赖

你可以通过 Maven 引入该项目：

```xml

<dependency>
    <groupId>io.github.zhitron</groupId>
    <artifactId>chinese-pinyin</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🧩 功能特性

- 支持中文转拼音（全拼、首字母）
- 多音字处理
- 支持多种输出格式（带声调、不带声调等）
- 高性能，低内存占用

---

## ✍️ 开发者

- **Zhitron**
- 邮箱: zhitron@foxmail.com
- 组织: [Zhitron](https://github.com/zhitron)

---

## 📦 发布状态

当前版本：`1.0.0`

该项目已发布至 [Maven Central](https://search.maven.org/)，支持快照版本与正式版本部署。

---

## 🛠 源码管理

GitHub 地址：https://github.com/zhitron/chinese-pinyin

使用 Git 进行版本控制：

```bash
git clone https://github.com/zhitron/chinese-pinyin.git
```

---

## 📚 文档与社区

- Javadoc 文档可通过 `mvn javadoc:javadoc` 生成。
- 如有问题或贡献，请提交 Issues 或 PR 至 GitHub 仓库。

---

## 🧪 测试案例

以下是一些基本的测试案例，展示了如何使用 `Chinese Pinyin` 库进行中文转拼音操作：

### 示例代码

```java
import com.github.zhitron.chinese_pinyin.ChinesePhonetic;
import com.github.zhitron.chinese_pinyin.ChinesePinyin;
import com.github.zhitron.chinese_pinyin.ChinesePinyinStyle;

import java.util.Arrays;

public class ChinesePinyinExample {
    public static void main(String[] args) {

        // 获取中文字符的拼音（不带声调）
        System.out.println("中 (不带声调): " + Arrays.toString(ChinesePinyin.BASIC.getFullSpellingByUnicode('中', ChinesePinyinStyle.WITHOUT_TONE_V_ASCII)));

        // 获取中文字符的拼音（不带声调）
        System.out.println("中 (不带声调): " + Arrays.toString(ChinesePinyin.BASIC.getFullSpellingByUnicode('中', ChinesePinyinStyle.WITHOUT_TONE_U_UNICODE)));

        // 获取中文字符的拼音（带声调）
        System.out.println("中 (带声调): " + Arrays.toString(ChinesePinyin.BASIC.getFullSpellingByUnicode('中', ChinesePinyinStyle.WITH_TONE_MARK)));

        // 获取中文字符的拼音（不带声调）
        System.out.println("国 (不带声调): " + Arrays.toString(ChinesePinyin.BASIC.getFullSpellingByUnicode('国', ChinesePinyinStyle.WITHOUT_TONE_V_ASCII)));

        // 获取中文字符的拼音（不带声调）
        System.out.println("国 (不带声调): " + Arrays.toString(ChinesePinyin.BASIC.getFullSpellingByUnicode('国', ChinesePinyinStyle.WITHOUT_TONE_U_UNICODE)));

        // 获取中文字符的拼音（带声调）
        System.out.println("国 (带声调): " + Arrays.toString(ChinesePinyin.BASIC.getFullSpellingByUnicode('国', ChinesePinyinStyle.WITH_TONE_MARK)));

        // 转换中文为拼音（不带声调）
        System.out.println("转换中文为拼音（不带声调）: " + Arrays.toString(ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, null)));

        // 转换中文为拼音（不带声调）
        System.out.println("转换中文为拼音（不带声调）: " + Arrays.toString(ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITHOUT_TONE_U_UNICODE, null)));

        // 转换中文为拼音（带声调）
        System.out.println("转换中文为拼音（带声调）: " + Arrays.toString(ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITH_TONE_MARK, null)));

    }
}
```

### 运行结果

```
中 (不带声调): [zhong]
中 (不带声调): [zhong]
中 (带声调): [zhòng, zhōng]
国 (不带声调): [guo]
国 (不带声调): [guo]
国 (带声调): [guó]
转换中文为拼音（不带声调）: [zhong guo]
转换中文为拼音（不带声调）: [zhong guo]
转换中文为拼音（带声调）: [zhòng guó, zhōng guó]
```

---

## 📎 License

Apache License, Version 2.0  
详见 [LICENSE](https://www.apache.org/licenses/LICENSE-2.0.txt)

---
