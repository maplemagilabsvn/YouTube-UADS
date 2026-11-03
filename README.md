# 📺 YouTube NADS Pro (v2.0) - Enhanced Fork

[<img src="https://img.shields.io/badge/Version-2.0--PRO-blue.svg">](https://github.com/letamphong628/Youtube-NADS)
[<img src="https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg">](https://github.com/letamphong628/Youtube-NADS)
[<img src="https://img.shields.io/badge/Build-Android%20Studio%20%7C%20Termux-purple.svg">](https://github.com/letamphong628/Youtube-NADS)
[<img src="https://img.shields.io/badge/License-Open%20Source-orange.svg">](https://github.com/letamphong628/Youtube-NADS)

---

## 🇻🇳 Tiếng Việt

### 🚀 Giới thiệu phiên bản Pro (v2.0)
**YouTube NADS Pro** là phiên bản nâng cấp toàn diện (Enhanced Fork) từ dự án gốc YouTube NADS. Ứng dụng khắc phục triệt để các hạn chế của phiên bản cũ, bổ sung cơ chế chặn quảng cáo 2 tầng (Two-Tier Adblock), tích hợp **SponsorBlock**, sửa lỗi toàn màn hình (Fullscreen), chống dừng phát khi tắt màn hình (Anti-Pause Background Audio) và hỗ trợ mở link YouTube trực tiếp từ các ứng dụng khác.

---

### ✨ Những nâng cấp đột phá so với bản v1.1

1. **🛡️ Chặn quảng cáo 2 tầng (Two-Tier Ultra AdBlock):**
   * **Tầng mạng (Network Interception):** Tự động chặn các yêu cầu gửi đến server quảng cáo (`googleads`, `doubleclick`, `pagead`, `log_event`...) ngay trước khi dữ liệu được tải về, giúp tiết kiệm dung lượng mạng di động (4G/5G).
   * **Tầng giao diện (DOM Fast-Forward):** Nếu quảng cáo video lọt qua, hệ thống lập tức tắt tiếng (Mute), tăng tốc độ phát lên 16x và tua thẳng đến cuối để tự động bấm nút *Bỏ qua quảng cáo* (Skip Ad) trong tích tắc.
   * Tự động ẩn sạch banner rác, popup cài ứng dụng và thông báo mời mua YouTube Premium.

2. **⚡ Tích hợp SponsorBlock (Tự động bỏ qua đoạn tài trợ):**
   * Kết nối trực tiếp với API cộng đồng SponsorBlock.
   * Tự động nhận diện và nhảy qua các đoạn quảng cáo tài trợ (sponsor), lời kêu gọi đăng ký (selfpromo), intro và outro dài dòng trong video.

3. **🖥️ Sửa lỗi Toàn màn hình (HTML5 Fullscreen CustomView):**
   * Tích hợp xử lý `onShowCustomView` và `onHideCustomView` trong WebChromeClient.
   * Tự động ẩn thanh Header và Navigation Bar, khóa xoay ngang cảm ứng (Sensor Landscape) và kích hoạt chế độ Immersive Fullscreen mượt mà.

4. **🎵 Công nghệ chống dừng phát ngầm (Anti-Pause Background Engine):**
   * Hook đè lên `document.visibilityState` và `visibilitychange` của trình duyệt.
   * Ngăn YouTube phát hiện người dùng chuyển ứng dụng hoặc tắt màn hình, đảm bảo âm thanh/nhạc/podcast tiếp tục phát liên tục mà không bao giờ bị dừng giữa chừng.

5. **⚡ Điều chỉnh tốc độ phát nhanh (Speed Selector):**
   * Nút bấm tốc độ trực tiếp trên thanh công cụ Header với 9 mức: `0.5x`, `0.75x`, `1.0x`, `1.25x`, `1.5x`, `1.75x`, `2.0x`, `2.5x`, `3.0x`.

6. **📥 Trung tâm Tải xuống & Chia sẻ link sạch (Clean URL & Downloader):**
   * Sao chép liên kết sạch: Tự động loại bỏ các mã theo dõi rác (`si=`, `feature=...`), tạo link rút gọn `https://youtu.be/...`.
   * Hỗ trợ bắn link sang các ứng dụng tải video chuyên dụng (Seal, NewPipe, YTDLnis, 1DM) hoặc mở nhanh qua Cobalt web downloader.

7. **🔗 Hỗ trợ Deep-Link Intent:**
   * Tự động bắt link YouTube (`m.youtube.com`, `youtube.com`, `youtu.be`) khi bấm vào liên kết từ Messenger, Zalo, Chrome, Telegram...

8. **🎨 Giao diện AMOLED Pitch Black & Liquid Glass:**
   * Nền đen sâu `#000000` tiết kiệm pin màn hình OLED/AMOLED.
   * Thanh điều hướng nổi bo tròn phong cách kính mờ (Liquid Glass).
   * Cơ chế chống thoát nhầm: Nhấn nút Back 2 lần trong 2 giây để thoát app.

9. **🛠️ Cấu trúc chuẩn hóa Android Studio & Termux:**
   * Bổ sung đầy đủ `settings.gradle`, `build.gradle`, `app/build.gradle` (SDK 34).
   * Kèm script `build_termux.py` cho người dùng muốn biên dịch trên môi trường dòng lệnh Termux.

---

### 🛠️ Hướng dẫn Biên dịch & Cài đặt

#### Cách 1: Sử dụng Android Studio (Khuyên dùng)
1. Mở Android Studio -> Chọn **Open Project** -> Trỏ tới thư mục dự án.
2. Đợi Gradle đồng bộ và bấm **Run** hoặc **Build APK** (`Build > Build Bundle(s) / APK(s) > Build APK(s)`).

#### Cách 2: Biên dịch qua Termux trên điện thoại
```bash
pkg update && pkg install openjdk-17 aapt2 android-tools python
python build_termux.py
```

---

## 🇬🇧 English

### 🚀 About YouTube NADS Pro (v2.0)
**YouTube NADS Pro** is an advanced fork of YouTube NADS. It features Two-Tier Ad-blocking, SponsorBlock integration, fixed HTML5 video fullscreen, Anti-Pause background audio, deep-linking, quick speed controls, and a sleek AMOLED Liquid Glass UI.

### ✨ Key Features
* **Two-Tier Ultra AdBlock:** Network-level ad URL blocking + DOM 16x fast-forward auto-skipper.
* **SponsorBlock Support:** Automatically skips in-video sponsorships, intros, and self-promotion segments.
* **HTML5 Fullscreen Support:** Smooth immersive fullscreen transition with auto-sensor rotation.
* **Anti-Pause Background Audio:** Page Visibility API hook keeps playback active when screen is locked.
* **Quick Speed Switcher:** 0.5x to 3.0x playback rates accessible directly from the header bar.
* **Download & Clean Share:** Strips tracking parameters and routes URLs to external downloaders (Seal, NewPipe, Cobalt).
* **Deep Linking:** Opens `youtube.com` and `youtu.be` links natively from other apps.
* **AMOLED Dark Mode & Liquid Glass UI:** Battery-saving pitch black theme with floating rounded bottom bar.
* **Gradle & Termux Ready:** Fully compatible with Android Studio and lightweight Termux CLI builds.

---

*Forked & Enhanced based on YouTube NADS by Letamphong628.*
