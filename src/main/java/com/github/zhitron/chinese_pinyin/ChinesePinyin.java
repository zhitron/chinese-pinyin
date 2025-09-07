package com.github.zhitron.chinese_pinyin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 中文拼音处理工具类
 * 用于将中文字符转换为对应的拼音
 *
 * @author zhitron
 */
public final class ChinesePinyin {
    /**
     * 单例实例，支持基本中文字符范围 (0x4E00 - 0x9FA6)
     */
    public static final ChinesePinyin BASIC = new ChinesePinyin(0x4E00, 0x9FA6, "/chinese_pinyin_basic");
    /**
     * 无音调元音字符集合，用于音调标记替换
     * 包含 a, e, i, o, u, ü 六个基本元音字符
     */
    private static final String UNMARKED = "aeiouü";
    /**
     * 带音调元音字符集合，用于音调标记替换
     * 按照 a(ā,á,ă,à,a), e(ē,é,ĕ,è,e), i(ī,í,ĭ,ì,i), o(ō,ó,ŏ,ò,o), u(ū,ú,ŭ,ù,u), ü(ǖ,ǘ,ǚ,ǜ,ü) 的顺序排列
     * 每个元音字符对应5种音调形式：1声(阴平), 2声(阳平), 3声(上声), 4声(去声), 5声(轻声/无音调)
     */
    private static final String MARKED = "āáăàaēéĕèeīíĭìiōóŏòoūúŭùuǖǘǚǜü";
    /**
     * 编码掩码，用于处理拼音数据的位操作
     */
    private static final int CODE_MASK = 0x3FFFFFFF;
    /**
     * 单值标记，用于标识单个拼音数据的格式
     */
    private static final int SINGLE_VALUE_SIGN = 0x3E00001F;
    /**
     * 单值标记掩码
     */
    private static final int SINGLE_VALUE_SIGN_MARK = CODE_MASK ^ SINGLE_VALUE_SIGN;
    /**
     * 双值引导标记，用于标识双字节拼音数据的第一部分
     */
    private static final int DOUBLE_VALUE_LEAD = 0x0000001F;
    /**
     * 双值引导标记掩码
     */
    private static final int DOUBLE_VALUE_LEAD_MARK = CODE_MASK ^ DOUBLE_VALUE_LEAD;
    /**
     * 双值尾部标记，用于标识双字节拼音数据的第二部分
     */
    private static final int DOUBLE_VALUE_TAIL = 0x3E000000;
    /**
     * 双值尾部标记掩码
     */
    private static final int DOUBLE_VALUE_TAIL_MARK = CODE_MASK ^ DOUBLE_VALUE_TAIL;
    /**
     * 起始Unicode编码值
     */
    private final int startUnicode;
    /**
     * 结束Unicode编码值
     */
    private final int endUnicode;
    /**
     * 拼音索引数组，用于快速查找拼音数据位置
     */
    private final int[] chinesePinyinIndex;
    /**
     * 拼音数据数组，存储实际的拼音编码信息
     */
    private final int[] chinesePinyinData;

    /**
     * 构造函数
     *
     * @param startUnicode      起始Unicode编码值
     * @param endUnicode        结束Unicode编码值
     * @param chinesePinyinPath 拼音数据文件路径
     */
    ChinesePinyin(int startUnicode, int endUnicode, String chinesePinyinPath) {
        if (endUnicode <= startUnicode || startUnicode < 0) {
            throw new IllegalArgumentException("Invalid range: startUnicodeInclusive=" + startUnicode + ", endUnicodeExclusive=" + endUnicode);
        }
        this.startUnicode = startUnicode;
        this.endUnicode = endUnicode;
        this.chinesePinyinIndex = new int[endUnicode - startUnicode + 1];
        this.chinesePinyinData = load(chinesePinyinPath);
    }

    /**
     * 检查是否支持指定中文字符的拼音转换
     *
     * @param unicode        中文字符
     * @param throwException 当不支持时是否抛出异常
     * @return 如果支持返回 {@code true}，否则 {@code false}
     */
    public boolean isSupport(int unicode, boolean throwException) {
        boolean result = unicode >= startUnicode && unicode <= endUnicode;
        if (throwException && !result) {
            throw new IllegalArgumentException("The [" + unicode + "] Chinese Pin Yin does not exist");
        }
        return result;
    }

