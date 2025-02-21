package com.example.jvcremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.jvcremote.ui.theme.JVCRemoteTheme

import kotlinx.coroutines.*


// Enum for operating commands (e.g., Power, HDMI)
enum class OperatingCommand(val code: String, val data: String) {
    POWER_ON("5057", "31"),  // Power on
    POWER_OFF("5057", "30"), // Power off
    HDMI_1("4950", "36"),    // Switch to HDMI 1
    HDMI_2("4950", "37")     // Switch to HDMI 2
}
// Enum for remote control commands (e.g., Standby, Info)
enum class RemoteControlCommand(val code: String) {
    STANDBY("37333036"),
    ON("37333035"),
    INPUT_MENU("37333038"),
    INFO("37333734"),
    ENV_SETTING("37333545"),
    LENS_CONTROL("37333330"),
    LENS_MEMORY("37334434"),
    LENS_APERTURE("37333230"),
    MPC("37334630"),
    P_ANALYSER("37333543"),
    BEFORE_AFTER("37334335"),
    HIDE("37333144"),
    UP("37333031"),
    DOWN("37333032"),
    LEFT("37333336"),
    RIGHT("37333334"),
    OK("37333246"),
    MENU("37333235"),
    BACK("37333033"),
    //FILM("37333639"),
    CINEMA("37333638"),
    ANIME("37333636"),
    NATURAL("37333641"),
    STAGE("37333637"),
    //PHOTO("37333842"),
    //THX("37333646"),
    USER_MODE("37334437"),
    THREE_D_SETTING("37334435"),
    ADVANCED_MENU("37333733"),
    GAMMA("37333735"),
    COLOR_TEMP("37333736"),
    COLOR_PROFILE("37333838"),
    PICTURE_ADJUST("37333732"),
}


class MainActivity : ComponentActivity() {
    private val projectorController = ProjectorController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JVCRemoteTheme {
                Surface(tonalElevation = 0.dp) {
                    RemoteControlScreen(
                        projectorController,
                    )
                }
            }
        }
    }
}

private fun sendCommand(
    controller: ProjectorController,
    isConnected: Boolean,
    feedback: MutableState<String>,
    coroutineScope: CoroutineScope,
    command: RemoteControlCommand
) {
    coroutineScope.launch {
        if (isConnected) {
            val success = controller.sendRemoteControlCommand(command)
            feedback.value = if (success) "${command.name} Sent" else "Failed to Send ${command.name}"
        } else {
            feedback.value = "Not connected"
        }
    }
}


@Composable
fun IPPortInputRow(
    ipAddress: String,
    port: String,
    onIPAddressChange: (String) -> Unit,
    onPortChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = ipAddress,
            onValueChange = onIPAddressChange,
            label = { Text("IP Address") },
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        OutlinedTextField(
            value = port,
            onValueChange = onPortChange,
            label = { Text("Port") },
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
fun OperatingCommandsRow(
    controller: ProjectorController,
    isConnected: Boolean,
    feedback: MutableState<String>,
    coroutineScope: CoroutineScope
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(
            OperatingCommand.POWER_ON to "Power On",
            OperatingCommand.POWER_OFF to "Power Off",
            OperatingCommand.HDMI_1 to "HDMI 1",
            OperatingCommand.HDMI_2 to "HDMI 2"
        ).forEach { (command, label) ->
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (isConnected) {
                            val success = controller.sendOperatingCommand(command)
                                feedback.value = if (success) "$label Sent" else "Failed to Send $label"
                        } else {
                            feedback.value = "Not connected"
                        }
                    }
                },
                modifier = Modifier.weight(1f).padding(4.dp)
            ) {
                Text(label)
            }
        }
    }
}
@Composable
fun RemoteControlGrid(
    controller: ProjectorController,
    isConnected: Boolean,
    feedback: MutableState<String>,
    coroutineScope: CoroutineScope
) {
    val remoteCommands = RemoteControlCommand.values().filter {
        it !in listOf(
            RemoteControlCommand.UP,
            RemoteControlCommand.DOWN,
            RemoteControlCommand.LEFT,
            RemoteControlCommand.RIGHT,
            RemoteControlCommand.OK
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(remoteCommands.size) { index ->
            val command = remoteCommands[index]

            Button(
                onClick = { sendCommand(controller, isConnected, feedback, coroutineScope, command) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(command.name.replace("_", " "))
            }
        }
    }
}


@Composable
fun CompassLayout(
    controller: ProjectorController,
    isConnected: Boolean,
    feedback: MutableState<String>,
    coroutineScope: CoroutineScope
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        Button(onClick = { sendCommand(controller, isConnected, feedback, coroutineScope, RemoteControlCommand.UP) }) {
            Text("▲")
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(onClick = { sendCommand(controller, isConnected, feedback, coroutineScope, RemoteControlCommand.LEFT) }) {
                Text("◄")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { sendCommand(controller, isConnected, feedback, coroutineScope, RemoteControlCommand.OK) }) {
                Text("OK")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { sendCommand(controller, isConnected, feedback, coroutineScope, RemoteControlCommand.RIGHT) }) {
                Text("►")
            }
        }

        Button(onClick = { sendCommand(controller, isConnected, feedback, coroutineScope, RemoteControlCommand.DOWN) }) {
            Text("▼")
        }
    }
}


@Composable
fun RemoteControlScreen(controller: ProjectorController) {
    var ipAddress by remember { mutableStateOf("10.0.0.33") }
    var port by remember { mutableStateOf("20554") }
    var isConnected by remember { mutableStateOf(false) }
    var feedback = remember { mutableStateOf("Not connected") }
    val coroutineScope = rememberCoroutineScope()

    // Auto-connect when the app is opened
    LaunchedEffect(Unit) {
        val result = controller.connect(ipAddress, port.toInt())
        isConnected = result
        feedback.value = if (result) "Connected to Projector" else "Connection Failed"
    }
    // Observe lifecycle events to trigger reconnection
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Try reconnecting when app regains focus
                coroutineScope.launch {
                    val result = controller.connect(ipAddress, port.toInt())
                    isConnected = result
                    feedback.value = if (result) "Connected to Projector" else "Connection Failed"
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // IP and Port Row
        IPPortInputRow(
            ipAddress = ipAddress,
            port = port,
            onIPAddressChange = { ipAddress = it },
            onPortChange = { port = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback text
        Text(
            feedback.value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Operating Commands Row
        OperatingCommandsRow(controller, isConnected, feedback, coroutineScope)

        Spacer(modifier = Modifier.height(16.dp))
        CompassLayout(controller, isConnected, feedback, coroutineScope)
        Spacer(modifier = Modifier.height(16.dp))
        // Remote Control Grid Layout
        RemoteControlGrid(controller, isConnected, feedback, coroutineScope)
    }
}

