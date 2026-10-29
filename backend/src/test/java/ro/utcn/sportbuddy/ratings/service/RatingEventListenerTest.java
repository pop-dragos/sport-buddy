package ro.utcn.sportbuddy.ratings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.common.events.AttendanceSavedEvent;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.PlayerCancelledEvent;

import java.util.List;

import static org.mockito.Mockito.*;

// Se verifică doar ce metode din RatingService se apelează; calculele au testele lor.
class RatingEventListenerTest {

    private static final Long LOBBY = 55L;

    private RatingService ratingService;
    private RatingEventListener listener;

    @BeforeEach
    void setUp() {
        ratingService = mock(RatingService.class);
        listener = new RatingEventListener(ratingService);
    }

    @Test
    void lateLeavePenalizesThePlayer() {
        listener.onPlayerCancelled(new PlayerCancelledEvent(LOBBY, 21L, true));

        verify(ratingService).applyLateCancel(LOBBY, 21L);
    }

    @Test
    void leavingWhileOpenIsFree() {
        listener.onPlayerCancelled(new PlayerCancelledEvent(LOBBY, 21L, false));

        verifyNoInteractions(ratingService);
    }

    @Test
    void lateCancelPenalizesTheHost() {
        listener.onLobbyCancelled(new LobbyCancelledEvent(LOBBY, 7L, true));

        verify(ratingService).applyLateCancel(LOBBY, 7L);
    }

    @Test
    void cancellingWhileOpenIsFree() {
        listener.onLobbyCancelled(new LobbyCancelledEvent(LOBBY, 7L, false));

        verifyNoInteractions(ratingService);
    }

    @Test
    void attendanceCountsHostAndPresentAndPenalizesAbsent() {
        listener.onAttendanceSaved(new AttendanceSavedEvent(LOBBY, 7L, List.of(21L, 22L), List.of(33L)));

        verify(ratingService).recordAttendance(7L);
        verify(ratingService).recordAttendance(21L);
        verify(ratingService).recordAttendance(22L);
        verify(ratingService).applyNoShow(LOBBY, 33L);
        verify(ratingService, never()).recordAttendance(33L);
        verify(ratingService, never()).applyLateCancel(anyLong(), anyLong());
    }

    @Test
    void attendanceWithEveryonePresentGivesNoPenalty() {
        listener.onAttendanceSaved(new AttendanceSavedEvent(LOBBY, 7L, List.of(21L), List.of()));

        verify(ratingService, times(2)).recordAttendance(anyLong());
        verify(ratingService, never()).applyNoShow(anyLong(), anyLong());
    }
}
