package com.example.fbstore

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fbstore.ui.theme.FBstoreTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        enableEdgeToEdge()
        setContent {
            FBstoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = this
                    Column(
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        //PickLocalImage(context, db)
                    }
                }
            }
        }
    }
}

// Teamデータをfirestoreに追加する
fun AddTeamData(db: FirebaseFirestore, imageuri: String) {
    var teamnum: Int = 0
    val user = hashMapOf(
        "id" to "2",
        "userName" to "Bob"
    )

    // usersへのデータの追加
    db.collection("team0")
        .document("users")
        .update("members", FieldValue.arrayUnion(user))
        .addOnSuccessListener {
            Log.d("Firestore", "Team successfully written!")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error writing team", e)
        }
    val image = Image(
        urI = "imageuri",
        userId = "1",
        userName = "John"
    )
    // imagesへのデータの追加
    db.collection("team${teamnum}")
        .document("images")
        .update("images", FieldValue.arrayUnion(image))
        .addOnSuccessListener { documentReference ->
        }
        .addOnFailureListener { e ->
        }
}

// ミッションデータをfirestoreに追加する
fun AddMissionData(db: FirebaseFirestore) {
    val reward: Int = 5
    val missionName: String = "mountain"
    db.collection("Missions")
        .document("${reward}Coins")
        .update("title", FieldValue.arrayUnion(missionName))
        .addOnSuccessListener { documentReference ->
        }
        .addOnFailureListener { e ->
        }
}


// Firestoreからデータを取得して表示するコンポーザブル関数
@Composable
fun MyScreen(db: FirebaseFirestore) {
    val coin: Int = 10
    val teamnum: Int = 0
    var Imagelist by remember { mutableStateOf<List<Image>>(emptyList()) }
    var Missionlist by remember { mutableStateOf<List<Missions>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) } // Add loading state

    db.collection("team${teamnum}")
        .document("images")
        .get()
        .addOnSuccessListener { result ->
            val imagesList = result.get("images") as? List<Map<String, Any>>
            Imagelist = imagesList?.map { imageMap ->
                Image(
                    urI = imageMap["urI"] as String,
                    userId = imageMap["userId"] as String,
                    userName = imageMap["userName"] as String
                )
            } ?: emptyList()
            isLoading = false // Data loaded, set loading to false
        }
        .addOnFailureListener { exception ->
            println("Error getting Image data: $exception")
            isLoading = false // Error occurred, set loading to false
        }

    db.collection("Missions")
        .document("${coin}Coins")
        .get()
        .addOnSuccessListener { result ->
            Missionlist = listOf(
                Missions(
                    MissionList = result["title"] as List<String>
                )
            )
            isLoading = false // Data loaded, set loading to false
        }


    if (isLoading) {
        Text("Loading data...")
    } else if (Imagelist.isEmpty()) {
        Text("No data")
    } else {
        // 取得したImageクラスインスタンスの情報を表示
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            items(Imagelist) { item ->
                Text(text = "Id:${item.userId}", modifier = Modifier.padding(8.dp))
                Text(
                    text = "Name:${item.userName}",
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(Missionlist) { item ->
                Text(text = "Mission:${item.MissionList}", modifier = Modifier.padding(8.dp))
            }
        }
    }
}


@Composable
fun PickLocalImage(context: Context, db: FirebaseFirestore, postmodel: PostModel, teamdata: TeamData) {
    val SelectedURI = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                // ここで取得したURIはローカルのファイルパス
                SelectedURI.value = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    val storageRef = Firebase.storage.reference
    // Cloud Storageへのファイルパス
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Button(onClick = {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Pick Image")
        }
    }
    if (SelectedURI.value != Uri.EMPTY) {
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        val inputStream = context.contentResolver.openInputStream(SelectedURI.value)
        if (inputStream != null) {
            val uploadTask = imageRef.putStream(inputStream)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("PickLocalImage", "Image uploaded: $downloadUri")
                }
            }.addOnFailureListener { exception ->
                Log.e("PickLocalImage", "Upload failed: ${exception.message}")
            }
        } else {
            Log.e("PickLocalImage", "Failed to open InputStream")
        }
    }
    // URIをfirestoreに保存
    AddTeamData(db, SelectedURI.value.toString())
}



@Preview
@Composable
fun PickLocalImagePreview() {
    Log.d("hoge3", "hoge3")
    val instance = Firebase.firestore
    PickLocalImage(
        context = LocalContext.current,
        db = instance,
        postmodel = PostModel(
            postId = "1",
            user = "user1",
            imageUrl = "",
            mission = "missionName",
            comments = listOf(
                "comment1",
                "comment2",
                "comment3"
            )
        ),
        teamdata = TeamData("team0", listOf("alice", "bob", "charlie"))
    )
}


