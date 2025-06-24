package milou.Util;

public class CodeGenerator {
    public static String generateCode() {
        String characters = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder code = new StringBuilder();
        int length = 6;

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}
