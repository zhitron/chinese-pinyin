package com.github.zhitron.chinese_pinyin;

import java.util.Arrays;

/**
 * 中文拼音工具类，用于处理中文字符序列的拼音转换
 * 提供获取拼音全拼和拼音首字母的功能
 *
 * @author zhitron
 */
public final class ChinesePhonetic {
    /**
     * 基础中文拼音实例，支持基本中文字符范围
     */
    public static final ChinesePhonetic BASIC = new ChinesePhonetic(ChinesePinyin.BASIC);
    /**
     * 关联的拼音处理核心类
     */
    private final ChinesePinyin chinesePinyin;

    /**
     * 构造函数
     *
     * @param chinesePinyin 拼音处理核心类实例
     */
    ChinesePhonetic(ChinesePinyin chinesePinyin) {
        this.chinesePinyin = chinesePinyin;
    }

    /**
     * 获取数组排列组合索引列表
     *
     * @param lengths 每个元素数组的索引数组
     * @return 返回数组排列组合索引列表
     */
    static int[][] combine(int... lengths) {
        if (lengths == null || lengths.length == 0) {
            throw new IllegalArgumentException("Invalid input: lengths cannot be null or empty.");
        }
        return combine(lengths, 0, lengths.length);
    }

    /**
     * 获取数组排列组合索引列表
     *
     * @param lengths 每个元素数组的索引数组
     * @return 返回数组排列组合索引列表
     */
    static int[][] combine(int[] lengths, int startInclusive, int endExclusive) {
        // 处理空输入或无效范围
        if (lengths == null || lengths.length == 0 || startInclusive < 0 || endExclusive > lengths.length || startInclusive >= endExclusive) {
            throw new IllegalArgumentException("Invalid input: lengths cannot be null or empty, and range must be valid.");
        }
        // 检查是否存在0或负数长度
        for (int i = startInclusive; i < endExclusive; i++) {
            if (lengths[i] <= 0) {
                throw new IllegalArgumentException("Invalid input: lengths cannot contain 0 or negative values.");
            }
        }
        // 预计算结果总数
        int total = 1;
        for (int i = startInclusive; i < endExclusive; i++) {
            // 防止整数溢出
            if (total > Integer.MAX_VALUE / lengths[i]) {
                throw new IllegalArgumentException("Result count exceeds maximum array size.");
            }
            total *= lengths[i];
        }
        int len = endExclusive - startInclusive;
        int[][] results = new int[total][len];
        // 使用数组而非List以提高性能
        int[] indexes = new int[len];
        // 初始化索引数组为0（默认就是0，但显式调用更清晰）
        Arrays.fill(indexes, 0);
        // 生成所有组合
        for (int i = 0; i < total; i++) {
            // 复制当前索引组合
            System.arraycopy(indexes, 0, results[i], 0, len);
            // 更新索引数组到下一个组合
            for (int j = len - 1; j >= 0; j--) {
                indexes[j]++;
                if (indexes[j] < lengths[startInclusive + j]) {
                    break;
                }
                // 只有在不是最高位时才重置为0
                if (j > 0) {
                    indexes[j] = 0;
                }
            }
        }
        return results;
    }

    /**
     * 获取关联的拼音处理核心类实例
     *
     * @return ChinesePinyin实例
     */
    public ChinesePinyin chinesePinyin() {
        return chinesePinyin;
    }

    /**
     * 根据输入的Unicode字符序列，获取每个汉字对应的拼音全拼，并生成所有可能的组合。
     * <p>
     * 该方法支持处理Unicode字符（包括代理对），并对每个汉字查找其所有可能的拼音全拼。
     * 如果某个字符不是汉字或没有对应拼音，则保留原字符。
     * 最终返回所有可能的拼音全拼组合字符串数组。
     *
     * @param unicodeSequence        输入的Unicode字符序列，可以包含汉字和其他字符
     * @param chinesePinyinStyle     拼音风格，决定是否带声调、使用何种字符表示等
     * @param caseFormatPreserveNull 控制输出字符的大小写格式：
     *                               - null：保持原样；
     *                               - true：转为大写；
     *                               - false：转为小写。
     * @return 所有可能的拼音全拼组合字符串数组；如果输入为null或空，返回空数组
     */
    public String[] listPinyinFullSpellingDataByUnicodeSequence(CharSequence unicodeSequence, ChinesePinyinStyle chinesePinyinStyle, Boolean caseFormatPreserveNull) {
        if (unicodeSequence == null) {
            return new String[0];
        }
        int sequenceLength = unicodeSequence.length();
        if (sequenceLength == 0) {
            return new String[0];
        }
        // 使用数组替代List以提高性能
        char[][][] pinyin = new char[sequenceLength][][];
        int[] lengths = new int[sequenceLength];
        int count = 0, total = 1;
        // 预处理：获取所有代码点并检查拼音
        for (int i = 0; i < sequenceLength; ) {
            int codePoint = Character.codePointAt(unicodeSequence, i);
            char[][] fullSpellingData = chinesePinyin.findFullSpellingDataByUnicode(codePoint, chinesePinyinStyle);
            if (fullSpellingData != null && fullSpellingData.length > 0) {
                pinyin[count] = fullSpellingData;
                lengths[count] = fullSpellingData.length;
                total *= fullSpellingData.length;
            } else {
                pinyin[count] = new char[][]{Character.toChars(codePoint)};
                lengths[count] = 1;
            }
            i += Character.charCount(codePoint);
            count++;
        }
        // 生成结果
        String[] result = new String[total];
        int[][] combinations = ChinesePhonetic.combine(lengths, 0, count);
        StringBuilder sb = new StringBuilder(); // 使用默认容量，避免频繁扩容
        for (int i = 0; i < total; i++) {
            sb.setLength(0);
            for (int j = 0; j < count; j++) {
                sb.append(pinyin[j][combinations[i][j]]).append(' ');
            }
            // 删除末尾空格
            int len = sb.length();
            if (len > 0) {
                sb.setLength(len - 1);
            }
            result[i] = sb.toString();
            // 大小写转换优化：只在需要时进行
            if (caseFormatPreserveNull != null) {
                if (caseFormatPreserveNull) {
                    result[i] = result[i].toUpperCase();
                } else {
                    result[i] = result[i].toLowerCase();
                }
            }
        }
        return result;
    }

