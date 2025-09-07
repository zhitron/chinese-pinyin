package com.github.zhitron.chinese_pinyin;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 中文拼音工具类测试类
 * 用于测试ChinesePhonetic类的各种功能，包括组合算法、拼音转换等
 *
 * @author zhitron
 */
public class ChinesePhoneticTest {
    /**
     * 测试组合算法的正确性
     * 验证不同输入情况下combine方法的输出是否符合预期
     */
    @Test
    public void test_combine() {
        // 测试正常情况：2个长度为3和2的数组，应产生6种组合(3*2=6)
        int[][] result = ChinesePhonetic.combine(3, 2);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生6种组合", 6, result.length);
        assertArrayEquals("第一个组合应为[0,0]", new int[]{0, 0}, result[0]);
        assertArrayEquals("第二个组合应为[0,1]", new int[]{0, 1}, result[1]);
        assertArrayEquals("第三个组合应为[1,0]", new int[]{1, 0}, result[2]);
        assertArrayEquals("第四个组合应为[1,1]", new int[]{1, 1}, result[3]);
        assertArrayEquals("第五个组合应为[2,0]", new int[]{2, 0}, result[4]);
        assertArrayEquals("第六个组合应为[2,1]", new int[]{2, 1}, result[5]);

        // 测试每个数组只有一个元素的情况，应只产生1种组合
        result = ChinesePhonetic.combine(1, 1, 1);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生1种组合", 1, result.length);
        assertArrayEquals("唯一组合应为[0,0,0]", new int[]{0, 0, 0}, result[0]);

        // 测试其中一个数组长度为0的情况，应抛出IllegalArgumentException异常
        try {
            ChinesePhonetic.combine(3, 0, 2);
            fail("应抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            assertEquals("异常信息应匹配", "Invalid input: lengths cannot contain 0 or negative values.", e.getMessage());
        }

        // 测试输入为null的情况，应抛出IllegalArgumentException异常
        try {
            ChinesePhonetic.combine((int[]) null);
            fail("应抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            assertEquals("异常信息应匹配", "Invalid input: lengths cannot be null or empty.", e.getMessage());
        }

        // 测试输入为空数组的情况，应抛出IllegalArgumentException异常
        try {
            ChinesePhonetic.combine();
            fail("应抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            assertEquals("异常信息应匹配", "Invalid input: lengths cannot be null or empty.", e.getMessage());
        }

        // 测试较大输入的情况：2*3*2=12种组合
        result = ChinesePhonetic.combine(2, 3, 2);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生12种组合", 12, result.length);
        // 检查第一项和最后一项
        assertArrayEquals("第一个组合应为[0,0,0]", new int[]{0, 0, 0}, result[0]);
        assertArrayEquals("最后一个组合应为[1,2,1]", new int[]{1, 2, 1}, result[11]);

        // 测试正常情况下的组合：2*3=6种组合
        result = ChinesePhonetic.combine(2, 3);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生6种组合", 6, result.length);
        assertEquals("每个组合应包含2个元素", 2, result[0].length);
        // 测试单个数组的组合，应产生3种组合
        result = ChinesePhonetic.combine(3);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生3种组合", 3, result.length);
        assertEquals("每个组合应包含1个元素", 1, result[0].length);
        // 测试三个数组的组合：2*2*2=8种组合
        result = ChinesePhonetic.combine(2, 2, 2);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生8种组合", 8, result.length);
        assertEquals("每个组合应包含3个元素", 3, result[0].length);

        // 测试带范围的组合：2*3*4=24种组合
        int[] lengths = {2, 3, 4};
        result = ChinesePhonetic.combine(lengths, 0, 3);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生24种组合", 24, result.length);
        assertEquals("每个组合应包含3个元素", 3, result[0].length);
        // 测试部分范围的组合：3*4=12种组合
        result = ChinesePhonetic.combine(lengths, 1, 3);
        assertNotNull("组合结果不应为null", result);
        assertEquals("应产生12种组合", 12, result.length);
        assertEquals("每个组合应包含2个元素", 2, result[0].length);
    }

    /**
     * 测试获取ChinesePinyin实例的方法
     * 验证是否能正确获取到ChinesePinyin实例
     */
    @Test
    public void test_chinesePinyin() {
        // 测试获取chinesePinyin实例
        assertNotNull("应能获取到ChinesePinyin实例", ChinesePhonetic.BASIC.chinesePinyin());
        assertEquals("获取的实例应与ChinesePinyin.BASIC相同", ChinesePinyin.BASIC, ChinesePhonetic.BASIC.chinesePinyin());
    }

    /**
     * 测试根据Unicode字符序列获取拼音全拼的方法
     * 验证不同输入和配置下方法的正确性
     */
    @Test
    public void test_listPinyinFullSpellingDataByUnicodeSequence() {
        // 测试正常情况下的全拼转换（不带声调，使用ASCII的v字符）
        String[] result1 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, null);
        assertNotNull("结果不应为null", result1);
        assertTrue("应至少产生一种组合", result1.length > 0);
        // 测试带声调的全拼转换
        String[] result2 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITH_TONE_MARK, null);
        assertNotNull("结果不应为null", result2);
        assertTrue("应至少产生一种组合", result2.length > 0);
        // 测试大小写转换 - 大写
        String[] result3 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, true);
        assertNotNull("结果不应为null", result3);
        assertTrue("应至少产生一种组合", result3.length > 0);
        assertEquals("结果应全部为大写", result3[0], result3[0].toUpperCase());
        // 测试大小写转换 - 小写
        String[] result4 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("中国", ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, false);
        assertNotNull("结果不应为null", result4);
        assertTrue("应至少产生一种组合", result4.length > 0);
        assertEquals("结果应全部为小写", result4[0], result4[0].toLowerCase());
        // 测试混合字符（包含非中文字符）
        String[] result5 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("ABC中国123", ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, null);
        assertNotNull("结果不应为null", result5);
        assertTrue("应至少产生一种组合", result5.length > 0);
        // 测试null输入，应返回空数组
        String[] result6 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence(null, ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, null);
        assertNotNull("结果不应为null", result6);
        assertEquals("应返回空数组", 0, result6.length);
        // 测试空字符串输入，应返回空数组
        String[] result7 = ChinesePhonetic.BASIC.listPinyinFullSpellingDataByUnicodeSequence("", ChinesePinyinStyle.WITHOUT_TONE_V_ASCII, null);
        assertNotNull("结果不应为null", result7);
        assertEquals("应返回空数组", 0, result7.length);
    }

