package ro.utcn.sportbuddy.catalog.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

// Doar apelul HTTP către Banca Națională a României (cursul oficial al zilei, gratuit, fără cheie).
// Regulile (cache, valori de rezervă) stau în ExchangeRateService.
@Component
public class BnrClient {

    // adresa actuală a cursului zilei (www.bnr.ro/nbrfxrates.xml nu mai există și trimite pe prima pagină)
    private static final String URL = "https://curs.bnr.ro/nbrfxrates.xml";
    private static final Duration TIMEOUT = Duration.ofSeconds(4);

    private final RestClient restClient;

    public BnrClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public String fetchXml() {
        return restClient.get().uri(URL).retrieve().body(String.class);
    }
}
