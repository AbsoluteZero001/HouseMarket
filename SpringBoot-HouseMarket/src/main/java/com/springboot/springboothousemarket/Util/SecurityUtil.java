package com.springboot.springboothousemarket.Util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 安全工具类，提供输入验证、XSS防护、SQL注入防护等功能
 */
@Component
public class SecurityUtil {

    // 常见XSS攻击模式
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onsubmit(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    // SQL注入关键词
    private static final String[] SQL_INJECTION_KEYWORDS = {
            "'", "\"", "--", "#", ";", "/*", "*/", "xp_",
            "union", "select", "insert", "delete", "update",
            "drop", "create", "alter", "exec", "execute",
            "truncate", "shutdown", "waitfor", "delay",
            "information_schema", "sysdatabases"
    };

    /**
     * 验证字符串是否包含XSS攻击代码
     *
     * @param input 输入字符串
     * @return true如果安全，false如果包含XSS攻击
     */
    public boolean isSafeFromXSS(String input) {
        if (StringUtils.isBlank(input)) {
            return true;
        }

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清理字符串中的XSS攻击代码
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public String sanitizeXSS(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        String sanitized = input;
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }

        // 转义HTML特殊字符
        sanitized = sanitized.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");

        return sanitized;
    }

    /**
     * 验证字符串是否包含SQL注入攻击代码
     *
     * @param input 输入字符串
     * @return true如果安全，false如果包含SQL注入攻击
     */
    public boolean isSafeFromSQLInjection(String input) {
        if (StringUtils.isBlank(input)) {
            return true;
        }

        String lowerInput = input.toLowerCase();
        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                // 检查是否为正常的包含情况，比如英文名称包含'select'
                // 简单的关键词检测，实际应用可能需要更复杂的逻辑
                return false;
            }
        }
        return true;
    }

    /**
     * 验证用户名格式
     *
     * @param username 用户名
     * @return true如果格式正确
     */
    public boolean isValidUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        // 用户名允许字母、数字、下划线、中文，长度3-20
        return Pattern.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,20}$", username);
    }

    /**
     * 验证密码格式
     *
     * @param password 密码
     * @return true如果格式正确
     */
    public boolean isValidPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        // 密码至少6位，包含字母和数字
        return Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$", password);
    }

    /**
     * 验证邮箱格式
     *
     * @param email 邮箱
     * @return true如果格式正确
     */
    public boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email);
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return true如果格式正确
     */
    public boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        // 中国手机号格式
        return Pattern.matches("^1[3-9]\\d{9}$", phone);
    }

    /**
     * 验证整数范围
     *
     * @param value 整数值
     * @param min   最小值
     * @param max   最大值
     * @return true如果在范围内
     */
    public boolean isValidInteger(Integer value, int min, int max) {
        if (value == null) {
            return false;
        }
        return value >= min && value <= max;
    }

    /**
     * 验证价格范围
     *
     * @param price 价格
     * @return true如果价格有效
     */
    public boolean isValidPrice(Double price) {
        if (price == null) {
            return false;
        }
        return price >= 0 && price <= 1000000; // 最大100万
    }

    /**
     * 验证面积范围
     *
     * @param area 面积
     * @return true如果面积有效
     */
    public boolean isValidArea(Double area) {
        if (area == null) {
            return false;
        }
        return area >= 10 && area <= 500; // 10-500平米
    }

    /**
     * 验证URL是否安全
     *
     * @param url URL
     * @return true如果URL安全
     */
    public boolean isValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        // 简单URL验证，实际可能需要更复杂的验证
        return Pattern.matches("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", url);
    }

    /**
     * 验证输入长度
     *
     * @param input 输入字符串
     * @param min   最小长度
     * @param max   最大长度
     * @return true如果长度符合要求
     */
    public boolean isValidLength(String input, int min, int max) {
        if (StringUtils.isBlank(input)) {
            return min == 0;
        }
        int length = input.length();
        return length >= min && length <= max;
    }

    /**
     * 抛出安全性异常
     *
     * @param message 异常信息
     */
    public void throwSecurityException(String message) {
        throw new SecurityException(message);
    }
}