package com.example.sceneviewtesting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import io.github.sceneview.Scene
import io.github.sceneview.SceneView
import io.github.sceneview.managers.color
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberIndirectLight
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberSkybox


/**
 * Scene View version 1.2.6
 */
@Composable
private fun SceneViewVersion_1_2_6() {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val nodes = remember { mutableStateListOf<Node>() }
    val cameraNode = rememberCameraNode(engine).apply {
        position = Position(z = 4.0f)
    }
    val mainLightNode = rememberMainLightNode(engine = engine).apply {
        position = Position(z = -4.0f)
    }
    val frontLight = rememberMainLightNode(engine = engine, creator = {
        LightNode(
            engine = engine,
            type = LightManager.Type.FOCUSED_SPOT,
            apply = {
                color(SceneView.DEFAULT_MAIN_LIGHT_COLOR)
                falloff(1000f)
                intensity(48_000_0.0f)
                direction(0f, 0f, -3f)
                position(0f, 0f, -3f)
            })
    })
    val indirectLight = rememberIndirectLight(engine = engine, creator = {
        IndirectLight.Builder()
            .intensity(100_000.0f)
            .build(engine)
    })
    LaunchedEffect(key1 = true) {
        modelLoader.loadModelInstanceAsync(
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
                    //nodes.add(frontLight)
                    nodes.add(node)
                }
            })
        /*ModelNode(
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/damaged_helmet.glb"
            ),
            scaleToUnits = 1.0f
        ).also { nodes.add(it) }*/
    }

    val transparentSkybox = rememberSkybox(engine = engine, creator = {
        Skybox.Builder()
            .color(0f, 0f, 0f, 0f)
            .build(engine)
    })


    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        //cameraNode = cameraNode,
        childNodes = nodes,
        mainLightNode = mainLightNode,
        indirectLight = indirectLight,
        skybox = null,
        onViewCreated = {
            this.scene.skybox = null
            this.setTranslucent(true)
        }
    )
}