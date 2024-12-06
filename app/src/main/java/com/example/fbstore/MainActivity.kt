package com.example.fbstore

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fbstore.ui.theme.FBstoreTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val db = Firebase.firestore
        enableEdgeToEdge()
        setContent {
            FBstoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val storage = Firebase.storage
                    var storageRef = storage.reference
                    val ImageRef = storageRef.child("images/image.jpg")
                    // ↓の処理でアプリが落ちる
                    val stream = FileInputStream("C:\\Users\\suzuk\\AndroidStudioProjects\\FBstore\\image.jpg")
                    val uploadTask = ImageRef.putStream(stream)
                    uploadTask.addOnFailureListener {
                        // Handle unsuccessful uploads
                    }.addOnSuccessListener { taskSnapshot ->
                        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                        // ...
                    }
                }
            }
        }
    }

    fun AddData(db: FirebaseFirestore, name: String, age: Int, nationality: String) {
        val user = hashMapOf(
            "name" to name,
            "age" to age,
            "nationality" to nationality
        )
        // データの追加
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                println("DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }


    @Composable
    fun MyScreen(db: FirebaseFirestore) {
        var datalist by remember { mutableStateOf<List<Sampledata>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) } // Add loading state

        LaunchedEffect(Unit) {
            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    datalist = result.documents.map { document ->
                        Sampledata(
                            name = document["name"] as String,
                            age = (document["age"] as Long).toInt(),
                            nationality = document["nationality"] as String
                        )
                    }
                    isLoading = false // Data loaded, set loading to false
                }
                .addOnFailureListener { exception ->
                    println("Error getting documents: $exception")
                    isLoading = false // Error occurred, set loading to false
                }
        }

        if (isLoading) { // Show loading indicator while loading
            Text("Loading data...")
        } else if (datalist.isEmpty()) {
            Text("No data")
        } else {
            LazyColumn {
                items(datalist) { item ->
                    Text(text = "Name:${item.name}", modifier = Modifier.padding(8.dp))
                    Text(text = "Age:${item.age}", modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Nationality:${item.nationality}",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}