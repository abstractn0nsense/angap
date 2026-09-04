package com.angap.photosendguide

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.angap.photosendguide.ui.theme.PhotoSendGuideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoSendGuideTheme {
                PhotoSendGuideApp()
            }
        }
    }
}

@Composable
private fun PhotoSendGuideApp() {
    var isGuideOpen by rememberSaveable { mutableStateOf(false) }
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }

    if (isGuideOpen) {
        PhotoSendingGuide(
            currentStepIndex = currentStepIndex,
            onPrevious = { currentStepIndex-- },
            onNext = { currentStepIndex++ },
            onClose = {
                currentStepIndex = 0
                isGuideOpen = false
            },
        )
    } else {
        HomeScreen(onStart = { isGuideOpen = true })
    }
}

@Composable
private fun HomeScreen(onStart: () -> Unit) {
    var showPermissionExplanation by rememberSaveable { mutableStateOf(false) }
    var showMessagingAppUnavailable by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "사진 보내기 도우미",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "사진을 보내는 방법을 차근차근 안내해 드립니다.",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.size(32.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "사진 보내기 시작",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        OutlinedButton(
            onClick = { showPermissionExplanation = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "다른 앱 위에서 안내 보기",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (showPermissionExplanation) {
            OverlayPermissionDialog(
                onDismiss = { showPermissionExplanation = false },
                onMessagingAppUnavailable = { showMessagingAppUnavailable = true },
            )
        }

        if (showMessagingAppUnavailable) {
            AlertDialog(
                onDismissRequest = { showMessagingAppUnavailable = false },
                title = { Text("메시지 앱을 열 수 없어요") },
                text = { Text("휴대전화에 기본 메시지 앱이 설정되어 있는지 확인한 뒤 다시 시도해 주세요.") },
                confirmButton = {
                    Button(onClick = { showMessagingAppUnavailable = false }) {
                        Text("확인")
                    }
                },
            )
        }
    }
}

@Composable
private fun OverlayPermissionDialog(
    onDismiss: () -> Unit,
    onMessagingAppUnavailable: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isPermissionGranted = Settings.canDrawOverlays(context)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("다른 앱 위에 안내를 표시할까요?") },
        text = {
            Text(
                if (isPermissionGranted) {
                    "안내창을 표시합니다. 언제든 안내창의 닫기 버튼으로 끝낼 수 있습니다."
                } else {
                    "메시지 앱을 보면서 안내를 받으려면 ‘다른 앱 위에 표시’ 권한이 필요합니다. 메시지나 사진 내용은 읽지 않습니다."
                },
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isPermissionGranted) {
                        context.startService(Intent(context, GuideOverlayService::class.java))
                        onDismiss()
                        if (!openMessagingApp(context)) {
                            context.stopService(Intent(context, GuideOverlayService::class.java))
                            onMessagingAppUnavailable()
                        }
                    } else {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}"),
                            ),
                        )
                    }
                },
            ) {
                Text(if (isPermissionGranted) "안내 시작" else "권한 설정")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

private fun openMessagingApp(context: Context): Boolean {
    return try {
        context.startActivity(
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
        )
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

@Composable
private fun PhotoSendingGuide(
    currentStepIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    val step = guideSteps[currentStepIndex]
    val isFirstStep = currentStepIndex == 0
    val isLastStep = currentStepIndex == guideSteps.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "사진 보내기",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "${currentStepIndex + 1} / ${guideSteps.size} 단계",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.size(36.dp))
            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = step.description,
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isLastStep) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("안내 마치기", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("다음", style = MaterialTheme.typography.titleLarge)
                }
            }

            if (!isFirstStep) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("이전", style = MaterialTheme.typography.titleLarge)
                }
            }
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("처음으로", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoSendGuideAppPreview() {
    PhotoSendGuideTheme {
        HomeScreen(onStart = {})
    }
}
