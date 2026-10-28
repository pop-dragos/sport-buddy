package ro.utcn.sportbuddy.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

// Pozele de profil se salvează pe disc, în folderul uploads/ (ignorat de Git). În baza de date rămâne doar numele fișierului.
@Component
public class AvatarStorage {

    // Doar nume generate de noi: <userId>-<uuid>.<extensie>. Orice altceva (de exemplu "../") este refuzat.
    private static final Pattern FILE_NAME = Pattern.compile("^\\d+-[0-9a-f]{32}\\.(png|jpg|webp|gif)$");

    private final Path dir;

    public AvatarStorage(@Value("${app.uploads.dir:uploads}") String dir) {
        this.dir = Path.of(dir).toAbsolutePath().normalize();
    }

    public String save(Long userId, byte[] content, String extension) {
        String name = userId + "-" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(name), content);
        } catch (IOException e) {
            throw new UncheckedIOException("Poza nu a putut fi salvată.", e);
        }
        return name;
    }

    public void delete(String name) {
        if (name == null || !FILE_NAME.matcher(name).matches()) return;
        try {
            Files.deleteIfExists(dir.resolve(name));
        } catch (IOException e) {
            // un fișier rămas pe disc nu strică nimic; nu oprim cererea pentru asta
        }
    }

    public Optional<Path> find(String name) {
        if (name == null || !FILE_NAME.matcher(name).matches()) return Optional.empty();
        Path file = dir.resolve(name);
        return Files.isRegularFile(file) ? Optional.of(file) : Optional.empty();
    }
}
