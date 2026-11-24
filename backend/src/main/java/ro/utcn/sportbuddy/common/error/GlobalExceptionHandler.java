package ro.utcn.sportbuddy.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import ro.utcn.sportbuddy.common.i18n.Language;
import ro.utcn.sportbuddy.common.i18n.Messages;
import ro.utcn.sportbuddy.common.i18n.RequestLanguage;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Mesajele sunt scrise în română în cod; la cereri în engleză (Accept-Language) se traduc aici, o singură dată.
    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message,
                                                       Map<String, String> fields) {
        Language language = RequestLanguage.current();
        Map<String, String> translated = null;
        if (fields != null) {
            translated = new LinkedHashMap<>();
            for (Map.Entry<String, String> f : fields.entrySet()) {
                translated.put(f.getKey(), Messages.translate(f.getValue(), language));
            }
        }
        return ResponseEntity.status(status)
                .body(new ErrorResponse(code, Messages.translate(message, language), translated));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e) {
        return error(e.getStatus(), e.getCode(), e.getMessage(), e.getFields());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Corpul cererii este invalid.", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEndpoint(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Adresa nu există.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Metoda nu este permisă la această adresă.", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Formatul cererii nu este acceptat.", null);
    }

    // Fișier peste limita din spring.servlet.multipart (cererea e oprită înainte să ajungă la controller).
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException e) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                Map.of("file", "Fișierul este prea mare."));
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> handleMissingFile(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                Map.of("file", "Alege un fișier."));
    }
}