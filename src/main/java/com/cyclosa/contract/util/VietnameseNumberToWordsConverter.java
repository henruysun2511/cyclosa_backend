package com.cyclosa.contract.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Tiện ích chuyển đổi số tiền thành chữ Tiếng Việt dùng trong Hợp đồng lao động.
 * Ví dụ: 25,000,000 -> "Hai mươi lăm triệu đồng chẵn"
 */
public final class VietnameseNumberToWordsConverter {

    private static final String[] DIGITS = {
            "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    private static final String[] UNITS = {
            "", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"
    };

    private VietnameseNumberToWordsConverter() {}

    public static String convertToWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Không đồng chẵn";
        }

        BigInteger integerPart = amount.toBigInteger().abs();
        if (integerPart.equals(BigInteger.ZERO)) {
            return "Không đồng chẵn";
        }

        String numberStr = integerPart.toString();
        // Nhóm từng 3 chữ số từ phải sang trái
        int length = numberStr.length();
        int groups = (length + 2) / 3;
        int leadingZeros = 3 * groups - length;
        String padded = "0".repeat(leadingZeros) + numberStr;

        StringBuilder result = new StringBuilder();
        boolean hasLeading = false;

        for (int i = 0; i < groups; i++) {
            int groupIndex = groups - 1 - i;
            String groupStr = padded.substring(i * 3, (i + 1) * 3);
            int hundred = groupStr.charAt(0) - '0';
            int ten = groupStr.charAt(1) - '0';
            int unit = groupStr.charAt(2) - '0';

            if (hundred == 0 && ten == 0 && unit == 0) {
                continue;
            }

            String groupText = readThreeDigits(hundred, ten, unit, hasLeading);
            if (!groupText.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(groupText);
                if (groupIndex < UNITS.length && !UNITS[groupIndex].isEmpty()) {
                    result.append(" ").append(UNITS[groupIndex]);
                }
                hasLeading = true;
            }
        }

        String formatted = result.toString().trim();
        if (formatted.isEmpty()) {
            return "Không đồng chẵn";
        }

        // Viết hoa chữ cái đầu tiên và thêm "đồng chẵn"
        String firstLetter = formatted.substring(0, 1).toUpperCase();
        return firstLetter + formatted.substring(1) + " đồng chẵn";
    }

    private static String readThreeDigits(int hundred, int ten, int unit, boolean hasLeading) {
        StringBuilder sb = new StringBuilder();

        if (hundred > 0 || hasLeading) {
            sb.append(DIGITS[hundred]).append(" trăm");
        }

        if (ten > 1) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(DIGITS[ten]).append(" mươi");
        } else if (ten == 1) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("mười");
        } else if (hundred > 0 && unit > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("lẻ");
        }

        if (unit > 0) {
            if (sb.length() > 0) sb.append(" ");
            if (unit == 1 && ten > 1) {
                sb.append("mốt");
            } else if (unit == 5 && ten > 0) {
                sb.append("lăm");
            } else if (unit == 4 && ten > 1) {
                sb.append("tư");
            } else {
                sb.append(DIGITS[unit]);
            }
        }

        return sb.toString();
    }
}
