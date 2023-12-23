import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.appwrite.Appwrite
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import kotlinx.coroutines.launch
import org.jetbrains.skia.skottie.Logger

@Composable
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        var user by remember { mutableStateOf<Session?>(null) }
        var email by remember { mutableStateOf("anwar.hussen.pro@gmail.com") }
        var password by remember { mutableStateOf("34722645") }

        if (user != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Logged in as ${user!!.userId}")
                Button(onClick = {
                    coroutineScope.launch {
                        Appwrite.onLogout()
                        user = null
                    }
                }) {
                    Text("Logout")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                user = Appwrite.onLogin(email, password)
                            } catch (e: AppwriteException) {
                                e.printStackTrace()
                                println("${e.code}")
                            }
                        }
                    }) {
                        Text("Login")
                    }
                }
            }
        }
    }
}