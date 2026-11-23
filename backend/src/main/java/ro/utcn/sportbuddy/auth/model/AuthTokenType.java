package ro.utcn.sportbuddy.auth.model;

public enum AuthTokenType {
    VERIFY_EMAIL,
    RESET_PASSWORD,
    // confirmarea unui email nou (schimbat din Setări); linkul merge la adresa nouă
    CHANGE_EMAIL
}
