package ro.utcn.sportbuddy.common.i18n;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

// Limbile aplicației. Textele din cod sunt scrise în română (RO); EN se obține din dicționarul din Messages.
public enum Language {
    RO, EN;

    // Din antetul Accept-Language trimis de frontend la fiecare cerere („en”, „en-US”, „ro”...).
    // Orice altceva, sau lipsa antetului, înseamnă română.
    public static Language fromHeader(String acceptLanguage) {
        if (acceptLanguage == null) return RO;
        String first = acceptLanguage.split("[,;]")[0].trim().toLowerCase(Locale.ROOT);
        return first.startsWith("en") ? EN : RO;
    }

    public static Language of(HttpServletRequest request) {
        return fromHeader(request.getHeader("Accept-Language"));
    }
}
