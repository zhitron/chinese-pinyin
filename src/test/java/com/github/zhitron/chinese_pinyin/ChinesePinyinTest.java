package com.github.zhitron.chinese_pinyin;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 中文拼音工具类测试
 *
 * @author zhitron
 */
public class ChinesePinyinTest {
    /**
     * 测试 isSupport 方法
     * 验证中文字符是否在支持的Unicode范围内
     */
    @Test
    public void test_isSupport() {
        // 测试支持的Unicode范围内的字符
        assertTrue(ChinesePinyin.BASIC.isSupport(0x4E00, false)); // '一'
        assertTrue(ChinesePinyin.BASIC.isSupport(0x9FA6, false)); // 最后一个支持的字符
        // 测试不支持的Unicode范围外的字符
        assertFalse(ChinesePinyin.BASIC.isSupport(0x4DFF, false)); // 范围前一个字符
        assertFalse(ChinesePinyin.BASIC.isSupport(0x9FA7, false)); // 范围后一个字符
        // 测试不支持的Unicode范围外的字符并抛出异常
        try {
            assertFalse(ChinesePinyin.BASIC.isSupport(0x4DFF, true)); // 范围后一个字符
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    /**
     * 测试 getFullSpellingByUnicode 方法
     * 验证获取中文字符完整拼音的功能
     */
    @Test
    public void test_getFullSpellingByUnicode() {
        // 测试支持的中文字符获取完整拼音
        String[] result = ChinesePinyin.BASIC.getFullSpellingByUnicode(0x4E00, ChinesePinyinStyle.WITHOUT_TONE_V_ASCII); // '一'
        assertNotNull(result);
        assertTrue(result.length > 0);
        // 测试不支持的字符获取完整拼音，应该返回包含原字符的数组
        String[] result2 = ChinesePinyin.BASIC.getFullSpellingByUnicode('A', ChinesePinyinStyle.WITHOUT_TONE_V_ASCII);
        assertNotNull(result2);
        assertEquals(1, result2.length);
        assertEquals("A", result2[0]);
        // 测试不同拼音风格的输出
        int unicode = 0x4F60; // '你'
        String[] withToneMark = ChinesePinyin.BASIC.getFullSpellingByUnicode(unicode, ChinesePinyinStyle.WITH_TONE_MARK);
        String[] withoutToneV = ChinesePinyin.BASIC.getFullSpellingByUnicode(unicode, ChinesePinyinStyle.WITHOUT_TONE_V_ASCII);
        String[] withoutToneU = ChinesePinyin.BASIC.getFullSpellingByUnicode(unicode, ChinesePinyinStyle.WITHOUT_TONE_U_UNICODE);
        assertNotNull(withToneMark);
        assertNotNull(withoutToneV);
        assertNotNull(withoutToneU);
    }

    /**
     * 测试 findFullSpellingDataByUnicode 方法
     * 验证查找中文字符完整拼音数据的功能
     */
    @Test
    public void test_findFullSpellingDataByUnicode() {
        // 测试支持的中文字符查找完整拼音数据
        char[][] result = ChinesePinyin.BASIC.findFullSpellingDataByUnicode(0x4E00, ChinesePinyinStyle.WITHOUT_TONE_V_ASCII); // '一'
        assertNotNull(result);
        // 测试不支持的字符查找完整拼音数据，应该返回null
        char[][] result2 = ChinesePinyin.BASIC.findFullSpellingDataByUnicode('A', ChinesePinyinStyle.WITHOUT_TONE_V_ASCII);
        assertNull(result2);
    }

    /**
     * 测试 findFirstLetterDataByUnicode 方法
     * 验证查找中文字符拼音首字母的功能
     */
    @Test
    public void test_findFirstLetterDataByUnicode() {
        // 测试支持的中文字符查找拼音首字母
        char[] result = ChinesePinyin.BASIC.findFirstLetterDataByUnicode(0x4E00); // '一'
        assertNotNull(result);
        // 测试不支持的字符查找拼音首字母，应该返回null
        char[] result2 = ChinesePinyin.BASIC.findFirstLetterDataByUnicode('A');
        assertNull(result2);
        // 测试中文字符获取拼音首字母并去重
        char[] result3 = ChinesePinyin.BASIC.findFirstLetterDataByUnicode(0x559C); // '喜' 字，可能有多个读音
        if (result3 != null) {
            // 验证去重功能
            for (int i = 0; i < result3.length - 1; i++) {
                assertTrue(result3[i] <= result3[i + 1]); // 应该已排序
                assertNotEquals(result3[i], result3[i + 1]); // 应该无重复
            }
        }
    }

    /**
     * 测试 ChinesePinyin 构造函数
     * 验证构造函数对无效参数的处理
     */
    @Test
    public void test_constructor() {
        // 测试构造函数传入无效范围
        try {
            new ChinesePinyin(0x9FA6, 0x4E00, "/chinese_pinyin_basic");
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid range: startUnicodeInclusive=40870, endUnicodeExclusive=19968", e.getMessage());
        }
        // 测试构造函数传入负数起始值
        try {
            new ChinesePinyin(-1, 100, "/chinese_pinyin_basic");
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid range: startUnicodeInclusive=-1, endUnicodeExclusive=100", e.getMessage());
        }
    }
}
