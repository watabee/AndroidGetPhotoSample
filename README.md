# AndroidGetPhotoSample

以下のことが行える Compose のサンプルアプリ。

- カメラアプリを起動し、カメラで撮影した画像をアプリで表示する
- 端末に保存された画像が選択でき、選択した画像をアプリで表示する

前者は `ActivityResultContracts.TakePicture` を使っており、後者は `ActivityResultContracts.PickVisualMedia` を使っている。

`ActivityResultContracts.TakePicture` を使った場合の問題点として、カメラアプリを起動する際に `Uri` を指定するが、この `Uri` はカメラアプリで撮影が完了した後に結果として受け取ることができないため、アプリ側で保持しておく必要がある。
カメラアプリを起動している間に呼び出し元の Activity が破棄される可能性があるため、この `Uri` を単に何かしらのインスタンス変数として保持することができない。
このサンプルではカメラアプリの呼び出し処理を `rememberTakePictureLauncher` に集約し `SavedStateRegistry` を使って `Uri` を保持できるようにしつつ、呼び出し側は簡単にカメラを起動できるようにしている。

`rememberTakePictureLauncher` は以下のように使う。

```kotlin
var imageUri: Uri by rememberSaveable { mutableStateOf(Uri.EMPTY) }
val takePicture = rememberTakePictureLauncher { uri ->
    if (uri != null) {
        imageUri = uri
    }
}

Button(
    onClick = {
        takePicture.launch(filename = "your-file-name.jpeg")
    }
) {
    Text(text = "Take picture")
}
```
