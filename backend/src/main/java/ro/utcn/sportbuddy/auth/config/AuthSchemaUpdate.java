package ro.utcn.sportbuddy.auth.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.common.db.EnumCheckSync;

// auth_tokens.type a primit CHANGE_EMAIL după crearea tabelei: restricția din baza de date trebuie refăcută.
@Component
public class AuthSchemaUpdate implements ApplicationRunner {

    private final EnumCheckSync enumCheckSync;

    public AuthSchemaUpdate(EnumCheckSync enumCheckSync) {
        this.enumCheckSync = enumCheckSync;
    }

    @Override
    public void run(ApplicationArguments args) {
        enumCheckSync.sync("auth_tokens", "type", AuthTokenType.class);
    }
}
