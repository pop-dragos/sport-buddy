package ro.utcn.sportbuddy.ratings.service;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.dto.TopPlayerDto;
import ro.utcn.sportbuddy.ratings.model.Review;
import ro.utcn.sportbuddy.ratings.model.ReviewType;
import ro.utcn.sportbuddy.ratings.model.UserRatingStats;
import ro.utcn.sportbuddy.ratings.repository.ReviewRepository;
import ro.utcn.sportbuddy.ratings.repository.UserRatingStatsRepository;
import ro.utcn.sportbuddy.users.dto.PublicProfile;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.List;

@Service
public class RatingService {

    // „Top jucători" ia în calcul doar cine are cel puțin atâtea review-uri (docs/api.md)
    public static final int TOP_MIN_REVIEWS = 5;

    private final UserRatingStatsRepository statsRepository;
    private final ReviewRepository reviewRepository;
    private final RatingCalculator calculator;
    private final UserService userService;

    public RatingService(UserRatingStatsRepository statsRepository, ReviewRepository reviewRepository,
                         RatingCalculator calculator, UserService userService) {
        this.statsRepository = statsRepository;
        this.reviewRepository = reviewRepository;
        this.calculator = calculator;
        this.userService = userService;
    }

    // ---------- citire ----------

    // Folosit și de alte module (lobbies, dashboard). Cine nu are încă statistici e „nou".
    public RatingStatsDto getStats(Long userId) {
        return statsRepository.findById(userId)
                .map(this::toDto)
                .orElseGet(() -> RatingStatsDto.newUser(userId));
    }

    // Notele se trimit o singură dată pe lobby. Folosit de ReviewService și de contorul „De făcut" (lobbies).
    public boolean hasReviewed(Long lobbyId, Long userId) {
        return reviewRepository.existsByLobbyIdAndReviewerIdAndType(lobbyId, userId, ReviewType.NORMAL);
    }

    // Pentru dashboard. Conturile șterse (inactive) nu apar.
    public List<TopPlayerDto> getTopPlayers(int limit) {
        return statsRepository
                .findByReviewCountGreaterThanEqualOrderByAvgRatingDescReviewCountDesc(TOP_MIN_REVIEWS, Limit.of(limit))
                .stream()
                .filter(stats -> userService.isActive(stats.getUserId()))
                .map(stats -> {
                    PublicProfile profile = userService.getPublicProfile(stats.getUserId());
                    return new TopPlayerDto(stats.getUserId(), profile.username(), profile.avatarUrl(),
                            round(stats.getAvgRating()), stats.getReviewCount());
                })
                .toList();
    }

    // ---------- scriere ----------
    // Apelate de endpoint-ul de review-uri și de ascultătorii evenimentelor din lobbies (când apar).

    // Nota dată de un coleg după meci.
    @Transactional
    public void recordReview(Long lobbyId, Long reviewerId, Long targetUserId, int stars) {
        addReview(lobbyId, reviewerId, targetUserId, ReviewType.NORMAL, stars);
    }

    // Jucătorul (sau organizatorul) a anulat după confirmare: review automat de 2 stele.
    @Transactional
    public void applyLateCancel(Long lobbyId, Long userId) {
        UserRatingStats stats = addReview(lobbyId, null, userId, ReviewType.LATE_CANCEL,
                RatingCalculator.LATE_CANCEL_STARS);
        stats.setLateCancelCount(stats.getLateCancelCount() + 1);
    }

    // Jucătorul a fost bifat absent: review automat de 1 stea.
    @Transactional
    public void applyNoShow(Long lobbyId, Long userId) {
        UserRatingStats stats = addReview(lobbyId, null, userId, ReviewType.NO_SHOW,
                RatingCalculator.NO_SHOW_STARS);
        stats.setNoShowCount(stats.getNoShowCount() + 1);
    }

    // Jucătorul a venit la meci; contează doar pentru fiabilitate, nu pentru medie.
    @Transactional
    public void recordAttendance(Long userId) {
        UserRatingStats stats = statsOf(userId);
        stats.setAttendedCount(stats.getAttendedCount() + 1);
        statsRepository.save(stats);
    }

    // Salvează review-ul și actualizează media. Calculul vine primul, ca o notă invalidă să nu salveze nimic.
    private UserRatingStats addReview(Long lobbyId, Long reviewerId, Long targetUserId, ReviewType type, int stars) {
        UserRatingStats stats = statsOf(targetUserId);
        double newAverage = calculator.addReview(stats.getAvgRating(), stats.getReviewCount(), stars);

        reviewRepository.save(new Review(lobbyId, reviewerId, targetUserId, type, stars));
        stats.setAvgRating(newAverage);
        stats.setReviewCount(stats.getReviewCount() + 1);
        return statsRepository.save(stats);
    }

    private UserRatingStats statsOf(Long userId) {
        return statsRepository.findById(userId).orElseGet(() -> new UserRatingStats(userId));
    }

    private RatingStatsDto toDto(UserRatingStats stats) {
        Integer reliability = calculator.reliabilityPercent(
                stats.getAttendedCount(), stats.getLateCancelCount(), stats.getNoShowCount());
        // Etichetele nu au încă tabelă în entitati.md; până se decide, lista e goală.
        return new RatingStatsDto(stats.getUserId(), round(stats.getAvgRating()), stats.getReviewCount(),
                reliability, List.of());
    }

    private static Double round(Double average) {
        return average == null ? null : Math.round(average * 10) / 10.0;
    }
}
