package ro.utcn.sportbuddy.common.i18n;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Traducerea mesajelor de eroare. În cod mesajele rămân scrise în română; când cererea vine în engleză
// (antetul Accept-Language), textul se caută aici după forma lui exactă din română.
// Un mesaj lipsit din dicționar rămâne în română, deci o omisiune nu strică nimic, doar se vede.
public final class Messages {

    private static final Map<String, String> EN = new HashMap<>();
    // mesaje cu numere sau nume în ele: șablon (regex) -> text englez cu $1, $2...
    private static final Map<Pattern, String> EN_PATTERNS = new LinkedHashMap<>();

    private Messages() {}

    public static String translate(String ro, Language language) {
        if (ro == null || language == Language.RO) return ro;
        String exact = EN.get(ro);
        if (exact != null) return exact;
        for (Map.Entry<Pattern, String> p : EN_PATTERNS.entrySet()) {
            Matcher m = p.getKey().matcher(ro);
            if (m.matches()) return m.replaceFirst(p.getValue());
        }
        return ro;
    }

    // folosit de testul care verifică acoperirea dicționarului
    public static boolean knows(String ro) {
        if (EN.containsKey(ro)) return true;
        return EN_PATTERNS.keySet().stream().anyMatch(p -> p.matcher(ro).matches());
    }

    private static void add(String ro, String en) {
        EN.put(ro, en);
    }

    private static void pattern(String roRegex, String en) {
        EN_PATTERNS.put(Pattern.compile(roRegex), en);
    }

