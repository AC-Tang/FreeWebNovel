package com.example.freefiction.utils.ip;

import javax.servlet.http.HttpServletRequest;

public class IpAddressUtil {

    public static String getRealIpAddr(HttpServletRequest request) {
        // 尝试从 X-Forwarded-For 请求头获取客户端的真实 IP 地址
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 如果 X-Forwarded-For 不可用，尝试从 Proxy-Client-IP 获取
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 如果 Proxy-Client-IP 不可用，尝试从 WL-Proxy-Client-IP 获取
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 如果所有代理头都不可用，使用 getRemoteAddr 获取
            ip = request.getRemoteAddr();
        }
        // 如果 X-Forwarded-For 包含多个 IP 地址，取第一个非 unknown 的有效 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
