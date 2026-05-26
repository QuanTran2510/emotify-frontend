# Emotify API Final

Bản này được chỉnh theo API mới:

+ `POST /auth` để đồng bộ user sau khi đăng nhập Firebase
+ `GET /profile` và `PATCH /profile` cho màn hình cá nhân
+ `GET /library` cho tab Thư viện
+ `POST /favorites`, `DELETE /favorites`, `GET /favorites` cho Yêu thích
+ `POST /recently-played`, `GET /recently-played` cho lịch sử nghe
+ `POST /api/playlists`, `GET /api/playlists`, `GET /api/playlists/:playlistId`, `PATCH /api/playlists/add-song`, `PATCH /api/playlists/remove-song`, `PATCH /api/playlists/rename`, `DELETE /api/playlists/:playlistId` cho Playlist
+ `GET /api/songs/home`, `GET /api/songs/search`, `GET /api/songs/recommended`, `POST /api/songs/next-recommended`, `GET /api/songs/:songId` cho bài hát và gợi ý

Các điểm đã sửa:

+ Không dùng local storage cho library, playlist, favorites, recentlyPlayed
+ Google Sign-In dùng đúng Credential Manager với CustomCredential
+ Favorites hoạt động qua API thật
+ Playlist hoạt động qua API thật
+ Profile lấy và cập nhật qua API thật
+ Search gọi API `/api/songs/search`
+ Gợi ý theo mood gọi API `/api/songs/recommended`
+ Recently played ghi qua API `/recently-played`
+ Song/Playlist parser được làm an toàn hơn khi backend trả `artist` dạng chuỗi hoặc mảng, `duration` dạng chuỗi hoặc số, playlist `songs` dạng id hoặc object

Lưu ý vận hành:

+ Không push `local.properties`
+ Không copy `.git`, `.idea`, `.gradle`, `build` từ ZIP vào repo cũ
+ Nếu Google Login lỗi, kiểm tra SHA-1 trong Firebase và tải lại `google-services.json`
+ Nếu API đổi domain, sửa `BASE_URL` trong `RetrofitClient.kt`
