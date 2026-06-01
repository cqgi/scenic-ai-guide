package com.scenic.ai.rag.service;

import com.scenic.ai.scenic.ScenicSpot;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class TextProcessingService {

    private static final Pattern YEAR_PATTERN = Pattern.compile("[一二三四五六七八九十百千万零〇]{2,6}年|\\d{3,4}年|明代|清代|唐代|宋代|元代|民国");
    private static final Pattern PERSON_PATTERN = Pattern.compile("[\\p{IsHan}]{2,4}(?:大师|法师|居士|先生|将军|诗人|书法家|画家|皇帝|名人)");
    private static final Pattern FACILITY_PATTERN = Pattern.compile("(游客中心|服务中心|停车场|卫生间|观景台|步道|入口|出口|码头|长廊|古寺|湖|谷)");
    private static final Set<String> STOP_WORDS = Set.of("这个", "那个", "什么", "怎么", "可以", "一下", "介绍", "讲解", "景区", "游客", "是否", "这里");

    public String clean(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace('\u00A0', ' ')
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    public List<String> splitChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = clean(text).split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String normalized = paragraph.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            if (current.length() + normalized.length() > 600 && !current.isEmpty()) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(normalized);
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    public List<String> tokenize(String text) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        String normalized = clean(text).toLowerCase();
        Matcher latin = Pattern.compile("[a-z0-9]{2,}").matcher(normalized);
        while (latin.find()) {
            tokens.add(latin.group());
        }
        Matcher chineseWord = Pattern.compile("[\\p{IsHan}]{2,8}").matcher(normalized);
        while (chineseWord.find()) {
            String value = chineseWord.group();
            if (!STOP_WORDS.contains(value)) {
                tokens.add(value);
            }
            for (int size = 2; size <= 4; size++) {
                for (int i = 0; i + size <= value.length(); i++) {
                    String gram = value.substring(i, i + size);
                    if (!STOP_WORDS.contains(gram)) {
                        tokens.add(gram);
                    }
                }
            }
        }
        return new ArrayList<>(tokens);
    }

    public ExtractedTerms extractTerms(String text, List<ScenicSpot> spots) {
        LinkedHashSet<String> spotNames = new LinkedHashSet<>();
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        LinkedHashSet<String> keywords = new LinkedHashSet<>(tokenize(text));

        for (ScenicSpot spot : spots) {
            if (contains(text, spot.getName())) {
                spotNames.add(spot.getName());
                keywords.add(spot.getName());
            }
            if (spot.getAlias() != null) {
                for (String alias : spot.getAlias().split(",")) {
                    String value = alias.trim();
                    if (!value.isEmpty() && contains(text, value)) {
                        aliases.add(value);
                        keywords.add(value);
                    }
                }
            }
        }
        addMatches(YEAR_PATTERN, text, keywords);
        addMatches(PERSON_PATTERN, text, keywords);
        addMatches(FACILITY_PATTERN, text, keywords);
        return new ExtractedTerms(
                String.join(",", keywords),
                String.join(",", aliases),
                String.join(",", spotNames)
        );
    }

    public String buildSearchText(String title, String content, ExtractedTerms terms, String tags) {
        return String.join(" ",
                nullToEmpty(title),
                nullToEmpty(content),
                nullToEmpty(terms.keywords()),
                nullToEmpty(terms.aliases()),
                nullToEmpty(terms.spotNames()),
                nullToEmpty(tags)
        ).trim();
    }

    private boolean contains(String text, String value) {
        return text != null && value != null && text.contains(value);
    }

    private void addMatches(Pattern pattern, String text, Set<String> target) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            target.add(matcher.group());
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record ExtractedTerms(String keywords, String aliases, String spotNames) {
    }
}
