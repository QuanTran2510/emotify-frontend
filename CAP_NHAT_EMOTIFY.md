# Cập nhật Emotify

Các thay đổi đã thực hiện:

- Đổi màu nhận diện chính của app sang mint/teal `#38D9C6`, tránh dùng màu xanh kiểu Spotify.
- Thay launcher icon Android mặc định bằng icon Emotify riêng: mặt cười + nốt nhạc.
- Thêm icon Google và Facebook ở màn hình đăng nhập.
- Bỏ cảm xúc `Relaxed/Thư giãn` khỏi luồng nhận diện cảm xúc.
- Nhận diện cảm xúc chỉ trả về 3 mood: `happy`, `sad`, `neutral`.
- Home/Search/Player chỉ hiển thị và gợi ý nhạc theo 3 cảm xúc: Vui vẻ, Buồn, Trung lập.
- Thêm ảnh mockup giao diện mới tại `docs/emotify_ui_showcase.png`.

## Cách push lên branch main của repo đã tạo sẵn

Nếu máy bạn đã clone repo rồi:

```bash
cd ten-thu-muc-repo-cua-ban
```

Giải nén file ZIP cập nhật, sau đó copy toàn bộ nội dung thư mục `emotify-frontend` vừa giải nén đè vào repo hiện tại.

Kiểm tra thay đổi:

```bash
git status
git diff
```

Commit và push:

```bash
git add .
git commit -m "Update Emotify UI and remove relaxed mood"
git branch -M main
git push origin main
```

Nếu đây là lần đầu push repo local lên GitHub:

```bash
git remote add origin <URL_REPO_CUA_BAN>
git branch -M main
git push -u origin main
```

Nếu remote `origin` đã tồn tại nhưng sai URL:

```bash
git remote set-url origin <URL_REPO_CUA_BAN>
git push -u origin main
```

## Cập nhật phiên đăng nhập và màn hình Cá nhân

### Đã thêm

- Nếu Firebase vẫn còn `currentUser`, app sẽ tự động vào thẳng màn hình chính thay vì bắt đăng nhập lại.
- Thêm tab `Cá nhân` ở Bottom Navigation.
- Màn hình `Cá nhân` hiển thị:
  - Tên người dùng.
  - Email đăng nhập.
  - Trạng thái tài khoản.
  - Dịch vụ đăng nhập: Email / Google / Facebook.
  - Nút `Đăng xuất`.
- Khi nhấn `Đăng xuất`, app gọi Firebase sign out, Facebook logout, clear Credential Manager và quay lại màn hình đăng nhập.
- Nếu đã đăng xuất mà chưa đăng nhập lại, lần sau mở app sẽ vẫn ở màn hình đăng nhập.

### File được chỉnh/thêm

- `app/src/main/java/com/emotify/ui/navigations/NavGraph.kt`
- `app/src/main/java/com/emotify/ui/navigations/Screen.kt`
- `app/src/main/java/com/emotify/ui/screen/MainScreen.kt`
- `app/src/main/java/com/emotify/ui/screen/profile/ProfileScreen.kt`
