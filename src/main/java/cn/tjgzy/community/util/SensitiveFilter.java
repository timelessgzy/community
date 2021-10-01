package cn.tjgzy.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author GongZheyi
 * @create 2021-09-30-20:09
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();


    @PostConstruct
    public void init() {
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败！");
        }


    }

    /**
     * 将敏感词加入字典树
     * @param keyword
     */
    private void addKeyword(String keyword) {
        char[] chars = keyword.toCharArray();
        TrieNode temp = this.rootNode;
        for (char ch: chars) {
            if (!temp.subNodes.containsKey(ch)) {
                TrieNode node = new TrieNode();
                temp.subNodes.put(ch, node);
            }
            temp = temp.subNodes.get(ch);
        }
        temp.setKeyWordEnd(true);
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return  过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        TrieNode tempNode = rootNode;
        // 指向
        int begin = 0, position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin为开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 归位
                begin++;
                position = begin;
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd) {
                // 发现了敏感词
                sb.append(REPLACEMENT);
                position++;
                begin = position;
                tempNode = rootNode;
            } else {
                position++;
            }
        }
        // 将最后一批字符串计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断是否为符号
     * @param c
     * @return
     */
    private boolean isSymbol(char c) {
        // 0x2E80 —— 0x9FFFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFFF);
    }



    private class TrieNode {
        // 是否为结束节点
        private boolean isKeyWordEnd = false;

        // 子节点集合
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }
    }
}
