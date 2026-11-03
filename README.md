# 📺 YouTube-UADS (Unadsen)-V2.0

> **Bản phối lại (Remix / Enhanced Fork)** từ dự án gốc mã nguồn mở [**YouTube-NADS**](https://github.com/letamphong628/Youtube-NADS) do tác giả **Letamphong628** thực hiện.

[<img src="https://img.shields.io/badge/Version-UADS--V2.0-blue.svg">](https://github.com/letamphong628/Youtube-NADS)
[<img src="https://img.shields.io/badge/Type-Remix%20%2F%20Ph%E1%BB%91i%20l%E1%BA%A1i-purple.svg">](https://github.com/letamphong628/Youtube-NADS)
[<img src="https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg">](https://github.com/letamphong628/Youtube-NADS)
[<img src="https://img.shields.io/badge/Base%20Project-YouTube--NADS%20by%20Letamphong628-orange.svg">](https://github.com/letamphong628/Youtube-NADS)

---

## 🇻🇳 Tiếng Việt

### 📖 Giới thiệu
**YouTube-UADS (Unadsen)-V2.0** là phiên bản phối lại và nâng cấp mạnh mẽ từ dự án gốc **YouTube-NADS** của tác giả **Letamphong628**. Dự án kế thừa ý tưởng tinh gọn ban đầu, đồng thời tái cấu trúc mã nguồn để mang lại một ứng dụng khách YouTube hoàn thiện, chặn đứng mọi loại quảng cáo, hỗ trợ phát ngầm chống dừng và mang lại trải nghiệm xem video mượt mà chuẩn hiện đại.

### 🌟 Nguồn gốc & Tuyên bố bản quyền (Credits & Attribution)
* **Dự án gốc:** [YouTube-NADS](https://github.com/letamphong628/Youtube-NADS) (v1.1)
* **Tác giả gốc:** [Letamphong628](https://github.com/letamphong628) *(Discord: 07.04.04)*
* **Bản phối lại:** **YouTube-UADS (Unadsen) - V2.0**
* **Mục tiêu phối lại:** Nâng cấp kiến trúc, bổ sung chặn quảng cáo 2 tầng (Two-Tier AdBlock), tích hợp SponsorBlock, sửa triệt để lỗi phóng to toàn màn hình và công nghệ chống dừng phát âm thanh trong nền.

---

### ✨ Các tính năng nâng cấp nổi bật

1. **🛡️ Cơ chế chặn quảng cáo 2 tầng (Two-Tier Ultra AdBlock):**
   * **Chặn tầng mạng (`shouldInterceptRequest`):** Ngăn chặn các kết nối tới server quảng cáo (`googleads`, `doubleclick`, `pagead`, `log_event`...) ngay trước khi dữ liệu được tải về máy, giúp tiết kiệm dung lượng 4G/5G.
   * **Chặn tầng giao diện (DOM 16x Fast-Forward):** Tự động tắt tiếng (Mute) ngay lập tức khi phát hiện quảng cáo video, tăng tốc độ phát lên 16x và tua thẳng về cuối để tự động bấm nút *Bỏ qua quảng cáo* (Skip Ad).
   * Tự động ẩn sạch banner rác, popup mời mua YouTube Premium và gợi ý cài app.

2. **⚡ Tích hợp SponsorBlock (Tự động bỏ qua đoạn tài trợ):**
   * Tự động kết nối API SponsorBlock cộng đồng để nhảy qua các phân đoạn quảng cáo của nhà tài trợ (sponsor), lời kêu gọi đăng ký kênh (selfpromo), đoạn intro và outro dài dòng.

3. **🖥️ Sửa lỗi Toàn màn hình (HTML5 Fullscreen CustomView):**
   * Triển khai đầy đủ `onShowCustomView` và `onHideCustomView` trong WebChromeClient.
   * Tự động ẩn thanh công cụ, kích hoạt chế độ **Immersive Fullscreen** và xoay ngang màn hình cảm biến (`SENSOR_LANDSCAPE`).

4. **🎵 Công nghệ chống dừng phát ngầm (Anti-Pause Background Engine):**
   * Can thiệp vào Page Visibility API (`document.visibilityState`, `visibilitychange`).
   * Ngăn YouTube phát hiện người dùng chuyển ứng dụng hoặc tắt màn hình, đảm bảo âm thanh/nhạc/podcast tiếp tục phát liên tục 100% khi màn hình khóa.

5. **⚡ Điều chỉnh tốc độ phát nhanh (Speed Selector):**
   * Nút bấm tốc độ trực tiếp trên thanh công cụ Header với 9 mức: `0.5x`, `0.75x`, `1.0x`, `1.25x`, `1.5x`, `1.75x`, `2.0x`, `2.5x`, `3.0x`.

6. **📥 Trung tâm Tải xuống & Chia sẻ link sạch (Clean URL & Downloader):**
   * Sao chép liên kết sạch: Tự động loại bỏ các mã theo dõi rác (`si=`, `feature=...`), rút gọn về định dạng chuẩn `https://youtu.be/...`.
   * Hỗ trợ chuyển tiếp video sang các công cụ tải chuyên dụng (Seal, NewPipe, YTDLnis, 1DM) hoặc mở nhanh qua Cobalt web downloader.

7. **🔗 Hỗ trợ Deep-Link Intent:**
   * Tự động bắt link YouTube (`m.youtube.com`, `youtube.com`, `youtu.be`) khi bấm vào liên kết từ Messenger, Zalo, Chrome, Telegram...

8. **🎨 Giao diện AMOLED Pitch Black & Liquid Glass:**
   * Nền đen sâu `#000000` tiết kiệm pin màn hình OLED/AMOLED.
   * Thanh điều hướng nổi bo tròn phong cách kính mờ (Liquid Glass).
   * Cơ chế chống thoát nhầm: Nhấn phím Back 2 lần trong 2 giây để thoát app.

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

### 📖 Introduction
**YouTube-UADS (Unadsen)-V2.0** is an enhanced remix edition forked from the open-source project **YouTube-NADS** originally created by **Letamphong628**. It preserves the lightweight nature of the original while adding Two-Tier Ad-blocking, SponsorBlock integration, fixed HTML5 video fullscreen, Anti-Pause background audio, deep-linking, quick speed controls, and a sleek AMOLED Liquid Glass UI.

### 🌟 Credits & Attribution
* **Base Project:** [YouTube-NADS](https://github.com/letamphong628/Youtube-NADS)
* **Original Author:** [Letamphong628](https://github.com/letamphong628) *(Discord: 07.04.04)*
* **Remixed Edition:** **YouTube-UADS (Unadsen)-V2.0**

---

*Remixed with ❤️ based on YouTube-NADS by Letamphong628.*
