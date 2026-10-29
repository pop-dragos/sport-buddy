package ro.utcn.sportbuddy.ratings.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import ro.utcn.sportbuddy.common.events.AttendanceSavedEvent;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.PlayerCancelledEvent;

// Penalizările și prezența, ca reacție la ce se întâmplă în lobbies (B).
// Rulează doar după ce acțiunea lui B s-a salvat: nu penalizăm o anulare care până la urmă nu a avut loc.
// Fiecare eveniment are tranzacția lui, deci toate modificările lui se salvează împreună sau deloc.
@Component
public class RatingEventListener {

    private final RatingService ratingService;

    public RatingEventListener(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Jucătorul a ieșit după confirmare: review automat de 2 stele.
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerCancelled(PlayerCancelledEvent event) {
        if (event.late()) {
            ratingService.applyLateCancel(event.lobbyId(), event.userId());
        }
    }

    // Organizatorul a anulat după confirmare: aceeași penalizare, pentru el.
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLobbyCancelled(LobbyCancelledEvent event) {
        if (event.late()) {
            ratingService.applyLateCancel(event.lobbyId(), event.hostUserId());
        }
    }

    // Prezența după meci: organizatorul și cei veniți primesc o prezență, absenții review de 1 stea.
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAttendanceSaved(AttendanceSavedEvent event) {
        ratingService.recordAttendance(event.hostUserId());
        event.presentUserIds().forEach(ratingService::recordAttendance);
        event.absentUserIds().forEach(userId -> ratingService.applyNoShow(event.lobbyId(), userId));
    }
}
