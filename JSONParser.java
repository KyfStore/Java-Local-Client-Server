import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JSONParser {

    public HashMap<String, String> parseJSONFile(String filePath) {
        HashMap<String, String> resultMap = new HashMap<>();

        if (Files.exists(Paths.get(filePath))) {
            try (FileReader reader = new FileReader(filePath)) {
                StringBuilder jsonBuilder = new StringBuilder();
                int nextChar;
                while ((nextChar = reader.read()) != -1) {
                    jsonBuilder.append((char) nextChar);
                }

                String jsonString = jsonBuilder.toString();
                Object parsedObject = parseJSON(jsonString);

                if (parsedObject instanceof Map) {
                    convertToHashMap((Map<?, ?>) parsedObject, resultMap);
                } else {
                    throw new IllegalStateException("Root element of JSON should be an object.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        return resultMap;
    }

    private static void convertToHashMap(Map<?, ?> inputMap, HashMap<String, String> outputMap) {
        for (Map.Entry<?, ?> entry : inputMap.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof String) {
                outputMap.put(key, (String) value);
            } else {
                throw new IllegalArgumentException("Value is not of type String for key: " + key);
            }
        }
    }

    private static Object parseJSON(String jsonString) {
        JSONTokenizer tokenizer = new JSONTokenizer(jsonString);
        return tokenizer.parse();
    }

    private static class JSONTokenizer {
        private String json;
        private int index;

        public JSONTokenizer(String json) {
            this.json = json;
            this.index = 0;
        }

        public Object parse() {
            skipWhiteSpace();
            char nextChar = peekNextChar();

            if (nextChar == '{') {
                return parseObject();
            } else if (nextChar == '[') {
                return parseArray();
            } else if (nextChar == '"') {
                return parseString();
            } else if (Character.isDigit(nextChar) || nextChar == '-') {
                return parseNumber();
            } else if (nextChar == 't' || nextChar == 'f') {
                return parseBoolean();
            } else if (nextChar == 'n') {
                return parseNull();
            } else {
                throw new IllegalStateException("Unexpected character at position " + index);
            }
        }

        private char peekNextChar() {
            if (index < json.length()) {
                return json.charAt(index);
            } else if (index == json.length() && json.length() == 0) {
                throw new IllegalStateException("JSON string is empty");
            } else {
                throw new IllegalStateException("Attempted to peek beyond end of JSON string");
            }
        }

        private void skipWhiteSpace() {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
        }

        private Object parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            consume('{');
            skipWhiteSpace();
            while (peekNextChar() != '}') {
                String key = parseString();
                skipWhiteSpace();
                consume(':');
                Object value = parse();
                map.put(key, value);
                skipWhiteSpace();
                if (peekNextChar() == ',') {
                    consume(',');
                    skipWhiteSpace();
                }
            }
            consume('}');
            return map;
        }

        private Object parseArray() {
            List<Object> list = new ArrayList<>();
            consume('[');
            skipWhiteSpace();
            while (peekNextChar() != ']') {
                list.add(parse());
                skipWhiteSpace();
                if (peekNextChar() == ',') {
                    consume(',');
                    skipWhiteSpace();
                }
            }
            consume(']');
            return list;
        }

        private String parseString() {
            consume('"');
            StringBuilder sb = new StringBuilder();
            while (peekNextChar() != '"') {
                sb.append(consumeNextChar());
            }
            consume('"');
            return sb.toString();
        }

        private Number parseNumber() {
            StringBuilder sb = new StringBuilder();
            while (index < json.length() && (Character.isDigit(peekNextChar()) || peekNextChar() == '-' || peekNextChar() == '.')) {
                sb.append(consumeNextChar());
            }
            String numberStr = sb.toString();
            if (numberStr.contains(".")) {
                return Double.parseDouble(numberStr);
            } else {
                return Long.parseLong(numberStr);
            }
        }

        private Boolean parseBoolean() {
            if (peekNextChar() == 't') {
                consumeWord("true");
                return true;
            } else {
                consumeWord("false");
                return false;
            }
        }

        private Object parseNull() {
            consumeWord("null");
            return null;
        }

        private void consume(char expected) {
            char nextChar = consumeNextChar();
            if (nextChar != expected) {
                throw new IllegalStateException("Expected '" + expected + "' but found '" + nextChar + "' at position " + index);
            }
        }

        private void consumeWord(String expected) {
            for (int i = 0; i < expected.length(); i++) {
                char nextChar = consumeNextChar();
                if (nextChar != expected.charAt(i)) {
                    throw new IllegalStateException("Expected '" + expected + "' but found '" + nextChar + "' at position " + index);
                }
            }
        }

        private char consumeNextChar() {
            char nextChar = peekNextChar();
            index++;
            return nextChar;
        }
    }
}