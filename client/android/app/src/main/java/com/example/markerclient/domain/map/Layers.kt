package com.example.markerclient.domain.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.compose.style.layers.generated.SymbolLayerState
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource

@OptIn(MapboxDelicateApi::class)
@Composable
fun AddMarkLayer(id : String, geoJsonData : GeoJSONData) {

}

fun AddBufferLayer(geoJsonData: GeoJSONData) {

}