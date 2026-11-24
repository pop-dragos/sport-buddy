package ro.utcn.sportbuddy.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Hibernate pune pe coloanele enum o restricție CHECK cu valorile de la crearea tabelei și, cu ddl-auto=update,
// nu o mai actualizează. O valoare adăugată ulterior în enum ar fi respinsă de baza de date.
// sync() reface restricția cu valorile actuale; se apelează la pornire pentru coloanele care au primit valori noi.
@Component
public class EnumCheckSync {

    private static final Logger log = LoggerFactory.getLogger(EnumCheckSync.class);
    private static final Pattern NAME = Pattern.compile("[a-z_][a-z0-9_]*");

    private final JdbcTemplate jdbc;

    public EnumCheckSync(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void sync(String table, String column, Class<? extends Enum<?>> type) {
        // numele vin din cod, nu de la utilizator; verificarea doar previne o greșeală de scriere
        if (!NAME.matcher(table).matches() || !NAME.matcher(column).matches()) {
            throw new IllegalArgumentException("Nume invalid: " + table + "." + column);
        }
        String constraint = table + "_" + column + "_check";
        String values = Arrays.stream(type.getEnumConstants())
                .map(v -> "'" + v.name() + "'")
                .collect(Collectors.joining(", "));
        jdbc.execute("alter table " + table + " drop constraint if exists " + constraint);
        jdbc.execute("alter table " + table + " add constraint " + constraint
                + " check (" + column + " in (" + values + "))");
        log.debug("Restricția {} are acum valorile {}", constraint, values);
    }
}
