package task4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import task4.specification.Message;

public class ChecksumUtility {

    private static final int DEFAULT_RANDOM_MESSAGE_SIZE = 32;
    private static final String CHECKSUM_DELIMITER = "|";

    /**
     * Computes the MD5 checksum of the given message bytes.
     *
     * @param message The byte array to compute the checksum for.
     * @return A string representing the hexadecimal MD5 checksum.
     */
    public static String computeChecksum(byte[] message) {
        return hashWithAlgorithm("MD5", message);
    }

    /**
     * Verifies that the checksum matches the provided message bytes.
     *
     * @param message The message content as a byte array.
     * @param receivedChecksum The checksum received with the message.
     */
    public static void verifyChecksum(byte[] message, String receivedChecksum) {
        Assertions.assertEquals(receivedChecksum, computeChecksum(message), "Checksum verification failed");
    }

    /**
     * Generates a random Message with a checksum appended.
     *
     * @param size The size of the random message in bytes.
     * @return A new `Message` object with random content and checksum appended.
     */
    public static Message generateRandomMessageWithChecksum(int size) {
        byte[] randomContent = new byte[size];
        new Random().nextBytes(randomContent);

        String messageWithChecksum = Base64.getEncoder().encodeToString(randomContent) +
                CHECKSUM_DELIMITER + computeChecksum(randomContent);
        byte[] messageBytes = messageWithChecksum.getBytes(StandardCharsets.UTF_8);

        return new Message(messageBytes, 0, messageBytes.length);
    }

    /**
     * Overloaded method to generate a random Message with a default size.
     *
     * @return A new `Message` object with random content and checksum appended.
     */
    public static Message generateRandomMessageWithChecksum() {
        return generateRandomMessageWithChecksum(DEFAULT_RANDOM_MESSAGE_SIZE);
    }

    /**
     * Verifies the received Message by extracting and validating its checksum.
     *
     * @param receivedMessage The received `Message` object containing content and checksum.
     * @return True if the checksum is valid, false otherwise.
     */
    public static boolean verifyReceivedMessage(Message receivedMessage) {
        String[] parts = new String(receivedMessage.message, StandardCharsets.UTF_8).split("\\" + CHECKSUM_DELIMITER, 2);
        if (parts.length != 2) return false;

        byte[] messageContent = Base64.getDecoder().decode(parts[0]);
        try {
            verifyChecksum(messageContent, parts[1]);
            return true;
        } catch (AssertionError e) {
            System.err.println("Checksum verification failed.");
            return false;
        }
    }

    /**
     * Creates a close message with a checksum appended.
     *
     * @return A new `Message` object representing a "CLOSE" command with checksum appended.
     */
    public static Message createCloseMessageWithChecksum() {
        byte[] closeMessage = "CLOSE".getBytes(StandardCharsets.UTF_8);
        String messageWithChecksum = "CLOSE" + CHECKSUM_DELIMITER + computeChecksum(closeMessage);
        return new Message(messageWithChecksum.getBytes(StandardCharsets.UTF_8), 0, messageWithChecksum.length());
    }

    /**
     * Checks if the given message is a close message.
     *
     * @param message The `Message` object to check.
     * @return True if it is a close message, false otherwise.
     */
    public static boolean isCloseMessage(Message message) {
        String content = new String(message.message, StandardCharsets.UTF_8).split("\\" + CHECKSUM_DELIMITER, 2)[0];
        return "CLOSE".equals(content);
    }

    // Helper method to handle hashing
    private static String hashWithAlgorithm(String algorithm, byte[] message) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(message);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) hexString.append(String.format("%02x", b));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found: " + algorithm, e);
        }
    }
}
