package com.github.zhitron.chinese_pinyin;

/**
 * 汉语拼音输出风格枚举类
 * <p>
 * 该枚举定义了汉语拼音的不同输出格式，主要用于指定拼音的音调和特殊字符的表示方式。
 *
 * @author zhitron
 */
public enum ChinesePinyinStyle {
    /**
     * 该选项表示汉语拼音输出带有音调标记
     */
    WITH_TONE_MARK,
    /**
     * 不带音调，使用ASCII字符"v"替代"ü"
     */
    WITHOUT_TONE_V_ASCII,
    /**
     * 不带音调，使用Unicode字符"ü"
     */
    WITHOUT_TONE_U_UNICODE,
}
