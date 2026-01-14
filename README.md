![Build Status](https://github.com/robdeas/musical_note_timing_player/actions/workflows/release.yml/badge.svg)
# 🎵 Musical Note Timing Player

![Build Status](https://github.com/robdeas/musical_note_timing_player/actions/workflows/release.yml/badge.svg)
[![Latest Release](https://img.shields.io/github/v/release/robdeas/musical_note_timing_player?label=Download%20APK)](https://github.com/robdeas/musical_note_timing_player/releases/latest)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A lightweight Android application designed to help musicians practice their timing and rhythm by playing specific musical notes at adjustable intervals.


## 🚀 Features

* **Adjustable Timing:** Fine-tune the interval between notes to match your practice speed.
* **Note Selection:** Practice with different musical notes (Current version: v0.1.0).
* **Open Source:** Licensed under GPL-3.0 for community improvement.

## 🛠 Built With

* **Kotlin** - Primary programming language.
* **Jetpack Compose** - For a modern, reactive UI.
* **GitHub Actions** - Automated CI/CD pipeline for building and signing releases.

## 📦 Installation

To get the app on your Android device:

1. Go to the [Releases](https://github.com/robdeas/musical_note_timing_player/releases) page.
2. Download the `app-release-final.apk` file.
3. Open the file on your Android device and select **Install**.
   * *Note: You may need to allow "Install from Unknown Sources" in your device settings.*

## ⚙️ Development & Automation

This project utilizes a fully automated release pipeline. 

* **CI/CD:** Every version tag (`v*`) pushed to the repository triggers a GitHub Actions workflow.
* **Build Process:** The workflow automatically sets up the Java environment, builds the release APK, and signs it using a secure keystore stored in GitHub Secrets.
* **Deployment:** Once built, the signed APK is automatically attached to a new GitHub Release.

### Local Setup
To build the project locally:
```bash
git clone [https://github.com/robdeas/musical_note_timing_player.git](https://github.com/robdeas/musical_note_timing_player.git)
cd musical_note_timing_player
./gradlew assembleDebug

🤝 Contributing
Contributions are welcome!

Fork the Project.

Create your Feature Branch (git checkout -b feature/AmazingFeature).

Commit your Changes (git commit -m 'Add some AmazingFeature').

Push to the Branch (git push origin feature/AmazingFeature).

Open a Pull Request.

📄 License
Distributed under the GNU General Public License v3.0. See LICENSE for more information.

Created by Rob Deas
