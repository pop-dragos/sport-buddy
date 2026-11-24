package ro.utcn.sportbuddy.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

// Cursul pentru afișarea prețurilor în alte monede.
// rates: câte unități din fiecare monedă fac 1 RON (de exemplu EUR = 0.196...), deci suma în monedă = RON * curs.
// live = false: BNR nu a putut fi citit și se folosesc cursuri fixe, orientative; date e atunci data calculului.
public record ExchangeRatesDto(String base, Map<String, BigDecimal> rates, LocalDate date, boolean live) {}