    /**
     * 获取指定中文字符的拼音
     *
     * @param unicode            中文字符的 Unicode 编码值
     * @param chinesePinyinStyle 拼音风格，决定是否带声调、使用何种字符表示等
     * @return 拼音字符串数组，如果该字符没有对应的拼音信息则返回包含原字符的数组
     */
    public String[] getFullSpellingByUnicode(int unicode, ChinesePinyinStyle chinesePinyinStyle) {
        // 获取字符的拼音信息
        char[][] fullSpellingData = findFullSpellingDataByUnicode(unicode, chinesePinyinStyle);
        if (fullSpellingData != null) {
            // 将字符数组转换为字符串数组
            String[] pinyinFullSpellingString = new String[fullSpellingData.length];
            for (int i = 0; i < fullSpellingData.length; i++) {
                pinyinFullSpellingString[i] = new String(fullSpellingData[i]);
            }
            return pinyinFullSpellingString;
        }
        return new String[]{new String(Character.toChars(unicode))};
    }

    /**
     * 获取指定中文字符的拼音
     *
     * @param unicode            中文字符的 Unicode 编码值
     * @param chinesePinyinStyle 拼音风格，决定是否带声调、使用何种字符表示等
     * @return 拼音字符串数组，如果该字符没有对应的拼音信息则返回 {@code null}
     */
    public char[][] findFullSpellingDataByUnicode(int unicode, ChinesePinyinStyle chinesePinyinStyle) {
        if (isSupport(unicode, false)) {
            char[][] array = new char[8][];
            int len = read(unicode, chinesePinyinStyle, array, null);
            if (len > 0) {
                // 先对数组进行排序，以便后续去重操作更高效
                Arrays.sort(array, 0, len, (a, b) -> {
                    if (a == null && b == null) return 0;
                    if (a == null) return -1;
                    if (b == null) return 1;
                    int length = Math.min(a.length, b.length);
                    for (int i = 0; i < length; i++) {
                        int compare = Character.compare(a[i], b[i]);
                        if (compare != 0) {
                            return compare;
                        }
                    }
                    return Integer.compare(a.length, b.length);
                });
                int writeIndex = 0;
                for (int readIndex = 0; readIndex < len; readIndex++) {
                    // 跳过重复元素
                    if (readIndex > 0 && Arrays.equals(array[readIndex], array[readIndex - 1])) {
                        continue;
                    }
                    // 保留非重复元素
                    array[writeIndex++] = array[readIndex];
                }
                return writeIndex == array.length ? array : Arrays.copyOf(array, writeIndex);
            }
        }
        return null;
    }

    /**
     * 获取指定中文字符的拼音
     *
     * @param unicode 中文字符的 Unicode 编码值
     * @return 拼音字符串数组，如果该字符没有对应的拼音信息则返回 {@code null}
     */
    public char[] findFirstLetterDataByUnicode(int unicode) {
        if (isSupport(unicode, false)) {
            char[] array = new char[8];
            int len = read(unicode, null, null, array);
            if (len > 0) {
                Arrays.sort(array, 0, len);
                // 使用双指针法快速去重（适用于已排序数组）
                int writeIndex = 0;
                for (int readIndex = 0; readIndex < len; readIndex++) {
                    if (readIndex == 0 || array[readIndex] != array[readIndex - 1]) {
                        array[writeIndex++] = array[readIndex];
                    }
                }
                // 截取去重后的有效部分
                return Arrays.copyOf(array, writeIndex);
            }
        }
        return null;
    }

