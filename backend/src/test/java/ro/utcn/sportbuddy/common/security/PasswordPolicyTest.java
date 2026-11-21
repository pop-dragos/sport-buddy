package ro.utcn.sportbuddy.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void acceptsPasswordsWithEightCharactersLetterAndDigit() {
        assertTrue(PasswordPolicy.problem("parola12").isEmpty());
        assertTrue(PasswordPolicy.problem("Șnițel2024!").isEmpty());
    }

    @Test
    void rejectsWeakPasswords() {
        assertTrue(PasswordPolicy.problem(null).isPresent());
        assertTrue(PasswordPolicy.problem("abc1234").isPresent());     // 7 caractere
        assertTrue(PasswordPolicy.problem("12345678").isPresent());    // fără literă
        assertTrue(PasswordPolicy.problem("parolaaa").isPresent());    // fără cifră
        assertTrue(PasswordPolicy.problem("a1".repeat(37)).isPresent()); // peste 72
    }
}
