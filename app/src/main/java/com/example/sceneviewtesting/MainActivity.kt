package com.example.sceneviewtesting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sceneviewtesting.ui.theme.SceneViewTestingTheme
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import io.github.sceneview.Scene
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.managers.color
import io.github.sceneview.math.Position
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberScene
import io.github.sceneview.rememberSkybox
import io.github.sceneview.rememberView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SceneViewTestingTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White) // Specify color here
                ) {
                    SceneViewBase()
                }
            }
        }
    }
}

val LightBackgroundColor = Color(0xFFFAFAFA)

@Preview
@Composable
private fun SceneViewBase() {
    val nodes = remember { mutableStateListOf<Node>() }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val mainLight = rememberMainLightNode(engine = engine)
    val view = rememberView(engine = engine)
    val renderer = rememberRenderer(engine)
    val scene = rememberScene(engine = engine)
    val materialLoader = rememberMaterialLoader(engine)
    val skybox = rememberSkybox(engine = engine, creator = {
        Skybox
            .Builder()
            .color(
                LightBackgroundColor.red,
                LightBackgroundColor.green,
                LightBackgroundColor.blue,
                1f
            )
            .build(engine)
    })
    var isTransparent by remember { mutableStateOf(false) }
    var isLightAdded by remember { mutableStateOf(false) }
    var currentModelNodeId by remember { mutableIntStateOf(0) }
    var playerModelNode by remember { mutableStateOf<ModelNode?>(null) }
    var helmetModelNode by remember { mutableStateOf<ModelNode?>(null) }

    val frontLightNode = LightNode(
        engine = engine,
        type = LightManager.Type.DIRECTIONAL,
        apply = {
            color(SceneView.DEFAULT_MAIN_LIGHT_COLOR)
            intensity(SceneView.DEFAULT_MAIN_LIGHT_COLOR_INTENSITY)
            direction(0f, 0f, -4.0f)
        })

    LoadModels(
        modelLoader = modelLoader,
        onHelmetModelLoaded = {
            helmetModelNode = it
        },
        onPlayerModelLoaded = {
            playerModelNode = it
            nodes.add(it) // Add the player model as initial model
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.padding(top = 150.dp, bottom = 200.dp),
            activity = LocalContext.current as? MainActivity,
            lifecycle = LocalLifecycleOwner.current.lifecycle,
            childNodes = nodes,
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            scene = scene,
            view = view,
            renderer = renderer,
            skybox = if (isTransparent) null else skybox,
            mainLightNode = mainLight,
            onViewCreated = {
                setTranslucent(isTransparent)
            },
            onViewUpdated = {
                setTranslucent(isTransparent)
            }
        )

        SceneSettings(
            modifier = Modifier.align(Alignment.BottomEnd),
            onTransparentBackgroundColor = {
                isTransparent = !isTransparent
            },
            onAddLight = {
                if (isLightAdded) {
                    nodes.remove(frontLightNode)
                } else {
                    nodes.add(frontLightNode)
                }
                isLightAdded = !isLightAdded
            },
            onChangeModel = {
                when (currentModelNodeId) {
                    0 -> {
                        helmetModelNode?.let {
                            nodes.remove(playerModelNode!!)
                            nodes.add(it)
                            currentModelNodeId = 1
                        }
                    }

                    1 -> {
                        playerModelNode?.let {
                            nodes.remove(helmetModelNode!!)
                            nodes.add(it)
                            currentModelNodeId = 0
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SceneSettings(
    modifier: Modifier = Modifier,
    onTransparentBackgroundColor: () -> Unit,
    onAddLight: () -> Unit,
    onChangeModel: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp, vertical = 24.dp)
    ) {
        Button(
            modifier = modifier
                .fillMaxWidth(),
            onClick = onTransparentBackgroundColor
        ) {
            Text(text = "Transparent BG color")
        }
        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = onAddLight
        ) {
            Text(text = "Add light")
        }
        Button(
            modifier = modifier.fillMaxWidth(), onClick = onChangeModel
        ) {
            Text(text = "Change model")
        }
    }
}

@Composable
fun LoadModels(
    modelLoader: ModelLoader,
    onHelmetModelLoaded: (ModelNode) -> Unit,
    onPlayerModelLoaded: (ModelNode) -> Unit
) {
    LaunchedEffect(key1 = true) {
        /*modelLoader.loadModelInstanceAsync(
            "https://github.com/JohanSkoett/glb_tests/raw/main/untitled2.glb",
            onResult = { modelInstance ->
                modelInstance?.let {
                    val node = ModelNode(
                        modelInstance = it,
                        scaleToUnits = 2.5f
                    ).apply {
                        transform(
                            position = Position(z = -4f, x = -0.1f, y = -0.3f),
                            rotation = Rotation(x = 0f)
                        )
                    }
                    playerModelNode = node
                }
            })*/

        ModelNode(
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/player.glb"
            ),
            scaleToUnits = 2.5f
        ).apply {
            transform(
                position = Position(z = -4f, x = -0.1f, y = -0.3f),
            )
            onPlayerModelLoaded(this)
        }

        ModelNode(
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/damaged_helmet.glb"
            ),
            scaleToUnits = 1f
        ).apply {
            transform(
                position = Position(z = -1f, x = 0f, y = 0f),
            )
            onHelmetModelLoaded(this)
        }
    }
}