package com.example.markerclient.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import com.example.markerclient.R
import com.example.markerclient.domain.map.GetExampleListOfFeatures
import com.mapbox.geojson.Feature
import com.mapbox.maps.InteractionContext
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.rememberStyleState
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.interactions.FeatureState
import com.mapbox.maps.interactions.FeaturesetFeature
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.markerclient.domain.CustomDismissCardViewModel
import com.example.markerclient.domain.NavigationTrailingButtonViewModel
import com.example.markerclient.ui.components.MarkContent
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mapbox.maps.extension.style.expressions.dsl.generated.properties
import com.mapbox.maps.plugin.viewport.ViewportStatus
import org.json.JSONArray
import com.example.markerclient.domain.Mark
import com.example.markerclient.domain.MarkViewModel
import com.google.gson.JsonParser

@OptIn(MapboxDelicateApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modifier : Modifier,
              mapViewportState : MapViewportState,
              coroutineScope : CoroutineScope,
              markViewModel : MarkViewModel,
              navigationTrailingButtonViewModel: NavigationTrailingButtonViewModel,
              customDismissCardViewModel : CustomDismissCardViewModel
) {
    val viewportStatus by remember { derivedStateOf { mapViewportState.mapViewportStatus}}

    // Get Resource variables
    val MARKER_GEOJSON_SOURCE_ID : String = stringResource(R.string.map_marks_geojson_source_id)
    val MARKER_LAYER_ID : String = stringResource(R.string.map_marks_layer_id)
    val MARKER_LAYER_COLOR : Color = colorResource(R.color.mark_circle_color)
    val MARKER_LAYER_CIRCLE_RADIUS : Double = dimensionResource(R.dimen.mark_circle_radius).value.toDouble()

    val MARKER_BUFFER_GEOJSON_SOURCE_ID : String = stringResource(R.string.map_marks_buffer_geojson_source_id)
    val MARKER_BUFFER_LAYER_ID : String = stringResource(R.string.map_marks_buffer_layer_id)
    val MARKER_BUFFER_LAYER_COLOR : Color = colorResource(R.color.mark_buffer_circle_color)
    val MARKER_BUFFER_LAYER_CIRCLE_RADIUS : Double = dimensionResource(R.dimen.mark_buffer_circle_radius).value.toDouble()

    val mapCoroutineScope = coroutineScope

    // Initialize mapState
    val mapState = rememberMapState {}

    // get marks from the repository
    val marks by markViewModel.marks.collectAsState()
    // get features from the repository
    val markerFeatures by markViewModel.markFeatures.collectAsState()
    // get buffer marks from the repository
    val bufferMarks by markViewModel.bufferMarks.collectAsState()
    // get buffer features from the repository
    val markerBufferFeatures by markViewModel.bufferMarkFeatures.collectAsState()

    val markerGeoJsonSource = remember(markerFeatures) {
        GeoJsonSourceState(
            sourceId = MARKER_GEOJSON_SOURCE_ID
        ).apply {
            data = GeoJSONData(markerFeatures)
        }
    }

    val markerBufferGeoJsonSource = remember(markerBufferFeatures) {
        GeoJsonSourceState(
            sourceId = MARKER_BUFFER_GEOJSON_SOURCE_ID
        ).apply {
            data = GeoJSONData(markerBufferFeatures)
        }
    }

    var selectedFeatureState by remember { mutableStateOf<FeaturesetFeature<FeatureState>?>(null) }
    var showMarkCard by remember { mutableStateOf(false) }

    // View model for the currently selected mark
    var selectedMark: Mark? by remember { mutableStateOf(Mark()) }

    // Set Navigation FAB
    LaunchedEffect(Unit) {
        navigationTrailingButtonViewModel.updateButton(
            visible = true,
            icon = Icons.Filled.MyLocation,
            action = {
                mapViewportState
                    .transitionToFollowPuckState(mapViewDefaultFollowPuckViewportStateOptions())
            }
        )
    }

    // update FAB visibility
    LaunchedEffect(viewportStatus) {
        if ( viewportStatus == ViewportStatus.Idle) {
            navigationTrailingButtonViewModel.updateButton(visible = true)
        }
        else{
            navigationTrailingButtonViewModel.updateButton(visible = false)
        }
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        mapState = mapState,
        style = {
            MapStyle(
                style = Style.STANDARD,
                styleState = rememberStyleState{
                    styleInteractionsState
                        .onMapLongClicked { interactionContext ->
                            handleMapLongClickInteraction(interactionContext, markViewModel, markerBufferFeatures)
                            true
                        }
                        .onLayerClicked(id = MARKER_LAYER_ID) { featureSetFeatureState, interactionContext ->
                            selectedFeatureState = featureSetFeatureState
                            true
                        }
                        .onLayerClicked(id = MARKER_BUFFER_LAYER_ID) { featureSetFeatureState, interactionContext ->
                            selectedFeatureState = featureSetFeatureState
                            true
                        }
                }
            )

            // Add layer for buffer markers
            CircleLayer(
                sourceState = markerBufferGeoJsonSource,
                layerId = MARKER_BUFFER_LAYER_ID
            ) {
                circleColor = ColorValue(MARKER_BUFFER_LAYER_COLOR)
                circleRadius = DoubleValue(MARKER_BUFFER_LAYER_CIRCLE_RADIUS)
            }

            // Add layer for persistent markers
            CircleLayer(
                sourceState = markerGeoJsonSource,
                layerId = MARKER_LAYER_ID
            ) {
                circleColor = ColorValue(MARKER_LAYER_COLOR)
                circleRadius = DoubleValue(MARKER_LAYER_CIRCLE_RADIUS)
            }
        }
    ){
        MapEffect(Unit) { mapView ->
            mapView.location.updateSettings{
                locationPuck = createDefault2DPuck(withBearing = true)
                enabled = true
                puckBearing = PuckBearing.HEADING
                puckBearingEnabled = true
            }
            // Follow user location puck
            mapViewportState.transitionToFollowPuckState(
                mapViewDefaultFollowPuckViewportStateOptions()
            )
        }

        // if we selected a feature, load the modal bottom sheet
        LaunchedEffect(selectedFeatureState) {
            showMarkCard = selectedFeatureState != null
        }

        // set displaying the bottom sheet to true
        LaunchedEffect(showMarkCard) {
            if (showMarkCard && selectedFeatureState != null){
                selectedMark = markViewModel.getMarkById(
                    selectedFeatureState!!.properties.getLong("markId")
                )

                selectedMark?.let {
                    customDismissCardViewModel.update(visible = true,
                        content = {
                            MarkContent(mark = selectedMark!!, markViewModel = markViewModel)
                        },
                        rtlDismiss = true,
                        ltrDismiss = true ,
                        onDismiss = {
                            selectedFeatureState =  null
                            showMarkCard = false
                            customDismissCardViewModel.reset()

                            markViewModel.updateMark(selectedMark!!)

                            Log.d("MapScreen", "MarkCard Dismiss")
                        }
                    )
                }
            }
        }
    }
}

