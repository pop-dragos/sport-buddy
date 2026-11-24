package ro.utcn.sportbuddy.inbox.dto;

import java.time.LocalDateTime;

// Un rând din lista de conversații.
// type: DIRECT (cu o persoană; id = id-ul ei) sau GROUP (chat-ul unui lobby; id = id-ul lobby-ului).
// title: numele persoanei sau, la grup, sportul lobby-ului; place: terenul (doar la grup); lobbyAt: când se joacă.
// lastSender: la grup, cine a scris ultimul mesaj (gol la mesajele de sistem); la conversația directă rămâne gol.
public record ConversationDto(String type, Long id, String title, String place, String avatarUrl, String sportCode,
                              LocalDateTime lobbyAt, String lastSender, String lastMessage, boolean lastFromMe,
                              LocalDateTime sentAt, int unread) {}
