package ro.utcn.sportbuddy.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.catalog.dto.ExchangeRatesDto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest {

    private static final String XML = """
            <?xml version="1.0" encoding="utf-8"?>
            <DataSet xmlns="http://www.bnr.ro/xsd">
              <Header><Publisher>National Bank of Romania</Publisher></Header>
              <Body>
                <OrigCurrency>RON</OrigCurrency>
                <Cube date="2026-11-23">
                  <Rate currency="EUR">5.0000</Rate>
                  <Rate currency="HUF" multiplier="100">1.2500</Rate>
                  <Rate currency="USD">4.0000</Rate>
                </Cube>
              </Body>
            </DataSet>
            """;

    // ceas pe care îl putem muta înainte
    private static class TestClock extends Clock {
        Instant now = Instant.parse("2026-11-24T10:00:00Z");

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private BnrClient bnr;
    private TestClock clock;
    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        bnr = mock(BnrClient.class);
        clock = new TestClock();
        service = new ExchangeRateService(bnr, clock);
    }

    @Test
    void parsesBnrXmlIncludingMultipliers() throws Exception {
        Map<String, BigDecimal> ron = ExchangeRateService.parse(XML);

        assertEquals(0, new BigDecimal("5").compareTo(ron.get("EUR")));
        assertEquals(0, new BigDecimal("4").compareTo(ron.get("USD")));
        // 1.25 RON pentru 100 HUF = 0.0125 RON pentru unul
        assertEquals(0, new BigDecimal("0.0125").compareTo(ron.get("HUF")));
    }

    @Test
    void rejectsXmlWithDoctype() {
        String evil = "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><a>&e;</a>";
        assertThrows(Exception.class, () -> ExchangeRateService.parse(evil));
    }

    @Test
    void ratesAreUnitsOfCurrencyPerRon() {
        when(bnr.fetchXml()).thenReturn(XML);

        ExchangeRatesDto rates = service.getRates();

        assertTrue(rates.live());
        assertEquals("RON", rates.base());
        // 5 RON = 1 EUR, deci 1 RON = 0.2 EUR; 4 RON = 1 USD, deci 0.25
        assertEquals(0, new BigDecimal("0.2").compareTo(rates.rates().get("EUR")));
        assertEquals(0, new BigDecimal("0.25").compareTo(rates.rates().get("USD")));
        assertFalse(rates.rates().containsKey("HUF"), "doar monedele din Setări");
    }

    @Test
    void ratesAreCachedForSixHours() {
        when(bnr.fetchXml()).thenReturn(XML);

        service.getRates();
        clock.now = clock.now.plus(Duration.ofHours(5));
        service.getRates();
        verify(bnr, times(1)).fetchXml();

        clock.now = clock.now.plus(Duration.ofHours(2));
        service.getRates();
        verify(bnr, times(2)).fetchXml();
    }

    @Test
    void fallsBackToFixedRatesWhenBnrIsDown() {
        when(bnr.fetchXml()).thenThrow(new RuntimeException("timeout"));

        ExchangeRatesDto rates = service.getRates();

        assertFalse(rates.live());
        // 5.30 RON pentru un euro, deci 1 RON = 0.188679 EUR
        assertEquals(0, new BigDecimal("0.188679").compareTo(rates.rates().get("EUR")));
        assertNotNull(rates.date());
    }

    @Test
    void retriesBnrAfterFifteenMinutesNotOnEveryRequest() {
        when(bnr.fetchXml()).thenThrow(new RuntimeException("timeout"));

        service.getRates();
        service.getRates();
        verify(bnr, times(1)).fetchXml();

        // metoda aruncă în acest moment, deci o re-stubuim cu doReturn
        doReturn(XML).when(bnr).fetchXml();
        clock.now = clock.now.plus(Duration.ofMinutes(16));
        assertTrue(service.getRates().live());
    }

    @Test
    void keepsLastGoodRatesWhenBnrFailsLater() {
        when(bnr.fetchXml()).thenReturn(XML);
        service.getRates();

        when(bnr.fetchXml()).thenThrow(new RuntimeException("timeout"));
        clock.now = clock.now.plus(Duration.ofHours(7));
        ExchangeRatesDto rates = service.getRates();

        assertTrue(rates.live(), "rămâne ultimul curs real, nu cel fix");
        assertEquals(0, new BigDecimal("0.25").compareTo(rates.rates().get("USD")));
    }

    @Test
    void xmlWithoutEurFallsBack() {
        when(bnr.fetchXml()).thenReturn(XML.replace("EUR", "XXX"));
        assertFalse(service.getRates().live());
    }
}