    static {
        // ---- generale ----
        add("Date invalide.", "Invalid data.");
        add("Corpul cererii este invalid.", "The request body is invalid.");
        add("Adresa nu există.", "This address does not exist.");
        add("Metoda nu este permisă la această adresă.", "This method is not allowed at this address.");
        add("Formatul cererii nu este acceptat.", "The request format is not supported.");
        add("Fișierul este prea mare.", "The file is too large.");
        add("Alege un fișier.", "Choose a file.");
        add("Autentificare necesară.", "Authentication required.");
        add("Nu ai dreptul la această acțiune.", "You are not allowed to do this.");

        // ---- cont, conectare, parolă ----
        add("Introdu username-ul sau emailul.", "Enter your username or email.");
        add("Introdu parola.", "Enter your password.");
        add("Introdu parola nouă.", "Enter the new password.");
        add("Introdu parola curentă.", "Enter your current password.");
        add("Numele este obligatoriu.", "Name is required.");
        add("Emailul este obligatoriu.", "Email is required.");
        add("Email invalid.", "Invalid email.");
        add("Vârsta este obligatorie.", "Age is required.");
        add("Vârsta minimă este 16.", "The minimum age is 16.");
        add("Vârsta maximă este 99.", "The maximum age is 99.");
        add("Alege cel puțin un sport.", "Choose at least one sport.");
        add("Descrierea poate avea maximum 300 de caractere.", "The description can have at most 300 characters.");
        add("Linkul nu este complet.", "The link is incomplete.");
        add("Scrie emailul nou.", "Enter the new email.");
        add("Lipsește răspunsul de la Google.", "The response from Google is missing.");
        add("Furnizor invalid.", "Invalid provider.");
        add("Există deja un cont cu acest email.", "An account with this email already exists.");
        add("Pentru un cont Google folosește butonul „Continuă cu Google”.",
                "For a Google account, use the “Continue with Google” button.");
        add("Înregistrarea cu Google a expirat. Apasă din nou „Continuă cu Google”.",
                "The Google sign-up has expired. Press “Continue with Google” again.");
        add("Poza se poate adăuga din Setări, după conectare.", "You can add the photo from Settings after signing in.");
        add("Utilizator sau parolă incorecte.", "Incorrect username or password.");
        add("Contul nu este confirmat încă. Ți-am trimis pe email linkul de confirmare.",
                "Your account isn't confirmed yet. We've emailed you the confirmation link.");
        add("Conectarea cu Google nu este configurată pe server.", "Google sign-in is not configured on the server.");
        add("Conectarea cu Google nu a reușit. Încearcă din nou.", "Google sign-in failed. Please try again.");
        add("Contul Google nu are un email confirmat.", "This Google account has no confirmed email.");
        add("Folosește butonul „Continuă cu Google”.", "Use the “Continue with Google” button.");
        add("Linkul de confirmare nu mai este valabil. Conectează-te ca să primești altul.",
                "The confirmation link is no longer valid. Sign in to get a new one.");
        add("Linkul de resetare nu mai este valabil. Cere unul nou.",
                "The reset link is no longer valid. Request a new one.");
        add("Dacă există un cont neconfirmat, vei primi un email cu linkul de confirmare.",
                "If there is an unconfirmed account, you'll receive an email with the confirmation link.");
        add("Dacă există un cont, vei primi un email cu linkul de resetare.",
                "If an account exists, you'll receive an email with the reset link.");
        add("Parola a fost schimbată. Te poți conecta cu parola nouă.",
                "Your password has been changed. You can now sign in with the new password.");
        add("Am retrimis linkul de confirmare.", "We've resent the confirmation link.");
        add("Parola trebuie să aibă minimum 8 caractere.", "The password must have at least 8 characters.");
        add("Parola poate avea maximum 72 de caractere.", "The password can have at most 72 characters.");
        add("Parola trebuie să conțină cel puțin o literă.", "The password must contain at least one letter.");
        add("Parola trebuie să conțină cel puțin o cifră.", "The password must contain at least one digit.");
        add("Parola curentă este greșită.", "The current password is wrong.");
        add("Parola nouă trebuie să fie diferită de cea curentă.", "The new password must differ from the current one.");
        pattern("Emailul contului este acum (.+)\\.", "The account email is now $1.");

        // ---- schimbarea emailului ----
        add("Acesta este deja emailul contului.", "This is already the account's email.");
        add("Contul nu există.", "The account does not exist.");
        add("Nu există nicio schimbare de email în așteptare.", "There is no pending email change.");
        add("Linkul de confirmare nu mai este valabil. Cere unul nou din Setări.",
                "The confirmation link is no longer valid. Request a new one from Settings.");

        // ---- profil ----
        add("Utilizatorul nu există.", "The user does not exist.");
        add("Numele nu poate fi gol.", "The name cannot be empty.");
        add("Emailul se schimbă din butonul „Schimbă emailul”, cu confirmare.",
                "Change your email with the “Change email” button, which asks for confirmation.");
        add("Orașul ales nu există.", "The chosen city does not exist.");
        add("Orașul nu există.", "The city does not exist.");
        add("Alege o imagine.", "Choose an image.");
        add("Imaginea poate avea maximum 2 MB.", "The image can be at most 2 MB.");
        add("Sunt acceptate doar imagini PNG, JPG, WebP sau GIF.", "Only PNG, JPG, WebP or GIF images are accepted.");
        add("Sporturile selectate sunt invalide.", "The selected sports are invalid.");
        add("Sportul ales nu există.", "The chosen sport does not exist.");

        // ---- feed ----
        add("Scrie un comentariu.", "Write a comment.");
        add("Comentariul poate avea maximum 500 de caractere.", "The comment can have at most 500 characters.");
        add("Scrie ceva în postare.", "Write something in your post.");
        add("Postarea poate avea maximum 1000 de caractere.", "The post can have at most 1000 characters.");
        add("Alege sportul.", "Choose the sport.");
        add("Alege tipul postării.", "Choose the post type.");
        add("Pagina nu poate fi negativă.", "The page cannot be negative.");
        add("Postarea nu există.", "The post does not exist.");
        add("Notificarea nu există.", "The notification does not exist.");
        add("Textul nu poate fi gol.", "The text cannot be empty.");
        pattern("Mărimea paginii trebuie să fie între 1 și (\\d+)\\.", "Page size must be between 1 and $1.");

        // ---- prieteni și mesaje ----
        add("Scrie un mesaj.", "Write a message.");
        add("Mesajul poate avea maximum 1000 de caractere.", "The message can have at most 1000 characters.");
        add("Mesajul poate avea maximum 500 de caractere.", "The message can have at most 500 characters.");
        add("Alege utilizatorul.", "Choose the user.");
        add("Nu îți poți trimite cerere ție.", "You can't send a request to yourself.");
        add("Sunteți deja prieteni.", "You are already friends.");
        add("Ai trimis deja o cerere.", "You have already sent a request.");
        add("Nu sunteți prieteni.", "You are not friends.");
        add("Cererea nu mai este în așteptare.", "The request is no longer pending.");
        add("Cererea nu există.", "The request does not exist.");
        add("Poți trimite mesaje doar prietenilor.", "You can only message your friends.");
        add("Acest utilizator nu primește cereri de prietenie.", "This user does not accept friend requests.");
        add("Acest utilizator primește cereri doar de la prietenii prietenilor lui.",
                "This user only accepts requests from friends of their friends.");
        add("Lista de prieteni a acestui utilizator nu este vizibilă.", "This user's friend list is not visible.");
        pattern("Cel mult (\\d+) de utilizatori odată\\.", "At most $1 users at once.");

        // ---- lobby-uri ----
        add("Lista de prezență lipsește.", "The attendance list is missing.");
        add("Alege terenul.", "Choose the field.");
        add("Alege data și ora.", "Choose the date and time.");
        add("Alege numărul de locuri.", "Choose the number of spots.");
        add("Lobby-ul trebuie să aibă cel puțin 2 locuri.", "The lobby needs at least 2 spots.");
        add("Lobby-ul poate avea maximum 50 de locuri.", "The lobby can have at most 50 spots.");
        add("Alege minimul de jucători.", "Choose the minimum number of players.");
        add("Minimul de jucători este 2.", "The minimum number of players is 2.");
        add("Spune câți jucători sunt deja.", "Tell us how many players there already are.");
        add("Cel puțin organizatorul joacă.", "At least the organizer is playing.");
        add("Completează costul terenului.", "Enter the field cost.");
        add("Costul nu poate fi negativ.", "The cost cannot be negative.");
        add("Alege jucătorul.", "Choose the player.");
        add("Terenul nu există.", "The field does not exist.");
        add("Terenul ales nu este pentru acest sport.", "The chosen field is not for this sport.");
        add("Meciul trebuie să fie cu cel puțin 3 ore în viitor, iar minutele 00, 15, 30 sau 45.",
                "The match must be at least 3 hours in the future, with minutes 00, 15, 30 or 45.");
        add("Minimul nu poate depăși numărul de locuri.", "The minimum cannot exceed the number of spots.");
        add("Jucătorii existenți nu pot depăși numărul de locuri.",
                "The existing players cannot exceed the number of spots.");
        add("Ești deja în acest lobby.", "You are already in this lobby.");
        add("Termenul de înscriere a trecut.", "The sign-up deadline has passed.");
        add("Lobby-ul s-a încheiat.", "The lobby has ended.");
        add("Lobby-ul s-a încheiat deja.", "The lobby has already ended.");
        add("Prezența se salvează după meci.", "Attendance is saved after the match.");
        add("Prezența a fost deja salvată.", "Attendance has already been saved.");
        add("Confirmă lobby-ul înainte să îl închei.", "Confirm the lobby before you finish it.");
        add("Nu ești în acest lobby.", "You are not in this lobby.");
        add("Lobby-ul nu există.", "The lobby does not exist.");
        add("Cererea a fost deja rezolvată.", "The request has already been resolved.");
        add("Doar organizatorul poate face asta.", "Only the organizer can do that.");
        add("Lobby-ul nu mai primește jucători.", "The lobby is no longer accepting players.");
        add("Lobby-ul este complet.", "The lobby is full.");
        add("Luna trebuie să aibă forma 2026-10.", "The month must look like 2026-10.");
        add("Filtrul poate fi UPCOMING, CREATED, REQUESTS sau FINISHED.",
                "The filter can be UPCOMING, CREATED, REQUESTS or FINISHED.");
        add("Organizatorul nu poate ieși. Poate anula lobby-ul.", "The organizer can't leave. They can cancel the lobby.");
        add("Nu mai există jucători demo care să nu fie în lobby.", "There are no more demo players that aren't in the lobby.");

        // ---- plăți ----
        add("Alege lobby-ul.", "Choose the lobby.");
        add("Completează numărul cardului.", "Enter the card number.");
        add("Completează data expirării.", "Enter the expiry date.");
        add("Completează codul CVC.", "Enter the CVC code.");
        add("Lobby-ul este gratuit.", "This lobby is free.");
        add("Ai plătit deja acest meci.", "You have already paid for this match.");

        add("Doar participanții lobby-ului pot folosi chat-ul.", "Only the lobby's participants can use the chat.");
        add("Plata a fost refuzată. Încearcă alt card.", "The payment was declined. Try another card.");
        add("Plata online e posibilă după confirmarea lobby-ului.",
                "Online payment is possible after the lobby is confirmed.");
        add("Prognoza nu este disponibilă acum.", "The forecast is not available right now.");

        // ---- recomandarea de pe prima pagină (vremea) ----
        add("Plouă. Mai bine baschet sau volei în sală.", "Rain expected. Better go for basketball or volleyball indoors.");
        add("E frig. Echipează-te bine sau alege o sală.", "It's cold. Dress warmly or pick an indoor venue.");
        add("Vreme bună pentru fotbal, tenis și padel afară.", "Good weather for football, tennis and padel outdoors.");

        // ---- mesajele de sistem din chat (se salvează în română, se traduc la afișare) ----
        add("Lobby confirmat.", "Lobby confirmed.");
        add("Lobby-ul a fost anulat.", "The lobby was cancelled.");
        pattern("(.+) s-a înscris\\.", "$1 joined.");
        pattern("(.+) a ieșit din lobby\\.", "$1 left the lobby.");
        pattern("Lobby confirmat\\. Preț final: (.+) RON / persoană\\.", "Lobby confirmed. Final price: $1 RON / person.");

        // ---- review-uri ----
        add("Dă cel puțin o notă.", "Give at least one rating.");
        add("Alege colegul.", "Choose the teammate.");
        add("Alege nota.", "Choose the rating.");
        add("Nota trebuie să fie între 1 și 5.", "The rating must be between 1 and 5.");
        add("Ai trimis deja notele pentru acest meci.", "You have already submitted the ratings for this match.");
        add("Doar cei care au jucat dau note.", "Only players who took part can give ratings.");
        add("Notele se dau după meci.", "Ratings are given after the match.");
        add("Poți nota doar colegii care au venit la meci, câte o dată.",
                "You can only rate teammates who attended the match, once each.");
    }
}