    /**
     * 测试根据Unicode字符序列获取拼音首字母的方法
     * 验证不同输入和配置下方法的正确性
     */
    @Test
    public void test_listPinyinFirstLetterByUnicodeSequence() {
        // 测试正常情况下的首字母转换
        String[] result1 = ChinesePhonetic.BASIC.listPinyinFirstLetterByUnicodeSequence("中国", null);
        assertNotNull("结果不应为null", result1);
        assertTrue("应至少产生一种组合", result1.length > 0);
        // 测试大小写转换 - 大写
        String[] result2 = ChinesePhonetic.BASIC.listPinyinFirstLetterByUnicodeSequence("中国", true);
        assertNotNull("结果不应为null", result2);
        assertTrue("应至少产生一种组合", result2.length > 0);
        assertEquals("结果应全部为大写", result2[0], result2[0].toUpperCase());
        // 测试大小写转换 - 小写
        String[] result3 = ChinesePhonetic.BASIC.listPinyinFirstLetterByUnicodeSequence("中国", false);
        assertNotNull("结果不应为null", result3);
        assertTrue("应至少产生一种组合", result3.length > 0);
        assertEquals("结果应全部为小写", result3[0], result3[0].toLowerCase());
        // 测试混合字符（包含非中文字符）
        String[] result4 = ChinesePhonetic.BASIC.listPinyinFirstLetterByUnicodeSequence("ABC中国123", null);
        assertNotNull("结果不应为null", result4);
        assertTrue("应至少产生一种组合", result4.length > 0);
        // 测试null输入，应返回空数组
        String[] result5 = ChinesePhonetic.BASIC.listPinyinFirstLetterByUnicodeSequence(null, null);
        assertNotNull("结果不应为null", result5);
        assertEquals("应返回空数组", 0, result5.length);
        // 测试空字符串输入，应返回空数组
        String[] result6 = ChinesePhonetic.BASIC.listPinyinFirstLetterByUnicodeSequence("", null);
        assertNotNull("结果不应为null", result6);
        assertEquals("应返回空数组", 0, result6.length);
    }

}
