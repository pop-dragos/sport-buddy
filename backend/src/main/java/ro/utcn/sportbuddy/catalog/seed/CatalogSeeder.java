package ro.utcn.sportbuddy.catalog.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ro.utcn.sportbuddy.catalog.model.City;
import ro.utcn.sportbuddy.catalog.model.Field;
import ro.utcn.sportbuddy.catalog.model.Sport;
import ro.utcn.sportbuddy.catalog.repository.CityRepository;
import ro.utcn.sportbuddy.catalog.repository.FieldRepository;
import ro.utcn.sportbuddy.catalog.repository.SportRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class CatalogSeeder implements CommandLineRunner {

    // code, nume, capacitate implicită, tip de teren, în sală
    private static final Object[][] SPORTS = {
            { "football", "Fotbal", 10, "Teren de fotbal", false },
            { "basketball", "Baschet", 10, "Sală de baschet", true },
            { "tennis", "Tenis", 2, "Teren de tenis", false },
            { "padel", "Padel", 4, "Teren de padel", false },
            { "volleyball", "Volei", 12, "Sală de volei", true },
            { "handball", "Handbal", 14, "Sală de handbal", true },
            { "badminton", "Badminton", 2, "Sală de badminton", true },
            { "table-tennis", "Tenis de masă", 2, "Sală de tenis de masă", true },
            { "squash", "Squash", 2, "Teren de squash", true },
            { "running", "Alergare", 10, "Traseu de alergare", false },
            { "cycling", "Ciclism", 10, "Traseu de ciclism", false },
            { "swimming", "Înot", 6, "Bazin de înot", true }
    };

    // nume, latitudine, longitudine
    private static final Object[][] CITIES = {
            { "Alba Iulia", 46.07, 23.58 }, { "Alexandria", 43.97, 25.33 }, { "Arad", 46.18, 21.31 },
            { "Bacău", 46.57, 26.91 }, { "Baia Mare", 47.66, 23.58 }, { "Bistrița", 47.13, 24.49 },
            { "Botoșani", 47.75, 26.67 }, { "Brăila", 45.27, 27.96 }, { "Brașov", 45.65, 25.61 },
            { "București", 44.43, 26.10 }, { "Buzău", 45.15, 26.82 }, { "Călărași", 44.20, 27.33 },
            { "Cluj-Napoca", 46.77, 23.59 }, { "Constanța", 44.18, 28.63 }, { "Craiova", 44.32, 23.80 },
            { "Deva", 45.88, 22.90 }, { "Drobeta-Turnu Severin", 44.63, 22.66 }, { "Focșani", 45.70, 27.18 },
            { "Galați", 45.44, 28.05 }, { "Giurgiu", 43.90, 25.97 }, { "Iași", 47.16, 27.59 },
            { "Miercurea Ciuc", 46.36, 25.80 }, { "Oradea", 47.05, 21.92 }, { "Piatra Neamț", 46.93, 26.37 },
            { "Pitești", 44.86, 24.87 }, { "Ploiești", 44.94, 26.02 }, { "Râmnicu Vâlcea", 45.10, 24.37 },
            { "Reșița", 45.30, 21.89 }, { "Satu Mare", 47.79, 22.88 }, { "Sfântu Gheorghe", 45.86, 25.79 },
            { "Sibiu", 45.79, 24.15 }, { "Slatina", 44.43, 24.37 }, { "Slobozia", 44.56, 27.37 },
            { "Suceava", 47.65, 26.26 }, { "Târgoviște", 44.93, 25.46 }, { "Târgu Jiu", 45.04, 23.27 },
            { "Târgu Mureș", 46.54, 24.56 }, { "Timișoara", 45.75, 21.23 }, { "Tulcea", 45.18, 28.80 },
            { "Vaslui", 46.64, 27.73 }, { "Zalău", 47.19, 23.06 },
            { "Adjud", 46.10, 27.18 }, { "Aiud", 46.31, 23.72 }, { "Baia Sprie", 47.66, 23.69 },
            { "Băicoi", 45.04, 25.85 }, { "Băilești", 44.02, 23.35 }, { "Bârlad", 46.23, 27.67 },
            { "Beiuș", 46.67, 22.35 }, { "Blaj", 46.18, 23.92 }, { "Bocșa", 45.37, 21.71 },
            { "Borșa", 47.66, 24.66 }, { "Brad", 46.13, 22.79 }, { "Bragadiru", 44.37, 25.98 },
            { "Buftea", 44.57, 25.95 }, { "Calafat", 43.99, 22.93 }, { "Câmpeni", 46.36, 23.04 },
            { "Câmpia Turzii", 46.55, 23.88 }, { "Câmpina", 45.13, 25.73 }, { "Câmpulung", 45.27, 25.05 },
            { "Câmpulung Moldovenesc", 47.53, 25.55 }, { "Caracal", 44.11, 24.35 }, { "Caransebeș", 45.42, 22.22 },
            { "Carei", 47.68, 22.47 }, { "Cernavodă", 44.34, 28.03 }, { "Codlea", 45.70, 25.45 },
            { "Comănești", 46.42, 26.43 }, { "Corabia", 43.78, 24.50 }, { "Cugir", 45.84, 23.36 },
            { "Curtea de Argeș", 45.14, 24.68 }, { "Dej", 47.14, 23.88 }, { "Dorohoi", 47.96, 26.40 },
            { "Drăgășani", 44.66, 24.26 }, { "Eforie", 44.06, 28.63 }, { "Făgăraș", 45.84, 24.97 },
            { "Fălticeni", 47.46, 26.30 }, { "Fetești", 44.38, 27.83 }, { "Gheorgheni", 46.72, 25.60 },
            { "Gherla", 47.03, 23.91 }, { "Hunedoara", 45.75, 22.90 }, { "Huși", 46.67, 28.06 },
            { "Ineu", 46.43, 21.84 }, { "Jimbolia", 45.79, 20.72 }, { "Lugoj", 45.69, 21.90 },
            { "Lupeni", 45.36, 23.24 }, { "Mangalia", 43.82, 28.58 }, { "Marghita", 47.35, 22.34 },
            { "Medgidia", 44.25, 28.27 }, { "Mediaș", 46.16, 24.35 }, { "Mioveni", 44.96, 24.94 },
            { "Moinești", 46.47, 26.49 }, { "Motru", 44.80, 22.97 }, { "Năsăud", 47.28, 24.41 },
            { "Năvodari", 44.32, 28.61 }, { "Negrești-Oaș", 47.87, 23.43 }, { "Odorheiu Secuiesc", 46.30, 25.30 },
            { "Oltenița", 44.09, 26.64 }, { "Onești", 46.25, 26.77 }, { "Oravița", 45.04, 21.69 },
            { "Orăștie", 45.84, 23.20 }, { "Otopeni", 44.55, 26.07 }, { "Pantelimon", 44.45, 26.20 },
            { "Pașcani", 47.25, 26.72 }, { "Petroșani", 45.42, 23.37 }, { "Popești-Leordeni", 44.38, 26.17 },
            { "Predeal", 45.50, 25.58 }, { "Rădăuți", 47.84, 25.92 }, { "Râmnicu Sărat", 45.38, 27.06 },
            { "Râșnov", 45.59, 25.46 }, { "Reghin", 46.78, 24.71 }, { "Roman", 46.92, 26.92 },
            { "Roșiorii de Vede", 44.11, 24.99 }, { "Săcele", 45.62, 25.69 }, { "Salonta", 46.80, 21.66 },
            { "Sânnicolau Mare", 46.07, 20.63 }, { "Sebeș", 45.96, 23.57 }, { "Sighetu Marmației", 47.93, 23.89 },
            { "Sighișoara", 46.22, 24.79 }, { "Sinaia", 45.35, 25.55 }, { "Târgu Neamț", 47.20, 26.36 },
            { "Târnăveni", 46.33, 24.28 }, { "Tecuci", 45.85, 27.42 }, { "Toplița", 46.92, 25.35 },
            { "Turda", 46.57, 23.78 }, { "Turnu Măgurele", 43.75, 24.87 }, { "Urziceni", 44.72, 26.64 },
            { "Vatra Dornei", 47.35, 25.36 }, { "Voluntari", 44.49, 26.18 }, { "Vulcan", 45.38, 23.28 }
    };

    private static final String[] DISTRICTS = { "Centru", "Nord", "Sud" };

    private final SportRepository sportRepository;
    private final CityRepository cityRepository;
    private final FieldRepository fieldRepository;

    public CatalogSeeder(SportRepository sportRepository, CityRepository cityRepository,
            FieldRepository fieldRepository) {
        this.sportRepository = sportRepository;
        this.cityRepository = cityRepository;
        this.fieldRepository = fieldRepository;
    }

    @Override
    public void run(String... args) {
        seedSports();
        seedCities();
        seedFields();
    }

    private void seedSports() {
        if (sportRepository.count() > 0)
            return;

        List<Sport> sports = new ArrayList<>();
        for (Object[] d : SPORTS) {
            Sport s = new Sport();
            s.setCode((String) d[0]);
            s.setName((String) d[1]);
            s.setDefaultCapacity((Integer) d[2]);
            s.setVenueLabel((String) d[3]);
            s.setIndoor((Boolean) d[4]);
            sports.add(s);
        }
        sportRepository.saveAll(sports);
    }

    private void seedCities() {
        if (cityRepository.count() > 0)
            return;

        List<City> cities = new ArrayList<>();
        for (Object[] d : CITIES) {
            City c = new City();
            c.setName((String) d[0]);
            c.setLatitude((Double) d[1]);
            c.setLongitude((Double) d[2]);
            cities.add(c);
        }
        cityRepository.saveAll(cities);
    }

    // Câte un teren pe fiecare zonă (3), pentru fiecare oraș și sport.
    private void seedFields() {
        if (fieldRepository.count() > 0)
            return;

        List<Sport> sports = sportRepository.findAll();
        List<Field> fields = new ArrayList<>();
        for (City city : cityRepository.findAll()) {
            for (Sport sport : sports) {
                for (String district : DISTRICTS) {
                    Field f = new Field();
                    f.setCityId(city.getId());
                    f.setSportId(sport.getId());
                    f.setName(sport.getVenueLabel() + " – " + district);
                    f.setDistrict(district);
                    fields.add(f);
                }
            }
        }
        fieldRepository.saveAll(fields);
    }
}