    /**
     * 根据输入的Unicode字符序列，获取每个汉字对应的拼音首字母，并生成所有可能的组合。
     * <p>
     * 该方法支持处理Unicode字符（包括代理对），并对每个汉字查找其拼音首字母。
     * 如果某个字符不是汉字或没有对应拼音，则保留原字符。
     * 最终返回所有可能的拼音首字母组合字符串数组。
     *
     * @param unicodeSequence        输入的Unicode字符序列，可以包含汉字和其他字符
     * @param caseFormatPreserveNull 控制输出字符的大小写格式：
     *                               - null：保持原样；
     *                               - true：转为大写；
     *                               - false：转为小写。
     * @return 所有可能的拼音首字母组合字符串数组；如果输入为null或空，返回空数组
     */
    public String[] listPinyinFirstLetterByUnicodeSequence(CharSequence unicodeSequence, Boolean caseFormatPreserveNull) {
        if (unicodeSequence == null) {
            return new String[0];
        }
        int length = unicodeSequence.length();
        if (length == 0) {
            return new String[0];
        }
        // 预估实际字符数量（处理代理对）
        int estimatedCodePointCount = length;
        for (int i = 0; i < length; ) {
            int codePoint = Character.codePointAt(unicodeSequence, i);
            if (Character.charCount(codePoint) == 2) {
                estimatedCodePointCount--;
            }
            i += Character.charCount(codePoint);
        }
        // 使用数组替代List以提高性能
        char[][] pinyin = new char[length][];
        int[] codepoints = new int[length];
        int[] lengths = new int[length];
        int count = 0, total = 1, codePoint;
        // 预处理：获取所有代码点并检查拼音
        for (int i = 0; i < length; ) {
            codePoint = Character.codePointAt(unicodeSequence, i);
            codepoints[count] = codePoint;
            char[] pinyinFirstLetter = chinesePinyin.findFirstLetterDataByUnicode(codePoint);
            if (pinyinFirstLetter != null && pinyinFirstLetter.length > 0) {
                pinyin[count] = pinyinFirstLetter;
                lengths[count] = pinyinFirstLetter.length;
                // 防止整数溢出
                if (total > Integer.MAX_VALUE / pinyinFirstLetter.length) {
                    throw new RuntimeException("Too many combinations, result would overflow");
                }
                total *= pinyinFirstLetter.length;
            } else {
                lengths[count] = 1;
            }
            i += Character.charCount(codePoint);
            count++;
        }
        // 生成结果
        String[] result = new String[total];
        int[][] combinations = ChinesePhonetic.combine(lengths, 0, count);
        StringBuilder sb = new StringBuilder(count << 1); // 预设容量
        int codepoint;
        for (int i = 0; i < total; i++) {
            sb.setLength(0);
            for (int j = 0; j < count; j++) {
                if (pinyin[j] != null) {
                    codepoint = pinyin[j][combinations[i][j]];
                } else {
                    codepoint = codepoints[j];
                }
                // 大小写转换优化：只在需要时进行
                if (caseFormatPreserveNull != null) {
                    if (caseFormatPreserveNull) {
                        codepoint = Character.toUpperCase(codepoint);
                    } else {
                        codepoint = Character.toLowerCase(codepoint);
                    }
                }
                sb.appendCodePoint(codepoint);
            }
            result[i] = sb.toString();
        }
        return result;
    }
}
