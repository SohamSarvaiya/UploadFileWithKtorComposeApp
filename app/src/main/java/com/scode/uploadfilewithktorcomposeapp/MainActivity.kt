package com.scode.uploadfilewithktorcomposeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scode.uploadfilewithktorcomposeapp.ui.theme.UploadFileWithKtorComposeAppTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UploadFileWithKtorComposeAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel {
                        UploadViewModel(
                            repository = FileRepository(
                                httpClient = HttpClient.client,
                                fileReader = FileReaders(
                                    context = applicationContext
                                )
                            )
                        )
                    }
                    val state = viewModel.state

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { contentUri ->
                        contentUri?.let {
                            viewModel.uploadFile(contentUri)
                        }
                    }

                    LaunchedEffect(key1 = state.errorMessage) {
                        state.errorMessage?.let {
                            Toast.makeText(
                                applicationContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    LaunchedEffect(key1 = state.isUploadComplete) {
                        if(state.isUploadComplete) {
                            Toast.makeText(
                                applicationContext,
                                "Upload completed!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            !state.isUploading -> {
                                Button(onClick = {
                                    filePickerLauncher.launch("*/*")
                                }, shape = RoundedCornerShape(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.teal_200)),
                                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)) {
                                    Text(text = "Choose a file", color = colorResource(id = R.color.white))
                                }
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = state.progress,
                                        animationSpec = tween(durationMillis = 100),
                                        label = "File upload progress bar"
                                    )
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        trackColor = colorResource(id = R.color.teal_700),
                                        color = colorResource(id = R.color.teal_200),
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .height(16.dp)
                                            .background(color = colorResource(id = R.color.teal_200))
                                    )
                                    Text(
                                        text = "${(state.progress * 100).roundToInt()}%",
                                        fontSize = 18.sp, color = colorResource(id = R.color.teal_200)
                                    )
                                    Button(onClick = {
                                        viewModel.cancelUpload()
                                    },shape = RoundedCornerShape(30.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.teal_200)),
                                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)) {
                                        Text(text = "Cancel upload",color = colorResource(id = R.color.white))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}