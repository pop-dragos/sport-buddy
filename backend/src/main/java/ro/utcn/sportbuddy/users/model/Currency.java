package ro.utcn.sportbuddy.users.model;

// Moneda în care utilizatorul vede prețurile. Plata rămâne mereu în RON; celelalte monede sunt doar afișare,
// calculată după cursul BNR (vezi ExchangeRateService).
public enum Currency {
    RON, EUR, USD
}
