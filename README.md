# Aro

Aro is an Android app for continuous emotion annotation. More specifically, after the user has
input some basic personal information, they are shown a series of videos. During each video they
have the opportunity to continuously specify their arousal and valence elicited by the content
on the screen by means of a circular control element at the edge of the screen. After each video,
the user rates their overall arousal and valence experienced during the video on a scale from
one to nine.

## Usage

Upon first launch, the user will be asked to give the app permission to read and write the phone's
external storage. This is required to read the video files which are stored on the phone's storage.
Once granted, the app will check wether the directory `Aro/` exists on the phone and if it contains
video files. If not, the directory will be created and the user is prompted to place video files
into this folder and restart the app. The same happens if the directory already exists but doesn't
contain any video files. Note that the app only looks for `*.mp4` files.

Once the user has placed the video files for the experiment into the `Aro/` folder and started the
app again, they are asked for a name (or user ID), their age and gender. Once filled in, the videos
will be played in alphabetical order, each one followed by a quick questionnare. Once all videos
were played and all questionnaires answered an ending screen is displayed.

After that, the files containing the collected data can be found in the phone's `Downloads/`
directory with the names `[user_id]_[age]_[gender]_values.csv` for the values recorded during the
videos and `[user_id]_[age]_[gender]_questionnaire.csv` with the data collected from the
questionnaires.

## Development

This app is being developed using Android Studio and can be imported straight into it. All
dependencies are declared in the corresponding `gradle` files. Please note that the minimum API
level for this app is set to 24 (Android Nougat 7.0) and requires the use of Java 8.