fun handleMapLongClickInteraction(interactionContext : InteractionContext, markViewModel: MarkViewModel, currentFeatures: List<Feature>) {
    // add feature to all marker features
    val bufferFeature = Feature.fromGeometry(
        interactionContext.coordinateInfo.coordinate
    ).apply {
        addNumberProperty(Mark.getDefaultFeatureMarkIdKey(), Mark.getDefaultMarkId())
        addStringProperty(Mark.getDefaultFeatureTitleKey(), Mark.getDefaultMarkTitle())
        addStringProperty(Mark.getDefaultFeatureDescKey(), Mark.getDefaultMarkDesc())
    }

    val newBufferMark = Mark().update(
        isBuffer = true,
        feature = bufferFeature
    )

    markViewModel.addMark(newBufferMark)
}

fun handleMarkerBufferClicked(featureSetFeatureState : FeaturesetFeature<FeatureState>, interactionContext : InteractionContext) {
    for (key in featureSetFeatureState.properties.keys()) {
        Log.d("Key", featureSetFeatureState.properties.get(key).toString())
    }
}

fun mapViewDefaultFollowPuckViewportStateOptions() : FollowPuckViewportStateOptions {
    return FollowPuckViewportStateOptions.Builder()
        .pitch(0.0)
        .build()
}