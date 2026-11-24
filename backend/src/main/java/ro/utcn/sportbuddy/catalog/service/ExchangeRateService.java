package ro.utcn.sportbuddy.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ro.utcn.sportbuddy.catalog.dto.ExchangeRatesDto;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

// Cursul de afișare pentru monedele din Setări. Se citește de la BNR și se ține în memorie 6 ore.
// Dacă BNR nu răspunde, se păstrează ultimul curs bun, iar dacă nu există niciunul, se folosesc cursuri fixe
// (orientative; live = false), ca aplicația să meargă oricum. O nouă încercare se face după 15 minute.
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    private static final Duration FRESH_FOR = Duration.ofHours(6);
    private static final Duration RETRY_AFTER = Duration.ofMinutes(15);
    // câți RON valorează o unitate din fiecare monedă; folosite doar dacă BNR nu poate fi citit
    private static final Map<String, BigDecimal> FALLBACK_RON_PER_UNIT = Map.of(
            "EUR", new BigDecimal("5.30"), "USD", new BigDecimal("4.90"));

    static final String[] CURRENCIES = {"EUR", "USD"};

    private record Cached(ExchangeRatesDto rates, Instant retryAt) {}

    private final BnrClient client;
    private final Clock clock;
    private volatile Cached cached;

    @Autowired
    public ExchangeRateService(BnrClient client) {
        this(client, Clock.system(ZONE));
    }

    ExchangeRateService(BnrClient client, Clock clock) {
        this.client = client;
        this.clock = clock;
    }

    public ExchangeRatesDto getRates() {
        Cached current = cached;
        Instant now = clock.instant();
        if (current != null && now.isBefore(current.retryAt())) return current.rates();
        return refresh(now);
    }

    private synchronized ExchangeRatesDto refresh(Instant now) {
        // altcineva a reîmprospătat între timp
        Cached current = cached;
        if (current != null && now.isBefore(current.retryAt())) return current.rates();
        try {
            ExchangeRatesDto fresh = toRates(parse(client.fetchXml()), true, null);
            cached = new Cached(fresh, now.plus(FRESH_FOR));
            return fresh;
        } catch (Exception e) {
            log.warn("Cursul BNR nu a putut fi citit ({}). Folosesc ultimul curs cunoscut sau valorile fixe.",
                    e.getMessage());
            ExchangeRatesDto fallback = current != null ? current.rates()
                    : toRates(FALLBACK_RON_PER_UNIT, false, LocalDate.now(clock));
            cached = new Cached(fallback, now.plus(RETRY_AFTER));
            return fallback;
        }
    }

    // RON pentru o unitate din fiecare monedă cerută -> unități de monedă pentru 1 RON
    private ExchangeRatesDto toRates(Map<String, BigDecimal> ronPerUnit, boolean live, LocalDate date) {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        for (String code : CURRENCIES) {
            BigDecimal ron = ronPerUnit.get(code);
            if (ron == null || ron.signum() <= 0) throw new IllegalStateException("Lipsește cursul " + code);
            rates.put(code, BigDecimal.ONE.divide(ron, 6, RoundingMode.HALF_UP));
        }
        LocalDate day = date != null ? date : LocalDate.now(clock);
        return new ExchangeRatesDto("RON", rates, day, live);
    }

    // Din XML-ul BNR: <Rate currency="EUR">5.0987</Rate>, cu multiplier="100" la monedele mici (HUF, JPY...).
    // Întoarce câți RON valorează o unitate.
    static Map<String, BigDecimal> parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        NodeList nodes = doc.getElementsByTagNameNS("*", "Rate");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element rate = (Element) nodes.item(i);
            String multiplier = rate.getAttribute("multiplier");
            BigDecimal value = new BigDecimal(rate.getTextContent().trim());
            if (!multiplier.isBlank()) value = value.divide(new BigDecimal(multiplier), 8, RoundingMode.HALF_UP);
            result.put(rate.getAttribute("currency"), value);
        }
        return result;
    }
}
