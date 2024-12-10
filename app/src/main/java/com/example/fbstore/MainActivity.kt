package com.example.fbstore

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fbstore.ui.theme.FBstoreTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.File
import java.io.FileInputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        enableEdgeToEdge()
        setContent {
            FBstoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyScreen(db)
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

// 画像ファイル(ローカル)をCloud Storageにアップロードした後、uriをfirestoreに保存
fun StoreImage(context: Context, db: FirebaseFirestore) {
    var imageuri = ""
    val storage = Firebase.storage
    var storageRef = storage.reference
    // これはCloud Storageのファイルパス
    val ImageRef = storageRef.child("images/sky.jpg")
    // ローカル(仮想端末)のファイルパス
    val filePath = File(context.filesDir, "sky.jpg")
    val stream = FileInputStream(filePath)
    val uploadTask = ImageRef.putStream(stream)
    // 画像をCloudStorageにアップロード
    uploadTask.addOnFailureListener {
        // Handle unsuccessful uploads
    }.addOnSuccessListener { taskSnapshot ->
        ImageRef.downloadUrl.addOnSuccessListener { uri ->
            imageuri = uri.toString()//これがダウンロード用のurl
            AddTeamData(db, imageuri)
        }.addOnFailureListener {
            // Handle any errors
            Log.d("imageerror", "imageerror")
        }
        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
    }
}


