# UFRO Sustentable App

UFRO Sustentable App is a mobile application designed to promote recycling among students at the Universidad de La Frontera. The app incentivizes recycling by awarding points for recycling activities, which can be exchanged for rewards.

## Features

- **Login with Google**: Secure login using Google accounts.
- **Interactive Map**: Find the nearest recycling points on the campus.
- **QR Scanner**: Scan QR codes located at recycling points to register recycling activities.
- **Request History**: View the history of your recycling requests.
- **Rewards Section**: Exchange your accumulated points for various rewards.
- **User Profile**: Manage your profile and customize the app’s appearance, including contrast levels and system colors.

## Technologies Used

- **Kotlin Multiplatform**: For developing the app for both Android and iOS.
- **Google Maps**: To display the location of recycling points.
- **Firebase**: For authentication, data storage, and real-time database functionality.
- **Material Design 3**: To ensure a consistent and modern UI across different devices.
- **Jetpack Compose**: For building the UI in a declarative way.

## Getting Started

### Prerequisites

- Android Studio or Xcode for development.
- Google account for Firebase integration.
- Firebase project set up with authentication, Firestore, and real-time database enabled.

### Installation

1. **Clone the repository**:
   ```sh
   git clone https://github.com/Billyflin/UfroSustentableApp.git
   cd UfroSustentableApp
   ```

2. **Set up Firebase**:
   - Follow the [Firebase documentation](https://firebase.google.com/docs/android/setup) to add Firebase to your Android and iOS projects.
   - Place the `google-services.json` (for Android) and `GoogleService-Info.plist` (for iOS) files in the appropriate directories.

3. **Build and Run**:
   - Open the project in Android Studio or Xcode.
   - Sync the project to ensure all dependencies are downloaded.
   - Build and run the app on your device or emulator.

## Usage

1. **Login**:
   - Open the app and tap on the "Login with Google" button.
   - Select your Google account to log in.

2. **Finding Recycling Points**:
   - Navigate to the map screen to see nearby recycling points.
   - Tap on a point to get more details.

3. **Scanning QR Codes**:
   - Go to a recycling point and scan the QR code using the QR scanner in the app.
   - Follow the prompts to complete the recycling request.

4. **Viewing History**:
   - Navigate to the history screen to see all your past recycling activities.

5. **Exchanging Rewards**:
   - Go to the rewards section to see the available rewards.
   - Exchange your points for rewards by selecting them from the list.

6. **Managing Profile**:
   - Visit your profile to update your information and customize the app’s appearance.

## Screenshots

![Login Screen](path_to_screenshot)
![Map Screen](path_to_screenshot)
![QR Scanner](path_to_screenshot)
![History Screen](path_to_screenshot)
![Rewards Screen](path_to_screenshot)
![Profile Screen](path_to_screenshot)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

- Thanks to the Universidad de La Frontera for supporting this project.
- Special thanks to the development team: Victor Leñam, Claudio Sáez, and Billy Martinez.

For more details, refer to the [Software Requirements Specification](path_to_SRS_document).
