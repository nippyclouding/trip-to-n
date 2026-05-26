package server.TripToN.global.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ClientIpResolver {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final int MAX_IP_LENGTH = 45;

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (isPresent(forwardedFor)) {
            String firstAddress = forwardedFor.split(",", 2)[0].trim();
            if (isValidIpAddress(firstAddress)) {
                return firstAddress;
            }
        }

        String realIp = request.getHeader(X_REAL_IP);
        if (isPresent(realIp)) {
            String candidate = realIp.trim();
            if (isValidIpAddress(candidate)) {
                return candidate;
            }
        }

        String remoteAddress = request.getRemoteAddr();
        if (isPresent(remoteAddress)) {
            String candidate = remoteAddress.trim();
            if (isValidIpAddress(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isPresent(String value) {
        return value != null
                && !value.isBlank()
                && !"unknown".equalsIgnoreCase(value.trim());
    }

    private static boolean isValidIpAddress(String address) {
        if (address.isEmpty() || address.length() > MAX_IP_LENGTH || containsControlCharacter(address)) {
            return false;
        }

        if (address.indexOf(':') >= 0) {
            return isValidIpv6(address);
        }

        return isValidIpv4(address);
    }

    private static boolean isValidIpv4(String address) {
        String[] octets = address.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3) {
                return false;
            }
            for (int index = 0; index < octet.length(); index++) {
                char character = octet.charAt(index);
                if (character < '0' || character > '9') {
                    return false;
                }
            }
            if (Integer.parseInt(octet) > 255) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv6(String address) {
        if (address.indexOf('%') >= 0 || !address.matches("[0-9A-Fa-f:.]+")) {
            return false;
        }

        try {
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private static boolean containsControlCharacter(String address) {
        for (int index = 0; index < address.length(); index++) {
            if (Character.isISOControl(address.charAt(index))) {
                return true;
            }
        }
        return false;
    }
}