    /**
     * 获取指定中文字符的拼音
     *
     * @param unicode            中文字符的 Unicode 编码值
     * @param chinesePinyinStyle 拼音风格，决定是否带声调、使用何种字符表示等
     */
    private int read(int unicode, ChinesePinyinStyle chinesePinyinStyle, char[][] fullSpelling, char[] firstLetter) {
        // 判断字符是否在支持的 Unicode 范围内
        int len = 0;
        if (unicode >= startUnicode && unicode <= endUnicode) {
            int index = unicode - startUnicode;
            // 获取拼音数据在索引表中的起始和结束位置
            int startIndex = (chinesePinyinIndex[index]) & 0xFFFF;
            int endIndex = (chinesePinyinIndex[index] >> 16) & 0xFFFF;
            // 若存在拼音数据，则进行解析
            if (endIndex > startIndex) {
                // 默认拼音风格设置
                if (chinesePinyinStyle == null) {
                    chinesePinyinStyle = ChinesePinyinStyle.WITHOUT_TONE_V_ASCII;
                }
                char[] chars = new char[7]; // 用于存储拼音字母
                // 遍历当前字符的所有拼音数据项
                for (int i = startIndex, code, tone, n, v; i < endIndex; i++) {
                    // 处理单字节编码格式（高位标记）
                    if ((chinesePinyinData[i] & SINGLE_VALUE_SIGN) == SINGLE_VALUE_SIGN) {
                        code = (chinesePinyinData[i] & SINGLE_VALUE_SIGN_MARK) >> 5;
                        tone = 4;
                    }
                    // 处理双字节编码格式
                    else if ((chinesePinyinData[i] & DOUBLE_VALUE_LEAD) == DOUBLE_VALUE_LEAD &&
                            (chinesePinyinData[i + 1] & DOUBLE_VALUE_TAIL) == DOUBLE_VALUE_TAIL) {
                        code = (chinesePinyinData[i] & DOUBLE_VALUE_LEAD_MARK) >> 5;
                        if ((chinesePinyinData[i + 1] & DOUBLE_VALUE_TAIL_MARK) != 0) {
                            code = ((chinesePinyinData[i + 1] & DOUBLE_VALUE_TAIL_MARK) << 25 & DOUBLE_VALUE_TAIL) ^ code;
                        }
                        tone = 4;
                    }
                    // 处理普通编码格式
                    else {
                        code = chinesePinyinData[i];
                        tone = chinesePinyinData[i] >> 30 & 0x3;
                    }
                    // 提取有效编码部分
                    code &= CODE_MASK;
                    // 将编码转换为字母序列
                    for (n = 0; n < 6 && (v = (code >> (n * 5)) & 0x1F) > 0 && v <= 26; n++) {
                        chars[n] = (char) ('a' + v - 1);
                    }
                    if (n > 0) {
                        if (firstLetter != null) {
                            firstLetter[len] = chars[0];
                        }
                        // 如果有有效字母，根据拼音风格构建拼音字符串
                        if (fullSpelling != null) {
                            // 根据不同风格处理拼音输出
                            switch (chinesePinyinStyle) {
                                case WITH_TONE_MARK: {
                                    // 先处理"v"替换为"ü"
                                    // 查找元音位置并添加声调标记
                                    int vi = -1, vc = -1;
                                    boolean skip = false;
                                    for (int ii = 0; ii < n; ii++) {
                                        if (chars[ii] == 'v') {
                                            chars[ii] = 'ü';
                                        }
                                        if (vi < 0 || !skip) {
                                            if (chars[ii] == 'a') {
                                                vc = 'a';
                                                vi = ii;
                                                skip = true;
                                            } else if (chars[ii] == 'e') {
                                                vc = 'e';
                                                vi = ii;
                                                skip = true;
                                            } else if (chars[ii] == 'o' && ii + 1 < n && chars[ii + 1] == 'u') {
                                                vc = 'o';
                                                vi = ii;
                                                skip = true;
                                            }
                                        }
                                        if (!skip && UNMARKED.indexOf(chars[ii]) >= 0) {
                                            vc = chars[ii];
                                            vi = ii;
                                        }
                                    }
                                    if (vc != -1) {
                                        chars[vi] = MARKED.charAt(UNMARKED.indexOf(vc) * 5 + tone);
                                    }
                                    break;
                                }
                                case WITHOUT_TONE_U_UNICODE: {
                                    // 不带声调，但使用 Unicode 的 ü 字符
                                    for (int ii = 0; ii < n; ii++) {
                                        if (chars[ii] == 'v') {
                                            chars[ii] = 'ü';
                                        }
                                    }
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                            // 添加构建好的拼音字符串到结果列表中
                            fullSpelling[len] = Arrays.copyOf(chars, n);
                        }
                        len++;
                    }
                }
            }
        }
        return len;
    }

    /**
     * 从资源文件加载拼音数据
     *
     * @param path 拼音数据文件路径
     * @return 解析后的拼音数据数组
     */
    private int[] load(String path) {
        InputStream inputStream = ChinesePinyin.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new RuntimeException("Can not find resource: " + path);
        }
        // 拼音存储格式说明:
        // 字母 a-z 映射为 1-26 (5位二进制表示)
        // 声调 1-4 用 2 位二进制表示: 1->00, 2->01, 3->10, 4->11
        // 数据存储采用压缩编码方式，根据数据长度选择不同的存储格式
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            IntStream.Builder phoneticList = IntStream.builder();
            int row = 0, currentIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                char[] chars = line.toCharArray();
                int startInclusive = 0, endExclusive = chars.length;
                // 去除行首和行尾的空白字符
                while (startInclusive < endExclusive && Character.isWhitespace(chars[startInclusive])) startInclusive++;
                while (startInclusive < endExclusive && Character.isWhitespace(chars[endExclusive - 1])) endExclusive--;
                if (startInclusive < endExclusive) {
                    int startIndex = currentIndex;
                    for (int i = startInclusive; i <= endExclusive; i++) {
                        if (i == endExclusive || chars[i] == ',') {
                            // 提取声调和编码拼音字母
                            int tone = 0, code = 0;
                            if (i > startInclusive && Character.isDigit(chars[i - 1])) {
                                // 将声调转换为0-3的数值(对应1-4声)
                                tone = chars[i - 1] - '0' - 1;
                            }
                            if (tone < 0 || tone > 4) {
                                throw new RuntimeException("Invalid tone: " + line);
                            }
                            // 编码最多6个字母
                            int num = 0;
                            for (; num < i - startInclusive - 1 && num < 6; num++) {
                                char c = chars[startInclusive + num];
                                if (c >= 'a' && c <= 'z') {
                                    // 每个字母用5位二进制表示(a=1, b=2, ..., z=26)
                                    code |= ((c - 'a' + 1) & 0x1F) << (num * 5);
                                } else {
                                    throw new RuntimeException("Invalid character: " + line);
                                }
                            }
                            code = code & CODE_MASK;
                            int value;
                            if (tone == 4) {
                                // 第5声(轻声)使用特殊标记存储
                                if (num <= 4) {
                                    // 单值存储格式
                                    value = code << 5 & SINGLE_VALUE_SIGN_MARK;
                                    value = value | SINGLE_VALUE_SIGN;
                                } else {
                                    // 双值存储格式(分高位和低位存储)
                                    value = code << 5 & DOUBLE_VALUE_LEAD_MARK;
                                    value = value | DOUBLE_VALUE_LEAD;
                                    phoneticList.add(value);
                                    currentIndex++;
                                    if (num == 5) {
                                        value = DOUBLE_VALUE_TAIL;
                                    } else {
                                        // 处理6个字母的情况
                                        value = code >> 25 & DOUBLE_VALUE_LEAD | DOUBLE_VALUE_TAIL;
                                    }
                                }
                            } else {
                                // 第1-4声使用高位2位存储声调信息
                                value = code | (tone & 0x3) << 30;
                            }
                            phoneticList.add(value);
                            currentIndex++;
                            startInclusive = i + 1;
                        }
                    }
                    // 存储索引信息：低16位为起始索引，高16位为结束索引
                    chinesePinyinIndex[row] = (currentIndex << 16) | startIndex;
                } else {
                    // 处理空行情况，起始和结束索引相同
                    chinesePinyinIndex[row] = (currentIndex << 16) | currentIndex;
                }
                row++;
            }
            return phoneticList.build().toArray();
        } catch (Exception e) {
            throw new RuntimeException("拼音数据加载失败", e);
        }
    }
}
