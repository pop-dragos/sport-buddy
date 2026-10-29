package ro.utcn.sportbuddy.ratings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.ratings.dto.PendingReviewDto;
import ro.utcn.sportbuddy.ratings.dto.SubmitReviewsRequest.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// LobbyService și RatingService sunt simulate; se verifică doar regulile pentru note.
class ReviewServiceTest {

    private static final Long LOBBY = 55L;
    private static final Long ANA = 1L;      // organizator, a venit
    private static final Long ANDREI = 2L;   // a venit
    private static final Long RADU = 5L;     // absent
    private static final Long IOANA = 3L;    // nu a fost în lobby

    private LobbyService lobbyService;
    private RatingService ratingService;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        lobbyService = mock(LobbyService.class);
        ratingService = mock(RatingService.class);
        reviewService = new ReviewService(lobbyService, ratingService);
    }

    // ---------- getPending ----------

    @Test
    void pendingListsColleaguesWhoCame() {
        finishedLobby();

        List<PendingReviewDto> pending = reviewService.getPending(LOBBY, ANA);

        assertEquals(List.of(new PendingReviewDto(ANDREI, "andrei")), pending); // fără mine, fără radu (absent)
    }

    @Test
    void pendingIsEmptyAfterSubmitting() {
        finishedLobby();
        when(ratingService.hasReviewed(LOBBY, ANA)).thenReturn(true);

        assertTrue(reviewService.getPending(LOBBY, ANA).isEmpty());
    }

    @Test
    void beforeAttendanceEveryoneCountsAsPresent() {
        when(lobbyService.getDetails(LOBBY, ANA)).thenReturn(lobby("FINISHED",
                participant(ANA, "ana", null), participant(ANDREI, "andrei", null), participant(RADU, "radu", null)));

        assertEquals(2, reviewService.getPending(LOBBY, ANA).size());
    }

    // ---------- cine și când ----------

    @Test
    void lobbyMustBeFinished() {
        when(lobbyService.getDetails(LOBBY, ANA)).thenReturn(lobby("LOCKED",
                participant(ANA, "ana", null), participant(ANDREI, "andrei", null)));

        ApiException e = assertThrows(ApiException.class, () -> reviewService.getPending(LOBBY, ANA));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("LOBBY_NOT_FINISHED", e.getCode());
    }

    @Test
    void outsiderCannotReview() {
        when(lobbyService.getDetails(LOBBY, IOANA)).thenReturn(finishedDetails());

        ApiException e = assertThrows(ApiException.class, () -> reviewService.getPending(LOBBY, IOANA));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("NOT_PARTICIPANT", e.getCode());
    }

    @Test
    void absentPlayerCannotReview() {
        when(lobbyService.getDetails(LOBBY, RADU)).thenReturn(finishedDetails());

        ApiException e = assertThrows(ApiException.class,
                () -> reviewService.submit(LOBBY, RADU, List.of(new Item(ANDREI, 4))));
        assertEquals("NOT_PARTICIPANT", e.getCode());
        verify(ratingService, never()).recordReview(anyLong(), anyLong(), anyLong(), anyInt());
    }

    // ---------- submit ----------

    @Test
    void submitRecordsEachReview() {
        when(lobbyService.getDetails(LOBBY, ANDREI)).thenReturn(finishedDetails());

        reviewService.submit(LOBBY, ANDREI, List.of(new Item(ANA, 5)));

        verify(ratingService).recordReview(LOBBY, ANDREI, ANA, 5);
    }

    @Test
    void cannotSubmitTwice() {
        finishedLobby();
        when(ratingService.hasReviewed(LOBBY, ANA)).thenReturn(true);

        ApiException e = assertThrows(ApiException.class,
                () -> reviewService.submit(LOBBY, ANA, List.of(new Item(ANDREI, 4))));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("ALREADY_REVIEWED", e.getCode());
    }

    @Test
    void cannotReviewSelfAbsentOrStranger() {
        finishedLobby();

        for (Long target : List.of(ANA, RADU, IOANA)) {
            ApiException e = assertThrows(ApiException.class,
                    () -> reviewService.submit(LOBBY, ANA, List.of(new Item(target, 4))));
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        }
        verify(ratingService, never()).recordReview(anyLong(), anyLong(), anyLong(), anyInt());
    }

    @Test
    void sameColleagueTwiceIsRejectedAndNothingIsSaved() {
        finishedLobby();

        assertThrows(ApiException.class,
                () -> reviewService.submit(LOBBY, ANA, List.of(new Item(ANDREI, 4), new Item(ANDREI, 5))));
        verify(ratingService, never()).recordReview(anyLong(), anyLong(), anyLong(), anyInt());
    }

    // ---------- ajutătoare ----------

    private void finishedLobby() {
        when(lobbyService.getDetails(LOBBY, ANA)).thenReturn(finishedDetails());
    }

    private static LobbyDetails finishedDetails() {
        return lobby("FINISHED",
                participant(ANA, "ana", true), participant(ANDREI, "andrei", true), participant(RADU, "radu", false));
    }

    private static LobbyDetails.Participant participant(Long id, String username, Boolean attended) {
        return new LobbyDetails.Participant(id, username, id.equals(ANA), null, 0, null, null, attended);
    }

    private static LobbyDetails lobby(String status, LobbyDetails.Participant... participants) {
        return new LobbyDetails(LOBBY, null, null, null, null, status, null,
                10, 6, 0, participants.length, 10 - participants.length, null, 0, null, null, null, null,
                false, false, "ACCEPTED", false, List.of(participants), List.of());
    }
}
