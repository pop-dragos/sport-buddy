package ro.utcn.sportbuddy.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Limba cererii curente, pentru codul care nu primește cererea (servicii care trimit emailuri sau creează conturi).
// În afara unei cereri (teste, sarcini programate) răspunsul e româna.
public final class RequestLanguage {

    private RequestLanguage() {}

    public static Language current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet) {
            HttpServletRequest request = servlet.getRequest();
            return Language.of(request);
        }
        return Language.RO;
    }
}
