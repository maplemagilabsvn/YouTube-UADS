#!/usr/bin/env python3
"""
YouTube UADS (Unadsen) v2.0 - Quick Build Script for Termux & CLI Android Environment
Builds and packages YouTube_UADS_v2.0.apk without requiring full Gradle.
Prerequisites in Termux: pkg install openjdk-17 aapt2 android-tools
"""

import os
import subprocess
import sys

def run_cmd(cmd):
    print(f"[CMD] {cmd}")
    res = subprocess.run(cmd, shell=True)
    if res.returncode != 0:
        print(f"[ERROR] Command failed with code {res.returncode}")
        sys.exit(res.returncode)

def main():
    print("=== BẮT ĐẦU QUÁ TRÌNH BIÊN DỊCH YOUTUBE NADS PRO (v2.0) ===")
    
    os.makedirs("build/res", exist_ok=True)
    os.makedirs("build/gen", exist_ok=True)
    os.makedirs("build/obj", exist_ok=True)
    
    # 1. Compile resources with aapt2 if available
    android_jar = os.environ.get("ANDROID_JAR", "/data/data/com.termux/files/usr/share/aapt/android.jar")
    if not os.path.exists(android_jar) and os.path.exists("android.jar"):
        android_jar = "android.jar"
        
    print(f"[*] Sử dụng Android Framework JAR: {android_jar}")

    # 2. Sinh file R.java từ AndroidManifest & res
    if os.path.exists(android_jar):
        run_cmd(f"aapt package -f -m -J build/gen -M app/src/main/AndroidManifest.xml -S app/src/main/res -I {android_jar}")
    
    # 3. Biên dịch mã nguồn Java
    java_files = [
        "app/src/main/java/com/nads/youtube/MainActivity.java",
        "app/src/main/java/com/nads/youtube/LoginActivity.java",
        "app/src/main/java/com/nads/youtube/R.java"
    ]
    
    if os.path.exists(android_jar):
        files_str = " ".join(java_files)
        run_cmd(f"javac -d build/obj -cp {android_jar} {files_str}")
        run_cmd(f"d8 --output build/ build/obj/com/nads/youtube/*.class")
        run_cmd(f"aapt package -f -M app/src/main/AndroidManifest.xml -S app/src/main/res -I {android_jar} -F build/unsigned.apk build/")
        
        # Ký APK bằng khóa trong mykey
        if os.path.exists("mykey/key.pk8") and os.path.exists("mykey/cert.pem"):
            run_cmd("apksigner sign --key mykey/key.pk8 --cert mykey/cert.pem --out YouTube_UADS_v2.0.apk build/unsigned.apk")
            print("[✓] ĐÃ TẠO VÀ KÝ THÀNH CÔNG: YouTube_UADS_v2.0.apk")
    else:
        print("[!] Không tìm thấy android.jar. Vui lòng build bằng Android Studio thông qua file build.gradle hoặc tải android-sdk trong Termux.")

if __name__ == "__main__":
    main()
