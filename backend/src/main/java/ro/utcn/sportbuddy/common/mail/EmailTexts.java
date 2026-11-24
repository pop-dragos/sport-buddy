package ro.utcn.sportbuddy.common.mail;

import ro.utcn.sportbuddy.common.i18n.Language;

// Textele emailurilor, în limba cererii care le-a declanșat (cea aleasă în aplicație).
// Linkul și butonul se completează la trimitere (MailService.sendLink); aici sunt doar cuvintele.
public final class EmailTexts {

    public record Text(String subject, String greeting, String body, String button, String footer) {}

    private EmailTexts() {}

    private static boolean en(Language language) {
        return language == Language.EN;
    }

    public static Text verifyEmail(Language language, String fullName) {
        if (en(language)) {
            return new Text("Confirm your SportBuddy account", "Welcome, " + fullName + "!",
                    "One last step: confirm your email address so you can use your account. "
                            + "The link is valid for 24 hours.",
                    "Confirm email",
                    "If you didn't create a SportBuddy account, ignore this email.");
        }
        return new Text("Confirmă-ți contul SportBuddy", "Bun venit, " + fullName + "!",
                "Mai ai un singur pas: confirmă adresa de email ca să-ți poți folosi contul. "
                        + "Linkul este valabil 24 de ore.",
                "Confirmă emailul",
                "Dacă nu ți-ai creat tu un cont SportBuddy, ignoră acest email.");
    }

    public static Text resetPassword(Language language, String fullName) {
        if (en(language)) {
            return new Text("SportBuddy password reset", "Hi, " + fullName + "!",
                    "We received a request to reset the password of your account. "
                            + "The link is valid for one hour and can be used only once.",
                    "Choose a new password",
                    "If you didn't request this, ignore this email: your password stays unchanged.");
        }
        return new Text("Resetarea parolei SportBuddy", "Salut, " + fullName + "!",
                "Am primit o cerere de resetare a parolei pentru contul tău. "
                        + "Linkul este valabil o oră și poate fi folosit o singură dată.",
                "Alege o parolă nouă",
                "Dacă nu ai cerut tu resetarea, ignoră acest email: parola rămâne neschimbată.");
    }

    // trimis la adresa NOUĂ, cu linkul de confirmare
    public static Text confirmNewEmail(Language language, String fullName) {
        if (en(language)) {
            return new Text("Confirm your new SportBuddy email", "Hi, " + fullName + "!",
                    "You asked for this to be the new email of your SportBuddy account. "
                            + "Confirm it with the link below, valid for 24 hours.",
                    "Confirm new email",
                    "If you didn't request this change, ignore this email: your account stays on the old address.");
        }
        return new Text("Confirmă noul email SportBuddy", "Salut, " + fullName + "!",
                "Ai cerut ca acesta să fie noul email al contului tău SportBuddy. "
                        + "Confirmă-l din linkul de mai jos, valabil 24 de ore.",
                "Confirmă emailul nou",
                "Dacă nu ai cerut tu schimbarea, ignoră acest email: contul rămâne pe adresa veche.");
    }

    // trimis la adresa VECHE, ca avertizare (fără link)
    public static Text emailChangeWarning(Language language, String fullName, String newEmail) {
        if (en(language)) {
            return new Text("Your SportBuddy account email is changing", "Hi, " + fullName + "!",
                    "Someone asked to change your SportBuddy account email to " + newEmail + ". "
                            + "The change only happens after confirmation from the link sent to the new address.",
                    null,
                    "If this wasn't you, sign in, cancel the change in Settings and change your password.");
        }
        return new Text("Emailul contului SportBuddy se schimbă", "Salut, " + fullName + "!",
                "Cineva a cerut schimbarea emailului contului tău SportBuddy în " + newEmail + ". "
                        + "Schimbarea se face abia după confirmarea din linkul trimis la adresa nouă.",
                null,
                "Dacă nu ai fost tu, intră în cont, anulează schimbarea din Setări și schimbă-ți parola.");
    }
}
