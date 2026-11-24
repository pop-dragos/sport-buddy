package ro.utcn.sportbuddy.common.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    void romanianStaysAsWritten() {
        assertEquals("Date invalide.", Messages.translate("Date invalide.", Language.RO));
    }

    @Test
    void translatesExactAndPatternMessages() {
        assertEquals("Invalid data.", Messages.translate("Date invalide.", Language.EN));
        assertEquals("Page size must be between 1 and 50.",
                Messages.translate("Mărimea paginii trebuie să fie între 1 și 50.", Language.EN));
        assertEquals("The account email is now ana@demo.ro.",
                Messages.translate("Emailul contului este acum ana@demo.ro.", Language.EN));
    }

    @Test
    void translatesTheChatSystemMessagesStoredInRomanian() {
        assertEquals("Lobby confirmed.", Messages.translate("Lobby confirmat.", Language.EN));
        assertEquals("The lobby was cancelled.", Messages.translate("Lobby-ul a fost anulat.", Language.EN));
        assertEquals("ana joined.", Messages.translate("ana s-a înscris.", Language.EN));
        assertEquals("ana left the lobby.", Messages.translate("ana a ieșit din lobby.", Language.EN));
        assertEquals("Lobby confirmed. Final price: 30 RON / person.",
                Messages.translate("Lobby confirmat. Preț final: 30 RON / persoană.", Language.EN));
        // în română rămân neschimbate
        assertEquals("ana s-a înscris.", Messages.translate("ana s-a înscris.", Language.RO));
    }

    @Test
    void unknownMessagesStayInRomanianInsteadOfBreaking() {
        assertEquals("Ceva nou.", Messages.translate("Ceva nou.", Language.EN));
        assertNull(Messages.translate(null, Language.EN));
    }

    @Test
    void languageComesFromTheAcceptLanguageHeader() {
        assertEquals(Language.EN, Language.fromHeader("en"));
        assertEquals(Language.EN, Language.fromHeader("en-US,en;q=0.9"));
        assertEquals(Language.RO, Language.fromHeader("ro-RO"));
        assertEquals(Language.RO, Language.fromHeader("fr"));
        assertEquals(Language.RO, Language.fromHeader(null));
    }

    @Test
    void currentLanguageFollowsTheRequestAndDefaultsToRomanian() {
        assertEquals(Language.RO, RequestLanguage.current());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            assertEquals(Language.EN, RequestLanguage.current());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    // Dacă cineva adaugă un mesaj de eroare nou în română și uită dicționarul, testul pică și arată care lipsește.
    @Test
    void everyErrorMessageInTheCodeHasAnEnglishTranslation() throws IOException {
        Path root = Path.of("src/main/java");
        Pattern call = Pattern.compile(
                "(?:new ApiException\\(\\s*HttpStatus\\.\\w+\\s*,\\s*\"[A-Z_]+\"\\s*,|invalid\\(\\s*[^,]+,|message\\s*=|invalidLink\\()"
                        + "\\s*((?:\"(?:[^\"\\\\\\n]|\\\\.)*\"\\s*\\+?\\s*)+)");
        Pattern literal = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");

        List<String> missing = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String text = Files.readString(file);
                Matcher m = call.matcher(text);
                while (m.find()) {
                    StringBuilder joined = new StringBuilder();
                    Matcher l = literal.matcher(m.group(1));
                    while (l.find()) joined.append(l.group(1));
                    String message = joined.toString();
                    // mesajul se termină cu variabilă (ex. „Cel mult ” + n): îl acoperă un șablon, nu îl verificăm aici
                    boolean dynamic = m.group(1).trim().endsWith("+");
                    if (!dynamic && message.contains(" ") && !Messages.knows(message)) {
                        missing.add(file.getFileName() + ": " + message);
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "Lipsesc din Messages: " + missing);
    }
}
