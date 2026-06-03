package at.redi2go.photonics.common.iris;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.anarres.cpp.Token;

import java.util.HashSet;
import java.util.Set;

public class UniformPatcher {
    private final static String UNIFORM_PREFIX = "//ph_required:";

    private static Set<String> uniforms = new HashSet<>();
    private static int parsingState = 0;

    public static void prepare() {
        uniforms.clear();
    }

    public static void nextToken(Token token) {
        switch (token.getType()) {
            // Token.WHITESPACE
            case 294 -> {
                return;
            }

            // Token.CCOMMENT
            case 260 -> {
                return;
            }

            // Token.CPPCOMMENT
            case 261 -> {
                return;
            }


            case 270 -> {
                if (token.getText().equals("uniform") && parsingState == 0) {
                    parsingState = 1;
                    return;
                }

                if (parsingState == 1) {
                    parsingState = 2;
                    return;
                }

                if (parsingState != 2) return;

                uniforms.add(token.getText());
            }

            default -> {
                parsingState = 0;
                return;
            }
        }
    }

    private static void expectStr(StringReader reader, String str) throws CommandSyntaxException {
        for (var i = 0; i < str.length(); i++) reader.expect(str.charAt(i));
    }

    public static String addRequiredUniforms(String source) throws CommandSyntaxException {
        String[] lines = source.split("\n");
        StringBuilder str = new StringBuilder();
        StringBuilder uniformBuilder = new StringBuilder();
        final int[] uniformCount = { 0 };

        boolean replaced = false;

        Runnable nextUniform = () -> {
            if (!uniformBuilder.isEmpty()) {
                String uniform = uniformBuilder.toString();
                uniformBuilder.setLength(0);

                if (uniforms.contains(uniform)) return;
                if (uniformCount[0]++ > 0) str.append(", ");

                str.append(uniform);
            }
        };

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.startsWith(UNIFORM_PREFIX)) continue;

            str.setLength(0);
            uniformBuilder.setLength(0);
            uniformCount[0] = 0;

            StringReader reader = new StringReader(line);
            expectStr(reader, UNIFORM_PREFIX);

            reader.skipWhitespace();
            expectStr(reader, "uniform");
            str.append("uniform ");

            reader.skipWhitespace();
            str.append(reader.readStringUntil(' '));
            str.append(' ');

            while (reader.canRead() && reader.peek() != ';') {
                switch (reader.peek()) {
                    case ' ' -> {
                        nextUniform.run();
                        reader.skipWhitespace();
                    }

                    case ',' -> {
                        nextUniform.run();

                        reader.skip();
                        reader.skipWhitespace();
                    }

                    default -> uniformBuilder.append(reader.read());
                }
            }

            nextUniform.run();

            if (uniformCount[0] == 0) continue;

            str.append(';');
            lines[i] = str.toString();
            replaced = true;
        }

        if (!replaced) return source;
        return String.join("\n", lines);
    }
}
